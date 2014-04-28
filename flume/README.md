# Cygnus connector

This connector is a (conceptual) derivative work of ngsi2cosmos (https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos), and implements a Flume-based connector for context data coming from Orion Context Broker and aimed to be stored in a specific persistent storage, such as HDFS or CKAN.

## Development

All the details about Flume can be found at http://flume.apache.org/ but, as a reminder, some concepts will be explained here:
* A Flume source is an agent gathering event data from the real source (Twitter stream, a notification system, etc.), either by polling the source or listening for incoming pushes of data. Gathered data is sent to a Flume channel.
* A Flume channel is a passive store (implemented by means of a file, memory, etc.) that holds the event until it is consumed by the Flume sink.
* A Flume sink connects with the final destination of the data (a local file, HDFS, a database, etc.), taking events from the channel and consuming them (processing and/or persisting it).

There exists a wide collection of already developed sources, channels and sinks. The Flume-based cosmos-injector, also called Cygnus, development extends that collection by adding:
* OrionRestHandler. A custom HTTP source handler for the default HTTP source. The existing HTTP source behaviour can be governed depending on the request handler associated to it in the configuration. In this case, the custom handler takes care of the method, the target and the headers (specially the Content-Type one) within the request, cheking everything is according to the expected request format (https://forge.fi-ware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). This allows for a certain degree of control on the incoming data. The header inspection step allows for a content type identification as well by sending, together with the data, the Content-Type header.
* OrionHDFSSink. A custom HDFS sink for persiting Orion context data in the appropriate way. Data from Orion must be persisted in the Cosmos HDFS in the form of files containing multiple lines, each line storing the value an entity's attribute has had in a certain timestamp. In addition, each file only considers the values for a (entity,attribute) pair. Several HDFS backends can be used for the data persistence (WebHDFS, HttpFS, Infinity), all of them based on the native WebHDFS REST API from Hadoop.
* OrionCKANSink. A custom sink that persist Orion context data in CKAN server instances (see http://docs.ckan.org/en/latest/)

## Functionality explained (Json notification example)

Let's consider the following notification in Json format coming from an Orion Context Broker instance:

    POST http://localhost:1028/notify
    Content-Length: 492
    User-Agent: orion/0.9.0
    Host: localhost:1028
    Accept: application/xml, application/json
    Content-Type: application/json
    
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
                "value" : "26.5"
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

Such a notification is sent by Orion to the default Flume HTTP source, which relies on the developed OrionRestHandler for checking its validity (it is a POST request, the target is "notify" and the headers are OK), detecting the content type (it is in Json format), extracting the data (the Json part) and creating an event to be put in the channel:

    event={body={the_json_part...},headers={{"content-type","application/json"}}}

The channel is a simple MemoryChannel behaving as a FIFO queue, and from where the OrionHDFSSink extracts the events.

Depending on the sink, the context element is persisted in a different way:

### OrionHDFSSink

From Cygnus v0.2, this sink does not persist the data according to the original ngsi2cosmos specification anymore (i.e. for each (entity,attribute) pair a file was created). Instead, a unique file is created per entity, following this specification:

    cygnus-<hdfs_user>-<hdfs_dataset>-<entity_id>-<entity_type>.txt
    
Another novelty added by Cygnus v0.2 is related to the data serialization in such above files. Now, instead of persisting CSV-like lines, Json documents are written in the format below. This complies with the new Orion Context Broker functionality allowing for complex Json values for attributes.

    {"ts":"xxx", "iso8601date":"xxx", "entityId":"xxx", "entityType":"xxx", "attrName":"xxx", "attrType":"xxx", "attrValue":"xxx"|{...}|[...]}
    
Thus, the file named `cygnus-mysuer-mydataset-room1-Room.txt` (it is created if not existing) will contain a new line such as `{"ts":"13453464536", "iso8601data":"2014-02-27T14_46_21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":"26.5"}`

### OrionCKANSink

This sink persists the data in a datastore in CKAN (see http://docs.ckan.org/en/latest/maintaining/datastore.html). Datastores are associated to CKAN resources and as CKAN
resources we use the entityID-entityType string concatenation. All CKAN resource IDs belong to the same datastore (also referred as package in CKAN terms), which name
is specified with the 'dataset' property in the CKAN sink configuration.

Each context element in the datastore has the following fields:

* ts: timestamp since Unix Epoch.
* iso8601date: the same as ts, but in human readable form
* attrName: the attribute name, coming from the NGSI notification
* attrType: the attribute type, coming from the NGSI notification
* attrValue: the attribute value, coming from the NGSI notification. It its simplest form, this value is just a string, but since Orion 0.11.0 it can
  be JSON object or JSON array.

The information stored in the datastore can be accesses as any other CKAN information, e.g. through the web frontend or using the query API, e.g;

    curl -s -S "http://${CKAN_HOST}/api/3/action/datastore_search?resource_id=${RESOURCE_ID}

## XML notification example

Cygnus also works with XML-based notifications sent to the injector (it can be seen at https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). The only difference is the event is created by specifying the content type is XML, and the notification parsing is done in a different way:

    event={body={the_json_part...},headers={{"content-type","application/xml"}}}

The key point is the behaviour remains the same than in the Json example: the same file will be created, and the same data line will be persisted within it.

## Prerequisites

Maven (and thus Java SDK, since Maven is a Java tool) is needed in order to install and run the injector.

In order to install Java SDK (not JRE), just type (CentOS machines):

    $ yum install java-1.6.0-openjdk-devel

Remember to export the JAVA_HOME environment variable. In the case of using yum install as shown above, it would be:

    $ export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64

In order to do it permanently, edit /root/.bash_profile (root user) or /etc/profile (other users).

Maven is installed by downloading it from http://maven.apache.org/download.cgi. Install it in a folder of your choice (represented by APACHE_MAVEN_HOME):

    $ wget http://www.eu.apache.org/dist/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz
    $ tar xzvf apache-maven-3.2.1-bin.tar.gz
    $ mv apache-maven-3.2.1 APACHE_MAVEN_HOME

## Installing Cygnus and its dependencies

Apache Flume can be easily installed by downloading its latests version from http://flume.apache.org/download.html. Move the untared directory to a folder of your choice (represented by APACHE_FLUME_HOME):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME
    $ mkdir -p APACHE_FLUME_HOME/plugins.d/cygnus/
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/libext

The creation of the `plugins.d` directory is related to the installation of third-party software, like Cygnus.

Then, the developed classes must be packaged in a Java jar file; this can be done by including the dependencies in the package:

    $ git clone https://github.com/telefonicaid/fiware-connectors.git
    $ cd fiware-connectors/flume
    $ APACHE_MAVEN_HOME/bin/mvn clean compile assembly:single
    $ cp target/cygnus-0.1.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib

or not:

    $ git clone https://github.com/telefonicaid/fiware-connectors.git
    $ cd fiware-connectors/flume
    $ APACHE_MAVEN_HOME/bin/mvn package
    $ cp target/cygnus-0.1.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib

If the dependencies are included in the built Cygnus package, then nothing has to be done. If not, and depending on the Cygnus components you are going to use, you may need to install additional .jar files under APACHE_FLUME_HOME/plugins.d/cygnus/libext. Typically, you can get the .jar file from your Maven repository (under .m2 in your user home directory) and use the `cp` command.

In addition:
* Please observe the version of httpcomponents-core and httpcomponents-client in the pom.xml are matching the version of such packages within the Flume bundle (httpclient-4.2.1.jar and httpcore-4.2.1.jar). These are not the newest versions of such packages, but trying to build the cosmos-injector with such newest libraries has shown incompatibilities with Flume's ones.
* libthrift-0.9.1.jar must overwrite APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar

### OrionCKANSink dependencies

These are the packages you will need to install under APACHE_FLUME_HOME/plugins.d/cygnus/libext if you did not included them in the Cygnus package:

* json-simple-1.1.jar

### OrionHDFSSink dependencies

These are the packages you will need to install under APACHE_FLUME_HOME/plugins.d/cygnus/libext if you did not included them in the Cygnus package:

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

In addition, as already said, please remember to overwrite the APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar package with this one:
* libthrift-0.9.1.jar

## Cygnus configuration

The typical configuration when using the HTTP source, the OrionRestHandler, the MemoryChannel and the sinks is shown below (the file cygnus.conf must be created from the scratch):

```Python
# APACHE_FLUME_HOME/conf/cygnus.conf

# The next tree fields set the sources, sinks and channels used by Cygnus. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use at the same time both 'hdfs-sink' and 'ckan-sink' (configuration example shows only 'hdfs-sink')
cygnusagent.sources = http-source
cygnusagent.sinks = hdfs-sink
cygnusagent.channels = notifications

#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.http-source.channels = notifications
# source class, must not be changed
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# listening port the Flume source will use for receiving incoming notifications
cygnusagent.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
cygnusagent.sources.http-source.handler = es.tid.fiware.orionconnectors.cosmosinjector.OrionRestHandler
# regular expression for the orion version the notifications will have in their headers
cygnusagent.sources.http-source.handler.orion_version = 0\.10\.*
# URL target
cygnusagent.sources.http-source.handler.notification_target = /notify

# ============================================
# OrionHDFSSink configuration
# channel name from where to read notification events
cygnusagent.sinks.hdfs-sink.channel = notifications
# sink class, must not be changed
cygnusagent.sinks.hdfs-sink.type = es.tid.fiware.orionconnectors.cosmosinjector.OrionHDFSSink
# IP address of the Cosmos deployment where the notification events will be persisted
cygnusagent.sinks.hdfs-sink.cosmos_host = x.y.z.w
# port of the Cosmos service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs and free choice for inifinty
cygnusagent.sinks.hdfs-sink.cosmos_port = 14000
# username allowed to write in HDFS (/user/myusername)
cygnusagent.sinks.hdfs-sink.cosmos_username = myusername
# dataset where to persist the data (/user/myusername/mydataset)
cygnusagent.sinks.hdfs-sink.cosmos_dataset = mydataset
# HDFS backend type (webhdfs, httpfs or infinity)
cygnusagent.sinks.hdfs-sink.hdfs_api = httpfs

# ============================================
# OrionCKANSink configuration
# channel name from where to read notification events
cygnusagent.sinks.ckan-sink.channel = notifications
# sink class, must not be changed
cygnusagent.sinks.ckan-sink.type = es.tid.fiware.orionconnectors.cosmosinjector.OrionCKANSink
# The CKAN API key to use
cygnusagent.sinks.ckan-sink.api_key = <api_key>
# The host for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_host = localhost
# The port for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_port = 80
# The dasaset (i.e. package) name to use. It will be created at Flume startup time if it doesn't previously exist
cygnusagent.sinks.ckan-sink.dataset = mydataset

#=============================================
# channel configuration
# channel name
cygnusagent.channels.notifications.type = memory
# capacity of the channel
cygnusagent.channels.notifications.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.notifications.transactionCapacity = 100
```

## log4j configuration

The injector uses the log4j facilities added by Flume for logging purposes. You can maintain the default APACHE_FLUME_HOME/conf/log4j.properties file, where a console and a file appernder are defined (in addition, the console is used by default), or customize it by adding new appenders. Typically, you will have several instances of the cosmos-injector running; they will be listening on different TCP ports for incoming notifyContextRequest and you'll probably want to have differente log files for them. E.g., if you have two Flume processes listening on TCP/1028 and TCP/1029 ports, then you can add the following lines to the log4j.properties file:

```Python
log4j.appender.cosmosinjector1028=org.apache.log4j.RollingFileAppender
log4j.appender.cosmosinjector1028.MaxFileSize=100MB
log4j.appender.cosmosinjector1028.MaxBackupIndex=10
log4j.appender.cosmosinjector1028.File=${flume.log.dir}/cosmos-injector.1028.log
log4j.appender.cosmosinjector1028.layout=org.apache.log4j.PatternLayout
log4j.appender.cosmosinjector1028.layout.ConversionPattern=%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%t] (%C.%M:%L) %x - %m%n

log4j.appender.cosmosinjector1029=org.apache.log4j.RollingFileAppender
log4j.appender.cosmosinjector1029.MaxFileSize=100MB
log4j.appender.cosmosinjector1029.MaxBackupIndex=10
log4j.appender.cosmosinjector1029.File=${flume.log.dir}/cosmos-injector.1029.log
log4j.appender.cosmosinjector1029.layout=org.apache.log4j.PatternLayout
log4j.appender.cosmosinjector1029.layout.ConversionPattern=%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%t] (%C.%M:%L) %x - %m%n
```

Once the log4j has been properly configured, you only have to add to the Flume command line the following parameter, which overwrites the default configutation (flume.root.logger=INFO,LOGFILE):

    -Dflume.root.logger=<loggin_level>,cosmos-injector.<TCP_port>.log

## Running

In foreground (with logging):

    APACHE_FLUME_HOME/bin/flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cygnus.conf -n cygnusagent -Dflume.root.logger=INFO,console

In background:

    nohup APACHE_FLUME_HOME/bin/flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cygnus.conf -n cygnusagent -Dflume.root.logger=INFO,LOGFILE &

Remember you can change the logging level and the logging appender by changing the -Dflume.root.logger parameter.

## Orion subscription

Once the connector is running, it is necessary to tell Orion Context Broker about it, in order Orion can send context data notifications to the connector. This can be done on behalf of the connector by performing the following curl command:

    (curl localhost:1026/NGSI10/subscribeContext -s -S --header 'Content-Type: application/xml' -d @- | xmllint --format -) <<EOF
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
      <!-- This is the part where the cosmos-injector is specified -->
      <reference>http://host_running_the_cosmos-injector:5050/notify</reference>
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

Its equivalent in Json format can be seen at https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE

## Contact

* Fermin Galan Marquez (fermin at tid dot es).
* Francisco Romero Bueno (frb at tid dot es).
