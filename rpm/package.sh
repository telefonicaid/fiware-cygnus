#!/usr/bin/env bash

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
    # download artifact from external URL and unzip it into ${RPM_BASE_DIR}
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

    
    _log "### Disable httpclient and httpcore libraries distributed within apache-flume bundle... ###"
    mv ${FLUME_WO_TAR}/lib/httpclient-4.2.1.jar ${FLUME_WO_TAR}/lib/httpclient-4.2.1.jar.old
    mv ${FLUME_WO_TAR}/lib/httpcore-4.2.1.jar ${FLUME_WO_TAR}/lib/httpcore-4.2.1.jar.old

    _log "### Disable the bundled version of libthrift within Apache Flume... ###"
    mv ${FLUME_WO_TAR}/lib/libthrift-0.7.0.jar ${FLUME_WO_TAR}/lib/libthrift-0.7.0.old

    _log "#### Cleaning the temporal folders... ####"
    rm -rf ${RPM_SOURCE_DIR}/${FLUME_WO_TAR}
    rm -rf ${FLUME_WO_TAR}/docs # erase flume documentation
    rm ${FLUME_WO_TAR}/conf/flume-conf.properties.template # we will add our own templates
    rm ${FLUME_WO_TAR}/conf/log4j.properties # we will add our own templates
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
    rm -rf ${RPM_PRODUCT_SOURCE_DIR}/bin/cygnus-flume-ng
    cp ${BASE_DIR}/cygnus-common/src/main/resources/cygnus-flume-ng ${RPM_PRODUCT_SOURCE_DIR}/bin
}

function copy_cygnus_jar_to_flume_directory(){
    local component_name=${1}
    _logStage "######## Copying the ${component_name} jar into the apache-flume... ########"
    mkdir -p ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus
    if [[ ${component_name} == "cygnus-common" ]]; then
        mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/libext
        cp $BASE_DIR/${component_name}/target/${component_name}-${PRODUCT_VERSION}-jar-with-dependencies.jar ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/libext
    elif [[ ${component_name} == "cygnus-ngsi" ]]; then
        mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
        cp $BASE_DIR/${component_name}/target/${component_name}-${PRODUCT_VERSION}-jar-with-all-dependencies.jar ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
    else
        mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
        cp $BASE_DIR/${component_name}/target/${component_name}-${PRODUCT_VERSION}-jar-with-dependencies.jar ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
    fi
}

function copy_cygnus_conf() {
    local component_name=${1}
    _logStage "######## Copying ${component_name} template config files to destination config directory... ########"
    rm -rf ${RPM_SOURCE_DIR}/config 
    mkdir -p ${RPM_SOURCE_DIR}/config
    cp ${BASE_DIR}/${component_name}/conf/* ${RPM_SOURCE_DIR}/config/ # templates are copied
    if [[ -f ${RPM_SOURCE_DIR}/config/log4j.properties.template ]]; then
        mv ${RPM_SOURCE_DIR}/config/log4j.properties.template ${RPM_SOURCE_DIR}/config/log4j.properties
    fi
    mv ${RPM_SOURCE_DIR}/config/README.md ${RPM_SOURCE_DIR}/config/README-${component_name}.md 
}

function clean_up_previous_builds() {
    _logStage "######## Cleaning up previous builds of rpm... ########"
    rm -rf ${RPM_BASE_DIR}/{RPMS,BUILDROOT,BUILD,SRPMS}
    rm -rf ${RPM_SOURCE_DIR}/{config,usr}
    return 0
}

function get_name_suffix() {
    # At this moment Cygnus package name only has a Hadoop core version
    HADOOP_VERSION=$(grep -A1 "hadoop-core" ${BASE_DIR}/cygnus-common/pom.xml | tail -1 | tr -d "</version> ")
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
    printf "    -r RELEASE            Mandatory parameter. Release for product. I.E. 0.ge58dffa \n" >&2
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
BASE_DIR=$(python -c 'import os,sys;print os.path.realpath(sys.argv[1])' $0/../..)

# Import the colors for deployment script
source ${BASE_DIR}/rpm/colors_shell.sh

# check user
if [[ $(id -u) == "0" ]]; then
	_logError "${0}: shouldn't be executed as root"
	exit 1
fi


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
    _logError "A product release must be specified with -r parameter."
    usage
    exit 2
fi

if [[ -z ${ARTIFACT_URL} ]]; then
	ARTIFACT_URL="http://archive.apache.org/dist/flume/1.4.0/"
fi

if [[ -z ${ARTIFACT_NAME} ]]; then
	ARTIFACT_NAME="apache-flume-1.4.0-bin.tar.gz"
fi

_logStage "######## Setting the environment... ########"


_log "#### Iterate over every SPEC file ####"

for SPEC_FILE in $(find "${BASE_DIR}" -type f -name *.spec)
do

    RPM_BASE_DIR="$(dirname ${SPEC_FILE})"
    RPM_BASE_DIR=${RPM_BASE_DIR%*SPECS} # remove trailing SPECS 
    RPM_SOURCE_DIR="${RPM_BASE_DIR}/SOURCES"
    RPM_PRODUCT_SOURCE_DIR="${RPM_SOURCE_DIR}/usr/cygnus/"

    CYGNUS_COMPONENT_NAME=${RPM_BASE_DIR%*/spec/}
    CYGNUS_COMPONENT_NAME=${CYGNUS_COMPONENT_NAME##*/}


    clean_up_previous_builds 

    if [[ ${CYGNUS_COMPONENT_NAME} == "cygnus-common" ]]; then 

        download_flume ${ARTIFACT_URL} ${ARTIFACT_NAME}
        [[ $? -ne 0 ]] && exit 1

        copy_cygnus_startup_script
        [[ $? -ne 0 ]] && _logError "Cygnus startup script copy has failed. Did you run 'mvn clean compile exec:exec assembly:single'? Does the version in pom.xml file match $PRODUCT_VERSION?" && exit 1

        get_name_suffix
        [[ $? -ne 0 ]] && _logError "Can't get the name suffix" && exit 1

    else 
        # dummy NAME_SUFFIX
        NAME_SUFFIX="dummy_suffix"
    fi

    copy_cygnus_jar_to_flume_directory ${CYGNUS_COMPONENT_NAME}
    [[ $? -ne 0 ]] && _logError "Cygnus jar copy has failed. Did you run 'mvn clean compile exec:exec assembly:single'? Does the version in pom.xml file match $PRODUCT_VERSION?" && exit 1

    copy_cygnus_conf ${CYGNUS_COMPONENT_NAME}
    [[ $? -ne 0 ]] && exit 1

    _logStage "######## Executing the rpmbuild ... ########"

    _log "#### Packaging using: ${SPEC_FILE}... ####"
    # Execute command to create RPM
    RPM_BUILD_COMMAND="rpmbuild -v -bb ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} --define '_name_suffix '${NAME_SUFFIX}"
    _log "Rpm construction command: ${RPM_BUILD_COMMAND}"
    rpmbuild -v -bb ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} --define '_name_suffix '${NAME_SUFFIX}
    _logStage "######## rpmbuild finished! ... ########"
done

_logStage "######## RPM Stage Finished! ... ########"
