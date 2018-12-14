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
cygnus-ngsi.sinks = hdfs-sink mysql-sink ckan-sink mongo-sink sth-sink kafka-sink dynamo-sink postgresql-sink cartodb-sink
cygnus-ngsi.channels = hdfs-channel mysql-channel ckan-channel mongo-channel sth-channel kafka-channel dynamo-channel postgresql-channel cartodb-channel

#=============================================
# source configuration
# channel name where to write the notification events
cygnus-ngsi.sources.http-source.channels = hdfs-channel mysql-channel ckan-channel mongo-channel sth-channel kafka-channel dynamo-channel postgresql-channel
# source class, must not be changed
cygnus-ngsi.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# listening port the Flume source will use for receiving incoming notifications
cygnus-ngsi.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
cygnus-ngsi.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
# URL target
cygnus-ngsi.sources.http-source.handler.notification_target = /notify
# default service (service semantic depends on the persistence sink)
cygnus-ngsi.sources.http-source.handler.default_service = default
# default service path (service path semantic depends on the persistence sink)
cygnus-ngsi.sources.http-source.handler.default_service_path = /
# source interceptors, do not change
cygnus-ngsi.sources.http-source.interceptors = ts gi
# TimestampInterceptor, do not change
cygnus-ngsi.sources.http-source.interceptors.ts.type = timestamp
# GroupingInterceptor, do not change
cygnus-ngsi.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
# Grouping rules for the GroupingIntercetor, put the right absolute path to the file if necessary
# see the doc/design/interceptors document for more details
cygnus-ngsi.sources.http-source.interceptors.gi.grouping_rules_conf_file = /usr/cygnus/conf/grouping_rules.conf

# ============================================
# NGSIHDFSSink configuration
# channel name from where to read notification events
cygnus-ngsi.sinks.hdfs-sink.channel = hdfs-channel
# sink class, must not be changed
cygnus-ngsi.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.NGSIHDFSSink
# true or false, true applies the new encoding, false applies the old encoding.
#cygnus-ngsi.sinks.hdfs-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.enable_lowercase = false
# rest if the interaction with HDFS will be WebHDFS/HttpFS-based, binary if based on the Hadoop API
#cygnus-ngsi.sinks.hdfs-sink.backend.impl = rest
# maximum number of Http connections to HDFS backend
#cygnus-ngsi.sinks.hdfs-sink.backend.max_conns = 500
# maximum number of Http connections per route to HDFS backend
#cygnus-ngsi.sinks.hdfs-sink.backend.max_conns_per_route = 100
# Comma-separated list of FQDN/IP address regarding the HDFS Namenode endpoints
# If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
#cygnus-ngsi.sinks.hdfs-sink.hdfs_host = x1.y1.z1.w1,x2.y2.z2.w2
# port of the HDFS service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs
#cygnus-ngsi.sinks.hdfs-sink.hdfs_port = 14000
# username allowed to write in HDFS
cygnus-ngsi.sinks.hdfs-sink.hdfs_username = hdfs_username
# password for the above username; this is only required for Hive authentication
cygnus-ngsi.sinks.hdfs-sink.hdfs_password = xxxxxxxx
# OAuth2 token for HDFS authentication
cygnus-ngsi.sinks.hdfs-sink.oauth2_token = xxxxxxxx
# true if the notified fiware-service (or the default one, if no one is notified) is used as the HDFS namespace, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.service_as_namespace = false
# how the attributes are stored, available formats are json-row, json-column, csv-row and csv-column
#cygnus-ngsi.sinks.hdfs-sink.file_format = json-column
# character used for separating the values when using CSV file formats
#cygnus-ngsi.sinks.hdfs-sink.csv_separator = ,
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.hdfs-sink.batch_size = 100
# timeout for batch accumulation
# cygunsagent.sinks.hdfs-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.hdfs-sink.batch_ttl = 10
# Hive enabling
#cygnus-ngsi.sinks.hdfs-sink.hive = false
# Hive server version, 1 or 2 (ignored if hive is false)
#cygnus-ngsi.sinks.hdfs-sink.hive.server_version = 2
# Hive FQDN/IP address of the Hive server (ignored if hive is false)
#cygnus-ngsi.sinks.hdfs-sink.hive.host = x.y.z.w
# Hive port for Hive external table provisioning (ignored if hive is false)
#cygnus-ngsi.sinks.hdfs-sink.hive.port = 10000
# Hive database type, available types are default-db and namespace-db
#cygnus-ngsi.sinks.hdfs-sink.hive.db_type = default-db
# Kerberos-based authentication enabling
#cygnus-ngsi.sinks.hdfs-sink.krb5_auth = false
# Kerberos username (ignored if krb5_auth is false)
cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
# Kerberos password (ignored if krb5_auth is false)
cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
# Kerberos login file (ignored if krb5_auth is false)
#cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file = /usr/cygnus/conf/krb5_login.conf
# Kerberos configuration file (ignored if krb5_auth is false)
#cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

