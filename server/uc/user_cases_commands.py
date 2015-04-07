import os
from services import space_settings
from services import space_commands
from services import space_threads
from common import utils
from common.context import Feature
from services.space_commands import CommandExecutionResult, CommandTaskExecution


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
        cm = self.service(space_commands.CommandManger)
        assert isinstance(cm, space_commands.CommandManger)
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
        cm = self.service(space_commands.CommandManger)
        assert isinstance(cm, space_commands.CommandManger)
        answer = []
        for command_map in cm.commands():
            answer.append(_command_map_to_object(command_map))
        return answer


class ExecuteCommand(Feature):
    def __init__(self):
        super(ExecuteCommand, self).__init__(ExecuteCommand)

    def execute(self, task_request=None):
        cm = self.service(space_commands.CommandManger)
        assert isinstance(cm, space_commands.CommandManger)

        answer_object = cm.execute_command(task_request.id, task_request.args)
        sm = self.service(space_settings.SettingManager)
        assert isinstance(sm, space_settings.SettingManager)
        tm = self.service(space_threads.ThreadManager)

        assert isinstance(answer_object, CommandExecutionResult)
        assert isinstance(tm, space_threads.ThreadManager)

        sub_tasks = []
        for task_execution in answer_object.sub_execution_list:
            assert isinstance(task_execution, CommandTaskExecution)
            task_execution.persist_dir = sm.task_dir()
            task_execution.persist()
            sub_tasks.append(task_execution._task.id)
            tm.post("main", task_execution)
        return {
            "success": answer_object.is_success,
            "error_msg": answer_object.error_msg,
            "results": answer_object.results,
            "sub_tasks": sub_tasks
        }


class GetTasksList(Feature):

    def __init__(self):
        super(GetTasksList, self).__init__(GetTasksList)

    def execute(self, args=None):

        sm = self.service(space_settings.SettingManager)
        assert isinstance(sm, space_settings.SettingManager)

        file_list = os.listdir(sm.task_dir())
        result = []
        for file in file_list:
            if file.endswith(".task.json"):
                result.append(file[0:-10])
        return result


class GetTaskDetails(Feature):

    def __init__(self):
        super(GetTaskDetails, self).__init__(GetTaskDetails)

    def execute(self, task_id=None):
        sm = self.service(space_settings.SettingManager)
        assert isinstance(sm, space_settings.SettingManager)

        cm = self.service(space_commands.CommandManger)
        assert isinstance(cm, space_commands.CommandManger)
        task_details = cm.load_task_details(sm.task_dir(), task_id)
        if task_details is None:
            return None
        return task_details
