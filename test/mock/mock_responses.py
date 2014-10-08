# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
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

#constants
import time

HOST_NAME            = '0.0.0.0'
PORT_NUMBER          = 8090
MOCK_HOST            = u'10.95.20.2'
ORGANIZATION_DEFAULT = u'orga_default'
DATASET_DEFAULT      = u'fiware-test'
RESOURCE_DEFAULT     = u'Room1-Room'
NAME                 = u'name'
PATH                 = u'path'
BODY                 = u'body'
CODE                 = u'code'
METHOD               = u'method'
GET                  = u'GET '
POST                 = u'POST'
PUT                  = u'PUT '
HADOOP               = u'webhdfs'

#headers
OK                   = 200
CREATED              = 201
REDIRECT             = 307
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
APP_JSON             = u'application/json'
NO_CACHE             = u'no-cache'
METHODS              = u'POST, PUT, GET, DELETE, OPTIONS'
ACCESS_ALLOW_HEADERS = u'X-CKAN-API-KEY, Authorization, Content-Type'
KEEP_ALIVE           = u'Keep-Alive'
DATE_VALUE           = (time.strftime("%a, %d %b %Y %H:%M:%S GMT"))
EXPIRES_VALUE        = u'Thu, 01-Jan-1970 00:00:00 GMT'
CHUNKED              = u'chunked'
SET_COOKIE_VALUE     = u'hadoop.auth=\"u=cloud-user&p=cloud-user&t=simple&e=1412642504703&s=KKIbNLYXIuBi94sGfNUm1X3A9Dg=\";Path=/'

headersCKAN = {CONTENT_TYPE: APP_JSON,
               CONTENT_LENGTH: 0,
               PRAGMA: NO_CACHE,
               CACHE_CONTROL: NO_CACHE,
               ACCESS_CONTROL_ALLOW_ORIGIN: "*",
               ACCESS_CONTROL_ALLOW_METHODS: METHODS,
               ACCESS_CONTROL_ALLOW_HEADERS: ACCESS_ALLOW_HEADERS,
               CONNECTION: KEEP_ALIVE,
               AGE: 1
}

headersHADOOP = {CACHE_CONTROL: NO_CACHE,
                 EXPIRES: EXPIRES_VALUE,
                 DATE: DATE_VALUE,
                 PRAGMA: NO_CACHE,
                 DATE: DATE_VALUE,
                 PRAGMA: NO_CACHE,
                 SET_COOKIE: SET_COOKIE_VALUE,
                 CONTENT_TYPE: APP_JSON,
                 CONTENT_LENGTH: 0
}

'''
Cache-Control: no-cache
Expires: Thu, 01-Jan-1970 00:00:00 GMT
Date: Tue, 07 Oct 2014 12:37:08 GMT
Pragma: no-cache
Date: Tue, 07 Oct 2014 12:37:08 GMT
Pragma: no-cache
Content-Type: application/json
Set-Cookie: hadoop.auth="u=cloud-user&p=cloud-user&t=simple&e=1412721428110&s=hzC3s6hZPZAEmkJQEjDgfmvXpSg=";Path=/
Transfer-Encoding: chunked
Server: Jetty(6.1.26)

'''

#CKAN
CKAN_VERSION                     = u'ckan_version'
CKAN_ORGANIZATION_SHOW           = u'ckan_organization_show'
CKAN_ORGANIZATION_CREATE         = u'ckan_organization_create'
CKAN_PACKAGE_CREATE              = u'ckan_package_create'
CKAN_RESOURCE_CREATE             = u'ckan_resource_create'
CKAN_DATASTORE_CREATE            = u'ckan_datastore_create'
CKAN_DISCOVER_RESOURCES          = u'ckan_discover_resource'
CKAN_INSERT_ROW                  = u'ckan_insert_row'

