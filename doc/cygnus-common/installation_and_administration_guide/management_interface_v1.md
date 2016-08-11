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
* [POST `/v1/subscriptions`](#section9)
  * [`NGSI Version 1`](#section9.1)
  * [`NGSI Version 2`](#section9.2)
* [DELETE `/v1/subscriptions`](#section10)
* [GET `/v1/subscriptions`](#section11)
  * [GET subscription by ID](#section11.1)
  * [GET all subscriptions](#section11.2)
* [GET `/v1/admin/log/appenders`](#section12)
  * [GET appender by name](#section12.1)
  * [GET all appenders](#section12.2)
* [GET `/v1/admin/log/loggers`](#section13)
  * [GET logger by name](#section13.1)
  * [GET all loggers](#section13.2)
* [PUT `/v1/admin/log/appenders`](#section14)
* [POST `/v1/admin/log/appenders`](#section15)
* [PUT `/v1/admin/log/loggers`](#section16)
* [POST `/v1/admin/log/loggers`](#section17)
* [DELETE `/v1/admin/log/appenders`](#section18)
  * [DELETE appender by name](#section18.1)
  * [DELETE all appenders](#section18.2)
* [DELETE `/v1/admin/log/loggers`](#section19)
  * [DELETE logger by name](#section19.1)
  * [DELETE all loggers](#section19.2)

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
Deletes a [grouping rules](../flume_extensions_catalogue/grouping_interceptor.md), given its ID as a query parameter.

```
DELETE http://<cygnus_host>:<management_port>/v1/groupingrules?id=2
```

Response:

```
{"success":"true"}
```

[Top](#top)

##<a name="section9"></a>`POST /v1/subscriptions`
###<a name="section9.1"></a>`NGSI Version 1`

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

###<a name="section9.2"></a>`NGSI Version 2`

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

##<a name="section10"></a>`DELETE /v1/subscriptions`

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

##<a name="section11"></a>`GET /v1/subscriptions`
###<a name="section11.1"></a> GET subscription by ID

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

###<a name="section11.2"></a> GET all subscriptions

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

##<a name="section12"></a> GET `/v1/admin/log/appenders`
###<a name="section12.1"></a> GET appender by name

Gets an existent appender from a running logger given its name. It can be retrieved from the running Cygnus or from the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the appenders are retrieved from Cygnus, if `transient=false` are retrieved from file.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/log/appenders?name=<appender_name>&transient=<transient_value>"
```

Responses:

When an appender is found given its name:
```
{"success":"true","appender":"[{"name":".....","layout":"......"}]}
```

When there aren't an appender with a given name:
```
{"success":"false","result":"Appender name not found"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

###<a name="section12.2"></a> GET all appenders

Gets all existent appenders from a running logger. They can be retrieved from the running Cygnus or from the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the appenders are retrieved from Cygnus, if `transient=false` are retrieved from file.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/log/appenders?transient=<transient_value>"
```

Responses:

When all appenders are found:
```
{"success":"true","appenders":[{"name":".....","layout":"......"},{"name":".....","layout":"....."}]}
```

When there aren't appenders to be shown:
```
{"success":"false","result":"No log4j appenders found"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section13"></a> GET `/v1/admin/log/loggers`
###<a name="section13.1"></a> GET logger by name

Gets an existent logger from a running Cygnus given its name. It can be retrieved from the running Cygnus or from the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the logger is retrieved from Cygnus, if `transient=false` is retrieved from file.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?name=<logger_name>&transient=<transient_value>"
```

Responses:

When a logger is found given its name:
```
{"success":"true","logger":[{"name":"....."}]}
```

When there aren't an logger with a given name:
```
{"success":"false","result":"logger name not found"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

###<a name="section13.2"></a> GET all loggers

Gets all existent loggers from a running Cygnus. They can be retrieved from the running Cygnus or from the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the loggers are retrieved from Cygnus, if `transient=false` are retrieved from file.

```
GET "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=<transient_value>"
```

Responses:

When all loggers are found:
```
{"success":"true","loggers":[{"name":".......","level":"...."},{"name":"....."},{"name":".....","level":"....."}]}
```

When there aren't appenders to be shown:
```
{"success":"false","result":"No log4j loggers found"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section14"></a> PUT `/v1/admin/log/appenders`

Puts an appender in a running Cygnus given a JSON with the information about the name and class of the appender and its layout and ConversionPattern of its pattern. If parameterised with `transient=true` (or omitting this parameter) the appender is put on Cygnus, if `transient=false` is put on the file.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/log/appenders?transient=<transient_value>" -d
'{
	"appender": {
		"name":".....",
		"class":"....."
	  },
	  "pattern": {
		"layout":".....",
		"ConversionPattern":"....."  
	  }
  }'
```

Responses:

When a new appender is put:
```
{"success":"true","result":{"Appender '....' put."}}
```

When the appender exist and is updated:
```
{"success":"true","result":{"Appender '....' updated succesfully"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section15"></a> POST `/v1/admin/log/appenders`

Posts an appender in a running Cygnus given a JSON with the information about the name and class of the appender and its layout and ConversionPattern of its pattern. If parameterised with `transient=true` (or omitting this parameter) the appender is posted on Cygnus, if `transient=false` is posted on the file.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=<transient_value>" -d
'{
	"appender": {
		"name":".....",
		"class":"....."
	  },
	  "pattern": {
		"layout":".....",
		"ConversionPattern":"....."  
	 }
  }'
```

Responses:

When a new appender is put:
```
{"success":"true","result":{"Appender '.....' posted."}}
```

When the appender exist and is updated:
```
{"success":"false","result":{"Appender '.....' already exist"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section16"></a> PUT `/v1/admin/log/loggers`

Puts a logger in a running Cygnus given a JSON with the information about the name and level of the logger. This method only accepts the parameter `transient=false` due to logger creation limitations in the code. Therefore, the loggers are put on the `log4j.properties` file.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=false" -d
'{
	"logger": {
		"name":".....",
		"level":"....."
	}
}'
```

Responses:

When a new appender is put:
```
{"success":"true","result":{"Appender '....' put."}}
```

When the appender exist and is updated:
```
{"success":"true","result":{"Appender '....' updated succesfully"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section17"></a> POST `/v1/admin/log/loggers`

Posts an logger on a running Cygnus. This method only accepts the parameter `transient=false` due to logger creation limitations in the code. Therefore, the loggers are posted on the `log4j.properties` file.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=false" -d
'{
	"logger": {
		"name":".....",
		"level":"....."
	}
}'
```

Responses:

When a new logger is put:
```
{"success":"true","result":{"Logger '.....' posted."}}
```

When the logger exist and is updated:
```
{"success":"false","result":{"Logger '.....' already exist"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)


##<a name="section18"></a> DELETE `/v1/admin/log/appenders`
###<a name="section18.1"></a> DELETE appender by name

Deletes an existent appender from a running logger given its name. It can be deleted on the running Cygnus or in the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the appender is deleted on Cygnus, if `transient=false` is deleted in the file.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/log/appenders?name=<appender_name>&transient=<transient_value>"
```

Responses:

When an appender is found and deleted given its name:
```
{"success":"true","result":"Appender '.....' removed succesfully"}
```

When there aren't an appender with a given name:
```
{"success":"false","result":"Appender name not found"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

###<a name="section18.2"></a> DELETE all appenders

Deletes all existent appenders from a running logger. They can be deleted on the running Cygnus or in the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the appenders are deleted on Cygnus, if `transient=false` are deleted in the file.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/log/appenders?transient=<transient_value>"
```

Responses:

When all appenders are deleted:
```
{"success":"true","result":"Appenders removed succesfully"}}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section19"></a> DELETE `/v1/admin/log/loggers`
###<a name="section19.1"></a> DELETE logger by name

Deletes an existent logger from a running Cygnus given its name. It can be deleted on a running Cygnus or in the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the logger is deleted on Cygnus, if `transient=false` is deleted in the file.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?name=<logger_name>&transient=<transient_value>"
```

Responses:

When a logger is found and deleted given its name:
```
{"success":"true","result":"Logger '.....' removed succesfully"}
```

When there aren't an logger with a given name:
```
{"success":"false","result":"Logger name not found"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

###<a name="section19.2"></a> DELETE all loggers

Deletes all existent loggers from a running Cygnus. They can be deleted on a running Cygnus or in the `log4j.properties` file.
If parameterised with `transient=true` (or omitting this parameter) the loggers are deleted on Cygnus, if `transient=false` are deleted in the file.

```
DELETE "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=<transient_value>"
```

Responses:

When all loggers are deleted:
```
{"success":"true","result":"Loggers removed succesfully"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)
