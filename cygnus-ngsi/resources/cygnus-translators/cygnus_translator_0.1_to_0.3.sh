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

# This script is aimed to translate the already persisted context data from
# ngsi2cosmos and Cygnus 0.1 format (CSV-like) to Cygnus 0.2 format (Json).
# It must be run in the NameNode of the Hadoop cluster where the data is stored.
# Translation is done by downloading, file by file, the HDFS source folder
# content into a local file located at /tmp. Then a new local file is created
# at /tmp as well with the translated content, which is finally uploaded to the
# HDFS destination folder as a temporal file. All the temporal HDFS files
# regarding a common entity are finally merged. The usage of /tmp instead of
# the RAM memory is justified due to the expected large size of the HDFS files.

# show the usage
if [ $# -ne 4 ]; then
	echo "Usage: cygnus-translator.sh <HDFS_user> <prefix_name> <src_HDFS_directory> <dst_HDFS_directory>"
	echo "       (If you want an empty <prefix_name>, please use \"\")"
	exit 1;
fi

# input parameters
hdfsUser=$1
prefixName=$2
srcHDFSFolder=$3
dstHDFSFolder=$4

# check if the current user matches the given one; if matches, no superuser privileges are need
superuser=0

if [ "$USER" != "$hdfsUser" ]; then
        echo "You ($USER) are trying to impersonate another user ($hdfsUser)"

	# check if the current user is sudoer
        if [[ $(sudo -v | grep 'Sorry') == 0 ]]; then
                superuser=1
        else
                exit 1;
        fi
fi

# check if the script is being run in the Hadoop cluster NameNode
if ! [[ $(ps -ef | grep NameNode | grep -v grep) ]]; then
        echo "This script has been designed for executing in a Hadoop cluster NameNode!"
        exit 1;
fi

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

	if [ $superuser -eq 1 ]; then
		sudo -u $hdfsUser hadoop fs -mkdir $dstHDFSFolder
	else
		hadoop fs -mkdir $dstHDFSFolder
	fi
fi

# create a local temporary folder
if [ $superuser -eq 1 ]; then
	tmpDir=$(sudo -u $hdfsUser mktemp -d /tmp/cygnus.XXXX)
else
	tmpDir=$(mktemp -d /tmp/cygnus.XXXX)
fi

echo "Creating $tmpDir as working directory"

# declare a variable that will be used to store unique final HDFS files
declare -a dstHDFSFileNames=()

# file counter
index=0

# iterate on the HDFS files within the source HDFS directory
while read -r lsLine; do
	# get the HDFS file size and path
	IFS=' ' read -ra array <<< "${lsLine}"
	hdfsFileSize="${array[0]}"
	hdfsFilePath="${array[1]}"

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

        # get the entityId and the entityType
        IFS='-' read -ra array <<< "${hdfsFileName}"
        entityId="${array[0]}"
        entityType="${array[1]}"

	# create a temporary output file within the temporary working folder
	if [ $superuser -eq 1 ]; then
		tmpOutput=$(sudo -u $hdfsUser mktemp $tmpDir/output.XXXX)
	else
		tmpOutput=$(mktemp $tmpDir/output.XXXX)
	fi

	# translate line by line, each file; use a temporary destination file
	echo -n "Translating into $tmpOutput"

	while IFS='|' read -ra array; do
		jsonLine="{\"recvTimeTs\":\"${array[1]}\", \
			\"recvTime\":\"${array[0]}\", \
			\"entityId\":\"${array[2]}\", \
			\"entityType\":\"${array[3]}\", \
			\"attrName\":\"${array[4]}\", \
			\"attrType\":\"${array[5]}\", \
			\"attrValue\":\"${array[6]}\"}"
		echo $jsonLine >> $tmpOutput
	done < $tmpDir/$hdfsFileName

	echo " [DONE]"

	# copy the translated file to HDFS as a temporal file to be merged at the end
	translatedFileSize=$(ls -la $tmpOutput | awk '{ print $5 }')
	echo -n "Writing hdfs://localhost$dstHDFSFolder/$prefixName$entityId-$entityType.$index.tmp ($translatedFileSize bytes)"

	if [ $superuser -eq 1 ]; then
		sudo -u $hdfsUser hadoop fs -put $tmpOutput $dstHDFSFolder/$prefixName$entityId-$entityType.$index.tmp
	else
		hadoop fs -put $tmpOutput $dstHDFSFolder/$prefixName$entityId-$entityType.$index.tmp
	fi

	echo " [DONE]"

	# delete all the temporary files
	rm $tmpDir/$hdfsFileName
	rm $tmpOutput

	# store the final HDFS name, if not stored yet
        if ! [[ $(echo "${dstHDFSFileNames[@]}" | grep $prefixName$entityId-$entityType) ]]; then
                dstHDFSFileNames[index]=$prefixName$entityId-$entityType
        fi

        # update the file counter
        index=$((index+1))
done < <(hadoop fs -ls $srcHDFSFolder | grep -v Found | awk '{ print $5" "$8 }')

# iterate on the unique final HDFS files in order to merge all the tmp files related with them
for i in "${dstHDFSFileNames[@]}"; do
        echo "Merging $dstHDFSFolder/$i.*.tmp into $dstHDFSFolder/$i/$i.txt"

	if [ $superuser -eq 1 ]; then
		sudo -u $hdfsUser hadoop fs -mkdir $dstHDFSFolder/$i
        	sudo -u $hdfsUser hadoop fs -cat $dstHDFSFolder/$i.*.tmp | sudo -u $hdfsUser hadoop fs -put - $dstHDFSFolder/$i/$i.txt
	else
		hadoop fs -mkdir $dstHDFSFolder/$i
                hadoop fs -cat $dstHDFSFolder/$i.*.tmp | hadoop fs -put - $dstHDFSFolder/$i/$i.txt
	fi
done

# delete all the tmp files in the destination HDFS folder
if [ $superuser -eq 1 ]; then
	sudo -u $hdfsUser hadoop fs -rmr $dstHDFSFolder/*.tmp
else
	hadoop fs -rmr $dstHDFSFolder/*.tmp
fi

# delete the local temporary folder (should not be necessary since the folder
# seems to be deleted automatically)
rm -r $tmpDir