CKAN_VERSION_PATH                = u'/api/util/status'
CKAN_ORGANIZATION_PATH           = u'/api/3/action/organization_show?id=%s'   # ex: /api/3/action/organization_show?id=orga_default
CKAN_ORGANIZATION_CREATE_PATH    = u'/api/3/action/organization_create'
CKAN_PACKAGE_CREATE_PATH         = u'/api/3/action/package_create'
CKAN_RESOURCE_CREATE_PATH        = u'/api/3/action/resource_create'
CKAN_DATASTORE_CREATE_PATH       = u'/api/3/action/datastore_create'
CKAN_DISCOVER_RESOURCES_PATH     = u'/api/3/action/package_show?id=%s'        # ex: /api/3/action/package_show?id=orga_default_fiware-test
CKAN_INSERT_ROW_PATH             = u'/api/3/action/datastore_upsert'

CKAN_VERSION_RESPONSE            = u'{"ckan_version": "2.2", "site_url": "", "site_description": "", "site_title": "CKAN Development", "error_emails_to": "you@yourdomain.com", "locale_default": "en", "extensions": ["stats", "text_preview", "recline_preview", "datastore"]}'
CKAN_ORGANIZATION_SHOW_RESPONSE  = u'{"help": "Return the details of a organization. ", "success": true, "result": {"users": [{"openid": null, "about": null, "capacity": "admin", "name": "fiware", "created": "2014-02-11T16:32:26.795157", "email_hash": "453124d6f80d05ba8139184a1474cdac", "sysadmin": true, "activity_streams_email_notifications": false, "state": "active", "number_of_edits": 934, "number_administered_packages": 154, "display_name": "FI-WARE Administrator", "fullname": "FI-WARE Administrator", "id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83"}], "display_name": "%s", "description": "", "image_display_url": "", "title": "", "package_count": 1, "created": "2014-09-19T10:05:09.645434", "approval_status": "approved", "is_organization": true, "state": "active", "extras": [], "image_url": "", "groups": [], "num_followers": 0, "revision_id": "011b805f-1646-4bb3-a40b-589d4750131a", "packages": [{"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-09-19T08:05:10.081136", "id": "6ecec202-5219-4878-b290-ff5a08154cf6", "metadata_created": "2014-09-19T08:05:10.081136", "metadata_modified": "2014-09-19T11:05:50.655623", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [{"resource_group_id": "67d13406-cce2-4875-9f54-398c7dfff8fc", "cache_last_updated": null, "revision_timestamp": "2014-09-19T11:05:50.654222", "webstore_last_updated": null, "id": "0a31399e-aa24-49ca-abe8-d0f46e1d22ca", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "last_modified": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-09-19T13:05:50.669579", "url": "http://foo.bar/newresource", "webstore_url": null, "mimetype_inner": null, "position": 0, "revision_id": "4b1232c4-9785-4239-9ded-db364767bc0c", "resource_type": null}], "num_resources": 1, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": {"description": "", "created": "2014-09-19T10:05:09.645434", "title": "", "name": "%s", "revision_timestamp": "2014-09-19T08:05:09.620151", "is_organization": true, "state": "active", "image_url": "", "revision_id": "011b805f-1646-4bb3-a40b-589d4750131a", "type": "organization", "id": "0b2f31bb-dcca-4771-bf1a-5e554b78e381", "approval_status": "approved"}, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "0b2f31bb-dcca-4771-bf1a-5e554b78e381", "extras": [], "title": "%s", "revision_id": "bf584836-4112-424b-ab4f-307db0ab73c9"}], "type": "organization", "id": "0b2f31bb-dcca-4771-bf1a-5e554b78e381", "tags": [], "name": "%s"}}'
CKAN_ORGANIZATION_CREATE_RESPONSE= u'{"help": "Create a new organization. ", "success": true, "result": {"users": [{"openid": null, "about": null, "capacity": "admin", "name": "fiware", "created": "2014-02-11T16:32:26.795157", "email_hash": "453124d6f80d05ba8139184a1474cdac", "sysadmin": true, "activity_streams_email_notifications": false, "state": "active", "number_of_edits": 940, "number_administered_packages": 154, "display_name": "FI-WARE Administrator", "fullname": "FI-WARE Administrator", "id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83"}], "display_name": "%s", "description": "", "image_display_url": "", "title": "", "package_count": 0, "created": "2014-10-02T15:41:09.676905", "approval_status": "approved", "is_organization": true, "state": "active", "extras": [], "image_url": "", "groups": [], "revision_id": "c91218a3-b5a9-47c2-9425-a28cc040b47c", "packages": [], "type": "organization", "id": "09f50f44-a472-481d-b662-88e1e0e72ade", "tags": [], "name": "%s"}}'
CKAN_PACKAGE_CREATE_RESPONSE     = u'{"help": "Create a new dataset (package).    You must be authorized to create new datasets. ", "success": true, "result": {"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-10-02T13:57:45.116438", "id": "41db4c0c-d3eb-4ba1-958e-5b9db9ca4a3e", "metadata_created": "2014-10-02T13:57:45.116438", "metadata_modified": "2014-10-02T13:57:45.124769", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [], "num_resources": 0, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": null, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "2f240381-88bc-4f0f-81ae-4c148452fea3", "extras": [], "title": "%s", "revision_id": "22fafb07-bdc5-40ce-9dae-c6b7c8a0e5e4"}}'
CKAN_RESOURCE_CREATE_RESPONSE    = u'{"help": "Appends a new resource to a datasets list of resources. ", "success": true, "result": {"resource_group_id": "a1eda311-2510-4411-92ee-897bd17f4fae", "cache_last_updated": null, "revision_timestamp": "2014-10-02T14:08:56.154981", "webstore_last_updated": null, "id": "b3dd26e9-5660-4950-8f9d-b180e3e8eb59", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "mimetype_inner": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-10-02T16:08:56.168481", "url": "http://foo.bar/newresourcecol", "webstore_url": null, "last_modified": null, "position": 0, "revision_id": "a8f9c016-4752-43c8-9e1a-0b4f1e2e764e", "resource_type": null}}'
CKAN_DATASTORE_CREATE_RESPONSE   = u'{"help": "Adds a new table to the DataStore. ", "success": true, "result": {"fields": [{"type": "timestamp", "id": "recvTime"}, {"type": "json", "id": "temperature"}, {"type": "json", "id": "temperature_md"}, {"type": "json", "id": "pressure"}, {"type": "json", "id": "pressure_md"}, {"type": "json", "id": "humidity"}, {"type": "json", "id": "humidity_md"}], "force": true, "method": "insert", "resource_id": "ec007dae-43ca-4501-b90e-e7d4fe8101c1"}}'
CKAN_DISCOVER_RESOURCES_RESPONSE = u'{"help": "Return the metadata of a dataset (package) and its resources. ", "success": true, "result": {"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-09-23T12:58:43.074777", "id": "de736543-1b40-46d2-a20a-1bce53ff0a63", "metadata_created": "2014-09-23T12:58:43.074777", "metadata_modified": "2014-09-23T12:58:43.650723", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [{"resource_group_id": "93f9737f-cd2b-4fb9-8387-6001e9451e67", "cache_last_updated": null, "revision_timestamp": "2014-09-23T12:58:43.649955", "webstore_last_updated": null, "datastore_active": true, "id": "7f17017a-fba1-41d3-b1f1-8f65c689b6b9", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "last_modified": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-09-23T14:58:43.663541", "url": "http://foo.bar/newresource", "webstore_url": null, "mimetype_inner": null, "position": 0, "revision_id": "58fe82ae-939c-4fe8-bc22-b5055b51894b", "resource_type": null}], "num_resources": 1, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": {"description": "", "created": "2014-09-23T14:58:42.203538", "title": "", "name": "orga-345", "revision_timestamp": "2014-09-23T12:58:42.178558", "is_organization": true, "state": "active", "image_url": "", "revision_id": "6c538718-2ed2-44b2-b1f2-f0cb258825bb", "type": "organization", "id": "2f169682-e7d9-4ca0-9749-a1ceb4f8bbd6", "approval_status": "approved"}, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "2f169682-e7d9-4ca0-9749-a1ceb4f8bbd6", "extras": [], "title": "%s", "revision_id": "edbdcb09-a1ee-4a08-b319-d06f1d3933db"}}'
CKAN_INSERT_ROW_RESPONSE          = u'{"help": "Updates or inserts into a table in the DataStore    The datastore_upsert API action allows you to add or edit records to    an existing DataStore resource. ", "success": true, "result": {"records": [{"attrName": "teperature", "attrType": "centigrade", "recvTime": "2014-10-07T10:30:06.305", "recvTimeTs": "1412670606", "attrValue": "720"}], "force": true, "method": "insert", "resource_id": "6b819c69-1494-416f-a44e-3a39d4b9a687"}}'

