# <a name="top"></a>NGSILDPostgreSQLSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI-LD events to `NGSILDEvent` objects](#section1.1)
    * [Mapping `NGSILDEvent`s to PostgreSQL data structures](#section1.2)
        * [PostgreSQL databases naming conventions](#section1.2.1)
        * [PostgreSQL schemas naming conventions](#section1.2.2)
        * [PostgreSQL tables naming conventions](#section1.2.3)
        * [Row-like storing](#section1.2.4)
        * [Column-like storing](#section1.2.5)
    * [Example](#section1.3)
        * [`NGSILDEvent`](#section1.3.1)
        * [Database, schema and table names](#section1.3.2)
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
* [Programmers guide](#section3)
    * [`NGSIPostgreSQLSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSILDPostgreSQLSink`, or simply `NGSILDPostgreSQLSink` is a sink designed to persist NGSI-LD-like context data events within a [PostgreSQL server](https://www.postgresql.org/). Usually, such a context data is notified by a [Orion-LD Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI-LD language</i>.

Independently of the data generator, NGSI-LD context data is always transformed into internal `NGSILDEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific PostgreSQL data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI-LD events to `NGSILDEvent` objects
Notified NGSI-LD events (containing context data) are transformed into `NGSILDEvent` objects (for each context element a `NGSILDEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI-LD data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi-ld Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_ld_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSILDEvent`s to PostgreSQL data structures
PostgreSQL organizes the data in schemas inside a database that contain tables of data rows. Such organization is exploited by `NGSILDPostgreSQLSink` each time a `NGSILDEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>PostgreSQL databases naming conventions
Previous to any operation with PostgreSQL you need to create the database to be used.

It must be said [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). This leads to  certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.

PostgreSQL [databases name length](http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) is limited to 63 characters.

[Top](#top)

#### <a name="section1.2.2"></a>PostgreSQL schemas naming conventions
A schema named as the notified `fiware-service` header value (or, in absence of such a header, the defaulted value for the FIWARE service) is created (if not existing yet).

It must be said [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). This leads to  certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.

PostgreSQL [schemas name length](http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) is limited to 63 characters.

[Top](#top)

#### <a name="section1.2.3"></a>PostgreSQL tables naming conventions
The name of these tables depends on the configured data model (see the [Configuration](#section2.1) section for more details):

* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified entity ID is collected in order to compose the table name, using (`_`) for encodigng the special characters presented in the id fied. 

* Data model by entity type (`data_model=dm-by-entity-type`). For each entity type, the notified entity type is collected in order to compose the table name. using (`_`) for encodigng the special characters presented in the id fied.

It must be said [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) alphanumeric characters and the underscore (`_`). This leads to  certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.

PostgreSQL [tables name length](http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) is limited to 63 characters.

The following table summarizes the table name composition (old encoding):

| `dm-by-entity` | `dm-by-entity-type`|
|---|---|
|`<entityId>` |`<entityType>`|


[Top](#top)

#### <a name="section1.2.5"></a>Column-like storing
Regarding the specific data stored within the above table, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified property/relationship, a field named as the property/relationship is considered. This field will store the property/relationship values along the time, if no unique value is presented, the values will be stored like a JSON string.


[Top](#top)

### <a name="section1.3"></a>Example
#### <a name="section1.3.1"></a>`NGSILDEvent`
Assuming the following `NGSILD-event` is received from the NGSIRESTHANDLER:

    ngsi-notification=
    headers={
       fiware-service=opniot,
       transaction-id=1234567890-0000-1234567890,
       correlation-id=1234567890-0000-1234567890,
       timestamp=1234567890,
    },
    {
            "id": "urn:ngsi:ld:OffStreetParking:Downtown1",
            "type": "OffStreetParking",
            "@context": [
            "http://example.org/ngsi-ld/parking.jsonld",
            "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld"],
            "name": {
            "type": "Property",
               "value": "Downtown One"
            },
            "availableSpotNumber": {
                "type": "Property",
                "value": 122,
                "observedAt": "2017-07-29T12:05:02Z",
                "reliability": {
                    "type": "Property",
                    "value": 0.7
                },
                "providedBy": {
                    "type": "Relationship",
                    "object": "urn:ngsi-ld:Camera:C1"
                }
            },
            "totalSpotNumber": {
                "type": "Property",
                "value": 200
            },
                "location": {
                "type": "GeoProperty",
                "value": {
                    "type": "Point",
                    "coordinates": [-8.5, 41.2]
                }
            }
    }
    


[Top](#top)

#### <a name="section1.3.2"></a>Database, schema and table names
The PostgreSQL database name will be of the user's choice.

The PostgreSQL schema will always be `openiot`.

The PostgreSQL table names will be, depending on the configured data model, the following ones (old encoding):

| `dm-by-entity` | `dm-by-entity-type`|
|---|---|
|`urn_ngsi_ld_offstreetparking_downtown1` |`OffStreetParking`|


[Top](#top)

Assuming `attr_persistence=column` as configuration parameters, then `NGSILDPostgreSQLSink` will persist the data within the body as:


#### <a name="section1.3.4"></a>Column-like storing
Coming soon.
    
    $ psql -U myuser
    psql (9.5.0)
    Type "help" for help.
    postgres-# \c postgres

    postgres=#  \dn
        List of schemas
        Name     |  Owner   
    -------------+----------
     openiot | postgres
     public      | postgres
    (2 rows) 

    postgres=# \dt openiot.*
                            List of relations
       Schema    |                  Name                  | Type  |  Owner   
    -------------+----------------------------------------+-------+----------
     def_serv_ld | urn_ngsi_ld_offstreetparking_downtown1 | table | postgres  
     (1 row) 

     select * from openiot.urn_ngsi_ld_offstreetparking_downtown1;
         recvtime         | fiwareservicepath |                entityid                |    entitytype    | availablespotnumber | availablespotnumber_observedat | availablespotnumber_reliability | availab
    lespotnumber_providedby |     name     |                  location                  | totalspotnumber 
    --------------------------+-------------------+----------------------------------------+------------------+---------------------+--------------------------------+---------------------------------+--------
    ------------------------+--------------+--------------------------------------------+-----------------
     2020-05-12T15:10:39.47Z  | /def_servpath     | urn:ngsi:ld:OffStreetParking:Downtown1 | OffStreetParking | 122                 | 2017-07-29T12:05:02Z           | 0.7                             | urn:ngs
    i-ld:Camera:C1          | Downtown One | {"type":"Point","coordinates":[-8.5,41.2]} | 200
     2020-05-12T15:27:09.690Z | /def_servpath     | urn:ngsi:ld:OffStreetParking:Downtown1 | OffStreetParking | 122                 | 2017-07-29T12:05:02Z           | 0.7                             | urn:ngs
    i-ld:Camera:C1          | Downtown One | {"type":"Point","coordinates":[-8.5,41.2]} | 200
    (2 rows) 


[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSILDPostgreSQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSILDPostgreSQLSink</i> |
| channel | yes | N/A ||
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity | <i>dm-by-entity</i> or <i>dm-by-entity-type</i>    |
| postgresql\_host | no | localhost | FQDN/IP address where the PostgreSQL server runs. |
| postgresql\_port | no | 5432 ||
| postgresql\_database | no | postgres | `postgres` is the default database that is created automatically when install |
| postgresql\_username | no | postgres | `postgres` is the default username that is created automatically when install |
| postgresql\_password | no | N/A | Empty value by default (No password is created when install) |
| attr\_persistence | no | row | <i>row</i> or <i>column</i>. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| backend.enable\_cache | no | false | <i>true</i> or <i>false</i>, <i>true</i> enables the creation of a Cache, <i>false</i> disables the creation of a Cache. |

A configuration example could be:

    cygnus-ngsi-ld.sinks = postgresql-sink
    cygnus-ngsi-ld.channels = postgresql-channel
    ...
    cygnus-ngsi-ld.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.NGSILDPostgreSQLSink
    cygnus-ngsi-ld.sinks.postgresql-sink.channel = postgresql-channel
    cygnus-ngsi-ld.sinks.postgresql-sink.enable_encoding = false
    cygnus-ngsi-ld.sinks.postgresql-sink.enable_grouping = false
    cygnus-ngsi-ld.sinks.postgresql-sink.enable_lowercase = false
    cygnus-ngsi-ld.sinks.postgresql-sink.enable_name_mappings = false
    cygnus-ngsi-ld.sinks.postgresql-sink.data_model = dm-by-entity
    cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_host = 192.168.80.34
    cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_port = 5432
    cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_database = mydatabase
    cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_username = myuser
    cygnus-ngsi-ld.sinks.postgresql-sink.postgresql_password = mypassword
    cygnus-ngsi-ld.sinks.postgresql-sink.attr_persistence = row
    cygnus-ngsi-ld.sinks.postgresql-sink.batch_size = 100
    cygnus-ngsi-ld.sinks.postgresql-sink.batch_timeout = 30
    cygnus-ngsi-ld.sinks.postgresql-sink.batch_ttl = 10
    cygnus-ngsi-ld.sinks.postgresql-sink.batch_retry_intervals = 5000
    cygnus-ngsi-ld.sinks.postgresql.backend.enable_cache = false

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSILDPostgreSQLSink` if you are looking for a big database with several tenants. PostgreSQL is bad at having several databases, but very good at having different schemas.

[Top](#top)

### <a name="section2.3"></a>Important notes

#### <a name="section2.3.2"></a>About the persistence mode
The `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `NGSIPostgreSQLSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same PostgreSQL table. If processing the events one by one, we would need 100 inserts into PostgreSQL; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination PostgreSQL table. In the worst case, the whole 100 entities will be about 100 different entities (100 different PostgreSQL tables), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSILDPostgreSQLSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

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
### <a name="section3.1"></a>`NGSILDPostgreSQLSink` class
As any other NGSI-like sink, `NGSILDPostgreSQLSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSILDEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the PostgreSQL table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `PostgreSQLBackend` implementation.

    public void start();

An implementation of `PostgreSQLBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSILDPostgreSQLSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSILDPostgreSQLSink` relies on the database, username and password credentials created at the PostgreSQL endpoint.

[Top](#top)
