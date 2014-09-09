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
import MySQLdb
import general_utils
import time
from myTools.constants import *

class MySQL:
    world.conn = None

    def __init__(self, mysql_version, mysql_host, mysql_port, mysql_user, mysql_passw, mysql_attrValueType_default, mysql_metadataType_default):
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
        world.mysql_attrValueType_default = mysql_attrValueType_default
        world.mysql_metadataType_default = mysql_metadataType_default

    def __errorAssertion (self, value, error=False):
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

    def __newQuery (self, queryValue, error=False):
        """
        new query
        :param queryValue:
        :return: message as text (True) or as assertion (False)
        """
        try:
            cur = world.conn.cursor ()
            cur.execute (queryValue)
            return cur
        except Exception, e:
            return  self.__errorAssertion ('DB exception: %s' % e, error)

    def openConnection (self):
        """
        Open a new mysql connection
        """
        try:
            world.conn =  MySQLdb.connect(world.mysql_host, world.mysql_user, world.mysql_passw, EMPTY)
        except Exception, e:
            return self.__errorAssertion ('DB exception: %s' % e)

    def closeConnection (self):
        """
        Close a mysql connection and drop the database before
        """
        self.__newQuery("DROP SCHEMA "+world.mysql_database)  # drop database
        world.conn.close ()  # close mysql connection

    def createDB (self, organization):
        """
        create a new Database
        :param DBname: database name (organization)
        """
        if organization != DEFAULT: world.organization[world.cygnus_type] = organization
        world.mysql_database = world.mysql_prefix+world.organization[world.cygnus_type].lower()   # converted to lowercase, because cygnus always convert to lowercase
        self.__newQuery(MYSQL_CREATE_DATABASE+world.mysql_database)

    def __generateField (self, attrQuantity, attrValueType, metadataType):
        """
        generate fields to datastore request
        :param attrQuantity:
        :param attrValueType:
        :param metadataType:
        :return: fields list
        """
        field = " (recvTime text"
        for i in range(int(attrQuantity)):
            field = field + ", " + ATTR_NAME+"_"+str(i)+" "+ attrValueType
            if metadataType != WITHOUT_METADATA_FIELD:
               field = field + ", " + ATTR_NAME+"_"+str(i)+"_md "+ metadataType
        field = field + ")"
        return field

    def __splitResource (self, resource):
        """
        split resource in entityType and entityId
        :param content:
        """
        res = resource.split ("-")
        world.resource_identityType = res [1]
        world.resource_identityId = res [0]
        return world.resource_identityId + "_"+world.resource_identityType

    def createTable (self, resource, attrQuantity, attrValueType, metadataType):
        """
        create a new table per column type
        :param resource:
        :param attrQuantity:
        :param attrValueType:
        :param metadataType:
        """
        if resource != DEFAULT : world.resource = resource
        resp=self.__splitResource(world.resource)
        if attrQuantity != DEFAULT: world.attrsNumber = attrQuantity
        world.mysql_tableName = world.mysql_prefix+resp
        field = self.__generateField (attrQuantity, attrValueType, metadataType)
        self.__newQuery(MYSQL_CREATE_TABLE + world.mysql_database + "." + world.mysql_tableName +field);

