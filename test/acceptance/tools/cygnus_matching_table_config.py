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


import json
import subprocess


# constants
EMPTY                 = u''
FILE                  = u'file'
SUDO                  = u'sudo'
PATH                  = u'target_path'
CONFIGURATION_FILE    = u'configuration.json'
SETTINGS_PATH         = u'path_to_settings_folder'
MATCHING_TABLE_NAME   = u'matching_table_name'
COMMAND               = u'command'
CURRENT_PATH          = u'path'
SUDO                  = u'sudo'
UTF_8                 = u'utf-8'

# commands list
OPS_LIST = []



class Matching_tables:
    """
    Manage matching table file
    """

    def __init__(self, **kwargs):
        """
        constructor
        :param file: matching table specific created in setting folder to overwrite matching_table.conf in cygnus conf folder
        :param target_path: path where will be created the matching table in cygnus conf folder remotely
        :param sudo: privilege in remote server to create or delete files
        :return matching table file name in cygnus conf folder (see configuration.json)
        """
        self.file_name   = kwargs.get(FILE, EMPTY)
        self.target_path = kwargs.get(PATH, EMPTY)
        self.sudo        = kwargs.get(SUDO, False)

        with open(CONFIGURATION_FILE) as config_file:   # open configuration.json file
            try:
                self.configuration = json.load(config_file)
            except Exception, e:
                assert False, 'ERROR  - parsing %s file: \n%s' % (CONFIGURATION_FILE, e)

        if self.file_name != EMPTY:
            with open("%s/%s" % (self.configuration[SETTINGS_PATH], self.file_name)) as help_file:  # open matching_table.conf in local, storing in memory
                try:
                    self.content = help_file.read().splitlines()
                except Exception, e:
                    assert False, "ERROR  - parsing \"%s\" file \n       - %s" % (self.file_name, str(e))

    def __append_command(self, command, path, sudo):
        """
        append command lines into a list with its path and if it is necessary the superuser privilege (sudo)
        :param command: command to execute
        :param path: current path where is executed the command
        :param sudo: if it is necessary the superuser privilege (sudo) (True | False)
        """
        OPS_LIST.append({COMMAND:command, CURRENT_PATH: path, SUDO: sudo})

    def content_to_copy(self):
        """
        generate commands to create a new file in remote with matching table configuration
        :return: command list
        """
        self.__append_command("rm -rf %s" % self.configuration[MATCHING_TABLE_NAME], self.target_path, self.sudo)
        for i in range (len (self.content)):
            unicode_string = self.content[i].decode(UTF_8)   # convert ascii to utf-8
            self.__append_command('echo \"%s\" >> %s' % (unicode_string,  self.configuration[MATCHING_TABLE_NAME]), self.target_path, self.sudo)
        return OPS_LIST

    def get_matching_table_file_name(self):
        """
        delete matching table file in cygnus conf remotely
        used the file name "matching_table_name" stored in configuration.json file
        """
        return self.configuration[MATCHING_TABLE_NAME]



