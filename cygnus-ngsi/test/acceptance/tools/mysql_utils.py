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

import time
import MySQLdb
import gc

# constants
EMPTY         = u''
WITHOUT       = u'without'

# mysql commands
SELECT_VERSION        = u'SELECT version ()'
MYSQL_CREATE_DATABASE = u'CREATE DATABASE IF NOT EXISTS '
MYSQL_CREATE_TABLE    = u'CREATE TABLE IF NOT EXISTS '
MYSQL_DROP_DATABASE   = u'DROP SCHEMA IF EXISTS '
MYSQL_SHOW_DATABASE   = u'SHOW DATABASES'
MYSQL_SHOW_TABLES     = u'SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = \''


class Mysql:
    """
    mysql funcionabilities
    """

    def __init__(self, **kwargs):
        """
        constructor
        :param host: mysql host (MANDATORY)
        :param port: mysql port (MANDATORY)
        :param user: mysql user (MANDATORY)
        :param password:  mysql pass (MANDATORY)
        :param database: mysql database (OPTIONAL)
        :param version: mysql version (OPTIONAL)
        :param mysql_verify_version: determine whether the version is verified or not (True or False). (OPTIONAL)
        :param capacity: capacity of the channel (OPTIONAL)
        :param channel_transaction_capacity: amount of bytes that can be sent per transaction (OPTIONAL)
        :param retries_number: number of retries when get values (OPTIONAL)
        :param delay_to_retry: time to delay each retry (OPTIONAL)
        """
        self.host     = kwargs.get("host", EMPTY)
        self.port     = kwargs.get("port", EMPTY)
        self.user     = kwargs.get("user", EMPTY)
        self.password = kwargs.get("password", EMPTY)
        self.database = kwargs.get("database", EMPTY)
        self.version = kwargs.get("version", "2,2")
        self.mysql_verify_version = kwargs.get("mysql_verify_version", "false")
        self.capacity = kwargs.get("capacity", "1000")
        self.transaction_capacity = kwargs.get("transaction_capacity", "100")
        self.retries_number=int(kwargs.get('retries_number',1))
        self.retry_delay=int(kwargs.get('delay_to_retry',10))
        self.conn     = None
        self.database = None

    def __error_assertion(self, value, error=False):
        """
        It Shows exception error or return for evaluation
        :param value: exception error text
        :param error: True or False (True - return per evaluation |False shows the exception error)
        :return: exception error text
        """
        if error:
            return value
        assert False, value

    def __query(self, sql, error=False):
        """
        new query
        :param sql: query
        :return: message as text
        """
        try:
            cur = self.conn.cursor()
            cur.execute(sql)
            return cur
        except Exception, e:
            return  self.__error_assertion('DB exception: %s' % (e), error)

    def __drop_database (self):
        """
        delete a database
        """
        self.__query("%s `%s`" % (MYSQL_DROP_DATABASE, self.database))  # drop database

    # public methods ------------------------------------------
    def connect(self):
        """
        Open a new mysql connection
        """
        try:
            self.database = EMPTY
            self.conn =  MySQLdb.connect(self.host, self.user, self.password, self.database)
        except Exception, e:
            return self.__error_assertion ('DB exception: %s' % (e))

    def set_database (self, database):
        """
        set database name
        """
        self.database = database


    def disconnect (self):
        """
        Close a mysql connection and drop the database before
        """
        self.__drop_database()
        self.conn.close()  # close mysql connection
        gc.collect()       # invoking the Python garbage collector

    def verify_version(self):
        """
        Verify if the mysql version is the expected
        """
        if self.mysql_verify_version.lower() == "true":
            cur = self.__query(SELECT_VERSION)
            row = cur.fetchone ()
            assert row[0] == self.version, \
                "Wrong version expected: %s. and version installed: %s"  % (str(self.version),  str(row[0]))

    def create_database(self, name):
        """
        create a new Database
        :param name:
        """
        self.database = name.lower() # converted to lowercase, because cygnus always convert to lowercase per ckan
        self.__query("%s `%s`;" % (MYSQL_CREATE_DATABASE, self.database))

    def generate_field_datastore_to_resource (self, attributes_number, attributes_name, attribute_type, metadata_type, recvtime="timestamp"):
        """
        generate fields to datastore request
        :return: fields list
        """
        field = " (recvTime "+recvtime
        for i in range(int(attributes_number)):
            if attribute_type != WITHOUT: field = field + ", " + attributes_name+"_"+str(i)+" "+ attribute_type
            if metadata_type  != WITHOUT: field = field + ", " + attributes_name+"_"+str(i)+"_md "+ metadata_type
        return  field + ")"

    def create_table (self, name, database_name, fields):
        """
        create a new table per column type
        :param name:
        :param database_name:
        :param fields:
        """
        self.table = name
        self.__query("%s `%s`.`%s` %s;" % (MYSQL_CREATE_TABLE,  database_name, self.table, fields))

    def table_exist (self, database_name, table_name):
        """
        determine if table exist in database
        :param database_name:
        :param table_name:
        """
        cur = self.__query('SELECT table_name FROM information_schema.tables WHERE table_schema = "%s" AND table_name = "%s" LIMIT 1;' % (database_name, table_name))
        return  cur.fetchone ()

    def table_search_one_row (self, database_name, table_name):
         """
         get last record from a table
         :param database_name:
         :param table_name:
         """
         if self.table_exist(database_name, table_name) != None:
             cur = self.__query('SELECT * FROM `%s`.`%s` ORDER BY 1 DESC LIMIT 1;' % (database_name, table_name))
             return  cur.fetchone ()   # return one row from the table
         return False

    def table_search_several_rows (self,rows, database_name, table_name):
         """
         get last records from a table
         :param rows:
         :param database_name:
         :param table_name:
         """
         if self.table_exist(database_name, table_name) != None:
             cur = self.__query('SELECT * FROM `%s`.`%s` ORDER BY 1 DESC LIMIT %s;' % (database_name, table_name, rows))

             return  cur.fetchall ()   # return several lines from the table
         return False




