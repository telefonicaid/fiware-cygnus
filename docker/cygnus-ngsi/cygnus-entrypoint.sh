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
file_env 'CYGNUS_POSTGIS_USER' ''
file_env 'CYGNUS_POSTGIS_PASS' ''
file_env 'CYGNUS_CARTO_USER' ''
file_env 'CYGNUS_CARTO_KEY' ''

PIDS=""
trap 'kill -TERM $PIDS' TERM INT

# Export JAVA_OPTS
JAVA_OPTS=${CYGNUS_JAVA_OPTS}
export JAVA_OPTS
echo "INFO: Using JAVA_OPTS: <${JAVA_OPTS}>"

AGENT_CONF_FILE=agent.conf
NAMEMAPPING_CONF_FILE=name_mappings.conf



if [ "${CYGNUS_SKIP_CONF_GENERATION,,}" == "true" ]; then
    # Force to skip all kind of generation
    CYGNUS_MYSQL_SKIP_CONF_GENERATION="true"
    CYGNUS_MONGO_SKIP_CONF_GENERATION="true"
    CYGNUS_CKAN_SKIP_CONF_GENERATION="true"
    CYGNUS_HDFS_SKIP_CONF_GENERATION="true"
    CYGNUS_POSTGRESQL_SKIP_CONF_GENERATION="true"
    CYGNUS_CARTO_SKIP_CONF_GENERATION="true"
    CYGNUS_ORION_SKIP_CONF_GENERATION="true"
    CYGNUS_POSTGIS_SKIP_CONF_GENERATION="true"
    CYGNUS_ELASTICSEARCH_SKIP_CONF_GENERATION="true"
    CYGNUS_ARCGIS_SKIP_CONF_GENERATION="true"
    CYGNUS_MYSQL_SKIP_NAME_MAPPINGS_GENERATION="true"
    CYGNUS_MONGO_SKIP_NAME_MAPPINGS_GENERATION="true"
    CYGNUS_CKAN_SKIP_NAME_MAPPINGS_GENERATION="true"
    CYGNUS_HDFS_SKIP_NAME_MAPPINGS_GENERATION="true"
    CYGNUS_ORION_SKIP_NAME_MAPPINGS_GENERATION="true"
    CYGNUS_POSTGRESQL_SKIP_NAME_MAPPINGS_GENERATION="true"
    CYGNUS_POSTGIS_SKIP_NAME_MAPPINGS_GENERATION="true"
fi

if [ "${CYGNUS_MULTIAGENT,,}" == "false" ]; then
    # and check if SKIP ALL env var
    if [ "${CYGNUS_SKIP_CONF_GENERATION,,}" == "false" ]; then
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/multisink_agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
fi

