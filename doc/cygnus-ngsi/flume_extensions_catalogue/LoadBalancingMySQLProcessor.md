
# LoadBalancingMySQLProcessor

This sink proccessor can be used to provide load balancing capabilities over multiple `MySQLSinks` inside of a group, all of them configured to use the same database connection.

`LoadBalancingMySQLProcessor` allows Cygnus to exploit all the benefits of connection pooling and concurrent sink processing.

|  | Standard Flume Load Balancer | NGSI MySQL Load Balancer with parent Sink |
|---|---|---|
|Sink types supported| All types| Only `MySQLSink` supported|
|Number of connection pools| 1 pool per sink and database | 1 pool per database.|
|Number of Database Connections | max one connection per pool. | Each pool limmited by parent's `maxPoolSize` parameter.|
|Number of threads running | 1 thread | 1 thread per Sink.|
|Failover support| Yes, failed sinks are asigned a cool down period.| N/A |
|Load Balance Between diferent databases| Yes, max 1 per sink. | No, All sinks share the same database. |

If parent sink isn't set, `LoadBalancingMySQLProcessor` works like a standard Flume balancer.

Properties:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
|sinks|yes|N/A|Space-separated list of sinks that are participating in the group.|
|processor.parentSink|no|N/A| ParentSink shares its connection pools with the rest of sinks in the group.</br>This makes `LoadBalancingMySQLProcessor` to run each balanced Sink in its own thread, and to create database connectios on demand at sink group level.</br> When setted, just `parentSink` database connection has to be configured. And it's `maxPoolSize` default value will be calculated for the given number of sinks.</br> The database connection parameters of the rest of sinks will be ignored.</br>If not set, `LoadBalancingMySQLProcessor` works like a standard Flume balancer.|
|processor.type|yes|N/A|`com.telefonica.iot.cygnus.processors.LoadBalancingMySQLProcessor`|
|processor.selector|no|ROUND_ROBIN| Selection method, `ROUND_ROBIN` or `RANDOM`|

Group connection example:

    cygnusagent.sinkgroups = sg
    cygnusagent.sinkgroups.sg.sinks = mysql-sink  mysql-sink2 mysql-sink3
    cygnusagent.sinkgroups.sg.parentSink = mysql-sink2
    cygnusagent.sinkgroups.sg.processor.type = com.telefonica.iot.cygnus.processors.LoadBalancingMySQLProcessor
    cygnusagent.sinkgroups.sg.processor.selector = random
    
```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐ 
│                                                                                         JVM │
│              ┌─────┐     ┌───────────────────┐          ┌────────────┐                      │
│        ┌─────│ ch1 │─────│ sink1             │     ┌────│ mysql-sink │──┐                   │
│        │     └─────┘     └───────────────────┘     │    └────────────┘  │                   │
│  ┌─────┐     ┌─────┐     ┌───────────────────┐     │    ┌────────────┐  │ ┌────────────────┐│
│  │ src │─────│ ch2 │─────│ MySQLLoadBalancer │─────┼────│ mysql-sink2│──┼─│ Connection pool││
│  └─────┘     └─────┘     └───────────────────┘     │    └────────────┘  │ └────────────────┘│
│        │       ...          ...                    │       ...          │                   │
│        │     ┌─────┐     ┌───────────────────┐     │    ┌────────────┐  │                   │
│        └─────│ ch3 │─────│ sink3             │     └────│ mysql-sink3│──┘                   │
│              └─────┘     └───────────────────┘          └────────────┘                      │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```
