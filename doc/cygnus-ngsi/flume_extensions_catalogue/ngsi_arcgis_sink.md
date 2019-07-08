# <a name="top"></a>NGSIArcGisSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to Arcgis](#section1.2)
        * [ArcGis layers naming conventions](#section1.2.1)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Important notes](#section2.2)
        * [About batching](#section2.2.1)
* [Programmers guide](#section3)
    * [`NGSIArcGisSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIArcGisSink` is a sink designed to persist NGSI-like context data events within a [ArcGis](https://www.arcgis.com/home/index.html). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific ArcGis structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to ArcGis
ArcGis saves the data in its databases and you can watch this information in the Arcgis's maps, Such organization is exploited by `NGSIArcGisSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>ArcGis databases naming conventions
A layers named as the notified `fiware-service-path` header value, you must create it before sending entities.

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

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIArcGisSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIArcGisSink</i> |
| channel | yes | N/A ||
| enable_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| arcgis\_url | yes | N/A | https://{url\_host}/{id\_arcgis}/arcgis/rest/services|
| arcgis\_username | yes | N/A |  |
| arcgis\_password | yes | N/A |  |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |


A configuration example could be:

    cygnus-ngsi.sinks = arcgis-sink
    cygnus-ngsi.channels = arcgis-channel
    ...
    cygnus-ngsi.sinks.arcgis-sink.type = com.telefonica.iot.cygnus.sinks.NGSIArcGisSink
    cygnus-ngsi.sinks.arcgis-sink.channel = arcgis-channel
    cygnus-ngsi.sinks.arcgis-sink.enable_encoding = false
    cygnus-ngsi.sinks.arcgis-sink.enable_grouping = false
    cygnus-ngsi.sinks.arcgis-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.arcgis-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.arcgis-sink.arcgis_host = https://arcgis.com/UsuarioArcgis/arcgis/rest/services
    cygnus-ngsi.sinks.arcgis-sink.arcgis_username = myuser
    cygnus-ngsi.sinks.arcgis-sink.arcgis_password = mypassword
    cygnus-ngsi.sinks.arcgis-sink.batch_size = 10
    cygnus-ngsi.sinks.arcgis-sink.batch_timeout = 30
    cygnus-ngsi.sinks.arcgis-sink.batch_ttl = 10

[Top](#top)

### <a name="section2.2"></a>Important notes


#### <a name="section2.2.1"></a>About batching
As explained in the [programmers guide](#section3), `NGSIArcGisSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 10 `NGSIEvent`s. In the best case, all these events regard to the same type of entity, which means all the data within them will be persisted in the same ArcGis layer. If processing the events one by one, we would need 10 inserts into ArcGis; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique type of entity, and many entities may be involved within a batch. 

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIArcGisSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)


## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIArcGisSink` class
As any other NGSI-like sink, `NGSIArcGisSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration,
          CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the ArcGis where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `Arcgis` implementation.

    public void start();

This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIArcGisSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIArcGisSink` relies on the username and password credentials created at the ArcGis endpoint.

[Top](#top)
