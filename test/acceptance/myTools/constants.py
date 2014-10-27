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
HADDOP               =  u'hadoop'
NOTIFY               = u'notify'
NOTIFY_ERROR         = u'notify_error'
WITHOUT_ORGANIZATION = u'without organization'
ORGANIZATION_MISSING = u'organization_missing'
ORGANIZATION_WITHOUT_DATASET = u'organization_without_dataset'
RESOURCE_MISSING     = u'resource-missing'
DATASTORE_MISSING    = u'datastore-missing'
WITH_100             = u'with 100 characters'
WITH_64              = u'with 64 characters'
WITH_32              = u'with 32 characters'
LARGE_THAN_100       = u'large than 100 characters'
LARGE_THAN_64        = u'large than 64 characters'
LARGE_THAN_32        = u'large than 32 characters'
ROW_TYPE             = u'row'
COL_TYPE             = u'column'

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
ORGANIZATION_CREATE    = u'organization_create'
ORGANIZATION_LIST      = u'organization_list'
PACKAGE_CREATE         = u'package_create'
PACKAGE_SHOW           = u'package_show?id='
RESOURCE_CREATE        = u'resource_create'
DATASTORE_CREATE       = u'datastore_create'
PATH_API_CREATE        = u'api/3/action'
OWNER_ORG              = u'owner_org'
URL                    = u'url'
URL_EXAMPLE            = u'http://foo.bar/newresource'
PACKAGE_ID             = u'package_id'
RESOURCE_ID            = u'resource_id'
NUM_RESOURCE           = u'num_resources'
FIELD                  = u'fields'
RECVTIME               ='recvTime'
TIMESTAMP              = u'timestamp'
FORCE                  = u'force'
WITHOUT_METADATA_FIELD = u'without metadata field'
ORGANIZATION_SHOW      = u'organization_show'

#mySQL
MYSQL_ERROR_MESSAGE   = u'DB exception: '
MYSQL_CREATE_DATABASE = u'CREATE DATABASE IF NOT EXISTS '
MYSQL_CREATE_TABLE    = u'CREATE TABLE IF NOT EXISTS '
MYSQL_DROP_DATABASE   = u'DROP SCHEMA IF EXISTS '
MYSQL_SHOW_DATABASE   = u'SHOW DATABASES'
MYSQL_SHOW_TABLES     = u'SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = \''


#hadoop
OPEN_FILE                 = 'OPEN'
DELETE_FILE               = 'DELETE'
PATH_HADOOP               = u'webhdfs/v1/user'
QUERY_PARAM_HADOOP        = u'txt?op'
QUERY_PARAM_HADOOP_DELETE = u'recursive=true&user.name'
QUERY_PARAM_HADOOP_OPEN   = u'user.name'
HADOOP_VERSION_PATH       = u'ws/v1/cluster/info'
CLUSTER_INFO              = u'clusterInfo'
HADOOP_VERSION            = u'hadoopVersion'
STARTED                   = u'STARTED'
HADOOP_STATE              = u'state'


ATTRIBUTE_FIELD_LIST   = [u'temperature', u'pressure', u'humidity', u'speed', u'volumen', u'time', u'length', u'mass', u'density', u'power', u'acceleration', u'conductivity', u'capacity', u'resistance', u'energy', u'consumption', u'torque', u'area', u'viscosity', u'inductance', u'weight', ]

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
ATTRIBUTE_NUMBER         = u'attributesNumber'
ATTR_VALUE               = u'attrValue'
COMPOUND_NUMBER          = u'compoundNumber'
METADATA_NUMBER          = u'metadatasNumber'
METADATA_VALUE           = u'MDValue'
CONTENT                  = u'content'

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
WITH_100_VALUE = u'1234567890abcdefghij1234567890klmnopqrst1234567890uvwxyzzzz-12345678912345678901234567890abcdefghij'
WITH_64_VALUE_ORG = u'a234567890abcdefghij1234567890jklmnopqrst1234567890uvwxyzzzz01'
WITH_32_VALUE_ORG = u'a234567890abcdefghij1234567890j'
WITH_64_VALUE_RESOURCE = u'a234567890abcdefghij1234567890-klmnopqrst1234567890uvwxyzzzz01'
NAME_IS_MISSING   = u'Name is missing'


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
VALIDATE_RESOURCE_IS_NOT_EMPTY_MSG = u'...Resource is not empty: '

#MySQL
SELECT_VERSION       = u'SELECT version ()'
VALIDATE_DB_ERROR    = u'Incorrect database name'
VALIDATE_TABLE_ERROR = u'Incorrect table name'
ERROR_DB_MSG      = u'Database paramenter created wrongly...'


