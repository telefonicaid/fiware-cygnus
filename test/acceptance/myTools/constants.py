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

### constants
NOT   = u'NOT'
EMPTY = u''
FALSE = u'False'
TRUE  = u'True'
DEFAULT = u'default'

#Contents
XML = u'xml'
JSON = u'json'

#HTTP status code
status_codes = {"OK": 200,
                "Created": 201,
                "No Content": 204,
                "Moved Permanently": 301,
                "Redirect": 307,
                "Bad Request": 400,
                "unauthorized": 401,
                "Not Found": 404,
                "Bad Method": 405,
                "Not Acceptable":406,
                "Conflict": 409,
                "Unsupported Media Type": 415,
                "Internal Server Error": 500}

#HTTP methods
GET = u'GET'
POST = u'POST'
PUT = u'PUT'
DELETE = u'DELETE'

#HTTP Headers
ACCEPT              = u'Accept'
CONTENT_TYPE        = u'Content-Type'
APPLICATION_CONTENT = u"application/"
FIWARE_SERVICE      = u"Fiware-Service"
AUTHORIZATION       = u'Authorization'
USER_AGENT          = u'User-Agent'


#ERRORs
ERROR = { NOT: None}

#cygnus
CKAN                 =  u'ckan'
MYSQL                =  u'mysql'
COSMOS               =  u'cosmos'
NOTIFY               = u'notify'
NOTIFY_ERROR         = u'notify_error'
WITHOUT_ORGANIZATION = u'without organization'
WITH_100             = u'with 100 characters'
WITH_64              = u'with 64 characters'
LARGE_THAN_100       = u'large than 100 characters'
LARGE_THAN_64        = u'large than 64 characters'

#CKAN API
VERSION                = u'ckan_version'
DATASET                = u'dataset'
RESOURCE_SEARCH        = u'resource_search'
RESOURCE_SEARCH_OFFSET = u'resource_search_offset'
RESOURCE_ID            = u'resource_Id'
RESOURCE               = u'resources'
CKAN_HEADER            = u'ckan_header'
PATH_VERSION_CKAN      = u'api/util/status'
PATH_DATASET_SEARCH    = u'api/action/datastore_search?resource_id='
PATH_API_REST_DATASET  = u'api/rest/dataset'
OFFSET                 = u'&offset='

#REQUEST constants
#Notification
NOTIFY_CONTEXT_REQUEST   = u'notifyContextRequest'
SUBSCRIPTION_ID          = u'subscriptionId'
ORIGINATOR               = u'originator'
CONTEXT_RESPONSE_LIST    = u'contextResponseList'
CONTEXT_ELEMENT_RESPONSE = u'contextElementResponse'
CONTEXT_RESPONSE_JSON    = u'contextResponses'
CONTEXT_ELEMENT          = u'contextElement'
ENTITY_ID                = u'entityId'
ENTITY_TYPE_XML          = u'@type'
ENTITY_PATTERN_JSON      = u'isPattern'
ENTITY_PATTERN           = u'@isPattern'
ID                       = u'id'
CONTEXT_ATTRIBUTE_LIST   = u'contextAttributeList'
CONTEXT_ATTRIBUTE        = u'contextAttribute'
ATTRIBUTE_JSON           = u'attributes'
NAME                     = u'name'
TYPE                     = u'type'
CONTENT_VALUE            = u'contextValue'
VALUE_JSON               = u'value'
METADATA                 = u'metadata'
CONTEXT_METADATA         = u'contextMetadata'
METADATAS_JSON           = u'metadatas'
STATUS_CODE              = u'statusCode'
CODE                     = u'code'
REASON_PHRASE            = u'reasonPhrase'
ITEM                     = u'item'

#Happy path constants
SUBS_ID_VALUE =  "51c0ac9ed714fb3b37d7d5a8"
ORIG_VALUE =  "localhost"
ENTITY_TYPE_VALUE = u'Room'
PATERN_VALUE = u'false'
ID_VALUE    = u'Room1'
TEMP_NAME = u'temperature'
TEMP_TYPE = u'centigrade'
TEMP_VALUE  = "6.5"
PRESS_NAME = u'pressure'
PRESS_TYPE = u'mmhg'
PRESS_VALUE = "720"
CODE_VALUE = "200"
REASON_PHRASE_VALUE = "OK"
#WITH_100_VALUE = u'1234567890abcdefghij1234567890klmnopqrst1234567890uvwxyzzzz-1234567891234567890123456789'
WITH_100_VALUE = u'1234567890abcdefghij1234567890klmnopqrst1234567890uvwxyzzzz-12345678912345678901234567890abcdefghij'
WITH_64_VALUE_ORG = u'a234567890abcdefghij1234567890jklmnopqrst1234567890uvwxyzzzz01'
WITH_64_VALUE_RESOURCE = u'a234567890abcdefghij1234567890-klmnopqrst1234567890uvwxyzzzz01'
                 #1234567890abcdefghij1234567890klmnopqrst1234567890uv

NOTIFICATION = {
    XML: {
            NOTIFY_CONTEXT_REQUEST: {
                SUBSCRIPTION_ID: SUBS_ID_VALUE,
                ORIGINATOR: ORIG_VALUE,
                CONTEXT_RESPONSE_LIST: {
                    CONTEXT_ELEMENT_RESPONSE: {
                        CONTEXT_ELEMENT: {
                            ENTITY_ID: {ENTITY_TYPE_XML: ENTITY_TYPE_VALUE, ENTITY_PATTERN: PATERN_VALUE, ID: ID_VALUE},
                            CONTEXT_ATTRIBUTE_LIST: {
                                CONTEXT_ATTRIBUTE: [None]
                            }
                        },
                        STATUS_CODE: {
                            CODE: CODE_VALUE,
                            REASON_PHRASE: REASON_PHRASE_VALUE
                        }
                    }
                }
            }
    },
    JSON: {
            SUBSCRIPTION_ID: SUBS_ID_VALUE,
            ORIGINATOR: ORIG_VALUE,
            CONTEXT_RESPONSE_JSON: [{
                  CONTEXT_ELEMENT: {
                      ATTRIBUTE_JSON: [None],
                      TYPE: ENTITY_TYPE_VALUE,
                      ENTITY_PATTERN_JSON: PATERN_VALUE,
                      ID: ID_VALUE,
                  },
                  STATUS_CODE: {
                      CODE: CODE_VALUE,
                      REASON_PHRASE: REASON_PHRASE_VALUE
                  }
            }]
    }
}

#RESPONSEs
#

NOT_FOUND             = u'Not found'
RESULT                = u'result'
TOTAL                 = u'total'
RECORDS               = u'records'
ATTR_NAME             = u'attrName'
ATTR_TYPE             = u'attrType'
ATTR_VALUE            = u'attrValue'
ATTR_MD               = u'attrMd'
VALIDATE_DATA_MSG     = u'...Wrong data stored in ckan: '
VALIDATE_DATASET_MSG  = u'...Dataset does not exist, is possible it was has deleted: '
VALIDATE_DATASET_MSG  = u'...Dataset does exist: '
VALIDATE_RESOURCE_MSG = u'resource is missing...'

#MySQL
SELECT_VERSION       = u'SELECT version ()'
VALIDATE_DB_ERROR    = u'Incorrect database name'
VALIDATE_TABLE_ERROR = u'Incorrect table name'
ERROR_DB_MSG      = u'Database paramenter created wrongly...'


