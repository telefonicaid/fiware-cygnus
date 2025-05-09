#
# Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FIWARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
#

Summary:          Package for Cygnus common component
Name:             cygnus-common
Version:          %{_product_version}
Release:          %{_product_release}
License:          AGPLv3
BuildRoot:        %{_topdir}/BUILDROOT/
BuildArch:        x86_64
Requires(post):   /sbin/chkconfig, /usr/sbin/useradd
Requires(preun):  /sbin/chkconfig, /sbin/service
Requires(postun): /sbin/service
Group:            Applications/cygnus
Vendor:           Telefonica I+D
Provides:         cygnus-common%{_name_suffix} = %{_product_version}-%{_product_release}

%define _rpmfilename %%{ARCH}/%%{NAME}%{_name_suffix}-%%{VERSION}-%%{RELEASE}.%%{ARCH}.rpm

%description
This connector is a (conceptual) derivative work of ngsi2cosmos, and implements
a Flume-based connector common base for context data coming from several sources.

# Project information
%define _project_name cygnus
%define _project_user cygnus
%define _service_name cygnus

# improve package speed avoiding jar repack
%define __jar_repack %{nil}

# System folders
# _sourcedir =${topdir}/SOURCES
%define _srcdir %{_sourcedir}/../../
%define _install_dir /usr
%define _project_install_dir %{_install_dir}/%{_project_name}

# _localstatedir is a system var that goes to /var
 %define _log_dir %{_localstatedir}/log/%{_project_name}

# RPM Building folder
%define _build_root_project %{buildroot}%{_project_install_dir}

# -------------------------------------------------------------------------------------------- #
# prep section, setup macro:
# -------------------------------------------------------------------------------------------- #
%prep

echo "[INFO] Preparing installation"
# Create rpm/BUILDROOT folder
rm -Rf $RPM_BUILD_ROOT && mkdir -p $RPM_BUILD_ROOT
[ -d %{_build_root_project} ] || mkdir -p %{_build_root_project}

# Copy all from src to rpm/BUILD
cp -R  %{_srcdir}/src \
       %{_srcdir}/pom.xml \
       %{_srcdir}/README.md \
       %{_builddir}

