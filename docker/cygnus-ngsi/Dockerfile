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

FROM centos:centos7.4.1708

MAINTAINER Francisco Romero Bueno <francisco.romerobueno@telefonica.com>

# Environment variables
ENV CYGNUS_USER "cygnus"
ENV CYGNUS_HOME "/opt/fiware-cygnus"
ENV CYGNUS_VERSION "1.8.0_SNAPSHOT"
ENV CYGNUS_CONF_PATH "/opt/apache-flume/conf"
ENV CYGNUS_CONF_FILE "/opt/apache-flume/conf/agent.conf"
ENV CYGNUS_AGENT_NAME "cygnus-ngsi"
ENV CYGNUS_LOG_LEVEL "INFO"
ENV CYGNUS_LOG_APPENDER "console"
ENV CYGNUS_SERVICE_PORT "5050"
ENV CYGNUS_API_PORT "8081"
ENV CYGNUS_MYSQL_HOST "iot-mysql"
ENV CYGNUS_MYSQL_PORT "3306"
ENV CYGNUS_MYSQL_USER "mysql"
ENV CYGNUS_MYSQL_PASS "mysql"
ENV CYGNUS_MONGO_HOSTS "iot-mongo:27017"
ENV CYGNUS_MONGO_USER "mongo"
ENV CYGNUS_MONGO_PASS "mongo"
ENV CYGNUS_CKAN_HOST "iot-ckan"
ENV CYGNUS_CKAN_PORT "80"
ENV CYGNUS_CKAN_API_KEY ""
ENV CYGNUS_CKAN_SSL "false"
ENV CYGNUS_HDFS_HOST "iot-hdfs"
ENV CYGNUS_HDFS_PORT "50070"
ENV CYGNUS_HDFS_USER "hdfs"
ENV CYGNUS_HDFS_TOKEN ""
ENV CYGNUS_CARTO_USER "carto"
ENV CYGNUS_CARTO_KEY "carto"

# NOTE: Configure correctly GIT_URL_CYGNUS and GIT_REV_CYGNUS for each git branch/fork used
ENV GIT_URL_CYGNUS "https://github.com/telefonicaid/fiware-cygnus.git"
# ENV GIT_URL_CYGNUS "https://github.com/myuser/fiware-cygnus.git"
ENV GIT_REV_CYGNUS "master"
# ENV GIT_REV_CYGNUS "mybranch"

ENV MVN_VER "3.5.2"
ENV MVN_HOME "/opt/apache-maven"

ENV FLUME_VER "1.4.0"
ENV FLUME_HOME "/opt/apache-flume"

ENV JAVA_VERSION "1.8.0"

