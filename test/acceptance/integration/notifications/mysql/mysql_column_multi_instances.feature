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

Feature: start multi-instances of cygnus using mysql sink and column mode
  As a cygnus user
  I want to be able to start multi-instances of cygnus using mysql sink and column mode
  so that they become more functional and useful

  @happy_path @multi_instances
  Scenario Outline: start multi-instances of cygnus using mysql sink, column mode, ports differents and store multiples notifications one by instance and the port defined incremented
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "<instances_number>", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "false"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And create a new database "<tenant>"
    And create a new table "default" with service path "<service_path>", "<attributes_number>" attributes called "<attribute_name>", attribute type "<attribute_type>", attribute data type "text" and metadata data type "text"
    When receives multiples notifications one by instance and the port defined incremented with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
    And delete instances files
  Examples:
    | instances_number | tenant                   | service_path | attributes_number | attribute_name | attribute_type | attribute_value | metadata_value | content |
    | 1                | cygnus_multi_instance_01 | /myserv1     | 1                 | pressure       | celcius        | 40.0            | True           | json    |
    | 1                | cygnus_multi_instance_01 | /myserv1     | 1                 | pressure       | celcius        | 41.1            | False          | json    |
    | 1                | cygnus_multi_instance_01 | /myserv1     | 1                 | pressure       | celcius        | 42.2            | True           | xml     |
    | 1                | cygnus_multi_instance_01 | /myserv1     | 1                 | pressure       | celcius        | 43.3            | False          | xml     |
    | 5                | cygnus_multi_instance_02 | /myserv1     | 1                 | pressure       | celcius        | 44.4            | True           | json    |
    | 5                | cygnus_multi_instance_02 | /myserv1     | 1                 | pressure       | celcius        | 45.5            | False          | json    |
    | 5                | cygnus_multi_instance_02 | /myserv1     | 1                 | pressure       | celcius        | 46.6            | True           | xml     |
    | 5                | cygnus_multi_instance_02 | /myserv1     | 1                 | pressure       | celcius        | 47.7            | False          | xml     |
    | 10               | cygnus_multi_instance_03 | /myserv1     | 1                 | pressure       | celcius        | 48.8            | True           | json    |
    | 10               | cygnus_multi_instance_03 | /myserv1     | 1                 | pressure       | celcius        | 49.9            | False          | json    |
    | 10               | cygnus_multi_instance_03 | /myserv1     | 1                 | pressure       | celcius        | 50.0            | True           | xml     |
    | 10               | cygnus_multi_instance_03 | /myserv1     | 1                 | pressure       | celcius        | 51.1            | False          | xml     |


  @same_port @multi_instances @ISSUE_46
  Scenario Outline: try to start multi-instances of cygnus using mysql sink, column mode and same ports to all instances
    Given copy properties.json file from "epg_properties.json" to test "mysql-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "false", agents files quantity "<instances_number>", id "test" and in "column" mode
    And copy another configuration files and restart cygnus service and this execution is only once "false"
    And verify if cygnus is installed correctly
    And "mysql" is installed correctly
    And create a new database "<tenant>"
    And create a new table "default" with service path "<service_path>", "<attributes_number>" attributes called "<attribute_name>", attribute type "<attribute_type>", attribute data type "text" and metadata data type "text"
    When receives multiples notifications one by instance and the port defined incremented with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then Verify that the attribute value is stored in mysql
    And Verify the metadatas are stored in mysql
    And Close mysql connection
    And delete instances files
  Examples:
    | instances_number | tenant                   | service_path | attributes_number | attribute_name | attribute_type | attribute_value | metadata_value | content |
    | 2                | cygnus_multi_instance_04 | /myserv1     | 1                 | pressure       | celcius        | 40.0            | True           | json    |
    | 2                | cygnus_multi_instance_04 | /myserv1     | 1                 | pressure       | celcius        | 41.1            | False          | json    |
    | 2                | cygnus_multi_instance_04 | /myserv1     | 1                 | pressure       | celcius        | 42.2            | True           | xml     |
    | 2                | cygnus_multi_instance_04 | /myserv1     | 1                 | pressure       | celcius        | 43.3            | False          | xml     |
    | 5                | cygnus_multi_instance_05 | /myserv1     | 1                 | pressure       | celcius        | 44.4            | True           | json    |
    | 5                | cygnus_multi_instance_05 | /myserv1     | 1                 | pressure       | celcius        | 45.5            | False          | json    |
    | 5                | cygnus_multi_instance_05 | /myserv1     | 1                 | pressure       | celcius        | 46.6            | True           | xml     |
    | 5                | cygnus_multi_instance_05 | /myserv1     | 1                 | pressure       | celcius        | 47.7            | False          | xml     |