# Check if MYSQL ENV vars
if [ "${CYGNUS_MYSQL_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_mysql.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5080 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41415 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5080 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_MYSQL_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_mysql.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_mysql.conf
            if [ "${CYGNUS_MYSQL_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_MYSQL_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-mysql /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-mysql.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-mysql.port = '${CYGNUS_MYSQL_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_mysql.conf
            if [ "${CYGNUS_MYSQL_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-mysql.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-mysql.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-mysql.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-mysql.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = mysql-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = mysql-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_host/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_host = '${CYGNUS_MYSQL_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_port/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_port = '${CYGNUS_MYSQL_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_username = '${CYGNUS_MYSQL_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_password = '${CYGNUS_MYSQL_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_MYSQL_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.enable_encoding = '${CYGNUS_MYSQL_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
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
    if [ "$CYGNUS_MYSQL_ATTR_NATIVE_TYPES" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.attr_native_types/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.attr_native_types = '${CYGNUS_MYSQL_ATTR_NATIVE_TYPES} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
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
    if [ "$CYGNUS_MYSQL_OPTIONS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_options/c '${CYGNUS_AGENT_NAME}'.sinks.mysql-sink.mysql_options = '${CYGNUS_MYSQL_OPTIONS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5080 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41415 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5080 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
fi

# Check if MONGO ENV vars
if [ "${CYGNUS_MONGO_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_mongo.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5081 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41416 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5081 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_MONGO_HOSTS" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_mongo.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_MONGO_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_MONGO_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_mongo.conf
            if [ "${CYGNUS_MONGO_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-mongo /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-mongo.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-mongo.port = '${CYGNUS_MONGO_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_MONGO_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_mongo.conf
            if [ "${CYGNUS_MONGO_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-mongo.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-mongo.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-mongo.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-mongo.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = mongo-sink sth-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = mongo-channel sth-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_hosts/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_hosts = '${CYGNUS_MONGO_HOSTS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_auth_source/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_auth_source = '${CYGNUS_MONGO_AUTH_SOURCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_replica_set/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.mongo_replica_set = '${CYGNUS_MONGO_REPLICA_SET} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_hosts/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_hosts = '${CYGNUS_MONGO_HOSTS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_username = '${CYGNUS_MONGO_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_password = '${CYGNUS_MONGO_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_auth_source/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_auth_source = '${CYGNUS_MONGO_AUTH_SOURCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_replica_set/c '${CYGNUS_AGENT_NAME}'.sinks.sth-sink.mongo_replica_set = '${CYGNUS_MONGO_REPLICA_SET} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_MONGO_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.mongo-sink.enable_encoding = '${CYGNUS_MONGO_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
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
        PIDS="$PIDS $!"
    fi
fi

# Check if CKAN ENV vars
if [ "${CYGNUS_CKAN_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_ckan.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5082 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41417 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5082 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_CKAN_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_ckan.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_CKAN_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_ckan.conf
            if [ "${CYGNUS_CKAN_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_CKAN_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-ckan /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-ckan.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-ckan.port = '${CYGNUS_CKAN_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_CKAN_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_ckan.conf
            if [ "${CYGNUS_CKAN_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-ckan.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-ckan.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-ckan.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-ckan.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
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
        PIDS="$PIDS $!"
    fi
fi

# Check if HDFS ENV vars
if [ "${CYGNUS_HDFS_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_hdfs.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5083 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41418 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5083 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_HDFS_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_hdfs.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_HDFS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_hdfs.conf
            if [ "${CYGNUS_HDFS_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_HDFS_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-hdfs /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-hdfs.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-hdfs.port = '${CYGNUS_HDFS_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_HDFS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_hdfs.conf
            if [ "${CYGNUS_HDFS_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-hdfs.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-hdfs.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-hdfs.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-hdfs.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
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
        PIDS="$PIDS $!"
    fi
fi

# Check if PostgreSQL ENV vars
if [ "${CYGNUS_POSTGRESQL_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_postgresql.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5084 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41419 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5084 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_POSTGRESQL_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_postgresql.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_postgresql.conf
            if [ "${CYGNUS_POSTGRESQL_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_POSTGRESQL_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-postgresql /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-postgresql.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-postgresql.port = '${CYGNUS_POSTGRESQL_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_postgresql.conf
            if [ "${CYGNUS_POSTGRESQL_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-postgresql.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-postgresql.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-postgresql.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-postgresql.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = postgresql-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = postgresql-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}

    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_host/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_host = '${CYGNUS_POSTGRESQL_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_port/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_port = '${CYGNUS_POSTGRESQL_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_username/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_username = '${CYGNUS_POSTGRESQL_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_password/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_password = '${CYGNUS_POSTGRESQL_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_POSTGRESQL_DATABASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_database/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_database = '${CYGNUS_POSTGRESQL_DATABASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_encoding = '${CYGNUS_POSTGRESQL_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_name_mappings = '${CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.enable_lowercase = '${CYGNUS_POSTGRESQL_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.data_model = '${CYGNUS_POSTGRESQL_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.attr_persistence = '${CYGNUS_POSTGRESQL_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGRESQL_ATTR_NATIVE_TYPES" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.attr_native_types/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.attr_native_types = '${CYGNUS_POSTGRESQL_ATTR_NATIVE_TYPES} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
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
    if [ "$CYGNUS_POSTGRESQL_OPTIONS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_options/c '${CYGNUS_AGENT_NAME}'.sinks.postgresql-sink.postgresql_options = '${CYGNUS_POSTGRESQL_OPTIONS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5084 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41419 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5084 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
fi

# Check if CARTODB ENV vars
if [ "${CYGNUS_CARTO_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_carto.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5085 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41420 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5085 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_CARTO_USER" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_carto.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_CARTO_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_carto.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_CARTO_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-cartodb /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-cartodb.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-cartodb.port = '${CYGNUS_CARTO_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_CARTO_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_carto.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-cartodb.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-cartodb.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-cartodb.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-cartodb.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
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
        PIDS="$PIDS $!"
    fi
fi

# Check if ORION ENV vars
if [ "${CYGNUS_ORION_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_orion.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5086 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41421 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5086 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
    fi
elif [ "$CYGNUS_ORION_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_orion.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_ORION_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_orion.conf
            if [ "${CYGNUS_ORION_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_ORION_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-orion /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-orion.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-orion.port = '${CYGNUS_ORION_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_ORION_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_orion.conf
            if [ "${CYGNUS_ORION_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-orion.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-orion.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-orion.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-orion.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
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
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5086 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41421 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5086 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
fi

# Check if POSTGIS ENV vars
if [ "${CYGNUS_POSTGIS_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_postgis.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5087 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41422 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5087 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_POSTGIS_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_postgis.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_postgis.conf
            if [ "${CYGNUS_POSTGIS_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_POSTGIS_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-postgis /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-postgis.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-postgis.port = '${CYGNUS_POSTGIS_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_postgis.conf
            if [ "${CYGNUS_POSTGIS_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-postgis.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source-postgis.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-postgis.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source-postgis.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = postgis-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = postgis-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}

    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_host/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_host = '${CYGNUS_POSTGIS_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_port/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_port = '${CYGNUS_POSTGIS_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_username/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_username = '${CYGNUS_POSTGIS_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_password/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_password = '${CYGNUS_POSTGIS_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_POSTGIS_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_encoding = '${CYGNUS_POSTGIS_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_name_mappings = '${CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_lowercase = '${CYGNUS_POSTGIS_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.data_model = '${CYGNUS_POSTGIS_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.attr_persistence = '${CYGNUS_POSTGIS_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ATTR_NATIVE_TYPES" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.attr_native_types/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.attr_native_types = '${CYGNUS_POSTGIS_ATTR_NATIVE_TYPES} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.batch_size = '${CYGNUS_POSTGIS_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.batch_timeout = '${CYGNUS_POSTGIS_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.batch_ttl = '${CYGNUS_POSTGIS_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ENABLE_CACHE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.backend.enable_cache/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.backend.enable_cache = '${CYGNUS_POSTGIS_ENABLE_CACHE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_OPTIONS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_options/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.postgis_options = '${CYGNUS_POSTGIS_OPTIONS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5087 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41422 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5087 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
fi

# Check if ELASTICSEARCH ENV vars
if [ "${CYGNUS_ELASTICSEARCH_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-elasticsearch.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5088 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41423 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5088 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_ELASTICSEARCH_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-elasticsearch.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_ELASTICSEARCH_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    else
        sed -i 's/'${CYGNUS_AGENT_NAME}'.sources =/'${CYGNUS_AGENT_NAME}'.sources = http-source-elasticsearch /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-elasticsearch.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-elasticsearch.port = '${CYGNUS_ELASTICSEARCH_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = elasticsearch-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = elasticsearch-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.elasticsearch_host/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.elasticsearch_host = '${CYGNUS_ELASTICSEARCH_HOST} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.elasticsearch_port/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.elasticsearch_port = '${CYGNUS_ELASTICSEARCH_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.ssl/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.ssl = '${CYGNUS_ELASTICSEARCH_SSL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_ELASTICSEARCH_INDEX_PREFIX" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.index_prefix/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.index_prefix = '${CYGNUS_ELASTICSEARCH_INDEX_PREFIX} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_MAPPING_TYPE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.mapping_type/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.mapping_type = '${CYGNUS_ELASTICSEARCH_MAPPING_TYPE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONNS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.backend.max_conns/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.backend.max_conns = '${CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONNS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONSS_PER_ROUTE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.backend.max_conns_per_route/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.backend.max_conns_per_route = '${CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONSS_PER_ROUTE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_IGNORE_WHITE_SPACES" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.ignore_white_spaces/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.ignore_white_spaces = '${CYGNUS_ELASTICSEARCH_IGNORE_WHITE_SPACES} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.attr_persistence = '${CYGNUS_ELASTICSEARCH_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_TIMEZONE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.timezone/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.timezone = '${CYGNUS_ELASTICSEARCH_TIMEZONE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_CAST_VALUE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.cast_value/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.cast_value = '${CYGNUS_ELASTICSEARCH_CAST_VALUE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_CACHE_FLASH_INTERVAL_SEC" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.cache_flash_interval_sec/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.cache_flash_interval_sec = '${CYGNUS_ELASTICSEARCH_CACHE_FLASH_INTERVAL_SEC} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ELASTICSEARCH_CHARSET" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.charset/c '${CYGNUS_AGENT_NAME}'.sinks.elasticsearch-sink.charset = '${CYGNUS_ELASTICSEARCH_CHARSET} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5088 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41423 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5088 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
fi




# Check if ARCGIS ENV vars
if [ "${CYGNUS_ARCGIS_SKIP_CONF_GENERATION,,}" == "true" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent-arcgis.conf
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5089 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41424 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5089 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
    fi
elif [ "$CYGNUS_ARCGIS_URL" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_arcgis.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_ARCGIS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_arcgis.conf
            if [ "${CYGNUS_ARCGIS_SKIP_NAME_MAPPINGS_GENERATION,,}" != "true" ]; then
                cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            fi
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '${CYGNUS_ARCGIS_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source-arcgis.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source-arcgis.port = '${CYGNUS_ARCGIS_SERVICE_PORT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.sinks =/'${CYGNUS_AGENT_NAME}'.sinks = arcgis-sink /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i 's/'${CYGNUS_AGENT_NAME}'.channels =/'${CYGNUS_AGENT_NAME}'.channels = arcgis-channel /g' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.arcgis_url/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.arcgis_url = '${CYGNUS_ARCGIS_URL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.arcgis_username/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.arcgis_username = '${CYGNUS_ARCGIS_USER} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    sed -i '/'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.arcgis_password/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.arcgis_password = '${CYGNUS_ARCGIS_PASS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    # The following are optional and disabled by default
    if [ "$CYGNUS_ARCGIS_ENABLE_ENCODING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.enable_encoding/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.enable_encoding = '${CYGNUS_ARCGIS_ENABLE_ENCODING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ARCGIS_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.enable_name_mappings = '${CYGNUS_ARCGIS_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ARCGIS_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.enable_lowercase = '${CYGNUS_ARCGIS_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ARCGIS_DATA_MODEL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.data_model/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.data_model = '${CYGNUS_ARCGIS_DATA_MODEL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ARCGIS_BATCH_SIZE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_size/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_size = '${CYGNUS_ARCGIS_BATCH_SIZE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ARCGIS_BATCH_TIMEOUT" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_timeout/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_timeout = '${CYGNUS_ARCGIS_BATCH_TIMEOUT} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_ARCGIS_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_ttl = '${CYGNUS_ARCGIS_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
        if [ "$CYGNUS_ARCGIS_BATCH_TTL" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_ttl/c '${CYGNUS_AGENT_NAME}'.sinks.arcgis-sink.batch_ttl = '${CYGNUS_ARCGIS_BATCH_TTL} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5089 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41424 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5089 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
        PIDS="$PIDS $!"
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
    PIDS="$PIDS $!"
fi

touch /var/log/cygnus/cygnus.log
ln -snf /dev/stdout /var/log/cygnus/cygnus.log & PIDS="$PIDS $!"

if [ "$CYGNUS_LOG_LEVEL" ]; then
    (sleep 10; curl -X PUT 'http://localhost:'$CYGNUS_API_PORT'/admin/log?level='$CYGNUS_LOG_LEVEL) &
fi


wait $PIDS
trap - TERM INT
wait $PIDS
