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

# ckan-datastore-cleaner.sh

# This script aims to delete old entries within CKAN datastore given that. It has to be run
# in the same machine where the underlying postgresql CKAN database runs, given that it is
# not possible to delete ranges using the API
# (see details at http://stackoverflow.com/questions/24288744/deleting-rows-in-datastore-by-time-range

# The information is stored in the tables of the datastore_default database. Each table name
# match the ID of the corresponding resource.
#
# The script has to be run by root or by an user with sudo privilegies.
#
# The way the data is deleted is by comparing the current date with the stored "recvTime" in
# each row. If such difference is greater than a specified number of days, the row is deleted.
# The script iterates in all tables (i.e. resources' datastore), based on the following ouptut
# fot the '\d' (table listing) psql command:
#
# ...
# public | be1a5083-9d19-yyyy-xxxx-31c2ef607e10         | table    | ckan_default
# public | be1a5083-9d19-yyyy-xxxx-31c2ef607e10__id_seq | sequence | ckan_default
# ...

# show the usage
if [ $# -ne 1 ]; then
        echo "Usage: ckan-datastore-cleaner.sh <data_lifetime_days>";
        exit 1;
fi

# get the input parameters
LIFETIME=$1

# terate on tables (i.e. resources's datastore)
while read -r line; do
        echo "Cleaning datastore of resource $line"
        sudo -i -u postgres psql -d datastore_default -c "DELETE FROM \"$line\" WHERE current_timestamp - \"recvTime\" > interval '$LIFETIME day'"
done < <(sudo -i -u postgres psql -d datastore_default -c "\d" | grep '| table' | awk -F '|' '{ print $2 }' | tr -d ' ')

