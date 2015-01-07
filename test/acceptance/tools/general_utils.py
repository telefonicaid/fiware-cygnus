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
# For those usages not covered by the GNU Affero General Public License please contact:
#  iot_support at tid.es
#
#


import json
import random
import string
import time
import xmltodict


# general constants
EMPTY   = u''
XML     = u'xml'
JSON    = u'json'


def string_generator(size=10, chars=string.ascii_letters + string.digits):
        """
        Method to create random strings
        :param size: define the string size
        :param chars: the characters to be use to create the string
        return ''.join(random.choice(chars) for x in range(size))
        """
        return ''.join(random.choice(chars) for x in range(size))

def number_generator (size=5, decimals="%0.1f"):
    """"
    Method to create random number
    :param decimals: decimal account
    :param size: define the number size
    :return:
    """
    return decimals % (random.random() * (10**size))

def convert_str_to_dict (body, content):
    """
    Convert string to Dictionary
    :param body: String to convert
    :param content: content type (json or xml)
    :return: dictionary
    """
    if content == XML:
        return xmltodict.parse(body)
    else:
        return json.loads(body)

def convert_dict_to_str (body, content):
    """
    Convert Dictionary to String
    :param body: dictionary to convert
    :param content: content type (json or xml)
    :return: string
    """
    if content == XML:
        return xmltodict.unparse(body)
    else:
        return json.dumps(body)

def convert_str_to_list (text, separator):
    """
    Convert String to list
    :param text: text to convert
    :param separator: separator used
    :return: list []
    """
    return text.split(separator)

def convert_list_to_string (list, separator):
    """
    Convert  List to String
    :param text: list to convert
    :param separator: separator used
    :return: string ""
    """
    return separator.join(list)

def show_times (init_value):
    """
    shows the time duration of the entire test
    :param initValue: initial time
    """
    print "**************************************************************"
    print "Initial (date & time): " + str(init_value)
    print "Final   (date & time): " + str(time.strftime("%c"))
    print "**************************************************************"

def generate_date_zulu():
    """
    generate date & time zulu
    ex: 2014-05-06T10:39:47.696Z
    """
    return str(time.strftime("%Y-%m-%dT%H:%M:%S.095Z"))

