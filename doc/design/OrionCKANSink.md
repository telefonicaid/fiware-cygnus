#<a name="top"></a>OrionCKANSink
* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to CKAN data structures](#section1.2)
    * [Example](#section1.3)
* [Users guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the persistence mode](#section2.3.1)
        * [About batching](#section2.3.2)
* [Programmers guide](#section3)
    * [`OrionCKANSink` class](#section3.1)
    * [`CKANBackendImpl` class](#section3.2)
    * [`CKANCache` class](#section3.3)
* [Reporting issues and contact information](#section4)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionCKANSink`, or simply `OrionCKANSink` is a sink designed to persist NGSI-like context data events within a [CKAN](http://ckan.org/) server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific CKAN data structures.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

Since this is a common task done by Cygnus independently of the final backend, it is documented in [this](from_ngsi_events_to_flume_events.md) other independent document.

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to CKAN data structures
[CKAN organizes](http://docs.ckan.org/en/latest/user-guide.html) the data in organizations containing packages or datasets; each one of these packages/datasets contains several resources whose data is finally stored in a PostgreSQL database (CKAN Datastore) or plain files (CKAN Filestore). Such organization is exploited by `OrionCKANSink` each time a Flume event is going to be persisted.

According to the [naming conventions](naming_convetions.md), an organization called as the `fiware-service` header values is created (if not existing yet). The same occurs with a package/dataset named as the `fiware-servicePath` header value.

The context responses/entities within the container are iterated, and a resource named as the `destination` header value is created (if not yet existing). A datastore associated to the resource is created as well.

The context attributes within each context response/entity are iterated, and a new data row is upserted in the datastore related to the resource. The format for this append depends on the configured persistence mode:

* `row`: A data row is upserted for each notified context attribute. This kind of row will always contain 8 fields:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
    * `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
* `column`: A single row is upserted for all the notified context attributes. This kind of row will contain two fields per each entity's attribute (one for the value, called `<attrName>`, and other for the metadata, called `<attrName>_md`), plus four additional fields:
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `fiwareservicePath`: The notified one or the default one.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.

[Top](#top)

###<a name="section1.3"></a>Example
Assuming the following Flume event is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	        content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         ttl=10,
	         notified-service=vehicles,
	         notified-servicepath=4wheels,
	         default-destination=car1_car
	         default-servicepaths=4wheels
	         grouped-destination=car1_car
	         grouped-servicepath=4wheels
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

Assuming `api_key=myapikey` and `attr_persistence=row` as configuration parameter, then `OrionCKANSink` will persist the data within the body as:

    $ curl -s -S -H "Authorization: myapikey" "http://192.168.80.34:80/api/3/action/datastore_search?resource_id=3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7"
    {
        "help": "Search a DataStore resource...",
        "success": true,
        "result": {
            "resource_id": "3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7",
            "fields": [
                {
                    "type": "int4",
                    "id": "_id"
                },
                {
                    "type": "int4",
                    "id": "recvTimeTs"
                },
                {
                    "type": "timestamp",
                    "id": "recvTime"
                },
                {
                    "id": "entityId",
                    "type": "text"
                },
                {
                    "id": "entityType",
                    "type": "text"
                },
                {
                    "type": "text",
                    "id": "attrName"
                },
                {
                    "type": "text",
                    "id": "attrType"
                },
                {
                    "type": "json",
                    "id": "attrValue"
                },
                {
                    "type": "json",
                    "id": "attrMd"
                }
            ],
            "records": [
                {
                    "attrType": "float",
                    "recvTime": "2015-04-20T12:13:22.41.124Z",
                    "recvTimeTs": 1429535775,
                    "attrMd": null,
                    "attrValue": "112.9",
                    "attrName": "speed",
                    "_id": 1
                },
                {
                    "attrType": "float",
                    "recvTime": "2015-04-20T12:13:22.41.124Z",
                    "recvTimeTs": 1429535775,
                    "attrMd": null,
                    "attrValue": "74.6",
                    "attrName": "oil_level",
                    "_id": 2
                }
            ],
            "_links": {
                "start": "/api/3/action/datastore_search?resource_id=3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7",
                "next": "/api/3/action/datastore_search?offset=100&resource_id=3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7"
            },
            "total": 2
        }
    }

If `attr_persistence=colum` then `OrionCKANSink` will persist the data within the body as:

    $ curl -s -S -H "Authorization: myapikey" "http://130.206.83.8:80/api/3/action/datastore_search?resource_id=611417a4-8196-4faf-83bc-663c173f6986"
    {
        "help": "Search a DataStore resource...",
        "success": true,
        "result": {
            "resource_id": "611417a4-8196-4faf-83bc-663c173f6986",
            "fields": [
                {
                    "type": "int4",
                    "id": "_id"
                },
                {
                    "type": "timestamp",
                    "id": "recvTime"
                },
                {
                    "type": "json",
                    "id": "speed"
                },
                {
                    "type": "json",
                    "id": "speed_md"
                },
                {
                    "type": "json",
                    "id": "oil_level"
                },
                {
                    "type": "json",
                    "id": "oil_level_md"
                }
            ],
            "records": [
                {
                    "recvTime": "2015-04-20T12:13:22.41.124Z",
                    "speed": "112.9",
                    "speed_md": null,
                    "oil_level": "74.6",
                    "oil_level_md": null,
                    "_id": 1
                }
            ],
            "_links": {
                "start": "/api/3/action/datastore_search?resource_id=611417a4-8196-4faf-83bc-663c173f6986",
                "next": "/api/3/action/datastore_search?offset=100&resource_id=611417a4-8196-4faf-83bc-663c173f6986"
            },
            "total": 1 
        }
    }

NOTE: `curl` is a Unix command allowing for interacting with REST APIs such as the exposed by CKAN.

[Top](#top)

##<a name="section2"></a>Users guide
###<a name="section2.1"></a>Configuration
`OrionCKANSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionCKANSink</i> |
| channel | yes | N/A |
| enable_grouping | no | false | <i>true</i> or <i>false</i> |
| ckan_host | no | localhost | FQDN/IP address where the CKAN server runs |
| ckan_port | no | 80 |
| ssl | no | false |
| api_key | yes | N/A |
| attr_persistence | no | row | <i>row</i> or <i>column</i>
| orion_url | no | http://localhost:1026 | To be put as the filestore URL |
| batch_size | no | 1 | Number of events accumulated before persistence |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is |

A configuration example could be:

    cygnusagent.sinks = ckan-sink
    cygnusagent.channels = ckan-channel
    ...
    cygnusagent.sinks.ckan-sink.type = com.telefonica.iot.cygnus.sinks.OrionCKANSink
    cygnusagent.sinks.ckan-sink.channel = ckan-channel
    cygnusagent.sinks.ckan-sink.enable_grouping = false
    cygnusagent.sinks.ckan-sink.ckan_host = 192.168.80.34
    cygnusagent.sinks.ckan-sink.ckan_port = 80
    cygnusagent.sinks.ckan-sink.ssl = false
    cygnusagent.sinks.ckan-sink.api_key = myapikey
    cygnusagent.sinks.ckan-sink.attr_persistence = column
    cygnusagent.sinks.ckan-sink.orion_url = http://localhost:1026
    cygnusagent.sinks.ckan-sink.batch_size = 100
    cygnusagent.sinks.ckan-sink.batch_timeout = 30

[Top](#top)

###<a name="section2.2"></a>Use cases
Use `OrionCKANSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

###<a name="section2.3"></a>Important notes
####<a name="section2.3.1"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields rows are upserted for each notified attribute. Nevertheless, the `column` mode may be affected by several rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the Datastore cannot be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

####<a name="section2.3.2"></a>About batching
As explained in the [programmers guide](#section3), `OrionCKANSink` extends `OrionSink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows exteding classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same CKAN resource. If processing the events one by one, we would need 100 inserts into CKAN; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination CKAN resource. In the worst case, the whole 100 entities will be about 100 different entities (100 different CKAN resources), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `OrionCKANSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](../operation/performance_tuning_tips.md).

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`OrionCKANSink` class
As any other NGSI-like sink, `OrionCKANSink` extends the base `OrionSink`. The methods that are extended are:

    void persistBatch(Batch defaultEvents, Batch groupedEvents) throws Exception;
    
A `Batch` contanins a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the CKAN resource where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `CKANBackend` implementation. There are two sets of events, default and grouped ones, because depending on the sink configuration the default or the grouped notified destination and fiware servicePath are used.
    
    public void start();

An implementation of `CKANBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionCKANSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section3.2"></a>`CKANBackendImpl` class
This is a convenience backend class for CKAN that extends the `HttpBackend` abstract class (provides common logic for any Http connection-based backend) and implements the `CKANBackend` interface (provides the methods that any CKAN backend must implement). Relevant methods are:

    public void persist(String orgName, String pkgName, String resName, String aggregation) throws Exception;
    
Persists the aggregated context data regarding a single entity's attribute (row mode) or a full list of attributes (column mode) within the datastore associated to the given resource. This resource belongs to the given package/dataset, which in the end belongs to the given organization as well. This method creates the parts of the hierarchy (organization, package/dataset, resource and datastore) if any of them is missing.

[Top](#top)

###<a name="section3.3"></a>`CKANCache` class
This class is used to improve the performance of `OrionCKANSink` by caching information about the already created organizations, packages/datasets and resources (and datastores). `CKANCache` implements the `HttpBackend` interface since its methods are able to interact directly with CKAN API when some element of the hierarchy is not cached.

In detail, this is the workflow when `OrionCKANSink` is combined with `CKANCache`:

1. `OrionCKANSink`, previously to accessing CKAN API (it consumes a lot of computational resources), queries the cache for the data (stored in memory, faster and efficient), in order to know if the different elements of the hierarchy involved in the persistence operation are already created or not.
2. If the element is cached, then a single upsert operation is done against the CKAN API.
3. If the element is not cached, CKAN is queried in order to get the information. If the element was not found, `OrionCKANSink` is informed about that. If the element was found, it is cached for future queries and `OrionCAKNSink` performs an upsert operation against the CKAN API.
4. If the element was not found in the cache nor in CKAN, it is created by `OrionCKANSink`. Then, an upsert operation is performed.

[Top](#top)

##<a name="section4"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](german.torodelvalle@telefonica.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.

[Top](#top)
