#<a name="top"></a>Cygnus configuration
Content:

* [Introduction](#section1)
* [`cygnus_instance_<id>.conf`](#section2)
* [`agent_<id>.conf`](#section3)
* [`flume-env.sh`](#section4)
* [`grouping_rules.conf`](#section5)
* [`log4j.properties`](#section6)
* [Configuration examples](#section7)
    * [Single source, single storage (basic configuration)](#section7.1)
    * [Single source, multiple storages](#section7.2)
    * [Single source, single storage, parallel sinking](#section7.3)
    * [Single source, multiple storages, parallel sinking](#section7.4)
    * [Multiple sources](#section7.5)
    * [Using interceptors](#section7.6)

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

[Top](#top)

##<a name="section3"></a>`agent_<id>.conf`
The file `agent_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/agent.conf.template`.

```Java
#=============================================
# To be put in APACHE_FLUME_HOME/conf/cygnus.conf
#
# General configuration template explaining how to setup a sink of each of the available types (HDFS, CKAN, MySQL).

#=============================================
# The next tree fields set the sources, sinks and channels used by Cygnus. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use multiple types at the same time; the only requirement is to provide a channel for each
# one of them (this example shows how to configure 3 sink types at the same time). Even, you can define more than one
# sink of the same type and sharing the channel in order to improve the performance (this is like having
# multi-threading).
cygnusagent.sources = http-source
cygnusagent.sinks = hdfs-sink mysql-sink ckan-sink mongo-sink sth-sink kafka-sink
cygnusagent.channels = hdfs-channel mysql-channel ckan-channel mongo-channel sth-channel kafka-channel

#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.http-source.channels = hdfs-channel mysql-channel ckan-channel mongo-channel sth-channel kafka-channel
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
# how the attributes are stored, available formats are json-row, json-column, csv-row and csv-column
cygnusagent.sinks.hdfs-sink.file_format = json-column
# number of notifications to be included within a processing batch
cygnusagent.sinks.hdfs-sink.batch_size = 100
# timeout for batch accumulation
cygunsagent.sinks.hdfs-sink.batch_timeout = 30
# Hive enabling
cygnusagent.sinks.hdfs-sink.hive = true
# Hive server version, 1 or 2 (ignored if hive is false)
cygnusagent.sinks.hdfs-sink.hive.server_version = 2
# Hive FQDN/IP address of the Hive server (ignored if hive is false)
cygnusagent.sinks.hdfs-sink.hive.host = x.y.z.w
# Hive port for Hive external table provisioning (ignored if hive is false)
cygnusagent.sinks.hdfs-sink.hive.port = 10000
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

# ============================================
# OrionMySQLSink configuration
# channel name from where to read notification events
cygnusagent.sinks.mysql-sink.channel = mysql-channel
# sink class, must not be changed
cygnusagent.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.OrionMySQLSink
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.mysql-sink.enable_grouping = false
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

# ============================================
# OrionMongoSink configuration
# sink class, must not be changed
cygnusagent.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.OrionMongoSink
# channel name from where to read notification events
cygnusagent.sinks.mongo-sink.channel = mongo-channel
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.mongo-sink.enable_grouping = false
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

# ============================================
# OrionSTHSink configuration
# sink class, must not be changed
cygnusagent.sinks.sth-sink.type = com.telefonica.iot.cygnus.sinks.OrionSTHSink
# channel name from where to read notification events
cygnusagent.sinks.sth-sink.channel = sth-channel
# true if the grouping feature is enabled for this sink, false otherwise
cygnusagent.sinks.sth-sink.enable_grouping = false
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

#=============================================
# OrionKafkaSink configuration
# sink class, must not be changed
cygnusagent.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.OrionKafkaSink
# channel name from where to read notification events
cygnusagent.sinks.kafka-sink.channel = kafka-channel
# select the Kafka topic type between topic-by-service, topic-by-service-path and topic-by-destination
cygnusagent.sinks.kafka-sink.topic_type = topic-by-destination
# comma-separated list of Kafka brokers (a broker is defined as host:port)
cygnusagent.sinks.kafka-sink.broker_list = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# Zookeeper endpoint needed to create Kafka topics, in the form of host:port
cygnusagent.sinks.kafka-sink.zookeeper_endpoint = x.y.z.w:port

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
```

[Top](#top)

##<a name="section4"></a>`flume-env.sh`
The file `flume-env.sh` can be instantiated from a template given in the Cygnus repository, `conf/flume-env.sh.template`. 

```
#=============================================
# To be put in APACHE_FLUME_HOME/conf/flume-env.sh
#=============================================

#JAVA_HOME=/usr/lib/jvm/java-6-sun

# Give Flume more memory and pre-allocate, enable remote monitoring via JMX
#JAVA_OPTS="-Xms100m -Xmx200m -Dcom.sun.management.jmxremote"

# Note that the Flume conf directory is always included in the classpath.
#FLUME_CLASSPATH="/path/to/the/flume/classpath"
```

`flume-env.sh` file has been inherited from Apache Flume, and it is used in order to configure certain Flume parameters such as an alternative classpath, some Java options etc.

[Top](#top)

##<a name="section5"></a>`grouping_rules.conf`
The file `grouping_rules.conf` can be instantiated from a template given in the Cygnus repository, `conf/grouping_rules.conf.template`. 


```
{
    "grouping_rules": [
        {
            "id": 1,
            "fields": [
                ...
            ],
            "regex": "...",
            "destination": "...",
            "fiware_service_path": "..."
        },
        ...
    ]
} 
```

Being:

* <b>id</b>: A unique unsigned integer-based identifier. Not really used in the current implementation, but could be useful in the future.
* <b>fields</b>: These are the fields that will be concatenated for regular expression matching. The available dictionary of fields for concatenation is "entityId", "entityType" and "servicePath". The order of these fields is important since the concatenation is made from left to right.
* <b>regex</b>: Java-like regular expression to be applied on the concatenated fields. Special characters like '\' must be escaped ('\' is escaped as "\\\\").
* <b>destination</b>: Name of the HDFS file or CKAN resource where the data will be effectively persisted. In the case of MySQL, Mongo and STH this sufixes the table/collection name. Please, have a look to [doc/design/naming_conventions.md](doc/design/naming_conventions.md) for more details.
* <b>fiware\_service\_path</b>: New `fiware-servicePath` replacing the notified one. The sinks will translate this into the name of the HDFS folder or CKAN package where the above destination entity will be placed. In the case of MySQL, Mongo and STH this prefixes the table/collection name. Please, have a look to [doc/design/naming_conventions.md](doc/design/naming_conventions.md) for more details.

`grouping_rules.conf` must be put in `APACHE_FLUME_HOME/conf/`  .

[Top](#top)

##<a name="section6"></a>`log4j.properties`
The file `log4j.properties` can be instantiated from a template given in the Cygnus repository, `conf/log4j.properties.template`.

Its content should not be edited unless some of the default values for log path, file name, logging level or appender are wanted to be changed.

```
# Define some default values.
# These can be overridden by system properties, e.g. the following logs in the standard output, which is very useful
# for testing purposes:
# -Dflume.root.logger=DEBUG,console
flume.root.logger=INFO,LOGFILE
#flume.root.logger=DEBUG,console
flume.log.dir=/var/log/cygnus/
flume.log.file=flume.log

# Logging levels for certain components.
log4j.logger.org.apache.flume.lifecycle = INFO
log4j.logger.org.jboss = WARN
log4j.logger.org.mortbay = INFO
log4j.logger.org.apache.avro.ipc.NettyTransceiver = WARN
log4j.logger.org.apache.hadoop = INFO

# Define the root logger to the system property "flume.root.logger".
log4j.rootLogger=${flume.root.logger}

# Stock log4j rolling file appender.
# Default log rotation configuration.
log4j.appender.LOGFILE=org.apache.log4j.RollingFileAppender
log4j.appender.LOGFILE.MaxFileSize=100MB
log4j.appender.LOGFILE.MaxBackupIndex=10
log4j.appender.LOGFILE.File=${flume.log.dir}/${flume.log.file}
log4j.appender.LOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.LOGFILE.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

# Warning: If you enable the following appender it will fill up your disk if you don't have a cleanup job!
# cleanup job example: find /var/log/cygnus -type f -mtime +30 -exec rm -f {} \;
# This uses the updated rolling file appender from log4j-extras that supports a reliable time-based rolling policy.
# See http://logging.apache.org/log4j/companions/extras/apidocs/org/apache/log4j/rolling/TimeBasedRollingPolicy.html
# Add "DAILY" to flume.root.logger above if you want to use this.
log4j.appender.DAILY=org.apache.log4j.rolling.RollingFileAppender
log4j.appender.DAILY.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
log4j.appender.DAILY.rollingPolicy.ActiveFileName=${flume.log.dir}/${flume.log.file}
log4j.appender.DAILY.rollingPolicy.FileNamePattern=${flume.log.dir}/${flume.log.file}.%d{yyyy-MM-dd}
log4j.appender.DAILY.layout=org.apache.log4j.PatternLayout
log4j.appender.DAILY.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

# Console appender, i.e. printing logs in the standar output.
# Add "console" to flume.root.logger above if you want to use this.
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.err
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n
```

[Top](#top)

##<a name="section7"></a>Configuration examples
###<a name="section7.1"></a>Single source, single storage (basic configuration)
To be done

[Top](#top)

###<a name="section7.2"></a>Single source, multiple storages
To be done

[Top](#top)

###<a name="section7.3"></a>Single source, single storage, parallel sinking
To be done

[Top](#top)

###<a name="section7.4"></a>Single source, multiple storages, parallel sinking
To be done

[Top](#top)

###<a name="section7.5"></a>Multiple sources 
To be done

[Top](#top)

###<a name="section7.6"></a>Using interceptors
Interceptors are components of the Flume agent architecture. Typically, such an agent is based on a source dealing with the input, a sink dealing with the output and a channel communicating them. The source processes the input, producing Flume events (an object based on a set of headers and a byte-based body) that are put in the channel; then the sink consumes the events by getting them from the channel. This basic architecture may be enriched by the addition of Interceptors, a chained sequence of Flume events preprocessors that <i>intercept</i> the events before they are put into the channel and performing one of these operations:

* Drop the event.
* Modify an existent header of the Flume event.
* Add a new header to the Flume event.

Interceptors should never modify the body part. Once an event is preprocessed, it is put in the channel as usual.

As can be seen, this mechanism allows for very useful ways of enriching the basic Flume events a certain Flume source may generate. Let's see how Cygnus makes use of this concept in order to add certain information to the Flume events created from the Orion notifications.

[Top](#top)

