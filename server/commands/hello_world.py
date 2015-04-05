import _common


def define(command_definition_builder):
        assert isinstance(command_definition_builder, _common.CommandDefinitionBuilder)
        command_definition_builder\
            .title("Greetings")\
            .about("Hello World Greeting test script")\
            .text_arg(True, "name", "Your name goes here. So server will say Hello to you")


def execute(context, args_map, log):
        log.info("Hello "+args_map["name"]+" ! I`m pybee server")
        assert isinstance(context, _common.ExecutionContext)
        context.message("Say Hello", "Hello "+args_map["name"]+" ! I`m pybee server")
        # context.stop("Test stop functionality")
