# <a name="top"></a>Introduction
This document describes how to use cygnus-twitter once it has been [installed](../installation_and_administration_guide/introduction.md) and how it works.

cygnus-twitter is a Cygnus agent (i.e., a flume agent) that has as source tweets and it can have different sinks. Right now, the HDFS sink is already implemented.

## <a name="top"></a>Configuration file
From the point of view of the user, the main differences with respect to the Cygnus-ngsi agent are in the configuration file `agent_<id>.conf`. In this file, the first difference is the source that is a twitter source:

`cygnus-twitter.sources = twitter-source`

Cygnus-twitter agent configuration file needs to specify parameters related to the source` of the agent in order to perform the Twitter query. The most relevant parameters are: the source, the keywords, the coordinates, and the credentials to connect with Twitter.

The source:
`cygnus-twitter.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource`

The keyworks (hashtags) that are used in the twitter query to filter tweets with an specific keyword(s):

`cygnus-twitter.sources.twitter-source.keywords = keyword1, keyword2, keyword3`

The coordinates to specify the spatial area where the source will collect geo-located tweets. The coordinates will be used in the twitter query:

```
cygnus-twitter.sources.twitter-source.south_west_latitude = 39.4247692
cygnus-twitter.sources.twitter-source.south_west_longitude = -0.4315448
cygnus-twitter.sources.twitter-source.north_east_latitude = 39.5038788
cygnus-twitter.sources.twitter-source.north_east_longitude = -0.3124204
```

These coordinates are used to define a rectangle filter where tweets have been geo-located. Only tweets inside this rectangle are stored.
```
             -------------- north-east
            |                  |
            |                  |
            |                  |
       south-west ------------   

```

The credentials used to connect with Twitter API:

```
cygnus-twitter.sources.twitter-source.consumerKey = xxxxxxx
cygnus-twitter.sources.twitter-source.consumerSecret = xxxxxxx
cygnus-twitter.sources.twitter-source.accessToken = xxxxxxx
cygnus-twitter.sources.twitter-source.accessTokenSecret = xxxxxxx
```

Once the parameters related to the source are defined, the file continues defining properties associated to the sinks. Currently, for the `cygnus-twitter` agent, the only sink defined is the `HDFS sink`. The `HDFS sink` parameters that appear in the configuration file and are directly related to `cygnus-twitter` agent are:

`hdfs_folder`: to declare the folder where the tweets file will be created.

`hdfs_file`: to declare the file where tweets will be stored inside the `hdfs_folder`



## <a name="top"></a>Configuration file example
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
cygnus-twitter.sources = twitter-source
cygnus-twitter.sinks = hdfs-sink
cygnus-twitter.channels = hdfs-channel
#=============================================
# source configuration
# channel name where to write the notification events
cygnus-twitter.sources.twitter-source.channels = hdfs-channel
# source class, must not be changed
cygnus-twitter.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource
# keywords
# cygnus-twitter.sources.twitter-source.keywords = keyword1, keyword2, keyword3
# Coordinates for filter query
cygnus-twitter.sources.twitter-source.south_west_latitude = 39.4247692
cygnus-twitter.sources.twitter-source.south_west_longitude = -0.4315448
cygnus-twitter.sources.twitter-source.north_east_latitude = 39.5038788
cygnus-twitter.sources.twitter-source.north_east_longitude = -0.3124204
cygnus-twitter.sources.twitter-source.consumerKey = xxxxxxxx
cygnus-twitter.sources.twitter-source.consumerSecret = xxxxxxxx
cygnus-twitter.sources.twitter-source.accessToken = xxxxxxxx
cygnus-twitter.sources.twitter-source.accessTokenSecret = xxxxxxxx

# ============================================
# NGSIHDFSSink configuration
# channel name from where to read notification events
cygnus-twitter.sinks.hdfs-sink.channel = hdfs-channel
# sink class, must not be changed
cygnus-twitter.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.NGSIHDFSSink
# true applies the new encoding, false applies the old encoding
#cygnus\-twitter\.sinks.hdfs-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus\-twitter\.sinks.hdfs-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus\-twitter\.sinks.hdfs-sink.enable_lowercase = false
# rest if the interaction with HDFS will be WebHDFS/HttpFS-based, binary if based on the Hadoop API
#cygnus\-twitter\.sinks.hdfs-sink.backend.impl = rest
# maximum number of Http connections to HDFS backend
#cygnus\-twitter\.sinks.hdfs-sink.backend.max_conns = 500
# maximum number of Http connections per route to HDFS backend
#cygnus\-twitter\.sinks.hdfs-sink.backend.max_conns_per_route = 100
# Comma-separated list of FQDN/IP address regarding the HDFS Namenode endpoints
# If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
#cygnus\-twitter\.sinks.hdfs-sink.hdfs_host = x1.y1.z1.w1,x2.y2.z2.w2
# port of the HDFS service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs
#cygnus\-twitter\.sinks.hdfs-sink.hdfs_port = 14000
# username allowed to write in HDFS
cygnus-twitter.sinks.hdfs-sink.hdfs_username = hdfs_username
# password for the above username; this is only required for Hive authentication
cygnus-twitter.sinks.hdfs-sink.hdfs_password = xxxxxxxx
# OAuth2 token for HDFS authentication
cygnus-twitter.sinks.hdfs-sink.oauth2_token = xxxxxxxx
# true if the notified fiware-service (or the default one, if no one is notified) is used as the HDFS namespace, false otherwise
#cygnus\-twitter\.sinks.hdfs-sink.service_as_namespace = false
# how the attributes are stored, available formats are json-row, json-column, csv-row and csv-column
#cygnus\-twitter\.sinks.hdfs-sink.file_format = json-column
# character used for separating the values when using CSV file formats
#cygnus\-twitter\.sinks.hdfs-sink.csv_separator = ,
# number of notifications to be included within a processing batch
#cygnus\-twitter\.sinks.hdfs-sink.batch_size = 100
# timeout for batch accumulation
# cygunsagent.sinks.hdfs-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus\-twitter\.sinks.hdfs-sink.batch_ttl = 10
# Hive enabling
#cygnus\-twitter\.sinks.hdfs-sink.hive = false
# Hive server version, 1 or 2 (ignored if hive is false)
#cygnus\-twitter\.sinks.hdfs-sink.hive.server_version = 2
# Hive FQDN/IP address of the Hive server (ignored if hive is false)
#cygnus\-twitter\.sinks.hdfs-sink.hive.host = x.y.z.w
# Hive port for Hive external table provisioning (ignored if hive is false)
#cygnus\-twitter\.sinks.hdfs-sink.hive.port = 10000
# Hive database type, available types are default-db and namespace-db
#cygnus\-twitter\.sinks.hdfs-sink.hive.db_type = default-db
# Kerberos-based authentication enabling
#cygnus\-twitter\.sinks.hdfs-sink.krb5_auth = false
# Kerberos username (ignored if krb5_auth is false)
cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
# Kerberos password (ignored if krb5_auth is false)
cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
# Kerberos login file (ignored if krb5_auth is false)
#cygnus\-twitter\.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file = /usr/cygnus/conf/krb5_login.conf
# Kerberos configuration file (ignored if krb5_auth is false)
#cygnus\-twitter\.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

#=============================================
# hdfs-channel configuration
# channel type (must not be changed)
cygnus-twitter.channels.hdfs-channel.type = memory
# capacity of the channel
cygnus-twitter.channels.hdfs-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-twitter.channels.hdfs-channel.transactionCapacity = 100
```

[Top](#top)
