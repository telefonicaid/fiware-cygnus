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

FROM debian:12.11-slim
ARG GITHUB_ACCOUNT=telefonicaid
ARG GITHUB_REPOSITORY=fiware-cygnus

# Environment variables
ENV CYGNUS_USER "cygnus"
ENV CYGNUS_HOME "/opt/fiware-cygnus"
ENV CYGNUS_VERSION "3.16.0-SNAPSHOT"
ENV CYGNUS_CONF_PATH "/opt/apache-flume/conf"
ENV CYGNUS_CONF_FILE "/opt/apache-flume/conf/agent.conf"
ENV CYGNUS_AGENT_NAME "cygnus-ngsi"
ENV CYGNUS_LOG_LEVEL "INFO"
ENV CYGNUS_LOG_APPENDER "LOGFILE"
ENV CYGNUS_SERVICE_PORT "5050"
ENV CYGNUS_JAVA_OPTS "-Xms2048m -Xmx4096m"
ENV CYGNUS_API_PORT "5080"
ENV CYGNUS_MULTIAGENT false
ENV CYGNUS_MONITORING_TYPE ""

# These variables are optional ENV variables to docker runtime
ENV CYGNUS_SKIP_CONF_GENERATION false

# MySQL options
ENV CYGNUS_MYSQL_HOST ""
ENV CYGNUS_MYSQL_PORT ""
# ENV CYGNUS_MYSQL_USER ""  # Warning: For development use only - Storing sensitive information in clear text is insecure
# ENV CYGNUS_MYSQL_PASS ""  # Warning: For development use only - Storing sensitive information in clear text is insecure
ENV CYGNUS_MYSQL_SKIP_CONF_GENERATION ""
ENV CYGNUS_MYSQL_SERVICE_PORT "5050"
ENV CYGNUS_MYSQL_ENABLE_ENCODING ""
ENV CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_MYSQL_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_MYSQL_ENABLE_LOWERCASE ""
ENV CYGNUS_MYSQL_DATA_MODEL ""
ENV CYGNUS_MYSQL_ATTR_PERSISTENCE ""
ENV CYGNUS_MYSQL_ATTR_NATIVE_TYPES ""
ENV CYGNUS_MYSQL_BATCH_SIZE ""
ENV CYGNUS_MYSQL_BATCH_TIMEOUT ""
ENV CYGNUS_MYSQL_BATCH_TTL ""
ENV CYGNUS_MYSQL_OPTIONS ""

# MongoDB / STH options
ENV CYGNUS_MONGO_HOSTS ""
# ENV CYGNUS_MONGO_USER "" # Warning: For development use only - Storing sensitive information in clear text is insecure
# ENV CYGNUS_MONGO_PASS "" # Warning: For development use only - Storing sensitive information in clear text is insecure
ENV CYGNUS_MONGO_REPLICA_SET ""
ENV CYGNUS_MONGO_AUTH_SOURCE ""
ENV CYGNUS_MONGO_SKIP_CONF_GENERATION ""
ENV CYGNUS_MONGO_SERVICE_PORT "5051"
ENV CYGNUS_MONGO_ENABLE_ENCODING ""
ENV CYGNUS_MONGO_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_MONGO_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_MONGO_ENABLE_LOWERCASE ""
ENV CYGNUS_MONGO_DATA_MODEL ""
ENV CYGNUS_MONGO_ATTR_PERSISTENCE ""
ENV CYGNUS_MONGO_DB_PREFIX ""
ENV CYGNUS_MONGO_COLLECTION_PREFIX ""
ENV CYGNUS_MONGO_BATCH_SIZE ""
ENV CYGNUS_MONGO_BATCH_TIMEOUT ""
ENV CYGNUS_MONGO_BATCH_TTL ""
ENV CYGNUS_MONGO_DATA_EXPIRATION ""
ENV CYGNUS_MONGO_COLLECTIONS_SIZE ""
ENV CYGNUS_MONGO_MAX_DOCUMENTS ""
ENV CYGNUS_MONGO_IGNORE_WHITE_SPACES ""
ENV CYGNUS_STH_ENABLE_ENCODING ""
ENV CYGNUS_STH_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_STH_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_STH_ENABLE_LOWERCASE ""
ENV CYGNUS_STH_DATA_MODEL ""
ENV CYGNUS_STH_DB_PREFIX ""
ENV CYGNUS_STH_COLLECTION_PREFIX ""
ENV CYGNUS_STH_RESOLUTIONS ""
ENV CYGNUS_STH_BATCH_SIZE ""
ENV CYGNUS_STH_BATCH_TIMEOUT ""
ENV CYGNUS_STH_BATCH_TTL ""
ENV CYGNUS_STH_DATA_EXPIRATION ""

