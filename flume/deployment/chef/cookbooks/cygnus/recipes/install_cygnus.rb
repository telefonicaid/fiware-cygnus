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

# Git Provider fails when the directory you want to clone the repo to already exists.
# https://tickets.opscode.com/browse/CHEF-1845
directory "/tmp/fiware-connectors" do
	recursive true
	action :delete
	only_if { ::File.exists?("/tmp/fiware-connectors") } 
end

# Git clone Cygnus.
git "/tmp/fiware-connectors" do
	repository "https://github.com/telefonicaid/fiware-connectors.git"
	revision "release/#{node[:cygnus][:version]}"
	action :sync
	notifies :run, "bash[install_cygnus]", :immediately
end

# Build Cygnus and move it to Flume's plugins.d/cygnus/lib/ folder.
bash "install_cygnus" do
        user "root"
        cwd "/tmp/fiware-connectors/flume"

	if "#{node[:cygnus][:version]}" == "0.1"
	        code <<-EOH
                	#{node[:maven][:path]}/apache-maven-#{node[:maven][:version]}/bin/mvn package 
	                cp target/cygnus-#{node[:cygnus][:version]}.jar #{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/lib
		EOH
	else
		code <<-EOH
			#{node[:maven][:path]}/apache-maven-#{node[:maven][:version]}/bin/mvn clean compile assembly:single
                        cp target/cygnus-#{node[:cygnus][:version]}-jar-with-dependencies.jar #{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/plugins.d/cygnus/lib
	        EOH
	end

        action :nothing
	notifies :create, "template[#{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/conf/cygnus.conf]", :immediately
end

