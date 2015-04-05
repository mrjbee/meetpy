from common.beerest import ActionHandler, HandlerResponse, INVALID
import context_config
from common.context import RequestExecutor
from features import features_fs
from features.features_commands import ListCommands, GetCommandDetails, ExecuteCommand
from common import utils

executor = context_config.request_executor()


class VersionHandler(ActionHandler):
    def do_get(self, id_map, header_map):
        response = HandlerResponse()
        response.write_json({
            "version": "0.1"
        })
        return response


class Places(ActionHandler):
    def do_get(self, id_map, header_map):
        global executor
        assert isinstance(executor, RequestExecutor)
        places = executor.execute(features_fs.GetAvailablePlaces)
        respond_json = []
        for place in places:
            place_map = {"name": place.name, "folder": place.folder}
            respond_json.append(place_map)
        response = HandlerResponse()
        response.write_json(respond_json)
        return response


class Files(ActionHandler):
    def do_get(self, id_map, header_map):
        global executor
        parent_path = header_map.get("path", None)
        if parent_path is None:
            return INVALID
        assert isinstance(executor, RequestExecutor)
        sub_files = executor.execute(features_fs.ExploreFiles, parent_path)
        if sub_files is None:
            return None
        respond_json = []
        for child in sub_files:
            file_map = {"path": child.path, "is_file": child.is_file, "size": child.size}
            respond_json.append(file_map)
        response = HandlerResponse()
        response.write_json(respond_json)
        return response


class Commands(ActionHandler):
    def do_get(self, id_map, header_map):
        global executor
        assert isinstance(executor, RequestExecutor)
        command_list = executor.execute(ListCommands)
        if len(command_list) == 0:
            return None
        respond_json = []
        for command in command_list:
            command_map = {"id": command.id, "title": command.title, "about": command.about}
            respond_json.append(command_map)
        response = HandlerResponse()
        response.write_json(respond_json)
        return response


class CommandDetails(ActionHandler):
    def do_get(self, id_map, header_map):
        global executor
        assert isinstance(executor, RequestExecutor)
        command_detail = executor.execute(GetCommandDetails, id_map["command_id"])
        if command_detail is None:
            return None
        command_map = {"id": command_detail.id,
                       "title": command_detail.title,
                       "about": command_detail.about,
                       "args": command_detail.arguments}
        response = HandlerResponse()
        response.write_json(command_map)
        return response


class CreateCommandTask(ActionHandler):

    def do_post(self, id_map, header_map, body):

        command_task = utils.Object()
        command_task.id = body.get("command_id")
        command_task.args = body.get("args")

        global executor
        assert isinstance(executor, RequestExecutor)
        result = executor.execute(ExecuteCommand, command_task)

        response = HandlerResponse()
        response.write_json(result)
        return response