#!/bin/sh
# Copyright 2015 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus.
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

URL=$1

if [ "$2" != "" ]
then
   SERVICE=$2
else
   SERVICE=def_serv
fi

if [ "$3" != "" ]
then
   SERVICE_PATH=$3
else
   SERVICE_PATH=def_serv_path
fi

curl $URL -v -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'User-Agent: orion/0.10.0' --header "Fiware-Service: $SERVICE" --header "Fiware-ServicePath: $SERVICE_PATH" -d @- <<EOF
{
  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
  "originator" : "localhost",
  "contextResponses" : [
    {
      "contextElement" : {
        "id": "netrisModel",
        "isPattern": "false",
        "type": "model",
        "attributes": [
          {
            "name": "TimeInstant",
            "type": "ISO8601",
            "value": "2014-03-20T14:21:14Z"
          },
          {
            "name": "ModelProp1",
            "type": "String",
            "value": "abc"
          },
          {
            "name": "ModelProp2",
            "type": "String",
            "value": "def"
          },
          {
            "name": "et",
            "type": "compound",
            "value":
              {
                "name": "external temperature",
                "phenomenon": "temperature",
      	        "type": "Quantity",
                "uom": "Cel",
                "persistence": [ "sth", "ckan" ]
              }
          },
          {
            "name": "internal temperature",
            "type": "compound",
            "value": [
              {
                "name": "internal temperature",
                "phenomenon": "temperature",
                "type": "Quantity",
                "uom": "Cel"
              }
            ]
          },
          {
            "name": "h",
            "type": "compound",
            "value": [
              {
                "name": "house humidity",
                "phenomenon": "humidity",
      	        "type": "Quantity",
                "uom": "%"
              }
            ]
          },
          {
            "name": "reset",
            "type": "compound",
            "value": [
              {
                "type": "action",
                "input_params": "reqTimeInstant,correlatorID,param_a,param_b",
                "output_params": "result,resTimeIntant,param_c,param_d"
              }
            ]
          },
          {
            "name": "color",
            "type": "compound",
            "value": [
              {
                "type": "action",
                "input_params": "reqTimeInstant,correlatorID",
                "output_params": "result,resTimeInstant"
              }
            ]
          }
        ]
      },
      "statusCode" : {
        "code" : "200",
        "reasonPhrase" : "OK"
      }
    }
  ]
}
EOF
