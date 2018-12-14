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
import json

__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

import time

from lettuce import world
from decimal import Decimal

from tools import general_utils
from tools import http_utils
from tools.notification_utils import Notifications
from tools.fabric_utils import FabricSupport
from tools.cygnus_agent_config import Agent
from tools.cygnus_instance_config import Cygnus_Instance
from tools.cygnus_krb5_config import Krb5
from tools.cygnus_grouping_rules_config import Grouping_Rules
from tools.remote_log_utils import Remote_Log


# notification constants

VERSION                     = u'version'

# general constants
ROW_MODE         = u'row'
COL_MODE         = u'column'
CKAN_SINK        = u'ckan'
MYSQL_SINK       = u'mysql'
HDFS_SINK        = u'hdfs'
MONGO_SINK       = u'mongo'
STH_SINK         = u'sth'
DEFAULT          = u'default'
RANDOM           = u'random'
EMPTY            = u''

# ckan constants
MAX_TENANT_LENGTH              = u'abcde67890123456789012345'
MAX_SERVICE_PATH_LENGTH        = u'/abcdefghij1234567890abcdefghij1234567890abcdefgh'
MAX_RESOURCE_LENGTH            = u'123456789012345678901234567890123456789012345678901234567890123'
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

# mongo/sth
STH_DATABASE_PREFIX   = u'sth'
STH_COLLECTION_PREFIX = u'sth'
AGGR                  = u'aggr'

