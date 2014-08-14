#!/bin/bash

# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
# 
# This file is part of fiware-connectors (FI-WARE project).
# 
# cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
# 
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
# 
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# frb@tid.es

#################################################
###       Main script for Cygnus RPM Stage    ###
#################################################

function download_flume(){
	# download form artifactory and unzip it into ${RPM_BASE_DIR}
	_logStage "######## Preparing the apache-flume component... ########"

	TMP_DIR="tmp_deleteme"
	mkdir -p ${TMP_DIR}
	pushd ${TMP_DIR} &> /dev/null
	FLUME_TAR="apache-flume-1.4.0-bin.tar.gz"
	FLUME_WO_TAR="apache-flume-1.4.0-bin"

	_log "#### The version of the component is (${FLUME_TAR}) ####"
	ARTIFACT_FLUME_URL="http://archive.apache.org/dist/flume/1.4.0/"
	_log "#### BASE_DIR = $BASE_DIR ####"
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

	_log "#### Cleaning the temporal folders... ####"
	rm -rf ${RPM_SOURCE_DIR}/${FLUME_WO_TAR}
	rm -rf ${FLUME_WO_TAR}/docs # erase flume documentation
	rm -rf ${RPM_PRODUCT_SOURCE_DIR}
	mkdir -p ${RPM_PRODUCT_SOURCE_DIR}
	cp -R ${FLUME_WO_TAR}/* ${RPM_PRODUCT_SOURCE_DIR}/
	popd &> /dev/null
	rm -rf ${TMP_DIR}
	return 0

	_logStage "######## The apache-flume is ready for use! ... ########"
}

function copy_cygnus_to_flume(){
	_logStage "######## Copying the cygnus jar into the apache-flume... ########"
	mkdir -p ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus
	mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
	mkdir ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/libext
	cp $BASE_DIR/target/cygnus-${PRODUCT_VERSION}-jar-with-dependencies.jar ${RPM_PRODUCT_SOURCE_DIR}/plugins.d/cygnus/lib
}

function copy_cygnus_conf() {
	_logStage "######## Copying cygnus template as conf file... ########"
	cp $BASE_DIR/conf/cygnus.conf.template ${RPM_SOURCE_DIR}/config/cygnus.conf
}

usage() {
    SCRIPT=$(basename $0)

    printf "\n" >&2
    printf "usage: ${SCRIPT} [options] \n" >&2
    printf "\n" >&2
    printf "Options:\n" >&2
    printf "\n" >&2
    printf "    -h                    show usage\n" >&2
    printf "    -v VERSION            Mandatory parameter. Version for rpm product preferably in format x.y.z \n" >&2
    printf "    -r RELEASE            Optional parameter. Release for product. I.E. 0.ge58dffa \n" >&2
    printf "\n" >&2
    exit 1
}

while getopts ":v:r:h" opt

do
    case $opt in
        v)
            VERSION_ARG=${OPTARG}
            ;;
        r)
            RELEASE_ARG=${OPTARG}
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



# Import the colors for deployment script
source colors_shell.sh

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


_logStage "######## Setting the environment... ########"

# Setting folders this scrtipt is in neore/scripts BASE_DIR. Note: $0/.. is CWD
BASE_DIR=$(python -c 'import os,sys;print os.path.realpath(sys.argv[1])' $0/../../..)
_log "#### BASE_DIR = $BASE_DIR ####"

RPM_BASE_DIR="${BASE_DIR}/neore/rpm"
_log "#### RPM_BASE_DIR = $RPM_BASE_DIR ####"

RPM_SOURCE_DIR="${RPM_BASE_DIR}/SOURCES"
_log "#### RPM_SOURCE_DIR = $RPM_SOURCE_DIR ####"

 RPM_PRODUCT_SOURCE_DIR="${RPM_SOURCE_DIR}/usr/cygnus/"
_log "#### RPM_PRODUCT_SOURCE_DIR=$RPM_PRODUCT_SOURCE_DIR ####"

_log "#### Creating output folder ####"
[[ -d "${BASE_DIR}/target" ]] || mkdir -p "${BASE_DIR}/target"

_log "#### Iterate over every SPEC file ####"
if [[ -d "${RPM_BASE_DIR}" ]]; then

	download_flume
	[[ $? -ne 0 ]] && exit 1

	copy_cygnus_to_flume
	[[ $? -ne 0 ]] && _logError "Cygnus copy has failed. Did you run 'mvn clean compile assembly:single'? Does the version in pom.xml file match $PRODUCT_VERSION?" && exit 1

	copy_cygnus_conf
	[[ $? -ne 0 ]] && exit 1

	_logStage "######## Executing the rpmbuild ... ########"
	for SPEC_FILE in $(find "${RPM_BASE_DIR}" -type f -name *.spec)
	do
		_log "#### Packaging using: ${SPEC_FILE}... ####"
		# Execute command to create RPM
		RPM_BUILD_COMMAND="rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} "
		_log "Rpm construction command: ${RPM_BUILD_COMMAND}"
		rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} 
		_logStage "######## rpmbuild finished! ... ########"
	done

# _logStage "######## Moving to target folder... ########"
# find "${RPM_BASE_DIR}/RPMS" -type f -name '*.rpm' -exec cp {} "${BASE_DIR}/target" \;
fi

_logStage "######## RPM Stage Finished! ... ########"
