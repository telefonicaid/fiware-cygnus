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

Feature: Store in mysql new notifications per row from context broker
  As a cygnus user
  I want to be able to store in mysql new notifications per row from context broker
  so that they become more functional and useful

  @happy_path
  Scenario Outline: stored new notifications in mysql from context broker with or without metadata
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "tenant_3", service path "/test", resource "room2_room", with attribute number "4", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
  Examples:
    | content |
    | json    |
    | xml     |

  @happy_path
  Scenario Outline: stored new notifications in mysql from context broker with or without metadata
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "tenant_4", service path "/test", resource "room2_room", with attribute number "<attributes_number>", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
  Examples:
    | attributes_number | attribute_value   | metadata_value | content |
    | 1                 | 45.1              | True           | json    |
    | 2                 | dfgdfgdg          | True           | xml     |
    | 3                 | -45.2344          | False          | xml     |
    | 4                 | {'a':'1','b':'2'} | False          | json    |

  @organizations
  Scenario Outline:  store in mysql new notifications with different organization values
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "<tenant>", service path "/test", resource "room2_room", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
  Examples:
    | tenant                  | content |
    | orga60100_row           | json    |
    | orga60100_row           | xml     |
    | ORGA60111_row           | json    |
    | ORGA60111_row           | xml     |
    | Org_61401_row           | json    |
    | Org_61401_row           | xml     |
    | with max length allowed | json    |
    | with max length allowed | xml     |

  @service_path
  Scenario Outline:  store in mysql new notifications with different service path values
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "<tenant>", service path "<service_path>", resource "room2_room", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
  Examples:
    | tenant                | service_path | content |
    | serv_path_several_100 |              | json    |
    | serv_path_several_110 |              | xml     |
    | serv_path_several_200 | serv6010     | json    |
    | serv_path_several_210 | serv6011     | xml     |
    | serv_path_several_300 | SERV6012     | json    |
    | serv_path_several_310 | SERV6013     | xml     |
    | serv_path_several_400 | Serv_614     | json    |
    | serv_path_several_410 | Serv_615     | xml     |
    | serv_path_several_500 | 1234567890   | json    |
    | serv_path_several_510 | 1234567890   | xml     |
    | serv_path_several_600 | /1234567890  | json    |
    | serv_path_several_610 | /1234567890  | xml     |
    | serv_path_several_700 | /            | json    |
    | serv_path_several_710 | /            | xml     |

  @resources
  Scenario Outline: store in ckan new notifications with different resources values
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "tenant", service path "servicepath", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
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
  Scenario Outline:  store in ckan new notifications with different quantities of attributes
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "tenant_5", service path "/test", resource "room2_room", with attribute number "<attribute_number>", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
  Examples:
    | attribute_number | content |
    | 1                | json    |
    | 1                | xml     |
    | 3                | json    |
    | 3                | xml     |
    | 10               | json    |
    | 10               | xml     |

  @types
  Scenario Outline: stored new notifications in mysql with different data types
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "<tenant>", service path "servicepath", resource "room1_room", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
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