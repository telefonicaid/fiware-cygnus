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
	rm -rf ${FLUME_WO_TAR}/docs # erase flume documentation
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

usage() {
    SCRIPT=$(basename $0)

    printf "\n" >&2
    printf "usage: ${SCRIPT} [options] \n" >&2
    printf "\n" >&2
    printf "Options:\n" >&2
    printf "\n" >&2
    printf "    -h                    show usage\n" >&2
    printf "    -v VERSION            version for rpm product preferably in format x.y.z \n" >&2
    printf "    -r RELEASE            release for product. I.E. 0.ge58dffa \n" >&2
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
	[[ $? -ne 0 ]] && exit 1

	_logStage "######## Executing the rpmbuild ... ########"
	for SPEC_FILE in $(find "${RPM_BASE_DIR}" -type f -name *.spec)
	do
		_log "#### Packaging using: ${SPEC_FILE}... ####"
		# Execute command to create RPM
		RPM_BUILD_COMMAND="rpmbuild -v -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_RELEASE} "
		_log "Rpm construction command: ${RPM_BUILD_COMMAND}"
		${RPM_BUILD_COMMAND}
		_logStage "######## rpmbuild finished! ... ########"
	done

# _logStage "######## Moving to target folder... ########"
# find "${RPM_BASE_DIR}/RPMS" -type f -name '*.rpm' -exec cp {} "${BASE_DIR}/target" \;
fi

_logStage "######## RPM Stage Finished! ... ########"
