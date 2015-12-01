#!/bin/bash

# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
# 
# This file is part of fiware-cygnus (FI-WARE project).
# 
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
# 
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
# 
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es

#################################################
###       Main script for Cygnus RPM Stage    ###
#################################################

function download_flume(){
    # download form artifactory and unzip it into ${RPM_BASE_DIR}
    _logStage "######## Preparing the apache-flume component... ########"

    local ARTIFACT_FLUME_URL=${1}
    local FLUME_TAR=${2}

    local TMP_DIR="tmp_deleteme"
    mkdir -p ${TMP_DIR}
    pushd ${TMP_DIR} &> /dev/null
    #remove .tar.gz so twice is executed
    local FLUME_WO_TAR=${FLUME_TAR%.*}
    FLUME_WO_TAR=${FLUME_WO_TAR%.*}


    _log "#### The version of the component is (${FLUME_TAR}) ####"
    _log "#### Downloading apache-flume: ${FLUME_TAR}... ####"
    curl -s -o ${FLUME_TAR} ${ARTIFACT_FLUME_URL}/${FLUME_TAR}
    if [[ $? -ne 0 ]]; then
        _logError "cannot download apache-flume.tar.gz (${FLUME_TAR}) from ${ARTIFACT_FLUME_URL}"
        return 1
    else
        _logOk ".............. Done! .............."
    fi

    _log "#### Uncompresing apache-flume: ${FLUME_TAR}... ####"
    tar xvzf ${FLUME_TAR} &> /dev/null
    if [[ $? -ne 0 ]]; then
        _logError ".............. Cannot untar flume.tar.gz (${FLUME_TAR}) .............."
        return 1
    else
        _logOk ".............. Done! .............."
    fi

    _log "### Download libthrift patch... ###"
    ARTIFACT_LIBTHRIFT_URL="http://repo1.maven.org/maven2/org/apache/thrift/libthrift/0.9.1/"
    LIBTHRIFT_JAR=libthrift-0.9.1.jar 
    curl -s -o ${LIBTHRIFT_JAR} ${ARTIFACT_LIBTHRIFT_URL}/${LIBTHRIFT_JAR}
    if [[ $? -ne 0 ]]; then
        _logError "cannot download libthrift jar (${LIBTHRIFT_JAR}) from ${ARTIFACT_LIBTHRIFT_URL}"
        return 1
    else
        _logOk ".............. Done! .............."
    fi
    rm -f ${FLUME_WO_TAR}/lib/libthrift-*.jar
    mv ${LIBTHRIFT_JAR} ${FLUME_WO_TAR}/lib

    _log "### Disable httpclient and httpcore libraries distributed within apache-flume bundle... ###"
    mv ${FLUME_WO_TAR}/lib/httpclient-4.2.1.jar ${FLUME_WO_TAR}/lib/httpclient-4.2.1.jar.old
    mv ${FLUME_WO_TAR}/lib/httpcore-4.2.1.jar ${FLUME_WO_TAR}/lib/httpcore-4.2.1.jar.old

    _log "#### Cleaning the temporal folders... ####"
    rm -rf ${RPM_SOURCE_DIR}/${FLUME_WO_TAR}
    rm -rf ${FLUME_WO_TAR}/docs # erase flume documentation
    rm ${FLUME_WO_TAR}/conf/flume-conf.properties.template # we will add our own templates
    rm -rf ${RPM_PRODUCT_SOURCE_DIR}
    mkdir -p ${RPM_PRODUCT_SOURCE_DIR}
    cp -R ${FLUME_WO_TAR}/* ${RPM_PRODUCT_SOURCE_DIR}/
    popd &> /dev/null
    rm -rf ${TMP_DIR}
    return 0

    _logStage "######## The apache-flume is ready for use! ... ########"
}

function copy_cygnus_startup_script(){
    _logStage "######## Copying the cygnus startup script into the apache-flume... ########"
    rm ${RPM_PRODUCT_SOURCE_DIR}/bin/flume-ng
    cp $BASE_DIR/target/classes/cygnus-flume-ng ${RPM_PRODUCT_SOURCE_DIR}/bin
}

function copy_cygnus_to_flume(){
    _logStage "######## Copying the cygnus jar into the apache-flume... ########"
    mkdir -p ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus
    mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
    mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/libext
    cp $BASE_DIR/target/cygnus-${PRODUCT_VERSION}-jar-with-dependencies.jar ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
}

function copy_cygnus_conf() {
    _logStage "######## Copying cygnus template config files to destination config directory... ########"
    rm -rf {RPM_SOURCE_DIR}/config 
    mkdir -p ${RPM_SOURCE_DIR}/config
    cp ${BASE_DIR}/conf/* ${RPM_SOURCE_DIR}/config/
}

function clean_up_previous_builds() {
    _logStage "######## Cleaning up previous builds of rpm... ########"
    rm -rf ${RPM_BASE_DIR}/{RPMS,BUILDROOT,BUILD,SRPMS}
    rm -rf ${RPM_SOURCE_DIR}/{config,usr}
    return 0
}

function get_name_suffix() {
    # At this moment Cygnus package name only has a Hadoop core version
    HADOOP_VERSION=$(grep -A1 "hadoop-core" ${BASE_DIR}/pom.xml | tail -1 | tr -d "</version> ")
    if [[ -z "${HADOOP_VERSION}" || ${HADOOP_VERSION} == "" ]]; then
        return 1
    fi
    NAME_SUFFIX="_hadoopcore_${HADOOP_VERSION}"
}

function usage() {
    SCRIPT=$(basename $0)

    printf "\n" >&2
    printf "usage: ${SCRIPT} [options] \n" >&2
    printf "\n" >&2
    printf "Options:\n" >&2
    printf "\n" >&2
    printf "    -h                    show usage\n" >&2
    printf "    -v VERSION            Mandatory parameter. Version for rpm product preferably in format x.y.z \n" >&2
    printf "    -r RELEASE            Optional parameter. Release for product. I.E. 0.ge58dffa \n" >&2
    printf "    -u ARTIFACT_URL       Optional parameter. Url to server that contains flume package. Default value is http://archive.apache.org/dist/flume/1.4.0/ \n" >&2
    printf "    -a ARTIFACT_NAME      Optional parameter. Artifact name. Default value is apache-flume-1.4.0-bin.tar.gz. It is important that artifact be in .tar.gz format\n" >&2
    printf "\n" >&2
    exit 1
}

while getopts ":v:r:u:a:h" opt

do
    case $opt in
        v)
            VERSION_ARG=${OPTARG}
            ;;
        r)
            RELEASE_ARG=${OPTARG}
            ;;
        u)
            ARTIFACT_URL=${OPTARG}
            ;;
        a)
            ARTIFACT_NAME=${OPTARG}
            ;;
        h)
            usage
            ;;
        *)
            echo "invalid argument: '${OPTARG}'"
            exit 1
            ;;
    esac
done

# Setting folders this scrtipt is in neore/scripts BASE_DIR. Note: $0/.. is CWD
BASE_DIR=$(python -c 'import os,sys;print os.path.realpath(sys.argv[1])' $0/../../..)
RPM_BASE_DIR="${BASE_DIR}/neore/rpm"
RPM_SOURCE_DIR="${RPM_BASE_DIR}/SOURCES"
RPM_PRODUCT_SOURCE_DIR="${RPM_SOURCE_DIR}/usr/cygnus/"

# Import the colors for deployment script
source ${BASE_DIR}/neore/scripts/colors_shell.sh

_log "#### BASE_DIR = $BASE_DIR ####"
_log "#### RPM_BASE_DIR = $RPM_BASE_DIR ####"
_log "#### RPM_SOURCE_DIR = $RPM_SOURCE_DIR ####"
_log "#### RPM_PRODUCT_SOURCE_DIR=$RPM_PRODUCT_SOURCE_DIR ####"

_log "#### Creating output folder ####"
[[ -d "${BASE_DIR}/target" ]] || mkdir -p "${BASE_DIR}/target"

# check user

if [[ $(id -u) == "0" ]]; then
	_logError "${0}: shouldn't be executed as root"
	exit 1
fi

describe_tags="$(git describe --tags --long 2>/dev/null)"
GIT_PRODUCT_RELEASE="${describe_tags#*-}"
GIT_PRODUCT_RELEASE="${GIT_PRODUCT_RELEASE#*-}"
GIT_PRODUCT_RELEASE=$(echo ${GIT_PRODUCT_RELEASE} | sed -e "s@-@.@g")


if [[ ! -z ${VERSION_ARG} ]]; then
	PRODUCT_VERSION=${VERSION_ARG}
else
	_logError "A product version must be specified with -v parameter."
	usage
	exit 2
fi

if [[ ! -z ${RELEASE_ARG} ]]; then
	PRODUCT_RELEASE=${RELEASE_ARG}
else
	PRODUCT_RELEASE=${GIT_PRODUCT_RELEASE}
fi

if [[ -z ${ARTIFACT_URL} ]]; then
	ARTIFACT_URL="http://archive.apache.org/dist/flume/1.4.0/"
fi

if [[ -z ${ARTIFACT_NAME} ]]; then
	ARTIFACT_NAME="apache-flume-1.4.0-bin.tar.gz"
fi

_logStage "######## Setting the environment... ########"


_log "#### Iterate over every SPEC file ####"
if [[ -d "${RPM_BASE_DIR}" ]]; then

	clean_up_previous_builds 

	download_flume ${ARTIFACT_URL} ${ARTIFACT_NAME}
	[[ $? -ne 0 ]] && exit 1

    copy_cygnus_startup_script
    [[ $? -ne 0 ]] && _logError "Cygnus startup script copy has failed. Did you run 'mvn clean compile exec:exec assembly:single'? Does the version in pom.xml file match $PRODUCT_VERSION?" && exit 1

	copy_cygnus_to_flume
	[[ $? -ne 0 ]] && _logError "Cygnus jar copy has failed. Did you run 'mvn clean compile exec:exec assembly:single'? Does the version in pom.xml file match $PRODUCT_VERSION?" && exit 1

	copy_cygnus_conf
	[[ $? -ne 0 ]] && exit 1

        get_name_suffix
        [[ $? -ne 0 ]] && _logError "Can't get the name suffix" && exit 1
 
	_logStage "######## Executing the rpmbuild ... ########"
	rm -rf ${RPM_BASE_DIR}/BUILD
	rm -rf ${RPM_BASE_DIR}/BUILDROOT
	for SPEC_FILE in $(find "${RPM_BASE_DIR}" -type f -name *.spec)
	do
		_log "#### Packaging using: ${SPEC_FILE}... ####"
		# Execute command to create RPM
		RPM_BUILD_COMMAND="rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} --define '_name_suffix '${NAME_SUFFIX} "
		_log "Rpm construction command: ${RPM_BUILD_COMMAND}"
		rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} --define '_name_suffix '${NAME_SUFFIX} 
		_logStage "######## rpmbuild finished! ... ########"
	done

# _logStage "######## Moving to target folder... ########"
# find "${RPM_BASE_DIR}/RPMS" -type f -name '*.rpm' -exec cp {} "${BASE_DIR}/target" \;
fi

_logStage "######## RPM Stage Finished! ... ########"
