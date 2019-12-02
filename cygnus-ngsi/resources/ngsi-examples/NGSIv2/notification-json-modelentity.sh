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

curl $URL -v -s -S --header 'Content-Type: application/json; charset=utf-8' --header 'Accept: application/json' --header 'User-Agent: orion/0.10.0' --header "Fiware-Service: $SERVICE" --header "Fiware-ServicePath: $SERVICE_PATH" --header "ngsiv2-attrsformat: normalized" -d @- <<EOF
{
  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
  "data" : [
    {
      "id": "netrisModel",
      "type": "model",
      "TimeInstant": {
          "type": "ISO8601",
          "value": "2014-03-20T14:21:14Z",
          "metadata": {}
        },
      "ModelProp1": {
        "type": "String",
        "value": "abc",
        "metadata": {}
      },
      "ModelProp2": {
        "type": "String",
        "value": "def",
        "metadata": {}
      },
      "et": {
        "type": "compound",
        "value":
          {
            "name": "external temperature",
            "phenomenon": "temperature",
            "type": "Quantity",
            "uom": "Cel",
            "persistence": [ "sth", "ckan" ]
          },
        "metadata": {}
      },
      "internal temperature": {
        "type": "compound",
        "value": [
          {
            "name": "internal temperature",
            "phenomenon": "temperature",
            "type": "Quantity",
            "uom": "Cel"
          }
        ],
        "metadata": {}
      },
      "h": {
        "type": "compound",
        "value": [
          {
            "name": "house humidity",
            "phenomenon": "humidity",
            "type": "Quantity",
            "uom": "%"
          }
        ],
        "metadata": {}
      },
      "reset": {
        "type": "compound",
        "value": [
          {
            "type": "action",
            "input_params": "reqTimeInstant,correlatorID,param_a,param_b",
            "output_params": "result,resTimeIntant,param_c,param_d"
          }
        ],
        "metadata": {}
      },
      "color": {
        "type": "compound",
        "value": [
          {
            "type": "action",
            "input_params": "reqTimeInstant,correlatorID",
            "output_params": "result,resTimeInstant"
          }
        ],
        "metadata": {}
      }
    }
  ]
}
EOF
