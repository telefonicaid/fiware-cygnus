#<a name="top"></a>OrionTestSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to logs](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionTestSink`, or simply `OrionTestSink` is a sink designed to test Cygnus when receiving NGSI-like context data events. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events is not meant to be persisted at any real storage, but simply logged (depending on your `log4j` configuration, the logs will be printed in console, a file...).

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to [`OrionRestHandler`](./orion_rest_handler.md). Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events lo logs
The mapping is direct, converting the context data into strings to be written in console, or file...

[Top](#top)

###<a name="section1.3"></a>Example
Assuming the following Flume event is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	        content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         ttl=10,
	         notified-service=vehicles,
	         notified-servicepath=4wheels,
	         default-destination=car1_car
	         default-servicepaths=4wheels
	         grouped-destination=car1_car
	         grouped-servicepath=4wheels
        },
        body={
	         entityId=car1,
	         entityType=car,
	         attributes=[
	             {
	                 attrName=speed,
	                 attrType=float,
	                 attrValue=112.9
	             },
	             {
	                 attrName=oil_level,
	                 attrType=float,
	                 attrValue=74.6
	             }
	         ]
	     }
    }

Assuming the log appender is the console, then `OrionTestSink` will log the data within the body as:

```
time=2015-11-27T10:41:26.504CET | lvl=INFO | trans=1448617281-654-0000000000 | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.OrionRestHandler[150] : Starting transaction (1448617281-654-0000000000)
time=2015-11-27T10:41:26.507CET | lvl=INFO | trans=1448617281-654-0000000000 | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.OrionRestHandler[232] : Received data ({  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "attributes" : [          {            "name" : "speed",            "type" : "float",            "value" : "112.9"          },          {            "name" : "oil-level",            "type" : "float",            "value" : "74.6"          }        ],        "type" : "car",        "isPattern" : "false",        "id" : "car1"      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2015-11-27T10:41:26.512CET | lvl=INFO | trans=1448617281-654-0000000000 | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.OrionRestHandler[255] : Event put in the channel (id=347293150, ttl=10)
time=2015-11-27T10:41:27.123CET | lvl=INFO | trans=1448617281-654-0000000000 | function=persistOne | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.OrionTestSink[77] : [test-sink] Processing headers (recvTimeTs=1448617286513 (2015-11-27T09:41:26.513Z), fiwareService=vehicles, fiwareServicePath=[4wheels], destinations=[car1_car])
time=2015-11-27T10:41:27.123CET | lvl=INFO | trans=1448617281-654-0000000000 | function=persistOne | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.OrionTestSink[90] : [test-sink] Processing context element (id=car1, type= car)
time=2015-11-27T10:41:27.124CET | lvl=INFO | trans=1448617281-654-0000000000 | function=persistOne | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.OrionTestSink[109] : [test-sink] Processing context attribute (name=speed, type=float, value=112.9, metadata=[])
time=2015-11-27T10:41:27.124CET | lvl=INFO | trans=1448617281-654-0000000000 | function=persistOne | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.OrionTestSink[109] : [test-sink] Processing context attribute (name=oil-level, type=float, value=74.6, metadata=[])
time=2015-11-27T10:41:27.124CET | lvl=INFO | trans=1448617281-654-0000000000 | function=process | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.OrionSink[178] : Finishing transaction (1448617281-654-0000000000)
```

[Top](#top)

##<a name="section2"></a>Adinistration guide
###<a name="section2.1"></a>Configuration
`OrionTestSink` is very easy to configure... it has no specific configuration!

Simply list the sink as usual in a basic Flume configuration, i.e. by especifying its type and its channel:

```
<agentname>.sinks = test-sink
<agentname>.channels = test-channel
...
<agentname>.sinks.test-sink.type = com.telefonica.iot.cygnus.sinks.OrionTestSink
<agentname>.sinks.test-sink.channel = test-channel
```

[Top](#top)

###<a name="section2.2"></a>Use cases
Use this sink in order to test if a Cygnus deployment is properly receiveing notifications from an Orion Context Broker premise.

[Top](#top)
