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

from lettuce import world
import time
import http_utils
import general_utils
from myTools.constants import *

class Hadoop:
    def __init__(self,  hadoop_version, hadoop_verify_version, hadoop_namenode_url, hadoop_conputenode_url, hadoop_user, hadoop_retries_open_file, hadoop_delay_to_retry):
        """
        constructor
        :param hadoop_version:
        :param hadoop_namenode_url:
        :param hadoop_conputenode_url:
        :param hadoop_user:
        """

        world.hadoop_version           = hadoop_version
        world.hadoop_verify_version    = hadoop_verify_version
        world.hadoop_namenode_url      = hadoop_namenode_url
        world.hadoop_conputenode_url   = hadoop_conputenode_url
        world.hadoop_user              = hadoop_user
        world.hadoop_retries_open_file = hadoop_retries_open_file
        world.hadoop_delay_to_retry    = hadoop_delay_to_retry

    def __createUrl(self, operation):
        """
        create the url for different operations
        :param element:
        :param offset: used in resource search
        :param operation: operation type (dataset, etc)
        :return: request url
        """

        world.organization[world.cygnus_type] = world.organization[world.cygnus_type].lower()

        if operation == OPEN_FILE:
           value = "%s/%s/%s/%s/%s/%s.%s=%s&%s=%s" % (world.hadoop_namenode_url, PATH_HADOOP, world.hadoop_user, world.organization[world.cygnus_type], world.resource, world.resource, QUERY_PARAM_HADOOP, operation, "user.name", world.hadoop_user)
        if operation == DELETE_FILE:
           value = "%s/%s/%s/%s/%s/%s.%s=%s&%s=%s" % (world.hadoop_namenode_url, PATH_HADOOP, world.hadoop_user, world.organization[world.cygnus_type], world.resource, world.resource, QUERY_PARAM_HADOOP, operation, QUERY_PARAM_HADOOP_DELETE, world.hadoop_user)
        if operation == VERSION:
            value = "%s/%s"  % (world.hadoop_conputenode_url, HADOOP_VERSION_PATH)
        return value

    def version (self):
        """
        Verify if hadoop is installed and that version is the expected
        """
        resp, body = http_utils.request2(GET, self.__createUrl(VERSION), self.__createHeaders(), EMPTY, TRUE, ERROR[NOT])
        bodyDict = general_utils.convertStrToDict(body, JSON)
        assert  STARTED == str (bodyDict[CLUSTER_INFO][HADOOP_STATE]), \
        "hadoop is not started...\nverified: %s. Expected: %s. \n\nBody content: %s" \
        % (str (bodyDict[CLUSTER_INFO][HADOOP_STATE]), STARTED, str(body))

        assert  str(world.hadoop_version) == str (bodyDict[CLUSTER_INFO][HADOOP_VERSION]), \
        "Wrong hadoop version \nverified: %s. Expected: %s. \n\nBody content: %s" \
        % (str (bodyDict[CLUSTER_INFO][HADOOP_VERSION]), str(world.hadoop_version), str(body))

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
                i = 0
            i=i+1
            if i == len(temp): break
        for i in range(len(list)-1, -1, -1):    #from last element in the list until first, desc
            dictTemp = general_utils.convertStrToDict(list[i], JSON)
            if dictTemp [ATTR_NAME] == name:
                return dictTemp
        return NAME_IS_MISSING

    def __openFile (self, retry, content, name):
        retries = 0
        delayToRetry = world.hadoop_delay_to_retry
        while retry > 0:
            self.body = None
            self.element = None
            resp, self.body = http_utils.request2(GET, self.__createUrl(OPEN_FILE), self.__createHeaders(content), EMPTY, TRUE, ERROR[NOT])
            if self.body.find(ERROR [FILE_NOT_FOUND]) < 0:
                self.element = self.__splitElement(self.body, name)
                if str(self.element).find(NAME_IS_MISSING) < 0: # the element is found
                    return self.element
            time.sleep(delayToRetry)
            retry-=1
            retries+=1
            print "      ....Retry to open file in hadoop system. No: "+ str (retries)
        return "file: /%s/%s/%s.txt does no exist... In %s retries..." % (world.organization[world.cygnus_type], world.resource, world.resource, str(retries))


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
        for i in range(int(world.attrsNumber)):                                                                     # loops through all our  attributes

            world.element = self.body = self.__openFile(world.hadoop_retries_open_file, content, world.attrs[i][NAME])
            if str(world.element).find(NAME_IS_MISSING) >= 0 or str(world.element).find("retries...") >= 0:
                return str(world.element)
            else:
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
                    return "The "+world.attrs[i][TYPE]+" type does not match..."
                outMsg = "OK"
        return outMsg

    def verifyDatasetSearch_metadatas (self, content):
        """
        Verify that the attribute contents (metadatas) are stored in hadoop
        :param content: xml or json

        """
        world.element = None
        self.body     = None
        outMsg = NAME_IS_MISSING
        if world.metadatasNumber <= 0: return "OK"
        for i in range(int(world.attrsNumber)):                                                                                      # loops through all our  attributes
            world.element = self.body = self.__openFile(world.hadoop_retries_open_file, content, world.attrs[i][NAME])
            if str(world.element).find(NAME_IS_MISSING) >= 0 or str(world.element).find("retries...") >= 0:
                return str(world.element)
            else:
                meta = world.element [ATTR_MD]                                                                                # verify if it has metadatas
                for j in range(len (meta)):
                    if content == XML:
                        if meta[j][TYPE] != world.attrs[i][METADATA][CONTEXT_METADATA][j][TYPE]:
                            return "The \""+ world.attrs[i][METADATA][CONTEXT_METADATA][j][TYPE]+"\" metatada type does not match..."
                    else:
                        if meta[j][TYPE] != world.attrs[i][METADATAS_JSON][j][TYPE]:
                            return "The \""+world.attrs[i][METADATAS_JSON][j][TYPE]+"\" metatada type does not match..."
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
        pass

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







