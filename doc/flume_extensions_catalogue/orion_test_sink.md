#<a name="top"></a>OrionTestSink
Content:

* [Functionality](#section1)
* [Configuration](#section2)
* [Use cases](#section3)

##<a name="section1"></a>Functionality
`OrionTestSink` is a testing sink. It is not meant to persist context data at any real storage, but cosumed Flume events are simply logged (depending on your `log4j` configuration, the logs will be printed in console, a file...).

As said, you can use this sink in order to test if a Cygnus deployment is properly receiveing notifications from an Orion Context Broker premise.

[Top](#top)

##<a name="section2"></a>Configuration
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

##<a name="section3"></a>Use cases
Use this sink in order to test if a Cygnus deployment is properly receiveing notifications from an Orion Context Broker premise.

[Top](#top)
