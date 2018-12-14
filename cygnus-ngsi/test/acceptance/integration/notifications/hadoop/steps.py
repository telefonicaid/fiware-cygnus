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

from integration.notifications.common_steps.multi_instances import * # common_steps to multi-instances
from integration.notifications.common_steps.configuration import *   # common_steps to pre-configurations
from integration.notifications.common_steps.notifications import *   # common_steps to notifications
from integration.notifications.common_steps.grouping_rules import *   # common_steps to grouping rules

# ----------------------------------- COMMON STEPS ------------------------------------
# ---------------------------- configuration.py --------------------------------------
# @step (u'copy properties.json file from "([^"]*)" to test "([^"]*)" and sudo local "([^"]*)"')
# @step (u'configuration of cygnus instances with different ports "([^"]*)", agents files quantity "([^"]*)", id "([^"]*)" and in "([^"]*)" mode')
# @step (u'copy flume-env.sh, grouping rules file from "([^"]*)", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "([^"]*)"')
# @step (u'verify if cygnus is installed correctly')
# @step (u'reinitialize log file')
# @step (u'check in log, label "([^"]*)" and text "([^"]*)"')
# @step (u'delete grouping rules file')

# --------------------------- notifications.py ------------------------------------
# @step (u'service "([^"]*)", service path "([^"]*)", entity type "([^"]*)", entity id "([^"]*)", with attribute number "([^"]*)", attribute name "([^"]*)" and attribute type "([^"]*)"')
# @step(u'receives a notification with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
# @step (u'receives "([^"]*)" notifications with consecutive values beginning with "([^"]*)" and with one step')
# @step (u'receives multiples notifications one by instance and the port defined incremented with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
# @step(u'receive an "([^"]*)" http code')

# --------------------------- grouping_rules.py -----------------------------------
# @step (u'update real values in resource "([^"]*)" and service path "([^"]*)" to notification request')
# @step (u'changes new destination "([^"]*)" where to verify in dataset "([^"]*)"')

# --------------------------- multi_instances.py ----------------------------------
# @step (u'delete instances files')

#----------------------------------------------------------------------------------
@step(u'verify if hadoop is installed correctly')
def hadoop_is_installed_correctly(step):
    """
     verify that Hadoop is installed correctly, version is controlled
     see "hadoop_verify_version" property in properties.json file
    :param step:
    """
    world.hadoop.manager_version()

@step(u'delete the file created in hadoop')
def delete_the_file_created_in_hadoop (step):
    """
    delete the file created in hadoop
    :param step:
    """
    world.hadoop.delete_directory()

#------------------------------- validations -----------------------------------

@step (u'Validate that the attribute value and type are stored in hadoop')
def validate_that_the_attribute_value_and_type_are_stored_in_hadoop(step):
    """
    Validate that the attribute value and type are stored in hadoop
    :param step:
    """
    world.cygnus.verify_file_search_values_and_type()

@step (u'Validate that the attribute metadatas are stored in hadoop')
def validate_that_the_attribute_metadatas_are_stored_in_hadoop(step):
    """
    Validate that the attribute metadata are stored in hadoop
    :param step:
    """
    world.cygnus.verify_file_search_metadata()



