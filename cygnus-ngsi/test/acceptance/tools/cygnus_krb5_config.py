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

# constants
FILE_NAME              = u'krb5.conf'
EMPTY                  = u''
COMMAND                = u'command'
CURRENT_PATH           = u'path'
SUDO                   = u'sudo'

# commands list
OPS_LIST = []

class Krb5:
    """
    krb5.conf file configuration
        [libdefaults]
         default_realm = NOVALOCAL
         dns_lookup_realm = false
         dns_lookup_kdc = false
         ticket_lifetime = 24h
         renew_lifetime = 7d
         forwardable = true

        [realms]
         NOVALOCAL = {
          kdc = <>
          admin_server = test-iot-ambari
         }

        [domain_realm]
         .example.com = NOVALOCAL
         example.com = NOVALOCAL

    """

    def __init__(self, path, sudo_run):
        """
        constructor
        """
        self.target_path = path
        self.sudo        = sudo_run

    def __append_command(self, command, path, sudo):
        """
        append command lines into a list with its path and if it is necessary the superuser privilege (sudo)
        :param command: command to execute
        :param path: current path where is executed the command
        :param sudo: if it is necessary the superuser privilege (sudo) (True | False)
        """
        OPS_LIST.append({COMMAND:command, CURRENT_PATH: path, SUDO: sudo})

    def config_kbr5(self, **kwargs):
        """
        configuration of krb5.conf file (http://web.mit.edu/kerberos/krb5-1.12/doc/admin/conf_files/krb5_conf.html)
        :param default_realm: Identifies the default Kerberos realm for the client. Set its value to your Kerberos realm.
        :param  kdc: The name or address of a host running a KDC for that realm. An optional port number, separated from the hostname by a colon, may be included
        :param admin_server: Identifies the host where the administration server is running. Typically, this is the master Kerberos server
        :param dns_lookup_realm: Looks for DNS records for fallback host-to-realm mappings and the default realm
        :param dns_lookup_kdc: Indicate whether DNS SRV records should be used to locate the KDCs and other servers for a realm
        :param ticket_lifetime: Sets the default lifetime for initial ticket requests. The default value is 1 day.
        :param renew_lifetime: Sets the default renewable lifetime for initial ticket requests. The default value is 0.
        :param forwardable: If this flag is true, initial tickets will be forwardable by default, if allowed by the KDC. The default value is false.
        """
        default_realm    = kwargs.get("default_realm", "NOVALOCAL")
        kdc              = kwargs.get("kdc", "localhost")
        admin_server     = kwargs.get("admin_server", "localhost")
        dns_lookup_realm = kwargs.get("dns_lookup_realm", "false")
        dns_lookup_kdc   = kwargs.get("dns_lookup_kdc", "false")
        ticket_lifetime  = kwargs.get("ticket_lifetime", "24h")
        renew_lifetime   = kwargs.get("renew_lifetime", "7d")
        forwardable      = kwargs.get("forwardable", "true")


        self.__append_command("cp -R %s.template %s" % (FILE_NAME, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/EXAMPLE.COM/%s/" %s/%s ' % (default_realm, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/kdc = .*/kdc = %s/" %s/%s ' % (kdc, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/admin_server = .*/admin_server = %s/" %s/%s ' % (admin_server, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/dns_lookup_realm = .*/dns_lookup_realm = %s/" %s/%s ' % (dns_lookup_realm, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/dns_lookup_kdc = .*/dns_lookup_kdc = %s/" %s/%s ' % (dns_lookup_kdc, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/ticket_lifetime = .*/ticket_lifetime = %s/" %s/%s ' % (ticket_lifetime, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/renew_lifetime = .*/renew_lifetime = %s/" %s/%s ' % (renew_lifetime, self.target_path, FILE_NAME), self.target_path, self.sudo)
        self.__append_command('sed -i "s/forwardable = .*/forwardable = %s/" %s/%s ' % (forwardable, self.target_path, FILE_NAME), self.target_path, self.sudo)

        return OPS_LIST




