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
* [POST `/v1/subscriptions`](#section11)
  * [`NGSI Version 1`](#section11.1)
  * [`NGSI Version 2`](#section11.2)
* [DELETE `/v1/subscriptions`](#section12)
* [GET `/v1/subscriptions`](#section13)
  * [GET subscription by ID](#section13.1)
  * [GET all subscriptions](#section13.2)

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

##<a name="section11"></a>`POST /v1/subscriptions`
###<a name="section11.1"></a> `NGSI Version 1`

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

###<a name="section11.2"></a> `NGSI Version 2`

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

##<a name="section12"></a>`DELETE /v1/subscriptions`

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

##<a name="section13"></a>`GET /v1/subscriptions`
###<a name="section13.1"></a> GET subscription by ID

Gets an existent subscription from Orion, given the ngsi version and the subscription id as a query parameter.

Valid ngsi versions are `1` and `2` (This method only works with `ngsi_version=2` due to this method is not implemented in version `1`).

```
GET "http://<cygnus_host>:<management_port>/v1/subscriptions?ngsi_version=<ngsiVersion>&subscription_id=<subscriptionId>" -d '{"host":"<host>", "port":"<port>", "ssl":"false", "xauthtoken":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}'
```

Responses:

Valid and current subscription id with the correct ngsi version:

```
{"success":"true","result" : {{"notification":{"lastNotification":"2015-07-28T22:24:33.00Z","timesSent":8122767998226748692,"callback":"http:\/\/xxx.xxx.xx.xxx:xxxx\/ngsi10\/notifyContext","attributes":[]},"expires":"2016-09-25T08:17:47.00Z","subject":{"condition":{"expression":{"q":"","geometry":"","coords":"","georel":""},"attributes":["TimeInstant"]},"entities":[{"id":"","type":"sevilla:fountain","idPattern":"patternId"}]},"id":"54325022q460a3873d30oe95","status":"active"}}
```

Valid but inexistent subscription id with the correct ngsi version:

```
{"success":"false","result" : {{"description":"","error":"subscriptionId does not correspond to an active subscription"}}
```

Invalid ngsi version:

```
{"success":"false","error":"Parse error, invalid parameter (ngsi_version): Must be 1 or 2. Check it for errors."}
```

Valid but not implemented ngsi version (sending `ngsi_version=1`):

```
{"success":"false","error":"GET /v1/subscriptions not implemented."}
```

Missing or empty parameters:

```
{"success":"false","error":"Parse error, missing parameter (subscription_id). Check it for errors."}
{"success":"false","error":"Parse error, empty parameter (ngsi_version). Check it for errors."}
```

[Top](#top)

###<a name="section13.2"></a> GET all subscriptions

Gets all existent subscriptions from Orion, given the ngsi version as a query parameter.

Valid ngsi versions are `1` and `2` (This method only works with `ngsi_version=2` due to this method is not implemented in version `1`).

```
GET "http://<cygnus_host>:<management_port>/v1/subscriptions?ngsi_version=<ngsiVersion>" -d '{"host":"<host>", "port":"<port>", "ssl":"false", "xauthtoken":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}'
```

Responses:

Valid and current subscription id with the correct ngsi version:

```
{"success":"true","result" : {{"result":[{"notification":{"lastNotification":"2015-07-28T22:23:30.00Z","timesSent":7126568376946155044,"http":{"url":"http:\/\/130.206.82.120:1026\/ngsi10\/notifyContext"},"attrs":[]},"expires":"2016-09-13T09:27:15.00Z","subject":{"condition":{"attrs":["chlorine"]},"entities":[{"id":"","type":"sevilla:fountain","idPattern":"Sevilla:FUENTES:"}]},"id":"54228e731860a3873d395d66","status":"active"},{"notification":{"lastNotification":"2015-07-28T22:24:33.00Z","timesSent":8122767998226748692,"http":{"url":"http:\/\/130.206.82.120:1026\/ngsi10\/notifyContext"},"attrs":[]},"expires":"2016-09-25T08:17:47.00Z","subject":{"condition":{"attrs":["TimeInstant"]},"entities":[{"id":"","type":"sevilla:fountain","idPattern":"Sevilla:FUENTES"}]},"id":"5432502b1860a3873d395e95","status":"active"},{"notification":{"lastNotification":"2015-07-28T22:24:53.00Z","timesSent":8786703474088345564,"http":{"url":"http:\/\/130.206.83.12:1026\/ngsi10\/notifyContext"},"attrs":[]},"expires":"2035-01-06T09:11:45.00Z","subject":{"condition":{"attrs":["TimeInstant"]},"entities":[{"id":"","type":"sevilla:fountain","idPattern":"Sevilla:FUENTES"}]},"id":"553614511860a3f429e3fd80","status":"active"},{"notification":{"lastNotification":"2015-07-28T22:25:01.00Z","timesSent":8785875184958230546,"http":{"url":"http:\/\/130.206.83.12:1026\/ngsi10\/notifyContext"},"attrs":[]},"expires":"2035-01-06T09:27:18.00Z","subject":{"condition":{"attrs":["TimeInstant","longitude","latitude"]},"entities":[{"id":"","type":"sevilla:fountain","idPattern":"Sevilla:FUENTES:"}]},"id":"553617f61860a3f429e3fd85","status":"active"},{"notification":{"lastNotification":"2016-04-11T13:24:19.00Z","timesSent":7918500433202332193,"http":{"url":"http:\/\/52.16.174.229:2700"},"attrs":[]},"expires":"2016-07-11T21:33:03.00Z","subject":{"condition":{"attrs":["sound"]},"entities":[{"id":"","type":"santander:sound","idPattern":"urn:smartsantander:testbed:.*"}]},"id":"55a2dd0f9a3bb06493b38fef","status":"active"},{"notification":{"lastNotification":"2016-04-11T13:24:19.00Z","timesSent":8793806529804560773,"http":{"url":"http:\/\/52.16.174.229:2735\/sound"},"attrs":[]},"expires":"2016-07-27T08:43:13.00Z","subject":{"condition":{"attrs":["sound"]},"entities":[{"id":"","type":"santander:sound","idPattern":"urn:smartsantander:testbed:.*"}]},"id":"55b740a1760b7a367c9c7bd1","status":"active"},{"notification":{"lastNotification":"2016-04-11T13:24:19.00Z","timesSent":8793414191782978685,"http":{"url":"http:\/\/lmctmlgw7thh.runscope.net\/sound"},"attrs":[]},"expires":"2016-07-27T08:46:15.00Z","subject":{"condition":{"attrs":["sound"]},"entities":[{"id":"","type":"santander:sound","idPattern":"urn:smartsantander:testbed:.*"}]},"id":"55b74157760b7a367c9c7bd2","status":"active"},{"notification":{"http":{"url":"http:\/\/130.206.123.223:5050\/notify"},"attrs":["taxiId","time","lat","lon","status"]},"expires":"2016-09-03T10:43:17.00Z","subject":{"condition":{"attrs":["time"]},"entities":[{"id":"","type":"Taxi","idPattern":".*"}]},"id":"55f00d453de71c949d1422e1","status":"active"},{"notification":{"http":{"url":"http:\/\/130.206.123.223:5050\/notify"},"attrs":["taxiId","time","lat","lon","status"]},"expires":"2016-09-03T11:22:52.00Z","throttling":1,"subject":{"condition":{"attrs":["time"]},"entities":[{"id":"","type":"Taxi","idPattern":".*"}]},"id":"55f0168c3de71c949d1422e2","status":"active"},{"notification":{"lastNotification":"2016-03-09T22:10:04.00Z","timesSent":8512673826141801965,"http":{"url":"http:\/\/130.206.123.223:5050\/notify"},"attrs":["taxiId","time","lat","lon","status"]},"expires":"2016-09-03T11:26:58.00Z","throttling":1,"subject":{"condition":{"attrs":["time"]},"entities":[{"id":"","type":"Taxi","idPattern":".*"}]},"id":"55f017823de71c949d1422e3","status":"active"},{"notification":{"lastNotification":"2015-09-24T14:56:08.00Z","timesSent":8040851773959392932,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-21T13:30:50.00Z","subject":{"condition":{"attrs":["status"]},"entities":[{"id":"40.64059722.944096","type":"location","idPattern":""}]},"id":"5603fb0aebf4aa5a1588cd21","status":"active"},{"notification":{"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-21T14:10:26.00Z","subject":{"condition":{"attrs":["status"]},"entities":[{"id":"crowdId","type":"crowd","idPattern":""}]},"id":"56040452ebf4aa5a1588cd22","status":"active"},{"notification":{"lastNotification":"2015-09-24T14:11:30.00Z","timesSent":8040851773959392932,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-21T14:10:54.00Z","subject":{"condition":{"attrs":["observations"]},"entities":[{"id":"crowdId","type":"crowd","idPattern":""}]},"id":"5604046eebf4aa5a1588cd23","status":"active"},{"notification":{"lastNotification":"2015-09-24T14:24:04.00Z","timesSent":8040851773959392932,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-21T14:23:15.00Z","subject":{"condition":{"attrs":["status"]},"entities":[{"id":"40.64059781.944096","type":"location","idPattern":""}]},"id":"56040753ebf4aa5a1588cd24","status":"active"},{"notification":{"lastNotification":"2015-09-24T14:25:21.00Z","timesSent":7512234279060279666,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-21T14:25:20.00Z","subject":{"condition":{"attrs":["status"]},"entities":[{"id":"40.64059777777.944096","type":"location","idPattern":""}]},"id":"560407d0ebf4aa5a1588cd25","status":"active"},{"notification":{"lastNotification":"2015-09-24T15:12:36.00Z","timesSent":7512234279060279666,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-21T15:12:36.00Z","subject":{"condition":{"attrs":["status"]},"entities":[{"id":"40.64059712.944096","type":"location","idPattern":""}]},"id":"560412e4ebf4aa5a1588cd26","status":"active"},{"notification":{"lastNotification":"2015-09-25T12:44:22.00Z","timesSent":8231085825504083150,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-22T10:37:15.00Z","subject":{"condition":{"attrs":["aqiText"]},"entities":[{"id":"40.64368522.963004","type":"location","idPattern":""}]},"id":"560523dbf0ad42c03e86d912","status":"active"},{"notification":{"lastNotification":"2015-09-25T12:44:40.00Z","timesSent":7901856702247101528,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-22T10:42:07.00Z","subject":{"condition":{"attrs":["aqiText"]},"entities":[{"id":"40.60113922.960463","type":"location","idPattern":""}]},"id":"560524fff0ad42c03e86d918","status":"active"},{"notification":{"lastNotification":"2015-09-25T12:44:39.00Z","timesSent":6886708066761051270,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-22T10:43:10.00Z","subject":{"condition":{"attrs":["aqiText"]},"entities":[{"id":"40.64059922.944096","type":"location","idPattern":""}]},"id":"5605253ef0ad42c03e86d91b","status":"active"},{"notification":{"lastNotification":"2015-09-25T12:44:39.00Z","timesSent":8578622459237801700,"http":{"url":"http:\/\/exasa.gr\/various\/test.php"},"attrs":[]},"expires":"2025-09-22T10:43:14.00Z","subject":{"condition":{"attrs":["aqiText"]},"entities":[{"id":"40.62371522.957265","type":"location","idPattern":""}]},"id":"56052542f0ad42c03e86d91c","status":"active"}]}}

```

Invalid ngsi version:

```
{"success":"false","error":"Parse error, invalid parameter (ngsi_version): Must be 1 or 2. Check it for errors."}
```

Valid but not implemented ngsi version (sending `ngsi_version=1`):

```
{"success":"false","error":"GET /v1/subscriptions not implemented for NGSI version 1."}
```

Missing or empty parameters:

```
{"success":"false","error":"Invalid endpoint, field 'ssl' is missing"}
```

[Top](#top)
