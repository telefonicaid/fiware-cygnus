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

import time

from lettuce import world, after, before

import tools.general_utils

@before.all
def before_all_scenarios():
    """
    actions before all scenario
    :param scenario:
    """
    world.test_time_init = time.strftime("%c")
    world.background_executed = False  # used to that background will be executed only once in each feature

@before.each_scenario
def before_each_scenario(scenario):
    """
    actions before each scenario
    :param scenario:
    """
    pass

@after.each_scenario
def after_each_scenario(scenario):
    """
    actions after each scenario
    :param scenario:
    """
    pass

@after.all
def after_all_scenarios(scenario):
    """
    Actions after all scenarios
    Show the initial and final time of the tests completed
    Delete all cygnus instances files
	And cygnus services is stopped
    :param scenario:
    """
    world.cygnus.cygnus_service("stop")
    world.cygnus.delete_cygnus_instances_files()
    tools.general_utils.show_times(world.test_time_init)