# CKAN options
ENV CYGNUS_CKAN_HOST ""
ENV CYGNUS_CKAN_PORT ""
ENV CYGNUS_CKAN_SERVICE_PORT "5052"
ENV CYGNUS_CKAN_API_KEY ""
ENV CYGNUS_CKAN_SSL ""
ENV CYGNUS_CKAN_SKIP_CONF_GENERATION ""
ENV CYGNUS_CKAN_ENABLE_ENCODING ""
ENV CYGNUS_CKAN_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_CKAN_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_CKAN_DATA_MODEL ""
ENV CYGNUS_CKAN_ATTR_PERSISTENCE ""
ENV CYGNUS_CKAN_ORION_URL ""
ENV CYGNUS_CKAN_BATCH_SIZE ""
ENV CYGNUS_CKAN_BATCH_TIMEOUT ""
ENV CYGNUS_CKAN_BATCH_TTL ""
ENV CYGNUS_CKAN_BACKEND_MAX_CONNS ""
ENV CYGNUS_CKAN_BACKEND_MAX_CONSS_PER_ROUTE ""

# HDFS options
ENV CYGNUS_HDFS_HOST ""
ENV CYGNUS_HDFS_PORT ""
# ENV CYGNUS_HDFS_USER "" # Warning: For development use only - Storing sensitive information in clear text is insecure
# ENV CYGNUS_HDFS_TOKEN "" # Warning: For development use only - Storing sensitive information in clear text is insecure
ENV CYGNUS_HDFS_SKIP_CONF_GENERATION ""
ENV CYGNUS_HDFS_SERVICE_PORT "5053"
ENV CYGNUS_HDFS_ENABLE_ENCODING ""
ENV CYGNUS_HDFS_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_HDFS_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_HDFS_ENABLE_LOWERCASE ""
ENV CYGNUS_HDFS_DATA_MODEL ""
ENV CYGNUS_HDFS_FILE_FORMAT ""
ENV CYGNUS_HDFS_BACKEND_IMPL ""
ENV CYGNUS_HDFS_BACKEND_MAX_CONNS ""
ENV CYGNUS_HDFS_BACKEND_MAX_CONNS_PER_ROUTE ""
ENV CYGNUS_HDFS_PASSWORD ""
ENV CYGNUS_HDFS_SERVICE_AS_NAMESPACE ""
ENV CYGNUS_HDFS_BATCH_SIZE ""
ENV CYGNUS_HDFS_BATCH_TIMEOUT ""
ENV CYGNUS_HDFS_BATCH_TTL ""
ENV CYGNUS_HDFS_BATCH_RETRY_INTERVALS ""
ENV CYGNUS_HDFS_HIVE ""
ENV CYGNUS_HDFS_KRB5_AUTH ""

# PostgreSQL options
ENV CYGNUS_POSTGRESQL_HOST ""
ENV CYGNUS_POSTGRESQL_PORT ""
# ENV CYGNUS_POSTGRESQL_USER "" # Warning: For development use only - Storing sensitive information in clear text is insecure
# ENV CYGNUS_POSTGRESQL_PASS "" # Warning: For development use only - Storing sensitive information in clear text is insecure
ENV CYGNUS_POSTGRESQL_DATABASE ""
ENV CYGNUS_POSTGRESQL_SKIP_CONF_GENERATION ""
ENV CYGNUS_POSTGRESQL_SERVICE_PORT "5055"
ENV CYGNUS_POSTGRESQL_ENABLE_ENCODING ""
ENV CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_POSTGRESQL_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_POSTGRESQL_ENABLE_LOWERCASE ""
ENV CYGNUS_POSTGRESQL_ATTR_PERSISTENCE ""
ENV CYGNUS_POSTGRESQL_ATTR_NATIVE_TYPES ""
ENV CYGNUS_POSTGRESQL_BATCH_SIZE ""
ENV CYGNUS_POSTGRESQL_BATCH_TIMEOUT ""
ENV CYGNUS_POSTGRESQL_BATCH_TTL ""
ENV CYGNUS_POSTGRESQL_ENABLE_CACHE ""
ENV CYGNUS_POSTGRESQL_OPTIONS ""

