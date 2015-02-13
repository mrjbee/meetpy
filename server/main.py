# -*- coding: utf-8 -*-
from BaseHTTPServer import HTTPServer
from common import beerest
from common.utils import log_server, log_execution
import logging
import transport_config

VERSION = "0.1 (beta)"

PORT_NUMBER = 9999
SERVER_LOG_PATH = "server.log"
SERVER_LOG_LEVEL = logging.INFO
EXECUTION_LOG_PATH = "execution.log"
EXECUTION_LOG_LEVEL = logging.INFO
CONSOLE_LOG_LEVEL = None  # logging.DEBUG

# Welcome prompt
print "MeetPy server. Version", VERSION
print " == server log path:", SERVER_LOG_PATH
print " == execution log path:", EXECUTION_LOG_PATH
print " == console log level:", CONSOLE_LOG_LEVEL
print " == starting on a port:", PORT_NUMBER


# Logging setup
fmt = logging.Formatter('%(levelname)s:%(message)s')
file_fmt = logging.Formatter('%(asctime)s - %(levelname)s:%(message)s')
console_handler = logging.StreamHandler()
console_handler.setFormatter(fmt)
if CONSOLE_LOG_LEVEL:
    console_handler.setLevel(CONSOLE_LOG_LEVEL)

file_handler_execution = logging.FileHandler(EXECUTION_LOG_PATH)
file_handler_execution.setFormatter(file_fmt)

file_handler_server = logging.FileHandler(SERVER_LOG_PATH)
file_handler_server.setFormatter(file_fmt)

logger_under_setup = log_server()
if CONSOLE_LOG_LEVEL:
    logger_under_setup.addHandler(console_handler)
logger_under_setup.addHandler(file_handler_server)
logger_under_setup.setLevel(SERVER_LOG_LEVEL)

logger_under_setup = log_execution()
if CONSOLE_LOG_LEVEL:
    logger_under_setup.addHandler(console_handler)
logger_under_setup.addHandler(file_handler_execution)
logger_under_setup.setLevel(EXECUTION_LOG_LEVEL)

beerest.handlers = [
    transport_config.VersionHandler("/version"),
    transport_config.Places("/places"),
    transport_config.Files("/files"),
    transport_config.Commands("/commands"),
    transport_config.CommandDetails("/command/<command_id>"),
    transport_config.CreateCommandTask("/tasks")
]

server = HTTPServer(('', PORT_NUMBER), beerest.RestServlet)
try:
    log_server().info("Server started at port: "+str(PORT_NUMBER))
    # Wait forever for incoming http requests
    server.serve_forever()
except KeyboardInterrupt or SystemExit:
    log_server().debug("Server exit requested")
finally:
    server.socket.close()
    log_server().info("Server stop")