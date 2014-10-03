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
HOST_NAME = '0.0.0.0'
PORT_NUMBER = 8090
ORGANIZATION_DEFAULT = u'orga_default'
DATASET_DEFAULT      = u'fiware-test'
RESOURCE_DEFAULT     = u'Room1-Room'
NAME                 = u'name'
PATH                 = u'path'
BODY                 = u'body'
METHOD               = u'method'
GET                  = u'GET '
POST                 = u'POST'
PUT                  = u'PUT '

#headers
OK                   = 200
CREATED              = 201
CONTENT_TYPE         = u'Content-type'
LOCATION             = u'Location'
CONTENT_LENGTH       = u'Content-Length'
APP_JSON             = u'application/json'

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

CKAN_VERSION_RESPONSE            = u'{"ckan_version": "2.0", "site_url": "", "site_description": "", "site_title": "CKAN Development", "error_emails_to": "you@yourdomain.com", "locale_default": "en", "extensions": ["stats", "text_preview", "recline_preview", "datastore"]}'
CKAN_ORGANIZATION_SHOW_RESPONSE  = u'{"help": "Return the details of a organization.\n\n    :param id: the id or name of the organization\n    :type id: string\n    :param include_datasets: include a list of the organization\'s datasets\n         (optional, default: ``True``)\n    :type id: boolean\n\n    :rtype: dictionary\n\n    .. note:: Only its first 1000 datasets are returned\n    ", "success": true, "result": {"users": [{"openid": null, "about": null, "capacity": "admin", "name": "fiware", "created": "2014-02-11T16:32:26.795157", "email_hash": "453124d6f80d05ba8139184a1474cdac", "sysadmin": true, "activity_streams_email_notifications": false, "state": "active", "number_of_edits": 934, "number_administered_packages": 154, "display_name": "FI-WARE Administrator", "fullname": "FI-WARE Administrator", "id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83"}], "display_name": "%s", "description": "", "image_display_url": "", "title": "", "package_count": 1, "created": "2014-09-19T10:05:09.645434", "approval_status": "approved", "is_organization": true, "state": "active", "extras": [], "image_url": "", "groups": [], "num_followers": 0, "revision_id": "011b805f-1646-4bb3-a40b-589d4750131a", "packages": [{"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-09-19T08:05:10.081136", "id": "6ecec202-5219-4878-b290-ff5a08154cf6", "metadata_created": "2014-09-19T08:05:10.081136", "metadata_modified": "2014-09-19T11:05:50.655623", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [{"resource_group_id": "67d13406-cce2-4875-9f54-398c7dfff8fc", "cache_last_updated": null, "revision_timestamp": "2014-09-19T11:05:50.654222", "webstore_last_updated": null, "id": "0a31399e-aa24-49ca-abe8-d0f46e1d22ca", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "last_modified": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-09-19T13:05:50.669579", "url": "http://foo.bar/newresource", "webstore_url": null, "mimetype_inner": null, "position": 0, "revision_id": "4b1232c4-9785-4239-9ded-db364767bc0c", "resource_type": null}], "num_resources": 1, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": {"description": "", "created": "2014-09-19T10:05:09.645434", "title": "", "name": "%s", "revision_timestamp": "2014-09-19T08:05:09.620151", "is_organization": true, "state": "active", "image_url": "", "revision_id": "011b805f-1646-4bb3-a40b-589d4750131a", "type": "organization", "id": "0b2f31bb-dcca-4771-bf1a-5e554b78e381", "approval_status": "approved"}, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "0b2f31bb-dcca-4771-bf1a-5e554b78e381", "extras": [], "title": "%s", "revision_id": "bf584836-4112-424b-ab4f-307db0ab73c9"}], "type": "organization", "id": "0b2f31bb-dcca-4771-bf1a-5e554b78e381", "tags": [], "name": "%s"}}'
CKAN_ORGANIZATION_CREATE_RESPONSE= u'{"help": "Create a new organization.\n\n    You must be authorized to create organizations.\n\n    Plugins may change the parameters of this function depending on the value\n    of the ``type`` parameter, see the ``IGroupForm`` plugin interface.\n\n    :param name: the name of the organization, a string between 2 and 100 characters\n        long, containing only lowercase alphanumeric characters, ``-`` and\n        ``_``\n    :type name: string\n    :param id: the id of the organization (optional)\n    :type id: string\n    :param title: the title of the organization (optional)\n    :type title: string\n    :param description: the description of the organization (optional)\n    :type description: string\n    :param image_url: the URL to an image to be displayed on the organization\'s page\n        (optional)\n    :type image_url: string\n    :param state: the current state of the organization, e.g. ``\'active\'`` or\n        ``\'deleted\'``, only active organizations show up in search results and\n        other lists of organizations, this parameter will be ignored if you are not\n        authorized to change the state of the organization (optional, default:\n        ``\'active\'``)\n    :type state: string\n    :param approval_status: (optional)\n    :type approval_status: string\n    :param extras: the organization\'s extras (optional), extras are arbitrary\n        (key: value) metadata items that can be added to organizations, each extra\n        dictionary should have keys ``\'key\'`` (a string), ``\'value\'`` (a\n        string), and optionally ``\'deleted\'``\n    :type extras: list of dataset extra dictionaries\n    :param packages: the datasets (packages) that belong to the organization, a list\n        of dictionaries each with keys ``\'name\'`` (string, the id or name of\n        the dataset) and optionally ``\'title\'`` (string, the title of the\n        dataset)\n    :type packages: list of dictionaries\n    :param users: the users that belong to the organization, a list of dictionaries\n        each with key ``\'name\'`` (string, the id or name of the user) and\n        optionally ``\'capacity\'`` (string, the capacity in which the user is\n        a member of the organization)\n    :type users: list of dictionaries\n\n    :returns: the newly created organization\n    :rtype: dictionary\n\n    ", "success": true, "result": {"users": [{"openid": null, "about": null, "capacity": "admin", "name": "fiware", "created": "2014-02-11T16:32:26.795157", "email_hash": "453124d6f80d05ba8139184a1474cdac", "sysadmin": true, "activity_streams_email_notifications": false, "state": "active", "number_of_edits": 940, "number_administered_packages": 154, "display_name": "FI-WARE Administrator", "fullname": "FI-WARE Administrator", "id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83"}], "display_name": "%s", "description": "", "image_display_url": "", "title": "", "package_count": 0, "created": "2014-10-02T15:41:09.676905", "approval_status": "approved", "is_organization": true, "state": "active", "extras": [], "image_url": "", "groups": [], "revision_id": "c91218a3-b5a9-47c2-9425-a28cc040b47c", "packages": [], "type": "organization", "id": "09f50f44-a472-481d-b662-88e1e0e72ade", "tags": [], "name": "%s"}}'
CKAN_PACKAGE_CREATE_RESPONSE     = u'{"help": "Create a new dataset (package).\n\n    You must be authorized to create new datasets. If you specify any groups\n    for the new dataset, you must also be authorized to edit these groups.\n\n    Plugins may change the parameters of this function depending on the value\n    of the ``type`` parameter, see the ``IDatasetForm`` plugin interface.\n\n    :param name: the name of the new dataset, must be between 2 and 100\n        characters long and contain only lowercase alphanumeric characters,\n        ``-`` and ``_``, e.g. ``\'warandpeace\'`\n    :type name: string\n    :param title: the title of the dataset (optional, default: same as\n        ``name``)\n    :type title: string\n    :param author: the name of the dataset\'s author (optional)\n    :type author: string\n    :param author_email: the email address of the dataset\'s author (optional)\n    :type author_email: string\n    :param maintainer: the name of the dataset\'s maintainer (optional)\n    :type maintainer: string\n    :param maintainer_email: the email address of the dataset\'s maintainer\n        (optional)\n    :type maintainer_email: string\n    :param license_id: the id of the dataset\'s license, see ``license_list()``\n        for available values (optional)\n    :type license_id: license id string\n    :param notes: a description of the dataset (optional)\n    :type notes: string\n    :param url: a URL for the dataset\'s source (optional)\n    :type url: string\n    :param version: (optional)\n    :type version: string, no longer than 100 characters\n    :param state: the current state of the dataset, e.g. ``\'active\'`` or\n        ``\'deleted\'``, only active datasets show up in search results and\n        other lists of datasets, this parameter will be ignored if you are not\n        authorized to change the state of the dataset (optional, default:\n        ``\'active\'``)\n    :type state: string\n    :param type: the type of the dataset (optional), ``IDatasetForm`` plugins\n        associate themselves with different dataset types and provide custom\n        dataset handling behaviour for these types\n    :type type: string\n    :param resources: the dataset\'s resources, see ``resource_create()``\n        for the format of resource dictionaries (optional)\n    :type resources: list of resource dictionaries\n    :param tags: the dataset\'s tags, see ``tag_create()`` for the format\n        of tag dictionaries (optional)\n    :type tags: list of tag dictionaries\n    :param extras: the dataset\'s extras (optional), extras are arbitrary\n        (key: value) metadata items that can be added to datasets, each extra\n        dictionary should have keys ``\'key\'`` (a string), ``\'value\'`` (a\n        string)\n    :type extras: list of dataset extra dictionaries\n    :param relationships_as_object: see ``package_relationship_create()`` for\n        the format of relationship dictionaries (optional)\n    :type relationships_as_object: list of relationship dictionaries\n    :param relationships_as_subject: see ``package_relationship_create()`` for\n        the format of relationship dictionaries (optional)\n    :type relationships_as_subject: list of relationship dictionaries\n    :param groups: the groups to which the dataset belongs (optional), each\n        group dictionary should have one or more of the following keys which\n        identify an existing group:\n        \'id\'`` (the id of the group, string), ``\'name\'`` (the name of the\n        group, string), ``\'title\'`` (the title of the group, string), to see\n        which groups exist call ``group_list()``\n    :type groups: list of dictionaries\n    :param owner_org: the id of the dataset\'s owning organization, see\n        ``organization_list()`` or ``organization_list_for_user`` for\n        available values (optional)\n    :type owner_org: string\n\n    :returns: the newly created dataset (unless \'return_id_only\' is set to True\n              in the context, in which case just the dataset id will be returned)\n    :rtype: dictionary\n\n    ", "success": true, "result": {"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-10-02T13:57:45.116438", "id": "41db4c0c-d3eb-4ba1-958e-5b9db9ca4a3e", "metadata_created": "2014-10-02T13:57:45.116438", "metadata_modified": "2014-10-02T13:57:45.124769", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [], "num_resources": 0, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": null, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "2f240381-88bc-4f0f-81ae-4c148452fea3", "extras": [], "title": "%s", "revision_id": "22fafb07-bdc5-40ce-9dae-c6b7c8a0e5e4"}}'
CKAN_RESOURCE_CREATE_RESPONSE    = u'{"help": "Appends a new resource to a datasets list of resources.\n\n    :param package_id: id of package that the resource needs should be added to.\n    :type package_id: string\n    :param url: url of resource\n    :type url: string\n    :param revision_id: (optional)\n    :type revisiion_id: string\n    :param description: (optional)\n    :type description: string\n    :param format: (optional)\n    :type format: string\n    :param hash: (optional)\n    :type hash: string\n    :param name: (optional)\n    :type name: string\n    :param resource_type: (optional)\n    :type resource_type: string\n    :param mimetype: (optional)\n    :type mimetype: string\n    :param mimetype_inner: (optional)\n    :type mimetype_inner: string\n    :param webstore_url: (optional)\n    :type webstore_url: string\n    :param cache_url: (optional)\n    :type cache_url: string\n    :param size: (optional)\n    :type size: int\n    :param created: (optional)\n    :type created: iso date string\n    :param last_modified: (optional)\n    :type last_modified: iso date string\n    :param cache_last_updated: (optional)\n    :type cache_last_updated: iso date string\n    :param webstore_last_updated: (optional)\n    :type webstore_last_updated: iso date string\n    :param upload: (optional)\n    :type upload: FieldStorage (optional) needs multipart/form-data\n\n    :returns: the newly created resource\n    :rtype: dictionary\n\n    ", "success": true, "result": {"resource_group_id": "a1eda311-2510-4411-92ee-897bd17f4fae", "cache_last_updated": null, "revision_timestamp": "2014-10-02T14:08:56.154981", "webstore_last_updated": null, "id": "b3dd26e9-5660-4950-8f9d-b180e3e8eb59", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "mimetype_inner": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-10-02T16:08:56.168481", "url": "http://foo.bar/newresourcecol", "webstore_url": null, "last_modified": null, "position": 0, "revision_id": "a8f9c016-4752-43c8-9e1a-0b4f1e2e764e", "resource_type": null}}'
CKAN_DATASTORE_CREATE_RESPONSE   = u'{"help": "Adds a new table to the DataStore.\n\n    The datastore_create action allows you to post JSON data to be\n    stored against a resource. This endpoint also supports altering tables,\n    aliases and indexes and bulk insertion. This endpoint can be called multiple\n    times to initially insert more data, add fields, change the aliases or indexes\n    as well as the primary keys.\n\n    To create an empty datastore resource and a CKAN resource at the same time,\n    provide ``resource`` with a valid ``package_id`` and omit the ``resource_id``.\n\n    If you want to create a datastore resource from the content of a file,\n    provide ``resource`` with a valid ``url``.\n\n    See :ref:`fields` and :ref:`records` for details on how to lay out records.\n\n    :param resource_id: resource id that the data is going to be stored against.\n    :type resource_id: string\n    :param force: set to True to edit a read-only resource\n    :type force: bool (optional, default: False)\n    :param resource: resource dictionary that is passed to\n        :meth:`~ckan.logic.action.create.resource_create`.\n        Use instead of ``resource_id`` (optional)\n    :type resource: dictionary\n    :param aliases: names for read only aliases of the resource. (optional)\n    :type aliases: list or comma separated string\n    :param fields: fields/columns and their extra metadata. (optional)\n    :type fields: list of dictionaries\n    :param records: the data, eg: [{\"dob\": \"2005\", \"some_stuff\": [\"a\", \"b\"]}]  (optional)\n    :type records: list of dictionaries\n    :param primary_key: fields that represent a unique key (optional)\n    :type primary_key: list or comma separated string\n    :param indexes: indexes on table (optional)\n    :type indexes: list or comma separated string\n\n    Please note that setting the ``aliases``, ``indexes`` or ``primary_key`` replaces the exising\n    aliases or constraints. Setting ``records`` appends the provided records to the resource.\n\n    **Results:**\n\n    :returns: The newly created data object.\n    :rtype: dictionary\n\n    See :ref:`fields` and :ref:`records` for details on how to lay out records.\n\n    ", "success": true, "result": {"fields": [{"type": "timestamp", "id": "recvTime"}, {"type": "json", "id": "temperature"}, {"type": "json", "id": "temperature_md"}, {"type": "json", "id": "pressure"}, {"type": "json", "id": "pressure_md"}, {"type": "json", "id": "humidity"}, {"type": "json", "id": "humidity_md"}], "force": true, "method": "insert", "resource_id": "ec007dae-43ca-4501-b90e-e7d4fe8101c1"}}'
CKAN_DISCOVER_RESOURCES_RESPONSE = u'{"help": "Return the metadata of a dataset (package) and its resources.\n\n    :param id: the id or name of the dataset\n    :type id: string\n    :param use_default_schema: use default package schema instead of\n        a custom schema defined with an IDatasetForm plugin (default: False)\n    :type use_default_schema: bool\n\n    :rtype: dictionary\n\n    ", "success": true, "result": {"license_title": null, "maintainer": null, "relationships_as_object": [], "private": false, "maintainer_email": null, "revision_timestamp": "2014-09-23T12:58:43.074777", "id": "de736543-1b40-46d2-a20a-1bce53ff0a63", "metadata_created": "2014-09-23T12:58:43.074777", "metadata_modified": "2014-09-23T12:58:43.650723", "author": null, "author_email": null, "state": "active", "version": null, "creator_user_id": "54b2e1e2-fdda-45e1-ab0c-7e3bfd4ebc83", "type": "dataset", "resources": [{"resource_group_id": "93f9737f-cd2b-4fb9-8387-6001e9451e67", "cache_last_updated": null, "revision_timestamp": "2014-09-23T12:58:43.649955", "webstore_last_updated": null, "datastore_active": true, "id": "7f17017a-fba1-41d3-b1f1-8f65c689b6b9", "size": null, "state": "active", "hash": "", "description": "", "format": "", "tracking_summary": {"total": 0, "recent": 0}, "last_modified": null, "url_type": null, "mimetype": null, "cache_url": null, "name": "%s", "created": "2014-09-23T14:58:43.663541", "url": "http://foo.bar/newresource", "webstore_url": null, "mimetype_inner": null, "position": 0, "revision_id": "58fe82ae-939c-4fe8-bc22-b5055b51894b", "resource_type": null}], "num_resources": 1, "tags": [], "tracking_summary": {"total": 0, "recent": 0}, "groups": [], "license_id": null, "relationships_as_subject": [], "num_tags": 0, "organization": {"description": "", "created": "2014-09-23T14:58:42.203538", "title": "", "name": "orga-345", "revision_timestamp": "2014-09-23T12:58:42.178558", "is_organization": true, "state": "active", "image_url": "", "revision_id": "6c538718-2ed2-44b2-b1f2-f0cb258825bb", "type": "organization", "id": "2f169682-e7d9-4ca0-9749-a1ceb4f8bbd6", "approval_status": "approved"}, "name": "%s", "isopen": false, "url": null, "notes": null, "owner_org": "2f169682-e7d9-4ca0-9749-a1ceb4f8bbd6", "extras": [], "title": "%s", "revision_id": "edbdcb09-a1ee-4a08-b319-d06f1d3933db"}}'
CKAN_INSERT_ROW_RESPONSE         = u'{"help": "Updates or inserts into a table in the DataStore\n\n    The datastore_upsert API action allows you to add or edit records to\n    an existing DataStore resource. In order for the *upsert* and *update*\n    methods to work, a unique key has to be defined via the datastore_create\n    action. The available methods are:\n\n    *upsert*\n        Update if record with same key already exists, otherwise insert.\n        Requires unique key.\n    *insert*\n        Insert only. This method is faster that upsert, but will fail if any\n        inserted record matches an existing one. Does *not* require a unique\n        key.\n    *update*\n        Update only. An exception will occur if the key that should be updated\n        does not exist. Requires unique key.\n\n\n    :param resource_id: resource id that the data is going to be stored under.\n    :type resource_id: string\n    :param force: set to True to edit a read-only resource\n    :type force: bool (optional, default: False)\n    :param records: the data, eg: [{\"dob\": \"2005\", \"some_stuff\": [\"a\",\"b\"]}] (optional)\n    :type records: list of dictionaries\n    :param method: the method to use to put the data into the datastore.\n                   Possible options are: upsert, insert, update (optional, default: upsert)\n    :type method: string\n\n    **Results:**\n\n    :returns: The modified data object.\n    :rtype: dictionary\n\n    ", "success": true, "result": {"records": [{"attrType": "centigrade", "recvTime": "2014-06-04T17:23:45.890000", "recvTimeTs": 1381226461, "attrMd": "34", "attrValue": "34", "attrName": "temperature"}], "force": true, "method": "insert", "resource_id": "bce69771-3fd0-433d-a7bf-3743d4737f88"}}'

