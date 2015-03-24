# -*- coding: utf-8 -*-
#
# Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact:
#  iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

from integration.notifications.common_steps.configuration import *   # steps to pre-configurations

#----------------------------------------------------------------------------------

@step (u'Close mysql connection')
def close_mysql_connection(step):
    """
    Close mysql connection
    :param step:
    """
    world.cygnus.close_connection()

@step(u'"([^"]*)" is installed correctly')
def mysql_is_installed_correctly(step, sink):
    """
     verify that Mysql is installed correctly, version is controlled
    :param sink:
    :param step:
    """
    world.sink = sink
    world.mysql.connect()
    world.mysql.verify_version()

@step (u'create a new database "([^"]*)"')
def create_a_new_database (step, tenant):
    """
    create a new Database per column
    :param tenant:
    :param step:
    :param DBname: database name
    """
    world.organization_operation = tenant  # this flag is used at create a new table
    world.cygnus.create_database(tenant)

@step (u'create a new table "([^"]*)" with service path "([^"]*)", "([^"]*)" attributes called "([^"]*)", attribute type "([^"]*)", attribute data type "([^"]*)" and metadata data type "([^"]*)"')
def create_a_new_table_with_service_attributes_attribute_type_attribute_data_type_and_metadata_data_type (step, resource_name, service_path, attributes_number, attribute_name, attribute_type, attribute_data_type, metadata_data_type):
    """
    create a new table to column mode
    :param step:
    :param resource_name:
    :param service_path:
    :param attributes_number:
    :param attribute_name:
    :param attribute_type:
    :param attribute_data_type:
    :param metadata_data_type:
    """
    world.cygnus.create_table (resource_name, service_path, attributes_number, attribute_name, attribute_type, attribute_data_type, metadata_data_type)

@step (u'receives a notification with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
def receives_a_notification_with_attributes_value_metadata_value_and_content (step, attribute_value, metadata_value, content):
    """
    store notification values in mysql
    :param step:
    :param attribute_value:
    :param metadata_value:
    :param content:
    """
    world.resp = world.cygnus.received_notification(world.cygnus.mappingQuotes (attribute_value), metadata_value, content)

@step (u'a tenant "([^"]*)", service path "([^"]*)", resource "([^"]*)", with attribute number "([^"]*)", attribute name "([^"]*)" and attribute type "([^"]*)"')
def a_tenant_service_path_resource_with_attribute_number_and_attribute_name (step, tenant, service_path, resource_name,attribute_number, attribute_name, attribute_type):
    """
    row configuration in row mode
    :param step:
    :param tenant:
    :param service_path:
    :param resource_name:
    :param attribute_number:
    :param attribute_name:
    :param attribute_type:
    """
    world.cygnus.row_configuration(tenant, service_path, resource_name,attribute_number, attribute_name, attribute_type)

@step (u'changes new destination "([^"]*)" where to verify in table "([^"]*)"')
def changes_new_destination_where_to_verify_in_table (step, new_destination, new_service_path):
    """
    change new destination and dataset to validate
    :param step:
    :param new_destination:
    :param new_service_path:
    """
    world.cygnus.change_destination_to_pattern (new_destination, new_service_path)

# ------------------------------------------------------------------------------------------------------------------
@step (u'Verify that the attribute value is stored in mysql')
def verify_that_the_attribute_value_is_stored_in_mysql(step):
    """
    Validate that the attribute value and type are stored in mysql per column
    :param step:
    """
    world.cygnus.verify_table_search_values_by_column()

@step (u'Verify the metadatas are stored in mysql')
def verify_the_metadatas_are_stored_in_mysql(step):
    """
    Validate that the attribute metadata is stored in mysql per column
    :param step:
    """
    world.cygnus.verify_table_search_metadatas_values_by_column()

@step (u'Verify that is not stored in mysql "([^"]*)"')
def verify_that_is_not_stored_in_mysql (step, error_msg):
    """
    Verify that is not stored in mysql
    :param step:
    :param error_msg:
    """
    world.cygnus.verify_table_search_without_data (error_msg)

@step (u'update real values in resource "([^"]*)" and service path "([^"]*)" to notification request')
def update_real_values_in_resource_and_service_path_to_notification_request (step, resource, service_path):
    """
    change real resource and service path to notification request
    :param step:
    :param resource:
    :param service_path:
    """
    world.cygnus.change_destination_to_pattern (resource, service_path)

@step (u'Validate that the attribute value, metadata and type are stored in mysql')
def validate_that_the_attribute_value_and_type_are_stored_in_mysql (step):
    """
    Validate that the attributes values and type are stored in mysql per row mode
    :param step:
    """
    world.cygnus.verify_table_search_values_by_row()

#----------------------------------------------------------------------------------
