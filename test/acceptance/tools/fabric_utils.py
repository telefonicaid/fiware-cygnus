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


from fabric.api import env, run, get
from fabric.context_managers import hide
from fabric.operations import sudo
from StringIO import StringIO


#constants
EMPTY             = u''
HOST              = u'host'
HOST_DEFAULT      = u'localhost'
PORT              = u'port'
PORT_DEFAULT      = u'22'
USER              = u'user'
USER_DEFAULT      = u'root'
PASSWORD          = u'password'
CERT_FILE         = u'cert_file'
PATH              = u'path'
RETRY             = u'retry'
RETRY_DEFAULT     = u'1'
SUDO              = u'sudo'
COMMAND           = u'command'
HIDE              = u'hide'


class FabricSupport:
    """
    manage fabric tools
    """

    def __init__ (self, **kwargs):
        """
        constructor
        :param host: hostname or IP to connect
        :param port: specify a default port used to connect
        :param hide: show message or not (True or False)
        :param user: implicit user used to connect
        :param password: passwords associated to user
        :param cert_file: certificate file associated to user
        :param retry: number of retries in case of error
        :param sudo: with superuser privileges (True | False)
        """
        self.host                = kwargs.get(HOST,HOST_DEFAULT)
        self.port                = kwargs.get(PORT,PORT_DEFAULT)
        self.hide                = kwargs.get(HIDE, True)
        env.host_string          = "%s:%s" % (self.host, self.port)
        env.user                 = kwargs.get(USER,USER_DEFAULT)
        env.password             = kwargs.get(PASSWORD,EMPTY)
        env.key_filename         = kwargs.get(CERT_FILE,EMPTY)
        env.connection_attempts  = kwargs.get(RETRY,RETRY_DEFAULT)
        env.cwd                  = kwargs.get(PATH, "")
        self.sudo                = kwargs.get(SUDO, False)

    def __sub_run (self, command, sudo_run):
        """
        run a command independently that the output message by console is displayed or not
        :param command: command o script to execute in remote
        :param sudo_run: with superuser privileges (True | False)
        """
        if sudo_run:
            sudo (command)
        else:
            run(command)

    def run(self, command, **kwargs):
        """
        run a command or script in remote host
        :param command: command o script to execute in remote
        :param path: path where execute the command
        :param sudo: with superuser privileges (True | False)
        :param hide: show message or not (True or False)
        """
        env.cwd   = kwargs.get(PATH, env.cwd)
        sudo_run  = kwargs.get(SUDO, self.sudo)
        self.hide = kwargs.get(HIDE, self.hide)
        try:
            if self.hide:
                with hide('running', 'stdout', 'stderr'):
                    self.__sub_run(command,sudo_run)
            else:
                self.__sub_run(command,sudo_run)
        except Exception, e:
            assert False, "ERROR  - running the command \"%s\" remotely with Fabric \n       - %s" % (command, str (e))

    def runs(self, ops_list, **kwargs):
        """
        run several commands in a list with its path associated
        :param sudo: with superuser privileges (True | False)
        :param ops_list: list of commands with its current path associated and if it is necessary of superuser privilege (dictionary).
                         ex:{"command": "ls", "path": "/tmp", "sudo": False}
        """
        for op in ops_list:
            self.run(op[COMMAND], path=op[PATH], sudo=op[SUDO])

    def current_directory (self, directory):
        """
        change current directory
        :param directory: directory path
        """
        env.cwd = directory

    def __sub_read_file(self, file):
        """
        read a file independently that the output message by console is displayed or not
        :param file: file name to read
        """
        fd = StringIO()
        get(file, fd)
        return fd.getvalue()

    def read_file(self, file, **kwargs):
        """
        read a file remotely
        :param file: file name to read
        :param path: path where the file is read
        :param hide: show message or not (True or False)
        """
        env.cwd  = kwargs.get(PATH, env.cwd)
        self.hide = kwargs.get(HIDE, self.hide)
        try:
            if self.hide:
                with hide('running', 'stdout', 'stderr'):
                    return self.__sub_read_file(file)
            else:
                return self.__sub_read_file(file)
        except Exception, e:
            assert False, "ERROR  -reading a File \"%s\" remotely with Fabric \n       - %s" % (file, str (e))

