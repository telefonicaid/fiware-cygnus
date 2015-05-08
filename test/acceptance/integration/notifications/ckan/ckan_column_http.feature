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
# iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

#
#  Note: the "skip" tag is to skip the scenarios that still are not developed or failed
#        -tg=-skip
#

Feature: Stored in ckan new notifications per column from context broker
  As a cygnus user
  I want to be able to store in ckan new notifications per column from context broker
  so that they become more functional and useful

  @happy_path
  Scenario Outline: stored new notifications in ckan from context broker with or without metadata
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<organization>" with a dataset "<service_path>"
    And create a new resource "default" with "<attributesQuantity>" attributes called "<attribute_name>", attribute type "<attribute_type>", attribute data type "json" and metadata data type "json"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then I receive an "OK" http code
    And Verify that the attribute value is stored in ckan
  Examples:
    | organization   | service_path | attributesQuantity | attribute_name | attribute_type | attribute_value   | metadata_value | content |
    | cygnus_col_023 | myserv6      | 1                  | pressure       | celcius        | 46.3              | True           | json    |
    | cygnus_col_023 | myserv6      | 1                  | pressure       | celcius        | 46.3              | True           | xml     |
    | cygnus_col_031 | myserv6      | 2                  | my_attribute   | my_Type        | dfgdfgdg          | True           | json    |
    | cygnus_col_031 | myserv6      | 2                  | my_attribute   | my_Type        | dfgdfgdg          | True           | xml     |
    | cygnus_col_041 | myserv6      | 3                  | my_attribute   | my_Type        | {'a':'1','b':'2'} | False          | json    |
    | cygnus_col_041 | myserv6      | 3                  | my_attribute   | my_Type        | {'a':'1','b':'2'} | False          | xml     |
    | cygnus_col_051 | myserv6      | 4                  | my_attribute   | my_Type        | -45.2344          | False          | json    |
    | cygnus_col_051 | myserv6      | 4                  | my_attribute   | my_Type        | -45.2344          | False          | xml     |
    | cygnus_col_061 | /myserv6     | 5                  | pressure       | celcius        | 549.3             | True           | json    |
    | cygnus_col_061 | /myserv6     | 5                  | pressure       | celcius        | 549.3             | True           | xml     |
    | cygnus_col_072 | /            | 5                  | pressure       | celcius        | 549.3             | True           | json    |
    | cygnus_col_072 | /            | 5                  | pressure       | celcius        | 549.3             | True           | xml     |

  @organizations
  Scenario Outline: store in ckan new notifications with different organizations behavior
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<organization>" with a dataset "/myservicepath"
    And create a new resource "default" with "1" attributes called "temperature", attribute type "celcius", attribute data type "json" and metadata data type "json"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that the attribute value is stored in ckan
    And Verify the metadatas are stored in ckan
  Examples:
    | organization            | content |
    | org60100008             | json    |
    | org60100008             | xml     |
    | orga60120008            | json    |
    | orga60120008            | xml     |
    | orga_6140008            | json    |
    | orga_6140008            | xml     |
    | with max length allowed | json    |
    | with max length allowed | xml     |

  @service_path
  Scenario Outline: store in ckan new notifications with different service_path behavior
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<organization>" with a dataset "<service_path>"
    And create a new resource "room2_room" with "1" attributes called "temperature", attribute type "celcius", attribute data type "json" and metadata data type "json"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that the attribute value is stored in ckan
    And Verify the metadatas are stored in ckan
  Examples:
    | organization          | service_path | content |
    | serv_path_several_101 |              | json    |
    | serv_path_several_111 |              | xml     |
    | serv_path_several_201 | serv6010     | json    |
    | serv_path_several_211 | serv6011     | xml     |
    | serv_path_several_301 | SERV6012     | json    |
    | serv_path_several_311 | SERV6013     | xml     |
    | serv_path_several_401 | Serv_614     | json    |
    | serv_path_several_411 | Serv_615     | xml     |
    | serv_path_several_501 | 1234567890   | json    |
    | serv_path_several_511 | 1234567890   | xml     |
    | serv_path_several_601 | /1234567890  | json    |
    | serv_path_several_611 | /1234567890  | xml     |
    | serv_path_several_701 | /            | json    |
    | serv_path_several_711 | /            | xml     |

  @resources
  Scenario Outline: store in ckan new notifications with different service_path behavior
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "resource_multi_021" with a dataset "default"
    And create a new resource "<resource>" with "1" attributes called "temperature", attribute type "celcius", attribute data type "json" and metadata data type "json"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that the attribute value is stored in ckan
    And Verify the metadatas are stored in ckan
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
  Scenario Outline:  store in ckan new notifications with different quantities of attributes
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<organization>" with a dataset "default"
    And create a new resource "room1_room" with "<attribute_number>" attributes called "temperature", attribute type "celcius", attribute data type "json" and metadata data type "json"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that the attribute value is stored in ckan
    And Verify the metadatas are stored in ckan
  Examples:
    | organization          | attribute_number | content |
    | attributes_multi_0011 | 1                | json    |
    | attributes_multi_0011 | 1                | xml     |
    | attributes_multi_0031 | 3                | json    |
    | attributes_multi_0031 | 3                | xml     |
    | attributes_multi_0101 | 10               | json    |
    | attributes_multi_0101 | 10               | xml     |

  @types
  Scenario Outline: stored new notifications in ckan with different data types
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<organization>" with a dataset "default"
    And create a new resource "room1_room" with "1" attributes called "temperature", attribute type "celcius", attribute data type "<attribute_data_type>" and metadata data type "json"
    When receives a notification with attributes value "<attribute_value>", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that the attribute value is stored in ckan
    And Verify the metadatas are stored in ckan
  Examples:
    | organization       | attribute_data_type | attribute_value   | content |
    | org_col_json_0111  | json                | 45.41             | json    |
    | org_col_json_0111  | json                | 45.42             | xml     |
    | org_col_json_0111  | json                | {'a':'1','b':'2'} | json    |
    | org_col_text_0111  | text                | 45.43             | json    |
    | org_col_text_0111  | text                | 45.44             | xml     |
    | org_col_float_0111 | float               | 45.45             | json    |
    | org_col_float_0111 | float               | 45.46             | xml     |
    | org_col_float_0111 | float               | -45.47            | json    |
    | org_col_float_0111 | float               | -45.48            | xml     |
    | org_col_int_0211   | int                 | 45                | json    |
    | org_col_int_0211   | int                 | 46                | xml     |
    | org_col_int_0211   | int                 | -47               | json    |
    | org_col_int_0211   | int                 | -48               | xml     |
    | org_col_bool_0211  | bool                | True              | json    |
    | org_col_bool_0211  | bool                | False             | xml     |
    | org_col_date_0211  | date                | 2014-12-25        | json    |
    | org_col_date_0211  | date                | 2014-11-25        | xml     |
    | org_col_time_0211  | time                | 12:42:00          | json    |
    | org_col_time_0211  | time                | 12:43:00          | xml     |

  @error_field
  Scenario Outline: try to store new notification in ckan without value or metadata fields
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "without_fields_031" with a dataset "default"
    And create a new resource "room1_room" with "1" attributes called "temperature", attribute type "celcius", attribute data type "<value_field>" and metadata data type "<metadata_field>"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that is not stored in ckan "<error>"
  Examples:
    | value_field | metadata_field | content | error                                 |
    | text        | without        | json    | without metadata field                |
    | text        | without        | xml     | without metadata field                |
    | without     | text           | json    | without attribute field               |
    | without     | text           | xml     | without attribute field               |
    | without     | without        | json    | without attribute and metadata fields |
    | without     | without        | xml     | without attribute and metadata fields |

  @error_data
  Scenario Outline: try to store new notifications in ckan with differents errors in data type
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<organization>" with a dataset "default"
    And create a new resource "room2_room" with "1" attributes called "temperature", attribute type "celcius", attribute data type "<value_field>" and metadata data type "json"
    When receives a notification with attributes value "<attribute_value>", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that is not stored in ckan "error with value in wrong data type"
  Examples:
    | organization       | value_field | attribute_value   | content |
    | org_col_float_1001 | float       | df_json           | json    |
    | org_col_float_1001 | float       | df_xml            | xml     |
    | org_col_float_1001 | float       | 1,23              | json    |
    | org_col_float_1001 | float       | 1,24              | xml     |
    | org_col_float_1001 | float       | {'a':'1','b':'2'} | json    |
    | org_col_float_1001 | float       | <a>1</a><b>2</b>  | xml     |
    | org_col_int_1001   | int         | df_json           | json    |
    | org_col_int_1001   | int         | df_xml            | xml     |
    | org_col_int_1001   | int         | {'a':'1','b':'2'} | json    |
    | org_col_int_1001   | int         | <a>1</a><b>2</b>  | xml     |
    | org_col_bool_1001  | bool        | df_json           | json    |
    | org_col_bool_1001  | bool        | df_xml            | xml     |
    | org_col_bool_1001  | bool        | {'a':'1','b':'2'} | json    |
    | org_col_bool_1001  | bool        | <a>1</a><b>2</b>  | xml     |
    | org_col_bool_1001  | bool        | 45,56             | json    |
    | org_col_bool_1001  | bool        | 45,57             | xml     |
    | org_col_date_1201  | date        | df_json           | json    |
    | org_col_date_1201  | date        | df_xml            | xml     |
    | org_col_date_1201  | date        | {'a':'1','b':'2'} | json    |
    | org_col_date_1201  | date        | <a>1</a><b>2</b>  | xml     |
    | org_col_date_1201  | date        | 12.45             | json    |
    | org_col_date_1201  | date        | 12.46             | xml     |
    | org_col_time_1001  | time        | 12h               | json    |
    | org_col_time_1001  | time        | 12h               | xml     |
    | org_col_time_1001  | time        | {'a':'1','b':'2'} | json    |
    | org_col_time_1001  | time        | <a>1</a><b>2</b>  | xml     |

  @element_not_exist @BUG-181
  Scenario Outline: try to store new notification in ckan if some element does not exist
    Given copy properties.json file from "epg_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, matching table file from "default", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And create a new organization "<tenant>" with a dataset "default"
    And create a new resource "<resource>" with "2" attributes called "temperature", attribute type "celcius", attribute data type "json" and metadata data type "json"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Verify that is not stored if element does not exist "<error>" in ckan
  Examples:
    | tenant                       | resource         | content | error                        |
    | organization is missing      | default          | json    | organization is missing      |
    | organization is missing      | default          | xml     | organization is missing      |
    | organization_without_dataset | default          | json    | organization without dataset |
    | organization_without_dataset | default          | xml     | organization without dataset |
    | org_without_resource         | resource_missing | json    | resource missing             |
    | org_without_resource         | resource_missing | xml     | resource missing             |