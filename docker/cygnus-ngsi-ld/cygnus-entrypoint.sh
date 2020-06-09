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
file_env 'CYGNUS_POSTGIS_USER' ''
file_env 'CYGNUS_POSTGIS_PASS' ''
file_env 'CYGNUS_CARTO_USER' ''
file_env 'CYGNUS_CARTO_KEY' ''

# Export JAVA_OPTS
JAVA_OPTS=${CYGNUS_JAVA_OPTS}
export JAVA_OPTS

AGENT_CONF_FILE=agent.conf
GROUPING_CONF_FILE=grouping_rules.conf
NAMEMAPPING_CONF_FILE=name_mappings.conf

if [ "${CYGNUS_MULTIAGENT,,}" == "false" ]; then
    cp -p /opt/fiware-cygnus/docker/cygnus-ngsi-ld/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
fi

# Check if PostgreSQL ENV vars
if [ "$CYGNUS_POSTGRESQL_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_postgresql.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi-ld/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGRESQL_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules_postgresql.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_postgresql.conf
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



# Check if ORION ENV vars
if [ "$CYGNUS_ORION_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_orion.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi-ld/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_ORION_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules_orion.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_ORION_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_orion.conf
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

# Check if POSTGIS ENV vars
if [ "$CYGNUS_POSTGIS_HOST" != "" ]; then
    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        AGENT_CONF_FILE=agent_postgis.conf
        cp -p /opt/fiware-cygnus/docker/cygnus-ngsi-ld/agent.conf ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        if [ "${CYGNUS_POSTGIS_ENABLE_GROUPING,,}" == "true" ]; then
            GROUPING_CONF_FILE=grouping_rules_postgis.conf
            cp -p ${FLUME_HOME}/conf/grouping_rules.conf ${FLUME_HOME}/conf/${GROUPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.gi.grouping_rules_conf_file = '${FLUME_HOME}/conf/${GROUPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        if [ "${CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS,,}" == "true" ]; then
            NAMEMAPPING_CONF_FILE=name_mappings_postgis.conf
            cp -p ${FLUME_HOME}/conf/name_mappings.conf ${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors.nmi.name_mappings_conf_file = '${FLUME_HOME}/conf/${NAMEMAPPING_CONF_FILE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
            sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors =/c '${CYGNUS_AGENT_NAME}'.sources.http-source.interceptors = ts nmi' ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
        fi
        sed -i '/'${CYGNUS_AGENT_NAME}'.sources.http-source.port/c '${CYGNUS_AGENT_NAME}'.sources.http-source.port = '5057 ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
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
    if [ "$CYGNUS_POSTGIS_ENABLE_GROUPING" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_grouping/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_grouping = '${CYGNUS_POSTGIS_ENABLE_GROUPING} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_name_mappings/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_name_mappings = '${CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ENABLE_LOWERCASE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_lowercase/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.enable_lowercase = '${CYGNUS_POSTGIS_ENABLE_LOWERCASE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
    fi
    if [ "$CYGNUS_POSTGIS_ATTR_PERSISTENCE" != "" ]; then
        sed -i '/#'${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.attr_persistence/c '${CYGNUS_AGENT_NAME}'.sinks.postgis-sink.attr_persistence = '${CYGNUS_POSTGIS_ATTR_PERSISTENCE} ${FLUME_HOME}/conf/${AGENT_CONF_FILE}
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

    if [ "${CYGNUS_MULTIAGENT,,}" == "true" ]; then
        if [ "$CYGNUS_MONITORING_TYPE" != "" ]; then
            # Run the Cygnus command with monitoring
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5087 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type=${CYGNUS_MONITORING_TYPE} -Dflume.monitoring.port=41421 &
        else
            # Run the Cygnus command
            ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f ${FLUME_HOME}/conf/${AGENT_CONF_FILE} -n ${CYGNUS_AGENT_NAME} -p 5087 -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC -Dfile.encoding=UTF-8 &
        fi
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

touch /var/log/cygnus/cygnus.log && tail -f /var/log/cygnus/cygnus.log &
PIDS="$PIDS $!"

wait $PIDS
trap - TERM INT
wait $PIDS