# ============================================
# NGSICKANSink configuration
# channel name from where to read notification events
cygnus-ngsi.sinks.ckan-sink.channel = ckan-channel
# sink class, must not be changed
cygnus-ngsi.sinks.ckan-sink.type = com.telefonica.iot.cygnus.sinks.NGSICKANSink
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.ckan-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.ckan-sink.enable_name_mappings = false
# true applies the new encoding, false applies the old encoding.
#cygnus-ngsi.sinks.ckan-sink.enable_encoding = false
# the CKAN API key to use
cygnus-ngsi.sinks.ckan-sink.api_key = ckanapikey
# the FQDN/IP address for the CKAN API endpoint
#cygnus-ngsi.sinks.ckan-sink.ckan_host = x.y.z.w
# the port for the CKAN API endpoint
#cygnus-ngsi.sinks.ckan-sink.ckan_port = 80
# the viewer attached to the created resources
#cygnus-ngsi.sinks.ckan-sink.ckan_viewer = recline_grid_view
# Orion URL used to compose the resource URL with the convenience operation URL to query it
#cygnus-ngsi.sinks.ckan-sink.orion_url = http://localhost:1026
# how the attributes are stored, either per row either per column (row, column)
#cygnus-ngsi.sinks.ckan-sink.attr_persistence = row
# enable SSL for secure Http transportation; 'true' or 'false'
#cygnus-ngsi.sinks.ckan-sink.ssl = false
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.ckan-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi.sinks.ckan-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.ckan-sink.batch_ttl = 10
# maximum number of Http connections to CKAN backend
#cygnus-ngsi.sinks.ckan-sink.backend.max_conns = 500
# maximum number of Http connections per route to CKAN backend
#cygnus-ngsi.sinks.ckan-sink.backend.max_conns_per_route = 100

# ============================================
# NGSIPostgreSQLSink configuration
# channel name from where to read notification events
cygnus-ngsi.sinks.postgresql-sink.channel = postgresql-channel
# sink class, must not be changed
cygnus-ngsi.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIPostgreSQLSink
# true applies the new encoding, false applies the old encoding.
#cygnus-ngsi.sinks.postgresql-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.postgresql-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.postgresql-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.postgresql-sink.enable_lowercase = false
# the FQDN/IP address where the PostgreSQL server runs
#cygnus-ngsi.sinks.postgresql-sink.postgresql_host = x.y.z.w
# the port where the PostgreSQL server listens for incomming connections
#cygnus-ngsi.sinks.postgresql-sink.postgresql_port = 5432
# the name of the postgresql database
#cygnus-ngsi.sinks.postgresql-sink.postgresql_database = postgres
# a valid user in the PostgreSQL server
#cygnus-ngsi.sinks.postgresql-sink.postgresql_username = root
# password for the user above
#cygnus-ngsi.sinks.postgresql-sink.postgresql_password = xxxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
#cygnus-ngsi.sinks.postgresql-sink.attr_persistence = column
# select the data_model: dm-by-service-path or dm-by-entity
#cygnus-ngsi.sinks.postgresql-sink.data_model = by-service-path
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.postgresql-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi.sinks.postgresql-sink.batch_timeout = 30
# number of retries upon persistence error
cygnus-ngsi.sinks.postgresql-sink.batch_ttl = 10
# true enables cache, false disables cache
#cygnus-ngsi.sinks.postgresql-sink.backend.enable_cache = false

