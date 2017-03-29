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
    * [Scenarios involving a pool of sinks per persistene backend](#section2.3)

## <a name="section1"></a>Basic configurations
Basic configurations are those involving a single listener/source and one or more sinks receiving a copy of the same internal NGSI event obtained once a NGSI notification is processed at the source.

### <a name="section1.1"></a>Simplest scenario
A Cygnus agent based on a single source, a single channel and a single sink.

```
+-----------------------------------+
|  +-----+     +----+     +------+  |
|  | src |-----| ch |-----| sink |  |
|  +-----+     +----+     +------+  |
+-----------------------------------+
```

Its agent configuration file is the usal one:

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

### <a name="section1.2"></a>Scenarios involving multiple persistence backends
Similar as the previous one, in this case there is one channel and one sink per destination backend.

```
+-------------------------------------+
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

Use multi-backend configuration when you simply want the same NGSI context data to be replicated in each one of the backends. This is possible thanks to the default channel selector, which [replicates](https://flume.apache.org/FlumeUserGuide.html#replicating-channel-selector-default) internal NGSI events to all the channels connected to the source. Such a replication is always done independently of the number of FIWARE service paths you own within your FIWARE service, or the number of kind of entities you have.

[Top](#top)

## <a name="section2"></a>Advanced configurations
### <a name="section2.1"></a>Scenarios involving multiple FIWARE services
Cygnus NGSI, as many of the FIWARE components, natively supports multitenancy thanks to the **FIWARE service** concept. NGSI notifications consumed by Cygnus contain a Http header carrying such a value, which is used for segmentating context information in the historical backend in the form of per-service dedicated MySQL/MongoDB/STH database, or HDFS user space, or CKAN organization, etc.

Such a segmentation can be perfectly done by a single sink if it is configured with some *superuser* credentials. I.e. the sink interacts with the backend API authenticating as a superuser able to create databases, HDFS user spaces, CKAN organizations, etc. Then, it is only a question of enabling certain backend users in order the clients can query for data.

Nevertheless, what happens if we want, for instance, to enhance the performance by adding a sink per service? Or if we want to decide applying Name Mappings in a per service way, too? In those cases, a single super sink is not enough.

Next sections explain how to achieve that by implementing advanced configurations.

[Top](#top)

#### <a name="section2.1.1"></a>Multiple agents
The first (and obvious) solution is to instantiate multiple Cygnus agents (multiple JVM processes, each one listening on a dedicated TCP port), one per FIWARE service. This solution ensures each FIWARE service has specific Name Mappings, dedicated sink (or sinks), etc.

```
+--------------------------------------------------------------+
|  +--------------+     +-------------+     +---------------+  |
|  | src-client-1 |-----| ch-client-1 |-----| sink-client-1 |  |
|  +--------------+     +-------------+     +---------------+  |
+--------------------------------------------------------------+

+--------------------------------------------------------------+
|  +--------------+     +-------------+     +---------------+  |
|  | src-client-2 |-----| ch-client-2 |-----| sink-client-2 |  |
|  +--------------+     +-------------+     +---------------+  |
+--------------------------------------------------------------+
                          
                              ...
                          
+--------------------------------------------------------------+
|  +--------------+     +-------------+     +---------------+  |
|  | src-client-N |-----| ch-client-N |-----| sink-client-N |  |
|  +--------------+     +-------------+     +---------------+  |
+--------------------------------------------------------------+
```

For each client/FIWARE service called `<id>` an `agent_<id>.conf` configuration file is required:

```
$ cat /usr/cygnus/conf/agent_<id>.conf
# declarations
cygnus-ngsi-<id>.sources = src-client-<id>
cygnus-ngsi-<id>.sinks = sink-client-<id>
cygnus-ngsi-<id>.channels = ch-client-<id>

# sources
cygnus-ngsi-<id>.sources.src-client-<id>.type = http
cygnus-ngsi-<id>.sources.src-client-<id>.channels = ch-client-<id>
cygnus-ngsi-<id>.sources.src-client-<id>.port = <client_port>
cygnus-ngsi-<id>.sources.src-client-<id>...

# sinks
cygnus-ngsi-<id>.sinks.sink-client-<id>.type = ...
cygnus-ngsi-<id>.sinks.sink-client-<id>.channel = ch-client-<id>
cygnus-ngsi-<id>.sinks.sink-client-<id>...

# channels
cygnus-ngsi-<id>.channels.ch-client-<id>.type = ...
cygnus-ngsi-<id>.channels.ch-client-<id>...
```

[Multi-instance](ref) support is available form the very begining of Cygnus, through the `cygnus_instance_<id>.conf` configuration files used when running Cygnus [as a service](ref):

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
A variation regarding the previus one is to have a single agent (single JVM process) and multiple Http sources (one per FIWARE service).

```
+--------------------------------------------------------------+
|  +--------------+     +-------------+     +---------------+  |
|  | src-client-1 |-----| ch-client-1 |-----| sink-client-1 |  |
|  +--------------+     +-------------+     +---------------+  |
|  +--------------+     +-------------+     +---------------+  |
|  | src-client-2 |-----| ch-client-2 |-----| sink-client-2 |  |
|  +--------------+     +-------------+     +---------------+  |
|        ...                  ...                  ...         |
|  +--------------+     +-------------+     +---------------+  |
|  | src-client-N |-----| ch-client-N |-----| sink-client-N |  |
|  +--------------+     +-------------+     +---------------+  |
+--------------------------------------------------------------+
```

Such an architecture is achieved by using the following agent configuration:

```
$ cat /usr/cygnus/conf/agent_all.conf
# declarations
cygnus-ngsi.sources = src-client-1 src-client-2 ... src-client-N
cygnus-ngsi.sinks = sink-client-1 sink-client-2 ... sink-client-N
cygnus-ngsi.channels = ch-client-1 ch-client-2 ... ch-client-N

# sources
cygnus-ngsi.sources.src-client-1.type = http
cygnus-ngsi.sources.src-client-1.channels = ch-client-1
cygnus-ngsi.sources.src-client-1.port = <client1-port>
cygnus-ngsi.sources.src-client-1...

cygnus-ngsi.sources.src-client-2.type = http
cygnus-ngsi.sources.src-client-2.channels = ch-client-2
cygnus-ngsi.sources.src-client-2.port = <client2-port>
cygnus-ngsi.sources.src-client-2...

...

cygnus-ngsi.sources.src-client-N.type = http
cygnus-ngsi.sources.src-client-N.channels = ch-client-N
cygnus-ngsi.sources.src-client-N.port = <clientN-port>
cygnus-ngsi.sources.src-client-N...

# sinks
cygnus-ngsi.sinks.sink-client-1.type = ...
cygnus-ngsi.sinks.sink-client-1.channel = ch-client-1
cygnus-ngsi.sinks.sink-client-1...

cygnus-ngsi.sinks.sink-client-2.type = ...
cygnus-ngsi.sinks.sink-client-2.channel = ch-client-2
cygnus-ngsi.sinks.sink-client-2...

...

cygnus-ngsi.sinks.sink-client-N.type = ...
cygnus-ngsi.sinks.sink-client-N.channel = ch-client-N
cygnus-ngsi.sinks.sink-client-N...

# channels
cygnus-ngsi.channels.ch-client-1.type = ...
cygnus-ngsi.channels.ch-client-1...

cygnus-ngsi.channels.ch-client-2.type = ...
cygnus-ngsi.channels.ch-client-2...

...

cygnus-ngsi.channels.ch-client-N.type = ...
cygnus-ngsi.channels.ch-client-N...
```

The related `cygnus_instance_all.conf` should look like:

```
$ cat /usr/cygnus/conf/cygnus_instance_all.conf
CYGNUS_USER=cygnus
CONFIG_FOLDER=/usr/cygnus/conf
CONFIG_FILE=/usr/cygnus/conf/agent_all.conf
AGENT_NAME=cygnus-ngsi
LOGFILE_NAME=cygnus.log
ADMIN_PORT=8081
POLLING_INTERVAL=30
```

Use this kind of advanced configuraiton if you hace restrictions in terms of hardware, but you can still expose several TCP ports.

[Top](#top)

#### <a name="section2.1.3"></a>Single agent, single source, multiplexing per FIWARE service
Finally, insted of multiplexing the notifications per TCP port (i.e. one port per FIWARE service), you can enable the reception of all kind of notifications through a single TCP port and perform such a multiplexing internally to a single Cygnus agent.

```
+---------------------------------------------------------+
|         service1 +-------------+     +---------------+  |
|       +----------| ch-client-1 |-----| sink-client-1 |  |
|       |          +-------------+     +---------------+  |
|       | service2 +-------------+     +---------------+  |
|       |    +-----| ch-client-2 |-----| sink-client-2 |  |
|       |    |     +-------------+     +---------------+  |
|  +---------+                                            |
|  | src-all |           ...                  ...         |
|  +---------+                                            |
|       |    |     +-------------+     +---------------+  |
|       |    +-----| ch-client-N |-----| sink-client-N |  |
|       | serviceN +-------------+     +---------------+  |
|       |          +-------------+     +---------------+  |
|       +----------|   ch-def    |-----|   sink-def    |  |
|                  +-------------+     +---------------+  |
+---------------------------------------------------------+
```

I.e. using this agent configuration:

```
$ cat /usr/cygnus/conf/agent_all.conf
# declarations
cygnus-ngsi.sources = src-all
cygnus-ngsi.sinks = sink-client-1 sink-client-2 ... sink-client-N sink-def
cygnus-ngsi.channels = ch-client-1 ch-client-2 ... ch-client-N ch-def

# sources
cygnus-ngsi.sources.src-all.type = http
cygnus-ngsi.sources.src-all.channels = ch-client-1 ch-client-2 ... ch-client-N ch-def
cygnus-ngsi.sources.src-all.selector.type = multiplexing
cygnus-ngsi.sources.src-all.selector.header = fiware-service
cygnus-ngsi.sources.src-all.selector.mappings.<service1> = ch-client-1
cygnus-ngsi.sources.src-all.selector.mappings.<service2> = ch-client-2
...
cygnus-ngsi.sources.src-all.selector.mappings.<serviceN> = ch-client-N
cygnus-ngsi.sources.src-all.selector.mappings.default = ch-def
cygnus-ngsi.sources.src-all.port = 5050

cygnus-ngsi.sources.src-all...

# sinks
cygnus-ngsi.sinks.sink-client-1.type = ...
cygnus-ngsi.sinks.sink-client-1.channel = ch-client-1
cygnus-ngsi.sinks.sink-client-1...

cygnus-ngsi.sinks.sink-client-2.type = ...
cygnus-ngsi.sinks.sink-client-2.channel = ch-client-2
cygnus-ngsi.sinks.sink-client-2...

...

cygnus-ngsi.sinks.sink-client-N.type = ...
cygnus-ngsi.sinks.sink-client-N.channel = ch-client-N
cygnus-ngsi.sinks.sink-client-N...

cygnus-ngsi.sinks.sink-def.type = ...
cygnus-ngsi.sinks.sink-def.channel = ch-def
cygnus-ngsi.sinks.sink-def...

# channels
cygnus-ngsi.channels.ch-client-1.type = ...
cygnus-ngsi.channels.ch-client-1...

cygnus-ngsi.channels.ch-client-2.type = ...
cygnus-ngsi.channels.ch-client-2...

...

cygnus-ngsi.channels.ch-client-N.type = ...
cygnus-ngsi.channels.ch-client-N...

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
ADMIN_PORT=8081
POLLING_INTERVAL=30
```

Use this kind of advanced configuration in the most restrictive scenarios, where both hardware and exposed ports are very limited and/or highly valuable.

[Top](#top)

#### <a name="section2.1.4"></a>Orion Context Broker subscriptions
It seems obvious a per client subscription is required when a per client listener is configured for Cygnus (either by using multiple agents, either by using a single agent with multiple listeners). In that case, each subscription will point to a different endpoint URL.

Nevertheless, it may not seem so obvious how subscriptions look like when a single Cygnus agent with a single listener internally multiplexes the notified context data based on the FIWARE service. In that case, a suscription per client is also required (since Orion does not allow to subscribe to all FIWARE services, due to security reasons), but all subscriptions must point to the same single endpoint URL.

[Top](#top)

### <a name="section2.2"></a>Scenarios involving multiple FIWARE services
Another header, the **FIWARE service path**, can be used to segmentate data belonging to a single tenant or client. Such a header is used by Cygnus to prefix the name of tables and collections in database-based sinks, or as a subfolder within the HDFS user space, or as a package/dataset in CKAN, etc.

Analogous to the FIWARE service, the FIWARE service path can be used to parallelize the processing of the context data notified by Orion Context Broker to Cygnus. Either by creating one listener per service-service path pair (then a Cygnus agent can be created per listener, or a single Cygnus agent with multiple listeners can be created), either creating a single listener for all the service-service path pairs and internarlly to Cygnus multiplex the data based on the FIWARE service-path header.

Regarding Orion Context Broker subscriptions, if multiple listeners are used (either multiple agents either a single agent with many listeners) a subscription for each service-service path pair is required, pointing each one to a different endpoint URL. Nevertheless, if a single Cygnus agent with a single listener, internally multiplexing context data based on the FIWARE service path, is used, then a single suscription can be made for all the service paths within the client FIWARE service. This is achieved by using a special FIWARE service path header's value: `/#`.

[Top](#top)

### <a name="section2.3"></a>Scenarios involving a pool of sinks per persistene backend
A pool of sinks can be configured in order to increase the performance of a Cygnus agent. Such a pool works in a Round Robin way, i.e. sequentially taking NGSI events from a common channel as long as the sinks become available.

```
+------------------------------------+
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
