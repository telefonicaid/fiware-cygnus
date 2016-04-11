#!/bin/sh
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
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
	"subscriptionId": "56ebd67673a3713d25fcccb6",
	"originator": "localhost",
	"contextResponses": [{
		"contextElement": {
			"type": "thing",
			"isPattern": "false",
			"id": "thing:edisonFran",
			"attributes": [{
				"name": "TimeInstant",
				"type": "ISO8601",
				"value": "2016-03-29T13:20:04.131490Z"
			}, {
				"name": "h",
				"type": "string",
				"value": "48.8",
				"metadatas": [{
					"name": "TimeInstant",
					"type": "ISO8601",
					"value": "2016-03-29T13:20:04.131974Z"
				}]
			}, {
				"name": "humidity",
				"type": "string",
				"value": "68.6",
				"metadatas": [{
					"name": "TimeInstant",
					"type": "ISO8601",
					"value": "2016-03-18T10:21:48.722010Z"
				}]
			}, {
				"name": "r",
				"type": "string",
				"value": "false",
				"metadatas": [{
					"name": "TimeInstant",
					"type": "ISO8601",
					"value": "2016-03-22T14:24:04.132386Z"
				}]
			}, {
				"name": "t",
				"type": "string",
				"value": "24.2",
				"metadatas": [{
					"name": "TimeInstant",
					"type": "ISO8601",
					"value": "2016-03-22T14:24:04.131490Z"
				}]
			}, {
				"name": "temperature",
				"type": "string",
				"value": "62.4",
				"metadatas": [{
					"name": "TimeInstant",
					"type": "ISO8601",
					"value": "2016-03-18T10:21:48.720730Z"
				}]
			}]
		},
		"statusCode": {
			"code": "200",
			"reasonPhrase": "OK"
		}
	}]
}
EOF
