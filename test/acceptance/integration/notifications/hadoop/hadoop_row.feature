# -*- coding: utf-8 -*-
#
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
#     Author: Ivan Arias
#

Feature: Stored in hadoop new notifications per row from context broker
    As a cygnus user
    I want to be able to store in hadoop new notifications per row from context broker
    so that they become more functional and useful

    @happy_path
    Scenario Outline:  store in hadoop new notifications from context broker
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When store in hadoop with a organization "default", resource "default" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |content|
      |json   |
      |xml    |

    @organizations
    Scenario Outline:  store in hadoop new notifications with different organizations behavior
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When store in hadoop with a organization "<organization>", resource "default" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |organization        |content|
      |org601              |json   |
      |org601              |xml    |
      |ORGA601             |json   |
      |ORGA601             |xml    |
      |Orga_61             |json   |
      |Orga_61             |xml    |
      #|without organization|json   |
      #|without organization|xml    |
      |with 32 characters  |json   |
      |with 32 characters  |xml    |
      |org601.test         |json   |
      |org601.test         |xml    |
      
     @resources
    Scenario Outline:  store in hadoop new notifications with different resources behavior
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When store in hadoop with a organization "default", resource "<resource>" and the attribute number "default", the compound number "default", the metadata number "default" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |resource               |content|
      |Room2-Room             |json   |
      |Room2-Room             |xml    |
      |Room2-HOUSE            |json   |
      |Room2-HOUSE            |xml    |
      |with 64 characters     |json   |
      |with 64 characters     |xml    |
      |Room2-                 |json   |
      |Room2-                 |xml    |
      |modelogw.assetgw-device|json   |
      |modelogw.assetgw-device|xml    |
      |ROOM-house             |json   |
      |ROOM-house             |xml    |
       
    @attrNumbers
    Scenario Outline:  store in hadoop new notifications with different quantities of attributes
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When store in hadoop with a organization "attributes_multiples_3", resource "default" and the attribute number "<attrNumber>", the compound number "default", the metadata number "default" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |attrNumber|content|
      |1         |json   |
      |1         |xml    |
      |3         |json   |
      |3         |xml    |
      |50        |json   |
      |50        |xml    |
    
    @metadatas
    Scenario Outline:  store in hadoop new notifications with different quantities of metadata attributes
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When store in hadoop with a organization "metadata_1", resource "default" and the attribute number "default", the compound number "default", the metadata number "<metadatas>" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |metadatas|content|
      |1        |json   |
      |1        |xml    |
      |3        |json   |
      |3        |xml    |
      |50       |json   |
      |50       |xml    |

    @compounds
    Scenario Outline:  store in hadoop new notifications with different quantities of values ​​of compounds attributes
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When store in hadoop with a organization "compound_1", resource "default" and the attribute number "default", the compound number "<compound>", the metadata number "default" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |compound|content|
      |1       |json   |
      |1       |xml    |
      |3       |json   |
      |3       |xml    |
      |50      |json   |
      |50      |xml    |

    @happy_path
    Scenario Outline: stored new notifications in hadoop from context broker with differents values
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When append in hadoop with a organization "default", resource "default", with "<attributesQuantity>" new attributes values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |attributesQuantity|attrValue        |metadataValue|content|
      |1                 |45.1             |True         |json   |
      |2                 |dfgdfgdg         |True         |xml    |
      |3                 |-45.2344         |False        |xml    |
      |4                 |{'a':'1','b':'2'}|False        |json   |

    @values
    Scenario Outline: stored new notifications in hadoop with different values
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When append in hadoop with a organization "default", resource "default", with "1" new attributes values "<attrValue>", the metadata value "True" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |attrValue        |content|
      |45.41            |json   |
      |45.41            |xml    |
      |{'a':'1','b':'2'}|json   |
      |<a>1</a><b>2</b> |xml    |
      |-45.47           |json   |
      |-45.48           |xml    |   
      |2014-12-25       |json   |
      |2014-11-25       |xml    |
      |12:42:00         |json   |
      |12:43:00         |xml    |
      
    @resources
    Scenario Outline: stored new notifications in hadoop with different file name and the same directory
        Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When append in hadoop with a organization "default", resource "<resource>", with "1" new attributes values "<attrValue>", the metadata value "True" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop

    Examples:
      |resource   |attrValue |content|
      |Room1-Room |41.41     |json   |
      |Room1-Room |42.41     |xml    |
      |Room2-Room |43.41     |json   |
      |Room2-Room |44.41     |xml    |
      |Room3-Room |45.41     |json   |
      |Room3-Room |46.41     |xml    |
      |Room4-Room |47.41     |json   |
      |Room4-Room |48.41     |xml    |

    @multiples_attributes
    Scenario Outline: stored new notifications in hadoop with multiples attributes
       Given cygnus is installed with type "row"
         And "hadoop" is installed correctly
        When append in hadoop with a organization "default", resource "default", with "<attributesQuantity>" new attributes values "<attrValue>", the metadata value "True" and content "<content>"
        Then I receive an "OK" http code
         And Validate that the attribute value and type are stored in hadoop
         And Validate that the attribute metadatas are stored in hadoop
         And delete the file created in hadoop
    Examples:
      |attributesQuantity|attrValue        |content|
      |1                 |40.41            |json   |
      |1                 |41.41            |xml    |
      |5                 |42.41            |json   |
      |5                 |43.41            |xml    |
      |10                |44.41            |json   |
      |10                |55.41            |xml    |
      |50                |46.41            |json   |
      |50                |47.41            |xml    |

