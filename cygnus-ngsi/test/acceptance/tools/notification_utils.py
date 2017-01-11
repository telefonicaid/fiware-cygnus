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

from decimal import Decimal

from tools import general_utils, http_utils

# general constants
EMPTY         = u''
XML           = u'xml'
JSON          = u'json'
RANDOM        = u'random'
RANDOM_NUMBER = u'random number='
RANDOM_ALPHANUMERIC = u'random alphanumeric='

# headers constants
HEADER_ACCEPT                             = u'Accept'
HEADER_CONTENT_TYPE                       = u'Content-Type'
HEADER_APPLICATION                        = u'application/'
HEADER_TENANT                             = u'Fiware-Service'
HEADER_SERVICE_PATH                       = u'Fiware-ServicePath'
HEADER_USER_AGENT                         = u'User-Agent'

# notifications init constants
POST                                      = u'POST'
NOTIF_USER_AGENT                          = u'notif_user_agent'
NOTIF_USER_AGENT_DEFAULT                  = u'orion/0.10.0'
NOTIF_TENANT_DEFAULT                      = u'tenant'
NOTIF_SERVICE_PATH_DEFAULT                = u'service_path'
NOTIF_CONTENT                             = u'content'

# notification fields request constants
NOTIFY_CONTEXT_REQUEST                    = u'notifyContextRequest'
CONTEXT_RESPONSE_LIST                     = u'contextResponseList'
CONTEXT_ELEMENT_RESPONSE                  = u'contextElementResponse'
CONTEXT_ELEMENT                           = u'contextElement'
CONTEXT_ATTRIBUTE_LIST                    = u'contextAttributeList'
CONTEXT_ATTRIBUTE                         = u'contextAttribute'
CONTEXT_RESPONSES                         = u'contextResponses'
ATTRIBUTES                                = u'attributes'
ENTITY_ID                                 = u'entityId'
ID                                        = u'id'
TYPE                                      = u'type'
ENTITY_TYPE_XML                           = u'@type'
ENTITY_PATTERN                            = u'@isPattern'
PATTERN_JSON                              = u'isPattern'
PATTERN_VALUE                             = u'false'
SUBSCRIPTION_ID                           = u'subscriptionId'
SUBSCRIPTION_ID_VALUE                     = u'51c0ac9ed714fb3b37d7d5a8'
ORIGINATOR                                = u'originator'
ORIGINATOR_VALUE                          = u'localhost'
STATUS_CODE                               = u'statusCode'
CODE                                      = u'code'
CODE_VALUE                                = u'200'
REASON_PHRASE                             = u'reasonPhrase'
REASON_PHRASE_VALUE                       = u'OK'
CONTEXT_METADATA                          = u'contextMetadata'
METADATA                                  = u'metadata'
METADATAS                                 = u'metadatas'
NAME                                      = u'name'
VALUE                                     = u'value'
CONTENT_VALUE                             = u'contextValue'

NOTIFICATION = {
    XML: {
            NOTIFY_CONTEXT_REQUEST: {
                CONTEXT_RESPONSE_LIST: {
                    CONTEXT_ELEMENT_RESPONSE: {
                        CONTEXT_ELEMENT: {
                            CONTEXT_ATTRIBUTE_LIST: {
                                CONTEXT_ATTRIBUTE: []
                            }
                        }
                    }
                }
            }
    },
    JSON: {
            CONTEXT_RESPONSES: [{
                  CONTEXT_ELEMENT: {
                      ATTRIBUTES: []
                  }
            }]
    }
}


