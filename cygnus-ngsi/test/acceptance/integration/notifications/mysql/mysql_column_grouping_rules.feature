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

Feature: Stored in mysql new notifications per column from context broker using grouping rules with patterns
  As a cygnus user
  I want to be able to store in mysql new notifications per column from context broker using grouping rules with patterns
  so that they become more functional and useful

  @happy_path  @grouping_rules
  Scenario Outline: stored new notifications in mysql with different grouping_rules patterns
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "db_pattern", service path "<new_service_path>", entity type "<new_dest_entity_type>", entity id "<new_dest_entity_id>", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    And update real values in resource "<resource>" and service path "<service_path>" to notification request
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then changes new destination "<new_dest_entity_id>_<new_dest_entity_type>" where to verify "<new_service_path>"
    And Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
  Examples:
    | service_path | resource        | new_dest_entity_type | new_dest_entity_id | new_service_path | content |
    # identity id
    | parks        | car1_cars       | modern               | cars               | vehicles         | json    |
    | parks        | car2_cars       | modern               | cars               | vehicles         | xml     |
    | parks        | car.34_cars     | modern               | cars               | vehicles         | json    |
    | parks        | car.46_cars     | modern               | cars               | vehicles         | xml     |
  # identity type
    | parks        | car1_car        | modern               | cars               | vehicles         | json    |
    | parks        | car1_cars       | modern               | cars               | vehicles         | xml     |
  # service_path
    | gardens      | left1_west      | city                 | gardens            | city_indicators  | json    |
    | gardens      | left.2_west     | city                 | gardens            | city_indicators  | xml     |
    | gardens      | right3_south    | city                 | gardens            | city_indicators  | json    |
    | gardens      | right.4_north   | city                 | gardens            | city_indicators  | xml     |
  # identityId and IdentityType
    | parks        | speed1_car      | modern               | cars               | vehicles         | json    |
    | parks        | speed2_car      | modern               | cars               | vehicles         | xml     |
    | parks        | speed.1_car     | modern               | cars               | vehicles         | json    |
    | parks        | speed.2_car     | modern               | cars               | vehicles         | xml     |
    | parks        | speed.1_car1    | modern               | cars               | vehicles         | json    |
    | parks        | speed.2_car1    | modern               | cars               | vehicles         | xml     |
    | parks        | speed.1_car.1   | modern               | cars               | vehicles         | json    |
    | parks        | speed.2_car.1   | modern               | cars               | vehicles         | xml     |
  # servicePath and identityId
    | cities       | flowers1_west   | city                 | gardens            | city_indicators  | json    |
    | cities       | flowers2_west   | city                 | gardens            | city_indicators  | xml     |
    | cities       | flowers.1_west  | city                 | gardens            | city_indicators  | json    |
    | cities       | flowers.2_west  | city                 | gardens            | city_indicators  | xml     |
  # servicePath and identityType
    | train        | town1_center1   | modern               | cars               | vehicles         | xml     |
    | train        | town2_center2   | modern               | cars               | vehicles         | xml     |
    | train        | town.1_center.1 | modern               | cars               | vehicles         | json    |
    | train        | town.2_center.2 | modern               | cars               | vehicles         | xml     |


  @errors @grouping_rules @BUG-271 @BUG_460
  Scenario Outline: not stored new notifications in mysql with errors in grouping_rules patterns
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if mysql is installed correctly
    And service "db_pattern", service path "<service_path>", entity type "<entity_type>", entity id "<entity_id>", with attribute number "2", attribute name "pressure" and attribute type "celcius"
    And create a new database and a table with attribute data type "text" and metadata data type "text"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And check in log, label "lvl=WARN" and text "Invalid grouping rule, some field is empty. It will be discarded."
    And Close mysql connection
  Examples:
  # in case of Malformed matching rule, it will be discarded
  #  error rules in grouping_rules.conf file (id: 15 and id:16)
    | service_path     | entity_type | entity_id       | content |
    | servpath_col_100 | error       | destmissing1    | json    |
    | servpath_col_100 | error       | destmissing1    | xml     |
    | servpath_col_200 | error       | datasetmissing1 | json    |
    | servpath_col_200 | error       | datasetmissing1 | xml     |


  @not_found @grouping_rules
  Scenario: not start cygnus if grouping_rules file does not exists
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And reinitialize log file
    And copy flume-env.sh, grouping rules file from "", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    Then verify if cygnus is installed correctly
    And check in log, label "lvl=ERROR" and text "File not found. Details=/usr/cygnus/conf/grouping_rules.conf (No such file or directory)"

