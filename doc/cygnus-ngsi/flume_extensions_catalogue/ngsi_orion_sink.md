# <a name="top"></a>NGSIOrionSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Example](#section1.2)
        * [`NGSIEvent`](#section1.2.1)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
* [Programmers guide](#section3)
    * [`NGSIOrionSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIOrionSink`, or simply `NGSIOrionSink` is a sink designed to persist NGSI-like context data events within a [Orion Context Broker](https://fiware-orion.readthedocs.io/en/master/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific Context Broker data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)



### <a name="section1.2"></a>Example
#### <a name="section1.2.1"></a>`NGSIEvent`
Assuming the following `NGSIEvent` is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    ngsi-event={
        headers={
	         content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         correlationId=1429535775-308-0000000000,
	         fiware-service=vehicles,
	         fiware-servicepath=/4wheels,
	         <grouping_rules_interceptor_headers>,
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

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIOrionSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIOrionSink</i> |
| channel | yes | N/A ||
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity | <i>dm-by-service-path</i> or <i>dm-by-entity</i>. <i>dm-by-service</i> and <dm-by-attribute</i> are not currently supported. |
| orion\_host | no | localhost | FQDN/IP address where the 'Context Broker' server runs. |
| orion\_port | no | N/A | You must to write port.|
| orion\_ssl | no | false | the connection of `ontext broker` is the default without ssl |
| orion\_username | no | N/A | You must to write username. |
| orion\_password | no | N/A | You must to write password. |
| keystone\_host | no | localhost | FQDN/IP address where the 'KeyStone' server runs. |
| keystone\_port | no | N/A | You must to write port.|
| keystone\_ssl | no | false | the connection of `KeyStone` is the default without ssl |
| orion\_fiware | no | N/A | You must to write fiware service. |
| orion\_fiware\_path | N/A | false | You must to write fiware servicePath. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| backend.enable\_cache | no | false | <i>true</i> or <i>false</i>, <i>true</i> enables the creation of a Cache, <i>false</i> disables the creation of a Cache. |

A configuration example could be:

    cygnus-ngsi.sinks = orion-sink
    cygnus-ngsi.channels = orion-channel
    ...
    cygnus-ngsi.sinks.orion-sink.type = com.telefonica.iot.cygnus.sinks.NGSIOrionSink
    cygnus-ngsi.sinks.orion-sink.channel = orion-channel
    cygnus-ngsi.sinks.orion-sink.enable_encoding = false
    cygnus-ngsi.sinks.orion-sink.enable_grouping = false
    cygnus-ngsi.sinks.orion-sink.enable_lowercase = false
    cygnus-ngsi.sinks.orion-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.orion-sink.type = com.telefonica.iot.cygnus.sinks.NGSINGSIOrionSink
    cygnus-ngsi.sinks.orion-sink.channel = orion-channel
    cygnus-ngsi.sinks.orion-sink.enable_encoding = false
    cygnus-ngsi.sinks.orion-sink.enable_grouping = false
    cygnus-ngsi.sinks.orion-sink.enable_lowercase = false
    cygnus-ngsi.sinks.orion-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.orion-sink.orion_host = XXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.orion_port = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.orion_ssl = false
    cygnus-ngsi.sinks.orion-sink.orion_username = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.orion_password = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.keystone_host = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.keystone_port = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.keystone_ssl = false
    cygnus-ngsi.sinks.orion-sink.orion_fiware = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.orion_fiware_path = XXXXXXXXXX
    cygnus-ngsi.sinks.orion-sink.batch_size = 100
    cygnus-ngsi.sinks.orion-sink.batch_timeout = 30
    cygnus-ngsi.sinks.orion-sink.batch_ttl = 10
    cygnus-ngsi.sinks.orion-sink.batch_retry_intervals = 5000
    cygnus-ngsi.sinks.orion.backend.enable_cache = false
    
    
    

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIOrionSink` class
As any other NGSI-like sink, `NGSIOrionSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(NGSIBatch batch) throws Exception;

A `NGSIBatch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies other Context Borker where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `OrionBackend` implementation.

    public void start();

An implementation of `OrionBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIOrionSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIOrionSink` relies on the keystone, username and password credentials created at the keyStone endpoint.


[Top](#top)