#################################################################################
# Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
################################################################################


#!/bin/bash -x

JUST_BASH=false

if $JUST_BASH; then
###############################################################################
#
#  I just want to get inside the machine
#  Set the variable above `JUST_BASH` to true
#
###############################################################################

bash


else
###############################################################################
#
#  Compile
#
#  If for any reason this script fails, you keep your dependencies
#  in the folder /tmp/maven-deps (which is ignored in git, so don't worry)
#
###############################################################################

mkdir -p /cygnus-compiler/maven-deps >/dev/null
/tmp/maven/bin/mvn clean compile assembly:single


fi
