# -*- coding: utf-8 -*-
#
# Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FI-WARE project).
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
#from tools.properties_config import Properties
import gc


__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

import time
from lettuce import world
from tools import general_utils
from tools.notification_utils import Notifications
import http_utils
from tools.fabric_utils import FabricSupport
from tools.cygnus_agent_config import Agent
from tools.cygnus_instance_config import Cygnus_Instance


# notification constants
MANAGEMENT_PORT             = u'management_port'
VERSION                     = u'version'
VERIFY_VERSION              = u'verify_version'
LOG_LEVEL                   = u'log_level'
MANAGEMENT_PORT_VALUE       = "8081"
NOTIF_USER_AGENT            = u'notif_user_agent'
NOTIF_USER_AGENT_VALUE      = u'orion/0.10.0'
TENANT_ROW_DEFAULT          = u'tenant_row_default'
TENANT_COL_DEFAULT          = u'tenant_col_default'
SERVICE_PATH_DEFAULT        = u'service_path_default'
IDENTITY_ID_DEFAULT         = u'identity_id_default'
IDENTITY_ID_VALUE           = u'Room1'
IDENTITY_TYPE_DEFAULT       = u'identity_type_default'
IDENTITY_TYPE_VALUE         = u'Room'
ATTRIBUTES_NUMBER_DEFAULT   = u'attributes_number_default'
ATTRIBUTES_NUMBER_VALUE     = 1
ATTRIBUTES_NAME_DEFAULT     = u'attributes_name_default'
ATTRIBUTES_NAME_VALUE       = u'temperature'
TTL                         = u'ttl'
TTL_VALUE                   = u'10'

# general constants
ROW_MODE         = u'row'
COL_MODE         = u'column'
CKAN_SINK        = u'ckan'
MYSQL_SINK       = u'mysql'
HDFS_SINK        = u'hdfs'
DEFAULT          = u'default'
RANDOM           = u'random'
EMPTY            = u''

# ckan constants
MAX_TENANT_LENGTH              = u'abcde678901234567890123456789019'
MAX_TENANT_LENGTH_ROW          = u'abcde67890123456789012345699_row'
MAX_RESOURCE_LENGTH            = u'1234567890123_45678901234567890123456789012345678901234567890126'
WITH_MAX_LENGTH_ALLOWED        = u'with max length allowed'
ORGANIZATION_MISSING           = u'organization is missing'
ORGANIZATION_WITHOUT_DATASET   = u'organization_without_dataset'
RESOURCE_MISSING               = u'resource_missing'
RESULT                         = u'result'
RECORDS                        = u'records'
VALUE                          = u'value'
CONTENT_VALUE                  = u'contextValue'
NAME                           = u'name'
METADATA                       = u'metadata'
CONTEXT_METADATA               = u'contextMetadata'
METADATAS                      = u'metadatas'
ATTR_NAME                      = u'attrName'
ATTR_TYPE                      = u'attrType'
ATTR_VALUE                     = u'attrValue'
ATTR_MD                        = u'attrMd'
TYPE                           = u'type'

# mysql constants
DATABASE_WITHOUT_TABLE         = u'database_without_table'
DATABASE_MISSING               = u'database_missing'
MAX_TABLE_LENGTH               = 64

#fabric
HOST                           = u'host'
USER                           = u'user'
PASSWORD                       = u'password'
CERT_FILE                      = u'cert_file'
ERROR_RETRY                    = u'error_retry'
SOURCE_PATH                    = u'source_path'
TARGET_PATH                    = u'target_path'
SUDO_CYGNUS                    = u'sudo_cygnus'



