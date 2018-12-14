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

# Author: María García Sopo
 
# Show the usage
if [ $# -ne 6 ]; then
        echo "Usage: cygnus_translator_pre0.10.0_to_0.10.0_mysql.sh user password database tableFormat backup"
        echo "where user        : a valid user in the MySQL server granted to modify the below database"
        echo "      password    : password for the above user"
        echo "      database    : a valid database name"
        echo "      table       : a valid table name"
        echo "      table_format: either row or column"
        echo "      backup      : either true or false"
        exit 1;
fi

# Input parameters
user=$1
password=$2
database=$3
table=$4
tableFormat=$5
backup=$6

# Check for the empty value
if [ "$null_value" == "empty" ]; then
        null_value=""
fi

# Function to translate a row-like table
function translate_table_row {
        mysql -u $user -p$password -e "use $database;alter table $table add fiwareServicePath text default null after recvTime"
}

# Function to translate a column-like table
function translate_table_column {
        mysql -u $user -p$password -e "use $database;alter table $table add fiwareServicePath varchar(255) default null after recvTime"
        mysql -u $user -p$password -e "use $database;alter table $table add entityId varchar(255) default null after fiwareServicePath"
        mysql -u $user -p$password -e "use $database;alter table $table add entityType varchar(255) default null after entityId"
}

if [ "$backup" == "yes" ]; then
   bak='_bak'
   tablebak=$table$bak
   mysql -u $user -p$password -e "use $database;create table $tablebak like $table"
   mysql -u $user -p$password -e "use $database;insert $tablebak select * from $table"
fi

if [ "$tableFormat" == "row" ]; then
   translate_table_row 
elif [ "$tableFormat" == "column" ]; then
   translate_table_column
else
   echo "Unknown table format: $tableFormat"
fi

