#<a name="top"></a>Management interface
Content:

* [Apiary version of this document](#section1)
* [GET `/v1/version`](#section2)
* [GET `/v1/stats`](#section3)
* [PUT `/v1/stats`](#section4)
* [GET `/v1/groupingrules`](#section5)
* [POST `/v1/groupingrules`](#section6)
* [PUT `/v1/groupingrules`](#section7)
* [DELETE `/v1/groupingrules`](#section8)
* [GET `/admin/log`](#section9)
* [PUT `/admin/log`](#section10)
* [GET `/admin/configuration`](#section11)
  * [GET all parameters](#section11.1)
  * [GET a single parameter](#section11.2)
* [POST `/admin/configuration/agent`](#section12)
* [PUT `/admin/configuration/agent`](#section13)
* [DELETE `/admin/configuration/agent`](#section14)
* [GET `/admin/configuration/instance`](#section15)
  * [GET all parameters](#section15.1)
  * [GET a single parameter](#section15.2)
* [POST `/v1/subscriptions`](#section16)
  * [`NGSI Version 1`](#section16.1)
  * [`NGSI Version 2`](#section16.2)
* [DELETE `/v1/subscriptions`](#section17)
* [GET `/v1/subscriptions`](#section18)
  * [GET subscription by ID](#section18.1)
  * [GET all subscriptions](#section18.2)

##<a name="section1"></a>Apiary version of this document
This API specification can be checked at [Apiary](http://telefonicaid.github.io/fiware-cygnus/api/) as well.

[Top](#top)

##<a name="section2"></a>`GET /v1/version`
Gets the version of the running software, including the last Git commit:

```
GET http://<cygnus_host>:<management_port>/v1/version
```

Response:

```
{
    "success": "true",
    "version": "0.12.0_SNAPSHOT.52399574ea8503aa8038ad14850380d77529b550"
}
```

[Top](#top)

##<a name="section3"></a>`GET /v1/stats`
Gets statistics about the configured Flume components. It is important to note <b>in order to gathering statistics from the channels</b>, these must be of type `com.telefonica.iot.cygnus.channels.CygnusMemoryChannel` or `com.telefonica.iot.cygnus.channels.CygnusFileChannel`.

Regarding the sources, it returns:

* Name of the source as written in the configuration.
* Setup time of the source.
* Status of the source, i.e. started or stopped.
* Number of processed events, i.e. number of events received by the source and attempeted to be put in the channels.
* Number of finally events put in the channels.

Regarding the channels, it returns:

* Name of the channel as written in the configuration.
* Setup time of the channel.
* Status of the channel, i.e. started or stopped.
* Number of events currently at the channel.
* Number of successful puts.
* Number of failed puts.
* Number of sucessful takes.
* Number of failed takes.

Regarding the sinks, it returns:

* Name of the sink as written in the configuration.
* Setup time of the sink.
* Status of the sink, i.e. started or stopped.
* Number of processed events, i.e. number of events taken from the channel and attempted for persistence.
* Number of finally persisted events.

```
GET http://<cygnus_host>:<management_port>/v1/stats
```

Response:

```
{
    "stats": {
        "channels": [
            {
                "name": "mysql-channel",
                "num_events": 0,
                "num_puts_failed": 0,
                "num_puts_ok": 11858,
                "num_takes_failed": 1,
                "num_takes_ok": 11858,
                "setup_time": "2016-02-05T10:34:25.80Z",
                "status": "START"
            }
        ],
        "sinks": [
            {
                "name": "mysql-sink",
                "num_persisted_events": 11800,
                "num_processed_events": 11858,
                "setup_time": "2016-02-05T10:34:24.978Z",
                "status": "START"
            }
        ],
        "sources": [
            {
                "name": "http-source",
                "num_processed_events": 11858,
                "num_received_events": 11858,
                "setup_time": "2016-02-05T10:34:24.921Z",
                "status": "START"
            }
        ]
    },
    "success": "true"
}
```

[Top](#top)

##<a name="section4"></a>`PUT /v1/stats`
Resets the statistics about the configured Flume components. It is important to note <b>in order to reset statistics from the channels</b>, these must be of type `com.telefonica.iot.cygnus.channels.CygnusMemoryChannel` or `com.telefonica.iot.cygnus.channels.CygnusFileChannel`.

```
PUT http://<cygnus_host>:<management_port>/v1/stats
```

Response:

```
{"success":"true"}
```

[Top](#top)

##<a name="section5"></a>`GET /v1/groupingrules`
Gets the configured [grouping rules](../flume_extensions_catalogue/grouping_interceptor.md).

```
GET http://<cygnus_host>:<management_port>/v1/groupingrules
```

Response:

```
{
    "grouping_rules": [
        {
            "destination": "allcars",
            "fields": [
                "entityType"
            ],
            "fiware_service_path": "cars",
            "id": 1,
            "regex": "Car"
        },
        {
            "destination": "allrooms",
            "fields": [
                "entityType"
            ],
            "fiware_service_path": "rooms",
            "id": 2,
            "regex": "Room"
        }
    ],
    "success": "true"
}
```

[Top](#top)

##<a name="section6"></a>`POST /v1/groupingrules`
Adds a new rule, passed as a Json in the payload, to the [grouping rules](../flume_extensions_catalogue/grouping_interceptor.md).

```
POST http://<cygnus_host>:<management_port>/v1/groupingrules
{
	"regex": "Room",
	"destination": "allrooms",
	"fiware_service_path": "rooms",
	"fields": ["entityType"]
}
```

Response:

```
{"success":"true"}
```

Please observe the `id` field is not passed as part of the posted Json. This is because the Management Interface automatically deals with the proper ID insertion.

[Top](#top)

##<a name="section7"></a>`PUT /v1/groupingrules`
Updates an already existent [grouping rules](../flume_extensions_catalogue/grouping_interceptor.md), given its ID as a query parameter and passed the rule as a Json in the payload.

```
PUT http://<cygnus_host>:<management_port>/v1/groupingrules=id=2
{
	"regex": "Room",
	"destination": "otherrooms",
	"fiware_service_path": "rooms",
	"fields": ["entityType"]
}
```

Response:

```
{"success":"true"}
```

[Top](#top)

##<a name="section8"></a>`DELETE /v1/groupingrules`
Deletes a [grouping rules](../flume_extensions_catalogue/grouping_interceptor.md), given its ID a a query parameter.

```
DELETE http://<cygnus_host>:<management_port>/v1/groupingrules?id=2
```

Response:

```
{"success":"true"}
```

[Top](#top)

##<a name="section9"></a>`GET /admin/log`
Gets the log4j configuration (relevant parts, as the logging level or the appender names and layouts).

```
GET http://<cygnus_host>:<management_port>/admin/log
```

Responses:

```
200 OK
{
    "log4j": {
        "appenders": [
            {
                "layout": "...",
                "name": "..."
            }
        ],
        "level": "..."
    },
    "success": "true"
}
```

```
500 Internal Server Error
{
    "error": "..."
}
```

[Top](#top)

##<a name="section10"></a>`PUT /admin/log`
Updates the logging level of Cygnus, given the logging level as a query parameter.

Valid logging levels are `DEBUG`, `INFO`, `WARNING` (`WARN` also works), `ERROR` and `FATAL`.

```
PUT http://<cygnus_host>:<management_port>/admin/log?level=<log_level>
```

Responses:

```
200 OK
```

```
400 Bad Request
{"error":"Invalid log level"}
```

```
400 Bad Request
{"error":"}
```

[Top](#top)

##<a name="section11"></a>`GET /admin/configuration/agent`
###<a name="section11.1"></a>`GET` all parameters

Gets all the parameters from an agent given the path to the configuration file as the URI within the URL. The name of the agent must start with `agent_`.

```
GET "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf"
```

NOTE: Using the `/v1/admin/configuration/agent` path behaves the same way.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf"
```

Responses:

Valid path to the agent configuration file:
```
{"success":"true","result" : {
{"agent":{"cygnusagent.sinks.mysql-sink.mysql_port":"3306","channels.mysql-channel.transactionCapacity":"100","cygnusagent.sources.http-source.interceptors.gi.type":"com.interceptors.NGSIGroupingInterceptor$Builder","cygnusagent.sources.http-source.handler.default_service":"def_service","cygnusagent.sources.http-source.interceptors":"ts gi","cygnusagent.sinks.mysql-sink.mysql_host":"localhost","cygnusagent.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnusagent.sources.http-source.handler.default_service_path":"\/def_servpath","cygnusagent.sources.http-source.handler.notification_target":"\/notify","cygnusagent.sinks.mysql-sink.enable_grouping":"false","cygnusagent.sinks.mysql-sink.mysql_password":"root","cygnusagent.sources":"http-source","cygnusagent.channels.mysql-channel.capacity":"1000","cygnusagent.sinks.mysql-sink.type":"com.NGSIMySQLSink","cygnusagent.sources.http-source.interceptors.ts.type":"timestamp","cygnusagent.sinks.mysql-sink.batch_timeout":"10","cygnusagent.sources.http-source.port":"5050","cygnusagent.sinks.mysql-sink.batch_size":"1","cygnusagent.sinks.mysql-sink.mysql_username":"root","cygnusagent.sinks.mysql-sink.channel":"mysql-channel","cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules_2.conf","cygnusagent.channels.mysql-channel.type":"memory","cygnusagent.sources.http-source.channels":"mysql-channel","cygnusagent.sources.http-source.handler.events_ttl":"2","cygnusagent.sources.http-source.handler":"com.handlers.NGSIRestHandler","cygnusagent.sinks.mysql-sink.data_model":"dm-by-service-path","cygnusagent.sinks":"mysql-sink","cygnusagent.sinks.mysql-sink.attr_persistence":"row","cygnusagent.channels":"mysql-channel"}}}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : { "File not found in the path received" }
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

###<a name="section11.2"></a>`GET` a single parameter

Gets a single parameter from an agent given the path to the configuration file as the URI within the URL and the name of the parameter as a query parameter. The name of the agent must start with `agent_`.

```
GET "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf&param=<param_name>"
```

NOTE: Using the `/v1/admin/configuration/agent` path behaves the same way.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf&param=<param_name>"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"cygnusagent.sinks.mysql-sink.data_model":"dm-by-attribute"}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : {"File not found in the path received"}
```

Parameter not found in the agent configuration file:

```
{"success":"false","result" : {"Param 'cygnusagent.sinks.mysql-sink.new_parameter' not found in the agent"}
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

##<a name="section12"></a>`POST /admin/configuration/agent`

Posts a single parameter if it doesn't exist in the agent given the path to the configuration file as the URI within the URL and the name and the value of the parameter as a query parameters. The name of the agent must start with `agent_`.

```
POST "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnusagent.sinks.mysql-sink.my_new_param&value=my_new_value"
```

NOTE: Using the `/v1/admin/configuration/agent` path behaves the same way.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnusagent.sinks.mysql-sink.my_new_param&value=my_new_value"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"agent":{"cygnusagent.sinks.mysql-sink.mysql_port":"3306","cygnusagent.channels.mysql-channel.transactionCapacity":"100","cygnusagent.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnusagent.sources.http-source.handler.default_service":"def_service","cygnusagent.sources.http-source.interceptors":"ts gi","cygnusagent.sinks.mysql-sink.mysql_host":"localhost","cygnusagent.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnusagent.sources.http-source.handler.default_service_path":"\/def_servpath","cygnusagent.sources.http-source.handler.notification_target":"\/notify","cygnusagent.sinks.mysql-sink.enable_grouping":"true","cygnusagent.sinks.mysql-sink.mysql_password":"","cygnusagent.channels.aux-channel.capacity":"1000","cygnusagent.sources":"http-source","cygnusagent.channels.mysql-channel.capacity":"1000","cygnusagent.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnusagent.sources.http-source.interceptors.ts.type":"timestamp","cygnusagent.channels.aux-channel.type":"memory","cygnusagent.sinks.mysql-sink.batch_timeout":"10","cygnusagent.sources.http-source.port":"5050","cygnusagent.sinks.mysql-sink.batch_size":"1","cygnusagent.sinks.mysql-sink.mysql_username":"root","cygnusagent.sinks.mysql-sink.channel":"mysql-channel","cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnusagent.channels.mysql-channel.type":"memory","cygnusagent.channels.aux-channel.transactionCapacity":"100","cygnusagent.sources.http-source.channels":"mysql-channel","cygnusagent.sources.http-source.handler.events_ttl":"2","cygnusagent.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnusagent.sinks.mysql-sink.data_model":"dm-by-attribute","cygnusagent.sinks.mysql-sink.my_new_param":"my_new_value","cygnusagent.sinks":"mysql-sink","cygnusagent.sinks.mysql-sink.attr_persistence":"row","cygnusagent.channels":"mysql-channel aux-channel"}}}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : { "File not found in the path received" }
```

Existing value in the agent configuration file:

```
{"success":"false","result" : {"agent":{"cygnusagent.sinks.mysql-sink.mysql_port":"3306","cygnusagent.channels.mysql-channel.transactionCapacity":"100","cygnusagent.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnusagent.sources.http-source.handler.default_service":"def_service","cygnusagent.sources.http-source.interceptors":"ts gi","cygnusagent.sinks.mysql-sink.mysql_host":"localhost","cygnusagent.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnusagent.sources.http-source.handler.default_service_path":"\/def_servpath","cygnusagent.sources.http-source.handler.notification_target":"\/notify","cygnusagent.sinks.mysql-sink.enable_grouping":"true","cygnusagent.sinks.mysql-sink.mysql_password":"","cygnusagent.channels.aux-channel.capacity":"1000","cygnusagent.sources":"http-source","cygnusagent.channels.mysql-channel.capacity":"1000","cygnusagent.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnusagent.sources.http-source.interceptors.ts.type":"timestamp","cygnusagent.channels.aux-channel.type":"memory","cygnusagent.sinks.mysql-sink.batch_timeout":"10","cygnusagent.sources.http-source.port":"5050","cygnusagent.sinks.mysql-sink.mysql_username":"root","cygnusagent.sinks.mysql-sink.batch_size":"1","cygnusagent.sinks.mysql-sink.channel":"mysql-channel","cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnusagent.channels.mysql-channel.type":"memory","cygnusagent.channels.aux-channel.transactionCapacity":"100","cygnusagent.sources.http-source.channels":"mysql-channel","cygnusagent.sources.http-source.handler.events_ttl":"2","cygnusagent.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnusagent.sinks.mysql-sink.data_model":"dm-by-attribute","cygnusagent.sinks.mysql-sink.my_new_param":"my_new_value","cygnusagent.sinks":"mysql-sink","cygnusagent.sinks.mysql-sink.attr_persistence":"row","cygnusagent.channels":"mysql-channel aux-channel"}}}
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

##<a name="section13"></a>`PUT /admin/configuration/agent`

Puts a single parameter if it doesn't exist or update it if already exists in the agent given the path to the configuration file as the URI within the URL and the name and the value of the parameter as a query parameters. The name of the agent must start with `agent_`.

```
PUT "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnusagent.sinks.mysql-sink.my_new_param&value=my_new_value"
```

NOTE: Using the `/v1/admin/configuration/agent` path behaves the same way.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnusagent.sinks.mysql-sink.new_param&value=new_value"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"agent":{"cygnusagent.sinks.mysql-sink.mysql_port":"3306","cygnusagent.channels.mysql-channel.transactionCapacity":"100","cygnusagent.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnusagent.sources.http-source.handler.default_service":"def_service","cygnusagent.sources.http-source.interceptors":"ts gi","cygnusagent.sinks.mysql-sink.mysql_host":"localhost","cygnusagent.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnusagent.sources.http-source.handler.default_service_path":"\/def_servpath","cygnusagent.sources.http-source.handler.notification_target":"\/notify","cygnusagent.sinks.mysql-sink.enable_grouping":"true","cygnusagent.sinks.mysql-sink.mysql_password":"","cygnusagent.channels.aux_channel1.capacity":"1000","cygnusagent.sources":"http-source","cygnusagent.channels.mysql-channel.capacity":"1000","cygnusagent.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnusagent.sources.http-source.interceptors.ts.type":"timestamp","cygnusagent.channels.aux_channel1.type":"memory","cygnusagent.sinks.mysql-sink.batch_timeout":"10","cygnusagent.sources.http-source.port":"5050","cygnusagent.sinks.mysql-sink.mysql_username":"root","cygnusagent.sinks.mysql-sink.batch_size":"1","cygnusagent.sinks.mysql-sink.channel":"mysql-channel","cygnusagent.channels.aux_channel2.type":"memory","cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnusagent.channels.mysql-channel.type":"memory","cygnusagent.channels.aux_channel1.transactionCapacity":"100","cygnusagent.sources.http-source.channels":"mysql-channel","cygnusagent.sources.http-source.handler.events_ttl":"2","cygnusagent.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnusagent.channels.aux_channel2.transactionCapacity":"100","cygnusagent.channels.aux_channel2.capacity":"1000","cygnusagent.sinks.mysql-sink.data_model":"dm-by-attribute","cygnusagent.sinks":"mysql-sink","cygnusagent.sinks.mysql-sink.attr_persistence":"row","cygnusagent.channels":"mysql-channel aux_channel1 aux_channel2"}}}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : { "File not found in the path received" }
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

Below you can see the tested agent configuration file after the `PUT` method.

```
cygnusagent.sources = http-source
cygnusagent.sinks = mysql-sink
cygnusagent.channels = mysql-channel aux_channel1 aux_channel2

cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
cygnusagent.sources.http-source.handler.default_service = def_service
cygnusagent.sources.http-source.interceptors = ts gi
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnusagent.sources.http-source.handler.default_service_path = /def_servpath
cygnusagent.sources.http-source.handler.notification_target = /notify
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
cygnusagent.sources.http-source.port = 5050
cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = /cygnus/apache-flume-1.4.0-bin/conf/grouping_rules.conf
cygnusagent.sources.http-source.channels = mysql-channel
cygnusagent.sources.http-source.handler.events_ttl = 2
cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler

cygnusagent.channels.mysql-channel.transactionCapacity = 100
cygnusagent.channels.mysql-channel.capacity = 1000
cygnusagent.channels.mysql-channel.type = memory

cygnusagent.channels.aux_channel1.capacity = 1000
cygnusagent.channels.aux_channel1.type = memory
cygnusagent.channels.aux_channel1.transactionCapacity = 100

cygnusagent.channels.aux_channel2.type = memory
cygnusagent.channels.aux_channel2.transactionCapacity = 100
cygnusagent.channels.aux_channel2.capacity = 1000

cygnusagent.sinks.mysql-sink.mysql_port = 3306
cygnusagent.sinks.mysql-sink.mysql_host = localhost
cygnusagent.sinks.mysql-sink.enable_grouping = true
cygnusagent.sinks.mysql-sink.mysql_password =
cygnusagent.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMySQLSink
cygnusagent.sinks.mysql-sink.batch_timeout = 10
cygnusagent.sinks.mysql-sink.mysql_username = root
cygnusagent.sinks.mysql-sink.batch_size = 1
cygnusagent.sinks.mysql-sink.channel = mysql-channel
cygnusagent.sinks.mysql-sink.new_parameter = new_value
cygnusagent.sinks.mysql-sink.data_model = dm-by-attribute
cygnusagent.sinks.mysql-sink.attr_persistence = row
```

[Top](#top)

##<a name="section14"></a>`DELETE /admin/configuration/agent`

Deletes a single parameter if it exists in the agent given the path to the configuration file as the URI within the URL and the name of the parameter as a query parameter. The name of the agent must start with `agent_`.

```
DELETE "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnusagent.sinks.mysql-sink.new_parameter
```

There are a second option for `DELETE` a single parameter, using `/v1/admin/configuration/agent`. Both have the same behaviour, you are free to use either of them.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnusagent.sinks.mysql-sink.new_param"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"agent":{"cygnusagent.sinks.mysql-sink.mysql_port":"3306","cygnusagent.channels.mysql-channel.transactionCapacity":"100","cygnusagent.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnusagent.sources.http-source.handler.default_service":"def_service","cygnusagent.sources.http-source.interceptors":"ts gi","cygnusagent.sinks.mysql-sink.mysql_host":"localhost","cygnusagent.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnusagent.sources.http-source.handler.default_service_path":"\/def_servpath","cygnusagent.sources.http-source.handler.notification_target":"\/notify","cygnusagent.sinks.mysql-sink.enable_grouping":"true","cygnusagent.sinks.mysql-sink.mysql_password":"","cygnusagent.channels.aux_channel1.capacity":"1000","cygnusagent.sources":"http-source","cygnusagent.channels.mysql-channel.capacity":"1000","cygnusagent.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnusagent.sources.http-source.interceptors.ts.type":"timestamp","cygnusagent.channels.aux_channel1.type":"memory","cygnusagent.sinks.mysql-sink.batch_timeout":"10","cygnusagent.sources.http-source.port":"5050","cygnusagent.sinks.mysql-sink.mysql_username":"root","cygnusagent.sinks.mysql-sink.batch_size":"1","cygnusagent.sinks.mysql-sink.channel":"mysql-channel","cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnusagent.channels.mysql-channel.type":"memory","cygnusagent.channels.aux_channel1.transactionCapacity":"100","cygnusagent.sources.http-source.channels":"mysql-channel","cygnusagent.sources.http-source.handler.events_ttl":"2","cygnusagent.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnusagent.sinks.mysql-sink.data_model":"dm-by-attribute","cygnusagent.sinks":"mysql-sink","cygnusagent.sinks.mysql-sink.attr_persistence":"row","cygnusagent.channels":"mysql-channel aux_channel1"}}}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : { "File not found in the path received" }
```

Parameter not found in the agent configuration file:

```
{"success":"false","result" : {"agent":{"cygnusagent.sinks.mysql-sink.mysql_port":"3306","cygnusagent.channels.mysql-channel.transactionCapacity":"100","cygnusagent.sinks.mysql-sink.new_parameter":"new_value","cygnusagent.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnusagent.sources.http-source.handler.default_service":"def_service","cygnusagent.sources.http-source.interceptors":"ts gi","cygnusagent.sinks.mysql-sink.mysql_host":"localhost","cygnusagent.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnusagent.sources.http-source.handler.default_service_path":"\/def_servpath","cygnusagent.sources.http-source.handler.notification_target":"\/notify","cygnusagent.sinks.mysql-sink.enable_grouping":"true","cygnusagent.sinks.mysql-sink.mysql_password":"","cygnusagent.channels.aux_channel1.capacity":"1000","cygnusagent.sources":"http-source","cygnusagent.channels.mysql-channel.capacity":"1000","cygnusagent.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnusagent.sources.http-source.interceptors.ts.type":"timestamp","cygnusagent.channels.aux_channel1.type":"memory","cygnusagent.sinks.mysql-sink.batch_timeout":"10","cygnusagent.sources.http-source.port":"5050","cygnusagent.sinks.mysql-sink.batch_size":"1","cygnusagent.sinks.mysql-sink.mysql_username":"root","cygnusagent.sinks.mysql-sink.channel":"mysql-channel","cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnusagent.channels.mysql-channel.type":"memory","cygnusagent.channels.aux_channel1.transactionCapacity":"100","cygnusagent.sources.http-source.channels":"mysql-channel","cygnusagent.sources.http-source.handler.events_ttl":"2","cygnusagent.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnusagent.sinks.mysql-sink.data_model":"dm-by-attribute","cygnusagent.sinks":"mysql-sink","cygnusagent.sinks.mysql-sink.attr_persistence":"row","cygnusagent.channels":"mysql-channel aux_channel1"}}}
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

##<a name="section15"></a>`GET /admin/configuration/instance`
###<a name="section15.1"></a>`GET` all parameters

Gets all the parameters from an instance given the path to the configuration file as the URI within the URL. The path to the instance must be with `/usr/cygnus/conf`.

```
GET "http://<cygnus_host>:<management_port>/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf"
```

NOTE: Using the `/v1/admin/configuration/instance` path behaves the same way.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf"
```

Responses:

Valid path to the instance configuration file:
```
{"success":"true","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnusagent","ADMIN_PORT":"8081","CONFIG_FOLDER":"\/usr\/cygnus\/conf","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}
```

Invalid path to the instance configuration file:

```
{"success":"false","result" : {"Invalid path for a instance configuration file"}
```

Instance configuration file not found:

```
{"success":"false","result" : {"File not found in the path received"}
```

[Top](#top)

###<a name="section15.2"></a>`GET` a single parameter

Gets a single parameter from an instance given the path to the configuration file as the URI within the URL and the name of the parameter as a query parameter. The path to the instance must be with `/usr/cygnus/conf`.

```
GET "http://<cygnus_host>:<management_port>/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>"
```

NOTE: Using the `/v1/admin/configuration/instance` path behaves the same way.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>"
```

Responses:

Valid path to the instance configuration file:

```
{"success":"true","result" : {"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf"}
```

Invalid path to the instance configuration file:

```
{"success":"false","result" : {"Invalid path for a instance configuration file"}
```

Parameter not found in the instance configuration file:

```
{"success":"false","result" : {"Param 'CONFIG_FOLDER_FILE' not found in the instance"}
```

Instance configuration file not found:

```
{"success":"false","result" : {"File not found in the path received"}
```

[Top](#top)

##<a name="section16"></a>`POST /v1/subscriptions`
###<a name="section16.1"></a> `NGSI Version 1`

Creates a new subscription to Orion given the version of NGSI (`ngsi_version=1` in this case). The Json passed in the payload contains the Json subscription itself and Orion's endpoint details.

```
POST "http://<cygnus_host>:<management_port>/v1/subscriptions&ngsi_version=1"
{
    "subscription":{
          "entities": [
              {
                  "type": "Room",
                  "isPattern": "false",
                  "id": "Room1"
              }
          ],
          "attributes": [],
          "reference": "http://<reference_host>:<reference_port>",
          "duration": "P1M",
          "notifyConditions": [
              {
                  "type": "ONCHANGE",
                  "condValues": []
              }
          ],
          "throttling": "PT5S"
    },
    "endpoint":{
          "host":"<endpoint_host>",
          "port":"<endpoint_port>",
          "ssl":"false",
          "xauthtoken":"234123123123123"
    }
}
```

Responses:

Valid subscription:

```
{"success":"true","result" : {{"subscribeResponse":{"duration":"P1M","throttling":"PT5S","subscriptionId":"56f9081c3c6fb7e9d2a912a0"}}}}
```

Invalid subscription (Unknown fields in this case):

```
{"success":"true","result" :{"subscribeError":{"errorCode":{"code":"400","reasonPhrase":"Bad Request","details":"JSON Parse Error: unknown field: \/extraField"}}}
```

Invalid JSON (Empty field and missing field):

```
{"success":"false","error":"Invalid subscription, field 'duration' is empty"}

{"success":"false","error":"Invalid subscription, field 'notifyConditions' is missing"
```

Please observe Cygnus checks if the Json passed in the payload is valid (syntactically and semantically).

[Top](#top)

###<a name="section16.2"></a> `NGSI Version 2`

Creates a new subscription to Orion given the version of NGSI (`ngsi_version=2` in this case). The Json passed in the payload contains the Json subscription itself and Orion's endpoint details.

```
POST "http://<cygnus_host>:<management_port>/v1/subscriptions&ngsi_version=2"
{
    "subscription":{
        "description": "One subscription to rule them all",
        "subject": {
            "entities": [
                {
                    "idPattern": ".*",
                    "type": "Room"
                }
            ],
            "condition": {
                "attrs": [
                    "temperature"
                ],
                "expression": {
                      "q": "temperature>40"
                }
            }
        },
        "notification": {
            "http": {
                "url": "http://localhost:1234"
            },
            "attrs": [
                "temperature",
                "humidity"
            ]
        },
        "expires": "2016-05-05T14:00:00.00Z",
        "throttling": 5
    },
    "endpoint":{
        "host":"<endpoint_host>",
        "port":"<endpoint_port>",
        "ssl":"false",
        "xauthtoken":"QsENv67AJj7blC2qJ0YvfSc5hMWYrs"
    }
}
```

Responses:

Valid subscription:

```
{"success":"true","result" : {"SubscriptionID" : "572ae23d20e1387832ed98d0"}}
```

Invalid subscription (Unknown fields in this case):

```
{"success":"false","error":"Parse error, malformed Json. Check it for errors."}
```

Invalid JSON (e.g. Missing fields or invalid endpoint):

```
{"success":"false","error":"Invalid subscription, field 'xxxxxx' is missing"}

{"success":"false","error":"Missing endpoint"}
```

Please observe Cygnus checks if the Json passed in the payload is valid (syntactically and semantically).

[Top](#top)

##<a name="section17"></a>`DELETE /v1/subscriptions`

Deletes a subscription made to Orion given its ID and the NGSI version. The Json passed in the payload contains the Orion's endpoint details.

```
DELETE "http://<cygnus_host>:<management_port>/v1/subscriptions?subscription_id=<subscriptionId>&ngsi_version=<ngsiVersion>"
```

Responses:

Subscriptions deleted in v1 and v2:

```
{"success":"true","result" : {" Subscription deleted "}

```

Wrong parameter:

```
{"success":"false","error":"Parse error, wrong parameter (subscription_id). Check it for errors."}

{"success":"false","error":"Parse error, wrong parameter (ngsi_version). Check it for errors."}
```

Wrong subscription ID:

```
[NGSI v1]

{"success":"false","result" : {{"subscriptionId":"571872a9c0585c7451571be4","statusCode":{"code":"404","reasonPhrase":"No context element found","details":"subscriptionId: \/571872a9c0585c7451571be4\/"}}}

[NGSI v2]

{"success":"false","result" : {{"description":"The requested subscription has not been found. Check id","error":"NotFound"}}
```

Empty or missing authentication token:

```
{"success":"false","error":"Empty Auth-Token. Required for DELETE subscriptions"}

{"success":"false","error":"Missing Auth-Token. Required for DELETE subscriptions"}
```

Missing fields (empty or not given):

```
{"success":"false","error":"Missing endpoint"}
{"success":"false","error":"Invalid endpoint, field 'host' is missing"}
{"success":"false","error":"Invalid endpoint, field 'ssl is empty"}
```

[Top](#top)

##<a name="section18"></a>`GET /v1/subscriptions`
###<a name="section18.1"></a> GET subscription by ID

Gets an existent subscription from Orion, given the NGSI version and the subscription id as a query parameter.

Valid NGSI versions are `1` and `2` (this method only works with `ngsi_version=2` due to this method is not implemented in version `1`).

```
GET "http://<cygnus_host>:<management_port>/v1/subscriptions?ngsi_version=<ngsiVersion>&subscription_id=<subscriptionId>" -d '{"host":"<host>", "port":"<port>", "ssl":"false", "xauthtoken":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}'
```

Responses:

Valid and current subscription id with the correct NGSI version:

```
{"success":"true","result" : {{"notification":{"lastNotification":"2015-07-28T22:24:33.00Z","timesSent":8122767998226748692,"callback":"http:\/\/192.168.64.111\/ngsi23\/notifyContext","attributes":[]},"expires":"2016-09-25T08:17:47.00Z","subject":{"condition":{"expression":{"q":"","geometry":"","coords":"","georel":""},"attributes":["TimeInstant"]},"entities":[{"id":"","type":"sevilla:fountain","idPattern":"patternId"}]},"id":"54325022q460a3873d30oe95","status":"active"}}
```

Valid but inexistent subscription id with the correct NGSI version:

```
{"success":"false","result" : {{"description":"","error":"subscriptionId does not correspond to an active subscription"}}
```

Invalid NGSI version:

```
{"success":"false","error":"Parse error, invalid parameter (ngsi_version): Must be 1 or 2. Check it for errors."}
```

Valid but not implemented NGSI version (sending `ngsi_version=1`):

```
{"success":"false","error":"GET /v1/subscriptions not implemented."}
```

Missing or empty parameters:

```
{"success":"false","error":"Parse error, missing parameter (subscription_id). Check it for errors."}
{"success":"false","error":"Parse error, empty parameter (ngsi_version). Check it for errors."}
```

[Top](#top)

###<a name="section18.2"></a> GET all subscriptions

Gets all existent subscriptions from Orion, given the NGSI version as a query parameter.

Valid NGSI versions are `1` and `2` (this method only works with `ngsi_version=2` due to this method is not implemented in version `1`).

```
GET "http://<cygnus_host>:<management_port>/v1/subscriptions?ngsi_version=<ngsiVersion>" -d '{"host":"<host>", "port":"<port>", "ssl":"false", "xauthtoken":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}'
```

Responses:

When a valid subscription ID and an accepted NGSI version is used:

```
{"success":"true","result" : {{"result":[{"notification":{"lastNotification":"2015-07-28T22:23:30.00Z","timesSent":7126568376946155044,"http":{"url":"http:\/\/192.168.83.112:1026\/ngsi23\/notifyContext"},"attrs":[]},"expires":"2016-09-13T09:27:15.00Z","subject":{"condition":{"attrs":["temperature"]},"entities":[{"id":"","type":"city:lights","idPattern":"City:LUCES:"}]},"id":"54228e7318sddf233as323sd","status":"active"},{"notification":{"lastNotification":"2015-07-28T22:24:33.00Z","timesSent":8122767998226748692,"http":{"url":"http:\/\/192.168.83.112:1026\/ngsi21\/notifyContext"},"attrs":[]},"expires":"2016-09-25T08:17:47.00Z","subject":{"condition":{"attrs":["noise"]},"entities":[{"id":"","type":"city:fountain","idPattern":"City:LUCES"}]}}]}}}

```

When an invalid NGSI version is used:

```
{"success":"false","error":"Parse error, invalid parameter (ngsi_version): Must be 1 or 2. Check it for errors."}
```

When a valid but not implemented NGSI version (sending `ngsi_version=1`):

```
{"success":"false","error":"GET /v1/subscriptions not implemented for NGSI version 1."}
```

When there are missing or empty parameters:

```
{"success":"false","error":"Invalid endpoint, field 'ssl' is missing"}
```

[Top](#top)
