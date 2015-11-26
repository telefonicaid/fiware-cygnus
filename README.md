#<a name="top"></a>Cygnus
[![license badge](https://img.shields.io/badge/license-AGPL-blue.svg)](https://opensource.org/licenses/AGPL-3.0)

* [Welcome to Cygnus](#section1)
* [Basic operation](#section2)
    * [Installation (CentOS/RedHat)](#section2.1)
    * [Configuration](#section2.2)
    * [Running](#section2.3)
    * [Testing](#section2.4)
* [Advanced topics and further reading](#section3)
* [Licensing](#section4)
* [Reporting issues and contact information](#section5)

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
* [STH](https://github.com/telefonicaid/IoT-STH), a Short-Term Historic database built on top of MongoDB.
* [Kafka](http://kafka.apache.org/), the publish-subscribe messaging broker.
* [DynamoDB](https://aws.amazon.com/dynamodb/), a cloud-based NoSQL database by [Amazon Web Services](https://aws.amazon.com/).

You may consider to visit [Cygnus Quick Start Guide](doc/quick_start_guide.md) before going deep into the details.

[Top](#top)

##<a name="section2"></a>Basic operation
###<a name="section2.1"></a>Installation (CentOS/RedHat)
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

###<a name="section2.2"></a>Configuration
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

Check the [Installation and Administraion Guide](./installation_and_administration_guide/introduction.md) for configurations involving real data storages such as HDFS, MySQL, etc.

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

###<a name="section2.3"></a>Running
Cygnus can be run as a service by simply typing:

    $ service cygnus start

Logs are written in `/var/log/cygnus/cygnus.log`, and the PID of the process will be at `/var/run/cygnus/cygnus_1.pid`.

[Top](#top)

###<a name="section2.4"></a>Testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ mvn test

[Top](#top)

##<a name="section3"></a>Advanced topics and further reading
Detailed information regarding Cygnus can be found in the [Installation and Administration Guide](./installation_and_administration_guide/introduction.md), the [User and Programmer Guide](./user_and_programmer_guide/introduction.md) and the [Flume extensions catalogue](./flume_extensions_catalogue/introduction.md). The following is just a list of shortcuts regarding the most popular topics:

* [Installation with docker](doc/installation_and_administration_guide/install_with_docker). An alternative to RPM installation, docker is one of the main options when installing FIWARE components.
* [Installation from sources](doc/installation_and_administration_guide/install_from_sources.md). Sometimes you will need to install from sources, particularly when some of the dependencies must be modified, e.g. the `hadoop-core` libraries.
* [Running as a process](doc/installation_and_administration_guide/running_as_process.md). Running Cygnus as a process is very useful for testing and debuging purposes.
* [Management Interface](doc/installation_and_administration_guide/management_interface.md). From Cygnus 0.5 there is a REST-based management interface for administration purposes.
* [Pattern-based grouping](doc/). Designed as a Flume interceptor, this feature <i>overwrites</i> the default behaviour when building the `destination` header within the Flume events. It creates specific `fiware-servicePath` per notified context element as well.
* [Multi-instance](doc/installation_and_administration_guide/configuration.md). Several instances of Cygnus can be run as a service.
* [Performance tips](doc/installation_and_administration_guide/performance_tips.md). If you are experiencing performance issues or want to improve your statistics, take a look on how to obtaint the best from Cygnus.
* [New sink development](doc/user_and_programmer_guide/adding_new_sink.md). Addressed to those developers aiming to contribute to Cygnus with new sinks.

[Top](#top)

##<a name="section4"></a>Licensing
Cygnus is licensed under Affero General Public License (GPL) version 3. You can find a [copy of this license in the repository](./LICENSE).

[Top](#top)

##<a name="section5"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](german.torodelvalle@telefonica.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.

[Top](#top)
