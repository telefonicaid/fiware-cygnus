#!/bin/sh
# Copyright 2016 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus.
#
# fiware-cygnus is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# fiware-cygnus is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
# General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with fiware-cygnus. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# francisco.romerobueno@telefonica.com

# This script is aimed to Cygnus debugging.

URL=$1

if [ "$2" != "" ]
then
   SERVICE=$2
else
   SERVICE=default
fi

if [ "$3" != "" ]
then
   SERVICE_PATH=$3
else
   SERVICE_PATH=/
fi

if [ "$4" != "" ]
then
   SERVICE_PATH2=$4
else
   SERVICE_PATH2=/
fi

curl $URL -v -s -S --header 'Content-Type: application/json; charset=utf-8' --header 'Accept: application/json' --header 'User-Agent: orion/0.10.0' --header "Fiware-Service: $SERVICE" --header "Fiware-ServicePath: $SERVICE_PATH,$SERVICE_PATH2" --header "ngsiv2-attrsformat: normalized" -d @- <<EOF
{
  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
  "data" : [
    {
      "temperature" : {
          "type" : "centigrade",
          "value" : "26.5",
          "metadata": {}
        },
      "type" : "Room",
      "id" : "Room.1"
    },
    {
      "temperature" : {
          "type" : "centigrade",
          "value" : "19.3",
          "metadata": {}
        },
      "type" : "Room",
      "id" : "Room.suite"
    }
  ]
}
EOF
