# -*- coding: utf-8 -*-
from common.context import Context, RequestExecutor
from services import space_settings
from services import space_commands
from services import space_threads
from uc import user_cases_fs, user_cases_commands

SM = space_settings.SettingManager()

class ServerContext(Context):

    def configure(self):
        self.required_service(SM)
        self.required_service(space_threads.ThreadManager())
        self.required_service(space_commands.CommandManger())
        self.required_feature(user_cases_fs.GetAvailablePlaces())
        self.required_feature(user_cases_fs.ExploreFiles())
        self.required_feature(user_cases_commands.ListCommands())
        self.required_feature(user_cases_commands.GetCommandDetails())
        self.required_feature(user_cases_commands.ExecuteCommand())


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
    cm = context().get_service(user_cases_commands.CommandManger)
    assert isinstance(cm, user_cases_commands.CommandManger)