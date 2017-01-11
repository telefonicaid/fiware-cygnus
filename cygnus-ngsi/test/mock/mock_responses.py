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
#  iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

import time
import socket
import json

#constants
HOST_NAME            = '0.0.0.0'
HTTP                 = u'http'
HTTPS                = u'https'
PROTOCOL             = HTTP
PORT_NUMBER          = 8090
CERTIFICATE_HTTPS    = u''
MOCK_HOST            = socket.getfqdn() # Return mock hostname

# values by default
DATASET_DEFAULT      = u'fiware-test'
ORGANIZATION         = u'orga_default'
RESOURCES            = ['Room1-Room', 'Room2-Room', 'Room3-Room', 'modelogw_assetgw-device', 'Room2-HOUSE', 'Room2-', 'ROOM-house', 'modelogw_assetgw-device']
RESOURCE             = u'room1-room'
HADOOP               = u'webhdfs'
HADOOP_USER          = u'username'

DATASET              = ORGANIZATION+"_"+DATASET_DEFAULT
HADOOP_LOCATION_URL  = PROTOCOL+"://"+MOCK_HOST+":"+str(PORT_NUMBER)
HADOOP_FILE_PATH     = u'%s/%s/%s.txt' % (ORGANIZATION, RESOURCE,  RESOURCE)

NAME                 = u'name'
OWNER_ORG            = u'owner_org'
RESOURCE_ID          = u'resource_id'
PATH                 = u'path'
BODY                 = u'body'
CODE                 = u'code'
METHOD               = u'method'
GET                  = u'GET '
POST                 = u'POST'
PUT                  = u'PUT '
WARN                 = u'WARN - '
ERROR                = u'ERROR - '

#headers
EMPTY                = u''
OK                   = 200
CREATED              = 201
REDIRECT             = 307
PATH_ERROR           = WARN + u' your path is wrong'
CONTENT_TYPE         = u'Content-type'
CONTENT_LENGTH       = u'Content-Length'
LOCATION             = u'Location'
PRAGMA               = u'Pragma'
DATE                 = u'Date'
CACHE_CONTROL        = u'Cache-Control'
ACCESS_CONTROL_ALLOW_ORIGIN  = u'Access-Control-Allow-Origin'
ACCESS_CONTROL_ALLOW_METHODS = u'Access-Control-Allow-Methods'
ACCESS_CONTROL_ALLOW_HEADERS = u'Access-Control-Allow-Headers'
CONNECTION           = u'Connection'
AGE                  = u'Age'
EXPIRES              = u'Expires'
TRANSFER_ENCODING    = u'Transfer-Encoding'
SET_COOKIE           = u'Set_Cookie'
APP_JSON_HADOOP      = u'application/json'
APP_JSON_CKAN        = u'application/json;charset=utf-8 '
NO_CACHE             = u'no-cache'
METHODS              = u'POST, PUT, GET, DELETE, OPTIONS'
ACCESS_ALLOW_HEADERS = u'X-CKAN-API-KEY, Authorization, Content-Type'
KEEP_ALIVE           = u'Keep-Alive'
DATE_VALUE           = (time.strftime("%a, %d %b %Y %H:%M:%S GMT"))
EXPIRES_VALUE        = u'Thu, 01-Jan-1970 00:00:00 GMT'
CHUNKED              = u'chunked'
SET_COOKIE_VALUE     = u'hadoop.auth=\"u=username&p=username&t=simple&e=1412642504703&s=KKIbNLYXIuBi94sGfNUm1X3A9Dg=\";Path=/'

headersCKAN = {CONTENT_TYPE: APP_JSON_CKAN,
               CONTENT_LENGTH: 0,
               PRAGMA: NO_CACHE,
               CACHE_CONTROL: NO_CACHE,
               ACCESS_CONTROL_ALLOW_ORIGIN: "*",
               ACCESS_CONTROL_ALLOW_METHODS: METHODS,
               ACCESS_CONTROL_ALLOW_HEADERS: ACCESS_ALLOW_HEADERS,
               CONNECTION: KEEP_ALIVE,
               AGE: 0
}