#HADOOP
HADOOP_USER                      = u'username'
HADOOP_PREFIX                    = u''
HADOOP_LOCATION_URL              = "http://"+MOCK_HOST+":"+str(PORT_NUMBER)
HADOOP_FILE_PATH                 = u'%s%s/%s%s/%s%s.txt'
HADOOP_FILE_EXIST                = u'hadoop_file_exist'
HADOOP_CREATE_DIRECTORY          = u'hadoop_create_directory'
HADOOP_CREATE_FILE_REDIRECT      = u'hadoop_create_file_redirect'
HADOOP_CREATE_FILE               = u'hadoop_create_file'
HADOOP_APPEND_FILE_REDIRECT      = u'hadoop_append_file_redirect'
HADOOP_APPEND_FILE               = u'haddop_append_file'

HADOOP_FILE_EXIST_PATH           = u'/webhdfs/v1/user/%s/%s?op=getfilestatus&user.name=%s'
HADOOP_CREATE_DIRECTORY_PATH     = u'/webhdfs/v1/user/%s/%s?op=mkdirs&user.name=%s'
HADOOP_CREATE_FILE_REDIRECT_PATH = u'/webhdfs/v1/user/%s/%s?op=create&user.name=%s'
HADOOP_CREATE_FILE_LOCATION_PATH = u'/webhdfs/v1/user/%s/%s?op=create&user.name=%s&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false'
HADOOP_APPEND_FILE_REDIRECT_PATH = u'/webhdfs/v1/user/%s/%s?op=append&user.name=%s'
HADOOP_APPEND_FILE_LOCATION_PATH = u'/webhdfs/v1/user/%s/%s?op=append&user.name=%s&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false'

