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

from lettuce import step, world
from tools.properties_config import Properties


#-------------------------------------------- Configuration  -------------------------------
@step (u'copy properties.json file from "([^"]*)" to test "([^"]*)" and sudo local "([^"]*)"')
def copy_properties_json_file_to_test_from_setting_and_sudo_local (step, file_name, sink, sudo_run):
    """
    copy properties.json specific to feature from setting folder, read properties and create necessaries class
    :param step:
    :param file_name: file name of configuration associated at features, stored in settings folder
    :param sink: sink used in each feature (ckan-sink |mysql-sink | hdfs-sink)
    :param sudo_run:  with superuser privileges (True | False)
    """
    if not world.background_executed:
        world.sink = sink
        properties = Properties (file=file_name, sudo=sudo_run)
        properties.read_properties()
        properties.storing_dictionaries(sink)

@step (u'configuration of cygnus instances with different ports "([^"]*)", agents files quantity "([^"]*)", id "([^"]*)" and in "([^"]*)" mode')
def  configuration_of_cygnus_instances_agents_files_and_properties_json_file_and_sink (step, different_port, quantity, id, persistence):
    """
    configuration of cygnus instances, agents files and properties.json file
    :param step:
    :param quantity: number of instances created
    :param id: identifier prefix in the instances
    :param persistence: modo used (row |column)
    """
    world.persistence = persistence
    if not world.background_executed:
        world.cygnus.config_instances(id, quantity, world.sink, persistence, different_port)

@step (u'copy flume-env.sh, grouping rules file from "([^"]*)", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "([^"]*)"')
def copy_another_configuration_files_to_cygnus(step, grouping_rules_file, only_once):
    """
    copy another configuration files used by cygnus and restart cygnus service
          - log4j.properties
          - krb5.conf
          - grouping_rules.conf
          - flume-env.sh
    :param only_once: determine if the configuration is execute only once or no (True | False)
    :param step:
    """
    world.grouping_rules_file = grouping_rules_file
    if not world.background_executed:
        world.cygnus.another_files(grouping_rules_file)
        world.cygnus.cygnus_service("restart")
    if only_once.lower() == "true":
        world.background_executed = True
    else:
         world.background_executed = False

@step (u'verify if cygnus is installed correctly')
def cygnus_is_installed_with_type(step):
    """
    Verify if cygnus is installed and the type of persistent
    :param step:
    """
    world.cygnus.verify_cygnus()

@step (u'reinitialize log file')
def reinitialize_log_file(step):
     """
     reinitialize log file
     :param step:
     """
     world.cygnus.init_log_file()

@step (u'check in log, label "([^"]*)" and text "([^"]*)"')
def check_in_log_label_and_text(step, label, text):
    """
    Verify in log file if a label with a text exists
    :param step:
    :param label: label to find
    :param text: text to find (begin since the end)
    """
    world.cygnus.verify_log(label, text)

@step (u'delete grouping rules file')
def delete_grouping_rules_file(step):
    """
    delete grouping rules file in cygnus conf remotely
    used the file name "grouping_rules_name" stored in configuration.json file
    """
    world.cygnus.delete_grouping_rules_file(world.grouping_rules_file)