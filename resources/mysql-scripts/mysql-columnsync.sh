#!/bin/bash
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-connectors.
#
# Orion Context Broker is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Orion Context Broker is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
# General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with Orion Context Broker. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# xval@hi-iberia.es

# This script is aimed to Cygnus debugging. It creates the needed database and table in MySQL 
# when the sync between CYGNUS and MySQL is set to COLUMN
# It uses six arguments: 
# MYSQL_HOST
# MYSQL_USER It has to be allowed to create dbs and tables
# MYSQL_PASS User pass
# ENTITY_SERVICE Service Name like FIWARE-Service in Orion headers
# ENTITY_TYPE Type of entity (for this example is type "room")
# ENTITY_ID ID or name of the entity that is sync (for this example is type "room99")



# USAGES
# USING PREFIX 
# $ ./create-dbsinkplace.sh ipmysql dbuser dbpass service1 room room99 pre_
# WITHOUT PREFIX 
# $ ./create-dbsinkplace.sh ipmysql dbuser dbpass service2 room room99

# It should create a table to store TIMESTAMP TEMPERATURE and TEMPERATURE_METADATA if you need order fields, just customize it
MYSQL_HOST=$1
MYSQL_USER=$2
MYSQL_PASS=$3
ENTITY_SERVICE=$4
ENTITY_TYPE=$5
ENTITY_ID=$6

if [[ $7 -eq 0 ]] ; then
    CYGNUS_PREFIX=$7
	MYSQL_DBNAME=${CYGNUS_PREFIX}${ENTITY_SERVICE}
	MYSQL_TABLE=${CYGNUS_PREFIX}${ENTITY_ID}_${ENTITY_TYPE}
else
	MYSQL_DBNAME=${ENTITY_SERVICE}
	MYSQL_DBNAME=${ENTITY_ID}_${ENTITY_TYPE}
fi

echo "DROP DATABASE IF EXISTS ${MYSQL_DBNAME};
	CREATE DATABASE IF NOT EXISTS ${MYSQL_DBNAME};
	USE ${MYSQL_DBNAME};

	DROP TABLE IF EXISTS ${MYSQL_TABLE};
	CREATE TABLE IF NOT EXISTS ${MYSQL_DBNAME} (
	recvTime timestamp NULL DEFAULT NULL,
	temperature varchar(50) DEFAULT NULL,
	temperature_md varchar(50) DEFAULT NULL
	);" | mysql --host=${MYSQL_HOST} --user=${MYSQL_USER} --password=${MYSQL_PASS}

echo "TASK DONE!!
CREATED TABLE: ${MYSQL_DBNAME}
CREATED DATABASE: ${MYSQL_TABLE}"
