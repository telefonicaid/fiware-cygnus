#<a name="top"></a>Management interface
Content:

* [GET `/v1/version`](#section1)
* [GET `/v1/stats`](#section2)
* [PUT `/v1/stats`](#section3)
* [GET `/v1/groupingrules`](#section4)
* [POST `/v1/groupingrules`](#section5)
* [PUT `/v1/groupingrules`](#section6)
* [DELETE `/v1/groupingrules`](#section7)
* [PUT `/admin/log`](#section8)

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

##<a name="section8"></a>`PUT /admin/log`
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
{"error":"

[Top](#top)