# Carto options
# ENV CYGNUS_CARTO_USER ""
# ENV CYGNUS_CARTO_KEY ""
ENV CYGNUS_CARTO_SERVICE_PORT "5054"

# Docker Secrets Support - The following equivalents to storing clear text usernames and passwords are also available:
#
# ENV CYGNUS_MYSQL_USER_FILE
# ENV CYGNUS_MYSQL_PASS_FILE
# ENV CYGNUS_MONGO_USER_FILE
# ENV CYGNUS_MONGO_PASS_FILE
# ENV CYGNUS_HDFS_USER_FILE
# ENV CYGNUS_HDFS_TOKEN_FILE
# ENV CYGNUS_POSTGRESQL_USER_FILE
# ENV CYGNUS_POSTGRESQL_PASS_FILE
# ENV CYGNUS_CARTO_USER_FILE
# ENV CYGNUS_CARTO_KEY_FILE

# Orion options
ENV CYGNUS_ORION_SKIP_CONF_GENERATION ""
ENV CYGNUS_ORION_SERVICE_PORT "5050"
ENV CYGNUS_ORION_ENABLE_ENCODING ""
ENV CYGNUS_ORION_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_ORION_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_ORION_ENABLE_LOWERCASE ""
ENV CYGNUS_ORION_HOST ""
ENV CYGNUS_ORION_PORT ""
ENV CYGNUS_ORION_SSL ""
ENV CYGNUS_ORION_USER ""
ENV CYGNUS_ORION_PASS ""
ENV CYGNUS_ORION_KEYSTONE_HOST ""
ENV CYGNUS_ORION_KEYSTONE_PORT ""
ENV CYGNUS_ORION_KEYSTONE_SSL ""
ENV CYGNUS_ORION_FIWARE ""
ENV CYGNUS_ORION_FIWARE_PATH ""
ENV CYGNUS_ORION_BATCH_SIZE ""
ENV CYGNUS_ORION_BATCH_TIMEOUT ""
ENV CYGNUS_ORION_BATCH_TTL ""
ENV CYGNUS_ORION_BATCH_RETRY_INTERVALS ""
ENV CYGNUS_ORION_ENABLE_CACHE ""



# PostgreGIS options
ENV CYGNUS_POSTGIS_HOST ""
ENV CYGNUS_POSTGIS_PORT ""
# ENV CYGNUS_POSTGIS_USER "" # Warning: For development use only - Storing sensitive information in clear text is insecure
# ENV CYGNUS_POSTGIS_PASS "" # Warning: For development use only - Storing sensitive information in clear text is insecure
ENV CYGNUS_POSTGIS_SKIP_CONF_GENERATION ""
ENV CYGNUS_POSTGIS_SERVICE_PORT "5057"
ENV CYGNUS_POSTGIS_ENABLE_ENCODING ""
ENV CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_POSTGIS_SKIP_NAME_MAPPINGS_GENERATION ""
ENV CYGNUS_POSTGIS_ENABLE_LOWERCASE ""
ENV CYGNUS_POSTGIS_ATTR_PERSISTENCE ""
ENV CYGNUS_POSTGIS_ATTR_NATIVE_TYPES ""
ENV CYGNUS_POSTGIS_BATCH_SIZE ""
ENV CYGNUS_POSTGIS_BATCH_TIMEOUT ""
ENV CYGNUS_POSTGIS_BATCH_TTL ""
ENV CYGNUS_POSTGIS_ENABLE_CACHE ""
ENV CYGNUS_POSTGIS_OPTIONS ""


# Elasticsearch options
ENV CYGNUS_ELASTICSEARCH_HOST ""
ENV CYGNUS_ELASTICSEARCH_PORT ""
ENV CYGNUS_ELASTICSEARCH_SERVICE_PORT "5058"
ENV CYGNUS_ELASTICSEARCH_SSL ""
ENV CYGNUS_ELASTICSEARCH_SKIP_CONF_GENERATION ""
ENV CYGNUS_ELASTICSEARCH_INDEX_PREFIX ""
ENV CYGNUS_ELASTICSEARCH_MAPPING_TYPE ""
ENV CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONNS ""
ENV CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONSS_PER_ROUTE ""
ENV CYGNUS_ELASTICSEARCH_IGNORE_WHITE_SPACES ""
ENV CYGNUS_ELASTICSEARCH_ATTR_PERSISTENCE ""
ENV CYGNUS_ELASTICSEARCH_TIMEZONE ""
ENV CYGNUS_ELASTICSEARCH_CAST_VALUE ""
ENV CYGNUS_ELASTICSEARCH_CACHE_FLASH_INTERVAL_SEC ""
ENV CYGNUS_ELASTICSEARCH_CHARSET ""


