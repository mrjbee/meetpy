import logging


def log_server():
    return logging.getLogger("meetpy.SERVER")

def log_execution():
    return logging.getLogger("meetpy.EXECUTION")


class Object(object):
    pass