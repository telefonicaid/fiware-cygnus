#! /bin/bash
#
# Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
#

# Author: frb

# Show the usage
if [ $# -ne 5 ]; then
        echo "Usage: cygnus_translator_pre0.10.0_to_0.10.0_mysql.sh user password database tableFormat backup"
        echo "where user        : a valid user in the MySQL server granted to modify the below database"
        echo "      password    : password for the above user"
        echo "      database    : a valid database name"
        echo "      table_format: either row or column"
        echo "      backup      : either true or false"
        exit 1;
fi

# Input parameters
user=$1
password=$2
database=$3
tableFormat=$4
backup=$5

# Check for the empty value
if [ "$null_value" == "empty" ]; then
        null_value=""
fi

# Function to translate a row-like table
# $1 --> table to be translated (input)
function translate_table_row {
        local table=$1
        mysql -u $user -p$password -e "use $database;alter table $table add fiwareServicePath text default null after recvTime"
}

# Function to translate a column-like table
# $1 --> table to be translated (input)
function translate_table_column {
        local table=$1
        mysql -u $user -p$password -e "use $database;alter table $table add fiwareServicePath text default null after recvTime"
        mysql -u $user -p$password -e "use $database;alter table $table add entityId text default null after fiwareServicePath"
        mysql -u $user -p$password -e "use $database;alter table $table add entityType text default null after entityId"
}

# Function to translate a database
# $1 --> database to be translated (input)
function translate_database {
        local dbName=$1

        while read -r entry; do
                echo "Translating $database.$entry"

                if [ "$backup" == "yes" ]; then
                        bak='_bak'
                        entrybak=$entry$bak
                        mysql -u $user -p$password -e "use $database;create table $entrybak like $entry"
                        mysql -u $user -p$password -e "use $database;insert $entrybak select * from $entry"
                fi

                if [ "$tableFormat" == "row" ]; then
                        translate_table_row $entry
                elif [ "$tableFormat" == "column" ]; then
                        translate_table_column $entry
                else
                        echo "Unknown table format: $tableFormat"
                fi
        done < <(mysql -u $user -p$password -e "use $database;show tables" | grep -v Tables_in)
}

# Main function
translate_database $database

