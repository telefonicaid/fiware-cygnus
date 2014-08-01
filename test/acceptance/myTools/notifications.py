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
import http
import utils
from myTools.constants import *


class Notifications:
    world.attrs = []

    def __init__(self, cygnus_url, userAgent, organization, resource, attrNumber, metadataNumber, compoundNumber, dataset_default, mysql_prefix):
        """
        constructor
        :param cygnus_url:
        :param userAgent:
        :param organization:
        :param resource:
        :param attrNumber:
        :param metadataNumber:
        :param compoundNumber:
        """
        world.cygnus_url            = cygnus_url
        world.userAgent             = userAgent
        world.organization          = organization
        world.resource              = resource
        world.attrsNumber           = attrNumber
        world.metadatasNumber       = metadataNumber
        world.compoundNumber        = compoundNumber
        world.dataset_default       = dataset_default
        world.mysql_prefix          = mysql_prefix

    def __createUrl(self, operation, resourceId = None):
        """
        create the url for different operations
        :param operation: operation type (dataset, etc)
        :return: request url
        """
        if operation == NOTIFY:
           value = "%s/%s" % (world.cygnus_url, NOTIFY)
        return value

    def __createHeaders(self, operation, content="xml"):
        """
        create the header for different requests
        :param operation: different request
        :param content: "xml" or "json"
        :return:
        """
        if operation == NOTIFY:
            return {ACCEPT: APPLICATION_CONTENT + content, CONTENT_TYPE  : APPLICATION_CONTENT + content, FIWARE_SERVICE: world.organization, USER_AGENT: world.userAgent}
        if operation == NOTIFY_ERROR:
            return {ACCEPT: APPLICATION_CONTENT + content, CONTENT_TYPE  : APPLICATION_CONTENT + content, USER_AGENT: world.userAgent}
    #-----------------
    def __newMetadata(self, content):
        """
        Create a new Metadata
        :return: metadata dictionary
        """
        randomStr = utils.stringGenerator(4)
        randomInt = utils.numberGenerator(3)
        name = 'name_' + randomStr
        type = 'type_' + randomStr
        value = randomInt
        if utils.isXML(content):
            return {CONTEXT_METADATA: {NAME: name, TYPE: type, VALUE_JSON: value}}
        else:
            return {NAME: name, TYPE: type, VALUE_JSON: value}

    def __appendMetadatas (self, metadatasNumber, content):
        """
        append the metadatas
        :param metadatasNumber:
        :return:
        """
        if metadatasNumber <= 0: return None
        contextMetadatasList = []
        for i in range(int(metadatasNumber)):
            contextMetadatasList.append(self.__newMetadata(content))
        return contextMetadatasList

    def __newCompound (self, compound):

        """
        Add compound values in attributes
        :param compound:
        :return:
        """
        value = {}
        for i in range(compound):
            randomInt = utils.numberGenerator(3)
            value[ITEM+str(i)] = randomInt
        return value

    def __newAttribute(self, compound, metadatasNumber,content):
        """
        Create a new Attribute with n metadatas
        :return: attribute dict
        """
        values = []
        randomStr = utils.stringGenerator(6)
        randomInt = utils.numberGenerator(3)
        name = 'name_' + randomStr
        type = 'type_' + randomStr
        # for compound

        contextMetadatasList = self.__appendMetadatas(metadatasNumber, content)
        if compound > 0:
            value = self.__newCompound (compound)
        else:
            value = randomInt
        if utils.isXML(content):
            return  {NAME: name, TYPE: type, CONTENT_VALUE: value, METADATA: contextMetadatasList}
        else:
            return  {NAME: name, TYPE: type, VALUE_JSON: value, METADATAS_JSON: contextMetadatasList}

    def __appendAttributes (self, attributesNumber, compound, metadatasNumber, content):
        """
        appends all attributes
        :param attributesNumber:
        :param metadatasNumber:
        :param content:
        :return:
        """
        self.attrs = []
        for i in range(int(attributesNumber)):
            t = self.__newAttribute(compound, metadatasNumber, content)
            self.attrs.append(t)
        return self.attrs

    def __splitResource (self, content):
        """
        split resource in entityType and entityId
        :param content:
        """
        res = world.resource.split ("-")
        world.resource_identityType = res [1]
        world.resource_identityId = res [0]
        if content == XML:
            NOTIFICATION[content][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST][CONTEXT_ELEMENT_RESPONSE][CONTEXT_ELEMENT][ENTITY_ID][ENTITY_TYPE_XML] = res [1]
            NOTIFICATION[content][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST][CONTEXT_ELEMENT_RESPONSE][CONTEXT_ELEMENT][ENTITY_ID][ID] = res [0]
        else:
            NOTIFICATION[content][CONTEXT_RESPONSE_JSON][0][CONTEXT_ELEMENT][TYPE] = res [1]
            NOTIFICATION[content][CONTEXT_RESPONSE_JSON][0][CONTEXT_ELEMENT][ID] = res [0]

    def __createPayload (self, attributesNumber, compound, metadatasNumber, content):
        """
        create payload to Notifications
        :param content:
        :param attributesNumber:
        :param metadatasNumber:
        """
        self.__splitResource(content)
        world.attrs = self.__appendAttributes(attributesNumber, compound, metadatasNumber, content)
        if content == XML:
            NOTIFICATION[content][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST] [CONTEXT_ELEMENT_RESPONSE][CONTEXT_ELEMENT][CONTEXT_ATTRIBUTE_LIST][CONTEXT_ATTRIBUTE] = world.attrs
        else:
            NOTIFICATION[content][CONTEXT_RESPONSE_JSON][0][CONTEXT_ELEMENT][ATTRIBUTE_JSON] = world.attrs
        return utils.convertDictToStr(NOTIFICATION[content], content)

    #-----------------
    def notification (self, organization, resource, content, attributesNumber, compoundNumber, metadatasNumber, error):
        """
        Create or update a dataset/resource in ckan and wait for 3 secs until is stored in ckan
        :param attributesNumber:
        :param metadatasNumber:
        :param organization:
        :param resource:
        :param content:
        :param error:
        """
        delayTimeForCKAN = 4
        notify = NOTIFY
        if organization == WITHOUT_ORGANIZATION: notify = NOTIFY_ERROR
        elif organization == WITH_100: world.organization = WITH_100_VALUE [:-len (world.dataset_default)]
        elif organization == WITH_64: world.organization = WITH_64_VALUE_ORG [:-len (world.mysql_prefix)]
        elif organization == LARGE_THAN_100: world.organization = utils.stringGenerator(101)                       # used in ckan
        elif organization == LARGE_THAN_64 : world.organization = utils.stringGenerator(65)                        # used in mysql
        elif resource == WITH_100: world.resource = WITH_100_VALUE                                                 # used in ckan
        elif resource == WITH_64 : world.resource = WITH_64_VALUE_RESOURCE [:-len (world.mysql_prefix)]            # used in mysql
        elif resource == LARGE_THAN_64 : world.resource = utils.stringGenerator(35) +"-"+utils.stringGenerator(31) # used in mysql
        else:
             if organization != DEFAULT : world.organization = organization
             if resource != DEFAULT : world.resource = resource
        if attributesNumber != DEFAULT: world.attrsNumber = int(attributesNumber)
        if compoundNumber != DEFAULT: world.compoundNumber = int(compoundNumber)
        if metadatasNumber != DEFAULT: world.metadatasNumber = int(metadatasNumber)
        payload = self.__createPayload(world.attrsNumber, world.compoundNumber, world.metadatasNumber, content)
        world.response, world.body = http.request2(POST, self.__createUrl(NOTIFY), self.__createHeaders(notify, content), payload, TRUE, error)
        time.sleep(delayTimeForCKAN)  # delay for N secs while it is storing in ckan