headersHADOOP = {CACHE_CONTROL: NO_CACHE,
                 EXPIRES: EXPIRES_VALUE,
                 DATE: DATE_VALUE,
                 PRAGMA: NO_CACHE,
                 DATE: DATE_VALUE,
                 PRAGMA: NO_CACHE,
                 SET_COOKIE: SET_COOKIE_VALUE,
                 CONTENT_TYPE: APP_JSON_HADOOP,
                 CONTENT_LENGTH: 0
}

#CKAN
CKAN_VERSION_PATH                = u'/api/util/status'
CKAN_ORGANIZATION_PATH           = u'organization_show'   # ex: /api/3/action/organization_show?id=orga_default
CKAN_ORGANIZATION_CREATE_PATH    = u'organization_create'
CKAN_PACKAGE_CREATE_PATH         = u'package_create'
CKAN_RESOURCE_CREATE_PATH        = u'resource_create'
CKAN_DATASTORE_CREATE_PATH       = u'datastore_create'
CKAN_DISCOVER_RESOURCES_PATH     = u'package_show'        # ex: /api/3/action/package_show?id=orga_default_fiware-test
CKAN_INSERT_ROW_PATH             = u'datastore_upsert'
CKAN_DATASTORE_SEARCH_PATH       = u'datastore_search'

