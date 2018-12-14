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

import json
import os
import subprocess
from lettuce import world

from tools.cygnus import Cygnus
from tools.ckan_utils import Ckan
from tools.mysql_utils import Mysql
from tools.hadoop_utils import Hadoop
from tools.mongo_utils import Mongo

# constants
EMPTY                 = u''
FILE                  = u'file'
FILE_NAME             = u'properties.json'
CONFIGURATION_FILE    = u'configuration.json'
SETTINGS_PATH         = u'path_to_settings_folder'
SUDO                  = u'sudo'
JENKINS               = u'jenkins'



class Properties:
    """
    copy properties.json file associated to a feature from settings folder to overwrite properties.json
    after storing dictionaries
    """




    def __init__(self, **kwargs):
        """
         constructor
        :param file: properties.json file associated to a feature
        :param sudo:  with superuser privileges (True | False)
        """
        self.file_name = kwargs.get(FILE, EMPTY)
        self.sudo      = kwargs.get(SUDO, "false")

        with open(CONFIGURATION_FILE) as config_file:
            try:
                configuration = json.load(config_file)
            except Exception, e:
                assert False, 'Error parsing configuration.json file: \n%s' % (e)

        if configuration[JENKINS].lower() == "false":
            sudo_run = ""
            if self.sudo.lower() == "true": sudo_run = SUDO
            p = subprocess.Popen("%s cp -R %s/%s %s"% (sudo_run, configuration[SETTINGS_PATH], self.file_name, FILE_NAME), shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            STDOUT = p.stdout.readlines()
            assert STDOUT == [], "ERROR - coping %s from setting folder. \n %s" % (self.file_name, STDOUT)

    def read_properties(self):
        """
        Parse the JSON configuration file located in the acceptance folder and
        store the resulting dictionary in the lettuce world global variable.
        Make sure the logs path exists and create it otherwise.
        """
        with open(FILE_NAME) as config_file:
            try:
                world.config = json.load(config_file)
                if not os.path.exists(world.config["environment"]["logs_path"]):
                    os.makedirs(world.config["environment"]["logs_path"])
            except Exception, e:
                assert False, 'Error parsing config file (%s): \n%s' % (FILE_NAME, e)

    def storing_dictionaries(self, sink):
        """
        store dictionaries used by cygnus
        :param sink: ckan-sink | hdfs-sink | mysql-sink
        """
        world.cygnus    = Cygnus (protocol=world.config['cygnus']['cygnus_protocol'],
                                  host=world.config['cygnus']['cygnus_host'],
                                  port=world.config['cygnus']['cygnus_port'],
                                  management_port=world.config['cygnus']['cygnus_management_port'],
                                  version=world.config['cygnus']['cygnus_version'],
                                  verify_version=world.config['cygnus']['cygnus_verify_version'],
                                  log_level=world.config['cygnus']['cygnus_log_level'],
                                  ttl=world.config['cygnus']['cygnus_ttl'],
                                  # fabric
                                  user=world.config['cygnus']['cygnus_fabric_user'],
                                  password=world.config['cygnus']['cygnus_fabric_password'],
                                  cert_file=world.config['cygnus']['cygnus_fabric_cert_file'],
                                  error_retry=world.config['cygnus']['cygnus_fabric_error_retry'],
                                  source_path=world.config['cygnus']['cygnus_fabric_source_path'],
                                  target_path=world.config['cygnus']['cygnus_fabric_target_path'],
                                  sudo_run=world.config['cygnus']['cygnus_fabric_sudo'],
                                  # log file
                                  log_file=world.config['cygnus']['cygnus_log_file'],
                                  log_owner=world.config['cygnus']['cygnus_log_owner'],
                                  log_group=world.config['cygnus']['cygnus_log_group'],
                                  log_mod=world.config['cygnus']['cygnus_log_mod']
        )
        if sink == "ckan-sink":
            world.ckan  = Ckan(ckan_version=world.config['ckan']['ckan_version'],
                               ckan_verify_version=world.config['ckan']['ckan_verify_version'],
                               authorization=world.config['ckan']['ckan_authorization'],
                               host=world.config['ckan']['ckan_host'],
                               port=world.config['ckan']['ckan_port'],
                               orion_url=world.config['ckan']['ckan_orion_url'],
                               ssl=world.config['ckan']['ckan_ssl'],
                               capacity=world.config['ckan']['ckan_channel_capacity'],
                               transaction_capacity=world.config['ckan']['ckan_channel_transaction_capacity'],
                               retries_dataset_search=world.config['ckan']['ckan_retries_dataset_search'],
                               delay_to_retry=world.config['ckan']['ckan_delay_to_retry']
            )
        elif sink == "mysql-sink":
            world.mysql  = Mysql(host=world.config['mysql']['mysql_host'],
                                 port=world.config['mysql']['mysql_port'],
                                 user=world.config['mysql']['mysql_user'],
                                 password=world.config['mysql']['mysql_pass'],
                                 version=world.config['mysql']['mysql_version'],
                                 mysql_verify_version=world.config['mysql']['mysql_verify_version'],
                                 capacity=world.config['mysql']['mysql_channel_capacity'],
                                 transaction_capacity=world.config['mysql']['mysql_channel_transaction_capacity'],
                                 retries_number=world.config['mysql']['mysql_retries_table_search'],
                                 delay_to_retry=world.config['mysql']['mysql_delay_to_retry']
            )
        elif sink == "hdfs-sink":
            world.hadoop = Hadoop (namenode_url=world.config['hadoop']['hadoop_namenode_url'],
                                 user=world.config['hadoop']['hadoop_user'],
                                 password=world.config['hadoop']['hadoop_password'],
                                 version=world.config['hadoop']['hadoop_version'],
                                 verify_version=world.config['hadoop']['hadoop_verify_version'],
                                 manager_node_url=world.config['hadoop']['hadoop_managenode_url'],
                                 api=world.config['hadoop']['hadoop_api'],
                                 krb5_auth=world.config['hadoop']['hadoop_krb5_auth'],
                                 krb5_user=world.config['hadoop']['hadoop_krb5_user'],
                                 krb5_password=world.config['hadoop']['hadoop_krb5_password'],
                                 capacity=world.config['hadoop']['hadoop_channel_capacity'],
                                 transaction_capacity=world.config['hadoop']['hadoop_channel_transaction_capacity'],
                                 retries_number=world.config['hadoop']['hadoop_retries_open_file'],
                                 retry_delay=world.config['hadoop']['hadoop_delay_to_retry']
            )
        elif sink == "mongo-sink":
            world.mongo = Mongo (version=world.config['mongo']['mongo_version'],
                                 verify_version=world.config['mongo']['mongo_verify_version'],
                                 host=world.config['mongo']['mongo_host'],
                                 port=world.config['mongo']['mongo_port'],
                                 user=world.config['mongo']['mongo_user'],
                                 password=world.config['mongo']['mongo_password'],
                                 database=world.config['mongo']['mongo_database'],
                                 retries=world.config['mongo']['mongo_retries_search'],
                                 retry_delay=world.config['mongo']['mongo_delay_to_retry']
            )
        elif sink == "sth-sink":
            world.sth = Mongo   (version=world.config['sth']['sth_version'],
                                 verify_version=world.config['sth']['sth_verify_version'],
                                 host=world.config['sth']['sth_host'],
                                 port=world.config['sth']['sth_port'],
                                 user=world.config['sth']['sth_user'],
                                 password=world.config['sth']['sth_password'],
                                 database=world.config['sth']['sth_database'],
                                 retries=world.config['sth']['sth_retries_search'],
                                 retry_delay=world.config['sth']['sth_delay_to_retry']
            )




