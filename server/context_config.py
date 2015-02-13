# -*- coding: utf-8 -*-
from common.context import Context, RequestExecutor
from services import settings
from services import commands
from features import fs, command

SM = settings.SettingManager()

class ServerContext(Context):

    def configure(self):
        self.required_service(SM)
        self.required_service(commands.CommandManger())
        self.required_feature(fs.GetAvailablePlaces())
        self.required_feature(fs.ExploreFiles())
        self.required_feature(command.ListCommands())
        self.required_feature(command.GetCommandDetails())
        self.required_feature(command.ExecuteCommand())


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
    cm = context().get_service(command.CommandManger)
    assert isinstance(cm, command.CommandManger)