# Configure Cygnus within Flume's conf/ folder.
template "#{node[:flume][:path]}/apache-flume-#{node[:flume][:version]}-bin/conf/cygnus.conf" do
	if "#{node[:cygnus][:version]}" == "0.1"
		source "cygnus/cygnus-0.1.conf.erb"
		variables({
     			:port => "#{node[:cygnus][:http_source][:port]}",
			:orion_version => "#{node[:cygnus][:http_source][:orion_version]}",
			:cosmos_host => "#{node[:cygnus][:hdfs_sink][:host]}",
			:cosmos_port => "#{node[:cygnus][:hdfs_sink][:port]}",
			:cosmos_username => "#{node[:cygnus][:hdfs_sink][:username]}",
			:cosmos_dataset => "#{node[:cygnus][:hdfs_sink][:dataset]}",
			:hdfs_api => "#{node[:cygnus][:hdfs_sink][:hdfs_api]}",
			:hdfs_channel_capacity => "#{node[:cygnus][:hdfs_channel][:capacity]}",
			:hdfs_channel_trans_capacity => "#{node[:cygnus][:hdfs_channel][:transaction_capacity]}"
		})
	elsif "#{node[:cygnus][:version]}" == "0.2" || "#{node[:cygnus][:version]}" == "0.2.1"
        	source "cygnus/cygnus-0.2-0.2.1.conf.erb"
		variables({
			:port => "#{node[:cygnus][:http_source][:port]}",
                        :orion_version => "#{node[:cygnus][:http_source][:orion_version]}",
                        :cosmos_host => "#{node[:cygnus][:hdfs_sink][:host]}",
                        :cosmos_port => "#{node[:cygnus][:hdfs_sink][:port]}",
                        :cosmos_username => "#{node[:cygnus][:hdfs_sink][:username]}",
                        :cosmos_dataset => "#{node[:cygnus][:hdfs_sink][:dataset]}",
                        :hdfs_api => "#{node[:cygnus][:hdfs_sink][:hdfs_api]}",
                        :ckan_api_key => "#{node[:cygnus][:ckan_sink][:api_key]}",
                        :ckan_host => "#{node[:cygnus][:ckan_sink][:host]}",
                        :ckan_port => "#{node[:cygnus][:ckan_sink][:port]}",
                        :ckan_dataset => "#{node[:cygnus][:ckan_sink][:dataset]}",
                        :mysql_host => "#{node[:cygnus][:mysql_sink][:host]}",
                        :mysql_port => "#{node[:cygnus][:mysql_sink][:port]}",
                        :mysql_username => "#{node[:cygnus][:mysql_sink][:username]}",
                        :mysql_password => "#{node[:cygnus][:mysql_sink][:password]}",
                        :mysql_persistence => "#{node[:cygnus][:mysql_sink][:persistence_mode]}",
                        :hdfs_channel_capacity => "#{node[:cygnus][:hdfs_channel][:capacity]}",
                        :hdfs_channel_trans_capacity => "#{node[:cygnus][:hdfs_channel][:transaction_capacity]}",
                        :ckan_channel_capacity => "#{node[:cygnus][:ckan_channel][:capacity]}",
                        :ckan_channel_trans_capacity => "#{node[:cygnus][:ckan_channel][:transaction_capacity]}",
                        :mysql_channel_capacity => "#{node[:cygnus][:mysql_channel][:capacity]}",
                        :mysql_channel_trans_capacity => "#{node[:cygnus][:mysql_channel][:transaction_capacity]}"
                })
	else
        	source "cygnus/cygnus-0.3-higher.conf.erb"
		variables({
                        :port => "#{node[:cygnus][:http_source][:port]}",
			:default_org => "#{node[:cygnus][:http_source][:default_organization]}",
                        :cosmos_host => "#{node[:cygnus][:hdfs_sink][:host]}",
                        :cosmos_port => "#{node[:cygnus][:hdfs_sink][:port]}",
                        :cosmos_username => "#{node[:cygnus][:hdfs_sink][:username]}",
			:cosmos_password => "#{node[:cygnus][:hdfs_sink][:password]}",
			:hdfs_persisence => "#{node[:cygnus][:hdfs_sink][:persistence_mode]}",
			:hdfs_naming_prefix => "#{node[:cygnus][:hdfs_sink][:naming_prefix]}",
			:hive_port => "#{node[:cygnus][:hdfs_sink][:hive_port]}",
                        :hdfs_api => "#{node[:cygnus][:hdfs_sink][:hdfs_api]}",
                        :ckan_api_key => "#{node[:cygnus][:ckan_sink][:api_key]}",
                        :ckan_host => "#{node[:cygnus][:ckan_sink][:host]}",
                        :ckan_port => "#{node[:cygnus][:ckan_sink][:port]}",
                        :ckan_dataset => "#{node[:cygnus][:ckan_sink][:dataset]}",
			:ckan_persistence => "#{node[:cygnus][:ckan_sink][:persistence_mode]}",
			:orion_url => "#{node[:cygnus][:ckan_sink][:orion_url]}",
                        :mysql_host => "#{node[:cygnus][:mysql_sink][:host]}",
                        :mysql_port => "#{node[:cygnus][:mysql_sink][:port]}",
                        :mysql_username => "#{node[:cygnus][:mysql_sink][:username]}",
                        :mysql_password => "#{node[:cygnus][:mysql_sink][:password]}",
                        :mysql_persistence => "#{node[:cygnus][:mysql_sink][:persistence_mode]}",
			:mysql_naming_prefix => "#{node[:cygnus][:mysql_sink][:naming_prefix]}",
                        :hdfs_channel_capacity => "#{node[:cygnus][:hdfs_channel][:capacity]}",
                        :hdfs_channel_trans_capacity => "#{node[:cygnus][:hdfs_channel][:transaction_capacity]}",
                        :ckan_channel_capacity => "#{node[:cygnus][:ckan_channel][:capacity]}",
                        :ckan_channel_trans_capacity => "#{node[:cygnus][:ckan_channel][:transaction_capacity]}",
                        :mysql_channel_capacity => "#{node[:cygnus][:mysql_channel][:capacity]}",
                        :mysql_channel_trans_capacity => "#{node[:cygnus][:mysql_channel][:transaction_capacity]}"
                })
	end
	action :nothing
end
