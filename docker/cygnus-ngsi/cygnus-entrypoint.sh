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

# Change parameters in the agent configuration file
rm -f ${FLUME_HOME}/conf/agent*.conf

# usage: file_env VAR [DEFAULT]
#    ie: file_env 'XYZ_DB_PASSWORD' 'example'
# (will allow for "$XYZ_DB_PASSWORD_FILE" to fill in the value of
#  "$XYZ_DB_PASSWORD" from a file, especially for Docker's secrets feature)
file_env() {
    local var="$1"
    local fileVar="${var}_FILE"
    local def="${2:-}"
    if [ "${!var:-}" ] && [ "${!fileVar:-}" ]; then
        echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
        exit 1
    fi
    local val="$def"
    if [ "${!var:-}" ]; then
        val="${!var}"
    elif [ "${!fileVar:-}" ]; then
        val="$(< "${!fileVar}")"
    fi
    export "$var"="$val"
    unset "$fileVar"
}

file_env 'CYGNUS_MYSQL_USER' ''
file_env 'CYGNUS_MYSQL_PASS' ''
file_env 'CYGNUS_MONGO_USER' ''
file_env 'CYGNUS_MONGO_PASS' ''
file_env 'CYGNUS_HDFS_USER' ''
file_env 'CYGNUS_HDFS_TOKEN' ''
file_env 'CYGNUS_POSTGRESQL_USER' ''
file_env 'CYGNUS_POSTGRESQL_PASS' ''
file_env 'CYGNUS_CARTO_USER' ''
file_env 'CYGNUS_CARTO_KEY' ''

# Export JAVA_OPTS
JAVA_OPTS=${CYGNUS_JAVA_OPTS}
export JAVA_OPTS

AGENT_CONF_FILE=agent.conf
GROUPING_CONF_FILE=grouping_rules.conf
NAMEMAPPING_CONF_FILE=name_mappings.conf

if [ "${CYGNUS_MULTIAGENT,,}" == "false" ]; then
    cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
fi

