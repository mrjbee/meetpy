import _common

__VERSION__ = 1


class Command (_common.CommandTemplate):

    def __init__(self):
        super(Command, self).__init__(__VERSION__)

    def define(self, command_definition_builder):
        assert isinstance(command_definition_builder, _common.CommandDefinitionBuilder)
        command_definition_builder\
            .title("Greetings")\
            .about("Hello World Greeting test script")\
            .text_arg(True, "name", "Your name goes here. So server will say Hello to you")

    def execute(self, result, args_map, log=_common.default_logger()):
        log.info("Hello "+args_map["name"]+" ! I`m pybee server")
        assert isinstance(result, _common.ResultDefinitionBuilder)
        #result.message("Say Hello", "Hello "+args_map["name"]+" ! I`m pybee server")
        #result.stop("Test stop functionality")

    def short_execution(self):
        return True