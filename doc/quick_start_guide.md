#Cygnus Quick Start Guide
This quick start overviews the steps a newbie programmer will have to follow in order to get familiar with Cygnus and its basic functionality. For more detailed information, please refer to the [Cygnus GitHub](https://github.com/telefonicaid/fiware-connectors).

##Basic knowlegde about Cygnus
Previous to installing and using it, you should know a couple of things about Cygnus.

###What is is for?
Cygnus is a connector in charge of persisting Orion context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using Cygnus.

###How does it receives context data from Orion Context Broker?
Cygnus uses the subscription/notification feature of Orion. A subscription is made in Orion on behalf of Cygnus, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

###Which storages is it able to integrate?
Current stable release is able to persist Orion context data in:

* HDFS, the Hadoop distributed file system.
* MySQL, the well-know relational database manager.
* CKAN, an Open Data platform.

###Which is its basic architecture?
Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/). In fact, Cygnus is a Flume agent, which is basically composed of a <i>source</i> in charge of receiving the data, a <i>channel</i> where the source puts the data once it has been transformed into a Flume event, and a <i>sink</i>, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

As said, Cygnus is able to persist in HDFS, MySQL and CKAN, thus there exists a sink for each one of those storages.

##Installing Cygnus
Open a terminal and simply configure the FIWARE repository if not yet configured and use your applications manager in order to install the latest version of Cy	gnus (CentOS/RedHat example):

```
$ sudo cat > /etc/yum.repos.d/fiware.repo <<EOL
[Fiware]
name=FIWARE repository
baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
gpgcheck=0
enabled=1
EOL
$ sudo yum install cygnus
```

**IMPORTANT NOTE:**

Cygnus, as it is based on Apache Flume, needs Java SDK is installed. If not yet installed, do it right now:

```
$ sudo yum install java-1.6.0-openjdk-devel
```

Remember to export the JAVA_HOME environment variable. In the case you have installed Java SDK right now as shown above, this would be as:

```
$ export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64
```

In order to do it permanently, edit `/root/.bash_profile` (root user) or `/etc/profile` (other users).

##Configuring a test agent
This kind of agent is the simplest one you can configure with Cygnus. It is based on a standard `HTTPSource`, a `MemoryChannel` and a `OrionTestSink`. Don't worry about the configuration details, specially those about the source; simply think on a Http listener waiting for Orion notifications on port TCP/5050 and sending that notifications in the form of Flume events to a testing purpose sink that will not really persist anything in a third-party storage, but will log the notified context data.

(1) Create and edit a `/usr/cygnus/conf/agent_test.conf` file (as a sudoer):

```
cygnusagent.sources = http-source
cygnusagent.sinks = test-sink
cygnusagent.channels = test-channel

cygnusagent.sources.http-source.channels = test-channel
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnusagent.sources.http-source.port = 5050
cygnusagent.sources.http-source.handler = es.tid.fiware.fiwareconnectors.cygnus.handlers.OrionRestHandler
cygnusagent.sources.http-source.handler.notification_target = /notify
cygnusagent.sources.http-source.handler.default_service = def_serv
cygnusagent.sources.http-source.handler.default_service_path = def_servpath
cygnusagent.sources.http-source.handler.events_ttl = 2
cygnusagent.sources.http-source.interceptors = ts de
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
cygnusagent.sources.http-source.interceptors.de.type = es.tid.fiware.fiwareconnectors.cygnus.interceptors.DestinationExtractor$Builder
cygnusagent.sources.http-source.interceptors.de.matching_table = /Applications/apache-flume-1.4.0-bin/conf/matching_table.conf

cygnusagent.channels.test-channel.type = memory
cygnusagent.channels.test-channel.capacity = 1000
cygnusagent.channels.test-channel.transactionCapacity = 100

cygnusagent.sinks.test-sink.channel = test-channel
cygnusagent.sinks.test-sink.type = es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionTestSink
```

(2) Start Cygnus from the command line; Cygnus will be printing logs on the standard output (i.e. your screen):

```
$ /usr/cygnus/bin/cygnus-flume-ng agent --conf /usr/cygnus/conf/ -f /usr/cygnus/conf/agent_test.conf -n cygnusagent -Dflume.root.logger=INFO,console
```

