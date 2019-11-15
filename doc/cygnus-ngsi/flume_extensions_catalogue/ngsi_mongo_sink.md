# <a name="top"></a>NGSIMongoSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to MongoDB data structures](#section1.2)
        * [MongoDB databases naming conventions](#section1.2.1)
        * [MongoDB collections naming conventions](#section1.2.2)
        * [Row-like storing](#section1.2.3)
        * [Column-like storing](#section1.2.4)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Database and table names](#section1.3.2)
        * [Row-like storing](#section1.3.3)
        * [Column-like storing](#section1.3.4)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About batching](#section2.3.1)
        * [About `recvTime` and `TimeInstant` metadata](#section2.3.2)
        * [About the encoding](#section2.3.3)
        * [About supported versions of MongoDB](#section2.3.4)
* [Programmers guide](#section3)
    * [`NGSIMongoSink` class](#section3.1)
    * [`NGSIMongoBackend` class](#section3.2)
    * [Authentication and authorization](#section3.3)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIMongoSink`, or simply `NGSIMongoSink` is a sink designed to persist NGSI-like context data events within a MongoDB server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific HDFS data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to MongoDB data structures
MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by `NGSIMongoSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>MongoDB databases naming conventions
A database called as the `fiware-service` header value within the event is created (if not existing yet). A configured prefix is added (by default, `sth_`).

It must be said [MongoDB does not accept](https://docs.mongodb.com/manual/reference/limits/#naming-restrictions) `/`, `\`, `.`, `"` and `$` in the database names. This leads to certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.

MongoDB [namespaces (database + collection) name length](https://docs.mongodb.com/manual/reference/limits/#naming-restrictions) is limited to 113 bytes.

[Top](#top)

#### <a name="section1.2.2"></a>MongoDB collections naming conventions
The name of these collections depends on the configured data model and analysis mode (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](./ngsi_rest_handler.md)) is used as the name of the collection. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table. The configured prefix is prepended to the collection name.
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the collections name. If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated. The configured prefix is prepended to the collection name.
* Data model by attribute (`data_model=dm-by-attribute`). For each entity's attribute, the notified/default FIWARE service path is concatenated to the notified entity ID and type and to the notified attribute name in order to compose the collection name. If the FIWARE service path is the root one (`/`) then only the entity ID and type and the attribute name and type are concatenated. The configured prefix is prepended to the collection name.

It must be said [MongoDB does not accept](https://docs.mongodb.com/manual/reference/limits/#naming-restrictions) `$` in the collection names, so it will be replaced by underscore, `_`. This leads to certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.

MongoDB [namespaces (database + collection) name length](https://docs.mongodb.com/manual/reference/limits/#naming-restrictions) is limited to 113 bytes.

The following table summarizes the table name composition (assuming default `sth_` prefix, old encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|
| `/` | `sth_/` | `sth_/<entityId>_<entityType>` | `sth_/<entityId>_<entityType>_<attrName>` |
| `/<svcPath>` | `sth_/<svcPath>` | `sth_/<svcPath>_<entityId>_<entityType>` | `sth_/<svcPath>_<entityId>_<entityType>_<attrName>` |

Using the new encoding:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|
| `/` | `sth_x002f` | `sth_x002fxffff<entityId>xffff<entityType>` | `sth_x002fxffff<entityId>xffff<entityType>xffff<attrName>` |
| `/<svcPath>` | `sth_x002f<svcPath>` | `sth_x002f<svcPath>xffff<entityId>xffff<entityType>` | `sth_x002f<svcPath>xffff<entityId>xffff<entityType>xffff<attrName>` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSIEvent`.

[Top](#top)

#### <a name="section1.2.3"></a>Row-like storing
Regarding the specific data stored within the above collections, if `attr_persistence` parameter is set to `row` (default storing mode) then the notified data is stored attribute by attribute, composing a Json document for each one of them. Each document contains a variable number of fields, depending on the configured `data_model`:

* Data model by service path:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
    * `attrMetadata`: will be stored only if it was configured to (attr_metadata_store set to true in the configuration file ngsi_agent.conf). It is a Json object.
* Data model by entity:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
    * `attrMetadata`: will be stored only if it was configured to (attr_metadata_store set to true in the configuration file ngsi_agent.conf). It is a Json object.
* Data model by attribute:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
    * `attrMetadata`: will be stored only if it was configured to (attr_metadata_store set to true in the configuration file ngsi_agent.conf). It is a Json object.

[Top](#top)

#### <a name="section1.2.4"></a>Column-like storing
Regarding the specific data stored within the above collections, if `attr_persistence` parameter is set to `column` then a single Json document is composed for the whole notified entity. Each document contains a variable number of fields, depending on the configured `data_model`:

* Data model by service path:
    * `recvTime`: Timestamp in human-readable format (Similar to [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601), but avoiding the `Z` character denoting UTC, since all MySQL timestamps are supposed to be in UTC format).
    * `fiwareServicePath`: The notified one or the default one.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    *  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
    *  For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.
* Data model by entity:
    * `recvTime`: Timestamp in human-readable format (Similar to [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601), but avoiding the `Z` character denoting UTC, since all MySQL timestamps are supposed to be in UTC format).
    * `fiwareServicePath`: The notified one or the default one.
    *  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
    *  For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.
* Data model by attribute. **This combination has no sense, so it is avoided.**

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

#### <a name="section1.3.2"></a>Database and collection names
A MongoDB database named as the concatenation of the prefix and the notified FIWARE service path, i.e. `sth_vehicles`, will be created.

Regarding the collection names, the MongoDB collection names will be, depending on the configured data model, the following ones (old encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|
| `/` | `sth_/` | `sth_/car1_car` | `sth_/car1_car_speed`<br>`sth_/car1_car_oil_level` |
| `/4wheels` | `sth_/4wheels` | `sth_/4wheels_car1_car` | `sth_/4wheels_car1_car_speed`<br>`sth_/4wheels_car1_car_oil_level` |

Using the new encoding:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-attribute` |
|---|---|---|---|
| `/` | `sth_x002f` | `sth_x002fxffffcar1xffffcar` | `sth_x002fxffffcar1xffffcarxffffspeed`<br>`sth_x002fxffffcar1xffffcarxffffoil_level` |
| `/4wheels` | `sth_x002f4wheels` | `sth_x002f4wheelsxffffcar1xffffcar` | `sth_x002f4wheelsxffffcar1xfffcarxffffspeed`<br>`sth_x002f4wheelsxffffcar1xffffcarxffffoil_level` |

[Top](#top)

#### <a name="section1.3.3"></a>Row-like storing
Assuming `data_model=dm-by-service-path` and `attr_persistence=row` as configuration parameters, then `NGSIMongoSink` will persist the data within the body as:

    $ mongo -u myuser -p
    MongoDB shell version: 3.6.14
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

If `data_model=dm-by-entity` and `attr_persistence=row` then `NGSIMongoSink` will persist the data within the body as:

    $ mongo -u myuser -p
    MongoDB shell version: 3.6.14
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

If `data_model=dm-by-attribute` and `attr_persistence=row` then `NGSIMongoSink` will persist the data as:

    $ mongo -u myuser -p
    MongoDB shell version: 3.6.14
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

[Top](#top)

#### <a name="section1.3.4"></a>Column-like storing
If `data_model=dm-by-service-path` and `attr_persistence=column` then `NGSIMongoSink` will persist the data within the body as:

    $ mongo -u myuser -p
    MongoDB shell version: 3.6.14
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

If `data_model=dm-by-entity` and `attr_persistence=column` then `NGSIMongoSink` will persist the data within the body as:

    $ mongo -u myuser -p
    MongoDB shell version: 3.6.14
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

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIMongoSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.NGSIMongoSink |
| channel | yes | N/A |
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | Always <i>false</i> ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity | <i>dm-by-service-path</i>, <i>dm-by-entity</i> or <dm-by-attribute</i>. <i>dm-by-service</i> is not currently supported. |
| attr\_persistence | no | row | <i>row</i> or <i>column</i>. |
| attr\_metadata\_store | no | false | <i>true</i> or <i>false</i>. |
| mongo\_hosts | no | localhost:27017 | FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run. |
| mongo\_username | no | <i>empty</i> | If empty, no authentication is done. |
| mongo\_password | no | <i>empty</i> | If empty, no authentication is done. |
| db\_prefix | no | sth_ ||
| collection\_prefix | no | sth_ | `system.` is not accepted. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| data\_expiration | no | 0 | Collections will be removed if older than the value specified in seconds. The reference of time is the one stored in the `recvTime` property. Set to 0 if not wanting this policy. |
| collections\_size | no | 0 | The oldest data (according to insertion time) will be removed if the size of the data collection gets bigger than the value specified in bytes. Notice that the size-based truncation policy takes precedence over the time-based one. Set to 0 if not wanting this policy. Minimum value (different than 0) is 4096 bytes. |
| max\_documents | no | 0 | The oldest data (according to insertion time) will be removed if the number of documents in the data collections goes beyond the specified value. Set to 0 if not wanting this policy. |
| ignore\_white\_spaces | no | true | <i>true</i> if exclusively white space-based attribute values must be ignored, <i>false</i> otherwise. |

A configuration example could be:

    cygnus-ngsi.sinks = mongo-sink
    cygnus-ngsi.channels = mongo-channel
    ...
    cygnus-ngsi.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMongoSink
    cygnus-ngsi.sinks.mongo-sink.channel = mongo-channel
    cygnus-ngsi.sinks.mongo-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.mongo-sink.attr_persistence = column
    cygnus-ngsi.sinks.mongo-sink.enable_encoding = false
    cygnus-ngsi.sinks.mongo-sink.enable_grouping = false
    cygnus-ngsi.sinks.mongo-sink.enable_lowercase = false
    cygnus-ngsi.sinks.mongo-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.mongo-sink.mongo_hosts = 192.168.80.34:27017
    cygnus-ngsi.sinks.mongo-sink.mongo_username = myuser
    cygnus-ngsi.sinks.mongo-sink.mongo_password = mypassword
    cygnus-ngsi.sinks.mongo-sink.db_prefix = cygnus_
    cygnus-ngsi.sinks.mongo-sink.collection_prefix = cygnus_
    cygnus-ngsi.sinks.mongo-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.mongo-sink.batch_size = 100
    cygnus-ngsi.sinks.mongo-sink.batch_timeout = 30
    cygnus-ngsi.sinks.mongo-sink.batch_ttl = 10
    cygnus-ngsi.sinks.mongo-sink.batch_retry_intervals = 5000
    cygnus-ngsi.sinks.mongo-sink.data_expiration = 0
    cygnus-ngsi.sinks.mongo-sink.collections_size = 0
    cygnus-ngsi.sinks.mongo-sink.max_documents = 0
    cygnus-ngsi.sinks.mongo-sink.ignore_white_spaces = true

[Top](#top)

### <a neme="section2.2"></a>Use cases
Use `NGSIMongoSink` if you are looking for a Json-based document storage not growing so much in the mid-long term.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About batching
As explained in the [programmers guide](#section3), `NGSIMongoSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same MongoDB collection. If processing the events one by one, we would need 100 inserts into MongoDB; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination MongoDB collection. In the worst case, the whole 100 entities will be about 100 different entities (100 different MongoDB collections), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIMongoSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.2"></a>About `recvTime` and `TimeInstant` metadata
By default, `NGSIMongoSink` stores the notification reception timestamp. Nevertheless, if (and only if) working in `row` mode and a metadata named `TimeInstant` is notified, then such metadata value is used instead of the reception timestamp. This is useful when wanting to persist a measure generation time (which is thus notified as a `TimeInstant` metadata) instead of the reception time.

[Top](#top)

#### <a name="section2.3.3"></a>About the encoding
`NGSIMongoSink` follows the [MongoDB naming restrictions](https://docs.mongodb.org/manual/reference/limits/#naming-restrictions). In a nutshell:

Until version 1.2.0 (included), Cygnus applied a very simple encoding:

* Database names will have the characters `\`, `/`, `.`, `$`, `"` and ` ` encoded as `_`.
* Collections names will have the characters `$` encoded as `_`.

From version 1.3.0 (included), Cygnus applies this specific encoding tailored to MongoDB data structures:

* Equals character, `=`, is encoded as `xffff`.
* All the forbidden characters are encoded as a `x` character followed by the [Unicode](http://unicode-table.com) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* `xffff` is used as concatenator character.

Despite the old encoding will be deprecated in the future, it is possible to switch the encoding type through the `enable_encoding` parameter as explained in the [configuration](#section2.1) section.

[Top](#top)

#### <a name="section2.3.4"></a>About supported versions of MongoDB
This sink has been tested with the following versions of Mongo:

* 3.4
* 3.6
* 4.2

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSISTHSink` class
`NGSIMongoSink` extends `NGSIMongoBaseSink`, which as any other NGSI-like sink, extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the MongoDB collection where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `MongoBackend` implementation.

    public void start();

An implementation of `MongoBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIMongoSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>`MongoBackend` class
This is a convenience backend class for MongoDB that provides methods to persist the context data both in raw of aggregated format. Relevant methods regarding raw format are:

    public void createDatabase(String dbName) throws Exception;

Creates a database, given its name, if not exists.

    public void createCollection(String dbName, String collectionName) throws Exception;

Creates a collection, given its name, if not exists in the given database.

    public void insertContextDataRaw(String dbName, String collectionName, long recvTimeTs, String recvTime, String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd) throws Exception;

Updates or inserts (depending if the document already exists or not) a set of documents in the given collection within the given database. Such a set of documents contains all the information regarding current and past notifications (historic) for a single attribute. a set of documents is managed since historical data is stored using several resolutions and range combinations (second-minute, minute-hour, hour-day, day-month and month-year). See STH Comet at [Github](https://github.com/telefonicaid/IoT-STH/blob/develop/README.md) for more details.

Nothing special is done with regards to the encoding. Since Cygnus generally works with UTF-8 character set, this is how the data is written into the collections. It will responsability of the MongoDB client to convert the bytes read into UTF-8.

[Top](#top)

### <a name="section3.3"></a>Authentication and authorization
Current implementation of `NGSIMongoSink` relies on the username and password credentials created at the MongoDB endpoint.

[Top](#top)
