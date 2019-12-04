# <a name="top"></a>cygnus-ngsi docker
Content:

* [Before starting](#section1)
* [Getting an image](#section2)
    * [Building form sources](#section2.1)
    * [Using docker hub image](#section2.2)
* [Using the image](#section3)
    * [As it is](#section3.1)
    * [Using a specific configuration](#section3.2)
        * [Editing the docker files](#section3.2.1)
        * [Environment variables](#section3.2.2)
        * [Using volumes](#section3.2.3)

## <a name="section1"></a>Before starting
Obviously, you will need docker installed and running in you machine. Please, check [this](https://docs.docker.com/linux/started/) official start guide.

[Top](#top)

## <a name="section2"></a>Getting an image
### <a name="section2.1"></a>Building from sources
Start by cloning the `fiware-cygnus` repository:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git

Change directory:

    $ cd fiware-cygnus

And run the following command:

    $ sudo docker build -f docker/cygnus-ngsi/Dockerfile -t cygnus-ngsi .

Once finished (it may take a while), you can check the available images at your docker by typing:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
cygnus-ngsi         latest              6a9e16550c82        10 seconds ago      462.1 MB
centos              6                   273a1eca2d3a        2 weeks ago         194.6 MB
```

[Top](#top)

### <a name="section2.2"></a>Using docker hub image
Instead of building an image from the scratch, you may download it from [hub.docker.com](https://hub.docker.com/r/fiware/cygnus-ngsi/):

    $ docker pull fiware/cygnus-ngsi

It can be listed the same way than above:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
cygnus-ngsi         latest              6a9e16550c82        10 seconds ago      462.1 MB
centos              6                   273a1eca2d3a        2 weeks ago         194.6 MB
```

[Top](#top)

## <a name="section3"></a>Using the image
### <a name="section3.1"></a>As it is
The cygnus-ngsi image (either built from the scratch, either downloaded from hub.docker.com) allows running a Cygnus agent in charge of receiving NGSI-like notifications and persisting them into wide variety of storages: MySQL (Running in a  `iot-mysql` host), MongoDB and STH (running in a  `iot-mongo` host), CKAN (running in `iot-ckan` host), HDFS (running in `iot-hdfs` host) and Carto (a cloud service at `https://<your_user>.cartodb.com`).

Start a container for this image by typing in a terminal:

    $ docker run cygnus-ngsi

Immediately after, you will start seeing cygnus-ngsi logging traces (MySQL example):

```
+ exec /usr/lib/jvm/java-1.7.0/bin/java -Xmx20m -Dflume.root.logger=INFO,console -cp '/opt/apache-flume/conf:/opt/apache-flume/lib/*:/opt/apache-flume/plugins.d/cygnus/lib/*:/opt/apache-flume/plugins.d/cygnus/libext/*' -Djava.library.path= com.telefonica.iot.cygnus.nodes.CygnusApplication -f /opt/apache-flume/conf/agent.conf -n cygnus-ngsi
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/apache-flume/lib/slf4j-log4j12-1.6.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/lib/cygnus-ngsi-0.13.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/libext/cygnus-common-0.13.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
time=2016-05-05T09:57:55.150UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp= | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[166] : Starting Cygnus, version 0.13.0_SNAPSHOT.5200773899b468930e82df4a0b34d44fd4632893
...
...
time=2016-05-05T09:57:56.287UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 5080 (Management Interface)
```

You can check the running container (in a second terminal shell):

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                CREATED              STATUS              PORTS                NAMES
9ce0f09f5676        cygnus-ngsi         "/cygnus-entrypoint.   About a minute ago   Up About a minute   5050/tcp, 5080/tcp   focused_kilby
```

You can check the IP address of the container above by doing:

```
$ docker inspect 9ce0f09f5676 | grep \"IPAddress\"
        "IPAddress": "172.17.0.13",
```

Once the IP address of the container is gotten, you may ask for the Cygnus version (in a second terminal shell):

```
$ curl "http://172.17.0.13:5080/v1/version"
{"success":"true","version":"0.13.0_SNAPSHOT.5200773899b468930e82df4a0b34d44fd4632893"}
```

Even, you may send a NGSI-like notification emulation (please, check the notification examples at [cygnus-ngsi](cygnus-ngsi/resources/ngsi-examples)):

```
$ ./notification.sh http://172.17.0.13:5050/notify
* About to connect() to 172.17.0.13 port 5050 (#0)
*   Trying 172.17.0.13... connected
* Connected to 172.17.0.13 (172.17.0.13) port 5050 (#0)
> POST /notify HTTP/1.1
> Host: 172.17.0.13:5050
> Content-Type: application/json
> Accept: application/json
> User-Agent: orion/0.10.0
> Fiware-Service: default
> Fiware-ServicePath: /
> Content-Length: 460
>
< HTTP/1.1 200 OK
< Transfer-Encoding: chunked
< Server: Jetty(6.1.26)
<
* Connection #0 to host 172.17.0.13 left intact
* Closing connection #0
```

You will be able to see something like the following in the cygnus-ngsi terminal (MySQL example):

```
time=2016-05-05T10:01:22.111UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=getEvents | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[249] : Starting internal transaction (8bed4f8d-c47f-499a-a70d-365883584ac7)
time=2016-05-05T10:01:22.113UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=getEvents | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[265] : Received data ({  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "attributes" : [          {            "name" : "temperature",            "type" : "centigrade",            "value" : "26.5"          }        ],        "type" : "Room",        "isPattern" : "false",        "id" : "Room1"      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-05-05T10:01:31.687UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.sinks.NGSISink[342] : Batch accumulation time reached, the batch will be processed as it is
time=2016-05-05T10:01:31.689UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-05-05T10:01:31.708UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=persistAggregation | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.sinks.NGSIMySQLSink[501] : [mysql-sink] Persisting data at OrionMySQLSink. Database (default), Table (Room1_Room), Fields ((recvTimeTs,recvTime,fiwareServicePath,entityId,entityType,attrName,attrType,attrValue,attrMd)), Values (('1462442482115','2016-05-05T10:01:22.115','/','Room1','Room','temperature','centigrade','26.5','[]'))
time=2016-05-05T10:01:32.050UTC | lvl=ERROR | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.sinks.NGSISink[411] : Persistence error (Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.)
time=2016-05-05T10:01:32.051UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.sinks.NGSISink[423] : TTL exhausted, finishing internal transaction (8bed4f8d-c47f-499a-a70d-365883584ac7)
```

Don't worry about the error, it is normal (please, see next section).

You can stop the container as:

```
$ docker stop 9ce0f09f5676
9ce0f09f5676
$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

Support for NGSIv2 notifications has been added from version above 1.17.1. For this purpose, it's been added a new dir [/NGSIv2](./resources/ngsi-examples/NGSIv2) which contains script files in order to emulate some NGSIv2 notification types. 

[Top](#top)

### <a name="section3.2"></a>Using a specific configuration
As seen above, the default configuration distributed with the image is tied to certain values that may not be suitable for you tests. Specifically:

* Configuration generation:
    * Handle generation of configuration: CYGNUS_SKIP_CONF_GENERATION. If enabled (i.e. env var set to false) a configuration will be created for container, if disabled (i.e. env var set to true) then your should provide a configuration file.

* Multiagent:
    * Enable multiagent cygnus: CYGNUS_MULTIAGENT environment variable. If enabled, each sink will have a different configuration file and will be executed by a different cygnus agent (java process). If disabled, all sinks are configured in the same agent configuration file and are executed by the same agent (java procss).
    In both cases, multiagent or not, each cygnus sink will always run in a diferent port (multisink):

| sink   | port   | admin_port |
|--:|--:|--:|
| mysql  | 5050 | 5080 |
| mongo | 5051 | 5081 |
| ckan | 5052 | 5082 |
| hdfs | 5053 | 5083 |
| postgresql | 5054 | 5084 |
| cartodb | 5055 | 5085 |
| orion | 5056 | 5086 |
| postgis | 5057 | 5087 |
| elasticsearch | 5058 | 5088 |
| arcgis | 5059 | 5089 |


* MySQL:
    * It only works for building historical context data in MySQL.
    * The endpoint for MYSQL is `iot-mysql` but can be changed through the CYGNUS_MYSQL_HOST environment variable.
    * The port for MYSQL is `3306` but can be changed through the CYGNUS_MYSQL_PORT environment variable.
    * The user for MySQL is `mysql` but can be changed through the CYGNUS_MYSQL_USER environment variable.
    * The pass for MySQL is `mysql` but can be changed through the CYGNUS_MYSQL_PASS environment variable.
    * CYGNUS_MYSQL_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_MYSQL_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_MYSQL_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_MYSQL_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_MYSQL_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_MYSQL_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_MYSQL_DATA_MODEL: select the data_model: dm-by-service-path, dm-by-entity or dm-by-entity-type.
    * CYGNUS_MYSQL_ATTR_PERSISTENCE: how the attributes are stored, either per row either per column (row, column).
    * CYGNUS_MYSQL_ATTR_NATIVE_TYPES: how the attribute are stored, using native type (true) or stringfy (false, by default).
    * CYGNUS_MYSQL_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_MYSQL_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_MYSQL_BATCH_TTL: number of retries upon persistence error.

* Mongo and STH:
    * Mongo only works for building historical context data in Mongo.
    * STH only works for building historical context data in STH.
    * The endpoint for Mongo and STH, containing host and port, is `iot-mongo:27017` but can be changed through the CYGNUS_MONGO_HOSTS environment variable.
    * The user for Mongo and STH is `mongo` but can be changed through the CYGNUS_MONGO_USER environment variable.
    * The pass for Mongo and STH is `mongo` but can be changed through the CYGNUS_MONGO_PASS environment variable.
    * CYGNUS_MONGO_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_MONGO_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_MONGO_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_MONGO_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_MONGO_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_MONGO_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_MONGO_DATA_MODEL: select the data_model: dm-by-service-path or dm-by-entity.
    * CYGNUS_MONGO_ATTR_PERSISTENCE: how the attributes are stored, either per row either per column (row, column).
    * CYGNUS_MONGO_DB_PREFIX: prefix for the MongoDB databases
    * CYGNUS_MONGO_COLLECTION_PREFIX: prefix for the MongoDB collections
    * CYGNUS_MONGO_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_MONGO_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_MONGO_BATCH_TTL: number of retries upon persistence error.
    * CYGNUS_MONGO_DATA_EXPIRATION: value specified in seconds. Set to 0 if not wanting this policy
    * CYGNUS_MONGO_COLLECTIONS_SIZE: value specified in bytes. Set to 0 if not wanting this policy. Minimum value (different than 0) is 4096 bytes
    * CYGNUS_MONGO_MAX_DOCUMENTS: value specifies the number of documents. Set to 0 if not wanting this policy
    * CYGNUS_MONGO_IGNORE_WHITE_SPACES: true if exclusively white space-based attribute values must be ignored, false otherwise
    * CYGNUS_STH_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_STH_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_STH_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_STH_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_STH_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_STH_DATA_MODEL: select the data_model: dm-by-service-path or dm-by-entity.
    * CYGNUS_STH_DB_PREFIX: prefix for the MongoDB databases
    * CYGNUS_STH_COLLECTION_PREFIX: prefix for the MongoDB collections
    * CYGNUS_STH_RESOLUTIONS: accepted values are month, day, hour, minute and second separated by comma
    * CYGNUS_STH_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_STH_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_STH_BATCH_TTL: number of retries upon persistence error.
    * CYGNUS_STH_DATA_EXPIRATION: value specified in seconds. Set to 0 if not wanting this policy

* CKAN:
    * It only works for building historical context data in CKAN.
    * The endpoint for CKAN is `iot-ckan` but can be changed through the CYGNUS_CKAN_HOST environment variable.
    * The port for CKAN is `80` but can be changed through the CYGNUS_CKAN_PORT environment variable.
    * The ssl for CKAN is `false` but can be changed through the CYGNUS_CKAN_SSL environment variable.
    * The api_key for CKAN is blank but can be changed through the CYGNUS_CKAN_API_KEY environment variable.
    * CYGNUS_CKAN_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_CKAN_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_CKAN_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_CKAN_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_CKAN_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_CKAN_DATA_MODEL: select the data_model: dm-by-service-path or dm-by-entity.
    * CYGNUS_CKAN_ATTR_PERSISTENCE: how the attributes are stored, either per row either per column (row, column).
    * CYGNUS_CKAN_ORION_URL: Orion URL used to compose the resource URL with the convenience operation URL to query it
    * CYGNUS_CKAN_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_CKAN_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_CKAN_BATCH_TTL: number of retries upon persistence error.
    * CYGNUS_CKAN_BACKEND_MAX_CONNS: maximum number of Http connections to CKAN backend
    * CYGNUS_CKAN_BACKEND_MAX_CONSS_PER_ROUTE: maximum number of Http connections per route to CKAN backend

* HDFS:
    * It only works for building historical context data in HDFS.
    * The endpoint for HDFS is `iot-hdfs` but can be changed through the CYGNUS_HDFS_HOST environment variable.
    * The port for HDFS is `50070` but can be changed through the CYGNUS_HDFS_PORT environment variable.
    * The user for HDFS is `hdfs` but can be changed through the CYGNUS_HDFS_USER environment variable.
    * The token for HDFS is empty but can be set through the CYGNUS_HDFS_TOKEN environment variable
    * CYGNUS_HDFS_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_HDFS_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_HDFS_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_HDFS_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_HDFS_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_HDFS_DATA_MODEL: select the data_model: dm-by-service-path or dm-by-entity.
    * CYGNUS_HDFS_FILE_FORMAT: how the attributes are stored, available formats are json-row, json-column, csv-row and csv-column
    * CYGNUS_HDFS_BACKEND_IMPL: rest if the interaction with HDFS will be WebHDFS/HttpFS-based, binary if based on the Hadoop API
    * CYGNUS_HDFS_BACKEND_MAX_CONNS: maximum number of Http connections to HDFS backend
    * CYGNUS_HDFS_BACKEND_MAX_CONNS_PER_ROUTE: maximum number of Http connections per route to HDFS backend
    * CYGNUS_HDFS_PASSWORD: password for the above username; this is only required for Hive authentication
    * CYGNUS_HDFS_SERVICE_AS_NAMESPACE: true if the notified fiware-service (or the default one, if no one is notified) is used as the HDFS namespace, false otherwis
    * CYGNUS_HDFS_BATCH_SIZE:  number of notifications to be included within a processing batch.
    * CYGNUS_HDFS_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_HDFS_BATCH_TTL: number of retries upon persistence error.
    * CYGNUS_HDFS_BATCH_RETRY_INTERVALS
    * CYGNUS_HDFS_HIVE: true enables Hive, false disabled it
    * CYGNUS_HDFS_KRB5_AUTH: true enables Kerberos-basded authentication, false disables it

* PostgreSQL:
    * It only works for building historical context data in PostgreSQL.
    * The endpoint for PostgreSQL is `iot-postgresql` but can be changed through the CYGNUS_POSTGRESQL_HOST environment variable.
    * The port for PostgreSQL is `3306` but can be changed through the CYGNUS_POSTGRESQL_PORT environment variable.
    * The user for PostgreSQL is `postgresql` but can be changed through the CYGNUS_POSTGRESQL_USER environment variable.
    * The pass for PostgreSQL is `postgresql` but can be changed through the CYGNUS_POSTGRESQL_PASS environment variable.
    * CYGNUS_POSTGRESQL_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_POSTGRESQL_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_POSTGRESQL_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_POSTGRESQL_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_POSTGRESQL_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_POSTGRESQL_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_POSTGRESQL_ATTR_PERSISTENCE: how the attributes are stored, either per row either per column (row, column).
    * CYGNUS_POSTGRESQL_ATTR_NATIVE_TYPES: how the attribute are stored, using native type (true) or stringfy (false, by default).
    * CYGNUS_POSTGRESQL_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_POSTGRESQL_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_POSTGRESQL_BATCH_TTL: number of retries upon persistence error.

* Carto:
    * It only works for building historical context data in Carto.
    * The user for Carto is `carto` but can be changed through the CYGNUS_CARTO_USER environment variable.
    * The key for Carto is `carto` but can be changes through the CYGNUS_CARTO_KEY environment variable.

* Orion:
    * It only works for building historical context data in Orion.
    * The endpoint for Orion is `iot-orion-ext` but can be changed through the CYGNUS_ORION_HOST environment variable.
    * The port for Orion is `1026` but can be changed through the CYGNUS_ORION_PORT environment variable.
    * The user for Orion is empty but can be changed through the CYGNUS_ORION_USER environment variable.
    * The pass for Orion is empty but can be changed through the CYGNUS_ORION_PASS environment variable.
    * CYGNUS_ORION_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_ORION_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_ORION_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_ORION_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_ORION_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_ORION_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_ORION_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_ORION_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_ORION_BATCH_TTL: number of retries upon persistence error.
    * CYGNUS_ORION_SSL: SSL flag for connection to use with Orion.
    * CYGNUS_ORION_KEYSTONE_HOST: Keystone IDM host used by Orion sink to perform authentication.
    * CYGNUS_ORION_KEYSTONE_PORT: Keystone IDM port used by Orion sink to perform authentication.
    * CYGNUS_ORION_KEYSTONE_SSL: SSL flag for connection to use with Keystone IDM.
    * CYGNUS_ORION_FIWARE: Fiware Service header to provide to Orion sink.
    * CYGNUS_ORION_FIWARE_PATH=: Fiware ServicePath header to provide to Orion sink.

* Postgis:
    * It only works for building historical context data in Postgis.
    * The endpoint for Postgis is `iot-postgresql` but can be changed through the CYGNUS_POSTGIS_HOST environment variable.
    * The port for Postgis is `3306` but can be changed through the CYGNUS_POSTGIS_PORT environment variable.
    * The user for Postgis is `postgresql` but can be changed through the CYGNUS_POSTGIS_USER environment variable.
    * The pass for Postgis is `postgresql` but can be changed through the CYGNUS_POSTGIS_PASS environment variable.
    * CYGNUS_POSTGIS_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_POSTGIS_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_POSTGIS_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_POSTGIS_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_POSTGIS_SKIP_NAME_MAPPINGS_GENERATION: true if name mappings should not be generated empty at start, false otherwise. Default is false.
    * CYGNUS_POSTGIS_ENABLE_LOWERCASE: true if lower case is wanted to forced in all the element names, false otherwise.
    * CYGNUS_POSTGIS_ATTR_PERSISTENCE: how the attributes are stored, either per row either per column (row, column).
    * CYGNUS_POSTGIS_ATTR_NATIVE_TYPES: how the attribute are stored, using native type (true) or stringfy (false, by default).
    * CYGNUS_POSTGIS_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_POSTGIS_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_POSTGIS_BATCH_TTL: number of retries upon persistence error.

* Elasticsearch:
    * It only works for building historical context data in Elasticsearch.
    * CYGNUS_ELASTICSEARCH_HOST: the hostname of Elasticsearch server. Default is `localhost`, but this environment variable is mandatory so you have to set this environment variable explicitly.
    * CYGNUS_ELASTICSEARCH_PORT: the port number of Elasticsearch server. Default is `9200`, but this environment variable is mandatory so you have to set this environment variable explicitly.
    * CYGNUS_ELASTICSEARCH_SSL: true if connect to Elasticsearch server using SSL. Default is `false`, but this environment variable is mandatory so you have to set this environment variable explicitly.
    * CYGNUS_ELASTICSEARCH_SKIP_CONF_GENERATION: true skips the generation of the conf files, typically this files will be got from a volume, false autogenerate the conf files from the rest of environment variables.
    * CYGNUS_ELASTICSEARCH_INDEX_PREFIX: the prefix of index name. Default is `cygnus`.
    * CYGNUS_ELASTICSEARCH_MAPPING_TYPE: the mapping type name of Elasticsearch. Default is `cygnus_type`.
    * CYGNUS_ELASTICSEARCH_IGNORE_WHITE_SPACES: true if exclusively white space-based attribute values must be ignored, false otherwise. Default is `true`.
    * CYGNUS_ELASTICSEARCH_ATTR_PERSISTENCE: how the attributes are stored, either per row either per column (row, column). Default is `row`. (see [the document of NGSIElasticsearchSink](/doc/cygnus-ngsi/flume_extensions_catalogue/ngsi_elasticsearch_sink.md#section1.2.3) because there are some points to consider.)
    * CYGNUS_ELASTICSEARCH_TIMEZONE: timezone to be used as a document's timestamp. Default is `UTC`.
    * CYGNUS_ELASTICSEARCH_CAST_VALUE: true if cast the attrValue using attrType. (see [the document of NGSIElasticsearchSink](/doc/cygnus-ngsi/flume_extensions_catalogue/ngsi_elasticsearch_sink.md#section1.2.2).)
    * CYGNUS_ELASTICSEARCH_CACHE_FLASH_INTERVAL_SEC: 0 if notified data will be persisted to Elasticsearch immediately. positive integer if notified data are cached on container's memory and will be persisted to Elasticsearch periodically every `CYGNUS_ELASTICSEARCH_CACHE_FLASH_INTERVAL_SEC`. Default is `0`. (see [the document of NGSIElasticsearchSink](/doc/cygnus-ngsi/flume_extensions_catalogue/ngsi_elasticsearch_sink.md#section2.3.1) because there are some points to consider.)
    * CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONNS: Maximum number of connections allowed for a Http-based Elasticsearch backend. Default is `500`.
    * CYGNUS_ELASTICSEARCH_BACKEND_MAX_CONSS_PER_ROUTE: Maximum number of connections per route allowed for a Http-based Elasticsearch backend. Default is `100`.


* ArcGis:
    * It only works for building historical context data in ArcGis.
    * The url for ArcGis is `ArcGis` but can be changed through the CYGNUS_ARCGIS_URL environment variable.
    * The user for ArcGis is `ArcGis` but can be changed through the CYGNUS_ARCGIS_USER environment variable.
    * The pass for ArcGis is `ArcGis` but can be changed through the CYGNUS_ARCGIS_PASS environment variable.
    * CYGNUS_ARCGIS_ENABLE_ENCODING: true applies the new encoding, false applies the old encoding.
    * CYGNUS_ARCGIS_ENABLE_GROUPING: true if the grouping feature is enabled for this sink, false otherwise.
    * CYGNUS_ARCGIS_ENABLE_NAME_MAPPINGS: true if name mappings are enabled for this sink, false otherwise.
    * CYGNUS_ARCGIS_DATA_MODEL: select the data_model: dm-by-service-path, dm-by-entity or dm-by-entity-type.
    * CYGNUS_ARCGIS_BATCH_SIZE: number of notifications to be included within a processing batch.
    * CYGNUS_ARCGIS_BATCH_TIMEOUT: timeout for batch accumulation in seconds.
    * CYGNUS_ARCGIS_BATCH_TTL: number of retries upon persistence error.

* Log4j configuration file:
    * The logging level is `INFO` but can be changed through the CYGNUS_LOG_LEVEL environment variable.
    * The logging appender is `console` but can be changed through the CYGNUS_LOG_APPENDER environment variable.

* Monitoring:
    * CYGNUS_MONITORING_TYPE: monitoring type. Choose from `http` or `ganglia`. If it is not specified, it will be disabled.

### Docker Secrets

As an alternative to passing sensitive information via environment variables, `_FILE` may be appended to the previously listed environment variables, causing the initialization script to load the values for those variables from files present in the container. In particular, this can be used to load passwords from Docker secrets stored in `/run/secrets/<secret_name>` files. For example:

```bash
docker run --name some-cygnus -e CYGNUS_MYSQL_PASS_FILE=/run/secrets/mysql-root -d fiware/cygnus-ngsi:tag
```

Currently, this is only supported for:

* `CYGNUS_MYSQL_USER`
* `CYGNUS_MYSQL_PASS`
* `CYGNUS_MONGO_USER`
* `CYGNUS_MONGO_PASS`
* `CYGNUS_HDFS_USER`
* `CYGNUS_HDFS_TOKEN`
* `CYGNUS_POSTGRESQL_USER`
* `CYGNUS_POSTGRESQL_PASS`
* `CYGNUS_CARTO_USER`
* `CYGNUS_CARTO_KEY`




[Top](#top)

#### <a name="section3.2.1"></a>Editing the docker files
The easiest way is by editing both the `Dockerfile` and/or `agent.conf` file under `docker/cygnus-ngsi` and building the cygnus-ngsi image from the scratch.

This gives you total control on the docker image.

[Top](#top)

#### <a name="section3.2.2"></a>Environment variables
Those parameters associated to an environment variable can be easily overwritten in the command line using the `-e` option. For instance, if you want to change the log4j logging level, simply run:

    $ docker run -e CYGNUS_LOG_LEVEL='DEBUG' cygnus-ngsi

Or if you want to configure non empty MySQL user and password:

    $ docker run -e CYGNUS_MYSQL_USER='myuser' -e CYGNUS_MYSQL_PASS='mypass' cygnus-ngsi

[Top](#top)

#### <a name="section3.2.3"></a>Using volumes
Another possibility is to start a container with a volume (`-v` option) and map the entire configuration file within the container with a local version of the file:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf cygnus-ngsi

Of course, you can combine volumes and environment variables overwriting:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf -e LOG_LEVEL='DEBUG' cygnus-ngsi

[Top](#top)
