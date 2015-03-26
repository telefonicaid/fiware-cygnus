#<a name="top"></a>Cygnus

* [What is Cygnus](#section1)
* [Design](#section2)
* [Functionality explained (Json notification example)](#section3)
  * [OrionHDFSSink persistence](#section3.1)
  * [OrionCKANSink persistence](#section3.2)
  * [OrionMySQLSink persistence](#section3.3)
* [Functionality explained (XML notification example)](#section4)
* [Installing Cygnus](#section5)
  * [RPM install (recommended)](#section5.1)
  * [Installing from sources (advanced)](#section5.2)
* [Cygnus configuration](#section6)
  * [`cygnus_instance_<id>.conf`](#section6.1)
  * [`agent_<id>.conf`](#section6.2)
* [Running Cygnus](#section7)
  * [As a service (recommended)](#section7.1)
  * [As standalone application (advanced)](#section7.2)
* [Orion subscription](#section8)
* [Logs](#section9)
  * [log4j configuration](#section9.1)
  * [Message types](#section9.2)
* [Management interface](#section10)
* [Contact](#section11)

##<a name="section1"></a>What is Cygnus

Cygnus is a connector in charge of persisting [Orion](https://github.com/telefonicaid/fiware-orion) context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using Cygnus.

Cygnus uses the subscription/notification feature of Orion. A subscription is made in Orion on behalf of Cygnus, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/). In fact, Cygnus is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Orion context data in:

* [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.
* [MySQL](https://www.mysql.com/), the well-know relational database manager.
* [CKAN](http://ckan.org/), an Open Data platform.

Cygnus is a (conceptual) derivative work of [ngsi2cosmos](https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos).

[Top](#top)

##<a name="section2"></a>Design

All the details about Flume can be found at [flume.apache.org](http://flume.apache.org/), but, as a reminder, some concepts will be explained here:

* A Flume source is an agent gathering event data from the real source (Twitter stream, a notification system, etc.), either by polling the source or listening for incoming pushes of data. Gathered data is sent to a Flume channel.
* A Flume channel is a passive store (implemented by means of a file, memory, etc.) that holds the event until it is consumed by the Flume sink.
* A Flume sink connects with the final destination of the data (a local file, HDFS, a database, etc.), taking events from the channel and consuming them (processing and/or persisting it).

There exists a wide collection of already developed sources, channels and sinks. The Flume-based connector, also called Cygnus, development extends that collection by adding:

* **OrionRestHandler**. A custom HTTP source handler for the default HTTP source. The existing HTTP source behaviour can be governed depending on the request handler associated to it in the configuration. In this case, the custom handler takes care of the method, the target and the headers (specially the Content-Type one) within the request, cheking everything is according to the expected [request format](https://forge.fi-ware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). This allows for a certain degree of control on the incoming data. The header inspection step allows for a content type identification as well by sending, together with the data, the Content-Type header.
* **OrionHDFSSink**. A custom sink that persists Orion content data in a HDFS deployment. There already exists a native Flume HDFS sink persisting each event in a new file, but this is not suitable for Cygnus. Within Cygnus, the data coming from Orion must be persisted in the Cosmos HDFS in the form of files (a file per entity) containing Json-like lines about the values such entity's attributes have had along time. Several HDFS backends can be used for the data persistence (WebHDFS, HttpFS, Infinity), all of them based on the native WebHDFS REST API from Hadoop.
* **OrionCKANSink**. A custom sink that persists Orion context data in CKAN server instances (see http://docs.ckan.org/en/latest/).
* **OrionMySQLSink**. A custom sink for persisting Orion context data in a MySQL server. Each user owns a database, and each entity is mapped to a table within that database. Tables contain rows about the values such entity's attributes have had along time.

All these new components (OrionRestHandler, OrionHDFSSink, etc) are combined with other native ones included in Flume itself (e.g. HttpSource), with the purpose of implementing the following data flow:

1.  On behalf of Cygnus, subscribe to Orion for certain context information.
2.  Receive from Orion notifications about new update context data; this notification will be handled by the native HttpSource together with the custom OrionRestHandler.
3.  Translate the notification into the Flume event format, and put them into the different sink channels (native memory ones).
4.  For each enabled custom sink (OrionHDFSSink, OrionCKANSink, OrionMySQLSink), get the Flume events from the sink channel and persist the data in the appropriate format.

[Top](#top)

##<a name="section3"></a>Functionality explained (Json notification example)

Let's consider the following notification in Json format coming from an Orion Context Broker instance:

    POST http://localhost:1028/notify
    Content-Length: 492
    User-Agent: orion/0.9.0
    Host: localhost:1028
    Accept: application/xml, application/json
    Content-Type: application/json
    Fiware-Service: my-company-name
    Fiware-ServicePath: /workingrooms/floor4 
    
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
                "value" : "26.5",
                "metadatas": [
                  {
                     "name": "ID",
                     "type": "string",
                     "value": "ground"
                  }
                ]
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

Such a notification is sent by Orion to the default Flume HTTP source, which relies on the developed OrionRestHandler for checking its validity (that it is a POST request, that the target is 'notify' and that the headers are OK), detecting the content type (that it is in Json format), extracting the data (the Json part) and finally creating a Flume event to be put in the channel:

    event={
		body=json_data,
		headers={
			content-type=application/json,
			fiware-service=my_company_name,
			fiware-servicepath=workingrooms_floor4,
			timestamp=1402409899391,
			transactionId=asdfasdfsdfa,
			ttl=10,
			destination=Room1-Room
		}
	}

<b>NOTE: The above is an <i>object representation</i>, not Json data nor any other data format.</b>

Let's have a look on the Flume event headers:

* The <b>content-type</b> header is a replica of the HTTP header. It is needed for the different sinks to know how to parse the event body. In this case it is JSON.
* Note that Orion can include a `Fiware-Service` HTTP header specifying the tenant/organization associated to the notification, which is added to the event headers as well (as `fiware-service`). Since version 0.3, Cygnus is able to support this header, although the actual processing of such tenant/organization depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service specified in the `default_service` configuration property. Please observe that the notified `fiware-service` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* Orion can notify another HTTP header, `Fiware-ServicePath` specifying a subservice within a tenant/organization, which is added to the event headers as well (as `fiware-servicepath`). Since version 0.6, Cygnus is able to support this header, although the actual processing of such subservice depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service path specified in the `default_service_path` configuration property. Please observe that the notified `fiware-servicePath` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* The notification reception time is included in the list of headers (as <b>timestamp</b>) for timestamping purposes in the different sinks. It is added by a native interceptor. See the [doc/design/interceptors.md](doc/design/interceptors.md) document for more details.
* The <b>transactionId</b> identifies a complete Cygnus transaction, starting at the source when the context data is notified, and finishing in the sink, where such data is finally persisted.
* The time-to-live (or <b>ttl</b>) specifies the number of re-injection retries in the channel when something goes wrong while persisting the data. This re-injection mechanism is part of the reliability features of Flume. -1 means inifinite retries.
* The <b>destination</b> headers is used to identify the persistence element within the used storage, i.e. a file in HDFS, a MySQL table or a CKAN resource. This is added by a custom interceptor called `DestinationExtractor` added to the Flume's suite. See the <i>doc/design/interceptors</i> document for more details.

Finally, the channel is a simple MemoryChannel behaving as a FIFO queue, and from where the different sinks extract the events in order to persist them; let's see how:

[Top](#top)

###<a name="section3.1"></a>OrionHDFSSink persistence

This sink persists the data in files, one per each entity, following this entity descriptor format:

    <entityDescriptor>=<entity_id>-<entity_type>.txt

These files are stored under this HDFS path:

    hdfs:///user/<username>/<service>/<servicePath>/<entityDescriptor>/<entityDescriptor>.txt

Usernames allow for specific private HDFS data spaces, and in the current version, it is given by the `cosmos_default_username` parameter that can be found in the configuration. Both the `service` and `servicePath` directories are given by Orion as headers in the notification (`Fiware-Service` and `Fiware-ServicePath` respectively) and sent to the sinks through the Flume event headers (`fiware-service` and `fiware-servicepath` respectively).

More details regarding the naming conventions can be found at [doc/design/naming_convetions.md](doc/design/naming_convetions.md).
    
Within files, Json documents are written following one of these two schemas:

* Fixed 8-field lines: `recvTimeTs`, `recvTime`, `entityId`, `entityType`, `attrName`, `attrType`, `attrValue` and `attrMd`. Regarding `attrValue`, in its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array. Regarding `attrMd`, it contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
*  Two fields per each entity's attribute (one for the value and other for the metadata), plus an additional field about the reception time of the data (`recvTime`). Regarding this kind of persistence, the notifications must ensure a value per each attribute is notified.

In both cases, the files are created at execution time if the file doesn't exist previously to the line insertion. The behaviour of the connector regarding the internal representation of the data is governed through a configuration parameter, `attr_persistence`, whose values can be `row` or `column`.

Thus, by receiving a notification like the one above, being the persistence mode `row` and a `default_user` as the default Cosmos username, then the file named `hdfs:///user/default_user/mycompanyname/workingrooms/floor4/Room1-Room/Room1-Room.txt` (it is created if not existing) will contain a new line such as:

    {"recvTimeTs":"13453464536", "recvTime":"2014-02-27T14:46:21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":"26.5", "attrMd":[{name:ID, type:string, value:ground}]}

On the contrary, being the persistence mode `column`, the file named `hdfs:///user/default_user/mycompanyname/workingrooms/floor4/Room1-Room/Room1-Room.txt` (it is created if not existing) will contain a new line such as:

    {"recvTime":"2014-02-27T14:46:21", "temperature":"26.5", "temperature_md":[{"name":"ID", "type":"string", "value":"ground"}]}

A special particularity regarding HDFS persisted data is the posssibility to exploit such data through Hive, a SQL-like querying system. OrionHDFSSink automatically creates a Hive table (similar to a SQL table) for each persisted entity in the default database, being the name for such tables:

    <username>_<service>_<servicePath>_<entity_descriptor>_[row|column]

Following with the example, by receiving a notification like the one above, and being the persistence mode `row`, the table named `default_user_mycompanyname_workingrooms_floor4_room1_Room_row` will contain a new row such as:

    | recvTimeTs   | recvTime            | entityId | entityType | attrName    | attrType   | attrValue | attrMd                                             |
    |--------------|---------------------|----------|------------|-------------|------------|-----------|----------------------------------------------------|
    | 13453464536  | 2014-02-27T14:46:21 | Room1    | Room       | temperature | centigrade | 26.5      | [{"name":"ID", "type":"string", "value":"ground"}] |

On the contrary, being the persistence mode `column`, the table named `default_user_mycompanyname_workingrooms_floor4_room1_Room_column` will contain a new row such as:

    | recvTime            | temperature | temperature_md                                     | 
    |---------------------|-------------|----------------------------------------------------|
    | 2014-02-27T14:46:21 | 26.5        | [{"name":"ID", "type":"string", "value":"ground"}] |  

[Top](#top)

###<a name="section3.2"></a>OrionCKANSink persistence

This sink persists the data in a [datastore](see http://docs.ckan.org/en/latest/maintaining/datastore.html) in CKAN. Datastores are associated to CKAN resources and as CKAN resources we use the entityId-entityType string concatenation. All CKAN resource IDs belong to the same dataset  (also referred as package in CKAN terms), whose name is specified by the notified `Fiware-ServicePath` header (or by the `default_service_path` property -prefixed by organization name- in the CKAN sink configuration, if such a header is not notified). Datasets belong to single organization, whose name is specified by the notified `Fiware-Service` header (or by the `default_service` property if it is not notified). 

More details regarding the naming conventions can be found at [doc/design/naming_convetions.md](doc/design/naming_convetions.md).

Each datastore, we can find two options:

* Fixed 6-field lines: `recvTimeTs`, `recvTime`, `attrName`, `attrType`, `attrValue` and `attrMd`. Regarding `attrValue`, in its simplest form, this value is just a string, but since Orion 0.11.0 it can be JSON object or JSON array. Regarding `attrMd`, it contains a string serialization of the metadata array for the attribute in JSON (if the attribute hasn't metadata, `null` is inserted).
* Two columns per each entity's attribute (one for the value and other for the metadata), plus an additional field about the reception time of the data (`recvTime`). Regarding this kind of persistence, the notifications must ensure a value per each attribute is notified.

The behaviour of the connector regarding the internal representation of the data is governed through a configuration parameter, `attr_persistence`, whose values can be `row` or `column`.

Thus, by receiving a notification like the one above, and being the persistence mode `row`, the resource `room1-Room` (it is created if not existing), will containt the following row in its datastore:

    | _id | recvTimeTs   | recvTime            | attrName    | attrType   | attrValue | attrMd                                              |
    |-----|--------------|---------------------|-----.-------|------------|-----------|-----------------------------------------------------|
    | i   | 13453464536  | 2014-02-27T14:46:21 | temperature | centigrade | 26.5      | [{"name":"ID", "type":"string", "value":"ground"}]  |

where `i` depends on the number of rows previously inserted.

On the contrary, being the persistence mode `column`, the resource `Room1-Room` (it and its datastore must be created in advance) will contain a new row such as shown below. In this case, an extra column ended with `_md` is added for the metadata.

    | _id | recvTime           | temperature | temperature_md                                     |
    |--------------------------|-------------|----------------------------------------------------|
    | i   |2014-02-27T14:46:21 | 26.5        | [{"name":"ID", "type":"string", "value":"ground"}] |

where `i` depends on the number of rows previously inserted.

In both cases, `row` or `column`, the CKAN organization will be `mycompanyname` and the dataset containing the resource will be `workingrooms_floor4`.

The information stored in the datastore can be accesses as any other CKAN information, e.g. through the web frontend or using the query API, e.g;

    curl -s -S "http://${CKAN_HOST}/api/3/action/datastore_search?resource_id=${RESOURCE_ID}

Each organization/tenant is associated to a CKAN organization.

[Top](#top)

###<a name="section3.3"></a>OrionMySQLSink persistence

Similarly to OrionHDFSSink, a table is considered for each entity in order to store its notified context data, being the name for these tables the following entity descriptor:

    <entity_descriptor>=<servicePath>_<entity_id>_<entity_type>

These tables are stored in databases, one per service, enabling a private data space such as:

    jdbc:mysql:///<service>

Both the `service` and `servicePath` names are given by Orion as headers in the notification (`Fiware-Service` and `Fiware-ServicePath` respectively) and sent to the sinks through the Flume event headers (`fiware-service` and `fiware-servicepath` respectively). 

More details regarding the naming conventions can be found at [doc/design/naming_convetions.md](doc/design/naming_convetions.md).

Within tables, we can find two options:

* Fixed 8-field rows, as usual: `recvTimeTs`, `recvTime`, `entityId`, `entityType`, `attrName`, `attrType`, `attrValue` and `attrMd`. These tables (and the databases) are created at execution time if the table doesn't exist previously to the row insertion. Regarding `attrValue`, in its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array. Regarding `attrMd`, it contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted),
* Two columns per each entity's attribute (one for the value and other for the metadata), plus an addition column about the reception time of the data (`recv_time`). This kind of tables (and the databases) must be provisioned previously to the execution of Cygnus, because each entity may have a different number of attributes, and the notifications must ensure a value per each attribute is notified.

The behaviour of the connector regarding the internal representation of the data is governed through a configuration parameter, `attr_persistence`, whose values can be `row` or `column`.

Thus, by receiving a notification like the one above, and being the persistence mode `row`, the table named `workingrooms_floor4_room1_Room` (it is created if not existing) will contain a new row such as:

    | recvTimeTs   | recvTime            | entityId | entityType | attrName    | attrType   | attrValue | attrMd                                             |
    |--------------|---------------------|----------|------------|-------------|------------|-----------|----------------------------------------------------|
    | 13453464536  | 2014-02-27T14:46:21 | Room1    | Room       | temperature | centigrade | 26.5      | [{"name":"ID", "type":"string", "value":"ground"}] |

On the contrary, being the persistence mode `column`, the table named `workingrooms_floor4_room1_Room` (it must be created in advance) will contain a new row such as:

    | recvTime            | temperature | temperature_md                                     | 
    |---------------------|-------------|----------------------------------------------------|
    | 2014-02-27T14:46:21 | 26.5        | [{"name":"ID", "type":"string", "value":"ground"}] |

Each organization/tenant is associated to a different database.

[Top](#top)

##<a name="section4"></a>Functionality explained (XML notification example)

Cygnus also works with [XML-based notifications](https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE) sent to the connector. The only difference is the event is created by specifying the content type will be XML (in order the notification parser notices it):

    event={
		body=json_data,
		headers={
			content-type=application/xml,
			fiware-service=my_company_name,
			fiware-servicepath=workingrooms_floor4,
			timestamp=1402409899391,
			transactionId=asdfasdfsdfa,
			ttl=10,
			destination=Room1-Room
		}
	}

The key point is the behaviour remains the same than in the Json example: the same file/datastores/tables will be created, and the same data will be persisted within it.

[Top](#top)

##<a name="section5"></a>Installing Cygnus
###<a name="section5.1"></a>RPM install (recommended)
Simply configure the FIWARE repository if not yet configured and use your applications manager in order to install the latest version of Cygnus (CentOS/RedHat example):

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
    gpgcheck=0
    enabled=1
    EOL
    $ yum install cygnus

[Top](#top)

###<a name="section5.2"></a>Installing from sources (advanced)
Please, refer to [this](doc/installation/src_install.md) document if your aim is to install Cygnus from sources.

[Top](#top)

##<a name="section6"></a>Cygnus configuration
Cygnus is configured through two different files:

* A `cygnus_instance_<id>.conf` file addressing all those non Flume parameters, such as the Flume agent name, the specific log file for this instance, the administration port, etc. This configuration file is not necessary if Cygnus is run as a standlalone application (see later), bt it is mandatory if run as a service (see later).
* An `agent_<id>.conf` file addressing all those Flume parameters, i.e. how to configure the different sources, channels, sinks, etc. that compose the Flume agent behind the Cygnus instance. always mandatory.

Please observe there may exist several Cygnus instances identified by `<id>`, which must be the same for both configuration files regarding the same Cygnus instance.

[Top](#top)

###<a name="section6.1"></a>`cygnus_instance_<id>.conf`

The file `cygnus_instance_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/cygnus_instance.conf.template`.

```
# The OS user that will be running Cygnus. Note this must be `root` if you want to run cygnus in a privileged port (<1024), either the admin port or the port in which Cygnus receives Orion notifications
CYGNUS_USER=cygnus
# Which is the config folder
CONFIG_FOLDER=/usr/cygnus/conf
# Which is the config file
CONFIG_FILE=/usr/cygnus/conf/agent_<id>.conf
# Name of the agent. The name of the agent is not trivial, since it is the base for the Flume parameters naming conventions, e.g. it appears in <AGENT_NAME>.sources.http-source.channels=...
AGENT_NAME=cygnusagent
# Name of the logfile located at /var/log/cygnus. It is important to put the extension '.log' in order to the log rotation works properly
LOGFILE_NAME=cygnus.log
# Administration port. Must be unique per instance
ADMIN_PORT=8081
# Polling interval (seconds) for the configuration reloading
POLLING_INTERVAL=30
```

[Top](#top)

###<a name="section6.2"></a>`agent_<id>.conf`
A typical configuration when using the `HTTPSource`, the `OrionRestHandler`, the `MemoryChannel` and any of the available sinks is shown below. More advanced configurations can be found at [`doc/operation/performance_tuning_tips.md`](doc/operation/performance_tuning_tips.md).

Kerberos authentication enabling in HDFS is described at [`doc/operation/hdfs_kerberos_authentication.md`](doc/operation/hdfs_kerberos_authentication.md). If your HDFS is not using such an authentication method, just set `cygnusagent.sinks.hdfs-sink.krb5_auth` to `false` and forget the rest of the Kerberos part.

The file `agent_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/agent.conf.template`.

```Java
#=============================================
# To be put in APACHE_FLUME_HOME/conf/cygnus.conf
#
# General configuration template explaining how to setup a sink of each of the available types (HDFS, CKAN, MySQL).

#=============================================
# The next tree fields set the sources, sinks and channels used by Cygnus. You could use different names than the
# ones suggested below, but in that case make sure you keep coherence in properties names along the configuration file.
# Regarding sinks, you can use multiple types at the same time; the only requirement is to provide a channel for each
# one of them (this example shows how to configure 3 sink types at the same time). Even, you can define more than one
# sink of the same type and sharing the channel in order to improve the performance (this is like having
# multi-threading).
cygnusagent.sources = http-source
cygnusagent.sinks = hdfs-sink mysql-sink ckan-sink
cygnusagent.channels = hdfs-channel mysql-channel ckan-channel

#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.http-source.channels = hdfs-channel mysql-channel ckan-channel
# source class, must not be changed
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
# listening port the Flume source will use for receiving incoming notifications
cygnusagent.sources.http-source.port = 5050
# Flume handler that will parse the notifications, must not be changed
cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.OrionRestHandler
# URL target
cygnusagent.sources.http-source.handler.notification_target = /notify
# Default service (service semantic depends on the persistence sink)
cygnusagent.sources.http-source.handler.default_service = def_serv
# Default service path (service path semantic depends on the persistence sink)
cygnusagent.sources.http-source.handler.default_service_path = def_servpath
# Number of channel re-injection retries before a Flume event is definitely discarded (-1 means infinite retries)
cygnusagent.sources.http-source.handler.events_ttl = 10
# Source interceptors, do not change
cygnusagent.sources.http-source.interceptors = ts de
# Interceptor type, do not change
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
# Destination extractor interceptor, do not change
cygnusagent.sources.http-source.interceptors.de.type = com.telefonica.iot.cygnus.interceptors.DestinationExtractor$Builder
# Matching table for the destination extractor interceptor, put the right absolute path to the file if necessary
# See the doc/design/interceptors document for more details
cygnusagent.sources.http-source.interceptors.de.matching_table = /usr/cygnus/conf/matching_table.conf

# ============================================
# OrionHDFSSink configuration
# channel name from where to read notification events
cygnusagent.sinks.hdfs-sink.channel = hdfs-channel
# sink class, must not be changed
cygnusagent.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.OrionHDFSSink
# Comma-separated list of FQDN/IP address regarding the Cosmos Namenode endpoints
# If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
cygnusagent.sinks.hdfs-sink.cosmos_host = x1.y1.z1.w1,x2.y2.z2.w2
# port of the Cosmos service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs and free choice for inifinty
cygnusagent.sinks.hdfs-sink.cosmos_port = 14000
# default username allowed to write in HDFS
cygnusagent.sinks.hdfs-sink.cosmos_default_username = cosmos_username
# default password for the default username
cygnusagent.sinks.hdfs-sink.cosmos_default_password = xxxxxxxxxxxxx
# HDFS backend type (webhdfs, httpfs or infinity)
cygnusagent.sinks.hdfs-sink.hdfs_api = httpfs
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.hdfs-sink.attr_persistence = column
# Hive FQDN/IP address of the Hive server
cygnusagent.sinks.hdfs-sink.hive_host = x.y.z.w
# Hive port for Hive external table provisioning
cygnusagent.sinks.hdfs-sink.hive_port = 10000
# Kerberos-based authentication enabling
cygnusagent.sinks.hdfs-sink.krb5_auth = false
# Kerberos username
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
# Kerberos password
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
# Kerberos login file
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file = /usr/cygnus/conf/krb5_login.conf
# Kerberos configuration file
cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

# ============================================
# OrionCKANSink configuration
# channel name from where to read notification events
cygnusagent.sinks.ckan-sink.channel = ckan-channel
# sink class, must not be changed
cygnusagent.sinks.ckan-sink.type = com.telefonica.iot.cygnus.sinks.OrionCKANSink
# the CKAN API key to use
cygnusagent.sinks.ckan-sink.api_key = ckanapikey
# the FQDN/IP address for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_host = x.y.z.w
# the port for the CKAN API endpoint
cygnusagent.sinks.ckan-sink.ckan_port = 80
# Orion URL used to compose the resource URL with the convenience operation URL to query it
cygnusagent.sinks.ckan-sink.orion_url = http://localhost:1026
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.ckan-sink.attr_persistence = row
# enable SSL for secure Http transportation; 'true' or 'false'
cygnusagent.sinks.ckan-sink.ssl = false

# ============================================
# OrionMySQLSink configuration
# channel name from where to read notification events
cygnusagent.sinks.mysql-sink.channel = mysql-channel
# sink class, must not be changed
cygnusagent.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.OrionMySQLSink
# the FQDN/IP address where the MySQL server runs 
cygnusagent.sinks.mysql-sink.mysql_host = x.y.z.w
# the port where the MySQL server listes for incomming connections
cygnusagent.sinks.mysql-sink.mysql_port = 3306
# a valid user in the MySQL server
cygnusagent.sinks.mysql-sink.mysql_username = root
# password for the user above
cygnusagent.sinks.mysql-sink.mysql_password = xxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.mysql-sink.attr_persistence = column

#=============================================
# hdfs-channel configuration
# channel type (must not be changed)
cygnusagent.channels.hdfs-channel.type = memory
# capacity of the channel
cygnusagent.channels.hdfs-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.hdfs-channel.transactionCapacity = 100

#=============================================
# ckan-channel configuration
# channel type (must not be changed)
cygnusagent.channels.ckan-channel.type = memory
# capacity of the channel
cygnusagent.channels.ckan-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.ckan-channel.transactionCapacity = 100

#=============================================
# mysql-channel configuration
# channel type (must not be changed)
cygnusagent.channels.mysql-channel.type = memory
# capacity of the channel
cygnusagent.channels.mysql-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.mysql-channel.transactionCapacity = 100
```

[Top](#top)

##<a name="section7"></a>Running Cygnus
###<a name="section7.1"></a>As a service (recommended)
<i>NOTE: Cygnus can only be run as a service if you installed it through the RPM.</i>

Once the `cygnus_instance_<id>.conf` and `agent_<id>.conf` files are properly configured, just use the `service` command to start, restart, stop or get the status (as a sudoer):

    $ sudo service cygnus status

    $ sudo service cygnus start

    $ sudo service cygnus restart

    $ sudo service cygnus stop

Previous commands afefcts to **all** of Cygnus instances configured. If only one instance is wanted to be managed by the service script then the instance identifier after de the action must be specified:

    $ sudo service cygnus status <id>

    $ sudo service cygnus start <id>

    $ sudo service cygnus restart <id>

    $ sudo service cygnus stop <id>

Where `<id>` is the suffix at the end of the `cygnus_instace_<id>.conf` or `agent_<id>.conf` files you used to configure the instance.

[Top](#top)

###<a name="section7.2"></a>As standalone application (advanced)

<i>NOTE: If you installed Cygnus through the RPM, APACHE\_FLUME\_HOME is `/usr/cygnus/`. If not, it is a directory of your choice.</i>

Cygnus implements its own startup script, `cygnus-flume-ng` which replaces the standard `flume-ng` one, which in the end runs a custom `com.telefonica.iot.cygnus.nodes.CygnusApplication` instead of a standard `org.apache.flume.node.Application`. 

In foreground (with logging):

    $ APACHE_FLUME_HOME/bin/cygnus-flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cygnus.conf -n cygnusagent -Dflume.root.logger=INFO,console [-p <mgmt-if-port>] [-t <polling-interval>]

In background:

    $ nohup APACHE_FLUME_HOME/bin/cygnus-flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/cygnus.conf -n cygnusagent -Dflume.root.logger=INFO,LOGFILE [-p <mgmt-if-port>] [-t <polling-interval>] &

The parameters used in these commands are:

* `agent`. This is the type of application to be run by the `cygnus-flume-ng` script.
* `--conf`. Points to the Apache Flume configuration folder.
* `-f` (or `--conf-file`). This is the agent configuration (`agent_<id>.conf`) file. Please observe when running in this mode no `cygnus_instance_<id>.conf` file is required.
* `-n` (or `--name`). The name of the Flume agent to be run.
* `-Dflume.root.logger`. Changes the logging level and the logging appender for log4j.
* `-p` (or `--mgmt-if-port`). Configures the listening port for the Management Interface. If not configured, the default value is used, `8081`.
* `-t` (or `--polling-interval`). Configures the polling interval (seconds) when the configuration is periodically reloaded. If not configured, the default value is used, `30`.

[Top](#top)

##<a name="section8"></a>Orion subscription

Once the connector is running, it is necessary to tell Orion Context Broker about it, in order Orion can send context data notifications to the connector. This can be done on behalf of the connector by performing the following curl command:

    $ (curl localhost:1026/NGSI10/subscribeContext -s -S --header 'Content-Type: application/xml' -d @- | xmllint --format -) <<EOF
    <?xml version="1.0"?>
    <subscribeContextRequest>
      <entityIdList>
        <entityId type="Room" isPattern="false">
          <id>Room1</id>
        </entityId>
      </entityIdList>
      <attributeList>
        <attribute>temperature</attribute>
      </attributeList>
      <!-- This is the part where Cygnus endpoint is specified -->
      <reference>http://host_running_cygnus:5050/notify</reference>
      <duration>P1M</duration>
      <notifyConditions>
        <notifyCondition>
          <type>ONCHANGE</type>
          <condValueList>
            <condValue>pressure</condValue>
          </condValueList>
        </notifyCondition>
      </notifyConditions>
      <throttling>PT5S</throttling>
    </subscribeContextRequest>
    EOF

Its equivalent in Json format can be seen [here](https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE).

[Top](#top)

##<a name="section9"></a>Logs
###<a name="section9.1"></a>log4j configuration

Cygnus uses the log4j facilities added by Flume for logging purposes. You can maintain the default `APACHE_FLUME_HOME/conf/log4j.properties` file, where a console and a file appender are defined (in addition, the console is used by default), or customize it by adding new appenders. Typically, you will have several instances of Cygnus running; they will be listening on different TCP ports for incoming notifyContextRequest and you'll probably want to have differente log files for them. E.g., if you have two Flume processes listening on TCP/1028 and TCP/1029 ports, then you can add the following lines to the `log4j.properties` file:

    log4j.appender.cygnus1028=org.apache.log4j.RollingFileAppender
    log4j.appender.cygnus1028.MaxFileSize=100MB
    log4j.appender.cygnus1028.MaxBackupIndex=10
    log4j.appender.cygnus1028.File=${flume.log.dir}/cygnus.1028.log
    log4j.appender.cygnus1028.layout=org.apache.log4j.PatternLayout
    log4j.appender.cygnus1028.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n
    
    log4j.appender.cygnus1029=org.apache.log4j.RollingFileAppender
    log4j.appender.cygnus1029.MaxFileSize=100MB
    log4j.appender.cygnus1029.MaxBackupIndex=10
    log4j.appender.cygnus1029.File=${flume.log.dir}/cygnus.1029.log
    log4j.appender.cygnus1029.layout=org.apache.log4j.PatternLayout
    log4j.appender.cygnus1029.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

Regarding the log4j Conversion Pattern:

* `time` makes reference to a timestamp following the [RFC3339](http://tools.ietf.org/html/rfc3339).
* `lvl`means logging level, and matches the traditional log4j levels: `INFO`, `WARN`, `ERROR`, `FATAL` and `DEBUG`.
* `trans` is a transaction identifier, i.e. an identifier that is printed in all the traces related to the same Orion notification. The format is `<cygnus_boot_time/1000>-<cygnus_boot_time%1000>-<10_digits_transaction_count>`. Its generation logic ensures that every transaction identifier is unique, also for Cygnus instances running in different VMs, except if they are started in the exactly same millisecond (highly unprobable).
* `function` identifies the function/method within the class printing the log.
* `comp` is always `Cygnus`.
* `msg` is a custom message that has always the same format: `<class>[<line>] : <message>`.

Once the log4j has been properly configured, you only have to add to the Flume command line the following parameter, which overwrites the default configutation (`flume.root.logger=INFO,LOGFILE`):

    -Dflume.root.logger=<loggin_level>,cygnus.<TCP_port>.log

In addition, you have a complete `log4j.properties` template in `conf/log4j.properties.template`, once you clone the Cygnus repository.

[Top](#top)

###<a name="section9.2"></a>Message types

Check [doc/operation/alarms.md](doc/operation/alarms.md) for a detailed list of message types.

[Top](#top)

##<a name="section10"></a>Management interface

From Cygnus 0.5 there is a REST-based management interface for administration purposes. Current available operations are:

<b>Get the version of the running software, including the last Git commit</b>:

    GET http://host:management_port/version

    {"version":"0.5_SNAPSHOT.8a6c07054da894fc37ef30480cb091333e2fccfa"}

[Top](#top)

##<a name="section11"></a>Contact

* Fermín Galán Márquez (fermin.galanmarquez@telefonica.com).
* Francisco Romero Bueno (francisco.romerobueno@telefonica.com).

[Top](#top)