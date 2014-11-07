# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# francisco.romerobueno@telefonica.com
#
#     Author: Ivan Arias
#

import httplib
import httplib2
from urlparse import urlparse
import requests
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

def printResponse3(response):
    """
    Show response in console
    :param response: http code, header and body returned
    """
    print "---------------------------------- Response ----------------------------------------------"
    print "status code: " + str(response.status_code)
    print "\nHeader: " + str(response.headers)
    print "\nBody: (" + str(response.text) + ")\n\n\n"
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
    #printRequest(method, url, headers, body)
    parsed_url = urlparse(url)
    con = httplib.HTTPConnection(parsed_url.netloc)

    con.request(method, parsed_url.path, body, headers)
    response = con.getresponse()


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
    #printRequest(method, url, headers, body)
    parsed_url = urlparse(url)
    h = httplib2.Http()
    h.disable_ssl_certificate_validation=False
    if redirect != TRUE:
        h.follow_redirects = redirect
    else:
        h.follow_all_redirects = redirect
    (response, body) = h.request(url, method, body, headers)

    # printResponse2(response, body)
    return response, body

def request3 (method, Url, Headers, Body, Parameters, Redirect,  VerifySSL):
    """

    :param method: POST, GET, PUT, DELETE methods
    :param Url:  endpoint (with port) and path
    :param Headers: headers in request
    :param Body: payload if is required
    :param Parameters: queries parameter if are required
    :param Redirect: if redirect id allowed
    :param VerifySSL: if the SSL is verified
    :return: response (code, headers and body)
    """
    try:
        #printRequest(method, Url, Headers, Body)
        if method == POST:
            resp = requests.post(url=Url, headers=Headers, data=Body, params=Parameters, allow_redirects= Redirect, verify=VerifySSL)
        elif method == GET:
            resp = requests.get(url=Url, headers=Headers, data=Body, params=Parameters, allow_redirects= Redirect, verify=VerifySSL)
        if method == PUT:
            resp = requests.put(url=Url, headers=Headers, data=Body, params=Parameters, allow_redirects= Redirect, verify=VerifySSL)
        if method == DELETE:
            resp = requests.delete(url=Url, headers=Headers, data=Body, params=Parameters, allow_redirects= Redirect, verify=VerifySSL)
        #printResponse3(resp)
        return resp
    except Exception, e:
        print " REQUEST ERROR -->  "+ str(e)

#--------------------------------------------------------------------------------------------------------

