# Cygnus connector

This connector is a (conceptual) derivative work of [ngsi2cosmos](https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos), and implements a Flume-based connector for context data coming from Orion Context Broker and aimed to be stored in a specific persistent storage, such as HDFS, CKAN or MySQL.

## Development

All the details about Flume can be found at [flume.apache.org](http://flume.apache.org/), but, as a reminder, some concepts will be explained here:

* A Flume source is an agent gathering event data from the real source (Twitter stream, a notification system, etc.), either by polling the source or listening for incoming pushes of data. Gathered data is sent to a Flume channel.
* A Flume channel is a passive store (implemented by means of a file, memory, etc.) that holds the event until it is consumed by the Flume sink.
* A Flume sink connects with the final destination of the data (a local file, HDFS, a database, etc.), taking events from the channel and consuming them (processing and/or persisting it).

There exists a wide collection of already developed sources, channels and sinks. The Flume-based connector, also called Cygnus, development extends that collection by adding:

* **OrionRestHandler**. A custom HTTP source handler for the default HTTP source. The existing HTTP source behaviour can be governed depending on the request handler associated to it in the configuration. In this case, the custom handler takes care of the method, the target and the headers (specially the Content-Type one) within the request, cheking everything is according to the expected [request format](https://forge.fi-ware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). This allows for a certain degree of control on the incoming data. The header inspection step allows for a content type identification as well by sending, together with the data, the Content-Type header.
* **OrionHDFSSink**. A custom sink that persists Orion content data in a HDFS deployment. There already exists a native Flume HDFS sink persisting each event in a new file, but this is not suitable for Cygnus. Within Cygnus, the data coming from Orion must be persisted in the Cosmos HDFS in the form of files (a file per entity) containing Json-like lines about the values such entity's attributes have had along time. Several HDFS backends can be used for the data persistence (WebHDFS, HttpFS, Infinity), all of them based on the native WebHDFS REST API from Hadoop.
* **OrionCKANSink**. A custom sink that persists Orion context data in CKAN server instances (see http://docs.ckan.org/en/latest/).
* **OrionMySQLSink**. A custom sink for persisting Orion context data in a MySQL server. Each user owns a database, and each entity is mapped to a table within that database. Tables contain rows about the values such entity's attributes have had along time.

All these new components (OrionRestHandler, OrionHDFSSink, etc) are combined with other native ones included in Flume itself (e.g. HttpSource), with the purpose of implementing the following data flow:

1.  On behalf of Cygnus, subscribe to Orion for certain context information.
2.  Receive from Orion notifications about new update context data; this notification will be handled by the native HttpSource together with the custom OrionRestHandler.
3.  Translate the notification into the Flume event format, and put them into the different sink channels (native memory ones).
4.  For each enabled custom sink (OrionHDFSSink, OrionCKANSink, OrionMySQLSink), get the notifications from the sink channel and persist the data in the appropriate format.

## Functionality explained (Json notification example)

Let's consider the following notification in Json format coming from an Orion Context Broker instance:

    POST http://localhost:1028/notify
    Content-Length: 492
    User-Agent: orion/0.9.0
    Host: localhost:1028
    Accept: application/xml, application/json
    Content-Type: application/json
    Fiware-Service: Org42
    
    {
      "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
      "originator" : "localhost",
      "contextResponses" : [
        {
          "contextElement" : {
            "attributes" : [
              {
                "name" : "temperature",
                "type" : "centigrade",
                "value" : "26.5",
                "metadatas": [
                  {
                     "name": "ID",
                     "type": "string",
                     "value": "ground"
                  }
                ]
              }
            ],
            "type" : "Room",
            "isPattern" : "false",
            "id" : "Room1"
          },
          "statusCode" : {
            "code" : "200",
            "reasonPhrase" : "OK"
          }
        }
      ]
    }

Such a notification is sent by Orion to the default Flume HTTP source, which relies on the developed OrionRestHandler for checking its validity (it is a POST request, the target is "notify" and the headers are OK), detecting the content type (it is in Json format), extracting the data (the Json part) and finally creating a Flume event to be put in the channel:

    event={body={the_json_part...},headers={{"content-type","application/json"}, {"fiware-service","Org42"}, {"recvTimeTs","1402409899391"}}

Let's have a look on the Flume event headers:

* The `content-type` header is a replica of the Http one in order the different sinks know how to parse the event body, in this case it is Json.
* Note that Orion can include a `Fiware-Service` HTTP header specifying the tenant/organization associated to the notification, which is added to the event headers as well. Since version 0.3, Cygnus is able to support this header, although the actual processing of such tenant/organization depends on the particular sink. If the notification doesn't include the Fiware-Service header, then Cygnus will use the default organization specified in the "default_organization" configuration property.
* The notification reception time is included in the list of headers (as `recvTimeTs`) for timestamping purposes in the different sinks. 

The channel is a simple MemoryChannel behaving as a FIFO queue, and from where the different sinks extract the events in order to persist them; let's see how:


### OrionHDFSSink persistence

This sink persists the data in files, one per each entity, following this specification:

    <naming_prefix><entity_id>-<entity_type>.txt

Observe <code>naming_prefix</code> is a configuration parameter of the sink, which may be empty if no prefix is desired.
    
Within files, Json documents are written following one of these two schemas:

* Fixed 8-field lines: `recvTimeTs`, `recvTime`, `entityId`, `entityType`, `attrName`, `attrType`, `attrValue` and `attrMd`. Regarding `attrValue`, in its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array. Regarding `attrMd`, in contains a string serialization of the metadata for the attribute in Json (if the attribute hasn't metadata, `[]` is inserted).
*  Two fields per each entity's attribute (one for the value and other for the metadata), plus an additional field about the reception time of the data (`recvTime`). Regarding this kind of persistence, the notifications must ensure a value per each attribute is notified.

In both cases, the files are created at execution time if the file doesn't exist previously to the line insertion. The behaviour of the connector regarding the internal representation of the data is governed through a configuration parameter, `attr_persistence`, whose values can be `row` or `column`.

Thus, by receiving a notification like the one above, and being the persistence mode 'row', the file named `room1-Room.txt` (it is created if not existing) will contain a new line such as:

    {"recvTimeTs":"13453464536", "recvTime":"2014-02-27T14:46:21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":"26.5", "attrMd":[{name:ID, type:string, value:ground}]}

On the contrary, being the persistence mode 'column', the file named `room1-Room.txt` (it is created if not existing) will contain a new line such as:

    {"recvTime":"2014-02-27T14:46:21", "temperature":"26.5", "temperature_md":[{"name":"ID", "type":"string", "value":"ground"}]}

Each organization/tenant is associated to a different user in the HDFS filesystem.

### OrionCKANSink persistence

This sink persists the data in a [datastore](see http://docs.ckan.org/en/latest/maintaining/datastore.html) in CKAN. Datastores are associated to CKAN resources and as CKAN resources we use the entityId-entityType string concatenation. All CKAN resource IDs belong to the same datastore (also referred as package in CKAN terms), which name is specified with the `default_dataset` property (prefixed by organization name) in the CKAN sink configuration.

Each datastore, we can find two options:

* Fixed 6-field lines: `recvTimeTs`, `recvTime`, `attrName`, `attrType`, `attrValue` and `attrMd`. Regarding `attrValue`, in its simplest form, this value is just a string, but since Orion 0.11.0 it can be JSON object or JSON array. Regarding `attrMd`, in contains a string serialization of the metadata for the attribute in JSON (if the attribute hasn't metadata, `null` is inserted).
* Two columns per each entity's attribute (one for the value and other for the metadata), plus an additional field about the reception time of the data (`recvTime`). Regarding this kind of persistence, the notifications must ensure a value per each attribute is notified.

The behaviour of the connector regarding the internal representation of the data is governed through a configuration parameter, <code>attr_persistence</code>, whose values can be <code>row</code> or <code>column</code>.

Thus, by receiving a notification like the one above, and being the persistence mode 'row', the resource <code>room1-Room</code> (it is created if not existing) will containt the following row in its datastore:

    | _id | recvTimeTs   | recvTime            | attrName    | attrType   | attrValue | attrMd                                              |
    |-----|--------------|---------------------|-----.-------|------------|-----------|-----------------------------------------------------|
    | i   | 13453464536  | 2014-02-27T14:46:21 | temperature | centigrade | 26.5      | [{"name":"ID", "type":"string", "value":"ground"}]  |

where `i` depends on the number of rows previously inserted.

On the contrary, being the persistence mode 'column', the resource <code>room1-Room</code> (it and its datastore must be created in advance) will contain a new row such as shown below. In this case, an extra column ended with "_md" is added for the metadata.

    | _id | recvTime           | temperature | temperature_md                                     |
    |--------------------------|-------------|----------------------------------------------------|
    | i   |2014-02-27T14:46:21 | 26.5        | [{"name":"ID", "type":"string", "value":"ground"}] |

where `i` depends on the number of rows previously inserted.

The information stored in the datastore can be accesses as any other CKAN information, e.g. through the web frontend or using the query API, e.g;

    curl -s -S "http://${CKAN_HOST}/api/3/action/datastore_search?resource_id=${RESOURCE_ID}

Each organization/tenant is associated to a CKAN organization.

### OrionMySQLSink persistence

Similarly to OrionHDFSSink, a table is considered for each entity in order to store its notified context data, being the name for these tables:

    <naming_prefix><entity_id>_<entity_type>

These tables are stored in databases, one per user, enabling a private data space, with this name format:

    <naming_prefix><mysql_user>

Observe <code>naming_prefix</code> is a configuration parameter of the sink, which may be empty if no prefix is desired.

Within tables, we can find two options:

* Fixed 8-field rows, as usual: `recvTimeTs`, `recvTime`, `entityId`, `entityType`, `attrName`, `attrType`, `attrValue` and `attrMd`. These tables (and the databases) are created at execution time if the table doesn't exist previously to the row insertion. Regarding `attrValue`, in its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array. Regarding `attrMd`, in contains a string serialization of the metadata for the attribute in Json (if the attribute hasn't metadata, `[]` is inserted),
* Two columns per each entity's attribute (one for the value and other for the metadata), plus an addition column about the reception time of the data (`recv_time`). This kind of tables (and the databases) must be provisioned previously to the execution of Cygnus, because each entity may have a different number of attributes, and the notifications must ensure a value per each attribute is notified.

The behaviour of the connector regarding the internal representation of the data is governed through a configuration parameter, `attr_persistence`, whose values can be `row` or `column`.

Thus, by receiving a notification like the one above, and being the persistence mode 'row', the table named `room1-Room` (it is created if not existing) will contain a new row such as:

    | recvTimeTs   | recvTime            | entityId | entityType | attrName    | attrType   | attrValue | attrMd                                             |
    |--------------|---------------------|----------|------------|-------------|------------|-----------|----------------------------------------------------|
    | 13453464536  | 2014-02-27T14:46:21 | Room1    | Room       | temperature | centigrade | 26.5      | [{"name":"ID", "type":"string", "value":"ground"}] |

On the contrary, being the persistence mode 'column', the table named `room1-Room` (it must be created in advance) will contain a new row such as:

    | recvTime            | temperature | temperature_md                                     | 
    |---------------------|-------------|----------------------------------------------------|
    | 2014-02-27T14:46:21 | 26.5        | [{"name":"ID", "type":"string", "value":"ground"}] |

Each organization/tenant is associated to a different database.

## XML notification example

Cygnus also works with [XML-based notifications](https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE) sent to the connector. The only difference is the event is created by specifying the content type will be XML (in order the notification parser notices it):

    event={body={the_xml_part...},headers={{"content-type","application/xml"}, {"fiware-service","Org42"}, {"recvTimeTs","1402409899391"}}

The key point is the behaviour remains the same than in the Json example: the same file/datastores/tables will be created, and the same data will be persisted within it.

## Prerequisites

Maven (and thus Java SDK, since Maven is a Java tool) is needed in order to install and run Cygnus.

In order to install Java SDK (not JRE), just type (CentOS machines):

    $ yum install java-1.6.0-openjdk-devel

Remember to export the JAVA_HOME environment variable. In the case of using `yum install` as shown above, it would be:

    $ export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64

In order to do it permanently, edit `/root/.bash_profile` (`root` user) or `/etc/profile` (other users).

Maven is installed by downloading it from [maven.apache.org](http://maven.apache.org/download.cgi). Install it in a folder of your choice (represented by `APACHE_MAVEN_HOME`):

    $ wget http://www.eu.apache.org/dist/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz
    $ tar xzvf apache-maven-3.2.1-bin.tar.gz
    $ mv apache-maven-3.2.1 APACHE_MAVEN_HOME

## Installing Cygnus and its dependencies

Apache Flume can be easily installed by downloading its latests version from [flume.apache.org](http://flume.apache.org/download.html). Move the untared directory to a folder of your choice (represented by `APACHE_FLUME_HOME`):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME
    $ mkdir -p APACHE_FLUME_HOME/plugins.d/cygnus/
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/libext

The creation of the `plugins.d` directory is related to the installation of third-party software, like Cygnus.

Then, the developed classes must be packaged in a Java jar file; this can be done by including the dependencies in the package (**recommended**):

    $ git clone https://github.com/telefonicaid/fiware-connectors.git
    $ git checkout <branch>
    $ cd fiware-connectors/flume
    $ APACHE_MAVEN_HOME/bin/mvn clean compile assembly:single
    $ cp target/cygnus-0.2.1-jar-with-dependecies.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib

or not:

    $ git clone https://github.com/telefonicaid/fiware-connectors.git
    $ git checkout <branch>
    $ cd fiware-connectors/flume
    $ APACHE_MAVEN_HOME/bin/mvn package
    $ cp target/cygnus-0.2.1.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib

where `<branch>` is `develop` if you are trying to install the latest features or `release/x.y` if you are trying to install a stable release.

If the dependencies are included in the built Cygnus package, then nothing has to be done. If not, and depending on the Cygnus components you are going to use, you may need to install additional .jar files under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/`. Typically, you can get the .jar file from your Maven repository (under .m2 in your user home directory) and use the `cp` command.

In addition:

* Observe the version of `httpcomponents-core` and `httpcomponents-client` in the `pom.xml` are matching the version of such packages within the Flume bundle (`httpclient-4.2.1.jar` and `httpcore-4.2.1.jar`). These are not the newest versions of such packages, but trying to build Cygnus with such newest libraries has shown incompatibilities with Flume's ones.
* `libthrift-0.9.1.jar` must overwrite `APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar`

### OrionCKANSink dependencies

These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* json-simple-1.1.jar

### OrionHDFSSink dependencies

These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* hadoop-core-0.20.0.jar (or higher)
* hive-exec-0.12.0.jar
* hive-jdbc-0.12.0.jar
* hive-metastore-0.12.0.jar
* hive-service-0.12.0.jar
* hive-common-0.12.0.jar
* hive-shims-0.12.0.jar

These packages are not necessary to be installed since they are already included in the Flume bundle (they have been listed just for informative purposes):

* httpclient-4.2.1.jar
* httpcore-4.2.2.jar

In addition, as already said, remember to overwrite the `APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar` package with this one:

* libthrift-0.9.1.jar

### OrionMysQLSink dependencies

These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* mysql-connector-java-5.1.31-bin.jar

## Cygnus configuration

The typical configuration when using the `HTTPSource`, the `OrionRestHandler`, the `MemoryChannel` and the sinks is shown below (the file `cygnus.conf` can be instantiated from a template given in the clone Cygnus repository, `conf/cygnus.conf.template`):

```Python
# APACHE_FLUME_HOME/conf/cygnus.conf

# The next tree fields set the sources, sinks and channels used by Cygnus. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use multiple ones at the same time; the only requirement is to provide a channel for each
# one of them (this example shows how to configure 3 sinks at the same time).
cygnusagent.sources = http-source
cygnusagent.sinks = hdfs-sink mysql-sink ckan-sink
cygnusagent.channels = hdfs-channel mysql-channel ckan-channel

#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.http-source.channels = hdfs-channel mysql-channel ckan-channel
# source class, must not be changed
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# listening port the Flume source will use for receiving incoming notifications
cygnusagent.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
cygnusagent.sources.http-source.handler = es.tid.fiware.fiwareconnectors.cygnus.handlers.OrionRestHandler
# regular expression for the orion version the notifications will have in their headers
cygnusagent.sources.http-source.handler.orion_version = 0\.10\.*
# URL target
cygnusagent.sources.http-source.handler.notification_target = /notify
# Default organization (organization semantic depend on the persistence sink)
cygnusagent.sources.http-source.handler.default_organization = org42

# ============================================
# OrionHDFSSink configuration
# channel name from where to read notification events
cygnusagent.sinks.hdfs-sink.channel = hdfs-channel
# sink class, must not be changed
cygnusagent.sinks.hdfs-sink.type = es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionHDFSSink
# The FQDN/IP address of the Cosmos deployment where the notification events will be persisted
cygnusagent.sinks.hdfs-sink.cosmos_host = x.y.z.w
# port of the Cosmos service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs and free choice for inifinty
cygnusagent.sinks.hdfs-sink.cosmos_port = 14000
# username allowed to write in HDFS (/user/myusername)
cygnusagent.sinks.hdfs-sink.cosmos_username = myusername
# dataset where to persist the data (/user/myusername/mydataset)
cygnusagent.sinks.hdfs-sink.cosmos_dataset = mydataset
# HDFS backend type (webhdfs, httpfs or infinity)
cygnusagent.sinks.hdfs-sink.hdfs_api = httpfs
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.hdfs-sink.attr_persistence = column
# prefix for the database and table names, empty if no prefix is desired
cygnusagent.sinks.hdfs-sink.naming_prefix =
# Hive port for Hive external table provisioning
cygnusagent.sinks.hdfs-sink.hive_port = 10000

# ============================================
# OrionCKANSink configuration
# channel name from where to read notification events
cygnusagent.sinks.ckan-sink.channel = ckan-channel
# sink class, must not be changed
cygnusagent.sinks.ckan-sink.type = es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionCKANSink
# the CKAN API key to use
cygnusagent.sinks.ckan-sink.api_key = ckanapikey
# the FQDN/IP address for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_host = x.y.z.w
# the port for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_port = 80
# the dasaset (i.e. package) name to use within the organization. Must be purely lowercase alphanumeric (ascii)
# characters plus "-" and "_" acording to CKAN limitations. The default_dataset is prefixed by organization name
# to ensure uniqueness (see http://stackoverflow.com/questions/24203808/is-it-possible-to-create-packages-with-the-same-name-in-different-organizations)
cygnusagent.sinks.ckan-sink.default_dataset = mydataset
# Orion URL used to compose the resource URL with the convenience operation URL to query it
cygnusagent.sinks.ckan-sink.orion_url = http://localhost:1026
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.ckan-sink.attr_persistence = row

# ============================================
# OrionMySQLSink configuration
# channel name from where to read notification events
cygnusagent.sinks.mysql-sink.channel = mysql-channel
# sink class, must not be changed
cygnusagent.sinks.mysql-sink.type = es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionMySQLSink
# the FQDN/IP address where the MySQL server runs 
cygnusagent.sinks.mysql-sink.mysql_host = x.y.z.w
# the port where the MySQL server listes for incomming connections
cygnusagent.sinks.mysql-sink.mysql_port = 3306
# a valid user in the MySQL server
cygnusagent.sinks.mysql-sink.mysql_username = root
# password for the user above
cygnusagent.sinks.mysql-sink.mysql_password = xxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.mysql-sink.attr_persistence = column
# prefix for the database and table names, empty if no prefix is desired
cygnusagent.sinks.mysql-sink.naming_prefix =

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

```

## log4j configuration

Cygnus uses the log4j facilities added by Flume for logging purposes. You can maintain the default `APACHE_FLUME_HOME/conf/log4j.properties` file, where a console and a file appernder are defined (in addition, the console is used by default), or customize it by adding new appenders. Typically, you will have several instances of Cygnus running; they will be listening on different TCP ports for incoming notifyContextRequest and you'll probably want to have differente log files for them. E.g., if you have two Flume processes listening on TCP/1028 and TCP/1029 ports, then you can add the following lines to the `log4j.properties` file:

```Python
log4j.appender.cygnus1028=org.apache.log4j.RollingFileAppender
log4j.appender.cygnus1028.MaxFileSize=100MB
log4j.appender.cygnus1028.MaxBackupIndex=10
log4j.appender.cygnus1028.File=${flume.log.dir}/cygnus.1028.log
log4j.appender.cygnus1028.layout=org.apache.log4j.PatternLayout
log4j.appender.cygnus1028.layout.ConversionPattern=%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%t] (%C.%M:%L) %x - %m%n

log4j.appender.cygnus1028=org.apache.log4j.RollingFileAppender
log4j.appender.cygnus1028.MaxFileSize=100MB
log4j.appender.cygnus1028.MaxBackupIndex=10
log4j.appender.cygnus1028.File=${flume.log.dir}/cygnus.1029.log
log4j.appender.cygnus1028.layout=org.apache.log4j.PatternLayout
log4j.appender.cygnus1028.layout.ConversionPattern=%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%t] (%C.%M:%L) %x - %m%n
```

Once the log4j has been properly configured, you only have to add to the Flume command line the following parameter, which overwrites the default configutation (`flume.root.logger=INFO,LOGFILE`):

    -Dflume.root.logger=<loggin_level>,cygnus.<TCP_port>.log

## Running

In foreground (with logging):

    $ APACHE_FLUME_HOME/bin/flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cygnus.conf -n cygnusagent -Dflume.root.logger=INFO,console

In background:

    $ nohup APACHE_FLUME_HOME/bin/flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cygnus.conf -n cygnusagent -Dflume.root.logger=INFO,LOGFILE &

Remember you can change the logging level and the logging appender by changing the `-Dflume.root.logger` parameter.

## Orion subscription

Once the connector is running, it is necessary to tell Orion Context Broker about it, in order Orion can send context data notifications to the connector. This can be done on behalf of the connector by performing the following curl command:

    $ (curl localhost:1026/NGSI10/subscribeContext -s -S --header 'Content-Type: application/xml' -d @- | xmllint --format -) <<EOF
    <?xml version="1.0"?>
    <subscribeContextRequest>
      <entityIdList>
        <entityId type="Room" isPattern="false">
          <id>Room1</id>
        </entityId>
      <entityIdList>
      <attributeList>
        <attribute>temperature</attribute>
      </attributeList>
      <!-- This is the part where Cygnus endpoint is specified -->
      <reference>http://host_running_cygnus:5050/notify</reference>
      <duration>P1M</duration>
      <notifyConditions>
        <notifyCondition>
          <type>ONCHANGE</type>
          <condValueList>
            <condValue>pressure</condValue>
          </condValueList>
        </notifyCondition>
      </notifyConditions>
      <throttling>PT5S</throttling>
    </subscribeContextRequest>
    EOF

Its equivalent in Json format can be seen [here](https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE).

## Contact

* Fermín Galán Márquez (fermin at tid dot es).
* Francisco Romero Bueno (frb at tid dot es).
