#!/bin/bash
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus.
#
# fiware-cygnus  is free software: you can redistribute it and/or
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
# along with Orion Context Broker. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# frb@tid.es

# This script is aimed to Cygnus debugging. It uses one argument: the URL to which
# the notification will be sent

API_HOST=$1
API_KEY=$2
PKG_ID=$3
RES_NAME=$4

(curl -s -S -X POST http://${API_HOST}/api/3/action/resource_create -H "Authorization: ${API_KEY}" -d @- | python -mjson.tool) <<EOF
{
   "name": "$RES_NAME",
   "url": "http://foo.bar/$RES_NAME",
   "package_id": "${PKG_ID}"
}
EOF
