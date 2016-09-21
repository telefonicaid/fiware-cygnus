#! /bin/bash
#
# Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FI-WARE project).
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
if [ $# -ne 4 ]; then
	echo "Usage:"
	echo "      cygnus_translator_pre1.3.0_to_1.3.0_hdfs.sh <function> <force> <basepath> <dictionary>"
	echo "Where:"
	echo "      <function> may be 'analyze' or 'encode'"
	echo "      <force> may be 'true' or 'false'"
	echo "      <basepath> is the base path where to start analyzing/encoding"
	echo "      <dictionaly> is a file containing encodings this script is not able to do by itself"
	exit 1
fi

# Input parameters
function=$1
force=$2
basepath=$3
dictFile=$4

# Check the function value
if [ "$function" != "analyze" ] && [ "$function" != "encode" ]; then
	echo "Unknown function value '$function', use 'analyze' or 'encode' instead"
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

# Function to get the encoding of a path
# $1 --> path to be encoded (input)
# $2 --> type of path (input)
function get_encoding {
	local path=$1
	local type_=$2

	# Try getting a dictionary-based encoding
	local res=$(get_dictionary $path)

	# If no dictionary-based encoding was found, encode the underscores
	if [ -z "$res" ]; then
		numUnderscores=$(grep -o _ <<< $path | wc -l)

		if [ "$type_" == "service" ] || [ "$type_" == "subservice" ]; then
			if [ $numUnderscores -eq 0 ]; then
				res=$path
			elif [ "$force" == "true" ]; then
				res=$(echo "$path" | sed -r 's/[_]+/xffff/g')
			else
				res="null"
			fi
		elif [ "$type_" == "entity_dir" ] || [ "$type_" == "entity_file" ]; then
			if [ $numUnderscores -eq 1 ]; then
                                res=$path
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

# Function to process a path
# $1 --> function to be done (input)
# $2 --> path to be processed (input)
# $3 --> type of path (input)
function process {
	local func=$1
        local path=$2
        local type_=$3

	# Get the encoding of the given path
	dir="$(dirname $path)"
        name="$(basename $path)"
	encoding=$(get_encoding $name $type_)

	# Some pretty printigs
        if [ "$type_" == "service" ]; then
                if [ "$encoding" == "null" ]; then
			echo -e " |    |__ ${BLUE}$name${NOCOLOR} -> ${RED}unable${NOCOLOR}"
		elif [ "$encoding" == "$name" ]; then
			echo -e " |    |__ ${BLUE}$name${NOCOLOR} --> $encoding"
		else
			hadoop fs -mv $path $dir/$encoding
			echo -e " |    |__ ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
                fi
        elif [ "$type_" == "subservice" ]; then
                if [ "$encoding" == "null" ]; then
			echo -e " |    |    |__ ${BLUE}$name${NOCOLOR} -> ${RED}unable${NOCOLOR}"
                elif [ "$encoding" == "$name" ]; then
                        echo -e " |    |    |__ ${BLUE}$name${NOCOLOR} --> $encoding"
		else
			hadoop fs -mv $path $dir/$encoding
                        echo -e " |    |    |__ ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
                fi
        elif [ "$type_" == "entity_dir" ]; then
                if [ "$encoding" == "null" ]; then
			echo -e " |    |    |    |__ ${BLUE}$name${NOCOLOR} -> ${RED}unable${NOCOLOR}"
		elif [ "$encoding" == "$name" ]; then
                        echo -e " |    |    |    |__ ${BLUE}$name${NOCOLOR} --> $encoding"
                else
			hadoop fs -mv $path $dir/$encoding
                        echo -e " |    |    |    |__ ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
                fi
        elif [ "$type_" == "entity_file" ]; then
                if [ "$encoding" == "null" ]; then
			echo -e " |    |    |    |    |__ ${BLUE}$name${NOCOLOR} -> ${RED}unable${NOCOLOR}"
		elif [ "$encoding" == "$name" ]; then
                        echo -e " |    |    |    |    |__ ${BLUE}$name${NOCOLOR} --> $encoding"
                else
			hadoop fs -mv $path $dir/$encoding
                        echo -e " |    |    |    |    |__ ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
                fi
        else
                echo "Unknown type '$type_'"
        fi
}

# Function to iterate a path
# $1 --> path to be processed (input)
# $2 --> type of path (input)
function iterate {
	local path=$1
	local type_=$2
	local subpath
	local subtype

	# Some pretty pritings
	if [ "$type_" == "basepath" ]; then
		echo "Entering $path ($type_)"
	elif [ "$type_" == "service" ]; then
		echo " |   Entering $path ($type_)"
        elif [ "$type_" == "subservice" ]; then
		echo " |    |   Entering $path ($type_)"
        elif [ "$type_" == "entity_dir" ]; then
		echo " |    |    |   Entering $path ($type_)"
        elif [ "$type_" == "entity_file" ]; then
		echo " |    |    |    |   Entering $path ($type_)"
        else
                echo "Unknown type '$type_'"
        fi

	# Iterate on the subpaths only if the current path is not an entity file
	if [ "$type_" != "entity_file" ]; then
		while read -r subpath; do
			dirOrFile=$(hadoop fs -ls $path |grep $subpath |awk '{print $1}' |head -c 1)

			if [ "$dirOrFile" == "d" ]; then
				if [ "$type_" == "basepath" ]; then
					subtype="service"
				elif [ "$type_" == "service" ]; then
					subtype="subservice"
				elif [ "$type_" == "subservice" ]; then
					subtype="entity_dir"
				fi
			else
				subtype="entity_file"
			fi

			iterate $subpath $subtype
	  	done < <(hadoop fs -ls $path | grep -v Found | awk '{print $8}')
	fi

	# Analyze the current path
	if [ "$type_" != "basepath" ]; then
		process $function $path $type_
	fi
}

# Main function
load_dictionary
iterate $basepath "basepath"
