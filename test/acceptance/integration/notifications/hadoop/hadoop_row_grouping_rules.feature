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
    And "hadoop" is installed correctly
    And a tenant "tenant", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And changes new destination "<new_destination>" where to verify in dataset "<new_dataset>"
    And Validate that the attribute value and type are stored in hadoop
    And Validate that the attribute metadatas are stored in hadoop
    And delete the file created in hadoop
  Examples:
    | service_path | resource        | new_destination | new_dataset     | content |
    # identity id
    | parks        | car1_cars       | cars_modern     | vehicles        | json    |
    | parks        | car2_cars       | cars_modern     | vehicles        | xml     |
    | parks        | car.34_cars     | cars_modern     | vehicles        | json    |
    | parks        | car.46_cars     | cars_modern     | vehicles        | xml     |
  # identity type
    | parks        | car1_car        | cars_modern     | vehicles        | json    |
    | parks        | car1_cars       | cars_modern     | vehicles        | xml     |
  # service_path
    | gardens      | left1_west      | gardens_city    | city_indicators | json    |
    | gardens      | left.2_west     | gardens_city    | city_indicators | xml     |
    | gardens      | right3_south    | gardens_city    | city_indicators | json    |
    | gardens      | right.4_north   | gardens_city    | city_indicators | xml     |
  # identityId and IdentityType
    | parks        | speed1_car      | cars_modern     | vehicles        | json    |
    | parks        | speed2_car      | cars_modern     | vehicles        | xml     |
    | parks        | speed.1_car     | cars_modern     | vehicles        | json    |
    | parks        | speed.2_car     | cars_modern     | vehicles        | xml     |
    | parks        | speed.1_car1    | cars_modern     | vehicles        | json    |
    | parks        | speed.2_car1    | cars_modern     | vehicles        | xml     |
    | parks        | speed.1_car.1   | cars_modern     | vehicles        | json    |
    | parks        | speed.2_car.1   | cars_modern     | vehicles        | xml     |
  # servicePath and identityId
    | cities       | flowers1_west   | gardens_city    | city_indicators | json    |
    | cities       | flowers2_west   | gardens_city    | city_indicators | xml     |
    | cities       | flowers.1_west  | gardens_city    | city_indicators | json    |
    | cities       | flowers.2_west  | gardens_city    | city_indicators | xml     |
  # servicePath and identityType
    | train        | town1_center1   | cars_modern     | vehicles        | xml     |
    | train        | town2_center2   | cars_modern     | vehicles        | xml     |
    | train        | town.1_center.1 | cars_modern     | vehicles        | json    |
    | train        | town.2_center.2 | cars_modern     | vehicles        | xml     |

  @errors @grouping_rules @BUG-271 @skip @460
  Scenario Outline: not stored new notifications in hadoop with errors in grouping_rules patterns
    Given copy properties.json file from "filab_properties.json" to test "hdfs-sink" and sudo local "false"
    And reinitialize log file
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And "hadoop" is installed correctly
    And a tenant "tenant", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "attribute" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value and type are stored in hadoop
    And check in log, label "WARN" and text "Malformed matching rule, it will be discarded."
  Examples:
  # in case of Malformed matching rule, it will be discarded
  #  error rules in grouping_rules.conf file (id: 15 and id:16)
    | service_path | resource              | content |
    | servpath_33  | destmissing1_error    | json    |
    | servpath_33  | destmissing1_error    | xml     |
    | servpath_33  | datasetmissing1_error | json    |
    | servpath_33  | datasetmissing1_error | xml     |

  @not_found @grouping_rules
  Scenario: not start cygnus if grouping_rules file does not exists
    Given copy properties.json file from "filab_properties.json" to test "hdfs-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And reinitialize log file
    And copy flume-env.sh, grouping rules file from "", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    Then verify if cygnus is installed correctly
    And check in log, label "lvl=ERROR" and text "File not found. Details=/usr/cygnus/conf/grouping_rules.conf (No such file or directory)"
