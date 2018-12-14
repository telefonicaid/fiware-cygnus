#! /bin/bash
#
# Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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
if [ $# -ne 4 ]; then
	echo "Usage: cygnus_translator_pre0.10.0_to_0.10.0_hdfs.sh hdfs_folder file_format null_value backup"
	echo "where hdfs_folder: a valid HDFS folder path"
	echo "      file_format: a value within json-row|json-column|csv-row|csv-column"
	echo "      null_value : string to be inserted as null value, use the keyword \"empty\" for an empty value"
	echo "      backup     : either true or false"
	exit 1;
fi

# Input parameters
rootFolder=$1
fileFormat=$2
null_value=$3
backup=$4

# Check for the empty value
if [ "$null_value" == "empty" ]; then
        null_value=""
fi

# Function to get the nth ocurrence of a character within a string
# $1 --> string
# $2 --> character
# $3 --> position
function nth_index_of {
	local string=$1
	local character=$2
	local position=$3
	local i=0
	local res=0

	while [ $i -lt $position ]; do
		index=$(expr index "$string" "$character")
		res=$(echo "$res+$index" | bc)
		string=$(echo ${string:$index})
		i=$(echo "$i+1" | bc)
	done

	echo $res
}

# Function to translate a file written in json-row format
# $1 --> file to be translated (input)
function translate_file_json_row {
	local file=$1
        local delimiter=","
        local tempFile=$(mktemp --tmpdir=./ cygnus.XXXX)

        while read -r jsonLine; do
                index=$(nth_index_of "$jsonLine" "$delimiter" 2)
                leftPart=$(echo ${jsonLine:0:$index})
                rightPart=$(echo ${jsonLine:$index})
                newJsonLine=$(echo $leftPart\"fiwareServicePath\":\"$null_value\",$rightPart)
                echo $newJsonLine >> $tempFile
        done < <(hadoop fs -cat $file)

        if [ "$backup" == "true" ]; then
                hadoop fs -cp $file $file.bak
        fi

        hadoop fs -rm $file > /dev/null
        hadoop fs -put $tempFile $file
        rm $tempFile
}

# Function to translate a file written in json-column format
# $1 --> file to be translated (input)
function translate_file_json_column {
	local file=$1
        local delimiter=","
        local tempFile=$(mktemp --tmpdir=./ cygnus.XXXX)

        while read -r jsonLine; do
                index=$(nth_index_of "$jsonLine" "$delimiter" 2)
                leftPart=$(echo ${jsonLine:0:$index})
                rightPart=$(echo ${jsonLine:$index})
                newJsonLine=$(echo $leftPart\"fiwareServicePath\":\"$null_value\",\"entityId\":\"$null_value\",\"entityType\":\"$null_value\",$rightPart)
                echo $newJsonLine >> $tempFile
        done < <(hadoop fs -cat $file)

        if [ "$backup" == "true" ]; then
                hadoop fs -cp $file $file.bak
        fi

        hadoop fs -rm $file > /dev/null
        hadoop fs -put $tempFile $file
        rm $tempFile
}

# Function to translate a file written in csv-row format
# $1 --> file to be translated (input)
function translate_file_csv_row {
	local file=$1
        local delimiter=","
        local tempFile=$(mktemp --tmpdir=./ cygnus.XXXX)

        while read -r jsonLine; do
                index=$(nth_index_of "$jsonLine" "$delimiter" 2)
                leftPart=$(echo ${jsonLine:0:$index})
                rightPart=$(echo ${jsonLine:$index})
                newJsonLine=$(echo $leftPart$null_value,$rightPart)
                echo $newJsonLine >> $tempFile
        done < <(hadoop fs -cat $file)

        if [ "$backup" == "true" ]; then
                hadoop fs -cp $file $file.bak
        fi

        hadoop fs -rm $file > /dev/null
        hadoop fs -put $tempFile $file
        rm $tempFile
}

# Function to translate a file written in csv-column format
# $1 --> file to be translated (input)
function translate_file_csv_column {
        local file=$1
        local delimiter=","
        local tempFile=$(mktemp --tmpdir=./ cygnus.XXXX)

        while read -r jsonLine; do
                index=$(nth_index_of "$jsonLine" "$delimiter" 2)
                leftPart=$(echo ${jsonLine:0:$index})
                rightPart=$(echo ${jsonLine:$index})
                newJsonLine=$(echo $leftPart$null_value,$null_value,$null_value,$rightPart)
                echo $newJsonLine >> $tempFile
        done < <(hadoop fs -cat $file)

        if [ "$backup" == "true" ]; then
                hadoop fs -cp $file $file.bak
        fi

        hadoop fs -rm $file > /dev/null
        hadoop fs -put $tempFile $file
        rm $tempFile
}

# Function to translate a folder
# $1 --> folder to be translated (input)
function translate_folder {
	local folder=$1

	while read -r entry; do
		if [ -d "$entry" ]; then
			translate_folder $entry
		else
			if [ "$fileFormat" == "json-row" ]; then
				translate_file_json_row $entry
			elif [ "$fileFormat" == "json-column" ]; then
				translate_file_json_column $entry
			elif [ "$fileFormat" == "csv-row" ]; then
				translate_file_csv_row $entry
			elif [ "$fileFormat" == "csv-column" ]; then
				translate_file_csv_column $entry
			else
				echo "Unknown file format: $fileFormat"
			fi
		fi
	done < <(hadoop fs -ls $folder | grep -v Found | awk '{ print $8}')
}

# Main function
translate_folder $rootFolder

