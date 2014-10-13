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


class Notifications:
    world.attrs = None

    def __init__(self, cygnus_url, userAgent, organization_row, organization_col, resource, attrNumber, metadataNumber, compoundNumber, dataset_default, mysql_prefix, hadoop_prefix):
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
        world.organization          = {ROW_TYPE: organization_row, COL_TYPE: organization_col}
        world.resource              = resource
        world.attrsNumber           = attrNumber
        world.metadatasNumber       = metadataNumber
        world.compoundNumber        = compoundNumber
        world.dataset_default       = dataset_default
        world.mysql_prefix          = mysql_prefix
        world.hadoop_prefix         = hadoop_prefix

    def __createUrl(self, operation, resourceId = None):
        """
        create the url for different operations
        :param operation: operation type (dataset, etc)
        :return: request url
        """
        if operation == NOTIFY:
           value = "%s/%s" % (world.cygnus_url, NOTIFY)
        return value

    def verifyCygnus (step, cygnusType):
        world.cygnus_type = cygnusType

    def __createHeaders(self, operation, content="xml"):
        """
        create the header for different requests
        :param operation: different request
        :param content: "xml" or "json"
        :return:
        """
        if operation == NOTIFY:
            return {ACCEPT: APPLICATION_CONTENT + content, CONTENT_TYPE  : APPLICATION_CONTENT + content, FIWARE_SERVICE: world.organization[world.cygnus_type], USER_AGENT: world.userAgent}
        if operation == NOTIFY_ERROR:
            return {ACCEPT: APPLICATION_CONTENT + content, CONTENT_TYPE  : APPLICATION_CONTENT + content, USER_AGENT: world.userAgent}
    #---------------- Request -------------------------
    def __newMetadata(self, content):
        """
        Create a new Metadata
        :return: metadata dictionary
        """
        randomStr = general_utils.stringGenerator(4)
        randomInt = general_utils.numberGenerator(3)
        name = 'name_' + randomStr
        type = 'type_' + randomStr
        value = randomInt
        if general_utils.isXML(content):
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
            randomInt = general_utils.numberGenerator(3)
            value[ITEM+str(i)] = randomInt
        return value

    def __newAttribute(self, compound, metadatasNumber,content):
        """
        Create a new Attribute with n metadatas per row
        :return: attribute dict
        """
        randomStr = general_utils.stringGenerator(6)
        randomInt = general_utils.numberGenerator(3)
        name = 'name_' + randomStr
        type = 'type_' + randomStr
        contextMetadatasList = self.__appendMetadatas(metadatasNumber, content)
        # for compound
        if compound > 0:
            value = self.__newCompound (compound)
        else:
            value = randomInt
        if general_utils.isXML(content):
            return  {NAME: name, TYPE: type, CONTENT_VALUE: value, METADATA: contextMetadatasList}
        else:
            return  {NAME: name, TYPE: type, VALUE_JSON: value, METADATAS_JSON: contextMetadatasList}

    def __newAttribute_column(self, attrValue, MDValue, position, content):
        """
        Create a new Attribute  per column
        :param attrValue: attribute value
        :param MDValue: if it has metadata or not (boolean)
        :param position: attribute position
        :param content: XML or json
        :return: attribute dict
        """
        name = ATTR_NAME+"_"+str(position)
        type = ATTR_TYPE+"_"+str(position)
        #name = ATTRIBUTE_FIELD_LIST[position]
        #type = ATTR_TYPE+"_"+str(position)
        value= attrValue
        if MDValue == TRUE: world.metadatasNumber = 1
        else: world.metadatasNumber = 0
        contextMetadatasList = self.__appendMetadatas(world.metadatasNumber, content)
        if general_utils.isXML(content):
            return  {NAME: name, TYPE: type, CONTENT_VALUE: value, METADATA: contextMetadatasList}
        else:
            return  {NAME: name, TYPE: type, VALUE_JSON: value, METADATAS_JSON: contextMetadatasList}

    def __appendAttributes (self, payloadData):
        """
        appends all attributes
        :param attributesNumber:
        :param metadatasNumber:
        :param content:
        :return:
        """
        attributesNumber = payloadData[ATTRIBUTE_NUMBER]
        compoundsNumber  = payloadData[COMPOUND_NUMBER]
        metadatasNumber  = payloadData[METADATA_NUMBER]
        attrValue        = payloadData[ATTR_VALUE]
        MDValue          = payloadData[METADATA_VALUE]
        content          = payloadData[CONTENT]
        self.attrs = []
        for i in range(int(attributesNumber)):
            if world.cygnus_type == ROW_TYPE:
                t = self.__newAttribute(compoundsNumber, metadatasNumber, content)
            else:
                t = self.__newAttribute_column(attrValue, MDValue, i, content)
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

    def __setPayloadData (self, attributesNumber, attrValue, compoundNumber, metadatasNumber,  MDValue, content):
        """
        Define payload data, in row and column types
        :param attributesNumber: quantity of attribute used in row
        :param attrValue: attribute value used in column
        :param compoundNumber: quantity compound attribute used in row
        :param metadatasNumber: quantity of metadatas used in row
        :param MDValue: metadata value used in column
        :param content: xml o json used in both
        """
        payloadData = {ATTRIBUTE_NUMBER: None,
                         ATTR_VALUE      : None,
                         COMPOUND_NUMBER : None,
                         METADATA_NUMBER : None,
                         METADATA_VALUE  : None,
                         CONTENT         : None}
        payloadData[ATTRIBUTE_NUMBER] = attributesNumber
        payloadData[ATTR_VALUE] = attrValue
        payloadData[COMPOUND_NUMBER] = compoundNumber
        payloadData[METADATA_NUMBER] = metadatasNumber
        payloadData[METADATA_VALUE] = MDValue
        payloadData[CONTENT] = content
        return payloadData

    def __createPayload (self, payloadData):
        """
        create payload to Notifications per row
        :param content:
        :param attributesNumber:
        :param metadatasNumber:
        """
        world.attrs = None
        content = payloadData[CONTENT]
        self.__splitResource(content)
        world.attrs = self.__appendAttributes(payloadData)
        if content == XML:
            NOTIFICATION[content][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST] [CONTEXT_ELEMENT_RESPONSE][CONTEXT_ELEMENT][CONTEXT_ATTRIBUTE_LIST][CONTEXT_ATTRIBUTE] = world.attrs
        else:
            NOTIFICATION[content][CONTEXT_RESPONSE_JSON][0][CONTEXT_ELEMENT][ATTRIBUTE_JSON] = world.attrs

        return general_utils.convertDictToStr(NOTIFICATION[content], content)

    def __mappingQuotes (self, attrValue):
        temp = ""
        for i in range (len(attrValue)):
            if attrValue[i] == "'":  temp = temp + "\""
            else:temp = temp + attrValue[i]
        return temp

    #----------------- Notifications --------------------------
    def notification_row (self, organization, resource, content, attributesNumber, compoundNumber, metadatasNumber, error):
        """
        Create or update a org/dataset/resource per row and wait for 4 secs until it is stored in ckan
        :param attributesNumber:
        :param metadatasNumber:
        :param organization:
        :param resource:
        :param content:
        :param error:
        """

        delayTimeForCKAN = 5
        notify = NOTIFY
        if organization == WITHOUT_ORGANIZATION: notify = NOTIFY_ERROR
        elif organization == WITH_100: world.organization[world.cygnus_type] = WITH_100_VALUE [:-len (world.dataset_default)]            # used in ckan
        elif organization == WITH_32: world.organization[world.cygnus_type] = WITH_32_VALUE_ORG [:-len (world.mysql_prefix)]             # used in mysql
        elif organization == LARGE_THAN_100: world.organization[world.cygnus_type] = general_utils.stringGenerator(101)                          # used in ckan
        elif organization == LARGE_THAN_32 : world.organization[world.cygnus_type] = general_utils.stringGenerator(33)                           # used in mysql
        elif resource == WITH_100: world.resource = WITH_100_VALUE                                                    # used in ckan
        elif resource == WITH_64 : world.resource = WITH_64_VALUE_RESOURCE [:-len (world.mysql_prefix)]               # used in mysql
        elif resource == LARGE_THAN_32 : world.resource = general_utils.stringGenerator(20) + "-" + general_utils.stringGenerator(12) # used in mysql
        else:
            if organization != DEFAULT : world.organization[world.cygnus_type] = organization
            if resource != DEFAULT : world.resource = resource
        if attributesNumber != DEFAULT: world.attrsNumber = int(attributesNumber)
        if compoundNumber != DEFAULT: world.compoundNumber = int(compoundNumber)
        if metadatasNumber != DEFAULT: world.metadatasNumber = int(metadatasNumber)

        payload = self.__createPayload(self.__setPayloadData (world.attrsNumber, None, world.compoundNumber, world.metadatasNumber,  None, content))
        world.response, world.body = http_utils.request2(POST, self.__createUrl(NOTIFY), self.__createHeaders(notify, content), payload, TRUE, error)
        time.sleep(delayTimeForCKAN)  # delay for N secs while it is storing in ckan

    def notification_col (self, attrValue, MDValue, content, error):
        """
        Create or update a org/dataset/resource per column and wait for 4 secs until it is stored in ckan
        :param organization:
        :param resource:
        :param content:
        :param attrValue:
        :param MDValue:
        :param error:
        """

        if world.operation == "mysql": attrValue = self.__mappingQuotes (attrValue)
        delayTimeForCKAN = 5
        if attrValue != DEFAULT: world.attrsValue = attrValue
        if MDValue != DEFAULT: world.metadataValue = MDValue
        payload = self.__createPayload(self.__setPayloadData (world.attrsNumber, world.attrsValue, None, None, world.metadataValue, content))
        world.response, world.body = http_utils.request2(POST, self.__createUrl(NOTIFY), self.__createHeaders(NOTIFY, content), payload, TRUE, error)
        time.sleep(delayTimeForCKAN)  # delay for N secs while it is storing in ckan