class Notifications:
    """
    Generate notifications
    """

    def __init__(self, notif_endpoint_url, **kwargs):
        """
        constructor
        :param endpoint_url: endpoint url used in notification request (MANDATORY)
        :param user_agent: User-Agent header used in notification request (OPTIONAL)
        :param tenant_default: Fiware_Service header. Tenant by default used in notification request (OPTIONAL)
        :param service_path: Fiware_ServicePath header. Service path by default used in notification request (OPTIONAL)
        :param content: Content_Type and Accept headers (xml | json) (OPTIONAL)
        """
        self.endpoint_url = notif_endpoint_url
        self.user_agent = kwargs.get(NOTIF_USER_AGENT, NOTIF_USER_AGENT_DEFAULT)
        self.tenant = kwargs.get(NOTIF_TENANT_DEFAULT.lower(), NOTIF_TENANT_DEFAULT)
        self.service_path = kwargs.get(NOTIF_SERVICE_PATH_DEFAULT.lower(), NOTIF_SERVICE_PATH_DEFAULT)
        self.content = kwargs.get(NOTIF_CONTENT, JSON)
        self.attrs = []
        self.metadatas = None

    def  __create_headers(self):
        """
        create the header for different requests
        :return: headers dictionary
        """
        return {HEADER_ACCEPT: HEADER_APPLICATION + self.content, HEADER_CONTENT_TYPE  : HEADER_APPLICATION + self.content, HEADER_TENANT: self.tenant, HEADER_USER_AGENT: self.user_agent, HEADER_SERVICE_PATH: self.service_path}

    def __insert_identity_id_and_identity_type (self, identity_id, identity_type):
        """
        insert several fields in the request:
            - identityId, identityType, pattern, subscriptionId, originator and statusCode
        """
        if self.content == XML:
            NOTIFICATION [XML][NOTIFY_CONTEXT_REQUEST][SUBSCRIPTION_ID] = SUBSCRIPTION_ID_VALUE
            NOTIFICATION [XML][NOTIFY_CONTEXT_REQUEST][ORIGINATOR] = ORIGINATOR_VALUE
            NOTIFICATION [XML][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST][CONTEXT_ELEMENT_RESPONSE][CONTEXT_ELEMENT][ENTITY_ID]= {ENTITY_TYPE_XML: identity_type, ENTITY_PATTERN: PATTERN_VALUE, ID: identity_id}
            NOTIFICATION [XML][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST][CONTEXT_ELEMENT_RESPONSE][STATUS_CODE] = {CODE: CODE_VALUE, REASON_PHRASE: REASON_PHRASE_VALUE}
        else:
            NOTIFICATION [JSON][SUBSCRIPTION_ID] = SUBSCRIPTION_ID_VALUE
            NOTIFICATION [JSON][ORIGINATOR] = ORIGINATOR_VALUE
            NOTIFICATION [JSON][CONTEXT_RESPONSES][0][CONTEXT_ELEMENT][ID] = identity_id
            NOTIFICATION [JSON][CONTEXT_RESPONSES][0][CONTEXT_ELEMENT][TYPE] =  identity_type
            NOTIFICATION [JSON][CONTEXT_RESPONSES][0][CONTEXT_ELEMENT][PATTERN_JSON] = PATTERN_VALUE
            NOTIFICATION [JSON][CONTEXT_RESPONSES][0][STATUS_CODE] = {CODE: CODE_VALUE, REASON_PHRASE: REASON_PHRASE_VALUE}

    def __create_payload(self):
        """
        generate payload to Notifications
        """
        if self.content == XML:
            NOTIFICATION[self.content][NOTIFY_CONTEXT_REQUEST][CONTEXT_RESPONSE_LIST][CONTEXT_ELEMENT_RESPONSE][CONTEXT_ELEMENT][CONTEXT_ATTRIBUTE_LIST][CONTEXT_ATTRIBUTE] = self.attrs
        else:
            NOTIFICATION[self.content][CONTEXT_RESPONSES][0][CONTEXT_ELEMENT][ATTRIBUTES] = self.attrs
        return general_utils.convert_dict_to_str(NOTIFICATION[self.content], self.content)

    def __new_metadata(self, name, type, value):
        """
         create a metadata
        :param name: metadata name (OPTIONAL: random)
        :param type: metadata type (OPTIONAL: random)
        :param value: metadata value (OPTIONAL: random)
        :return: metadata dictionary
        """
        if name == RANDOM:
            name = 'name_' + general_utils.string_generator(4)
        if type == RANDOM:
            type = 'type_' + general_utils.string_generator(4)
        if value == RANDOM:
            value = general_utils.string_generator(4)
        return {NAME: name, TYPE: type, VALUE: value}

    def __append_attribute(self, name, type, value, metadatas):
        """
         create a attribute
        :param name: attribute name (OPTIONAL: random)
        :param type: attribute type (OPTIONAL: random)
        :param value: attribute value (OPTIONAL: random)
        :param metadata: metadata attribute
        :return: attribute dictionary
        """
        if self.content == XML:
            dict_temp = {NAME: name, TYPE: type, CONTENT_VALUE: value}
            if metadatas != None: dict_temp[METADATA] = metadatas
        else:
            dict_temp =  {NAME: name, TYPE: type, VALUE: value}
            if metadatas != None: dict_temp[METADATAS] = metadatas
        return dict_temp

    #  ---  public methods  ---- #

    def create_metadatas_attribute (self, number, name, type, value):
        """
        append the metadatas list
        :param number: quantity metadatas per attributes
        :param name: metadata name (OPTIONAL: random)
        :param type: metadata type (OPTIONAL: random)
        :param value: metadata value (OPTIONAL: random)
        :return: metadatas attribute list
        """
        self.attributes_metadata_number = number
        if self.attributes_metadata_number <= 0: return None
        contextMetadatasList = []
        for i in range(int(self.attributes_metadata_number)):
            contextMetadatasList.append(self.__new_metadata(name, type, value))
        if self.content == XML:
            self.metadatas = {CONTEXT_METADATA:contextMetadatasList}
        else:
            self.metadatas = contextMetadatasList
        return self.metadatas

    def create_attributes (self, number, name, type, value):
        """
        create attributes to Notifications
        :param number: number of attributes
        :param name: prefix to attribute name. ex: temperature --> temperature_0
        :param type: attributes type
        :param value: attributes value or in same cases could you use random values ( random (alphanumeric with 4 characters only) | random alphanumeric | random number)
        :return attributes list
        """
        self.attrs = []
        self.attributes_number = number
        if name == RANDOM:
            self.attributes_name = 'name_' + general_utils.string_generator(4)
        else:
            self.attributes_name = name
        if type == RANDOM:
            self.attribute_type = 'type_' + general_utils.string_generator(4)
        else:
            self.attribute_type = type

        if value == RANDOM:
            self.attributes_value = general_utils.string_generator(4)
        elif value.find(RANDOM_ALPHANUMERIC) >= 0:
            length = int(value.split("=")[1])
            self.attributes_value = general_utils.string_generator(length)
        elif value.find(RANDOM_NUMBER) >= 0:
            length = int(value.split("=")[1])
            self.attributes_value = general_utils.number_generator(length)
        else:
            self.attributes_value = value
        self.attributes_value = str(self.attributes_value)
        for i in range(0,int(self.attributes_number)):
            self.attrs.append(self.__append_attribute(self.attributes_name+"_"+str(i), self.attribute_type, self.attributes_value, self.metadatas))
        return self.attrs

    def get_attributes_name(self):
        """
        Get attribute name
        :return: string
        """
        return self.attributes_name

    def get_attributes_value(self):
        """
         Get attribute value
        :return: string
        """
        return self.attributes_value

    def send_notification(self, identity_id, identity_type):
        """
        send a new notification
        Note: remmenber to create some attribute
        :param identity_id: identityId use in notification request
        :param identity_type: identityType use in notification request
        :param attrs: attributes list
        :return: response (code status, headers and body)
        """
        assert len(self.attrs) != 0, \
            " ERROR - It is necessary to create the attributes previously. See create_attributes() method... "
        self.identity_id = identity_id
        self.identity_type = identity_type
        self.__insert_identity_id_and_identity_type (self.identity_id, self.identity_type)
        payload = self.__create_payload()
        return  http_utils.request(http_utils.POST, url=self.endpoint_url, headers=self.__create_headers(), data=payload)

    def get_attributes (self):
        """
        Get attributes
        :return: attributes dictionary
        """
        return self.attrs

    def get_attributes_number (self):
        """
        Get attributes number
        :return: attributes number
        """
        return self.attributes_number

    def get_identities (self):
        """
        Get identityId and IdentityTpe
        :return: identityId and IdentityTpe duple
        """
        return self.identity_id, self.identity_type

    def get_services (self):
        """
        Get Service (tenant) and servicePath
        :return: service (tenant) and servicePath duple
        """
        return self.tenant, self.service_path

    def get_attributes_metadata_number (self):
        """
        Get metadata attributes number
        :return: metadata attributes number
        """
        return self.attributes_metadata_number


