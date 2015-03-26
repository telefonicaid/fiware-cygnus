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
def ckan_is_installed_correctly(step, sink):
    """
     verify that CKAN is installed correctly, version is controlled
    :param sink:
    :param step:
    """
    world.sink = sink
    world.ckan.verify_version ()

@step(u'create a new organization "([^"]*)" with a dataset "([^"]*)"')
def create_a_new_organization (step, tenant, service_path):
    """
    create a new organization and a new dataset if they do exists
    :param tenant:
    :param service_path:
    :param step:
    :param orgName: organization name (tenant).
    :param servPath: service path
    """
    world.organization_operation = tenant  # this flag is used at create a new resource
    world.cygnus.create_organization_and_dataset(tenant, service_path)

@step (u'create a new resource "([^"]*)" with "([^"]*)" attributes called "([^"]*)", attribute type "([^"]*)", attribute data type "([^"]*)" and metadata data type "([^"]*)"')
def create_a_new_resource_with_attrValue_data_type_and_metadata_data_type (step, resource_name, attribute_number, attribute_name, attribute_type,attribute_data_type, metadata_data_type):
    """
    Create a new resource with a datastore if it does not exists
    :param attribute_type:
    :param step:
    :param resource_name: resource name
    :param attribute_number: Quantity of attributes
    :param attribute_name: attribute name
    :param attribute_data_type: attribute data type
    :param metadata_data_type:  metadata data type
    """
    world.cygnus.create_resource_and_datastore (resource_name, attribute_number, attribute_name, attribute_type, attribute_data_type, metadata_data_type)

@step (u'receives a notification with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
def receives_a_notification_with_attributes_value_metadata_value_and_content (step, attribute_value, metadata_value, content):
    """
    store notification values in ckan
    :param step:
    :param attribute_value:
    :param metadata_value:
    :param content:
    """
    world.resp = world.cygnus.received_notification(attribute_value, metadata_value, content)

@step (u'a tenant "([^"]*)", service path "([^"]*)", resource "([^"]*)", with attribute number "([^"]*)", attribute name "([^"]*)" and attribute type "([^"]*)"')
def a_tenant_service_path_resource_with_attribute_number_and_attribute_name (step, tenant, service_path, resource_name,attribute_number, attribute_name, attribute_type):
    """
    ckan configuration in row mode
    :param step:
    :param tenant:
    :param service_path:
    :param resource_name:
    :param attribute_number:
    :param attribute_name:
    :param attribute_type:
    """
    world.cygnus.row_configuration(tenant, service_path, resource_name,attribute_number, attribute_name, attribute_type)

@step (u'update real values in resource "([^"]*)" and service path "([^"]*)" to notification request')
def update_real_values_in_resource_and_service_path_to_notification_request (step, resource, service_path):
    """
    change real resource and service path to notification request
    :param step:
    :param resource:
    :param service_path:
    """
    world.cygnus.change_destination_to_pattern (resource, service_path)

@step (u'changes new destination "([^"]*)" where to verify in dataset "([^"]*)"')
def changes_new_destination_where_to_verify_in_dataset (step, destination, dataset):
    """
    change new destination and dataset to validate
    :param step:
    :param destination:
    :param dataset:
    """
    world.cygnus.change_destination_to_pattern (destination, dataset)

#----------------------------- validations -----------------------------------------------------
@step(u'I receive an "([^"]*)" http code')
def i_receive_an_http_code (step, http_code_expected):
    """
    validate http code in response
    :param step:
    :param http_code_expected:  http code for validate
    """
    world.cygnus.verify_response_http_code (http_code_expected, world.resp)

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

@step (u'Validate that the attribute value, metadata and type are stored in ckan')
def validate_that_the_attribute_value_and_type_are_stored_in_ckan (step):
    """
    Validate that the attributes values and type are stored in ckan per row mode
    :param step:
    """
    world.cygnus.verify_dataset_search_values_by_row()

