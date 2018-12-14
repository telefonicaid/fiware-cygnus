# <a name="top"></a>Logs and alarms
Content:

* [Introduction](#section1)
* [Log message types](#section2)
* [Alarm conditions](#seciton3)

## <a name="section1"></a>Introduction
This document describes the alarms a platform integrating Cygnus should raise when an incident happens. Thus, it is addressed to professional operators and such platform administrators.

Cygnus messages are explained before the alarm conditions deriving from those messages are described.

For each alarm, the following information is given:

* <b>Alarm identifier</b>. A unique numerical identifier starting by 1.
* <b>Severity</b>. CRITICAL or WARNING.
* <b>Detection strategy</b>. An example log trace which identifies related alarm.
* <b>Stop condition</b>. An example log trace which means that related problem is no longer active.
* <b>Description</b>. A detailed explanation of the situation which triggers the alarm.
* <b>Action</b>. A detailed plan to cope with this situation (e.g. reboots, checks connectivities, etc).

[Top](#top)

## <a name="section2"></a>Log message types
Cygnus logs are categorized under seven message types, each one identified by a tag in the custom message part of the trace. These are the tags:

* <i>Fatal error</i> (`FATAL` level). These kind of errors may cause Cygnus to stop, and thus must be repported to the development team through [stackoverflow.com](http://stackoverflow.com/search?q=fiware) (please, tag it with <i>fiware</i>).

    Example: `Fatal error (SSL cannot be used, no such algorithm. Details=...)`
* <i>Runtime error</i> (`ERROR` level). These kind of errors may cause Cygnus to fail, and thus must be repported to the development team through [stackoverflow.com](http://stackoverflow.com/search?q=fiware) (please, tag it with <i>fiware</i>).

    Example: `Runtime error (The Hive table cannot be created. Hive query=.... Details="...)`
* <i>Bad configuration</i> (`ERROR` level). These kind of errors regard to a bad configuration parameter, and eventually may lead to a Cygnus fail.

    Example: `Bad configuration (Unrecognized HDFS API. The sink can start, but the data is not going to be persisted!)`
* <i>Bad HTTP notification</i> (`WARN` level). These kind of errors are related to malformed notifications regarding the HTTP message: not supported REST method, target, user agent or content type, and empty body as well. They are exclusively thrown by the `NGSIRestHandler` component.  

    Example: `Bad HTTP notification (aggregation target not supported)`
* <i>Bad context data</i> (`WARN` level). These kind of errors are related to semantic inconsistences within the notified context data: anomalous number of attributes or not existent attribute (even when the number of attributes matches) for an already known instance. They are exclusively thrown by the sinks.

    Example: `Bad context data (The markup in the document following the root element must be well-formed)`
* <i>Channel error</i> (`ERROR` level). These kind of errors tell about problems with the internal channel of the agent. This channel is used as part of the failover mechanisms of Flume, storing those events that cannot be processed by the sinks. Nevertheless, the channel may fail itself, either because the HTTP source is not able to put the event (channel error, or simply it is full), either because the sink cannot get a new event.

    Example: `Channel error (The event could not be got. Details=...)`
* <i>Persistence error</i> (`ERROR` level). These kind of errors tell about problems with the persistence backend: unable to connect or not existent entity (when the backend needs to have provisioned a container for that entity, e.g. entity-related tables in MySQL or CKAN). They are exclusively thrown by the sinks. Please observe Cygnus itself may solve the problem thanks to the channel-based failover mechanism of Flume, and the Flume Failover Sink Processor which switchs to a passive sink (if configured).

    Example: `Persistence error (Could not connect to the MySQL server)`

Debug messages are labeled as <i>Debug</i>, with a logging level of `DEBUG`. Informational messages such as Cygnus version, transaction start/end and other are labeled as <i>Informational</i>, being `INFO` the logging level.

[Top](#top)

## <a name="section3"></a>Alarm conditions
Alarm ID | Severity | Detection strategy | Stop condition | Description | Action
---|---|---|---|---|---
1 | CRITICAL | A `FATAL` trace is found. | For each configured Cygnus component (i.e. `NGSIRestHandler`, `NGSIHDFSSink`, `NGSIMySQLSink` and `NGSICKANSink`), the following trace is found: <i>Startup completed</i>. | A problem has happend at Cygnus startup. The `msg` field details the particular problem. | Fix the issue that is precluding Cygnus startup, e.g. if the problem was due to the listening port of a certain source is already being used, then change such listening port or stop the process using it.  
2 | CRITICAL | The following `ERROR` trace is found: <i>Runtime error</i>. | N/A | A runtime error has happened. The `msg` field containts the detailed information. | Restart Cygnus. If the error persits (e.g. new <i>Runtime errors</i> appear within the next hour), scale up the problem to the development team.
3 | CRITICAL | The following `ERROR` trace is found: <i>Bad configuration</i>. | For each configured Cygnus component (i.e. `NGSIRestHandler`, `NGSIHDFSSink`, `NGSIMySQLSink` and `NGSICKANSink`), the following `INFO` trace is found: <i>Startup completed</i>. | A Cygnus component has not been configured in the appropriate way. | Configure the component in the appropriate way.
4 | CRITICAL | The following `ERROR` trace is found: <i>Channel error</i>. | The following `INFO` traces are found: <i>Event got from the channel</i>. | Flume events, put by the sources, cannot be got by the sinks from the internal channel due to a problem with the channel (most probably) or the sink itself | A runtime error has happened. The `msg` field containts the detailed information. | Restart Cygnus. If the error persits (e.g. new <i>Channel errors</i> appear within the next hour), scale up the problem to the development team.
5 | WARNING | The following `WARN` trace is found: <i>Bad HTTP notification</i>. | The following `INFO` traces are found: <i>Event put in the channel</i>. | The HTTP notification sent by Orion is not properly formed, being the target, the method, the user agent and/or the content type anomalous. | Nothing has to be done at Cygnus. Check why the sender (Orion Context Broker) is building the notification in such anomalous way.
6 | WARNING | The following `WARN` trace is found: <i>Bad context data in sink_name, being <i>sink_name</i>: `NGSIHDFSSink`, `NGSIMySQLSink` or `NGSICKANSink`</i>. | The following `INFO` traces are found: <i>Persisting data in sink_name</i>, being <i>sink_name</i> the same sink that raised the alarm. | The context data within the notification is wrong, either making reference to an unexistent entity, either showing an abnormal number of attributes, either showing an unexistent attribute. | Nothing has to be done at Cygnus. Check the provision of the data <i>containers</i> (e.g. tables in case of using MySQL) and fix any inconsistence it may exist.
7 | WARNING | The following `ERROR` trace is found: <i>Persistence error in sink_name, being <i>sink_name</i>: `NGSIHDFSSink`, `NGSIMySQLSink` or `NGSICKANSink`</i>. | The following `INFO` traces are found: <i>Persisting data in sink_name</i>, being <i>sink_name</i> the same sink that raised the alarm. | Any of the sinks is not able to persist the context data in the final storage HDFS, MySQL or CKAN), due to a connection problem or a storage crash/shutdown. | Once solved the problem with the storage, Cygnus should be able to fix this kind of errors automatically by means of the internal channel, which works as a temporal buffer for not already processed Flume events (containing context data to be persisted).

[Top](#top)
