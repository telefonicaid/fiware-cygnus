#!/bin/bash -x

JUST_BASH=false

if $JUST_BASH; then
###############################################################################
#
#  I just want to get inside the machine
#  Set the variable above `JUST_BASH` to true
#
###############################################################################

bash


else
###############################################################################
#
#  Compile
#
#  If for any reason this script fails, you keep your dependencies
#  in the folder /tmp/maven-deps (which is ignored in git, so don't worry)
#
###############################################################################

mkdir -p /cygnus-compiler/maven-deps >/dev/null
/tmp/maven/bin/mvn clean compile assembly:single


fi
