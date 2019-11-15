# <a name="top"></a>NGSIMySQLSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to MySQL data structures](#section1.2)
        * [MySQL databases naming conventions](#section1.2.1)
        * [MySQL tables naming conventions](#section1.2.2)
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
        * [About the table type and its relation with the grouping rules](#section2.3.1)
        * [About the persistence mode](#section2.3.2)
        * [About batching](#section2.3.3)
        * [Time zone information](#section2.3.4)
        * [About the encoding](#section2.3.5)
        * [About capping resources/expirating records](#section2.3.6)
* [Programmers guide](#section3)
    * [`NGSIMySQLSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIMySQLSink`, or simply `NGSIMySQLSink` is a sink designed to persist NGSI-like context data events within a [MySQL server](https://www.mysql.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific MySQL data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to MySQL data structures
MySQL organizes the data in databases that contain tables of data rows. Such organization is exploited by `NGSIMySQLSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>MySQL databases naming conventions
A database named as the notified `fiware-service` header value (or, in absence of such a header, the defaulted value for the FIWARE service) is created (if not existing yet).

It must be said MySQL [only accepts](http://dev.mysql.com/doc/refman/5.7/en/identifiers.html) alphanumerics `$` and `_`. This leads to certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.

MySQL [databases name length](http://dev.mysql.com/doc/refman/5.7/en/identifiers.html) is limited to 64 characters.

[Top](#top)

#### <a name="section1.2.2"></a>MySQL tables naming conventions
The name of these tables depends on the configured data model (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](./ngsi_rest_handler.md) is used as the name of the table. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table. The only constraint regarding this data model is the FIWARE service path cannot be the root one (`/`).
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and type in order to compose the table name. The concatenation character is `_` (underscore). If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated.
* Data model by entity type (`data_model=dm-by-entity-type`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity type in order to compose the table name. The concatenation character is `_` (underscore). If the FIWARE service path is the root one (`/`) then only the entity type is concatenated.

It must be said MySQL [only accepts](http://dev.mysql.com/doc/refman/5.7/en/identifiers.html) alphanumerics `$` and `_`. This leads to certain [encoding](#section2.3.5) is applied depending on the `enable_encoding` configuration parameter.

MySQL [tables name length](http://dev.mysql.com/doc/refman/5.7/en/identifiers.html) is limited to 64 characters.

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

Please observe the concatenation of entity ID and type is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSIEvent`.

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
* `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).

[Top](#top)

#### <a name="section1.2.4"></a>Column-like storing
Regarding the specific data stored within the above table, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: Timestamp in human-readable format (Similar to [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601), but avoiding the `Z` character denoting UTC, since all MySQL timestamps are supposed to be in UTC format).
* `fiwareServicePath`: The notified one or the default one.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
*  For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.

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

#### <a name="section1.3.2"></a>Database and table names
The MySQL database name will always be `vehicles`.

The MySQL table names will be, depending on the configured data model, the following ones (old encoding):

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
Assuming `attr_persistence=row` as configuration parameter, then `NGSIMySQLSink` will persist the data within the body as:

    mysql> select * from 4wheels_car1_car;
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    | recvTimeTs | recvTime                   | fiwareServicePath | entityId | entityType | attrName    | attrType  | attrValue | attrMd |
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        |  speed      | float     | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        |  oil_level  | float     | 74.6      | []     |
    +------------+----------------------------+-------------------+----------+------------+-------------+-----------+-----------+--------+
    2 row in set (0.00 sec)

[Top](#top)

#### <a name="section1.3.4"></a>Column-like storing
If `attr_persistence=colum` then `NGSIMySQLSink` will persist the data within the body as:

    mysql> select * from 4wheels_car1_car;
    +----------------------------+-------------------+----------+------------+-------+----------+-----------+--------------+
    | recvTime                   | fiwareServicePath | entityId | entityType | speed | speed_md | oil_level | oil_level_md |
    +----------------------------+-------------------+----------+------------+-------+----------+-----------+--------------+
    | 2015-04-20T12:13:22.41.124 | 4wheels           | car1     | car        | 112.9 | []       |  74.6     | []           |
    +----------------------------+-------------------+----------+------------+-------+----------+-----------+--------------+
    1 row in set (0.00 sec)

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIMySQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIMySQLSink</i> |
| channel | yes | N/A ||
| enable_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity | <i>dm-by-service-path</i>, <i>dm-by-entity</i> or <i>dm-by-entity-type</i>. <i>dm-by-service</i> and <dm-by-attribute</i> are not currently supported. |
| mysql\_host | no | localhost | FQDN/IP address where the MySQL server runs |
| mysql\_port | no | 3306 ||
| mysql\_username | no | root | `root` is the default username that is created automatically |
| mysql\_password | no | N/A | Empty value as default (no password is created automatically) |
| mysql\_maxPoolSize | no | 3 | Max number of connections per database pool |
| attr\_persistence | no | row | <i>row</i> or <i>column</i>
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| persistence\_policy.max_records | no | -1 | Maximum number of records allowed for a table before it is capped. `-1` disables this policy. |
| persistence\_policy.expiration_time | no | -1 | Maximum number of seconds a record is maintained in a table before expiration. `-1` disables this policy. |
| persistence\_policy.checking_time | no | 3600 | Frequency (in seconds) at which the sink checks for record expiration. |
| attr\_native\_types | no | false | if the attribute value will be native <i>true</i> or stringfy or <i>false</i>. |

A configuration example could be:

    cygnus-ngsi.sinks = mysql-sink
    cygnus-ngsi.channels = mysql-channel
    ...
    cygnus-ngsi.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMySQLSink
    cygnus-ngsi.sinks.mysql-sink.channel = mysql-channel
    cygnus-ngsi.sinks.mysql-sink.enable_encoding = false
    cygnus-ngsi.sinks.mysql-sink.enable_grouping = false
    cygnus-ngsi.sinks.mysql-sink.enable_lowercase = false
    cygnus-ngsi.sinks.mysql-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.mysql-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.mysql-sink.mysql_host = 192.168.80.34
    cygnus-ngsi.sinks.mysql-sink.mysql_port = 3306
    cygnus-ngsi.sinks.mysql-sink.mysql_username = myuser
    cygnus-ngsi.sinks.mysql-sink.mysql_password = mypassword
    cygnus-ngsi.sinks.mysql-sink.mysql_maxPoolSize = 3
    cygnus-ngsi.sinks.mysql-sink.attr_persistence = row
    cygnus-ngsi.sinks.mysql-sink.attr_native_types = false
    cygnus-ngsi.sinks.mysql-sink.batch_size = 100
    cygnus-ngsi.sinks.mysql-sink.batch_timeout = 30
    cygnus-ngsi.sinks.mysql-sink.batch_ttl = 10
    cygnus-ngsi.sinks.mysql-sink.batch_retry_intervals = 5000
    cygnus-ngsi.sinks.mysql-sink.persistence_policy.max_records = 5
    cygnus-ngsi.sinks.mysql-sink.persistence_policy.expiration_time = 86400
    cygnus-ngsi.sinks.mysql-sink.persistence_policy.checking_time = 600

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIMySQLSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About the table type and its relation with the grouping rules
The table type configuration parameter, as seen, is a method for <i>direct</i> aggregation of data: by <i>default</i> destination (i.e. all the notifications about the same entity will be stored within the same MySQL table) or by <i>default</i> service-path (i.e. all the notifications about the same service-path will be stored within the same MySQL table).

The [Grouping feature](./ngsi_grouping_interceptor.md) is another aggregation mechanism, but an <i>inderect</i> one. This means the grouping feature does not really aggregates the data into a single table, that's something the sink will done based on the configured table type (see above), but modifies the default destination or service-path, causing the data is finally aggregated (or not) depending on the table type.

For instance, if the chosen table type is by destination and the grouping feature is not enabled then two different entities data, `car1` and `car2` both of type `car` will be persisted in two different MySQL tables, according to their <i>default</i> destination, i.e. `car1_car` and `car2_car`, respectively. However, if a grouping rule saying "all cars of type `car` will have a modified destination named `cars`" is enabled then both entities data will be persisted in a single table named `cars`. In this example, the direct aggregation is determined by the table type (by destination), but indirectly we have been deciding the aggregation as well through a grouping rule.

[Top](#top)

#### <a name="section2.3.2"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields data rows are inserted for each notified attribute. Nevertheless, the `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the table can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `NGSIMySQLSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same MySQL table. If processing the events one by one, we would need 100 inserts into MySQL; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination MySQL table. In the worst case, the whole 100 entities will be about 100 different entities (100 different MySQL tables), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIMySQLSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.4"></a>Time zone information
Time zone information is not added in MySQL timestamps since MySQL stores that information as a environment variable. MySQL timestamps are stored in UTC time.

[Top](#top)

#### <a name="section2.3.5"></a>About the encoding
Until version 1.2.0 (included), Cygnus applied a very simple encoding:

* All non alphanumeric characters were replaced by underscore, `_`.
* The underscore was used as concatenator character as well.
* The slash, `/`, in the FIWARE service paths is ignored.

From version 1.3.0 (included), Cygnus applies this specific encoding tailored to MySQL data structures:

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
### <a name="section3.1"></a>`NGSIMySQLSink` class
As any other NGSI-like sink, `NGSIMySQLSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the MySQL table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `MySQLBackend` implementation.

    void capRecords(NGSIBatch batch, long maxRecords) throws EventDeliveryException;
    
This method is always called immediatelly after `persistBacth()`. The same destination tables that were upserted are now checked in terms of number of records: if the configured maximum (`persistence_policy.max_records`) is exceeded for any of the updated tables, then as many oldest records are deleted as required until the maximum number of records is reached.
    
    void expirateRecords(long expirationTime);
    
This method is called in a periodical way (based on `persistence_policy.checking_time`), and if the configured expiration time (`persistence_policy.expiration_time`) is exceeded for any of the records within any of the tables, then it is deleted.

    public void start();

An implementation of `MySQLBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIMySQLSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIMySQLSink` relies on the username and password credentials created at the MySQL endpoint.

[Top](#top)
