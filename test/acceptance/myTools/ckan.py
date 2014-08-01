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
import Selenium_utils
import http
import utils
from myTools.constants import *

class Ckan:
    world.dictTemp = None

    def __init__(self, ckan_version, ckan_url, ckan_authorization):
        """
        constructor
        :param ckan_version:
        :param ckan_url:
        :param ckan_authorization:
        """
        world.ckan_version    = ckan_version
        world.ckan_url        = ckan_url
        world.authorization   = ckan_authorization


    def __createUrl(self, operation, resourceId = None, offset = None):
        """
        create the url for different operations
        :param resourceId:
        :param offset: used in resource search
        :param operation: operation type (dataset, etc)
        :return: request url
        """
        if operation == VERSION:
            value = "%s/%s" % (world.ckan_url, PATH_VERSION_CKAN)
        if operation == RESOURCE_ID:
            value = "%s/%s/%s" % (world.ckan_url, PATH_API_REST_DATASET, world.dataset)
        if operation == RESOURCE_SEARCH:
            value = "%s/%s%s" % (world.ckan_url, PATH_DATASET_SEARCH, resourceId)
        if operation == RESOURCE_SEARCH_OFFSET:
            value = "%s/%s%s%s%s" % (world.ckan_url, PATH_DATASET_SEARCH, resourceId, OFFSET, offset)
        return value

    def __createHeaders(self, operation, content="xml"):
        """
        create the header for different requests
        :param operation: different request
        :param content: "xml" or "json"
        :return:
        """
        if operation == CKAN_HEADER:
            return {ACCEPT: APPLICATION_CONTENT + content, CONTENT_TYPE: APPLICATION_CONTENT + content, AUTHORIZATION: world.authorization}

    def __getResourceId (self, error = None):
        """
        get Resource Id from ckan by API
        :return: resource_Id
        """
        world.dataset = world.organization+"_"+world.dataset_default
        world.resource = world.resource_identityId + "-" + world.resource_identityType
        resp =  http.request(GET, self.__createUrl(RESOURCE_ID), self.__createHeaders(CKAN_HEADER), EMPTY, ERROR[NOT])
        body = resp.read()
        if error and utils.ifSubstrExistsInStr(body, NOT_FOUND):
            return NOT_FOUND
        assert body.find(NOT_FOUND) <= 0, VALIDATE_DATASET_MSG+" %s \n" % (world.dataset)
        dictBody = utils.convertStrToDict(body, JSON)[RESOURCE]
        for i in range(len (dictBody)):
            if dictBody[i][NAME] == world.resource:
                resp = dictBody[i][ID]
                break
            else:
                resp = VALIDATE_RESOURCE_MSG
        assert resp != VALIDATE_RESOURCE_MSG, VALIDATE_RESOURCE_MSG+" %s \n" % (world.resource)
        return resp

    #-----------------
    def versionCKAN (self):
        """
        Verify if ckan is installed and that version is the expected
        """
        resp, body = http.request2(GET, self.__createUrl(VERSION), self.__createHeaders(CKAN_HEADER, JSON), EMPTY, TRUE, ERROR[NOT])
        bodyDict = utils.convertStrToDict(body, JSON)
        assert  world.ckan_version == str(bodyDict[VERSION]), \
        "Wrong ckan version verified: %d. Expected: %d. \n\nBody content: %s" \
        % (str(bodyDict[VERSION]), world.ckan_version, str(body))

    def verifyDatasetSearch_valuesAndType (self, content):
        """
        Verify that the attribute contents (type and value) are stored in ckan
        :param content: xml or json
        """
        outMsg = "Names are missing"
        resourceId = self.__getResourceId()
        if content == XML:
            valueTemp = CONTENT_VALUE
        else:
             valueTemp = VALUE_JSON
        resp, body = http.request2(GET, self.__createUrl(RESOURCE_SEARCH, resourceId), self.__createHeaders(CKAN_HEADER, JSON), EMPTY, TRUE, ERROR[NOT])
        world.dictTemp = utils.convertStrToDict(body, JSON)
        offset = (world.dictTemp[RESULT][TOTAL]/100)*100
        if offset != 0:
            resp, body = http.request2(GET, self.__createUrl(RESOURCE_SEARCH_OFFSET, resourceId, offset), self.__createHeaders(CKAN_HEADER, JSON), EMPTY, TRUE, ERROR[NOT])
            world.dictTemp = utils.convertStrToDict(body, JSON)
        for i in range(world.attrsNumber):                                                                      # loops through all our  attributes
            for j in range(len(world.dictTemp[RESULT][RECORDS])):                                                     # loops through all ckan data in the resource
                if world.dictTemp[RESULT][RECORDS][j][ATTR_NAME] == world.attrs[i][NAME]:                             # if find the name, begin the verifications
                    if world.compoundNumber == 0:
                        if world.dictTemp[RESULT][RECORDS][j][ATTR_VALUE] != world.attrs[i][valueTemp]:                  # verify the value
                            return "The "+world.attrs[i][NAME]+" value does not match..."
                    else:
                        for l in range(world.compoundNumber):
                            if world.dictTemp[RESULT][RECORDS][j][ATTR_VALUE][ITEM+str(l)] != world.attrs[i][valueTemp][ITEM+str(l)]:   # verify the compound values
                                return "The "+world.attrs[i][NAME][ITEM+str(l)]+" compound values does not match..."
                    if world.dictTemp[RESULT][RECORDS][j][ATTR_TYPE] != world.attrs[i][TYPE]:                        # verify the type
                        return "The "+world.attrs[i][NAME]+" type does not match..."
                    outMsg = "OK"
                    break
        return outMsg

    def verifyDatasetSearch_metadatas (self, content):
        """
        Verify that the attribute contents (metadatas) are stored in ckan
        :param content: xml or json
        """
        if world.metadatasNumber <= 0: return "does not has metadata.."
        outMsg = "Names are missing"
        for i in range(world.attrsNumber):                                                                      # loops through all our  attributes
            for j in range(len(world.dictTemp[RESULT][RECORDS])):                                                    # loops through all ckan data in the resource
                if world.dictTemp[RESULT][RECORDS][j][ATTR_NAME] == world.attrs[i][NAME]:                             # if find the name, begin the verification
                    if world.dictTemp[RESULT][RECORDS][j][ATTR_MD] != None:                                        # verify if it has metadatas
                        for k in range(len(world.dictTemp[RESULT][RECORDS][j][ATTR_MD])):
                            if content == XML:
                                if world.dictTemp[RESULT][RECORDS][j][ATTR_MD][k][TYPE] != world.attrs[i][METADATA][CONTEXT_METADATA][k][TYPE]:
                                     return "The "+world.attrs[i][NAME]+" metatada type does not match..."
                            else:
                                if world.dictTemp[RESULT][RECORDS][j][ATTR_MD][k][TYPE] != world.attrs[i][METADATAS_JSON][k][TYPE]:
                                     return "The "+world.attrs[i][NAME]+" metatada type does not match..."
                outMsg = "OK"
                break
        return outMsg

    def verifyIfDatasetExist (self):
        resp =  self.__getResourceId(TRUE) # TRUE is for error operation
        assert resp == NOT_FOUND, VALIDATE_DATASET_MSG+" %s \n" % (world.dataset)

    def validateResponse (self, response):
        """
        assert the response obtained after content verifications
        :param response:
        """
        assert response == "OK", \
        "...Wrong data stored in ckan: %s \n" % (response)

    #------------------
    def connectionToCKAN (self, operation):
        """
        open a connection with the browser
        :param operation: operation type (dataset, etc)
        :return: if is connected (boolean)
        """
        return Selenium_utils.connect(self.__createUrl(operation))

    def disconnectionToCKAN (self):
        """
        close the connection with the browser
        """
        Selenium_utils.disconnect()
