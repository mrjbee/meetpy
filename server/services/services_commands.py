import os, traceback, uuid
from common.context import Service
from common import utils
from common.utils import log_execution
from commands import _common
from services import services_threads

def _build_command_definition(command):
    assert isinstance(command, CommandMethods)
    builder = _common.CommandDefinitionBuilder()
    definition = command.method_define(builder)
    return definition


def _flag_async(command):
    assert isinstance(command, CommandMethods)
    return command.flag_async


class CommandManger (Service):

    def __init__(self):
        super(CommandManger, self).__init__(CommandManger)
        self._command_map = {}

    def init(self):
        self._loadCommands()

    def create_command_execution(self, command_id, arguments):
        log_execution().info("Preparing command for lazy execution: %s (%s)", command_id, arguments)
        command = self._command_map.get(command_id)
        assert isinstance(command, CommandMethods)
        context = _common.AsyncExecutionContext()
        lazy_execution = LazyCommandExecution(context)
        lazy_execution.run_method = command.method_execute
        lazy_execution.args = arguments
        return lazy_execution

    def execute_command(self, command_id, arguments):
        log_execution().info("Command execution: %s (%s)", command_id, arguments)
        command = self._command_map.get(command_id)
        assert isinstance(command, CommandMethods)
        command_result = utils.Object()
        command_result.is_success = True
        command_result.result_details = []
        command_result.details = None
        try:
            context = _common.ExecutionContext()
            command.method_execute(context, arguments, log_execution())
            log_execution().info("Command executed: %s", command_id)
            command_result.result_details = context._result_to_json()
        except Exception as error:
            command_result.is_success = False
            command_result.details = error.message
            error_trace = traceback.format_exc()
            log_execution().exception("Command execution failed: %s", command_id)
            command_result.result_details = [{"type": "message", "trace": error_trace}]
        return command_result

    def is_command_short_execution(self, command_id):
        command = self._command_map.get(command_id)
        return not(_flag_async(command))

    def command(self, command_name):
        command = self._command_map.get(command_name)
        if command is None:
            return None
        definition = self.request_command_definition(command)
        args_list = []
        for arg in definition._arg_lst:
            args_list.append(arg.build_definition_map())
        return {"id": command_name, "title": definition._title, "about": definition._about, "args": args_list}

    def commands(self):
        answer = []
        for key, value in self._command_map.items():
            definition = self.request_command_definition(value)
            answer.append({"id": key, "title": definition._title, "about": definition._about})
        return answer

    def _loadCommands(self):
        commands_files = os.listdir("commands")
        for command in commands_files:
            if command.startswith("_") is False and command.endswith(".py"):
                name = os.path.splitext(command)[0]
                module = __import__('commands.'+name, fromlist=['define, execute, async'])
                instance = CommandMethods()
                instance.method_define = getattr(module, 'define')
                instance.method_execute = getattr(module, 'execute')
                if hasattr(module, 'async'):
                    instance.flag_async = getattr(module, 'async')
                self._command_map[name] = instance


class CommandMethods(object):
    def __init__(self):
        super(CommandMethods, self).__init__()
        self.method_define = None
        self.method_execute = None
        self.flag_async = None


class LazyCommandExecution(services_threads.ThreadRunnable):

    def __init__(self, exec_context):
        super(LazyCommandExecution, self).__init__()
        self.context = exec_context
        self.run_method = None
        self.args = None
        self.id = str(uuid.uuid4())

    def run(self):
        try:
            self.run_method(self.context, self.args, log_execution(self.id))
        except _common.StopExecutionError:
            log_execution(self.id).info("Execution stopped")
        except Exception as error:
            log_execution(self.id).exception(error)


