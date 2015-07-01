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

Feature: stored in mongo new notifications from context broker but do not apply grouping rules with patterns
  As a cygnus user
  I want to be able stored in mongo new notifications from context broker but do not apply grouping rules with patterns
  so that they become more functional and useful


  @happy_path  @grouping_rules @ISSUE_447 @skip
  Scenario Outline: stored in mongo new notifications from context broker but do not apply grouping rules with patterns
    Given copy properties.json file from "epg_properties.json" to test "mongo-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And verify if mongo is installed correctly
    And service "test_happy_path", service path "<service_path>", resource "<resource>", with attribute number "1", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random number=2", metadata value "True" and content "<content>"
    Then receive an "OK" http code
    And validate that the attribute value and type are stored in mongo
  Examples:
    | service_path | resource        | content |
    # identity id
    | parks        | car1_cars       |  json    |
    | parks        | car2_cars       |  xml     |
    | parks        | car.34_cars     |  json    |
    | parks        | car.46_cars     |  xml     |
  # identity type
    | parks        | car1_car        |  json    |
    | parks        | car1_cars       |  xml     |
  # service_path
    | gardens      | left1_west      |  json    |
    | gardens      | left.2_west     |  xml     |
    | gardens      | right3_south    |  json    |
    | gardens      | right.4_north   |  xml     |
  # identityId and IdentityType
    | parks        | speed1_car      |  json    |
    | parks        | speed2_car      |  xml     |
    | parks        | speed.1_car     |  json    |
    | parks        | speed.2_car     |  xml     |
    | parks        | speed.1_car1    |  json    |
    | parks        | speed.2_car1    |  xml     |
    | parks        | speed.1_car.1   |  json    |
    | parks        | speed.2_car.1   |  xml     |
  # servicePath and identityId
    | cities       | flowers1_west   |  json    |
    | cities       | flowers2_west   |  xml     |
    | cities       | flowers.1_west  |  json    |
    | cities       | flowers.2_west  |  xml     |
  # servicePath and identityType
    | train        | town1_center1   |  xml     |
    | train        | town2_center2   |  xml     |
    | train        | town.1_center.1 |  json    |
    | train        | town.2_center.2 |  xml     |

  @not_found @grouping_rules
  Scenario: not start cygnus if grouping_rules file does not exists
    Given copy properties.json file from "epg_properties.json" to test "mongo-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "column" mode
    And reinitialize log file
    And copy flume-env.sh, grouping rules file from "", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    Then verify if cygnus is installed correctly
    And check in log, label "lvl=ERROR" and text "File not found. Details=/usr/cygnus/conf/grouping_rules.conf (No such file or directory)"

