#<a name="top"></a>OrionKafkaSink
* [Functionality](#section1)
    * [Mapping Flume events to Kafka data structures](#section1.1)
    * [Example](#section1.2)
* [Configuration](#section2)
* [Use cases](#section3)
* [Implementation details](#section4)
    * [`OrionKafkaSink` class](#section4.1)
    * [`KafkaProducer` class (backend)](#section4.2)
* [Contact](#section5)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionKafkaSink`, or simply `OrionKafkaSink` is a sink designed to persist NGSI-like context data events within a [Apache Kafka](http://kafka.apache.org/) deployment. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always [transformed](from_ngsi_events_to_flume_events.md) into internal Flume events at Cygnus sources thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. In the end, the information within these Flume events must be mapped into specific Kafka data structures.

[Top](#top)

###<a name="section1.1"></a>Mapping Flume events to Kafka data structures
[Apache Kafka organizes](http://kafka.apache.org/documentation.html#introduction) the data in topics (a category or feed name to which messages are published). Such organization is exploited by `OrionKafkaSink` each time a Flume event is taken, by performing the following workflow:

1. The bytes within the event's body are parsed and a `NotifyContextRequest` object container is created.
2. A Kafka topic is created (number of partitions 1) if not yet existing depending on the configured topic type:
    * `topic-per-destination`. A topic named `<destination>` is created, where `<destination>` value is got from the event headers.
    * `topic-per-service-path`. A topic named `<fiware-servicePath>` is created, where `<fiware-servicePath>` value is got from the event headers.
    * `topic-per-service`. A topic named `<fiware-service>` is created, where `<fiware-service>` value is got from the event headers.
3. The context responses/entities within the container are iterated, and they are serialized in the Kafka topic as JSON documents.

###<a name="section1.2"></a>Example
Assuming the following Flume event is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	        content-type=application/json,
	        fiware-service=vehicles,
	        fiware-servicepath=4wheels,
	        timestamp=1429535775,
	        transactionId=1429535775-308-0000000000,
	        ttl=10,
	        destination=car1_car
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

Assuming `topic_type=topic-per-destination` as configuration parameter, then `OrionKafkaSink` will persist the data within the body as:

    $ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic room1_room --from-beginning
    {"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"},{"name":"oil_level","type":"float","value":"74.6"}],"type":"Room","isPattern":"false","id":"Room1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}
    
If `topic_type=topic-per-service-path` then `OrionKafkaSink` will persist the data within the body as:

    $ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic 4wheels --from-beginning
    {"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"},{"name":"oil_level","type":"float","value":"74.6"}],"type":"Room","isPattern":"false","id":"Room1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}
    
If `topic_type=topic-per-service` then `OrionKafkaSink` will persist the data within the body as:

    $ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic vehicles --from-beginning
    {"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"},{"name":"oil_level","type":"float","value":"74.6"}],"type":"Room","isPattern":"false","id":"Room1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}
    
[Top](#top)

##<a name="section2"></a>Configuration
`OrionKafkaSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionKafkaSink</i> |
| channel | yes | N/A |
| topic_type | no | topic-by-destination | <i>topic-by-destination</i>, <i>topic-by-service-path</i> or <i>topic-by-service</i> |
| broker_list | no | localhost:9092 | Comma-separated list of Kafka brokers (a broker is defined as <i>host:port</i>) |
| zookeeper_endpoint | no | localhost:2181 | Zookeeper endpoint needed to create Kafka topics, in the form of <i>host:port</i> |

A configuration example could be:

    cygnusagent.sinks = kafka-sink
    cygnusagent.channels = kafka-channel
    ...
    cygnusagent.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.OrionKafkaSink
    cygnusagent.sinks.kafka-sink.channel = kafka-channel
    cygnusagent.sinks.kafka-sink.topic_type = topic-by-destination
    cygnusagent.sinks.kafka-sink.broker_list = localhost:9092
    cygnusagent.sinks.kafka-sink.zookeeper_endpoint = localhost:2181

[Top](#top)

##<a name="section3"></a>Use cases
Use `OrionKafkaSink` if you want to integrate OrionContextBroker with a Kafka-based consumer, as a Storm real-time application.

[Top](#top)

##<a name="section4"></a>Implementation details
###<a name="section4.1"></a>`OrionKafkaSink` class
As any other NGSI-like sink, `OrionKafkaSink` extends the base `OrionSink`. The methods that are extended are:

    void persist(Map<String, String>, NotifyContextRequest) throws Exception;
    
The context data, already parsed by `OrionSink` in `NotifyContextRequest`, is iterated and persisted in the Kafka backend by means of a `KafKaProducer` instance. Header information from the `Map<String, String>` is used to complete the persitence process, such as the FIWARE service, the FIWARE service path or the destination.
    
    public void start();

`KafkaProducer` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionKafkaSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the one described above is read from the given `Context` instance.

[Top](#top)

###<a name="section4.2"></a>`KafkaProducer` class (backend)
The implementation of a class dealing with the details of the backend is given by Kafka itself through the [`KafkaProducer`](http://kafka.apache.org/082/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html) class. Thus, the sink has been developed by invoking the methods within that class, specially:

    public send(ProducerRecord<K,V>);
    
Which sends a [`ProducerRecord`](http://kafka.apache.org/082/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html) object to the configured topic.

[Top](#top)

##<a name="section5"></a>Contact
Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**

[Top](#top)
