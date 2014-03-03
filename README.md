# FI-WARE Orion Connectors

## Flume-based cosmos-injector (proof of concept)

This proof of concept is meant to analyze whether or not Flume is suitable for developing the FI-WARE Orion Connectors, and in the case it is possible, how easy or difficult it is. The tested injector has been the cosmos one (cosmos-injector), which is a (conceptual) derivative work of ngsi2cosmos (https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos).

### Development

All the details about Flume can be found at http://flume.apache.org/ but, as a reminder, some concepts will be explained here:
* A Flume source is an agent gathering event data from the real source (Twitter stream, a notification system, etc.), either by polling the source or listening for incoming pushes of data. Gathered data is sent to a Flume channel.
* A Flume channel is a pasive store (implemented by means of a file, memory, etc.) that holds the event until it is consumed by the Flume sink.
* A Flume sink connects with the final destination of the data (a local file, HDFS, a database, etc.), taking events from the channel and consuming them (processing and/or persisting it).

There exists a wide collection of already developed sources, channels and sinks. The Flume-based cosmos-injector development extends that collection by adding:
* OrionRestHandler. A custom HTTP source handler for the default HTTP source. The existing HTTP source behaviour can be governed depending on the request handler associated to it in the configuration. In this case, the custom handler takes care of the method, the target and the headers (specially the Content-Type one) within the request, cheking everything is according to the expected request format (https://forge.fi-ware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). This allows for a certain degree of control on the incoming data. The header inspection step allows for a content type identification as well by sending, together with the data, the Content-Type header.
* OrionHDFSSink. A custom HDFS sink for persiting Orion context data in the appropriate way. Data from Orion must be persisted in the Cosmos HDFS in the form of files containing multiple lines, each line storing the value an entity's attribute has had in a certain timestamp. In addition, each file only considers the values for a (entity,attribute) pair.

### Example

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

### Prerequisites

Maven (and thus Java SDK, since Maven is a Java tool) is needed in order to install and run the injector.

In order to install Java SDK (not JRE), just type (CentOS machines):

    $ yum install java-1.6.0-openjdk-devel

Maven is installed by downloading it from http://maven.apache.org/download.cgi. Install it in a folder of your choice (represented by APACHE_MAVEN_HOME):

    $ wget http://www.eu.apache.org/dist/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz
    $ tar xzvf apache-maven-3.2.1-bin.tar.gz
    $ mv apache-maven-3.2.1-bin APACHE_MAVEN_HOME

### Installation

Apache Flume can be easily installed by downloading its latests version from http://flume.apache.org/download.html. Move the untared directory to a folder of your choice (represented by APACHE_FLUME_HOME):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME

Then, the developed classes must be packaged in a Java jar file which must be added to the APACHE_FLUME_HOME/lib directory. In addition, it may be necessary to add the jars regarding httpcomponents-client-4.3.1-bin.tar.gz (the version used by default in the development, see the pom.xml file).

    $ git clone https://github.com/telefonicaid/fiware-orion-connectors.git
    $ cd fiware-orion-connectors/cosmos-injector
    $ APACHE_MAVEN_HOME/bin/mvn package
    $ cp target/cosmos-injector-1.0-SNAPSHOT.jar APACHE_FLUME_HOME/lib
    $ ...
    $ wget http://archive.apache.org/dist/httpcomponents/httpclient/binary/httpcomponents-client-4.3.1-bin.tar.gz
    $ tar xzvf httpcomponents-client-4.3.1-bin.tar.gz
    $ cp httpcomponents-client-4.3.1-bin APACHE_FLUME_HOME/lib

### Configuration

The typical configuration when using the HTTP source, the OrionRestHandler, the MemoryChannel and the OrionHDFSSink is shown below:

    # APACHE_FLUME_HOME/conf/cosmos-injector.conf
    orionagent.sources = http-source
    orionagent.sinks = hdfs-sink
    orionagent.channels = notifications
    
    orionagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
    orionagent.sources.http-source.channels = notifications
    orionagent.sources.http-source.port = 5050
    orionagent.sources.http-source.handler = es.tid.fiware.orionconnectors.cosmosinjector.OrionRestHandler
    
    orionagent.sinks.hdfs-sink.channel = notifications
    orionagent.sinks.hdfs-sink.type = es.tid.fiware.orionconnectors.cosmosinjector.OrionHDFSSink
    orionagent.sinks.hdfs-sink.cosmos_host = x.y.z.w
    orionagent.sinks.hdfs-sink.cosmos_port = 14000
    orionagent.sinks.hdfs-sink.cosmos_username = opendata
    orionagent.sinks.hdfs-sink.cosmos_basedir = test
    orionagent.sinks.hdfs-sink.hdfs_api = httpfs
    
    orionagent.channels.notifications.type = memory
    orionagent.channels.notifications.capacity = 1000
    orionagent.channels.notifications.transactionCapacity = 100

### Running

In foreground (with logging):

    APACHE_FLUME_HOME/bin/flume-ng agent -f APACHE_FLUME_HOME/conf/cosmos-injector.cont -n orionagent -Dflume.root.logger=INFO,console 

In background:

    nohup APACHE_FLUME_HOME/bin/flume-ng agent -f APACHE_FLUME_HOME/conf/cosmos-injector.cont -n orionagent &
