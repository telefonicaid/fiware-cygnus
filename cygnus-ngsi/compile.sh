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

# Show the usage
if [ $# -ne 2 ]; then
	echo "Usage:"
	echo "      compile.sh <version> <flume_base_path>"
	exit 1
fi

# Input parameters
version=$1
flumeBasePath=$2

# Export some Maven options
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"

# Compile
mvn clean compile assembly:single exec:exec

# Install
cp target/cygnus-ngsi-$version-jar-with-dependencies.jar $flumeBasePath/plugins.d/cygnus/lib/
