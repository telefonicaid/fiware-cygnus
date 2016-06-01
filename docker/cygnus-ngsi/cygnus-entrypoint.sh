#!/bin/bash
#
# Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FI-WARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
# General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
# option) any later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
#

# Change the MySQL user and password in the agent configuration file
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username = '${CYGNUS_MYSQL_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password = '${CYGNUS_MYSQL_PASS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/agent.conf

# Run the Cygnus command
${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${CYGNUS_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p ${CYGNUS_API_PORT} -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER}
