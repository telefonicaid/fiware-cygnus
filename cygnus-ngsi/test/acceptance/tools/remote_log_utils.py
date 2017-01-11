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
import os

import general_utils


# constants
FILE                  = u'file'
FILE_DEFAULT          = u'file.log'
FABRIC                = u'fabric'
CYGNUS_DEFAULT        = u'cygnus'
OWNER                 = u'owner'
GROUP                 = u'group'
MOD                   = u'mod'
MOD_DEFAULT           = u'664'
CONFIGURATION_FILE    = u'configuration.json'
LOG_FILE              = u'log_file'


class Remote_Log:
    """
    manage remote log
    """

    def __init__(self, **kwargs):
        """
        constructor
        :param file: log file
        :param fabric: instance from tools.fabric_utils import FabricSupport
            ex: myfab = FabricSupport(host=host,
                                      user=user,
                                      password=password,
                                      cert_file=cert_file,
                                      retry=error_retry,
                                      hide=True,
                                      sudo=sudo_cygnus)
        """
        self.file_path  = kwargs.get(FILE, FILE_DEFAULT)
        self.fabric     = kwargs.get(FABRIC, None)

        if self.file_path == FILE_DEFAULT:
            with open(CONFIGURATION_FILE) as config_file:   # open configuration.json file
                try:
                    self.file_path = json.load(config_file)[LOG_FILE]
                except Exception, e:
                    assert False, 'ERROR  - parsing %s file: \n%s' % (CONFIGURATION_FILE, e)
        try:
            temp = os.path.split(self.file_path)  # split the path and the file of the file and path together
            self.file = temp[1]
            self.path = temp[0]
            self.fabric.current_directory(self.path)
        except Exception, e:
            assert False, "Error  - %s" % (str(e))

    def delete_log_file (self):
        """
        delete file log
        """
        self.fabric.run("rm -f %s" % (self.file))

    def create_log_file(self, **kwargs):
        """
        create a log file
        :param owner: file owner
        :param group: group ownership of the file
        :param mod: file permissions. Mode can be specified with octal numbers or with letters.
        """
        owner = kwargs.get(OWNER, CYGNUS_DEFAULT)
        group = kwargs.get(GROUP, CYGNUS_DEFAULT)
        mod   = kwargs.get(MOD, MOD_DEFAULT)
        self.fabric.run("echo '' > %s" % self.file)
        self.fabric.run("chown %s:%s %s" % (owner, group, self.file))
        self.fabric.run("chmod %s %s" % (mod, self.file))

    def find_line(self, label, text):
        """
        find the last occurrence in log with a label and a text
        :param label: label to find
        :param text: text to find
        :return: line found or None
        """
        label_list = []
        log_lines = self.fabric.read_file(self.file)
        log_lines_list = general_utils.convert_str_to_list(log_lines, "\n")
        for line in log_lines_list:  # find all lines with the label
            if line.find(label)>= 0:
                label_list.append(line)
        label_list.reverse() # list reverse because looking for the last occurrence
        for line in label_list:
            if line.find(text) >= 0:
                return line
        return None







