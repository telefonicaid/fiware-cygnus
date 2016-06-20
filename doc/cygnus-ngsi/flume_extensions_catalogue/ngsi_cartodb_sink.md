#<a name="top"></a>NGSICartoDBSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to CartoDB data structures](#section1.2)
        * [PostgreSQL databases and schemas naming conventions](#section1.2.1)
        * [PostgreSQL tables naming conventions](#section1.2.2)
        * [Raw-based storing](#section1.2.3)
        * [Distance-based storing](#section1.2.4)
    * [Example](#section1.3)
        * [Flume event](#section1.3.1)
        * [Table names](#section1.3.2)
        * [Raw-based storing](#section1.3.3)
        * [Distance-based storing](#section1.3.4)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [`NGSICartoDBSink` and non-geolocated entities](#section2.3.1)
        * [Multitenancy support](#section2.3.1)
        * [Batching](#section2.3.3)
* [Programmers guide](#section3)
    * [`NGSICartoDBSSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

##<a name="section2"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSICartoDBSink`, or simply `NGSICartoDBSSink` is a cygnus-ngsi sink designed to persist NGSI-like context data events within [CartoDB](https://cartodb.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at cygnus-ngsi sources. In the end, the information within these Flume events must be mapped into specific CartoDB data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](./ngsi_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to CartoDB data structures
CartoDB is based on [PostgreSQL](http://www.postgresql.org/) and [PostGIS](http://postgis.net/) extensions. It organizes the data in databases (one per organization), schemas (one per user within an organization) and tables (a schema may have one or more tables). Such organization is exploited by `NGSICartoDBSink` each time a Flume event is going to be persisted.

[Top](#top)

####<a name="section1.2.1"></a>PostgreSQL databases and schemas naming conventions
PostgreSQL databases and schemas are already created by CartoDB upon organization and username request, respectively. Thus, it is up to CartoDB to define the naming conventions for these elements; specifically:

* Organization must... ?
* Username must only contain lowercase letters, numbers and the dash symbol (`-`).

Here it is assumed the notified/default FIWARE service maps the PostgreSQL schema/username, ensuring this way multitenancy and data isolation. This multitenancy approach is complemented by the usage of a configuration file holding the mapping between FIWARE service/CartoDB username and API Key (please, check the [Configuration](#section2.1) section).

[Top](#top)

####<a name="section1.2.2"></a>PostgreSQL tables naming conventions
The name of these tables depends on the configured data model and analysis mode (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](.ngsi_rest_handler.md)) is used as the name of the table. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table.
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the table name. The concatenation string is `0x0000`, closely related to the encoding of not allowed characters (see below). If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated.

The above applies both if `enable_raw` or `enable_distance` es set to `true`. In adddition, the distance analysis mode adds the sufix `x0000distance` to the table name.

It must be said [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). All the other characters will be encoded as `xXXXX` when composing the table names, where `XXXX` is the [unicode](https://en.wikipedia.org/wiki/List_of_Unicode_characters) of the character. For instance, the initial slash (`/`) of the FIWARE service path is encoded as `x002f`.

The following table summarizes the table name composition:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` |
|---|---|---|
| `/` | `x002f` | `x002f<entityId>x0000<entityType>[x0000distance]` |
| `/<svcPath>` | `x002f<svcPath>[x0000distance]` | `x002f<svcPath>x0000<entityId>x0000<entityType>[x0000distance]` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the Flume event.

[Top](#top)

####<a name="section1.2.3"></a>Raw-based storing
Regarding the specific data stored within the tables, if `enable_raw` parameter is set to `true` (default storing mode) then the notified data is **stored as it is, without any processing or modification**. This is the simplest way of storing geolocation data.

A single insert is composed for each notified entity, containing such insert the following fields:

* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `the_geom`: The current geolocation point, i.e. containing longitude and latitude. It must be of type PostGIS Geometry, it can be created for instance using [`ST_Point()`](http://postgis.net/docs/ST_Point.html). The data used for composing the PostGIS Geometry is obtained from a special notified attribute:
    * Either of type `geo:point`.
    * Either having associated a `location` metadata of type `string` and value `WGS84`.
* For each not-geolocated attribute, the insert will contain two additional fields, one for the value, named `<attrName>`, and another for the metadata, named `<attrName>_md`).

[Top](#top)

####<a name="section1.2.4"></a>Distance-based storing
If `enable_distance` parameter is set to `true` (by default, this kind of storing is not run) then the notified data is processed based on a distance analysis. As said, the linear distance and elapsed time with regards to the previous geolocation of the entity is obtained, and this information is used to update certain aggregations: total amount of distance, total amount of time and many others. The speed is obtained as well as the result of dividing the distance by the time, and such speed calculation is used as well for updating certain aggregations.

The final goal is to **pre-compute a set of distance-based measures** as a function of the geolocation of an entity, allowing for querying about <i>"the total amount of time this entity took to arrive to this point"</i>, or <i>"which was the average speed of this entity when passing through this point"</i>, etc. **without performing any computation at querying time**.

A single insert is composed for each notified entity, containing such insert the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `the_geom`: The current geolocation point, i.e. containing longitude and latitude. It must be of type PostGIS Geometry, it can be created for instance using [`ST_Point()`](http://postgis.net/docs/ST_Point.html). The data used for composing the PostGIS Geometry is obtained from a special notified attribute:
    * Either of type `geo:point`.
    * Either having associated a `location` metadata of type `string` and value `WGS84`.
* `stageDistance`: The linear distance between the current and previous geopoint.
* `stageTime`: The elapsed time when moving from the previous geopoint to the current one.
* `stageSpeed`: The result of dividing the stage distance by the stage time.
* `sumDistance`: Sum of stage distances. Dividing this value by the number of stages results in the average stage distance.
* `sumTime`: Sum of elapsed times. Dividing this value by the number of stages results in the average stage time.
* `sumSpeed`: Sum of stage speeds. Dividing this value by the number of stages results in the average stage speed.
* `sumDistance2`: Sum of the square root of the stage distances. Dividing this value by the number of stages results in the variance of the stage distances.
* `sumTime2`: Sum of the square root of the stage times. Dividing this value by the number of stages results in the variance of the stage times.
* `sumSpeed2`: Sum of the square root of the stage speed. Dividing this value by the number of stages results in the variance of the stage speeds.
* `maxDistance`: Maximum stage distance.
* `minDistance`: Minimum stage distance.
* `maxTime`: Maximum stage time.
* `minTime`: Minimum stage time.
* `maxSpeed`: Maximum stage speed.
* `minSpeed`: Minimum stage speed.
* `numStages`: Number of stages.

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
	            },
	            {
	                attrName=location,
	                attrType=geo:point,
	                attrValue="41.102, -3.008"
	            }
	        ]
	    }
    }
    
[Top](#top)

####<a name="section1.3.2"></a>Table names
The PostgreSQL table names will be, depending on the configured data model and analysis mode, the following ones:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` |
|---|---|---|
| `/` | `x002f` | `x002fcar1x0000car[x0000distance]` |
| `/4wheels` | `x002f4wheels[x0000distance]` | `x002f4wheelsx0000car1x0000car[x0000distance]` |
    
[Top](#top)

####<a name="section1.3.3"></a>Raw-based storing
Let's assume a table name `x002f4wheelsx0000car1x0000car` (data model by entity, non-root service path, only raw analysis mode). The data stored within this table would be:

```
curl "https://myusername.cartodb.com/api/v2/sql?q=select * from x002f4wheelsx0000car1x0000car&api_key=abcdef0123456789"
{
  "rows": [
    {
      "cartodb_id": 1,
      "the_geom": "0101000020E61000007B3D8FFE54EF0EC04E0BE7FD50B94540",
      "the_geom_webmercator": "0101000020110F00002DF05823E4451AC101051DDC46865441",
      "oil_level_md": "[]",
      "oil_level": "74.6",
      "speed_md": "[]",
      "speed": "112.9",
      "entitytype": "car",
      "entityid": "car1",
      "fiwareservicepath": "/4wheels",
      "recvtime": "2016-04-21T10:34:23.423Z"
    }
  ],
  "time": 0.001,
  "fields": {
    "cartodb_id": {
      "type": "number"
    },
    "the_geom": {
      "type": "geometry"
    },
    "the_geom_webmercator": {
      "type": "geometry"
    },
    "oil_level_md": {
      "type": "string"
    },
    "oil_level": {
      "type": "string"
    },
    "speed_md": {
      "type": "string"
    },
    "speed": {
      "type": "string"
    },
    "entitytype": {
      "type": "string"
    },
    "entityid": {
      "type": "string"
    },
    "fiwareservicepath": {
      "type": "string"
    },
    "recvtime": {
      "type": "string"
    }
  },
  "total_rows": 1
}
```

[Top](#top)

####<a name="section1.3.4"></a>Distance-based storing
Let's assume a table name `x002f4wheelsx0000car1x0000carx0000distance` (data model by entity, non-root service path, only distance analysis mode) with a previous insertion (on the contrary, this would be the first insertion and almost all the aggregated values will be set to 0). The data stored within this table would be:

```
curl "https://myusername.cartodb.com/api/v2/sql?q=select * from x002f4wheelsx0000car1x0000carx0000distance&api_key=abcdef0123456789"
{
  "rows": [
    {
      "cartodb_id": 1,
      "the_geom": "0101000020E61000001886EA3B22530EC0CADCE89377BD4540",
      "the_geom_webmercator": "0101000020110F0000D1F6B55E3BC119C1EBDCC03D228B5441",
      "numsamples": 1,
      "minspeed": Infinity,
      "mintime": Infinity,
      "mindistance": Infinity,
      "maxspeed": -Infinity,
      "maxtime": -Infinity,
      "maxdistance": -Infinity,
      "sumspeed2": 0,
      "sumtime2": 0,
      "sumdistance2": 0,
      "sumspeed": 0,
      "sumtime": 0,
      "sumdistance": 0,
      "stagespeed": 0,
      "stagetime": 0,
      "stagedistance": 0,
      "entitytype": "car",
      "entityid": "car1",
      "fiwareservicepath": "/4wheels",
      "recvtime": "2016-04-21T10:34:23.423Z"
    },
    {
      "cartodb_id": 2,
      "the_geom": "0101000020E61000001886EA3B22530EC0CADCE89377BD4540",
      "the_geom_webmercator": "0101000020110F0000D1F6B55E3BC119C1EBDCC03D228B5441",
      "numsamples": 2,
      "minspeed": 0.00503757972391,
      "mintime": 15,
      "mindistance": 0.0755636958586634,
      "maxspeed": 0.00503757972391,
      "maxtime": 15,
      "maxdistance": 0.0755636958586634,
      "sumspeed2": 0.00002537720947,
      "sumtime2": 225,
      "sumdistance2": 0.00570987213182,
      "sumspeed": 0.00503757972391,
      "sumtime": 15,
      "sumdistance": 0.0755636958586634,
      "stagespeed": 0.00503757972391,
      "stagetime": 15,
      "stagedistance": 0.0755636958586634,
      "entitytype": "car",
      "entityid": "car1",
      "fiwareservicepath": "/4wheels",
      "recvtime": "2016-04-21T10:34:23.448Z"
    }
  ],
  "time": 0.001,
  "fields": {
    "cartodb_id": {
      "type": "number"
    },
    "the_geom": {
      "type": "geometry"
    },
    "the_geom_webmercator": {
      "type": "geometry"
    },
    "numsamples": {
      "type": "number"
    },
    "minspeed": {
      "type": "number"
    },
    "mintime": {
      "type": "number"
    },
    "mindistance": {
      "type": "number"
    },
    "maxspeed": {
      "type": "number"
    },
    "maxtime": {
      "type": "number"
    },
    "maxdistance": {
      "type": "number"
    },
    "sumspeed2": {
      "type": "number"
    },
    "sumtime2": {
      "type": "number"
    },
    "sumdistance2": {
      "type": "number"
    },
    "sumspeed": {
      "type": "number"
    },
    "sumtime": {
      "type": "number"
    },
    "sumdistance": {
      "type": "number"
    },
    "stagespeed": {
      "type": "number"
    },
    "stagetime": {
      "type": "number"
    },
    "stagedistance": {
      "type": "number"
    },
    "entitytype": {
      "type": "string"
    },
    "entityid": {
      "type": "string"
    },
    "fiwareservicepath": {
      "type": "string"
    },
    "recvtime": {
      "type": "string"
    }
  },
  "total_rows": 2
}
```

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`NGSICartoDBSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSICartoDBSink</i> |
| channel | yes | N/A ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. |
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data_model | no | dm-by-entity |  <i>dm-by-service-path</i> or <i>dm-by-entity</i>. |
| keys\_conf\_file | yes | N/A | Absolute path to the CartoDB file containing the mapping between FIWARE service/CartoDB usernames and CartoDB API Keys. |
| flip\_coordinates | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, the latitude and longitude values are exchanged. |
| enable\_raw | no | true | <i>true</i> or <i>false</i>. If <i>true</i>, a raw based storage is done. |
| enable\_distance | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, a distance based storage is done. |
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |

A configuration example could be:

```
cygnus-ngsi.sinks = cartodb-sink
cygnus-ngsi.channels = cartodb-channel
...
cygnus-ngsi.sinks.raw-sink.channel = cartodb-channel
cygnus-ngsi.sinks.raw-sink.type = com.telefonica.iot.cygnus.sinks.NGSICartoDBSink
cygnus-ngsi.sinks.raw-sink.enable_grouping = false
cygnus-ngsi.sinks.raw-sink.enable_lowercase = false
cygnus-ngsi.sinks.raw-sink.keys_conf_file = /usr/cygnus/conf/cartodb_keys.conf
cygnus-ngsi.sinks.raw-sink.flip_coordinates = true
cygnus-ngsi.sinks.raw-sink.enable_raw = true
cygnus-ngsi.sinks.raw-sink.enable_distance = false
cygnus-ngsi.sinks.raw-sink.data_model = dm-by-entity
cygnus-ngsi.sinks.raw-sink.batch_size = 10
cygnus-ngsi.sinks.raw-sink.batch_timeout = 5
cygnus-ngsi.sinks.raw-sink.batch_ttl = 0
```

An example of CartoDB keys configuration file could be (this can be generated from the configuration template distributed with Cygnus):

```
$ cat /usr/cygnus/conf/cartodb_keys.conf
{
   "cartodb_keys": [
      {
         "username": "user1",
         "endpoint": "https://user1.cartodb.com",
         "key": "1234567890abcdef"
      },
      {
         "username": "user2",
         "endpoint": "https://user2.cartodb.com",
         "key": "abcdef1234567890"
      }
   ]
}
```

[Top](#top)

###<a name="section2.2"></a>Use cases
The raw-based storing is addressed for those use cases simply wanting to save which were an entity's attribute values at certain time instant or geolocation. Of course, it allows for more complex analysis if experiencing computation time delays is not a problem: the data must be processed at querying time.

The above is avoided by the distance-based storing, which provides pre-computed aggregations regarding certain time instant or geolocation. Having pre-computed those aggregations highly improves the response time of the queries. This is suitable for queries such as:

* Which is the total amount of time this entity took to arrive to this point?
* Which was the average speed of this entity when passing through this point?
* Which was the entity with the highest maximum velocity at this time instant?
* Which is the largest stage an entity traveled?
* etc.

[Top](#top)

###<a name="section2.3"></a>Important notes
####<a name="section2.3.1"></a>`NGSICartoDBSink` and non-geolocated entities
It is mandatory the entities aimed to be handled by this sink have a geolocated attribute, either as a `geo:point`-typed attribute, either as an attribute holding a `location` metadata of type `string` and `WGS84` value.

[Top](#top)

####<a name="section2.3.2"></a>Multitenancy support
Different than other NGSI sinks, where a single authorized user is able to create user sapces and write data on behalf of all the other users (who can only read the data), this sink requires the writting credentials of each user and such user spaces created in advance. The reason is CartoDB imposes the database and schema upon user account creation, which typically are related to the FIWARE service (or FIWARE tenant ID), and the only persistence element Cygnus must create are the tables within the already provisiones databases and schemas. As can be inferred, accessing these databases and schemas require specific user credentials.

[Top](#top)

####<a name="section2.3.3"></a>Batching
As explained in the [programmers guide](#section3), `NGSICartoDBSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of inserts is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same CartoDB table. If processing the events one by one, we would need 100 inserts in CartoDB; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination CartoDB table. In the worst case, the whole 100 entities will be about 100 different entities (100 different CartoDB destinations), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batching mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `NGSICartoDBSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](../operation/performance_tuning_tips.md).

Finally, it must be said currenty batching only works with the raw-like storing. Due to the distance-like storing involves data aggregations based on the <i>previous</i> event, the sink currently relies in the PostgreSQL features of querying by the last event for implementing this kind of calculations. Of course, for the sake of performance this is expected to be done <i>inside</i> the sink, by temporarily pointing to the previous event in memory, which will be always faster than accessing the database.

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`NGSICartoDBSink` class
Coming soon.

[Top](#top)

###<a name="section3.2"></a>Authentication and authorization
Authentication is done by means of an API key related to the username. Once authenticated, the client is only allowed to create, read, update and delete PostgreSQL tables in the user space (PostgreSQL schema) within the organization (PostgreSQL database).

[Top](#top)
