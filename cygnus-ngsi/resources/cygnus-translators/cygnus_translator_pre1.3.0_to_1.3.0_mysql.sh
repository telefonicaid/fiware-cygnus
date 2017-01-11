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

# Color constants
BLUE='\033[0;34m'
GREEN='\033[0;32m'
RED='\033[0;31m'
NOCOLOR='\033[0m'

# Show the usage
if [ $# -ne 6 ]; then
	echo "Usage:"
	echo "      cygnus_translator_pre1.3.0_to_1.3.0_mysql.sh <function> <force> <databases> <dictionary> <user> <password>"
	echo "Where:"
	echo "      <function> may be 'analyze', 'replace_encode' or 'copy_encode'"
	echo "      <force> may be 'true' or 'false'"
	echo "      <databases> is the database where to start analyzing/encoding"
	echo "            If configured to 'ALL', all databases are analyzed/encoded"
	echo "      <dictionaly> is a file containing encodings this script is not able to do by itself"
	echo "      <user> is a valid MySQL user"
	echo "      <password> is the password for the above user"
	exit 1
fi

# Input parameters
function=$1
force=$2
database=$3
dictFile=$4
user=$5
password=$6

# Check the function value
if [ "$function" != "analyze" ] && [ "$function" != "replace_encode" ] && [ "$function" != "copy_encode" ]; then
	echo "Unknown function value '$function', use 'analyze', 'replace_encode' or 'copy_encode' instead"
	exit 2
fi

# Check the force value
if [ "$force" != "true" ] && [ "$force" != "false" ]; then
	echo "Unknown force value '$force', use 'true' or 'false' instead"
	exit 2
fi

# Function to load a dictionary
declare -A dictionary

function load_dictionary {
	while IFS='' read -r line || [[ -n "$line" ]]; do
		IFS=',' read -ra splits <<< "$line"
		key=${splits[0]}
		value=${splits[1]}
		dictionary[$key]=$value
	done < "$dictFile"
}

# Function to get a dictionary entry
# $1 --> entry (input)
function get_dictionary {
	local entry=$1

	echo ${dictionary[$entry]}
}

# Function to get the encoding of an element
# $1 --> element to be encoded (input)
# $2 --> type of element (input)
function get_encoding {
	local element=$1
	local type_=$2

	# Try getting a dictionary-based encoding
	local res=$(get_dictionary $element)

	# If no dictionary-based encoding was found, encode the underscores
	if [ -z "$res" ]; then
		numUnderscores=$(grep -o _ <<< $element | wc -l)

		if [ "$type_" == "database" ]; then
			if [ $numUnderscores -eq 0 ]; then
				res=$element
			elif [ "$force" == "true" ]; then
				res=$(echo "$path" | sed -r 's/[_]+/xffff/g')
			else
				res="null"
			fi
		elif [ "$type_" == "table" ]; then
			if [ $numUnderscores -eq 2 ]; then
				res=$element
			elif [ "$force" == "true" ]; then
				res=$(echo "$path" | sed -r 's/[_]+/xffff/g')
			else
				res="null"
			fi
		else
			echo "Unknown type '$type_'"
		fi
	fi

	echo $res
}

# Function to process a table
# $1 --> table to be processed (input)
# $2 --> original database (input)
# $3 --> encoded database (input)
function process_table {
	local table=$1
	local originalDB=$2
	local encodedDB=$3

	# Get the encoding of the given table
	encoding=$(get_encoding $table "table")

	if [ "$encoding" == "null" ]; then
		echo -e "   [tbl] ${BLUE}$originalDB.$table${NOCOLOR} -> ${RED}$table${NOCOLOR}"
	elif [ "$encoding" == "$name" ]; then
		echo -e "   [tbl] ${BLUE}$originalDB.$table${NOCOLOR} -> $encodedDB.$encoding"
	else
		if [ "$function" == "copy_encode" ]; then
			if [ "$originalDB" == "$encodedDB" ]; then
				mysql -u $user -p$password -e "USE $originalDB; CREATE TABLE $encoding LIKE $table"
				mysql -u $user -p$password -e "USE $originalDB; INSERT INTO $encoding SELECT * FROM $table"
			else
				mysql -u $user -p$password -e "USE $encodedDB; RENAME TABLE $table TO $encoding"
			fi
		elif [ "$function" == "replace_encode" ]; then
			mysql -u $user -p$password -e "USE $encodedDB; RENAME TABLE $table TO $encoding"
		fi

		echo -e "   [tbl] ${BLUE}$originalDB.$table${NOCOLOR} -> ${GREEN}$encodedDB.$encoding${NOCOLOR}"
	fi
}

# Function to iterate all the tables given a database
# $1 --> original database (input)
# $2 --> encoded database (input)
function iterate_tables {
	local originalDB=$1
	local encodedDB=$2

	while read -r table; do
		process_table $table $originalDB $encodedDB
	done < <(mysql -u $user -p$password -e "USE $originalDB; SHOW TABLES" | grep -v Tables_in)
}

# Function to process a database
# $1 --> database to be processed (input)
function process_database {
	local database=$1

	# Get the encoding of the given database
	encoding=$(get_encoding $database "database")

	if [ "$encoding" == "null" ]; then
		echo -e "[db] ${BLUE}$database${NOCOLOR} -> ${RED}$database${NOCOLOR}"
		iterate_tables $database $database
	elif [ "$encoding" == "$database" ]; then
		echo -e "[db] ${BLUE}$database${NOCOLOR} -> $encoding"
		iterate_tables $database $database
	else
		echo -e "[db] ${BLUE}$database${NOCOLOR} -> ${GREEN}$encoding${NOCOLOR}"

		if [ "$function" == "analyze" ]; then
			iterate_tables $database $encoding
		elif [ "$function" == "copy_encode" ]; then
			mysql -u $user -p$password -e "CREATE DATABASE IF NOT EXISTS $encoding"
			tables=$(mysql -u $user -p$password -e "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE table_schema='$database'")

			for name in $tables; do
				mysql -u $user -p$password -e "CREATE TABLE $encoding.$name LIKE $database.$name"
				mysql -u $user -p$password -e "INSERT INTO $encoding.$name SELECT * FROM $database.$name"
			done

			iterate_tables $database $encoding
		elif [ "$function" == "replace_encode" ]; then
			# Renaming a database is not possible. The only possibility is to create a database with the new name,
			# to rename the tables (database, not table name itself) and to drop the old database
			mysql -u $user -p$password -e "CREATE DATABASE IF NOT EXISTS $encoding"
			tables=$(mysql -u $user -p$password -e "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE table_schema='$database'")

			for name in $tables; do
				mysql -u $user -p$password -e "RENAME TABLE $database.$name to $encoding.$name"
			done

			iterate_tables $database $encoding
			mysql -u $user -p$password -e "DROP DATABASE $database"
		fi
	fi
}

# Function to iterate all the databases
function iterate_databases {
	while read -r database; do
		process_database $database
	done < <(mysql -u $user -p$password -e "SHOW DATABASES" | grep -v Database)
}

# Main function
load_dictionary

if [ "$database" == "ALL" ]; then
	iterate_databases
else
	exists=$(mysql -u $user -p$password -e "SHOW DATABASES LIKE '$database'")

	if [ "$exists" == "" ]; then
		echo "The database does not exist"
	else
		process_database $database
	fi
fi
