#!/bin/bash

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
	ARTIFACTORY_FLUME_URL="http://artifactory.hi.inet/artifactory/simple/common/flume/"
	_log "#### BASE_DIR = $BASE_DIR ####"
	_log "#### Downloading apache-flume: ${FLUME_TAR}... ####"
	curl -s -o ${FLUME_TAR} ${ARTIFACTORY_FLUME_URL}/${FLUME_TAR}
	if [[ $? -ne 0 ]]; then
			_logError "cannot download apache-flume.tar.gz (${FLUME_TAR}) from ${ARTIFACTORY_FLUME_URL}"
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

	_log "#### Cleaning the temporal folders... ####"
	rm -rf ${RPM_SOURCE_DIR}/${FLUME_WO_TAR}
	mkdir -p ${RPM_PRODUCT_SOURCE_DIR}
	cp -R ${FLUME_WO_TAR}/* ${RPM_PRODUCT_SOURCE_DIR}/
	popd &> /dev/null
	rm -rf ${TMP_DIR}
	return 0

	_logStage "######## The apache-flume is ready for use! ... ########"
}

function copy_cygnus_to_flume(){
	_logStage "######## Copying the cygnus jar into the apache-flume... ########"
	cp $BASE_DIR/target/*.jar ${RPM_PRODUCT_SOURCE_DIR}/lib/
}

# Import the colors for deployment script
source ./colors_shell.sh

_logStage "######## Starting the RPM stage... ########"

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
	[[ $? -ne 0 ]] && exit 1

	_logStage "######## Executing the rpmbuild ... ########"
	for SPEC_FILE in $(find "${RPM_BASE_DIR}" -type f -name *.spec)
	do
		_log "#### Packaging using: ${SPEC_FILE}... ####"
		# Execute command to create RPM
		_log "#### Rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version 0.1' ####"
		rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version 0.1'
		_logStage "######## rpmbuild finished! ... ########"
	done

# _logStage "######## Moving to target folder... ########"
# find "${RPM_BASE_DIR}/RPMS" -type f -name '*.rpm' -exec cp {} "${BASE_DIR}/target" \;
fi

_logStage "######## RPM Stage Finished! ... ########"