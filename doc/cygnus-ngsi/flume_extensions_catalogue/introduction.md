# <a name="top"></a>Flume extensions catalogue
This document details the catalogue of extensions developed for Cygnus on top of [Apache Flume](https://flume.apache.org/).

# Intended audience
The Flume extensions catalogue is a basic piece of documentation for all those FIWARE users using Cygnus. It describes the available extra components added to the Flume technology in order to deal with NGSI-like context data in terms of historic building.

Software developers may also be interested in this catalogue since it may guide the creation of new components (specially, sinks) for Cygnus/Flume.

[Top](#top)

# Structure of the document
The document starts detailing the naming conventions adopted in Cygnus when creating data structures in the different storages. This means those data structure (databases, files, tables, collections, etc) names will derive from a subset of the NGSI-like notified information (mainly fiware-service and fiware-servicePath headers, entityId and entityType).

Then, it is time to explain [`NGSIRestHandler`](./ngsi_rest_handler.md), the NGSI oriented handler for the http Fume source in charge of translating a NGSI-like notification into a Flume event.

Then, each one of the NGSI oriented sinks is described; for each sink an explanation about the functionality (including how the information within a Flume event is mapped into the storage data structures, according to the above mentioned naming conventions), configuration, uses cases and implementation details are given.

Finally, other components added to Flume are explained, such as the [`GroupingInterceptor`](./ngsi_grouping_interceptor.md) or the [`RoundRobinChannelSelector`](./round_robin_channel_selector.md).

[Top](#top)
