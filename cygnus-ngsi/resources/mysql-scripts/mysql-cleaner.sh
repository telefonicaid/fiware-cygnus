#! /bin/bash
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus.
#
# fiware-cygnus  is free software: you can redistribute it and/or
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
# along with Orion Context Broker. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# frb@tid.es

# mysql-cleaner.sh

# This script aims to delete old entries within Cygnus related MySQL tables. In the context
# information persistence world, MySQL is conceived as a short-term historic storage, and thus
# cannot contain all the context information forever.
#
# The way the data is deleted is by comparing the current date with the stored "recvTime" in
# each row. If such difference is greater than a specified number of days, the row is deleted.
#
# The script iterates on all the databases (since each database belogs to a tenant/service) and
# all the tables within databases (since each table belongs to a NGSI entity). This is based on
# the assumption all the databases and tables are Cygnus related; be careful if you host any non
# Cygnus database on the same MySQL server: do not use a "recvTime" column or your data risks to
# be deleted!

# show the usage
if [ $# -ne 4 ]; then
        echo "Usage: mysql-cleaner.sh <MySQL_host> <MySQL_user> <MySQL_password> <data_lifetime_days>";
	exit 1;
fi

# get the input parameters
HOST=$1
USER=$2
PASSWORD=$3
LIFETIME=$4

# show the databases and iterate on them; each one belogs to a different tenant/service
while read -r database; do
        # check if the database must be ignored; as part of the output of "show databases" a
	# "Database" token is generated
        if [ "$database" == "Database" ]; then
                continue
        fi

        # use the database, show the tables and iterate on them; each one belongs to a different
	# NGSI entity
        while read -r table; do
                # check if the table must be ignored; as part of the output of "show tables" a
		# "Tables_in_<database>" token is generated
                if [[ "$table" == *Tables_in_* ]]; then
                        continue
                fi
		
		# delete the rows older than $LIFETIME days
		mysql -h $HOST -u $USER -p$PASSWORD -e "use $database;delete from $table where datediff(curdate(), date(recvTime)) > $LIFETIME"
	done < <(mysql -h $HOST -u $USER -p$PASSWORD -e "use $database;show tables")
done < <(mysql -h $HOST -u $USER -p$PASSWORD -e "show databases")

