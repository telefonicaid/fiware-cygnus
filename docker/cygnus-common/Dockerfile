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

# FIXME: this PR is based in CentOS 8, which has reached End of Life. It should be migrated to Debian,
# as we did with cygnus-ngsi Dockerfile. Basically, the changes done in PR https://github.com/telefonicaid/fiware-cygnus/pull/2152
# should be also applied here

FROM centos:centos8.4.2105

MAINTAINER Francisco Romero Bueno <francisco.romerobueno@telefonica.com>

# Environment variables
ENV CYGNUS_USER "cygnus"
ENV CYGNUS_HOME "/opt/fiware-cygnus"
ENV CYGNUS_VERSION "3.16.0-SNAPSHOT"
ENV CYGNUS_CONF_PATH "/opt/apache-flume/conf"
ENV CYGNUS_CONF_FILE "/opt/apache-flume/conf/agent.conf"
ENV CYGNUS_AGENT_NAME "cygnus-common"
ENV CYGNUS_LOG_LEVEL "INFO"
ENV CYGNUS_LOG_APPENDER "console"
ENV CYGNUS_SERVICE_PORT "5050"
ENV CYGNUS_API_PORT "8081"

# FIXME: this image could be obsolete. If re-activate, then probably the changes done in 
# PR https://github.com/telefonicaid/fiware-cygnus/pull/1801 should be also done in this Dockerfile
# NOTE: Configure correctly GIT_URL_CYGNUS and GIT_REV_CYGNUS for each git branch/fork used
ENV GIT_URL_CYGNUS "https://github.com/telefonicaid/fiware-cygnus.git"
ENV GIT_REV_CYGNUS "master"

ENV MVN_VER "3.5.4"
ENV MVN_TGZ "apache-maven-${MVN_VER}-bin.tar.gz"
ENV MVN_URL "http://www.eu.apache.org/dist/maven/maven-3/${MVN_VER}/binaries/${MVN_TGZ}"
ENV MVN_HOME "/opt/apache-maven"

ENV FLUME_VER "1.11.0"
ENV FLUME_TGZ "apache-flume-${FLUME_VER}-bin.tar.gz"
ENV FLUME_URL "http://archive.apache.org/dist/flume/${FLUME_VER}/${FLUME_TGZ}"
ENV FLUME_HOME "/opt/apache-flume"

ENV JAVA_VERSION "1.11.0"

# Add Cygnus user
RUN adduser ${CYGNUS_USER}

# Install
RUN \
    # FIXME: default yum repositories has been changed to vault due to CentOS 8 EOL on February 1st, 2022
    # This is just a temporal solution so the build doesn't break while we find a new distro to use
    sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-Linux-* &&\
    sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-Linux-* &&\
    yum upgrade -y && \
    yum -y install tar nc which git java-${JAVA_VERSION}-openjdk-devel && \
    yum clean all && \
    export JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk && \
    curl --remote-name --location --insecure --silent --show-error "${MVN_URL}" && \
    tar xzvf ${MVN_TGZ} && \
    mv apache-maven-${MVN_VER} ${MVN_HOME} && \
    export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8" && \
    curl --remote-name --location --insecure --silent --show-error "${FLUME_URL}" && \
    tar zxf ${FLUME_TGZ} && \
    mv apache-flume-${FLUME_VER}-bin ${FLUME_HOME} && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus/lib && \
    mkdir -p ${FLUME_HOME}/plugins.d/cygnus/libext && \
    chown -R cygnus:cygnus ${FLUME_HOME} && \
    # Make a shallow clone to help reduce final image size
    git clone ${GIT_URL_CYGNUS} ${CYGNUS_HOME} && \
    cd ${CYGNUS_HOME} && \
    git checkout ${GIT_REV_CYGNUS} && \
    # Build and install cygnus-common
    cd ${CYGNUS_HOME}/cygnus-common && \
    ${MVN_HOME}/bin/mvn clean compile exec:exec assembly:single && \
    cp target/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar ${FLUME_HOME}/plugins.d/cygnus/libext/ && \
    ${MVN_HOME}/bin/mvn install:install-file -Dfile=${FLUME_HOME}/plugins.d/cygnus/libext/cygnus-common-${CYGNUS_VERSION}-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=${CYGNUS_VERSION} -Dpackaging=jar -DgeneratePom=false && \
    # Install Cygnus Application script
    cp ${CYGNUS_HOME}/cygnus-common/target/classes/cygnus-flume-ng ${FLUME_HOME}/bin/ && \
    chmod +x ${FLUME_HOME}/bin/cygnus-flume-ng && \
    # Instantiate some configuration files
    cp ${CYGNUS_HOME}/cygnus-common/conf/log4j2.properties.template ${FLUME_HOME}/conf/log4j2.properties && \
    # Create Cygnus log folder
    mkdir /var/log/cygnus && \
    # Cleanup to thin the final image
    cd ${CYGNUS_HOME}/cygnus-common && \
    ${MVN_HOME}/bin/mvn clean && \
    rm -rf /root/.m2 && \
    bash -c 'find /usr/share/locale -maxdepth 1 -mindepth 1 -type d | grep -v -e "en_US" | xargs rm -rfv' && \
    bash -c 'localedef --list-archive | grep -v -e "en_US" | xargs localedef --delete-from-archive' && \
    mv -f /usr/lib/locale/locale-archive /usr/lib/locale/locale-archive.tmpl && \
    build-locale-archive && \
    rm -f /opt/${MVN_TGZ} && \
    rm -f /opt/${FLUME_TGZ}

# Copy some files needed for starting cygnus-common
COPY cygnus-entrypoint.sh /
COPY agent.conf ${FLUME_HOME}/conf/

# Define the entry point
ENTRYPOINT ["/cygnus-entrypoint.sh"]

# Ports used by cygnus-common
EXPOSE ${CYGNUS_SERVICE_PORT}
EXPOSE ${CYGNUS_API_PORT}