# ============================================
# NGSIMySQLSink configuration
# channel name from where to read notification events
cygnus-ngsi.sinks.mysql-sink.channel = mysql-channel
# sink class, must not be changed
cygnus-ngsi.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMySQLSink
# true applies the new encoding, false applies the old encoding
#cygnus-ngsi.sinks.mysql-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.mysql-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.mysql-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.enable_lowercase = false
# the FQDN/IP address where the MySQL server runs
#cygnus-ngsi.sinks.mysql-sink.mysql_host = x.y.z.w
# the port where the MySQL server listens for incomming connections
#cygnus-ngsi.sinks.mysql-sink.mysql_port = 3306
# a valid user in the MySQL server
#cygnus-ngsi.sinks.mysql-sink.mysql_username = root
# password for the user above
#cygnus-ngsi.sinks.mysql-sink.mysql_password = xxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
#cygnus-ngsi.sinks.mysql-sink.attr_persistence = column
# select the data_model: dm-by-service-path or dm-by-entity
#cygnus-ngsi.sinks.mysql-sink.data_model = dm-by-entity
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.mysql-sink.batch_size = 100
# timeout for batch accumulation
# cygunsagent.sinks.mysql-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.mysql-sink.batch_ttl = 10

# ============================================
# NGSIMongoSink configuration
# sink class, must not be changed
cygnus-ngsi.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMongoSink
# channel name from where to read notification events
cygnus-ngsi.sinks.mongo-sink.channel = mongo-channel
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.mongo-sink.enable_name_mappings = false
# true applies the new encoding, false applies the old encoding
#cygnus-ngsi.sinks.mongo-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.mongo-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.enable_lowercase = false
# FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run
#cygnus-ngsi.sinks.mongo-sink.mongo_hosts = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# a valid user in the MongoDB server (or empty if authentication is not enabled in MongoDB)
#cygnus-ngsi.sinks.mongo-sink.mongo_username = mongo_username
# password for the user above (or empty if authentication is not enabled in MongoDB)
#cygnus-ngsi.sinks.mongo-sink.mongo_password = xxxxxxxx
# prefix for the MongoDB databases
#cygnus-ngsi.sinks.mongo-sink.db_prefix = sth_
# prefix for the MongoDB collections
#cygnus-ngsi.sinks.mongo-sink.collection_prefix = sth_
# select the data_model: dm-by-service-path, dm-by-entity or dm-by-attribute
#cygnus-ngsi.sinks.mongo-sink.data_model = dm-by-entity  
# how the attributes are stored, either per row either per column (row, column)
#cygnus-ngsi.sinks.mongo-sink.attr_persistence = column
# true if the attribute metadata will be stored, false by default
#cygnus-ngsi.sinks.mongo-sink.attr_metadata_store = false
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.mongo-sink.batch_size = 100
# timeout for batch accumulation
# cygunsagent.sinks.mongo-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.mongo-sink.batch_ttl = 10
# true if white space-based attribute values must be ignored
#cygnus-ngsi.sinks.mongo-sink.ignore_white_spaces = true
# value specified in seconds. Set to 0 if not wanting this policy
#cygnus-ngsi.sinks.mongo-sink.data_expiration = 0
# value specified in bytes. Set to 0 if not wanting this policy. Minimum value (different than 0) is 4096 bytes
#cygnus-ngsi.sinks.mongo-sink.collections_size = 0
# value specifies the number of documents. Set to 0 if not wanting this policy
#cygnus-ngsi.sinks.mongo-sink.max_documents = 0
# true if exclusively white space-based attribute values must be ignored, false otherwise
#cygnus-ngsi.sinks.mongo-sink.ignore_white_spaces = true

