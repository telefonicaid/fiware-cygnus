# -*- coding: utf-8 -*-
#
# Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact:
#  iot_support at tid.es
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
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "tenant_3", service path "/servpath01", resource "room_room2", with attribute number "4", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "True" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
  Examples:
    | content |
    | json    |
    | xml     |

  @happy_path
  Scenario Outline: stored new notifications in ckan from context broker with or without metadata
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "tenant_4", service path "default", resource "default", with attribute number "<attributes_number>", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "<metadata_value>" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
  Examples:
    | attributes_number | attribute_value   | metadata_value | content |
    | 1                 | 45.1              | True           | json    |
    | 2                 | dfgdfgdg          | True           | xml     |
    | 3                 | -45.2344          | False          | xml     |
    | 4                 | {'a':'1','b':'2'} | False          | json    |


  @organizations
  Scenario Outline:  store in ckan new notifications with different organizations values
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "<tenant>", service path "default", resource "default", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
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
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "<tenant>", service path "<service_path>", resource "room_room2", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
  Examples:
    | tenant                 | service_path | content |
    | serv_path_several_1000 |              | json    |
    | serv_path_several_1100 |              | xml     |
    | serv_path_several_2000 | serv6010     | json    |
    | serv_path_several_2100 | serv6011     | xml     |
    | serv_path_several_3000 | SERV6012     | json    |
    | serv_path_several_3100 | SERV6013     | xml     |
    | serv_path_several_4000 | Serv_614     | json    |
    | serv_path_several_4100 | Serv_615     | xml     |
    | serv_path_several_5000 | 1234567890   | json    |
    | serv_path_several_5100 | 1234567890   | xml     |
    | serv_path_several_6000 | /1234567890  | json    |
    | serv_path_several_6100 | /1234567890  | xml     |
    | serv_path_several_7000 | /            | json    |
    | serv_path_several_7100 | /            | xml     |

  @resource @BUG-280 @skip
  Scenario Outline: store in ckan new notifications with different resources values in the same datastore
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "tenant_240", service path "/servicepath", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
  Examples:
    | resource                | content |
    | Room2_Room              | json    |
    | Room2_Room              | xml     |
    | Room2_HOUSE             | json    |
    | Room2_HOUSE             | xml     |
       # these lines are commented because is the same bug 280
    | Room2_                  | json    |
    | Room2_                  | xml     |
    | ROOM_house              | json    |
    | ROOM_house              | xml     |
    | modelogw.assetgw_device | json    |
    | modelogw.assetgw_device | xml     |
    | with max length allowed | json    |
    | with max length allowed | xml     |

  @resources
  Scenario Outline: store in ckan new notifications with different resources values in differents datastore
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "tenant_2500", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
  Examples:
    | service_path | resource                | content |
    | /service100  | Room2_Room              | json    |
    | /service200  | Room2_Room              | xml     |
    | /service300  | Room2_HOUSE             | json    |
    | /service400  | Room2_HOUSE             | xml     |
    | /service500  | Room2_                  | json    |
    | /service600  | Room2_                  | xml     |
    | /service700  | ROOM_house              | json    |
    | /service800  | ROOM_house              | xml     |
    | /service900  | modelogw.assetgw_device | json    |
    | /serviceaa0  | modelogw.assetgw_device | xml     |
    | /servicebbb  | with max length allowed | json    |
    | /serviceccc  | with max length allowed | xml     |

  @attributes_number
  Scenario Outline:  store in ckan new notifications with different quantities of attributes
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "<tenant>", service path "servicepath", resource "room1_room", with attribute number "<attribute_number>", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
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
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "<tenant>", service path "servicepath", resource "room1_room", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "<attribute_value>", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
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

  @matching_table @skip
  Scenario Outline: stored new notifications in ckan with different matching_table patterns
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "match_table", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And changes new destination "<new_destination>" where to verify in dataset "<new_dataset>"
    And Validate that the attribute value, metadata and type are stored in ckan
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

  @matching_table_errors @BUG-271 @skip
  Scenario Outline: not stored new notifications in mysql with errors in matching_table patterns
    Given copy properties.json file from "filab_properties.json" to test "ckan-sink" and sudo local "false"
    And configuration of cygnus instances, agents files quantity "1", id "test" and in "row" mode
    And copy another configuration files and restart cygnus service and this execution is only once "true"
    And verify if cygnus is installed correctly
    And "ckan" is installed correctly
    And a tenant "match_table", service path "<service_path>", resource "<resource>", with attribute number "2", attribute name "random" and attribute type "celcius"
    When receives a notification with attributes value "random", metadata value "False" and content "<content>"
    Then I receive an "OK" http code
    And Validate that the attribute value, metadata and type are stored in ckan
  Examples:
  #  error lines in matching_table.conf file
  #  14|entityId|destmissing(\d*)||errordataset
  #  15|entityId|datasetmissing(\d*)|dest_error|
    | service_path     | resource              | content |
    | servpath_row_010 | destmissing1_error    | json    |
    | servpath_row_010 | destmissing1_error    | xml     |
    | servpath_row_020 | datasetmissing1_error | json    |
    | servpath_row_020 | datasetmissing1_error | xml     |
