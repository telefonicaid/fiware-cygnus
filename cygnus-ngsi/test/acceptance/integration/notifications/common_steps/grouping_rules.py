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

@step (u'update real values in resource "([^"]*)" and service path "([^"]*)" to notification request')
def update_real_values_in_resource_and_service_path_to_notification_request (step, destination, service_path):
    """
    change real resource and service path to notification request
    :param step:
    :param resource:
    :param service_path:
    """
    world.cygnus.change_destination_to_pattern (destination, service_path)

@step (u'changes new destination "([^"]*)" where to verify "([^"]*)"')
def changes_new_destination_where_to_verify_in_dataset (step, destination, dataset):
    """
    change new destination and dataset to validate
    :param step:
    :param destination:
    :param dataset:
    """
    world.cygnus.change_destination_to_pattern (destination, dataset)