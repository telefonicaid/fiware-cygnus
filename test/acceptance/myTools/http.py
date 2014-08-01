# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# frb@tid.es
#
#     Author: Ivan Arias
#

import httplib
import httplib2
from urlparse import urlparse
from myTools.constants import *

def printRequest(method, url, headers, body):
    """
    Show Request in console
    :param method: method used
    :param url: url used
    :param headers: header used
    :param body: body used
    """
    print "------------------------------ Request ----------------------------------------------"
    print "url: (" + str(method) + "  " + str(url)+")"
    print "\nHeader: (" + str(headers) + ")\n"
    if body is not None:
        print "\nBody: ("  + str(body) + ")\n\n"
    print "----------------------------- End request ---------------------------------------------\n\n\n\n"

def printResponse(response):
    """
    Show response in console
    :param response: http code, header and body returned
    """
    print "---------------------------------- Response ----------------------------------------------"
    print "status code: " + str(response.status)
    print "\nHeader: " + str(response.msg)
    print "\nBody: (" + str(response.read()) + ")\n\n\n"
    print "--------------------------------- End Response --------------------------------------------"

def printResponse2(response, body):
    """
    Show response in console
    :param response: http code and header returned
    :param body: body returned
    """
    print "---------------------------------- Response ----------------------------------------------"
    print "status code: " + str(response.status)
    print "\nHeader: " + str(response)
    print "\nBody: (" + str(body) + ")\n\n\n"
    print "--------------------------------- End Response --------------------------------------------"

def request(method, url, headers, body, error):
    """
    launch a request
    :param method: method used
    :param url: url used
    :param headers: header used
    :param body: body used
    :param error: error type
    :return: body, header and http code responses
    """
    parsed_url = urlparse(url)
    con = httplib.HTTPConnection(parsed_url.netloc)
    con.request(method, parsed_url.path, body, headers)
    response = con.getresponse()

    #printRequest(method, url, headers, body)
    #printResponse(response)
    return response

def request2(method, url, headers, body, redirect, error):
    """
    launch a request 2
    :param method: method used
    :param url: url used
    :param headers: header used
    :param body: body used
    :param error: error type
    :param redirect: redirect (true or false)
    :return: body, header and http code responses
    """
    parsed_url = urlparse(url)
    h = httplib2.Http()
    if redirect != TRUE:
        h.follow_redirects = redirect
    else:
         h.follow_all_redirects = redirect
    (response, body) = h.request(url, method, body, headers)

    #printRequest(method, url, headers, body)
    #printResponse2(response, body)
    return response, body
#--------------------------------------------------------------------------------------------------------

