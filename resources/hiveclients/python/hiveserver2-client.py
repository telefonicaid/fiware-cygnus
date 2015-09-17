#!/usr/bin/env python
# -*- coding: utf-8 -*-

# imports
import sys
import pyhs2
from pyhs2.error import Pyhs2Exception

# get the input parameters
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