# Check if MYSQL ENV vars
if [ "$CYGNUS_MYSQL_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-mysql.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_MYSQL_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-mysql.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-mysql.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = mysql-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = mysql-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5050 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_host/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_host = '${CYGNUS_MYSQL_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_port/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_port = '${CYGNUS_MYSQL_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username = '${CYGNUS_MYSQL_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password = '${CYGNUS_MYSQL_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_MYSQL_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_encoding = '${CYGNUS_MYSQL_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_grouping = '${CYGNUS_MYSQL_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_name_mappings = '${CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_lowercase = '${CYGNUS_MYSQL_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.data_model = '${CYGNUS_MYSQL_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.attr_persistence = '${CYGNUS_MYSQL_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.batch_size = '${CYGNUS_MYSQL_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.batch_timeout = '${CYGNUS_MYSQL_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MYSQL_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.batch_ttl = '${CYGNUS_MYSQL_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5080 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41415 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5080 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
fi

# Check if MONGO ENV vars
if [ "$CYGNUS_MONGO_HOSTS" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-mongo.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5051 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_MONGOL_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-mongo.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_MONGO_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-mongo.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = mongo-sink sth-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = mongo-channel sth-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_hosts/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_hosts = '${CYGNUS_MONGO_HOSTS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_hosts/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_hosts = '${CYGNUS_MONGO_HOSTS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_MONGO_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_encoding = '${CYGNUS_MONGO_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_grouping = '${CYGNUS_MONGO_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_name_mappings = '${CYGNUS_MONGO_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_lowercase = '${CYGNUS_MONGO_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.data_model = '${CYGNUS_MONGO_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.attr_persistence = '${CYGNUS_MONGO_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_DB_PREFIX" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.db_prefix/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.db_prefix = '${CYGNUS_MONGO_DB_PREFIX} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_COLLECTION_PREFIX" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.collection_prefix/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.collection_prefix = '${CYGNUS_MONGO_COLLECTION_PREFIX} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.batch_size = '${CYGNUS_MONGO_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.batch_timeout = '${CYGNUS_MONGO_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.batch_ttl = '${CYGNUS_MONGO_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_DATA_EXPIRATION" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.data_expiration/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.data_expiration = '${CYGNUS_MONGO_DATA_EXPIRATION} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_COLLECTIONS_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.collections_size/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.collections_size = '${CYGNUS_MONGO_COLLECTIONS_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_MONGO_MAX_DOCUMENTS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.max_documents/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.max_documents = '${CYGNUS_MONGO_MAX_DOCUMENTS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_IGNORE_WHITE_SPACES" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.ignore_white_spaces/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.ignore_white_spaces = '${CYGNUS_MONGO_IGNORE_WHITE_SPACES} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_encoding = '${CYGNUS_STH_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_grouping = '${CYGNUS_STH_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_name_mappings = '${CYGNUS_STH_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.enable_lowercase = '${CYGNUS_STH_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.data_model = '${CYGNUS_STH_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "$CYGNUS_STH_DB_PREFIX" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.db_prefix/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.db_prefix = '${CYGNUS_STH_DB_PREFIX} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_COLLECTION_PREFIX" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.collection_prefix/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.collection_prefix = '${CYGNUS_STH_COLLECTION_PREFIX} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_RESOLUTIONS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.resolutions/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.resolutions = '${CYGNUS_STH_RESOLUTIONS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.batch_size = '${CYGNUS_STH_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.batch_timeout = '${CYGNUS_STH_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.batch_ttl = '${CYGNUS_STH_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_STH_DATA_EXPIRATION" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.data_expiration/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.data_expiration = '${CYGNUS_STH_DATA_EXPIRATION} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5081 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41416 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5081 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
fi

# Check if CKAN ENV vars
if [ "$CYGNUS_CKAN_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-ckan.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_CKAN_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-ckan.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_CKAN_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-ckan.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5052 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = ckan-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = ckan-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_host/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_host = '${CYGNUS_CKAN_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_port/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ckan_port = '${CYGNUS_CKAN_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ssl/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.ssl = '${CYGNUS_CKAN_SSL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.api_key/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.api_key = '${CYGNUS_CKAN_API_KEY} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_CKAN_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.enable_encoding = '${CYGNUS_CKAN_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.enable_grouping = '${CYGNUS_CKAN_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.enable_name_mappings = '${CYGNUS_CKAN_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.data_model = '${CYGNUS_CKAN_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.attr_persistence = '${CYGNUS_CKAN_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_ORION_URL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.orion_url/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.orion_url = '${CYGNUS_CKAN_ORION_URL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.batch_size = '${CYGNUS_CKAN_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.batch_timeout = '${CYGNUS_CKAN_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.batch_ttl = '${CYGNUS_CKAN_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_BACKEND_MAX_CONNS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.backend.max_conns/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.backend.max_conns = '${CYGNUS_CKAN_MAX_CONNS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_CKAN_BACKEND_MAX_CONNS_PER_ROUTE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.backend.max_conns_per_route/c '${CYGNUS_AGENT_NAME}'.sinks.ckan-sink.backend.max_conns_per_route = '${CYGNUS_CKAN_MAX_CONNS_PER_ROUTE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5082 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41417 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5082 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
fi

# Check if HDFS ENV vars
if [ "$CYGNUS_HDFS_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-hdfs.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_HDFS_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-hdfs.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_HDFS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-hdfs.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5053 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = hdfs-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = hdfs-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}

    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_host/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_host = '${CYGNUS_HDFS_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_port/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_port = '${CYGNUS_HDFS_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_username/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hdfs_username = '${CYGNUS_HDFS_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.oauth2_token/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.oauth2_token = '${CYGNUS_HDFS_TOKEN} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_HDFS_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_encoding = '${CYGNUS_HDFS_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_grouping = '${CYGNUS_HDFS_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_name_mappings = '${CYGNUS_HDFS_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.enable_lowercase = '${CYGNUS_HDFS_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.data_model = '${CYGNUS_HDFS_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_FILE_FORMAT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.file_format/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.file_format = '${CYGNUS_HDFS_FILE_FORMAT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BACKEND_IMPL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.backend.impl/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.backend.impl = '${CYGNUS_HDFS_BACKEND_IMPL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BACKEND_MAX_CONNS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.backend.max_conns/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.backend.max_conns = '${CYGNUS_HDFS_BACKEND_MAX_CONNS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BACKEND_MAX_CONNS_PER_ROUTE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.backend.max_conns_per_route/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.backend.max_conns_per_route = '${CYGNUS_HDFS_BACKEND_MAX_CONNS_PER_ROUTE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_PASSWORD" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.password/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.password = '${CYGNUS_HDFS_PASSWORD} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_SERVICE_AS_NAMESPACE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.service_as_namespace/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.service_as_namespace = '${CYGNUS_HDFS_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_size = '${CYGNUS_HDFS_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_timeout = '${CYGNUS_HDFS_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_ttl = '${CYGNUS_HDFS_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_BATCH_RETRY_INTERVALS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_retry_intervals/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.batch_retry_intervals = '${CYGNUS_HDFS_BATCH_RETRY_INTERVALS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_HIVE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hive/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.hive = '${CYGNUS_HDFS_HIVE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_HDFS_KRB5_AUTH" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.krb5_auth/c '${CYGNUS_AGENT_NAME}'.sinks.hdfs-sink.krb5_auth = '${CYGNUS_HDFS_KRB5_AUTH} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5083 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41418 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5083 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
fi

# Check if PostgreSQL ENV vars
if [ "$CYGNUS_POSTGRESQL_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-postgresql.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGRESQL_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-postgresql.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-postgresql.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5054 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = postgresql-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = postgresql-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}

    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_host/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_host = '${CYGNUS_POSTGRESQL_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_port/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_port = '${CYGNUS_POSTGRESQL_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_username/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_username = '${CYGNUS_POSTGRESQL_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_password/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_password = '${CYGNUS_POSTGRESQL_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_POSTGRESQL_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_encoding = '${CYGNUS_POSTGRESQL_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_grouping = '${CYGNUS_POSTGRESQL_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_name_mappings = '${CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_lowercase = '${CYGNUS_POSTGRESQL_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.attr_persistence = '${CYGNUS_POSTGRESQL_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.batch_size = '${CYGNUS_POSTGRESQL_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.batch_timeout = '${CYGNUS_POSTGRESQL_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.batch_ttl = '${CYGNUS_POSTGRESQL_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_CACHE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.backend.enable_cache/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.backend.enable_cache = '${CYGNUS_POSTGRESQL_ENABLE_CACHE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5084 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41419 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5084 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
fi

# Check if CARTODB ENV vars
if [ "$CYGNUS_CARTO_USER" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-carto.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_CARTO_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-carto.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_CARTO_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-carto.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5055 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = cartodb-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = cartodb-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}

    # Change parameters in the cartodb key configuration file
    sed -i 's/\"user\"/\"'${CYGNUS_CARTO_USER}'\"/g' ${FLUME_HOME}/conf/cartodb_keys.conf
    sed -i 's/\/\/user/\/\/'${CYGNUS_CARTO_USER}'/g' ${FLUME_HOME}/conf/cartodb_keys.conf
    sed -i '/"key":/c "key":"'${CYGNUS_CARTO_KEY}'"' ${FLUME_HOME}/conf/cartodb_keys.conf
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5085 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41420 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5085 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
fi

# Check if ORION ENV vars
if [ "$CYGNUS_ORION_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-orion.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_ORION_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules-orion.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_ORION_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings-orion.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5056 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = orion-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = orion-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}

    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_host/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_host = '${CYGNUS_ORION_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_port/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_port = '${CYGNUS_ORION_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_ssl/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_ssl = '${CYGNUS_ORION_SSL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_username/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_username = '${CYGNUS_ORION_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_password/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_password = '${CYGNUS_ORION_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.keystone_host/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.keystone_host = '${CYGNUS_ORION_KEYSTONE_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.keystone_port/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.keystone_port = '${CYGNUS_ORION_KEYSTONE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.keystone_ssl/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.keystone_ssl = '${CYGNUS_ORION_KEYSTONE_SSL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_fiware/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_fiware = '${CYGNUS_ORION_FIWARE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_fiware_path/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.orion_fiware_path = '${CYGNUS_ORION_FIWARE_PATH} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}    
    # The following are optional and disabled by default
    if [ "$CYGNUS_ORION_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_encoding = '${CYGNUS_ORION_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_grouping = '${CYGNUS_ORION_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_name_mappings = '${CYGNUS_ORION_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.enable_lowercase = '${CYGNUS_ORION_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.attr_persistence = '${CYGNUS_ORION_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_size = '${CYGNUS_ORION_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_timeout = '${CYGNUS_ORION_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_ttl = '${CYGNUS_ORION_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_BATCH_RETRY_INTERVALS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.batch_retry_intervals/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.bach_retry_intervals = '${CYGNUS_ORION_BATCH_RETRY_INTERNALS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ORION_ENABLE_CACHE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.orion-sink.backend.enable_cache/c '${CYGNUS_AGENT_NAME}'.sinks.orion-sink.backend.enable_cache = '${CYGNUS_ORION_ENABLE_CACHE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        # Run the Cygnus command
        ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5086 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
    fi
fi




if [ "${CYGNUS_MULTIAGENT,,}" == "false" ]; then
    if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
        # Run the Cygnus command with monitoring
        ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${CYGNUS_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p ${CYGNUS_API_PORT} -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41414 &
    else
        # Run the Cygnus command
        ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${CYGNUS_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p ${CYGNUS_API_PORT} -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
    fi
fi


touch /var/log/cygnus/cygnus.log && tail -f /var/log/cygnus/cygnus.log