CKAN_VERSION_RESPONSE            = u'{"ckan_version": "2.0", "site_url": "", "site_description": "", "site_title": "CKAN Development", "error_emails_to": "you@yourdomain.com", "locale_default": "en", "extensions": ["stats", "text_preview", "recline_preview", "datastore"]}'
CKAN_ORGANIZATION_SHOW_RESPONSE  = u'{"help": "Return the details of a organization.", "success": true, "result": {"users": [{"openid": null, "about": null, "apikey": "3d20cdae-44ad-4eeb-a8b7-8de485140133", "capacity": "admin", "name": "admin", "created": "2014-10-09T17:47:49.077113", "reset_key": null, "email": "admin@admim.com", "sysadmin": false, "activity_streams_email_notifications": false, "email_hash": "1643b58b8150973cf5cf2d06d9372d03", "number_of_edits": 49, "number_administered_packages": 6, "display_name": "admin", "fullname": "admin", "id": "ccac7ce8-4159-4467-89f7-6ae9b05e2ee7"}], "display_name": "%s", "description": "", "title": "%s", "package_count": 2, "created": "2014-10-10T10:14:29.041392", "approval_status": "approved", "is_organization": true, "state": "active", "extras": [], "image_url": "http://www.mipage.es", "groups": [], "num_followers": 0, "revision_id": "f8d267c4-4f32-4668-b59c-16d9243ff93c", "packages": [{"owner_org": "e5065988-2132-4c6a-9d55-0ad1b2de54b4", "maintainer": null, "name": "%s", "author": null, "url": null, "capacity": "organization", "notes": "", "title": "%s", "private": false, "maintainer_email": null, "author_email": null, "state": "active", "version": null, "license_id": null, "revision_id": "a781ffa2-f99a-4a78-af98-02e17efcccf3", "type": "dataset", "id": "865d12ea-8550-4fbc-a239-f8a52008c5ce"}], "type": "organization", "id": "e5065988-2132-4c6a-9d55-0ad1b2de54b4", "tags": [], "name": "%s"}}'
CKAN_ORGANIZATION_CREATE_RESPONSE= u'{"help": "Create a new organization.", "success": true, "result": {"users": [{"openid": null, "about": null, "capacity": "admin", "name": "admin", "created": "2014-02-11T16:32:26.795157", "email_hash": "453124d6f80d05ba8139184a1474cdac", "sysadmin": true, "activity_streams_email_notifications": false, "state": "active", "number_of_edits": 1045, "number_administered_packages": 184, "display_name": "FIWARE Administrator", "fullname": "FIWARE Administrator", "id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83"}], "display_name": "%s", "description": "", "image_display_url": "", "title": "", "package_count": 0, "created": "2014-11-04T10:47:12.091433", "approval_status": "approved", "is_organization": true, "state": "active", "extras": [], "image_url": "", "groups": [], "revision_id": "387058ab-a12d-47f3-bb98-d65345a0ce82", "packages": [], "type": "organization", "id": "af2db883-72cb-4803-b52a-fa06efd84c05", "tags": [], "name": "%s"}}'
CKAN_PACKAGE_CREATE_RESPONSE     = u'{"help": "Create a new dataset - package.", "success": true, "result": {"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-11-04T10:14:09.254885", "id": "e9ab2733-0e71-47f5-a159-5cf3669c4c3e", "metadata_created": "2014-11-04T10:14:09.254885", "metadata_modified": "2014-11-04T10:14:09.263657", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [], "num_resources": 0, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": {"description": "", "created": "2014-08-19T09:57:57.217850", "title": "", "name": "%s", "revision_timestamp": "2014-08-19T07:57:57.190205", "is_organization": true, "state": "active", "image_url": "", "revision_id": "23483ac9-73a7-4f78-afed-d93afdd03326", "type": "organization", "id": "6d0f116a-0aa1-4e11-9c0a-94c3fc596e93", "approval_status": "approved"}, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "6d0f116a-0aa1-4e11-9c0a-94c3fc596e93", "extras": [], "title": "%s", "revision_id": "d4ed8f3e-1a08-414f-ac84-0510eb4d1bae"}}'
CKAN_RESOURCE_CREATE_RESPONSE    = u'{"help": "Appends a new resource to a datasets list of resources. ", "success": true, "result": {"resource_group_id": "a1eda311-2510-4411-92ee-897bd17f4fae", "cache_last_updated": null, "revision_timestamp": "2014-10-02T14:08:56.154981", "webstore_last_updated": null, "id": "b3dd26e9-5660-4950-8f9d-b180e3e8eb59", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "mimetype_inner": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-10-02T16:08:56.168481", "url": "http://foo.bar/newresourcecol", "webstore_url": null, "last_modified": null, "position": 0, "revision_id": "a8f9c016-4752-43c8-9e1a-0b4f1e2e764e", "resource_type": null}}'
CKAN_DATASTORE_CREATE_RESPONSE   = u'{"help": "Adds a new table to the DataStore. ", "success": true, "result": {"fields": [{"type": "timestamp", "id": "recvTime"}, {"type": "json", "id": "temperature"}, {"type": "json", "id": "temperature_md"}, {"type": "json", "id": "pressure"}, {"type": "json", "id": "pressure_md"}, {"type": "json", "id": "humidity"}, {"type": "json", "id": "humidity_md"}], "force": true, "method": "insert", "resource_id": "%s"}}'
CKAN_RESOURCE_ONE                = u'{"resource_group_id":"b647103b-6dc4-4d08-a508-74dc869434fb","cache_last_updated":null,"revision_timestamp":"2014-11-04T15:14:25.246592","webstore_last_updated":null,"datastore_active":true,"id":"3ee246ab-603d-4896-a152-e21985989a34","size":null,"state":"active","hash":"","description":"","format":"","tracking_summary":{"total":0,"recent":0},"last_modified":null,"url_type":null,"mimetype":null,"cache_url":null,"name":"%s","created":"2014-11-04T16:14:25.261137","url":"http://foo.bar/newresourcecol","webstore_url":null,"mimetype_inner":null,"position":0,"revision_id":"39df1b64-8f9b-471c-b209-98a48d55825f","resource_type":null}'
CKAN_DISCOVER_RESOURCES_RESPONSE = u'{"help":"Return the metadata of a dataset (package) and its resources","success":true,"result":{"license_title":null,"maintainer":null,"relationships_as_object":[],"private":false,"maintainer_email":null,"revision_timestamp":"2014-11-04T10:14:09.254885","id":"e9ab2733-0e71-47f5-a159-5cf3669c4c3e","metadata_created":"2014-11-04T10:14:09.254885","metadata_modified":"2014-11-04T16:03:52.906351","author":null,"author_email":null,"state":"active","version":null,"creator_user_id":"54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83","type":"dataset","resources":[%s],"num_resources":%d,"tags":[],"tracking_summary":{"total":0,"recent":0},"groups":[],"license_id":null,"relationships_as_subject":[],"num_tags":0,"organization":{"description":"","created":"2014-08-19T09:57:57.217850","title":"","name":"%s","revision_timestamp":"2014-08-19T07:57:57.190205","is_organization":true,"state":"active","image_url":"","revision_id":"23483ac9-73a7-4f78-afed-d93afdd03326","type":"organization","id":"6d0f116a-0aa1-4e11-9c0a-94c3fc596e93","approval_status":"approved"},"name":"%s","isopen":false,"url":null,"notes":null,"owner_org":"6d0f116a-0aa1-4e11-9c0a-94c3fc596e93","extras":[],"title":"%s","revision_id":"d4ed8f3e-1a08-414f-ac84-0510eb4d1bae"}}'
CKAN_INSERT_ROW_RESPONSE         = u'{"help": "Updates or inserts into a table in the DataStore    The datastore_upsert API action allows you to add or edit records to    an existing DataStore resource. ", "success": true, "result": {"records": [{"attrName": "teperature", "attrType": "centigrade", "recvTime": "2014-10-07T10:30:06.305", "recvTimeTs": "1412670606", "attrValue": "720"}], "force": true, "method": "insert", "resource_id": "6b819c69-1494-416f-a44e-3a39d4b9a687"}}'

