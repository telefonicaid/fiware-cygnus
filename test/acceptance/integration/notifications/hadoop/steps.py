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
from myTools.hadoop_utils import *


#----------------------------------------------------------------------------------
@step (u'cygnus is installed with type "([^"]*)"')
def cygnus_is_installed_with_type(step, type):
    """
    Verify if cygnus is installed and the type of persistent
    :param step:
    :param type: type of persistent (ROW or COLUMN)
    """
    world.cygnus_type = type

@step(u'"([^"]*)" is installed correctly')
def is_installed_correctly(step, operation):
    """
     verify that hadoop is installed correctly
    :param step:
    """
    world.operation = operation
    world.hadoop.version()

#----------------------------------------------------------------------------------
@step (u'store in hadoop with a organization "([^"]*)", resource "([^"]*)" and the attribute number "([^"]*)", the compound number "([^"]*)", the metadata number "([^"]*)" and content "([^"]*)"')
def new_notifications_in_a_organization_by_default_with_content(step, organization, resource, attributesNumber, compoundNumber, metadatasNumber, content):
    """
    Store a notification in hadoop with different scenarios
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

@step (u'append in hadoop with a organization "([^"]*)", resource "([^"]*)", with "([^"]*)" new attributes values "([^"]*)", the metadata value "([^"]*)" and content "([^"]*)"')
def append_in_hadoop_with_a_organization_resource_with_new_attributes_values_the_metadata_value_and_content (step, organization, resource, attributesQuantity, attrValue, metadataValue, content):
    """
    Store a notification in hadoop with different values
    :param step:
    :param organization:
    :param resource:
    :param attributesQuantity:
    :param attrValue:
    :param metadataValue:
    :param content:
    """
    world.hadoop.parametersToNotification_col(organization, resource, attributesQuantity, metadataValue, content)
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

@step (u'Validate that the attribute value and type are stored in hadoop')
def validate_that_the_attribute_value_and_type_are_stored_in_hadoop(step):
    """
    Validate that the attribute value and type are stored in hadoop
    """
    resp = world.hadoop.verifyDatasetSearch_valuesAndType(world.content)
    world.hadoop.validateResponse (resp)


@step (u'Validate that the attribute metadatas are stored in hadoop')
def validate_that_the_attribute_metadatas_are_stored_in_hadoop(step):
    """
    Validate that the attribute value and type are stored in hadoop
    """
    resp = world.hadoop.verifyDatasetSearch_metadatas (world.content)
    world.hadoop.validateResponse (resp)

@step (u'Validate that the file is not created in hadoop')
def validate_that_the_file_is_not_created_in_hadoop (step):
    """
    Validate that dataset is not created in hadoop
    :param step:
    """
    pass


@step(u'delete the file created in hadoop')
def delete_the_file_created_in_hadoop (step):
    """
    delete the file created in hadoop
    :param step: 
    """
    world.hadoop.deleteFile()



