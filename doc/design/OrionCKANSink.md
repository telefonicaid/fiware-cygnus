#<a name="top"></a>OrionCKANSink
* [Functionality](#section1)
    * [Mapping Flume events to CKAN data structures](#section1.1)
    * [Example](#section1.2)
* [Configuration](#section2)
* [Use cases](#section3)
* [Implementation details](#section4)
    * [`OrionCKANSink` class](#section4.1)
    * [`CKANBackendImpl` class](#section4.2)
    * [`CKANCache` class](#section4.3)
* [Contact](#section5)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionCKANSink`, or simply `OrionCKANSink` is a sink designed to persist NGSI-like context data events within a [CKAN](http://ckan.org/) server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always [transformed](from_ngsi_events_to_flume_events.md) into internal Flume events at Cygnus sources thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. In the end, the information within these Flume events must be mapped into specific CKAN data structures.

[Top](#top)

###<a name="section1.1"></a>Mapping Flume events to CKAN data structures
[CKAN organizes](http://docs.ckan.org/en/latest/user-guide.html) the data in organizations containing packages or datasets; each one of these packages/datasets contains several resources whose data is finally stored in a PostgreSQL database (CKAN Datastore) or plain files (CKAN Filestore). Such organization is exploited by `OrionCKANSink` each time a Flume event is taken, by performing the following workflow:

1. The bytes within the event's body are parsed and a `NotifyContextRequest` object container is created.
2. According to the [naming conventions](naming_convetions.md), an organization called as the `fiware-service` header values is created (if not existing yet). The same occurs with a package/dataset called as the `fiware-servicePath` header value.
3. The context responses/entities within the container are iterated, and a resource called as the `destination` header value is created (if not yet existing). A datastore associated to the resource is created as well.
4. The context attributes within each context response/entity are iterated, and a new data row is upserted in the datastore related to the resource. The format for this append depends on the configured persistence mode:
    * `row`: A data row is upserted for each notified context attribute. This kind of row will always contain 8 fields:
        * `recvTimeTs`: UTC timestamp expressed in miliseconds.
        * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
        * `entityId`: Notified entity identifier.
        * `entityType`: Notified entity type.
        * `attrName`: Notified attribute name.
        * `attrType`: Notified attribute type.
        * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
        * `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
    * `column`: A single row is upserted for all the notified context attributes. This kind of row will contain two fields per each entity's attribute (one for the value, called `<attrName>`, and other for the metadata, called `<attrName>_md`), plus an additional field about the reception time of the data (`recvTime`).

[Top](#top)

####Important notes regarding the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields rows are upserted for each notified attribute. Nevertheless, the `column` mode may be affected by several rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the Datastore can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

###<a name="section1.2"></a>Example
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

##<a name="section2"></a>Configuration
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

[Top](#top)

##<a name="section3"></a>Use cases
Use `OrionCKANSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

##<a name="section4"></a>Implementation details
###<a name="section4.1"></a>`OrionCKANSink` class
As any other NGSI-like sink, `OrionCKANSink` extends the base `OrionSink`. The methods that are extended are:

    void persist(Map<String, String>, NotifyContextRequest) throws Exception;
    
The context data, already parsed by `OrionSink` in `NotifyContextRequest`, is iterated and persisted in the CKAN backend by means of a `CKANBackendImpl` instance. Header information from the `Map<String, String>` is used to complete the persitence process, such as the timestamp or the destination.
    
    public void start();

`CKANBackendImpl` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionCKANSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section4.2"></a>`CKANBackendImpl` class
This is a convenience backend class for CKAN that extends the `HttpBackend` abstract class (provides common logic for any Http connection-based backend) and implements the `CKANBackend` interface (provides the methods that any CKAN backend must implement). Relevant methods are:

    public void persist(long recvTimeTs, String recvTime, String orgName, String pkgName, String resName, String attrName, String attrType, String attrValue, String attrMd) throws Exception;
    
Persists the context data regarding a single entity's attribute within the datastore associated to the given resource. This resource belongs to the given package/dataset, which in the end belongs to the given organization as well. Other notified attributes will be persisted by using this method, next to current one. This method creates the parts of the hierarchy (organization-package/dataset-resource-datastore) if any of them is missing (row-like mode).

    public void persist(String recvTime, String orgName, String pkgName, String resName, Map<String, String> attrList, Map<String, String> attrMdList) throws Exception
    
Persists the context data regarding all an entity's attributes within the datastore associated to the given resource. This resource belongs to the given package/dataset, which in the end belongs to the given organization as well. Since all the attributes are stored with this operation, no other one is required. This method does not create any part of the hierarchy (organization-pacakge/dataset-resource-datastore), and all of them must be provisioned in advanced (column-like).

[Top](#top)

###<a name="section4.3"></a>`CKANCache` class
This class is used to improve the performance of `OrionCKANSink` by caching information about the already created organizations, packages/datasets and resources (and datastores). `CKANCache` implements the `HttpBackend` interface since its methods are able to interact directly with CKAN API when some element of the hierarchy is not cached.

In detail, this is the workflow when `OrionCKANSink` is combined with `CKANCache`:

1. `OrionCKANSink`, previously to accessing CKAN API (it consumes a lot of computational resources), queries the cache for the data (stored in memory, faster and efficient), in order to know if the different elements of the hierarchy involved in the persistence operation are already created or not.
2. If the element is cached, then a single upsert operation is done against the CKAN API.
3. If the element is not cached, CKAN is queried in order to get the information. If the element was not found, `OrionCKANSink` is informed about that. If the element was found, it is cached for future queries and `OrionCAKNSink` performs an upsert operation against the CKAN API.
4. If the element was not found in the cache nor in CKAN, it is created by `OrionCKANSink`. Then, an upsert operation is performed.

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