#HADOOP
HADOOP_FILE_EXIST_PATH           = u'getfilestatus'
HADOOP_CREATE_DIRECTORY_PATH     = u'mkdirs'
HADOOP_CREATE_FILE_REDIRECT_PATH = u'create'
HADOOP_CREATE_FILE_LOCATION_PATH = u'namenoderpcaddress'
HADOOP_APPEND_FILE_REDIRECT_PATH = u'append'
HADOOP_APPEND_FILE_LOCATION_PATH = u'namenoderpcaddress'

HADOOP_FILE_EXIST_RESPONSE           = u'{"FileStatus":{"accessTime":1411659643100,"blockSize":134217728,"childrenNum":0,"fileId":17379,"group":"hdfs","length":18244,"modificationTime":1411659643193,"owner":"%s","pathSuffix":"","permission":"755","replication":3,"type":"FILE"}}' % (HADOOP_USER)
HADOOP_CREATE_DIRECTORY_RESPONSE     = u'{boolean: true}'
HADOOP_CREATE_FILE_REDIRECT_RESPONSE = u''
HADOOP_CREATE_FILE_REDIRECT_LOCATION = u'/webhdfs/v1/user/username/orga_default/Room1-Room/Room1-Room.txt?op=create&user.name=username&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false'
HADOOP_CREATE_FILE_RESPONSE          = u''
HADOOP_APPEND_FILE_REDIRECT_RESPONSE = u''
HADOOP_APPEND_FILE_REDIRECT_LOCATION = u'/webhdfs/v1/user/username/orga_default/Room1-Room/Room1-Room.txt?op=append&user.name=username&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false'
HADOOP_APPEND_FILE_RESPONSE          = u''


