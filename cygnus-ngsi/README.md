#<a name="top"></a>Cygnus
[![License badge](https://img.shields.io/badge/license-AGPL-blue.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Documentation badge](https://readthedocs.org/projects/fiware-cygnus/badge/?version=latest)](http://fiware-cygnus.readthedocs.org/en/latest/?badge=latest)
[![Docker badge](https://img.shields.io/docker/pulls/fiware/cygnus.svg)](https://hub.docker.com/r/fiware/cygnus/)
[![Support badge]( https://img.shields.io/badge/support-sof-yellowgreen.svg)](http://stackoverflow.com/questions/tagged/fiware-cygnus)
[![Support badge]( https://img.shields.io/badge/support-askbot-yellowgreen.svg)](https://ask.fiware.org/questions/scope%3Aall/tags%3Acygnus/)

* [Welcome to Cygnus](#section1)
* [Basic operation](#section2)
    * [Hardware requirements](#section2.1)
    * [Installation (CentOS/RedHat)](#section2.2)
    * [Configuration](#section2.3)
    * [Running](#section2.4)
    * [Unit testing](#section2.5)
    * [e2e testing](#section2.6)
    * [Management API overview](#section2.7)
* [Add a new image to the docker hub](#section3)
* [Advanced topics and further reading](#section4)
* [Features summary](#section5)
* [Licensing](#section6)
* [Reporting issues and contact information](#section7)

##<a name="section1"></a>Welcome to Cygnus
This project is part of [FIWARE](http://fiware.org), being part of the [Cosmos](http://catalogue.fiware.org/enablers/bigdata-analysis-cosmos) Ecosystem.

Cygnus is a connector in charge of persisting [Orion](https://github.com/telefonicaid/fiware-orion) context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using Cygnus.

Cygnus uses the subscription/notification feature of Orion. A subscription is made in Orion on behalf of Cygnus, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/). In fact, Cygnus is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Orion context data in:

* [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.
* [MySQL](https://www.mysql.com/), the well-know relational database manager.
* [CKAN](http://ckan.org/), an Open Data platform.
* [MongoDB](https://www.mongodb.org/), the NoSQL document-oriented database.
* [FIWARE Comet](https://github.com/telefonicaid/IoT-STH), a Short-Term Historic database built on top of MongoDB.
* [Kafka](http://kafka.apache.org/), the publish-subscribe messaging broker.
* [DynamoDB](https://aws.amazon.com/dynamodb/), a cloud-based NoSQL database by [Amazon Web Services](https://aws.amazon.com/).

You may consider to visit [Cygnus Quick Start Guide](doc/quick_start_guide.md) before going deep into the details.

[Top](#top)

##<a name="section2"></a>Basic operation
###<a name="section2.1"></a>Hardware requirements
* RAM: 1 GB, specially if abusing of the batching mechanism.
* HDD: A few GB may be enough unless the channel types are configured as `FileChannel` type.

[Top](#top)

###<a name="section2.2"></a>Installation (CentOS/RedHat)
Simply configure the FIWARE repository if not yet configured:

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
    gpgcheck=0
    enabled=1
    EOL

And use your applications manager in order to install the latest version of Cygnus:

    $ yum install cygnus

The above will install Cygnus in `/usr/cygnus/`.

[Top](#top)

###<a name="section2.3"></a>Configuration
Cygnus is a tool with a high degree of configuration required for properly running it. The reason is the configuration describes the Flume-based agent choosen to be run.

So, the starting point is choosing the internal architecture of the Cygnus agent. Let's assume the simplest one:

```
      +-------+
      |  Orion|
      |   REST|
      |Handler|
+-------------+    +----------------+    +---------------+
| http source |----| memory channel |----| OrionTestSink |
+-------------+    +----------------+    +---------------+
```

Attending to the above architecture, the content of `/usr/cygnus/conf/cygnus_1.conf` will be:

```
cygnusagent.sources = http-source
cygnusagent.sinks = test-sink
cygnusagent.channels = test-channel

cygnusagent.sources.http-source.channels = test-channel
cygnusagent.sources.http-source.type = http
cygnusagent.sources.http-source.port = 5050
cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.OrionRestHandler
cygnusagent.sources.http-source.handler.notification_target = /notify
cygnusagent.sources.http-source.handler.default_service = def_serv
cygnusagent.sources.http-source.handler.default_service_path = def_servpath
cygnusagent.sources.http-source.handler.events_ttl = 10
cygnusagent.sources.http-source.interceptors = ts gi
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.GroupingInterceptor$Builder
cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = /usr/cygnus/conf/grouping_rules.conf

cygnusagent.channels.test-channel.type = memory
cygnusagent.channels.test-channel.capacity = 1000
cygnusagent.channels.test-channel.transactionCapacity = 100
```

Check the [Installation and Administraion Guide](./doc/installation_and_administration_guide/introduction.md) for configurations involving real data storages such as HDFS, MySQL, etc.

In addition, a `/usr/cygnus/conf/cygnus_instance_1.conf` file must be created if we want to run Cygnus as a service (see next section):

```
CYGNUS_USER=cygnus
CONFIG_FOLDER=/usr/cygnus/conf
CONFIG_FILE=/usr/cygnus/conf/agent_1.conf
AGENT_NAME=cygnusagent
LOGFILE_NAME=cygnus.log
ADMIN_PORT=8081
POLLING_INTERVAL=30
```

[Top](#top)

###<a name="section2.4"></a>Running
Cygnus can be run as a service by simply typing:

    $ service cygnus start

Logs are written in `/var/log/cygnus/cygnus.log`, and the PID of the process will be at `/var/run/cygnus/cygnus_1.pid`.

[Top](#top)

###<a name="section2.5"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ mvn test

[Top](#top)

###<a name="section2.6"></a>e2e testing
Cygnus works by receiving NGSI-like notifications, which are finaly persisted. In order to test this, you can run any of the notificacion scripts located in the [resources folder](./resources/ngsi-examples) of this repo, which emulate certain notification types.

```
$ ./notification-json-simple.sh http://localhost:5050/notify myservice myservicepath
*   Trying ::1...
* Connected to localhost (::1) port 5050 (#0)
> POST /notify HTTP/1.1
> Host: localhost:5050
> Content-Type: application/json
> Accept: application/json
> User-Agent: orion/0.10.0
> Fiware-Service: myservice
> Fiware-ServicePath: myservicepath
> Content-Length: 460
>
* upload completely sent off: 460 out of 460 bytes
< HTTP/1.1 200 OK
< Transfer-Encoding: chunked
< Server: Jetty(6.1.26)
<
* Connection #0 to host localhost left intact
```

Or you can connect a real NGSI source such as [Orion Context Broker](https://github.com/telefonicaid/fiware-orion). Please, check the [User and Programmer Guide](./doc/user_and_programmer_guide/connecting_orion.md) for further details.

[Top](#top)

###<a name="section2.7"></a>Management API overview
Run the following `curl` in order to get the version (assuming Cygnus runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/version"
{
    "success": "true",
    "version": "0.12.0_SNAPSHOT.52399574ea8503aa8038ad14850380d77529b550"
}
```

Run the following `curl` in order to get certain Flume components statistics (assuming Cygnus runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/stats" | python -m json.tool
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   489  100   489    0     0  81500      0 --:--:-- --:--:-- --:--:-- 97800
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

Many other operations, like getting/putting/updating/deleting the grouping rules can be found in Management Interface [documentation](./doc/installation_and_administration_guide/management_interface.md).

[Top](#top)

##<a name="section3"></a>Add a new image to the docker hub

Everytime we release a new version (`<release version>`), please run the following commands (provided you have access to the docker hub of `fiware-cygnus`)

    docker-compose -f ./docker/0.compose.jar-compiler.yml -p cygnus run --rm compiler
    docker build -f ./docker/Dockerfile -t fiware/cygnus:<release version> .
    docker tag fiware/cygnus:<release version> fiware/cygnus:latest
    docker login
    docker push

This will create an image with that version, tag it as the latest, and push this image into docker hub, both with its release version (eg: `0.13`), as with the `latest` tag.

[Top](#top)

##<a name="section4"></a>Advanced topics and further reading
Detailed information regarding Cygnus can be found in the [Installation and Administration Guide](./doc/installation_and_administration_guide/introduction.md), the [User and Programmer Guide](./doc/user_and_programmer_guide/introduction.md) and the [Flume extensions catalogue](./doc/flume_extensions_catalogue/introduction.md). The following is just a list of shortcuts regarding the most popular topics:

* [Installation with docker](doc/installation_and_administration_guide/install_with_docker). An alternative to RPM installation, docker is one of the main options when installing FIWARE components.
* [Installation from sources](doc/installation_and_administration_guide/install_from_sources.md). Sometimes you will need to install from sources, particularly when some of the dependencies must be modified, e.g. the `hadoop-core` libraries.
* [Running as a process](doc/installation_and_administration_guide/running_as_process.md). Running Cygnus as a process is very useful for testing and debuging purposes.
* [Management Interface](doc/installation_and_administration_guide/management_interface.md). From Cygnus 0.5 there is a REST-based management interface for administration purposes.
* [Pattern-based grouping](doc/). Designed as a Flume interceptor, this feature <i>overwrites</i> the default behaviour when building the `destination` header within the Flume events. It creates specific `fiware-servicePath` per notified context element as well.
* [Multi-instance](doc/installation_and_administration_guide/configuration.md). Several instances of Cygnus can be run as a service.
* [Performance tips](doc/installation_and_administration_guide/performance_tips.md). If you are experiencing performance issues or want to improve your statistics, take a look on how to obtaint the best from Cygnus.
* [New sink development](doc/user_and_programmer_guide/adding_new_sink.md). Addressed to those developers aiming to contribute to Cygnus with new sinks.

[Top](#top)

##<a name="section5"></a>Features summary
<table>
  <tr><th>Component</th><th>Feature</th><th>From version</th></tr>
  <tr><td rowspan="11">OrionHDFSSink</td><td>First implementation</td><td>0.1.0</td></tr>
  <tr><td>Multiple HDFS endpoint setup</td><td>0.4.1</td></tr>
  <tr><td>Kerberos support</td><td>0.7.0</td></tr>
  <tr><td>OAuth2 support</td><td>0.8.2</td></tr>
  <tr><td>CSV support</td><td>0.9.0</td></tr>
  <tr><td>HiveServer2 support</td><td>0.9.0</td></tr>
  <tr><td>Table type select</td><td>0.9.0</td></tr>
  <tr><td>enable/disable Hive</td><td>0.10.0</td></tr>
  <tr><td>HDFSBackendImplBinary</td><td>0.10.0</td></tr>
  <tr><td>Batching mechanism</td><td>0.10.0</td></tr>
  <tr><td>Per-user Hive databases</td><td>0.12.0</td></tr>
  <tr><td rowspan="3">OrionCKANSink</td><td>First implementation</td><td>0.2.0</td></tr>
  <tr><td>Enable SSL</td><td>0.4.2</td></tr>
  <tr><td>Batching mechanism</td><td>0.11.0</td></tr>
  <tr><td>OrionDynamoDBSink</td><td>First implementation</td><td>0.11.0</td></tr>
  <tr><td rowspan="2">OrionKafkaSink</td><td>First implementation</td><td>0.9.0</td></tr>
  <tr><td>Batching mechanims</td><td>0.11.0</td></tr>
  <tr><td rowspan="5">OrionMongoSink</td><td>First implementation</td><td>0.8.0</td></tr>
  <tr><td>Hash based collections</td><td>0.8.1</td></tr>
  <tr><td>Batching support</td><td>0.12.0</td></tr>
  <tr><td>Time and size-based data management policies</td><td>0.13.0</td></tr>
  <tr><td>Ignore white space-based attribute values</td><td>0.14.0</td></tr>
  <tr><td rowspan="2">OrionMySQLSink</td><td>First implementation</td><td>0.2.0</td></tr>
  <tr><td>Batching mechanism</td><td>0.10.0</td></tr>
  <tr><td rowspan="7">OrionSTHSink</td><td>First implementation</td><td>0.8.0</td></tr>
  <tr><td>Hash based collections</td><td>0.8.1</td></tr>
  <tr><td>TimeInstant metadata as reception time</td><td>0.12.0</td></tr>
  <tr><td>Batching mechanism</td><td>0.13.0</td></tr>
  <tr><td>Time and size-based data management policies</td><td>0.13.0</td></tr>
  <tr><td>String-based aggregation (occurrences)</td><td>0.14.0</td></tr>
  <tr><td>Ignore white space-based attribute values</td><td>0.14.0</td></tr>
  <tr><td>OrionPostgreSQLSink</td><td>First implementation</td><td>0.12.0</d></tr>
  <tr><td rowspan="2">OrionTestSink</td><td>First implementation</td><td>0.7.0</td></tr>
  <tr><td>Batching mechanism</td><td>0.12.0</td></tr>
  <tr><td rowspan="7">All sinks</td><td>Events TTL</td><td>0.4.1</td></tr>
  <tr><td>Pattern-based grouping</td><td>0.5.0</td></tr>
  <tr><td>Infinite events TTL</td><td>0.7.0</td></tr>
  <tr><td>enable/disable Grouping Rules</td><td>0.9.0</td></tr>
  <tr><td>Data model configuration</td><td>0.12.0</td></tr>
  <tr><td>enable/disable forced lower case</td><td>0.13.0</td></tr>
  <tr><td>Per batch TTL</td><td>0.13.0</td></tr>
  <tr><td rowspan="6">Management Interface</td><td>GET /version</td><td>0.5.0</td></tr>
  <tr><td>GET /stats</td><td>0.13.0</td></tr>
  <tr><td>GET /groupingrules</td><td>0.13.0</td></tr>
  <tr><td>POST /groupingrules</td><td>0.13.0</td></tr>
  <tr><td>PUT /groupingrules</td><td>0.13.0</td></tr>
  <tr><td>DELETE /groupingrules</td><td>0.13.0</td></tr>
  <tr><td rowspan="7">General</td><td>RPM building framework</td><td>0.3.0</td></tr>
  <tr><td>TDAF-like logs</td><td>0.4.0</td></tr>
  <tr><td>RoundRobinChannelSelector</td><td>0.6.0</td></tr>
  <tr><td>Multi-instances</td><td>0.7.0</td></tr>
  <tr><td>start/stop/status per instance</td><td>0.7.1</td></tr>
  <tr><td>Ordered death of Cygnus</td><td>0.8.0</td></tr>
  <tr><td>Polling interval parameter</td><td>0.8.0</td></tr>
</table>

[Top](#top)

##<a name="section6"></a>Licensing
Cygnus is licensed under Affero General Public License (GPL) version 3. You can find a [copy of this license in the repository](./LICENSE).

[Top](#top)

##<a name="section7"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](mailto:german.torodelvalle@telefonica.com) **[Contributor]**
    * [herman.junge@telefonica.com](mailto:chpdg42@gmail.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.

[Top](#top)
