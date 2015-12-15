# -*- coding: utf-8 -*-
from http.server import HTTPServer
from common import beerest
from common.utils import log_server, log_execution
import logging
import transport_config
from context_config import SM
import ssl
import sys

VERSION = "0.1 (beta)"

PORT_NUMBER = SM.setting("port")
SERVER_LOG_PATH = SM.setting("log_server_path")
SERVER_LOG_LEVEL = SM.log_level("log_server_level")
EXECUTION_LOG_PATH = SM.setting("log_execution_path")
EXECUTION_LOG_LEVEL = SM.log_level("log_execution_level")
CONSOLE_LOG_LEVEL = SM.log_level("log_console_level")
CONSOLE_LOG_ENABLED = SM.setting("log_console_enabled")

SSL_ENABLED = SM.setting("ssl_enabled")
SSL_PEM_PATH = SM.setting("ssl_pem_path")

if CONSOLE_LOG_ENABLED is False:
    CONSOLE_LOG_LEVEL = None

# Welcome prompt
print("MeetPy server. Version", VERSION)
print(" == server log path:", SERVER_LOG_PATH)
print(" == execution log path:", EXECUTION_LOG_PATH)
print(" == console log level:", CONSOLE_LOG_LEVEL)
print(" == starting on a port:", PORT_NUMBER)


# Logging setup
fmt = logging.Formatter('%(levelname)s:%(message)s')
file_fmt = logging.Formatter('[%(threadName)s] %(asctime)s - %(levelname)s:%(message)s')
console_handler = logging.StreamHandler(sys.stdout)
console_handler.setFormatter(fmt)
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
    transport_config.CreateCommandTask("/tasks"),
    transport_config.TaskDetails("/task/<task_id>")
]

server = HTTPServer(('', PORT_NUMBER), beerest.RestServlet)
try:
    log_server().info("Server started at port: "+str(PORT_NUMBER))
    if SSL_ENABLED:
        log_server().info("Server SSL enabled certificate = "+str(SSL_PEM_PATH))
        server.socket = ssl.wrap_socket(server.socket, certfile=str(SSL_PEM_PATH))
    # Wait forever for incoming http requests
    server.serve_forever()
except KeyboardInterrupt or SystemExit:
    log_server().debug("Server exit requested")
finally:
    server.socket.close()
    log_server().info("Server stop")