responseBody = [{METHOD: GET,  PATH: CKAN_VERSION_PATH,                CODE: OK,       BODY: CKAN_VERSION_RESPONSE,                LOCATION: None},
                {METHOD: GET,  PATH: CKAN_ORGANIZATION_PATH,           CODE: OK,       BODY: CKAN_ORGANIZATION_SHOW_RESPONSE,      LOCATION: None},
                {METHOD: POST, PATH: CKAN_ORGANIZATION_CREATE_PATH,    CODE: OK,       BODY: CKAN_ORGANIZATION_CREATE_RESPONSE,    LOCATION: None},
                {METHOD: POST, PATH: CKAN_PACKAGE_CREATE_PATH,         CODE: OK,       BODY: CKAN_PACKAGE_CREATE_RESPONSE,         LOCATION: None},
                {METHOD: POST, PATH: CKAN_RESOURCE_CREATE_PATH,        CODE: OK,       BODY: CKAN_RESOURCE_CREATE_RESPONSE,        LOCATION: None},
                {METHOD: POST, PATH: CKAN_DATASTORE_CREATE_PATH,       CODE: OK,       BODY: CKAN_DATASTORE_CREATE_RESPONSE,       LOCATION: None},
                {METHOD: GET,  PATH: CKAN_DISCOVER_RESOURCES_PATH,     CODE: OK,       BODY: CKAN_DISCOVER_RESOURCES_RESPONSE,     LOCATION: None},
                {METHOD: POST, PATH: CKAN_INSERT_ROW_PATH,             CODE: OK,       BODY: CKAN_INSERT_ROW_RESPONSE,             LOCATION: None},
                {METHOD: GET,  PATH: HADOOP_FILE_EXIST_PATH,           CODE: OK,       BODY: HADOOP_FILE_EXIST_RESPONSE,           LOCATION: None},
                {METHOD: PUT,  PATH: HADOOP_CREATE_DIRECTORY_PATH,     CODE: OK,       BODY: HADOOP_CREATE_DIRECTORY_RESPONSE,     LOCATION: None},
                {METHOD: PUT,  PATH: HADOOP_CREATE_FILE_REDIRECT_PATH, CODE: REDIRECT, BODY: HADOOP_CREATE_FILE_REDIRECT_RESPONSE, LOCATION: HADOOP_CREATE_FILE_REDIRECT_LOCATION},
                {METHOD: PUT,  PATH: HADOOP_CREATE_FILE_LOCATION_PATH, CODE: CREATED,  BODY: HADOOP_CREATE_FILE_RESPONSE,          LOCATION: None},
                {METHOD: POST, PATH: HADOOP_APPEND_FILE_REDIRECT_PATH, CODE: REDIRECT, BODY: HADOOP_APPEND_FILE_REDIRECT_RESPONSE, LOCATION: HADOOP_APPEND_FILE_REDIRECT_LOCATION},
                {METHOD: POST, PATH: HADOOP_APPEND_FILE_LOCATION_PATH, CODE: OK,       BODY: HADOOP_APPEND_FILE_RESPONSE,          LOCATION: None}
]

def __usage():
    """
    usage message
    """
    print " ***********************************************************************************************************"
    print " *  usage: python cygnus_mock.py <-u> <-p=port> <-c=certificate file> <-dd= default dataset>               *"
    print " *           ex: python cygnus_mock.py -p=8092 -c=server.pem  -dd=fiware-test                              *"
    print " *                                                                                                         *"
    print " *  parameters:                                                                                            *"
    print " *         -u: show this usage.                                                                            *"
    print " *         -p: change of mock port (by default 8090).                                                      *"
    print " *         -c: certificate path and file used in https protocol.                                           *"
    print " *        -dd: default dataset, obligatory in ckan  (by default \"fiware-test\").                              *"
    print " *                                                                                                         *"
    print " *  Comments:                                                                                              *"
    print " *         Default Dataset is prefixed by organization name to ensure uniqueness ant it.                   *"
    print " *            Must be purely lowercase alphanumeric (ascii) characters,                                    *"
    print " *            plus \"-\" and \"_\" acording to CKAN limitations.                                               *"
    print " *         HTTP protocol: the certificate file is not necessary.                                           *"
    print " *         HTTPS protocol: the certificate file is  necessary.                                             *"
    print " *            how to create certificate file:                                                              *"
    print " *                openssl req -new -x509 -keyout <file>.pem -out <file>.pem -days 365 -nodes               *"
    print " *                                                                                                         *"
    print " *                                     ( use <Ctrl-C> to stop )                                            *"
    print " ***********************************************************************************************************"
    exit(0)

