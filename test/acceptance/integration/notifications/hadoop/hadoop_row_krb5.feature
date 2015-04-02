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


Feature: Stored in hadoop new notifications per row from context broker
  As a cygnus user
  I want to be able to store in hadoop new notifications per row from context broker
  so that they become more functional and useful

  @happy_path
  Scenario Outline:  store in hadoop new notifications from context broker
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "tenant", service path "service", resource "room21_room", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop
  Examples:
    | content |
    | json    |
    | xml     |

  @happy_path
  Scenario Outline: stored new notifications in hadoop from context broker with differents values
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "default", service path "default", resource "room2_room", with attribute number "<attributes_number>", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop

  Examples:
    | attributes_number | attribute_value   | metadata_value | content |
    | 1                 | 45.1              | True           | json    |
    | 2                 | dfgdfgdg          | True           | xml     |
    | 3                 | -45.2344          | False          | xml     |
    | 4                 | {'a':'1','b':'2'} | False          | json    |

  @organizations
  Scenario Outline:  store in hadoop new notifications with different organizations values
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "<tenant>", service path "service", resource "room21_room", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop
  Examples:
    | tenant                  | content |
    | org6010000              | json    |
    | org6010000              | xml     |
    | ORGA6012000             | json    |
    | ORGA6012000             | xml     |
    | Orga_614000             | json    |
    | Orga_614000             | xml     |
    | with max length allowed | json    |
    | with max length allowed | xml     |

  @service_path
  Scenario Outline:  store in hadoop new notifications with different service path values
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "<tenant>", service path "<service_path>", resource "room21_room", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop
  Examples:
    | tenant               | service_path | content |
    | serv_path_several_10 |              | json    |
    | serv_path_several_11 |              | xml     |
    | serv_path_several_20 | serv6010     | json    |
    | serv_path_several_21 | serv6011     | xml     |
    | serv_path_several_30 | SERV6012     | json    |
    | serv_path_several_31 | SERV6013     | xml     |
    | serv_path_several_40 | Serv_614     | json    |
    | serv_path_several_41 | Serv_615     | xml     |
    | serv_path_several_50 | 1234567890   | json    |
    | serv_path_several_51 | 1234567890   | xml     |
    | serv_path_several_60 | /1234567890  | json    |
    | serv_path_several_61 | /1234567890  | xml     |
    | serv_path_several_70 | /            | json    |
    | serv_path_several_71 | /            | xml     |

  @resources
  Scenario Outline: store in hadoop new notifications with different resources values
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "tenant", service path "servicepath", resource "<resource>", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop

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
  Scenario Outline:  store in hadoop new notifications with different quantities of attributes
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "<tenant>", service path "servicepath", resource "room1_room", with attribute number "<attribute_number>", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop

  Examples:
    | tenant               | attribute_number | content |
    | attributes_multi_001 | 1                | json    |
    | attributes_multi_001 | 1                | xml     |
    | attributes_multi_003 | 3                | json    |
    | attributes_multi_003 | 3                | xml     |
    | attributes_multi_010 | 10               | json    |
    | attributes_multi_010 | 10               | xml     |

  @values
  Scenario Outline: stored new notifications in hadoop with different values
    Given copy properties.json file from "epg_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "<tenant>", service path "servicepath", resource "room1_room", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop

  Examples:
    | tenant        | attribute_value   | content |
    | org_json_011  | 45.41             | json    |
    | org_json_011  | 45.42             | xml     |
    | org_json_011  | {'a':'1','b':'2'} | json    |
    | org_text_011  | 45.43             | json    |
    | org_text_011  | 45.44             | xml     |
    | org_text_011  | erwer             | json    |
    | org_text_011  | fgdfg             | xml     |
    | org_float_011 | 45.45             | json    |
    | org_float_011 | 45.46             | xml     |
    | org_float_011 | -45.47            | json    |
    | org_float_011 | -45.48            | xml     |
    | org_int_021   | 45                | json    |
    | org_int_021   | 46                | xml     |
    | org_int_021   | -47               | json    |
    | org_int_021   | -48               | xml     |
    | org_bool_021  | True              | json    |
    | org_bool_021  | False             | xml     |
    | org_date_021  | 2014-12-25        | json    |
    | org_date_021  | 2014-11-25        | xml     |
    | org_time_021  | 12:42:00          | json    |
    | org_time_021  | 12:43:00          | xml     |