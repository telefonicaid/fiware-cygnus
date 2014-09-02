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


import json
import random
import string
import xmltodict
from myTools.constants import *

def errorLabel(value, error):
    """
    insert error label name
    :param value: last value
    :param error: error type
    :return:
    """
    if error == "wrong":
        return '1234567890'
    elif error == "empty":
        return ''
    else:
        return value

def stringGenerator(size=10, chars=string.ascii_letters + string.digits):
        """Method to create random strings
        :param size: define the string size
        :param chars: the characters to be use to create the string
        return ''.join(random.choice(chars) for x in range(size))
        """
        return ''.join(random.choice(chars) for x in range(size))

def numberGenerator (size=5, decimals="%0.1f"):
    """"
    Method to create random number
    :param decimals: decimal account
    :param size: define the number size
    :return:
    """
    return decimals % (random.random() * (10**size))

def isXML(content):
    """
    verify if an element contains xml string
    :param content: XML, JSOn o header complete
    :return: boolean
    """
    return (content.find(XML) >= 0)

def convertStrToDict (body, content):
    """
    Convert string to Dictionary
    :param body: String to convert
    :param content: content type (json or xml)
    :return: dictionary
    """
    if isXML(content):
        return xmltodict.parse(body)
    else:
        return json.loads(body)

def convertDictToStr (body, content):
    """
    Convert Dictionary to String
    :param body: dictionary to convert
    :param content: content type (json or xml)
    :return: string
    """
    if isXML(content):
        return xmltodict.unparse(body)
    else:
        return json.dumps(body)

def ifSubstrExistsInStr (text, subtext):
    """
    Verify if text contains subtext
    :param text:
    :param subtext:
    :return:
    """
    return (text.find(subtext) > 0)
#------------------
def validateHTTPCode(expectedStatusCode, receivedStatus, receivedBody):
    """
    validate http status code
    :param expected_status_code: Http code expected
    """

    assert receivedStatus == status_codes[expectedStatusCode], \
        "Wrong status code received: %d. Expected: %d. \n\nBody content: %s" \
        % (str(receivedStatus), status_codes[expectedStatusCode], str(receivedBody))