def __config_print():
    """
    show of the current configuration and te paths mocked
    """
    print " ***********************************************************************************************************"
    print " * Current configuration:"
    print " *      protocol         : "+ PROTOCOL
    print " *      mock host        : "+ MOCK_HOST
    print " *      port             : "+ str(PORT_NUMBER)
    if CERTIFICATE_HTTPS != u'': print " *    certificate        : "+ CERTIFICATE_HTTPS
    print " *     organization      : "+ ORGANIZATION
    print " *     dataset           : "+ DATASET
    print " *     resources allowed : "+ str(RESOURCES)
    print " *     hdfs user         : "+ HADOOP_USER
    print " ***********************************************************************************************************"
    print " * Paths mocked:"
    print " *    1 - GET  - 200 -- /api/util/status"
    print " *    2 - GET  - 200 -- /api/3/action/organization_show?id=orga_default"
    print " *    3 - POST - 200 -- /api/3/action/organization_create"
    print " *    4 - POST - 200 -- /api/3/action/package_create"
    print " *    5 - POST - 200 -- /api/3/action/resource_create"
    print " *    6 - POST - 200 -- /api/3/action/datastore_create"
    print " *    7 - GET  - 200 -- /api/3/action/package_show?id=orga_default_fiware-test"
    print " *    8 - POST - 200 -- /api/3/action/datastore_upsert"
    print " *    9 - GET  - 200 -- /webhdfs/v1/user/username/orga_default/room1-room/room1-room.txt?op=getfilestatus&user.name=username"
    print " *   10 - PUT  - 200 -- /webhdfs/v1/user/username/orga_default?op=mkdirs&user.name=username"
    print " *   11 - PUT  - 307 -- /webhdfs/v1/user/username/orga_default/room1-room/room1-room.txt?op=create&user.name=username"
    print " *   12 - PUT  - 201 -- /webhdfs/v1/user/username/orga_default/room1-room/room1-room.txt?op=create&user.name=username&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false"
    print " *   13 - POST - 307 -- /webhdfs/v1/user/username/orga_default/room1-room/room1-room.txt?op=append&user.name=username"
    print " *   14 - POST - 200 -- /webhdfs/v1/user/username/orga_default/room1-room/room1-room.txt?op=append&user.name=username&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false"
    print " *                                                                                                          "
    print " *                                     ( use <Ctrl-C> to stop )                                             "
    print " ***********************************************************************************************************"

def configuration (arguments):
    """
    Define values for configuration
    :param arguments: parameters in command line
    """
    global PORT_NUMBER, CERTIFICATE_HTTPS, PROTOCOL, DATASET_DEFAULT, DATASET, HADOOP_LOCATION_URL
    for i in range(len(arguments)):
        if arguments[i].find('-u') >= 0: __usage()
        try:
            if arguments[i].find('-p') >= 0:
                 errorMsg = "port parameter"
                 PORT_NUMBER = int (str(arguments[i]).split("=")[1])
            if arguments[i].find('-c') >= 0:
                errorMsg = "certificate parameter"
                CERTIFICATE_HTTPS = str(arguments[i]).split("=")[1]
                PROTOCOL = HTTPS
                HADOOP_LOCATION_URL  = PROTOCOL+"://"+MOCK_HOST+":"+str(PORT_NUMBER)

            if arguments[i].find('-dd') >= 0:
                errorMsg = "certificate parameter"
                DATASET_DEFAULT = str(arguments[i]).split("=")[1]
                DATASET         = ORGANIZATION+"_"+DATASET_DEFAULT
        except Exception, e:
            print ERROR+" in "+errorMsg+" see usage below -  "+ str(e)
            __usage()
    __config_print()

def __appendResources ():
    """
    create a resources list to body response
    :return: string
    """
    resourceList = CKAN_RESOURCE_ONE % (RESOURCE)
    for i in range (len(RESOURCES)):
        resourceList = resourceList + ", "+ CKAN_RESOURCE_ONE % (RESOURCES[i])
    return resourceList, len(RESOURCES)+1

