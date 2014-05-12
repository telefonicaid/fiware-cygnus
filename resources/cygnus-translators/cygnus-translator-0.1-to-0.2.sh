#! /bin/sh
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
# frb@tid.es

# This script is aimed to translate the already persisted context data from
# ngsi2cosmos and Cygnus 0.1 format (CSV-like) to Cygnus 0.2 format (Json).
# Translation is done by downloading, file by file, the HDFS source folder
# content into a local file located at /tmp. Then a new local file is created
# at /tmp as well with the translated content, which is finally uploaded to the
# HDFS destination folder. The usage of /tmp instead of the RAM memory is
# justified due to the expected large size of the HDFS files.

# show the usage
if [ $# -ne 3 ]; then
	echo "Usage: cygnus-translator.sh <HDFS_user> <src_HDFS_directory> <dst_HDFS_directory>";
	exit 1;
fi

# input parameters
hdfsUser=$1
srcHDFSFolder=$2
dstHDFSFolder=$3

# create the destination HDFS folder
if hadoop fs -test -d $dstHDFSFolder; then
	length=$(hadoop fs -ls $dstHDFSFolder | wc -l)

	if [ $length -gt 0 ]; then
		echo "hdfs://locahost$dstHDFSFolder already exists and is not empty!"
		exit 1;
	else
		echo "hdfs://localhost$dstHDFSFolder exists but is empty"
	fi
else
	echo "Creating hdfs://localhost$dstHDFSFolder"
	sudo -u $hdfsUser hadoop fs -mkdir $dstHDFSFolder
fi

# create a local temporary folder
tmpDir=$(sudo -u $hdfsUser mktemp -d /tmp/cygnus.XXXX)
echo "Creating $tmpDir as working directory"

# iterate on the HDFS files within the source HDFS directory
hadoop fs -ls $srcHDFSFolder | grep -v Found | awk '{ print $5" "$8 }' | while read -r lsLine; do
	# get the HDFS file size and path
	IFS=' ' read -ra ADDR
	hdfsFileSize="${ADDR[0]}"
	hdfsFilePath="${ADDR[1]}"

	# check if the file may be allocated in /tmp
	available=$(df -k /tmp | grep -v Filesystem | awk '{ print $4 }')

	if [ $available -lt $hdfsFileSize ]; then
		echo "Not enough local disk space for allocating the HDFS file, it will not be translated!"
		continue
	fi

	# copy the content of the HDFS file into the temporary folder
	echo -n "Reading hdfs://localhost$hdfsFilePath ($hdfsFileSize bytes)"
	hadoop fs -get $hdfsFilePath $tmpDir/
	echo " [DONE]"

	# get the file name
	hdfsFileName=${hdfsFilePath##*/}

	# create a temporary output file within the temporary working folder
	tmpOutput=$(sudo -u $hdfsUser mktemp $tmpDir/output.XXXX)

	# translate line by line, each file; use a temporary destination file
	echo -n "Translating into $tmpOutput"

	while IFS='|' read -ra ADDR; do
		jsonLine="{\"ts\":\"${ADDR[1]}\", \
			\"iso8601date\":\"${ADDR[0]}\", \
			\"entityId\":\"${ADDR[2]}\", \
			\"entityType\":\"${ADDR[3]}\", \
			\"attrName\":\"${ADDR[4]}\", \
			\"attrType\":\"${ADDR[5]}\", \
			\"attrValue\":\"${ADDR[6]}\"}"
		echo $jsonLine >> $tmpOutput
	done < $tmpDir/$hdfsFileName

	echo " [DONE]"

	# copy the translated file to HDFS
	translatedFileSize=$(ls -la $tmpOutput | awk '{ print $5 }')
	echo -n "Writing hdfs://localhost$dstHDFSFolder/$hdfsFileName ($translatedFileSize bytes)"
	sudo -u $hdfsUser hadoop fs -put $tmpOutput $dstHDFSFolder/$hdfsFileName
	echo " [DONE]"

	# delete all the temporary files
	rm $tmpDir/$hdfsFileName
	rm $tmpOutput
done

# delete the local temporary folder (should not be necessary since the folder
#seems to be deleted automatically)
rm -r $tmpDir
