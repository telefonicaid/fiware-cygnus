# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
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
#

from lettuce import world, after, before
import time
import tools.general_utils
from tools.cygnus import Cygnus
from tools.ckan_utils import Ckan

@before.all
def before_all_scenarios():
    world.test_time_init = time.strftime("%c")


@before.each_scenario
def before_each_scenario(scenario):
    """
    actions before each scenario
    :param scenario:
    """
    world.cygnus        = Cygnus (world.config['cygnus']['cygnus_url'],
                                  management_port=world.config['cygnus']['cygnus_management_port'],
                                  version=world.config['cygnus']['cygnus_version'],
                                  verify_version=world.config['cygnus']['cygnus_verify_version'],
                                  notif_user_agent= world.config['cygnus']['cygnus_user_agent'],
                                  tenant_row_default=world.config['cygnus']['cygnus_organization_per_row_default'],
                                  tenant_col_default=world.config['cygnus']['cygnus_organization_per_col_default'],
                                  service_path_default=world.config['cygnus']['cygnus_service_path_default'],
                                  identity_id_default =world.config['cygnus']['cygnus_identity_id_default'],
                                  identity_type_default=world.config['cygnus']['cygnus_identity_type_default'],
                                  attribute_number_default=world.config['cygnus']['cygnus_attributes_number_default'],
                                  attribute_name_default=world.config['cygnus']['cygnus_attributes_name_default']
    )

    world.ckan          = Ckan(world.config['ckan']['ckan_url'],
                               world.config['ckan']['ckan_authorization'],
                               ckan_version=world.config['ckan']['ckan_version'],
                               retries_dataset_search=world.config['ckan']['ckan_retries_dataset_search'],
                               delay_to_retry=world.config['ckan']['ckan_delay_to_retry']
    )


@after.each_scenario
def after_each_scenario(scenario):
    """
    actions after each scenario
    :param scenario:
    """
    pass

@after.all
def after_all_scenarios(scenario):
    tools.general_utils.show_times(world.test_time_init)
