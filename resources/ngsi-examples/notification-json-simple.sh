#!/bin/sh
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

# This script is aimed to Cygnus debugging. It uses one argument: the URL to which
# the notification will be sent

curl $1 -v -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'User-Agent: orion/0.10.0' -d @- <<EOF
{
  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
  "originator" : "localhost",
  "contextResponses" : [
    {
      "contextElement" : {
        "attributes" : [
          {
            "name" : "temperature",
            "type" : "centigrade",
            "value" : "26.5"
          }
        ],
        "type" : "Room",
        "isPattern" : "false",
        "id" : "Room1"
      },
      "statusCode" : {
        "code" : "200",
        "reasonPhrase" : "OK"
      }
    }
  ]
}
EOF
