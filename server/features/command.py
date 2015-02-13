from services import settings
from services import commands
from common import utils
from common.context import Feature


def _command_map_to_object(command_map):
    comm = utils.Object()
    comm.id = command_map["id"]
    comm.title = command_map["title"]
    comm.about = command_map["about"]
    return comm


class GetCommandDetails(Feature):
    def __init__(self):
        super(GetCommandDetails, self).__init__(GetCommandDetails)

    def execute(self, command_id):
        cm = self.service(commands.CommandManger)
        assert isinstance(cm, commands.CommandManger)
        command_map = cm.command(command_id)
        if command_map is None:
            return None
        command = _command_map_to_object(command_map)
        command.arguments = command_map["args"]
        return command


class ListCommands(Feature):

    def __init__(self):
        super(ListCommands, self).__init__(ListCommands)

    def execute(self, lang="en"):
        cm = self.service(commands.CommandManger)
        assert isinstance(cm, commands.CommandManger)
        answer = []
        for command_map in cm.commands():
            answer.append(_command_map_to_object(command_map))
        return answer


class ExecuteCommand(Feature):
    def __init__(self):
        super(ExecuteCommand, self).__init__(ExecuteCommand)

    def execute(self, command_task):
        cm = self.service(commands.CommandManger)
        assert isinstance(cm, commands.CommandManger)
        if cm.is_command_short_execution(command_task.id):
            answer_object = cm.execute_command(command_task.id, command_task.args)
            return {
                "success": answer_object.is_success,
                "details": answer_object.details,
                "results": answer_object.result_details
            }
        else:
            raise ValueError("long term commands execution not implemented")