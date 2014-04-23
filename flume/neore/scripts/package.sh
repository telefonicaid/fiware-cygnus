#!/bin/bash

function download_flume(){
	# donwload Vert.x zip form artifactory and unzip it into ${RPM_BASE_DIR}
	TMP_DIR="tmp_deleteme"
	mkdir -p ${TMP_DIR}
	pushd ${TMP_DIR} &> /dev/null
	FLUME_TAR="apache-flume-1.4.0-bin.tar.gz"
	FLUME_WO_TAR=${FLUME_TAR%.*}
	ARTIFACTORY_FLUME_URL="http://artifactory.hi.inet/artifactory/simple/common/flume/"
	echo -n "Downloading apache-flume: ${FLUME_TAR}... "
	curl -s -o ${FLUME_TAR} ${ARTIFACTORY_FLUME_URL}/${FLUME_TAR}
	if [[ $? -ne 0 ]]; then
			echo "ERROR: cannot download apache-flume.tar.gz (${FLUME_TAR}) from ${ARTIFACTORY_FLUME_URL}"
			return 1
		else
			echo "done!"
		fi

		echo -n "Uncompresing apache-flume: ${FLUME_TAR}... "
	tar xvzf ${FLUME_TAR} &> /dev/null
	if [[ $? -ne 0 ]]; then
		echo "ERROR: cannot untar flume.tar.gz (${FLUME_TAR})"
		return 1
	else
		echo "done!"
	fi

	rm -rf ${RPM_SOURCE_PRODUCT_DIR}/${FLUME_WO_TAR}
	mkdir -p ${RPM_SOURCE_PRODUCT_DIR}
	mv ${VERTX_WO_ZIP} ${RPM_SOURCE_PRODUCT_DIR}
	cd ${RPM_SOURCE_PRODUCT_DIR}; ln -s ${VERTX_WO_ZIP} vert.x
	popd &> /dev/null
	rm -rf ${TMP_DIR}
	return 0
}

# Setting folders this scrtipt is in neore/scripts BASE_DIR. Note: $0/.. is CWD
BASE_DIR=$(python -c 'import os,sys;print os.path.realpath(sys.argv[1])' $0/../../..)

# Create output folder
[[ -d "${BASE_DIR}/target" ]] || mkdir -p "${BASE_DIR}/target"

RPM_BASE_DIR="${BASE_DIR}/neore/rpm"
RPM_SOURCE_DIR="${RPM_BASE_DIR}/SOURCES"
RPM_SOURCE_PRODUCT_DIR="$RPM_SOURCE_DIR/opt/cygnus"

# Iterate over every SPEC file
if [[ -d "${RPM_BASE_DIR}" ]]; then

	download_flume

	[[ $? -ne 0 ]] && exit 1

	for SPEC_FILE in $(find "${RPM_BASE_DIR}" -type f -name *.spec)
	do
		echo "Packaging using: ${SPEC_FILE}..."
		# Execute command to create RPM
		echo "rpmbuild -v --clean -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_REALEASE}"
		rpmbuild -v --clean -ba ${SPEC_FILE} --define '_topdir '${RPM_BASE_DIR} --define '_product_version '${PRODUCT_VERSION} --define '_product_release '${PRODUCT_REALEASE}
	done