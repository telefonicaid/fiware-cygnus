# -*- coding: utf-8 -*-
#
# Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FIWARE project).
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
# iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

#
#  Note: the "skip" tag is to skip the scenarios that still are not developed or failed
#        -tg=-skip
#

Feature: Stored in mysql new notifications per column from context broker
  As a cygnus user
  I want to be able to store in mysql new notifications per column from context broker
  so that they become more functional and useful

  @happy_path
  Scenario Outline: stored new notifications in mysql from context broker with or without metadata
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "<database>", service path "<service_path>", entity type "room", entity id "room2", with attribute number "<attributes_number>", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | database       | service_path | attributes_number | attribute_value   | metadata_value | content |
    | cygnus_col_021 | /myserv6     | 1                 | 49.3              | True           | json    |
    | cygnus_col_021 | /myserv6     | 1                 | 46.3              | True           | xml     |
    | cygnus_col_031 | /myserv6     | 2                 | dfgdfgdg          | True           | json    |
    | cygnus_col_031 | /myserv6     | 2                 | dfgdfgdg          | True           | xml     |
    | cygnus_col_041 | /myserv6     | 3                 | {'a':'1','b':'2'} | False          | json    |
    | cygnus_col_041 | /myserv6     | 3                 | {'a':'1','b':'2'} | False          | xml     |
    | cygnus_col_051 | /myserv6     | 4                 | -45.2344          | False          | json    |
    | cygnus_col_051 | /myserv6     | 4                 | -45.2344          | False          | xml     |

  @organizations
  Scenario Outline: store in mysql new notifications with different organizations behavior
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "<database>", service path "/test", entity type "room", entity id "room2", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | database                | content |
    | org601000               | json    |
    | org601100               | xml     |
    | ORGA601200              | json    |
    | ORGA601300              | xml     |
    | Orga_61400              | json    |
    | Orga_61500              | xml     |
    | with max length allowed | json    |
    | with max length allowed | xml     |

  @service_path
  Scenario Outline: store in mysql new notifications with different organizations behavior
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "multi_service_path", service path "<service_path>", entity type "room", entity id "room2", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | service_path | content |
    |              | json    |
    |              | xml     |
    | serv6010     | json    |
    | serv6011     | xml     |
    | SERV6012     | json    |
    | SERV6013     | xml     |
    | Serv_614     | json    |
    | Serv_615     | xml     |
    | 1234567890   | json    |
    | 1234567890   | xml     |
    | /1234567890  | json    |
    | /1234567890  | xml     |
    | /            | json    |
    | /            | xml     |

  @resources
  Scenario Outline: store in mysql new notifications with different service_path behavior
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "multi_resource", service path "/test", entity type "<entity_type>", entity id "<entity_id>", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | entity_type             | entity_id               | content |
    | Room                    | Room2                   | json    |
    | Room                    | Room2                   | xml     |
    | HOUSE                   | Room2                   | json    |
    | HOUSE                   | Room2                   | xml     |
    |                         | Room2                   | json    |
    |                         | Room2                   | xml     |
    | house                   | ROOM                    | json    |
    | house                   | ROOM                    | xml     |
    | device                  | modelogw.assetgw        | json    |
    | device                  | modelogw.assetgw        | xml     |
    | room                    | with max length allowed | json    |
    | room                    | with max length allowed | xml     |
    | with max length allowed | room2                   | json    |
    | with max length allowed | room2                   | xml     |

  @attributes_number
  Scenario Outline:  store in mysql new notifications with different quantities of attributes
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "<database>", service path "/test", entity type "room", entity id "room2", with attribute number "<attribute_number>", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | database            | attribute_number | content |
    | attributes_multi_01 | 1                | json    |
    | attributes_multi_01 | 1                | xml     |
    | attributes_multi_03 | 3                | json    |
    | attributes_multi_03 | 3                | xml     |
    | attributes_multi_10 | 10               | json    |
    | attributes_multi_10 | 10               | xml     |

  @types
  Scenario Outline: stored new notifications in mysql with different data types
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "<database>", service path "/test", entity type "room", entity id "room2", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "<attribute_data_type>" and metadata data type "text"
    When receives a notification with attributes value "<attribute_value>", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | database            | attribute_data_type | attribute_value   | content |
    | org_col_varchar_10  | varchar(10)         | 45.41             | json    |
    | org_col_varchar_10  | varchar(10)         | 45.41             | xml     |
    | org_col_varchar_100 | varchar(100)        | {'a':'1','b':'2'} | json    |
    | org_col_text_1      | text                | 45.43             | json    |
    | org_col_text_1      | text                | 45.44             | xml     |
    | org_col_float_1     | float               | 45.45             | json    |
    | org_col_float_1     | float               | 45.46             | xml     |
    | org_col_float_03    | float(3)            | -45.47            | json    |
    | org_col_float_03    | float(3)            | -45.48            | xml     |
    | org_col_double_1    | double              | 45.45             | json    |
    | org_col_double_1    | double              | 45.46             | xml     |
    | org_col_double_1    | double              | -45.47            | json    |
    | org_col_double_1    | double              | -45.48            | xml     |
    | org_col_real_1      | real                | 45.45             | json    |
    | org_col_real_1      | real                | 45.46             | xml     |
    | org_col_real_1      | real                | -45.47            | json    |
    | org_col_real_1      | real                | -45.48            | xml     |
    | org_col_int_1       | int                 | 45                | json    |
    | org_col_int_1       | int                 | 46                | xml     |
    | org_col_int_1       | int                 | -47               | json    |
    | org_col_int_1       | int                 | -48               | xml     |
    | org_col_bool_1      | boolean             | 0                 | json    |
    | org_col_bool_1      | boolean             | 1                 | xml     |
    | org_col_date_1      | date                | 2014-12-25        | json    |
    | org_col_date_1      | date                | 2014-11-25        | xml     |
    | org_col_time_1      | time                | 12:42:00          | json    |
    | org_col_time_1      | time                | 12:43:00          | xml     |


  @error_data  @BUG-184
  Scenario Outline: try to store new notifications in mysql with differents errors in data type
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "<database>", service path "/test", entity type "room", entity id "room2", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "<attribute_data_type>" and metadata data type "text"
    When receives a notification with attributes value "<attribute_value>", metadata value "True" and content "<content>"
    Then Verify that is not stored in mysql "Error in <attribute_data_type> with value <attribute_data_type>"
    And Close mysql connection
  Examples:
    | database          | attribute_data_type | attribute_value   | content |
    | org_col_float_10  | float               | df_json           | json    |
    | org_col_float_10  | float               | df_xml            | xml     |
    | org_col_float_10  | float               | 1,23              | json    |
    | org_col_float_10  | float               | 1,24              | xml     |
    | org_col_float_10  | float               | {'a':'1','b':'2'} | json    |
    | org_col_float_10  | float               | <a>1</a><b>2</b>  | xml     |
    | org_col_double_10 | double              | df_json           | json    |
    | org_col_double_10 | double              | df_xml            | xml     |
    | org_col_double_10 | double              | 1,23              | json    |
    | org_col_double_10 | double              | 1,24              | xml     |
    | org_col_double_10 | double              | {'a':'1','b':'2'} | json    |
    | org_col_double_10 | double              | <a>1</a><b>2</b>  | xml     |
    | org_col_real_10   | real                | df_json           | json    |
    | org_col_real_10   | real                | df_xml            | xml     |
    | org_col_real_10   | real                | 1,23              | json    |
    | org_col_real_10   | real                | 1,24              | xml     |
    | org_col_real_10   | real                | {'a':'1','b':'2'} | json    |
    | org_col_real_10   | real                | <a>1</a><b>2</b>  | xml     |
    | org_col_int_10    | int                 | df_json           | json    |
    | org_col_int_10    | int                 | df_xml            | xml     |
    | org_col_int_10    | int                 | {'a':'1','b':'2'} | json    |
    | org_col_int_10    | int                 | <a>1</a><b>2</b>  | xml     |
    | org_col_bool_10   | boolean             | df_json           | json    |
    | org_col_bool_10   | boolean             | df_xml            | xml     |
    | org_col_bool_10   | boolean             | {'a':'1','b':'2'} | json    |
    | org_col_bool_10   | boolean             | <a>1</a><b>2</b>  | xml     |
    | org_col_bool_10   | boolean             | 45,56             | json    |
    | org_col_bool_10   | boolean             | 45,57             | xml     |
    | org_col_date_10   | date                | df_json           | json    |
    | org_col_date_10   | date                | df_xml            | xml     |
    | org_col_date_10   | date                | {'a':'1','b':'2'} | json    |
    | org_col_date_10   | date                | <a>1</a><b>2</b>  | xml     |
    | org_col_date_10   | date                | 12.45             | json    |
    | org_col_date_10   | date                | 12.46             | xml     |
    | org_col_time_10   | time                | 12h               | json    |
    | org_col_time_10   | time                | 12h               | xml     |
    | org_col_time_10   | time                | {'a':'1','b':'2'} | json    |
    | org_col_time_10   | time                | <a>1</a><b>2</b>  | xml     |

  @error_field
  Scenario Outline: try to store new notification in mysql without value or metadata fields
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "without_metadata_field", service path "/test", entity type "room", entity id "room2", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "<attribute_data_type>" and metadata data type "<attribute_metadata_type>"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that is not stored in mysql "<error>"
    And Close mysql connection
  Examples:
    | attribute_data_type | attribute_metadata_type | content | error
    | text                | without                 | json    | without metadata field                |
    | text                | without                 | xml     | without metadata field                |
    | without             | text                    | json    | without attribute field               |
    | without             | text                    | xml     | without attribute field               |
    | without             | without                 | json    | without attribute and metadata fields |
    | without             | without                 | xml     | without attribute and metadata fields |

  @element_not_exist @BUG-181
  Scenario Outline: try to store new notification in mysql if some element does not exist
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "<database>", service path "/test", entity type "room", entity id "room2", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that is not stored in mysql "<error>"
    And Close mysql connection

  Examples:
    | database               |  content | error                  |
    | database_missing       |  json    | database_missing       |
    | database_missing       |  xml     | database_missing       |
    | database_without_table |  json    | database_without_table |
    | database_without_table |  xml     | database_without_table |


