#<a name="top"></a>OrionSTHSink
* [Functionality](#section1)
    * [Mapping Flume events to MongoDB data structures](#section1.1)
    * [Example](#section1.2)
* [Configuration](#section2)
* [Use cases](#section3)
* [Implementation details](#section4)
    * [`OrionSTHSink` class](#section4.1)
    * [`MongoBackend` class](#section4.2)
* [Contact](#section5)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionSTHSink`, or simply `OrionSTHSink` is a sink designed to persist NGSI-like context data events within a MongoDB server in an aggregated way. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always [transformed](from_ngsi_events_to_flume_events.md) into internal Flume events at Cygnus sources thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. In the end, the information within these Flume events must be mapped into specific MongoDB data structures.

[Top](#top)

###<a name="section1.1"></a>Mapping Flume events to MongoDB data structures
MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by `OrionSTHSink` each time a Flume event is taken, by performing the following workflow:

1. The bytes within the event's body are parsed and a `NotifyContextRequest` object container is created.
2. A database called as the `fiware-service` header value within the event is created (if not existing yet).
3. The context responses/entities within the container are iterated, and a collection is created (if not yet existing) for each unit data. The unit data depends on the chosen data model (see the configuration section):
    * <i>collection-per-service-path</i>: the collection is called as the `fiware-servicePath` header value within the event.
    * <i>collection-per-entity</i>: the collection is called as the concatenation of the `fiware-servicePath`_`destination` headers values within the event.
    * <i>collection-per-attribute</i>: the collection is called as the concatenation of the `fiware-servicePath`\_`destination`\_`attrName`.
4. The context attributes within each context response/entity are iterated, and a new Json document is appended to the current collection.

[Top](#top)

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

Assuming `mongo_username=myuser` and `data_model=collection-per-service-path` as configuration parameters, then `OrionSTHSink` will persist the data within the body as:

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
    sth_/4wheels.aggr
    sth_/4wheels_car1_car.aggr
    sth_/4wheels_car1_car_speed.aggr
    system.indexes
    > db['sth_/4wheels.aggr'].find()
    { 
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "hour", "range" : "day", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 12, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 23, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "sepeed", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "month", "range" : "year", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 1, "sum" : 0, "sum2" : 0, "min" : 0, "max" : 0 },
            ...,
            { "offset" : 3, "samples" : 0, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 11, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "second", "range" : "minute", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 22, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "minute", "range" : "hour", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 13, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "day", "range" : "month", "attrType" : "float" },
        "points" : [
            { "offset" : 1, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 20, "samples" : 1, "sum" : 112.9, "sum2" : 12746.41, "min" : 112.9, "max" : 112.9 },
            ...,
            { "offset" : 31, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    { 
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "hour", "range" : "day", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 12, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 23, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "month", "range" : "year", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 1, "sum" : 0, "sum2" : 0, "min" : 0, "max" : 0 },
            ...,
            { "offset" : 3, "samples" : 0, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 11, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "second", "range" : "minute", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 22, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "minute", "range" : "hour", "attrType" : "float" },
        "points" : [
            { "offset" : 0, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity },
            ...,
            { "offset" : 13, "samples" : 1, "sum" : 74.6, "sum2" : 5565.16, "min" : 74.6, "max" : 74.6 },
            ...,
            { "offset" : 59, "samples" : 0, "sum" : 0, "sum2" : 0, "min" : Infinity, "max" : -Infinity }
        ]
    }
    {
        "_id" : { "entityId" : "car1", "entityType" : "car", "attrName" : "oil_level", "origin" : ISODate("2015-04-20T12:13:22.41Z"), "resolution" : "day", "range" : "month", "attrType" : "float" },
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

##<a name="section2"></a>Configuration
`OrionSTHSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.OrionMongoSink |
| channel | yes | N/A |
| mongo_hosts | no | localhost:27017 | FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run
| mongo_username | no | <i>empty</i> | If empty, no authentication is done |
| mongo_password | no | <i>empty</i> | If empty, no authentication is done |
| data_model | no | collection-per-entity | Under study |
| db_prefix | no | sth_ | Under study |
| collection_prefix | no | sth_ | Under study |

A configuration example could be:

    cygnusagent.sinks = sth-sink
    cygnusagent.channels = sth-channel
    ...
    cygnusagent.sinks.sth-sink.type = com.telefonica.iot.cygnus.sinks.OrionMongoSink
    cygnusagent.sinks.sth-sink.channel = sth-channel
    cygnusagent.sinks.sth-sink.mongo_hosts = 192.168.80.34:27017
    cygnusagent.sinks.sth-sink.mongo_username = myuser
    cygnusagent.sinks.sth-sink.mongo_password = mypassword
    cygnusagent.sinks.sth-sink.data_model = collection-per-entity
    cygnusagent.sinks.sth-sink.db_prefix = cygnus_
    cygnusagent.sinks.sth-sink.collection_prefix = cygnus_

[Top](#top)

##<a name="section3"></a>Use cases
Use `OrionSTHSink` if you are looking for a Json-based document storage about aggregated data not growing so much in the mid-long term.

[Top](#top)

##<a name="section4"></a>Implementation details
###<a name="section4.1"></a>`OrionSTHSink` class
`OrionSTHSink` extends `OrionMongoBaseSink`, which as any other NGSI-like sink extends the base `OrionSink`. The methods that are extended are by `OrionMongoBaseSink` are:

    public void start();

`MongoBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionSTHSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

The methods that are extended by `OrionSTHSink` are:

    void persist(Map<String, String>, NotifyContextRequest) throws Exception;
    
The context data, already parsed by `OrionSink` in `NotifyContextRequest`, is iterated and persisted in the MongoDB backend by means of a `MongoBackend` instance. Header information from the `Map<String, String>` is used to complete the persitence process, such as the timestamp or the destination.

[Top](#top)

###<a name="section4.2"></a>`MongoBackend` class
This is a convenience backend class for MongoDB that provides methods to persist the context data both in raw of aggregated format. Relevant methods regarding raw format are:

    public void createDatabase(String dbName) throws Exception;
    
Creates a database, given its name, if not exists.
    
    public void createCollection(String dbName, String collectionName) throws Exception;
    
Creates a collection, given its name, if not exists in the given database.
    
    public void insertContextDataRaw(String dbName, String collectionName, long recvTimeTs, String recvTime, String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd) throws Exception;
    
Updates or inserts (depending if the document already exists or not) a set of documents in the given collection within the given database. Such a set of documents contains all the information regarding current and past notifications (historic) for a single attribute. a set of documents is managed since historical data is stored using several resolutions and range combinations (second-minute, minute-hour, hour-day, day-month and month-year). See STH at [Github](https://github.com/telefonicaid/IoT-STH/blob/develop/README.md) for more details.

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