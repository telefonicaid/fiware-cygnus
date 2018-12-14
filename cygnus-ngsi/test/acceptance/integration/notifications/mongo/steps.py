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

@step(u'verify if mongo is installed correctly')
def mongo_is_installed_correctly(step):
    """
     verify that mongo is installed correctly, the version is controlled
    :param step:
    """
    driver = None
    if world.sink == "mongo-sink":
        driver = world.mongo
    elif world.sink == "sth-sink":
        driver = world.sth
    else:
        raise Exception(" ERROR - unknown sink: %s" % world.sink)
    world.cygnus.verify_mongo_version(driver)

@step(u'delete database in mongo')
def delete_database_in_mongo(step):
    """
    delete database and collections in mongo
    :param step:
    """
    driver = None
    if world.sink == "mongo-sink":
        driver = world.mongo
    elif world.sink == "sth-sink":
        driver = world.sth
    else:
        raise Exception(" ERROR - unknown sink: %s" % world.sink)
    world.cygnus.drop_database_in_mongo(driver)

# ---------------------------------  validations -----------------------------------

@step (u'validate that the attribute value and type are stored in mongo')
def validate_that_the_attribute_value_metadata_and_type_are_stored_in_mongo(step):
    """
    Validate that the attributes values type are stored in mongo
    :param step:
    """
    world.cygnus.verify_values_in_mongo()

@step(u'validate that the aggregated value is generate by resolution "([^"]*)" in mongo')
def validate_that_the_aggregated_values_are_generate_in_mongo(step, resolution):
    """
    Validate that the aggregated value is generate in mongo by several resolutions
          - origin, max, min, sum sum2
    :param step:
    :param resolution: resolutions type (  month | day | hour | minute | second )
    """
    world.cygnus.verify_aggregates_in_mongo(resolution)

@step(u'validate that the aggregated is calculated successfully with resolution "([^"]*)"')
def validate_that_the_aggregated_is_calculated_successfully(step, resolution):
    """
    validate that the aggregated is calculated successfully
    :param method: resolution
    :param step:
    """
    world.cygnus.validate_that_the_aggregated_is_calculated_successfully(resolution)

