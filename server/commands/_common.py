from common import utils

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


class ResultDefinitionBuilder(object):

    def __init__(self):
        super(ResultDefinitionBuilder, self).__init__()
        self._result_list = []

    def message(self, title, msg):
        self._result_list.append(StringResult(title, msg))

    def results(self):
        return self._result_list

    def stop(self, description):
        raise StopExecutionError(description)


class CommandTemplate(object):
    def __init__(self, version):
        super(CommandTemplate, self).__init__()
        self._version = version

    def build_definition(self):
        builder = CommandDefinitionBuilder()
        self.define(builder)
        return builder

    def short_execution(self):
        return False

    def define(self, command_definition_builder):
        """
        :param command_definition_builder:CommandDefinitionBuilder
        :return: None
        """
        pass

    def execute_internal(self, args_map, log=utils.log_execution()):
        result = ResultDefinitionBuilder()
        self.execute(result, args_map, log)
        return result

    def execute(self, result, args_map, log):
        pass

    def version(self):
        return self._version