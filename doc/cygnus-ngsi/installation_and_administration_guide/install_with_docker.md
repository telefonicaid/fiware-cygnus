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
time=2016-05-05T09:57:56.287UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp=cygnus-ngsi | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 8081 (Management Interface)
```

You can check the running container (in a second terminal shell):

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                CREATED              STATUS              PORTS                NAMES
9ce0f09f5676        cygnus-ngsi         "/cygnus-entrypoint.   About a minute ago   Up About a minute   5050/tcp, 8081/tcp   focused_kilby
```

You can check the IP address of the container above by doing:

```
$ docker inspect 9ce0f09f5676 | grep \"IPAddress\"
        "IPAddress": "172.17.0.13",
```

Once the IP address of the container is gotten, you may ask for the Cygnus version (in a second terminal shell):

```
$ curl "http://172.17.0.13:8081/v1/version"
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

[Top](#top)

### <a name="section3.2"></a>Using a specific configuration
As seen above, the default configuration distributed with the image is tied to certain values that may not be suitable for you tests. Specifically:

* MySQL:
    * It only works for building historical context data in MySQL.
    * The endpoint for MYSQL is `iot-mysql` but can be changed through the CYGNUS_MYSQL_HOST environment variable.
    * The port for MYSQL is `3306` but can be changed through the CYGNUS_MYSQL_PORT environment variable.
    * The user for MySQL is `mysql` but can be changed through the CYGNUS_MYSQL_USER environment variable.
    * The pass for MySQL is `mysql` but can be changed through the CYGNUS_MYSQL_PASS environment variable.
* Mongo and STH:
    * Mongo only works for building historical context data in Mongo.
    * STH only works for building historical context data in STH.
    * The endpoint for Mongo and STH, containing host and port, is `iot-mongo:27017` but can be changed through the CYGNUS_MONGO_HOSTS environment variable.
    * The user for Mongo and STH is `mongo` but can be changed through the CYGNUS_MONGO_USER environment variable.
    * The pass for Mongo and STH is `mongo` but can be changed through the CYGNUS_MONGO_PASS environment variable.
* CKAN:
    * It only works for building historical context data in CKAN.
    * The endpoint for CKAN is `iot-ckan` but can be changed through the CYGNUS_CKAN_HOST environment variable.
    * The port for CKAN is `80` but can be changed through the CYGNUS_CKAN_PORT environment variable.
    * The ssl for CKAN is `false` but can be changed through the CYGNUS_CKAN_SSL environment variable.
    * The api_key for CKAN is `` but can be changed through the CYGNUS_CKAN_API_KEY environment variable.
* HDFS:
    * It only works for building historical context data in HDFS.
    * The endpoint for HDFS is `iot-hdfs` but can be changed through the CYGNUS_HDFS_HOST environment variable.
    * The port for HDFS is `50070` but can be changed through the CYGNUS_HDFS_PORT environment variable.
    * The user for HDFS is `hdfs` but can be changed through the CYGNUS_HDFS_USER environment variable.
    * The token for HDFS is empty but can be set through the CYGNUS_HDFS_TOKEN environment variable
* Carto:
    * It only works for building historical context data in Carto.
    * The user for Carto is `carto` but can be changed through the CYGNUS_CARTO_USER environment variable.
    * The key for Carto is `carto` but can be changes through the CYGNUS_CARTO_KEY environment variable.
* Log4j configuration file:
    * The logging level is `INFO` but can be changed through the CYGNUS_LOG_LEVEL environment variable.
    * The logging appender is `console` but can be changed through the CYGNUS_LOG_APPENDER environment variable.

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
