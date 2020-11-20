# <a name="top"></a>cygnus-ngsi agent configuration
cygnus-ngsi, as any other Cygnus agent, follows the multi-instance configuration of cygnus-common.

The file `agent_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/agent_ngsi.conf.template`.

```Java
#=============================================
# To be put in APACHE_FLUME_HOME/conf/cygnus.conf
#
# General configuration template explaining how to setup a sink of each of the available types (HDFS, CKAN, MySQL, PostgreSQL, Mongo, STH Comet, Kafka, DynamoDB, CartoDB).

#=============================================
# The next tree fields set the sources, sinks and channels used by Cygnus. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use multiple types at the same time; the only requirement is to provide a channel for each
# one of them (this example shows how to configure 3 sink types at the same time). Even, you can define more than one
# sink of the same type and sharing the channel in order to improve the performance (this is like having
# multi-threading).
cygnus-ngsi.sources = http-source
cygnus-ngsi.sinks = postgresql-sink 
cygnus-ngsi-ld.channels = postgresql-channel 

#=============================================
# source configuration
# channel name where to write the notification events
cygnus-ngsi-ld.sources.http-source.channels = postgresql-channel 
# source class, must not be changed
cygnus-ngsi-ld.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# listening port the Flume source will use for receiving incoming notifications
cygnus-ngsi-ld.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
cygnus-ngsi-ld.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
# URL target
cygnus-ngsi-ld.sources.http-source.handler.notification_target = /notify
# default service (service semantic depends on the persistence sink)
cygnus-ngsi-ld.sources.http-source.handler.default_service = default
# default service path (service path semantic depends on the persistence sink)
cygnus-ngsi-ld.sources.http-source.handler.default_service_path = /
# source interceptors, do not change
cygnus-ngsi-ld.sources.http-source.interceptors = ts gi
# TimestampInterceptor, do not change
cygnus-ngsi-ld.sources.http-source.interceptors.ts.type = timestamp
# GroupingInterceptor, do not change
cygnus-ngsi-ld.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
# Grouping rules for the GroupingIntercetor, put the right absolute path to the file if necessary
# see the doc/design/interceptors document for more details
cygnus-ngsi-ld.sources.http-source.interceptors.gi.grouping_rules_conf_file = /usr/cygnus/conf/grouping_rules.conf


# ============================================
# NGSIPostgreSQLSink configuration
# channel name from where to read notification events
cygnus-ngsi-ld.sinks.postgresql-sink.channel = postgresql-channel
# sink class, must not be changed
cygnus-ngsi-ld.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIPostgreSQLSink
# true applies the new encoding, false applies the old encoding.
#cygnus-ngsi-ld.sinks.postgresql-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi-ld.sinks.postgresql-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi-ld.sinks.postgresql-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi-ld.sinks.postgresql-sink.enable_lowercase = false
# the FQDN/IP address where the PostgreSQL server runs
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_host = x.y.z.w
# the port where the PostgreSQL server listens for incomming connections
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_port = 5432
# the name of the postgresql database
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_database = postgres
# a valid user in the PostgreSQL server
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_username = root
# password for the user above
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_password = xxxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_database = postgres
# the database name of PostgreSQL
#cygnus-ngsi-ld.sinks.postgresql-sink.attr_persistence = column
# select the data_model: dm-by-service-path, dm-by-entity or dm-by-entity-type
#cygnus-ngsi-ld.sinks.postgresql-sink.data_model = by-service-path
# number of notifications to be included within a processing batch
#cygnus-ngsi-ld.sinks.postgresql-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi-ld.sinks.postgresql-sink.batch_timeout = 30
# number of retries upon persistence error
cygnus-ngsi-ld.sinks.postgresql-sink.batch_ttl = 10
# true enables cache, false disables cache
#cygnus-ngsi-ld.sinks.postgresql-sink.backend.enable_cache = false
#cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_options = sslmode=require
# the jdbc optional parameters string which concatinates to jdbc url

# =============================================
# postgresql-channel configuration
# channel type (must not be changed)
cygnus-ngsi-ld.channels.postgresql-channel.type = memory
# capacity of the channel
cygnus-ngsi-ld.channels.postgresql-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi-ld.channels.postgresql-channel.transactionCapacity = 100



[Top](#top)