# ============================================
# NGSISTHSink configuration
# sink class, must not be changed
cygnus-ngsi.sinks.sth-sink.type = com.telefonica.iot.cygnus.sinks.NGSISTHSink
# channel name from where to read notification events
cygnus-ngsi.sinks.sth-sink.channel = sth-channel
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.mongo-sink.enable_name_mappings = false
# true applies the new encoding, false applies the old encoding
#cygnus-ngsi.sinks.mongo-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.sth-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.hdfs-sink.enable_lowercase = false
# FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run
#cygnus-ngsi.sinks.sth-sink.mongo_hosts = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# a valid user in the MongoDB server (or empty if authentication is not enabled in MongoDB)
#cygnus-ngsi.sinks.sth-sink.mongo_username = mongo_username
# password for the user above (or empty if authentication is not enabled in MongoDB)
#cygnus-ngsi.sinks.sth-sink.mongo_password = xxxxxxxx
# prefix for the MongoDB databases
#cygnus-ngsi.sinks.sth-sink.db_prefix = sth_
# prefix for the MongoDB collections
#cygnus-ngsi.sinks.sth-sink.collection_prefix = sth_
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.sth-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi.sinks.sth-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.sth-sink.batch_ttl = 10
# true if white space-based attribute values must be ignored
#cygnus-ngsi.sinks.sth-sink.ignore_white_spaces = true
# select the data_model: dm-by-service-path, dm-by-entity or dm-by-attribute
#cygnus-ngsi.sinks.sth-sink.data_model = dm-by-entity
# accepted values are month, day, hour, minute and second separated by comma
#cygnus-ngsi.sinks.sth-sink.resolutions = month,day
# value specified in seconds. Set to 0 if not wanting this policy
#cygnus-ngsi.sinks.sth-sink.data_expiration = 0

#=============================================
# NGSIKafkaSink configuration
# sink class, must not be changed
cygnus-ngsi.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.NGSIKafkaSink
# channel name from where to read notification events
cygnus-ngsi.sinks.kafka-sink.channel = kafka-channel
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.kafka-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.kafka-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.kafka-sink.enable_lowercase = false
# select the data_model: dm-by-service, dm-by-service-path, dm-by-entity or dm-by-attribute
#cygnus-ngsi.sinks.kafka-sink.data_model = dm-by-entity
# comma-separated list of Kafka brokers (a broker is defined as host:port)
#cygnus-ngsi.sinks.kafka-sink.broker_list = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# Zookeeper endpoint needed to create Kafka topics, in the form of host:port
#cygnus-ngsi.sinks.kafka-sink.zookeeper_endpoint = x.y.z.w:port
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.kafka-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi.sinks.kafka-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.kafka-sink.batch_ttl = 10
# rumber of partitions for a topic
#cygnus-ngsi.sinks.kafka-sink.partitions = 5
# replication factor must be less than or equal to the number of brokers created
#cygnus-ngsi.sinks.kafka-sink.replication_factor = 1

