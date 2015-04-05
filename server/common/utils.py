import logging


def log_server():
    return logging.getLogger("meetpy.SERVER")


def log_execution(uuid=None):
    if uuid is None:
        return logging.getLogger("meetpy.EXECUTION")
    else:
        return logging.getLogger("meetpy.EXECUTION."+str(uuid))


class Object(object):
    pass