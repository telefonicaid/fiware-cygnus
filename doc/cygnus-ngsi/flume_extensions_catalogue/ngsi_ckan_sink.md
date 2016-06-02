#<a name="top"></a>NGSICKANSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to CKAN data structures](#section1.2)
        * [Organizations naming conventions](#section1.2.1)
        * [Package/dataset naming conventions](#section1.2.2)
        * [Resource naming conventions](#section1.2.3)
        * [Row-like storing](#section1.2.4)
        * [Column-like storing](#section1.2.5)
    * [Example](#section1.3)
        * [Flume event](#section1.3.1)
        * [Organization, dataset and resource names](#section1.3.2)
        * [Raw-like storing](#section1.3.3)
        * [Column-like storing](#section1.3.4)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the persistence mode](#section2.3.1)
        * [About batching](#section2.3.2)
* [Programmers guide](#section3)
    * [`NGSICKANSink` class](#section3.1)
* [Annexes](#section4)
    * [Provisioning a CKAN resource for the column mode](#section4.1)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSICKANSink`, or simply `NGSICKANSink` is a sink designed to persist NGSI-like context data events within a [CKAN](http://ckan.org/) server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific CKAN data structures.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](./ngsi_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to CKAN data structures
[CKAN organizes](http://docs.ckan.org/en/latest/user-guide.html) the data in organizations containing packages or datasets; each one of these packages/datasets contains several resources whose data is finally stored in a PostgreSQL database (CKAN Datastore) or plain files (CKAN Filestore). Such organization is exploited by `NGSICKANSink` each time a Flume event is going to be persisted.

[Top](#top)

####<a name="section1.2.1"></a>Organizations naming conventions
An organization named as the notified `fiware-service` header value (or, in absence of such a header, the defaulted value for the FIWARE service) is created (if not existing yet).

[Top](#top)

####<a name="section1.2.2"></a>Packages/datasets naming conventions
A package/dataset named as the concatenation of the notified `fiware-service` and `fiware-servicePath` header values (or, in absence of such headers, the defaulted value for the FIWARE service and service path) is created (if not existing yet) in the above organization.

Please observe if the notified/defaulted FIWARE service path is the root one, i.e. `/`, then the package/dataset name is equals to the organization name.

The concatenation character is the underscore, `_`.

[Top](#top)

####<a name="section1.2.3"></a>Resources naming conventions
CKAN resources follow a single data model (see the [Configuration](#section2.1) section for more details), i.e. per entity. Thus, a resource name always take the entity ID and type, concatenated by the underscore character, `_`. Such a name is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the Flume event.

It must be noticed a CKAN Datastore (and a viewer) is also created and associated to the resource above. This datastore, which in the end is a PostgreSQL table, will hold the persisted data.

[Top](#top)

####<a name="section1.2.4"></a>Row-like storing
Regarding the specific data stored within the datastore associated to the resource, if `attr_persistence` parameter is set to `row` (default storing mode) then the notified data is stored attribute by attribute, composing an insert for each one of them. Each insert contains the following fields:

* `recvTimeTs`: UTC timestamp expressed in miliseconds.
* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `attrName`: Notified attribute name.
* `attrType`: Notified attribute type.
* `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
* `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).

[Top](#top)

####<a name="section1.2.4"></a>Column-like storing
Regarding the specific data stored within the datastore associated to the resource, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: The notified one or the default one.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
*  For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.

[Top](#top)

###<a name="section1.3"></a>Example
####<a name="section1.3.1"></a>Flume event
Assuming the following Flume event is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	         content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         ttl=10,
	         fiware-service=vehicles,
	         fiware-servicepath=4wheels,
	         notified-entities=car1_car
	         notified-servicepaths=4wheels
	         grouped-entities=car1_car
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
    
[Top](#top)

####<a section="1.3.2"></a>Organization, dataset and resource names

[Top](#top)

####<a section="1.3.2"></a>Row-like storing
Assuming `attr_persistence=row` as configuration parameter, then `NGSICKANSink` will persist the data within the body as:

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
                    "type": "text",
                    "id": "fiwareServicePath"
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
                    "entityId": "car1",
                    "entityType": "car",
                    "fiwareServicePath": "4wheels",
                    "attrType": "float",
                    "recvTime": "2015-04-20T12:13:22.41.124Z",
                    "recvTimeTs": 1429535775,
                    "attrMd": null,
                    "attrValue": "112.9",
                    "attrName": "speed",
                    "_id": 1
                },
                {
                    "entityId": "car1",
                    "entityType": "car",
                    "fiwareServicePath": "4wheels",
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

[Top](#top)

####<a section="1.3.2"></a>Column-like storing
If `attr_persistence=colum` then `NGSICKANSink` will persist the data within the body as:

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
                    "type": "text",
                    "fiwareServicePath": "4wheels"
                },
                {
                    "type": "text",
                    "entityId": "car1"
                },
                {
                    "type": "text",
                    "entityType": "car"
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
                    "fiwareServicePath": "4wheels",
                    "entityId": "car1",
                    "entityType": "car",
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

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`NGSICKANSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSICKANSink</i> |
| channel | yes | N/A |
| enable_grouping | no | false | <i>true</i> or <i>false</i>. ||
| data_model | no | dm-by-entity |  Always <i>dm-by-entity</i>, even if not configured. ||
| attr_persistence | no | row | <i>row</i> or <i>column.</i>|
| ckan_host | no | localhost | FQDN/IP address where the CKAN server runs. ||
| ckan_port | no | 80 ||
| ssl | no | false ||
| api_key | yes | N/A ||
| orion_url | no | http://localhost:1026 | To be put as the filestore URL. |
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |

A configuration example could be:

    cygnusagent.sinks = ckan-sink
    cygnusagent.channels = ckan-channel
    ...
    cygnusagent.sinks.ckan-sink.type = com.telefonica.iot.cygnus.sinks.NGSICKANSink
    cygnusagent.sinks.ckan-sink.channel = ckan-channel
    cygnusagent.sinks.ckan-sink.enable_grouping = false
    cygnusagent.sinks.ckan-sink.data_model = dm-by-entity
    cygnusagent.sinks.ckan-sink.attr_persistence = column
    cygnusagent.sinks.ckan-sink.ckan_host = 192.168.80.34
    cygnusagent.sinks.ckan-sink.ckan_port = 80
    cygnusagent.sinks.ckan-sink.ssl = false
    cygnusagent.sinks.ckan-sink.api_key = myapikey
    cygnusagent.sinks.ckan-sink.orion_url = http://localhost:1026
    cygnusagent.sinks.ckan-sink.batch_size = 100
    cygnusagent.sinks.ckan-sink.batch_timeout = 30
    cygnusagent.sinks.ckan-sink.batch_ttl = 10

[Top](#top)

###<a name="section2.2"></a>Use cases
Use `NGSICKANSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

###<a name="section2.3"></a>Important notes
####<a name="section2.3.1"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields rows are upserted for each notified attribute. Nevertheless, the `column` mode may be affected by several rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the Datastore cannot be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

Please check the [Annexes](#section4) in order to know how to provision a resource for the column mode.

[Top](#top)

####<a name="section2.3.2"></a>About batching
As explained in the [programmers guide](#section3), `NGSICKANSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows exteding classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same CKAN resource. If processing the events one by one, we would need 100 inserts into CKAN; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination CKAN resource. In the worst case, the whole 100 entities will be about 100 different entities (100 different CKAN resources), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `NGSICKANSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](../operation/performance_tuning_tips.md).

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`NGSICKANSink` class
As any other NGSI-like sink, `NGSICKANSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contanins a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the CKAN resource where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `CKANBackend` implementation.

    public void start();

An implementation of `CKANBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSICKANSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

##<a name="section4"></a>Annexes

###<a name="section4.1"></a>Provisioning a CKAN resource for the column mode
This section is built upon the assumption you are familiar with the CKAN API. If not, please have a look on [it](http://docs.ckan.org/en/latest/api/).

First of all, you'll need a CKAN organization and package/dataset before creating a resource and an associated datastore in order to persist the data.

Creating an organization, let's say in [`demo.ckan.org`](http://demo.ckan.org/) for the sake of demonstration, but should be a CKAN deployment of yours; the organization name is `service`, because our entity will be in that FIWARE service:

```
$ curl -X POST "http://demo.ckan.org/api/3/action/organization_create" -d '{"name":"service"}' -H "Authorization: xxxxxxxx"
```

Creating a package/dataset within the above organization; the package name is `service_test`, because our entity will be in the FIWARE service `service` and in the FIWARE service path service\_test:

```
$ curl -X POST "http://demo.ckan.org/api/3/action/package_create" -d '{"name":"service_test","owner_org":"service"}' -H "Authorization: xxxxxxxx"
```

Creating a resource within the above package/dataset (the package ID is given in the response to the above package creation request); the name of the resource is `room1_room` because the entity ID will be `room1` and its type `room`:

```
$ curl -X POST "http://demo.ckan.org/api/3/action/resource_create" -d '{"name":"room1_room","url":"none","format":"","package_id":"d35fca28-732f-4096-8376-944563f175ba"}' -H "Authorization: xxxxxxxx"
```

Finally, creating a datastore associated to the above resource and suitable for receiving Cgynus data in column mode (the resource ID is given in the response to the above resource creation request):

```
$ curl -X POST "http://demo.ckan.org/api/3/action/datastore_create" -d '{"fields":[{"id":"recvTime","type":"text"}, {"id":"fiwareServicePath","type":"text"}, {"id":"entityId","type":"text"}, {"id":"entityType","type":"text"}, {"id":"temperature","type":"float"}, {"id":"temperature_md","type":"json"}],"resource_id":"48c120df-5bcd-48c7-81fa-8ecf4e4ef9d7","force":"true"}' -H "Authorization: xxxxxxxx"
```

Now, Cygnus is able to persist data for an entity with ID `room1` of type `room` in the `service` service, `service_test` service path:

```
time=2016-04-26T15:54:45.753CEST | lvl=INFO | corr=b465ffb8-710f-4cd3-9573-dc3799f774f9 | trans=b465ffb8-710f-4cd3-9573-dc3799f774f9 | svc=service | subsvc=/service_test | function=getEvents | comp=cygnusagent | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[240] : Starting internal transaction (b465ffb8-710f-4cd3-9573-dc3799f774f9)
time=2016-04-26T15:54:45.754CEST | lvl=INFO | corr=b465ffb8-710f-4cd3-9573-dc3799f774f9 | trans=b465ffb8-710f-4cd3-9573-dc3799f774f9 | svc=service | subsvc=/service_test | function=getEvents | comp=cygnusagent | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[256] : Received data ({  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "attributes" : [          {            "name" : "temperature",            "type" : "centigrade",            "value" : "26.5"          }        ],        "type" : "room",        "isPattern" : "false",        "id" : "room1"      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-04-26T15:55:07.843CEST | lvl=INFO | corr=b465ffb8-710f-4cd3-9573-dc3799f774f9 | trans=b465ffb8-710f-4cd3-9573-dc3799f774f9 | svc=service | subsvc=/service_test | function=processNewBatches | comp=cygnusagent | msg=com.telefonica.iot.cygnus.sinks.NGSISink[342] : Batch accumulation time reached, the batch will be processed as it is
time=2016-04-26T15:55:07.844CEST | lvl=INFO | corr=b465ffb8-710f-4cd3-9573-dc3799f774f9 | trans=b465ffb8-710f-4cd3-9573-dc3799f774f9 | svc=service | subsvc=/service_test | function=processNewBatches | comp=cygnusagent | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-04-26T15:55:07.846CEST | lvl=INFO | corr=b465ffb8-710f-4cd3-9573-dc3799f774f9 | trans=b465ffb8-710f-4cd3-9573-dc3799f774f9 | svc=service | subsvc=/service_test | function=persistAggregation | comp=cygnusagent | msg=com.telefonica.iot.cygnus.sinks.NGSICKANSink[419] : [ckan-sink] Persisting data at OrionCKANSink (orgName=service, pkgName=service_test, resName=room1_room, data={"recvTime": "2016-04-26T13:54:45.756Z","fiwareServicePath": "/service_test","entityId": "room1","entityType": "room","temperature": "26.5"})
time=2016-04-26T15:55:08.948CEST | lvl=INFO | corr=b465ffb8-710f-4cd3-9573-dc3799f774f9 | trans=b465ffb8-710f-4cd3-9573-dc3799f774f9 | svc=service | subsvc=/service_test | function=processNewBatches | comp=cygnusagent | msg=com.telefonica.iot.cygnus.sinks.NGSISink[400] : Finishing internal transaction (b465ffb8-710f-4cd3-9573-dc3799f774f9)
```

The insertion can be checked through the CKAN API as well:

```
$ curl -X POST "http://demo.ckan.org/api/3/action/datastore_search" -d '{"resource_id":"48c120df-5bcd-48c7-81fa-8ecf4e4ef9d7"}' -H "Authorization: xxxxxxxx"
```

[Top](#top)
