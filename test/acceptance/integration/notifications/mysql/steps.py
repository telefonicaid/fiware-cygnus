# -*- coding: utf-8 -*-
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# francisco.romerobueno@telefonica.com
#
#      Author: Ivan Arias
#

from lettuce import step
from myTools.mysql_utils import *


#----------------------------------------------------------------------------------
@step(u'"([^"]*)" is installed correctly')
def mysql_is_installed_correctly(step, operation):
    """
     verify that MySQL is installed correctly, version is controlled
    :param step:
    """
    world.operation = operation
    world.mysql.openConnection ()

@step (u'cygnus is installed with type "([^"]*)"')
def cygnus_is_installed_with_type(step, type):
    """
    Verify if cygnus is installed and the type of persistent
    :param step:
    :param type: type of persistent (ROW or COLUMN)
    """
    world.cygnus.verifyCygnus (type)

@step (u'Close mysql connection')
def close_mysql_connection(step):
    """
    Close mysql connection
    :param step:
    """
    world.mysql.closeConnection()

@step (u'create a new database "([^"]*)"')
def create_a_new_database (step, organization):
    """
    create a new Database per column
    :param step:
    :param DBname: database name
    """
    world.organizationOperation = organization
    world.mysql.createDB(organization)

@step (u'create a new table "([^"]*)" with "([^"]*)" attributes, attrValue data type "([^"]*)" and metadata data type "([^"]*)"')
def create_a_new_table_with_attrValue_data_type_and_metadata_data_type (step, resource, attrQuantity, attrValueType, metadataType):
    #world.mysql.createTable (tableName, attrValueType, metadataType)
    """
    create a new table per column
    :param step:
    :param tableName:
    :param attrQuantity:
    :param attrValueType:
    :param metadataType:
    """
    world.metadataType = metadataType
    world.resourceOperation = resource
    world.mysql.createTable(resource, attrQuantity, attrValueType, metadataType)


#----------------------------------------------------------------------------------
@step (u'store in mysql with a organization "([^"]*)", resource "([^"]*)" and the attribute number "([^"]*)", the compound number "([^"]*)", the metadata number "([^"]*)" and content "([^"]*)"')
def new_notifications_in_a_organization_by_default_with_content(step, organization, resource, attributesNumber, compoundNumber, metadatasNumber, content):
    """
    Store a notification in mysql with different scenarios per rows
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

@step (u'append a new attribute values "([^"]*)", the metadata value "([^"]*)" and content "([^"]*)"')
def append_a_new_attribute_values_the_metadata_value_and_content(step, attrValue, metadataValue, content):
    """
    Store a notification in mysql with different scenarios per columns
    :param step:
    :param attrValue:
    :param metadataValue:
    :param content:
    """
    world.content = content
    world.metadataValue = metadataValue
    world.cygnus.notification_col (attrValue, metadataValue, content, ERROR[NOT])

#----------------------------------------------------------------------------------
@step (u'Validate that the attribute value and type are stored in mysql')
def validate_that_the_attribute_value_and_type_are_stored_in_mysql(step):
    """
    Validate that the attribute value and type are stored in mysql per row
    """
    resp = world.mysql.verifyDatasetSearch_valuesAndType (world.content)
    world.mysql.validateResponse (resp)


@step (u'Validate that the attribute metadatas are stored in mysql')
def validate_that_the_attribute_metadatas_are_stored_in_mysql(step):
    """
    Validate that the attribute metadata is stored in mysql per row
    """
    resp = world.mysql.verifyDatasetSearch_metadatas (world.content)
    world.mysql.validateResponse (resp)


@step (u'Validate that the database is not created in mysql')
def validate_that_the_database_does_not_created_in_mysql (step):
    """
    Validate that the database parameter are not created in mysql per row
    :param step:
    """
    world.mysql.verifyIfDatabaseExist ()

@step (u'Verify that the attribute value is stored in mysql')
def verify_that_the_attribute_value_is_stored_in_mysql(step):
    """
    Validate that the attribute value and type are stored in mysql per column
    """
    resp = world.mysql.verifyDatasetSearch_valuesAndType_per_column (world.content)
    world.mysql.validateResponse (resp)

@step (u'Verify the metadatas are stored in mysql')
def verify_the_metadatas_are_stored_in_mysql(step):
    """
    Validate that the attribute metadata is stored in mysql per column
    """
    resp = world.mysql.verifyDatasetSearch_metadatas_per_column (world.content)
    world.mysql.validateResponse (resp)


@step (u'Verify the notification is not stored in mysql')
def verify_the_notification_is_not_stored_in_mysql (step):
    """
    Verify the notification is not stored in mysql
    :param step:
    """
    world.mysql.verifyIfTableIsEmpty()


@step (u'Verify that the database does not exist in mysql')
def verify_that_the_database_does_not_exist_in_mysql (step):
    """
    Verify that the database does not exist in mysql in column mode
    :param step:
    """
    world.mysql.verifyDatabaseNotExist()


@step (u'Verify that the table does not exist in mysql')
def verify_that_the_table_does_not_exist_in_mysql (step):
    """
    Verify that the table does not exist in mysql in column mode
    :param step:
    """
    world.mysql.verifyTableNotExist()



#----------------------------------------------------------------------------------------






