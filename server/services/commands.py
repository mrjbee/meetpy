import os, traceback
from common.context import Service
from common import utils
from common.utils import log_execution

class CommandManger (Service):

    def __init__(self):
        super(CommandManger, self).__init__(CommandManger)
        self._command_map = {}

    def init(self):
        self._loadCommands()

    def execute_command(self, command_id, arguments):
        log_execution().info("Command execution: %s (%s)", command_id, arguments)
        command = self._command_map.get(command_id)
        command_result = utils.Object()
        command_result.is_success = True
        command_result.result_details = []
        command_result.details = None
        try:
            result = command.execute_internal(arguments)
            log_execution().info("Command executed: %s", command_id)
            command_result.result_details = []
            for res in result.results():
                command_result.result_details.append(res.to_map())
        except Exception as error:
            command_result.is_success = False
            command_result.details = error.message
            error_trace = traceback.format_exc()
            log_execution().exception("Command execution failed: %s", command_id)
            command_result.result_details = [{"type": "message", "trace": error_trace}]
        return command_result

    def is_command_short_execution(self, command_id):
        command = self._command_map.get(command_id)
        return command.short_execution()

    def command(self, command_name):
        command = self._command_map.get(command_name)
        if command is None:
            return None
        definition = command.build_definition()
        args_list = []
        for arg in definition._arg_lst:
            args_list.append(arg.build_definition_map())
        return {"id": command_name, "title": definition._title, "about": definition._about, "args": args_list}

    def commands(self):
        answer = []
        for key, value in self._command_map.items():
            definition = value.build_definition()
            answer.append({"id": key, "title": definition._title, "about": definition._about})
        return answer

    def _loadCommands(self):
        commands_files = os.listdir("commands")
        for command in commands_files:
            if command.startswith("_") is False and command.endswith(".py"):
                name = os.path.splitext(command)[0]
                module = __import__('commands.'+name, fromlist=['Command'])
                instance = getattr(module, 'Command')()
                self._command_map[name] = instance