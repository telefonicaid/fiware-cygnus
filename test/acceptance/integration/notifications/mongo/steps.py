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
# iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

from integration.notifications.common_steps.multi_instances import * # steps to multi-instances
from integration.notifications.common_steps.configuration import *   # steps to pre-configurations
from integration.notifications.common_steps.notifications import *   # steps to notifications

#  -------------------------------  notifications ----------------------------------------------------------------------
#  steps in integration.notifications.common_steps.notifications.py:
#    - service "([^"]*)", service path "([^"]*)", resource "([^"]*)", with attribute number "([^"]*)", attribute name "([^"]*)" and attribute type "([^"]*)
#    - receives a notification with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"
#    - receives "([^"]*)" notifications with consecutive values beginning with "([^"]*)" and with one step


# ------------------------------------------------ validations --------------------------------------------------------
@step(u'verify if mongo is installed correctly')
def mysql_is_installed_correctly(step):
    """
     verify that mongo is installed correctly, the version is controlled
    :param step:
    """
    world.cygnus.verify_mongo_version()

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
    world.sth.verify_aggregates_in_mongo(resolution)

@step(u'delete database in mongo')
def delete_database_in_mongo(step):
    """
    delete database and collections in mongo
    :param step:
    """
    world.cygnus.drop_database_in_mongo(world.mongo)

