#<a name="top"></a>Cygnus

* [What is Cygnus](#section1)
* [Design on top of Apache Flume](#section2)
* [Data flow example](#section3)
    * [Subscription to the NGSI-like source](#section3.1)
    * [NGSI notification reception](#section3.2)
    * [From NGSI format to Flume format](#section3.3)
    * [Data persistence](#section3.4)
        * [HDFS persistence](#section3.4.1)
        * [CKAN persistence](#section3.4.2)
        * [MySQL persistence](#section3.4.3)
        * [MongoDB persistence](#section3.4.4)
* [Installing Cygnus](#section4)
    * [RPM install (recommended)](#section4.1)
    * [Installing from sources (advanced)](#section4.2)
* [Cygnus configuration](#section5)
    * [`cygnus_instance_<id>.conf`](#section5.1)
    * [`agent_<id>.conf`](#section5.2)
* [Running Cygnus](#section6)
    * [As a service (recommended)](#section6.1)
    * [As standalone application (advanced)](#section6.2)
* [Logs](#section7)
    * [log4j configuration](#section7.1)
    * [Message types](#section7.2)
* [Advanced topics](#section8)
* [Contact](#section9)

##<a name="section1"></a>What is Cygnus

Cygnus is a connector in charge of persisting [Orion](https://github.com/telefonicaid/fiware-orion) context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using Cygnus.

Cygnus uses the subscription/notification feature of Orion. A subscription is made in Orion on behalf of Cygnus, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/). In fact, Cygnus is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Orion context data in:

* [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.
* [MySQL](https://www.mysql.com/), the well-know relational database manager.
* [CKAN](http://ckan.org/), an Open Data platform.
* [MongoDB](https://www.mongodb.org/), the NoSQL document-oriented database.

Cygnus is a (conceptual) derivative work of the deprecated [ngsi2cosmos](https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos).

[Top](#top)

##<a name="section2"></a>Design on top of Apache Flume

All the details about Flume can be found at [flume.apache.org](http://flume.apache.org/), but, as a reminder, some concepts will be explained here:

* A Flume **source** is an agent gathering event data from the real source (Twitter stream, a notification system, etc.), either by polling the source or listening for incoming pushes of data. Gathered data is sent to a Flume channel in the form of a Flume event (metadata headers + data body).
    * Certain sources behaviour is governed through Flume **handlers**.
    * Flume events may be transformed (by adding new metadata headers) or directly dropped by Flume **interceptors** before the events are put in the channel.
* A Flume **channel** is a passive store (implemented by means of a file, memory, etc.) that holds the event until it is consumed by the Flume sink.
* A Flume **sink** connects with the final destination of the data (a local file, HDFS, a database, etc.), taking events from the channel and consuming them (processing and/or persisting it).

There exists a wide collection of already developed sources, channels and sinks. The Flume-based connector, also called Cygnus, development extends that collection by adding:

* `OrionRestHandler`. A custom HTTP source handler for the default HTTP source. The existing HTTP source behaviour can be governed depending on the request handler associated to it in the configuration. In this case, the custom handler takes care of the method, the target and the headers (specially the Content-Type one) within the request, cheking everything is according to the expected [request format](https://forge.fi-ware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE). This allows for a certain degree of control on the incoming data. The header inspection step allows for a content type identification as well by sending, together with the data, the Content-Type header.
* `OrionHDFSSink`. A custom sink that persists Orion content data in a HDFS deployment. There already exists a native Flume HDFS sink persisting each event in a new file, but this is not suitable for Cygnus. Within Cygnus, the data coming from Orion must be persisted in the Cosmos HDFS in the form of files (a file per entity) containing Json-like lines about the values such entity's attributes have had along time. Several HDFS backends can be used for the data persistence (WebHDFS, HttpFS, Infinity), all of them based on the native WebHDFS REST API from Hadoop.
* `OrionCKANSink`. A custom sink that persists Orion context data in CKAN server instances (see http://docs.ckan.org/en/latest/).
* `OrionMySQLSink`. A custom sink for persisting Orion context data in a MySQL server. Each user owns a database, and each entity is mapped to a table within that database. Tables contain rows about the values such entity's attributes have had along time.
* `OrionMongoSink`. A custom sink for persisting Orion context data in a MongoDB server. Each user owns a database, and each entity is mapped to a collection within that database. Collections contain documents about the values such entity's attributes have had along time.
* `DestinationExtractorInterceptor`.

All these new components (`OrionRestHandler`, `OrionHDFSSink`, etc) are combined with other native ones included in Flume itself (e.g. `HTTPSource` or `MemoryChannel`), with the purpose of implementing the following basic data flow:

1.  On behalf of Cygnus, subscribe to certain NGSI-like source (typically Orion Context Broker) for certain context information.
2.  Receive from the NGSI-like source notifications about new updated context data; this notification will be handled by the native `HttpSource` together with the custom `OrionRestHandler`.
3.  Translate the notification into the Flume event format (metadata headers + data body), and put them into the different sink channels, typically of type `MemoryChannel`.
4.  In the meantime, some interceptors such as the native `Timestamp` one or the custom `DestinationExtractorInterceptor` may modify the event before it is put in the channel or channels.
5.  For each enabled custom sink (`OrionHDFSSink`, `OrionCKANSink`, `OrionMySQLSink`, `OrionMongoSink`), get the Flume events from the sink channels and persist the data in the appropriate format.

More complex architectures and data flows can be checked in the [architecture](doc/design/architecture.md) document.

[Top](#top)

##<a name="section3"></a>Data flow example
Next sections will consider an example NGSI entity called 'car1' of type 'car', with attributes 'speed' (type 'kmh') and 'oil_level' (type 'percentage'). Is not a goal for this document to show you how to define a NGSI entity nor how to create it in the most common NGSI source for Cygnus, Orion Context Broker. Please, refer to the [official Orion documentation](https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#Entity_Creation) for more details.

[Top](#top)

###<a name="section3.1"></a>Subscription to the NGSI-like source
Cygnus takes advantage of the subscription-notification mechanism of NGSI. Specifically, Cygnus needs to be notified each time certain entity's attributes change, and in order to do that, Cygnus must subscribe to those entity's attribute changes.

As long as the typical NGSI-like source is Orion Context Broker, you can make a subscription about the example NGSI entity ('car1' of type 'car') by using the `curl` command in this [way](https://forge.fi-ware.eu/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE) (assuming Orion runs in localhost and listens on the TCP/1026 port):

    (curl localhost:1026/v1/subscribeContext -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' -d @- | python -mjson.tool) <<EOF
    {
        "entities": [
            {
                "type": "car",
                "isPattern": "false",
                "id": "car1"
            }
        ],
        "attributes": [
            "speed",
            "oil_level"
        ],
        "reference": "http://localhost:5050/notify",
        "duration": "P1M",
        "notifyConditions": [
            {
                "type": "ONCHANGE",
                "condValues": [
                    "speed"
                ]
            }
        ],
        "throttling": "PT1S"
    }
    EOF

Which means: <i>Each time the the 'car1' entity, of type 'car', changes its value of 'speed' send a notification to http://localhost:5050/notify (where Cygnus will be listening) with the 'speed' and 'oil_level' values. This subscription will have a duration of one month, and please, do not send me notifications more than one per second</i>.

[Top](#top)

###<a name="section3.2"></a>NGSI notification reception
Let's supose the 'speed' of the 'car1' entity changes to '112.9'; then the following NGSI notification (or NGSI event) would be sent as a Http POST to the configured Cygnus listener, i.e. the native `HTTPSource` (the code below is an <i>object representation</i>, not any real data format):

    ngsi-event={
        http-headers={
            Content-Length: 492
            User-Agent: orion/0.9.0
            Host: localhost:1028
            Accept: application/xml, application/json
            Content-Type: application/json
            Fiware-Service: vehicles
            Fiware-ServicePath: 4wheels 
        },
        payload={
            {
                "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
                "originator" : "localhost",
                "contextResponses" : [
                    {
                        "contextElement" : {
                        "attributes" : [
                            {
                                "name" : "speed",
                                "type" : "kmh",
                                "value" : "112.9",
                                "metadatas": []
                            },
                            {
                                "name" : "oil_level",
                                "type" : "percentage",
                                "value" : "74.6",
                                "metadatas": []
                            }
                        ],
                        "type" : "car",
                        "isPattern" : "false",
                        "id" : "car1"
                    },
                    "statusCode" : {
                        "code" : "200",
                        "reasonPhrase" : "OK"
                    }
                ]
            }
        }
    }

[Top](#top)

###<a name="section3.3"></a>From NGSI format to Flume format
Flume events are not much more different than the above representation: there is a set of headers and a body. This is an advantage, since allows for a quick translation between formats. Thus, once the notification is received, the `HTTPSource` relies on the custom `OrionRestHandler` for checking its validity (that it is a POST request, that the target is 'notify' and that the headers are OK), detecting the content type (in the example, Json format), extracting the data (the Json part) and finally creating the Flume event to be put in the native `MemoryChannel`.

The equivalent <i>object representation</i> (not any real data format) for such a notified NGSI event could be the following Flume event:

    flume-event={
        headers={
	         content-type=application/json,
	         fiware-service=vehicles,
	         fiware-servicepath=4wheels,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         ttl=10,
	         destination=car1_car
        },
        body={
	         entityId=car1,
	         entityType=car,
	         attributes=[
	             {
	                  attrName=speed,
	                  attrType=kmh,
	                  attrValue=112.9
	             },
	             {
	                  attrName=oil_level,
	                  attrType=percentage,
	                  attrValue=74.6
	             }
	         ]
	     }
    }

The headers are a subset of the notified HTTP headers and others added by Cygnus [interceptors](doc/design/interceptors.md):

* The <b>content-type</b> header is a replica of the HTTP header. It is needed for the different sinks to know how to parse the event body. In this case it is JSON.
* Note that Orion can include a `Fiware-Service` HTTP header specifying the tenant/organization associated to the notification, which is added to the event headers as well (as `fiware-service`). Since version 0.3, Cygnus is able to support this header, although the actual processing of such tenant/organization depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service specified in the `default_service` configuration property. Please observe that the notified `fiware-service` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* Orion can notify another HTTP header, `Fiware-ServicePath` specifying a subservice within a tenant/organization, which is added to the event headers as well (as `fiware-servicepath`). Since version 0.6, Cygnus is able to support this header, although the actual processing of such subservice depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service path specified in the `default_service_path` configuration property. Please observe that the notified `fiware-servicePath` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* The notification reception time is included in the list of headers (as <b>timestamp</b>) for timestamping purposes in the different sinks. It is added by a native interceptor. See the [doc/design/interceptors.md](doc/design/interceptors.md) document for more details.
* The <b>transactionId</b> identifies a complete Cygnus transaction, starting at the source when the context data is notified, and finishing in the sink, where such data is finally persisted.
* The time-to-live (or <b>ttl</b>) specifies the number of re-injection retries in the channel when something goes wrong while persisting the data. This re-injection mechanism is part of the reliability features of Flume. -1 means inifinite retries.
* The <b>destination</b> headers is used to identify the persistence element within the used storage, i.e. a file in HDFS, a MySQL table or a CKAN resource. This is added by a custom interceptor called `DestinationExtractor` added to the Flume's suite. See the <i>doc/design/interceptors</i> document for more details.

The body simply contains a byte representation of the HTTP payload that will be parsed by the sinks.

[Top](#top)

###<a name="section3.4"></a>Data persistence
####<a name="section3.4.1"></a>HDFS persistence

[HDFS organizes](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html#The_File_System_Namespace) the data in folders containinig big data files. Such organization is exploited by [`OrionHDFSSink`](doc/design/OrionHDFSSink.md) each time a Flume event is taken from its channel.

Assuming `cosmos_default_username=myuser` and `attr_persistence=row` as configuration parameters, then the data within the body will be persisted as:

    $ hadoop fs -cat /user/myuser/vehicles/4wheels/car1_car/car1_car.txt
    {"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124UTC","entityId":"car1","entityType":"car","attrName":"speed","attrType":"kmh","attrValue":"112.9","attrMd":[]}
    {"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124UTC","entityId":"car1","entityType":"car","attrName":"oil","attrType":"percentage","attrValue":"74.6","attrMd":[]}

If `attr_persistence=colum` then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myser/vehicles/4wheels/car1_car/car1_car.txt
    {"recvTime":"2015-04-20T12:13:22.41.124UTC","speed":"112.9","speed_md":[],"oil":"74.6","oil_md":[]} 
    
NOTE: `hadoop fs -cat` is the HDFS equivalent to the Unix command `cat`.

[Top](#top)

####<a name="section3.4.2"></a>OrionCKANSink persistence

[CKAN organizes](http://docs.ckan.org/en/latest/user-guide.html) the data in organizations containing packages or datasets; each one of these packages/datasets contains several resources whose data is finally stored in a PostgreSQL database (CKAN Datastore) or plain files (CKAN Filestore). Such organization is exploited by [`OrionCKANSink`](doc/design/OrionCKANSink.md) each time a Flume event is taken from its channel.

Assuming `api_key=myapikey` and `attr_persistence=row` as configuration parameter, then `OrionCKANSink` will persist the data within the body as:

    $ curl -s -S -H "Authorization: myapikey" "http://192.168.80.34:80/api/3/action/datastore_search?resource_id=3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7"
    {
        "help": "Search a DataStore resource...",
        "success": true,
        "result": {
            "resource_id": "3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7",
            "fields": [
                {
                    "type": "int4",
                    "id": "_id"
                },
                {
                    "type": "int4",
                    "id": "recvTimeTs"
                },
                {
                    "type": "timestamp",
                    "id": "recvTime"
                },
                {
                    "type": "text",
                    "id": "attrName"
                },
                {
                    "type": "text",
                    "id": "attrType"
                },
                {
                    "type": "json",
                    "id": "attrValue"
                },
                {
                    "type": "json",
                    "id": "attrMd"
                }
            ],
            "records": [
                {
                    "attrType": "kmh",
                    "recvTime": "2015-04-20T12:13:22.41.124UTC",
                    "recvTimeTs": 1429535775,
                    "attrMd": null,
                    "attrValue": "112.9",
                    "attrName": "speed",
                    "_id": 1
                },
                {
                    "attrType": "percentage",
                    "recvTime": "2015-04-20T12:13:22.41.124UTC",
                    "recvTimeTs": 1429535775,
                    "attrMd": null,
                    "attrValue": "74.6",
                    "attrName": "oil_level",
                    "_id": 2
                }
            ],
            "_links": {
                "start": "/api/3/action/datastore_search?resource_id=3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7",
                "next": "/api/3/action/datastore_search?offset=100&resource_id=3254b3b4-6ffe-4f3f-8eef-c5c98bfff7a7"
            },
            "total": 2
        }
    }

If `attr_persistence=colum` then `OrionCKANSink` will persist the data within the body as:

    $ curl -s -S -H "Authorization: myapikey" "http://130.206.83.8:80/api/3/action/datastore_search?resource_id=611417a4-8196-4faf-83bc-663c173f6986"
    {
        "help": "Search a DataStore resource...",
        "success": true,
        "result": {
            "resource_id": "611417a4-8196-4faf-83bc-663c173f6986",
            "fields": [
                {
                    "type": "int4",
                    "id": "_id"
                },
                {
                    "type": "timestamp",
                    "id": "recvTime"
                },
                {
                    "type": "json",
                    "id": "speed"
                },
                {
                    "type": "json",
                    "id": "speed_md"
                },
                {
                    "type": "json",
                    "id": "oil_level"
                },
                {
                    "type": "json",
                    "id": "oil_level_md"
                }
            ],
            "records": [
                {
                    "recvTime": "2015-04-20T12:13:22.41.124UTC",
                    "speed": "112.9",
                    "speed_md": null,
                    "oil_level": "74.6",
                    "oil_level_md": null,
                    "_id": 1
                }
            ],
            "_links": {
                "start": "/api/3/action/datastore_search?resource_id=611417a4-8196-4faf-83bc-663c173f6986",
                "next": "/api/3/action/datastore_search?offset=100&resource_id=611417a4-8196-4faf-83bc-663c173f6986"
            },
            "total": 1 
        }
    }

NOTE: `curl` is a Unix command allowing for interacting with REST APIs such as the exposed by CKAN.

[Top](#top)

####<a name="section3.4.3"></a>OrionMySQLSink persistence

MySQL organizes the data in databases that contain tables of data rows. Such organization is exploited by [`OrionCKANSink`](doc/design/OrionMySQL.md) each time a Flume event is taken from its channel.

Assuming `mysql_username=myuser` and `attr_persistence=row` as configuration parameters, then `OrionMySQLSink` will persist the data within the body as:

    $ mysql -u myuser -p
    Enter password: 
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    ...
    mysql> show databases;
    +-----------------------+
    | Database              |
    +-----------------------+
    | information_schema    |
    | vehicles              |
    | mysql                 |
    | test                  |
    +-----------------------+
    4 rows in set (0.05 sec)

    mysql> use vehicles;
    ...
    Database changed
    mysql> show tables;
    +--------------------+
    | Tables_in_vehicles |
    +--------------------+
    | 4wheels_car1_car   |
    +--------------------+
    1 row in set (0.00 sec)

    mysql> select * from 4wheels_car1_car;
    +------------+-------------------------------+----------+------------+-------------+------------+-----------+--------+
    | recvTimeTs | recvTime                      | entityId | entityType | attrName    | attrType   | attrValue | attrMd |
    +------------+-------------------------------+----------+------------+-------------+------------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124UTC | car1     | car        |  speed      | kmh        | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124UTC | car1     | car        |  oil_level  | percentage | 74.6      | []     |
    +------------+-------------------------------+----------+------------+-------------+------------+-----------+--------+
    2 row in set (0.00 sec)
    
If `attr_persistence=colum` then `OrionHDFSSink` will persist the data within the body as:

    $ mysql -u myuser -p
    Enter password: 
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    ...
    mysql> show databases;
    +-----------------------+
    | Database              |
    +-----------------------+
    | information_schema    |
    | vehicles              |
    | mysql                 |
    | test                  |
    +-----------------------+
    4 rows in set (0.05 sec)

    mysql> use vehicles;
    ...
    Database changed
    mysql> show tables;
    +--------------------+
    | Tables_in_vehicles |
    +--------------------+
    | 4wheels_car1_car   |
    +--------------------+
    1 row in set (0.00 sec)

    mysql> select * from 4wheels_car1_car;
    +-------------------------------+-------+----------+-----------+--------------+
    | recvTime                      | speed | speed_md | oil_level | oil_level_md |
    +-------------------------------+-------+----------+-----------+--------------+
    | 2015-04-20T12:13:22.41.124UTC | 112.9 | []       |  74.6     | []           |
    +-------------------------------+-------+----------+-----------+--------------+
    1 row in set (0.00 sec)

NOTE: `mysql` is the MySQL CLI for querying the data.

[Top](#top)

####<a name="section3.4.4"></a>OrionMongoSink persistence

MongoDB organizes the data in databases that contain collections of Json documents. Such organization is exploited by [`OrionMongoSink`](doc/desing/OrionMongoSink.md) each time a Flume event is taken from its channel.

Assuming `mongo_username=myuser` as configuration parameter, the data within the body will be persisted as:

    $ mongo -u myuser -p
    MongoDB shell version: 2.6.9
    connecting to: test
    > show databases
    admin              (empty)
    local              0.031GB
    vehicles           0.031GB
    test               0.031GB
    > use vehicles
    switched to db vehicles
    > show collections
    4wheels
    4wheels_car1_car
    4wheels_car1_car_speed
    system.indexes
    > db.4wheels.find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.124UTC", "entityId" : "car1", "entityType" : "car", "attrName" : "speed", "attrType" : "kmh", "attrValue" : "112.9" }
    > db.4wheels_car1_car.find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.412UTC", "attrName" : "speed", "attrType" : "kmh", "attrValue" : "112.9" }
    > db.4wheels_car1_car_speed.find()
    { "_id" : ObjectId("5534d143fa701f0be751db82"), "recvTimeTs": "1402409899391", "recvTime" : "2015-04-20T12:13:22.41.560UTC", "attrType" : "kmh", "attrValue" : "112.9" }

NOTE: the results for the three different data models (<i>collection-per-service-path</i>, <i>collection-per-service</i> and <i>collection-per-attribute</i>) are shown respectively; and no database prefix nor collection prefix was used (see [Cygnus configuration](#section6) for more details).

NOTE: `mongo` is the MongoDB CLI for querying the data.

[Top](#top)

##<a name="section4"></a>Installing Cygnus
###<a name="section4.1"></a>RPM install (recommended)
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

###<a name="section4.2"></a>Installing from sources (advanced)
Please, refer to [this](doc/installation/src_install.md) document if your aim is to install Cygnus from sources.

[Top](#top)

##<a name="section5"></a>Cygnus configuration
Cygnus is configured through two different files:

* A `cygnus_instance_<id>.conf` file addressing all those non Flume parameters, such as the Flume agent name, the specific log file for this instance, the administration port, etc. This configuration file is not necessary if Cygnus is run as a standlalone application (see later), bt it is mandatory if run as a service (see later).
* An `agent_<id>.conf` file addressing all those Flume parameters, i.e. how to configure the different sources, channels, sinks, etc. that compose the Flume agent behind the Cygnus instance. always mandatory.

Please observe there may exist several Cygnus instances identified by `<id>`, which must be the same for both configuration files regarding the same Cygnus instance.

[Top](#top)

###<a name="section5.1"></a>`cygnus_instance_<id>.conf`

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

###<a name="section5.2"></a>`agent_<id>.conf`
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
cygnusagent.sinks = hdfs-sink mysql-sink ckan-sink mongo-sink
cygnusagent.channels = hdfs-channel mysql-channel ckan-channel mongo-channel

#=============================================
# source configuration
# channel name where to write the notification events
cygnusagent.sources.http-source.channels = hdfs-channel mysql-channel ckan-channel mongo-channel
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
# the port where the MySQL server listens for incomming connections
cygnusagent.sinks.mysql-sink.mysql_port = 3306
# a valid user in the MySQL server
cygnusagent.sinks.mysql-sink.mysql_username = root
# password for the user above
cygnusagent.sinks.mysql-sink.mysql_password = xxxxxxxxxxxx
# how the attributes are stored, either per row either per column (row, column)
cygnusagent.sinks.mysql-sink.attr_persistence = column

# ============================================
# OrionMongoSink configuration
# sink class, must not be changed
cygnusagent.sinks.mongo-sink.type = com.telefonica.iot.cygnus.sinks.OrionMongoSink
# channel name from where to read notification events
cygnusagent.sinks.mongo-sink.channel = mongo-channel
# FQDN/IP:port where the MongoDB server runs (standalone case) or comma-separated list of FQDN/IP:port pairs where the MongoDB replica set members run
cygnusagent.sinks.mongo-sink.mongo_hosts = x1.y1.z1.w1:port1,x2.y2.z2.w2:port2,...
# a valid user in the MongoDB server
cygnusagent.sinks.mongo-sink.mongo_username = mongo_username
# password for the user above
cygnusagent.sinks.mongo-sink.mongo_password = xxxxxxxx
# data model (collection-per-service-path, collection-per-entity, collection-per-attribute)
cygnusagent.sinks.mongo-sink.data_model = collection-per-entity
# prefix for the MongoDB databases (empty for none)
cygnusagent.sinks.mongo-sink.db_prefix =
# prefix pro the MongoDB collections (empty for none)
cygnusagent.sinks.mongo-sink.collection_prefix =

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

#=============================================
# mongo-channel configuration
# channel type (must not be changed)
cygnusagent.channels.mongo-channel.type = memory
# capacity of the channel
cygnusagent.channels.mongo-channel.capacity = 1000
# amount of bytes that can be sent per transaction
cygnusagent.channels.mongo-channel.transactionCapacity = 100
```

[Top](#top)

##<a name="section6"></a>Running Cygnus
###<a name="section6.1"></a>As a service (recommended)
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

###<a name="section6.2"></a>As standalone application (advanced)

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

##<a name="section7"></a>Logs
###<a name="section7.1"></a>log4j configuration

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

###<a name="section7.2"></a>Message types

Check [doc/operation/alarms.md](doc/operation/alarms.md) for a detailed list of message types.

[Top](#top)

##<a name="section8"></a>Advanced topics
Please refer to the linked specific documents when looking for information regarding these topics:

* [Management Interface](doc/design/management_interface.md). From Cygnus 0.5 there is a REST-based management interface for administration purposes.
* [Pattern-based grouping](doc/design/interceptors.md). Designed as a Flume interceptor, this feature <i>overwrites</i> the default behaviour when building the `destination` header within the Flume events.
* [Kerberized HDFS](doc/operation/hdfs_kerberos_authentication.md). This document shows you how to authenticate Cygnus on a Kerberized HDFS.
* [Multi-instance](conf/README.md). Several instances of Cygnus can be run as a service.
* [Performance tips](doc/operation/performance_tuning_tips.md). If you are experiencing performance issues or want to improve your statistics, take a look on how to obtaint the best from Cygnus.
* [New sink development](doc/devel/add_new_sink.md). Addressed to those developers aiming to contribute to Cygnus with new sinks.

[Top](#top)

##<a name="section9"></a>Contact

Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**

[Top](#top)