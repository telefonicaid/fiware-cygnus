# -*- coding: utf-8 -*-
#
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
#     Author: Ivan Arias
#

Feature: Store in mysql new notifications per row from context broker
    As a cygnus user
    I want to be able to store in mysql new notifications per row from context broker
    so that they become more functional and useful

    @happy_path
    Scenario Outline:  store in mysql new notifications from context broker
       Given "mysql" is installed correctly
        When store in mysql with a organization "default", resource "default" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then Validate that the attribute value and type are stored in mysql
         And Validate that the attribute metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |content|
      |json|
      |xml|

    @organizations
    Scenario Outline:  store in mysql new notifications with different organizations behavior
       Given "mysql" is installed correctly
        When store in mysql with a organization "<organization>", resource "default" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then Validate that the attribute value and type are stored in mysql
         And Validate that the attribute metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |organization|content|
      |org60|json|
      |org60|xml|
      #|without organization|json|
      #|without organization|xml|
      |with 64 characters|json|
      |with 64 characters|xml|

    @resources
    Scenario Outline:  store in ckan new notifications with different resources behavior
       Given "mysql" is installed correctly
        When store in mysql with a organization "default", resource "<resource>" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then Validate that the attribute value and type are stored in mysql
         And Validate that the attribute metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |resource|content|
      |Room2-Room|json|
      |Room2-Room|xml|
      |Room2-HOUSE|json|
      |Room2-HOUSE|xml|
      |with 64 characters|json|
      |with 64 characters|xml|
      |Room2-|json|
      |Room2-|xml|

    @attrNumbers
    Scenario Outline:  store in mysql new notifications with different quantities of attributes
       Given "mysql" is installed correctly
        When store in mysql with a organization "default", resource "default" and the attribute number "<attrNumber>", the compound number "default", the metadata number "default" and content "<content>"
        Then Validate that the attribute value and type are stored in mysql
         And Validate that the attribute metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |attrNumber|content|
      |1|json|
      |1|xml|
      |3|json|
      |3|xml|
      |50|json|
      |50|xml|

    @metadatas
    Scenario Outline:  store in mysql new notifications with different quantities of metadata attributes
       Given "mysql" is installed correctly
        When store in mysql with a organization "default", resource "default" and the attribute number "default", the compound number "default", the metadata number "<metadatas>" and content "<content>"
        Then Validate that the attribute value and type are stored in mysql
         And Validate that the attribute metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |metadatas|content|
      |1|json|
      |1|xml|
      |3|json|
      |3|xml|
      |50|json|
      |50|xml|

    @compounds @skip @BUG-102
    Scenario Outline:  store in ckan new notifications with different quantities of values ​​of compounds attributes
       Given "ckan" is installed correctly
        When store in ckan with a organization "default", resource "default" and the attribute number "default", the compound number "<compound>", the metadata number "default" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in ckan
         And Validate that the attribute metadatas are stored in ckan
    Examples:
      |compound|content|
      |1|json|
      |1|xml|
      |3|json|
      |3|xml|
      |50|json|
      |50|xml|

    @errors
    Scenario Outline:  controlled error if the database parameters are wrong
       Given "mysql" is installed correctly
        When store in mysql with a organization "<organization>", resource "<resource>" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then Validate that the database is not created in mysql
    Examples:
      |organization|resource|content|
      |large than 64 characters|default|json|
      |large than 64 characters|default|xml|
      |default|large than 64 characters|json|
      |default|large than 64 characters|xml|