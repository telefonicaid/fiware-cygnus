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


# general constants
from tools import general_utils, http_utils

EMPTY   = u''
WITHOUT = u'without'

# url, headers and payload constants
HEADER_AUTHORIZATION   = u'authorization'
HEADER_CONTENT_TYPE    = u'Content-Type'
HEADER_APPLICATION     = u'application/json'
VERSION                = u'ckan_version'
VERSION_VALUE_DEFAULT  = u'2.0'
HOST                   = u'host'
HOST_VALUE_DEFAULT     = u'127.0.0.1'
PORT                   = u'port'
PORT_VALUE_DEFAULT     = u'80'
AUTHORIZATION          = u'authorization'
VERIFY_VERSION         = u'verify_version'
FALSE_VALUE            = u'false'
ORION_URL              = u'orion_url'
ORION_URL_DEFAULT      = u'http://localhost:1026'
SSL                    = u'ssl'
RETRIES_DATASET_SEARCH = u'retries_dataset_search'
DELAY_TO_RETRY         = u'delay_to_retry'

PATH_VERSION_CKAN      = u'api/util/status'
PATH_API_CREATE        = u'api/3/action'
PATH_PACKAGE_SHOW      = u'package_show?id='
PATH_DSTORE_SEARCH_SQL = u'datastore_search_sql?sql='
ORGANIZATION_LIST      = u'organization_list'
ORGANIZATION_CREATE    = u'organization_create'
PACKAGE_CREATE         = u'package_create'
RESOURCE_CREATE        = u'resource_create'
DATASTORE_CREATE       = u'datastore_create'
PACKAGE_SHOW           = u'package_show'
DATASTORE_SEARCH_SQL   = u'datastore_search_sql'
RESULT                 = u'result'
RECORDS                = u'records'
NAME                   = u'name'
OWNER_ORG              = u'owner_org'
ID                     = u'id'
TYPE                   = u'type'
RESOURCES              = u'resources'
URL_EXAMPLE            = u'http://foo.bar/newresource'
URL                    = u'url'
PACKAGE_ID             = u'package_id'
RESOURCE_ID            = u'resource_id'
FIELD                  = u'fields'
FORCE                  = u'force'
RECVTIME               = u'recvTime'
TIMESTAMP              = u'timestamp'
TRUE                   = u'true'


