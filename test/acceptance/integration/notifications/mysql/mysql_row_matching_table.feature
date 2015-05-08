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

Feature: Store in mysql new notifications per row from context broker using matching table with patterns
  As a cygnus user
  I want to be able to store in mysql new notifications per row from context broker using matching table with patterns
  so that they become more functional and useful

  @happy_path  @matching_table
  Scenario Outline: stored new notifications in ckan with different matching_table patterns
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "tenant", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then changes new destination "<new_destination>" where to verify in table "<new_service_path>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And Close mysql connection
    And delete matching table file
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

  @matching_table @BUG-271
  Scenario Outline: not stored new notifications in mysql with errors in matching_table patterns
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And reinitialize log file
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, matching table file from "matching_table.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And a tenant "tenant", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then Validate that the attribute value, metadata "true" and type are stored in mysql
    And check in log, label "WARN" and text "Malformed matching rule, it will be discarded."
    And Close mysql connection
    And delete matching table file
  Examples:
  # in case of Malformed matching rule, it will be discarded
  #  error lines in matching_table.conf file
  #  14|entityId|destmissing(\d*)||errordataset
  #  15|entityId|datasetmissing(\d*)|dest_error|
    | service_path     | resource              | content |
    | servpath_row_010 | destmissing1_error    | json    |
    | servpath_row_010 | destmissing1_error    | xml     |
    | servpath_row_020 | datasetmissing1_error | json    |
    | servpath_row_020 | datasetmissing1_error | xml     |

  @matching_table
  Scenario: not start cygnus if matching_table file does not exists
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And reinitialize log file
    And copy flume-env.sh, matching table file from "", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    Then verify if cygnus is installed correctly
    And check in log, label "lvl=ERROR" and text "Runtime error (File not found. Details=/usr/cygnus/conf/matching_table.conf (No such file or directory))"