HADOOP_FILE_EXIST_RESPONSE           = u'{"FileStatus":{"accessTime":1411659643100,"blockSize":134217728,"childrenNum":0,"fileId":17379,"group":"hdfs","length":18244,"modificationTime":1411659643193,"owner":"%s","pathSuffix":"","permission":"755","replication":3,"type":"FILE"}}'
HADOOP_CREATE_DIRECTORY_RESPONSE     = u'{boolean: true}'
HADOOP_CREATE_FILE_REDIRECT_RESPONSE = u''
HADOOP_CREATE_FILE_RESPONSE          = u''
HADOOP_APPEND_FILE_REDIRECT_RESPONSE = u''
HADOOP_APPEND_FILE_RESPONSE          = u''


responseBody = [{NAME: CKAN_VERSION,                METHOD: GET,  PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_ORGANIZATION_SHOW,      METHOD: GET,  PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_ORGANIZATION_CREATE,    METHOD: POST, PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_PACKAGE_CREATE,         METHOD: POST, PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_RESOURCE_CREATE,        METHOD: POST, PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_DATASTORE_CREATE,       METHOD: POST, PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_DISCOVER_RESOURCES,     METHOD: GET,  PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: CKAN_INSERT_ROW,             METHOD: POST, PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: HADOOP_FILE_EXIST,           METHOD: GET,  PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: HADOOP_CREATE_DIRECTORY,     METHOD: PUT,  PATH: None, CODE: OK,       BODY: None, LOCATION: None},
                {NAME: HADOOP_CREATE_FILE_REDIRECT, METHOD: PUT,  PATH: None, CODE: REDIRECT, BODY: None, LOCATION: None},
                {NAME: HADOOP_CREATE_FILE,          METHOD: PUT,  PATH: None, CODE: CREATED,  BODY: None, LOCATION: None},
                {NAME: HADOOP_APPEND_FILE_REDIRECT, METHOD: POST, PATH: None, CODE: REDIRECT, BODY: None, LOCATION: None},
                {NAME: HADOOP_APPEND_FILE,          METHOD: POST, PATH: None, CODE: OK,       BODY: None, LOCATION: None}
]

