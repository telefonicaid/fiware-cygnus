#<a name="top"></a>OrionSTHSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to MongoDB data structures](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [Hashing based collections](#section2.3.1)
        * [About batching](#section2.3.2)
        * [About `recvTime` and `TimeInstant` metadata](#section2.3.3)
        * [Databases and collections encoding details](#section2.3.4)
* [Implementation details](#section3)
    * [`OrionSTHSink` class](#section3.1)
    * [`MongoBackend` class](#section3.2)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionSTHSink`, or simply `OrionSTHSink` is a sink designed to persist NGSI-like context data events within a MongoDB server in an aggregated way, specifically these measures are computed:

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

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to [`OrionRestHandler`](./orion_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to MongoDB data structures
MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by `OrionSTHSink` each time a Flume event is going to be persisted.

A database called as the `fiware-service` header value within the event is created (if not existing yet).

The context responses/entities within the container are iterated, and a collection is created (if not yet existing) for each unit data. the collection is called as the concatenation of the `fiware-servicePath`_`destination` headers values within the event.

The context attributes within each context response/entity are iterated, and a new Json document is appended to the current collection.

[Top](#top)

###<a name="section1.3"></a>Example
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

Assuming `mongo_username=myuser`, `data_model=dm-by-entity` and  `should_hash=false` as configuration parameters, then `OrionSTHSink` will persist the data within the body as:

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

NOTES:

* `mongo` is the MongoDB CLI for querying the data.
* `sth_` prefix is added by default when no database nor collection prefix is given (see next section for more details).
* This sink adds the original '/' initial character to the `fiware-servicePath`, which was removed by `OrionRESTHandler`.

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`OrionSTHSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.OrionSTHSink |
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
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| data_expiration | no | 0 | Collections will be removed if older than the value specified in seconds. The reference of time is the one stored in the `_id.origin` property. Set to 0 if not wanting this policy. |
| ignore\_white\_spaces | no | true | <i>true</i> if exclusively white space-based attribute values must be ignored, <i>false</i> otherwise. |

A configuration example could be:

    cygnusagent.sinks = sth-sink
    cygnusagent.channels = sth-channel
    ...
    cygnusagent.sinks.sth-sink.type = com.telefonica.iot.cygnus.sinks.OrionSTHSink
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
    cygnusagent.sinks.sth-sink.batch_size = 100
    cygnusagent.sinks.sth-sink.batch_timeout = 30
    cygnusagent.sinks.sth-sink.batch_ttl = 10
    cygnusagent.sinks.sth-sink.data_expiration = 0
    cygnusagent.sinks.sth-sink.ignore_white_spaces = true

[Top](#top)

###<a name="section2.2"></a>Use cases
Use `OrionSTHSink` if you are looking for a Json-based document storage about aggregated data not growing so much in the mid-long term.

[Top](#top)

###<a name="section2.3"></a>Important notes
###<a name="section2.3.1"></a>Hashing based collections
In case the `should_hash` option is set to `true`, the collection names are generated as a concatenation of the `collection_prefix` plus a generated hash plus `.aggr` for the collections of the aggregated data. To avoid collisions in the generation of these hashes, they are forced to be 20 bytes long at least. Once again, the length of the collection name plus the `db_prefix` plus the database name (i.e. the fiware-service) should not be more than 120 bytes using UTF-8 or MongoDB will complain and will not create the collection, and consequently no data would be stored by Cygnus. The hash function used is SHA-512.

In case of using hashes as part of the collection names and to let the user or developer easily recover this information, a collection named `<collection_prefix>_collection_names` is created and fed with information regarding the mapping of the collection names and the combination of concrete services, service paths, entities and attributes.

[Top](#top)

###<a name="section2.3.2"></a>About batching
Despite `OrionSTHSink` allows for batching configuration, it is not true it works with real batches as the rest of sinks. The batching mechanism was designed to accumulate NGSI-like notified data following the configured data model (i.e. by service, service path, entity or attribute) and then perform a single bulk-like insert operation comprising all the accumulated data.

Nevertheless, FIWARE Comet storage aggregates data through updates, i.e. there are no inserts but updates of certain pre-populated collections. Then, these updates implement at MongoDB level the expected aggregations of FIWARE Comet (sum, sum2, max and min).

The problem with such an approach (updates versus inserts) is there is no operation in the Mongo API enabling the update of a batch. As much, there exists a `updateMany` operation, but it is about updating many collections with a single data (the updated collections are those matching the given query).

Thus, `OrionSTHSink` does not implement a real batching mechanism as usual. Please observe the batching accumulation is still valid, since many events may be accumulated and processed at the same time, even in the case of configuring a batch size of 1, a single notification may include several context elements. The difference with regard to the other sinks is the events within the batch will be processed one by one after all.

[Top](#top)

###<a name="section2.3.3"></a>About `recvTime` and `TimeInstant` metadata
By default, `OrionSTHSink` stores the notification reception timestamp. Nevertheless, if a metadata named `TimeInstant` is notified, then such metadata value is used instead of the reception timestamp. This is useful when wanting to persist a measure generation time (which is thus notified as a `TimeInstant` metadata) instead of the reception time.

[Top](#top)

###<a name="section2.3.4"></a>Databases and collections encoding details
`OrionMongoSink` follows the [MongoDB naming restrictions](https://docs.mongodb.org/manual/reference/limits/#naming-restrictions). In a nutshell:

* Database names will have the characters `\`, `/`, `.`, `$`, `"` and ` ` encoded as `_`.
* Collections names will have the characters `$` encoded as `_`.

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`OrionSTHSink` class
`OrionSTHSink` extends `OrionMongoBaseSink`, which as any other NGSI-like sink, extends the base `OrionSink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the MongoDB collection where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `MongoBackend` implementation.

    public void start();

An implementation of `MongoBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionSTHSink()` (contructor), `configure()` and `start()`.

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

[Top](#top)