class Cygnus:
    """
    cygnus class with generals features
    """

    # --------------- Configuration ------------------------
    def __init__(self, cygnus_url, **kwargs):
        """
        constructor
        :param cygnus_url: <protocol>://<host>:<port> cygnus (MANDATORY)
        :param management_port : management port to know the version (OPTIONAL)
        :param version : cygnus version (OPTIONAL)
        :param verify_version : determine if verify cygnus version or not (True | False)(OPTIONAL)
        :param notif_user_agent : user agent to notification (OPTIONAL)
        :param tenant_row_default: tenant by default per row mode (OPTIONAL)
        :param tenant_col_default: tenant by default per col mode (OPTIONAL)
        :param service_path_default: service path by default (OPTIONAL)
        :param identity_id_default: identity id by default (OPTIONAL)
        :param identity_type_default: identity type by default (OPTIONAL)
        :param attributes_number_default: attribute number by default (OPTIONAL)
        :param attributes_name_default: attribute name by default (OPTIONAL)
        :param ttl: Number of channel re-injection retries before a Flume event is definitely discarded (-1 means infinite retries) (OPTIONAL)
        :param user: user used to connect by fabric (OPTIONAL)
        :param password: password used to connect by fabric, if use cert file, password will be None (OPTIONAL)
        :param cert_file: cert_file used to connect by fabric, if use password, cert_file will be None (OPTIONAL)
        :param error_retry: Number of times Fabric will attempt to connect when connecting to a new server (OPTIONAL)
        :param source_path: source path where are templates files [OPTIONAL]
        :param target_path: target path where are copied config files [OPTIONAL]
        :param sudo_cygnus: operations in cygnus with superuser privileges (True | False) [OPTIONAL]
        """

        self.cygnus_url              = cygnus_url
        self.management_port         = kwargs.get(MANAGEMENT_PORT, MANAGEMENT_PORT_VALUE)
        self.version                 = kwargs.get(VERSION, EMPTY)
        self.verify_version          = kwargs.get(VERIFY_VERSION, "true")
        self.log_level               = kwargs.get(LOG_LEVEL, "DEBUG")
        self.notif_user_agent        = kwargs.get(NOTIF_USER_AGENT, NOTIF_USER_AGENT_VALUE)
        self.tenant                  = {ROW_MODE: kwargs.get(TENANT_ROW_DEFAULT.lower(), TENANT_ROW_DEFAULT),
                                        COL_MODE: kwargs.get(TENANT_COL_DEFAULT.lower(), TENANT_COL_DEFAULT)}
        self.service_path            = kwargs.get(SERVICE_PATH_DEFAULT, SERVICE_PATH_DEFAULT)
        self.identity_id             = kwargs.get(IDENTITY_ID_DEFAULT, IDENTITY_ID_VALUE)
        self.identity_type           = kwargs.get(IDENTITY_TYPE_DEFAULT, IDENTITY_TYPE_VALUE)
        self.attributes_number       = int(kwargs.get(ATTRIBUTES_NUMBER_DEFAULT, ATTRIBUTES_NUMBER_VALUE))
        self.attributes_name         = kwargs.get(ATTRIBUTES_NAME_DEFAULT, ATTRIBUTES_NAME_VALUE)
        self.ttl                     = kwargs.get(TTL, TTL_VALUE)
        #fabric
        self.fabric_host             = self.cygnus_url.split(":")[1][2:]
        self.fabric_port             = self.cygnus_url.split(":")[2]
        self.fabric_user             = kwargs.get(USER, None)
        self.fabric_password         = kwargs.get(PASSWORD, None)
        self.fabric_cert_file        = kwargs.get(CERT_FILE, None)
        self.fabric_error_retry      = kwargs.get(ERROR_RETRY, 1)
        self.fabric_source_path      = kwargs.get(SOURCE_PATH, EMPTY)
        self.fabric_target_path      = kwargs.get(TARGET_PATH, EMPTY)
        self.fabric_sudo_cygnus      = kwargs.get(SUDO_CYGNUS, False)

        self.dataset_id              = None
        self.resource_id             = None

    def __get_port (self, port, inc):
        """
        get port value incremented to multi-instances
        :param port: port value
        :param inc: increment
        :return port string
        """
        try:
            return str(int(port)+inc)
        except Exception, e:
            assert False, "ERROR - port %s is not numeric format \n %s" % (str(port), str (e))

    def __get_channels(self, sinks):
        """
        return the channel used by each sink
        :param sinks: sinks list
        :return: channels (string)
        """
        sink_list = sinks.split (" ")    # sink ex: ckan-sink mysql-sink hdfs-sink
        channels = ""
        for i in range(len(sink_list)):
            channels = channels + sink_list[i].split("-")[0]+"-channel " # channel ex: ckan-channel mysql-channel hdfs-channel
        return channels[:len(channels)-1]

    def config_instances(self, id, quantity, sinks, persistence):
        """
        initialize instance files
        :param id:
        :param quantity:
        :param sink:
        """
        self.instance_id       = id
        self.instance_quantity = quantity
        self.persistence       = persistence
        myfab = FabricSupport(host=self.fabric_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry)
        cygnus_instance = Cygnus_Instance(source_path=self.fabric_source_path, target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        cygnus_agent    = Agent(source_path=self.fabric_source_path, target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        for i in range(int(self.instance_quantity)):
            # generate cygnus_instance_<id>.conf ex: cygnus_instance_test_0.conf
            myfab.runs(cygnus_instance.append_id(id=self.instance_id+"_"+str(i)))
            # generate agent_<id>.conf ex: agent_test_0.conf
            ops_list = cygnus_agent.append_id(id=self.instance_id+"_"+str(i))
            ops_list = cygnus_agent.source(sink=sinks, channel=self.__get_channels(sinks), port=self.__get_port(self.fabric_port, i), matching_table_file=self.fabric_target_path+"/matching_table.conf", default_service=self.tenant[self.persistence], default_service_path=self.service_path, ttl=self.ttl)
            sinks_list = sinks.split(" ")
            for i in range(len(sinks_list)):
                if sinks_list[i].find(HDFS_SINK)>=0:
                    hdfs_host = world.config['hadoop']['hadoop_namenode_url'].split(":")[1][2:]
                    hdfs_port = world.config['hadoop']['hadoop_namenode_url'].split(":")[2]
                    ops_list = cygnus_agent.config_hdfs_sink(sink=sinks_list[i], channel=self.__get_channels(sinks_list[i]), host=hdfs_host, port=hdfs_port, user=world.config['hadoop']['hadoop_user'], password=world.config['hadoop']['hadoop_password'], api=world.config['hadoop']['hadoop_api'], persistence=self.persistence, hive_host=hdfs_host, hive_port=hdfs_port, krb5_auth=world.config['hadoop']['hadoop_krb5_auth'], krb5_user=world.config['hadoop']['hadoop_krb5_user'], krb5_password=world.config['hadoop']['hadoop_krb5_password'], krb5_login_file=self.fabric_target_path+"/krb5_login.conf", krb5_conf_file=self.fabric_target_path+"/krb5.conf")
                    ops_list = cygnus_agent.config_channel (self.__get_channels(sinks_list[i]), capacity=world.config['hadoop']['hadoop_channel_capacity'], transaction_capacity=world.config['hadoop']['hadoop_channel_transaction_capacity'])
                elif sinks_list[i].find(CKAN_SINK)>=0:
                    ops_list = cygnus_agent.config_ckan_sink(sink=sinks_list[i], channel=self.__get_channels(sinks_list[i]),api_key=world.config['ckan']['ckan_authorization'], host=world.config['ckan']['ckan_host'],  port=world.config['ckan']['ckan_port'], orion_url=world.config['ckan']['ckan_orion_url'], persistence=self.persistence, ssl=world.config['ckan']['ckan_ssl'])
                    ops_list = cygnus_agent.config_channel (self.__get_channels(sinks_list[i]), capacity=world.config['ckan']['ckan_channel_capacity'], transaction_capacity=world.config['ckan']['ckan_channel_transaction_capacity'])
                elif sinks_list[i].find(MYSQL_SINK)>=0:
                    ops_list = cygnus_agent.config_mysql_sink(sink=sinks_list[i], channel=self.__get_channels(sinks_list[i]), host=world.config['mysql']['mysql_host'], port=world.config['mysql']['mysql_port'], user=world.config['mysql']['mysql_user'], password=world.config['mysql']['mysql_pass'], persistence=self.persistence)
                    ops_list = cygnus_agent.config_channel (self.__get_channels(sinks_list[i]), capacity=world.config['mysql']['mysql_channel_capacity'], transaction_capacity=world.config['mysql']['mysql_channel_transaction_capacity'])
            # create and modify values in agent_<id>.conf
            myfab.runs(ops_list)

    def another_files (self):
        """
        copy another configuration files used by cygnus
          - log4j.properties
          - matching_table.conf
          - flume-env.sh
          - krb5.conf
        """
        myfab = FabricSupport(host=self.fabric_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry)
        myfab.run("cp log4j.properties.template log4j.properties", path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        # change to DEBUG mode in log
        myfab.run(' sed -i "s/flume.root.logger=INFO,LOGFILE/flume.root.logger=%s,LOGFILE /" log4j.properties.template' % (self.log_level), path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        myfab.run("cp matching_table.conf.template matching_table.conf", path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        myfab.run("cp flume-env.sh.template flume-env.sh", path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        myfab.run("cp krb5.conf.template krb5.conf", path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)

    def cygnus_service(self, operation):
        """
         cygnus service (status | stop | start | restart)
        :param operation:
        """
        myfab = FabricSupport(host=self.fabric_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry)
        myfab.run("service cygnus %s" % operation, sudo=self.fabric_sudo_cygnus )


    def verify_cygnus (self):
        """
        verify if cygnus is installed correctly and its version
        """
        self.cygnus_mode = world.persistence
        if self.verify_version.lower() == "true":
            temp_split = self.cygnus_url.split(":")
            management_url = "%s:%s:%s/%s" % (str(temp_split[0]), str(temp_split[1]), self.management_port, VERSION)
            resp = http_utils.request(http_utils.GET, url= management_url)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - in management operation (version)")
            body_dict= general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            assert str(body_dict[VERSION]) == self.version, \
                "Wrong cygnus version verified: %s. Expected: %s. \n\nBody content: %s" \
                % (str(body_dict[VERSION]), str(self.version), str(resp.text))
        return True

     # ----------------------------- general action -----------------------------------------------------

    def __split_resource (self, resource_name):
        """
        split resource in identity Id and identity Type
        """
        res = resource_name.split ("_")
        return  res [0], res [1] # identity Id , identity Type

    def received_notification(self, attribute_value, metadata_value, content):
        """
        notifications
        :param attribute_value: attribute value
        :param metadata_value: metadata value (true or false)
        :param content: xml or json
        """
        self.content = content
        self.metadata_value = metadata_value
        metadata_attribute_number = 1
        cygnus_notification_path = u'/notify'
        notification = Notifications (self.cygnus_url+cygnus_notification_path,tenant=self.tenant[self.cygnus_mode], service_path=self.service_path, content=self.content)
        if self.metadata_value:
            notification.create_metadatas_attribute(metadata_attribute_number, RANDOM, RANDOM, RANDOM)
        notification.create_attributes (self.attributes_number, self.attributes_name, self.attribute_type, attribute_value)
        resp =  notification.send_notification(self.identity_id, self.identity_type)

        self.attributes=notification.get_attributes()
        self.attributes_name=notification.get_attributes_name()
        self.attributes_value=notification.get_attributes_value()
        self.attributes_number=notification.get_attributes_number()
        self.attributes_metadata=notification.get_attributes_metadata_number()
        self.attributes_number=int(notification.get_attributes_number())
        return resp

    def row_configuration(self, tenant, service_path, resource_name, attributes_number, attributes_name, attribute_type):
        """
        row configuration
        """
        self.dataset = None
        self.table = None
        if tenant == WITH_MAX_LENGTH_ALLOWED: self.tenant[self.cygnus_mode] = MAX_TENANT_LENGTH_ROW.lower()
        elif tenant != DEFAULT:
            self.tenant[self.cygnus_mode] = tenant.lower()
        if service_path != DEFAULT: self.service_path = service_path.lower()
        if self.service_path[:1] == "/": self.service_path = self.service_path[1:]
        if resource_name == WITH_MAX_LENGTH_ALLOWED:
            self.resource = MAX_RESOURCE_LENGTH[0:(len(MAX_RESOURCE_LENGTH)-len(self.service_path)-1)].lower()  # file (max 64 chars) = service_path + "_" + resource
        elif resource_name == DEFAULT:
            self.resource = str(self.identity_id+"_"+self.identity_type).lower()
        else:
            self.resource = resource_name.lower()
        self.identity_id, self.identity_type = self.__split_resource (self.resource)
        if attributes_number != DEFAULT: self.attributes_number = int(attributes_number)
        if attributes_name != DEFAULT: self.attributes_name = attributes_name
        self.attribute_type = attribute_type

        self.dataset = self.tenant[self.cygnus_mode]
        self.table = self.resource
        if self.service_path != EMPTY:
            self.dataset = self.dataset+"_"+self.service_path
            self.table = self.service_path+ "_" +self.table

    # ----------------------------- CKAN Column Mode -----------------------------------------------------

    def create_organization_and_dataset(self, tenant, service_path):
        """
        Create a new organization and a dataset associated
        :param tenant: organization name
        :param serv_path: dataset is organization_<service_path>
        """
        self.dataset = None
        if tenant == WITH_MAX_LENGTH_ALLOWED: self.tenant[self.cygnus_mode] = MAX_TENANT_LENGTH.lower()
        elif tenant != DEFAULT:
            self.tenant[self.cygnus_mode] = tenant.lower()

        if service_path != DEFAULT: self.service_path = service_path.lower()
        if self.service_path[:1] == "/": self.service_path = self.service_path[1:]
        self.dataset = self.tenant[self.cygnus_mode]
        if self.service_path != EMPTY: self.dataset = self.dataset+"_"+self.service_path
        if tenant != ORGANIZATION_MISSING:
            world.ckan.create_organization (self.tenant[self.cygnus_mode])
            if tenant != ORGANIZATION_WITHOUT_DATASET:
                self.dataset_id = world.ckan.create_dataset (self.dataset)

    def create_resource_and_datastore (self, resource_name, attributes_number, attributes_name, attribute_type, attribute_data_type, metadata_data_type):
        """
        create  a new resource and its datastore associated if it does not exists
        """
        if resource_name == WITH_MAX_LENGTH_ALLOWED:
            self.resource = MAX_RESOURCE_LENGTH.lower()
        elif resource_name == DEFAULT:
            self.resource = str(self.identity_id+"_"+self.identity_type).lower()
        else:
            self.resource = resource_name.lower()
        self.identity_id, self.identity_type = self.__split_resource (self.resource)
        if attributes_number != DEFAULT: self.attributes_number = int(attributes_number)
        if attributes_name != DEFAULT: self.attributes_name = attributes_name
        self.attribute_type = attribute_type
        self.dataset_id = world.ckan.verify_if_dataset_exist(self.dataset)
        if (self.tenant[self.cygnus_mode] != ORGANIZATION_MISSING and \
           self.tenant[self.cygnus_mode] != ORGANIZATION_WITHOUT_DATASET and \
           self.dataset_id  != False) and \
           resource_name != RESOURCE_MISSING:
            fields = world.ckan.generate_field_datastore_to_resource(self.attributes_number, self.attributes_name, attribute_data_type, metadata_data_type)
            self.resource_id = world.ckan.create_resource(self.resource, self.dataset_id, fields)

    def retry_in_datastore_search_sql_column (self, resource_name, dataset_name, attributes_name, value):
        """
        retry in get data from ckan in column mode
        :return: record from ckan
        """
        c=0
        row=1
        for i in range(int(world.ckan.retries_number)):
            resp=world.ckan.datastore_search_last_sql(row, resource_name, dataset_name)
            temp_dict = general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            if len(temp_dict[RESULT][RECORDS])>0:
                if str(temp_dict[RESULT][RECORDS][0][attributes_name+"_0"]) == str(value):
                    return temp_dict
            c+=1
            print " WARN - Retry in get data from ckan. No: ("+ str(c)+")"
            time.sleep(world.ckan.retry_delay)
        return u'ERROR - Attributes are missing....'

    def retry_in_datastore_search_sql_row (self, position, resource_name, dataset_name, attributes_name):
        """
        retry in get data from ckan in row mode
        :return: record from  ckan
        """
        c=0
        for i in range(int(world.ckan.retries_number)):
            resp=world.ckan.datastore_search_last_sql(self.attributes_number, resource_name, dataset_name)
            if resp != False:
                temp_dict = general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
                if len(temp_dict[RESULT][RECORDS]) == int (self.attributes_number):
                    for i in range(0, self.attributes_number):
                        if str(temp_dict[RESULT][RECORDS][i][ATTR_NAME]) == str(attributes_name+"_"+str(position)):
                            return temp_dict[RESULT][RECORDS][i]
            c+=1
            print " WARN - Retry in get data from ckan. No: ("+ str(c)+")"
            time.sleep(world.ckan.retry_delay)
        return u'ERROR - Attributes are missing....'

     # ----------------------------- CKAN Row Mode -----------------------------------------------------

    # ----------------------------- MySQL Column Mode -----------------------------------------------------

    def create_database (self,tenant):
        """
        create a new Database per column
        :param tenant: database name
        """
        if tenant == WITH_MAX_LENGTH_ALLOWED: self.tenant[self.cygnus_mode] = MAX_TENANT_LENGTH.lower()
        elif        tenant != DATABASE_WITHOUT_TABLE \
                and tenant != DATABASE_MISSING \
                and tenant != DEFAULT:
            self.tenant[self.cygnus_mode] = tenant.lower()
        if tenant != DATABASE_MISSING:
            world.mysql.create_database (self.tenant[self.cygnus_mode])

    def create_table (self, resource_name, service_path, attributes_number, attributes_name, attribute_type, attribute_data_type, metadata_data_type):
        """
        create a new table per column
        """
        if service_path != DEFAULT: self.service_path = service_path.lower()
        if self.service_path[:1] == "/": self.service_path = self.service_path[1:]
        if resource_name == WITH_MAX_LENGTH_ALLOWED:
            self.resource = MAX_RESOURCE_LENGTH[0:(len(MAX_RESOURCE_LENGTH)-len(self.service_path)-1)].lower()  # table (max 64 chars) = service_path + "_" + resource
        elif resource_name == DEFAULT:
            self.resource = str(self.identity_id+"_"+self.identity_type).lower()
        else:
            self.resource = resource_name.lower()
        self.table = self.resource
        if self.service_path != EMPTY: self.table = self.service_path+ "_" +self.table
        self.identity_id, self.identity_type = self.__split_resource (self.resource)
        if attributes_number != DEFAULT: self.attributes_number = int(attributes_number)
        if attributes_name != DEFAULT: self.attributes_name = attributes_name
        self.attribute_type = attribute_type

        if self.tenant[self.cygnus_mode] != DATABASE_MISSING and \
            self.tenant[self.cygnus_mode] != DATABASE_WITHOUT_TABLE and \
            resource_name != RESOURCE_MISSING:
             fields = world.mysql.generate_field_datastore_to_resource (attributes_number, attributes_name, attribute_data_type, metadata_data_type)
             world.mysql.create_table (self.table, self.tenant[self.cygnus_mode], fields)

    def retry_in_table_search_sql_column (self, table_name, database_name, value):
        """
        retry in get data from mysql
        :return: record in mysql
        """
        c   = 0
        for i in range(int(world.mysql.retries_number)):
            row=world.mysql.table_search_one_row(database_name, table_name)
            if row != None and str(row[1]) == value:
                return row
            c += 1
            print " WARN - Retry in get data from mysql. No: ("+ str(c)+")"
            time.sleep(world.mysql.retry_delay)
        return u'ERROR - Attributes are missing....'

    def retry_in_table_search_sql_row(self, position, database_name, table_name, attributes_name):
        """
        retry in get data from mysql
        :return: record in mysql
        """
        c = 0
        for i in range(int(world.mysql.retries_number)):
            rows=world.mysql.table_search_several_rows(self.attributes_number, database_name, table_name)
            if rows != False:
                if len(rows) == self.attributes_number:
                    for line in rows:
                        for j in range(len(line)):
                            if str(line[4]) == str(attributes_name+"_"+str(position)):
                                return line
            c += 1
            print " WARN - Retry in get data from mysql. No: ("+ str(c)+")"
            time.sleep(world.mysql.retry_delay)
        return u'ERROR - Attributes are missing....'

    # ----------------------------- Hadoop Row Mode -----------------------------------------------------

    def hadoop_configuration(self, tenant, service_path, resource_name, attributes_number, attributes_name, attribute_type):
        """
        hadoop configuration
        """
        if tenant == WITH_MAX_LENGTH_ALLOWED: self.tenant[self.cygnus_mode] = MAX_TENANT_LENGTH.lower()
        elif tenant != DEFAULT:
            self.tenant[self.cygnus_mode] = tenant.lower()
        if service_path != DEFAULT: self.service_path = service_path.lower()
        if resource_name == WITH_MAX_LENGTH_ALLOWED:
            self.resource = MAX_RESOURCE_LENGTH[0:(len(MAX_RESOURCE_LENGTH)-len(self.service_path)-1)].lower()  # file (max 64 chars) = service_path + "_" + resource
        elif resource_name == DEFAULT:
            self.resource = str(self.identity_id+"_"+self.identity_type).lower()
        else:
            self.resource = resource_name.lower()
        self.identity_id, self.identity_type = self.__split_resource (self.resource)
        if attributes_number != DEFAULT: self.attributes_number = int(attributes_number)
        if attributes_name != DEFAULT: self.attributes_name = attributes_name
        self.attribute_type = attribute_type

    # ------------------------------------------  Validations ----------------------------------------------------
    def verify_response_http_code (self, http_code_expected, response):
        """
        validate http code in response
        """
        http_utils.assert_status_code(http_utils.status_codes[http_code_expected], response, "ERROR - in http code received: ")

    def change_destination_to_pattern (self, destination, new_service_path):
        """
        change destination to verify
        :param destination:  new resource name
        :param dataset:  new dataset name
        """
        self.resource = destination
        if destination != EMPTY:
            self.identity_id, self.identity_type = self.__split_resource (self.resource)

        self.service_path = new_service_path                                     #  used in notification request


        self.dataset = self.tenant[self.cygnus_mode]+"_"+new_service_path        #  used in ckan validation request
        self.table = new_service_path +"_"+destination                           #  used in mysql validation

    # ------------------------------------------  ckan validations ----------------------------------------------------

    def verify_dataset_search_values_by_column(self):
        """
         Verify that the attribute contents (value) are stored in ckan
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE

        self.temp_dict=self.retry_in_datastore_search_sql_column (self.resource, self.dataset, self.attributes_name, self.attributes_value)
        assert self.temp_dict != u'ERROR - Attributes are missing....', u'\nERROR - Attributes %s are missing, value expected: %s \n In %s >>> %s ' %(self.attributes_name, self.attributes_value, self.dataset, self.resource)
        for i in range(0,self.attributes_number-1):
            temp_attr = str(self.attributes_name+"_"+str(i))
            assert str(self.temp_dict[RESULT][RECORDS][0][temp_attr]) == str(self.attributes[i][VALUE_TEMP]),\
                "The "+self.attributes[i][NAME]+" value does not match..."

    def verify_dataset_search_metadata_values_by_column(self):
        """
         Verify that the attribute metadata contents (value) are stored in ckan
        """
        if self.metadata_value:
            for i in range(0, self.attributes_number-1):
                if self.content == general_utils.XML:
                    assert str(self.temp_dict[RESULT][RECORDS][0][self.attributes_name+"_"+str(i)+"_md"][0][VALUE]) == str(self.attributes[i][METADATA][CONTEXT_METADATA][0][VALUE]),\
                        "The "+self.attributes[i][NAME]+" metadata value does not match..."
                else:
                    assert str(self.temp_dict[RESULT][RECORDS][0][self.attributes_name+"_"+str(i)+"_md"][0][VALUE]) == str(self.attributes[i][METADATAS][0][VALUE]),\
                        "The "+self.attributes[i][NAME]+" metadata value does not match..."

    def verify_dataset_search_without_data(self, error_msg):
        """
        Verify that is not stored in ckan when a field missing (attribute value or metadata)
        """
        row=1
        resp= world.ckan.datastore_search_last_sql (row, self.resource, self.dataset)
        http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - Ckan sql query")
        dict_temp = general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
        assert len(dict_temp[RESULT][RECORDS]) == 0, "ERROR - " + error_msg

    def verify_dataset_search_without_element(self, error_msg):
        """
        Verify that is not stored in ckan when a element missing (organization, dataset, resource)
        """
        row=1
        resp= world.ckan.datastore_search_last_sql (row, self.resource, self.dataset)
        assert not (resp), "ERROR - " + error_msg

    def verify_dataset_search_values_by_row(self):
        """
         Verify that the attribute contents (value, metadata and type) are stored in ckan in row mode
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE

        for i in range(0,self.attributes_number):
            self.temp_dict=self.retry_in_datastore_search_sql_row (i, self.resource, self.dataset, self.attributes_name)

            assert self.temp_dict != u'ERROR - Attributes are missing....', u'\nERROR - Attributes %s are missing, value expected: %s \n In %s >>> %s ' %(self.attributes_name, self.attributes_value, self.dataset, self.resource)

            assert str(self.temp_dict[ATTR_VALUE]) == str(self.attributes[i][VALUE_TEMP]),\
                "The "+self.attributes[i][NAME]+" value does not match..."

            assert str(self.temp_dict[ATTR_TYPE]) == str(self.attributes[i][TYPE]),\
                "The "+self.attributes[i][NAME]+" type does not match..."

            assert self.verify_dataset_search_metadata_by_row (i), \
                "The "+self.attributes[i][NAME]+" metadata value does not match..."

    def verify_dataset_search_metadata_by_row (self, position):
        """
         Verify that the attribute metadata contents (value) are stored in ckan by row mode
        """
        if self.metadata_value:
            if self.content == general_utils.XML:
               if (self.temp_dict[ATTR_MD][0][VALUE]) != str(self.attributes[position][METADATA][CONTEXT_METADATA][0][VALUE]):
                    return False
            else:
                if str(self.temp_dict[ATTR_MD][0][VALUE]) != str(self.attributes[position][METADATAS][0][VALUE]):
                    return False
        return True
    # ------------------------------------------  mysql validations ----------------------------------------------------

    def mappingQuotes (self, attr_value):
        """
        limitation in lettuce change ' by " in mysql
        """
        temp = ""
        for i in range (len(attr_value)):
            if attr_value[i] == "'":  temp = temp + "\""
            else:temp = temp + attr_value[i]
        return temp

    def close_connection (self):
        """
        close mysql connection and delete the database used
        """
        world.mysql.set_database(self.tenant[self.cygnus_mode])
        world.mysql.disconnect()

    def verify_table_search_values_by_column(self):
        """
         Verify that the attribute contents (value) are stored in mysql in column mode
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE

        self.row= self.retry_in_table_search_sql_column (self.table, self.tenant[self.cygnus_mode], self.attributes_value)
        assert self.row != u'ERROR - Attributes are missing....', u'ERROR - Attributes are missing....'
        for i in range(len (self.attributes)):
            if str(self.row [((i+1)*2)-1]) != str(self.attributes[i][VALUE_TEMP]):                  # verify the value
                    return "The "+self.attributes[i][NAME]+" value does not match..."

    def verify_table_search_metadatas_values_by_column(self):
        """
         Verify that the attribute metadata (value) are stored in mysql in column mode
        """
        if self.attributes_metadata > 0:
            if self.row != None:
                for i in range(len (self.attributes)):
                    self.metadata = general_utils.convert_str_to_dict(self.row [(i+1)*2], general_utils.JSON)
                    if self.content == general_utils.XML:
                        if self.metadata[0][VALUE] != self.attributes[i][METADATA][CONTEXT_METADATA][0][VALUE]:
                            return "The "+self.attributes[i][NAME]+" metatada value does not match..."
                    else:
                        if self.metadata[0][VALUE] != self.attributes[i][METADATAS][0][VALUE]:
                            return "The "+self.attributes[i][NAME]+" metatada value does not match..."
                    self.metadata = None

    def verify_table_search_without_data (self, error_msg):
        """
        Verify that is not stored in mysql
        """
        row=world.mysql.table_search_one_row(self.tenant[self.cygnus_mode], self.table)
        assert row == None or row == False, u'ERROR - ' + error_msg

    def verify_table_search_values_by_row(self):
        """
          Verify that the attribute contents (value, metadata and type) are stored in mysql in row mode
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE

        for i in range(0,self.attributes_number):
            self.temp_dict=self.retry_in_table_search_sql_row (i, self.tenant[self.cygnus_mode], self.table,  self.attributes_name)

            assert self.temp_dict != u'ERROR - Attributes are missing....', \
                u'\nERROR - Attributes %s are missing, value expected: %s \n In %s >>> %s ' %(self.attributes_name, self.attributes_value, self.table, )

            assert str(self.temp_dict[6]) == str(self.attributes[i][VALUE_TEMP]),\
                "The "+self.attributes[i][NAME]+" value does not match..."

            assert str(self.temp_dict[5]) == str(self.attributes[i][TYPE]),\
                "The "+self.attributes[i][NAME]+" type does not match..."

            assert self.verify_table_search_metadata_by_row (i), \
                "The "+self.attributes[i][NAME]+" metadata value does not match..."

    def verify_table_search_metadata_by_row (self, position):
        """
         Verify that the attribute metadata contents (value) are stored in ckan by row mode
        """
        metadata_remote = general_utils.convert_str_to_dict(self.temp_dict[7], general_utils.JSON)[0][VALUE]
        if self.metadata_value:
            if self.content == general_utils.XML:
               if (metadata_remote) != str(self.attributes[position][METADATA][CONTEXT_METADATA][0][VALUE]):
                    return False
            else:
                if str(metadata_remote) != str(self.attributes[position][METADATAS][0][VALUE]):
                    return False
        return True

    # ------------------------------------------  hadoop validations ----------------------------------------------------

    def verify_file_search_values_and_type(self):
        """
        Verify that the attribute contents (type and value) are stored in hadoop un row mode
        """
        directory = "%s/%s/%s" %(self.tenant[self.cygnus_mode], self.service_path, self.resource)
        file_name = self.resource
        for i in range (int(self.attributes_number)):
            resp=world.hadoop.retry_in_file_search_data (directory, file_name, self.attributes_name+"_"+str(i), self.attributes_value)
            assert resp != u'ERROR - Attributes are missing....', u'ERROR - Attributes are missing.... (%s)' % (self.attributes_name)

    def verify_file_search_metadata(self):
        """
        Verify that the attribute contents (type and value) are stored in hadoop un row mode
        """
        directory = "%s/%s/%s" %(self.tenant[self.cygnus_mode], self.service_path, self.resource)
        file_name = self.resource
        for i in range (int(self.attributes_number)):
            resp=world.hadoop.retry_in_file_search_data (directory, file_name, self.attributes_name+"_"+str(i), self.attributes_value)
            if self.content == general_utils.XML:
                assert resp[ATTR_MD][0][VALUE] == self.attributes[i][METADATA][CONTEXT_METADATA][0][VALUE],\
                    "The "+self.attributes[i][NAME]+" metatada value does not match..."
            else:
                 assert resp[ATTR_MD][0][VALUE] == self.attributes[i][METADATAS][0][VALUE], \
                     "The "+self.attributes[i][NAME]+" metatada value does not match..."





