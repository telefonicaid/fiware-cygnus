#<a name="top"></a>OrionMongoSink
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
* [Programmers guide](#section4)
    * [`OrionMongoSink` class](#section4.1)
    * [`MongoBackend` class](#section4.2)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionMongoSink`, or simply `OrionMongosink` is a sink designed to persist NGSI-like context data events within a MongoDB server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific HDFS data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to [`OrionRestHandler`](./orion_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to MongoDB data structures
MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by `OrionMongoSink` each time a Flume event is going to be persisted.

A database called as the `fiware-service` header value within the event is created (if not existing yet).

The context responses/entities within the container are iterated, and a collection is created (if not yet existing) for each unit data. The collection is called as the concatenation of the `fiware-servicePath`_`destination` headers values within the event.

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

According to different combinations of the parameters `datamodel` and `attr_persistence`, the system will persist the data in different ways, as we will describe below.
Assuming `mongo_username=myuser` and `should_hash=false` and `data_model=dm-by-entity` and `attr_persistence=row` as configuration parameters, then `OrionMongoSink` will persist the data within the body as:

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
    sth_/4wheels_car1_car
    system.indexes
    > db['sth_/4wheels_car1_car'].find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "attrName" : "speed", "attrType" : "float", "attrValue" : "112.9" }
    { "_id" : ObjectId("5534d143fa701f0be751db83"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "attrName" : "oil_level", "attrType" : "float", "attrValue" : "74.6" }

If `data_model=dm-by-entity` and `attr_persistence=column` then `OrionMongoSink` will persist the data within the body as:

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
    sth_/4wheels_car1_car
    system.indexes
    > db['sth_/4wheels_car1_car'].find()
    {"_id" : ObjectId("56337ea4c9e77c1614bfdbb7"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "speed" : "112.9", "oil_level" : "74.6"}

If `data_model=dm-by-service-path` and `attr_persistence=row` then `OrionMongoSink` will persist the data within the body in the same collection (i.e. `4wheels`) for all the entities of the same service path as:

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
    system.indexes
    > db['sth_/4wheels'].find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "attrType" : "float", "attrValue" : "112.9" }
    { "_id" : ObjectId("5534d143fa701f0be751db83"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "entityId" : "car1", "entityType" : "car", "attrName" : "oil_level", "attrType" : "float", "attrValue" : "74.6" }
    { "_id" : ObjectId("5534d143fa701f0be751db84"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "entityId" : "car2", "entityType" : "car", "attrName" : "speed", "attrType" : "float", "attrValue" : "123.0" }
    { "_id" : ObjectId("5534d143fa701f0be751db85"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "entityId" : "car2", "entityType" : "car", "attrName" : "oil_level", "attrType" : "float", "attrValue" : "40.9" }

Note: The first two documents were generated by the above flume-event, while the last two documents (`"entityId" : "car2"`) were originated by another event (not shown here).
We have left these documents in order to show that the same collection stores data of different entities, unlike what it happens with other value of `data_model` parameter.

Similarly, if `data_model=dm-by-service-path` and `attr_persistence=column` then `OrionMongoSink` will persist the data as:

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
    system.indexes
    > db['sth_/4wheels'].find()
    { "_id" : ObjectId("5534d143fa701f0be751db86"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "entityId" : "car1", "entityType" : "car", "speed" : "112.9", "oil_level" : "74.6" }

If `data_model=dm-by-attribute` and `attr_persistence=row` then `OrionMongoSink` will persist the data as:

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
    sth_/4wheels_car1_car_speed
    sth_/4wheels_car1_car_oil_level
    system.indexes
    > db['sth_/4wheels_car1_car_speed'].find()
     { "_id" : ObjectId("5534d143fa701f0be751db87"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "attrType" : "float", "attrValue" : "112.9" }
    > db['sth_/4wheels_car1_oil_level'].find()
     { "_id" : ObjectId("5534d143fa701f0be751db87"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412Z", "attrType" : "float", "attrValue" : "74.6" }

Finally, the pair of parameters `data_model=dm-by-attribute` and `attr_persistence=column` has no palpable sense if used together, thus **DON'T USE IT**. In this case, in fact, `OrionMongoSink` will not persist anything; only a warning will be logged.

NOTES:

* `mongo` is the MongoDB CLI for querying the data.
* `sth_` prefix is added by default when no database nor collection prefix is given (see next section for more details).
* This sink adds the original '/' initial character to the `fiware-servicePath`, which was removed by `OrionRESTHandler`.

NOTE: `mongo` is the MongoDB CLI for querying the data.

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`OrionMongoSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.OrionMongoSink |
| channel | yes | N/A |
| enable_grouping | no | false | <i>true</i> or <i>false</i>. |
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data_model | no | dm-by-entity | <i>dm-by-service-path</i>, <i>dm-by-entity</i> or <dm-by-attribute</i>. <i>dm-by-service</i> is not currently supported. |
| attr_persistence | no | row | <i>row</i> or <i>column</i>. |
| mongo_hosts | no | localhost:27017 | FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run. |
| mongo_username | no | <i>empty</i> | If empty, no authentication is done. |
| mongo_password | no | <i>empty</i> | If empty, no authentication is done. |
| should_hash | no | false | <i>true</i> for collection names based on a hash, <i>false</i> for human redable collections. |
| db_prefix | no | sth_ ||
| collection_prefix | no | sth_ ||
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| data_expiration | no | 0 | Collections will be removed if older than the value specified in seconds. The reference of time is the one stored in the `recvTime` property. Set to 0 if not wanting this policy. |
| collections_size | no | 0 | The oldest data (according to insertion time) will be removed if the size of the data collection gets bigger than the value specified in bytes. Notice that the size-based truncation policy takes precedence over the time-based one. Set to 0 if not wanting this policy. Minimum value (different than 0) is 4096 bytes. |
| max_documents | no | 0 | The oldest data (according to insertion time) will be removed if the number of documents in the data collections goes beyond the specified value. Set to 0 if not wanting this policy. |

A configuration example could be:

    cygnusagent.sinks = mongo-sink
    cygnusagent.channels = mongo-channel
    ...
    cygnusagent.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.OrionMongoSink
    cygnusagent.sinks.mongo-sink.channel = mongo-channel
    cygnusagent.sinks.mongo-sink.data_model = dm-by-entity
    cygnusagent.sinks.mongo-sink.attr_persistence = column
    cygnusagent.sinks.mongo-sink.enable_grouping = false
    cygnusagent.sinks.mongo-sink.enable_lowercase = false
    cygnusagent.sinks.mongo-sink.mongo_hosts = 192.168.80.34:27017
    cygnusagent.sinks.mongo-sink.mongo_username = myuser
    cygnusagent.sinks.mongo-sink.mongo_password = mypassword
    cygnusagent.sinks.mongo-sink.db_prefix = cygnus_
    cygnusagent.sinks.mongo-sink.collection_prefix = cygnus_
    cygnusagent.sinks.mongo-sink.should_hash = false
    cygnusagent.sinks.mongo-sink.data_model = dm-by-entity
    cygnusagent.sinks.mongo-sink.batch_size = 100
    cygnusagent.sinks.mongo-sink.batch_timeout = 30
    cygnusagent.sinks.mongo-sink.batch_ttl = 10
    cygnusagent.sinks.mongo-sink.data_expiration = 0
    cygnusagent.sinks.mongo-sink.collections_size = 0
    cygnusagent.sinks.mongo-sink.max_documents = 0

[Top](#top)

###<a neme="section2.2"></a>Use cases
Use `OrionMongoSink` if you are looking for a Json-based document storage not growing so much in the mid-long term.

[Top](#top)

###<a name="section2.3"></a>Important notes
####<a name="section2.3.1"></a>Hashing based collections
In case the `should_hash` option is set to `true`, the collection names are generated as a concatenation of the `collection_prefix` plus a generated hash plus `.aggr` for the collections of the aggregated data. To avoid collisions in the generation of these hashes, they are forced to be 20 bytes long at least. Once again, the length of the collection name plus the `db_prefix` plus the database name (i.e. the fiware-service) should not be more than 120 bytes using UTF-8 or MongoDB will complain and will not create the collection, and consequently no data would be stored by Cygnus. The hash function used is SHA-512.

In case of using hashes as part of the collection names and to let the user or developer easily recover this information, a collection named `<collection_prefix>_collection_names` is created and fed with information regarding the mapping of the collection names and the combination of concrete services, service paths, entities and attributes.

[Top](#top)

####<a name="section2.3.2"></a>About batching
As explained in the [programmers guide](#section3), `OrionMongoSink` extends `OrionSink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows exteding classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same MongoDB collection. If processing the events one by one, we would need 100 inserts into MongoDB; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination MongoDB collection. In the worst case, the whole 100 entities will be about 100 different entities (100 different MongoDB collections), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `OrionMongoSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](../operation/performance_tuning_tips.md).

[Top](#top)

##<a name="section4"></a>Programmers guide
###<a name="section4.1"></a>`OrionSTHSink` class
`OrionMongoSink` extends `OrionMongoBaseSink`, which as any other NGSI-like sink, extends the base `OrionSink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the MongoDB collection where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `MongoBackend` implementation.

    public void start();

An implementation of `MongoBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionMongoSink()` (contructor), `configure()` and `start()`.

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

    public void insertContextDataRaw(String dbName, String collectionName, long recvTimeTs, String recvTime, String entityId, String entityType, Map<String, String> attrs, Map<String, String> mds) throws Exception

Inserts a new document in the given collection within the given database. Such a document contains all the information regarding a single notification for multiple attributes. See STH at [Github](https://github.com/telefonicaid/IoT-STH/blob/develop/README.md) for more details.

[Top](#top)
