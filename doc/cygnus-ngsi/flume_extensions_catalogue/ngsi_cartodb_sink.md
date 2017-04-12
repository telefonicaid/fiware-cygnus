# <a name="top"></a>NGSICartoDBSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to Carto data structures](#section1.2)
        * [PostgreSQL databases and schemas naming conventions](#section1.2.1)
        * [PostgreSQL tables naming conventions](#section1.2.2)
        * [Raw-based storing](#section1.2.3)
        * [Distance-based storing](#section1.2.4)
        * [Raw snapshot-based storing](#section1.2.5)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Table names](#section1.3.2)
        * [Raw-based storing](#section1.3.3)
        * [Distance-based storing](#section1.3.4)
        * [Raw snapshot-based storing](#section1.3.5)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [`NGSICartoDBSink` and non-geolocated entities](#section2.3.1)
        * [Multitenancy support](#section2.3.1)
        * [Batching](#section2.3.3)
        * [About the encoding](#senction2.3.4)
        * [About automatically creating the tables](#section2.3.5)
        * [Supported Orion's geometries](#section2.3.6)
* [Programmers guide](#section3)
    * [`NGSICartoDBSSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)
* [Annexes](#section4)
    * [Annex 1: provisioning a table](#section4.1)

## <a name="section2"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSICartoDBSink`, or simply `NGSICartoDBSSink` is a cygnus-ngsi sink designed to persist NGSI-like context data events within [Carto](https://carto.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at cygnus-ngsi sources. In the end, the information within these events must be mapped into specific Carto data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to Carto data structures
Carto is based on [PostgreSQL](http://www.postgresql.org/) and [PostGIS](http://postgis.net/) extensions. It organizes the data in databases (one per organization), schemas (one per user within an organization) and tables (a schema may have one or more tables). Such organization is exploited by `NGSICartoDBSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>PostgreSQL databases and schemas naming conventions
PostgreSQL databases and schemas are already created by Carto upon organization and username request, respectively. Thus, it is up to Carto to define the naming conventions for these elements; specifically:

* Organization must only contain lowercase letters.
* Username must only contain lowercase letters, numbers and the dash symbol (`-`).

Here it is assumed the notified/default FIWARE service maps the PostgreSQL schema/username, ensuring this way multitenancy and data isolation. This multitenancy approach is complemented by the usage of a configuration file holding the mapping between FIWARE service/Carto username and API Key (please, check the [Configuration](#section2.1) section).

[Top](#top)

#### <a name="section1.2.2"></a>PostgreSQL tables naming conventions
The name of these tables depends on the configured data model and analysis mode (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](./ngsi_rest_handler.md)) is used as the name of the table. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table.
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the table name. If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated.

The above applies independently of the analysis modes enabled (`enable_raw`, `enable_distance` and `enable_raw_snapshot`). Nevertheless:

* The distance analysis mode adds the sufix `xffffdistance` to the table name.
* The raw snapshot analysis mode adds the sufix `xffffrawsnapshot` to the table name.

Since based in [PostgreSQL](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS), it must be said only alphanumeric characters and the underscore (`_`) are accepted. This leads to certain [encoding](#section2.3.4) is applied.

PostgreSQL [tables name length](http://dev.mysql.com/doc/refman/5.7/en/identifiers.html) is limited to 64 characters.

The following table summarizes the table name composition:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` |
|---|---|---|
| `/` | `x002f` | `x002fxffff<entityId>xffff<entityType>[xffffdistance]` |
| `/<svcPath>` | `x002fxffff<svcPath>[xffffdistance|xffffrawsnapshot]` | `x002fxffff<svcPath>xffff<entityId>xffff<entityType>[xffffdistance]` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSIEvent`.

[Top](#top)

#### <a name="section1.2.3"></a>Raw-based storing
Regarding the specific data stored within the tables, if `enable_raw` parameter is set to `true` (default storing mode) then the notified data is **stored as it is, without any processing or modification**. This is the simplest way of storing geolocation data.

A single insert is composed for each notified entity, containing such insert the following fields:

* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `the_geom`: The current geolocation point, i.e. containing longitude and latitude. It must be of type PostGIS Geometry, but it must only contain points. It can be created for instance using [`ST_Point()`](http://postgis.net/docs/ST_Point.html). The data used for composing the PostGIS Geometry is obtained from a special notified attribute:
    * Either of type `geo:point` (a point).
    * Either of type `geo:json` (a GeoJson representing a point).
    * Either having associated a `location` metadata of type `string` and value `WGS84`. Please observe this option is [deprecated](https://fiware-orion.readthedocs.io/en/master/deprecated/index.html).
* For each not-geolocated attribute, the insert will contain two additional fields, one for the value, named `<attrName>`, and another for the metadata, named `<attrName>_md`).

It must be said Cygnus does not create Carto tables in the raw-based storing. The reason is Cygnus is not able to infer from a notification the complete set of attributes an entity has, i.e. the columns cannot be inferred. Thus, tables must be preprovisioned in advance (please, check the Annex 1 for specific Carto queries).

[Top](#top)

#### <a name="section1.2.4"></a>Distance-based storing
If `enable_distance` parameter is set to `true` (by default, this kind of storing is not run) then the notified data is processed based on a distance analysis. As said, the linear distance and elapsed time with regards to the previous geolocation of the entity is obtained, and this information is used to update certain aggregations: total amount of distance, total amount of time and many others. The speed is obtained as well as the result of dividing the distance by the time, and such speed calculation is used as well for updating certain aggregations.

The final goal is to **pre-compute a set of distance-based measures** as a function of the geolocation of an entity, allowing for querying about <i>"the total amount of time this entity took to arrive to this point"</i>, or <i>"which was the average speed of this entity when passing through this point"</i>, etc. **without performing any computation at querying time**.

A single insert is composed for each notified entity, containing such insert the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `the_geom`: The current geolocation point, i.e. containing longitude and latitude. It must be of type PostGIS Geometry, but it must only contain points. It can be created for instance using [`ST_Point()`](http://postgis.net/docs/ST_Point.html). The data used for composing the PostGIS Geometry is obtained from a special notified attribute:
    * Either of type `geo:point` (a point).
    * Either of type `geo:json` (a GeoJson representing a point).
    * Either having associated a `location` metadata of type `string` and value `WGS84`. Please observe this option is [deprecated](https://fiware-orion.readthedocs.io/en/master/deprecated/index.html).
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

Different than the raw-based storing, Cygnus is able to create by itself the tables used by the distance-based storing. The reason is columns of the tables are well known in advance.

[Top](#top)

#### <a name="section1.2.5"></a>Raw snapshot-based storing
This analysis mode works the same than the raw-based storing one, except for:

* There is not a table per entity, but a table per FIWARE service path. In these sense, this analysis mode can be seen as always working with the `data_model` parameter set to `dm-by-service-path`.
* The notified data is not added as a new record in the table, but it is used for updating an already existent record (of course, if there is no previous record for a give FIWARE service path, entity ID and entity type, the record is added to the table).

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

#### <a name="section1.3.2"></a>Table names
The PostgreSQL table names will be, depending on the configured data model and analysis mode, the following ones:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` |
|---|---|---|
| `/` | `x002f` | `x002fcar1xffffcar[xffffdistance]` |
| `/4wheels` | `x002f4wheels[xffffdistance]` | `x002f4wheelsxffffcar1xffffcar[xffffdistance]` |

[Top](#top)

#### <a name="section1.3.3"></a>Raw historic-based storing
Let's assume a table name `x002f4wheelsxffffcar1xffffcar` (data model by entity, non-root service path, only raw historic analysis mode). The data stored within this table would be:

```
curl "https://myusername.cartodb.com/api/v2/sql?q=select * from x002f4wheelsxffffcar1xffffcar&api_key=abcdef0123456789"
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

#### <a name="section1.3.4"></a>Distance historic-based storing
Let's assume a table name `x002f4wheelsxffffcar1xffffcarxffffdistance` (data model by entity, non-root service path, only distance historic analysis mode) with a previous insertion (on the contrary, this would be the first insertion and almost all the aggregated values will be set to 0). The data stored within this table would be:

```
curl "https://myusername.cartodb.com/api/v2/sql?q=select * from x002f4wheelsxffffcar1xffffcarxffffdistance&api_key=abcdef0123456789"
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

#### <a name="section1.3.5"></a>Raw snapshot-based storing
Everything equals to the raw historic-based storing, but:

* The table name is `x002f4wheelsxffffcar1xffffcarxffffrawsnapshot`.
* The data is inserted if the given FIWARE service path, entity ID and entity type are not present in the table; used for update otherwise.

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSICartoDBSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be `com.telefonica.iot.cygnus.sinks.NGSICartoDBSink` |
| channel | yes | N/A ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity |  <i>dm-by-service-path</i> or <i>dm-by-entity</i>. |
| keys\_conf\_file | yes | N/A | Absolute path to the CartoDB file containing the mapping between FIWARE service/Carto usernames, endpoints, API Keys and account types. |
| swap\_coordinates | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, the latitude and longitude values are exchanged. |
| flip\_coordinates | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, the latitude and longitude values are exchanged. **Deprecated from release 1.6.0 in favour of `swap_coordinates`**. |
| enable\_raw\_historic | no | true | <i>true</i> or <i>false</i>. If <i>true</i>, a raw hiatoric-based storage is done. |
| enable\_raw | no | true | <i>true</i> or <i>false</i>. If <i>true</i>, a raw-based storage is done. **Deprecated from release 1.8.0 in favour of `enable_raw_historic`**. |
| enable\_distance\_historic | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, a distance historic-based storage is done. |
| enable\_distance | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, a distance-based storage is done. **Deprecated from release 1.8.0 in favour of `enable_distance_historic`**. |
| enable\_raw\_snapshot | no | false | <i>true</i> or <i>false</i>. If <i>true</i>, a raw snapshot based storage is done. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| backend.max\_conns | no | 500 | Maximum number of connections allowed for a Http-based HDFS backend. |
| backend.max\_conns\_per\_route | no | 100 | Maximum number of connections per route allowed for a Http-based HDFS backend. |

A configuration example could be:

```
cygnus-ngsi.sinks = cartodb-sink
cygnus-ngsi.channels = cartodb-channel
...
cygnus-ngsi.sinks.cartodb-sink.channel = cartodb-channel
cygnus-ngsi.sinks.cartodb-sink.type = com.telefonica.iot.cygnus.sinks.NGSICartoDBSink
cygnus-ngsi.sinks.cartodb-sink.enable_grouping = false
cygnus-ngsi.sinks.cartodb-sink.enable_name_mappings = false
cygnus-ngsi.sinks.cartodb-sink.enable_lowercase = false
cygnus-ngsi.sinks.cartodb-sink.keys_conf_file = /usr/cygnus/conf/cartodb_keys.conf
cygnus-ngsi.sinks.cartodb-sink.swap_coordinates = true
cygnus-ngsi.sinks.cartodb-sink.enable_raw_historic = true
cygnus-ngsi.sinks.cartodb-sink.enable_distance_historic = false
cygnus-ngsi.sinks.cartodb-sink.enable_raw_snapshot = false
cygnus-ngsi.sinks.cartodb-sink.data_model = dm-by-entity
cygnus-ngsi.sinks.cartodb-sink.batch_size = 10
cygnus-ngsi.sinks.cartodb-sink.batch_timeout = 5
cygnus-ngsi.sinks.cartodb-sink.batch_ttl = 0
cygnus-ngsi.sinks.cartodb-sink.batch_retries_intervals = 5000
cygnus-ngsi.sinks.cartodb-sink.backend.max_conns = 500
cygnus-ngsi.sinks.cartodb-sink.backend.max_conns_per_route = 100
```

An example of Carto keys configuration file could be (this can be generated from the configuration template distributed with Cygnus):

```
$ cat /usr/cygnus/conf/cartodb_keys.conf
{
   "cartodb_keys": [
      {
         "username": "user1",
         "endpoint": "https://user1.cartodb.com",
         "key": "1234567890abcdef",
         "type": "personal"
      },
      {
         "username": "user2",
         "endpoint": "https://user2.cartodb.com",
         "key": "abcdef1234567890",
         "type": "enterprise"
      }
   ]
}
```

[Top](#top)

### <a name="section2.2"></a>Use cases
The raw-based storing is addressed for those use cases simply wanting to save which were an entity's attribute values at certain time instant or geolocation. Of course, it allows for more complex analysis if experiencing computation time delays is not a problem: the data must be processed at querying time.

The above is avoided by the distance-based storing, which provides pre-computed aggregations regarding certain time instant or geolocation. Having pre-computed those aggregations highly improves the response time of the queries. This is suitable for queries such as:

* Which is the total amount of time this entity took to arrive to this point?
* Which was the average speed of this entity when passing through this point?
* Which was the entity with the highest maximum velocity at this time instant?
* Which is the largest stage an entity traveled?
* etc.

Finally, the raw snapshot storing simply geolocates an entity over time, without caring about the history.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>`NGSICartoDBSink` and non-geolocated entities
It is mandatory the entities aimed to be handled by this sink have a geolocated attribute, either as a `geo:point`-typed attribute, either as an attribute holding a `location` metadata of type `string` and `WGS84` value.

[Top](#top)

#### <a name="section2.3.2"></a>Multitenancy support
Different than other NGSI sinks, where a single authorized user is able to create user spaces and write data on behalf of all the other users (who can only read the data), this sink requires the writing credentials of each user and such user spaces created in advance. The reason is Carto imposes the database and schema upon user account creation, which typically are related to the FIWARE service (or FIWARE tenant ID), and the only persistence element Cygnus must create are the tables within the already provisiones databases and schemas. As can be inferred, accessing these databases and schemas require specific user credentials.

User credentials must be added to a special file that will be pointed by the Carto sink through the `keys_conf_file` configuration parameter. Of special interest is the account type, which can be `personal` or `enterprise`; such a distinction is important since the queries to the API differ from one to the other.

[Top](#top)

#### <a name="section2.3.3"></a>Batching
As explained in the [programmers guide](#section3), `NGSICartoDBSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of inserts is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same Carto table. If processing the events one by one, we would need 100 inserts in Carto; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination Carto table. In the worst case, the whole 100 entities will be about 100 different entities (100 different Carto destinations), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batching mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSICartoDBSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

Finally, it must be said currently batching only works with the raw-like storing. Due to the distance-like storing involves data aggregations based on the <i>previous</i> event, the sink currently relies in the PostgreSQL features of querying by the last event for implementing this kind of calculations. Of course, for the sake of performance this is expected to be done <i>inside</i> the sink, by temporarily pointing to the previous event in memory, which will be always faster than accessing the database.

[Top](#top)

#### <a name="section2.3.4"></a>About the encoding
Cygnus applies this specific encoding tailored to Carto data structures:

* Lowercase alphanumeric characters are not encoded.
* Upercase alphanumeric characters are encoded.
* Numeric characters are not encoded.
* Underscore character, `_`, is not encoded.
* Equals character, `=`, is encoded as `xffff`.
* All other characters, including the slash in the FIWARE service paths, are encoded as a `x` character followed by the [Unicode](http://unicode-table.com) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* `xffff` is used as concatenator character.

[Top](#top)

#### <a name="section2.3.5"></a>About automatically creating the tables
It has already been commented, but just a reminder: Cygnus does not automatically create the required tables for the raw-based nor the raw snapshot-based mode. This is because the first notification regarding an entity could not contain the full list of such an entity's attributes, i.e. only the updated attributes could be being notified.

On the contrary, the distance-based mode automatically creates the tables since the number and semantic of the table columns is always the same, and it is independent of the entity's attributes.

When required, the Annex 1 shows how to provision a table for Carto, among other interesting operations.

[Top](#top)

#### <a name="section2.3.6"></a>Supported Orion's geometries
Current version of `NGSICartoDBSink` supports the following NGSIv2 geometries:

* `geo:point`, in this case the geolocated attribute is about a single point.
* `geo:json`, despite a GeoJson can describe any geometry, from a simple point to a complex polygon, it must represent a single point.
* `location` metadata, in this case the geolocated attribute is about a single point.

You can get more information at [NGSIv2](http://telefonicaid.github.io/fiware-orion/api/v2/stable/) specification (<i>Geospatial properties of entities</i> section).

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSICartoDBSink` class
As any other NGSI-like sink, `NGSICartoDBSink ` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the CartoDB table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `CartoDBBackend` implementation.

    public void start();

An implementation of `CartoDBBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSICartoDBSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Authentication is done by means of an API key related to the username. Once authenticated, the client is only allowed to create, read, update and delete PostgreSQL tables in the user space (PostgreSQL schema) within the organization (PostgreSQL database).

[Top](#top)

## <a name="section4"></a>Annexes
### <a name="section4.1"></a>Annex 1: provisioning a table in Carto
Following you may find the queries required to provision a table in Carto. Start by creating the table:

    $ curl -G "https://<my_user>.cartodb.com/api/v2/sql?api_key=<api_key>" --data-urlencode "q=CREATE TABLE <table_name> (recvTime text, fiwareServicePath text, entityId text, entityType text, <attr_1> <type_1>, <attr_1>_md text, ..., <attr_n> <type_n>, <attr_n>_md text, the_geom geometry(POINT,4326))"

Every table in Carto has to be <i>cartodbfied</i>, if you want it appears in Carto web-based dashboard:

    $ curl -G "https://<my_user>.cartodb.com/api/v2/sql?api_key=<api_key>" --data-urlencode "q=SELECT CDB_CartodbfyTable('<my_user_or_schema>', '<table_name>')"

Now, you should be able to insert some data (just for testing purpose, since Cygnus will be in charge of this part):

    $ curl -G "https://<my_user>.cartodb.com/api/v2/sql?api_key=<api_key>" --data-urlencode "q=INSERT INTO <table_name> (recvTime, fiwareServicePath, entityId, entityType, <attr_1>, <attr_1>_md, ..., <attr_n>, <attr_n>_md, the_geom) VALUES ('2016-04-19T07:09:53.116Z', '<service_path>', '<entity_id>', '<entity_type>', '<attr_1_value>', '<attr_1_metadata>', ..., '<attr_n_value>', '<attr_n_metadata>', 'ST_SetSRID(ST_MakePoint(<lat>, <lon>), 4326))"

You can query the data as:

    $ curl -G "https://<my_user>.cartodb.com/api/v2/sql?api_key=<api_key>" --data-urlencode "q=SELECT * FROM <table_name>"

For completeness, let's see how to delete a table:

    $ curl -G "https://<my_user>.cartodb.com/api/v2/sql?api_key=<api_key>" --data-urlencode "q=DROP TABLE <table_name>"

[Top](#top)
