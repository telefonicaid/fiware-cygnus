# <a name="top"></a>Management interface: Original API
Content:

* [Apiary version of this document](#section1)
* [GET `/admin/log`](#section2)
* [PUT `/admin/log`](#section3)
* [GET `/admin/configuration`](#section4)
  * [GET all parameters](#section4.1)
  * [GET a single parameter](#section4.2)
* [POST `/admin/configuration/agent`](#section5)
* [PUT `/admin/configuration/agent`](#section6)
* [DELETE `/admin/configuration/agent`](#section7)
* [GET `/admin/configuration/instance`](#section8)
  * [GET all parameters](#section8.1)
  * [GET a single parameter](#section8.2)
* [POST `/admin/configuration/instance`](#section9)
* [PUT `/admin/configuration/instance`](#section10)
* [DELETE `/admin/configuration/instance`](#section11)

## <a name="section1"></a>Apiary version of this document
This API specification can be checked at [Apiary](http://telefonicaid.github.io/fiware-cygnus/api/latest) as well.

See also [v1 API (beta)](./management_interface_v1.md)

[Top](#top)

## <a name="section2"></a>`GET /admin/log`
Gets the logging level of Cygnus.

```
GET http://<cygnus_host>:<management_port>/admin/log
```

Responses:

```
200 OK
{"level": "...."}
```

```
500 Internal Server Error
{
    "error": "..."
}
```

[Top](#top)

## <a name="section3"></a>`PUT /admin/log`
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

## <a name="section4"></a>`GET /admin/configuration/agent`
### <a name="section4.1"></a>`GET` all parameters

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
{"agent":{"cygnus-common.sinks.mysql-sink.mysql_port":"3306","channels.mysql-channel.transactionCapacity":"100","cygnus-common.sources.http-source.interceptors.gi.type":"com.interceptors.NGSIGroupingInterceptor$Builder","cygnus-common.sources.http-source.handler.default_service":"def_service","cygnus-common.sources.http-source.interceptors":"ts gi","cygnus-common.sinks.mysql-sink.mysql_host":"localhost","cygnus-common.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnus-common.sources.http-source.handler.default_service_path":"\/def_servpath","cygnus-common.sources.http-source.handler.notification_target":"\/notify","cygnus-common.sinks.mysql-sink.enable_grouping":"false","cygnus-common.sinks.mysql-sink.mysql_password":"root","cygnus-common.sources":"http-source","cygnus-common.channels.mysql-channel.capacity":"1000","cygnus-common.sinks.mysql-sink.type":"com.NGSIMySQLSink","cygnus-common.sources.http-source.interceptors.ts.type":"timestamp","cygnus-common.sinks.mysql-sink.batch_timeout":"10","cygnus-common.sources.http-source.port":"5050","cygnus-common.sinks.mysql-sink.batch_size":"1","cygnus-common.sinks.mysql-sink.mysql_username":"root","cygnus-common.sinks.mysql-sink.channel":"mysql-channel","cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules_2.conf","cygnus-common.channels.mysql-channel.type":"memory","cygnus-common.sources.http-source.channels":"mysql-channel","cygnus-common.sources.http-source.handler.events_ttl":"2","cygnus-common.sources.http-source.handler":"com.handlers.NGSIRestHandler","cygnus-common.sinks.mysql-sink.data_model":"dm-by-service-path","cygnus-common.sinks":"mysql-sink","cygnus-common.sinks.mysql-sink.attr_persistence":"row","cygnus-common.channels":"mysql-channel"}}}
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

### <a name="section4.2"></a>`GET` a single parameter

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
{"success":"true","result" : {"cygnus-common.sinks.mysql-sink.data_model":"dm-by-attribute"}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : {"File not found in the path received"}
```

Parameter not found in the agent configuration file:

```
{"success":"false","result" : {"Param 'cygnus-common.sinks.mysql-sink.new_parameter' not found in the agent"}
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

## <a name="section5"></a>`POST /admin/configuration/agent`

Posts a single parameter if it doesn't exist in the agent given the path to the configuration file as the URI within the URL and the name and the value of the parameter as a query parameters. The name of the agent must start with `agent_`.

```
POST "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnus-common.sinks.mysql-sink.my_new_param&value=my_new_value"
```

NOTE: Using the `/v1/admin/configuration/agent` path behaves the same way.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnus-common.sinks.mysql-sink.my_new_param&value=my_new_value"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"agent":{"cygnus-common.sinks.mysql-sink.mysql_port":"3306","cygnus-common.channels.mysql-channel.transactionCapacity":"100","cygnus-common.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnus-common.sources.http-source.handler.default_service":"def_service","cygnus-common.sources.http-source.interceptors":"ts gi","cygnus-common.sinks.mysql-sink.mysql_host":"localhost","cygnus-common.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnus-common.sources.http-source.handler.default_service_path":"\/def_servpath","cygnus-common.sources.http-source.handler.notification_target":"\/notify","cygnus-common.sinks.mysql-sink.enable_grouping":"true","cygnus-common.sinks.mysql-sink.mysql_password":"","cygnus-common.channels.aux-channel.capacity":"1000","cygnus-common.sources":"http-source","cygnus-common.channels.mysql-channel.capacity":"1000","cygnus-common.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnus-common.sources.http-source.interceptors.ts.type":"timestamp","cygnus-common.channels.aux-channel.type":"memory","cygnus-common.sinks.mysql-sink.batch_timeout":"10","cygnus-common.sources.http-source.port":"5050","cygnus-common.sinks.mysql-sink.batch_size":"1","cygnus-common.sinks.mysql-sink.mysql_username":"root","cygnus-common.sinks.mysql-sink.channel":"mysql-channel","cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnus-common.channels.mysql-channel.type":"memory","cygnus-common.channels.aux-channel.transactionCapacity":"100","cygnus-common.sources.http-source.channels":"mysql-channel","cygnus-common.sources.http-source.handler.events_ttl":"2","cygnus-common.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnus-common.sinks.mysql-sink.data_model":"dm-by-attribute","cygnus-common.sinks.mysql-sink.my_new_param":"my_new_value","cygnus-common.sinks":"mysql-sink","cygnus-common.sinks.mysql-sink.attr_persistence":"row","cygnus-common.channels":"mysql-channel aux-channel"}}}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : { "File not found in the path received" }
```

Existing value in the agent configuration file:

```
{"success":"false","result" : {"agent":{"cygnus-common.sinks.mysql-sink.mysql_port":"3306","cygnus-common.channels.mysql-channel.transactionCapacity":"100","cygnus-common.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnus-common.sources.http-source.handler.default_service":"def_service","cygnus-common.sources.http-source.interceptors":"ts gi","cygnus-common.sinks.mysql-sink.mysql_host":"localhost","cygnus-common.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnus-common.sources.http-source.handler.default_service_path":"\/def_servpath","cygnus-common.sources.http-source.handler.notification_target":"\/notify","cygnus-common.sinks.mysql-sink.enable_grouping":"true","cygnus-common.sinks.mysql-sink.mysql_password":"","cygnus-common.channels.aux-channel.capacity":"1000","cygnus-common.sources":"http-source","cygnus-common.channels.mysql-channel.capacity":"1000","cygnus-common.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnus-common.sources.http-source.interceptors.ts.type":"timestamp","cygnus-common.channels.aux-channel.type":"memory","cygnus-common.sinks.mysql-sink.batch_timeout":"10","cygnus-common.sources.http-source.port":"5050","cygnus-common.sinks.mysql-sink.mysql_username":"root","cygnus-common.sinks.mysql-sink.batch_size":"1","cygnus-common.sinks.mysql-sink.channel":"mysql-channel","cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnus-common.channels.mysql-channel.type":"memory","cygnus-common.channels.aux-channel.transactionCapacity":"100","cygnus-common.sources.http-source.channels":"mysql-channel","cygnus-common.sources.http-source.handler.events_ttl":"2","cygnus-common.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnus-common.sinks.mysql-sink.data_model":"dm-by-attribute","cygnus-common.sinks.mysql-sink.my_new_param":"my_new_value","cygnus-common.sinks":"mysql-sink","cygnus-common.sinks.mysql-sink.attr_persistence":"row","cygnus-common.channels":"mysql-channel aux-channel"}}}
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

## <a name="section6"></a>`PUT /admin/configuration/agent`

Puts a single parameter if it doesn't exist or update it if already exists in the agent given the path to the configuration file as the URI within the URL and the name and the value of the parameter as a query parameters. The name of the agent must start with `agent_`.

```
PUT "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnus-common.sinks.mysql-sink.my_new_param&value=my_new_value"
```

NOTE: Using the `/v1/admin/configuration/agent` path behaves the same way.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnus-common.sinks.mysql-sink.new_param&value=new_value"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"agent":{"cygnus-common.sinks.mysql-sink.mysql_port":"3306","cygnus-common.channels.mysql-channel.transactionCapacity":"100","cygnus-common.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnus-common.sources.http-source.handler.default_service":"def_service","cygnus-common.sources.http-source.interceptors":"ts gi","cygnus-common.sinks.mysql-sink.mysql_host":"localhost","cygnus-common.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnus-common.sources.http-source.handler.default_service_path":"\/def_servpath","cygnus-common.sources.http-source.handler.notification_target":"\/notify","cygnus-common.sinks.mysql-sink.enable_grouping":"true","cygnus-common.sinks.mysql-sink.mysql_password":"","cygnus-common.channels.aux_channel1.capacity":"1000","cygnus-common.sources":"http-source","cygnus-common.channels.mysql-channel.capacity":"1000","cygnus-common.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnus-common.sources.http-source.interceptors.ts.type":"timestamp","cygnus-common.channels.aux_channel1.type":"memory","cygnus-common.sinks.mysql-sink.batch_timeout":"10","cygnus-common.sources.http-source.port":"5050","cygnus-common.sinks.mysql-sink.mysql_username":"root","cygnus-common.sinks.mysql-sink.batch_size":"1","cygnus-common.sinks.mysql-sink.channel":"mysql-channel","cygnus-common.channels.aux_channel2.type":"memory","cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnus-common.channels.mysql-channel.type":"memory","cygnus-common.channels.aux_channel1.transactionCapacity":"100","cygnus-common.sources.http-source.channels":"mysql-channel","cygnus-common.sources.http-source.handler.events_ttl":"2","cygnus-common.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnus-common.channels.aux_channel2.transactionCapacity":"100","cygnus-common.channels.aux_channel2.capacity":"1000","cygnus-common.sinks.mysql-sink.data_model":"dm-by-attribute","cygnus-common.sinks":"mysql-sink","cygnus-common.sinks.mysql-sink.attr_persistence":"row","cygnus-common.channels":"mysql-channel aux_channel1 aux_channel2"}}}
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
cygnus-common.sources = http-source
cygnus-common.sinks = mysql-sink
cygnus-common.channels = mysql-channel aux_channel1 aux_channel2

cygnus-common.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
cygnus-common.sources.http-source.handler.default_service = def_service
cygnus-common.sources.http-source.interceptors = ts gi
cygnus-common.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnus-common.sources.http-source.handler.default_service_path = /def_servpath
cygnus-common.sources.http-source.handler.notification_target = /notify
cygnus-common.sources.http-source.interceptors.ts.type = timestamp
cygnus-common.sources.http-source.port = 5050
cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file = /cygnus/apache-flume-1.4.0-bin/conf/grouping_rules.conf
cygnus-common.sources.http-source.channels = mysql-channel
cygnus-common.sources.http-source.handler.events_ttl = 2
cygnus-common.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler

cygnus-common.channels.mysql-channel.transactionCapacity = 100
cygnus-common.channels.mysql-channel.capacity = 1000
cygnus-common.channels.mysql-channel.type = memory

cygnus-common.channels.aux_channel1.capacity = 1000
cygnus-common.channels.aux_channel1.type = memory
cygnus-common.channels.aux_channel1.transactionCapacity = 100

cygnus-common.channels.aux_channel2.type = memory
cygnus-common.channels.aux_channel2.transactionCapacity = 100
cygnus-common.channels.aux_channel2.capacity = 1000

cygnus-common.sinks.mysql-sink.mysql_port = 3306
cygnus-common.sinks.mysql-sink.mysql_host = localhost
cygnus-common.sinks.mysql-sink.enable_grouping = true
cygnus-common.sinks.mysql-sink.mysql_password =
cygnus-common.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.NGSIMySQLSink
cygnus-common.sinks.mysql-sink.batch_timeout = 10
cygnus-common.sinks.mysql-sink.mysql_username = root
cygnus-common.sinks.mysql-sink.batch_size = 1
cygnus-common.sinks.mysql-sink.channel = mysql-channel
cygnus-common.sinks.mysql-sink.new_parameter = new_value
cygnus-common.sinks.mysql-sink.data_model = dm-by-attribute
cygnus-common.sinks.mysql-sink.attr_persistence = row
```

[Top](#top)

## <a name="section7"></a>`DELETE /admin/configuration/agent`

Deletes a single parameter if it exists in the agent given the path to the configuration file as the URI within the URL and the name of the parameter as a query parameter. The name of the agent must start with `agent_`.

```
DELETE "http://<cygnus_host>:<management_port>/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnus-common.sinks.mysql-sink.new_parameter
```

There are a second option for `DELETE` a single parameter, using `/v1/admin/configuration/agent`. Both have the same behaviour, you are free to use either of them.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/configuration/agent/cygnus/apache-flume-1.4.0-bin/conf/agent_cygnus.conf?param=cygnus-common.sinks.mysql-sink.new_param"
```

Responses:

Valid path to the agent configuration file:

```
{"success":"true","result" : {"agent":{"cygnus-common.sinks.mysql-sink.mysql_port":"3306","cygnus-common.channels.mysql-channel.transactionCapacity":"100","cygnus-common.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnus-common.sources.http-source.handler.default_service":"def_service","cygnus-common.sources.http-source.interceptors":"ts gi","cygnus-common.sinks.mysql-sink.mysql_host":"localhost","cygnus-common.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnus-common.sources.http-source.handler.default_service_path":"\/def_servpath","cygnus-common.sources.http-source.handler.notification_target":"\/notify","cygnus-common.sinks.mysql-sink.enable_grouping":"true","cygnus-common.sinks.mysql-sink.mysql_password":"","cygnus-common.channels.aux_channel1.capacity":"1000","cygnus-common.sources":"http-source","cygnus-common.channels.mysql-channel.capacity":"1000","cygnus-common.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnus-common.sources.http-source.interceptors.ts.type":"timestamp","cygnus-common.channels.aux_channel1.type":"memory","cygnus-common.sinks.mysql-sink.batch_timeout":"10","cygnus-common.sources.http-source.port":"5050","cygnus-common.sinks.mysql-sink.mysql_username":"root","cygnus-common.sinks.mysql-sink.batch_size":"1","cygnus-common.sinks.mysql-sink.channel":"mysql-channel","cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnus-common.channels.mysql-channel.type":"memory","cygnus-common.channels.aux_channel1.transactionCapacity":"100","cygnus-common.sources.http-source.channels":"mysql-channel","cygnus-common.sources.http-source.handler.events_ttl":"2","cygnus-common.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnus-common.sinks.mysql-sink.data_model":"dm-by-attribute","cygnus-common.sinks":"mysql-sink","cygnus-common.sinks.mysql-sink.attr_persistence":"row","cygnus-common.channels":"mysql-channel aux_channel1"}}}
```

Invalid path to the agent configuration file:

```
{"success":"false","result" : { "File not found in the path received" }
```

Parameter not found in the agent configuration file:

```
{"success":"false","result" : {"agent":{"cygnus-common.sinks.mysql-sink.mysql_port":"3306","cygnus-common.channels.mysql-channel.transactionCapacity":"100","cygnus-common.sinks.mysql-sink.new_parameter":"new_value","cygnus-common.sources.http-source.interceptors.gi.type":"com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder","cygnus-common.sources.http-source.handler.default_service":"def_service","cygnus-common.sources.http-source.interceptors":"ts gi","cygnus-common.sinks.mysql-sink.mysql_host":"localhost","cygnus-common.sources.http-source.type":"org.apache.flume.source.http.HTTPSource","cygnus-common.sources.http-source.handler.default_service_path":"\/def_servpath","cygnus-common.sources.http-source.handler.notification_target":"\/notify","cygnus-common.sinks.mysql-sink.enable_grouping":"true","cygnus-common.sinks.mysql-sink.mysql_password":"","cygnus-common.channels.aux_channel1.capacity":"1000","cygnus-common.sources":"http-source","cygnus-common.channels.mysql-channel.capacity":"1000","cygnus-common.sinks.mysql-sink.type":"com.telefonica.iot.cygnus.sinks.NGSIMySQLSink","cygnus-common.sources.http-source.interceptors.ts.type":"timestamp","cygnus-common.channels.aux_channel1.type":"memory","cygnus-common.sinks.mysql-sink.batch_timeout":"10","cygnus-common.sources.http-source.port":"5050","cygnus-common.sinks.mysql-sink.batch_size":"1","cygnus-common.sinks.mysql-sink.mysql_username":"root","cygnus-common.sinks.mysql-sink.channel":"mysql-channel","cygnus-common.sources.http-source.interceptors.gi.grouping_rules_conf_file":"\/cygnus\/apache-flume-1.4.0-bin\/conf\/grouping_rules.conf","cygnus-common.channels.mysql-channel.type":"memory","cygnus-common.channels.aux_channel1.transactionCapacity":"100","cygnus-common.sources.http-source.channels":"mysql-channel","cygnus-common.sources.http-source.handler.events_ttl":"2","cygnus-common.sources.http-source.handler":"com.telefonica.iot.cygnus.handlers.NGSIRestHandler","cygnus-common.sinks.mysql-sink.data_model":"dm-by-attribute","cygnus-common.sinks":"mysql-sink","cygnus-common.sinks.mysql-sink.attr_persistence":"row","cygnus-common.channels":"mysql-channel aux_channel1"}}}
```

Invalid agent configuration file name:

```
{"success":"false","error":"Agent file name must start with 'agent_'."}
```

[Top](#top)

## <a name="section8"></a>`GET /admin/configuration/instance`
### <a name="section8.1"></a>`GET` all parameters

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
{"success":"true","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","ADMIN_PORT":"5080","CONFIG_FOLDER":"\/usr\/cygnus\/conf","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}
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

### <a name="section8.2"></a>`GET` a single parameter

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

## <a name="section9"></a>`POST /admin/configuration/instance`

Posts a single parameter if it doesn't exist in the instance given the path to the configuration file as the URI within the URL and the name and the value of the parameter as a query parameters. The path to the instance must be with `/usr/cygnus/conf`.

```
POST "http://<cygnus_host>:<management_port>/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>&value=<param_value>"
```

NOTE: Using the `/v1/admin/configuration/instance` path behaves the same way.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>&value=<param_value>"
```

Responses:

Valid path to the instance configuration file:

```
{"success":"true","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","ADMIN_PORT":"5080","CONFIG_FOLDER":"\/usr\/cygnus\/conf","ADMIN_PORT_2":"5081","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}}
```

Invalid path to the instance configuration file:

```
{"success":"false","result" : {"Invalid path for a instance configuration file"}
```

Existing value in the instance configuration file:

```
{"success":"false","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","CONFIG_FOLDER":"\/usr\/cygnus\/conf","ADMIN_PORT":"5080","CYGNUS_USER":"cygnus","LOGFILE_NAME":"cygnus.log","POLLING_INTERVAL":"30"}}}
```

Instance configuration file not found:

```
{"success":"false","result" : {"File not found in the path received"}
```

[Top](#top)

## <a name="section10"></a>`PUT /admin/configuration/instance`

Puts a single parameter if it doesn't exist or update it if already exists in the instance given the path to the configuration file as the URI within the URL and the name and the value of the parameter as a query parameters. The path to the instance must be with `/usr/cygnus/conf`.

```
PUT "http://<cygnus_host>:<management_port>/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>&value=<param_value>"
```

NOTE: Using the `/v1/admin/configuration/instance` path behaves the same way.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>&value=<param_value>"
```

Responses:

Valid path to the instance configuration file. Adding `NEW_PARAM` with value `old_value`:

```
{"success":"true","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","ADMIN_PORT":"5080","CONFIG_FOLDER":"\/usr\/cygnus\/conf","NEW_PARAM":"old_value","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}}
```

Valid path to the instance configuration file. Updating `NEW_PARAM` with value `new_value`:

```
{"success":"true","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","ADMIN_PORT":"5080","CONFIG_FOLDER":"\/usr\/cygnus\/conf","NEW_PARAM":"new_value","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}}
```

Invalid path to the instance configuration file:

```
{"success":"false","result" : {"Invalid path for a instance configuration file"}
```

Instance configuration file not found:

```
{"success":"false","result" : {"File not found in the path received"}
```

Below you can see the tested instance configuration file after the `PUT` method.

```
#####
# Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FI-WARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es

# Which is the config file
CONFIG_FILE=/usr/cygnus/conf/agent.conf

# Name of the agent. The name of the agent is not trivial, since it is the base for the Flume parameters
# naming conventions, e.g. it appears in .sources.http-source.channels=...
AGENT_NAME=cygnus-common

# Administration port. Must be unique per instance
ADMIN_PORT=5080

# Where is the config folder
CONFIG_FOLDER=/usr/cygnus/conf

OLD_CONFIG=true

# Name of the logfile located at /var/log/cygnus. It is important to putthe extension '.log' in order to the log rotation works properly
LOGFILE_NAME=cygnus.log

# Who to run cygnus as. Note that you may need to use root if you want
# to run cygnus in a privileged port (<1024)
CYGNUS_USER=cygnus

# Polling interval (seconds) for the configuration reloading
POLLING_INTERVAL=30
```

[Top](#top)

## <a name="section11"></a>`DELETE /admin/configuration/instance`

Deletes a single parameter in the instance given the path to the configuration file as the URI within the URL and the name of the parameter as a query parameters. The path to the instance must be with `/usr/cygnus/conf`.

```
DELETE "http://<cygnus_host>:<management_port>/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>"
```

NOTE: Using the `/v1/admin/configuration/instance` path behaves the same way.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/configuration/instance/usr/cygnus/conf/cygnus_instance.conf?param=<param_name>"
```

Responses:

Valid path to the instance configuration file:

```
{"success":"true","result" : {"instance":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","ADMIN_PORT":"5080","CONFIG_FOLDER":"\/usr\/cygnus\/conf","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}}
```

Inexisting value in the instance configuration file:

```
{"success":"false","result" : {"agent":{"CONFIG_FILE":"\/usr\/cygnus\/conf\/agent.conf","AGENT_NAME":"cygnus-common","ADMIN_PORT":"898989","CONFIG_FOLDER":"\/usr\/cygnus\/conf","ADMIN_PORT_2":"1234","LOGFILE_NAME":"cygnus.log","CYGNUS_USER":"cygnus","POLLING_INTERVAL":"30"}}}
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
