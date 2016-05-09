
#<a name="top"></a>Adding new twitter sinks development guide
Content:

* [Introduction](#section1)
* [Base `TwitterSink` class](#section2)
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

##<a name="section1"></a>Introduction
`cygnus-twitter` allows for twitter context data persistence in certain storages by means of Flume sinks. As long as the current collection of sinks could be limited for your purposes, you can add your own sinks regarding a persistence technology of your choice and become an official `cygnus-twitter` contributor!

This document tries to guide you on the development of such alternative sinks, by giving you guidelines about how to write the sink code, but also how the different classes must be called, the backends that can be used, etc.

[Top](#top)

##<a name="section2"></a>Base `TwitterSink` class
`TwitterSink` is the base class all the sinks within `cygnus-twitter` extend. It is an abstract class which extends from `CygnusSink` class at `cygnus-common` (which, by its side, extends Flume's native `AbstractSink`).

`TwitterSink` provides most of the logic required by any Twitter-like sink:

* Configuration of parameters common to all the sinks.
* Starting and stoping the sink.
* Flume events consumption in a batch-like approach, including opening, committing and closing of Flume transactions.
* Counters for statistics (in fact, this feature is given by `CygnusSink`).

You find this class at the following path:

    fiware-cygnus/cygnus-twitter/src/main/java/com/telefonica/iot/cygnus/sinks/TwitterSink.java
    
[Top](#top)
    
###<a name="section2.1"></a>Inherited configuration
All the sinks extending `TwitterSink` inherit the following configuration parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| enable\_lowercase | no | false | Accepted values: <i>true</i> or <i>false</i>. |

These parameters are read (and defaulted, when required) in the `configure(Context)` method.

[Top](#top)

###<a name="section2.2"></a>Inherited starting and stoping
TBD

[Top](#top)

###<a name="section2.3"></a>Inherited events consumption    
The most important part of `TwitterSink` is where the events are consumed in a batch-like approach. This is done in the `process()` method inherited from `AbstractSink`, which is overwritten.

Such events processing is done by opening a Flume transaction and reading events as specified in the `batch_size` parameter (if no enough events are available, the accumulation ends when the `batch_timeout` is reached). For each event read, the transaction is committed. Once the accumulations ends the transaction is closed.

Please notice that the `process()` method handles all the possible errors that may occur during a Flume transaction by catching exceptions. There exists a collection of Cygnus-related exceptions whose usage is mandatory located at `cygnus-common`:

    fiware-cygnus/cygnus-common/src/main/java/com/telefonica/iot/cygnus/errors/


Specific persistence logic is implemented by overwritting the only abstract method within `TwitterSink`, i.e. `persistBatch(TwitterBatch)`:

    abstract void persistBatch(TwitterBatch) throws Exception;

[Top](#top)

###<a name="section2.4"></a>Inherited counters
Because `TwitterSink` extends `CygnusSink` the following counters are already available for retrieving statistics of any sink extending `TwitterSink`:

* Number of processed events, i.e. the number of events taken from the channel and accumulated in a batch for persistence.
* Number of persisted events, i.e. the number of events within batches finally written/inserted/added in the final storage.

[Top](#top)

##<a name="section3"></a>New sink class
###<a name="section3.1"></a>Specific configuration
The `configure(Context)` method of `TwitterSink` can be extended with specific configuration parameters reading (and defaulting, when required).

[Top](#top)


###<a name="section3.2"></a>Fitting to the specific data structures
To store tweets in HDFS, we have added the properties `hdfs_folder` and `hdfs_files` in the configuration file `agent_<id>.conf` to specify the place of storage. For other types of sinks, the required properties should be added in the configuration file and the code of the sink should treat this information in an appropriate way.


[Top](#top)
 
##<a name="section4"></a>Backend convenience classes
Sometimes all the necessary logic to persist the notified context data cannot be coded in the `persist` abstract method. In this case, you may want to create a backend class or set of classes wrapping the detailed interactions with the final backend. Nevertheless, these classes should not be located at `cygnus-twitter` but at `cygnus-common`.
    
[Top](#top)
    
##<a name="section5"></a>Naming and placing the new classes
New sink classes must be called `Twitter<technology>Sink`, being <i>technology</i> the name of the persistence backend. An example is the already existent sink `TwitterHDFSSink`.

Regarding the new sink class location, it must be:

    fiware-cygnus/cygnus-twitter/src/main/java/com/telefonica/iot/cygnus/sinks/
    
As already explained, backends must be located at:

    fiware-cygnus/cygnus-twitter/src/main/java/com/telefonica/iot/cygnus/backends/<technology>/
    
[Top](#top)
