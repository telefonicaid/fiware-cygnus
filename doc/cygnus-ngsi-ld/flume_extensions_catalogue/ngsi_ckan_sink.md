# <a name="top"></a>NGSICKANSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI-LD events to `NGSI-LDEvent` objects](#section1.1)
    * [Mapping `NGSI-LDEvent`s to CKAN data structures](#section1.2)
        * [Organizations naming conventions](#section1.2.1)
        * [Package/dataset naming conventions](#section1.2.2)
        * [Resource naming conventions](#section1.2.3)
        * [Column-like storing](#section1.2.4)
    * [Example](#section1.3)
        * [`NGSI-LDEvent`](#section1.3.1)
        * [Organization, dataset and resource names](#section1.3.2)
        * [Column-like storing](#section1.3.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the persistence mode](#section2.3.1)
        * [About batching](#section2.3.2)
        * [About the encoding](#section2.3.3)
        * [About geolocation attributes](#section2.3.4)
        * [About capping resources/expirating records](#section2.3.5)
* [Programmers guide](#section3)
    * [`NGSICKANSink` class](#section3.1)
* [Annexes](#section4)
    * [Provisioning a CKAN resource for the column mode](#section4.1)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSICKANSink`, or simply `NGSICKANSink` is a sink designed to persist NGSI-LD-like context data events within a [CKAN](http://ckan.org/) server. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI-LD language</i>.

Independently of the data generator, NGSI-LD context data is always transformed into internal `NGSI-LDEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific CKAN data structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI-LD events to `NGSI-LDEvent` objects
Notified NGSI-LD events (containing context data) are transformed into `NGSI-LDEvent` objects (for each context element a `NGSI-LDEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI-LD data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi-ld Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSI-LDEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSI-LDEvent`s to CKAN data structures
[CKAN organizes](http://docs.ckan.org/en/latest/user-guide.html) the data in organizations containing packages or datasets; each one of these packages/datasets contains several resources whose data is finally stored in a PostgreSQL database (CKAN Datastore) or plain files (CKAN Filestore). Such organization is exploited by `NGSICKANSink` each time a `NGSI-LDEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>Organizations naming conventions
* Data model by entity (`data_model=dm-by-entity`). An organization named as the notified `fiware-service` header value (or, in absence of such a header, the defaulted value for the FIWARE service) is created (if not existing yet).
Since based in [PostgreSQL only accepts] 
https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS), it must be said only alphanumeric characters and the underscore (`_`) are accepted. The hyphen ('-') is also accepted. This leads to certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.
Nevertheless, different than PostgreSQL, [organization lengths](http://docs.ckan.org/en/latest/api/#ckan.logic.action.create.organization_create) may be up to 100 characters (minimum, 2 characters).


* Data model by entity id (`data_model=dm-by-entity-id`). The organization name will take the value of the notified header `fiware-service`. Note that in this case, encoding is never applied (this is current marked as "debt" in the source code with a FIXME mark).

The following table summarizes the organization name composition:

| `dm-by-entity` | `dm-by-entity-id` |
|---|---|
| `<fiware-service>` | `<fiware-service>` | 

[Top](#top)

#### <a name="section1.2.2"></a>Packages/datasets naming conventions
* Data model by entity (`data_model=dm-by-entity`). A package/dataset named as the notified `fiware-service`  header value (or, in absence of such header, the defaulted value for the FIWARE service ) is created (if not existing yet) in the above organization.
Since based in [PostgreSQL only accepts](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS), it must be said only alphanumeric characters and the underscore (`_`) are accepted. The hyphen ('-') is also accepted. This leads to  certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter
Nevertheless, different than PostgreSQL, [dataset lengths](http://docs.ckan.org/en/latest/api/#ckan.logic.action.create.package_create) may be up to 100 characters (minimum, 2 characters).


* Data model by entity id (`data_model=dm-by-entity-id`). A package/dataset name always take the entity ID. Such a name is already given in the NGSI-LDEvent values, see the [Configuration](#section2.1) section for more details) within the the `NGSI-LDEvent`. Note that in this case, encoding is never applied (this is current marked as "debt" in the source code with a FIXME mark).

The following table summarizes the package name composition:

| `dm-by-entity` | `dm-by-entity-id` |
|---|---|
| `<fiware-service> | `<entityId>` | 

[Top](#top)

#### <a name="section1.2.3"></a>Resources naming conventions
The resource name depends on the configured data model (see the [Configuration](#section2.1) section for more details):

* Data model by entity (`data_model=dm-by-entity`). A resource name always take the concatenation of the entity ID and type. Such a name is already given in the `notified_entities`/`grouped_entities` header values (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSI-LDEvent`.


* Data model by entity id (`data_model=dm-by-entity-id`). A resource name always take the entity ID. Such a name is already given in the NGSI-LDEvent values, see the [Configuration](#section2.1) section for more details) within the the `NGSI-LDEvent`. Note that in this case, encoding is never applied (this is current marked as "debt" in the source code with a FIXME mark).

It must be noticed a CKAN Datastore (and a viewer) is also created and associated to the resource above. This datastore, which in the end is a PostgreSQL table, will hold the persisted data.

Since based in [PostgreSQL](https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS), it must be said only alphanumeric characters and the underscore (`_`) are accepted. The hyphen ('-') is also accepted. This leads to  certain [encoding](#section2.3.3) is applied depending on the `enable_encoding` configuration parameter.

Despite there is no real limit on the resource names, Cygnus will keep limiting their lengths up to 100 characters (minimum, 2 characters), accordingly to what's done with organization and package names.

The following table summarizes the resource name composition:

| `dm-by-entity` | `dm-by-entity-id` |
|---|---|
| `<entityId>_<entityType>` | `<entityId>` |

[Top](#top)


#### <a name="section1.2.3"></a>Column-like storing
Regarding the specific data stored within the datastore associated to the resource, if `attr_persistence` parameter is set to `column` then a single line is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified property/relationship, a field named as the property/relationship is considered. This field will store the property/relationship values along the time, if no unique value is presented, the values will be stored like a JSON string.


[Top](#top)

### <a name="section1.3"></a>Example
#### <a name="section1.3.1"></a>`NGSI-LDEvent`
Assuming the following `NGSI-LDEvent` is created from a notified NGSI-LD context data (the code below is an <i>object representation</i>, not any real data format):

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

#### <a section="1.3.2"></a>Organization, dataset and resource names
Given the above example and using the old encoding, these are the CKAN elements created

* Orgnaization: `openiot`.
* Package: `openiot`.
* Resource: `urn_ngsi_ld_OffStreetParking_Downtown1`.

Using the new encdoing:

* Orgnaization: `vehicles`.
* Package: `vehicles`.
* Resource: `urnxffffngsixffffldxffffOffStreetParkingxffffDowntown1`.

[Top](#top)

#### <a section="1.3.2"></a>Column-like storing
If `attr_persistence=colum` then `NGSICKANSink` will persist the data within the body as:

    $ curl -s -S -H "Authorization: myapikey" "http://130.206.83.8:80/api/3/action/datastore_search?resource_id=611417a4-8196-4faf-83bc-663c173f6986"
    {
        "help": "Search a DataStore resource...",
        "success": true,
        "result": {
            "resource_id": "611417a4-8196-4faf-83bc-663c173f6986",
            "fields": [
                {
                    "type": "int4",
                    "id": "_id"
                },
                {
                    "type": "timestamp",
                    "id": "recvTime"
                },
                {
                    "type": "text",
                    "id": "entityID"
                },
                {
                    "type": "text",
                    "id": "entityType"
                },
                {
                    "type": "text",
                    "id": "availableSpotNumber"
                },
                {
                    "type": "text",
                    "id": "availableSpotNumber_observedAt"
                },
                {
                    "type": "text",
                    "id": "availableSpotNumber_reliability"
                },
                {
                    "type": "text",
                    "id": "availableSpotNumber_providedBy"
                },
                {
                    "type": "text",
                    "id": "name"
                },
                {
                    "type": "text",
                    "id": "location"
                },
                {
                    "type": "text",
                    "id": "totalSpotNumber"
                }
            ],
            "records": [ 
                {
                    "recvTime":"2020-09-21T23:54:13.394Z",
                    "entityId":"urn:ngsi:ld:OffStreetParking:Downtown1",
                    "entityType":"OffStreetParking","availableSpotNumber":"122",
                    "availableSpotNumber_observedAt":"122",
                    "availableSpotNumber_reliability":"0.7",
                    "availableSpotNumber_providedBy":"urn:ngsi-ld:Camera:C1",
                    "name":"Downtown One",
                    "location":'{"type":"Point","coordinates":[-8.5,41.2]}',
                    "totalSpotNumber":"200"
                }
             ],
            "_links": {
                "start": "/api/3/action/datastore_search?resource_id=611417a4-8196-4faf-83bc-663c173f6986",
                "next": "/api/3/action/datastore_search?offset=100&resource_id=611417a4-8196-4faf-83bc-663c173f6986"
            },
            "total": 1
        }
    }

NOTE: `curl` is a Unix command allowing for interacting with REST APIs such as the exposed by CKAN.

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSICKANSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSICKANSink</i> |
| channel | yes | N/A |
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| data\_model | no | dm-by-entity | <i>dm-by-entity-id</i>, <i>dm-by-entity</i> ||
| attr\_persistence | no | column | <i>column.</i>|
| ckan\_host | no | localhost | FQDN/IP address where the CKAN server runs. ||
| ckan\_port | no | 80 ||
| ckan\_viewer | no | recline\_grid\_view | Please check the [available](http://docs.ckan.org/en/latest/maintaining/data-viewer.html) viewers at CKAN documentation. |
| ssl | no | false ||
| api\_key | yes | N/A ||
| orion\_url | no | http://localhost:1026 | To be put as the filestore URL. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| backend.max\_conns | no | 500 | Maximum number of connections allowed for a Http-based HDFS backend. |
| backend.max\_conns\_per\_route | no | 100 | Maximum number of connections per route allowed for a Http-based HDFS backend. |
| persistence\_policy.max_records | no | -1 | Maximum number of records allowed for a resource before it is capped. `-1` disables this policy. |
| persistence\_policy.expiration_time | no | -1 | Maximum number of seconds a record is maintained in a resource before expiration. `-1` disables this policy. |
| persistence\_policy.checking_time | no | 3600 | Frequency (in seconds) at which the sink ckecks for record expiration. |

A configuration example could be:

    cygnus-ngsi-ld.sources = http-source
    cygnus-ngsi-ld.sinks = ckan-sink
    cygnus-ngsi-ld.channels = test-channel
    
    cygnus-ngsi-ld.sources.http-source.channels = test-channel
    cygnus-ngsi-ld.sources.http-source.type = org.apache.flume.source.http.HTTPSource
    cygnus-ngsi-ld.sources.http-source.port = 5050
    cygnus-ngsi-ld.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
    cygnus-ngsi-ld.sources.http-source.handler.notification_target = /notify
    cygnus-ngsi-ld.sources.http-source.handler.default_service = def_serv_ld
    cygnus-ngsi-ld.sources.http-source.handler.events_ttl = 2
    cygnus-ngsi-ld.sources.http-source.interceptors = ts
    cygnus-ngsi-ld.sources.http-source.interceptors.ts.type = timestamp
    
    
    
    cygnus-ngsi-ld.channels.test-channel.type = memory
    cygnus-ngsi-ld.channels.test-channel.capacity = 1000
    cygnus-ngsi-ld.channels.test-channel.transactionCapacity = 100
    
    
    cygnus-ngsi-ld.sinks.ckan-sink.type = com.telefonica.iot.cygnus.sinks.NGSICKANSink
    cygnus-ngsi-ld.sinks.ckan-sink.channel = test-channel
    cygnus-ngsi-ld.sinks.ckan-sink.enable_name_mappings = false
    cygnus-ngsi-ld.sinks.ckan-sink.data_model = dm-by-entity
    cygnus-ngsi-ld.sinks.ckan-sink.attr_persistence = column
    cygnus-ngsi-ld.sinks.ckan-sink.ckan_host = localhost
    cygnus-ngsi-ld.sinks.ckan-sink.ckan_port = 5000
    cygnus-ngsi-ld.sinks.ckan-sink.ckan_viewer = recline_grid_view
    cygnus-ngsi-ld.sinks.ckan-sink.ssl = false
    cygnus-ngsi-ld.sinks.ckan-sink.api_key = 0bc7f58b-6546-4524-9dc2-9ee91501eee7
    cygnus-ngsi-ld.sinks.ckan-sink.orion_url = http://localhost:1026
    cygnus-ngsi-ld.sinks.ckan-sink.batch_size = 100
    cygnus-ngsi-ld.sinks.ckan-sink.batch_timeout = 30
    cygnus-ngsi-ld.sinks.ckan-sink.batch_ttl = 10
    cygnus-ngsi-ld.sinks.ckan-sink.batch_retry_intervals = 5000
    cygnus-ngsi-ld.sinks.ckan-sink.backend.max_conns = 500
    cygnus-ngsi-ld.sinks.ckan-sink.backend.max_conns_per_route = 100
    cygnus-ngsi-ld.sinks.ckan-sink.persistence_policy.max_records = 5
    cygnus-ngsi-ld.sinks.ckan-sink.persistence_policy.expiration_time = 86400
    cygnus-ngsi-ld.sinks.ckan-sink.persistence_policy.checking_time = 600

    

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSICKANSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-LD-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields rows are upserted for each notified attribute. Nevertheless, the `column` mode may be affected by several rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

[Top](#top)

#### <a name="section2.3.2"></a>About batching
As explained in the [programmers guide](#section3), `NGSICKANSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSI-LDEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same CKAN resource. If processing the events one by one, we would need 100 inserts into CKAN; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination CKAN resource. In the worst case, the whole 100 entities will be about 100 different entities (100 different CKAN resources), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 inserts of the event by event approach with only 10-15 inserts.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSICKANSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi-ld/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.3"></a>About the encoding
Until version 1.2.0 (included), Cygnus applied a very simple encoding:

* All non alphanumeric characters were replaced by underscore, `_`.
* The underscore was used as concatenator character as well.
* The slash, `/`, in the FIWARE service paths is ignored.

From version 1.3.0 (included), Cygnus applies this specific encoding tailored to CKAN data structures:

* Lowercase alphanumeric characters are not encoded.
* Upercase alphanumeric characters are encoded.
* Numeric characters are not encoded.
* Underscore character, `_`, is not encoded.
* Hyphen character, `-`, is not encoded.
* Equals character, `=`, is encoded as `xffff`.
* All other characters, including the slash in the FIWARE service paths, are encoded as a `x` character followed by the [Unicode](https://symbl.cc) of the character.
* User defined strings composed of a `x` character and a Unicode are encoded as `xx` followed by the Unicode.
* `xffff` is used as concatenator character.

Despite the old encoding will be deprecated in the future, it is possible to switch the encoding type through the `enable_encoding` parameter as explained in the [configuration](#section2.1) section.

[Top](#top)



#### <a name="section2.3.5"></a>About capping resources and expirating records
Capping and expiration are disabled by default. Nevertheless, if desired, this can be enabled:

* Capping by the number of records. This allows the resource growing up until certain configured maximum number of records is reached (`persistence_policy.max_records`), and then maintains a such a constant number of records.
* Expirating by time the records. This allows the resource growing up until records become old, i.e. overcome certain configured expiration time (`persistence_policy.expiration_time`).

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSICKANSink` class
As any other NGSI-LD-like sink, `NGSICKANSink` extends the base `NGSI-LDSink`. The methods that are extended are:

    void persistBatch(NGSI-LDBatch batch) throws Exception;

A `NGSI-LDBatch` contains a set of `NGSI-LDEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the CKAN resource where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `CKANBackend` implementation.

    void capRecords(NGSI-LDBatch batch, long maxRecords) throws EventDeliveryException;
    
This method is always called immediatelly after `persistBacth()`. The same destination resources that were upserted are now checked in terms of number of records: if the configured maximum (`persistence_policy.max_records`) is overcome for any of the updated resources, then as many oldest records are deleted as required until the maximum number of records is reached.
    
    void expirateRecords(long expirationTime);
    
This method is called in a peridocial way (based on `persistence_policy.checking_time`), and if the configured expiration time (`persistence_policy.expiration_time`) is overcome for any of the records within any of the resources, then it is deleted.

    public void start();

An implementation of `CKANBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSICKANSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

