import time
import _common

async = True


def define(command_definition_builder):
        assert isinstance(command_definition_builder, _common.CommandDefinitionBuilder)
        command_definition_builder\
            .title("Greetings [async]")\
            .about("Hello World Greeting test script which called in separate thread")\
            .text_arg(True, "name", "Your name goes here. So server will say Hello to you")


def execute(context, args_map, log):
        log.info("Hello "+args_map["name"]+" ! I`m pybee server")
        assert isinstance(context, _common.ExecutionContext)
        context.message("Say Hello", "Hello [before sleep] "+args_map["name"]+" ! I`m pybee server")
        time.sleep(15)
        context.message("Say Hello", "Hello [after sleep] "+args_map["name"]+" ! I`m pybee server")
        # context.stop("Test stop functionality")