#HADOOP
HADOOP_USER                      = u'username'
HADOOP_PREFIX                    = u''
HADOOP_FILE_PATH                 = u'%s%s/%s%s/%s%s.txt'
HADOOP_FILE_EXIST                = u'hadoop_file_exist'
HADOOP_CREATE_DIRECTORY          = u'hadoop_create_directory'
HADOOP_CREATE_FILE               = u'hadoop_create_file'
HADOOP_APPEND_FILE               = u'haddop_append_file'

HADOOP_FILE_EXIST_PATH           = u'/webhdfs/v1/user/%s/%s?op=getfilestatus&user.name=%s'
HADOOP_CREATE_DIRECTORY_PATH     = u'/webhdfs/v1/user/%s/%s?op=mkdirs&user.name=%s'
HADOOP_CREATE_FILE_PATH          = u'/webhdfs/v1/user/%s/%s?op=create&user.name=%s'
HADOOP_APPEND_FILE_PATH          = u'/webhdfs/v1/user/%s/%s?op=append&user.name=%s'

HADOOP_FILE_EXIST_RESPONSE       = u'{"FileStatus":{"accessTime":1411659643100,"blockSize":134217728,"childrenNum":0,"fileId":17379,"group":"hdfs","length":18244,"modificationTime":1411659643193,"owner":"%s","pathSuffix":"","permission":"755","replication":3,"type":"FILE"}}'
HADOOP_CREATE_DIRECTORY_RESPONSE = u'{boolean: true}'
HADOOP_CREATE_FILE_RESPONSE      = u''
HADOOP_CREATE_FILE_LOCATION      = u'webhdfs://int-iot-hadoop-fe-01.novalocal:50070/user/%s/%s'
HADOOP_APPEND_FILE_RESPONSE      = u''


