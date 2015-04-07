import time
import _common


def define(signature_builder):
        assert isinstance(signature_builder, _common.SignatureBuilder)
        signature_builder\
            .title("Greetings Async")\
            .about("Hello World Greeting test script in separate thread")\
            .args()\
            .text_arg(True, "name", "Your name goes here. So server will say Hello to you")


def execute(context, args_map, log):
        log.info("Hello "+args_map["name"]+" ! I`m pybee server")
        assert isinstance(context, _common.CommandExecutionContext)
        hello_task = SayHelloTask(args_map["name"])
        context.message("Before anything", "Hey dude,"+args_map["name"]+"! I`ll will say hello later in a task")
        context.sub_task(hello_task)


class SayHelloTask (_common.Task):

    def __init__(self, to_name):
        super(SayHelloTask, self).__init__()
        self._name = to_name


    def title(self):
        return "Hello To ["+self._name+"]"

    def execute(self, context, log):
        log.info("Hello "+self._name+" ! I`m pybee server")
        assert isinstance(context, _common.TaskExecutionContext)
        context.progress(0.1)
        time.sleep(15)
        context.message("Say Hello", "Hello [before sleep] "+self._name+" ! I`m pybee server")
        context.progress(0.5)
        time.sleep(15)
        context.message("Say Hello", "Hello [after sleep] "+self._name+" ! I`m pybee server")