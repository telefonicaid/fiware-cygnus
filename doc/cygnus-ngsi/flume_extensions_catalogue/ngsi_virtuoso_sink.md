# <a name="top"></a>NGSIVirtuosoSink
Content:

* [Functionality](#section1)
* [Administration guide](#section2)
    * [Configuration](#section2.1)


## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIVirtuosoSink`, or simply `NGSIVirtuosoSink` is a sink designed to persist NGSI-like context data events within a [Virtuoso server](https://virtuoso.openlinksw.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific Virtuoso data structures (like RDFs).

Next sections will explain this in detail.

[Top](#top)


## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIVirtuosoSink` is configured through the following parameters:


| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIVirtuosoSink</i> |
| channel | yes | N/A ||
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. |
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. |
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| last\_data\_mode | no | upsert | <i>upsert</i>, to set last data mode. Check this [link](./last_data_function.md) for more details. |
| last\_data\_table\_suffix | no | false | This suffix will be added to the table name in order to know where Cygnus will store the last record of an entity. Check this [link](./last_data_function.md) for more details. |
| last\_data\_unique\_key | no | entityId | This must be a unique key on the database to find when a previous record exists. Check this [link](./last_data_function.md) for more details. |
| last\_data\_timestamp\_key | no | recvTime | This must be a timestamp key on the aggregation to know which record is older. Check this [link](./last_data_function.md) for more details. |
| last\_data\_sql_timestamp\_format | no | YYYY-MM-DD HH24:MI:SS.MS | This must be a timestamp format to cast [SQL Text to timestamp](https://www.postgresql.org/docs/9.1/functions-formatting.html). Check this [link](./last_data_function.md) for more details. |
| data\_model | no | dm-by-entity | <i>dm-by-service-path</i> or <i>dm-by-entity</i> or <i>dm-by-entity-type</i> or <i>dm-by-entity-database</i> or <i>dm-by-entity-database-schema</i> or <i>dm-by-entity-type-database</i> or <i>dm-by-entity-type-database-schema</i>. <i>dm-by-service</i> and <dm-by-attribute</i> are not currently supported. |
| virtuoso\_host | no | localhost | FQDN/IP address where the Virtuoso server runs. |
| virtuoso\_port | no | 1111 ||
| virtuoso\_database | no | vdb | `vdb` is the default database that is created automatically when install. 
| virtuoso\_username | no | dba | `dba` is the default username that is created automatically when install |
| virtuoso\_password | no | N/A | Empty value by default (No password is created when install) |
| virtuoso\_maxPoolSize | no | 3 | Max number of connections per database pool |
| virtuoso\_maxPoolIdle | no | 2 | Max number of idle connections per database pool |
| virtuoso\_minPoolIdle | no | 0 | Min number of idle connections per database pool |
| virtuoso\_minPoolIdleTimeMillis | no | 10000 | minimum amount of time an idle connection before is eligible for eviction |
| virtuoso\_options | no | N/A | optional connection parameter(s) concatinated to jdbc url if necessary<br/> |
| attr\_persistence | no | row | <i>row</i> or <i>column</i>. |
| attr\_metadata\_store | no | false | <i>true</i> or <i>false</i>. |
| attr\_native\_types | no | false | if the attribute value will be native <i>true</i> or stringfy or <i>false</i>. |
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| backend.enable\_cache | no | false | <i>true</i> or <i>false</i>, <i>true</i> enables the creation of a Cache, <i>false</i> disables the creation of a Cache. |
| persist\_errors | no | true | if there is an exception when trying to persist data into storage then error is persisted into a table |


A configuration example could be:

    cygnus-ngsi.sinks = mysql-sink
    cygnus-ngsi.channels = mysql-channel
    ...
    cygnus-ngsi.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMySQLSink
    cygnus-ngsi.sinks.mysql-sink.channel = mysql-channel
    
    
    
    cygnus-ngsi.sinks.virtuoso-sink.enable_grouping = false
    cygnus-ngsi.sinks.virtuoso-sink.enable_name_mappings = true
    cygnus-ngsi.sinks.virtuoso-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.virtuoso-sink.virtuoso_host = 172.17.0.1
    cygnus-ngsi.sinks.virtuoso-sink.virtuoso_port = 1111
    cygnus-ngsi.sinks.virtuoso-sink.virtuoso_username = dba
    cygnus-ngsi.sinks.virtuoso-sink.virtuoso_password = dba
    cygnus-ngsi.sinks.virtuoso-sink.virtuoso_database = vdb
    cygnus-ngsi.sinks.virtuoso-sink.attr_persistence = column
    cygnus-ngsi.sinks.virtuoso-sink.attr_native_types = true
    cygnus-ngsi.sinks.virtuoso-sink.attr_metadata_store = false
    cygnus-ngsi.sinks.virtuoso-sink.batch_size = 2
    cygnus-ngsi.sinks.virtuoso-sink.batch_timeout = 10
    cygnus-ngsi.sinks.virtuoso-sink.batch_ttl = -1
    cygnus-ngsi.sinks.virtuoso-sink.last_data_mode = upsert
    cygnus-ngsi.sinks.virtuoso-sink.last_data_table_suffix = _lastdata
    cygnus-ngsi.sinks.virtuoso-sink.last_data_unique_key = entityId
    cygnus-ngsi.sinks.virtuoso-sink.last_data_timestamp_key = recvTime
    cygnus-ngsi.sinks.virtuoso-sink.last_data_sql_timestamp_format = YYYY-MM-DD HH24:MI:SS.MS


[Top](#top)
