# <a name="top"></a>Cygnus reliability
Content:

* [Persistence retries](#section1)
* [Traffic absorption peaks](#section2)
* [File channels](#section3)
* [High Availability](#section4)

## <a name="section1"></a>Persistence retries
Cygnus for NGSI implements a retry mechanism for those persistence attempts that failed for any reason. It is based on the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |

I.e. a certain number of retries or time-to-live has to be configured for complete batches of NGSI events (a batch may contain from 1 to N NGSI events); additionally, the retry frequency must be configured, which is not constant and can be different for first retries, for instance.

Batches for future retries are saved in memory independently of the type of the channel used. This is because NGSI events within batches are not re-inserted in the channel but maintained aggregated as a batch for performance purposes (the batch has not to be aggregated again). This means context information candidate for retry can be lost if Cygnus shuts down or crashes for any reason. In any case, this should not worry Cygnus users, since not persisted events would be lost without a retry mechanism as Cygnus' one.

Dispatching policy regarding new incoming NGSI events and candidate for retries is currently hardcoded. It is based on always giving the priority to retries (if any), and then process new incoming events (if any). This could change in the future in the form of a configurable policy. In any case, the `batch_retry_intervals` parameter grants new incoming events are always processed in between candidates for retry, even if `batch_ttl` is set to `-1` (always retry).

[Top](#top)

## <a name="section2"></a>Traffic absorption peaks
Because Cygnus inherits from Apache Flume, Cygnus agents of any type inherit an internal architecture based on the usage of channels communicating sources and sinks. Apart for achieving this communication, channels, which can be seen as buffers for incoming data, work as a mechanism for traffic absorption peaks.

In the specific case of Cygnus for NGSI, this means the channel will be able to absorb peaks of notifications from a NGSI source (typically Orion Context Broker) while the configured sinks work hard in the persistence of data. Of course, timing regarding reception time and persistence time will be different, since sinks will work slower than sources (typical consuming-producer problem), but al least data will not be lost.

A Cygnus administrator must carefully study the expected throughput for the whole architecture containing Cygnus and prevent high loads of NGSI notifications by configuring a channel capacity enough to absorb it.

[Top](#top)

## <a name="section3"></a>File channels
File channels are another heritage from Apache Flume. This kind of channels are based on files, and unlike memory channels, information within this kind of channel lives between crashes.

Using file channels is interesting if your notification rate is slower than Cygnus recovery time. Then, nothing will be lost. If not, using file channels instead of a *volatile* channel will not imply any significant difference, since the amount of new incoming events lost while Cygnus is down could be much higher than the number of past events saved in files.

File channels are particularly relevant if your aim is to implement some kind of High Availability architecture (see next section).

As the reader may imagine, file-based channels are slower than memory-based ones, since file seeking is slow. Thus, you must find a trade-off between reliability and speed when deciding the most suitable channel type for your agent.

[Top](#top)

## <a name="section4"></a>High Availability
Cygnus does not implement any High Availability (HA) mechanism *per se*. Anyway, implementing HA for Cygnus is as easy as running two instances of Cygnus and putting a load balancer in between them and the NGSI source (or sources). Of course, the load balancer itself is a single point of failure; there are solutions for this, but they are out of scope of this document.

```
                         +----------+
                         |  Cygnus  |  
                   +-----| (active) |-----+
                   |     +----------+     |
+-------+     +----+                      +---------+
| Orion |-----| LB |                      | backend |       
+-------+     +----+                      +---------+
                   |     +----------+     |
                   +-----|  Cygnus  |-----+
                         | (passive)|
                         +----------+
```

An important aspect regarding HA is what happens with events within active Cygnus agent's channels at the moment of moving to the passive Cygnus agent (which automatically becomes the active one). If such events are within a memory-based channel, they are lost and nothing can be done. Nevertheless, if the channels are based on files, events can be recovered. In fact, they are not really recovered but directly used since both Cygnus agents, active and passive running in the same machine, can be configured for using the same data and checkpoint directories. Even in the case the active and passive Cygnuses run in different machines, which is the common HA configuration, a third machine can be hosting the data files; in this case, the data files can be accessed if a distributed or shared file system is configured for the three machines.

```
                         +----------+
                         |  Cygnus  |
                   +-----| (active) |-----+
                   |     +-----+----+     |
                   |           |          |
+-------+     +----+   +-------+------+   +---------+
| Orion |-----| LB |   | channel data |   | backend |       
+-------+     +----+   | & checkpoint |   +---------+
                   |   +-------+------+   |
                   |           |          |
                   |     +-----+----+     |
                   +-----|  Cygnus  |-----+
                         | (passive)|
                         +----------+
```

[Top](#top)
