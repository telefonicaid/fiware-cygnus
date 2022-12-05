# <a name="top"></a>NGSIOracleSQLSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to Oracle data structures](#section1.2)
        * [Oracle databases naming conventions](#section1.2.1)
        * [Oracle tables naming conventions](#section1.2.2)
        * [Row-like storing](#section1.2.3)
        * [Column-like storing](#section1.2.4)
        * [Native attribute type](#section1.2.5)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Database and table names](#section1.3.2)
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
        * [About capping resources/expirating records](#section2.3.6)
* [Programmers guide](#section3)
    * [`NGSIOracleSQLSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIOracleSQLSink`, or simply `NGSIOracleSQLSink` is a sink designed to persist NGSI-like context data events within a [Oracle server](https://www.oracle.com/) 11g and 12c legacy versions. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific Oracle data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to Oracle data structures
Oracle organizes the data in databases that contain tables of data rows. Such organization is exploited by `NGSIOracleSQLSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>Oracle databases naming conventions
A database named as the notified `fiware-service` header value (or, in absence of such a header, the defaulted value for the FIWARE service) is created (if not existing yet).

It must be said Oracle [only accepts](https://docs.oracle.com/cd/E92917_01/PDF/8.1.x.x/common/HTML/DM_Naming/2_Table_and_Column_Naming_Standards.htm) alphanumerics `$`, `_` and `#`. This leads to certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.

Oracle prior version to 12.2 [databases name length](https://docs.oracle.com/en/database/oracle/oracle-database/21/odpnt/EFCoreIdentifier.html) is limited to 30 characters.

[Top](#top)

#### <a name="section1.2.2"></a>Oracle tables naming conventions
The name of these tables depends on the configured data model (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](./ngsi_rest_handler.md) is used as the name of the table. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table. The only constraint regarding this data model is the FIWARE service path cannot be the root one (`/`).
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the table name. The concatenation character is `_` (underscore). If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated.
* Data model by entity type (`data_model=dm-by-entity-type`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity type in order to compose the table name. The concatenation character is `_` (underscore). If the FIWARE service path is the root one (`/`) then only the entity type is concatenated.

It must be said Oracle [only accepts](https://docs.oracle.com/cd/E92917_01/PDF/8.1.x.x/common/HTML/DM_Naming/2_Table_and_Column_Naming_Standards.htm) alphanumerics `$`, `_` and `#`. This leads to certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.

Oracle prior version to 12.2 [databases name length](https://docs.oracle.com/en/database/oracle/oracle-database/21/odpnt/EFCoreIdentifier.html) is limited to 30 characters.


The following table summarizes the table name composition (old encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-entity-type` |
|---|---|---|---|
| `/` | N/A | `<entityId>_<entityType>` | `<entityType>` |
| `/<svcPath>` | `<svcPath>` | `<svcPath>_<entityId>_<entityType>` | `<svcPath>_<entityType>` |

The following table summarizes the table name composition (new encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-entity-type` |
|---|---|---|---|
| `/` | `x002f` | `x002fxffff<entityId>xffff<entityType>` | `x002fxffff<entityType>` |
| `/<svcPath>` | `x002f<svcPath>` | `x002f<svcPath>xffff<entityId>xffff<entityType>` |`x002f<svcPath>xffff<entityType>` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities` header value within the `NGSIEvent`.

[Top](#top)

#### <a name="section1.2.3"></a>Row-like storing
Regarding the specific data stored within the above table, if `attr_persistence` parameter is set to `row` (default storing mode) then the notified data is stored attribute by attribute, composing an insert for each one of them. Each insert contains the following fields:

* `recvTimeTs`: UTC timestamp expressed in miliseconds.
* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `attrName`: Notified attribute name.
* `attrType`: Notified attribute type.
* `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
* `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted). Will be stored only if it was configured to (attr_metadata_store set to true in the configuration file ngsi_agent.conf). It is a Json object.

[Top](#top)

#### <a name="section1.2.4"></a>Column-like storing
Regarding the specific data stored within the above table, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: Timestamp in human-readable format (Similar to [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601), but avoiding the `Z` character denoting UTC, since all Oracle timestamps are supposed to be in UTC format).
* `fiwareServicePath`: The notified one or the default one.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
*  For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.

[Top](#top)



#### <a name="section1.2.5">Native types

Regarding the specific data stored within the above table, if `attr_native_types` parameter is set to `true` then attribute is inserted using its native type (according with the following table), if `false` then will be stringify.

Type json     | Type
------------- | --------------------------------------- 
string        | varchar, varchar2, clob
number        | NUMBER(precision, scale)
boolean       | boolean (TRUE, FALSE, YES, NO, ON, OFF)
DateTime      | timestamp, timestamp with time zone, timestamp without time zone
json          | varchar, varchar2, clob - it`s treated as String
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

#### <a name="section1.3.2"></a>Database and table names
The Oracle database name will always be `vehicles`.

The Oracle table names will be, depending on the configured data model, the following ones (old encoding):

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-entity-type` |
|---|---|---|---|
| `/` | N/A | `car1_car` | `car` |
| `/4wheels` | `4wheels` | `4wheels_car1_car` | `4wheels_car` |

Using the new encoding:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` | `dm-by-entity` |
|---|---|---|---|
| `/` | `x002f` | `x002fxffffcar1xffffcar` | `x002fxffffcar` |
| `/wheels` | `x002f4wheels` | `x002f4wheelsxffffcar1xffffcar` | `x002f4wheelsxffffcar` |

[Top](#top)

#### <a name="section1.3.3"></a>Row-like storing
Assuming `attr_persistence=row` as configuration parameter, then `NGSIOracleSQLSink` will persist the data within the body as:

    sqlplus> select * from 4wheels_car1_car;
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    | recvTimeTs | recvTime                   | fiwareServicePath | entityId | entityType | attrName    | attrType  | attrValue | attrMd |
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        |  speed      | float     | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        |  oil_level  | float     | 74.6      | []     |
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    2 row in set (0.00 sec)

[Top](#top)

#### <a name="section1.3.4"></a>Column-like storing
If `attr_persistence=colum` then `NGSIOracleSQLSink` will persist the data within the body as:

    sqlplus> select * from 4wheels_car1_car;
    +----------------------------+-------------------+----------+------------+-------+----------+-----------+--------------+
    | recvTime                   | fiwareServicePath | entityId | entityType | speed | speed_md | oil_level | oil_level_md |
    +----------------------------+-------------------+----------+------------+-------+----------+-----------+--------------+
    | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        | 112.9 | []       |  74.6     | []           |
    +----------------------------+-------------------+----------+------------+-------+----------+-----------+--------------+
    1 row in set (0.00 sec)

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIOracleSQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIOracleSQLSink</i> |
| channel | yes | N/A ||
| enable_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| last\_data\_mode | no | insert | <i>insert</i>  to set last data mode. Check this [link](./last_data_function.md) for more details. In oracle sink <i>both</i> and <i>upsert</i> are not avaiable |
| last\_data\_table\_suffix | no | false | This suffix will be added to the table name in order to know where Cygnus will store the last record of an entity. Check this [link](./last_data_function.md) for more details. |
| last\_data\_unique\_key | no | entityId | This must be a unique key on the database to find when a previous record exists. Check this [link](./last_data_function.md) for more details. |
| last\_data\_timestamp\_key | no | recvTime | This must be a timestamp key on the aggregation to know which record is older. Check this [link](./last_data_function.md) for more details. |
| last\_data\_sql_timestamp\_format | no | YYYY-MM-DD HH24:MI:SS.MS | This must be a timestamp format to cast [SQL Text to timestamp](https://dev.oracle.com/doc/refman/8.0/en/date-and-time-functions.html). Check this [link](./last_data_function.md) for more details. |
| data\_model | no | dm-by-entity | <i>dm-by-service-path</i>, <i>dm-by-entity</i> or <i>dm-by-entity-type</i>. <i>dm-by-service</i> and <dm-by-attribute</i> are not currently supported. |
| oracle\_host | no | localhost | FQDN/IP address where the Oracle server runs |
| oracle\_port | no | 1521 ||
| oracle\_username | no |  system | `system` is the default username that is created automatically |
| oracle\_password | no | oracle | `oracle` is the default for default username |
| oracle\_database | no | xe | `xe` is the default database avaiable in oracle 11g XE (express edition) |
| oracle\_maxPoolSize | no | 3 | Max number of connections per database pool |
| oracle\_options | no | N/A | optional connection parameter(s) concatinated to jdbc url if necessary<br/>When `useSSL=true&requireSSL=false` is set to `oracle_options`, jdbc url will become like <b>jdbc:oracle://oracle.example.com:3306/fiwareservice?useSSL=true&requireSSL=false</b>|
| attr\_persistence | no | row | <i>row</i> or <i>column</i>
| attr\_metadata\_store | no | false | <i>true</i> or <i>false</i>. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| persistence\_policy.max_records | no | -1 | Maximum number of records allowed for a table before it is capped. `-1` disables this policy. |
| persistence\_policy.expiration_time | no | -1 | Maximum number of seconds a record is maintained in a table before expiration. `-1` disables this policy. |
| persistence\_policy.checking_time | no | 3600 | Frequency (in seconds) at which the sink checks for record expiration. |
| attr\_native\_types | no | false | if the attribute value will be native <i>true</i> or stringfy or <i>false</i>. When set to true, in case batch option is activated, insert null values for those attributes that doesn't exist in some of the entities to be inserted. If set to false, '' value is inserted for missing attributes. |
| persist\_errors | no | true | if there is an exception when trying to persist data into storage then error is persisted into a table |
| oracle_locator | no | false | if there is avaiable of [Oracle locator feature](https://docs.oracle.com/database/121/SPATL/sdo_locator.htm#SPATL340) which is just avaible since oracle 12c. THis imples if a geo:json is in converted a SDO_GEOMETRY or just leave in string format. |
| oracle_major_version | no | 11 | Major version of Oracle (it defines some values like max name length for identifiers, whichs is 30 for versions prior to 12)
| nls_timestamp_format | no | `YYYY-MM-DD HH24:MI:SS.FF6` | defines the default timestamp format to use with the TO_CHAR and TO_TIMESTAMP functions [nls_timestamp_format](https://docs.oracle.com/cd/B19306_01/server.102/b14237/initparams132.htm#REFRN10131) |
| nls_timestamp_tz_format | no | `YYYY-MM-DD"T"HH24:MI:SS.FF6 TZR` | NLS_TIMESTAMP_TZ_FORMAT defines the default timestamp with time zone format to use with the TO_CHAR and TO_TIMESTAMP_TZ functions [nls_timestamp_tz_format](https://docs.oracle.com/database/121/REFRN/GUID-A340C735-BA5A-4704-B24C-AC2C2380BA9E.htm#REFRN10132)|

A configuration example could be:

    cygnus-ngsi.sinks = oracle-sink
    cygnus-ngsi.channels = oracle-channel
    ...
    cygnus-ngsi.sinks.oracle-sink.type = com.telefonica.iot.cygnus.sinks.NGSIOracleSQLSink
    cygnus-ngsi.sinks.oracle-sink.channel = oracle-channel
    cygnus-ngsi.sinks.oracle-sink.enable_encoding = false
    cygnus-ngsi.sinks.oracle-sink.enable_lowercase = false
    cygnus-ngsi.sinks.oracle-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.oracle-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.oracle-sink.oracle_host = 192.168.80.34
    cygnus-ngsi.sinks.oracle-sink.oracle_port = 1521
    cygnus-ngsi.sinks.oracle-sink.oracle_database = xe
    cygnus-ngsi.sinks.oracle-sink.oracle_username = system
    cygnus-ngsi.sinks.oracle-sink.oracle_password = oracle
    cygnus-ngsi.sinks.oracle-sink.oracle_locator = false
    cygnus-ngsi.sinks.oracle-sink.nl_timestamp_format = YYYY-MM-DD HH24:MI:SS.FF6
    cygnus-ngsi.sinks.oracle-sink.nl_timestamp_tz_format = YYYY-MM-DD\"T\"HH24:MI:SS.FF6 TZR
    cygnus-ngsi.sinks.oracle-sink.oracle_maxPoolSize = 3
    cygnus-ngsi.sinks.oracle-sink.attr_persistence = column
    cygnus-ngsi.sinks.oracle-sink.attr_native_types = false
    cygnus-ngsi.sinks.oracle-sink.batch_size = 100
    cygnus-ngsi.sinks.oracle-sink.batch_timeout = 30
    cygnus-ngsi.sinks.oracle-sink.batch_ttl = 10
    cygnus-ngsi.sinks.oracle-sink.batch_retry_intervals = 5000
    cygnus-ngsi.sinks.oracle-sink.persistence_policy.max_records = 5
    cygnus-ngsi.sinks.oracle-sink.persistence_policy.expiration_time = 86400
    cygnus-ngsi.sinks.oracle-sink.persistence_policy.checking_time = 600
    cygnus-ngsi.sinks.oracle-sink.persist_errors = true

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIOracleSQLSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

### <a name="section2.3"></a>Important notes

#### <a name="section2.3.2"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields data rows are inserted for each notified attribute. Nevertheless, the `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the table can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `NGSIOracleSQLSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same Oracle table. If processing the events one by one, we would need 100 inserts into Oracle; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination Oracle table. In the worst case, the whole 100 entities will be about 100 different entities (100 different Oracle tables), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIOracleSQLSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.4"></a>Time zone information
Timezone in oracle is defiend by NLS_LANG environment variable of database instance.
Timestamp and timestampTZ formats are defined by NLS_TIMESTAMP_FORMAT and NLS_TIMESTAMP_TZ_FORMAT environment variables of database instance as well as each connection session.
More about [time tonze information](https://docs.oracle.com/cd/E11882_01/server.112/e10729/ch4datetime.htm#NLSPG004).

[Top](#top)

#### <a name="section2.3.5"></a>About the encoding
Until version 1.2.0 (included), Cygnus applied a very simple encoding:

* All non alphanumeric characters were replaced by underscore, `_`.
* The underscore was used as concatenator character as well.
* The slash, `/`, in the FIWARE service paths is ignored.

From version 1.3.0 (included), Cygnus applies this specific encoding tailored to Oracle data structures:

* Alphanumeric characters are not encoded.
* Numeric characters are not encoded.
* Underscore character, `_`, is not encoded.
* Equals character, `=`, is encoded as `xffff`.
* All other characters, including the slash in the FIWARE service paths, are encoded as a `x` character followed by the [Unicode](http://unicode-table.com) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* All the other characters are not encoded.
* `xffff` is used as concatenator character.
    
Despite the old encoding will be deprecated in the future, it is possible to switch the encoding type through the `enable_encoding` parameter as explained in the [configuration](#section2.1) section.

[Top](#top)

#### <a name="section2.3.6"></a>About capping resources and expirating records
Capping and expiration are disabled by default. Nevertheless, if desired, this can be enabled:

* Capping by the number of records. This allows the resource growing up until certain configured maximum number of records is reached (`persistence_policy.max_records`), and then maintains such a constant number of records.
* Expirating by time the records. This allows the resource growing up until records become old, i.e. exceed certain configured expiration time (`persistence_policy.expiration_time`).

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIOracleSQLSink` class
As any other NGSI-like sink, `NGSIOracleSQLSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the Oracle table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `OracleBackend` implementation.

    void capRecords(NGSIBatch batch, long maxRecords) throws EventDeliveryException;
    
This method is always called immediatelly after `persistBacth()`. The same destination tables that were upserted are now checked in terms of number of records: if the configured maximum (`persistence_policy.max_records`) is exceeded for any of the updated tables, then as many oldest records are deleted as required until the maximum number of records is reached.
    
    void expirateRecords(long expirationTime);
    
This method is called in a periodical way (based on `persistence_policy.checking_time`), and if the configured expiration time (`persistence_policy.expiration_time`) is exceeded for any of the records within any of the tables, then it is deleted.

    public void start();

An implementation of `OracleBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIOracleSQLSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIOracleSQLSink` relies on the username and password credentials created at the Oracle endpoint as well as database name.

[Top](#top)
