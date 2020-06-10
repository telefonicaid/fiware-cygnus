# <a name="top"></a>Adding new NGSI sinks development guide
Content:

* [Introduction](#section1)
* [Base `NGSILDSink` class](#section2)
    * [Inherited configuration](#section2.1)
    * [Inherited starting and stoping](#section2.2)
    * [Inherited events consumption](#section2.3)
    * [Inherited counters ](#section2.4)
* [New sink class](#section3)
    * [Specific configuration](#section3.1)
    * [Kind of information to be persisted](#section3.2)
    * [Fitting to the specific data structures](#section3.3)
* [Backend convenience classes](#section4)
* [Naming and placing the new sink](#section5)

## <a name="section1"></a>Introduction
`cygnus-ngsi-ld` allows for NGSI-LD context data persistence in certain storages by means of Flume sinks. As long as the current collection of sinks could be limited for your purposes, you can add your own sinks regarding a persistence technology of your choice and become an official `cygnus-ngsi-ld` contributor!

This document tries to guide you on the development of such alternative sinks, by giving you guidelines about how to write the sink code, but also how the different classes must be called, the backends that can be used, etc.

[Top](#top)

## <a name="section2"></a>Base `NGSILDSink` class
`NGSILDSink` is the base class all the sinks within `cygnus-ngsi-ld` extend. It is an abstract class which extends from `CygnusSink` class at `cygnus-common` (which, by its side, extends Flume's native `AbstractSink`).

`NGSILDSink` provides most of the logic required by any NGSI-like sink:

* Configuration of parameters common to all the sinks.
* Starting and stopping the sink.
* Flume events consumption in a batch-like approach, including opening, committing and closing of Flume transactions.
* Counters for statistics (in fact, this feature is given by `CygnusSink`).

You find this class at the following path:

    fiware-cygnus/cygnus-ngsi-ld/src/main/java/com/telefonica/iot/cygnus/sinks/NGSILDSink.java

[Top](#top)

### <a name="section2.1"></a>Inherited configuration
All the sinks extending `NGSILDSink` inherit the following configuration parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| data_model | no | dm-by-entity | Accepted values: <i>dm-by-service</i>, <i>dm-by-service-path</i>, <i>dm-by-entity</i>, <i>dm-by-entity-type</i> and <dm-by-attribute</i>. |
| enable_grouping | no | false | Accepted values: <i>true</i> or <i>false</i>. |
| enable\_lowercase | no | false | Accepted values: <i>true</i> or <i>false</i>. |

These parameters are read (and defaulted, when required) in the `configure(Context)` method.

[Top](#top)

### <a name="section2.2"></a>Inherited starting and stoping
TBD

[Top](#top)

### <a name="section2.3"></a>Inherited events consumption    
The most important part of `NGSILDSink` is where the events are consumed in a batch-like approach. This is done in the `process()` method inherited from `AbstractSink`, which is overwritten.

Such events processing is done by opening a Flume transaction and reading events as specified in the `batch_size` parameter (if no enough events are available, the accumulation ends when the `batch_timeout` is reached). For each event read, the transaction is committed. Once the accumulations ends the transaction is closed.

Please notice that the `process()` method handles all the possible errors that may occur during a Flume transaction by catching exceptions. There exists a collection of Cygnus-related exceptions whose usage is mandatory located at `cygnus-common`:

    fiware-cygnus/cygnus-common/src/main/java/com/telefonica/iot/cygnus/errors/

Once finished, accumulation results in a `NGSILDBatch` object, which internally holds sub-batches per each notified/grouped destination headers in the Flume event objects are inspected to created the sub-batches. Information within this `NGSILDBatch` object is the one the specific implementation of the new sink must persist.

Specific persistence logic is implemented by overwriting the only abstract method within `NGSISink`, i.e. `persistBatch(NGSIBatch)`:

    abstract void persistBatch(NGSILDBatch) throws Exception;

[Top](#top)

### <a name="section2.4"></a>Inherited counters
Because `NGSILDSink` extends `CygnusSink` the following counters are already available for retrieving statistics of any sink extending `NGSILDSink`:

* Number of processed events, i.e. the number of events taken from the channel and accumulated in a batch for persistence.
* Number of persisted events, i.e. the number of events within batches finally written/inserted/added in the final storage.

[Top](#top)

## <a name="section3"></a>New sink class
### <a name="section3.1"></a>Specific configuration
The `configure(Context)` method of `NGSILDSink` can be extended with specific configuration parameters reading (and defaulting, when required).

[Top](#top)

### <a name="section3.2"></a>Kind of information to be persisted
We include a list of fields that are usually persisted in Cygnus sinks:

* The reception time of the notification in miliseconds.
* The reception time of the notification in human-readable format.
* The notified/grouped FIWARE service path.
* The entity ID.
* The entity type.
* The attributes (Properties,Relationships ans Geo-properties).

Regarding the attributes and their metadata, you may choose between two options (or both of them, by means of a switching configuration parameter):

* <i>column</i> format, i.e. a single write/insertion/upsert containing all the attributes.

[Top](#top)

### <a name="section3.2"></a>Fitting to the specific data structures
It is worth to briefly comment how the specific data structures should be created.

Typically, the notified service (which defines a client/tenant) should map to the storage element in charge of defining namespaces per user. For instance, in MySQL, PostgreSQL, MongoDB and STH, the service maps to a specific database where permissions can be defined at user level. While in CKAN, the service maps to an organization. In other cases, the mapping is not so evident, as in HDFS, where the service maps into a folder under `hdfs://user/`. Or it is totally impossible to fit, as is the case of DynamoDB or Kafka, where the service can only be added as part of the persistence element name (table and topic, respectively).
Is important to remark that in `cygnus-ngsi-ld` we remove the need of including the `fiware-service-path`, because all the ata structures are build using only the `Fiware-Service` header. 
Finally, in order to differentiate among all the entities, the concatenation of entity ID and type should be used as the default destination name (unless a grouping rule is used to overwrite this default behavior).

[Top](#top)

## <a name="section4"></a>Backend convenience classes
Sometimes all the necessary logic to persist the notified context data cannot be coded in the `persist` abstract method. In this case, you may want to create a backend class or set of classes wrapping the detailed interactions with the final backend. Nevertheless, these classes should not be located at `cygnus-ngsi-ld` but at `cygnus-common`.

[Top](#top)

## <a name="section5"></a>Naming and placing the new classes
New sink classes must be called `NGSI<technology>Sink`, being <i>technology</i> the name of the persistence backend. Examples are the already existent sinks `NGSIPosgresqlSink`.

Regarding the new sink class location, it must be:

    fiware-cygnus/cygnus-ngsi-ld/src/main/java/com/telefonica/iot/cygnus/sinks/

As already explained, backends must be located at:

    fiware-cygnus/cygnus-common/src/main/java/com/telefonica/iot/cygnus/backends/<technology>/

[Top](#top)