#--------------------------
    def verifyMysqlVersion(self):
        """
        Verify if the mysql version is the expected
        """
        cur = self.__newQuery(SELECT_VERSION)
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
        world.mysql_database = world.mysql_prefix+world.organization[world.cygnus_type].lower()   # converted to lowercase, because cygnus always convert to lowercase
        world.mysql_table    = world.mysql_prefix+world.resource_identityId + "_" + world.resource_identityType

        for i in range(world.attrsNumber):                                                                      # loops through all our  attributes
            cur = self.__newQuery('SELECT * from '+world.mysql_database+'.'+world.mysql_table+ ' WHERE attrName = "'+world.attrs[i][NAME]+'"')
            world.row = cur.fetchone ()
            if world.row != None:                                                                                       # if find the name, begin the verifications
                attrType  = world.row [5]
                attrValue = world.row [6]
                if world.compoundNumber == 0:
                    if attrValue != world.attrs[i][valueTemp]:                                                        # verify the value
                            return "The "+world.attrs[i][NAME]+" value does not match..."
                else:
                    dictTemp = general_utils.convertStrToDict(attrValue, JSON)
                    for j in range(world.compoundNumber):
                        if dictTemp[ITEM+str(j)] != world.attrs[i][valueTemp][ITEM+str(j)]:              # verify the compound values
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
            cur = self.__newQuery('SELECT * from '+world.mysql_database+'.'+world.mysql_table+ ' WHERE attrName = "'+world.attrs[i][NAME]+'"') # if find the name, begin the verifications
            world.row = cur.fetchone ()
            if world.row != None:
                meta = general_utils.convertStrToDict(world.row [7],JSON)                                                                                 # verify if it has metadatas
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
        if response != "OK": self.closeConnection ()
        assert response == "OK", \
        "...Wrong data stored in mysql: %s \n" % (response)

    def verifyIfDatabaseExist (self):
        """
        Validate that the database parameters are not created in mysql
        """
        world.mysql_database = world.mysql_prefix+world.organization[world.cygnus_type]
        world.mysql_table    = world.mysql_prefix+world.resource_identityId + "_" + world.resource_identityType
        msg = self.__newQuery('SELECT * from '+world.mysql_database+'.'+world.mysql_table,True)
        assert msg.find (VALIDATE_DB_ERROR)  > 0 or  msg.find (VALIDATE_TABLE_ERROR) , "%s: %s.%s" % (ERROR_DB_MSG, world.mysql_database, world.mysql_table)

    # ----------------------- column ----------------------
    def verifyDatasetSearch_valuesAndType_per_column (self, content):
        """
        Verify that the attribute contents (metadatas) are stored in mysql per column
        :param content: xml or json
        """
        world.row = None
        DelayToVerifyValues = 0.5
        outMsg = "Names are missing"
        if content == XML:
            valueTemp = CONTENT_VALUE
        else:
             valueTemp = VALUE_JSON
        cur = self.__newQuery('SELECT * FROM '+world.mysql_database+'.'+world.mysql_tableName+ ' ORDER BY 1 DESC LIMIT 1')
        world.row  = cur.fetchone ()   # return de last line in the table
        if world.row != None:
            for i in range(int(world.attrsNumber)):
                time.sleep (DelayToVerifyValues)
                if str(str (world.row [((i+1)*2)-1])) != str(world.attrs[i][valueTemp]):                  # verify the value
                    return "The "+world.attrs[i][NAME]+" value does not match..."
            return "OK"

    def verifyDatasetSearch_metadatas_per_column (self, content):
        """
        Verify that the attribute contents (metadatas) are stored in mysql per column
        :param content: xml or json

        """
        if world.metadataValue == TRUE:
            if world.row != None:
                for i in range(int(world.attrsNumber)):
                    self.metadata = general_utils.convertStrToDict(world.row [(i+1)*2], JSON)
                    if content == XML:
                        if self.metadata[0][VALUE_JSON] != world.attrs[i][METADATA][0][CONTEXT_METADATA][VALUE_JSON]:
                            return "The "+world.attrs[i][NAME]+" metatada value does not match..."
                    else:
                        if self.metadata[0][VALUE_JSON] != world.attrs[i][METADATAS_JSON][0][VALUE_JSON]:
                            return "The "+world.attrs[i][NAME]+" metatada value does not match..."
                    self.metadata = None
        return "OK"

    def verifyIfTableIsEmpty (self):
        cur = self.__newQuery('SELECT COUNT(*) FROM '+world.mysql_database+'.'+world.mysql_tableName)
        row  = cur.fetchone ()
        assert row[0] == 0,\
            " %s %s \n" % (VALIDATE_RESOURCE_IS_NOT_EMPTY_MSG, world.resource)
        pass






