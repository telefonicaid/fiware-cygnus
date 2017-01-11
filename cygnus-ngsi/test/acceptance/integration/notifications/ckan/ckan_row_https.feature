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
Feature: Stored in ckan new notifications per row from context broker
  As a cygnus user
  I want to be able to store in ckan new notifications per row from context broker
  so that they become more functional and useful

  @happy_path
  Scenario Outline: stored new notifications in ckan from context broker with or without metadata
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "happy_path_row_01", service path "/test", entity type "room", entity id "room2", with attribute number "4", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
  Examples:
    | content |
    | json    |
    | xml     |

  @happy_path
  Scenario Outline: stored new notifications in ckan from context broker with or without metadata
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "happy_path_row_02", service path "/test", entity type "room", entity id "room2", with attribute number "<attributes_number>", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
  Examples:
    | attributes_number | attribute_value   | metadata_value | content |
    | 1                 | 45.1              | True           | json    |
    | 2                 | dfgdfgdg          | True           | xml     |
    | 3                 | -45.2344          | False          | xml     |
    | 4                 | {'a':'1','b':'2'} | False          | json    |

  @organizations
  Scenario Outline:  store in ckan new notifications with different organizations values
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "<tenant>", service path "/test", entity type "room", entity id "room2", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
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
  Scenario Outline:  store in ckan new notifications with different service path values
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "<tenant>", service path "<service_path>", entity type "room", entity id "room2", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
  Examples:
    | tenant                 | service_path | content |
    | serv_path_several_1001 |              | json    |
    | serv_path_several_1101 |              | xml     |
    | serv_path_several_2001 | serv6010     | json    |
    | serv_path_several_2101 | serv6011     | xml     |
    | serv_path_several_3001 | SERV6012     | json    |
    | serv_path_several_3101 | SERV6013     | xml     |
    | serv_path_several_4001 | Serv_614     | json    |
    | serv_path_several_4101 | Serv_615     | xml     |
    | serv_path_several_5001 | 1234567890   | json    |
    | serv_path_several_5101 | 1234567890   | xml     |
    | serv_path_several_6001 | /1234567890  | json    |
    | serv_path_several_6101 | /1234567890  | xml     |
    | serv_path_several_7001 | /            | json    |
    | serv_path_several_7101 | /            | xml     |

  @resources_same_dataset @BUG-280 @skip
  Scenario Outline: store in ckan new notifications with different resources values in the same datastore
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "tenant_240", service path "/servicepath", entity type "<entity_type>", entity id "<entity_id>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
  Examples:
    | entity_type             | entity_id               | content |
    | Room                    | Room2                   | json    |
    | Room                    | Room2                   | xml     |
    | HOUSE                   | Room2                   | json    |
    | HOUSE                   | Room2                   | xml     |
    |                         | Room2                   | json    |
    |                         | Room2                   | xml     |
    | house                   | ROOM                    | json    |
    | house                   | ROOM                    | xml     |
    | device                  | modelogw.assetgw        | json    |
    | device                  | modelogw.assetgw        | xml     |
    | room                    | with max length allowed | json    |
    | room                    | with max length allowed | xml     |
    | with max length allowed | room2                   | json    |
    | with max length allowed | room2                   | xml     |

  @resources_diff_dataset
  Scenario Outline: store in ckan new notifications with different resources values in differents datastore
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "test_resources", service path "<service_path>", entity type "<entity_type>", entity id "<entity_id>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
  Examples:
    | service_path | entity_type             | entity_id               | content |
    | /service10   | Room                    | Room2                   | json    |
    | /service10   | Room                    | Room2                   | xml     |
    | /service30   | HOUSE                   | Room2                   | json    |
    | /service30   | HOUSE                   | Room2                   | xml     |
    | /service50   |                         | Room2                   | json    |
    | /service50   |                         | Room2                   | xml     |
    | /service70   | house                   | ROOM                    | json    |
    | /service70   | house                   | ROOM                    | xml     |
    | /service90   | device                  | modelogw.assetgw        | json    |
    | /service90   | device                  | modelogw.assetgw        | xml     |
    | /service100  | room                    | with max length allowed | json    |
    | /service100  | room                    | with max length allowed | xml     |
    | /service110  | with max length allowed | room2                   | json    |
    | /service110  | with max length allowed | room2                   | xml     |

  @attributes_number
  Scenario Outline:  store in ckan new notifications with different quantities of attributes
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "<tenant>", service path "/servicepath", entity type "room", entity id "room1", with attribute number "<attribute_number>", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
  Examples:
    | tenant             | attribute_number | content |
    | attributes_row_001 | 1                | json    |
    | attributes_row_001 | 1                | xml     |
    | attributes_row_003 | 3                | json    |
    | attributes_row_003 | 3                | xml     |
    | attributes_row_010 | 10               | json    |
    | attributes_row_010 | 10               | xml     |

  @values
  Scenario Outline: stored new notifications in ckan with different values
    Given copy properties.json file from "ckan_https_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances with different ports "true", agents files quantity "1", id "test" and in "row" mode
    And copy flume-env.sh, grouping rules file from "grouping_rules.conf", log4j.properties, krb5.conf and restart cygnus service. This execution is only once "true"
    And verify if cygnus is installed correctly
    And verify if ckan is installed correctly
    And service "<tenant>", service path "/servicepath", entity type "room", entity id "room1", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "False" and content "<content>"
    Then receive an "OK" http code
    And Validate that the attribute value, metadata "true" and type are stored in ckan
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