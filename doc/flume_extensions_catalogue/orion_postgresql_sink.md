#<a name="top"></a>OrionPostgreSQLSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to PostgreSQL data structures](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the table type and its relation with the grouping rules](#section2.3.1)
        * [About the persistence mode](#section2.3.2)
        * [About batching](#section2.3.3)
* [Programmers guide](#section3)
    * [`OrionPostgreSQLSink` class](#section3.1)
    * [`PostgreSQLBackendImpl` class](#section3.2)
    * [Authentication and authorization](#section3.3)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionPostgreSQLSink`, or simply `OrionPostgreSQLSink` is a sink designed to persist NGSI-like context data events within a [PostgreSQL server](https://www.postgresql.org/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific PostgreSQL data structures.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to [`OrionRestHandler`](./orion_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to PostgreSQL data structures
PostgreSQL organizes the data in schemas inside a database that contain tables of data rows. Such organization is exploited by `OrionPostgreSQLSink` each time a Flume event is going to be persisted.

Previous to any operation with PostgreSQL you need to create the database to be used.

According to the [naming conventions](./naming_conventions.md), a schema named as the `fiware-service` header value within the event is created (if not existing yet).

Then, the context responses/entities within the container are iterated, and a table is created (if not yet existing) within the above schema whose name depends on the configured data model:

* `dm-by-entity`. A table named as the concatenation of `<fiware_servicePath>_<destination>` is created (if not yet existing).
* `dm-by-service-path`. A table named as the `<fiware-servicePath>` is created (if not yet existing). 

The context attributes within each context response/entity are iterated, and a new data row (or rows) is inserted in the current table. The format for this row depends on the configured persistence mode:

* `row`: A data row is added for each notified context attribute. This kind of row will always contain 8 fields:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
    * `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
    * `column`: A single data row is added for all the notified context attributes. This kind of row will contain two fields per each entity's attribute (one for the value, named `<attrName>`, and other for the metadata, named `<attrName>_md`), plus four additional fields:
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `fiwareServicePath`: The notified one or the default one.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.

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

Assuming `postgresql_username=myuser`, `data_model=dm-by-entity` and `attr_persistence=row` as configuration parameters, then `OrionPostgreSQLSink` will persist the data within the body as:

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

NOTES:

* `psql` is the PostgreSQL CLI for querying the data.
* Time zone information is not added in PostgreSQL timestamps since PostgreSQL stores that information as a environment variable. PostgreSQL timestamps are stored in UTC time.

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`OrionPostgreSQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionPostgreSQLSink</i> |
| channel | yes | N/A ||
| enable_grouping | no | false | <i>true</i> or <i>false</i>. |
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data_model | no | dm-by-entity | <i>dm-by-service-path</i> or <i>dm-by-entity</i>. <i>dm-by-service</i> and <dm-by-attribute</i> are not currently supported. |
| postgresql_host | no | localhost | FQDN/IP address where the PostgreSQL server runs. |
| postgresql_port | no | 3306 ||
| postgresql_username | yes | N/A ||
| postgresql_password | yes | N/A ||
| attr_persistence | no | row | <i>row</i> or <i>column</i>. |
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |

A configuration example could be:

    cygnusagent.sinks = postgresql-sink
    cygnusagent.channels = postgresql-channel
    ...
    cygnusagent.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.OrionPostgreSQLSink
    cygnusagent.sinks.postgresql-sink.channel = postgresql-channel
    cygnusagent.sinks.postgresql-sink.enable_grouping = false
    cygnusagent.sinks.postgresql-sink.enable_lowercase = false
    cygnusagent.sinks.postgresql-sink.data_model = dm-by-entity
    cygnusagent.sinks.postgresql-sink.postgresql_host = 192.168.80.34
    cygnusagent.sinks.postgresql-sink.postgresql_port = 5432
    cygnusagent.sinks.postgresql-sink.postgresql_database = mydatabase
    cygnusagent.sinks.postgresql-sink.posqtgresql_username = myuser
    cygnusagent.sinks.postgresql-sink.postgresql_password = mypassword
    cygnusagent.sinks.postgresql-sink.attr_persistence = column
    cygnusagent.sinks.postgresql-sink.batch_size = 100
    cygnusagent.sinks.postgresql-sink.batch_timeout = 30
    cygnusagent.sinks.postgresql-sink.batch_ttl = 10
    
[Top](#top)

###<a name="section2.2"></a>Use cases
Use `OrionPostgreSQLSink` if you are looking for a big database with several tenants. PostgreSQL is bad at having several databases, but very good at having different schemas.

[Top](#top)

###<a name="section2.3"></a>Important notes
####<a name="section2.3.1"></a>About the table type and its relation with the grouping rules
The table type configuration parameter, as seen, is a method for <i>direct</i> aggregation of data: by <i>default</i> destination (i.e. all the notifications about the same entity will be stored within the same PostgreSQL table) or by <i>default</i> service-path (i.e. all the notifications about the same service-path will be stored within the same PostgreSQL table).

The [Grouping feature](./interceptors.md) is another aggregation mechanims, but an <i>inderect</i> one. This means the grouping feature does not really aggregates the data into a single table, that's something the sink will done based on the configured table type (see above), but modifies the default destination or service-path, causing the data is finally aggregated (or not) depending on the table type.

For instance, if the chosen table type is by destination and the grouping feature is not enabled then two different entities data, `car1` and `car2` both of type `car` will be persisted in two different PostgreSQL tables, according to their <i>default</i> destination, i.e. `car1_car` and `car2_car`, respectively. However, if a grouping rule saying "all cars of type `car` will have a modified destination named `cars`" is enabled then both entities data will be persisted in a single table named `cars`. In this example, the direct aggregation is determined by the table type (by destination), but inderectly we have been deciding the aggregation as well through a grouping rule.

[Top](#top)

####<a name="section2.3.2"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields data rows are inserted for each notified attribute. Nevertheless, the `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the table can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

####<a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `OrionPostgreSQLSink` extends `OrionSink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows exteding classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same PostgreSQL table. If processing the events one by one, we would need 100 inserts into PostgreSQL; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination PostgreSQL table. In the worst case, the whole 100 entities will be about 100 different entities (100 different PostgreSQL tables), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `OrionPostgreSQLSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](../operation/performance_tuning_tips.md).

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`OrionPostgreSQLSink` class
As any other NGSI-like sink, `OrionPostgreSQLSink` extends the base `OrionSink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;
    
A `Batch` contanins a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the PostgreSQL table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `PostgreSQLBackend` implementation.
    
    public void start();

An implementation of `PostgreSQLBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionPostgreSQLSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section3.2"></a>`PostgreSQLBackendImpl` class
This is a convenience backend class for PostgreSQL that implements the `PostgreSQLBackend` interface (provides the methods that any PostgreSQL backend must implement). Relevant methods are:

    public void createSchema(String schemaName) throws Exception;

Creates a database, given its name, if not existing.

    public void createTable(String schemaName, String tableName, String fieldNames) throws Exception;

Creates a table, given its name, if not existing within the given database. The field names are given as well.

    void insertContextData(String schemaName, String tableName, String fieldNames, String fieldValues) throws Exception;

Persists the accumulated context data (in the form of the given field values) regarding an entity within the given table. This table belongs to the given database. The field names are given as well to ensure the right insert of the field values.

[Top](#top)

###<a name="section3.3"></a>Authentication and authorization
Current implementation of `OrionPostgreSQLSink` relies on the database, username and password credentials created at the PostgreSQL endpoint.

[Top](#top)

