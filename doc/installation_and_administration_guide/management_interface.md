#<a name="top"></a>Management interface
Content:

* [GET `/version`](#section1)
* [GET `/stats`](#section2)

##<a name="section1"></a>`GET /version`
Gets the version of the running software, including the last Git commit:

```
GET http://<cygnus_host>:<management_port>/version
```

Response:

```
{"version":"0.5_SNAPSHOT.8a6c07054da894fc37ef30480cb091333e2fccfa"}
```

[Top](#top)

##<a neme="section2"></a>`GET /stats`
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
GET http://<cygnus_host>:<management_port>/stats
```

Response:

```
{
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
}
```

[Top](#top)
