# -*- coding: utf-8 -*-
import re, json, traceback
from common.utils import log_server
from http.server import BaseHTTPRequestHandler

handlers = []
INVALID = ()


class RestServlet(BaseHTTPRequestHandler):

    def do_GET(self):
        try:
            global INVALID
            log_server().info("Request: %s", self.raw_requestline)
            handler = self._select_handler(self.path)
            if handler is None:
                log_server().info("Respond: bad path [no handler]")
                self._send_bad_path()
            else:
                handler_response = handler.get(self.path, self.headers)
                if handler_response is None:
                    self._send_not_found()
                    log_server().info("Respond: not found")
                elif handler_response is INVALID:
                    self._send_bad_request()
                    log_server().info("Respond: invalid request")
                else:
                    log_server().info("Respond: success")
                    self.send_response(200)
                    # TODO! process headers
                    self.send_header('Content-type', handler_response.get_content_type())
                    self.end_headers()
                    self.wfile.write(handler_response.get_content_as_bytes())
        except BaseException:
            log_server().exception("Exception during processing: "+str(self.raw_requestline))
            self.send_response(500)
        return

    def do_POST(self):
        try:
            log_server().info("Request: %s", self.raw_requestline)
            body_size = int(self.headers['Content-Length'])
            if body_size < 2:
                log_server().info("Respond: invalid request [no body]")
                self._send_bad_request()
                return
            body = (self.rfile.read(body_size)).decode("UTF-8")
            log_server().info("Request body: %s", body)
            global INVALID
            handler = self._select_handler(self.path)
            if handler is None:
                log_server().info("Respond: bad path [no handler]")
                self._send_bad_path()
            else:
                handler_response = handler.post(self.path, self.headers, body)
                if handler_response is None:
                    log_server().info("Respond: not found")
                    self._send_not_found()
                elif handler_response is INVALID:
                    log_server().info("Respond: invalid request")
                    self._send_bad_request()
                else:
                    log_server().info("Respond: success")
                    self.send_response(200)
                    # TODO! process headers
                    self.send_header('Content-type', handler_response.get_content_type())
                    self.end_headers()
                    self.wfile.write(handler_response.get_content_as_bytes())
        except BaseException:
            log_server().exception("Exception during processing: "+str(self.raw_requestline))
            self.send_response(500)
        return

    def _send_not_found(self):
        self.send_response(404)
        return

    def _send_bad_path(self):
        self.send_response(405)
        return

    def _send_bad_request(self):
        self.send_response(403)
        return

    def _select_handler(self, path):
        global handlers
        for handler in handlers:
            if handler.is_mine(path):
                return handler
        return None


class ActionHandler(object):

    def __init__(self, registration_path):
        super(ActionHandler, self).__init__()
        self._path_parser = _ParsedPath(registration_path)

    def is_mine(self, url):
        return self._path_parser.ismatch(url)

    def get(self, url, headers):
        ids = self._path_parser.get(url, True)
        return self.do_get(ids, headers)

    def do_get(self, id_map, header_map):
        return None

    def post(self, url, headers, body_string):
        ids = self._path_parser.get(url, True)
        body = json.loads(body_string)
        return self.do_post(ids, headers, body)

    def do_post(self, id_map, header_map, body):
        return None

class HandlerResponse(object):

    def __init__(self):
        self.headers = {}
        self._isString = False
        self._content_plain = ""
        self._content_type = "text/plain"

    def write_json(self, json_obj):
        self._isString = True
        self._content_type = "application/json"
        self._content_plain = json.dumps(json_obj, sort_keys=True, indent=4, separators=(',', ': '))

    def get_content_as_bytes(self):
        if self._isString:
            return bytes(self._content_plain, "UTF-8")
        # TODO: throw exception
        return None

    def get_content_type(self):
        return self._content_type


class _ParsedPath(object):
    def __init__(self, path_string):
        super(_ParsedPath, self).__init__()
        self._path_segments = []
        self._regexp = "^"
        for segment in path_string.split("/"):
            segment = str(segment).strip()
            if segment == "":
                continue
            if segment.__contains__("<"):
                self._path_segments.append((segment[1:-1], True))
            else:
                self._path_segments.append((segment, False))
        for parsed_segment in self._path_segments:
            self._regexp += "/"
            if not parsed_segment[1]:
                self._regexp += parsed_segment[0]
            else:
                self._regexp += "(?P<"+parsed_segment[0]+">[^/]*)"
        self._regexp += "$"

    def ismatch(self, url_path):
        matcher = re.search(self._regexp, url_path)
        if matcher is None:
            return False
        return True

    def get(self, url_path, do_check=True):
        matcher = re.search(self._regexp, url_path)
        if do_check and matcher is None:
            return None
        params = {}
        for parsed_segment in self._path_segments:
            if parsed_segment[1]:
                params[parsed_segment[0]] = matcher.group(parsed_segment[0])
        return params

# =============== Test methods starts here


def test_parse_url():

    # without variable
    simple_path = _ParsedPath("/hello/world")
    assert not simple_path.ismatch("/not/correct")
    assert simple_path.ismatch("/hello/world")
    assert not simple_path.ismatch("/hello/world/asd")

    # with single variable
    test_path = _ParsedPath("/hello/world/<id>")
    assert test_path.ismatch("/hello/world/456")
    assert not test_path.ismatch("/hello/world/456/asdasd")
    assert test_path.get("/hello/world/456")["id"] == "456"

    # with single variable in the middle
    test_path = _ParsedPath("/hello/world/<id>/but/in/middle")
    assert test_path.ismatch("/hello/world/345/but/in/middle")
    assert not test_path.ismatch("/hello/world/456/but/in/middle/asd")

    # with double variable
    test_path = _ParsedPath("/hello/world/<first>/<second>")
    assert test_path.ismatch("/hello/world/345/12")
    assert not test_path.ismatch("/hello/world/456/12/in")
    assert test_path.get("/hello/world/345/12")["first"] == "345"
    assert test_path.get("/hello/world/345/12")["second"] == "12"

if __name__ == "__main__":
    test_parse_url()
    pass