# ArcGis options
ENV CYGNUS_ARCGIS_URL ""
ENV CYGNUS_ARCGIS_USER ""
ENV CYGNUS_ARCGIS_SERVICE_PORT "5059"
ENV CYGNUS_ARCGIS_PASS ""
ENV CYGNUS_ELASTICSEARCH_SKIP_CONF_GENERATION ""
ENV CYGNUS_ARCGIS_ENABLE_ENCODING ""
ENV CYGNUS_ARCGIS_ENABLE_NAME_MAPPINGS ""
ENV CYGNUS_ARCGIS_DATA_MODEL ""
ENV CYGNUS_ARCGIS_BATCH_SIZE ""
ENV CYGNUS_ARCGIS_BATCH_TIMEOUT ""
ENV CYGNUS_ARCGIS_BATCH_TTL ""

ENV FLUME_VER "1.11.0"
ENV FLUME_HOME "/opt/apache-flume"
ENV FLUME_TGZ "apache-flume-${FLUME_VER}-bin.tar.gz"
ENV FLUME_URL "https://archive.apache.org/dist/flume/${FLUME_VER}/${FLUME_TGZ}"

ENV JAVA_VERSION "1.17.0"

COPY / ${CYGNUS_HOME}

WORKDIR ${CYGNUS_HOME}

RUN ls -lsrt