responseBody = [{NAME: CKAN_VERSION,              METHOD: GET,  PATH: None, BODY: None},
                {NAME: CKAN_ORGANIZATION_SHOW,    METHOD: GET,  PATH: None, BODY: None},
                {NAME: CKAN_ORGANIZATION_CREATE,  METHOD: POST, PATH: None, BODY: None},
                {NAME: CKAN_PACKAGE_CREATE,       METHOD: POST, PATH: None, BODY: None},
                {NAME: CKAN_RESOURCE_CREATE,      METHOD: POST, PATH: None, BODY: None},
                {NAME: CKAN_DATASTORE_CREATE,     METHOD: POST, PATH: None, BODY: None},
                {NAME: CKAN_DISCOVER_RESOURCES,   METHOD: GET,  PATH: None, BODY: None},
                {NAME: CKAN_INSERT_ROW,           METHOD: POST, PATH: None, BODY: None},
                {NAME: HADOOP_FILE_EXIST,         METHOD: GET,  PATH: None, BODY: None},
                {NAME: HADOOP_CREATE_DIRECTORY,   METHOD: PUT,  PATH: None, BODY: None},
                {NAME: HADOOP_CREATE_FILE,        METHOD: PUT,  PATH: None, BODY: None},
                {NAME: HADOOP_APPEND_FILE,        METHOD: POST, PATH: None, BODY: None},

]

