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
@step(u'verify if ckan is installed correctly')
def ckan_is_installed_correctly(step):
    """
    verify that CKAN is installed correctly, version is controlled
    :param step:
    """
    world.ckan.verify_version()

@step (u'create a new organization with a dataset and a new resource with attribute data type "([^"]*)" and metadata data type "([^"]*)"')
def create_a_new_organization_with_a_dataset_and_a_new_resource_with_attribute_data_type_and_metadata_data_type(step, attribute_data_type, metadata_data_type):
    """
    create a new organization with a dataset and a new resource with attribute data type "([^"]*)" and metadata data type
    :param step:
    :param attribute_data_type: attribute data type
    :param metadata_data_type:  metadata data type
    """
    world.cygnus.create_organization_and_dataset()
    world.cygnus.create_resource_and_datastore (attribute_data_type, metadata_data_type)

#----------------------------- validations -----------------------------------------------------

@step (u'Verify that the attribute value is stored in ckan')
def verify_that_the_attribute_value_is_stored_in_ckan(step):
    """
    Validate that the attributes values are stored in ckan per column mode
    :param step:
    """
    world.cygnus.verify_dataset_search_values_by_column()

@step (u'Verify the metadatas are stored in ckan')
def verify_that_the_metadata_are_stored_in_ckan(step):
    """
    Validate that the attribute metadatas values are stored in ckan per column
    :param step:
    """
    world.cygnus.verify_dataset_search_metadata_values_by_column()

@step (u'Verify that is not stored in ckan "([^"]*)"')
def verify_that_is_not_stored_in_ckan(step, error_msg):
    """
    Verify that is not stored in ckan
    :param step:
    :param error_msg:
    """
    world.cygnus.verify_dataset_search_without_data(error_msg)

@step (u'Verify that is not stored if element does not exist "([^"]*)" in ckan')
def verify_that_is_not_stored_if_element_does_not_exist(step, error_msg):
    """
    Verify that is not stored in ckan
    :param step:
    :param error_msg:
    """
    world.cygnus.verify_dataset_search_without_element(error_msg)

@step (u'Validate that the attribute value, metadata "([^"]*)" and type are stored in ckan')
def validate_that_the_attribute_value_and_type_are_stored_in_ckan(step, metadata):
    """
    Validate that the attributes values and type are stored in ckan per row mode
    :param step:
    """
    world.cygnus.verify_dataset_search_values_by_row(metadata)

