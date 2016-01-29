#################################################################################
# Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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
################################################################################


FROM centos:7
MAINTAINER Herman Junge <herman.junge@telefonica.com>


###############################################################################
#
#  JAVA and other packages
#
###############################################################################


RUN yum update -y
RUN yum install -y wget tar git java-1.7.0-openjdk.x86_64
ENV JAVA_HOME /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.95-2.6.4.0.el7_2.x86_64/jre


###############################################################################
#
#  Apache Flume
#
###############################################################################


RUN wget https://www.apache.org/dist/flume/1.5.2/apache-flume-1.5.2-bin.tar.gz && \
	tar xvfz apache-flume-1.5.2-bin.tar.gz && \
	rm apache-flume-1.5.2-bin.tar.gz && \
	mv apache-flume-1.5.2-bin flume && \
	mkdir -p /flume/plugins.d/cygnus/ && \
	mkdir /flume/plugins.d/cygnus/lib


###############################################################################
#
#  Apache Thrift
#
###############################################################################


RUN wget http://repo1.maven.org/maven2/org/apache/thrift/libthrift/0.9.1/libthrift-0.9.1.jar && \
	rm /flume/lib/libthrift-*.jar && \
	mv libthrift-0.9.1.jar /flume/lib


###############################################################################
#
#  Copy the jar from target to the plugins directory
#
#  And set cygnus-flume-ng into the flume directory
#
###############################################################################


ADD ./target/*.jar /tmp/
ADD ./src/main/resources/cygnus-flume-ng /flume/bin/cygnus-flume-ng
RUN cp $(ls /tmp/*.jar) /flume/plugins.d/cygnus/lib/. && \
	chmod +x /flume/bin/cygnus-flume-ng


###############################################################################
#
#  Expose the Port
#
###############################################################################


EXPOSE 5050
