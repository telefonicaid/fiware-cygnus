#!/bin/bash
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus.
#
# fiware-cygnus is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# fiware-cygnus is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
# General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with fiware-cygnus. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# francisco.romerobueno@telefonica.com

# This script is aimed to Cygnus debugging. It creates the needed database and table in MySQL 
# when the sync between CYGNUS and MySQL is set to COLUMN
# It uses seven arguments (plus an additional one):
# 1 MYSQL_HOST
# 2 MYSQL_USER It has to be allowed to create dbs and tables
# 3 MYSQL_PASS User pass
# 4 ENTITY_SERVICE Service Name like FIWARE-Service in Orion headers
# 5 ENTITY_TYPE Type of entity (for this example is type "room")
# 6 ENTITY_ID ID or name of the entity that is sync (for this example is type "room99")
# 7 ENTITY_ATT or the attribute type of value to store (for this example is used "temperature")
# 8 (Optional) CYGNUS_PREFIX 

# USAGES:
# WITHOUT PREFIX 
# $ ./create-dbsinkplace.sh ipmysql dbuser dbpass service1 room room99 temperature
# USING PREFIX 
# $ ./create-dbsinkplace.sh ipmysql dbuser dbpass service2 room room99 temperature pre_

# It should create a table to store TIMESTAMP, ENTITY_ATT and ENTITY_ATT_METADATA if you need extra fields, just customize it.
MYSQL_HOST=$1
MYSQL_USER=$2
MYSQL_PASS=$3
ENTITY_SERVICE=$4
ENTITY_TYPE=$5
ENTITY_ID=$6
ENTITY_ATT=$7

if [[ $8 -eq 0 ]] ; then
    CYGNUS_PREFIX=$8
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
	${ENTITY_ATT} varchar(50) DEFAULT NULL,
	${ENTITY_ATT}_md varchar(50) DEFAULT NULL
	);" | mysql --host=${MYSQL_HOST} --user=${MYSQL_USER} --password=${MYSQL_PASS}

echo "TASK COMPLETED!!
CREATED TABLE: ${MYSQL_DBNAME}
CREATED DATABASE: ${MYSQL_TABLE}"
