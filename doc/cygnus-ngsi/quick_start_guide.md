# Cygnus Quick Start Guide
This quick start overviews the steps a newbie programmer will have to follow in order to get familiar with Cygnus and its basic functionality. For more detailed information, please refer to the [README](https://github.com/telefonicaid/fiware-cygnus/blob/master/README.md); the [Installation and Administration Guide](./installation_and_administration_guide/introduction.md), the [User and Programmer Guide](user_and_programmer_guide/introduction.md) and the [Flume Extensions Catalogue](flume_extensions_catalogue/introduction.md) fully document Cygnus.

## Installing Cygnus
Open a terminal and simply configure the FIWARE release repository:
```
sudo wget -P /etc/yum.repos.d/ https://nexus.lab.fiware.org/repository/raw/public/repositories/el/7/x86_64/fiware-release.repo
```
Use your applications manager in order to install the latest version of Cygnus (CentOS/RedHat example):
```
sudo yum install cygnus-ngsi
```
**IMPORTANT NOTE:**

Cygnus, as it is based on Apache Flume, needs Java SDK is installed. If not yet installed, do it right now:

```
$ sudo yum install java-1.7.0-openjdk-devel
```

Remember to export the JAVA_HOME environment variable. In the case you have installed Java SDK right now as shown above, this would be as:

```
$ export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk.x86_64
```

In order to do it permanently, edit `/root/.bash_profile` (root user) or `/etc/profile` (other users).

## Configuring a test agent

This kind of agent is the simplest one you can configure with Cygnus. It is based on a standard `HTTPSource`, a `MemoryChannel` and a `NGSITestSink`. Don't worry about the configuration details, specially those about the source; simply think on a Http listener waiting for Orion notifications on port TCP/5050 and sending that notifications in the form of Flume events to a testing purpose sink that will not really persist anything in a third-party storage, but will log the notified context data.

(1) Create and edit a `/usr/cygnus/conf/agent_test.conf` file (as a sudoer):

```
cygnus-ngsi.sources = http-source
cygnus-ngsi.sinks = test-sink
cygnus-ngsi.channels = test-channel

cygnus-ngsi.sources.http-source.channels = test-channel
cygnus-ngsi.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnus-ngsi.sources.http-source.port = 5050
cygnus-ngsi.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
cygnus-ngsi.sources.http-source.handler.notification_target = /notify
cygnus-ngsi.sources.http-source.handler.default_service = def_serv
cygnus-ngsi.sources.http-source.handler.default_service_path = def_servpath
cygnus-ngsi.sources.http-source.handler.events_ttl = 2
cygnus-ngsi.sources.http-source.interceptors = ts gi
cygnus-ngsi.sources.http-source.interceptors.ts.type = timestamp
cygnus-ngsi.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
cygnus-ngsi.sources.http-source.interceptors.gi.grouping_rules_conf_file = /Applications/apache-flume-1.4.0-bin/conf/grouping_rules.conf

cygnus-ngsi.channels.test-channel.type = memory
cygnus-ngsi.channels.test-channel.capacity = 1000
cygnus-ngsi.channels.test-channel.transactionCapacity = 100

cygnus-ngsi.sinks.test-sink.channel = test-channel
cygnus-ngsi.sinks.test-sink.type = com.telefonica.iot.cygnus.sinks.NGSITestSink
cygnus-ngsi.sinks.test-sink.batch_size = 1
cygnus-ngsi.sinks.test_sink.batch_timeout = 10
```

(2) Start Cygnus from the command line; Cygnus will be printing logs on the standard output (i.e. your screen):

```
$ /usr/cygnus/bin/cygnus-flume-ng agent --conf /usr/cygnus/conf/ -f /usr/cygnus/conf/agent_test.conf -n cygnus-ngsi -Dflume.root.logger=INFO,console
```

(3) Open a new terminal (since Cygnus should be printing logs on the standard output of the first one) and create and edit somewhere a `notification.sh` file:

```
URL=$1

curl $URL -v -s -S --header 'Content-Type: application/json; charset=utf-8' --header 'Accept: application/json' --header "Fiware-Service: qsg" --header "Fiware-ServicePath: testsink" -d @- <<EOF
{
	"subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
	"originator" : "localhost",
	"contextResponses" : [
		{
			"contextElement" : {
				"attributes" : [
					{
						"name" : "temperature",
						"type" : "float",
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

This script will emulate the sending of an Orion notification to the URL endpoint passed as argument. The above notification is about and entity named `Room1` of type `Room` belonging to the FIWARE service `qsg` and the FIWARE service path `testsink`; it has a single attribute named `temperature` of type `float`.

(4) Give execution permissions to `notification.sh` and run it, passing as argument the URL of the listening `HTTPSource`:

```
$ chmod a+x notification.sh
$ ./notification.sh http://localhost:5050/notify
```

(5) Look at the logs printed by Cygnus. You should find something like this, which means the notification was successfully received, processed as a Flume event, put into the channel and taken by the test sink in order to print it!


```
time=2015-12-10T14:31:49.389CET | lvl=INFO | trans=1449754282-690-0000000000 | svc=def_serv | subsvc=def_serv_path | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[150] : Starting transaction (1449754282-690-0000000000)
time=2015-12-10T14:31:49.392CET | lvl=INFO | trans=1449754282-690-0000000000 | svc=def_serv | subsvc=def_serv_path | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[232] : Received data ({  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "attributes" : [          {            "name" : "temperature",            "type" : "centigrade",            "value" : "26.5"          }        ],        "type" : "Room",        "isPattern" : "false",        "id" : "Room1"      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2015-12-10T14:31:49.394CET | lvl=INFO | trans=1449754282-690-0000000000 | svc=def_serv | subsvc=def_serv_path | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[255] : Event put in the channel (id=1491400742, ttl=10)
time=2015-12-10T14:31:49.485CET | lvl=INFO | trans=1449754282-690-0000000000 | svc=def_serv | subsvc=def_serv_path | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSITestSink[176] : [test-sink] Persisting data at NGSITestSink. Data (Processing event={Processing headers={recvTimeTs=1449754309394, fiwareService=def_serv, fiwareServicePath=def_serv_path, destinations=room1_room}, Processing context element={id=Room1, type=Room}, Processing attribute={name=temperature, type=centigrade, value="26.5", metadata=[]}})
time=2015-12-10T14:31:49.486CET | lvl=INFO | trans=1449754282-690-0000000000 | svc=def_serv | subsvc=def_serv_path | function=process | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[178] : Finishing transaction (1449754282-690-0000000000)

```

## Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](german.torodelvalle@telefonica.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](http://stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.
