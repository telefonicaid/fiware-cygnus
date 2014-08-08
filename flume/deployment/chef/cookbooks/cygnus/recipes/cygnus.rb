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

# Create the Cygnus plugin for Flume, including the lib/ and libext/ folders.
directory "#{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/plugins.d/cygnus" do
	recursive true
end

directory "#{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/plugins.d/cygnus/lib"
directory "#{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/plugins.d/cygnus/libext"

# Git clone Cygnus
git "/tmp/fiware-connectors" do
	repository "https://github.com/telefonicaid/fiware-connectors.git"
	revision "release/#{node[:cygnus][:version]}"
	action :sync
	notifies :run, "bash[install_cygnus]", :immediately
end

# Build Cygnus and move it to plugins.d/cygnus/lib/.
bash "install_cygnus" do
        user "root"
        cwd "/tmp/fiware-connectors/flume"
        code <<-EOH
                #{node[:maven][:path]}/apache-maven-#{node[:maven][:version]}/bin/mvn clean compile assembly:single
                cp target/cygnus-#{node[:cygnus][:version]}-jar-with-dependencies.jar #{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/plugins.d/cygnus/lib
        EOH
        action :nothing
end

