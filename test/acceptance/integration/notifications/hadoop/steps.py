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
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'


from integration.notifications.common_steps.configuration import *   # steps to pre-configurations

#----------------------------------------------------------------------------------
@step(u'"([^"]*)" is installed correctly')
def hadoop_is_installed_correctly(step, sink):
    """
     verify that Hadoop is installed correctly, version is controlled
     see "hadoop_verify_version" property in properties.json file
    :param sink:
    :param step:
    """
    world.sink = sink
    if str(world.hadoop.verify_version).find("True") >= 0:
        world.hadoop.manager_version ()

@step (u'a tenant "([^"]*)", service path "([^"]*)", resource "([^"]*)", with attribute number "([^"]*)", attribute name "([^"]*)" and attribute type "([^"]*)"')
def a_tenant_service_path_resource_with_attribute_number_and_attribute_name (step, tenant, service_path, resource_name,attribute_number, attribute_name, attribute_type):
    """
    hadoop configuration in row mode
    :param step:
    :param tenant:
    :param service_path:
    :param resource_name:
    :param attribute_number:
    :param attribute_name:
    :param attribute_type:
    """
    world.tenant = tenant
    world.cygnus.hadoop_configuration(tenant, service_path, resource_name,attribute_number, attribute_name, attribute_type)


@step (u'receives a notification with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
def receives_a_notification_with_attributes_value_metadata_value_and_content (step, attribute_value, metadata_value, content):
    """
    store notification values in hadoop
    :param step:
    :param attribute_value:
    :param metadata_value:
    :param content:
    """
    world.resp = world.cygnus.received_notification(attribute_value, metadata_value, content)

@step(u'I receive an "([^"]*)" http code')
def i_receive_an_http_code (step, http_code_expected):
    """
    validate http code in response
    :param step:
    :param http_code_expected:  http code for validate
    """
    world.cygnus.verify_response_http_code (http_code_expected, world.resp)

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

@step(u'delete the file created in hadoop')
def delete_the_file_created_in_hadoop (step):
    """
    delete the file created in hadoop
    :param step:
    """
    world.hadoop.delete_directory(world.tenant)

@step (u'changes new destination "([^"]*)" where to verify in dataset "([^"]*)"')
def changes_new_destination_where_to_verify_in_dataset (step, destination, dataset):
    """
    change new destination and dataset to validate
    :param step:
    :param destination:
    :param dataset:
    """
    world.cygnus.change_destination_to_pattern (destination, dataset)

