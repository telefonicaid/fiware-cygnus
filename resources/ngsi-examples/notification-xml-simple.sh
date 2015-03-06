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

curl $URL -v -s -S --header 'Content-Type: application/xml' --header 'Accept: application/xml' --header 'User-Agent: orion/0.10.0' --header "Fiware-Service: $SERVICE" --header "Fiware-ServicePath: $SERVICE_PATH" -d @- <<EOF
<notifyContextRequest>
  <subscriptionId>51c0ac9ed714fb3b37d7d5a8</subscriptionId>
  <originator>localhost</originator>
  <contextResponseList>
    <contextElementResponse>
      <contextElement>
        <entityId type="Room" isPattern="false">
          <id>Room1</id>
        </entityId>
        <contextAttributeList>
          <contextAttribute>
            <name>temperature</name>
            <type>centigrade</type>
            <contextValue>26.5</contextValue>
          </contextAttribute>
        </contextAttributeList>
      </contextElement>
      <statusCode>
        <code>200</code>
        <reasonPhrase>OK</reasonPhrase>
      </statusCode>
    </contextElementResponse>
  </contextResponseList>
</notifyContextRequest>
EOF
