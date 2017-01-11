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
# iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

import requests

# constants
POST       = u'POST'
GET        = u'GET'
PUT        = u'PUT'
DELETE     = u'DELETE'
EMPTY      = u''

# request parameters constants
URL              = u'url'
HEADERS          = u'headers'
DATA             = u'data'
PARAM            = u'param'
ALLOW_REDIRECTS  = u'allow_redirects'
VERIFY           = u'verify'

#HTTP status code
OK                     = u'OK'
CREATED                = u'Created'
REDIRECT               = u'Redirect'
NO_CONTENT             = u'No Content'
MOVED_PERMANENTLY      = u'Moved Permanently'
BAD_REQUEST            = u'Bad Request'
UNAUTHORIZED           = u'unauthorized'
NOT_FOUND              = u'Not Found'
BAD_METHOD             = u'Bad Method'
NOT_ACCEPTABLE         = u'Not Acceptable'
CONFLICT               = u'Conflict'
UNSUPPORTED_MEDIA_TYPE = u'Unsupported Media Type'
INTERNAL_SERVER_ERROR  = u'Internal Server Error'

status_codes = {OK: 200,
                CREATED: 201,
                NO_CONTENT: 204,
                MOVED_PERMANENTLY: 301,
                REDIRECT: 307,
                BAD_REQUEST: 400,
                UNAUTHORIZED: 401,
                NOT_FOUND: 404,
                BAD_METHOD: 405,
                NOT_ACCEPTABLE:406,
                CONFLICT: 409,
                UNSUPPORTED_MEDIA_TYPE: 415,
                INTERNAL_SERVER_ERROR: 500}

def print_request(method, url, headers, body):
    """
    Show Request in console
    :param method: method used
    :param url: url used
    :param headers: header used
    :param body: body used
    """
    print "------------------------------ Request ----------------------------------------------"
    print "url: " + str(method) + "  " + str(url)+"\n"
    if headers is not None:
        print "Header: " + str(headers) + "\n"
    if body != EMPTY:
        print "Body: "  + str(body) + "\n\n\n"
    print "----------------------------- End request ---------------------------------------------\n\n\n\n"

def print_response(response):
    """
    Show response in console
    :param response: http code, header and body returned
    """
    body = response.text
    headers = response.headers
    print "---------------------------------- Response ----------------------------------------------"
    print "status code: " + str(response.status_code) + "\n"
    if headers is not None:
        print "Header: " + str(headers) + "\n"
    if body != EMPTY:
        print "Body: " + str(body) + "\n\n\n"
    print "--------------------------------- End Response --------------------------------------------"
def request (method, **kwargs):
    """
    launch a request
    :param method: POST, GET, PUT, DELETE methods (MANDATORY)
    :param url:  endpoint (with port and path)
    :param headers: headers in request
    :param data: payload if is required
    :param Param: queries parameter if are required
    :param allow_redirects: if redirect id allowed
    :param verify: if the SSL is verified
    :return: response (code, headers and body)

    Note: two lines are comments because these are only used to internal debug in tests, 
	      the first show the request and the second, show the response
    """
    try:
        url = kwargs.get(URL, EMPTY)
        headers = kwargs.get(HEADERS, None)
        body = kwargs.get (DATA, EMPTY)
        parameters = kwargs.get (PARAM, EMPTY)
        redirect = kwargs.get(ALLOW_REDIRECTS, True)
        verify_SSL = kwargs.get(VERIFY, False)
        #print_request(method, url, headers, body)
        resp = requests.request(method, url, headers=headers, data=body, params=parameters, allow_redirects= redirect, verify=verify_SSL)
        #print_response(resp)
        return resp
    except Exception, e:
        assert not True,  " ERROR IN REQUEST: %s  \nurl    : %s \nheaders: %s \npayload: %s" % (str(e), url, str(headers), body)

def assert_status_code (expected, resp, Error_msg):
     """
     Evaluate if the status code is the expected
     :param resp: response body
     :param Error_msg: message in error case
     """
     assert resp.status_code == int(expected), \
        "%s:  \n HttpCode expected: %s \n HttpCode received: %s \n Body: %s" % (Error_msg, str(expected), str(resp.status_code), str(resp.text))

