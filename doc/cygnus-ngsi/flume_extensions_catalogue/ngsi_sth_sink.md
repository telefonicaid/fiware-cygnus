#<a name="top"></a>NGSISTHSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to MongoDB data structures](#section1.2)
        * [MongoDB databases naming conventions](#section1.2.1)
        * [MongoDB collections naming conventions](#section1.2.2)
        * [Storing](#section1.2.3)
    * [Example](#section1.3)
        * [Flume event](#section1.3.1)
        * [Database and table names](#section1.3.2)
        * [Storing](#section1.3.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [Hashing based collections](#section2.3.1)
        * [About batching](#section2.3.2)
        * [About `recvTime` and `TimeInstant` metadata](#section2.3.3)
        * [Databases and collections encoding details](#section2.3.4)
* [Implementation details](#section3)
    * [`NGSISTHSink` class](#section3.1)
    * [`MongoBackend` class](#section3.2)
    * [Authentication and authorization](#section3.3)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSISTHSink`, or simply `NGSISTHSink` is a sink designed to persist NGSI-like context data events within a MongoDB server in an aggregated way, specifically these measures are computed:

* For numeric attribute values:
    * Sum of all the samples.
    * Sum of the square value of all the samples.
    * Maximum value among all the samples.
    * Minimum value among all the samples.
* Number of occurrences for string attribute values.

You can get further details on FIWARE Comet and the supported aggregations at [FIWARE Comet Github](https://github.com/telefonicaid/fiware-sth-comet).

Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific HDFS data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](./ngsi_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to MongoDB data structures
MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by `NGSISTHSink` each time a Flume event is going to be persisted.

[Top](#top)

####<a name="section1.2.1"></a>MongoDB databases and collections naming conventions
A database called as the `fiware-service` header value within the event is created (if not existing yet). A configured prefix is added (by default, `sth_`).

It must be said [MongoDB does not accept](https://docs.mongodb.com/manual/reference/limits/#naming-restrictions) `/`, `\`, `.`, `"` and `$` in the collection names, so they will be replaced by underscore, `_`.

[Top](#top)

####<a name="section1.2.2"></a>MongoDB collections naming conventions
The name of these collections depends on the configured data model and analysis mode (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](.ngsi_rest_handler.md)) is used as the name of the collection. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table. The configured prefix is prepended to the collection name, while `.aggr` sufix is appended to it.
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the collections name. The concatenation string is `0x0000`, closely related to the encoding of not allowed characters (see below). If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated. The configured prefix is prepended to the collection name, while `.aggr` sufix is appended to it.
* Data model by attribute (`data_model=dm-by-attribute`). For each entity's attribute, the notified/default FIWARE service path is concatenated to the notified entity ID and type and to the notified attribute name in order to compose the collection name. The concatenation character is `_` (underscore). If the FIWARE service path is the root one (`/`) then only the entity ID and type and the attribute name and type are concatenated.  The configured prefix is prepended to the collection name, while `.aggr` sufix is appended to it.

It must be said [MongoDB does not accept](https://docs.mongodb.com/manual/reference/limits/#naming-restrictions) `$` in the collection names, so it will be replaced by underscore, `_`.

The following table summarizes the table name composition (assuming default `sth_` prefix):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|
| `/` | `sth_/.aggr` | `sth_/<entityId>_<entityType>.aggr` | `sth_/<entityId>_<entityType>_<attrName>.aggr` |
| `/<svcPath>.aggr` | `sth_/<svcPath>.aggr` | `sth_/<svcPath>_<entityId>_<entityType>.aggr` | `sth_/<svcPath>_<entityId>_<entityType>_<attrName>.aggr` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the Flume event.

[Top](#top)

####<a name="section1.2.3"></a>Storing
As said, `NGSISTHSink` has been designed for pre-aggregating certain statistics about entities and their attributes:

* For numeric attribute values:
    * Sum of all the samples.
    * Sum of the square value of all the samples.
    * Maximum value among all the samples.
    * Minimum value among all the samples.
* Number of occurrences for string attribute values.

This is done by changing the <i>usual</i> behaviour of a NGSI-like sink: instead of appending more and more information elements, a set of information elements (in this case, Json documents within the collections) are created once, and updated many.

There will be at least (see below about the <i>origin</i>) a Json document for each <i>resolution</i> handled by `NGSISTHSink`: month, day, hour, minute and second. For each one of these documents, there will be as many <i>offset</i> fields as the resolution denotes, e.g. for a resolution of "day" ther will be 24 offsets, for a resolution of "second" there will be 60 offsets. Initially, these offsets will be setup to 0 (for numeric aggregations), or `{}` for string aggregations, and as long notifications arrive, these values will be updated depending on the resolution and the offset within that resolution the notification was receieved.

Each one of the Json documents (each one for each resolution) will also have an <i>origin</i>, the time for which the aggregated information applies. For instance, for a resolution of "minute" a valid origin could be `2015-03-01T13:00:00.000Z`, meaning the 13th hour of March, the 3rd, 2015. There may be another Json document having a different origin for the same resolution, e.g. `2015-03-01T14:00:00.000Z`, meaning the 14th hour of March, the 3rd, 2015. The origin is stored using UTC time to avoid locale issues.

Finally, each document will save the number of <i>samples</i> that were used for updating it. This is useful when getting values such as the average, which is the <i>sum</i> divided by the number of samples.

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

####<a name="section1.3.2"></a>Database and collection names
A MongoDB database named as the concatenation of the prefix and the notified FIWARE service path, i.e. `sth_vehicles`, will be created.

Regarding the collection names, the MongoDB collection names will be, depending on the configured data model, the following ones:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|
| `/` | `sth_/.aggr` | `sth_/car1_car.aggr` | `sth_/car1_car_speed.aggr`<br>`sth_/car1_car_oil_level.aggr` |
| `/4wheels` | `sth_/4wheels.aggr` | `sth_/4wheels_car1_car.aggr` | `sth_/4wheels_car1_car_speed.aggr`<br>`sth_/4wheels_car1_car_oil_level.aggr` |

[Top](#top)

####<a name="section1.3.3"></a>Storing
Assuming `data_model=dm-by-entity` and all the possible resolutions as configuration parameters (see section [Configuration](#section2.1) for more details), then `NGSISTHSink` will persist the data within the body as:

    $ mongo -u myuser -p
    MongoDB shell version: 2.6.9
    connecting to: test
    > show databases
    admin              (empty)
    local              0.031GB
    sth_vehicles       0.031GB
    test               0.031GB
    > use vehicles
    switched to db vehicles
    > show collections
    sth_/4wheels_car1_car.aggr
    system.indexes
    > db['sth_/4wheels_car1_car.aggr'].find()
    {
        "_id" : { "attrName" : "speed", "origin" : ISODate("2015-04-20T00:00:00Z"), "resolution" : "hour", "range" : "day", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 12, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 23, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "speed", "origin" : ISODate("2015-01-01T00:00:00Z"), "resolution" : "month", "range" : "year", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 1, "sum" : 0, "sum2" : 0, "min" : 0, "max" : 0 },
            ...,
            { "offset" : 3, "samples" : 0, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 11, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "speed", "origin" : ISODate("2015-04-20T12:13:00Z"), "resolution" : "second", "range" : "minute", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 22, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "speed", "origin" : ISODate("2015-04-20T12:00:00Z"), "resolution" : "minute", "range" : "hour", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 13, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "speed", "origin" : ISODate("2015-04-01T00:00:00Z"), "resolution" : "day", "range" : "month", "attrType" : "float" },
        "points" : [
            { "offset" : 1, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 20, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 31, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "oil_level", "origin" : ISODate("2015-04-20T00:00:00Z"), "resolution" : "hour", "range" : "day", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 12, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 23, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "oil_level", "origin" : ISODate("2015-01-01T00:00:00Z"), "resolution" : "month", "range" : "year", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 1, "sum" : 0, "sum2" : 0, "min" : 0, "max" : 0 },
            ...,
            { "offset" : 3, "samples" : 0, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 11, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:13:00Z"), "resolution" : "second", "range" : "minute", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 22, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:00:00Z"), "resolution" : "minute", "range" : "hour", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 13, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "attrName" : "oil_level", "origin" : ISODate("2015-04-01T00:00:00Z"), "resolution" : "day", "range" : "month", "attrType" : "float" },
        "points" : [
            { "offset" : 1, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 20, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 31, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`NGSISTHSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.NGSISTHSink |
| channel | yes | N/A |
| enable_grouping | no | false | <i>true</i> or <i>false</i>. |
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data_model | no | dm-by-entity | <i>dm-by-service-path</i>, <i>dm-by-entity</i> or <dm-by-attribute</i>. <i>dm-by-service</i> is not currently supported. |
| mongo_hosts | no | localhost:27017 | FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run. |
| mongo_username | no | <i>empty</i> | If empty, no authentication is done. |
| mongo_password | no | <i>empty</i> | If empty, no authentication is done. |
| should_hash | no | false | <i>true</i> for collection names based on a hash, <i>false</i> for human redable collections. |
| db_prefix | no | sth_ ||
| collection_prefix | no | sth_ | `system.` is not accepted. |
| resolutions | no | month,day,hour,minute,second | Resolutions for which it is desired to aggregate data. Accepted values are <i>month</i>, <i>day</i>, <i>hour</i>, <i>minute</i> and <i>second</i> separated  by comma. |
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| data_expiration | no | 0 | Collections will be removed if older than the value specified in seconds. The reference of time is the one stored in the `_id.origin` property. Set to 0 if not wanting this policy. |
| ignore\_white\_spaces | no | true | <i>true</i> if exclusively white space-based attribute values must be ignored, <i>false</i> otherwise. |

A configuration example could be:

    cygnusagent.sinks = sth-sink
    cygnusagent.channels = sth-channel
    ...
    cygnusagent.sinks.sth-sink.type = com.telefonica.iot.cygnus.sinks.NGSISTHSink
    cygnusagent.sinks.sth-sink.channel = sth-channel
    cygnusagent.sinks.sth-sink.enable_grouping = false
    cygnusagent.sinks.sth-sink.enable_lowercase = false
    cygnusagent.sinks.sth-sink.data_model = dm-by-entity
    cygnusagent.sinks.sth-sink.mongo_hosts = 192.168.80.34:27017
    cygnusagent.sinks.sth-sink.mongo_username = myuser
    cygnusagent.sinks.sth-sink.mongo_password = mypassword
    cygnusagent.sinks.sth-sink.db_prefix = cygnus_
    cygnusagent.sinks.sth-sink.collection_prefix = cygnus_
    cygnusagent.sinks.sth-sink.should_hash = false
    cygnusagent.sinks.sth-sink.resolutions = month,day
    cygnusagent.sinks.sth-sink.batch_size = 100
    cygnusagent.sinks.sth-sink.batch_timeout = 30
    cygnusagent.sinks.sth-sink.batch_ttl = 10
    cygnusagent.sinks.sth-sink.data_expiration = 0
    cygnusagent.sinks.sth-sink.ignore_white_spaces = true

[Top](#top)

###<a name="section2.2"></a>Use cases
Use `NGSISTHSink` if you are looking for a Json-based document storage about aggregated data not growing so much in the mid-long term.

[Top](#top)

###<a name="section2.3"></a>Important notes
###<a name="section2.3.1"></a>Hashing based collections
In case the `should_hash` option is set to `true`, the collection names are generated as a concatenation of the `collection_prefix` plus a generated hash plus `.aggr` for the collections of the aggregated data. To avoid collisions in the generation of these hashes, they are forced to be 20 bytes long at least. Once again, the length of the collection name plus the `db_prefix` plus the database name (i.e. the fiware-service) should not be more than 120 bytes using UTF-8 or MongoDB will complain and will not create the collection, and consequently no data would be stored by Cygnus. The hash function used is SHA-512.

In case of using hashes as part of the collection names and to let the user or developer easily recover this information, a collection named `<collection_prefix>_collection_names` is created and fed with information regarding the mapping of the collection names and the combination of concrete services, service paths, entities and attributes.

[Top](#top)

###<a name="section2.3.2"></a>About batching
Despite `NGSISTHSink` allows for batching configuration, it is not true it works with real batches as the rest of sinks. The batching mechanism was designed to accumulate NGSI-like notified data following the configured data model (i.e. by service, service path, entity or attribute) and then perform a single bulk-like insert operation comprising all the accumulated data.

Nevertheless, FIWARE Comet storage aggregates data through updates, i.e. there are no inserts but updates of certain pre-populated collections. Then, these updates implement at MongoDB level the expected aggregations of FIWARE Comet (sum, sum2, max and min).

The problem with such an approach (updates versus inserts) is there is no operation in the Mongo API enabling the update of a batch. As much, there exists a `updateMany` operation, but it is about updating many collections with a single data (the updated collections are those matching the given query).

Thus, `NGSISTHSink` does not implement a real batching mechanism as usual. Please observe the batching accumulation is still valid, since many events may be accumulated and processed at the same time, even in the case of configuring a batch size of 1, a single notification may include several context elements. The difference with regard to the other sinks is the events within the batch will be processed one by one after all.

[Top](#top)

###<a name="section2.3.3"></a>About `recvTime` and `TimeInstant` metadata
By default, `NGSISTHSink` stores the notification reception timestamp. Nevertheless, if a metadata named `TimeInstant` is notified, then such metadata value is used instead of the reception timestamp. This is useful when wanting to persist a measure generation time (which is thus notified as a `TimeInstant` metadata) instead of the reception time.

[Top](#top)

###<a name="section2.3.4"></a>Databases and collections encoding details
`NGSIMongoSink` follows the [MongoDB naming restrictions](https://docs.mongodb.org/manual/reference/limits/#naming-restrictions). In a nutshell:

* Database names will have the characters `\`, `/`, `.`, `$`, `"` and ` ` encoded as `_`.
* Collections names will have the characters `$` encoded as `_`.

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`NGSISTHSink` class
`NGSISTHSink` extends `NGSIMongoBaseSink`, which as any other NGSI-like sink, extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the MongoDB collection where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `MongoBackend` implementation.

    public void start();

An implementation of `MongoBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSISTHSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section3.2"></a>`MongoBackend` class
This is a convenience backend class for MongoDB that provides methods to persist the context data both in raw of aggregated format. Relevant methods regarding raw format are:

    public void createDatabase(String dbName) throws Exception;

Creates a database, given its name, if not exists.

    public void createCollection(String dbName, String collectionName) throws Exception;

Creates a collection, given its name, if not exists in the given database.

    public void insertContextDataRaw(String dbName, String collectionName, long recvTimeTs, String recvTime, String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd) throws Exception;

Updates or inserts (depending if the document already exists or not) a set of documents in the given collection within the given database. Such a set of documents contains all the information regarding current and past notifications (historic) for a single attribute. a set of documents is managed since historical data is stored using several resolutions and range combinations (second-minute, minute-hour, hour-day, day-month and month-year). See FIWARE Comet at [Github](https://github.com/telefonicaid/IoT-STH/blob/develop/README.md) for more details.

Nothing special is done with regards to the encoding. Since Cygnus generally works with UTF-8 character set, this is how the data is written into the collections. It will responsability of the MongoDB client to convert the bytes read into UTF-8.

[Top](#top)

###<a name="section3.3"></a>Authentication and authorization
Current implementation of `NGSIMongoSink` relies on the username and password credentials created at the MongoDB endpoint.

[Top](#top)
