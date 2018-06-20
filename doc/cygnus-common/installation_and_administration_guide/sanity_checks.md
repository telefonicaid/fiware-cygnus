# <a name="top"></a>Sanity checks
Content:

* [How to proceed](#section1)
* [Check: Logs](#section2)
* [Check: API port](#section3)
* [Check: GUI port](#section4)

## <a name="section1"></a>How to proceed
Verify all the sanity checks included in this document, one by one.

If you have any problem with one specific check, please go to the proper section of the [diagnosis procedures](./diagnosis_procedures.md) document.

[Top](#top)

## <a name="section2"></a>Check: Logs
Any Cygnus agent logs in `/var/log/cygnus/cygnus.log`, unless the `console` appender is used.

In any case, traced logs must look like the following ones:

```
time=2016-05-09T09:50:33.074CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=main | comp= | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[166] : Starting Cygnus, version 0.13.0_SNAPSHOT.180fd310917cade2f1f3f5f864610ea0b15907f9
time=2016-05-09T09:50:33.405CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=main | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[277] : Waiting for valid Flume components references...
time=2016-05-09T09:50:33.406CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=start | comp=cygnus-ngsi | msg=org.apache.flume.node.PollingPropertiesFileConfigurationProvider[61] : Configuration provider starting
time=2016-05-09T09:50:33.409CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=run | comp=cygnus-ngsi | msg=org.apache.flume.node.PollingPropertiesFileConfigurationProvider$FileWatcherRunnable[133] : Reloading configuration file:/Applications/Apache/apache-flume-1.4.0-bin/conf/agent_mongo.conf
time=2016-05-09T09:50:33.462CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.463CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.464CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.464CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.464CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.464CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.465CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.465CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.466CEST | lvl=WARN | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration[101] : Configuration property ignored: cygnus-ngsi.sinks.mongo-sink.mongo_username = 
time=2016-05-09T09:50:33.467CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.467CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.468CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.469CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.470CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.470CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.470CEST | lvl=WARN | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration[101] : Configuration property ignored: cygnus-ngsi.sinks.mongo-sink.mongo_password = 
time=2016-05-09T09:50:33.470CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.471CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[930] : Added sinks: mongo-sink Agent: cygnus-ngsi
time=2016-05-09T09:50:33.471CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=addProperty | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration$AgentConfiguration[1016] : Processing:mongo-sink
time=2016-05-09T09:50:33.487CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=validateConfiguration | comp=cygnus-ngsi | msg=org.apache.flume.conf.FlumeConfiguration[140] : Post-validation flume configuration contains configuration for agents: [cygnus-ngsi]
time=2016-05-09T09:50:33.487CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=loadChannels | comp=cygnus-ngsi | msg=org.apache.flume.node.AbstractConfigurationProvider[150] : Creating channels
time=2016-05-09T09:50:33.500CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=create | comp=cygnus-ngsi | msg=org.apache.flume.channel.DefaultChannelFactory[40] : Creating instance of channel mongo-channel type memory
time=2016-05-09T09:50:33.509CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=loadChannels | comp=cygnus-ngsi | msg=org.apache.flume.node.AbstractConfigurationProvider[205] : Created channel mongo-channel
time=2016-05-09T09:50:33.511CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=create | comp=cygnus-ngsi | msg=org.apache.flume.source.DefaultSourceFactory[39] : Creating instance of source http-source, type org.apache.flume.source.http.HTTPSource
time=2016-05-09T09:50:33.727CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=configure | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[145] : Startup completed
time=2016-05-09T09:50:33.785CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=create | comp=cygnus-ngsi | msg=org.apache.flume.sink.DefaultSinkFactory[40] : Creating instance of sink: mongo-sink, type: com.telefonica.iot.cygnus.sinks.NGSIMongoSink
time=2016-05-09T09:50:33.852CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=getConfiguration | comp=cygnus-ngsi | msg=org.apache.flume.node.AbstractConfigurationProvider[119] : Channel mongo-channel connected to [http-source, mongo-sink]
time=2016-05-09T09:50:33.861CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=startAllComponents | comp=cygnus-ngsi | msg=org.apache.flume.node.Application[138] : Starting new configuration:{ sourceRunners:{http-source=EventDrivenSourceRunner: { source:org.apache.flume.source.http.HTTPSource{name:http-source,state:IDLE} }} sinkRunners:{mongo-sink=SinkRunner: { policy:org.apache.flume.sink.DefaultSinkProcessor@630d36eb counterGroup:{ name:null counters:{} } }} channels:{mongo-channel=org.apache.flume.channel.MemoryChannel{name: mongo-channel}} }
time=2016-05-09T09:50:33.865CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=startAllComponents | comp=cygnus-ngsi | msg=org.apache.flume.node.Application[145] : Starting Channel mongo-channel
time=2016-05-09T09:50:34.000CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=register | comp=cygnus-ngsi | msg=org.apache.flume.instrumentation.MonitoredCounterGroup[110] : Monitoried counter group for type: CHANNEL, name: mongo-channel, registered successfully.
time=2016-05-09T09:50:34.001CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=start | comp=cygnus-ngsi | msg=org.apache.flume.instrumentation.MonitoredCounterGroup[94] : Component type: CHANNEL, name: mongo-channel started
time=2016-05-09T09:50:34.001CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=startAllComponents | comp=cygnus-ngsi | msg=org.apache.flume.node.Application[173] : Starting Sink mongo-sink
time=2016-05-09T09:50:34.002CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=startAllComponents | comp=cygnus-ngsi | msg=org.apache.flume.node.Application[184] : Starting Source http-source
time=2016-05-09T09:50:34.057CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[61] : Grouping rules read: {    "grouping_rules": [        {            "id": 1,            "fields": [                "servicePath"            ],            "regex": "/moba",            "destination": "newdest1",            "fiware_service_path": "/newservpath1"        },        {            "id": 2,            "fields": [                "entityType"            ],            "regex": "TYPEMATCH:2",            "destination": "newdest2",            "fiware_service_path": "/newservpath2"        },        {            "id": 3,            "fields": [                "entityId",                "entityType"            ],            "regex": "DEVMATCH:3TYPEMATCH:3",            "destination": "newdest3",            "fiware_service_path": "/newservpath3"        },        {            "id": 4,            "fields": [                "entityType"            ],            "regex": "Room",            "destination": "allrooms",            "fiware_service_path": "/rooms"        }      ]}
time=2016-05-09T09:50:34.080CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=start | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.sinks.NGSISink[240] : [mongo-sink] Startup completed
time=2016-05-09T09:50:34.187CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[71] : Grouping rules syntax is OK
time=2016-05-09T09:50:34.207CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[75] : Grouping rules regex'es have been compiled
time=2016-05-09T09:50:34.224CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[61] : Grouping rules read: {    "grouping_rules": [        {            "id": 1,            "fields": [                "servicePath"            ],            "regex": "/moba",            "destination": "newdest1",            "fiware_service_path": "/newservpath1"        },        {            "id": 2,            "fields": [                "entityType"            ],            "regex": "TYPEMATCH:2",            "destination": "newdest2",            "fiware_service_path": "/newservpath2"        },        {            "id": 3,            "fields": [                "entityId",                "entityType"            ],            "regex": "DEVMATCH:3TYPEMATCH:3",            "destination": "newdest3",            "fiware_service_path": "/newservpath3"        },        {            "id": 4,            "fields": [                "entityType"            ],            "regex": "Room",            "destination": "allrooms",            "fiware_service_path": "/rooms"        }      ]}
time=2016-05-09T09:50:34.225CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[71] : Grouping rules syntax is OK
time=2016-05-09T09:50:34.225CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[75] : Grouping rules regex'es have been compiled
time=2016-05-09T09:50:34.407CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=main | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 5080 (Management Interface)
time=2016-05-09T09:50:34.683CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=register | comp=cygnus-ngsi | msg=org.apache.flume.instrumentation.MonitoredCounterGroup[110] : Monitoried counter group for type: SOURCE, name: http-source, registered successfully.
time=2016-05-09T09:50:34.684CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=start | comp=cygnus-ngsi | msg=org.apache.flume.instrumentation.MonitoredCounterGroup[94] : Component type: SOURCE, name: http-source started
```

The order of the traces may differ, but usually, the first one trace tells you Cygnus has started, showing the release version:

    time=2016-05-09T09:50:33.074CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=main | comp= | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[166] : Starting Cygnus, version 0.13.0_SNAPSHOT.

Then, all the components of your configuration are created and started, e.g. the source (including its handler):

```
time=2016-05-09T09:50:33.511CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=create | comp=cygnus-ngsi | msg=org.apache.flume.source.DefaultSourceFactory[39] : Creating instance of source http-source, type org.apache.flume.source.http.HTTPSource
time=2016-05-09T09:50:33.727CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=configure | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[145] : Startup completed
```

The grouping rules are read:

```
time=2016-05-09T09:50:34.224CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[61] : Grouping rules read: {    "grouping_rules": [        {            "id": 1,            "fields": [                "servicePath"            ],            "regex": "/moba",            "destination": "newdest1",            "fiware_service_path": "/newservpath1"        },        {            "id": 2,            "fields": [                "entityType"            ],            "regex": "TYPEMATCH:2",            "destination": "newdest2",            "fiware_service_path": "/newservpath2"        },        {            "id": 3,            "fields": [                "entityId",                "entityType"            ],            "regex": "DEVMATCH:3TYPEMATCH:3",            "destination": "newdest3",            "fiware_service_path": "/newservpath3"        },        {            "id": 4,            "fields": [                "entityType"            ],            "regex": "Room",            "destination": "allrooms",            "fiware_service_path": "/rooms"        }      ]}
time=2016-05-09T09:50:34.225CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[71] : Grouping rules syntax is OK
time=2016-05-09T09:50:34.225CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=<init> | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.interceptors.CygnusGroupingRules[75] : Grouping rules regex'es have been compiled
```

And the Management Interface is setup: 

    time=2016-05-09T09:50:34.407CEST | lvl=INFO | corr= | trans= | svc= | subsvc= | function=main | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 5080 (Management Interface)

[Top](#top)

## <a name="section3"></a>Check: API port
The API must be up and running in the port you configured (either using the `-p` option in the command line, either using the `ADMIN_PORT` parameter in the `cygnus_instance_<id>.conf` file). `5080` is the default.

You can check it by asking for the Cygnus version:

```
$ curl "http://localhost:5080/v1/version"
{"success":"true","version":"0.13.0_SNAPSHOT.180fd310917cade2f1f3f5f864610ea0b15907f9"}
```

[Top](#top)

## <a name="section4"></a>Check: GUI port
Coming soon.

[Top](#top)
