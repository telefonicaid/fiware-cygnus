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
sudo -u $hdfsUser hadoop fs -mkdir $dstHDFSFolder

# HDFS content is temporarily donwloaded to /tmp/cygnus (we cannot rely on RAM memory
# since the HDFS files may be very large), check wether it that folder exists or not
# in the local file system
if [ -d /tmp/cygnus ]; then
        rm -rf /tmp/cygnus/*.tmp
        rm -rf /tmp/cygnus/*.txt
else
        mkdir /tmp/cygnus
fi

# iterate on the HDFS files within the source HDFS directory
hadoop fs -ls $srcHDFSFolder | grep -v Found | awk '{ print $8 }' | while read -r hdfsFile; do
        echo "processing $hdfsFile"

        # copy the content of the source HDFS file into a temporary source file
        hadoop fs -get $hdfsFile /tmp/cygnus/cygnus-translator-input.tmp

	# get the file name
	file=${hdfsFile##*/}

	# translate line by line, each file; use a temporary destination file
	while IFS='|' read -ra ADDR; do
		jsonLine="{\"ts\":\"${ADDR[1]}\", \
			\"iso8601date\":\"${ADDR[0]}\", \
			\"entityId\":\"${ADDR[2]}\", \
			\"entityType\":\"${ADDR[3]}\", \
			\"attrName\":\"${ADDR[4]}\", \
			\"attrType\":\"${ADDR[5]}\", \
			\"attrValue\":\"${ADDR[6]}\"}"
                echo $jsonLine >> /tmp/cygnus/cygnus-translator-output.tmp
        done < /tmp/cygnus/cygnus-translator-input.tmp

	# copy the translated file to HDFS
	sudo -u $hdfsUser hadoop fs -put /tmp/cygnus/cygnus-translator-output.tmp $dstHDFSFolder/$file

	# delete all the temporary files
        rm /tmp/cygnus/cygnus-translator-input.tmp
	rm /tmp/cygnus/cygnus-translator-output.tmp
done

