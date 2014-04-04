# Cygnus connector

This connector is a (conceptual) derivative work of ngsi2cosmos (https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos), and implements a Flume-based connector for context data coming from Orion Context Broker.

## Development

All the details about Flume can be found at http://flume.apache.org/ but, as a reminder, some concepts will be explained here:
* A Flume source is an agent gathering event data from the real source (Twitter stream, a notification system, etc.), either by polling the source or listening for incoming pushes of data. Gathered data is sent to a Flume channel.
* A Flume channel is a pasive store (implemented by means of a file, memory, etc.) that holds the event until it is consumed by the Flume sink.
* A Flume sink connects with the final destination of the data (a local file, HDFS, a database, etc.), taking events from the channel and consuming them (processing and/or persisting it).

There exists a wide collection of already developed sources, channels and sinks. The Flume-based cosmos-injector development extends that collection by adding:
* OrionRestHandler. A custom HTTP source handler for the default HTTP source. The existing HTTP source behaviour can be governed depending on the request handler associated to it in the configuration. In this case, the custom handler takes care of the method, the target and the headers (specially the Content-Type one) within the request, cheking everything is according to the expected request format (https://forge.fi-ware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). This allows for a certain degree of control on the incoming data. The header inspection step allows for a content type identification as well by sending, together with the data, the Content-Type header.
* OrionHDFSSink. A custom HDFS sink for persiting Orion context data in the appropriate way. Data from Orion must be persisted in the Cosmos HDFS in the form of files containing multiple lines, each line storing the value an entity's attribute has had in a certain timestamp. In addition, each file only considers the values for a (entity,attribute) pair. Several HDFS backends can be used for the data persistence (WebHDFS, HttpFS, Infinity), all of them based on the native WebHDFS REST API from Hadoop.

## Json notification example

Let's consider the following notification in Json format coming from an Orion Context Broker instance:

    POST http://localhost:1028/accumulate
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

The developed sink persists the data according to the original ngsi2cosmos specification, i.e. for each (entity,attribute) pair, create/append to a file named

    <entity_name>-<entity_type>-<attribute_name>-<attribute_type>.txt
    
data lines in the form

    <ts>|<ts_ms>|<entity_name>|<entity_type>|<attribute_name>|<attribute_type>|<value>
    
Thus, the file named "Room1-Room-temperature-centigrade.txt" (it is created if not existing) will contain a new line such as "2014-02-27 14_46_21|13453464536|Room1|Room|temperature|centigrade|26.5".

## XML notification example

The injector also works with XML-based notifications sent to the injector (it can be seen at https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). The only difference is the event is created by specifying the content type is XML, and the notification parsing is done in a different way:

    event={body={the_json_part...},headers={{"content-type","application/json"}}}

The key point is the behaviour remains the same than in the Json example: the same file will be created, and the same data line will be persisted within it.

## Prerequisites

Maven (and thus Java SDK, since Maven is a Java tool) is needed in order to install and run the injector.

In order to install Java SDK (not JRE), just type (CentOS machines):

    $ yum install java-1.6.0-openjdk-devel

Remember to export the JAVA_HOME environment variable.

    $ export JAVA_HOME=...

In order to do it permanently, edit /root/.bash_profile (root user) or /etc/profile (other users).

Maven is installed by downloading it from http://maven.apache.org/download.cgi. Install it in a folder of your choice (represented by APACHE_MAVEN_HOME):

    $ wget http://www.eu.apache.org/dist/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz
    $ tar xzvf apache-maven-3.2.1-bin.tar.gz
    $ mv apache-maven-3.2.1-bin APACHE_MAVEN_HOME

## Installation

Apache Flume can be easily installed by downloading its latests version from http://flume.apache.org/download.html. Move the untared directory to a folder of your choice (represented by APACHE_FLUME_HOME):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME

Then, the developed classes must be packaged in a Java jar file which must be added to the APACHE_FLUME_HOME/lib directory:

    $ git clone https://github.com/telefonicaid/fiware-connectors.git
    $ cd fiware-connectors/flume
    $ APACHE_MAVEN_HOME/bin/mvn package
    $ cp target/cosmos-injector-1.0-SNAPSHOT.jar APACHE_FLUME_HOME/lib

Please observe the cosmos-injector code has been built using the Flume provided versions of httpcomponents-core and httpcomponents-client (4.2.1). These are not the newest versions of such packages, but trying to build the cosmos-injector with such newest libraries has shown incompatibilities with Flume's ones.

## cosmos-injector configuration

The typical configuration when using the HTTP source, the OrionRestHandler, the MemoryChannel and the OrionHDFSSink is shown below:

```Python
# APACHE_FLUME_HOME/conf/cosmos-injector.conf
orionagent.sources = http-source
orionagent.sinks = hdfs-sink
orionagent.channels = notifications

# Flume source, must not be changed
orionagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# channel name where to write the notification events
orionagent.sources.http-source.channels = notifications
# listening port the Flume source will use for receiving incoming notifications
orionagent.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
orionagent.sources.http-source.handler = es.tid.fiware.orionconnectors.cosmosinjector.OrionRestHandler
# regular expression for the orion version the notifications will have in their headers
orionagent.sources.http-source.handler.orion_version = 0\.10\.*
# URL target
orionagent.sources.http-source.handler.notification_target = /notify

# channel name from where to read notification events
orionagent.sinks.hdfs-sink.channel = notifications
# Flume sink that will process and persist in HDFS the notification events, must not be changed
orionagent.sinks.hdfs-sink.type = es.tid.fiware.orionconnectors.cosmosinjector.OrionHDFSSink
# IP address of the Cosmos deployment where the notification events will be persisted
orionagent.sinks.hdfs-sink.cosmos_host = x.y.z.w
# port of the Cosmos service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs and free choice for inifinty
orionagent.sinks.hdfs-sink.cosmos_port = 14000
# username allowed to write in HDFS (/user/username)
orionagent.sinks.hdfs-sink.cosmos_username = opendata
# dataset where to persist the data (/user/username/dataset)
orionagent.sinks.hdfs-sink.cosmos_dataset = test
# HDFS backend type (webhdfs, httpfs or infinity)
orionagent.sinks.hdfs-sink.hdfs_api = httpfs

# channel name
orionagent.channels.notifications.type = memory
# capacity of the channel
orionagent.channels.notifications.capacity = 1000
# amount of bytes that can be sent per transaction
orionagent.channels.notifications.transactionCapacity = 100
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

    APACHE_FLUME_HOME/bin/flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cosmos-injector.conf -n orionagent -Dflume.root.logger=INFO,console 

In background:

    nohup APACHE_FLUME_HOME/bin/flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cosmos-injector.conf -n orionagent -Dflume.root.logger=INFO,LOGFILE &

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

* Fermín Galán Márquez (fermin at tid dot es).
* Francisco Romero Bueno (frb at tid dot es).
