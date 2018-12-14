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

Summary:          Package for Cygnus NGSI component
Name:             cygnus-ngsi
Version:          %{_product_version}
Release:          %{_product_release}
License:          AGPLv3
BuildRoot:        %{_topdir}/BUILDROOT/
BuildArch:        x86_64
Requires:         cygnus-common = %{_product_version}-%{_product_release}
Group:            Applications/cygnus
Vendor:           Telefonica I+D
Provides:         cygnus-ngsi = %{_product_version}-%{_product_release}


%description
This connector is a (conceptual) derivative work of ngsi2cosmos, and implements
a Flume-based connector for context data coming from Orion Context Broker.

# Project information
%define _project_name cygnus
%define _project_user cygnus

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
# read from SOURCES, write into BUILD

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
# Install section:
# -------------------------------------------------------------------------------------------- #
%install
# Read from BUILD and write into BUILDROOT
# RPM_BUILD_ROOT = BUILDROOT

mkdir -p %{_build_root_project}/conf

echo "[INFO] Installing the %{name}"

cp -R %{_builddir}/usr/cygnus/*                       %{_build_root_project}
cp %{_builddir}/config/*                              %{_build_root_project}/conf/

%clean
rm -rf $RPM_BUILD_ROOT


# -------------------------------------------------------------------------------------------- #
# Files to add to the RPM
# -------------------------------------------------------------------------------------------- #
%files
%defattr(755,%{_project_user},%{_project_user},755)

%{_project_install_dir}

%changelog
* Thu Dec 13 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.10.0
- [cygnus-ngsi][NGSINameMappingsInterceptor] Now namemapping checks sevice, subervice and (type of entity and id entity) of EntityMapping (#1535)
- [cygnus-ngsi][NGSIEvent] Unable to deliver event: null pointer getAttributeForNaming (#1506)
- [cygnus-ngsi][NGSIOrionSink] Create NGSIOrionSink (#1447)
- [cygnus-ngsi][NGSIDynamoDBSink] [bug] Southeast regions are bad coded (#1448)
- [cygnus-ngsi][KafkaSink] Use lower_case option to build topics (#1468)
- [cygnus-ngsi][KafkaSink] Using global connection to zookeeper instead of creating one each time an event arrives
- [cygnus-ngsi][Docker] Add name mappings files for multiagent
- [cygnus-ngsi][Docker] Add grouping rules files for multiagent (#1533)
- [cygnus-ngsi][Docker] Add new env var CYGNUS_MONITORING_TYPE

* Wed Jun 13 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.9.0
- [cygnus-ngsi][NGSINameMappingsInterceptor] Allow regular expression replacement for EntityIds (#1456)
- [cygnus-ngsi][HttpBackend] Add support to recover data from response header (#1455)
- [cygnus-ngsi][KeystoneUtils] Create new Auth functionality (#1471)
- [cygnus-ngsi][Docker] Add support to run cygnus docker in multiagent mode (mysql, mongo, ckan, hdfs, postgresql and carto sinks) (#1486)
- [cygnus-ngsi][Docker] Upgrade to just enable sinks and channels depending on ENV vars (#1482)
- [cygnus-ngsi][Docker] Upgrade to CentOS 7, Maven 3.5.2 and Java 1.8.0 in Dockerfile

* Wed Sep 13 2017 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.8.0
- [cygnus-ngsi][hardening] Add complex configuration examples (#629)
- [cygnus-ngsi][bug] Fix NGSIMySQLSink when persisting bulks of rows with different number of columns (#1409)
- [cygnus-ngsi][feature] Add metadata storing in NGSIMongoSink (#1156)
- [cygnus-ngsi][hardening] Set transaction and correlation IDs to 'N/A' when there is no activity (#1175)
- [cygnus-ngsi][hardening] Pre-aggregate batches in NGSISTHSink (#1359)
- [cygnus-ngsi][hardening] Change the name of historic-related enabling parameters (#1314)
- [cygnus-ngsi][bug] Fix NotifyContextRequest.ContextAttribute.toString(), removing double quote for attribute values (#1430)
- [cygnus-ngsi][bug] Add support for file channels (#1419)
- [cygnus-ngsi][hardening] Add a document about reliability (#1433)
- [cygnus-ngsi][hardening] Add a note about optional channels when using Flume's ReplicatingChannelSelector (#510)
- [cygnus-ngsi][hardening] Improve HDFS sink logs (#1358)
- [cygnus-ngsi][hardening] Add CygnusError base class (#1441)
- [cygnus-common, cygnus-ngsi][hardening] Fix FIWARE repository URL (#1435)
- [cygnus-common, cygnus-ngsi][hardening] Unify test classes (#1332, #1333)

* Tue Jan 31 2017 Francisco Romero <francisco.romerobueno@telefonica.com> 1.7.0 
- [cygnus-ngsi][feature] Add global data capping/expirationn options based on time and size (#1317)
- [cygnus-ngsi][feature] Add specific data capping/expiration implementation for NGSICKANSink (#1344)
- [cygnus-ngsi][feature] Add specific data capping/expiration implementation for NGSIMySQLSink (#1365)
- [cygnus] Add Travis CI (#1347)
- [cygnus-ngsi][hardening] Force lowercase organization, dataset and resource names in NGSICKANSink (#1352)
- [cygnus-ngsi][hardening] Replace warnings upon unnecessary header received with debug traces at NGSIRestHandler (#1354)
- [cygnus-ngsi][hardening] Improve persistence error logs at NGSISink (#1358)
- [cygnus-ngsi][hardening] Add raw bytes to NGSIEvent (#1345)
- [cygnus-ngsi][hardening] Add cache to NGSIMySQLSink (#130)
- [cygnus][hardening] Fix "FIWARE" name in license headers (#1369)
- [cygnus-ngsi][bug] Differentiate among enterprise and individual Carto accounts (#1382)
- [cygnus-ngsi][hardening] Add integration example with Apache Spark (#1397)
- [cygnus-ngsi][hardening] Add a piece of documentation about capping and/or expirating records in CKAN, MySQL, MongoDB and STH Comet (#1391)
- [cygnus-ngsi][hardening] Update Cygnus NGSI README with last features and new advanced topics (#1390)

* Fri Dec 02 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.6.0
- [cygnus-ngsi][hardening] Update migration script for HDFS regarding new encoding (#1271)
- [cygnus-ngsi][feature] Add support for geo:json Orion's type in NGSICartoDBSink (#1275)
- [cygnus-ngsi][hardening] Produce NGSIEvent's at Grouping Rules interceptor (#1280)
- [cygnus-ngsi][hardening] NGSIEvent must contain ContextElement's instead of NotifyContextRequest's (#1203)
- [cygnus-ngsi][hardening] Add a note regarding supported NGSI version (#1288)
- [cygnus-ngsi][hardening] Produce NGSIEvent's at NGSIRestHandler (#1204)
- [cygnus-ngsi][hardening] Avoid concatenation on entities naming when the type is null or empty (#1279)
- [cygnus-ngsi][bug] Add concatenator to CKAN packages when new encoding is enabled (#1291)
- [cygnus-ngsi][hardening] Add a documentation piece regarding deprecated functionality (#1289)
- [cygnus-ngsi][hardening] Add a note regarding geolocation attributes in NGSICKANSink documentation (#1293)
- [cygnus-ngsi][hardening] Add OLD_CONCATENATOR constant (#1284)
- [cygnus-ngsi][hardening] Document how Name Mappings behave in absence of a "newSomething" field (#1302)
- [cygnus-ngsi][bug] Close BufferedReader used to read notifications (#1304)
- [cygnus-ngsi][hardening] Deprecate Grouping Rules (#1182)
- [cygnus-ngsi][feature] Add raw snapshot analysis mode to NGSICKANSink (#1273)
- [cygnus-ngsi][bug] Fix getting location header when creating Json responses in HttpBackend (#1318)
- [cygnus-ngsi][hardening] Update performance document (#1306)
- [cygnus-ngsi][hardening] Improve HDFSBackendImplREST debug logs (#1319)
- [cygnus-ngsi][hardening] Update NGSICKANSink documentation regarding resource name length limit imposed by Cygnus (#1325)
- [cygnus-ngsi][bug] Obtained geometry not properly used in NGSICartoDBSink (#1327)
- [cygnus-ngsi][hardening] Replace flip_coordinates with swap_coordinates in NGSICartoDBSink (#1313)
- [cygnus-ngsi][bug] Add event to already existent sub-batches in NGSIBatch (#1331)
- [cygnus-ngsi][hardening] Warn instead of Error when no OAuth2 token is configured for NGSIHDFSSink (#1330)

* Wed Nov 02 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.5.0
- [cygnus][hardening] Add NGSICKANSink configuration in docker's agent.conf file (#1193)
- [cygnus][hardening] Add entry for Grouping Rules in Installation and Administration Guide (#1206)
- [cygnus-ngsi][hardening] Delay the retries of not persisted batches (#1138)
- [cygnus-ngsi][hardening] Add support for regular expressions in Name Mappings (#1202)
- [cygnus-ngsi][bug] Fix accepted charset by Kafka (#1217)
- [cygnus][hardening] Add missing creation of log4j path (#1212)
- [cygnus][hardening] Add compilation scripts (#1213)
- [cygnus][hardening] Warn about the incompatibility of installing cygnus-ngsi and cygnus-twitter in the same base path (#1214)
- [cygnus-ngsi][hardening] Fix automated builds because of "filelock denied" (#1208)
- [cygnus][hardening] Fix docker links in badges (#1222)
- [cygnus-ngsi][hardening] Update JMeter tests (#1229)
- [cygnus][hardening] Update link about getting tokens in documentations (#1227)
- [cygnus][hardening] Replace references to JDK 1.6 with JDK 1.7 (#1232)
- [cygnus][hardening] Add NGSIHDFSSink configuration in docker's agent.conf file (#1172)
- [cygnus][hardening] Fix NGSIMySQLSink configuration in docker's agent.conf file (#1237)
- [cygnus][hardening] Fix NGSIMongoSink and NGSISTHSink configuration in docker's agent.conf file (#1239)
- [cygnus][bug] Discard Http responses not containing Json payload when creating JsonResponse objects (#1243)
- [cygnus][hardening] Force the usage of certain version in libraries used by cygnus-ngsi docker (#1233)
- [cygnus][hardening] Fix yum install command (#1249)
- [cygnus][hardening] Add FIWARE architecture section in the documentation (#1248)
- [cygnus][hardening] Fix API entries in documentation (#1246)
- [cygnus][hardening] Add entries about running as process and running as service in documentation (#1255)
- [cygnus][hardening] Do configurable the CKAN viewer attached to resources (#1258)
- [cygnus][hardening] Document IPv6 support (#1058)
- [cygnus][hardening] Add introductory course slides link (#1265)

* Fri Oct 07 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.4.0
- [cygnus][bug] Fix documentation about DELETE subscriptions (#1147)
- [cygnus-ngsi][hardening] Remove unused hashing feature in NGSIMongoSink and NGSISTHSink (#1113)
- [cygnus-ngsi][hardening] Configurable Http parameters at backend (#1152)
- [cygnus-twitter][bug] Fixed misspelled underscores in doc examples (#1161)
- [cygnus-ngsi] [feature] Add implementation for /admin/log route (807) - continue - (#807)
- [cygnus-ngsi][bug] Add certain Http responses as condition for changing host (#1176)
- [cygnus-ngsi][feature] Name mappings (#1076)
- [cygnus-ngis][hardening] Add a cache for already created elements in PostgreSQLSink (#1171)
- [cygnus][hardening] Update mkdocs.yml with the google analytics ID (#1183)
- [cygnus][hardening] Update default agent.conf file with latests paremeters (#1194)

* Fri Sep 09 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.3.0
- [cygnus-ngsi][bug] Change mysql and mongo hosts to "iot-mysql" and "iot-mongo" (#1105)
- [cygnus-ngsi][feature] General encoding of per-sink forbidden characters (#1107)
- [cygnus][hardening] Add architecture documentation (#1127)
- [cygnus][feature] Standardize logs layout (#1118)
- [cygnus-ngsi][hardening] Reduce the size of the Docker image (#1117)
- [cygnus-ngsi][hardening] Docker image must print the logs in console (#1125)
- [cygnus-ngsi][feature] Do per-sink active checkings of element name sizes (#407)
- [cygnus][hardening] Add Orion-Cygnus-Kafka integration example (#1132)
- [cygnus-ngsi][hardening] Decrease to DEBUG the logging level of batching related traces (#1126)

* Wed Sep 07 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.2.1
- [cygnus][hardening] Add architecture documentation (#1127)
- [cygnus-ngsi][bug] Insert NULL in CKAN columns when an empty attribute value ("", null, {} or []) or empty attribute metadata (null or []) is notified (#1144)

* Fri Jul 01 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.2.0
- [cygnus-ngsi] [hardening] Do configurable the accepted resolutions in NGSISTHSink (#940)
- [cygnus-ngsi] [feature] Log time in UTC format (#1071)
- [cygnus-ngsi] [hardening] Document how the keys configuration file works (#1078)

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