def usage():
    """
    usage message
    """
    print " *******************************++++++***************************************"
    print " *  usage: python cygnus_mock.py <port> <organization> <dataset> <resource> *"
    print " *      values by default:                                                  *"
    print " *           port        : 8090                                             *"
    print " *           organization: orga_default                                     *"
    print " *           dataset     : fiware-test                                      *"
    print " *           resource    : room1-room                                       *"
    print " *       Note: all values will be defined in lowercase.                     *"
    print " *                  ( use <Ctrl-C> to stop )                                *"
    print " ****************************************************************************"

def createPath (type, organization ,dataset, resource):
    """
    Define all paths dynamically
    :param type:   path type
    :param organization: organization used
    :param dataset: dataset used
    :param resource: resource used
    :return:
    """
    if type == CKAN_VERSION: return CKAN_VERSION_PATH
    if type == CKAN_ORGANIZATION_SHOW: return CKAN_ORGANIZATION_PATH % (organization)
    if type == CKAN_ORGANIZATION_CREATE: return CKAN_ORGANIZATION_CREATE_PATH
    if type == CKAN_PACKAGE_CREATE: return CKAN_PACKAGE_CREATE_PATH
    if type == CKAN_RESOURCE_CREATE: return CKAN_RESOURCE_CREATE_PATH
    if type == CKAN_DATASTORE_CREATE: return CKAN_DATASTORE_CREATE_PATH
    if type == CKAN_DISCOVER_RESOURCES: return CKAN_DISCOVER_RESOURCES_PATH % (dataset)
    if type == CKAN_INSERT_ROW: return CKAN_INSERT_ROW_PATH

    if type == HADOOP_FILE_EXIST: return HADOOP_FILE_EXIST_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER)
    if type == HADOOP_CREATE_DIRECTORY: return HADOOP_CREATE_DIRECTORY_PATH % (HADOOP_USER, ORGANIZATION_DEFAULT, HADOOP_USER)
    if type == HADOOP_CREATE_FILE: return HADOOP_CREATE_FILE_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER)
    if type == HADOOP_APPEND_FILE: return HADOOP_APPEND_FILE_PATH % (HADOOP_USER, HADOOP_FILE_PATH, HADOOP_USER)

