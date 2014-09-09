# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# frb@tid.es
#
#     Author: Ivan Arias
#

from lettuce import world, after, before
from myTools.notifications import Notifications
from myTools.mysql_utils import MySQL


@before.each_scenario
def before_each_scenario(scenario):
    """
    actions before each scenario
    :param scenario:
    """
    world.cygnus        = Notifications(
                             world.config['cygnus']['cygnus_url'],
                             world.config['cygnus']['cygnus_user_agent'],
                             world.config['cygnus']['cygnus_organization_per_row_default'],
                             world.config['cygnus']['cygnus_organization_per_col_default'],
                             world.config['cygnus']['cygnus_resource_default'],
                             world.config['cygnus']['cygnus_attributesNumber_default'],
                             world.config['cygnus']['cygnus_metadatasNumber_default'],
                             world.config['cygnus']['cygnus_compoundNumber_default'],
                             world.config['cygnus']['ckan_dataset_default'],
                             world.config['cygnus']['mysql_prefix'],
    )

    world.mysql          = MySQL(
                             world.config['mysql']['mysql_version'],
                             world.config['mysql']['mysql_host'],
                             world.config['mysql']['mysql_port'],
                             world.config['mysql']['mysql_user'],
                             world.config['mysql']['mysql_pass'],
                             world.config['mysql']['mysql_attrValueType_default'],
                             world.config['mysql']['mysql_metadataType_default']

    )


@after.each_scenario
def after_each_scenario(scenario):
    """
    actions after each scenario
    :param scenario:
    """
    pass