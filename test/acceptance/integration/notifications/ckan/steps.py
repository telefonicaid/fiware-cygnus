# -*- coding: utf-8 -*-
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# frb@tid.es
#
#      Author: Ivan Arias
#

from lettuce import step
from myTools.ckan_utils import *


#----------------------------------------------------------------------------------
@step(u'"([^"]*)" is installed correctly')
def ckan_is_installed_correctly(step, operation):
    """
     verify that CKAN is installed correctly, version is controlled
    :param step:
    """
    world.operation = operation
    world.ckan.versionCKAN()

@step (u'cygnus is installed with type "([^"]*)"')
def cygnus_is_installed_with_type(step, type):
    """
    Verify if cygnus is installed and the type of persistent
    :param step:
    :param type: type of persistent (ROW or COLUMN)
    """
    world.cygnus.verifyCygnus (type)

@step(u'create a new organization "([^"]*)"')
def create_a_new_organization (step, orgName):
    """
    create a new organization and a new dataset if they do exists
    :param step:
    :param orgName: organization name. the dataset name is organizationName_packageDefault
    """
    world.organizationOperation = orgName
    world.ckan.createOrganization (orgName)

@step (u'create a new resource "([^"]*)" with "([^"]*)" attributes, attrValue data type "([^"]*)" and metadata data type "([^"]*)"')
def create_a_new_resource_with_attrValue_data_type_and_metadata_data_type (step, resourceName, attrQuantity, attrValueType, metadataType):
    """
    Create a new resource with a datastore if it does not exists
    :param step:
    :param resourceName: resource name
    :param attrQuantity: Quantity of attributes
    :param attrValueType: attribute data type
    :param metadataType:  metadata data type
    """
    world.resourceOperation = resourceName
    world.metadataType = metadataType
    world.ckan.createResource(resourceName, attrQuantity, attrValueType, metadataType)

#----------------------------------------------------------------------------------
@step (u'store in ckan with a organization "([^"]*)", resource "([^"]*)" and the attribute number "([^"]*)", the compound number "([^"]*)", the metadata number "([^"]*)" and content "([^"]*)"')
def new_notifications_in_a_organization_by_default_with_content(step, organization, resource, attributesNumber, compoundNumber, metadatasNumber, content):
    """
    Store a notification in Ckan with different scenarios in row type
    :param step:
    :param organization:
    :param resource:
    :param attributesNumber:
    :param compoundNumber:
    :param metadatasNumber:
    :param content:
    """
    world.content = content
    world.cygnus.notification_row (organization, resource, content, attributesNumber, compoundNumber, metadatasNumber, ERROR[NOT])

@step(u'append a new attribute values "([^"]*)", the metadata value "([^"]*)" and content "([^"]*)"')
def append_new_attribute_values_the_metadata_value_and_content(step, attrValue, metadataValue, content):
    """
    store in ckan values in column type
    :param step:
    :param attrValue:
    :param metadataValue:
    :param content: (XML or JSON)
    """
    world.content = content
    world.metadataValue = metadataValue
    world.cygnus.notification_col (attrValue, metadataValue, content, ERROR[NOT])

#----------------------------------------------------------------------------------
@step(u'I receive an "([^"]*)" http code')
def i_receive_an_http_code (step, httpCode):
    """
    validate http code in response
    :param httpCode:  http code for validate
    """
    status = world.response.status
    body = world.body
    general_utils.validateHTTPCode(httpCode, status, body)

@step (u'Validate that the attribute value and type are stored in ckan')
def validate_that_the_attribute_value_and_type_are_stored_in_ckan(step):
    """
    Validate that the attribute value and type are stored in ckan per row
    """
    resp = world.ckan.verifyDatasetSearch_valuesAndType(world.content)
    world.ckan.validateResponse (resp)


@step (u'Validate that the attribute metadatas are stored in ckan')
def validate_that_the_attribute_metadatas_are_stored_in_ckan(step):
    """
    Validate that the attribute value and type are stored in ckan per row
    """
    resp = world.ckan.verifyDatasetSearch_metadatas (world.content)
    world.ckan.validateResponse (resp)

@step (u'Validate that the dataset is not created in ckan')
def validate_that_the_dataset_is_not_created_in_ckan(step):
    """
    Validate that dataset is not created in ckan per row
    :param step:
    """
    world.ckan.verifyIfDatasetExist()

@step (u'Verify that the attribute value is stored in ckan')
def verify_that_the_attribute_value_is_stored_in_ckan(step):
    """
    Validate that the attributes values are stored in ckan per column
    :param step:
    """
    resp = world.ckan.verifyDatasetSearch_values_column(world.content)
    world.ckan.validateResponse (resp)
    pass

@step (u'Verify the metadatas are stored in ckan')
def verify_that_the_metadata_are_stored_in_ckan(step):
    """
    Validate that the metadatas values are stored in ckan per column
    :param step:
    """
    resp = world.ckan.verifyDatasetSearch_metadata_column (world.content)
    world.ckan.validateResponse (resp)
    pass

@step (u'Verify the notification is not stored in ckan')
def verify_that_the_notification_is_not_stored_in_ckan (step):
    """
    Validate that the attributes values are not stored in ckan per column
    :param step:
    """
    world.ckan.verifyIfResourceIsEmpty ()

@step(u'Verify that the organization does not exist in ckan')
def verify_that_the_organization_does_not_exist_in_ckan(step):
    """
    Validate that the organization is  not created in ckan per column
    :param step:
    """
    world.ckan.verifyOrganizationNotExist()

@step(u'Verify that the dataset does not exist in ckan')
def verify_that_the_dataset_does_not_exist_in_ckan(step):
    """
    Validate that the dataset is  not created in ckan per column
    :param step:
    """
    world.ckan.verifyDatasetNotExist ()

@step(u'Verify that the resource does not exist in ckan')
def verify_that_the_resource_does_not_exist_in_ckan(step):
    """
    Validate that the resource is  not created in ckan per column
    :param step:
    """
    world.ckan.verifyResourceNotExist ()