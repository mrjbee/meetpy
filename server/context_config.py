# -*- coding: utf-8 -*-
from common.context import Context, RequestExecutor
from services import services_settings
from services import services_commands
from services import services_threads
from features import features_fs, features_commands

SM = services_settings.SettingManager()

class ServerContext(Context):

    def configure(self):
        self.required_service(SM)
        self.required_service(services_threads.ThreadManager())
        self.required_service(services_commands.CommandManger())
        self.required_feature(features_fs.GetAvailablePlaces())
        self.required_feature(features_fs.ExploreFiles())
        self.required_feature(features_commands.ListCommands())
        self.required_feature(features_commands.GetCommandDetails())
        self.required_feature(features_commands.ExecuteCommand())


_context = None
_request_executor = None


def context():
    global _context
    if _context is None:
        _context = ServerContext()
        _context.load()
    assert isinstance(_context, ServerContext)
    return _context


def request_executor():
    global _request_executor
    if _request_executor is None:
        _request_executor = RequestExecutor(context())
    assert isinstance(_request_executor, RequestExecutor)
    return _request_executor


if __name__ == '__main__':
    cm = context().get_service(features_commands.CommandManger)
    assert isinstance(cm, features_commands.CommandManger)