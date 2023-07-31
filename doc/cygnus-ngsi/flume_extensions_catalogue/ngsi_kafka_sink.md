# <a name="top"></a>NGSIKafkaSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to Kafka data structures](#section1.2)
        * [Topics naming conventions](#section1.2.1)
        * [Storing](#section1.2.2)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Topic names](#section1.3.2)
        * [Storing](#section1.3.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About batching](#section2.3.1)
        * [About the encoding](#section2.3.2)
* [Programmers guide](#section3)
    * [`NGSIKafkaSink` class](#section3.1)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIKafkaSink`, or simply `NGSIKafkaSink` is a sink designed to persist NGSI-like context data events within a [Apache Kafka](http://kafka.apache.org/) deployment. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific Kafka data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to Kafka data structures
[Apache Kafka organizes](http://kafka.apache.org/documentation.html#introduction) the data in topics (a category or feed name to which messages are published). Such organization is exploited by `NGSIKafkaSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>Topics naming conventions
A Kafka topic is created (number of partitions 1) if not yet existing depending on the configured data model:

* Data model by service (`data_model=dm-by-service`). As the data model name denotes, the notified FIWARE service (or the configured one as default in [`NGSIRestHandler`](ngsi_rest_handler.md)) is used as the name of the topic. This allows the data about all the NGSI entities belonging to the same service is stored in this unique topic.
* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](ngsi_rest_handler.md)) is used as the name of the topic. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique topic. The only constraint regarding this data model is the FIWARE service path cannot be the root one (`/`).
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the topic name. If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated.
* Data model by attribute (`data_model=dm-by-attribute`). For each entity's attribute, the notified/default FIWARE service path is concatenated to the notified entity ID and type and to the notified attribute name in order to compose the topic name. If the FIWARE service path is the root one (`/`) then only the entity ID and type and the attribute name and type are concatenated.

It must be said there is no known character set accepted and/or forbidden for Kafka. Anyway, certaing [encoding](#section2.3.2) is applied.

The following table summarizes the topic name composition:

| FIWARE service path | `dm-by-service` | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|---|
| `/` | `<svc>` | `<svc>xffffx002f` | `<svc>xffffx002fxffff<entityId>xffff<entityType>` | `<svc>xffffx002fxffff<entityId>xffff<entityType>xffff<attrName>` |
| `/<svcPath>` | `<svc>` | `<svc>xffffx002f<svcPath>` | `<svc>xffffx002f<svcPath>xffff<entityId>xffff<entityType>` | `<svc>xffffx002f<svcPath>xffff<entityId>xffff<entityType>xffff<attrName>` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities` header value within the `NGSIEvent`.

[Top](#top)

#### <a name="section1.2.2"></a>Storing
`NGSIEvent`s structure is <i>stringified</i> as a Json object containing an array of headers and another object containing the Json data as it is notified by the NGSI-like source.

[Top](#top)

### <a name="section1.3"></a>Example
#### <a name="section1.3.1"></a>`NGSIEvent`
Assuming the following `NGSIEvent` is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    ngsi-event={
        headers={
	         content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         correlationId=1429535775-308-0000000000,
	         fiware-service=vehicles,
	         fiware-servicepath=/4wheels,
	         <name_mappings_interceptor_headers>
        },
        body={
	        entityId=car1,
	        entityType=car,
	        attributes=[
	            {
	                attrName=speed,
	                attrType=float,
	                attrValue=112.9
	            },
	            {
	                attrName=oil_level,
	                attrType=float,
	                attrValue=74.6
	            }
	        ]
	    }
    }

[Top](#top)

#### <a name="section1.3.2"></a>Topic names
The topic names will be, depending on the configured data model, the following ones:

| FIWARE service path | `dm-by-service` | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|---|
| `/` | `vehicles` | `vehiclesxffffx002f` | `vehiclesxffffx002fxffffcar1_car` | `vehiclesxffffx002fxffffcar1xffffcarxffffspeed`<br>`vehiclesxffffx002fxffffcar1xffffcarxffffoil_level` |
| `/4wheels` | `vehicles` | `vehiclesxffffx002f4wheels` | `vehiclesxffffx002f4wheelsxffffcar1xffffcar` | `vehiclesxffffx002f4wheelsxffffcar1xffffcarxffffspeed`<br>`vehiclesxffffx002f4wheelsxffffcar1xffffcarxffffoil_level` |

[Top](#top)

#### <a name="section1.3.3"></a>Storing
Let's assume a topic name `vehiclesxffffx002f4wheelsxffffcar1xffffcarxffffspeed` (data model by attribute, non-root service path). The data stored within this topic would be:

    $ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic vehiclesxffffx002f4wheelsxffffcar1xffffcarxffffspeed --from-beginning
    {"headers":[{"fiware-service":"vehicles"},{"fiware-servicePath":"/4wheels"},{"timestamp":1429535775}],"body":{"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"}],"type":"car","isPattern":"false","id":"car1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}}
    $ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic vehicles_4wheels_car1_car_oil_level --from-beginning
    {"headers":[{"fiware-service":"vehicles"},{"fiware-servicePath":"/4wheels"},{"timestamp":1429535775}],"body":{"contextElement":{"attributes":[{"name":"oil_level","type":"float","value":"74.6"}],"type":"car","isPattern":"false","id":"car1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}}

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIKafkaSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIKafkaSink</i> |
| channel | yes | N/A ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity |  <i>dm-by-service</i>, <i>dm-by-service-path</i>, <i>dm-by-entity</i> or <i>dm-by-attribute</i>. |
| broker\_list | no | localhost:9092 | Comma-separated list of Kafka brokers (a broker is defined as <i>host:port</i>). |
| zookeeper\_endpoint | no | localhost:2181 | Zookeeper endpoint needed to create Kafka topics, in the form of <i>host:port</i>. |
| partitions |  no | 1 | Number of partitions for a topic. |
| replication\_factor | no | 1 | For a topic with replication factor N, Kafka will tolerate N-1 server failures without losing any messages committed to the log. Replication factor must be less than or equal to the number of brokers created. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |

A configuration example could be:

    cygnus-ngsi.sinks = kafka-sink
    cygnus-ngsi.channels = kafka-channel
    ...
    cygnus-ngsi.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.NGSIKafkaSink
    cygnus-ngsi.sinks.kafka-sink.channel = kafka-channel
    cygnus-ngsi.sinks.kafka-sink.enable_lowercase = false
    cygnus-ngsi.sinks.kafka-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.kafka-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.kafka-sink.broker_list = localhost:9092
    cygnus-ngsi.sinks.kafka-sink.zookeeper_endpoint = localhost:2181
    cygnus-ngsi.sinks.kafka-sink.partitions = 5
    cygnus-ngsi.sinks.kafka-sink.replication_factor = 1
    cygnus-ngsi.sinks.kafka-sink.batch_size = 100
    cygnus-ngsi.sinks.kafka-sink.batch_timeout = 30
    cygnus-ngsi.sinks.kafka-sink.batch_ttl = 10
    cygnus-ngsi.sinks.kafka-sink.batch_retry_intervals = 5000

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIKafkaSink` if you want to integrate OrionContextBroker with a Kafka-based consumer, as a Storm real-time application.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About batching
As explained in the [programmers guide](#section3), `NGSIKafkaSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same Kafka topic. If processing the events one by one, we would need 100 writes to Kafka; nevertheless, in this example only one write is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination Kafka topic. In the worst case, the whole 100 entities will be about 100 different entities (100 different Kafka topics), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 writes of the event by event approach with only 10-15 writes.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIKafkaSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.2"></a>About the encoding
Cygnus applies this specific encoding tailored to Kafka data structures:

* Alphanumeric characters are not encoded.
* Numeric characters are not encoded.
* Underscore character, `_`, is not encoded.
* Hyphen character, `-`, is not encoded.
* Dot character, `.`, is not encoded.
* Equals character, `=`, is encoded as `xffff`.
* All other characters, including the slash in the FIWARE service paths, are encoded as a `x` character followed by the [Unicode](http://unicode-table.com) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* `xffff` is used as concatenator character.

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIKafkaSink` class
As any other NGSI-like sink, `NGSIKafkaSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the Kafka topic where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to the `KafkaProducer`.

    public void start();

`KafkaProducer` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIKafkaSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the one described above is read from the given `Context` instance.

[Top](#top)
