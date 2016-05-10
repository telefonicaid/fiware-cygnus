#<a name="top"></a>Introduction
This document describes how to use Cygnus-twitter once it has been [installed](../installation_and_administration_guide/introduction.md) and how it works.

Cygnus-twitter is a Cygnus agent (i.e., a flume agent) that has as source tweets and it can have different sinks. Right now, the HDFS sink is already implemented.

##<a name="top"></a>Configuration file
From the point of view of the user, the main differences with respect to the Cygnus-ngsi agent are in the configuration file `agent_<id>.conf`. In this file, the first difference is the source that is a twitter source:

`cygnusagent.sources = twitter-source`


Cygnus-twitter agent configuration file needs to specify parameters related to the source` of the agent in order to perform the Twitter query. The most relevant parameters are: the source, the keywords, the coordinates, and the credentials to connect with Twitter.

The source:
`cygnusagent.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource`

The keyworks (hashtags) that are used in the twitter query to filter tweets with an specific keyword(s):

`cygnusagent.sources.twitter-source.keywords = keyword1, keyword2, keyword3`

The coordinates to specify the spatial area where the source will collect geo-located tweets. The coordinates will be used in the twitter query:

```
cygnusagent.sources.twitter-source.top_left_latitude = 40.748433
cygnusagent.sources.twitter-source.top_left_longitude = -73.985656
cygnusagent.sources.twitter-source.bottom_right_latitude = 40.758611
cygnusagent.sources.twitter-source.bottom_right_longitude = -73.979167
```

These coordinates are used to define a rectangle filter where tweets have been geo-located. Only tweets inside this rectangle are stored.
```
        top-left --------------
            |                  |
            |                  |
            |                  |
             ------------ bottom-right   

```

The credentials used to connect with Twitter API:

```
cygnus-twitter.sources.twitter-source.consumerKey = xxxxxxx
cygnus-twitter.sources.twitter-source.consumerSecret = xxxxxxx
cygnus-twitter.sources.twitter-source.accessToken = xxxxxxx
cygnus-twitter.sources.twitter-source.accessTokenSecret = xxxxxxx
```

Once the parameters related to the source are defined, the file continues defining properties associated to the sinks. Currently, for the twitter-agent, the only sink defined is the `HDFS sink`. The `HDFS sink` parameters that appear in the configuration file and are directly related to `twitter-agent` are:

`hdfs_folder`: to declare the folder where the tweets file will be created.

`hdfs_file`: to declare the file where tweets will be stored inside the `hdfs_folder`



##<a name="top"></a>Configuration file example
```Java
#=============================================
# To be put in APACHE_FLUME_HOME/conf/cygnus.conf
#
# General configuration template explaining how to setup a sink of each of HDFS.

#=============================================
# The next tree fields set the sources, sinks and channels used by Cygnus-twitter. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use multiple types at the same time; the only requirement is to provide a channel for each
# one of them (this example shows how to configure 1 sink type). Even, you can define more than one
# sink of the same type and sharing the channel in order to improve the performance (this is like having
# multi-threading).
cygnusagent.sources = twitter-source
cygnusagent.sinks = hdfs-sink
cygnusagent.channels = hdfs-channel
#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.twitter-source.channels = hdfs-channel
# source class, must not be changed
cygnusagent.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource
# keywords
# cygnusagent.sources.twitter-source.keywords = keyword1, keyword2, keyword3
# Coordinates for filter query
cygnusagent.sources.twitter-source.top_left_latitude = 40.748433
cygnusagent.sources.twitter-source.top_left_longitude = -73.985656
cygnusagent.sources.twitter-source.bottom_right_latitude = 40.758611
cygnusagent.sources.twitter-source.bottom_right_longitude = -73.979167
cygnus-twitter.sources.twitter-source.consumerKey =
cygnus-twitter.sources.twitter-source.consumerSecret =
cygnus-twitter.sources.twitter-source.accessToken =
cygnus-twitter.sources.twitter-source.accessTokenSecret =

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

#=============================================
# hdfs-channel configuration
# channel type (must not be changed)
cygnusagent.channels.hdfs-channel.type = memory
# capacity of the channel
cygnusagent.channels.hdfs-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.hdfs-channel.transactionCapacity = 100
```

[Top](#top)
