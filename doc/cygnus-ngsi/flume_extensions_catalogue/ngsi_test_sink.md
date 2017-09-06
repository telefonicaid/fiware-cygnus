# <a name="top"></a>NGSITestSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to logs](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About batching](#section2.3.1)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSITestSink`, or simply `NGSITestSink` is a sink designed to test Cygnus when receiving NGSI-like context data events. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` Object at Cygnus sources. In the end, the information within these events is not meant to be persisted at any real storage, but simply logged (depending on your `log4j` configuration, the logs will be printed in console, a file...).

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s lo logs
The mapping is direct, converting the context data into strings to be written in console, or file...

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

Assuming the log appender is the console, then `NGSITestSink` will log the data within the body as:

```
time=2015-12-10T14:31:49.389CET | lvl=INFO | trans=1429535775-308-0000000000 | srv=vehicles | subsrv=4wheels | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[150] : Starting transaction (1429535775-308-0000000000)
time=2015-12-10T14:31:49.392CET | lvl=INFO | trans=1429535775-308-0000000000 | srv=vehicles | subsrv=4wheels | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[232] : Received data ({  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "attributes" : [          {            "name" : "speed",            "type" : "float",            "value" : "112.9"          },          {            "name" : "oil_level",            "type" : "float",            "value" : "74.6"          }        ],        "type" : "car",        "isPattern" : "false",        "id" : "car1"      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2015-12-10T14:31:49.394CET | lvl=INFO | trans=1429535775-308-0000000000 | srv=vehicles | subsrv=4wheels | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[255] : Event put in the channel (id=1491400742, ttl=10)
time=2015-12-10T14:31:49.485CET | lvl=INFO | trans=1429535775-308-0000000000 | srv=vehicles | subsrv=4wheels | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSITestSink[176] : [test-sink] Persisting data at NGSITestSink. Data (Processing event={Processing headers={recvTimeTs= 1429535775, fiwareService=vehicles, fiwareServicePath=4wheels, destinations=car1_car}, Processing context element={id=car1, type=car}, Processing attribute={name=speed, type=float, value="112.9", metadata=[]}, Processing attribute={name=oil_level, type=float, value="74.6", metadata=[]}})
time=2015-12-10T14:31:49.486CET | lvl=INFO | trans=1429535775-308-0000000000 | srv=vehicles | subsrv=4wheels | function=process | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[178] : Finishing transaction (1429535775-308-0000000000)
```

[Top](#top)

## <a name="section2"></a>Adinistration guide
### <a name="section2.1"></a>Configuration
`NGSITestSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSITestSink</i> |
| channel | yes | N/A ||
| enable_grouping | no | false | <i>true</i> or <i>false</i>. |
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data_model | no | dm-by-entity |  Always <i>dm-by-entity</i>, even if not configured. |
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |

A configuration example could be:

    cygnus-ngsi.sinks = test-sink
    cygnus-ngsi.channels = test-channel
    ...
    cygnus-ngsi.sinks.test-sink.type = com.telefonica.iot.cygnus.sinks.NGSITestSink
    cygnus-ngsi.sinks.test-sink.channel = ckan-channel
    cygnus-ngsi.sinks.test-sink.enable_grouping = false
    cygnus-ngsi.sinks.test-sink.enable_lowercase = false
    cygnus-ngsi.sinks.test-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.test-sink.batch_size = 100
    cygnus-ngsi.sinks.test-sink.batch_timeout = 30
    cygnus-ngsi.sinks.test-sink.batch_ttl = 10

[Top](#top)

### <a name="section2.2"></a>Use cases
Use this sink in order to test if a Cygnus deployment is properly receiving notifications from an Orion Context Broker premise.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About batching
`NGSITestSink` extends `NGSISink`, which provides a batch-based built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Particularly, this is not important for this test sink, but the other sinks will largely benefit from this feature. Please, check the specific sink documentation for more details.

[Top](#top)
