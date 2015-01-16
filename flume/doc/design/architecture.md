#Cygnus architecture

##Flume architecture
As stated in [flume.apache.org](http://flume.apache.org/FlumeDeveloperGuide.html):

>An Event is a unit of data that flows through a Flume agent. The Event flows from Source to Channel to Sink, and is represented by an implementation of the Event interface. An Event carries a payload (byte array) that is accompanied by an optional set of headers (string attributes). A Flume agent is a process (JVM) that hosts the components that allow Events to flow from an external source to a external destination.

>![](http://flume.apache.org/_images/DevGuide_image00.png)

>A Source consumes Events having a specific format, and those Events are delivered to the Source by an external source like a web server. For example, an AvroSource can be used to receive Avro Events from clients or from other Flume agents in the flow. When a Source receives an Event, it stores it into one or more Channels. The Channel is a passive store that holds the Event until that Event is consumed by a Sink. One type of Channel available in Flume is the FileChannel which uses the local filesystem as its backing store. A Sink is responsible for removing an Event from the Channel and putting it into an external repository like HDFS (in the case of an HDFSEventSink) or forwarding it to the Source at the next hop of the flow. The Source and Sink within the given agent run asynchronously with the Events staged in the Channel.

##Basic Cygnus architecture
The simplest way of using Cygnus is to adopt basic constructs of <i>source -> channel -> sink</i> as described in the Apache Flume documentation. There can be as many basic constructs as persistence elements; within Cygnus context, these are:

* <i>source -> hdfs-channel -> hdfs-sink</i>
* <i>source -> mysql-channel -> mysql-sink</i>
* <i>source -> ckan-channel -> ckan-sink</i>

Please observe a generic <i>source</i> has been used instead of specific <i>hdfs-source</i>, <i>mysql-source</i> or <i>ckan-source</i>. This is because **the source is the same for all the persistence elements**, i.e. a [`HttpSource`](http://flume.apache.org/FlumeUserGuide.html#http-source). The way this native source processes the Orion notifications is by means of a specific REST handler: `OrionRESTHandler`.  

Regarding the channels, in the current version of Cygnus all of them are  recommended to be of type [`MemoryChannel`](http://flume.apache.org/FlumeUserGuide.html#memory-channel), nevertheless nothing avoids a [`FileChannel`](http://flume.apache.org/FlumeUserGuide.html#file-channel) or a [`JDBCChannel`](http://flume.apache.org/FlumeUserGuide.html#jdbc-channel) can be used.

Finally, the sinks are custom ones, one per each persistence element covered by the current version of Cygnus: `OrionHDFSSink`, `OrionMySQLSink` and `OrionCKANSink`.
 
![](../images/basic_architecture.jpg")

##Advanced Cygnus architectures
All the advanced archictures arise when trying to improve the performance of Cygnus. As seen above, basic Cygnus configuration is about a source writting Flume events into a single channel where a single sink consumes those events. This can be clearly moved to a multiple sink configuration running in parallel. But there is not a single configuration but many (more details in [`doc/operation/performace_tuning_tips.md`](../operation/performance_tuning_tips.md)):

### Multiple sinks, single channel
You can simply add more sinks consuming events from the same single channel. This configuration theoretically increases the processing capabilities in the sink side, but usually shows an important drawback, specially if the events are consumed by the sinks very fast: the sinks have to compete for the single channel. Thus, some times you can find that adding more sinks in this way simply turns the system slower than a single sink configuration. This configuration is only recommended when the sinks require a lot of time to process a single event, ensuring few collisions when accessing the channel.

![](../images/multiple_sinks_single_channel_architecture.jpg")

### Multiple sinks, multiple channels
The above mentioned drawback can be solved by configuring a channel per each sink, avoiding the competition for the single channel.

This can only be done by using a Flume <i>Channel Selector</i>, a piece of software dispatching the events to the appropriate channel. The default one is [`ReplicatingChannelSelector`](http://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default), i.e. each time a Flume event is generated at the sources, it is replicated in all the channels connected to those sources. There is another selector, the [`MultiplexingChannelSelector`](http://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector), which puts the events in a channel given certain matching-like criteria. Nevertheless:

* We want the Flume events are put into a single channel, not replicated.
* And the dispatching criteria is not based on a matching rule but on a <i>round robin</i>-like behaviour.

Due to the available <i>Channel Selectors</i> do not fit our needs, a custom selector has been developed: `RoundRobinChannelSelector`. This selector extends [`AbstractChannelSelector`](https://flume.apache.org/releases/content/1.4.0/apidocs/org/apache/flume/channel/AbstractChannelSelector.html) as [`ReplicatingChannelSelector`](http://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default) and [`MultiplexingChannelSelector`](http://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector) do.

![](../images/multiple_sinks_multiple_channels_architecture.jpg")

##Sequence diagrams
### Notification handling (default `ReplicatingChannelSelector`)
![](../images/sequence_diagram_notification_handling_default.jpg")

### Notification handling (custom `RoundRobinChannelSelector`)
![](../images/sequence_diagram_notification_handling_round_robin.jpg")

### HDFS backend
![](../images/sequence_diagram_hdfs.jpg")

### CKAN backend
![](../images/sequence_diagram_ckan.jpg")

###MySQL backend
![](../images/sequence_diagram_mysql.jpg")

## Contact
* Fermín Galán Márquez (fermin.galanmarquez@telefonica.com).
* Francisco Romero Bueno (francisco.romerobueno@telefonica.com).
