# -*- coding: utf-8 -*-
#
# Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FIWARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact:
#  iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

import sys
import time
import BaseHTTPServer
import ssl
import mock_responses



class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_POST(s):
        """
        Respond to a POST request.
        """
        request_body = None
        try:
            length = int(s.headers['Content-Length'])
            request_body = s.rfile.read(length)   # get the request body
        except Exception, e:
            print "WARN - content-length header does not exist and payload is empty... "
        resp, code = mock_responses.response(s.path,request_body)
        if (resp.find (mock_responses.WARN)>=0 or resp.find (mock_responses.ERROR)>=0) and code == mock_responses.EMPTY:
            print resp
        else:
            s.send_response(code)
            if mock_responses.isHadoop(s.path):headers = mock_responses.headersHADOOP.items()
            else:headers=mock_responses.headersCKAN.items()
            for h, v in headers:
                s.send_header(h, v)
            if code == mock_responses.REDIRECT:
                s.send_header(mock_responses.LOCATION,mock_responses.HADOOP_LOCATION)
        s.end_headers()
        s.wfile.write(resp)

    def do_GET(s):
        """
        Respond to a GET request.
        """

        resp, code = mock_responses.response(s.path)
        if (resp.find (mock_responses.WARN)>=0 or resp.find (mock_responses.ERROR)>=0) and code == mock_responses.EMPTY:
            print resp
        else:
            s.send_response(code)
            if mock_responses.isHadoop(s.path):headers = mock_responses.headersHADOOP.items()
            else:headers=mock_responses.headersCKAN.items()
            for h, v in headers:
                s.send_header(h, v)
        s.end_headers()
        s.wfile.write(resp)


    def do_PUT(s):
        """
        Respond to a PUT request.
        """
        resp, code = mock_responses.response(s.path)
        if (resp.find (mock_responses.WARN)>=0 or resp.find (mock_responses.ERROR)>=0) and code == mock_responses.EMPTY:
            print resp
        else:
            s.send_response(code)
            for h, v in mock_responses.headersHADOOP.items():
                s.send_header(h, v)
            if code == mock_responses.REDIRECT:
                s.send_header(mock_responses.LOCATION,mock_responses.HADOOP_LOCATION)
        s.end_headers()
        s.wfile.write(resp)


if __name__ == '__main__':
    mock_responses.configuration(sys.argv)

    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((mock_responses.HOST_NAME, mock_responses.PORT_NUMBER), MyHandler)
    if mock_responses.CERTIFICATE_HTTPS != "":
        httpd.socket = ssl.wrap_socket (httpd.socket, certfile=mock_responses.CERTIFICATE_HTTPS, server_side=True)

    print time.asctime(), "Server Starts - %s:%s" % (mock_responses.HOST_NAME, mock_responses.PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (mock_responses.HOST_NAME, mock_responses.PORT_NUMBER)