# Install
RUN \
    apt-get -y update && \
    apt-get -y upgrade && \
    # Install dependencies
    apt-get -y install curl git python3 && \
    apt-get -y install openjdk-17-jdk openjdk-17-jdk-headless openjdk-17-jre openjdk-17-jre-headless && \
    apt-get -y install maven && \
    # Add Cygnus user
    adduser ${CYGNUS_USER} && \
    export JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64 && \
    export MAVEN_OPTS="-Xmx2048M -Xss128M -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=2048M -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8 -Dmaven.compiler.useIncrementalCompilation=false -DdependencyLocationsEnabled=false -XX:+TieredCompilation -XX:TieredStopAtLevel=1" && \
    export MAVEN_ARGS="-B -T8" && \
    # For debug Maven
    export MAVEN_ARGS="${MAVEN_ARGS} -X -e -Dmaven.compiler.verbose=true" && \
    echo "INFO: Getting apache preferred site and obtain URLs for Maven and Flume..." && \
    APACHE_DOMAIN="$(curl -s 'https://www.apache.org/dyn/closer.cgi?as_json=1' | python3 -c 'import json,sys;obj=json.load(sys.stdin);print obj["preferred"]')" \
      || APACHE_DOMAIN="http://archive.apache.org/dist/" && \
    echo -e $'INFO: Java version <'${JAVA_VERSION}'>\n'$(java -version)'\nINFO: Apache domain <'${APACHE_DOMAIN}'>\nINFO: URL MAVEN <'${MVN_URL}'>\nINFO: URL FLUME <'${FLUME_URL}'>' && \
    echo "INFO: Download and install Flume..." && \
    curl --remote-name --location --insecure --silent --show-error "${FLUME_URL}" && \
    tar zxf apache-flume-${FLUME_VER}-bin.tar.gz && \
    mv apache-flume-${FLUME_VER}-bin ${FLUME_HOME} && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus/lib && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus/libext && \
    chown -R cygnus:cygnus ${FLUME_HOME} && \
    cd ${CYGNUS_HOME} && \
    echo "INFO: Build and install cygnus-common" && \
    cd ${CYGNUS_HOME}/cygnus-common && \
    mvn ${MAVEN_ARGS} clean compile exec:exec assembly:single && \
    cp target/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar ${FLUME_HOME}/plugins.d/cygnus/libext/ && \
    mvn install:install-file -Dfile=${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=${CYGNUS_VERSION} -Dpackaging=jar -DgeneratePom=false && \
    echo "INFO: Build and install cygnus-ngsi" && \
    cd ${CYGNUS_HOME}/cygnus-ngsi && \
    mvn ${MAVEN_ARGS} clean compile exec:exec assembly:single && \
    cp target/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar ${FLUME_HOME}/plugins.d/cygnus/lib/ && \
    echo "INFO: Install Cygnus Application script" && \
    cp ${CYGNUS_HOME}/cygnus-common/target/classes/cygnus-flume-ng ${FLUME_HOME}/bin/ && \
    chmod +x ${FLUME_HOME}/bin/cygnus-flume-ng && \
    echo "INFO: Instantiate some configuration files" && \
    cp ${CYGNUS_HOME}/cygnus-common/conf/log4j2.properties.template ${FLUME_HOME}/conf/log4j2.properties && \
    cp ${CYGNUS_HOME}/cygnus-ngsi/conf/name_mappings.conf.template ${FLUME_HOME}/conf/name_mappings.conf && \
    echo "INFO: Create Cygnus log folder" && \
    mkdir -p /var/log/cygnus && \
    chmod 777 /var/log/cygnus && \
    echo "INFO: Cleanup to thin the final image... doing optimizations..." && \
    cd ${CYGNUS_HOME}/cygnus-common && \
    mvn ${MAVEN_ARGS} clean && \
    cd ${CYGNUS_HOME}/cygnus-ngsi && \
    mvn ${MAVEN_ARGS} clean && \
    rm -rf /root/.m2 && rm -rf ${FLUME_HOME}/docs && rm -rf ${CYGNUS_HOME}/doc && rm -f /*.tar.gz ${CYGNUS_HOME}/*.tar.gz && \
    # Cleanup
    apt-get clean && \
    apt-get -y remove python3 git && \
    apt-get -y autoremove --purge && \
    # FIXME: from now on, old cleanup in CentOS 8. Should be reviewed...
    # FIXME #2113: Fix java errors and warnings. Original script was breaking in CentOS 8
    #echo "INFO: Java runtime not needs JAVA_HOME... Unsetting..." && \
    #unset JAVA_HOME && \
    #yum erase -y git java-${JAVA_VERSION}-openjdk-devel python && \
    #yum erase -y git python && \
    #rpm -qa redhat-logos gtk2 pulseaudio-libs libvorbis jpackage* groff alsa* atk cairo libX* | xargs -r rpm -e --nodeps && \
    # FIXME #2113: disabled step 'rpm --rebuilddb', doesn't work in CentOS8 ?
    #yum clean all && rpm --rebuilddb && rm -rf /var/lib/yum/yumdb && rm -rf /var/lib/yum/history && \
    #yum clean all && rm -rf /var/lib/yum/yumdb && rm -rf /var/lib/yum/history && \
    find /usr/share/locale -mindepth 1 -maxdepth 1 ! -name 'en_US' ! -name 'locale.alias' | xargs -r rm -r && rm -f /var/log/*log && \
    bash -c 'localedef --list-archive | grep -v -e "en_US" | xargs localedef --delete-from-archive' && \
    /bin/cp -f /usr/lib/locale/locale-archive /usr/lib/locale/locale-archive.tmpl && \
    #build-locale-archive && find ${CYGNUS_HOME} -name '.[^.]*' 2>/dev/null | xargs -r rm -rf && \
    echo "INFO: END build"


COPY docker/cygnus-ngsi/cygnus-entrypoint.sh /
COPY docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/
COPY docker/cygnus-ngsi/agent.conf ${CYGNUS_HOME}/docker/cygnus-ngsi/agent.conf
RUN true
COPY docker/cygnus-ngsi/cartodb_keys.conf ${FLUME_HOME}/conf/

# Define the entry point
ENTRYPOINT ["/cygnus-entrypoint.sh"]

# Ports used by cygnus-ngsi
EXPOSE ${CYGNUS_SERVICE_PORT} ${CYGNUS_API_PORT}

LABEL "maintainer"="Cygnus Team. Telefónica I+D"
LABEL "org.opencontainers.image.authors"="iot_support@tid.es"
LABEL "org.opencontainers.image.documentation"="https://fiware-cygnus.rtfd.io/"
LABEL "org.opencontainers.image.vendor"="Telefónica Investigación y Desarrollo, S.A.U"
LABEL "org.opencontainers.image.licenses"="AGPL-3.0-only"
LABEL "org.opencontainers.image.title"="Cygnus Data Persistence"
LABEL "org.opencontainers.image.description"="A connector in charge of persisting context data sources into other third-party databases and storage systems, creating a historical view of the context"
LABEL "org.opencontainers.image.source"=https://github.com/${GITHUB_ACCOUNT}/${GITHUB_REPOSITORY}

