#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FIWARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
# General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
# option) any later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es

# imports
import sys
import pyhs2
from pyhs2.error import Pyhs2Exception

# get the input parameters
if len(sys.argv) != 6:
    print 'Usage: python hiveserver2-client.py <hive_host> <hive_port> <db_name> <hadoop_user> <hadoop_password>'
    sys.exit()

hiveHost = sys.argv[1]
hivePort = sys.argv[2]
dbName = sys.argv[3]
hadoopUser = sys.argv[4]
hadoopPassword = sys.argv[5]

# do the connection
with pyhs2.connect(host=hiveHost,
                   port=hivePort,
                   authMechanism="PLAIN",
                   user=hadoopUser,
                   password=hadoopPassword,
                   database=dbName) as conn:
    # get a client
    with conn.cursor() as client:
        # create a loop attending HiveQL queries
        while (1):
            query = raw_input('remotehive> ')

            try:
                if not query:
                    continue

                if query == 'exit':
                    sys.exit()

                # execute the query
                client.execute(query)
 
                # get the content
                for row in client.fetch():
                    print row

            except Pyhs2Exception, ex:
                print ex.errorMessage
