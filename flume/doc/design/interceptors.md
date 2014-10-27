# Flume Interceptors at Cygnus

## What is a Flume Interceptor
Interceptors are components of the Flume agent architecture. Typically, such an agent is based on a source dealing with the input, a sink dealing with the output and a channel communicating them. The source processes the input, producing Flume events (an object based on a set of headers and a byte-based body) that are put in the channel; then the sink consumes the events by getting them from the channel. This basic architecture may be enriched by the addition of Interceptors, a chained sequence of Flume events preprocessors that <i>intercept</i> the events before they are put into the channel and performing one of these operations:

* Drop the event.
* Modify an existent header of the Flume event.
* Add a new header to the Flume event.

Interceptors should never modify the body part. Once an event is preprocessed, it is put in the channel as usual.

As can be seen, this mechanism allows for very useful ways of enriching the basic Flume events a certain Flume source may generate. Let's see how Cygnus makes use of this concept in order to add certain information to the Flume events created from the Orion notifications.   

## `Timestamp` Interceptor
This is an Interceptor that can be [natively found](https://flume.apache.org/FlumeUserGuide.html#timestamp-interceptor) in any Flume distribution. It adds a `timestamp` header to the Flume event, whose value expresses the number of miliseconds from January the 1st, 1970.

The way Cygnus makes use of this Interceptor is the standard one:

    cygnusagent.sources.http-source.interceptors = ts <other-interceptors>
    cygnusagent.sources.http-source.interceptors.ts.type = timestamp 

## `DestinationExtractor` Interceptor
This is custom Interceptor specifically designed for Cygnus. Its goal is to infer the destination where the data regarding a notified entity is going to be persisted. This destination, depending on the used sinks, may be a HDFS file name, a MySQL table name or a CKAN resource name. Such an inference is made by inspecting (but not modifying) certain configured fields of the body part of the event; if the concatenation of such fields matches a configured regular expresion, then the configured destination is added as the value of a `destination` header.

There exists a <i>matching table</i> file containing the above <i>matching rules</i> definition, following this format line by line:

    <integer id>|<comma-separated fields to be concatenated>|<regex to be applied>|<destination>

For instance:

    1|entityId,entityType|Room\.(\d*)Room|numeric_rooms
    2|entityId,entityType|Room\.(\D*)Room|character_rooms
    3|entityType|Room|other_roorms

The above rules set that:

* All the `Room` entities having their ids composed by a `Room.` and an integer will be persisted in a `numeric_rooms` destination.
* All the `Room` entities having their ids composed by a `Room.` and any number of characters (no digits) will be persisted in a `character_rooms` destination.
* All other rooms will go to `other_rooms`.

Rules are tryed sequentially, and if any rules matches then the default destination for the notified entity is generated, i.e. the concatenation of the entity id, `_' and the entity type.

The available <i>dictionary</i> of fields for concatenation is:

* entitydId
* entityType
* fiwareService

The matching table file is usually placed at `[FLUME_HOME_DIR]/conf`, and there exists a template within Cygnus distribution. The usage of such an Interceptor is:

    cygnusagent.sources.http-source.interceptors = de <other-interceptors>
    cygnusagent.sources.http-source.interceptors.de.type = es.tid.fiware.fiwareconnectors.cygnus.interceptors.DestinationExtractor$Builder
    cygnusagent.sources.http-source.interceptors.de.matching_table = [FLUME_HOME_DIR]/conf/matching_table.conf

It is <b>very important</b> to configure the <b>absolute path to the matching table file</b>. 

## Contact

* Fermín Galán Márquez (fermin.galanmarquez@telefonica.com).
* Francisco Romero Bueno (francisco.romerobueno@telefonica.com).
