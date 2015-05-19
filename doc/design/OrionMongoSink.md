#<a name="top"></a>OrionMongoSink
* [Functionality](#section1)
    * [Mapping Flume events to HDFS data structures](#section1.1)
    * [Example](#section1.2)
* [Configuration](#section2)
* [Use cases](#section3)
* [Implementation details](#section4)
    * [`OrionMongoSink` class](#section4.1)
    * [`MongoBackend` class](#section4.2)
* [Contact](#section5)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionMongoSink`, or simply `OrionMongosink` is a sink designed to persist NGSI-like context data events within a MongoDB server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always [transformed](from_ngsi_events_to_flume_events.md) into internal Flume events at Cygnus sources thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. In the end, the information within these Flume events must be mapped into specific MongoDB data structures.

[Top](#top)

###<a name="section1.1"></a>Mapping Flume events to MongoDB data structures
MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by `OrionMongoSink` each time a Flume event is taken, by performing the following workflow:

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

Assuming `mongo_username=myuser` as configuration parameter, then `OrionMongoSink` will persist the data within the body as:

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
    sth_/4wheels	
    sth_/4wheels_car1_car
    sth_/4wheels_car1_car_speed
    system.indexes
    > db['sth_/4wheels'].find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.124Z", "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "attrType" : "float", "attrValue" : "112.9" }
    > db['sth_/4wheels_car1_car'].find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "attrName" : "speed", "attrType" : "float", "attrValue" : "112.9" }
    > db['sth_/4wheels_car1_car_speed'].find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.560Z", "attrType" : "float", "attrValue" : "112.9" }

NOTES:

* `mongo` is the MongoDB CLI for querying the data.
* The results for the three different data models (<i>collection-per-service-path</i>, <i>collection-per-entity</i> and <i>collection-per-attribute</i>) are shown respectively.
* `sth_` prefix is added by default when no database nor collection prefix is given (see next section for more details).
* This sink adds the original '/' initial character to the `fiware-servicePath`, which was removed by `OrionRESTHandler`.

NOTE: `mongo` is the MongoDB CLI for querying the data.

[Top](#top)

##<a name="section2"></a>Configuration
`OrionMongoSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.OrionMongoSink |
| channel | yes | N/A |
| mongo_hosts | no | localhost:27017 | FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run |
| mongo_username | no | <i>empty</i> | If empty, no authentication is done |
| mongo_password | no | <i>empty</i> | If empty, no authentication is done |
| data_model | no | collection-per-entity | Under study |
| db_prefix | no | sth_ | Under study |
| collection_prefix | no | sth_ | Under study |

A configuration example could be:

    cygnusagent.sinks = mongo-sink
    cygnusagent.channels = mongo-channel
    ...
    cygnusagent.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.OrionMongoSink
    cygnusagent.sinks.mongo-sink.channel = mongo-channel
    cygnusagent.sinks.mongo-sink.mongo_hosts = 192.168.80.34:27017
    cygnusagent.sinks.mongo-sink.mongo_username = myuser
    cygnusagent.sinks.mongo-sink.mongo_password = mypassword
    cygnusagent.sinks.mongo-sink.data_model = collection-per-entity
    cygnusagent.sinks.mongo-sink.db_prefix = cygnus_
    cygnusagent.sinks.mongo-sink.collection_prefix = cygnus_

[Top](#top)

##<a name="section3"></a>Use cases
Use `OrionMongoSink` if you are looking for a Json-based document storage not growing so much in the mid-long term.

[Top](#top)

##<a name="section4"></a>Implementation details
###<a name="section4.1"></a>`OrionMongoSink` class
As any other NGSI-like sink, `OrionMongoSink` extends the base `OrionSink`. The methods that are extended are:

    void persist(Map<String, String>, NotifyContextRequest) throws Exception;
    
The context data, already parsed by `OrionSink` in `NotifyContextRequest`, is iterated and persisted in the MongoDB backend by means of a `MongoBackend` instance. Header information from the `Map<String, String>` is used to complete the persitence process, such as the timestamp or the destination.
    
    public void start();

`MongoBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionMongoSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section4.2"></a>`MongoBackend` class
This is a convenience backend class for MongoDB that provides methods to persist the context data both in raw of aggregated format. Relevant methods regarding raw format are:

    public void createDatabase(String dbName) throws Exception;
    
Creates a database, given its name, if not exists.
    
    public void createCollection(String dbName, String collectionName) throws Exception;
    
Creates a collection, given its name, if not exists in the given database.
    
    public void insertContextDataRaw(String dbName, String collectionName, long recvTimeTs, String recvTime, String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd) throws Exception;
    
Inserts a new document in the given collection within the given database. Such a document contains all the information regarding a single notification for a single attribute. See STH at [Github](https://github.com/telefonicaid/IoT-STH/blob/develop/README.md) for more details.

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