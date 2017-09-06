# <a name="top"></a>Tuning tips for increasing the performance
Content:

* [Batching](#section1)
    * [Sizing](#section1.1)
    * [Retries](#section1.2)
* [Sink parallelization](#section2)
    * [Multiple sinks, single channel](#section2.1)
    * [Multiple sinks, multiple channels](#section2.2)
    * [Why the `LoadBalancingSinkProcessor` is not suitable](#section2.3)
* [Channel considerations](#section3)
    * [Channel type](#section3.1)
    * [Channel capacity](#section3.2)
* [Name Mappings](#section4)
* [Grouping Rules](#section5)
* [Writing logs](#section6)

## <a name="section1"></a>Batching
### <a name="section1.1"></a>Sizing
Batching is the mechanism Cygnus implements for processing sets of `NGSIEvent`s (a `NGSIEvent` typically comes from a Orion's notification) all together instead of one by one. These sets, or properly said <i>batches</i>, are built by `NGSISink`, the base class all the sinks extend. Thus, having the batches already created in the inherited code the sinks only have to deal with the persistence of the data within them. Typically, the information within a whole batch is aggregated into a large data chunk that is stored at the same time by using a single write/insert/upsert operation. Why?

What is important regarding the batch mechanism is it largely increases the performance of the sink because the number of writes is dramatically reduced. Let's see an example. Let's assume 100 notifications, no batching mechanism at all and a HDFS storage. It seems obvious 100 writes are needed, one per `NGSIEvent`/notification. And writing to disk is largely slow. Now let's assume a batch of size 100. In the best case, all these `NGSIEvent`s/notifications regard to the same entity, which means all the data within them will be persisted in the same HDFS file and therefore only one write is required.

Obviously, not all the `NGSIEvent`s/notifications will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches are created within a batch, one sub-batch per final destination HDFS file. In the worst case, the whole 100 entities will be about 100 different entities (100 different HDFS destinations), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 writes of the event by event approach with only 10-15 writes.

Nevertheless, a couple of risks arise when using batches:

* The first one is the last batch may never get built. I.e. in the above 100 size batch if only 99 `NGSIEvent`s/notifications are notified and the 100th `NGSIEvent`/notifications never arrives, then the batch is never ready to be processed by the sink. Thats the reason the batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.
* The second one is the data within the batch may be lost if Cygnus crashes or it is stopped while accumulating it. Please observe until the batch size (or the timeout) is reached the data within the batch is not persisted and it exists nowhere in the data workflow (the NGSI source -typically Orion Context Broker- most probably will not have a copy of the data anymore once it has been notified). There is an under study [issue](https://github.com/telefonicaid/fiware-cygnus/issues/566) regarding this.

By default, all the sinks have a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. These are the parameters all the sinks have for these purpose:

    <agent_name>.sinks.<sink_name>.batch_size = 1
    <agent_name>.sinks.<sink_name>.batch_timeout = 30

Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. On the contrary, very large batch sizes and timeouts may have impact on your data persistence if Cygnus crashes or it is stopped in the meantime.

[Top](#top)

### <a name="section1.2"></a>Retries
Batches may not be persisted. This is something may occur from time to time because the sink is temporarily not available, or the communications are failing. In that case, Cygnus has implemented a retry mechanism.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event (0 means no retries, -1 means infinite retries). On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then the second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, all the sinks have a configured batch TTL and retry intervals of 10 and 5000 miliseconds, respectively. These are the parameters all the sinks have for these purpose:

    <agent_name>.sinks.<sink_name>.batch_ttl = 10
    <agent_name>.sinks.<sink_name>.batch_retry_intervals = 5000
    
With regards to performance, on the one hand retries slow down Cygnus since they are `NGSIEvent`s/notification enqueued that consume CPU when they are attempted. But most important is all the time the persistence backend is not available, new `NGSIEvent`s/notifications will be added to the retry queue. This is specially important when using infinite retries (`batch_ttl = -1`). In that case, the retry queue size will never decrease.

On the other hand, very short retry intervals will make Cygnus working unncessarily if the persistence backend takes a while for recovering. This effect is multiplied if using infinite retries (`batch_ttl = -1`).

[Top](#top)

## <a name="section2"></a>Sink parallelization
Most of the processing effort done by Cygnus is located at the sinks, and these elements can be a bottleneck if not configured appropriately.

Basic Cygnus configuration is about a source writing Flume events into a single channel where a single sink consumes those events:

    cygnus-ngsi.sources = mysource
    cygnus-ngsi.sinks = mysink
    cygnus-ngsi.channels = mychannel

    cygnus-ngsi.sources.mysource.type = ...
    cygnus-ngsi.sources.mysource.channels = mychannel
    ... other source configurations...

    cygnus-ngsi.channels.mychannel.type = ...
    ... other channel configurations...

    cygnus-ngsi.sinks.mysink.type = ...
    cygnus-ngsi.sinks.mysink.channel = mychannel
    ... other sink configurations...

This can be clearly moved to a multiple sink configuration running in parallel. But there is not a single configuration but many:

[Top](#top)

### <a name="section2.1"></a>Multiple sinks, single channel
You can simply add more sinks consuming events from the same single channel. This configuration theoretically increases the processing capabilities in the sink side, but usually shows an important drawback, specially if the events are consumed by the sinks very fast: the sinks have to compete for the single channel. Thus, some times you can find that adding more sinks in this way simply turns the system slower than a single sink configuration. This configuration is only recommended when the sinks require a lot of time to process a single event, ensuring few collisions when accessing the channel.

    cygnus-ngsi.sources = mysource
    cygnus-ngsi.sinks = mysink1 mysink2 mysink3 ...
    cygnus-ngsi.channels = mychannel

    cygnus-ngsi.sources.mysource.type = ...
    cygnus-ngsi.sources.mysource.channels = mychannel
    ... other source configurations...

    cygnus-ngsi.channels.mychannel.type = ...
    ... other channel configurations...

    cygnus-ngsi.sinks.mysink1.type = ...
    cygnus-ngsi.sinks.mysink1.channel = mychannel
    ... other sink configurations...

    cygnus-ngsi.sinks.mysink2.type = ...
    cygnus-ngsi.sinks.mysink2.channel = mychannel
    ... other sink configurations...

    cygnus-ngsi.sinks.mysink3.type = ...
    cygnus-ngsi.sinks.mysink3.channel = mychannel
    ... other sink configurations...

    ... other sinks configurations...

[Top](#top)

### <a name="section2.2"></a>Multiple sinks, multiple channels
The above mentioned drawback can be solved by configuring a channel per each sink, avoiding the competition for the single channel.

However, when multiple channels are used for a same storage, then some kind of <i>dispatcher</i> deciding which channels will receive a copy of the events is required. This is the goal of the Flume <i>Channel Selectors</i>, a piece of software selecting the appropriate set of channels the Flume events will be put in. The default one is [`Replicating Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default), i.e. each time a Flume event is generated at the sources, it is replicated in all the channels connected to those sources. There is another selector, the [`Multiplexing Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector), which puts the events in a channel given certain matching-like criteria. Nevertheless:

* We want the Flume events to be replicated per each configured storage. E.g. we want the events are persisted both in a HDFS and CKAN storage.
* But within a storage, we want the Flume events to be put into a single channel, not replicated. E.g. among all the channels associated to a HDFS storage, we only want to put the event within a single one of them.
* And the dispatching criteria is not based on a matching rule but on a <i>round robin</i>-like behaviour. E.g. if we have 3 channels (`ch1`, `ch2`, `ch3`) associated to a HDFS storage, then select first `ch1`, then `ch2`, then `ch3` and then again `ch1`, etc.

Due to the available <i>Channel Selectors</i> do not fit our needs, a custom selector has been developed: `RoundRobinChannelSelector`. This selector extends [`AbstractChannelSelector`](https://flume.apache.org/releases/content/1.4.0/apidocs/org/apache/flume/channel/AbstractChannelSelector.html) as [`Replicating Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default) and [`Multiplexing Channel Selector`](http://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector) do.

    cygnus-ngsi.sources = mysource
    cygnus-ngsi.sinks = mysink1 mysink2 mysink3
    cygnus-ngsi.channels = mychannel1 mychannel2 mychannel3

    cygnus-ngsi.sources.mysource.type = ...
    cygnus-ngsi.sources.mysource.channels = mychannel1 mychannel2 mychannel3 ...
    cygnus-ngsi.sources.mysource.selector.type = com.telefonica.iot.cygnus.channelselectors.RoundRobinChannelSelector
    cygnus-ngsi.sources.mysource.selector.storages = N
    cygnus-ngsi.sources.mysource.selector.storages.storage1 = <subset_of_cygnusagent.sources.mysource.channels>
    ...
    cygnus-ngsi.sources.mysource.selector.storages.storageN = <subset_of_cygnusagent.sources.mysource.channels>
    ... other source configurations...

    cygnus-ngsi.channels.mychannel1.type = ...
    ... other channel configurations...

    cygnus-ngsi.channels.mychannel2.type = ...
    ... other channel configurations...

    cygnus-ngsi.channels.mychannel3.type = ...
    ... other channel configurations...

    cygnus-ngsi.sinks.mysink1.type = ...
    cygnus-ngsi.sinks.mysink1.channel = mychannel1
    ... other sink configurations...

    cygnus-ngsi.sinks.mysink2.type = ...
    cygnus-ngsi.sinks.mysink2.channel = mychannel2
    ... other sink configurations...

    cygnus-ngsi.sinks.mysink3.type = ...
    cygnus-ngsi.sinks.mysink3.channel = mychannel3
    ... other sink configurations...

    ... other sinks configurations...

Basically, the custom <i>Channel Selector</i> type must be configured, together with the mapping of channels per storage. This mapping is configured in the form of:

* Total number of different storages. E.g. if we have a MySQL storage, a CKAN storage and a HDFS storage then `cygnus-ngsi.sources.mysource.selector.storages = 3`. Please observe this apply to different storages of the same type, e.g. if we have a MySQL storage and two different HDFS storages (i.e. different HDFS endpoints), then `cygnus-ngsi.sources.mysource.selector.storages = 3` as well.
* Subset of channels associated to each storage. The union of all the subsets must be equal to all the channels configured for the source. E.g. if `cygnus-ngsi.sources.mysource.channels = ch1 ch2 ch3 ch4 ch5 ch6` and if `ch1` is associated to a MySQL storage, `ch2` and `ch3` are associated to a CKAN storage and `ch4`, `ch5` and `ch6` are associated to a HDFS storage then `cygnus-ngsi.sources.mysource.selector.storages.storage1 = ch1`, `cygnus-ngsi.sources.mysource.selector.storages.storage2 = ch2,ch3` and `cygnus-ngsi.sources.mysource.selector.storages.storage3 = ch4,ch5,ch6`.

[Top](#top)

### <a name="section2.3"></a>Why the `LoadBalancingSinkProcessor` is not suitable
[This](http://flume.apache.org/FlumeUserGuide.html#load-balancing-sink-processor) Flume <i>Sink Processor</i> is not suitable for our parallelization purposes due to the load balancing is done in a sequential way. I.e. either in a round robin-like configuration of the load balancer either in a random way, the sinks are used one by one and not at the same time.

[Top](#top)

## <a name="section3"></a>Channel considerations
### <a name="section3.1"></a>Channel type
The most important thing when designing a channel for Cygnus (in general, a Flume-based application) is the tradeoff between speed and reliability. This applies especially to the channels.

On the one hand, the `MemoryChannel` is a very fast channel since it is implemented directly in memory, but it is not reliable at all if, for instance, Cygnus crashes for any reason and it is recovered by a third party system (let's say <i>Monit</i>): in that case the Flume events put into the memory-based channel before the crash are lost. On the other hand, the `FileChannel` and `JDBCChannel` are very reliable since there is a permanent support for the data in terms of OS files or RDBM tables, respectively. Nevertheless, they are slower than a `MemoryChannel` since the I/O is done against the HDD and not against the memory.

[Top](#top)

### <a name="section3.2"></a>Channel capacity
There are no empirical tests showing a decrease of the performance if the channel capacity is configured with a large number, let's say 1 million of Flume events. The `MemoryChannel` is supposed to be designed as a chained FIFO queue, and the persistent channels only manage a list of pointers to the real data, which should not be hard to iterate.

Such large capacities are only required when the Flume sources are faster than the Flume sinks (and even in that case, sooner or later, the channels will get full) or a lot of processing retries are expected within the sinks (see next section).

In order to calculate the appropriate capacity, just have in consideration the following parameters:

* The amount of events to be put into the channel by the sources per unit time (let's say 1 minute).
* The amount of events to be gotten from the channel by the sinks per unit time.
* An estimation of the amount of events that could not be processed per unit time, and thus to be reinjected into the channel (see next section).

[Top](#top)

## <a name="section4"></a>Name Mappings
Name Mappings feature is a powerful tool for changing the original notified FIWARE service, FIWARE service path, entity ID and type, and attributes name and type. As a side effect of this changing, Name Mappings can be used for <i>routing</i> your data, for instance by setting a common alternative FIWARE service path for two or more original service paths, all the data regarding these service paths will be stored under the same CKAN package.

As you may suppose, the usage of Name Mappings slows down Cygnus because the alternative settings are obtained after checking a list of mappings written in Json format. Despite such a Json is loaded into memory and regular expressions are compiled into patterns, it must be iterated each time a `NGSIEvent`/notification is sent to Cygnus and the conditions for matching checked.

Nevertheless, you may write your Name Mappings in a smart way:

* Place the most probably mappings first. Since the checking is sequential, the sooner the appropriate mapping is found for a certain event the sooner another `NGSIEvent`/notification may be checked. Thus, having those mappings applying to the majority of the `NGSIEvent`s/notifications in the first place of the list will increase the performance; then, put the mappings applying to the second major set of `NGSIEvent`s/notifications, and so on.
* The simplest set of mappings derive from the simplest way of naming and typing the context entities and attributes, as well as the FIWARE service and FIWARE service path they belong to. Try to use names that can be easily grouped, e.g. <i>numeric rooms</i> and <i>not numeric rooms</i> can be easily modeled by using only 2 regular expressions such as `room\.(\d*)` and `room\.(\D*)`, but more anarchical ways of naming them will lead for sure into much more different more complex mappings.

[Top](#top)

## <a name="section5"></a>Grouping Rules
**IMPORTANT NOTE: from release 1.6.0, this feature is deprecated in favour of Name Mappings. More details can be found [here](./deprecated_and_removed.md#section2.1).**

Grouping Rules feature is a powerful tool for <i>routing</i> your data, i.e. setting an alternative FIWARE service path and entity, whcih in the end decides the HDFS file, MySQL/PostgreSQL/DynamoDB/Carto table, CKAN resource, Kafka queue or MongoDB collection for your context data; on the contrary, the default destination is used.

As you may suppose, the usage of Grouping Rules slows down Cygnus because the alternative FIWARE service path and entity are set after checking a list of rules in a sequential way, trying to find a regex match. Here, worth remembering that regex matching is slow, and that you may configure as many grouping rules as you want/need.

Nevertheless, you may write your Grouping Rules in a smart way:

* Place the most probably rules first. Since the checking is sequential, the sooner the appropriate rule is found for a certain event the sooner another event may be checked. Thus, having those rules applying to the majority of the events in the first place of the list will increase the performance; then, put the rules applying to the second major set of evens, and so on.
* The simplest matching set of rules derive from the simplest way of naming the context entities, their types or the fiware-service they belong to. Try to use names that can be easily grouped, e.g. <i>numeric rooms</i> and <i>not numeric rooms</i> can be easily modeled by using only 2 regular expressions such as `room\.(\d*)` and `room\.(\D*)`, but more anarchical ways of naming them will lead for sure into much more different more complex rules.

[Top](#top)

## <a name="section6"></a>Writing logs
Writing logs, as any I/O operation where disk writes are involved, is largely slow. Please avoid writing a huge number if logs unless necessary, i.e. because your are debugging Cygnus, and try running cygnus at least with `INFO` level (despite a lot of logs are still written at that level). The best is running with `ERROR` level. Logs are totally disabled by using the `OFF` level.

Logging level Cygnus run with is configured in `/usr/cygnus/conf/log4j.properties`. `INFO` is configured by default:

     flume.root.logger=INFO,LOGFILE

[Top](#top)