(3) Open a new terminal (since Cygnus should be printing logs on the standard output of the first one) and create and edit somewhere a `notification.sh` file:

```
URL=$1

curl $URL -v -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'User-Agent: orion/0.10.0' --header "Fiware-Service: qsg" --header "Fiware-ServicePath: testsink" -d @- <<EOF
{
	"subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
	"originator" : "localhost",
	"contextResponses" : [
		{
			"contextElement" : {
				"attributes" : [
					{
						"name" : "temperature",
						"type" : "centigrade",
						"value" : "26.5"
					}
				],
				"type" : "Room",
				"isPattern" : "false",
				"id" : "Room1"
			},
			"statusCode" : {
				"code" : "200",
				"reasonPhrase" : "OK"
			}
		}
	]
}
EOF
```

This script will emulate the sending of an Orion notification to the URL endpoint passed as argument.

(4) Give execution permissions to `notification.sh` and run it, pasing as argument the URL of the listening `HTTPSource`:

```
$ chmod a+x notification.sh
$ ./notification.sh http://localhost:5050/notify
```

(5) Look at the logs printed by Cygnus. You should find something like this, which means the notification was successfully received, processed as a Flume event, put into the channel and taken by the test sink in order to print it!


```
2015-03-06T08:54:20.696CET | lvl=INFO | trans=1425628437-99-0000000000 | function=getEvents | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.handlers.OrionRestHandler[153] : Starting transaction (1425628437-99-0000000000)
2015-03-06T08:54:20.699CET | lvl=INFO | trans=1425628437-99-0000000000 | function=getEvents | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.handlers.OrionRestHandler[239] : Received data ({	"subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",	"originator" : "localhost",	"contextResponses" : [		{			"contextElement" : {"attributes" : [					{						"name" : "temperature",						"type" : "centigrade",						"value" : "26.5"					}				],				"type" : "Room",				"isPattern" : "false",				"id" : "Room1"			},			"statusCode" : {				"code" : "200",				"reasonPhrase" : "OK"			}		}	]})
2015-03-06T08:54:20.704CET | lvl=INFO | trans=1425628437-99-0000000000 | function=getEvents | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.handlers.OrionRestHandler[261] : Event put in the channel (id=1621938227, ttl=2)
2015-03-06T08:54:20.799CET | lvl=INFO | trans=1425628437-99-0000000000 | function=process | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionSink[126] : Event got from the channel (id=1621938227, headers={fiware-servicepath=testsink, destination=room1_room, content-type=application/json, fiware-service=qsg, ttl=2, transactionId=1425628437-99-0000000000, timestamp=1425628460704}, bodyLength=384)
2015-03-06T08:54:20.803CET | lvl=INFO | trans=1425628437-99-0000000000 | function=persist | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionTestSink[77] : [test-sink] Processing headers (recvTimeTs=1425628460704 (2015-03-06T08:54:20.704), fiwareService=qsg, fiwareServicePath=testsink, destinations=[room1_room])
2015-03-06T08:54:20.803CET | lvl=INFO | trans=1425628437-99-0000000000 | function=persist | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionTestSink[90] : [test-sink] Processing context element (id=Room1, type= Room)
2015-03-06T08:54:20.803CET | lvl=INFO | trans=1425628437-99-0000000000 | function=persist | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionTestSink[109] : [test-sink] Processing context attribute (name=temperature, type=centigrade, value=26.5, metadata=[])
2015-03-06T08:54:20.804CET | lvl=INFO | trans=1425628437-99-0000000000 | function=process | comp=Cygnus | msg=es.tid.fiware.fiwareconnectors.cygnus.sinks.OrionSink[187] : Finishing transaction (1425628437-99-0000000000)

```

##Further reading
This Quick Start Guide is just an initial approach to Cygnus. In order to understand all its power and how to play with sinks persisting data in real third-party storages, please refer to the [Cygnus GitHub](https://github.com/telefonicaid/fiware-connectors), `flume/` section.

Of special interest are:

* The `README.md`, containing an expanded version of this guide.
* The `doc/` section, containing advanced and detailed documentation.

Apache Flume [web site](http://flume.apache.org/) is also another interesting pointer to learn more about the base technlogy used by Cygnus.

##Contact
Fermín Galán Marquez (fermin.galanmarquez@telefonica.com)
<br>
Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com)