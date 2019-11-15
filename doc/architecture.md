# <a name="top"></a>Cygnus architecture
Cygnus runs Flume agents. Thus, Cygnus agents' architecture is Flume agents' one. Let's see how this architecture ranges from the most basic configuration to the most complex one.

## Flume architecture
As stated in [flume.apache.org](http://flume.apache.org/FlumeDeveloperGuide.html):

>An Event is a unit of data that flows through a Flume agent. The Event flows from Source to Channel to Sink, and is represented by an implementation of the Event interface. An Event carries a payload (byte array) that is accompanied by an optional set of headers (string attributes). A Flume agent is a process (JVM) that hosts the components that allow Events to flow from an external source to a external destination.

>![](http://flume.apache.org/_images/DevGuide_image00.png)

>A Source consumes Events having a specific format, and those Events are delivered to the Source by an external source like a web server. For example, an AvroSource can be used to receive Avro Events from clients or from other Flume agents in the flow. When a Source receives an Event, it stores it into one or more Channels. The Channel is a passive store that holds the Event until that Event is consumed by a Sink. One type of Channel available in Flume is the FileChannel which uses the local filesystem as its backing store. A Sink is responsible for removing an Event from the Channel and putting it into an external repository like HDFS (in the case of an HDFSEventSink) or forwarding it to the Source at the next hop of the flow. The Source and Sink within the given agent run asynchronously with the Events staged in the Channel.

[Top](#top)

## Basic Cygnus agent architecture
The simplest way of using Cygnus is to adopt basic constructs/flows of <i>source - channel - sink</i> as described in the Apache Flume documentation. There can be as many basic constructs/flows as persistence elements, i.e. one for HDFS, another one for MySQL, etc.

For each one of this flows, a [`HttpSource`](http://flume.apache.org/FlumeUserGuide.html#http-source) has to be used. The way this native sources process the Orion notifications is by means of a specific REST handler: `NGSIRESTHandler`. Nevertheless, this basic approach requires each source receives its own event notifications. This is not a problem if the architect clearly defines which flows must end in a HDFS storage, or in a Carto storage, if talking about a NGSI agent. But, what happens if the same event must be stored at HDFS and Carto at the same time? In this case, the constructs are modified in order all of them have the same Http source; then, the notified event is replicated for each channel connected to the source.

Regarding the channels, in the current version of Cygnus all of them are recommended to be of type [`MemoryChannel`](http://flume.apache.org/FlumeUserGuide.html#memory-channel), nevertheless nothing avoids a [`FileChannel`](http://flume.apache.org/FlumeUserGuide.html#file-channel) or a [`JDBCChannel`](http://flume.apache.org/FlumeUserGuide.html#jdbc-channel) can be used.

Finally, the sinks are custom ones, one per each persistence element covered by the current version of Cygnus:

* NGSI sinks:
    * `NGSIHDFSSink`.
    * `NGSIMySQLSink`.
    * `NGSICKANSink`.
    * `NGSIMongoSink`.
    * `NGSISTHSink`.
    * `NGSIPostgreSQLSink`.
    * `NGSIPostgisSink`.
    * `NSGICartoDBSink`.
    * `NGSIDynamoDBSink`.
    * `NGSIKafkaSink`.
* Twitter sinks:
    * `TwitterHDFSSink`.

[Top](#top)

## Advanced Cygnus architectures
It depends on the specific Cygnus agent you are going to use.

In the case of Cygnus for NGSI, please check the [configuration examples](cygnus-ngsi/installation_and_administration_guide/configuration_examples.md). Because Flume/Cygnus configurations derive from Flume/Cygnus agent architecture, by checking the advanced configuration examples you'll be able to understand how complex Cygnus for NGSI architectures can be.

In the case of Cygnus for Twitter, agent architectures are limited to the basic one.

[Top](#top)

## High availability Cygnus architecture
Flume/Cygnus does not implement any High Availability (HA) mechanism *per se*. Anyway, implementing HA for Flume/Cygnus is as easy as running two instances of the software and putting a load balancer in between them and the data source (or sources). Of course, the load balancer itself is a single point of failure; there are solutions for this, but they are out of scope of this document.

Being said this, please check the [reliability](cygnus-ngsi/installation_and_administration_guide/reliability.md) documentation of Cygnus for NGSI regarding certain specific considerations when deploying a NGSI agent in HA.

[Top](#top)
