
# LoadBalancingMySQLProcessor

This sink proccessor can be used to provide load balancing capabilities over multiple `MySQLSinks` inside of a group, all of them configured to use the same database connection.

This proccessor allows Cygnus to exploit all the benefits of connection pooling and concurrent sink processing.

|  | Standard Flume Load Balancer | NGSI MySQL Load Balancer |
|---|---|---|
|Sink types supported| All types| Only `MySQLSink` supported|
|Number of connection pools| 1 pool per sink and database | 1 pool per database.|
|Number of Database Connections | max one connection per pool. | Each pool limmited by `maxPoolSize` parameter.|
|Number of threads running | 1 thread | 1 thread per Sink.|
|Failover support| Yes, failed sinks are asigned a cool down period.| N/A |
|Load Balance Between diferent databases| Yes, max 1 per sink. | No, All sinks share the same database. |

Properties:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
|sinks|yes|N/A|Space-separated list of sinks that are participating in the group. </br> Last sink on the list will be used to configure pool's database connections. the sb connection parameters of the rest of sinks will be ignored. |
|processor.type|yes|N/A|`com.telefonica.iot.cygnus.processors.LoadBalancingMySQLProcessor`|
|processor.selector|no|ROUND_ROBIN| Selection method, `ROUND_ROBIN` or `RANDOM`

Group connection example:

    cygnusagent.sinkgroups = sg
    cygnusagent.sinkgroups.sg.sinks = mysql-sink  mysql-sink2 mysql-sink3 mysql-sink4
    cygnusagent.sinkgroups.sg.processor.type = com.telefonica.iot.cygnus.processors.LoadBalancingMySQLProcessor
    cygnusagent.sinkgroups.sg.processor.selector = random
