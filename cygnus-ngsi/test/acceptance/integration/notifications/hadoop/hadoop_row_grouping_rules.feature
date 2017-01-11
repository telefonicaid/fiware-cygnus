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


Feature: Stored in hadoop new notifications per row from context broker using grouping rules with patterns
  As a cygnus user
  I want to be able to store in hadoop new notifications per row from context broker using grouping rules with patterns
  so that they become more functional and useful

  @happy_path  @grouping_rules
  Scenario Outline: stored new notifications in hadoop with different grouping_rules patterns
    Given copy properties.json file from "filab_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And verify if hadoop is installed correctly
    And service "happy_path_grouping_rules", service path "<service_path>", entity type "<entity_type>", entity id "<entity_id>", with attribute number "2", attribute name "temperature" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And changes new destination "<new_destination>" where to verify "<new_service_path>"
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop
  Examples:
    | service_path | entity_type | entity_id | new_destination | new_service_path | content |
    # identity id
    | parks        | cars        | car1      | cars_modern     | vehicles         | json    |
    | parks        | cars        | car2      | cars_modern     | vehicles         | xml     |
    | parks        | cars        | car.34    | cars_modern     | vehicles         | json    |
    | parks        | cars        | car.46    | cars_modern     | vehicles         | xml     |
  # identity type
    | parks        | car         | car1      | cars_modern     | vehicles         | json    |
    | parks        | cars        | car1      | cars_modern     | vehicles         | xml     |
  # service_path
    | gardens      | west        | left1     | gardens_city    | city_indicators  | json    |
    | gardens      | west        | left.2    | gardens_city    | city_indicators  | xml     |
    | gardens      | south       | right3    | gardens_city    | city_indicators  | json    |
    | gardens      | north       | right.4   | gardens_city    | city_indicators  | xml     |
  # identityId and IdentityType
    | parks        | car         | speed1    | cars_modern     | vehicles         | json    |
    | parks        | car         | speed2    | cars_modern     | vehicles         | xml     |
    | parks        | car         | speed.1   | cars_modern     | vehicles         | json    |
    | parks        | car         | speed.2   | cars_modern     | vehicles         | xml     |
    | parks        | car1        | speed.1   | cars_modern     | vehicles         | json    |
    | parks        | car1        | speed.2   | cars_modern     | vehicles         | xml     |
    | parks        | car.1       | speed.1   | cars_modern     | vehicles         | json    |
    | parks        | car.1       | speed.2   | cars_modern     | vehicles         | xml     |
  # servicePath and identityId
    | cities       | west        | flowers1  | gardens_city    | city_indicators  | json    |
    | cities       | west        | flowers2  | gardens_city    | city_indicators  | xml     |
    | cities       | west        | flowers.1 | gardens_city    | city_indicators  | json    |
    | cities       | west        | flowers.2 | gardens_city    | city_indicators  | xml     |
  # servicePath and identityType
    | train        | center1     | town1     | cars_modern     | vehicles         | xml     |
    | train        | center2     | town2     | cars_modern     | vehicles         | xml     |
    | train        | center.1    | town.1    | cars_modern     | vehicles         | json    |
    | train        | center.2    | town.2    | cars_modern     | vehicles         | xml     |

  @errors @grouping_rules @BUG-271 @460
  Scenario Outline: not stored new notifications in hadoop with errors in grouping_rules patterns
    Given copy properties.json file from "filab_properties.json" to test "hdfs-sink" and sudo local "false"
    And reinitialize log file
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And verify if hadoop is installed correctly
    And service "errors_grouping_rules", service path "<service_path>", entity type "<entity_type>", entity id "<entity_id>", with attribute number "2", attribute name "temperature" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And check in log, label "lvl=WARN" and text "Invalid grouping rule, some field is empty. It will be discarded."
  Examples:
  # in case of Malformed matching rule, it will be discarded
  #  error rules in grouping_rules.conf file (id: 15 and id:16)
    | service_path     | entity_type | entity_id       | content |
    | servpath_row_010 | error       | destmissing1    | json    |
    | servpath_row_010 | error       | destmissing1    | xml     |
    | servpath_row_020 | error       | datasetmissing1 | json    |
    | servpath_row_020 | error       | datasetmissing1 | xml     |

  @not_found @grouping_rules
  Scenario: not start cygnus if grouping_rules file does not exists
    Given copy properties.json file from "filab_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And reinitialize log file
    And copy flume-env.sh, grouping rules file from "", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    Then verify if cygnus is installed correctly
    And check in log, label "lvl=ERROR" and text "File not found. Details=/usr/cygnus/conf/grouping_rules.conf (No such file or directory)"
