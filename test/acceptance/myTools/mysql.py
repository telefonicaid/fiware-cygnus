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

from lettuce import world
import mysql_utils
import http
import utils
from myTools.constants import *

class MySQL:

    def __init__(self, mysql_version, mysql_host, mysql_port, mysql_user, mysql_passw):
        """
        constructor
        :param self:
        :param mysql_version:
        :param mysql_host:
        :param mysql_port:
        :param mysql_user:
        :param mysql_pass:
        :param mysql_database:
        """
        world.mysql_version  = mysql_version
        world.mysql_host     = mysql_host
        world.mysql_port     = mysql_port
        world.mysql_user     = mysql_user
        world.mysql_passw    = mysql_passw


    def openConnection (self):
        """
        Open a new mysql connection
        """
        return mysql_utils.connectToDB(world.mysql_host, world.mysql_user, world.mysql_passw, EMPTY)

    def closeConnection (self):
        """
        Close a mysql connection
        """
        mysql_utils.dropDB(world.mysql_database)
        mysql_utils.disconnectToDB()

#--------------------------
    def verifyMysqlVersion(self):
        """
        Verify if the mysql version is the expected
        """
        cur = mysql_utils.newQuery(SELECT_VERSION)
        row = cur.fetchone ()
        assert row[0] == world.mysql_version, \
        "Wrong version expected: %s. and version installed: %s" \
        % (str(world.mysql_version),  str(row[0]))

    def verifyDatasetSearch_valuesAndType (self, content):
        """
        Verify that the attribute contents (type and value) are stored in mysql
        :param content: xml or json
        """
        outMsg = "Names are missing"
        if content == XML:
            valueTemp = CONTENT_VALUE
        else:
             valueTemp = VALUE_JSON
        world.mysql_database = world.mysql_prefix+world.organization
        world.mysql_table    = world.mysql_prefix+world.resource_identityId + "_" + world.resource_identityType

        for i in range(world.attrsNumber):                                                                      # loops through all our  attributes
            cur = mysql_utils.newQuery('SELECT * from '+world.mysql_database+'.'+world.mysql_table+ ' WHERE attrName = "'+world.attrs[i][NAME]+'"')
            world.row = cur.fetchone ()
            if world.row != None:                                                                                       # if find the name, begin the verifications
                attrValue = world.row [6]
                attrType  = world.row [5]

                if world.compoundNumber == 0:
                    if attrValue != world.attrs[i][valueTemp]:                                                        # verify the value
                            return "The "+world.attrs[i][NAME]+" value does not match..."
                else:
                    dictTemp = utils.convertStrToDict(attrValue, JSON)
                    for j in range(world.compoundNumber):
                        if dictTemp[ATTR_VALUE][ITEM+str(j)] != world.attrs[i][valueTemp][ITEM+str(j)]:              # verify the compound values
                            return "The "+world.attrs[i][NAME][ITEM+str(j)]+" compound values does not match..."
                if attrType != world.attrs[i][TYPE]:                                                               # verify the type
                    return "The "+world.attrs[i][NAME]+" type does not match..."
                outMsg = "OK"
                break
        return outMsg

    def verifyDatasetSearch_metadatas (self, content):
        """
        Verify that the attribute contents (metadatas) are stored in mysql
        :param content: xml or json

        """
        if world.metadatasNumber <= 0: return "does not has metadata.."
        outMsg = "Names are missing"
        for i in range(world.attrsNumber):                                                                                                          # loops through all our  attributes
            cur = mysql_utils.newQuery('SELECT * from '+world.mysql_database+'.'+world.mysql_table+ ' WHERE attrName = "'+world.attrs[i][NAME]+'"') # if find the name, begin the verifications
            world.row = cur.fetchone ()
            if world.row != None:
                meta = utils.convertStrToDict(world.row [7],JSON)                                                                                 # verify if it has metadatas
                for j in range(len (meta)):
                    if content == XML:
                        if meta[j][TYPE] != world.attrs[i][METADATA][j][CONTEXT_METADATA][TYPE]:
                            return "The "+world.attrs[i][NAME]+" metatada type does not match..."
                    else:
                        if meta[j][TYPE] != world.attrs[i][METADATAS_JSON][j][TYPE]:
                            return "The "+world.attrs[i][NAME]+" metatada type does not match..."
                    outMsg = "OK"
                    break
        return outMsg


    def validateResponse (self, response):
        """
        assert the response obtained after content verifications
        :param response:
        """
        assert response == "OK", \
        "...Wrong data stored in mysql: %s \n" % (response)

    def verifyIfDatabaseExist (self):
        """
        Validate that the database parameters are not created in mysql
        """
        world.mysql_database = world.mysql_prefix+world.organization
        world.mysql_table    = world.mysql_prefix+world.resource_identityId + "_" + world.resource_identityType
        msg = mysql_utils.newQuery('SELECT * from '+world.mysql_database+'.'+world.mysql_table,True)
        assert msg.find (VALIDATE_DB_ERROR)  > 0 or  msg.find (VALIDATE_TABLE_ERROR) , "%s: %s.%s" % (ERROR_DB_MSG, world.mysql_database, world.mysql_table)