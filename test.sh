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

# Move to cygnus-common
cd cygnus-common
echo "<<<<<<<<<<<< Run cygnus-common tests >>>>>>>>>>>>"
# Run cygnus-common tests
mvn -q test
R1=$?

# Build and install cygnus-common, this is necessary because it is a dependency for cygnus-ngsi and others
mvn -q clean compile exec:exec assembly:single
VERSION=$(cat pom.xml | grep version | sed -n '1p' | sed -ne '/<version>/s#\s*<[^>]*>\s*##gp' | sed 's/ //g')
mvn -q install:install-file -Dfile=target/cygnus-common-$VERSION-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=$VERSION -Dpackaging=jar -DgeneratePom=true

# Move to cygnus-ngsi
cd ../cygnus-ngsi
echo "<<<<<<<<<<<< Run cygnus-ngsi tests >>>>>>>>>>>>"
# Run cygnus-ngsi tests
mvn -q test
R2=$?

# Move to cygnus-ngsi-ld
cd ../cygnus-ngsi-ld
echo "<<<<<<<<<<<< Run cygnus-ngsi-ld tests >>>>>>>>>>>>"
# Run cygnus-ngsi-ld tests
mvn -q test
R3=$?

# FIXME: cygnus-twitter test are not run by this script by I understand they should...

# Only if *all* R1, R2 and R3 are 0 the sum RT will be 0
RT = `expr $R1 + $R2 + $R3`
exit $RT
