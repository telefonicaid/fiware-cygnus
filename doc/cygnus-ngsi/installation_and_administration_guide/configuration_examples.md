# <a name="top"></a>Cygnus configuration examples
Content:

* [Basic configurations](#section1)
    * [Simplest scenario](#section1.1)
    * [Scenarios involving multiple persistence backends](#section1.2)
* [Advanced configurations](#section2)
    * [Scenarios involving multiple FIWARE services](#section2.1)
        * [Multiple agents](#section2.1.1)
        * [Single agent, multiple sources](#section2.1.2)
        * [Single agent, single source, multiplexing per FIWARE service](#section2.1.3)
        * [Orion Context Broker subscriptions](#section2.1.4)
    * [Scenarios involving multiple FIWARE service paths](#section2.2)
        * [Multiple agents](#section2.2.1)
        * [Single agent, multiple sources](#section2.2.2)
        * [Single agent, single source, multiplexing per FIWARE service path](#section2.2.3)
        * [Orion Context Broker subscriptions](#section2.2.4)
    * [Scenarios involving multiple FIWARE services, each one involving multiple FIWARE service paths](#section2.3)
    * [Scenarios involving a pool of sinks per persistene backend](#section2.4)

## <a name="section1"></a>Basic configurations
Basic configurations are those involving a single listener/source and one or more sinks receiving a copy of the same internal NGSI event obtained once a NGSI notification is processed at the source.

### <a name="section1.1"></a>Simplest scenario
A Cygnus agent based on a single source, a single channel and a single sink.

```
+-----------------------------------+
|                               JVM |
|  +-----+     +----+     +------+  |
|  | src |-----| ch |-----| sink |  |
|  +-----+     +----+     +------+  |
+-----------------------------------+
```

Its agent configuration file is the usual one:

```
$ cat /usr/cygnus/conf/agent_simplest.conf
# declarations
cygnus-ngsi.sources = src
cygnus-ngsi.sinks = sink
cygnus-ngsi.channels = ch

# sources
cygnus-ngsi.sources.src.type = http
cygnus-ngsi.sources.src.channels = ch
cygnus-ngsi.sources.src.port = 5050
cygnus-ngsi.sources.src...

# sinks
cygnus-ngsi.sinks.sink.type = ...
cygnus-ngsi.sinks.sink.channel = ch
cygnus-ngsi.sinks.sink...

# channels
cygnus-ngsi.channels.ch.type = ...
cygnus-ngsi.channels.ch...
```

[Top](#top)

### <a name="section1.2"></a>Scenarios involving multiple persistence backends
Similar as the previous one, in this case there is one channel and one sink per destination backend.

```
+-------------------------------------+
|                                 JVM |
|              +-----+     +-------+  |
|        +-----| ch1 |-----| sink1 |  |
|        |     +-----+     +-------+  |
|  +-----+     +-----+     +-------+  |
|  | src |-----| ch2 |-----| sink2 |  |
|  +-----+     +-----+     +-------+  |
|        |       ...          ...     |
|        |     +-----+     +-------+  |
|        +-----| ch3 |-----| sink3 |  |
|              +-----+     +-------+  |
+-------------------------------------+
```

Its agent configuration file is:

```
$ cat /usr/cygnus/conf/agent_multibackend.conf
# declarations
cygnus-ngsi.sources = src
cygnus-ngsi.sinks = sink1 sink2 ... sinkN
cygnus-ngsi.channels = ch1 ch2 ... chN

# sources
cygnus-ngsi.sources.src.type = http
cygnus-ngsi.sources.src.channels = ch1 ch2 ... chN
cygnus-ngsi.sources.src.port = 5050
cygnus-ngsi.sources.src...

# sinks
cygnus-ngsi.sinks.sink1.type = ...
cygnus-ngsi.sinks.sink1.channel = ch1
cygnus-ngsi.sinks.sink1...

cygnus-ngsi.sinks.sink2.type = ...
cygnus-ngsi.sinks.sink2.channel = ch2
cygnus-ngsi.sinks.sink2...

...

cygnus-ngsi.sinks.sinkN.type = ...
cygnus-ngsi.sinks.sinkN.channel = chN
cygnus-ngsi.sinks.sinkN...

# channels
cygnus-ngsi.channels.ch1.type = ...
cygnus-ngsi.channels.ch1...

cygnus-ngsi.channels.ch2.type = ...
cygnus-ngsi.channels.ch2...

...

cygnus-ngsi.channels.chN.type = ...
cygnus-ngsi.channels.chN...
```

Use multi-backend configuration when you simply want the same NGSI context data to be replicated in each one of the backends. This is possible thanks to the default channel selector, which [replicates](https://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default) internal NGSI events to all the channels connected to the source. Such a replication is always done independently of the number of FIWARE service paths you own within your FIWARE service, or the number of entity types you have.

**NOTE**: it must be taken into account Flume's replicating channel selector is designed to fail if a copy of the event cannot be put into one of the configured channels. If you want to avoid this behavior, just configure all the channels as optional ones (after explicitly saying the selector is the replicating one):

```
cygnus-ngsi.sources.src.channels = ch1 ch2 ... chN
cygnus-ngsi.sources.src.selector.type = replicating
cygnus-ngsi.sources.src.selector.optional = ch1 ch2 ... chN
```

[Top](#top)

## <a name="section2"></a>Advanced configurations
### <a name="section2.1"></a>Scenarios involving multiple FIWARE services
Cygnus NGSI, as many of the FIWARE components, natively supports multitenancy thanks to the **FIWARE service** concept. NGSI notifications consumed by Cygnus contain a HTTP header carrying such a value, which is used for segmentating context information in the historical backend in the form of per-service dedicated [MySQL](../flume_extensions_catalogue/ngsi_mysql_sink.md)/[MongoDB](../flume_extensions_catalogue/ngsi_mongo.md)/[STH](../flume_extensions_catalogue/ngsi_sth.md) database, or [HDFS](../flume_extensions_catalogue/ngsi_hdfs_sink.md) user space, or [CKAN](../flume_extensions_catalogue/ngsi_ckan_sink.md) organization (non exhaustive list).

Such a segmentation can be perfectly done by a single sink if it is configured with some *superuser* credentials. I.e. the sink interacts with the backend API authenticating as a superuser able to create databases, HDFS user spaces, CKAN organizations, etc. Then, it is only a question of enabling certain backend users in order the clients can query for data.

Nevertheless, what happens if we want, for instance, to enhance the performance by adding a sink per service? Or if we want to decide applying Name Mappings in a per service way, too? In those cases, a single super sink is not enough.

Next sections explain how to achieve that by implementing advanced configurations.

[Top](#top)

#### <a name="section2.1.1"></a>Multiple agents
The first (and obvious) solution is to instantiate multiple Cygnus agents (multiple JVM processes, each one listening on a dedicated TCP port), one per FIWARE service. This solution ensures each FIWARE service has specific Name Mappings, dedicated sink (or sinks), etc.

```
+-----------------------------------------------------+
|                                                JVM1 |
|  +-----------+     +----------+     +------------+  |
|  | src-svc-1 |-----| ch-svc-1 |-----| sink-svc-1 |  |
|  +-----------+     +----------+     +------------+  |
+-----------------------------------------------------+

+-----------------------------------------------------+
|                                                JVM2 |
|  +-----------+     +----------+     +------------+  |
|  | src-svc-2 |-----| ch-svc-2 |-----| sink-svc-2 |  |
|  +-----------+     +----------+     +------------+  |
+-----------------------------------------------------+

                           ...

+-----------------------------------------------------+
|                                                JVMN |
|  +-----------+     +----------+     +------------+  |
|  | src-svc-N |-----| ch-svc-N |-----| sink-svc-N |  |
|  +-----------+     +----------+     +------------+  |
+-----------------------------------------------------+
```

For each client/FIWARE service called `<id>` an `agent_<id>.conf` configuration file is required:

```
$ cat /usr/cygnus/conf/agent_<id>.conf
# declarations
cygnus-ngsi-<id>.sources = src-svc-<id>
cygnus-ngsi-<id>.sinks = sink-svc-<id>
cygnus-ngsi-<id>.channels = ch-svc-<id>

# sources
cygnus-ngsi-<id>.sources.src-svc-<id>.type = http
cygnus-ngsi-<id>.sources.src-svc-<id>.channels = ch-svc-<id>
cygnus-ngsi-<id>.sources.src-svc-<id>.port = <svc-port>
cygnus-ngsi-<id>.sources.src-svc-<id>...

# sinks
cygnus-ngsi-<id>.sinks.sink-svc-<id>.type = ...
cygnus-ngsi-<id>.sinks.sink-svc-<id>.channel = ch-svc-<id>
cygnus-ngsi-<id>.sinks.sink-svc-<id>...

# channels
cygnus-ngsi-<id>.channels.ch-svc-<id>.type = ...
cygnus-ngsi-<id>.channels.ch-svc-<id>...
```

[Multi-instance](ref) support is available from the very begining of Cygnus, through the `cygnus_instance_<id>.conf` configuration files used when running Cygnus [as a service](ref):

```
$ cat /usr/cygnus/conf/cygnus_instance_<id>.conf
CYGNUS_USER=cygnus
CONFIG_FOLDER=/usr/cygnus/conf
CONFIG_FILE=/usr/cygnus/conf/agent_<id>.conf
AGENT_NAME=cygnus-ngsi-<id>
LOGFILE_NAME=cygnus_<id>.log
ADMIN_PORT=<client_admin_port>
POLLING_INTERVAL=30
```

Alternatively, multiple instances of Cygnus can be run as single processes as well by running this command as many times as needed:

```
$ APACHE_FLUME_HOME/bin/cygnus-flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/agent_<id>.conf -n <agent_name> -Dflume.root.logger=INFO,console -Duser.timezone=UTC [-p <mgmt-if-port>] [-g <web-app-port>] [-t <polling-interval>]
```

Use this kind of advanced configuration if you have no restrictions in terms of hardware (please observe each JVM will require its own resources in terms of CPU and memory) and exposed TCP ports.

[Top](#top)

#### <a name="section2.1.2"></a>Single agent, multiple sources
A variation regarding the previus one is to have a single agent (single JVM process) and multiple HTTP sources (one per FIWARE service).

```
+-----------------------------------------------------+
|                                                 JVM |
|  +-----------+     +----------+     +------------+  |
|  | src-svc-1 |-----| ch-svc-1 |-----| sink-svc-1 |  |
|  +-----------+     +----------+     +------------+  |
|  +-----------+     +----------+     +------------+  |
|  | src-svc-2 |-----| ch-svc-2 |-----| sink-svc-2 |  |
|  +-----------+     +----------+     +------------+  |
|       ...               ...                ...      |
|  +-----------+     +----------+     +------------+  |
|  | src-svc-N |-----| ch-svc-N |-----| sink-svc-N |  |
|  +-----------+     +----------+     +------------+  |
+-----------------------------------------------------+
```

Such an architecture is achieved by using the following agent configuration:

```
$ cat /usr/cygnus/conf/agent_all.conf
# declarations
cygnus-ngsi.sources = src-svc-1 src-svc-2 ... src-svc-N
cygnus-ngsi.sinks = sink-svc-1 sink-svc-2 ... sink-svc-N
cygnus-ngsi.channels = ch-svc-1 ch-svc-2 ... ch-svc-N

# sources
cygnus-ngsi.sources.src-svc-1.type = http
cygnus-ngsi.sources.src-svc-1.channels = ch-svc-1
cygnus-ngsi.sources.src-svc-1.port = <svc-1-port>
cygnus-ngsi.sources.src-svc-1...

cygnus-ngsi.sources.src-svc-2.type = http
cygnus-ngsi.sources.src-svc-2.channels = ch-svc-2
cygnus-ngsi.sources.src-svc-2.port = <svc-2-port>
cygnus-ngsi.sources.src-svc-2...

...

cygnus-ngsi.sources.src-svc-N.type = http
cygnus-ngsi.sources.src-svc-N.channels = ch-svc-N
cygnus-ngsi.sources.src-svc-N.port = <svc-N-port>
cygnus-ngsi.sources.src-svc-N...

# sinks
cygnus-ngsi.sinks.sink-svc-1.type = ...
cygnus-ngsi.sinks.sink-svc-1.channel = ch-svc-1
cygnus-ngsi.sinks.sink-svc-1...

cygnus-ngsi.sinks.sink-svc-2.type = ...
cygnus-ngsi.sinks.sink-svc-2.channel = ch-svc-2
cygnus-ngsi.sinks.sink-svc-2...

...

cygnus-ngsi.sinks.sink-svc-N.type = ...
cygnus-ngsi.sinks.sink-svc-N.channel = ch-svc-N
cygnus-ngsi.sinks.sink-svc-N...

# channels
cygnus-ngsi.channels.ch-svc-1.type = ...
cygnus-ngsi.channels.ch-svc-1...

cygnus-ngsi.channels.ch-svc-2.type = ...
cygnus-ngsi.channels.ch-svc-2...

...

cygnus-ngsi.channels.ch-svc-N.type = ...
cygnus-ngsi.channels.ch-svc-N...
```

The related `cygnus_instance_all.conf` should look like:

```
$ cat /usr/cygnus/conf/cygnus_instance_all.conf
CYGNUS_USER=cygnus
CONFIG_FOLDER=/usr/cygnus/conf
CONFIG_FILE=/usr/cygnus/conf/agent_all.conf
AGENT_NAME=cygnus-ngsi
LOGFILE_NAME=cygnus.log
ADMIN_PORT=5080
POLLING_INTERVAL=30
```

Use this kind of advanced configuraiton if you hace restrictions in terms of hardware, but you can still expose several TCP ports.

[Top](#top)

#### <a name="section2.1.3"></a>Single agent, single source, multiplexing per FIWARE service
Finally, insted of multiplexing the notifications per TCP port (i.e. one port per FIWARE service), you can enable the reception of all kind of notifications through a single TCP port and perform such a multiplexing internally to a single Cygnus agent using Flume's [Multiplexing Channel Processor](https://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector).

```
+---------------------------------------------------+
|                                               JVM |
|            scv-1 +----------+     +------------+  |
|       +----------| ch-svc-1 |-----| sink-svc-1 |  |
|       |          +----------+     +------------+  |
|       |    svc-2 +----------+     +------------+  |
|       |    +-----| ch-svc-2 |-----| sink-svc-2 |  |
|       |    |     +----------+     +------------+  |
|  +---------+                                      |
|  | src-all |          ...               ...       |
|  +---------+                                      |
|       |    |     +----------+     +------------+  |
|       |    +-----| ch-svc-N |-----| sink-svc-N |  |
|       |    svc-N +----------+     +------------+  |
|       |          +----------+     +------------+  |
|       +----------|  ch-def  |-----|  sink-def  |  |
|                  +----------+     +------------+  |
+---------------------------------------------------+
```

I.e. using this agent configuration:

```
$ cat /usr/cygnus/conf/agent_all.conf
# declarations
cygnus-ngsi.sources = src-all
cygnus-ngsi.sinks = sink-svc-1 sink-svc-2 ... sink-svc-N sink-def
cygnus-ngsi.channels = ch-svc-1 ch-svc-2 ... ch-svc-N ch-def

# sources
cygnus-ngsi.sources.src-all.type = http
cygnus-ngsi.sources.src-all.channels = ch-svc-1 ch-svc-2 ... ch-svc-N ch-def
cygnus-ngsi.sources.src-all.selector.type = multiplexing
cygnus-ngsi.sources.src-all.selector.header = fiware-service
cygnus-ngsi.sources.src-all.selector.mappings.<svc-1> = ch-svc-1
cygnus-ngsi.sources.src-all.selector.mappings.<svc-2> = ch-svc-2
...
cygnus-ngsi.sources.src-all.selector.mappings.<svc-N> = ch-svc-N
cygnus-ngsi.sources.src-all.selector.mappings.default = ch-def
cygnus-ngsi.sources.src-all.port = 5050

cygnus-ngsi.sources.src-all...

# sinks
cygnus-ngsi.sinks.sink-svc-1.type = ...
cygnus-ngsi.sinks.sink-svc-1.channel = ch-svc-1
cygnus-ngsi.sinks.sink-svc-1...

cygnus-ngsi.sinks.sink-svc-2.type = ...
cygnus-ngsi.sinks.sink-svc-2.channel = ch-svc-2
cygnus-ngsi.sinks.sink-svc-2...

...

cygnus-ngsi.sinks.sink-svc-N.type = ...
cygnus-ngsi.sinks.sink-svc-N.channel = ch-svc-N
cygnus-ngsi.sinks.sink-svc-N...

cygnus-ngsi.sinks.sink-def.type = ...
cygnus-ngsi.sinks.sink-def.channel = ch-def
cygnus-ngsi.sinks.sink-def...

# channels
cygnus-ngsi.channels.ch-svc-1.type = ...
cygnus-ngsi.channels.ch-svc-1...

cygnus-ngsi.channels.ch-svc-2.type = ...
cygnus-ngsi.channels.ch-svc-2...

...

cygnus-ngsi.channels.ch-svc-N.type = ...
cygnus-ngsi.channels.ch-svc-N...

cygnus-ngsi.channels.ch-def.type = ...
cygnus-ngsi.channels.ch-def...
```

The related `cygnus_instance_all.conf` should look like:

```
$ cat /usr/cygnus/conf/cygnus_instance_all.conf
CYGNUS_USER=cygnus
CONFIG_FOLDER=/usr/cygnus/conf
CONFIG_FILE=/usr/cygnus/conf/agent_all.conf
AGENT_NAME=cygnus-ngsi
LOGFILE_NAME=cygnus.log
ADMIN_PORT=5080
POLLING_INTERVAL=30
```

Use this kind of advanced configuration in the most restrictive scenarios, where both hardware and exposed ports are very limited and/or highly valuable.

[Top](#top)

#### <a name="section2.1.4"></a>Orion Context Broker subscriptions
It seems obvious a per client subscription is required when a per client listener is configured for Cygnus (either by using multiple agents, either by using a single agent with multiple listeners). In that case, each subscription will point to a different endpoint URL.

Nevertheless, it may not seem so obvious how subscriptions look like when a single Cygnus agent with a single listener internally multiplexes the notified context data based on the FIWARE service. In that case, a suscription per client is also required (since Orion does not allow to subscribe to all FIWARE services, due to security reasons), but all subscriptions must point to the same single endpoint URL.

In any case, please consider the trade off between using as minimum Orion subscriptions as possible (subscriptions number impacts in terms of Orion performance) and using as maximum JVM/Cygnus agents (each JVM has its own resources in terms of memory and CPU).

[Top](#top)

### <a name="section2.2"></a>Scenarios involving multiple FIWARE service paths
Another header, the **FIWARE service path**, can be used to segmentate data belonging to a single tenant or client. Such a header is used by Cygnus to prefix the name of tables and collections in database-based sinks, or it is used as a subfolder within the [HDFS](../flume_extensions_catalogue/ngsi_hdfs_sink.md) user space, or as a package/dataset in [CKAN](../flume_extensions_catalogue/ngsi_ckan_sink.md) (non exhaustive list).

Analogous to the FIWARE service, the FIWARE service path can be used to parallelize the processing of the context data notified by Orion Context Broker to Cygnus. Either by creating one listener per (service, service path) pair (then a Cygnus agent can be created per listener, or a single Cygnus agent with multiple listeners can be created), either creating a single listener for all the (service, service path) pairs and internally to Cygnus multiplex the data based on the FIWARE service-path header.

[Top](#top)

#### <a name="section2.2.1"></a>Multiple agents
In this case, a Cygnus agent will be deployed for each FIWARE service path within a single FIWARE service.

```
+-----------------------------------------------------------------+
|                                                            JVM1 |
|  +---------------+     +--------------+     +----------------+  |
|  | src-svcpath-1 |-----| ch-svcpath-1 |-----| sink-svcpath-1 |  |
|  +---------------+     +--------------+     +----------------+  |
+-----------------------------------------------------------------+

+-----------------------------------------------------------------+
|                                                            JVM2 |
|  +---------------+     +--------------+     +----------------+  |
|  | src-svcpath-2 |-----| ch-svcpath-2 |-----| sink-svcpath-2 |  |
|  +---------------+     +--------------+     +----------------+  |
+-----------------------------------------------------------------+

                              ...

+-----------------------------------------------------------------+
|                                                            JVMN |
|  +---------------+     +--------------+     +----------------+  |
|  | src-svcpath-N |-----| ch-svcpath-N |-----| sink-svcpath-N |  |
|  +---------------+     +--------------+     +----------------+  |
+-----------------------------------------------------------------+
```

[Top](#top)

#### <a name="section2.2.2"></a>Single agent, multiple sources
Similar to the case above, a single Cygnus agent starts a HTTP listener for each FIWARE service path within a single FIWARE service.

```
+-----------------------------------------------------------------+
|                                                             JVM |
|  +---------------+     +--------------+     +----------------+  |
|  | src-svcpath-1 |-----| ch-svcpath-1 |-----| sink-svcpath-1 |  |
|  +---------------+     +--------------+     +----------------+  |
|  +---------------+     +--------------+     +----------------+  |
|  | src-svcpath-2 |-----| ch-svcpath-2 |-----| sink-svcpath-2 |  |
|  +---------------+     +--------------+     +----------------+  |
|         ...                   ...                   ...         |
|  +---------------+     +--------------+     +----------------+  |
|  | src-svcpath-N |-----| ch-svcpath-N |-----| sink-svcpath-N |  |
|  +---------------+     +--------------+     +----------------+  |
+-----------------------------------------------------------------+
```

[Top](#top)

#### <a name="section2.2.3"></a>Single agent, single source, multiplexing per FIWARE service path
The single Cygnus agent multiplexes the notified NGSI context data based on the FIWARE service path header.

```
+-----------------------------------------------------------+
|                                                       JVM |
|        svcpath-1 +--------------+     +----------------+  |
|       +----------| ch-svcpath-1 |-----| sink-svcpath-1 |  |
|       |          +--------------+     +----------------+  |
|       |svcpath-2 +--------------+     +----------------+  |
|       |    +-----| ch-svcpath-2 |-----| sink-svcpath-2 |  |
|       |    |     +--------------+     +----------------+  |
|  +---------+                                              |
|  | src-all |            ...                   ...         |
|  +---------+                                              |
|       |    |     +--------------+     +----------------+  |
|       |    +-----| ch-svcpath-N |-----| sink-svcpath-N |  |
|       |svcpath-N +--------------+     +----------------+  |
|       |          +--------------+     +----------------+  |
|       +----------|    ch-def    |-----|    sink-def    |  |
|                  +--------------+     +----------------+  |
+-----------------------------------------------------------+
```

[Top](#top)

#### <a name="section2.2.4"></a>Orion Context Broker subscriptions
Regarding Orion Context Broker subscriptions, if multiple listeners are used (either multiple agents either a single agent with many listeners) a subscription for each (service, service path) pair is required, pointing each one to a different endpoint URL. Nevertheless, if a single Cygnus agent with a single listener, internally multiplexing context data based on the FIWARE service path, is used, then a single suscription can be made for all the service paths within the client FIWARE service. This is achieved by using a special FIWARE service path header's value: `/#`.

In any case, please consider the trade off between using as minimum Orion subscriptions as possible (subscriptions number impacts in terms of Orion performance) and using as maximum JVM/Cygnus agents (each JVM has its own resources in terms of memory and CPU).

[Top](#top)

### <a name="section2.3"></a>Scenarios involving multiple FIWARE services, each one involving multiple FIWARE service paths
Attending to the configurations seen in previous sections, there are two possibilities:

* Create a Cygnus agent for each (service, service path) pair, which ensures a HTTP listener per pair in a JVM per pair.
* Create a Cygnus agent for each service, and then configure as many HTTP listeners as FIWARE service paths within the FIWARE service.

Nevertheless, it is not possible to have a single HTTP listener for all FIWARE services and FIWARE service paths, since current status of Flume technology does not allow for multiplexing based on two headers; current Flume's [Multiplexing Channel Selector](https://flume.apache.org/FlumeUserGuide.html#multiplexing-channel-selector) only enables multiplexion based on a single header. **A custom channel selector must be implemented for this purpose**.

Another possibility is to have a two-levels architecture: the first level contains a single Cygnus agent in charge of multiplexing per FIWARE service; its sinks simply forward the NGSI context information to the next level (using Flume's Avro [sinks](https://flume.apache.org/FlumeUserGuide.html#avro-sink) and [sources](https://flume.apache.org/FlumeUserGuide.html#avro-source), for instance). Then, the second level contains a Cygnus agent per FIWARE service, multiplexing the context information in a per FIWARE service path basis.

```
                    +--------------------------+   
              +-----| JVM-all-svcpath-in-svc-1 |
              |     +--------------------------+
+-------------+     +--------------------------+
| JVM-all-svc |-----| JVM-all-svcpath-in-svc-2 |
+-------------+     +--------------------------+
              |                  ...
              |     +--------------------------+   
              +-----| JVM-all-svcpath-in-svc-N |
                    +--------------------------+
```

[Top](#top)

### <a name="section2.4"></a>Scenarios involving a pool of sinks per persistene backend
A pool of sinks can be configured in order to increase the performance of a Cygnus agent. Such a pool works in a Round Robin way, i.e. sequentially taking NGSI events from a common channel as long as the sinks become available.

```
+------------------------------------+
|                                JVM |
|                         +-------+  |
|                   +-----| sink1 |  |
|                   |     +-------+  |
|  +-----+     +----+     +-------+  |
|  | src |-----| ch |-----| sink2 |  |
|  +-----+     +----+     +-------+  |
|                   |        ...     |
|                   |     +-------+  |
|                   +-----| sinkN |  |
|                         +-------+  |
+------------------------------------+
```

This configuration is enabled by configuring a Flume's native [Load Balancing Sink Processor](https://flume.apache.org/FlumeUserGuide.html#load-balancing-sink-processor) using its default configuration:

```
$ cat /usr/cygnus/conf/agent_simplest.conf
# declarations
cygnus-ngsi.sources = src
cygnus-ngsi.sinks = sink1 sink2 ... sinkN
cygnus-ngsi.channels = ch

# sources
cygnus-ngsi.sources.src.type = http
cygnus-ngsi.sources.src.channels = ch
cygnus-ngsi.sources.src.port = 5050
cygnus-ngsi.sources.src...

# sinks
cygnus-ngsi.sinks.sink1.type = ...
cygnus-ngsi.sinks.sink1.channel = ch
cygnus-ngsi.sinks.sink1...

cygnus-ngsi.sinks.sink2.type = ...
cygnus-ngsi.sinks.sink2.channel = ch
cygnus-ngsi.sinks.sink2...

...

cygnus-ngsi.sinks.sinkN.type = ...
cygnus-ngsi.sinks.sinkN.channel = ch
cygnus-ngsi.sinks.sinkN...

# sink processor
cygnus-ngsi.sinkgroups = sg
cygnus-ngsi.sinkgroups.sg.sinks = sink1 sink2 ... sinkN
cygnus-ngsi.sinkgroups.sg.processor.type = load_balance
cygnus-ngsi.sinkgroups.sg.processor.selector = round_robin

# channels
cygnus-ngsi.channels.ch.type = ...
cygnus-ngsi.channels.ch...
```

[Top](#top)
