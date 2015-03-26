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
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And create a new database "<tenant>"
    And create a new table "default" with service path "<service_path>", "<attributes_number>" attributes called "<attribute_name>", attribute type "<attribute_type>", attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | tenant         | service_path | attributes_number | attribute_name | attribute_type | attribute_value   | metadata_value | content |
    | cygnus_col_021 | myserv6      | 1                 | pressure       | celcius        | 49.3              | True           | json    |
    | cygnus_col_021 | myserv6      | 1                 | pressure       | celcius        | 46.3              | True           | xml     |
    | cygnus_col_031 | myserv6      | 2                 | my_attribute   | my_Type        | dfgdfgdg          | True           | json    |
    | cygnus_col_031 | myserv6      | 2                 | my_attribute   | my_Type        | dfgdfgdg          | True           | xml     |
    | cygnus_col_041 | myserv6      | 3                 | my_attribute   | my_Type        | {'a':'1','b':'2'} | False          | json    |
    | cygnus_col_041 | myserv6      | 3                 | my_attribute   | my_Type        | {'a':'1','b':'2'} | False          | xml     |
    | cygnus_col_051 | myserv6      | 4                 | my_attribute   | my_Type        | -45.2344          | False          | json    |
    | cygnus_col_051 | myserv6      | 4                 | my_attribute   | my_Type        | -45.2344          | False          | xml     |


  @organizations
  Scenario Outline: store in mysql new notifications with different organizations behavior
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "<tenant>"
    And create a new table "default" with service path "default", "2" attributes called "temperature", attribute type "temperature", attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | tenant                  | content |
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
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "multi_service"
    And create a new table "default" with service path "<service_path>", "2" attributes called "temperature", attribute type "nothing", attribute data type "text" and metadata data type "text"
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
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "multi_resource"
    And create a new table "<resource>" with service path "default", "2" attributes called "temperature", attribute type "nothing", attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | resource                | content |
    | Room2_Room              | json    |
    | Room2_Room              | xml     |
    | Room2_HOUSE             | json    |
    | Room2_HOUSE             | xml     |
    | Room2_                  | json    |
    | Room2_                  | xml     |
    | ROOM_house              | json    |
    | ROOM_house              | xml     |
    | modelogw.assetgw_device | json    |
    | modelogw.assetgw_device | xml     |
    | with max length allowed | json    |
    | with max length allowed | xml     |

  @attributes_number
  Scenario Outline:  store in mysql new notifications with different quantities of attributes
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "<tenant>"
    And create a new table "room1_room" with service path "default", "<attribute_number>" attributes called "temperature", attribute type "nothing", attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | tenant              | attribute_number | content |
    | attributes_multi_01 | 1                | json    |
    | attributes_multi_01 | 1                | xml     |
    | attributes_multi_03 | 3                | json    |
    | attributes_multi_03 | 3                | xml     |
    | attributes_multi_10 | 10               | json    |
    | attributes_multi_10 | 10               | xml     |

  @types
  Scenario Outline: stored new notifications in mysql with different data types
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "<tenant>"
    And create a new table "room1_room" with service path "default", "2" attributes called "temperature", attribute type "nothing", attribute data type "<attribute_data_type>" and metadata data type "text"
    When receives a notification with attributes value "<attribute_value>", metadata value "True" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | tenant              | attribute_data_type | attribute_value   | content |
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
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "<tenant>"
    And create a new table "room1_room" with service path "default", "2" attributes called "temperature", attribute type "nothing", attribute data type "<attribute_data_type>" and metadata data type "text"
    When receives a notification with attributes value "<attribute_value>", metadata value "True" and content "<content>"
    Then Verify that is not stored in mysql "Error in <attribute_data_type> with value <attribute_data_type>"
    And Close mysql connection
  Examples:
    | tenant            | attribute_data_type | attribute_value   | content |
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
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "without_metadata_field"
    And create a new table "room1_room" with service path "default", "2" attributes called "temperature", attribute type "nothing", attribute data type "<value_field>" and metadata data type "<metadata_field>"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that is not stored in mysql "<error>"
    And Close mysql connection
  Examples:
    | value_field | metadata_field | content | error
    | text        | without        | json    | without metadata field                |
    | text        | without        | xml     | without metadata field                |
    | without     | text           | json    | without attribute field               |
    | without     | text           | xml     | without attribute field               |
    | without     | without        | json    | without attribute and metadata fields |
    | without     | without        | xml     | without attribute and metadata fields |

  @element_not_exist @BUG-181
  Scenario Outline: try to store new notification in mysql if some element does not exist
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And  create a new database "<tenant>"
    And create a new table "<resource>" with service path "default", "2" attributes called "temperature", attribute type "nothing", attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Verify that is not stored in mysql "<error>"
    And Close mysql connection

  Examples:
    | tenant                       | resource         | content | error                        |
    | organization is missing      | default          | json    | organization is missing      |
    | organization is missing      | default          | xml     | organization is missing      |
    | organization_without_dataset | default          | json    | organization without dataset |
    | organization_without_dataset | default          | xml     | organization without dataset |
    | org_without_resource         | resource_missing | json    | resource missing             |
    | org_without_resource         | resource_missing | xml     | resource missing             |

  @matching_table
  Scenario Outline: stored new notifications in mysql with different matching_table patterns
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And create a new database "db_pattern"
    And create a new table "<new_destination>" with service path "<new_service_path>", "2" attributes called "temperature", attribute type "nothing", attribute data type "text" and metadata data type "text"
    And update real values in resource "<resource>" and service path "<service_path>" to notification request
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then changes new destination "<new_destination>" where to verify in table "<new_service_path>"
    And Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | service_path | resource        | new_destination | new_service_path | content |
    # identity id
    | parks        | car1_cars       | cars_modern     | vehicles         | json    |
    | parks        | car2_cars       | cars_modern     | vehicles         | xml     |
    | parks        | car.34_cars     | cars_modern     | vehicles         | json    |
    | parks        | car.46_cars     | cars_modern     | vehicles         | xml     |
  # identity type
    | parks        | car1_car        | cars_modern     | vehicles         | json    |
    | parks        | car1_cars       | cars_modern     | vehicles         | xml     |
  # service_path
    | gardens      | left1_west      | gardens_city    | city_indicators  | json    |
    | gardens      | left.2_west     | gardens_city    | city_indicators  | xml     |
    | gardens      | right3_south    | gardens_city    | city_indicators  | json    |
    | gardens      | right.4_north   | gardens_city    | city_indicators  | xml     |
  # identityId and IdentityType
    | parks        | speed1_car      | cars_modern     | vehicles         | json    |
    | parks        | speed2_car      | cars_modern     | vehicles         | xml     |
    | parks        | speed.1_car     | cars_modern     | vehicles         | json    |
    | parks        | speed.2_car     | cars_modern     | vehicles         | xml     |
    | parks        | speed.1_car1    | cars_modern     | vehicles         | json    |
    | parks        | speed.2_car1    | cars_modern     | vehicles         | xml     |
    | parks        | speed.1_car.1   | cars_modern     | vehicles         | json    |
    | parks        | speed.2_car.1   | cars_modern     | vehicles         | xml     |
  # servicePath and identityId
    | cities       | flowers1_west   | gardens_city    | city_indicators  | json    |
    | cities       | flowers2_west   | gardens_city    | city_indicators  | xml     |
    | cities       | flowers.1_west  | gardens_city    | city_indicators  | json    |
    | cities       | flowers.2_west  | gardens_city    | city_indicators  | xml     |
  # servicePath and identityType
    | train        | town1_center1   | cars_modern     | vehicles         | xml     |
    | train        | town2_center2   | cars_modern     | vehicles         | xml     |
    | train        | town.1_center.1 | cars_modern     | vehicles         | json    |
    | train        | town.2_center.2 | cars_modern     | vehicles         | xml     |


  @matching_table_errors @BUG-271
  Scenario Outline: not stored new notifications in mysql with errors in matching_table patterns
    Given copy properties.json file from "filab_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And create a new database "db_pattern"
    And create a new table "<resource>" with service path "<service_path>", "2" attributes called "temperature", attribute type "nothing", attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
   #  error lines in matching_table.conf file
   #  14|entityId|destmissing(\d*)||errordataset
   #  15|entityId|datasetmissing(\d*)|dest_error|
    | service_path | resource              | content |
    | servpath_33  | destmissing1_error    | json    |
    | servpath_33  | destmissing1_error    | xml     |
    | servpath_33  | datasetmissing1_error | json    |
    | servpath_33  | datasetmissing1_error | xml     |

