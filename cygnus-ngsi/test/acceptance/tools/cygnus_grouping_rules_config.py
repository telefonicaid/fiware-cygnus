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

import json

# constants

FILE                  = u'file'
SUDO                  = u'sudo'
PATH                  = u'target_path'
CONFIGURATION_FILE    = u'configuration.json'
SETTINGS_PATH         = u'path_to_settings_folder'
GROUPING_RULES_NAME   = u'grouping_rules_name'
SUDO                  = u'sudo'
FILE_NAME_DEFAULT     = u'grouping_rules.conf'
PATH_DEFAULT          = u'/usr/cygnus/conf'
FAB_DRIVER            = u'fab_driver'


class Grouping_Rules:
    """
    Manage grouping_rules file
    """

    def __init__(self, **kwargs):
        """
        constructor
        :param file: grouping_rules specific created in setting folder to overwrite grouping_rules.conf in cygnus conf folder
        :param target_path: path where will be created the grouping_rules in cygnus conf folder remotely
        :param sudo: privilege in remote server to create or delete files
        :return grouping_rules file name in cygnus conf folder (see configuration.json)
        """
        self.fab_driver  = kwargs.get(FAB_DRIVER, None)
        self.file_name   = kwargs.get(FILE, FILE_NAME_DEFAULT)
        self.target_path = kwargs.get(PATH, PATH_DEFAULT)
        self.sudo        = kwargs.get(SUDO, False)

        with open(CONFIGURATION_FILE) as config_file:   # open configuration.json file
            try:
                self.configuration = json.load(config_file)
            except Exception, e:
                assert False, 'ERROR  - parsing %s file: \n%s' % (CONFIGURATION_FILE, e)

        self.fab_driver.put_file_to_remote("%s/%s" % (self.configuration[SETTINGS_PATH], self.file_name), self.target_path)

    def get_grouping_rules_file_name(self):
        """
        return grouping rules file name
        used the file name "grouping_rules_name" stored in configuration.json file
        """
        return self.configuration[GROUPING_RULES_NAME]