class Ckan:
    def __init__(self, **kwargs):
        """
        constructor
        :param ckan_version: ckan version (OPTIONAL)
        :param ckan_verify_version: determine whether the version is verified or not (True or False). (OPTIONAL)
        :param authorization: API KEY (authorization) used in ckan requests (OPTIONAL)
        :param host: ckan host (MANDATORY)
        :param port: ckan port (MANDATORY)
        :param orion_url: Orion URL used to compose the resource URL with the convenience operation URL to query it (OPTIONAL)
        :param ssl: enable SSL for secure Http transportation; 'true' or 'false' (OPTIONAL)
        :param capacity: capacity of the channel (OPTIONAL)
        :param channel_transaction_capacity: amount of bytes that can be sent per transaction (OPTIONAL)
        :param retries_number: number of retries when get values (OPTIONAL)
        :param delay_to_retry: time to delay each retry (OPTIONAL)
        endpoint_url: endpoint url used in ckan requests
        """
        self.version             = kwargs.get(VERSION, VERSION_VALUE_DEFAULT)
        self.ckan_verify_version = kwargs.get(VERIFY_VERSION, FALSE_VALUE)
        self.authorization       = kwargs.get(AUTHORIZATION, EMPTY)
        self.host                = kwargs.get(HOST, HOST_VALUE_DEFAULT)
        self.port                = kwargs.get(PORT, PORT_VALUE_DEFAULT)
        self.orion_url           = kwargs.get(ORION_URL, ORION_URL_DEFAULT)
        self.ssl                 = kwargs.get(SSL, FALSE_VALUE)
        self.capacity            = kwargs.get("capacity", "1000")
        self.transaction_capacity= kwargs.get("transaction_capacity", "100")
        self.retries_number = kwargs.get(RETRIES_DATASET_SEARCH, 15)
        self.retry_delay = kwargs.get(DELAY_TO_RETRY, 10)

        if self.ssl.lower() == "true":
            self.endpoint = "https://"
        if self.ssl.lower() == "false":
            self.endpoint = "http://"
        self.endpoint = self.endpoint + self.host+":"+self.port

    def __create_url(self, operation, element=EMPTY):
        """
        create the url for different operations
        :param operation: operation type (dataset, etc)
        :return: request url
        """
        if operation == VERSION:
            value = "%s/%s" % (self.endpoint, PATH_VERSION_CKAN)
        if operation == ORGANIZATION_CREATE or operation == PACKAGE_CREATE or operation == RESOURCE_CREATE or operation == DATASTORE_CREATE or operation == ORGANIZATION_LIST:
             value = "%s/%s/%s" % (self.endpoint, PATH_API_CREATE, operation) # organization Name
        if operation == PACKAGE_SHOW:
            value = "%s/%s/%s%s" % (self.endpoint, PATH_API_CREATE, PATH_PACKAGE_SHOW, element) # datasetName
        if operation == DATASTORE_SEARCH_SQL:
            value = "%s/%s/%s%s" % (self.endpoint, PATH_API_CREATE, PATH_DSTORE_SEARCH_SQL, element) # sql
        return value

    def __create_headers(self):
        """
        create headers for different requests
        :return header dict
        """
        return {HEADER_AUTHORIZATION: self.authorization, HEADER_CONTENT_TYPE: HEADER_APPLICATION}

    def __create_datastore_in_resource (self, resource_id, fields):
        """
        create a datastore in a resource
        :param resource_id: resource id
        :param fields:  field in datastore
        """
        payload = general_utils.convert_dict_to_str({RESOURCE_ID:  resource_id,
                                                     FIELD:fields,
                                                     FORCE: TRUE}, general_utils.JSON)
        resp = http_utils.request(http_utils.POST, url=self.__create_url(DATASTORE_CREATE), headers=self.__create_headers(), data=payload)
        http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - Creating datastore in resource id: %s" % (resource_id))

    # ------------------------------ public methods ----------------------------------------

    def verify_version (self):
        """
        Verify if ckan is installed and that version is the expected, default version is 2.0
        """
        if self.ckan_verify_version.lower() == "true":
            resp= http_utils.request(http_utils.GET, url=self.__create_url(VERSION), headers=self.__create_headers())
            body_dict = general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            assert  self.version == str(body_dict[VERSION]), \
                "Wrong ckan version verified: %s. Expected: %s. \n\nBody content: %s"  % (str(body_dict[VERSION]), str(self.version), str(resp.text))
        return True

    def verify_if_organization_exist(self, name):
        """
        Verify if the organization exist
        :param name: organization name
        :return: return True if de organization does not exist, False if it does exist
        """
        resp = http_utils.request(http_utils.GET, url=self.__create_url(ORGANIZATION_LIST, name), headers=self.__create_headers())
        http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - list of the names of the site's organizations...")
        body_dict = general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
        for i in range(len(body_dict[RESULT])):
            if body_dict[RESULT][i] == name: return True
        return False

    def create_organization (self, name):
        """
        Create a new organization  if it does not exist
        :param name: organization name
        """
        self.organization = name
        if not(self.verify_if_organization_exist(name)):
            payload = general_utils.convert_dict_to_str({NAME: name}, general_utils.JSON)
            resp= http_utils.request(http_utils.POST, url=self.__create_url(ORGANIZATION_CREATE), headers=self.__create_headers(), data=payload)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - creating organization: %s ..." % (name))
            return True
        return False

    def get_organization (self):
        """
        get organization name
        :return: organization name
        """
        return self.organization

    def verify_if_dataset_exist(self, name):
        """
        Verify if the dataset exist
        :param name: dataset name
        :return: return True if de dataset does not exist, False if it does exist
        """
        resp = http_utils.request(http_utils.GET, url=self.__create_url(PACKAGE_SHOW, name), headers=self.__create_headers())
        if resp.status_code == http_utils.status_codes[http_utils.OK]:
            bodyDict= general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            self.dataset_id = bodyDict[RESULT][ID]
            return self.dataset_id
        return False

    def create_dataset (self, name):
        """
        Create a new dataset if it does not exist
        :param name: dataset name
        """
        self.dataset = name
        if not(self.verify_if_dataset_exist( name)):
            payload = general_utils.convert_dict_to_str({NAME:  self.dataset,
                                                         OWNER_ORG: self.organization}, general_utils.JSON)
            resp= http_utils.request(http_utils.POST, url=self.__create_url(PACKAGE_CREATE), headers=self.__create_headers(), data=payload)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - creating dataset: %s ..." % (name))
            bodyDict= general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            self.dataset_id = bodyDict[RESULT][ID]
            return bodyDict[RESULT][ID]
        return False

    def get_dataset (self):
        """
        get dataset name and dataset id
        :return: dataset name and dataset id
        """
        return self.dataset, self.dataset_id

    def verify_if_resource_exist(self, name, dataset_name):
        """
        Verify if the resource exist in a dataset
        :param name: resource name
        :param dataset_id:
        :return: return True if de resource does not exist, False if it does exist
        """
        resp = http_utils.request(http_utils.GET, url=self.__create_url(PACKAGE_SHOW, dataset_name), headers=self.__create_headers())
        if resp.status_code == http_utils.status_codes[http_utils.OK]:
            body_dict = general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            for i in range(len(body_dict[RESULT][RESOURCES])):
                if body_dict[RESULT][RESOURCES][i][NAME] == name:
                    self.resource_id = body_dict[RESULT][RESOURCES][i][ID]
                    return self.resource_id
        return False

    def generate_field_datastore_to_resource (self, attributes_number, attributes_name, attribute_type, metadata_type):
        """
        generate fields to datastore request
        :return: fields list
        """
        field = []
        field.append({ID:RECVTIME, TYPE: TIMESTAMP})
        for i in range(0, int(attributes_number)):
            if attribute_type != WITHOUT: field.append({ID:attributes_name+"_"+str(i), TYPE: attribute_type})
            if metadata_type != WITHOUT:field.append({ID:attributes_name+"_"+str(i)+"_md", TYPE: metadata_type})
        return field

    def create_resource(self, name, dataset_name, fields=[]):
        self.resource = name
        if not(self.verify_if_resource_exist(name, dataset_name)):
            payload = general_utils.convert_dict_to_str({NAME:  self.resource,
                                                         URL: URL_EXAMPLE,
                                                         PACKAGE_ID: self.dataset_id}, general_utils.JSON)
            resp= http_utils.request(http_utils.POST, url=self.__create_url(RESOURCE_CREATE), headers=self.__create_headers(), data=payload)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - creating resource: %s ..." % (name))
            bodyDict= general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            self.resource_id = bodyDict[RESULT][ID]
            self.__create_datastore_in_resource (self.resource_id, fields)
            return self.resource_id
        return False

    def get_resource (self):
        """
        get resource name and resource id
        :return: resource name and resource id
        """
        return self.resource, self.resource_id

    def datastore_search_last_sql (self, rows, resource_name, dataset_name):
        """
        get last record in a resource
        :param name:  resource name
        :param dataset_name: dataset name
        :return: record dict
        """
        resource_id = self.verify_if_resource_exist(resource_name, dataset_name)
        if resource_id != False:
            sql = 'SELECT * from "' + resource_id + '" ORDER BY 1 DESC LIMIT '+str (rows)
            resp= http_utils.request(http_utils.POST, url=self.__create_url(DATASTORE_SEARCH_SQL, sql), headers=self.__create_headers(), data=EMPTY)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - creating resource: %s ..." % (resource_name))
            return resp
        return resource_id




