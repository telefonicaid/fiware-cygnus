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
if [ $# -ne 5 ]; then
	echo "Usage:"
	echo "      cygnus_translator_pre1.3.0_to_1.3.0_hdfs.sh <function> <force> <basepath> <dictionary> <user>"
	echo "Where:"
	echo "      <function> may be 'analyze', 'copy_encode' or 'replace_encode'"
	echo "      <force> may be 'true' or 'false'"
	echo "      <basepath> is the base path where to start analyzing/encoding"
	echo "      <dictionaly> is a file containing encodings this script is not able to do by itself"
	echo "      <user> is a HDFS superuser"
	exit 1
fi

# Input parameters
function=$1
force=$2
basepath=$3
dictFile=$4
user=$5

# Check the function value
if [ "$function" != "analyze" ] && [ "$function" != "copy_encode" ] && [ "$function" != "replace_encode" ]; then
	echo "Unknown function value '$function', use 'analyze' 'copy_encode' or 'replace_encode' instead"
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

		if [ "$type_" == "service" ] || [ "$type_" == "service_path" ]; then
			if [ $numUnderscores -eq 0 ]; then
				res=$path
			elif [ "$force" == "true" ]; then
				res=$(echo "$path" | sed -r 's/[_]+/xffff/g')
			else
				res="null"
			fi
		elif [ "$type_" == "entity_dir" ] || [ "$type_" == "entity_file" ]; then
			if [ $numUnderscores -eq 1 ]; then
				res=$(echo "$path" | sed -r 's/[_]+/xffff/g')
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
# $1 --> path to be processed (input)
# $2 --> type of path (input)
# $3 --> function to be performed (input)
function process {
	local path=$1
	local type_=$2
	local funct=$3

	# Get the encoding of the given path
	dir="$(dirname $path)"
	name="$(basename $path)"
	encoding=$(get_encoding $name $type_)

	# Process
	if [ "$type_" == "service" ]; then
		if [ "$encoding" == "null" ]; then
			echo -e "[service] ${BLUE}$name${NOCOLOR} -> ${RED}$name${NOCOLOR}"
			iterate $path $type_ $funct
		elif [ "$encoding" == "$name" ]; then
			echo -e "[service] ${BLUE}$name${NOCOLOR} --> $encoding"
			iterate $path $type_ $funct
		else
			if [ "$funct" == "replace_encode" ]; then
				sudo -u $user hadoop fs -mv $path $dir/$encoding
				echo -e "[service] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				iterate $dir/$encoding $type_ $funct
			elif [ "$funct" == "copy_encode" ]; then
				sudo -u $user hadoop fs -mkdir $dir/$encoding
				sudo -u $user hadoop fs -cp $path/* $dir/$encoding/
				echo -e "[service] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				# From here on, everything must be replaced
				iterate $dir/$encoding $type_ "replace_encode"
			elif [ "$funct" == "analyze" ]; then
				echo -e "[service] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				iterate $path $type_ $funct
			else
				echo "Unknown function '$funct'"
			fi
		fi
	elif [ "$type_" == "service_path" ]; then
		if [ "$encoding" == "null" ]; then
			echo -e "   [service_path] ${BLUE}$name${NOCOLOR} -> ${RED}$name${NOCOLOR}"
			iterate $path $type_ $funct
		elif [ "$encoding" == "$name" ]; then
			echo -e "   [service_path] ${BLUE}$name${NOCOLOR} --> $encoding"
			iterate $path $type_ $funct
		else
			if [ "$funct" == "replace_encode" ]; then
				sudo -u $user hadoop fs -mv $path $dir/$encoding
				echo -e "   [service_path] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				iterate $dir/$encoding $type_ $funct
			elif [ "$funct" == "copy_encode" ]; then
				sudo -u $user hadoop fs -mkdir $dir/$encoding
				sudo -u $user hadoop fs -cp $path/* $dir/$encoding/
				echo -e "   [service_path] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				# From here on, everything must be replaced
				iterate $dir/$encoding $type_ "replace_encode"
			elif [ "$funct" == "analyze" ]; then
				echo -e "   [service_path] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				iterate $path $type_ $funct
			else
				echo "Unknown function '$funct'"
			fi
		fi
	elif [ "$type_" == "entity_dir" ]; then
		if [ "$encoding" == "null" ]; then
			echo -e "      [entity dir] ${BLUE}$name${NOCOLOR} -> ${RED}$name${NOCOLOR}"
			iterate $path $type_ $funct
		elif [ "$encoding" == "$name" ]; then
			echo -e "      [entity dir] ${BLUE}$name${NOCOLOR} --> $encoding"
			iterate $path $type_ $funct
		else
			if [ "$funct" == "replace_encode" ]; then
				sudo -u $user hadoop fs -mv $path $dir/$encoding
				echo -e "      [entity dir] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				iterate $dir/$encoding $type_ $funct
			elif [ "$funct" == "copy_encode" ]; then
				sudo -u $user hadoop fs -mkdir $dir/$encoding
				sudo -u $user hadoop fs -cp $path/* $dir/$encoding/
				echo -e "      [entity dir] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				# From here on, everything must be replaced
				iterate $dir/$encoding $type_ "replace_encode"
			elif [ "$funct" == "analyze" ]; then
				echo -e "      [entity dir] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
				iterate $path $type_
			else
				echo "Unknown function '$funct'"
			fi
		fi
	elif [ "$type_" == "entity_file" ]; then
		if [ "$encoding" == "null" ]; then
			echo -e "         [entity file] ${BLUE}$name${NOCOLOR} -> ${RED}$name${NOCOLOR}"
		elif [ "$encoding" == "$name" ]; then
			echo -e "         [entity file] ${BLUE}$name${NOCOLOR} --> $encoding"
		else
			if [ "$function" == "replace_encode" ]; then
				sudo -u $user hadoop fs -mv $path $dir/$encoding
			elif [ "$function" == "copy_encode" ]; then
				# In fact, this should be 'cp' and 'rm', but summarized as 'mv'
				sudo -u $user hadoop fs -mv $path $dir/$encoding
			fi

			echo -e "         [entity file] ${BLUE}$name${NOCOLOR} --> ${GREEN}$encoding${NOCOLOR}"
		fi
	else
		echo "Unknown type '$type_'"
	fi
}

# Function to iterate a path
# $1 --> path to be iterated (input)
# $2 --> type of path (input)
# $3 --> function to be performed (input)
function iterate {
	local path=$1
	local type_=$2
	local funct=$3

	while read -r subpath; do
		if [ "$type_" == "basepath" ]; then
			process $subpath "service" $funct
		elif [ "$type_" == "service" ]; then
			process $subpath "service_path" $funct
		elif [ "$type_" == "service_path" ]; then
			process $subpath "entity_dir" $funct
		elif [ "$type_" == "entity_dir" ]; then
			process $subpath "entity_file" $funct
		fi
	done < <(sudo -u $user hadoop fs -ls $path | grep -v Found | awk '{print $8}')
}

# Main function
load_dictionary
iterate $basepath "basepath" $function
