#<a name="top"></a>Cygnus configuration examples
Content:

* [Basic configurations](#section1)
* [Advanced configurations](#section2)
    * [Scenarios involving multiple FIWARE services](#section1)
* [Using interceptors](#section3)

##<a name="section1"></a>Basic configurations
Coming soon.

[Top](#top)

##<a name="section2"></a>Advanced configurations
###<a name="section2.1"></a>Scenarios involving multiple FIWARE services
Cygnus NGSI, as many of the FIWARE components, natively supports multitenancy thanks to the FIWARE service concept. NGSI notifications consumed by Cygnus contain a Http header carrying such a value, which is used for segmentating context information in the historical backend in the form of per-service dedicated MySQL/MongoDB/STH database, or HDFS user space, or CKAN organization, etc.

Such a segmentation can be perfectly done by a single sink. Nevertheless, what happens if we want, for instance, to enhance the performance by adding a sink per service? Or if we want to decide applying Name Mappings in a per service way, too?

Next sections explain how to achieve that by implementing advanced configurations.

[Top](#top)

####<a name="section2.1.1"></a>Multiple agents
The first (and obvious) solution is to instantiate multiple Cygnus agents (multiple JVM processes, each one listening on a dedicated TCP port), one per FIWARE service. This solution ensures each FIWARE service has specific Name Mappings, dedicated sink (or sinks), etc.

![](img)

For each agent an `agent_<id>.conf` configuration file is required:

```
$ cat /usr/cygnus/conf/agent_<id>.conf
```

[Multi-instance](ref) support is available form the very begining of Cygnus, through the `cygnus_instance_<id>.conf` configuration files used when running Cygnus [as a service](ref).

```
$ cat /usr/cygnus/conf/cygnus_instance_<id>.conf
```

Alternatively, multiple instances of Cygnus can be run as single processes as well by running this command as many times as needed:

```
$ command...
```

Use this kind of advaned configuration if you have no restrictions in terms of hardware (please observe each JVM will require its own resources in terms of CPU and memory) and exposed TCP ports.

[Top](#top)

####<a name="section2.1.2"></a>Single agent, multiple sources
A variation regarding the previus one is to have a single agent (single JVM process) and multiple Http sources (one per FIWARE service).

![](img)

Such an architecture is achieved by using the following agent configuration:

```
$ cat /usr/cygnus/conf/agent_1.conf
```

Use this kind of advanced configuraiton if you hace restrictions in terms of hardware, but you can still expose several TCP ports.

[Top](#top)

####<a name="section2.1.3"></a>Single agent, single source, multiplexing per FIWARE service
Finally, insted of multiplexing the notifications per TCP port (i.e. one port per FIWARE service), you can enable the reception of all kind of notifications through a single TCP port and perform such a multiplexing internally to a single Cygnus agent.

![](img)

I.e. using this agent configuration:

```
$ cat /usr/cygnus/conf/agent_1.conf
```

Use this kind of advanced configuration in the most restrictive scenarios, where both hardware and exposed ports are very limited and/or highly valuable.

[Top](#top)

##<a name="section3"></a>Using interceptors
Interceptors are components of the Flume agent architecture. Typically, such an agent is based on a source dealing with the input, a sink dealing with the output and a channel communicating them. The source processes the input, producing Flume events (an object based on a set of headers and a byte-based body) that are put in the channel; then the sink consumes the events by getting them from the channel. This basic architecture may be enriched by the addition of Interceptors, a chained sequence of Flume events preprocessors that <i>intercept</i> the events before they are put into the channel and performing one of these operations:

* Drop the event.
* Modify an existent header of the Flume event.
* Add a new header to the Flume event.

Interceptors should never modify the body part. Once an event is preprocessed, it is put in the channel as usual.

As can be seen, this mechanism allows for very useful ways of enriching the basic Flume events a certain Flume source may generate. Let's see how Cygnus makes use of this concept in order to add certain information to the Flume events created from the Orion notifications.

[Top](#top)
