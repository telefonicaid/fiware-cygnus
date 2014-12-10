# Tuning tips for increasing the performance

## Sink parallelization
Most of the processing effort done by Cygnus is located at the sinks, and these elements can be a bottleneck if not configured appropriately.

Basic Cygnus configuration is about a source writting Flume events into a single channel where a single sink consumes those events:

    cygnusagent.sources = mysource
    cygnusagent.sinks = mysink
    cygnusagent.channels = mychannel
    
    cygnusagent.sources.mysource.type = ...
    cygnusagent.sources.mysource.channels = mychannel
    ... other source configurations...

    cygnusagent.channels.mychannel.type = ...
    ... other channel configurations...
    
    cygnusagent.sinks.mysink.type = ...
    cygnusagent.sinks.mysink.channel = mychannel
    ... other sink configurations...

This can be clearly moved to a multiple sink configuration running in parallel. But there is not a single configuration but many:

### Multiple sinks, single channel
You can simply add more sinks consuming events from the same single channel. This configuration theoretically increases the processing capabilities in the sink side, but usually shows an important drawback, specially if the events are consumed by the sinks very fast: the sinks have to compete for the single channel. Thus, some times you can find that adding more sinks in this way simply turns the system slower than a single sink configuration. This configuration is only recommended when the sinks require a lot of time to process a single event, ensuring few collisions when accessing the channel.

    cygnusagent.sources = mysource
    cygnusagent.sinks = mysink1 mysink2 mysink3
    cygnusagent.channels = mychannel
    
    cygnusagent.sources.mysource.type = ...
    cygnusagent.sources.mysource.channels = mychannel
    ... other source configurations...

    cygnusagent.channels.mychannel.type = ...
    ... other channel configurations...
    
    cygnusagent.sinks.mysink1.type = ...
    cygnusagent.sinks.mysink1.channel = mychannel
    ... other sink configurations...
    
    cygnusagent.sinks.mysink2.type = ...
    cygnusagent.sinks.mysink2.channel = mychannel
    ... other sink configurations...

    cygnusagent.sinks.mysink3.type = ...
    cygnusagent.sinks.mysink3.channel = mychannel
    ... other sink configurations...

### Multiple sinks, multiple channels
The above mentioned drawback can be solved by configuring a channel per each sink, avoiding the competition for the single channel.

This can only be done by using a Flume <i>Channel Selector</i>, a piece of software dispatching the events to the appropriate channel. The default one is [`Replicating Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default), i.e. each time a Flume event is generated at the sources, it is replicated in all the channels connected to those sources. There is another selector, the [`Multiplexing Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector), which puts the events in a channel given certain matching-like criteria. Nevertheless:

* We want the Flume events are put into a single channel, not replicated.
* And the dispatching criteria is not based on a matching rule but on a <i>round robin</i>-like behaviour.

