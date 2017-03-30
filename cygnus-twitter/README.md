# <a name="top"></a>Cygnus-twitter

* [Welcome to Cygnus-twitter](#section1)
* [Basic operation](#section2)
    * [Hardware requirements](#section2.1)
    * [Configuration](#section2.3)
    * [Unit testing](#section2.5)
    * [Management API overview](#section2.7)
* [Features summary](#section5)
* [Licensing](#section6)
* [Reporting issues and contact information](#section7)

## <a name="section1"></a>Welcome to Cygnus-twitter
This project is part of [FIWARE](http://fiware.org), being part of the [Cosmos](http://catalogue.fiware.org/enablers/bigdata-analysis-cosmos) Ecosystem.

Cygnus-twitter is a connector in charge of persisting tweets (https://dev.twitter.com/overview/api/tweets) in certain configured third-party storages, creating a historical view of such data.

Internally, Cygnus-twitter is based on [Apache Flume](http://flume.apache.org/) and [Twitter4j](http://twitter4j.org/en/index.html). In fact, Cygnus-twitter is a Flume agent, which is basically composed of a source in charge of receiving the data from Twitter (https://twitter.com/), a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Twitter data in:

* [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.

[Top](#top)

## <a name="section2"></a>Basic operation
### <a name="section2.1"></a>Hardware requirements
* RAM: 1 GB, specially if abusing of the batching mechanism.
* HDD: A few GB may be enough unless the channel types are configured as `FileChannel` type.

[Top](#top)

### <a name="section2.3"></a>Configuration
Cygnus-twitter is a tool with a high degree of configuration required for properly running it.

So, the starting point is choosing the internal architecture of the Cygnus agent. Let's assume the simplest one:

```
+----------------+    +----------------+    +----------------+
| Twitter source |----| memory channel |----|  Twitter Sink  |
+----------------+    +----------------+    +----------------+
```

Attending to the above architecture, the content of `/usr/cygnus/conf/cygnus_1.conf` will specify the following parameters:

* The source:
`cygnus-twitter.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource`

* The keyworks (hashtags) that are used in the twitter query to filter tweets with an specific keyword(s):     
`cygnus-twitter.sources.twitter-source.keywords = keyword1, keyword2, keyword3`

* The coordinates to specify the spatial area where the source will collect geo-located tweets. The coordinates will be used in the twitter query:
```
cygnus-twitter.sources.twitter-source.south-west_latitude = 39.4247692
cygnus-twitter.sources.twitter-source.south-west_longitude = -0.4315448
cygnus-twitter.sources.twitter-source.north-east_latitude = 39.5038788
cygnus-twitter.sources.twitter-source.north-east_longitude = -0.3124204
```

    These coordinates are used to define a rectangle filter where tweets have been geo-located. Only tweets inside this rectangle are stored.
    ```
                     -------------- north-east
                    |                  |
                    |                  |
                    |                  |
               south-west ------------   

    ```

 * The credentials used to connect with Twitter API:
```
cygnus-twitter.sources.twitter-source.consumerKey = xxxxxxx
cygnus-twitter.sources.twitter-source.consumerSecret = xxxxxxx
cygnus-twitter.sources.twitter-source.accessToken = xxxxxxx
cygnus-twitter.sources.twitter-source.accessTokenSecret = xxxxxxx
```

* Parameters associated to the sinks. Currently, for the `cygnus-twitter` agent, the only sink defined is the `HDFS sink`. The `HDFS sink` parameters that appear in the configuration file and are directly related to `twitter-agent` are:
    * `hdfs_folder`: to declare the folder where the tweets file will be created.
    * `hdfs_file`: to declare the file where tweets will be stored inside the `hdfs_folder`

```
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
cygnus-twitter.sources.twitter-source.south-west_latitude = 39.4247692
cygnus-twitter.sources.twitter-source.south-west_longitude = -0.4315448
cygnus-twitter.sources.twitter-source.north-east_latitude = 39.5038788
cygnus-twitter.sources.twitter-source.north-east_longitude = -0.3124204
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
#cygnus-twitter.sinks.hdfs-sink.enable_encoding = false
# true if the grouping feature is enabled for this sink, false otherwise
#cygnus-twitter.sinks.hdfs-sink.enable_grouping = false
# true if lower case is wanted to forced in all the element names, false otherwise
#cygnus-twitter.sinks.hdfs-sink.enable_lowercase = false
# rest if the interaction with HDFS will be WebHDFS/HttpFS-based, binary if based on the Hadoop API
#cygnus-twitter.sinks.hdfs-sink.backend_impl = rest
# Comma-separated list of FQDN/IP address regarding the HDFS Namenode endpoints
# If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
#cygnus-twitter.sinks.hdfs-sink.hdfs_host = x1.y1.z1.w1,x2.y2.z2.w2
# port of the HDFS service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs
#cygnus-twitter.sinks.hdfs-sink.hdfs_port = 14000
# username allowed to write in HDFS
cygnus-twitter.sinks.hdfs-sink.hdfs_username = hdfs_username
# password for the above username; this is only required for Hive authentication
cygnus-twitter.sinks.hdfs-sink.hdfs_password = xxxxxxxx
# OAuth2 token for HDFS authentication
cygnus-twitter.sinks.hdfs-sink.oauth2_token = xxxxxxxx
# true if the notified fiware-service (or the default one, if no one is notified) is used as the HDFS namespace, false otherwise
#cygnus-twitter.sinks.hdfs-sink.service_as_namespace = false
# how the attributes are stored, available formats are json-row, json-column, csv-row and csv-column
#cygnus-twitter.sinks.hdfs-sink.file_format = json-column
# character used for separating the values when using CSV file formats
#cygnus-twitter.sinks.hdfs-sink.csv_separator = ,
# number of notifications to be included within a processing batch
#cygnus-twitter.sinks.hdfs-sink.batch_size = 100
# timeout for batch accumulation
#cygunsagent.sinks.hdfs-sink.batch_timeout = 30
# number of retries upon persistence error
#cygnus-twitter.sinks.hdfs-sink.batch_ttl = 10
# Hive enabling
#cygnus-twitter.sinks.hdfs-sink.hive = false
# Hive server version, 1 or 2 (ignored if hive is false)
#cygnus-twitter.sinks.hdfs-sink.hive.server_version = 2
# Hive FQDN/IP address of the Hive server (ignored if hive is false)
#cygnus-twitter.sinks.hdfs-sink.hive.host = x.y.z.w
# Hive port for Hive external table provisioning (ignored if hive is false)
#cygnus-twitter.sinks.hdfs-sink.hive.port = 10000
# Hive database type, available types are default-db and namespace-db
#cygnus-twitter.sinks.hdfs-sink.hive.db_type = default-db
# Kerberos-based authentication enabling
#cygnus-twitter.sinks.hdfs-sink.krb5_auth = false
# Kerberos username (ignored if krb5_auth is false)
cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
# Kerberos password (ignored if krb5_auth is false)
cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
# Kerberos login file (ignored if krb5_auth is false)
3cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file = /usr/cygnus/conf/krb5_login.conf
# Kerberos configuration file (ignored if krb5_auth is false)
#cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf
#=============================================
# hdfs-channel configuration
# channel type (must not be changed)
cygnus-twitter.channels.hdfs-channel.type = memory
# capacity of the channel
cygnus-twitter.channels.hdfs-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnus-twitter.channels.hdfs-channel.transactionCapacity = 100
```

Check the [User and Programmer Guide](../../doc/cygnus-twitter/user_and_programmer_guide/introduction.md) for configurations involving real data storages such as HDFS.

[Top](#top)

### <a name="section2.5"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ mvn test

[Top](#top)

### <a name="section2.7"></a>Management API overview
Run the following `curl` in order to get the version (assuming Cygnus runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/version"
{
    "success": "true",
    "version": "0.12.0_SNAPSHOT.52399574ea8503aa8038ad14850380d77529b550"
}
```

Run the following `curl` in order to get certain Flume components statistics (assuming Cygnus runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/stats" | python -m json.tool
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   489  100   489    0     0  81500      0 --:--:-- --:--:-- --:--:-- 97800
{
    "stats": {
        "channels": [
            {
                "name": "hdfs-channel",
                "num_events": 0,
                "num_puts_failed": 0,
                "num_puts_ok": 11858,
                "num_takes_failed": 1,
                "num_takes_ok": 11858,
                "setup_time": "2016-02-05T10:34:25.80Z",
                "status": "START"
            }
        ],
        "sinks": [
            {
                "name": "hdfs-sink",
                "num_persisted_events": 11800,
                "num_processed_events": 11858,
                "setup_time": "2016-02-05T10:34:24.978Z",
                "status": "START"
            }
        ],
        "sources": [
            {
                "name": "twitter-source",
                "num_processed_events": 11858,
                "num_received_events": 11858,
                "setup_time": "2016-02-05T10:34:24.921Z",
                "status": "START"
            }
        ]
    },
    "success": "true"
}
```

Many other operations, like getting/putting/updating/deleting the grouping rules can be found in Management Interface [documentation](../../doc/cygnus-common/installation_and_administration_guide/management_interface.md).

[Top](#top)

## <a name="section5"></a>Features summary
<table>
  <tr><th>Component</th><th>Feature</th><th>From version</th></tr>
  <tr><td rowspan="7">TwitterHDFSSink</td><td>First implementation</td><td>1.1.0</td></tr>
  <tr><td>Multiple HDFS endpoint setup</td><td>1.1.0</td></tr>
  <tr><td>Kerberos support</td><td>1.1.0</td></tr>
  <tr><td>OAuth2 support</td><td>1.1.0</td></tr>
  <tr><td>enable/disable Hive</td><td>1.1.0</td></tr>
  <tr><td>HDFSBackendImplBinary</td><td>1.1.0</td></tr>
  <tr><td>Batching mechanism</td><td>1.1.0</td></tr>
  <tr><td rowspan="2">All sinks</td><td>enable/disable forced lower case</td><td>1.1.0</td></tr>
  <tr><td>Per batch TTL</td><td>1.1.0</td></tr>
</table>

[Top](#top)

## <a name="section6"></a>Licensing
Cygnus is licensed under Affero General Public License (GPL) version 3. You can find a [copy of this license in the repository](../../LICENSE).

[Top](#top)

## <a name="section7"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelerator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.

[Top](#top)