def usage():
    """
    usage message
    """
    print " ****************************************************************************************"
    print " *  usage: python cygnus_mock.py <port> <organization> <dataset> <resource> <hdfs user> *"
    print " *      values by default:                                                              *"
    print " *           port        : 8090                                                         *"
    print " *           organization: orga_default                                                 *"
    print " *           dataset     : fiware-test                                                  *"
    print " *           resource    : room1-room                                                   *"
    print " *           hdfs user   : username                                                     *"
    print " *       Note: all values will be defined in lowercase.                                 *"
    print " *                  ( use <Ctrl-C> to stop )                                            *"
    print " ****************************************************************************************"

def createPath (type, organization ,dataset, resource):
    """
    Define all paths dynamically and the locations in redirects only
    :param type:   path type
    :param organization: organization used
    :param dataset: dataset used
    :param resource: resource used
    :return: path and location in redirect only
    """
    locationNone = None
    if type == CKAN_VERSION: return CKAN_VERSION_PATH, locationNone
    if type == CKAN_ORGANIZATION_SHOW: return CKAN_ORGANIZATION_PATH % (organization), locationNone
    if type == CKAN_ORGANIZATION_CREATE: return CKAN_ORGANIZATION_CREATE_PATH, locationNone
    if type == CKAN_PACKAGE_CREATE: return CKAN_PACKAGE_CREATE_PATH, locationNone
    if type == CKAN_RESOURCE_CREATE: return CKAN_RESOURCE_CREATE_PATH, locationNone
    if type == CKAN_DATASTORE_CREATE: return CKAN_DATASTORE_CREATE_PATH, locationNone
    if type == CKAN_DISCOVER_RESOURCES: return CKAN_DISCOVER_RESOURCES_PATH % (dataset), locationNone
    if type == CKAN_INSERT_ROW: return CKAN_INSERT_ROW_PATH, locationNone

    if type == HADOOP_FILE_EXIST: return HADOOP_FILE_EXIST_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER), locationNone
    if type == HADOOP_CREATE_DIRECTORY: return HADOOP_CREATE_DIRECTORY_PATH % (HADOOP_USER, ORGANIZATION_DEFAULT, HADOOP_USER), locationNone
    if type == HADOOP_CREATE_FILE_REDIRECT: return HADOOP_CREATE_FILE_REDIRECT_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER), HADOOP_LOCATION_URL + HADOOP_CREATE_FILE_LOCATION_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER)
    if type == HADOOP_CREATE_FILE: return HADOOP_CREATE_FILE_LOCATION_PATH  % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER), locationNone
    if type == HADOOP_APPEND_FILE_REDIRECT: return HADOOP_APPEND_FILE_REDIRECT_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER), HADOOP_LOCATION_URL + HADOOP_APPEND_FILE_LOCATION_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER)
    if type == HADOOP_APPEND_FILE: return HADOOP_APPEND_FILE_LOCATION_PATH  % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER), locationNone

def createBody (type, organization ,dataset, resource):
    """
    Define all payload responses
    :param type:   path type
    :param organization: organization used
    :param dataset: dataset used
    :param resource: resource used
    :return:
    """

    if type == CKAN_VERSION: return CKAN_VERSION_RESPONSE
    if type == CKAN_ORGANIZATION_SHOW: return CKAN_ORGANIZATION_SHOW_RESPONSE % (organization, resource, dataset, dataset, organization, organization)
    if type == CKAN_ORGANIZATION_CREATE: return CKAN_ORGANIZATION_CREATE_RESPONSE % (organization, organization)
    if type == CKAN_PACKAGE_CREATE: return CKAN_PACKAGE_CREATE_RESPONSE % (dataset, dataset)
    if type == CKAN_RESOURCE_CREATE: return CKAN_RESOURCE_CREATE_RESPONSE % (resource)
    if type == CKAN_DATASTORE_CREATE: return CKAN_DATASTORE_CREATE_RESPONSE % ()
    if type == CKAN_DISCOVER_RESOURCES: return CKAN_DISCOVER_RESOURCES_RESPONSE %(resource, dataset, dataset)
    if type == CKAN_INSERT_ROW: return CKAN_INSERT_ROW_RESPONSE

    if type == HADOOP_FILE_EXIST: return HADOOP_FILE_EXIST_RESPONSE % (HADOOP_USER)
    if type == HADOOP_CREATE_DIRECTORY: return HADOOP_CREATE_DIRECTORY_RESPONSE
    if type == HADOOP_CREATE_FILE_REDIRECT:return HADOOP_CREATE_FILE_REDIRECT_RESPONSE
    if type == HADOOP_CREATE_FILE: return HADOOP_CREATE_FILE_RESPONSE
    if type == HADOOP_APPEND_FILE_REDIRECT:return HADOOP_APPEND_FILE_REDIRECT_RESPONSE
    if type == HADOOP_APPEND_FILE: return HADOOP_APPEND_FILE_RESPONSE

