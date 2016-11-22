#!/bin/bash
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

# Unpack two big jar files
[[ -f ${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar.pack.gz ]] && unpack200 -r ${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar.pack.gz ${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar
[[ -f ${FLUME_HOME}/plugins.d/cygnus/lib/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar.pack.gz ]] && unpack200 -r ${FLUME_HOME}/plugins.d/cygnus/lib/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar.pack.gz ${FLUME_HOME}/plugins.d/cygnus/lib/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar

# Change parameters in the agent configuration file
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_host/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_host = '${CYGNUS_MYSQL_HOST} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_port/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_port = '${CYGNUS_MYSQL_PORT} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username = '${CYGNUS_MYSQL_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password = '${CYGNUS_MYSQL_PASS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_hosts/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_hosts = '${CYGNUS_MONGO_HOSTS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_hosts/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_hosts = '${CYGNUS_MONGO_HOSTS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_host/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_host = '${CYGNUS_CKAN_HOST} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_port/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_port = '${CYGNUS_CKAN_PORT} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ssl/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ssl = '${CYGNUS_CKAN_SSL} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.api_key/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.api_key = '${CYGNUS_CKAN_API_KEY} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_host/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_host = '${CYGNUS_HDFS_HOST} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_port/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_port = '${CYGNUS_HDFS_PORT} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_username/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_username = '${CYGNUS_HDFS_USER} ${FLUME_HOME}/conf/agent.conf
sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.oauth2_token/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.oauth2_token = '${CYGNUS_HDFS_TOKEN} ${FLUME_HOME}/conf/agent.conf

# Change parameters in the cartodb key configuration file
sed -i 's/\"user\"/\"'${CYGNUS_CARTO_USER}'\"/g' ${FLUME_HOME}/conf/cartodb_keys.conf
sed -i 's/\/\/user/\/\/'${CYGNUS_CARTO_USER}'/g' ${FLUME_HOME}/conf/cartodb_keys.conf
sed -i '/"key":/c "key":"'${CYGNUS_CARTO_KEY}'"' ${FLUME_HOME}/conf/cartodb_keys.conf

# Run the Cygnus command
${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${CYGNUS_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p ${CYGNUS_API_PORT} -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &

touch /var/log/cygnus/cygnus.log && tail -f /var/log/cygnus/cygnus.log