def createBody (type, organization ,dataset, resource):
    """
    Define all payload responses
    :param type:   path type
    :param organization: organization used
    :param dataset: dataset used
    :param resource: resource used
    :return:
    """
    global HADOOP_CREATE_FILE_LOCATION
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
    if type == HADOOP_CREATE_FILE:
        HADOOP_CREATE_FILE_LOCATION = HADOOP_CREATE_FILE_LOCATION % (HADOOP_USER, HADOOP_FILE_PATH)
        return HADOOP_CREATE_FILE_RESPONSE
    if type == HADOOP_APPEND_FILE: return HADOOP_APPEND_FILE_RESPONSE

def configuration (values):
    """
    Define values for configuration
    :param values: parameters in command line
    """
    global PORT_NUMBER, ORGANIZATION_DEFAULT, DATASET_DEFAULT, RESOURCE_DEFAULT, responseBody, DATASET, HADOOP_FILE_PATH
    if len (values) > 1: PORT_NUMBER          = int(values[1])
    if len (values) > 2: ORGANIZATION_DEFAULT = values[2]
    if len (values) > 3: DATASET_DEFAULT      = values[3]
    if len (values) > 4: RESOURCE_DEFAULT     = values[4]
    DATASET = ORGANIZATION_DEFAULT+"_"+DATASET_DEFAULT
    HADOOP_FILE_PATH = HADOOP_FILE_PATH % (HADOOP_PREFIX, ORGANIZATION_DEFAULT, HADOOP_PREFIX, RESOURCE_DEFAULT, HADOOP_PREFIX, RESOURCE_DEFAULT)

    for i in range(len(responseBody)):
        responseBody[i][PATH] = createPath(responseBody[i][NAME], ORGANIZATION_DEFAULT, DATASET , RESOURCE_DEFAULT)
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
    print " ****************************************************************************"
    print " * Paths mocked:"
    for i in range(len(responseBody)):
        if i < 9: pos = " "+str(i+1)
        else: pos = str (i+1)
        print " *   "+pos+" - "+ responseBody[i][METHOD] + " - "+str (responseBody[i][PATH])
    print " ****************************************************************************"

def response (path):
    """
    return body response associated to a path type
    :param type: (ex: CKAN_VERSION, etc)
    :return: body response associated to a path type
    :except: generic exception
    """
    path = path.lower()
    try:
        for i in range(len(responseBody)):
            if responseBody[i][PATH] == path: return responseBody [i][BODY]
        print "WARN - your path is wrong: "+ path
    except Exception, e:
        print "ERROR -  "+ str(e)