def configuration (values):
    """
    Define values for configuration
    :param values: parameters in command line
    """
    global PORT_NUMBER, ORGANIZATION_DEFAULT, DATASET_DEFAULT, RESOURCE_DEFAULT, responseBody, DATASET, HADOOP_USER, HADOOP_FILE_PATH, HADOOP_LOCATION_URL
    if len (values) > 1: PORT_NUMBER          = int(values[1])
    if len (values) > 2: ORGANIZATION_DEFAULT = values[2]
    if len (values) > 3: DATASET_DEFAULT      = values[3]
    if len (values) > 4: RESOURCE_DEFAULT     = values[4]
    if len (values) > 5: HADOOP_USER          = values[5]
    DATASET = ORGANIZATION_DEFAULT+"_"+DATASET_DEFAULT
    HADOOP_FILE_PATH = HADOOP_FILE_PATH % (HADOOP_PREFIX, ORGANIZATION_DEFAULT, HADOOP_PREFIX, RESOURCE_DEFAULT, HADOOP_PREFIX, RESOURCE_DEFAULT)
    for i in range(len(responseBody)):
        responseBody[i][PATH], responseBody[i][LOCATION] = createPath(responseBody[i][NAME], ORGANIZATION_DEFAULT, DATASET , RESOURCE_DEFAULT)
        responseBody[i][BODY] = createBody(responseBody[i][NAME], ORGANIZATION_DEFAULT, DATASET, RESOURCE_DEFAULT)
    return responseBody

def config_print(responseBody):
    """
    show of the current configuration and te paths mocked
    :param responseBody:
    """
    print " * Current configuration:"
    print " *     port        : "+ str(PORT_NUMBER)
    print " *     organization: "+ ORGANIZATION_DEFAULT
    print " *     dataset     : "+ DATASET
    print " *     resource    : "+ RESOURCE_DEFAULT
    print " *     hdfs user   : "+ HADOOP_USER
    print " ****************************************************************************"
    print " * Paths mocked:"
    for i in range(len(responseBody)):
        if i < 9: pos = " "+str(i+1)
        else: pos = str (i+1)
        print " *   "+pos+" - "+ responseBody[i][METHOD] + " - " + str(responseBody[i][CODE]) + " -- " + str (responseBody[i][PATH])
    print " ****************************************************************************"

def response (path):
    """
    return body response associated to a path type
    :param type: (ex: CKAN_VERSION, etc)
    :return: body response associated to a path type
    :except: generic exception
    """
    global HADOOP_LOCATION_URL
    path = path.lower()
    try:
        for i in range(len(responseBody)):
            if responseBody[i][PATH].lower() == path:
                headersCKAN[CONTENT_LENGTH] = len(responseBody[i][BODY])
                HADOOP_LOCATION_URL = responseBody[i][LOCATION]
                return responseBody[i][BODY], responseBody[i][CODE]
        print "WARN - your path is wrong: "+ path
    except Exception, e:
       print "ERROR -  "+ str(e)

def isHadoop (path):
    """
    determine if the path is for hadoop or not
    :param path:
    :return: boolean
    """
    if path.find (HADOOP) > 0:return True
    else:False


