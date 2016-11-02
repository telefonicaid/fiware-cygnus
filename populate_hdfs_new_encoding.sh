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

# Show the usage
if [ $# -ne 1 ]; then
	echo "Usage:"
	echo "      populate_hdfs_new_encoding.sh <basepath>"
	echo "Where:"
	echo "      <basepath> is the base path where to populate"
	echo "      Warning: before populating, this path will be deleted, if existing!"
	exit 1
fi

# Input parameters
$basepath=$1

sudo -u hdfs hadoop fs -rm -r -skipTrash $basepath
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc/
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc/svcpath/
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc/svcpath/room1_room/
sudo -u hdfs hadoop fs -touchz /user/frb/encoding/svc/svcpath/room1_room/room1_room.txt
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc/svcpath/room_2_room/
sudo -u hdfs hadoop fs -touchz /user/frb/encoding/svc/svcpath/room_2_room/room_2_room.txt
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc/svc_path/
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc/svc_path/room3_room/
sudo -u hdfs hadoop fs -touchz /user/frb/encoding/svc/svc_path/room3_room/room3_room.txt
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc_other/
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc_other/svcpath/
sudo -u hdfs hadoop fs -mkdir /user/frb/encoding/svc_other/svcpath/room4_room/
sudo -u hdfs hadoop fs -touchz /user/frb/encoding/svc_other/svcpath/room4_room/room4_room.txt