# ============================================
# NGSIDynamoDBSink configuration
# sink class, must not be changed
cygnus-ngsi.sinks.dynamo-sink.type = com.telefonica.iot.cygnus.sinks.NGSIDynamoDBSink
# channel name from where to read notification events
cygnus-ngsi.sinks.dynamo-sink.channel = dynamo-channel
# AWS Access Key Id
cygnus-ngsi.sinks.dynamo-sink.access_key_id = xxxxxxxx
# AWS Secret Access Key
cygnus-ngsi.sinks.dynamo-sink.secret_access_key = xxxxxxxxx
# AWS region where the tables will be created (link)
#cygnus-ngsi.sinks.dynamo-sink.region = eu-central-1
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.dynamo-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.dynamo-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.dynamo-sink.enable_lowercase = false
# how the attributes are stored, either per row either per column (row, column)
#cygnus-ngsi.sinks.dynamo-sink.attr_persistence = column
# select the data_model: dm-by-entity 	dm-by-entity or dm-by-service-path
#cygnus-ngsi.sinks.dynamo-sink.data_model = dm-by-entity
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.dynamo-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi.sinks.dynamo-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.dynamo-sink.batch_ttl = 10

# ============================================
# NGSICartoDBSink configuration
# sink class, must not be changed
cygnus-ngsi.sinks.cartodb-sink.type = com.telefonica.iot.cygnus.sinks.NGSICartoDBSink
# channel name from where to read notification events
cygnus-ngsi.sinks.cartodb-sink.channel = cartodb-channel
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-ngsi.sinks.cartodb-sink.enable_grouping = false
# true if name mappings are enabled for this sink, false otherwise
#cygnus-ngsi.sinks.cartodb-sink.enable_name_mappings = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-ngsi.sinks.cartodb-sink.enable_lowercase = false
# select the data_model: dm-by-service-path or dm-by-entity
#cygnus-ngsi.sinks.cartodb-sink.data_model = dm-by-entity
# absolute path to the CartoDB file containing the mapping between FIWARE service/CartoDB usernames and CartoDB API Keys
cygnus-ngsi.sinks.cartodb-sink.keys_conf_file = /usr/cygnus/conf/cartodb_keys.conf
# if true the latitude and longitude values are exchanged, false otherwise
#cygnus-ngsi.sinks.cartodb-sink.swap_coordinates = true
# if true, a raw based storage is done, false otherwise
#cygnus-ngsi.sinks.cartodb-sink.enable_raw = true
# if true, a distance based storage is done, false otherwise
#cygnus-ngsi.sinks.cartodb-sink.enable_distance = false
# number of notifications to be included within a processing batch
#cygnus-ngsi.sinks.cartodb-sink.batch_size = 100
# timeout for batch accumulation
#cygnus-ngsi.sinks.cartodb-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-ngsi.sinks.cartodb-sink.batch_ttl = 10
# maximum number of connections allowed for a Http-based HDFS backend
#cygnus-ngsi.sinks.cartodb-sink.backend.max_conns = 500
# maximum number of connections per route allowed for a Http-based HDFS backend
#cygnus-ngsi.sinks.cartodb-sink.backend.max_conns_per_route = 100

# =============================================
# hdfs-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.hdfs-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.hdfs-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.hdfs-channel.transactionCapacity = 100

# =============================================
# ckan-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.ckan-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.ckan-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.ckan-channel.transactionCapacity = 100

# =============================================
# mysql-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.mysql-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.mysql-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.mysql-channel.transactionCapacity = 100

# =============================================
# postgresql-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.postgresql-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.postgresql-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.postgresql-channel.transactionCapacity = 100

# =============================================
# mongo-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.mongo-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.mongo-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.mongo-channel.transactionCapacity = 100

# =============================================
# sth-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.sth-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.sth-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.sth-channel.transactionCapacity = 100

# =============================================
# kafka-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.kafka-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.kafka-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.mkafka-channel.transactionCapacity = 100

# =============================================
# dynamo-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.dynamo-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.dynamo-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.dynamo-channel.transactionCapacity = 100

# ============================================
# cartodb-channel configuration
# channel type (must not be changed)
cygnus-ngsi.channels.cartodb-channel.type = memory
# capacity of the channel
cygnus-ngsi.channels.cartodb-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-ngsi.channels.cartodb-channel.transactionCapacity = 100
```

[Top](#top)
