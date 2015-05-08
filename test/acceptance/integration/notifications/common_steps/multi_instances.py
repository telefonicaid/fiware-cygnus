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

from lettuce import step, world

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

@step (u'delete instances files')
def delete_instances_files(step):
    """
    Delete instances files
      - cygnus_instances_*.conf
      - agent_*.conf
    """
    world.cygnus.delete_cygnus_instances_files()