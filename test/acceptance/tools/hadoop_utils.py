# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of perseo
#
# perseo is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the License,
# or (at your option) any later version.
#
# perseo is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public
# License along with perseo.
# If not, seehttp://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact:
#  iot_support at tid.es
#
#
import time
import general_utils
import http_utils


# general constants
EMPTY = u''

# url and header constants
VERSION             = u'version'
VERIFY_VERSION      = u'verify_version'
MANAGER_NODE_URL    = u'manager_node_url'
RETRIES_NUMBER      = u'retries_number'
DELAY_TO_RETRY      = u'delay_to_retry'
OPEN_FILE           = 'OPEN'
DELETE_DIR         = 'DELETE'
PATH_VERSION        = u'ws/v1/cluster/info'
CONTENT_TYPE        = u'Content-Type'
APPLICATION_CONTENT = u'application/json'

# constants request
STARTED                   = u'STARTED'
CLUSTER_INFO              = u'clusterInfo'
HADOOP_STATE              = u'state'
HADOOP_VERSION            = u'hadoopVersion'
PATH_HADOOP               = u'webhdfs/v1/user'

NAME_IS_MISSING           = u'name is missing'
ATTR_NAME                 = u'attrName'
ATTR_VALUE                = u'attrValue'



class Hadoop:
    """
    Hadoop  functionabilities
    """

    def __init__(self, name_node_url, user, **kwargs):
        """
        constructor
        :param name node_url: namenode endpoint (MANDATORY)
        :param user: hadoop user (MANDATORY)
        :param version: hadoop version (OPTIONAL)
        :param verify_version: verify version (True |False) (OPTIONAL)
        :param manager_node_url: manager_node endpoint (OPTIONAL)
        :param retries_number: number of retries when get values (OPTIONAL)
        :param delay_to_retry: time to delay each retry (OPTIONAL)

        """
        self.name_node_url       = name_node_url
        self.user                = user
        self.version             = kwargs.get(VERSION, EMPTY)
        self.verify_version      = kwargs.get(VERSION, "False")
        self.manager_node_url    = kwargs.get(MANAGER_NODE_URL, EMPTY)
        self.retries_number      = kwargs.get(RETRIES_NUMBER, 15)
        self.retry_delay         = kwargs.get(DELAY_TO_RETRY, 10)

    def __create_url (self, operation):
        """
        create the url for different operations
        :param operation: operation type (dataset, etc)
        :return: request url
        """
        if operation == VERSION:
            value = "%s/%s"  % (self.namenode_url,PATH_VERSION)
        if operation == OPEN_FILE:
            value = "%s/%s/%s/%s/%s.txt?op=OPEN&user.name=%s" % (self.name_node_url, PATH_HADOOP, self.user, self.directory, self.file_name, self.user)
        if operation == DELETE_DIR:
            value = "%s/%s/%s/%s?op=DELETE&recursive=true&user.name=%s" % (self.name_node_url, PATH_HADOOP, self.user, self.tenant, self.user)

        return value

    def __create_headers(self):
        """
        create headers for different requests
        """
        return {CONTENT_TYPE: APPLICATION_CONTENT}

    def manager_version(self):
        """
        Verify if hadoop is installed and that version is the expected
        """
        resp = http_utils.request(http_utils.GET, url=self.__create_url(VERSION), headers=self.__create_headers())
        http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - in version request... ")
        bodyDict = general_utils.convert_to_str_to_dict(resp.text, general_utils.JSON)

        assert  STARTED == str (bodyDict[CLUSTER_INFO][HADOOP_STATE]), \
        "hadoop is not started...\nverified: %s. Expected: %s. \n\nBody content: %s" \
        % (str (bodyDict[CLUSTER_INFO][HADOOP_STATE]), STARTED, str(resp.text))

        assert  str(self.version) == str (bodyDict[CLUSTER_INFO][HADOOP_VERSION]), \
        "Wrong hadoop version \nverified: %s. Expected: %s. \n\nBody content: %s" \
        % (str (bodyDict[CLUSTER_INFO][HADOOP_VERSION]), str(self.version), str(resp.text))



    def open_file (self, directory, file_name):
        """
        open file in hadoop
        :param  directory: /tenant/service_path/resource/
        :param file:  resource.txt
        :return:
        """
        self.file_name = file_name
        self.directory= directory
        resp = http_utils.request(http_utils.GET, url=self.__create_url(OPEN_FILE), headers=self.__create_headers())
        return resp


    def __splitElement (self, body, attribute_name, attribute_value):
        """
        split the differents lines by \n and return the line associated a a name
        :param body:
        :param name:
        :return:
        """
        temp = body
        i=1
        list = []
        while len(temp)> 0:
            if temp[i] == "\n":
                list.append(temp[:i])
                temp = temp[i+1:]
                i = 0
            i=i+1
            if i == len(temp): break
        for i in range(len(list)-1, -1, -1):    #from last element in the list until first, desc
            dictTemp = general_utils.convert_str_to_dict(list[i], general_utils.JSON)
            if dictTemp [ATTR_NAME] == attribute_name and  dictTemp [ATTR_VALUE] == attribute_value :
                return dictTemp
        return NAME_IS_MISSING


    def retry_in_file_search_data (self, directory, file_name, attribute_name, attribute_value):
        """
        retry in get data from hadoop
        :return: hadoop file content
        """
        c=0
        for i in range(int(self.retries_number)):
            resp=self.open_file (directory, file_name)

            if resp.status_code == http_utils.status_codes[http_utils.OK]:
                self.element = self.__splitElement(resp.text, attribute_name, attribute_value)
                if str(self.element).find(NAME_IS_MISSING) < 0: # the element is found
                        return self.element
            c+=1
            print " WARN - Retry in get data from hadoop. No: ("+ str(c)+")"
            time.sleep(self.retry_delay)
        http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - opening file: %s/%s.txt ..." % (self.directory, self.file_name))
        return u'ERROR - Attributes are missing....'

    def delete_directory (self, name):
        """
        delete a directory
        :param name: directory name
        :return:
        """
        self.tenant=name
        resp = http_utils.request(http_utils.DELETE, url=self.__create_url(DELETE_DIR), headers=self.__create_headers())
        http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR- deleting directory %s ..." % (name))
        return resp




