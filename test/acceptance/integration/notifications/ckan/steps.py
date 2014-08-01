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
from myTools.ckan import *


#----------------------------------------------------------------------------------
@step(u'"([^"]*)" is installed correctly')
def ckan_is_installed_correctly(step, operation):
    """
     verify that CKAN is installed correctly, version is controlled
    :param step:
    """
    world.operation = operation
    world.ckan.versionCKAN()

#----------------------------------------------------------------------------------
@step (u'store in ckan with a organization "([^"]*)", resource "([^"]*)" and the attribute number "([^"]*)", the compound number "([^"]*)", the metadata number "([^"]*)" and content "([^"]*)"')
def new_notifications_in_a_organization_by_default_with_content(step, organization, resource, attributesNumber, compoundNumber, metadatasNumber, content):
    """
    Store a notification in Ckan with different scenarios
    :param step:
    :param organization:
    :param resource:
    :param attributesNumber:
    :param compoundNumber:
    :param metadatasNumber:
    :param content:
    """
    world.content = content
    world.cygnus.notification (organization, resource, content, attributesNumber, compoundNumber, metadatasNumber, ERROR[NOT])

#----------------------------------------------------------------------------------
@step(u'I receive an "([^"]*)" http code')
def i_receive_an_http_code (step, httpCode):
    """
    validate http code in response
    :param httpCode:  http code for validate
    """
    status = world.response.status
    body = world.body
    utils.validateHTTPCode(httpCode, status, body)

@step (u'Validate that the attribute value and type are stored in ckan')
def validate_that_the_attribute_value_and_type_are_stored_in_ckan(step):
    """
    Validate that the attribute value and type are stored in ckan
    """
    resp = world.ckan.verifyDatasetSearch_valuesAndType (world.content)
    world.ckan.validateResponse (resp)


@step (u'Validate that the attribute metadatas are stored in ckan')
def validate_that_the_attribute_metadatas_are_stored_in_ckan(step):
    """
    Validate that the attribute value and type are stored in ckan
    """
    resp = world.ckan.verifyDatasetSearch_metadatas (world.content)
    world.ckan.validateResponse (resp)

@step (u'Validate that the dataset is not created in ckan')
def validate_that_the_dataset_is_not_created_in_ckan (step):
    """
    Validate that dataset is not created in ckan
    :param step:
    """
    world.ckan.verifyIfDatasetExist()





