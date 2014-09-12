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

from lettuce import world
import time
import Selenium_utils
import http_utils
import general_utils
from myTools.constants import *

class Hadoop:
    def __init__(self, hadoop_url, hadoop_user):
        """
        constructor
        :param hadoop_ip:
        :param hadoop_user:
        :param hadoop_pass:
        """
        world.hadoop_url  = hadoop_url
        world.hadoop_user = hadoop_user

    def __createUrl(self, operation):
        """
        create the url for different operations
        :param element:
        :param offset: used in resource search
        :param operation: operation type (dataset, etc)
        :return: request url
        """
        resource = world.hadoop_prefix + world.resource
        world.organization[world.cygnus_type] = world.organization[world.cygnus_type].lower()
        if operation == OPEN_FILE:
           value = "%s/%s/%s/%s/%s/%s.%s=%s" % (world.hadoop_url, PATH_HADOOP, world.hadoop_user, world.organization[world.cygnus_type], resource, resource, QUERY_PARAM_HADOOP, operation )
        if operation == DELETE_FILE:
           value = "%s/%s/%s/%s/%s/%s.%s=%s&%s=%s" % (world.hadoop_url, PATH_HADOOP, world.hadoop_user, world.organization[world.cygnus_type], resource, resource, QUERY_PARAM_HADOOP, operation, QUERY_PARAM_HADOOP_DELETE, world.hadoop_user)
        return value

    def __createHeaders(self, content="xml"):
        """
        create the header for different requests
        :param operation: different request
        :param content: "xml" or "json"
        :return:
        """
        return {CONTENT_TYPE: APPLICATION_CONTENT + content}

    def __splitElement (self, body, name):
        """
        split the differents lines by \n and return the line associated a a name
        :param body: 
        :param name:
        :return:
        """
        temp = body
        i=1
        list = []
        while len(temp)> 0:
            if temp[i] == "\n":
                list.append(temp[:i])
                temp = temp[i+1:]
                i = 1
            i=i+1
        for i in range(len(list)-1, -1, -1):    #from N element in the list until 0, desc
            tempDict = general_utils.convertStrToDict(list[i], JSON)
            if tempDict[ATTR_NAME] == name:
                return tempDict
        return NAME_IS_MISSING

    def verifyDatasetSearch_valuesAndType (self, content):
        """
        Verify that the attribute contents (type and value) are stored in hadoop
        :param content: xml or json
        """
        world.element = None
        self.body = None
        self.attrValue = None
        self.attrType = None
        outMsg = NAME_IS_MISSING

        if content == XML:
            valueTemp = CONTENT_VALUE
        else:
             valueTemp = VALUE_JSON
        resp, self.body = http_utils.request2(GET, self.__createUrl(OPEN_FILE), self.__createHeaders(content), EMPTY, TRUE, ERROR[NOT])

        for i in range(int(world.attrsNumber)):                                                                      # loops through all our  attributes
            world.element =  self.__splitElement(self.body, world.attrs[i][NAME])
            if world.element == NAME_IS_MISSING:
                return NAME_IS_MISSING
            else:                                                                                                       # if find the name, begin the verifications
                self.attrType  = world.element [ATTR_TYPE]
                self.attrValue = world.element [ATTR_VALUE]
                if world.compoundNumber == 0:
                    if str(self.attrValue) != str(world.attrs[i][valueTemp]):                                                        # verify the value
                        return "The "+world.attrs[i][NAME]+" value does not match..."
                else:
                    for j in range(world.compoundNumber):
                        if self.attrValue[ITEM+str(j)] != world.attrs[i][valueTemp][ITEM+str(j)]:              # verify the compound values

                            return "The "+world.attrs[i][NAME][ITEM+str(j)]+" compound values does not match..."
                if self.attrType != world.attrs[i][TYPE]:                                                               # verify the type
                    return "The "+world.attrs[i][NAME]+" type does not match..."
                outMsg = "OK"
        return outMsg

    def verifyDatasetSearch_metadatas (self, content):
        """
        Verify that the attribute contents (metadatas) are stored in hadoop
        :param content: xml or json

        """
        world.element = None
        outMsg = NAME_IS_MISSING
        if world.metadatasNumber <= 0: return "OK"
        for i in range(int(world.attrsNumber)):                                                                                      # loops through all our  attributes
           world.element =  self.__splitElement(self.body, world.attrs[i][NAME])
           if world.element == NAME_IS_MISSING:
                return NAME_IS_MISSING
           else:
                meta = world.element [ATTR_MD]                                                                                # verify if it has metadatas

                for j in range(len (meta)):
                    if content == XML:
                        if meta[j][TYPE] != world.attrs[i][METADATA][j][CONTEXT_METADATA][TYPE]:
                            return "The "+world.attrs[i][NAME]+" metatada type does not match..."
                    else:
                        if meta[j][TYPE] != world.attrs[i][METADATAS_JSON][j][TYPE]:
                            return "The "+world.attrs[i][NAME]+" metatada type does not match..."
                    outMsg = "OK"
        return outMsg



    def validateResponse (self, response):
        """
        assert the response obtained after content verifications
        :param response:
        """
        assert response == "OK", \
        "...Wrong data stored in hadoop: %s \n" % (response)

    def deleteFile (self):
        """
        delete the file in use (used in terrain.py, after.each_scenario method)
        """
        http_utils.request2(DELETE, self.__createUrl(DELETE_FILE), self.__createHeaders(JSON), EMPTY, TRUE, ERROR[NOT])

    def parametersToNotification_col (self, organization, resource, attributesQuantity, metadataValue, content):
        """
        Add global parameters to notification
        :param organization:
        :param resource:
        :param attributesQuantity:
        :param metadataValue:
        :param content:
        """
        world.cygnus_type = COL_TYPE
        if organization != DEFAULT: world.organization[world.cygnus_type] = organization
        if resource != DEFAULT: world.resource = resource
        if attributesQuantity != DEFAULT: world.attrsNumber = attributesQuantity
        world.metadataValue = metadataValue
        world.content = content







