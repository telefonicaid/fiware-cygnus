#!/bin/bash

# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
# 
# This file is part of fiware-cygnus (FI-WARE project).
# 
# cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
# 
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
# 
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es

#############################################
###   Script for check the cygnus RPM   ###
#############################################

# Define the color log

source ${WORKSPACE}/flume/neore/scripts/colors_shell.sh

_logStage "######## Executing the Check RPM Stage ... ########"
echo ""

## Install RPM of cygnus
cd ${WORKSPACE}/flume/neore/rpm/RPMS/x86_64

_logStage "######## Installing RPM cygnus ... ########"
sudo rpm -Uvh cygnus-*.rpm
if [[ $? -eq 0 ]]; then
	_logOk ".............. cygnus RPM installed .............."
else
	_logError ".............. cygnus RPM NOT installed .............." 
	exit 1
fi
echo ""

## Check the if the user cygnus is created
_logStage "######## Checking the user configuration... ########"
if [ "`cat /etc/passwd | grep cygnus`" == "" ]; then
	_logError ".............. user cygnus is NOT created .............."
	exit 1
else
	_logOk ".............. user cygnus is created .............." 
fi
echo ""

## Check if exist the directory app in the home directory of the cygnus
_logStage "######## Check cygnus directories... ########"
if [[ ! -d /usr/cygnus/ ]]; then
	_logError ".............. the directories are NOT created well .............."
	exit 1
else
	_logOk ".............. the directories are created well .............."
fi 
echo ""

## Start the cygnus service
_logStage "######## Starting the cygnus service... ########"
sudo service cygnus start
if [[ $? -eq 0 ]]; then
	_logOk ".............. cygnus service is started .............."
else
	_logError ".............. cygnus service NOT started .............." 
	exit 1
fi
echo ""

## Check if the cygnus service is running
_logStage "######## Checking the cygnus service ########"
if [ "`sudo /etc/init.d/cygnus status | grep pid`" == "" ]; then
	_logError ".............. cygnus service is NOT running .............."
	exit 1
else
	_logOk ".............. cygnus service is running .............." 
fi
echo ""

## Check if the cygnus is listen the specific ports
_logStage "######## Checking the cygnus service ########"
if [ "`sudo netstat -putan | grep 5050`" == "" ]; then
	_logError ".............. cygnus is not LISTENING .............."
	exit 1
else
	_logOk ".............. cygnus is LISTENING the correct ports! .............." 
fi
echo ""

## Check END-TO-END for the CCB service
_logStage "######## Starting the END-TO-END Check ########"
curl -i localhost:5050/version
if [[ $? -eq 0 ]]; then
    _logOk ".............. The END-TO-END Check of the cygnus is OK .............."
else
    _logError ".............. The END-TO-END Check failed .............." 
    exit 1
fi
echo ""

# Stop the cygnus service 
_logStage "######## Stopping the cygnus service ########"
sudo service cygnus stop 
if [[ $? -eq 0 ]]; then
	_logOk ".............. cygnus service is stopped .............."
else
	_logError ".............. cygnus service is NOT stopped .............." 
	exit 1
fi
echo ""

## Uninstall the cygnus RPM
_logStage "######## Uninstall the cygnus RPM ########"
sudo rpm -e cygnus &> /dev/null
result=$?
if [[ $result -ne 0 ]]; then 
	_logError ".............. Uninstall failed .............."
	exit 1
else
	_logOk ".............. Uninstall completed! .............."
fi
echo ""

_logStage "######## Check if the directories are cleaned... ########"
if [[ ! -d /usr/cygnus/ ]]; then
	_logOk ".............. All clean .............."
else
	_logError ".............. FAIL the directories are NOT cleaned .............."
	exit 1
fi 
echo ""

_logStage "############ Check RPM Stage completed! ############"
