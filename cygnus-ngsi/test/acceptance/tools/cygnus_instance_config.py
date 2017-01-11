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

#constant
FILENAME_SOURCE        = u'cygnus_instance.conf.template'
FILENAME_TARGET        = u'cygnus_instance_%s.conf'

EMPTY        = u''
SOURCE_PATH  = u'source_path'
TARGET_PATH  = u'target_path'
PATH_DEFAULT = u'/'
ADMIN_PORT   = u'admin_port'
ADMIN_PORT_DEFAULT = u'8081'
ID           = u'id'
COMMAND      = u'command'
CURRENT_PATH = u'path'
SUDO         = u'sudo'

OPS_LIST = []


class Cygnus_Instance:
    """
    configuration of cygnus_instance_<id>.conf
    """
    def __init__(self, **kwargs):
        """
        constructor
        :param source_path: source path where read the cygnus_instance.conf
        :param target_path: target path where store the cygnus_instance.conf
        :param id: id postfix used by instances
        :param sudo:  with superuser privileges (True | False)
        """
        self.source_path = kwargs.get(SOURCE_PATH, PATH_DEFAULT)
        self.target_path = kwargs.get(TARGET_PATH, PATH_DEFAULT)
        self.id          = kwargs.get(ID, EMPTY)
        self.sudo        = kwargs.get(SUDO, False)

    def append_id (self, **kwargs):
        """
        append a new instance of cygnus
        :param source_path: source path where read the cygnus_instance.conf
        :param target_path: target path where store the cygnus_instance.conf
        :param management_port: management port used in the instance
        :param id: id postfix used by instances
        :return: string (commands list)
        """
        global OPS_LIST
        OPS_LIST = []
        self.source_path = kwargs.get(SOURCE_PATH, self.source_path)
        self.target_path = kwargs.get(TARGET_PATH, self.target_path)
        self.admin_port  = kwargs.get(ADMIN_PORT, ADMIN_PORT_DEFAULT)
        self.id   = kwargs.get(ID, self.id)
        return self.update()

    def __append_command(self, command, path, sudo):
        """
        append command lines into a list with its path and if it is necessary the superuser privilege (sudo)
        :param command: command to execute
        :param path: current path where is executed the command
        :param sudo: if it is necessary the superuser privilege (sudo) (True | False)
        """
        OPS_LIST.append({COMMAND:command, CURRENT_PATH: path, SUDO: sudo})

    def update (self):
        """
        generate commands list to modify cygnus_instance_<id>.conf file
            -  copy cygnus_instance.conf.template in target path
            -  modify CONFIG_FOLDER, CONFIG_FILE, AGENT_NAME
        :return: string (commands list)
        """
        name = FILENAME_TARGET % (self.id)
        # copy cygnus_instance.conf.template in target path with id postfix appended
        self.__append_command('cp -R %s/%s %s/%s' % (self.source_path, FILENAME_SOURCE,self.target_path, name), EMPTY, self.sudo)
        # modify cygnus_instance_<id>.conf file
        sed_target_path = self.target_path.replace("/", "\/")    # replace / to \/ in path that is used in sed command
        self.__append_command('sed -i "s/AGENT_NAME=.*/AGENT_NAME=%s/" %s/%s ' % (self.id, self.target_path, name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/CONFIG_FOLDER=.*/CONFIG_FOLDER=%s/" %s/%s ' % (sed_target_path, self.target_path, name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/CONFIG_FILE=.*/CONFIG_FILE=%s\/agent_%s.conf/" %s/%s ' % (sed_target_path, self.id, self.target_path, name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/ADMIN_PORT=.*/ADMIN_PORT=%s/" %s/%s ' % (self.admin_port, self.target_path, name), self.target_path, self.sudo)
        return OPS_LIST

    def get_file_name (self, id):
        """
        return file name with id included. ex: cygnus_instance__<id>.conf
        :param id: id to instance
        :return: string
        """
        return FILENAME_TARGET % (id)