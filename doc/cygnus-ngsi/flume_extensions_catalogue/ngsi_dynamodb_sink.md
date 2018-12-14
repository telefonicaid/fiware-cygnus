# <a name="top"></a>NGSIDynamoDBSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to DynamoDB data structures](#section1.2)
        * [DynamoDB databases naming conventions](#section1.2.1)
        * [DynamoDB tables naming conventions](#section1.2.2)
        * [Row-like storing](#section1.2.3)
        * [Column-like storing](#section1.2.4)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Table names](#section1.3.2)
        * [Row-based storing](#section1.3.3)
        * [Column-based storing](#section1.3.4)
* [Administrator guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the table type and its relation with the grouping rules](#section2.3.1)
        * [About the persistence mode](#section2.3.2)
        * [About batching](#section2.3.3)
        * [Throughput in DynamoDB](#section2.3.4)
        * [About the encoding](#section2.3.5)
* [Programmers guide](#section3)
    * [`NGSIDynamoDBSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIDynamoDBSink`, or simply `NGSIDynamoDBSink` is a sink designed to persist NGSI-like context data events within a [DynamoDB database](https://aws.amazon.com/dynamodb/) in [Amazon Web Services](https://aws.amazon.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objets at Cygnus sources. In the end, the information within these events must be mapped into specific DynamoDB data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to DynamoDB data structures
DynamoDB organizes the data in tables of data items. All the tables are located within the same *default database*, i.e. the Amazon Web Services user space. Such organization is exploited by `NGSIDynamoDBSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>DynamoDB databases naming conventions
As said, there is a DynamoDB database per Amazon user. The [name of these users](http://docs.aws.amazon.com/IAM/latest/UserGuide/reference_iam-limits.html) must be alphanumeric, including the following common characters: `+`, `=`, `,`, `.`, `@`, `_` and `-`. This leads to certain [encoding](#section2.3.5) is applied.

DynamoDB [databases name length](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html#limits-naming-rules) may be up to 255 characters (minimum, 3 characters).

Current version of the sink does not support multitenancy, that means only an Amazon user space (and thus only a database) can be used. Such a user space is specified in the configuration (please, check the [Configuration](#section2.1) section), and therefore it is not necessary an exact match among the Amazon user space name and the FIWARE service path. Nevertheless, it is expected future versions of the sink will implement multitenancy; in that case it will be mandatory both FIWARE service and Amazon user space name match in order to correctly <i>route</i> each service data to the appropriate Amazon user.

[Top](#top)

#### <a name="section1.2.2"></a>DynamoDB tables naming conventions
The name of these tables depends on the configured data model (see the [Configuration](#section2.1) section for more details):

* Data model by service path (`data_model=dm-by-service-path`). As the data model name denotes, the notified FIWARE service path (or the configured one as default in [`NGSIRestHandler`](/ngsi_rest_handler.md)) is used as the name of the table. This allows the data about all the NGSI entities belonging to the same service path is stored in this unique table. The only constraint regarding this data model is the FIWARE service path cannot be the root one (`/`).
* Data model by entity (`data_model=dm-by-entity`). For each entity, the notified/default FIWARE service path is concatenated to the notified entity ID and entityType in order to compose the table name. If the FIWARE service path is the root one (`/`) then only the entity ID and type are concatenated.

In both cases, the notified/defaulted FIWARE service is prefixed to the table name.

It must be said DynamoDB [only accepts](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/WorkingWithTables.html) alphanumerics and `_`, `-` and `.`. This leads to certain [encoding](#section2.3.5) is applied.

DynamoDB [tables name length](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html#limits-naming-rules) may be up to 255 characters (minimum, 3 characters).

The following table summarizes the table name composition:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` |
|---|---|---|
| `/` | `<svc>x002f` | `<svc>xffffx002fxffff<entityId>xffff<entityType>` |
| `/<svcPath>` | `<svc>xffffx002f<svcPath>` | `<svc>xffffx002f<svcPath>xffff<entityId>xffff<entityType>` |

Please observe the concatenation of entity ID and type is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSIEvent`.

[Top](#top)

#### <a name="section1.2.3"></a>Row-like storing
Regarding the specific data stored within the datastore associated to the resource, if `attr_persistence` parameter is set to `row` (default storing mode) then the notified data is stored attribute by attribute, composing an insert for each one of them. Each insert contains the following fields:

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
Regarding the specific data stored within the datastore associated to the resource, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
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

#### <a name="section1.3.2"></a>Table names
The DynamoDB table names will be, depending on the configured data model, the following ones:

| FIWARE service path | `dm-by-service-path` | `dm-by-entity` |
|---|---|---|
| `/` | `vehicles` | `vehiclesxffffx002fxffffcar1xffffcar` |
| `/4wheels` | `vehiclesxffffx002f4wheels` | `vehiclesxffffx002f4wheelsxffffcar1xffffcar` |

[Top](#top)

#### <a name="section1.3.3"></a>Raw-based storing
Let's assume a table name `x002fvehiclesxffff4wheelsxffffcar1xffffcar` (data model by entity, non-root service path) and `attr_persistence=row` as configuration parameter. The data stored within this table would be:

![](../images/dynamodb_row_destination.jpg)

[Top](#top)

#### <a name="section1.3.3"></a>Column-based storing
If `attr_persistence=colum` then `NGSIDynamoDBSink` will persist the data within the body as:

![](../images/dynamodb_column_destination.jpg)

[Top](#top)

## <a name="section2"></a>Administrator guide
### <a name="section2.1"></a>Configuration
`NGSIDynamoDBSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIDynamoDBSink</i> |
| channel | yes | N/A ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data_model | no | dm-by-entity |  <i>dm-by-entity</i> or <i>dm-by-service-path</i>. |
| attr\_persistence | no | row | <i>row</i> or <i>column</i>. |
| access\_key\_id | yes | N/A | Provided by AWS when creating an account. |
| secret\_access\_key | yes | N/A | Provided by AWS when creating an account. |
| region | no | eu-central-1 | [AWS regions](http://docs.aws.amazon.com/general/latest/gr/rande.html). |
| batch\_size | no | 1 | Number of events accumulated before persistence (Maximum 25, check [Amazon Web Services Documentation](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html) for more information). |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |

A configuration example could be:

    cygnus-ngsi.sinks = dynamodb-sink
    cygnus-ngsi.channels = dynamodb-channel
    ...
    cygnus-ngsi.sinks. dynamodb-sink.type = com.telefonica.iot.cygnus.sinks.NGSIDynamoDBSink
    cygnus-ngsi.sinks.dynamodb-sink.channel = dynamodb-channel
    cygnus-ngsi.sinks.dynamodb-sink.enable_grouping = false
    cygnus-ngsi.sinks.dynamodb-sink.enable_lowercase = false
    cygnus-ngsi.sinks.dynamodb-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.dynamodb-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.dynamodb-sink.attr_persistence = column
    cygnus-ngsi.sinks.dynamodb-sink.access_key_id = xxxxxxxx
    cygnus-ngsi.sinks.dynamodb-sink.secret_access_key = xxxxxxxxx
    cygnus-ngsi.sinks.dynamodb-sink.region = eu-central-1
    cygnus-ngsi.sinks.dynamodb-sink.batch_size = 25
    cygnus-ngsi.sinks.dynamodb-sink.batch_timeout = 30
    cygnus-ngsi.sinks.dynamodb-sink.batch_ttl = 10
    cygnus-ngsi.sinks.dynamodb-sink.batch_retry_intervals = 5000

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIDynamoDBSink` if you are looking for a cloud-based database with [relatively good throughput](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ProvisionedThroughputIntro.html) and scalable storage.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About the table type and its relation with the grouping rules
The table type configuration parameter, as seen, is a method for <i>direct</i> aggregation of data: by <i>default</i> destination (i.e. all the notifications about the same entity will be stored within the same DynamoDB table) or by <i>default</i> service-path (i.e. all the notifications about the same service-path will be stored within the same DynamoDB table).

The [Grouping feature](/ngsi_grouping_interceptor.md) is another aggregation mechanism, but an <i>inderect</i> one. This means the grouping feature does not really aggregates the data into a single table, that's something the sink will done based on the configured table type (see above), but modifies the default destination or service-path, causing the data is finally aggregated (or not) depending on the table type.

For instance, if the chosen table type is by destination and the grouping feature is not enabled then two different entities data, `car1` and `car2` both of type `car` will be persisted in two different DynamoDB tables, according to their <i>default</i> destination, i.e. `car1_car` and `car2_car`, respectively. However, if a grouping rule saying "all cars of type `car` will have a modified destination named `cars`" is enabled then both entities data will be persisted in a single table named `cars`. In this example, the direct aggregation is determined by the table type (by destination), but indirectly we have been deciding the aggregation as well through a grouping rule.

[Top](#top)

#### <a name="section2.3.2"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for DynamoDB since this kind of database is designed for holding items of different length within the same table. Anyway, it must be taken into account, when designing your applications, the `row` persistence mode will always insert fixed 8-fields data items for each notified attribute. And the `column` mode may be affected by several data items of different lengths (in term of fields), as already explained.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `NGSIDynamoDBSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of inserts is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same DynamoDB table. If processing the events one by one, we would need 100 inserts into DynamoDB; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination DynamoDB table. In the worst case, the whole 100 entities will be about 100 different entities (100 different DynamoDB tables), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIDynamoDBSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.4"></a>Throughput in DynamoDB
Please observe DynamoDB is a cloud-based storage whose throughput may be seriously affected by how far are the region the tables are going to be created and the amount of information per write.

Regarding the region, always choose the closest one to the host running Cygnus and `NGSIDynamoDBSink`.

Regarding the amount of information per write, please read carefully [this](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ProvisionedThroughputIntro.html) piece of documentation about how to fine tune the reserved capacity for write (and read) in AWS DynamoDB. Please observe increasing the write (or read) capabilities increases the cost of the service as well.

[Top](#top)

#### <a name="section2.3.5"></a>About the encoding
Cygnus applies this specific encoding tailored to DynamoDB data structures:

* Alphanumeric characters are not encoded.
* Numeric characters are not encoded.
* Underscore character, `_`, is not encoded.
* Hyphen character, `-`, is not encoded.
* Dot character, `.`, is not encoded.
* Equals character, `=`, is encoded as `xffff`.
* All other characters, including the slash in the FIWARE service paths, are encoded as a `x` character followed by the [Unicode](http://unicode-table.com) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* `xffff` is used as concatenator character.

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIDynamoDBSink` class
As any other NGSI-like sink, `NGSIDynamoDBSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the DynamoDB table where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `DynamoDBBackend` implementation.

    public void start();

An implementation of `DynamoDBBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIDynamoDBSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIDynamoDBSink` relies on the [AWS access keys](http://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html) mechanism.

[Top](#top)
