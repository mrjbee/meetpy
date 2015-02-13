# -*- coding: utf-8 -*-
from BaseHTTPServer import HTTPServer
import transport_config
from context_config import *
from common import beerest
from features import fs

PORT_NUMBER = 9999
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
    print 'Started HTTP server on port', PORT_NUMBER
    # Wait forever for incoming http requests
    server.serve_forever()
except KeyboardInterrupt:
    print 'Request server stop'

finally:
    print 'Stopping server'
    server.socket.close()