class Cygnus:
    """
    cygnus class with generals features
    """

    # ---------------------------------------- Configuration -----------------------------------------------------------
    def __init__(self,  **kwargs):
        """
        constructor
        :param protocol: protocol used in cygnus requests
        :param host: host used in cygnus, in multi-instances, will be incremental
        :param host: port used in cygnus
        :param management_port : management port to know the version
        :param version : cygnus version
        :param verify_version : determine if verify cygnus version or not (True | False)
        :param ttl: Number of channel re-injection retries before a Flume event is definitely discarded (-1 means infinite retries)
        :param user: user used to connect by fabric
        :param password: password used to connect by fabric, if use cert file, password will be None
        :param cert_file: cert_file used to connect by fabric, if use password, cert_file will be None
        :param error_retry: Number of times Fabric will attempt to connect when connecting to a new server
        :param source_path: source path where are templates files
        :param target_path: target path where are copied config files
        :param sudo_run: operations in cygnus with superuser privileges (True | False)
        :param log_level: log level used in cygnus (log4j.properties)
        :param log_file: log file used in cygnus
        :param log_owner: log file's owner of cygnus
        :param log_group: log file's group of cygnus
        :param log_mod: log file's mod of cygnus
        """
        self.cygnus_protocol         = kwargs.get("protocol", "http")
        self.cygnus_host             = kwargs.get("host", "localhost")
        self.cygnus_port             = kwargs.get("port", "5050")
        self.cygnus_url              = "%s://%s:%s" % (self.cygnus_protocol, self.cygnus_host, self.cygnus_port)
        self.management_port         = kwargs.get("management_port", "8081")
        self.version                 = kwargs.get("version", "0.1.0_test")
        self.verify_version          = kwargs.get("verify_version", "false")
        self.ttl                     = kwargs.get("ttl", "10")
         #fabric
        self.fabric_user             = kwargs.get("user", None)
        self.fabric_password         = kwargs.get("password", None)
        self.fabric_cert_file        = kwargs.get("cert_file", None)
        self.fabric_error_retry      = kwargs.get("error_retry", 1)
        self.fabric_source_path      = kwargs.get("source_path", "/tmp")
        self.fabric_target_path      = kwargs.get("target_path", "/tmp")
        self.fabric_sudo_cygnus      = bool(kwargs.get("sudo_run", "False"))
        # log file
        self.log_level               = kwargs.get("log_level", "INFO")
        self.log_file                = kwargs.get("log_file", "/var/log/cygnus/cygnus.log")
        self.log_owner               = kwargs.get("log_owner", "cygnus")
        self.log_group               = kwargs.get("log_group", "cygnus")
        self.log_mod                 = kwargs.get("log_mod", "775")

        self.dataset_id              = None
        self.resource_id             = None

    def configuration(self, service, service_path, entity_type, entity_id, attributes_number, attributes_name, attribute_type):
        """
        general configuration
         table (max 64 chars) = service_path + "_" + resource
        :param service:  service used in scenario
        :param service_path: service path used in scenario
        :param entity_type: entity type used in scenario
        :param entity_id: entity id used in scenario
        :param attributes_number: number of attributes used in scenario
        :param attributes_name: name of attributes used in scenario
        :param attribute_type: type of attributes used in scenario
        """
        self.dataset = None
        self.table = None

        if service == WITH_MAX_LENGTH_ALLOWED:
            self.service = MAX_TENANT_LENGTH.lower()+self.persistence
        else:
            self.service = service.lower()

        if service_path == WITH_MAX_LENGTH_ALLOWED: self.service_path = MAX_SERVICE_PATH_LENGTH.lower()
        else:
            self.service_path = service_path.lower()

        if not (self.sink.find("mongo") >= 0 or self.sink.find("sth") >= 0): # if sink is different of mongo or sth values, the service path remove "/" char if does exists
            if self.service_path[:1] == "/": self.service_path = self.service_path[1:]

        self.entity_type = entity_type
        self.entity_id = entity_id
        if (entity_type == WITH_MAX_LENGTH_ALLOWED): self.entity_type = MAX_RESOURCE_LENGTH[0:(len(MAX_RESOURCE_LENGTH)-len(self.service_path)-1)-len(entity_id)-2].lower()
        if (entity_id == WITH_MAX_LENGTH_ALLOWED):   self.entity_id = MAX_RESOURCE_LENGTH[0:(len(MAX_RESOURCE_LENGTH)-len(self.service_path)-1)-len(entity_type)-2].lower()
        self.resource = str(self.entity_id+"_"+self.entity_type).lower()

        self.attributes_number = int(attributes_number)
        self.attributes_name = attributes_name
        self.attributes_type = attribute_type

        self.dataset = self.service
        self.table = self.resource
        if self.service_path != EMPTY:
            self.dataset = self.dataset+"_"+self.service_path
            self.table = self.service_path+ "_" +self.table

    def __get_port (self, port, inc, different_port):
        """
        get port value incremented to multi-instances
        :param port: port value
        :param inc: increment
        :return port string
        """
        try:
            if different_port.lower() == "true":
                return str(int(port)+inc)
            else:
                return port
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

    def config_instances(self, id, quantity, sinks, persistence, different_port="true"):
        """
        initialize instance files
        In case of multi-instances and different_port is true, the port will be increment to initial port. ex: 5050, 5051, 5053, 5054, etc.
        :param id: postfix used in instances name
        :param quantity: number of instances
        :param sinks: sinks string list
        :param persistence: determine the mode of persistence by cygnus (row | column)
        :param different_port: determine if the port is different or not
        """
        self.instance_id       = id
        self.sink              = sinks
        self.instances_number  = quantity
        self.persistence       = persistence
        self.different_port    = different_port
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True)
        cygnus_instance = Cygnus_Instance(source_path=self.fabric_source_path, target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        cygnus_agent    = Agent(source_path=self.fabric_source_path, target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        for i in range(int(self.instances_number)):
            # generate cygnus_instance_<id>.conf ex: cygnus_instance_test_0.conf
            port = self.__get_port(self.cygnus_port, i, self.different_port)
            management_port = self.__get_port(self.management_port, i, self.different_port)
            myfab.runs(cygnus_instance.append_id(admin_port=str(management_port), id=self.instance_id+"_"+str(i)))
            # generate agent_<id>.conf ex: agent_test_0.conf
            ops_list = cygnus_agent.append_id(id=self.instance_id+"_"+str(i))
            ops_list = cygnus_agent.source(sink=sinks, channel=self.__get_channels(sinks), port=port, grouping_rules_file=self.fabric_target_path+"/grouping_rules.conf",)
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
                elif sinks_list[i].find(MONGO_SINK)>=0:
                    ops_list = cygnus_agent.config_mongo_sink(sink=sinks_list[i], channel=self.__get_channels(sinks_list[i]), host_port="%s:%s" % (world.config['mongo']['mongo_host'], world.config['mongo']['mongo_port']), user=world.config['mongo']['mongo_user'], password=world.config['mongo']['mongo_password'])
                    ops_list = cygnus_agent.config_channel (self.__get_channels(sinks_list[i]), capacity=world.config['mongo']['mongo_channel_capacity'], transaction_capacity=world.config['mongo']['mongo_channel_transaction_capacity'])
                elif sinks_list[i].find(STH_SINK)>=0:
                    ops_list = cygnus_agent.config_sth_sink(sink=sinks_list[i], channel=self.__get_channels(sinks_list[i]), host_port="%s:%s" % (world.config['sth']['sth_host'], world.config['sth']['sth_port']), user=world.config['sth']['sth_user'], password=world.config['sth']['sth_password'])
                    ops_list = cygnus_agent.config_channel (self.__get_channels(sinks_list[i]), capacity=world.config['sth']['sth_channel_capacity'], transaction_capacity=world.config['sth']['sth_channel_transaction_capacity'])

            # create and modify values in agent_<id>.conf
            myfab.runs(ops_list)

    def another_files (self, grouping_rules_file_name=DEFAULT):
        """
        copy another configuration files used by cygnus
          - flume-env.sh
          - grouping_rules.conf
          - log4j.properties
          - krb5.conf
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        myfab.current_directory(self.fabric_target_path)
        myfab.run("cp -R flume-env.sh.template flume-env.sh")
        #  grouping_rules.conf configuration
        if grouping_rules_file_name == DEFAULT:
            myfab.run("cp -R grouping_rules.conf.template grouping_rules.conf")
        elif grouping_rules_file_name != EMPTY:
             Grouping_Rules(fab_driver=myfab, file=grouping_rules_file_name, target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        else:
            myfab.run("rm -f grouping_rules.conf")
        # change to DEBUG mode in log4j.properties
        myfab.current_directory(self.fabric_target_path)
        myfab.run("cp -R log4j.properties.template log4j.properties", target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)
        myfab.run(' sed -i "s/flume.root.logger=INFO,LOGFILE/flume.root.logger=%s,LOGFILE /" log4j.properties.template' % (self.log_level))
        # krb5.conf configuration
        krb5 = Krb5(self.fabric_target_path, self.fabric_sudo_cygnus)
        myfab.runs(krb5.config_kbr5(default_realm=world.config['hadoop']['hadoop_krb5_default_realm'], kdc=world.config['hadoop']['hadoop_krb5_kdc'], admin_server=world.config['hadoop']['hadoop_krb5_admin_server'], dns_lookup_realm=world.config['hadoop']['hadoop_krb5_dns_lookup_realm'], dns_lookup_kdc=world.config['hadoop']['hadoop_krb5_dns_lookup_kdc'], ticket_lifetime=world.config['hadoop']['hadoop_krb5_ticket_lifetime'], renew_lifetime=world.config['hadoop']['hadoop_krb5_renew_lifetime'], forwardable=world.config['hadoop']['hadoop_krb5_forwardable']))

    def cygnus_service(self, operation):
        """
         cygnus service (status | stop | start | restart)
        :param operation:
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        myfab.warn_only(True)
        myfab.run("service cygnus %s" % operation, sudo=self.fabric_sudo_cygnus)

    def verify_cygnus (self):
        """
        verify if cygnus is installed correctly and its version
        """
        self.cygnus_mode = world.persistence

        with open("configuration.json") as config_file:
            try:
                configuration = json.load(config_file)
                if configuration["jenkins"].lower() == "true":
                    self.cygnus_host = "127.0.0.1"
                    self.cygnus_url  = "%s://%s:%s" % (self.cygnus_protocol, self.cygnus_host, self.cygnus_port)
            except Exception, e:
                assert False, 'Error parsing configuration.json file: \n%s' % (e)

        if self.verify_version.lower() == "true":
            management_url = "%s://%s:%s/%s" % (self.cygnus_protocol, self.cygnus_host, self.management_port, VERSION)
            resp = http_utils.request(http_utils.GET, url= management_url)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - in management operation (version)")
            body_dict= general_utils.convert_str_to_dict(resp.text, general_utils.JSON)
            assert str(body_dict[VERSION]) == self.version, \
                "Wrong cygnus version verified: %s. Expected: %s. \n\nBody content: %s" % (str(body_dict[VERSION]), str(self.version), str(resp.text))
        return True

    def init_log_file(self):
        """
        reinitialize log file
        delete and create a new log file (empty)
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        log = Remote_Log (fabric=myfab)
        log.delete_log_file()
        log.create_log_file()

    def verify_log(self, label, text):
        """
        Verify in log file if a label with a text exists
        :param label: label to find
        :param text: text to find (begin since the end)
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        log = Remote_Log (fabric=myfab)
        line = log.find_line(label, text)
        assert line != None, "ERROR  - label %s and text %s is not found.    \n       - %s" % (label, text, line)

    def delete_grouping_rules_file(self, grouping_rules_file_name):
        """
        delete grouping rules file in cygnus conf remotely
        used the file name "grouping_rules_name" stored in configuration.json file
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        myfab.current_directory(self.fabric_target_path)
        Grouping_Rules(fab_driver=myfab, file=grouping_rules_file_name, target_path=self.fabric_target_path, sudo=self.fabric_sudo_cygnus)

        grouping_rules = Grouping_Rules()
        myfab.run("rm -f %s" % grouping_rules.get_grouping_rules_file_name())

    def delete_cygnus_instances_files(self):
        """
        delete all cygnus instances files (cygnus_instance_%s_*.conf and agent_test_*.conf)
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        myfab.current_directory(self.fabric_target_path)
        myfab.run("rm -f cygnus_instance_%s_*.conf" % self.instance_id)
        myfab.run("rm -f agent_%s_*.conf" % self.instance_id)
        myfab.run("rm -f agent_%s_*.conf" % self.instance_id)


    # --------------------------------------------- general action -----------------------------------------------------

    def __split_resource (self, resource_name):
        """
        split resource in identity Id and identity Type
        """
        res = resource_name.split ("_")
        return  res [0], res [1] # identity Id , identity Type

    def get_timestamp_remote(self):
        """
        return date-time in timestamp from sth server
        :return float
        """
        myfab = FabricSupport(host=self.cygnus_host, user=self.fabric_user, password=self.fabric_password, cert_file=self.fabric_cert_file, retry=self.fabric_error_retry, hide=True, sudo=self.fabric_sudo_cygnus)
        return float(myfab.run("date +%s"))  # get timestamp

    def received_notification(self, attribute_value, metadata_value, content):
        """
        notifications
        :param attribute_value: attribute value
        :param metadata_value: metadata value (true or false)
        :param content: xml or json
        """
        if self.sink == "mysql-sink":  # limitation in lettuce change ' by " in mysql
            attribute_value = general_utils.mappingQuotes(attribute_value)
        self.content = content
        self.metadata_value = metadata_value
        metadata_attribute_number = 1
        cygnus_notification_path = u'/notify'
        notification = Notifications (self.cygnus_url+cygnus_notification_path,tenant=self.service, service_path=self.service_path, content=self.content)
        if self.metadata_value:
            notification.create_metadatas_attribute(metadata_attribute_number, RANDOM, RANDOM, RANDOM)
        notification.create_attributes (self.attributes_number, self.attributes_name, self.attributes_type, attribute_value)
        resp =  notification.send_notification(self.entity_id, self.entity_type)

        self.date_time           = self.get_timestamp_remote()
        self.attributes          = notification.get_attributes()
        self.attributes_name     = notification.get_attributes_name()
        self.attributes_value    = notification.get_attributes_value()
        self.attributes_metadata = notification.get_attributes_metadata_number()
        self.attributes_number   = int(notification.get_attributes_number())
        return resp

    def receives_n_notifications(self, notif_number, attribute_value_init):
        """
        receives N notifications with consecutive values, without metadatas and json content
        :param attribute_value_init: attribute value for all attributes in each notification increment in one
                hint: "random number=X" per attribute_value_init is not used in this function
        :param notif_number: number of notification
        """
        self.notifications_number = notif_number
        for i in range(int(notif_number)):
            temp_value = Decimal(attribute_value_init) + i
            resp = world.cygnus.received_notification(str(temp_value), "False", "json")
        self.attributes_value = attribute_value_init
        return resp

    def __change_port(self, port):
        """
        change a port used by notifications, update url variables
        :param port: new port
        """
        temp = self.cygnus_url.split(":")
        self.cygnus_url =  "%s:%s:%s" % (temp[0], temp[1], port)

    def received_multiples_notifications(self, attribute_value, metadata_value, content):
        """
        receive several notifications by each instance, but changing port
        :param attribute_value:
        :param metadata_value:
        :param content:
        :return: response
        """
        self.attrs_list = []
        for i in range(int(self.instances_number)):
            self.__change_port(self.__get_port(self.cygnus_port, i, self.different_port))
            resp = self.received_notification(attribute_value, metadata_value, content)
            http_utils.assert_status_code(http_utils.status_codes[http_utils.OK], resp, "ERROR - in multi-notifications")
            self.attrs_list.append(self.attributes) # used by verify if dates are stored



    # ------------------------------------------- CKAN Column Mode -----------------------------------------------------

    def create_organization_and_dataset(self):
        """
        Create a new organization and a dataset associated
        """
        self.dataset = self.service
        if self.service_path != EMPTY: self.dataset = self.dataset+"_"+self.service_path
        if self.service != ORGANIZATION_MISSING:
            world.ckan.create_organization (self.service)
            if self.service != ORGANIZATION_WITHOUT_DATASET:
                self.dataset_id = world.ckan.create_dataset (self.dataset)

    def create_resource_and_datastore (self, attribute_data_type, metadata_data_type):
        """
        create  a new resource and its datastore associated if it does not exists
        :param attribute_data_type: attribute data type
        :param metadata_data_type:  metadata data type
        """
        self.dataset_id = world.ckan.verify_if_dataset_exist(self.dataset)
        if (self.service != ORGANIZATION_MISSING and \
           self.service != ORGANIZATION_WITHOUT_DATASET and \
           self.dataset_id  != False) and \
           self.entity_id != RESOURCE_MISSING:
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


    # ---------------------------------------------- CKAN Row Mode -----------------------------------------------------


    # ------------------------------------------ MySQL Column Mode -----------------------------------------------------

    def create_database (self):
        """
        create a new Database per column
        :param tenant: database name
        """
        if self.service != DATABASE_MISSING:
            world.mysql.create_database (self.service)

    def create_table (self, attribute_data_type, metadata_data_type):
        """
        create a new table per column
        """

        self.table = self.resource
        if self.service_path != EMPTY: self.table = self.service_path+ "_" +self.table
        if self.service != DATABASE_MISSING and \
            self.service != DATABASE_WITHOUT_TABLE and \
            self.entity_id != RESOURCE_MISSING:
             fields = world.mysql.generate_field_datastore_to_resource (self.attributes_number, self.attributes_name, attribute_data_type, metadata_data_type)
             world.mysql.create_table (self.table, self.service, fields)

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


    # ---------------------------------------------- Mysql Row Mode ----------------------------------------------------

    # ------------------------------------------ Hadoop Row Mode -------------------------------------------------------

    def hadoop_configuration(self, tenant, service_path, resource_name, attributes_number, attributes_name, attribute_type):
        """
        hadoop configuration
        """
        if tenant == WITH_MAX_LENGTH_ALLOWED: self.service = MAX_TENANT_LENGTH.lower()
        elif tenant != DEFAULT:
            self.service = tenant.lower()
        if service_path != DEFAULT: self.service_path = service_path.lower()
        if resource_name == WITH_MAX_LENGTH_ALLOWED:
            self.resource = MAX_RESOURCE_LENGTH[0:(len(MAX_RESOURCE_LENGTH)-len(self.service_path)-1)].lower()  # file (max 64 chars) = service_path + "_" + resource
        elif resource_name == DEFAULT:
            self.resource = str(self.entity_id+"_"+self.entity_type).lower()
        else:
            self.resource = resource_name.lower()
        self.entity_id, self.entity_type = self.__split_resource (self.resource)
        if attributes_number != DEFAULT: self.attributes_number = int(attributes_number)
        if attributes_name != DEFAULT: self.attributes_name = attributes_name
        self.attributes_type = attribute_type

    # ------------------------------------------  mongo raw ------------------------------------------------------------

    def verify_mongo_version(self, driver):
        """
        verify mongo version
        if the version is incorrect show an error with both versions, the one used and the expected
        :param driver: mongo driver
        """
        driver.connect()
        resp = driver.eval_version()
        driver.disconnect()
        assert resp == u'OK', resp

    def drop_database_in_mongo(self, driver):
         """
         delete database and collections in mongo
         :param driver: mongo instance
         """
         driver.connect("%s_%s" % (STH_DATABASE_PREFIX, self.service))
         driver.drop_database()
         driver.disconnect()

    def verify_values_in_mongo(self):
        """
        verify attribute value and type from mongo
        :return document dict (cursor)
        """
        find_dict = { "attrName": {'$regex':'%s.*' % (self.attributes_name)}, #the regular expression is because in  multi attribute the name is with postfix <_value>. ex: temperature_0
                      "attrType" : self.attributes_type,
                      "attrValue" : str(self.attributes_value)
        }
        world.mongo.connect("%s_%s" % (STH_DATABASE_PREFIX, self.service))
        world.mongo.choice_collection("%s_%s_%s_%s" % (STH_COLLECTION_PREFIX, self.service_path, self.entity_id, self.entity_type))
        cursor = world.mongo.find_with_retry(find_dict)
        assert cursor.count() != 0, " ERROR - the attributes with prefix %s has not been stored in mongo successfully" % (self.attributes_name)
        world.mongo.disconnect()

    # ---------------------------------------  mongo aggregated---------------------------------------------------------

    def verify_aggregates_in_mongo(self, resolution):
        """
        verify aggregates from mongo:
            - origin, max, min, sum sum2
        :param resolution: resolutions type (  month | day | hour | minute | second )
        """

        time_zone = 2
        time.sleep(int(self.instances_number)) # delay to process all notifications and calculate aggregates
        find_dict = {"_id.attrName":  {'$regex':'%s.*' % (self.attributes_name)}, #the regular expression is because in  multi attribute the name is with postfix + <_value>. ex: temperature_0
                     "_id.resolution": str(resolution)}

        origin_year   = general_utils.get_date_only_one_value(self.date_time, "year")
        origin_month  = general_utils.get_date_only_one_value(self.date_time, "month")
        origin_day    = general_utils.get_date_only_one_value(self.date_time, "day")
        origin_hour   = int(general_utils.get_date_only_one_value(self.date_time, "hour"))-time_zone
        if origin_hour < 10: origin_hour = u'0' + str(origin_hour)
        origin_minute = general_utils.get_date_only_one_value(self.date_time, "minute")
        origin_second = general_utils.get_date_only_one_value(self.date_time, "second")

        world.sth.connect("%s_%s" % (STH_DATABASE_PREFIX, self.service))
        world.sth.choice_collection("%s_%s_%s_%s.%s" % (STH_COLLECTION_PREFIX, self.service_path, self.entity_id, self.entity_type, AGGR))
        cursor = world.sth.find_with_retry(find_dict)
        assert cursor.count() != 0, " ERROR - the aggregated has not been stored in mongo successfully "
        doc_list = world.sth.get_cursor_value(cursor)   # get all dictionaries into a cursor, return a list

        for doc in doc_list:
            offset = int(general_utils.get_date_only_one_value(self.date_time, resolution))
            if resolution == "month":
                offset=offset-1
                origin_by_resolution = "%s-01-01 00:00:00" % (origin_year)
            elif resolution == "day":
                offset=offset-1
                origin_by_resolution = "%s-%s-01 00:00:00" % (origin_year, origin_month)
            elif resolution == "hour":
                offset=offset-time_zone
                origin_by_resolution = "%s-%s-%s 00:00:00" % (origin_year, origin_month, origin_day)
            elif resolution == "minute":
                origin_by_resolution = "%s-%s-%s %s:00:00" % (origin_year, origin_month, origin_day, origin_hour)
            elif resolution == "second":
                c = 0
                MAX_SECS = 20
                while (c < MAX_SECS):
                    if float(doc["points"][offset]["min"]) == float(self.attributes_value):
                        break
                    offset = offset - 1
                    if offset < 0: offset = 59
                    c = c + 1
                if (origin_second < c): origin_minute = origin_minute - 1
                origin_by_resolution = "%s-%s-%s %s:%s:00" % (origin_year, origin_month, origin_day, origin_hour, origin_minute)
            else:
                assert False, " ERROR - resolution type \"%s\" is not allowed, review your tests in features..." % (resolution)

            sum_value  = 0
            sum2_value = 0
            for i in range(int(self.instances_number)):
                sum_value = sum_value + float(self.attributes_value)
                sum2_value = sum2_value + (float(self.attributes_value)*float(self.attributes_value))

            assert str(doc["_id"]["origin"]) == origin_by_resolution, " ERROR -- in origin field by the %s resolution in %s attribute" % (resolution, str(doc["_id"]["attrName"]))
            assert float(doc["points"][offset]["min"]) == float(self.attributes_value), " ERROR -- in minimun value into offset %s in %s attribute" % (str(offset), str(doc["_id"]["attrName"]))
            assert float(doc["points"][offset]["max"]) == float(self.attributes_value), " ERROR -- in maximun value into offset %s in %s attribute" % (str(offset), str(doc["_id"]["attrName"]))
            assert float(doc["points"][offset]["sum"]) == float(sum_value), " ERROR -- in sum value into offset %s in %s attribute" % (str(offset), str(doc["_id"]["attrName"]))
            assert float(doc["points"][offset]["sum2"]) == float(sum2_value), " ERROR -- in sum2 value into offset %s in %s attribute" % (str(offset), str(doc["_id"]["attrName"]))
        world.sth.disconnect()

    def verify_aggregates_is_not_in_mongo(self, resolution):
        """
        verify that aggregates is not stored in mongo:
        :param resolution: resolutions type (  month | day | hour | minute | second )
        """
        find_dict = {"_id.attrName" :  {'$regex':'%s.*' % (self.attributes_name)}, #the regular expression is because in  multi attribute the name is with postfix + <_value>. ex: temperature_0
                     "_id.entityId" : self.entity_id,
                     "_id.entityType" : self.entity_type,
                     "_id.resolution" : resolution }
        world.mongo.connect("%s_%s" % (STH_DATABASE_PREFIX, self.service))
        world.mongo.choice_collection("%s_%s.%s" % (STH_COLLECTION_PREFIX, self.service_path, AGGR))
        cursor = world.mongo.find_data(find_dict)
        assert cursor.count() == 0, " ERROR - the aggregated has been stored in mongo."
        world.mongo.disconnect()

    def validate_that_the_aggregated_is_calculated_successfully(self, resolution):
        """
        validate that the aggregated is calculated successfully
        """
        sum  = 0
        sum2 = 0
        offset = 0
        find_dict = {"_id.attrName":  {'$regex':'%s.*' % (self.attributes_name)}, #the regular expression is because in  multi attribute the name is with postfix + <_value>. ex: temperature_0
                     "_id.resolution": str(resolution)}

        world.sth.connect("%s_%s" % (STH_DATABASE_PREFIX, self.service))
        world.sth.choice_collection("%s_%s_%s_%s.%s" % (STH_COLLECTION_PREFIX, self.service_path, self.entity_id, self.entity_type, AGGR))
        cursor = world.sth.find_with_retry(find_dict)
        assert cursor.count() != 0, " ERROR - the aggregated has not been stored in mongo successfully "
        doc= world.sth.get_cursor_value(cursor)[0]   # get all dictionaries into a cursor, return a list

        offset = int(general_utils.get_date_only_one_value(self.date_time, resolution))
        if resolution == "month":
            offset=offset-1
        elif resolution == "day":
            offset=offset-1
        elif resolution == "hour":
            offset =offset-2

        assert float(doc["points"][offset]["min"]) == float(self.attributes_value), \
             "  ERROR - in aggregated with min %s" % (str(doc["points"][offset]["min"]))
        assert float(doc["points"][offset]["max"]) == float(self.attributes_value) + int(self.notifications_number)-1, \
             "  ERROR - in aggregated with max %s" % (str(doc["points"][offset]["max"]))
        for i in range(int(self.notifications_number)):
            v = int(self.attributes_value) + i
            sum = sum + v
        assert float(doc["points"][offset]["sum"]) == float(sum), \
             "  ERROR - in aggregated with sum %s" % (str(doc["points"][offset]["sum"]))
        for i in range(int(self.notifications_number)):
            v = int(self.attributes_value) + i
            sum2 = sum2 + (v*v)
        assert float(doc["points"][offset]["sum2"]) == float(sum2), \
             "  ERROR - in aggregated with sum2 %s" % (str(doc["points"][offset]["sum2"]))

     # ------------------------------------------  Validations ----------------------------------------------------------
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
            self.entity_id, self.entity_type = self.__split_resource (self.resource)

        self.service_path = new_service_path                                     #  used in notification request


        self.dataset = self.service+"_"+new_service_path        #  used in ckan validation request
        self.table = new_service_path +"_"+destination                           #  used in mysql validation

    # ------------------------------------------  ckan validations -----------------------------------------------------

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

    def verify_dataset_search_values_by_row(self, metadata="true"):
        """
         Verify that the attribute contents (value, metadata and type) are stored in ckan in row mode
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE

        for i in range(0,self.attributes_number):
            self.temp_dict=self.retry_in_datastore_search_sql_row (i, self.resource, self.dataset, self.attributes_name)

            assert self.temp_dict != u'ERROR - Attributes are missing....', \
                u'\nERROR - Attributes %s are missing, value expected: %s \n In %s >>> %s ' %(self.attributes_name, self.attributes_value, self.dataset, self.resource)

            assert str(self.temp_dict[ATTR_VALUE]) == str(self.attributes[i][VALUE_TEMP]),\
                "The "+self.attributes[i][NAME]+" value does not match..."

            assert str(self.temp_dict[ATTR_TYPE]) == str(self.attributes[i][TYPE]),\
                "The "+self.attributes[i][NAME]+" type does not match..."

            if metadata.lower() == "true":
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

    def close_connection (self):
        """
        close mysql connection and delete the database used
        """
        world.mysql.set_database(self.service)
        world.mysql.disconnect()

    def verify_table_search_values_by_column(self):
        """
         Verify that the attribute contents (value) are stored in mysql in column mode
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE
        self.row= self.retry_in_table_search_sql_column (self.table, self.service, self.attributes_value)
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
        row=world.mysql.table_search_one_row(self.service, self.table)
        assert row == None or row == False, u'ERROR - ' + error_msg

    def verify_table_search_values_by_row(self, metadata = "true"):
        """
          Verify that the attribute contents (value, metadata and type) are stored in mysql in row mode
        """
        if self.content == general_utils.XML:
            VALUE_TEMP = CONTENT_VALUE
        else:
             VALUE_TEMP = VALUE

        for i in range(0,self.attributes_number):
            self.temp_dict=self.retry_in_table_search_sql_row (i, self.service, self.table,  self.attributes_name)

            assert self.temp_dict != u'ERROR - Attributes are missing....', \
                u'\nERROR - Attributes %s are missing, value expected: %s \n In %s >>> %s ' %(self.attributes_name, self.attributes_value, self.table, )

            assert str(self.temp_dict[6]) == str(self.attributes[i][VALUE_TEMP]),\
                "The "+self.attributes[i][NAME]+" value does not match..."

            assert str(self.temp_dict[5]) == str(self.attributes[i][TYPE]),\
                "The "+self.attributes[i][NAME]+" type does not match..."
            if metadata.lower() == "true":
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

    # ------------------------------------------  hadoop validations ---------------------------------------------------

    def verify_file_search_values_and_type(self):
        """
        Verify that the attribute contents (type and value) are stored in hadoop un row mode
        """
        directory = "%s/%s/%s" %(self.service, self.service_path, self.resource)
        file_name = self.resource
        for i in range (int(self.attributes_number)):
            resp=world.hadoop.retry_in_file_search_data (directory, file_name, self.attributes_name+"_"+str(i), self.attributes_value)
            assert resp != u'ERROR - Attributes are missing....', u'ERROR - Attributes are missing.... (%s)' % (self.attributes_name)

    def verify_file_search_metadata(self):
        """
        Verify that the attribute contents (type and value) are stored in hadoop un row mode
        """
        directory = "%s/%s/%s" %(self.service, self.service_path, self.resource)
        file_name = self.resource
        for i in range (int(self.attributes_number)):
            resp=world.hadoop.retry_in_file_search_data (directory, file_name, self.attributes_name+"_"+str(i), self.attributes_value)
            if self.content == general_utils.XML:
                assert resp[ATTR_MD][0][VALUE] == self.attributes[i][METADATA][CONTEXT_METADATA][0][VALUE],\
                    "The "+self.attributes[i][NAME]+" metatada value does not match..."
            else:
                 assert resp[ATTR_MD][0][VALUE] == self.attributes[i][METADATAS][0][VALUE], \
                     "The "+self.attributes[i][NAME]+" metatada value does not match..."

    # ------------------------------------------  mongo validations ---------------------------------------------------





