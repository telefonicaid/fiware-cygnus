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

from lettuce import step, world

@step (u'service "([^"]*)", service path "([^"]*)", entity type "([^"]*)", entity id "([^"]*)", with attribute number "([^"]*)", attribute name "([^"]*)" and attribute type "([^"]*)"')
def a_service_service_path_resource_with_attribute_number_and_attribute_name (step, tenant, service_path, entity_type, entity_id, attribute_number, attribute_name, attribute_type):
    """
    row configuration in row mode
    :param step:
    :param tenant:
    :param service_path:
    :param resource_name:
    :param attribute_number:
    :param attribute_name:
    :param attribute_type:
    """
    world.cygnus.configuration(tenant, service_path, entity_type, entity_id, attribute_number, attribute_name, attribute_type)

@step(u'receives a notification with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
def and_receives_a_notification_with_attributes_value_group1_metadata_value_group2_and_content_group3(step, attribute_value, metadata_value, content):
    """
    received fake notifications simulating Context Broker
    :param step:
    :param attribute_value:
    :param metadata_value:
    :param content:
    """
    world.resp = world.cygnus.received_notification(attribute_value, metadata_value, content)

@step (u'receives "([^"]*)" notifications with consecutive values beginning with "([^"]*)" and with one step')
def receives_notifications_with_consecutive_values(step, notif_number, attribute_value_init):
    """
    receives N notifications with consecutive values, without metadatas and json content
    :param step:
    :param notif_number:
    """
    world.resp = world.cygnus.receives_n_notifications(notif_number, attribute_value_init)

@step (u'receives multiples notifications one by instance and the port defined incremented with attributes value "([^"]*)", metadata value "([^"]*)" and content "([^"]*)"')
def receives_multiples_notifications(step, attribute_value, metadata_value, content):
    """
    receive several notifications by each instance, but changing port
    :param step:
    :param attribute_value:
    :param metadata_value:
    :param content:
    """
    world.resp = world.cygnus.received_multiples_notifications(attribute_value, metadata_value, content)

@step(u'receive an "([^"]*)" http code')
def i_receive_an_http_code (step, http_code_expected):
    """
    validate http code in response
    :param step:
    :param http_code_expected:  http code for validate
    """
    world.cygnus.verify_response_http_code (http_code_expected, world.resp)

