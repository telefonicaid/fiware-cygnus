#<a name="top"></a>Cygnus agent configuration
Content:

* [Introduction](#section1)
* [`cygnus_instance_<id>.conf`](#section2)
* [`agent_<id>.conf`](#section3)

##<a name="section1"></a>Introduction
Cygnus is configured through two different files:

* A `cygnus_instance_<id>.conf` file addressing all those non Flume parameters, such as the Flume agent name, the specific log file for this instance, the administration port, etc. This configuration file is not necessary if Cygnus is run as a standlalone application (see later), bt it is mandatory if run as a service (see later).
* An `agent_<id>.conf` file addressing all those Flume parameters, i.e. how to configure the different sources, channels, sinks, etc. that compose the Flume agent behind the Cygnus instance. always mandatory.

Please observe there may exist several Cygnus instances identified by `<id>`, and this `<id>` must be the same for both configuration files regarding the same Cygnus instance. This is necessary if wanting to run several instances of Cygnus as a service in the same machine. E.g. running two different instances of Cygnus will require:

* First instance:
    * `cygnus_instance_1.conf`
    * `agent_1.conf`
* Second instance:
    * `cygnus_instance_2.conf`
    * `agent_2.conf`

In addition, (a unique) `log4j.properties` controls how Cygnus logs its traces.

[Top](#top)

##<a name="section2"></a>`cygnus_instance_<id>.conf`
The file `cygnus_instance_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/cygnus_instance.conf.template`.

```
# The OS user that will be running Cygnus. Note this must be `root` if you want to run cygnus in a privileged port (<1024), either the admin port or the port in which Cygnus receives Orion notifications
CYGNUS_USER=cygnus
# Which is the config folder
CONFIG_FOLDER=/usr/cygnus/conf
# Which is the config file
CONFIG_FILE=/usr/cygnus/conf/agent_<id>.conf
# Name of the agent. The name of the agent is not trivial, since it is the base for the Flume parameters naming conventions, e.g. it appears in <AGENT_NAME>.sources.http-source.channels=...
AGENT_NAME=cygnusagent
# Name of the logfile located at /var/log/cygnus. It is important to put the extension '.log' in order to the log rotation works properly
LOGFILE_NAME=cygnus.log
# Administration port. Must be unique per instance
ADMIN_PORT=8081
# Polling interval (seconds) for the configuration reloading
POLLING_INTERVAL=30
```

As you can see, this file allows configuring the log file. For a detailed logging configuration, please check the [`log4j.properties`](#section6) section.

[Top](#top)

##<a name="section3"></a>`agent_<id>.conf`
The file `agent_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/agent.conf.template`.

```Java
#=============================================
# To be put in APACHE_FLUME_HOME/conf/cygnus.conf
#
# General configuration template explaining how to setup a sink of each of the available types (HDFS, CKAN, MySQL, PostgreSQL, Mongo, STH, Kafka, DynamoDB).

#=============================================
# The next tree fields set the sources, sinks and channels used by Cygnus. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use multiple types at the same time; the only requirement is to provide a channel for each
# one of them (this example shows how to configure 3 sink types at the same time). Even, you can define more than one
# sink of the same type and sharing the channel in order to improve the performance (this is like having
# multi-threading).
cygnusagent.sources = http-source
cygnusagent.sinks = hdfs-sink mysql-sink ckan-sink mongo-sink sth-sink kafka-sink dynamo-sink postgresql-sink
cygnusagent.channels = hdfs-channel mysql-channel ckan-channel mongo-channel sth-channel kafka-channel dynamo-channel postgresql-channel

#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.http-source.channels = hdfs-channel mysql-channel ckan-channel mongo-channel sth-channel kafka-channel dynamo-channel postgresql-channel
# source class, must not be changed
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# listening port the Flume source will use for receiving incoming notifications
cygnusagent.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.OrionRestHandler
# URL target
cygnusagent.sources.http-source.handler.notification_target = /notify
# Default service (service semantic depends on the persistence sink)
cygnusagent.sources.http-source.handler.default_service = def_serv
# Default service path (service path semantic depends on the persistence sink)
cygnusagent.sources.http-source.handler.default_service_path = def_servpath
# Number of channel re-injection retries before a Flume event is definitely discarded (-1 means infinite retries)
cygnusagent.sources.http-source.handler.events_ttl = 10
# Source interceptors, do not change
cygnusagent.sources.http-source.interceptors = ts gi
# TimestampInterceptor, do not change
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
# GroupingInterceptor, do not change
cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.GroupingInterceptor$Builder
# Grouping rules for the GroupingIntercetor, put the right absolute path to the file if necessary
# See the doc/design/interceptors document for more details
cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = /usr/cygnus/conf/grouping_rules.conf

# ============================================
# OrionHDFSSink configuration
# channel name from where to read notification events
cygnusagent.sinks.hdfs-sink.channel = hdfs-channel
# sink class, must not be changed
cygnusagent.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.OrionHDFSSink
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.hdfs-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# rest if the interaction with HDFS will be WebHDFS/HttpFS-based, binary if based on the Hadoop API
cygnusagent.sinks.hdfs-sink.backend_impl = rest
# Comma-separated list of FQDN/IP address regarding the HDFS Namenode endpoints
# If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
cygnusagent.sinks.hdfs-sink.hdfs_host = x1.y1.z1.w1,x2.y2.z2.w2
# port of the HDFS service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs
cygnusagent.sinks.hdfs-sink.hdfs_port = 14000
# username allowed to write in HDFS
cygnusagent.sinks.hdfs-sink.hdfs_username = hdfs_username
# password for the above username; this is only required for Hive authentication
cygnusagent.sinks.hdfs-sink.hdfs_password = xxxxxxxx
# OAuth2 token for HDFS authentication
cygnusagent.sinks.hdfs-sink.oauth2_token = xxxxxxxx
# true if the notified fiware-service (or the default one, if no one is notified) is used as the HDFS namespace, false otherwise
cygnusagent.sinks.hdfs-sink.service_as_namespace = false
# how the attributes are stored, available formats are json-row, json-column, csv-row and csv-column
cygnusagent.sinks.hdfs-sink.file_format = json-column
# character used for separating the values when using CSV file formats
cygnusagent.sinks.hdfs-sink.csv_separator = ,
# number of notifications to be included within a processing batch
cygnusagent.sinks.hdfs-sink.batch_size = 100
# timeout for batch accumulation
cygunsagent.sinks.hdfs-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.hdfs-sink.batch_ttl = 10
# Hive enabling
cygnusagent.sinks.hdfs-sink.hive = true
# Hive server version, 1 or 2 (ignored if hive is false)
cygnusagent.sinks.hdfs-sink.hive.server_version = 2
# Hive FQDN/IP address of the Hive server (ignored if hive is false)
cygnusagent.sinks.hdfs-sink.hive.host = x.y.z.w
# Hive port for Hive external table provisioning (ignored if hive is false)
cygnusagent.sinks.hdfs-sink.hive.port = 10000
# Hive database type, available types are default-db and namespace-db
cygnusagent.sinks.hdfs-sink.hive.db_type = default-db
# Kerberos-based authentication enabling
cygnusagent.sinks.hdfs-sink.krb5_auth = false
# Kerberos username (ignored if krb5_auth is false)
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
# Kerberos password (ignored if krb5_auth is false)
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
# Kerberos login file (ignored if krb5_auth is false)
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file = /usr/cygnus/conf/krb5_login.conf
# Kerberos configuration file (ignored if krb5_auth is false)
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

# ============================================
# OrionCKANSink configuration
# channel name from where to read notification events
cygnusagent.sinks.ckan-sink.channel = ckan-channel
# sink class, must not be changed
cygnusagent.sinks.ckan-sink.type = com.telefonica.iot.cygnus.sinks.OrionCKANSink
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.ckan-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# the CKAN API key to use
cygnusagent.sinks.ckan-sink.api_key = ckanapikey
# the FQDN/IP address for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_host = x.y.z.w
# the port for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_port = 80
# Orion URL used to compose the resource URL with the convenience operation URL to query it
cygnusagent.sinks.ckan-sink.orion_url = http://localhost:1026
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.ckan-sink.attr_persistence = row
# enable SSL for secure Http transportation; 'true' or 'false'
cygnusagent.sinks.ckan-sink.ssl = false
# number of notifications to be included within a processing batch
cygnusagent.sinks.ckan-sink.batch_size = 100
# timeout for batch accumulation
cygnusagent.sinks.ckan-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.ckan-sink.batch_ttl = 10

# ============================================
# OrionPostgreSQLSink configuration
# channel name from where to read notification events
cygnusagent.sinks.postgresql-sink.channel = postgresql-channel
# sink class, must not be changed
cygnusagent.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.OrionPostgreSQLSink
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.postgresql-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# the FQDN/IP address where the PostgreSQL server runs
cygnusagent.sinks.postgresql-sink.postgresql_host = x.y.z.w
# the port where the PostgreSQL server listens for incomming connections
cygnusagent.sinks.postgresql-sink.postgresql_port = 5432
# the name of the postgresql database
cygnusagent.sinks.postgresql-sink.postgresql_database = postgres
# a valid user in the PostgreSQL server
cygnusagent.sinks.postgresql-sink.postgresql_username = root
# password for the user above
cygnusagent.sinks.postgresql-sink.postgresql_password = xxxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.postgresql-sink.attr_persistence = column
# select the table type
cygnusagent.sinks.postgresql-sink.data_model = by-service-path
# number of notifications to be included within a processing batch
cygnusagent.sinks.postgresql-sink.batch_size = 100
# timeout for batch accumulation
cygnusagent.sinks.postgresql-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.postgresql-sink.batch_ttl = 10

# ============================================
# OrionMySQLSink configuration
# channel name from where to read notification events
cygnusagent.sinks.mysql-sink.channel = mysql-channel
# sink class, must not be changed
cygnusagent.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.OrionMySQLSink
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.mysql-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# the FQDN/IP address where the MySQL server runs
cygnusagent.sinks.mysql-sink.mysql_host = x.y.z.w
# the port where the MySQL server listens for incomming connections
cygnusagent.sinks.mysql-sink.mysql_port = 3306
# a valid user in the MySQL server
cygnusagent.sinks.mysql-sink.mysql_username = root
# password for the user above
cygnusagent.sinks.mysql-sink.mysql_password = xxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.mysql-sink.attr_persistence = column
# select the table type from table-by-destination and table-by-service-path
cygnusagent.sinks.mysql-sink.table_type = table-by-destination
# number of notifications to be included within a processing batch
cygnusagent.sinks.mysql-sink.batch_size = 100
# timeout for batch accumulation
cygunsagent.sinks.mysql-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.mysql-sink.batch_ttl = 10

# ============================================
# OrionMongoSink configuration
# sink class, must not be changed
cygnusagent.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.OrionMongoSink
# channel name from where to read notification events
cygnusagent.sinks.mongo-sink.channel = mongo-channel
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.mongo-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run
cygnusagent.sinks.mongo-sink.mongo_hosts = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# a valid user in the MongoDB server (or empty if authentication is not enabled in MongoDB)
cygnusagent.sinks.mongo-sink.mongo_username = mongo_username
# password for the user above (or empty if authentication is not enabled in MongoDB)
cygnusagent.sinks.mongo-sink.mongo_password = xxxxxxxx
# prefix for the MongoDB databases
cygnusagent.sinks.mongo-sink.db_prefix = sth_
# prefix for the MongoDB collections
cygnusagent.sinks.mongo-sink.collection_prefix = sth_
# true is collection names are based on a hash, false for human redable collections
cygnusagent.sinks.mongo-sink.should_hash = false
# specify if the sink will use a single collection for each service path, for each entity or for each attribute
cygnusagent.sinks.mongo-sink.data_model = collection-per-entity  
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.mongo-sink.attr_persistence = column
# number of notifications to be included within a processing batch
cygnusagent.sinks.mongo-sink.batch_size = 100
# timeout for batch accumulation
cygunsagent.sinks.mongo-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.mongo-sink.batch_ttl = 10
# true if white space-based attribute values must be ignored
cygnusagent.sinks.mongo-sink.ignore_white_spaces = true

# ============================================
# OrionSTHSink configuration
# sink class, must not be changed
cygnusagent.sinks.sth-sink.type = com.telefonica.iot.cygnus.sinks.OrionSTHSink
# channel name from where to read notification events
cygnusagent.sinks.sth-sink.channel = sth-channel
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.sth-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run
cygnusagent.sinks.sth-sink.mongo_hosts = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# a valid user in the MongoDB server (or empty if authentication is not enabled in MongoDB)
cygnusagent.sinks.sth-sink.mongo_username = mongo_username
# password for the user above (or empty if authentication is not enabled in MongoDB)
cygnusagent.sinks.sth-sink.mongo_password = xxxxxxxx
# prefix for the MongoDB databases
cygnusagent.sinks.sth-sink.db_prefix = sth_
# prefix for the MongoDB collections
cygnusagent.sinks.sth-sink.collection_prefix = sth_
# true is collection names are based on a hash, false for human redable collections
cygnusagent.sinks.sth-sink.should_hash = false
# number of notifications to be included within a processing batch
cygnusagent.sinks.sth-sink.batch_size = 100
# timeout for batch accumulation
cygnusagent.sinks.sth-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.sth-sink.batch_ttl = 10
# true if white space-based attribute values must be ignored
cygnusagent.sinks.sth-sink.ignore_white_spaces = true

#=============================================
# OrionKafkaSink configuration
# sink class, must not be changed
cygnusagent.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.OrionKafkaSink
# channel name from where to read notification events
cygnusagent.sinks.kafka-sink.channel = kafka-channel
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.sth-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# select the Kafka topic type between topic-by-service, topic-by-service-path and topic-by-destination
cygnusagent.sinks.kafka-sink.topic_type = topic-by-destination
# comma-separated list of Kafka brokers (a broker is defined as host:port)
cygnusagent.sinks.kafka-sink.broker_list = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# Zookeeper endpoint needed to create Kafka topics, in the form of host:port
cygnusagent.sinks.kafka-sink.zookeeper_endpoint = x.y.z.w:port
# number of notifications to be included within a processing batch
cygnusagent.sinks.kafka-sink.batch_size = 100
# timeout for batch accumulation
cygnusagent.sinks.kafka-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.kafka-sink.batch_ttl = 10

# ============================================
# OrionDynamoDBSink configuration
# sink class, must not be changed
cygnusagent.sinks.dynamo-sink.type = com.telefonica.iot.cygnus.sinks.OrionDynamoDBSink
# channel name from where to read notification events
cygnusagent.sinks.dynamo-sink.channel = dynamo-channel
# AWS Access Key Id
cygnusagent.sinks.dynamo-sink.access_key_id = xxxxxxxx
# AWS Secret Access Key
cygnusagent.sinks.dynamo-sink.secret_access_key = xxxxxxxxx
# AWS region where the tables will be created (link)
cygnusagent.sinks.dynamo-sink.region = eu-central-1
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.dynamo-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
cygnusagent.sinks.hdfs-sink.enable_lowercase = false
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.dynamo-sink.attr_persistence = column
# select the table type from table-by-destination and table-by-service-path
cygnusagent.sinks.dynamo-sink.table_type = table-by-destination
# number of notifications to be included within a processing batch
cygnusagent.sinks.dynamo-sink.batch_size = 100
# timeout for batch accumulation
cygnusagent.sinks.dynamo-sink.batch_timeout = 30
# number of retries upon persistence error
cygnusagent.sinks.dynamo-sink.batch_ttl = 10

#=============================================
# hdfs-channel configuration
# channel type (must not be changed)
cygnusagent.channels.hdfs-channel.type = memory
# capacity of the channel
cygnusagent.channels.hdfs-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.hdfs-channel.transactionCapacity = 100

#=============================================
# ckan-channel configuration
# channel type (must not be changed)
cygnusagent.channels.ckan-channel.type = memory
# capacity of the channel
cygnusagent.channels.ckan-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.ckan-channel.transactionCapacity = 100

#=============================================
# mysql-channel configuration
# channel type (must not be changed)
cygnusagent.channels.mysql-channel.type = memory
# capacity of the channel
cygnusagent.channels.mysql-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.mysql-channel.transactionCapacity = 100

#=============================================
# postgresql-channel configuration
# channel type (must not be changed)
cygnusagent.channels.postgresql-channel.type = memory
# capacity of the channel
cygnusagent.channels.postgresql-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.postgresql-channel.transactionCapacity = 100

#=============================================
# mongo-channel configuration
# channel type (must not be changed)
cygnusagent.channels.mongo-channel.type = memory
# capacity of the channel
cygnusagent.channels.mongo-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.mongo-channel.transactionCapacity = 100

#=============================================
# sth-channel configuration
# channel type (must not be changed)
cygnusagent.channels.sth-channel.type = memory
# capacity of the channel
cygnusagent.channels.sth-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.sth-channel.transactionCapacity = 100

#=============================================
# kafka-channel configuration
# channel type (must not be changed)
cygnusagent.channels.kafka-channel.type = memory
# capacity of the channel
cygnusagent.channels.kafka-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.mkafka-channel.transactionCapacity = 100

#=============================================
# dynamo-channel configuration
# channel type (must not be changed)
cygnusagent.channels.dynamo-channel.type = memory
# capacity of the channel
cygnusagent.channels.dynamo-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.dynamo-channel.transactionCapacity = 100
```

[Top](#top)
