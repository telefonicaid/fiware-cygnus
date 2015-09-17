#!/usr/bin/env python
# -*- coding: utf-8 -*-

# imports
import sys
import hive_utils
from hive_service.ttypes import HiveServerException

# get the input parameters
hiveHost = sys.argv[1]
hivePort = sys.argv[2]
dbName = sys.argv[3]
hadoopUser = sys.argv[4]
hadoopPassword = sys.argv[5]

# do the connection
client = hive_utils.HiveClient(server=hiveHost,
                               port=hivePort,
                               db=dbName)

# create a loop attending HiveQL queries
while (1):
    query = raw_input('remotehive> ')

    try:
        if not query:
            continue

        if query == 'exit':
            sys.exit()

        # execute the query
        for row in client.execute(query):
            print row

    except HiveServerException, ex:
            print ex.message

