import uuid
from common import utils
from common.utils import log_execution
from services import space_threads


def _to_json_list(list):
    answer = []
    for elem in list:
        answer.append(elem.as_map())
    return answer

class _ArgumentDefinition(object):
    def __init__(self, arg_type, required, arg_id, arg_about):
        super(_ArgumentDefinition, self).__init__()
        self._required = required
        self._type = arg_type
        self._id = arg_id
        self._about = arg_about

    def as_map(self):
        definition_map = {
            "id": self._id,
            "about": self._about,
            "required": self._required,
            "type": self._type}
        self.define_map(definition_map)
        return definition_map

    def define_map(self, def_map):
        pass


class TextArgumentDefinition(_ArgumentDefinition):
    def __init__(self, required, arg_id, arg_about):
        super(TextArgumentDefinition, self).__init__("text", required, arg_id, arg_about)
        self._len = -1

    def max_len(self, len_value):
        self._len = len_value
        return self

    def define_map(self, def_map):
        def_map["len"] = self._len


class SignatureBuilder(object):
    def __init__(self):
        super(SignatureBuilder, self).__init__()
        self._title = "Unknown"
        self._about = "Unknown"
        self._argument_builder = ArgumentBuilder()

    def title(self, value_string):
        self._title = value_string
        return self

    def about(self, value_string):
        self._about = value_string
        return self

    def args(self):
        return self._argument_builder

    def args_as_map(self):
        return _to_json_list(self._argument_builder._arg_lst)


class ArgumentBuilder(object):
    def __init__(self):
        super(ArgumentBuilder, self).__init__()
        self._arg_lst = []

    def text_arg(self, required, name, about):
        arg_definition = TextArgumentDefinition(required, name, about)
        self._arg_lst.append(arg_definition)
        return arg_definition


class _Result(object):
    def __init__(self, res_type, title):
        super(_Result, self).__init__()
        self._type = res_type
        self._title = title

    def as_map(self):
        answer = {"type": self._type, "title": self._title}
        self.fill_result(answer)
        return answer

    def fill_result(self, result_map):
        pass


class StringResult(_Result):
    def __init__(self, title, value):
        super(StringResult, self).__init__("message", title)
        self._value = value

    def fill_result(self, result_map):
        result_map["value"] = self._value


class StopExecutionError(RuntimeError):
    def __init__(self, msg):
        super(StopExecutionError, self).__init__(msg)


class ExecutionContext(object):

    def __init__(self):
        super(ExecutionContext, self).__init__()
        self._result_list = []

    def _result(self, result):
        self._result_list.append(result)

    def message(self, title, msg):
        self._result(StringResult(title, msg))

    def stop(self, description):
        raise StopExecutionError(description)

    def result_as_map(self):
        return _to_json_list(self._result_list)


class CommandExecutionContext(ExecutionContext):

    def __init__(self):
        super(CommandExecutionContext, self).__init__()
        self._arg_builder = None
        self._tasks = []

    def request_arguments(self):
        self._arg_builder = ArgumentBuilder()
        return self._arg_builder

    def sub_task(self, task):
        assert isinstance(task, Task)
        self._tasks.append(task)


class TaskExecutionContext(ExecutionContext):

    def __init__(self):
        super(TaskExecutionContext, self).__init__()
        self._progress = 0.0
        self._observe_method = None

    def _notify_changes(self):
        if self._observe_method:
            self._observe_method(self)

    def progress(self, value):
        self._progress = value
        self._notify_changes()

    def _result(self, result):
        super(TaskExecutionContext, self)._result(result)
        self._notify_changes()


class Task(object):

    def __init__(self):
        super(Task, self).__init__()
        self.id = str(uuid.uuid4())

    def execute(self, task_context, log):
        pass