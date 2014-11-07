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

#
#  Note: the @skip tag is to skip the scenarios that still are not developed or failed
#        -tg=-skip
#

Feature: Stored in ckan new notifications per column from context broker
    As a cygnus user
    I want to be able to store in ckan new notifications per column from context broker
    so that they become more functional and useful

    @happy_path_1
    Scenario Outline: stored new notifications in ckan from context broker with or without metadata
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "<organization>"
         And create a new resource "default" with "<attributesQuantity>" attributes, attrValue data type "json" and metadata data type "json"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then I receive an "OK" http code
         And Verify that the attribute value is stored in ckan
         And Verify the metadatas are stored in ckan
    Examples:
      |organization  |attributesQuantity|attrValue        |metadataValue|content|
      |cygnus_col_016|1                 |45.0             |True         |json   |
      |cygnus_col_026|2                 |dfgdfgdg         |True         |xml    |
      |cygnus_col_036|3                 |{'a':'1','b':'2'}|False        |json   |
      |cygnus_col_046|4                 |-45.2344         |False        |xml    |

    @types
    Scenario Outline: stored new notifications in ckan with different data types
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "<organization>"
         And create a new resource "default" with "<attributesQuantity>" attributes, attrValue data type "<attrValueType>" and metadata data type "<metadataType>"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then I receive an "OK" http code
         And Verify that the attribute value is stored in ckan
         And Verify the metadatas are stored in ckan
    Examples:
      |organization   |attributesQuantity|attrValueType|attrValue        |metadataType|metadataValue|content|
      |org_col_json_1 |1                 |json         |45.41            |json        |True         |json   |
      |org_col_json_1 |1                 |json         |45.42            |json        |True         |xml    |
      |org_col_json_1 |1                 |json         |{'a':'1','b':'2'}|json        |True         |json   |
      |org_col_text_1 |1                 |text         |45.43            |json        |True         |json   |
      |org_col_text_1 |1                 |text         |45.44            |json        |True         |xml    |
      |org_col_float_1|1                 |float        |45.45            |json        |True         |json   |
      |org_col_float_1|1                 |float        |45.46            |json        |True         |xml    |
      |org_col_float_1|1                 |float        |-45.47           |json        |True         |json   |
      |org_col_float_1|1                 |float        |-45.48           |json        |True         |xml    |
      |org_col_int_2  |1                 |int          |45               |json        |True         |json   |
      |org_col_int_2  |1                 |int          |46               |json        |True         |xml    |
      |org_col_int_2  |1                 |int          |-47              |json        |True         |json   |
      |org_col_int_2  |1                 |int          |-48              |json        |True         |xml    |
      |org_col_bool_2 |1                 |bool         |True             |json        |True         |json   |
      |org_col_bool_2 |1                 |bool         |False            |json        |True         |xml    |
      |org_col_date_2 |1                 |date         |2014-12-25       |json        |True         |json   |
      |org_col_date_2 |1                 |date         |2014-11-25       |json        |True         |xml    |
      |org_col_time_2 |1                 |time         |12:42:00         |json        |True         |json   |
      |org_col_time_2 |1                 |time         |12:43:00         |json        |True         |xml    |

    @resources  @BUG_172 @skip
    Scenario Outline: stored new notifications in ckan with different resources and the same package
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "org_col_multi_resource"
         And create a new resource "<resource>" with "<attributesQuantity>" attributes, attrValue data type "<attrValueType>" and metadata data type "<metadataType>"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then I receive an "OK" http code
         And Verify that the attribute value is stored in ckan
         And Verify the metadatas are stored in ckan
    Examples:
      |resource                |attributesQuantity|attrValueType|attrValue |metadataType|metadataValue|content|
      |Room1-Room              |1                 |json         |41.41     |json        |True         |json   |
      |Room1-Room              |1                 |json         |42.41     |json        |True         |xml    |
      |Room2-Room              |1                 |json         |43.41     |json        |True         |json   |
      |Room2-Room              |1                 |json         |44.41     |json        |True         |xml    |
      |Room3-Room              |1                 |json         |45.41     |json        |True         |json   |
      |Room3-Room              |1                 |json         |46.41     |json        |True         |xml    |
      |modelogw.assetgw-device |1                 |json         |45.41     |json        |True         |json   |
      |modelogw.assetgw-device |1                 |json         |46.41     |json        |True         |xml    |

    @multiples_attributes
    Scenario Outline: stored new notifications in ckan with multiples attributes
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "<organization>"
         And create a new resource "default" with "<attributesQuantity>" attributes, attrValue data type "<attrValueType>" and metadata data type "json"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then I receive an "OK" http code
         And Verify that the attribute value is stored in ckan
         And Verify the metadatas are stored in ckan
    Examples:
      |organization    |attributesQuantity|attrValueType|attrValue        |content|
      |org_col_json_50 |50                |json         |45.41            |json   |
      |org_col_json_50 |50                |json         |45.41            |xml    |
      |org_col_text_50 |50                |text         |45.41            |json   |
      |org_col_text_50 |50                |text         |45.41            |xml    |
      |org_col_float_50|50                |float        |45.41            |json   |
      |org_col_float_50|50                |float        |45.41            |xml    |
      |org_col_int_50  |50                |int          |47               |json   |
      |org_col_int_50  |50                |int          |47               |xml    |
      |org_col_bool_50 |50                |bool         |True             |json   |
      |org_col_bool_50 |50                |bool         |True             |xml    |
      |org_col_date_50 |50                |date         |2014-11-25       |json   |
      |org_col_date_50 |50                |date         |2014-11-25       |xml    |
      |org_col_time_50 |50                |time         |12:41:00         |json   |
      |org_col_time_50 |50                |time         |12:41:00         |xml    |

    @error_data
    Scenario Outline: try to store new notifications in ckan with differents errors in data type
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "<organization>"
         And create a new resource "default" with "1" attributes, attrValue data type "<attrValueType>" and metadata data type "json"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then I receive an "OK" http code
         And Verify the notification is not stored in ckan
    Examples:
      |organization    |attrValueType|attrValue        |content|
      |org_col_float_10|float        |df_json          |json   |
      |org_col_float_10|float        |df_xml           |xml    |
      |org_col_float_10|float        |1,23             |json   |
      |org_col_float_10|float        |1,24             |xml    |
      |org_col_float_10|float        |{'a':'1','b':'2'}|json   |
      |org_col_float_10|float        |<a>1</a><b>2</b> |xml    |
      |org_col_int_10  |int          |df_json          |json   |
      |org_col_int_10  |int          |df_xml           |xml    |
      |org_col_int_10  |int          |{'a':'1','b':'2'}|json   |
      |org_col_int_10  |int          |<a>1</a><b>2</b> |xml    |
      |org_col_bool_10 |bool         |df_json          |json   |
      |org_col_bool_10 |bool         |df_xml           |xml    |
      |org_col_bool_10 |bool         |{'a':'1','b':'2'}|json   |
      |org_col_bool_10 |bool         |<a>1</a><b>2</b> |xml    |
      |org_col_bool_10 |bool         |45,56            |json   |
      |org_col_bool_10 |bool         |45,57            |xml    |
      |org_col_date_12 |date         |df_json          |json   |
      |org_col_date_12 |date         |df_xml           |xml    |
      |org_col_date_12 |date         |{'a':'1','b':'2'}|json   |
      |org_col_date_12 |date         |<a>1</a><b>2</b> |xml    |
      |org_col_date_12 |date         |12.45            |json   |
      |org_col_date_12 |date         |12.46            |xml    |
      |org_col_time_10 |time         |12h              |json   |
      |org_col_time_10 |time         |12h              |xml    |
      |org_col_time_10 |time         |{'a':'1','b':'2'}|json   |
      |org_col_time_10 |time         |<a>1</a><b>2</b> |xml    |

    @without_metadata_field
    Scenario Outline: try to store new notification in ckan without metadata field and with metadata value
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "without_meta_col"
         And create a new resource "default" with "1" attributes, attrValue data type "json" and metadata data type "without metadata field"
        When append a new attribute values "45", the metadata value "True" and content "<content>"
        Then I receive an "OK" http code
         And Verify the notification is not stored in ckan
    Examples:
      |content|
      |json   |
      |xml    |

    @element_not_exist @skip @BUG-181
    Scenario Outline: try to store new notification in ckan if some element does not exist
       Given cygnus is installed with type "column"
         And "ckan" is installed correctly
         And create a new organization "<organization>"
         And create a new resource "<resource>" with "1" attributes, attrValue data type "json" and metadata data type "json"
        When append a new attribute values "45.0", the metadata value "True" and content "<content>"
        Then I receive an "OK" http code
         And Verify that the organization does not exist in ckan
         And Verify that the dataset does not exist in ckan
         And Verify that the resource does not exist in ckan
    Examples:
      |organization                  |resource         |content|
      |organization_missing          |default          |json   |
      |organization_missing          |default          |xml    |
      |organization_without_dataset  |default          |json   |
      |organization_without_dataset  |default          |xml    |
      |default                       |resource-missing |json   |
      |default                       |resource-missing |xml    |



