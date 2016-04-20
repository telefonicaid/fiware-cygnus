#<a name="top"></a>Management interface
Content:

* [GET `/v1/version`](#section1)
* [GET `/v1/stats`](#section2)
* [PUT `/v1/stats`](#section3)
* [GET `/v1/groupingrules`](#section4)
* [POST `/v1/groupingrules`](#section5)
* [PUT `/v1/groupingrules`](#section6)
* [DELETE `/v1/groupingrules`](#section7)
* [GET `/admin/log`](#section8)
* [PUT `/admin/log`](#section9)
* [POST `/v1/subscriptions`](#section10)
* [DELETE `/v1/subscriptions`](#section11)

##<a name="section1"></a>`GET /v1/version`
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

##<a name="section2"></a>`GET /v1/stats`
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

##<a name="section3"></a>`PUT /v1/stats`
Resets the statistics about the configured Flume components. It is important to note <b>in order to reset statistics from the channels</b>, these must be of type `com.telefonica.iot.cygnus.channels.CygnusMemoryChannel` or `com.telefonica.iot.cygnus.channels.CygnusFileChannel`.

```
PUT http://<cygnus_host>:<management_port>/v1/stats
```

Response:

```
{"success":"true"}
```

[Top](#top)

##<a name="section4"></a>`GET /v1/groupingrules`
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

##<a name="section5"></a>`POST /v1/groupingrules`
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

##<a name="section6"></a>`PUT /v1/groupingrules`
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

##<a name="section7"></a>`DELETE /v1/groupingrules`
Deletes a [grouping rules](../flume_extensions_catalogue/grouping_interceptor.md), given its ID a a query parameter.

```
DELETE http://<cygnus_host>:<management_port>/v1/groupingrules?id=2
```

Response:

```
{"success":"true"}
```

[Top](#top)

##<a name="section8"></a>`GET /admin/log`
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

##<a name="section9"></a>`PUT /admin/log`
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

##<a name="section10"></a>`POST /v1/subscriptions`

Creates a new subscription to Orion. The Json passed in the payload contains the Json subscription itself and Orion's endpoint details.

```
POST "http://<cygnus_host>:<management_port>/v1/subscriptions"
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
}'
```
Responses:

Valid subscription:
```
{
    "success":"true",
    "result" : {
        {
            "subscribeResponse":{
                  "duration":"P1M",
                  "throttling":"PT5S",
                  "subscriptionId":"56f9081c3c6fb7e9d2a912a0"
            }
        }
    }
}
```

Invalid subscription (Unknown fields in this case)
```
{
    "success":"true",
    "result" :
        {
            "subscribeError":
                  {
                      "errorCode":
                            {
                                "code":"400",
                                "reasonPhrase":"Bad Request",
                                "details":"JSON Parse Error: unknown field: \/extraField"
                            }
                  }
      }
}
```

Invalid JSON (Empty field and missing field)
```
{
    "success":"false",
    "error":"Invalid subscription, field 'duration' is empty"
}

{
    "success":"false",
    "error":"Invalid subscription, field 'notifyConditions' is missing"
}


```

Please observe Cygnus checks if the Json passed in the payload is valid (syntactically and semantically).

[Top](#top)

##<a name="section11"></a>`DELETE /v1/subscriptions`

Deletes a subscription made to Orion with a given subscriptionId. The Json passed in the payload contains the Orion's endpoint details.

```
DELETE "http://<cygnus_host>:<management_port>/v1/subscriptions?subscription_id=<subscriptionId>"
```

Responses:

204 OK
```
{"success":"true","result" : {
      "Subscription deleted"
}
```

Wrong parameter:
```
{"success":"false","error":"Parse error, wrong parameter. Check it for errors."}
```

Wrong subscriptionId:
```
{"success":"false","result" : {
    "description":{
        "The requested subscription has not been found. Check id","error":"NotFound"
        }
    }
}
```

Empty or missing AuthToken:
```
{"success":"false","error":"Empty Auth-Token. Required for DELETE subscriptions"}

{"success":"false","error":"Missing Auth-Token. Required for DELETE subscriptions"}
```

Missing endpoind (Empty or not given):
```
{"success":"false","error":"Missing endpoint"}
```

[Top](#top)
