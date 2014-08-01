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
import logging

import MySQLdb
from lettuce import world


world.conn = None

def connectToDB (host, user, passw, dbname):
    """
    open a new mysql connection
    :param host:
    :param user:
    :param passw:
    :param dbname:
    """
    try:
        world.conn =  MySQLdb.connect(host, user, passw, dbname)
    except Exception, e:
        return errorAssertion ('DB exception: %s' % e)


def newQuery (queryValue, error=False):
    """
    :param queryValue:
    :return:
    """
    try:
        cur = world.conn.cursor ()
        cur.execute (queryValue)
        return cur
    except Exception, e:
        return  errorAssertion ('DB exception: %s' % e, error)

def dropDB (name):
    """
    drop a database existent
    :param name: database name
    """
    newQuery("drop schema "+name)

def disconnectToDB ():
    """
    Close a mysql connection
    """
    world.conn.close ()

def errorAssertion (value, error=False):
    """
    It Shows exception error or return for evaluation
    :param value: exception error text
    :param error: True or False (True - return per evaluation |False shows the exception error)
    :return: exception error text
    """
    if error:
        return value
    else:
        assert True==False, value









