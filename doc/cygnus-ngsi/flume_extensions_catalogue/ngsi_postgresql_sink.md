# <a name="top"></a>NGSIPostgreSQLSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [PostgreSQL data models](#section1.2)
        * [PostgreSQL databases naming conventions](#section1.2.1)
        * [PostgreSQL schemas naming conventions](#section1.2.2)
        * [PostgreSQL tables naming conventions](#section1.2.3)
        * [Row-like storing](#section1.2.4)
        * [Column-like storing](#section1.2.5)
        * [Native attribute type](#section1.2.6)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Database, schema and table names](#section1.3.2)
        * [Row-like storing](#section1.3.3)
        * [Column-like storing](#section1.3.4)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the persistence mode](#section2.3.2)
        * [About batching](#section2.3.3)
        * [Time zone information](#section2.3.4)
        * [About the encoding](#section2.3.5)
* [Programmers guide](#section3)
    * [`NGSIPostgreSQLSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)
    * [SSL/TLS connection](#section3.3)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIPostgreSQLSink`, or simply `NGSIPostgreSQLSink` is a sink designed to persist NGSI-like context data events within a [PostgreSQL server](https://www.postgresql.org/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific PostgreSQL data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2">PostgreSQL data models</a>
PostgreSQL organizes the data in schemas inside a database that contain tables of data rows. Such organization is exploited by `NGSIPostgreSQLSink` each time a `NGSIEvent` is going to be persisted, by mapping `NGSIEvent`s to PostgreSQL data structures. 

The name of these tables, schemas and databases depends on the configured data model, selected by defining the parameter `cygnus-ngsi.sinks.postgresql-sink.data_model` in the [`agent.conf`](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/ngsi_agent_conf.md) file or by defining the env var `CYGNUS_POSTGRESQL_DATA_MODEL`.

For a given entity, `entityname`, of type `entitytype`, inside a tenant `service` under `/servicepath`, how the information is stored in the database differs from using different datamodel configurations. The following table contains the database, schema and table names used for each datamodel supported by this sink:

| datamodel name                            | database name | schema  name | table name                        | note                                              |
|-------------------------------------------|---------------|--------------|-----------------------------------|---------------------------------------------------|
| by default                                | `agent.conf`  | service      | servicepath_entityname_entitytype | Automatic schema creation                         |
| `dm-by-fixed-entity-type-database-schema` | service       | servicepath | entitytype                        | Database and schema need to be created in advance |
| `dm-by-fixed-entity-type-database`        | service       | service      | entitytype                        | Database and schema need to be created in advance |
| `dm-by-fixed-entity-type`                 | `agent.conf`  | service      | entitytype                        | Automatic schema creation                         |
| `dm-by-entity-database-schema`            | service       | servicepath | servicepath_entityname_entitytype | Database and schema need to be created in advance |
| `dm-by-entity-database`                   | service       | service      | servicepath_entityname_entitytype | Database and schema need to be created in advance |
| `dm-by-entity-type-database-schema` | service | servicepath | servicepath_entitytype | Database and schema need to be created in advance |
| `dm-by-entity-type-database` | service | service | servicepath_entitytype | Database and schema need to be created in advance |
| `dm-by-entity-type`                       | `agent.conf`  | service      | servicepath_entitytype            | Automatic schema creation                         |
| `dm-by-service-path`                      | `agent.conf`  | service      | servicepath                       | Automatic schema creation                         |
| `dm-by-entity`                            | `agent.conf`  | service      | servicepath_entityname_entitytype | Automatic schema creation                         |
| `dm-by-attribute` | `agent.conf` | service | servicepath_entityname_entitytype_attribute | Automatic schema creation |

Where [`agent.conf`](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/ngsi_agent_conf.md) is the default database name selected in that file under the field `cygnus-ngsi.sinks.postgresql-sink.postgresql_database` or the env var `CYGNUS_POSTGRESQL_DATABASE`

[Top](#top)

#### <a name="section1.2.1"></a>PostgreSQL databases naming considerations

- Previous to any operation with PostgreSQL you need to create the database to be used.
- [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). This leads to  certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.
- PostgreSQL [databases name length](http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) is limited to 63 characters.
- Since version 2.2.0 It's added a new capability for Cygnus to create the schema and database name on runtime, this is possible trough enabling a specific Data Model on agent properties. See the [summary datamodels PostgreSQL data structure](#section1.2.4) section for more details.

[Top](#top)

#### <a name="section1.2.2"></a>PostgreSQL schemas naming considerations

- [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). This leads to  certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.
- PostgreSQL [schemas name length](http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) is limited to 63 characters.
- Since version 2.2.0 Cygnus creates the name of the schema name on runtime according to the selected DataModel for the sink. See the [summary datamodels PostgreSQL data structure](#section1.2) section for more details.

[Top](#top)

#### <a name="section1.2.3"></a>PostgreSQL tables naming considerations

- [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). This leads to  certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.
- PostgreSQL [tables name length](http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) is limited to 63 characters.

The following table summarizes the table name composition (old encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity`, `dm-by-entity-database-schema`, `dm-by-entity-database` | `dm-by-entity-type`, `dm-by-entity-type-database-schema`, `dm-by-entity-type-database` | `dm-by-fixed-entity-type`, `dm-by-fixed-entity-type-database-schema`, `dm-by-fixed-entity-type-database` |
|---|---|---|---|---|
| `/` | N/A | `<entityId>_<entityType>` | `<entityType>` | `<entityType>` |
| `/<svcPath>` | `<svcPath>` | `<svcPath>_<entityId>_<entityType>` | `<svcPath>_<entityType>` | `<entityType>` |

Using the new encoding:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity`, `dm-by-entity-database-schema`, `dm-by-entity-database` | `dm-by-entity-type`, `dm-by-entity-type-database-schema`, `dm-by-entity-type-database` | `dm-by-fixed-entity-type`, `dm-by-fixed-entity-type-database-schema`, `dm-by-fixed-entity-type-database` |
|---|---|---|---|---|
| `/` | `x002f` | `x002fxffff<entityId>xffff<entityType>` | `x002fxffff<entityType>` | `<entityType>` |
| `/<svcPath>` | `x002f<svcPath>` | `x002f<svcPath>xffff<entityId>xffff<entityType>` | `x002f<svcPath>xffff<entityType>` | `<entityType>` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities` header value within the `NGSIEvent`.

[Top](#top)

#### <a name="section1.2.4"></a>Row-like storing
Regarding the specific data stored within the above table, if `attr_persistence` parameter is set to `row` (default storing mode) then the notified data is stored attribute by attribute, composing an insert for each one of them. Each insert contains the following fields:

* `recvTimeTs`: UTC timestamp expressed in miliseconds.
* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `attrName`: Notified attribute name.
* `attrType`: Notified attribute type.
* `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
* `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted). Will be stored only if it was configured to (attr_metadata_store set to true in the configuration file ngsi_agent.conf). It is a Json object.

[Top](#top)

#### <a name="section1.2.5"></a>Column-like storing
Regarding the specific data stored within the above table, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: The notified one or the default one.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
*  For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.

#### <a name="section1.2.6"></a>Native attribute type
Regarding the specific data stored within the above table, if `attr_native_types` parameter is set to `true` then attribute is inserted using its native type (according with the following table), if `false` then will be stringify. 

Type json     | Type PostGreSQL/POSTGIS
------------- | --------------------------------------- 
string        | text
number        | double, precision, real, others (numeric, decimal)
boolean       | boolean (TRUE, FALSE, NULL)
DateTime      | timestamp, timestamp with time zone, timestamp without time zone
json          | text o json - it`s treated as String
null          | NULL

This only applies to Column mode.

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

#### <a name="section1.3.2"></a>Database, schema and table names
The PostgreSQL database name will be of the user's choice.

The PostgreSQL schema will always be `vehicles`.

The PostgreSQL table names will be, depending on the configured data model, the following ones (old encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-entity-type` | `dm-by-fixed-entity-type` |
|---|---|---|---|---|
| `/` | N/A | `car1_car` | `car` | `car` |
| `/4wheels` | `4wheels` | `4wheels_car1_car` | `4wheels_car` | `car` |

Using the new encoding:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-entity-type` | `dm-by-fixed-entity-type` |
|---|---|---|---|---|
| `/` | `x002f` | `x002fxffffcar1xffffcar` | `x002fxffffcar` | `car` |
| `/wheels` | `x002f4wheels` | `x002f4wheelsxffffcar1xffffcar` | `x002f4wheelsxffffcar` | `car` |

[Top](#top)

#### <a name="section1.3.3"></a>Row-like storing
Assuming `attr_persistence=row` as configuration parameters, then `NGSIPostgreSQLSink` will persist the data within the body as:

    $ psql -U myuser
    psql (9.5.0)
    Type "help" for help.
    postgres-# \c my-database

    my-database# \dn
       List of schemas
       Name   |  Owner
    ----------+----------
     vehicles | postgres
     public   | postgres
    (2 rows)

    my-database=# \dt vehicles.*
                   List of relations
      Schema  |       Name        | Type  |  Owner
    ----------+-------------------+-------+----------
     vehicles | 4wheels_car1_car  | table | postgres
    (1 row)

    postgresql> select * from vehicles.4wheels_car1_car;
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    | recvTimeTs | recvTime                   | fiwareServicePath | entityId | entityType | attrName    | attrType  | attrValue | attrMd |
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        |  speed      | float     | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        |  oil_level  | float     | 74.6      | []     |
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    2 row in set (0.00 sec)

[Top](#top)

#### <a name="section1.3.4"></a>Column-like storing
Coming soon.

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIPostgreSQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIPostgreSQLSink</i> |
| channel | yes | N/A ||
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| last\_data\_mode | no | upsert | <i>upsert</i> or <i>insert</i> or <i>both</i>, to set last data mode. Check this [link](./last_data_function.md) for more details. |
| last\_data\_table\_suffix | no | false | This suffix will be added to the table name in order to know where Cygnus will store the last record of an entity. Check this [link](./last_data_function.md) for more details. |
| last\_data\_unique\_key | no | entityId | This must be a unique key on the database to find when a previous record exists. Check this [link](./last_data_function.md) for more details. |
| last\_data\_timestamp\_key | no | recvTime | This must be a timestamp key on the aggregation to know which record is older. Check this [link](./last_data_function.md) for more details. |
| last\_data\_sql_timestamp\_format | no | YYYY-MM-DD HH24:MI:SS.MS | This must be a timestamp format to cast [SQL Text to timestamp](https://www.postgresql.org/docs/9.1/functions-formatting.html). Check this [link](./last_data_function.md) for more details. |
| data\_model | no | dm-by-entity | <i>dm-by-service-path</i> or <i>dm-by-entity</i> or <i>dm-by-entity-type</i> or <i>dm-by-entity-database</i> or <i>dm-by-entity-database-schema</i> or <i>dm-by-entity-type-database</i> or <i>dm-by-entity-type-database-schema</i>. <i>dm-by-service</i> and <dm-by-attribute</i> are not currently supported. |
| postgresql\_host | no | localhost | FQDN/IP address where the PostgreSQL server runs. |
| postgresql\_port | no | 5432 ||
| postgresql\_database | no | postgres | `postgres` is the default database that is created automatically when install. Note also than with datamodels <i>dm-by-entity-database</i>, <i>dm-by-entity-database-schema</i>, <i>dm-by-entity-type-database</i> and <i>dm-by-entity-type-database-schema</i> this setting is ignored (as the database is part of the mapping done by the datamodel) | |
| postgresql\_username | no | postgres | `postgres` is the default username that is created automatically when install |
| postgresql\_password | no | N/A | Empty value by default (No password is created when install) |
| postgresql\_maxPoolSize | no | 3 | Max number of connections per database pool |
| postgresql\_maxPoolIdle | no | 2 | Max number of connections idle per database pool |
| postgresql\_minPoolIdle | no | 0 | Min number of connections idle per database pool |
| postgresql\_minPoolIdleTimeMillis | no | 10000 | minimum amount of time an idle connection before is eligible for eviction |
| postgresql\_options | no | N/A | optional connection parameter(s) concatinated to jdbc url if necessary<br/>When `sslmode=require` is set to `postgresql_options`, jdbc url will become like <b>jdbc:postgresql://postgresql.example.com:5432/postgres?sslmode=require</b>|
| attr\_persistence | no | row | <i>row</i> or <i>column</i>. |
| attr\_metadata\_store | no | false | <i>true</i> or <i>false</i>. |
| attr\_native\_types | no | false | if the attribute value will be native <i>true</i> or stringfy or <i>false</i>. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| backend.enable\_cache | no | false | <i>true</i> or <i>false</i>, <i>true</i> enables the creation of a Cache, <i>false</i> disables the creation of a Cache. |
| persist\_errors | no | true | if there is an exception when trying to persist data into storage then error is persisted into a table |

A configuration example could be:

    cygnus-ngsi.sinks = postgresql-sink
    cygnus-ngsi.channels = postgresql-channel
    ...
    cygnus-ngsi.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIPostgreSQLSink
    cygnus-ngsi.sinks.postgresql-sink.channel = postgresql-channel
    cygnus-ngsi.sinks.postgresql-sink.enable_encoding = false
    cygnus-ngsi.sinks.postgresql-sink.enable_lowercase = false
    cygnus-ngsi.sinks.postgresql-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.postgresql-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.postgresql-sink.postgresql_host = 192.168.80.34
    cygnus-ngsi.sinks.postgresql-sink.postgresql_port = 5432
    cygnus-ngsi.sinks.postgresql-sink.postgresql_database = mydatabase
    cygnus-ngsi.sinks.postgresql-sink.postgresql_username = myuser
    cygnus-ngsi.sinks.postgresql-sink.postgresql_password = mypassword
    cygnus-ngsi.sinks.postgresql-sink.postgresql_maxPoolSize = 3
    cygnus-ngsi.sinks.postgresql-sink.postgresql_maxPoolIdle = 2
    cygnus-ngsi.sinks.postgresql-sink.postgresql_minPoolIdle = 0
    cygnus-ngsi.sinks.postgresql-sink.postgresql_minPoolIdleTimeMillis = 10000
    cygnus-ngsi.sinks.postgresql-sink.postgresql_options = sslmode=require
    cygnus-ngsi.sinks.postgresql-sink.attr_persistence = row
    cygnus-ngsi.sinks.postgresql-sink.attr_native_types = false
    cygnus-ngsi.sinks.postgresql-sink.batch_size = 100
    cygnus-ngsi.sinks.postgresql-sink.batch_timeout = 30
    cygnus-ngsi.sinks.postgresql-sink.batch_ttl = 10
    cygnus-ngsi.sinks.postgresql-sink.batch_retry_intervals = 5000
    cygnus-ngsi.sinks.postgresql.backend.enable_cache = false
    cygnus-ngsi.sinks.postgresql-sink.persist_errors = true

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIPostgreSQLSink` if you are looking for a big database with several tenants. PostgreSQL is bad at having several databases, but very good at having different schemas.

[Top](#top)

### <a name="section2.3"></a>Important notes

#### <a name="section2.3.2"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields data rows are inserted for each notified attribute. Nevertheless, the `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the table can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `NGSIPostgreSQLSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same PostgreSQL table. If processing the events one by one, we would need 100 inserts into PostgreSQL; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination PostgreSQL table. In the worst case, the whole 100 entities will be about 100 different entities (100 different PostgreSQL tables), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIPostgreSQLSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.4"></a>Time zone information
Time zone information is not added in PostgreSQL timestamps since PostgreSQL stores that information as a environment variable. PostgreSQL timestamps are stored in UTC time.

[Top](#top)

#### <a name="section2.3.4"></a>About the encoding
Until version 1.2.0 (included), Cygnus applied a very simple encoding:

* All non alphanumeric characters were replaced by underscore, `_`.
* The underscore was used as concatenator character as well.
* The slash, `/`, in the FIWARE service paths is ignored.

From version 1.3.0 (included), Cygnus applies this specific encoding tailored to PostgreSQL data structures:

* Lowercase alphanumeric characters are not encoded.
* Upercase alphanumeric characters are encoded.
* Numeric characters are not encoded.
* Underscore character, `_`, is not encoded.
* Equals character, `=`, is encoded as `xffff`.
* All other characters, including the slash in the FIWARE service paths, are encoded as a `x` character followed by the [Unicode](http://unicode-table.com) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* `xffff` is used as concatenator character.

Despite the old encoding will be deprecated in the future, it is possible to switch the encoding type through the `enable_encoding` parameter as explained in the [configuration](#section2.1) section.

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIPostgreSQLSink` class
As any other NGSI-like sink, `NGSIPostgreSQLSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the PostgreSQL table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `PostgreSQLBackend` implementation.

    public void start();

An implementation of `PostgreSQLBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIPostgreSQLSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIPostgreSQLSink` relies on the database, username and password credentials created at the PostgreSQL endpoint.

### <a name="section3.3"></a>SSL/TLS connection
When `NGSIPostgreSQLSink` want to connect PostgreSQL Server by using SSL or TLS, please set `postgresql_options` configuration parameter to configure jdbc.


[Top](#top)