Due to the available <i>Channel Selectors</i> do not fit our needs, a custom selector has been developed: `RoundRobinChannelSelector`. This selector extends [`AbstractChannelSelector`](https://flume.apache.org/releases/content/1.4.0/apidocs/org/apache/flume/channel/AbstractChannelSelector.html) as [`Replicating Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default) and [`Multiplexing Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector) do.

    cygnusagent.sources = mysource
    cygnusagent.sinks = mysink1 mysink2 mysink3
    cygnusagent.channels = mychannel1 mychannel2 mychannel3
    
    cygnusagent.sources.mysource.type = ...
    cygnusagent.sources.mysource.channels = mychannel1 mychannel2 mychannel3
    cygnusagent.sources.mysource.selector.type = es.tid.fiware.fiwareconnectors.cygnus.channelselectors.RoundRobinChannelSelector
    ... other source configurations...

    cygnusagent.channels.mychannel1.type = ...
    ... other channel configurations...

    cygnusagent.channels.mychannel2.type = ...
    ... other channel configurations...

    cygnusagent.channels.mychannel3.type = ...
    ... other channel configurations...
    
    cygnusagent.sinks.mysink1.type = ...
    cygnusagent.sinks.mysink1.channel = mychannel1
    ... other sink configurations...
    
    cygnusagent.sinks.mysink2.type = ...
    cygnusagent.sinks.mysink2.channel = mychannel2
    ... other sink configurations...

    cygnusagent.sinks.mysink3.type = ...
    cygnusagent.sinks.mysink3.channel = mychannel3
    ... other sink configurations...

### Why the `LoadBalancingSinkProcessor` is not suitable
[This](http://flume.apache.org/FlumeUserGuide.html#load-balancing-sink-processor) Flume <i>Sink Processor</i> is not suitable for our parallelization purposes due to the load balancing is done in a sequential way. I.e. either in a round robin-like configuration of the load balancer either in a ramdom way, the sinks are used one by one and not at the same time.

## Channel considerations

### Channel type
The most important thing when designing a channel for Cygnus (in general, a Flume-based application) is the tradeoff between speed and reliability. This applies especialy to the channels.

On the one hand, the `MemoryChannel` is a very fast channel since it is implemented directly in memory, but it is not reliable at all if, for instance, Cygnus crashes for any reason and it is recovered by a third party system (let's say <i>Monit</i>): in that case the Flume events put into the memory-based channel before the crash are lost. On the other hand, the `FileChannel` and `JDBCChannel` are very reliable since there is a permanent support for the data in terms of OS files or RDBM tables, respectively. Nevertheless, they are slower than a `MemoryChannel` sice the I/O is done against the HDD and not against the memory.

### Channel capacity
There are no empirical tests showing a decrease of the performance if the channel capacity is configured with a large number, let's say 1 million of Flume events. The `MemoryChannel` is supposed to be designed as a chained FIFO queue, and the persistent channels only manage a list of pointers to the real data, which should not be hard to iterate.

Such large capacities are only required when the Flume sources are faster than the Flume sinks (and even in that case, sooner or later, the channels will get full) or a lot of processing retries are expected within the sinks (see next section).

In order to calculate the appropiate capacity, just have in consideration the following parameters:

* The amount of events to be put into the channel by the sources per unit time (let's say 1 minute).
* The amount of events to be gotten from the channel by the sinks per unit time.
* An estimation of the amount of events that could not be processed per unit time, and thus to be reinjected into the channel (see next section).

## Events TTL
Every Flume event managed by Cygnus has associated a <i>Time-To-Live</i> (TTL), a number specifying how many times that event can be reinjected in the channel the sink got it from. Events are reinjected when a processing error occurs (for instance, the persistence system is not available, there has been a communication breakdown, etc.). This TTL has to be configured very carefully since large TTLs may lead to a quick channel capacity exhaustion, and once reached that capacity new events cannot be put into the channel. In addition, the more large is the TTL, the more will decrease the performance of the Cygnus instance since both new fresh events will have to coexist with old not processed events in the queue. 

If you don't care about not processed events, you may configure a 0 TTL, obtaining the maximum performance regarding this aspect.

## `DestinationExtractor` matching rules 
The destination extraction feature is a powerful tool for <i>routing</i> your data, i.e. deciding the right destination (HDFS file, MySQL table, CKAN resource) for your context data; on the contrary, the default destination is used, i.e. the concatenation of the entity identifier and the entity type.

As you may suppose, the usage of the destination extractor is slower than using the default. This is because the destination is decided after checking a list of rules in a sequential way, trying to find a regex match. Here, worth remembering that regex matching is slow, and that you may configure as many matching rules as you want/need.

Nevertheless, you may write your matching rules in a smart way:

* Place the most probably matching rules first. Since the checking is sequential, the sooner the appropriate rule is found for a certain event the sooner another event may be checked. Thus, having those rules applying to the majority of the events in the first place of the list will increase the performance; then, put the rules applying to the second major set of evens, and so on.
* The simplest matching set of rules derive from the simplest way of naming the context entities, their types or the fiware-service they belog to (see [doc/design/interceptors.md](doc/design/interceptors.md) for more details on these concepts). Try to use names that can be easily grouped, e.g. <i>numeric rooms</i> and <i>character rooms</i> can be easily modeled by using only 2 regular expressions such as `room\.(\d*)` and `room\.(\D*)`, but more anarchical ways of naming them will lead for sure into much more different more complex rules.

## Contact information
Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) 