def __updateConfiguration (path, payload):
    """
    update values from request to create response body
    :param path:
    :param payload:
    """
    errorMsg = ""
    global responseBody #, ORGANIZATION, DATASET, RESOURCE, RESOURCES
    try:
        if path.find(responseBody[1][PATH]) > 0:
            ORGANIZATION = str(path).split("=")[1]
            DATASET      = ORGANIZATION+"_"+DATASET_DEFAULT
            responseBody[1][BODY] = CKAN_ORGANIZATION_SHOW_RESPONSE %(ORGANIZATION, ORGANIZATION, DATASET, DATASET, ORGANIZATION)
        elif path.find(responseBody[2][PATH]) > 0 and payload != None:
            errorMsg =  "in request payload (Create a new organization)"
            ORGANIZATION = json.loads(payload)[NAME]         #{"name": "new _org"}
            responseBody[2][BODY] = CKAN_ORGANIZATION_CREATE_RESPONSE % (ORGANIZATION, ORGANIZATION)
        elif path.find(responseBody[3][PATH]) > 0 and payload != None:
            errorMsg = "in request payload (Create a new package to an organization)"
            dictTemp = json.loads(payload)
            ORGANIZATION = dictTemp[OWNER_ORG]               #{"name": "new_dataset",   "owner_org": "orga" }
            DATASET = dictTemp [NAME]
            responseBody[3][BODY] = CKAN_PACKAGE_CREATE_RESPONSE % (ORGANIZATION, DATASET, DATASET)
        elif path.find(responseBody[4][PATH]) > 0 and payload != None:
            errorMsg = "in request payload (Appends a new resource to a datasets)"
            dictTemp = json.loads(payload)
            RESOURCE = dictTemp [NAME]                       #{   "name": "newresource",   "url": "http://foo.bar/newresourcecol",   "package_id": "e9ab2733-0e71-47f5-a159-5cf3669c4c3e"}
            responseBody[4][BODY] = CKAN_RESOURCE_CREATE_RESPONSE % (RESOURCE)
        elif path.find(responseBody[5][PATH]) > 0 and payload != None:
            errorMsg = "in request payload (Adds a new table to the DataStore in a resource)"
            dictTemp = json.loads(payload)
            resource_id = dictTemp [RESOURCE_ID]                    # {"resource_id": "3ee246ab-603d-4896-a152-e21985989a34", "fields": [{ "id": "recvTime", "type": "timestamp"},{"id": "temperature", "type": "json" },{ "id": "temperature_md", "type": "json" },{ "id": "pressure", "type": "json" },{ "id": "pressure_md", "type": "json" },{ "id": "humidity", "type": "json" },{ "id": "humidity_md", "type": "json" }],   "force": "true"}
            responseBody[5][BODY] = CKAN_DATASTORE_CREATE_RESPONSE % (resource_id)
        elif path.find(responseBody[6][PATH]) > 0:
            DATASET = str(path).split("=")[1]
            ORGANIZATION = DATASET[:DATASET.find(DATASET_DEFAULT)-1]  # get the organization name from dataset
            resourceList, resourceNum  = __appendResources ()
            responseBody[6][BODY] = CKAN_DISCOVER_RESOURCES_RESPONSE % (resourceList, resourceNum, ORGANIZATION, DATASET, DATASET)
    except Exception, e:
         return ERROR + errorMsg+" "+ str (e)

def response (path, payload=None):
    """
    return body response associated to a path type
    :param type: (ex: CKAN_VERSION, etc)
    :return: body response associated to a path type
    :except: generic exception
    """
    global  HADOOP_LOCATION
    try:
        error = __updateConfiguration(path, payload)
        if error != None: return error, EMPTY  # only error is returned,
        path = path.lower()
        for i in range(len(responseBody)):
            if path.find(responseBody[i][PATH])>= 0:
                headersCKAN[CONTENT_LENGTH] = len(responseBody[i][BODY])
                headersHADOOP[CONTENT_LENGTH] = headersCKAN[CONTENT_LENGTH]
                HADOOP_LOCATION = HADOOP_LOCATION_URL + str(responseBody[i][LOCATION])
                if responseBody[i][PATH] == HADOOP_CREATE_FILE_REDIRECT_PATH or responseBody[i][PATH] == HADOOP_CREATE_FILE_REDIRECT_PATH:
                    if path.find(responseBody[i+1][PATH])> 0:
                        return responseBody[i+1][BODY], responseBody[i+1][CODE]
                return  responseBody[i][BODY], responseBody[i][CODE]
        return PATH_ERROR+ ": "+ path, EMPTY
    except Exception, e:
       print ERROR+str(e)

def isHadoop (path):
    """
    determine if the path is for hadoop or not
    :param path:
    :return: boolean
    """
    if path.find (HADOOP) > 0:return True
    else:False


