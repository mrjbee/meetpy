import uuid
from common import utils
from common.utils import log_execution
from services import services_threads


class _ArgumentDefinitionBuilder(object):
    def __init__(self, arg_type, required, arg_id, arg_about):
        super(_ArgumentDefinitionBuilder, self).__init__()
        self._required = required
        self._type = arg_type
        self._id = arg_id
        self._about = arg_about

    def build_definition_map(self):
        definition_map = {
            "id": self._id,
            "about": self._about,
            "required": self._required,
            "type": self._type}
        self.define_map(definition_map)
        return definition_map

    def define_map(self, def_map):
        pass


class TextArgumentDefinition(_ArgumentDefinitionBuilder):
    def __init__(self, required, arg_id, arg_about):
        super(TextArgumentDefinition, self).__init__("text", required, arg_id, arg_about)
        self._len = -1

    def max_len(self, len_value):
        self._len = len_value
        return self

    def define_map(self, def_map):
        def_map["len"] = self._len


class CommandDefinitionBuilder(object):
    def __init__(self):
        super(CommandDefinitionBuilder, self).__init__()
        self._title = "Unknown"
        self._about = "Unknown"
        self._arg_lst = []

    def title(self, value_string):
        self._title = value_string
        return self

    def about(self, value_string):
        self._about = value_string
        return self

    def text_arg(self, required, name, about):
        arg_builder = TextArgumentDefinition(required, name, about)
        self._arg_lst.append(arg_builder)
        return arg_builder


class _Result(object):
    def __init__(self, res_type, title):
        super(_Result, self).__init__()
        self._type = res_type
        self._title = title

    def to_map(self):
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
        self.on_change_observing = None

    def message(self, title, msg):
        self._result_list.append(StringResult(title, msg))
        self._notify_observer()

    def stop(self, description):
        raise StopExecutionError(description)

    def _result_to_json(self):
        answer = []
        for res in self._result_list:
            answer.append(res.to_map())
        return answer

    def _notify_observer(self):
        if self.on_change_observing:
            self.on_change_observing(self)


class AsyncExecutionContext(ExecutionContext):
    def __init__(self, command_id, args):
        super(AsyncExecutionContext, self).__init__()
        self.args = args
        self.id = str(uuid.uuid4())
        self.command_id = command_id

    def _to_json_map(self):
        return {"args": self.args, "results": self._result_to_json()}
