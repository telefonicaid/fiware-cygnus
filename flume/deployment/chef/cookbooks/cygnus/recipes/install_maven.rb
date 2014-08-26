#! /bin/bash
# Copyright 2014 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-connectors
#
# fiware-connectors is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# fiware-connectors is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
# General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with Orion Context Broker. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# frb@tid.es

# Download and install the Maven tar if not yet installed.
# The action must be :touch in order to notify the Maven installation even when the tar file already
# exits in /tmp
remote_file "/tmp/apache-maven-#{node[:maven][:version]}-bin.tar.gz" do
	source "http://www.eu.apache.org/dist/maven/maven-3/#{node[:maven][:version]}/binaries/apache-maven-#{node[:maven][:version]}-bin.tar.gz"
	notifies :run, "bash[install_maven]", :immediately
	action :touch
end

# Untar Maven and move it to its installation path.
bash "install_maven" do
	user "root"
	cwd "/tmp"
	code <<-EOH
		tar -zxf apache-maven-#{node[:maven][:version]}-bin.tar.gz
		mv apache-maven-#{node[:maven][:version]} #{node[:maven][:path]}
	EOH
	action :nothing
	not_if { ::File.exists?("#{node[:maven][:path]}/apache-maven-#{node[:maven][:version]}") } 
end

