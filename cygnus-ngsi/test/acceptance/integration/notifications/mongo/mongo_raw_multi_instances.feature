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

Feature: start multi-instances of cygnus using mongo sink
  As a cygnus user
  I want to be able to start multi-instances of cygnus using mongo sink
  so that they become more functional and useful

  @happy_path @multi_instances
  Scenario Outline: start multi-instances of cygnus using mongo sink, ports differents and store multiples notifications one by instance and the port defined incremented
    Given copy properties.json file from "epg_properties.json" to test "mongo-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "<instances_number>", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And verify if mongo is installed correctly
    And service "test_happy_path", service path "<service_path>", entity type "room", entity id "room2", with attribute number "1", attribute name "random" and attribute type "celcius"
    When receives multiples notifications one by instance and the port defined incremented with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then validate that the attribute value and type are stored in mongo
    And delete instances files
  Examples:
    | instances_number | attribute_value | metadata_value | content |
    | 1                | 40.0            | True           | json    |
    | 1                | 41.1            | False          | json    |
    | 1                | 42.2            | True           | xml     |
    | 1                | 43.3            | False          | xml     |
    | 5                | 44.4            | True           | json    |
    | 5                | 45.5            | False          | json    |
    | 5                | 46.6            | True           | xml     |
    | 5                | 47.7            | False          | xml     |
    | 10               | 48.8            | True           | json    |
    | 10               | 49.9            | False          | json    |
    | 10               | 50.0            | True           | xml     |
    | 10               | 51.1            | False          | xml     |

  @same_port @multi_instances
  Scenario Outline: try to start multi-instances of cygnus using mongo sink, row mode and same ports to all instances
    Given copy properties.json file from "epg_properties.json" to test "mongo-sink" and sudo local "false"
    And reinitialize log file
    And configuration of cygnus instances with different ports "false", agents files quantity "<instances_number>", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "false"
    And verify if cygnus is installed correctly
    And verify if mongo is installed correctly
    And service "test_same_port", service path "/test", entity type "room", entity id "room2", with attribute number "1", attribute name "random" and attribute type "celcius"
    When receives multiples notifications one by instance and the port defined incremented with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then check in log, label "lvl=FATAL" and text "Fatal error running the Management Interface. Details=Address already in use"
    And delete instances files
  Examples:
    | instances_number | attribute_value | metadata_value | content |
    | 2                | 40.0            | True           | json    |
    | 2                | 41.1            | False          | json    |
    | 2                | 42.2            | True           | xml     |
    | 2                | 43.3            | False          | xml     |
    | 5                | 44.4            | True           | json    |
    | 5                | 45.5            | False          | json    |
    | 5                | 46.6            | True           | xml     |
    | 5                | 47.7            | False          | xml     |
