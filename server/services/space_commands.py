import json
import os, traceback, uuid
from common.context import Service
from common.utils import log_execution
import common.command
from services import space_threads
from services import space_settings


def _build_command_definition(command, sm):
    assert isinstance(command, CommandMethods)
    builder = common.command.SignatureBuilder(sm.context_env())
    command.method_define(builder)
    return builder


class CommandManger (Service):

    def __init__(self, sm):
        super(CommandManger, self).__init__(CommandManger)
        self._command_map = {}
        self.sm = sm
        assert isinstance(self.sm, space_settings.SettingManager)

    def init(self):
        self._loadCommands()

    def execute_command(self, command_id, arguments, log=log_execution(), obsolete = None):
        log_execution().info("Command execution: %s (%s)", command_id, arguments)
        command = self._command_map.get(command_id)
        assert isinstance(command, CommandMethods)
        answer = CommandExecutionResult()
        try:

            context = common.command.CommandExecutionContext(self.sm.context_env())

            command.method_execute(context, arguments, log_execution())
            log.info("Command executed: %s", command_id)
            answer.is_success = True
            answer.results = context.result_as_map()
            # Deals with tasks
            for task in context._tasks:
                answer.sub_execution_list.append(
                    CommandTaskExecution(common.command.TaskExecutionContext(self.sm.context_env()), task))

        except Exception as error:
            log_execution().exception("Command execution failed: %s", command_id)
            log_execution().exception(error)
            answer.is_success = False
            answer.error_msg = error.args[0]
            error_trace = traceback.format_exc()
            answer.error_trace = error_trace

        return answer

    def command(self, command_name):
        command = self._command_map.get(command_name)
        if command is None:
            return None
        definition = _build_command_definition(command, self.sm)
        return {"id": command_name,
                "title": definition._title,
                "about": definition._about,
                "action_name": definition._action_name,
                "args": definition.args_as_map()}

    def commands(self):
        answer = []
        for key, value in list(self._command_map.items()):
            definition = _build_command_definition(value, self.sm)
            answer.append({"id": key, "title": definition._title, "about": definition._about})
        return answer

    def _loadCommands(self):
        sm = self.sm
        assert isinstance(sm, space_settings.SettingManager)
        command_dir = sm.command_dir()
        if not os.path.exists(command_dir):
            return
        commands_files = os.listdir(command_dir)
        for command in commands_files:
            if command.startswith("_") is False and command.endswith(".py"):
                name = os.path.splitext(command)[0]
                module = __import__(command_dir+'.'+name, fromlist=['define, execute, async'])
                instance = CommandMethods()
                instance.method_define = getattr(module, 'define')
                instance.method_execute = getattr(module, 'execute')
                self._command_map[name] = instance

    def load_task_details(self, task_folder, task_id):
        file_path = os.path.join(task_folder, task_id+'.task.json')
        if os.path.exists(file_path) is False:
            return None
        with open(file_path, 'r') as in_file:
            data = json.load(in_file)
        return data


class CommandExecutionResult(object):

    def __init__(self):
        super(CommandExecutionResult, self).__init__()
        self.is_success = True
        self.error_msg = None
        self.error_trace = None
        self.results = None
        self.sub_execution_list = []


class CommandMethods(object):
    def __init__(self):
        super(CommandMethods, self).__init__()
        self.method_define = None
        self.method_execute = None


class CommandTaskExecution(space_threads.ThreadRunnable):

    def __init__(self, context, task):
        super(CommandTaskExecution, self).__init__()
        assert isinstance(context, common.command.TaskExecutionContext)
        assert isinstance(task, common.command.Task)
        self._context = context
        self._task = task
        self.persist_dir = None
        self._status = "awaiting"
        self._error_msg = ""
        self._error_trace = ""
        self._context._observe_method = self._on_context_change


    def run(self):
        self._log().debug("Scheduled task started = "+self._task.id)
        self._status = "running"
        self.persist()

        try:
            self._task.execute(self._context, self._log())
            self._log().debug("Scheduled task executed successfully = "+self._task.id)
            self._status = "done"
            self._context.progress(1)
        except Exception as error:
            self._status = "error"
            self._log().debug("Scheduled task executed with error = "+self._task.id+" msg = "+error.args[0])
            self._log().exception(error)
            error_trace = traceback.format_exc()
            self._error_msg = error.args[0]
            self._error_trace = error_trace

        self.persist()

    def _on_context_change(self, context):
        self.persist()

    def _log(self):
        return log_execution(self._task.id)

    def persist(self):
        data = {"id": self._task.id,
                "title": self._task.title(),
                "description": self._task.description(),
                "results": self._context.result_as_map(),
                "status": self._status,
                "progress": self._context._progress,
                "error_msg": self._error_msg,
                "error_trace": self._error_trace}
        with open(os.path.join(self.persist_dir, self._task.id+'.task.json'), 'w') as outfile:
            json.dump(data, outfile)