# Install
RUN \
    # Add Cygnus user
    adduser ${CYGNUS_USER} && \
    yum -y install nc java-${JAVA_VERSION}-openjdk-devel git && \
    export JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk && \
    export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=128m -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8 -Dmaven.compiler.useIncrementalCompilation=false -DdependencyLocationsEnabled=false -XX:+TieredCompilation -XX:TieredStopAtLevel=1" && \
    export MAVEN_ARGS="-B -T4" && \
    # For debug Maven
    # export MAVEN_ARGS="${MAVEN_ARGS} -X -e -Dmaven.compiler.verbose=true" && \
    echo "INFO: Getting apache preferred site and obtain URLs for Maven and Flume..." && \
    APACHE_DOMAIN="$(curl -s 'https://www.apache.org/dyn/closer.cgi?as_json=1' | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["preferred"]')" \
      || APACHE_DOMAIN="http://archive.apache.org/dist/" && \
    MVN_URL="${APACHE_DOMAIN}maven/maven-3/${MVN_VER}/binaries/apache-maven-${MVN_VER}-bin.tar.gz" && \
    FLUME_URL="${APACHE_DOMAIN}flume/${FLUME_VER}/apache-flume-${FLUME_VER}-bin.tar.gz" && \
    echo -e $'INFO: Java version <'${JAVA_VERSION}'>\n'$(java -version)'\nINFO: Apache domain <'${APACHE_DOMAIN}'>\nINFO: URL MAVEN <'${MVN_URL}'>\nINFO: URL FLUME <'${FLUME_URL}'>' && \
    echo "INFO: Download and install Maven and Flume..." && \
    curl --remote-name --location --insecure --silent --show-error "${MVN_URL}" && \
    tar xzf apache-maven-${MVN_VER}-bin.tar.gz && \
    mv apache-maven-${MVN_VER} ${MVN_HOME} && \
    curl --remote-name --location --insecure --silent --show-error "${FLUME_URL}" && \
    tar zxf apache-flume-${FLUME_VER}-bin.tar.gz && \
    mv apache-flume-${FLUME_VER}-bin ${FLUME_HOME} && \
    mv ${FLUME_HOME}/lib/httpclient-4.2.1.jar ${FLUME_HOME}/lib/httpclient-4.2.1.jar.old && \
    mv ${FLUME_HOME}/lib/httpcore-4.2.1.jar ${FLUME_HOME}/lib/httpcore-4.2.1.jar.old && \
    mv ${FLUME_HOME}/lib/libthrift-0.7.0.jar ${FLUME_HOME}/lib/libthrift-0.7.0.jar.old && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus/lib && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus/libext && \
    chown -R cygnus:cygnus ${FLUME_HOME} && \
    # Make a shallow clone to help reduce final image size
    echo "INFO: Cloning Cygnus using <${GIT_URL_CYGNUS}> and <${GIT_REV_CYGNUS}>" && \
    git clone --depth 1 ${GIT_URL_CYGNUS} ${CYGNUS_HOME} && \
    cd ${CYGNUS_HOME} && \
    git checkout ${GIT_REV_CYGNUS} && \
    echo "INFO: Build and install cygnus-common" && \
    cd ${CYGNUS_HOME}/cygnus-common && \
    ${MVN_HOME}/bin/mvn ${MAVEN_ARGS} clean compile exec:exec assembly:single && \
    cp target/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar ${FLUME_HOME}/plugins.d/cygnus/libext/ && \
    ${MVN_HOME}/bin/mvn install:install-file -Dfile=${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=${CYGNUS_VERSION} -Dpackaging=jar -DgeneratePom=false && \
    echo "INFO: Build and install cygnus-ngsi" && \
    cd ${CYGNUS_HOME}/cygnus-ngsi && \
    ${MVN_HOME}/bin/mvn ${MAVEN_ARGS} clean compile exec:exec assembly:single && \
    cp target/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar ${FLUME_HOME}/plugins.d/cygnus/lib/ && \
    echo "INFO: Install Cygnus Application script" && \
    cp ${CYGNUS_HOME}/cygnus-common/target/classes/cygnus-flume-ng ${FLUME_HOME}/bin/ && \
    chmod +x ${FLUME_HOME}/bin/cygnus-flume-ng && \
    echo "INFO: Instantiate some configuration files" && \
    cp ${CYGNUS_HOME}/cygnus-common/conf/log4j.properties.template ${FLUME_HOME}/conf/log4j.properties && \
    cp ${CYGNUS_HOME}/cygnus-ngsi/conf/grouping_rules.conf.template ${FLUME_HOME}/conf/grouping_rules.conf && \
    echo "INFO: Create Cygnus log folder" && \
    mkdir -p /var/log/cygnus && \
    echo "INFO: Cleanup to thin the final image... doing optimizations..." && \
    cd ${CYGNUS_HOME}/cygnus-common && \
    ${MVN_HOME}/bin/mvn ${MAVEN_ARGS} clean && \
    cd ${CYGNUS_HOME}/cygnus-ngsi && \
    ${MVN_HOME}/bin/mvn ${MAVEN_ARGS} clean && \
    rm -rf /root/.m2 && rm -rf ${MVN_HOME} && rm -rf ${FLUME_HOME}/docs && rm -rf ${CYGNUS_HOME}/doc && rm -f /*.tar.gz && \
    echo "INFO: Java runtime not needs JAVA_HOME... Unsetting..." && \
    unset JAVA_HOME && \
    yum erase -y git java-${JAVA_VERSION}-openjdk-devel && \
    rpm -qa redhat-logos gtk2 pulseaudio-libs libvorbis jpackage* groff alsa* atk cairo libX* | xargs -r rpm -e --nodeps && yum -y erase libss && \
    yum clean all && rpm -vv --rebuilddb && rm -rf /var/lib/yum/yumdb && rm -rf /var/lib/yum/history && \
    find /usr/share/locale -mindepth 1 -maxdepth 1 ! -name 'en_US' ! -name 'locale.alias' | xargs -r rm -r && rm -f /var/log/*log && \
    bash -c 'localedef --list-archive | grep -v -e "en_US" | xargs localedef --delete-from-archive' && \
    /bin/cp -f /usr/lib/locale/locale-archive /usr/lib/locale/locale-archive.tmpl && \
    build-locale-archive && find ${CYGNUS_HOME} -name '.[^.]*' 2>/dev/null | xargs -r rm -rf && \
    echo "INFO: Compacting Cygnus jar files..." && \
    # Some class needs to pass file because bytecode errors of old jar dependencies
    pack200 -J-Xmx512m -J-XX:MaxPermSize=128m --effort=5 --segment-limit=786432 --modification-time=latest --pass-file=org/apache/hadoop/hive/shims/Hadoop23Shims.class \
      ${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar.pack.gz \
      ${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar && \
    pack200 -J-Xmx512m -J-XX:MaxPermSize=128m --effort=5 --segment-limit=786432 --modification-time=latest --pass-file=org/apache/hadoop/hive/shims/Hadoop23Shims.class \
      ${FLUME_HOME}/plugins.d/cygnus/lib/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar.pack.gz \
      ${FLUME_HOME}/plugins.d/cygnus/lib/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar && \
    rm -f ${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar && \
    rm -f ${FLUME_HOME}/plugins.d/cygnus/lib/cygnus-ngsi-${CYGNUS_VERSION}-jar-with-dependencies.jar && \
    echo "INFO: Copy some files needed for starting cygnus-ngsi" && \
    cp -p ${CYGNUS_HOME}/docker/cygnus-ngsi/cygnus-entrypoint.sh / && \
    cp -p ${CYGNUS_HOME}/docker/cygnus-ngsi/agent.conf ${FLUME_HOME}/conf/ && \
    cp -p ${CYGNUS_HOME}/docker/cygnus-ngsi/cartodb_keys.conf ${FLUME_HOME}/conf/

# Define the entry point
ENTRYPOINT ["/cygnus-entrypoint.sh"]

# Ports used by cygnus-ngsi
EXPOSE ${CYGNUS_SERVICE_PORT} ${CYGNUS_API_PORT}

