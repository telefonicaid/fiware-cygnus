# <a name="top"></a>cygnus-ngsi-ld docker
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

    $ sudo docker build -f docker/cygnus-ngsi-ld/Dockerfile -t cygnus-ngsi-ld .

Once finished (it may take a while), you can check the available images at your docker by typing:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
cygnus-ngsi-ld         latest              6a9e16550c82        10 seconds ago      462.1 MB
centos              6                   273a1eca2d3a        2 weeks ago         194.6 MB
```

[Top](#top)

### <a name="section2.2"></a>Using docker hub image
Instead of building an image from the scratch, you may download it from [hub.docker.com](https://hub.docker.com/r/fiware/cygnus-ngsi-ld/):

    $ docker pull fiware/cygnus-ngsi-ld

It can be listed the same way than above:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
cygnus-ngsi-ld         latest              6a9e16550c82        10 seconds ago      462.1 MB
centos              6                   273a1eca2d3a        2 weeks ago         194.6 MB
```

[Top](#top)

## <a name="section3"></a>Using the image
### <a name="section3.1"></a>As it is
The cygnus-ngsi-ld image (either built from the scratch, either downloaded from hub.docker.com) allows running a Cygnus agent in charge of receiving NGSI-like notifications and persisting them into wide variety of storages: MySQL (Running in a  `iot-mysql` host), MongoDB and STH (running in a  `iot-mongo` host), CKAN (running in `iot-ckan` host), HDFS (running in `iot-hdfs` host) and Carto (a cloud service at `https://<your_user>.cartodb.com`).

Start a container for this image by typing in a terminal:

    $ docker run cygnus-ngsi-ld

Immediately after, you will start seeing cygnus-ngsi-ld logging traces (MySQL example):

```
+ exec /usr/lib/jvm/java-1.7.0/bin/java -Xmx20m -Dflume.root.logger=INFO,console -cp '/opt/apache-flume/conf:/opt/apache-flume/lib/*:/opt/apache-flume/plugins.d/cygnus/lib/*:/opt/apache-flume/plugins.d/cygnus/libext/*' -Djava.library.path= com.telefonica.iot.cygnus.nodes.CygnusApplication -f /opt/apache-flume/conf/agent.conf -n cygnus-ngsi-ld
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/apache-flume/lib/slf4j-log4j12-1.6.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/lib/cygnus-ngsi-ld-0.13.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/libext/cygnus-common-0.13.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
time=2016-05-05T09:57:55.150UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp= | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[166] : Starting Cygnus, version 0.13.0_SNAPSHOT.5200773899b468930e82df4a0b34d44fd4632893
...
...
time=2016-05-05T09:57:56.287UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 5080 (Management Interface)
```

You can check the running container (in a second terminal shell):

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                CREATED              STATUS              PORTS                NAMES
9ce0f09f5676        cygnus-ngsi-ld         "/cygnus-entrypoint.   About a minute ago   Up About a minute   5050/tcp, 5080/tcp   focused_kilby
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

Even, you may send a NGSI-like notification emulation (please, check the notification examples at [cygnus-ngsi-ld](cygnus-ngsi-ld/resources/ngsi-examples)):

```
$ ./notification.sh http://172.17.0.13:5050/notify
* About to connect() to 172.17.0.13 port 5050 (#0)
*   Trying 172.17.0.13... connected
* Connected to 172.17.0.13 (172.17.0.13) port 5050 (#0)
> POST /notify HTTP/1.1
> Host: 172.17.0.13:5050
> Content-Type: application/json
> Accept: application/json
> User-Agent: orion/2.2.0
> Fiware-Service: default
> Fiware-ServicePath: /
> ngsiv2-attrsformat: normalized
> Content-Length: 460
>
< HTTP/1.1 200 OK
< Transfer-Encoding: chunked
< Server: Jetty(6.1.26)
<
* Connection #0 to host 172.17.0.13 left intact
* Closing connection #0
```

You will be able to see something like the following in the cygnus-ngsi-ld terminal (MySQL example):

```
time=2016-05-05T10:01:22.111UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=getEvents | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[249] : Starting internal transaction (8bed4f8d-c47f-499a-a70d-365883584ac7)
time=2016-05-05T10:01:22.113UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=getEvents | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[265] : Received data ({  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "attributes" : [          {            "name" : "temperature",            "type" : "centigrade",            "value" : "26.5"          }        ],        "type" : "Room",        "isPattern" : "false",        "id" : "Room1"      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-05-05T10:01:31.687UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.sinks.NGSISink[342] : Batch accumulation time reached, the batch will be processed as it is
time=2016-05-05T10:01:31.689UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-05-05T10:01:31.708UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=persistAggregation | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.sinks.NGSIMySQLSink[501] : [mysql-sink] Persisting data at OrionMySQLSink. Database (default), Table (Room1_Room), Fields ((recvTimeTs,recvTime,fiwareServicePath,entityId,entityType,attrName,attrType,attrValue,attrMd)), Values (('1462442482115','2016-05-05T10:01:22.115','/','Room1','Room','temperature','centigrade','26.5','[]'))
time=2016-05-05T10:01:32.050UTC | lvl=ERROR | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.sinks.NGSISink[411] : Persistence error (Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.)
time=2016-05-05T10:01:32.051UTC | lvl=INFO | corr=8bed4f8d-c47f-499a-a70d-365883584ac7 | trans=8bed4f8d-c47f-499a-a70d-365883584ac7 | srv=default | subsrv=/ | function=processNewBatches | comp=cygnus-ngsi-ld | msg=com.telefonica.iot.cygnus.sinks.NGSISink[423] : TTL exhausted, finishing internal transaction (8bed4f8d-c47f-499a-a70d-365883584ac7)
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

* Configuration generation:
    * Handle generation of configuration: CYGNUS_SKIP_CONF_GENERATION. If enabled (i.e. env var set to false) a configuration will be created for container, if disabled (i.e. env var set to true) then your should provide a configuration file.

* Multiagent:
    * Enable multiagent cygnus: CYGNUS_MULTIAGENT environment variable. If enabled, each sink will have a different configuration file and will be executed by a different cygnus agent (java process). If disabled, all sinks are configured in the same agent configuration file and are executed by the same agent (java procss).
    In both cases, multiagent or not, each cygnus sink will always run in a diferent port (multisink):

| sink   | port   | admin_port |
|--:|--:|--:|

| postgresql | 5054 | 5084 |



* PostgreSQL:
    * It only works for building historical context data in PostgreSQL.
    * The endpoint for PostgreSQL is `iot-postgresql` but can be changed through the CYGNUS_POSTGRESQL_HOST environment variable.
    * The port for PostgreSQL is `5432` but can be changed through the CYGNUS_POSTGRESQL_PORT environment variable.
    * The user for PostgreSQL is `postgresql` but can be changed through the CYGNUS_POSTGRESQL_USER environment variable.
    * The pass for PostgreSQL is `postgresql` but can be changed through the CYGNUS_POSTGRESQL_PASS environment variable.
    * The database for PostgreSQL is `postgres` but can be changed through the CYGNUS_POSTGRESQL_DATABASE environment variable.
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
    * CYGNUS_POSTGRESQL_DATA_MODEL: select the data_model: dm-by-service-path, dm-by-entity or dm-by-entity-type.
    * CYGNUS_POSTGRESQL_OPTIONS: the jdbc optional parameters string which concatinates to jdbc url.

* Log4j configuration file:
    * The logging level is `INFO` but can be changed through the CYGNUS_LOG_LEVEL environment variable.
    * The logging appender is `console` but can be changed through the CYGNUS_LOG_APPENDER environment variable.

* Monitoring:
    * CYGNUS_MONITORING_TYPE: monitoring type. Choose from `http` or `ganglia`. If it is not specified, it will be disabled.

### Docker Secrets

As an alternative to passing sensitive information via environment variables, `_FILE` may be appended to the previously listed environment variables, causing the initialization script to load the values for those variables from files present in the container. In particular, this can be used to load passwords from Docker secrets stored in `/run/secrets/<secret_name>` files. For example:

```bash
docker run --name some-cygnus -e CYGNUS_POSTGRESQL_PASS_FILE=/run/secrets/postgresql-root -d fiware/cygnus-ngsi-ld:tag
```

Currently, this is only supported for:

* `CYGNUS_POSTGRESQL_USER`
* `CYGNUS_POSTGRESQL_PASS`




[Top](#top)

#### <a name="section3.2.1"></a>Editing the docker files
The easiest way is by editing both the `Dockerfile` and/or `agent.conf` file under `docker/cygnus-ngsi-ld` and building the cygnus-ngsi-ld image from the scratch.

This gives you total control on the docker image.

[Top](#top)

#### <a name="section3.2.2"></a>Environment variables
Those parameters associated to an environment variable can be easily overwritten in the command line using the `-e` option. For instance, if you want to change the log4j logging level, simply run:

    $ docker run -e CYGNUS_LOG_LEVEL='DEBUG' cygnus-ngsi-ld

Or if you want to configure non empty POSTGRESQL user and password:

    $ docker run -e CYGNUS_POSTGRESQL_USER='myuser' -e CYGNUS_POSTGRESQL_PASS='mypass' cygnus-ngsi-ld

[Top](#top)

#### <a name="section3.2.3"></a>Using volumes
Another possibility is to start a container with a volume (`-v` option) and map the entire configuration file within the container with a local version of the file:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf cygnus-ngsi-ld

Of course, you can combine volumes and environment variables overwriting:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf -e LOG_LEVEL='DEBUG' cygnus-ngsi-ld

[Top](#top)