# Copy "extra files" from rpm/SOURCES to rpm/BUILDROOT
cp -R %{_sourcedir}/* %{_builddir}


# -------------------------------------------------------------------------------------------- #
# pre-install section:
# -------------------------------------------------------------------------------------------- #
%pre
# Read from BUILD, write into BUILDROOT

echo "[INFO] Creating %{_project_user} user"
getent group  %{_project_user} >/dev/null || groupadd -r %{_project_user}
getent passwd %{_project_user} >/dev/null || useradd -r -g %{_project_user} -m -s /bin/bash -c 'Cygnus user account' %{_project_user}

# -------------------------------------------------------------------------------------------- #
# Install section:
# -------------------------------------------------------------------------------------------- #
%install
# Read from BUILD and write into BUILDROOT
# RPM_BUILD_ROOT = BUILDROOT

echo "[INFO] Installing the %{name}"

echo "[INFO] Creating the directories"
mkdir -p %{_build_root_project}/init.d
# Create folder to store the PID (used by the Service)
mkdir -p %{buildroot}/var/run/%{_project_name}
# Create log folder
mkdir -p %{buildroot}/${_log_dir}
# Create /etc/cron.d directory
mkdir -p %{buildroot}/etc/cron.d
# Create /etc/logrotate.d directory
mkdir -p %{buildroot}/etc/logrotate.d
# create /etc/cygnus
mkdir -p %{buildroot}/etc/%{_project_name}
# create /etc/init.d
mkdir -p %{buildroot}/etc/init.d
# create /usr/bin
mkdir -p %{buildroot}/usr/bin

cp -R %{_builddir}/usr/cygnus/*                       %{_build_root_project}
cp %{_builddir}/init.d/%{_service_name}               %{_build_root_project}/init.d/%{_service_name}
cp %{_builddir}/config/*                              %{_build_root_project}/conf/
cp %{_builddir}/cron.d/cleanup_old_cygnus_logfiles    %{buildroot}/etc/cron.d
cp %{_builddir}/logrotate.d/logrotate-cygnus-daily    %{buildroot}/etc/logrotate.d

ln -s %{_project_install_dir}/init.d/%{_service_name}  %{buildroot}/etc/init.d/%{_service_name}
ln -s %{_project_install_dir}/conf                     %{buildroot}/etc/%{_project_name}/conf
ln -s %{_project_install_dir}/bin/flume-ng             %{buildroot}/usr/bin/flume-ng

# -------------------------------------------------------------------------------------------- #
# post-install section:
# -------------------------------------------------------------------------------------------- #
%post

#Logs
echo "[INFO] Creating log directory"
mkdir -p %{_log_dir}
chown %{_project_user}:%{_project_user} %{_log_dir}
chmod g+s %{_log_dir}
setfacl -d -m g::rwx %{_log_dir}
setfacl -d -m o::rx %{_log_dir}

echo "Done"

# -------------------------------------------------------------------------------------------- #
# pre-uninstall section:
# -------------------------------------------------------------------------------------------- #
%preun

/sbin/service %{_service_name} stop


# -------------------------------------------------------------------------------------------- #
# post-uninstall section:
# clean section:
# -------------------------------------------------------------------------------------------- #
%postun


%clean
rm -rf $RPM_BUILD_ROOT


# -------------------------------------------------------------------------------------------- #
# Files to add to the RPM
# -------------------------------------------------------------------------------------------- #
%files
%defattr(755,%{_project_user},%{_project_user},755)
%attr(0644, root, root) /etc/cron.d/cleanup_old_cygnus_logfiles
%attr(0644, root, root) /etc/logrotate.d/logrotate-cygnus-daily
%attr(0777, root, root) /etc/cygnus/conf
%attr(0777, root, root) /etc/init.d/cygnus
%attr(0777, root, root) /usr/bin/flume-ng

%{_project_install_dir}
/var/run/%{_project_name}

%changelog

* Fri Mar 14 2025 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.16.0

- [cygnus-ngsi][cygnus-common] Fix way to handle CygnusPersistenceException to allow batch retries in arcgis-sink if `.batch_ttl` configured
- [cygnus-ngsi][cygnus-common] Remove new line chars from Arcgis logs

* Thu Nov 14 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.15.0

- [cygnus-ngsi][cygnus-common] Add arcgis_connectionTimeout and arcgis_readTimeout sink options to allow set non infinite conection timeouts with arcgis (#2440)

* Thu Nov 7 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.14.0

* Mon Oct 14 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.13.0

* Fri Sep 27 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.12.0

- [cygnus-common][arcgis] toString of complex geometry is not returning equivalent input (#2418)

* Thu Sep 19 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.11.0

- [cygnus-common][cygnus-ngsi] New setting mongo_uri (#2387)
- [cygnus-common][cygnus-ngsi] Deprecate (mongo_hosts, mongo_username, mongo_password, mongo_auth_source, mongo_replica_set) (use mongo_uri instead)

* Thu Aug 8 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.10.0

- [cygnus-common] [mongo-backend] Use sslEnabled, sslInvalidHostNameAllowed, sslKeystorePathFile, sslKeystorePassword, sslTruststorePathFile and sslTruststorePassword options for mongoDB connections
- [cygnus-common] [mongo-backend] Allow mongodb autodiscover at connect when just one server is provided
- [cygnus-common] Upgrade gson dependency from 2.10.1 to 2.11.0

* Wed Jun 12 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.9.0
- [cygnus-common] [arcgis] Force null geometry when invalid geo:json format or type is found instead of Point(0,0) (#2379)

* Wed Apr 17 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.8.0
- [cygnus-common] [SQL-sinks] Define default values and allow to set connection pool config for maxPoolSize(3), maxPoolIddle(2), minPoolIdle(0) and minPoolIdleTimeMillis(10000) (#2366)

* Mon Mar 11 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.7.0

* Mon Mar 4 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.6.0
- [cygnus-common] [arcgis] Fix: check feature attributes containsKey before get it (#2347)
- [cygnus-common] Upgrade postgresql from 42.4.3 to 42.7.2

* Fri Feb 9 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.5.0
- [cygnus-common][cygnus-ngsi][cygnus-ngsi-ld] Upgrade in source code to support Java 17 build
- [cygnus-common] [Arcgis] All NGSI DateTime are translated to Millis from Epoch (esriFieldTypeDate) (#2339)

* Thu Feb 1 2024 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.4.0
- [cygnus-common] [Arcgis] include error in log about cast attributes json
- [cygnus-common] Upgrade Debian version from 12.1 to 12.4 in Dockerfile
- [cygnus-common] [NGSIGenericColumnAggregator] Fix getValue when gson object is null
- [cygnus-common] [Arcgis] Replace esriFieldTypeNumber by esriFieldTypeSingle (#2334)

* Thu Nov 16 2023 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.3.0
- [cygnus-common] [MongoDB] fix error about reindex when datamodel changed
- [cygnus-common] [Arcgis] check featureId ignore case (#2320)
- [cygnus-common] [Arcgis] check object_id ignore case (#2313)
- [cygnus-common] [Arcgis] fix url quoted based on uniqueFieldType (#2311)
- [cygnus-common] [SQL] Add Primary Key on Timestamp to Error Log table (#2302)
- [cygnus-common] Upgrade gson dependency from 2.6.2 to 2.10.1
- [cygnus-common] Upgrade mysql-java-connector dependency from 8.0.28 to 8.0.33
- [cygnus-common][SQL] Fix expiration records tablename used by delete and select (#2265)
- [cygnus-common][SQL] Fix expiration records select with a limit to avoid java out of memory error (#2273)
- [cygnus-common] Upgrade mongodb driver dep from 3.12.12 to 3.12.14        

* Tue Jun 20 2023 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.2.0
- [cygnus-common][pSQL] Allow use null for ST_GeomFromGeoJSON
- [cygnus-common][SQL] cache tablename with schema in pSQL for capRecords case
- [cygnus-common][SQL] Log full values in insert_error instead of placeholder
- [cygnus-common][SQL] Check keys for delete lastdata in lowercase for PSQL
- [cygnus-common][SQL] Check if delete query for lastdata was completed before use it
- [cygnus-common][cygnus-ngsi]  Fix: exclude of attribute processing alterationType send by a subscription in all of their values (#2231, reopened)
- [cygnus-common][SQL] Fix sql syntax for expiration records (select, delete, filters) in non mysql instances (#2242)
- [cygnus-common][SQL] Exclude error table from persist policy (cap records is based on recvtime which is non used by persistError)
- [cygnus-common][SQL] Add missed capRecords implementation for PSQL backends

* Thu May 24 2023 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.1.0
- [cygnus-common] Add: delete entity in lastData when alterationType entityDelete is received (#2231). Requires Orion 3.9+
- [cygnus-common] Add: exclude of attribute processing alterationType send by a subscription in all of their values (#2231)
- [cygnus-common] Fix: missing blank space in create table if not exists for postgresql
    
* Thu May 04 2023 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 3.0.0
- [cygnus-common] OracleSQL backend (#2195)
- [cygnus-common] MongoDB indexes are created depending on DM (#2204)
- [cygnus-common] Added support for reindexing of dataExpiration (#2160, partially)
- [cygnus-common] Added support for MongoDB hosts without a port (#2219)
- [cygnus-common] Fixed isANumber for number containing multiple decimals (#2226)
- [cygnus-common] Upgrade Java version from 1.8 to 1.11 in Dockerfile
- [cygnus-common] Upgrade flume-ng-node version from 1.9.0 to 1.11.0 (#2179)
- [cygnus-common] Upgrade log4j from v1 (1.2.17) to v2 (2.17.2) series (#1592)
- [cygnus-common] Upgrade hadoop-core from 1.2.1 to hadoop-client 2.7.0
- [cygnus-common] Upgrade hive-jdbc from 2.3.4 to 3.1.3
- [cygnus-common] Upgrade maven-compiler-plugin dep from 3.5.1 to 3.11.0
- [cygnus-common] Upgrade maven-assembly-plugin dep from 2.6.0 to 3.5.0
- [cygnus-common] Upgrade exec-maven-plugin  dep from 3.5.1 to 3.8.2
- [cygnus-common] Upgrade maven-site-plugin dep from 2.5.1 to 3.8.2
- [cygnus-common] Upgrade maven-surefire-report-plugin dep from 2.12.4 to 3.0.0
- [cygnus-common] Upgrade maven-checkstyle-plugin dep from 2.12.0 to 3.2.1
- [cygnus-common] Upgrade maven-javadoc-plugin dep from 2.9.0 to 3.5.0
- [cygnus-common] Upgrade mongodb driver dep from 3.12.11 to 3.12.12
- [cygnus-common] Upgrade apache httpclient dep from 4.5.13 to 4.5.14
- [cygnus-common] Upgrade apache httpcore dep from 4.3.1 to 4.4.16
- [cygnus-common] Upgrade apache common lang dep from 3.4.0 to 3.12.0
- [cygnus-common] Upgrade postgresql from 42.4.1 to 42.4.3

* Tue Oct 18 2022 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.20.0
- [cygnus-common][SQLBackend] Ordernig batch INSERT sentences upon upsert to avoid deadlocks (#2197)
- [cygnus-common][MySQLBackend] Upgrade mysql-connector-java from 8.0.27 to 8.0.28
- [cygnus-common] Upgrade postgresql from 42.3.3 to 42.4.1
- [cygnus-common] Remove grouping rules functionality (deprecated since version 1.6.0 in December 2016)

* Fri Jun 03 2022 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.19.0
- [cygnus-common] FIX: change log level should change log level of all cygnus loggers, not just rootLogger (#1827)

* Wed May 11 2022 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.18.0
- [cygnus-common][MongoBackendImpl] Remove unneded mongo create collection in non capped case (#2161)
- [cygnus-common] Upgrade mongodb-driver dependency from 3.12.8 to 3.12.11
- [cygnus-common][CKANBackendImpl] allow empty result as resource_view_list (#2150)

* Tue Mar 15 2022 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.17.0
- [cygnus-common][Postgis,PostgresSQL] Add new data models dm-by-entity-type-database and dm-by-entity-type-database-schema (#2142)
- [cygnus-common][SQLBackend] Try to insert in row mode tables without create db and table before
- [cygnus-common][SQLBackend] Try to insert in error table without create it before (#2132)
- [cygnus-common] Upgrade postgresql dependency from 42.2.25 to 42.3.3 due to github vulnerability report

* Fri Feb 4 2022 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.16.0
- [cygnus-commons] Upgrade postgresql dependency from 42.2.22 to 42.2.25 due to github vulnerability report

* Fri Dec 10 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.15.0
- [cygnus-common][SQLSinks] Add more info in logs about rollback, database and sink instance involved in current error
- [cygnus-common][MySQLBackend] Upgrade mysql-connector-java from 8.0.22 to 8.0.27
- [cygnus-common] Upgrade gson dep from 2.2.4 to 2.6.2 (#1922)
- [cygnus-common][SQLImpl] Allow multiple fields in last_data_unique_key (#2016)
- [cygnus-common][SQLBackendImpl][SQLSinks] Refactor persist aggregation sql sinks and utils (#2092)
- [cygnus-common] Upgrade Dockerfile base image from centos7.9.2009 to centos8.4.2105 (#1996)

* Tue Oct 5 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.14.0
- [cygnus-common][SQLBackendImpl] Fix: use all related queries when upsert error for logs, exception and persistError (#2088)
- [cygnus-common][SQLBackendImpl] Fix: use schema for postgis destination at sql insert query (#2085)
- [cygnus-common][SQLBackendImpl] Force to cast to ::text to ensure to_timestamp upsert (#2087)

* Mon Sep 20 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.13.0
- [cygnus-common] Use ckanPath in ckan cache
- [cygnus-common] Remove methods in class CKANBackendImpl for allowing the creation of the data store with ngsi-ld notifications due to break backward compatibilitgy ckan in column mode

* Wed Aug 18 2021 Ivan Hernandez <ivan.hernandez@atlantida.es> 2.12.0

* Thu Jul 22 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.11.0
-[cygnus-commons][PostgreSQL, PostGIS] Upgrade posgresql driver from 9.4-1206-jdbc41 to 42.2.22
-[cygnus-commons][MySQLBackend] Upgrade mysql-connector-java from 5.1.47 to 8.0.22 due to security vulnerability
-[cygnus-commons] Upgrade httpclient dependency from 4.3.6 to 4.5.13 due to security vulnerability

* Wed Jun 02 2021 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.10.0
- [cygnus-common][Mongo] Check mongo uri format (#2046)
- [cygnus-common][Mongo] Use mongo bulkWriter for aggregated data (#2018)
- [cygnus-common][MySQL, PostgreSQL, Postgis] Persit error about upsert
- [cygnus-common][MySQL] Upgrade mysql-connector from 5.1.47 to 5.1.49
- [cygnus-common] remove hive-exec dependency (unneeded)
- [cygnus-common] Upgrade mongodb-driver from 3.11.0 to 3.12.8

* Mon May 10 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.9.0

* Mon Apr 5 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.8.0
- [cygnus-common] Remove database initialization on constructors (#2004)

* Fri Mar 5 2021 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.7.0
- [cygnus-common] Change log level of timeinstant parser chain (from error to debug)
- [cygnus-common][ArcGis] New Rest based backend to persist Arcgis data (#1672)
- [cygnus-common][cygnus-ngsi][cygnus-ngsi-ld][cygnus-twitter] Upgrade Dockerfile base image from 7.6.1810 to centos7.9.2009

* Thu Nov 4 2020 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.6.0
- [cygnus-common][SQLBackendImpl] Avoid use word `purge` as tmp when purge mysql error table due is a reserved word in mysql

* Tue Oct 20 2020 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.5.0
- [cygnus-ngsi, cygnus-common][PosgtgreSQLSink, PostgisSink, MySQLSQLSink] Remove PreparedStatement building, to use String query (walkaround for #1959)
- [cygnus-ngsi, cygnus-common][PosgtgreSQLSink, PostgisSink, MySQLSQLSink] Log info about persisted data (#1939)
- [cygnus-ngsi, cygnus-common][PosgtgreSQLSink, PostgisSink, MySQLSQLSink] Create upsert transaction (#1806, #1936)
- [cygnus-common][NameMappingInterceptor] Catch and log Compile error namemapping (#1924)
- [cygnus-common][SQLBackendImpl] Add configuration option to persist errors in SQL sinks (#1928)
- [cygnus-common][SQLBackendImpl] Purge automatically persistence error logs table, keeping latest errors (max_latest_errors setting) (#1929)
- [cygnus-common] Add new methods in class CKANBackendImpl for allowing the creation of the data store with ngsi-ld notifications

* Fri Sep 04 2020 Alvaro Vega <alvaro.vegagarcia@telefonica.com> 2.4.0
- [cygnus-common][SQLBackendImpl] Exception handle for connection/statement SQL objects (#1907)

* Wed Jul 15 2020 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.3.0

* Tue Jun 23 2020 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.2.0

* Thu May 07 2020 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.1.0
- [cygnus-common][cygnus-ngsi] Add following parameters (and environment variables for docker) to be able to connect to sql database with SSL/TLS: "mysql_options" (`CYGNUS_MYSQL_OPTIONS`), "postgresql_options" (`CYGNUS_POSTGRESQL_OPTIONS`) and "postgis_options" (`CYGNUS_POSTGIS_OPTIONS`) (#1867)

* Thu Apr 16 2020 Fermin Galan <fermin.galanmarquez@telefonica.com> 2.0.0
- [cygnus-common][PostgreSQL, MySQL] Refactor code to use a SQL generic backend for all SQL-based sinks (#1836)
- [cygnus-common][PostgreSQL, MySQL] Lazy singleton to share pool connections of SQL backends among instances on JVM (#1836)
- [cygnus-common][PostgreSQL, MySQL] SQL error persistance (#1791)
- [cygnus-common][MongoBackend] Fix persistence error mongo authentication and side effect (using new configuration parameter mongo_auth_source) (#1849)
- [cygnus-common][Elasticsearch] Fix a bug raised NullPointerException when 'ignore_white_spaces' is true and an attribute has null value (#1838)

* Fri Feb 07 2020 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.18.0
- [cygnus-common][MongoBackend] Proper processing of mongo errors in create index operations (#1756)
- [cygnus-common] Fix: Changing cygnus port does not work (#1698)

* Tue Oct 29 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.17.0
- [cygnus-common][HDFSSink] Check attribute value before use it
- [cygnus-common][PostgreSQL][MySQL] catch exception close invalid connection
- [cygnus-common] Upgrade MongoDB driver from 3.0.4 to 3.11.0
- [cygnus-common] Fix for index name creation in Mongo and STH Sinks (#1714)

* Wed Sep 17 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.16.0
- [cygnus-common][doc] Add Install section in README.md (#1576)
- [cygnus-common] Enable JAVA_OPTS for Flume (#1704)
- [cygnus-common] Adjust build cygnus to use Java 1.8 (#1718)

* Fri Jun 28 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.15.0

* Tue Jun 04 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.14.0
- [cygnus-common][MongoDB] Warn about index creation error instead of throw exception and abort create collection (#1649)

* Wed May 22 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.13.0
- [cygnus-common][NMI] Allows th Original values to be omitted in the configuration file (#1466)

* Wed Apr 10 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.12.0
- [cygnus-common][PostgreSQL] Add connection pool
- [cygnus-common][YAFS][bugfix] Fixed issue, missmatching Jetty threads causes Cygnus crash (#1616)
- [cygnus-common][YAFS] Fixed issue, stopping Cygnus after config reloading (#1624)

* Wed Apr 03 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.11.0
- [cygnus-common] Upgrade flume-ng-node version from 1.4.0 to 1.9.0
- [cygnus-common][mongo] Upgrade mongo driver from 3.0.0 to 3.0.4
- [cygnus-common][mysql] Upgrade mysql connector from 5.1.31 to 5.1.47
- [cygnus-common][feature] Add /v1/namemappings route to API (#1277)
- [cygnus-common][MySQLBackend] Implement connection pooling (#1598, #1597)
- [cygnus-common][bugfix] Support empty files in mamemappings
- [cygnus-common][bugfix] Support empty servicenames in mamemappings

* Thu Dec 13 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.10.0
- [cygnus-common] Update hive dependence from 0.13.1 to 2.3.4 due to a vulnerability alert
- [cygnus-common] Update httpclient dependence to 4.3.6 due to a vulnerability
- [cygnus-common][CommonUtils][debug] Modify that CommonUtils#getTimeInstant can accepts the ISO8601 format with offset like 2018-01-02T03:04:05+09:00 (#1517)

* Wed Jun 13 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.9.0
- [cygnus-common][Docker] Upgrade to CentOS 7, Maven 3.5.2 and Java 1.8.0 in Dockerfile

* Wed Sep 13 2017 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.8.0
- [cygnus-common, cygnus-ngsi][hardening] Fix FIWARE repository URL (#1435)
- [cygnus-common, cygnus-ngsi][hardening] Unify test classes (#1332, #1333)
- [cygnus-common][bug] Allow disabling YAFS in order to avoid crashes upon configuration reloads (#1074)

* Tue Jan 31 2017 Francisco Romero <francisco.romerobueno@telefonica.com> 1.7.0
- [cygnus] Add Travis CI (#1347)
- [cygnus][hardening] Fix "FIWARE" name in license headers (#1369)
- [cygnus-common][hardening] Clean and modularize ManagementInterface.java (#1368)
- [cygnus-common][feature] Add KPIs API (#1339)
- [cygnus-common][bug] Fix error handling upon MongoDB collection creation (#1377)

* Fri Dec 02 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.6.0
- [cygnus][hardening] Add IPv6 support to API and GUI (#1261)

* Wed Nov 02 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.5.0
- [cygnus][hardening] Add entry for Grouping Rules in Installation and Administration Guide (#1206)
- [cygnus][hardening] Add missing creation of log4j path (#1212)
- [cygnus][hardening] Add compilation scripts (#1213)
- [cygnus][hardening] Warn about the incompatibility of installing cygnus-ngsi and cygnus-twitter in the same base path (#1214)
- [cygnus][hardening] Fix docker links in badges (#1222)
- [cygnus-common][hardening] Add -Dfile.encoding=UTF-8 Java option (#1225)
- [cygnus][hardening] Update link about getting tokens in documentations (#1227)
- [cygnus][hardening] Replace references to JDK 1.6 with JDK 1.7 (#1232)
- [cygnus][hardening] Fix yum install command (#1249)
- [cygnus][hardening] Add FIWARE architecture section in the documentation (#1248)
- [cygnus][hardening]] Fix API entries in documentation (#1246)
- [cygnus][hardening] Add entries about running as process and running as service in documentation (#1255)
- [cygnus-common][hardening] Add traces regarding loaded .jar files (#1210)
- [cygnus][hardening] Do configurable the CKAN viewer attached to resources (#1258)
- [cygnus][hardening] Document IPv6 support (#1058)
- [cygnus][hardening] Add introductory course slides link (#1265)

* Fri Oct 07 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.4.0

* Fri Sep 09 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.3.0
- [cygnus][hardening] Add architecture documentation (#1127)
- [cygnus][feature] Standardize logs layout (#1118)
- [cygnus-common][hardening] Add implementation for /admin/log route (#807)
- [cygnus-common][hardening] Create ManagementInterfaceUtils class (#1115)
- [cygnus-common][bug] Add fiware-service and fiware-servicePath headers to all API operations (#1124)
- [cygnus][hardening] Add Orion-Cygnus-Kafka integration example (#1132)
- [cygnus-common][bug] Release connection upon persistence error (#1139)
- [cygnus-common][bug] Insert NULL in CKAN columns when an empty attribute value ("", null, {} or []) or empty attribute metadata (null or []) is notified (#1144)

* Wed Sep 07 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.2.1
- [cygnus-common][bug] Release connection upon persistence error (#1139)
- [cygnus][hardening] Add architecture documentation (#1127)

* Fri Jul 01 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.2.0
- [cygnus-common] [hardening] Add /admin/configuration/agent/path/to/agent.conf route to API (#893)
- [cygnus-common] [hardening] Add /admin/configuration/instance/path/to/instance.conf route to API (#894)

* Wed Jun 01 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.1.0
- [cygnus-ngsi] [hardening] Force UTF-8 charset both at NGSIRestHandler and the sinks (#975)
- [cygnus-common] [hardening] Added a fiware-correlator header to messages of API responses (#932)
- [cygnus-common,cygnus-ngsi][hardening] Add sanity checks and diagnosis procedures (#627, #628)
- [cygnus-ngsi][bug] Fix logging folder creation in Dockerfile (#1009)
- [cygnus] [bug] Fix errors in apiary.apib file (#1011)
- [cygnus] [doc] Move backend documentation to cygnus-common (#1013)
- [cygnus] [feature] Add cygnus-common docker (#1021)
- [cygnus] [doc] Add CartoDB related documentation (#1016)
- [cygnus] [hardening] Add service port and api port as environment variables to Dockerfiles (#1020)
- [cygnus-ngsi] [feature] Add distance analysis support to NGSICartoDBSink (#1026)
- [cygnus-ngsi] [feature] Add multitenancy support to NGSICartoDBSink (#1028)
- [cygnus-ngsi] [hardening] Remove data model by attribute support in NGSICartoDBSink (#1030)
- [cygnus-common] [bug] Fixed error when POST a CygnusSubscription without subscription field (#1034)
- [cygnus] [doc] Update the sink docs regarding the naming conventions (#630)
- [cygnus-ngsi] [hardening] Change default credentials for docker MySQL agent (#1038)
- [cygnus-ngsi] [feature] Add MongoDB and STH sinks to docker agent (#1044)
- [cygnus-ngsi] [hardening] Remove support to deprecated 'matching_table' parameter (#1048)
- [cygnus-ngsi] [hardening] Add checks for invalid configuration in NGSIGroupingInterceptor.java (#1049)
- [cygnus-common] [hardening] Change the creation of the Json payload of the response in HttpBackend (#1052)
- [cygnus-ngsi] [hardening] Add support for new encoding mechanism (#1029)
- [cygnus-common] [bug] Getting API port fails when using port forwarding (#1055)

* Fri May 06 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.0.0
- [cygnus-ngsi] [feature] Add Cygnus GUI (#829)
- [cygnus-ngsi] [bug] Fix default authentication parameters in OrionMySQLSink and OrionPostgreSQLSink (#837)
- [cygnus-ngsi] [feature] Add support for string-based aggregation (occurences) in OrionSTHSink (#547)
- [cygnus-ngsi] [hardening] Use precompiled regexes in Utils.encode() and Utils.encodeHive() (#818)
- [cygnus-ngsi] [hardening] Remove a temporal fix regarding Orion <= 0.10.0 in OrionRESTHandler (#840)
- [cygnus-ngsi] [hardening] Add an efficient function for converting ArrayList to String in Utils.java (#841)
- [cygnus-ngsi] [bug] Fix the way a set of notified attributes are processed in OrionMySQLSink, no order must be assumed (#855)
- [cygnus-ngsi] [feature] Remove support for XML notifications in all sinks and in the documentation (#448)
- [cygnus-ngsi] [bug] Fix wrong class name in configuration section of OrionSTHSink.md (#852)
- [cygnus-ngsi] [feature] When notified, use the TimeInstant metadata instead of the reception time (#859)
- [cygnus-ngsi] [hardening] Add docker and support badges to the README (#858)
- [cygnus-ngsi] [hardening] Add a management API method for reseting the statistics (#851)
- [cygnus-ngsi] [feature] Reuse a notified Correlator ID when notified, otherwise generate it (#843)
- [cygnus-ngsi] [hardening] Remove deprecated configuration parameters in OrionHDFSSink (#868)
- [cygnus-ngsi] [feature] When configured, ignore white space-based string attributes (#678)
- [cygnus-ngsi] [bug] Fix processing of splited Http responses in HttpBackend (#875)
- [cygnus-ngsi] [feature] Add /v1/subscriptions route to API (#808)
- [cygnus-ngsi] [hardening] Improve the different elements naming when notified/default service path is / (#877)
- [cygnus-ngsi] [feature] Add implementation for /admin/log route (#807)
- [cygnus-ngsi] [bug] Fix some wrong links in the documentation (#885)
- [cygnus-ngsi] [hardening] Add missing components to the list of components having a restricted logging level in log4j.properties.template (#882)
- [cygnus-ngsi] [hardening] Set external dependencies logging level to WARN in lo4j.properties.template (#881)
- [cygnus-ngsi] [bug] Fix wrong data conversion from SQL timestamp to ISO 8601 UTC when a year must be decreased (#873)
- [cygnus-ngsi] [bug] Add support for ISO 8601 timestamps containing 6 microsecond digits (#906)
- [cygnus-ngsi] [hardening] Check the service-paths within grouping rules start with / (#901)
- [cygnus-ngsi] [hardening] Use MongoDB accepted character set in OrionMongoSink and OrionSTHSink (#898)
- [cygnus-ngsi] [hardening] Expand topic name in OrionKafkaSink (#880)
- [cygnus-ngsi] [bug] Remove 'Z' character (UTC mark) when encoding the reception time in OrionMySQLSink (#909)
- [cygnus-ngsi] [hardening] Create a backend for OrionKafkaSink (#912)
- [cygnus-ngsi] [hardening] Invalidate configurations instead of exiting Cygnus upon wrong configuration (#917)
- [cygnus-ngsi] [hardening] Add support for SQL timestamps with microseconds in Utils.getTimeInstant (#922)
- [cygnus-ngsi] [hardening] Remove the user-agent header, and consider content-type as a Http-only header (#920)
- [cygnus-ngsi] [hardening] Precompile the regexes once read in GroupingRules.java (#928)
- [cygnus-ngsi] [hardening] Add an internal Transaction ID (#930)
- [cygnus-ngsi] [bug] Add support for multi-valued Fiware-ServicePath within notifications (#923)
- [cygnus-ngsi] [hardening] Replace all references to "STH" with "FIWARE Comet" (#573)
- [cygnus-ngsi] [hardening] Use UUIDv4 in the generation of correlator and transaction IDs in OrionRestHandler (#931)
- [cygnus-ngsi] [task] Refactor de Github repository in a per agent fashion (#864)
- [cygnus-ngsi] [feature] Add NGSICartoDBSink (#927)
- [cygnus] [hardening] Add documentation in orion_ckan_sink.md about the creation of resources & datastores for column mode (#971)
- [cygnus-ngsi] [bug] Fix configuration of grouping rules in quick_start_guide.md (#983)
- [cygnus-ngsi] [bug] Fix collection name building when data_model is dm-by-service-path in Mongo sinks (#977)
- [cygnus-ngsi] [bug] Fix attribute-based accumulation (#982)
- [cygnus-ngsi] [bug] Pass the API and GUI ports as parameters, instead of hardcoding them in ManagementInterface.java (#995)
- [cygnus-ngsi] [hardening] Force default service and service path to use only alphanumerics and underscores (#985)
- [cygnus] [hardening] Update README.md with link to apiary documentation (#892)

* Tue Mar 01 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 0.13.0
- [FEATURE] Add /stats route to the Management Interface (#737)
- [FEATURE] Add /groupingrules route to the Management Interface (#745)
- [HARDENING] Check Http method in Management Interface API routes (#740)
- [HARDENING] Add /v1 to Management Interface API routes (#739)
- [BUG] Fix wrong spec date regarding release/0.12.0 (#750)
- [HARDENING] Add check for null content-type in notifications (#743)
- [FEATURE] Add support for batching in OrionSTHSink (#571)
- [HARDENING] Add support for configurable number of partitions and replication factor in OrionKafkaSink (#736)
- [BUG] Fix the name of the fiware service path field in all the sinks (#764)
- [HARDENING] Replace method Accumulator.getAccumulatorForRollBack() with Accumulator.clone() (#746)
- [BUG] Fix wrong section about STH implementation in orion_mongo_sink documentation (#761)
- [BUG] Fix wrong 'dynamodb-sink' sink reference in agent.conf.template (#763)
- [BUG] Add OrionDynamoDBSink configuration template in cygnus_agent_conf.md (#762)
- [BUG] Add OrionPostgreSQLSink configuration template in cygnus_agent_conf.md (#769)
- [BUG] Add missing sink types in agent.conf.templates (#773)
- [BUG] Fix wrong data_model configured for OrionPostgreSQLSink in agent.conf.template (#774)
- [HARDENING] Remove the persistOne method from all the sinks (#609)
- [HARDENING] Invalidate the sink if the data_model parameter is wrong (and prepare the code for further parameter checks) (#718)
- [HARDENING] Check for input parameters validity (#752)
- [BUG] Fix the generalized Hive-like encoding style used in OrionHDFSSink (#781)
- [FEATURE] Add time and size-based data management policies to collections in OrionMongoSink and OrionSTHSink (#484)
- [HARDENING] Relocate attr_persistence check from OrionMongoBaseSink to OrionMongoSink (#793)
- [BUG] Fix recvTime in OrionMongoSink (#796)
- [BUG] Fix the OrionCKANSink cache when to resources in different packages have the same name (#643)
- [BUG] Fix fiware service path field in OrionCKANSink, row mode (#800)
- [BUG] Fix the batching mechanism when two entities belonging the same service or service path have the same destination (#806)
- [HARDENING] Add cygnus_translator_pre0.10.0_to_0.10.0_mysql_table.sh and fix cygnus_translator_pre0.10.0_to_0.10.0_mysql_db.sh(#765)
- [HARDENING] User per batch TTL, not per event TTL (#714)
- [FEATURE] Add enable_lowercase parameter to all sinks (#815)
- [BUG] Fix the shared variables within OrionRESTHandler (#823)
- [HARDENING] Comment default configuration parameters in documentation and configuration template (#365)
- [BUG] Fix threads defunction supervision in CygnusApplication (#826)
- [HARDENING] Add documentation about testing environment (#777)

* Tue Feb 16 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 0.12.1
- [BUG] Fix wrong spec date regarding release/0.12.0 (#750)
- [BUG] Fix the name of the fiware service path field in all the sinks (#764)
- [BUG] Fix the generalized Hive-like encoding style used in OrionHDFSSink (#781)

* Mon Feb 01 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 0.12.0
- [FEATURE] Add postgreSQL sink support (#511)
- [FEATURE] Add batching support to OrionTestSink (#572)
- [HARDENING] Add the minimum HW requirements (#640)
- [HARDENING] Rename the HTTP and Flume header constants (#662)
- [HARDENING] Remove the dual batch accumulation in OrionSink (#664)
- [HARDENING] Add "requests" package as a requirement for cygnus_translator_pre0.10.0_to_0.10.0_ckan.py (#667)
- [HARDENING] Allow SSL connections to CKAN servers in cygnus_translator_pre0.10.0_to_0.10.0_ckan.py (#666)
- [BUG] Fix the grouped destination when using batches (#675)
- [HARDENING] Add the maximum accepted batch size in OrionDynamoDBSink (#672)
- [HARDENING] Add interface and implementation to MongoBackend (#654)
- [FEATURE] Add a general data_model parameter (#659)
- [HARDENING] Add grouping_rules.conf & flume-env.sh configuration files in the documentation (#657)
- [BUG] Use pagination when retrieving all the records to backup in cygnus_translator_pre0.10.0_to_0.10.0_ckan.py (#665)
- [FEATURE] Enable per-user or default Hive database creation in OrionHDFSSink (#516)
- [HARDENING] Document the service_as_namespace OrionHDFSSink parameter (#692)
- [HARDENING] Add a piece of documentation about multitenancy (#607)
- [BUG] Fix the log file when running as a service (#656)
- [HARDENING] Add a piece of documentation about required dependencies to be manually installed when building and installing without dependencies (#635)
- [HARDENING] Add a configurable value for CSV separator in OrionHDFSSink (#495)
- [HARDENING] Rename initializeBatching with initialize in all the sinks (#704)
- [FEATURE] Use TimeInstant (when available) instead of the reception time in OrionSTHSink (#651)
- [HARDENING] Do rollbacking of batches (#563)
- [HARDENING] Include service and servicePath in logs (#707)
- [HARDENING] Fix libthrift installation (#706)
- [BUG] Fix of the Cygnus installation using yum (#652)
- [BUG] Update links referring to sinks documentation (#713)
- [HARDENING] Finish a transaction only when it is really persisted, not rollbacked (#715)
- [FEATURE] Add batching support to OrionMongoSink (#570)
- [HARDENING] Document the Grouping Rules in the Installation and Administration Guide (#658)
- [HARDENING] Update Java and Fix which in docker (#709)
- [HARDENING] Document how to connect Orion and Cygnus (#726)
- [BUG] Fix some typos in agent.conf.template and OrionMySQLSink.md (#729)

* Fri Dec 04 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.11.0
- [RELEASE] Maintenance tasks in fiware-cygnus Git repository (#402)
- [FEATURE] Add support to batch processing within a transaction in OrionCKANSink (#567)
- [HARDENING] Remove unnecessary http mock in OrionHDFSSinkTest and fix some output messages (#605)
- [HARDENING] Trace the table_type parameter in OrionMySQLSink (#614)
- [HARDENING] Add enable_grouping parameter for OrionKafkaSink in agent.conf.template (#615)
- [FEATURE] Add OrionDynamoSink (#584)
- [HARDENING] Add flume-env.sh.template file to the configuration folder (#613)
- [HARDENING] Documentation refactoring (#611)
- [HARDENING] Change the name of the default log file, from flume.log to cygnus.log (#622)
- [BUG] Escape all the non-alphanumeric characters in Hive fields of column-like tables (#619)
- [FEATURE] The package name now has the Hadoop Core version Cygnus is compatible with (#565)
- [BUG] OrionTestSink now inherits the configuration from OrionSink base class (#632)
- [HARDENING] Add administration and programmers sections to all the sinks documentation (#631)
- [FEATURE] Add some interesting fields to CKAN persisted data (servicePath in row-like modes, servicePath, entityId and entityType in column-like modes), including migration script (#543)
- [FEATURE] Add batching support to OrionKafkaSink (#569)
- [HARDENING] Add the Quick Start Guide to readthedocs (#645)
- [HARDENING] Add a document containing a table in charge of relating features and Cygnus version (#638)

* Wed Nov 04 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.10.0
- [HARDENING] Cygnus user creation added to the from the sources installation guide (#558)
- [FEATURE] Add a binary implementation of the HDFS backend (#537)
- [HARDENING] Add docker as an option in the README for installing Cygnus (#552)
- [FEATURE] Add support to batch processing within a transaction in OrionHDFSSink (#554)
- [FEATURE] Add support to batch processing within a transaction in OrionMySQLSink (#568)
- [BUG] Add support to CKAN 2.4 API changes (#546)
- [BUG] Remove final line feed in the HDFS aggregation (#575)
- [FEATURE] Add some interesting fields to HDFS persisted data (servicePath in row-like modes, servicePath, entityId and entityType in column-like modes), including migration script (#544)
- [HARDENING] Document how to choose the hadoop-core dependency for compilation purposes (#559)
- [HARDENING] Add a batching timeout and accumulate the transaction IDs for right tracing (#562)
- [BUG] Ask for CKAN resources when populating a package cache after calling organization_show (#589)
- [FEATURE] Add some interesting fields to MySQL persisted data (servicePath in row-like modes, servicePath, entityId and entityType in column-like modes), including migration script (#501)
- [BUG] Fix the CKAN resource name building, removing references to old default destination (#592)
- [FEATURE] Allow enabling/disabling Hive in OrionHDFSSink (#555)
- [HARDENING] Explain the batching mechanism in the performance document (#564)
- [HARDENING] Fix the documentation regarding the installation from sources (#596)
- [BUG] Add extra fields (servicePath in row-like modes, servicePath, entityId and entityType in column-like modes) to Hive tables (#598)
- [FEATURE] Column-like persistence in OrionMongoSink (#548)

* Fri Oct 02 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.9.0
- [FEATURE] Add support for multiple file formats in HDFS; JSON is maintained and CSV is added (#303)
- [HARDENING] Add detailed information about reporting issues and contact information (#478)
- [HARDENING] Update the Quick Start Guide in order is is aligned with latest features (#490)
- [HARDENING] Remove patch supporting old versions of the CKAN API (<= 2.0) (#188)
- [FEATURE] The notified user agent is not checked anymore (#493)
- [FEATURE] Add OrionKafkaSink (#456)
- [HARDENING] Add missing configuration and documentation regarding OrionMongoSink and OrionSTHSink (#505)
- [FEATURE] Enable/disable grouping rules usage per sink (#447)
- [FEATURE] Add HiveServer2 support to OrionHDFSSink (#513)
- [FEATURE] Add Python-based clients both for HiveServer1 and HiveServer2 (#519)
- [FEATURE] Add Java-based clients both for HiveServer1 and HiveServer2 (#518)
- [HARDENING] Replace logs containing "HttpFS response" by "Server response" (#517)
- [BUG] Use a testing version of Zookeeper in OrionKafkaSink (#514)
- [BUG] Always create the Hive tables if not existing yet (#401)
- [HARDENING] Add {headers,body} structure to messages sent to Kafka (#512)
- [FEATURE] Allow selecting the table type (by service, servicePath or destination) (#540)
- [BUG] A second CKAN resource may be added to an existeng dataset in row mode (#280)
- [HARDENING] Add references to FIWARE and Cosmos Big Data Analysis GE in the README (#534)
- [BUG] entityId and entityType are now added to the CKAN resources when using OrionCKANSink in row mode (#539)

* Wed Jul 08 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.8.2
- [BUG] Fix many errors in the docuemtnation (#435)
- [BUG] Fix OrionMySQLLink in the README (#442)
- [BUG] Fix MySQL connections, adding a permanent connection to each database/fiware-service (#445)
- [BUG] Fix mongo-sink appearance in sth-sink configuration (both template and README) (#449)
- [BUG] Fix OrionMySQLSink, time zone is not added to the timestamp fields (#441)
- [HARDENING] Link the Quick Start Guide from the README (#454)
- [BUG] Fix the grouping rules validator, now empty fields and non-numeric ids are not allowed (#460)
- [HARDENING] Add detailed explanation about the syntax of the grouping rules (#459)
- [FEATURE] OAuth2 support for OrionHDFSSink (#483)

* Mon May 25 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.8.1
- [HARDENING] OrionHDFSSink parameters are now called "hdfs_*" instead of "cosmos_*" (#374)
- [HARDENING] Add a permament connection to the MySQL Backend (#364)
- [BUG] Add the original (and removed by OrionRESTHandler) slash character to fiware-servicePath (#403)
- [FEATURE] Use Json-like rules definition for pattern-based grouping (#387)
- [BUG] Set the Fiware-Service and Fiware-ServicePath maximum lenght to 50 characters (previously, it was 32 characters) (#406)
- [FEATURE] Add implementation details to the specific sinks documentation (#397)
- [BUG] Fix the origin date in OrionSTHSink (#413)
- [HARDENING] Remove unnecessary hfds_api parameter in OrionHDFSSink (#415)
- [FEATURE] Hashing based collections for OrionMongoSink and OrionSTHSink (#420)
- [HARDENING] Set data_model to collection-per-entity (#419)
- [BUG] Use multiple Fiware-ServicePaths in the sinks (#384)

* Thu May 07 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.8.0
- Ordered death of Cygnus if the logging system fails or it is stoped (#320)
- Management interface port is not opened twice anymore (#302)
- Added a polling interval parameter for reloading the Flume configuration (#334)
- Bug fix, flume-conf.properties.template file from Apache Flume is not copied anymore to Cygnus folder (#348)
- Cygnus exits if the HTTPSource listening port is already in use (#340)
- Added OrionMongoSink (#363)
- Bug fixed: matching table name can be null (no Flume parameter is used) or wrong (#367)
- Add specific sink documents, pointing to them from the README (#322)
- UTC/Z time zone detail added to all the sink timestamps (#370)
- Added OrionSTHSink (#19)
- Multitenancy support in HDFS (#338)
- ISODate objects are used for timestamping in OrionMongoSink (#372)
- Let the origin offset for months in aggregated points starts by 1 instead of 0 (#381)

* Fri Mar 06 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.7.1
- Added option to start/stop/status only one instance configured in Cygnus using init.d script (#332)
- Added a Quick Start Guide (#319)
- Added an explanation on the different configuration files in the README (#342)

* Fri Feb 20 2015 Francisco Romero <francisco.romerobueno@telefonica.com> 0.7.0
- Added Kerberos authentication in OrionHDFSSink (#290)
- Added posibility to start multiple instances of Cygnus (#299)
- Bug fixing: same port in log4j example about two Cygnus instances (#305)
- Bug fixing: error in configuration template (cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file) (#308)
- Encourage the usage of FQDNs regarding HDFS namenodes when using Kerberos (#309)
- Fixed the RoundRoundChannelSelector, now it works for multiple storages (#298)
- Allow for infinite TTL (#236)
- Added OrionTestSink (#307)
- Fixed OrionMySQLSink logger (now it logs as OrionMySQLSink, not as OrionHDFSSink) (#321)

* Wed Dec 17 2014 Daniel Moran <daniel.moranjimenez@telefonica.com> 0.6.0
- Remove the "resource" prefixing feature from MySQL and HDFS (#224)
- Add the "fiware-servicePath" header as a valid field of the grouping mechanism (#215)
- fiware-servicePath header processing (#212)
- Define and validate a common character set for all service and all resource names (#207)
- Bug fixing: correctly read the URL in the NGSI testing scripts (#255)
- Bug fixing: Document the correct Cygnus version in the README (#259)
- "iot_support at tid dot es" as contact email (#260)
- Round-Robin-like Flume Channel Selector (#254)
- Tuning tips for increading the Cygnus performance (#199)
- Bug fixing: The CKAN organization is not created in column mode anymore (#181)
- Bug fixing: Correct treatment of a root ("/") fiware-servicePath (#263)
- Run the Jetty server supporting the management interface from the appropriate place (#232)
- Do the resource URL in CKAN points to "none" (#246)
- Include changelog in Cygnus spec (#269)
- Change the way the PID is determined (#219)
- Removed execution privileges from configuration files (#264)
- Document how to install and run Cygnus from RPM (#275)
- Discard invalid matching rules at DestinationExtractor (#271)

* Tue Nov 11 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.4.2
- Enable https connections in CKAN (#230)
- Bug fixing: solve a conflict with Monit when running Cygnus as a service (#214)
- Bug fixing: delete the "cygnus" user when removing the Cygnus RPM (#213)
- Bug fixing: fix the events TTL parameter in the configuration template (#210)
- Bug fixing: errors when using '.' in CKAN organization names (#200)
- Bug fixing: process notifications with no contextAttributeList (#194)

* Sun Oct 19 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.5.1
- Bug fixing: typo in the configuration template (#238)
- Bug fixing: wrong path for the matching table (#239)

* Thu Oct 02 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.5
- Usage of SAX parser for XML-based notifications (#201)
- Cygnus version and snapshot shown in the logs (#187)
- Document how a developer may build his/her own sink (#136)
- Pattern-based grouping into tables (#107)
- Bug fixig: process notifications with no contextAttributeList (#222)
- Bug fixing: do the RPM scripts do not change the ownership of the whole /var/run folder (#203)
- Bug fixing: process the "value" tag in XML notifications instead of "contextValue" (#227)

* Wed Sep 05 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.4.1
- Only Flume events related to a persistence error are reínjected in the agent channel (#52)
- A TTL has been added to the re-injected Flume events (#126)
- Cygnus version is now showed in the logs (#104)
- Manual build and RPM build both using the same lo4j.properties.template (#111)
- Usage of Timestamp Interceptor instead of custom timestamping mechanism (#143)
- Configurable Hive endpoint independent of the HDFS endpoint (#173)
- Multiple HDFS endpoint setup (#175)
- Migration from Cygnus 0.1 to 0.2 or higher has been documented (#135)
- Migration from ngsi2cosmos to Cygnus has been documented (#105)
- Alarms related to Cygnus have been documented (#112)

* Thu Jul 31 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.4
- TDAF-like logs (#109)
- Standardize the default Cygnus logs path (#91)
- Bug fixing: quotes appearing in CKAN resource values (PR #97)
- Script for removing old data within CKAN-based short term historic (#62)
- CKAN basic management script (PR #99)
- CKAN cleanup script (PR #98)

* Thu Jul 03 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.3
- Attribute metadata persistence (#41)
- Multi-tenancy features (#36)
- Per column attribute persistence (#54 #60 #51)
- Move reception timestamping from the sink to the source (#53)
- MySQL connector added as a pom dependency (#78)
- RPM building framework (#76)
- Relaxed Orion version checking (#70)
- MySQL Connector added to pom.xml (#78)
- Translation scripts from Cygnus 0.1 to Cygnus 0.3 (#65)
- Script for removing old data within MySQL-based short term historic (#63)

* Wed Jun 04 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.2.1
- Change the OrionMySQLSink database and table names, by removing the "cygnus_" part (#57)
- Fixed bug in CKAN insertion mechanism

* Tue Jun 03 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.2
- Support for complex attribute values, specifically, vectors and objects (#43)
- Json-like persistency, independently of the notification type (XML or Json) or the backend (#29)
- Persistence in a MySQL backend (#42)
- Persistence in a CKAN backend (basic functionality) (#31)
- HDFS dataset and Hive table creation at startup time (#13)
- Change package names accordingly to the new repo structure (#30)
- Unit tests (#18)
- Add a template for cygnus.conf (#37)
- README files in UTF-8 (#33)
- Avoid special characters in HDFS file names, CKAN datastores IDs and MySQL table names (#9)
- Refactor the sinks and the HDFS backends, getting common code (#34)
- Per column attribute persistence in MySQL sink (#50)
- Hive basic client (#26)
- Plague Tracker application (#27)
- Translation scripts for moving from Cygnus 0.1 to Cygnus 0.2 (#29)
- XML and Json simple and compound notifications examples for testing (#39)

* Thu Mar 20 2014 Francisco Romero <francisco.romerobueno@telefonica.com> 0.1
- Parsing of simple (string-like values only) Json notifications (#8)
- Parsing of simple (string-like values only) XML notifications (#3)
- Persistence in a WebHDFS/HttpFS backend (#20)
- SFTP basic client
