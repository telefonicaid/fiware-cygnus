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

Feature: Stored in mysql new notifications per column from context broker
    As a cygnus user
    I want to be able to store in mysql new notifications per column from context broker
    so that they become more functional and useful

    @happy_path
    Scenario Outline: stored new notifications in mysql from context broker with or without metadata
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "<organization>"
         And create a new table "<resource>" with "<attributesQuantity>" attributes, attrValue data type "text" and metadata data type "text"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then Verify that the attribute value is stored in mysql
         And Verify the metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |organization  |resource   |attributesQuantity|attrValue         |metadataValue|content|
      |cygnus_col_01 |Room2-Room |1                 |45.0              |True         |json   |
      |cygnus_col_02 |Room2-Room |2                 |dfgdfgdg          |True         |xml    |
      |cygnus_col_03 |Room2-Room |3                 |{'a':'1','b':'2'} |False        |json   |
      |cygnus_col_04 |Room2-Room |4                 |-45.2344          |False        |xml    |

    @types
    Scenario Outline: stored new notifications in mysql with different data types
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "<organization>"
         And create a new table "default" with "1" attributes, attrValue data type "<attrValueType>" and metadata data type "text"
        When append a new attribute values "<attrValue>", the metadata value "True" and content "<content>"
        Then Verify that the attribute value is stored in mysql
         And Verify the metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |organization        |attrValueType|attrValue        |content|
      |org_col_varchar_10  |varchar(10)  |45.41            |json   |
      |org_col_varchar_10  |varchar(10)  |45.41            |xml    |
      |org_col_varchar_100 |varchar(100) |{'a':'1','b':'2'}|json   |
      |org_col_varchar_100 |varchar(100) |<a>1</a><b>2</b> |xml    |
      |org_col_text_1      |text         |45.43            |json   |
      |org_col_text_1      |text         |45.44            |xml    |
      |org_col_float_1     |float        |45.45            |json   |
      |org_col_float_1     |float        |45.46            |xml    |
      |org_col_float_03    |float(3)     |-45.47           |json   |
      |org_col_float_03    |float(3)     |-45.48           |xml    |
      |org_col_double_1    |double       |45.45            |json   |
      |org_col_double_1    |double       |45.46            |xml    |
      |org_col_double_1    |double       |-45.47           |json   |
      |org_col_double_1    |double       |-45.48           |xml    |
      |org_col_real_1      |real         |45.45            |json   |
      |org_col_real_1      |real         |45.46            |xml    |
      |org_col_real_1      |real         |-45.47           |json   |
      |org_col_real_1      |real         |-45.48           |xml    |
      |org_col_int_1       |int          |45               |json   |
      |org_col_int_1       |int          |46               |xml    |
      |org_col_int_1       |int          |-47              |json   |
      |org_col_int_1       |int          |-48              |xml    |
      |org_col_bool_1      |boolean      |0                |json   |
      |org_col_bool_1      |boolean      |1                |xml    |
      |org_col_date_1      |date         |2014-12-25       |json   |
      |org_col_date_1      |date         |2014-11-25       |xml    |
      |org_col_time_1      |time         |12:42:00         |json   |
      |org_col_time_1      |time         |12:43:00         |xml    |

    @resources
    Scenario Outline: stored new notifications in mysql with different resources and the same package
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "org_col_multi_resource"
         And create a new table "<resource>" with "1" attributes, attrValue data type "text" and metadata data type "text"
        When append a new attribute values "<attrValue>", the metadata value "True" and content "<content>"
        Then Verify that the attribute value is stored in mysql
         And Verify the metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |resource     |attrValue |content|
      |Room1-Room   |41.41     |json   |
      |Room1-Room   |42.41     |xml    |
      |Room2-Room   |43.41     |json   |
      |Room2-Room   |44.41     |xml    |
      |Room3-Room   |45.41     |json   |
      |Room3-Room   |46.41     |xml    |

    @multiples_attributes
    Scenario Outline: stored new notifications in mysql with multiples attributes
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "<organization>"
         And create a new table "default" with "<attributesQuantity>" attributes, attrValue data type "<attrValueType>" and metadata data type "text"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then Verify that the attribute value is stored in mysql
         And Verify the metadatas are stored in mysql
         And Close mysql connection
    Examples:
      |organization     |attributesQuantity|attrValueType|attrValue        |content|
      |org_col_json_50  |50                |varchar(10)  |45.41            |json   |
      |org_col_json_50  |50                |varchar(10)  |45.42            |xml    |
      |org_col_text_50  |50                |text         |45.43            |json   |
      |org_col_text_50  |50                |text         |45.44            |xml    |
      |org_col_float_50 |50                |float        |45.45            |json   |
      |org_col_float_50 |50                |float        |45.46            |xml    |
      |org_col_double_50|50                |double       |45.47            |json   |
      |org_col_double_50|50                |double       |45.48            |xml    |
      |org_col_real_50  |50                |real         |45.49            |json   |
      |org_col_real_50  |50                |real         |45.51            |xml    |
      |org_col_int_50   |50                |int          |47               |json   |
      |org_col_int_50   |50                |int          |48               |xml    |
      |org_col_bool_50  |50                |boolean      |0                |json   |
      |org_col_bool_50  |50                |boolean      |1                |xml    |
      |org_col_date_50  |50                |date         |2014-11-25       |json   |
      |org_col_date_50  |50                |date         |2014-12-25       |xml    |
      |org_col_time_50  |50                |time         |12:41:00         |json   |
      |org_col_time_50  |50                |time         |12:42:00         |xml    |

    @error_data @skip @BUG-184
    Scenario Outline: try to store new notifications in mysql with differents errors in data type
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "<organization>"
         And create a new table "default" with "1" attributes, attrValue data type "<attrValueType>" and metadata data type "text"
        When append a new attribute values "<attrValue>", the metadata value "<metadataValue>" and content "<content>"
        Then Verify the notification is not stored in mysql
         And Close mysql connection
    Examples:
      |organization     |attrValueType|attrValue        |content|
      |org_col_float_10 |float        |df_json          |json   |
      |org_col_float_10 |float        |df_xml           |xml    |
      |org_col_float_10 |float        |1,23             |json   |
      |org_col_float_10 |float        |1,24             |xml    |
      |org_col_float_10 |float        |{'a':'1','b':'2'}|json   |
      |org_col_float_10 |float        |<a>1</a><b>2</b> |xml    |
      |org_col_double_10|double       |df_json          |json   |
      |org_col_double_10|double       |df_xml           |xml    |
      |org_col_double_10|double       |1,23             |json   |
      |org_col_double_10|double       |1,24             |xml    |
      |org_col_double_10|double       |{'a':'1','b':'2'}|json   |
      |org_col_double_10|double       |<a>1</a><b>2</b> |xml    |
      |org_col_real_10  |real         |df_json          |json   |
      |org_col_real_10  |real         |df_xml           |xml    |
      |org_col_real_10  |real         |1,23             |json   |
      |org_col_real_10  |real         |1,24             |xml    |
      |org_col_real_10  |real         |{'a':'1','b':'2'}|json   |
      |org_col_real_10  |real         |<a>1</a><b>2</b> |xml    |
      |org_col_int_10   |int          |df_json          |json   |
      |org_col_int_10   |int          |df_xml           |xml    |
      |org_col_int_10   |int          |{'a':'1','b':'2'}|json   |
      |org_col_int_10   |int          |<a>1</a><b>2</b> |xml    |
      |org_col_bool_10  |boolean      |df_json          |json   |
      |org_col_bool_10  |boolean      |df_xml           |xml    |
      |org_col_bool_10  |boolean      |{'a':'1','b':'2'}|json   |
      |org_col_bool_10  |boolean      |<a>1</a><b>2</b> |xml    |
      |org_col_bool_10  |boolean      |45,56            |json   |
      |org_col_bool_10  |boolean      |45,57            |xml    |
      |org_col_date_10  |date         |df_json          |json   |
      |org_col_date_10  |date         |df_xml           |xml    |
      |org_col_date_10  |date         |{'a':'1','b':'2'}|json   |
      |org_col_date_10  |date         |<a>1</a><b>2</b> |xml    |
      |org_col_date_10  |date         |12.45            |json   |
      |org_col_date_10  |date         |12.46            |xml    |
      |org_col_time_10  |time         |12h              |json   |
      |org_col_time_10  |time         |12h              |xml    |
      |org_col_time_10  |time         |{'a':'1','b':'2'}|json   |
      |org_col_time_10  |time         |<a>1</a><b>2</b> |xml    |

   @without_metadata_field
    Scenario Outline: try to store new notification in mysql without metadata field and with metadata value
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "without_meta_col"
         And create a new table "default" with "1" attributes, attrValue data type "text" and metadata data type "without metadata field"
        When append a new attribute values "45", the metadata value "True" and content "<content>"
        Then Verify the notification is not stored in mysql
         And Close mysql connection
    Examples:
      |content|
      |json   |
      |xml    |

    @element_not_exist
    Scenario Outline: try to store new notification in ckan if some element does not exist
       Given cygnus is installed with type "column"
         And "mysql" is installed correctly
         And create a new database "<organization>"
         And create a new table "<resource>" with "1" attributes, attrValue data type "text" and metadata data type "text"
        When append a new attribute values "45.0", the metadata value "True" and content "<content>"
        Then Verify that the database does not exist in mysql
         And Verify that the table does not exist in mysql
         And Close mysql connection
    Examples:
      |organization                  |resource         |content|
      |organization_missing          |default          |json   |
      |organization_missing          |default          |xml    |
      |default                       |resource-missing |json   |
      |default                       |resource-missing |xml    |
