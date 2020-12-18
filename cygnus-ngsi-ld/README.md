# <a name="top"></a>Cygnus NGSI-LD
Content:

* [Welcome to Cygnus NGSI-LD](#section1)
* [Basic operation](#section2)
    * [Hardware requirements](#section2.1)
    * [Installation ](#section2.2)
    * [Configuration](#section2.3)
    * [Running](#section2.4)
    * [Unit testing](#section2.5)
    * [e2e testing](#section2.6)
    * [Management API overview](#section2.7)
* [Advanced topics and further reading](#section3)
* [Features summary](#section4)
* [Reporting issues and contact information](#section5)

## <a name="section1"></a>Welcome to Cygnus NGSI
Cygnus NGSI-LD is a connector in charge of persisting updates on context information managed through a Context Broker supporting NGSI-LD  context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using Cygnus NGSI-LD.

Cygnus NGSI-LD uses the subscription/notification feature of any NGSI-LD Context broker like [Orion-LD](https://github.com/FIWARE/context.Orion-LD) . A subscription is made in the Context Broker on behalf of Cygnus NGSI-LD, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

Internally, Cygnus NGSI-LD is based on [Apache Flume](http://flume.apache.org/), which is used through **cygnus-common** and which Cygnus NGSI-LD depends on. In fact, Cygnus NGSI-LD is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Orion context data in:

* [PostgreSQL](http://www.postgresql.org/), the well-know relational database manager.
* [PostGIS](http://postgis.net/), a spatial database extender for PostgreSQL object-relational database.
* [CKAN](http://ckan.org/), an Open Data platform.
You may consider to visit [Cygnus NGSI-LD Quick Start Guide](../doc/cygnus-ngsi-ld/quick_start_guide.md) before going deep into the details.

[Top](#top)

## <a name="section2"></a>Basic operation
### <a name="section2.1"></a>Hardware requirements
* RAM: 1 GB, specially if abusing of the batching mechanism.
* HDD: A few GB may be enough unless the channel types are configured as `FileChannel` type.

[Top](#top)

### <a name="section2.2"></a>Installation 
Please visit the visit [Cygnus NGSI-LD Installation Guide](../doc/cygnus-ngsi-ld/installation_and_administration_guide/introduction.md)

Please observe, as part of the installation process, cygnus-common is installed too.

[Top](#top)

### <a name="section2.3"></a>Configuration
Cygnus NGSI-LD is a tool with a high degree of configuration required for properly running it. The reason is the configuration describes the Flume-based agent chosen to be run.

So, the starting point is choosing the internal architecture of the Cygnus NGSI-LD agent. Let's assume the simplest one:

```
      +-------+
      |   NGSI|
      |   Rest|
      |Handler|
+-------------+    +----------------+    +--------------------+
| http source |----| memory channel |----| NGSIPostgreSQLSink |
+-------------+    +----------------+    +--------------------+
```

Attending to the above architecture, the content of `/usr/cygnus/conf/agent_1.conf` will be:

```
cygnusagent.sources = http-source
cygnusagent.sinks = test-sink
cygnusagent.channels = test-channel

cygnusagent.sources.http-source.channels = test-channel
cygnusagent.sources.http-source.type = http
cygnusagent.sources.http-source.port = 5050
cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
cygnusagent.sources.http-source.handler.notification_target = /notify
cygnusagent.sources.http-source.handler.default_service = def_serv
cygnusagent.sources.http-source.handler.default_service_path = /def_servpath
cygnusagent.sources.http-source.handler.events_ttl = 10
cygnusagent.sources.http-source.interceptors = ts gi
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = /usr/cygnus/conf/grouping_rules.conf

cygnusagent.channels.test-channel.type = memory
cygnusagent.channels.test-channel.capacity = 1000
cygnusagent.channels.test-channel.transactionCapacity = 100
```

Check the [Installation and Administration Guide](../doc/cygnus-ngsi-ld/installation_and_administration_guide/introduction.md) for configurations involving real data storages.

In addition, a `/usr/cygnus/conf/cygnus_instance_1.conf` file must be created if we want to run Cygnus NGSI-LD as a service (see next section):

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

### <a name="section2.4"></a>Running
Cygnus NGSI-LD can be run as a service by simply typing:

    $ (sudo) service cygnus start

Logs are written in `/var/log/cygnus/cygnus.log`, and the PID of the process will be at `/var/run/cygnus/cygnus_1.pid`.

[Top](#top)

### <a name="section2.5"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus NGSI sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus/cygnus-ngsi
    $ mvn test

[Top](#top)

### <a name="section2.6"></a>e2e testing
Cygnus NGSI-LD works by receiving NGSI-LD-like notifications, which are finally persisted. In order to test this, you can run any of the notification scripts located in the [resources folder](./resources/ngsi-examples) of this repo, which emulate certain notification types.

```
$ ./notification-json-simple.sh http://localhost:5050/notify myservice 
*   Trying ::1...
* Connected to localhost (::1) port 5050 (#0)
> POST /notify HTTP/1.1
> Host: localhost:5050
> Content-Type: application/json
> Accept: application/json
> User-Agent: orion/0.10.0
> Fiware-Service: myservice
> Content-Length: 460
>
* upload completely sent off: 460 out of 460 bytes
< HTTP/1.1 200 OK
< Transfer-Encoding: chunked
< Server: Jetty(6.1.26)
<
* Connection #0 to host localhost left intact
```

Or you can connect a real NGSI-LD source such as [Orion-LD Context Broker](https://github.com/FIWARE/context.Orion-LD).

[Top](#top)

### <a name="section2.7"></a>Management API overview
Run the following `curl` in order to get the version (assuming Cygnus NGSI-LD runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/version"
{
    "success": "true",
    "version": "0.12.0_SNAPSHOT.52399574ea8503aa8038ad14850380d77529b550"
}
```

Run the following `curl` in order to get certain Flume components statistics (assuming cygus-ngsi-ld runs on `localhost`):

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
                "name": "postgresql-sink",
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

## <a name="section3"></a>Advanced topics and further reading
Detailed information regarding cygus-ngsi can be found in the [Installation and Administration Guide](../doc/cygnus-ngsi-ld/installation_and_administration_guide/introduction.md), the [User and Programmer Guide](../doc/cygnus-ngsi-ld/user_and_programmer_guide/introduction.md) and the [Flume extensions catalogue](../doc/cygnus-ngsi-ld/flume_extensions_catalogue/introduction.md). The following is just a list of shortcuts regarding the most popular topics:

* [Installation with docker](../doc/cygnus-ngsi-ld/installation_and_administration_guide/install_with_docker.md). An alternative to RPM installation, docker is one of the main options when installing FIWARE components.
* [Installation from sources](../doc/cygnus-ngsi-ld/installation_and_administration_guide/install_from_sources.md). Sometimes you will need to install from sources, particularly when some of the dependencies must be modified.

[Top](#top)

## <a name="section4"></a>Features summary
<table>
  <tr><th>Component</th><th>Feature</th><th>From version</th></tr>
  <tr><td>NGSIPostgreSQLSink</td><td>First implementation</td><td>2.1.0</d></tr>
  <tr><td>NGSIPostgisLSink</td><td>First implementation</td><td>2.1.0</d></tr>
  <tr><td rowspan="4">NGSICKANSink</td><td>First implementation</td><td>2.1.0</td></tr>
  <tr><td rowspan="9">All sinks</td><td>Events TTL</td><td>2.1.0</td></tr>
  <tr><td>Infinite events TTL</td><td>2.1.0</td></tr>
  <tr><td>Data model configuration</td><td>2.1.0</td></tr>
  <tr><td>enable/disable forced lower case</td><td>2.1.0</td></tr>
  <tr><td>New encoding</td><td>2.1.0</td></tr>
  <tr><td>Agents and instances</td><td>2.1.0</td></tr>
  <tr><td>Logs</td><td>2.1.0</td></tr>
</table>

[Top](#top)

## <a name="section5"></a>Reporting issues and contact information
Any doubt you may have, please refer to the [Cygnus Core Team](../doc/cygnus-ngsi-ld/flume_extensions_catalogue/issues_and_contact.md).

[Top](#top)
