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

Feature: stored in mongo new notifications from context broker but do not apply grouping rules with patterns
  As a cygnus user
  I want to be able stored in mongo new notifications from context broker but do not apply grouping rules with patterns
  so that they become more functional and useful


  @happy_path  @grouping_rules @ISSUE_447 @skip
  Scenario Outline: stored in mongo new notifications from context broker but do not apply grouping rules with patterns
    Given copy properties.json file from "epg_properties.json" to test "sth-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And verify if mongo is installed correctly
    And service "test_happy_path", service path "<service_path>", entity type "<entity_type>", entity id "<entity_id>", with attribute number "1", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random number=2", metadata value "True" and content "<content>"
    Then receive an "OK" http code
    And validate that the aggregated value is generate by resolution "<resolution>" in mongo
  Examples:
    | service_path | entity_type | entity_id | content | resolution |
    # identity id
    | parks        | cars        | car1      | json    | month      |
    | parks        | cars        | car2      | xml     | day        |
    | parks        | cars        | car.34    | json    | hour       |
    | parks        | cars        | car.46    | xml     | minute     |
  # identity type
    | parks        | car         | car1      | json    | second     |
    | parks        | cars        | car1      | xml     | month      |
  # service_path
    | gardens      | west        | left1     | json    | day        |
    | gardens      | west        | left.2    | xml     | hour       |
    | gardens      | south       | right3    | json    | minute     |
    | gardens      | north       | right.4   | xml     | second     |
  # identityId and IdentityType
    | parks        | car         | speed1    | json    | month      |
    | parks        | car         | speed2    | xml     | day        |
    | parks        | car         | speed.1   | json    | hour       |
    | parks        | car         | speed.2   | xml     | minute     |
    | parks        | car1        | speed.1   | json    | second     |
    | parks        | car1        | speed.2   | xml     | month      |
    | parks        | car.1       | speed.1   | json    | day        |
    | parks        | car.1       | speed.2   | xml     | hour       |
  # servicePath and identityId
    | cities       | west        | flowers1  | json    | minute     |
    | cities       | west        | flowers2  | xml     | second     |
    | cities       | west        | flowers.1 | json    | month      |
    | cities       | west        | flowers.2 | xml     | day        |
  # servicePath and identityType
    | train        | center1     | town1     | xml     | hour       |
    | train        | center2     | town2     | xml     | minute     |
    | train        | center.1    | town.1    | json    | second     |
    | train        | center.2    | town.2    | xml     | month      |

  @not_found @grouping_rules
  Scenario: not start cygnus if grouping_rules file does not exists
    Given copy properties.json file from "epg_properties.json" to test "sth-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And reinitialize log file
    And copy flume-env.sh, grouping rules file from "", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    Then verify if cygnus is installed correctly
    And check in log, label "lvl=ERROR" and text "File not found. Details=/usr/cygnus/conf/grouping_rules.conf (No such file or directory)"

