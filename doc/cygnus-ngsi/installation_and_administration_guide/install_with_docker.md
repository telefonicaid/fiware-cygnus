#<a name="top"></a>cygnus-ngsi docker
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

##<a name="section1"></a>Before starting
Obviously, you will need docker installed and running in you machine. Please, check [this](https://docs.docker.com/linux/started/) official start guide.

[Top](#top)

##<a name="section2"></a>Getting an image
###<a name="section2.1"></a>Building from sources
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

###<a name="section2.2"></a>Using docker hub image
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

##<a name="section3"></a>Using the image
###<a name="section3.1"></a>As it is
The cygnus-ngsi image (either built from the scratch, either downloaded from hub.docker.com) allows running a Cygnus agent in charge of receiving NGSI-like notifications and persisting them into wide variety of storages: MySQL (Running in a  `mysql` host), MongoDB (running in a  `mongo` host), STH (running in `sth` host) and  CKAN (running in `ckan` host).

Start a container for this image by typing in a terminal:

    $ docker run cygnus-ngsi

Immediately after, you will start seeing cygnus-ngsi logging traces (MySQL example):

```
+ exec /usr/lib/jvm/java-1.6.0/bin/java -Xmx20m -Dflume.root.logger=INFO,console -cp '/opt/apache-flume/conf:/opt/apache-flume/lib/*:/opt/apache-flume/plugins.d/cygnus/lib/*:/opt/apache-flume/plugins.d/cygnus/libext/*' -Djava.library.path= com.telefonica.iot.cygnus.nodes.CygnusApplication -f /opt/apache-flume/conf/agent.conf -n cygnus-ngsi
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

###<a name="section3.2"></a>Using a specific configuration
As seen above, the default configuration distributed with the image is tied to certain values that may not be suitable for you tests. Specifically:

* MySQL:
  * It only works for building historical context data in MySQL.
  * The user for MySQL is `mysql`.
  * The pass for MySQL is `mysql`.
  * The logging level is `INFO`.
  * The logging appender is `console`.
* Mongo:
  * It only works for building historical context data in Mongo.
  * The user for Mongo is `mongo`.
  * The pass for Mongo is `mongo`.
  * The logging level is `INFO`.
  * The logging appender is `console`.
* STH:
  * It only works for building historical context data in STH.
  * The user for STH is `mongo`.
  * The pass for STH is `mongo`.
  * The logging level is `INFO`.
  * The logging appender is `console`.
* CKAN:
  * It only works for building historical context data in CKAN.
  * The endpoint for CKAN is `iot-ckan`.
  * The port for CKAN is `80`.
  * The ssl for CKAN is `false`.
  * The api_key for CKAN is ``.
* Log4j configuration file:
  * The logging level is `INFO`.
  * The logging appender is `console`.
  
```
# Copyright 2016 Telefónica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FI-WARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es

# To be put in APACHE_FLUME_HOME/conf/log4j.properties

# Define some default values.
# These can be overridden by system properties, e.g. the following logs in the standard output, which is very useful
# for testing purposes (-Dflume.root.logger=DEBUG,console)
flume.root.logger=INFO,LOGFILE
#flume.root.logger=DEBUG,console
flume.log.dir=/var/log/cygnus/
flume.log.file=cygnus.log

# Logging level for third party components.
log4j.logger.org.apache.flume.lifecycle = WARN
log4j.logger.org.jboss = WARN
log4j.logger.org.mortbay = WARN
log4j.logger.org.apache.avro.ipc.NettyTransceiver = WARN
log4j.logger.org.apache.hadoop = WARN
log4j.logger.org.mongodb = WARN
log4j.logger.org.apache.http = WARN
log4j.logger.org.apache.zookeeper = WARN
log4j.logger.org.apache.kafka = WARN
log4j.logger.org.I0Itec = WARN
log4j.logger.com.amazonaws = WARN

# Define the root logger to the system property "flume.root.logger".
log4j.rootLogger=${flume.root.logger}

# Stock log4j rolling file appender.
# Default log rotation configuration.
log4j.appender.LOGFILE=org.apache.log4j.RollingFileAppender
log4j.appender.LOGFILE.MaxFileSize=100MB
log4j.appender.LOGFILE.MaxBackupIndex=10
log4j.appender.LOGFILE.File=${flume.log.dir}/${flume.log.file}
log4j.appender.LOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.LOGFILE.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | corr=%X{correlatorId} | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

# Warning: If you enable the following appender it will fill up your disk if you don't have a cleanup job!
# cleanup job example: find /var/log/cygnus -type f -mtime +30 -exec rm -f {} \;
# This uses the updated rolling file appender from log4j-extras that supports a reliable time-based rolling policy.
# See http://logging.apache.org/log4j/companions/extras/apidocs/org/apache/log4j/rolling/TimeBasedRollingPolicy.html
# Add "DAILY" to flume.root.logger above if you want to use this.
log4j.appender.DAILY=org.apache.log4j.rolling.RollingFileAppender
log4j.appender.DAILY.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
log4j.appender.DAILY.rollingPolicy.ActiveFileName=${flume.log.dir}/${flume.log.file}
log4j.appender.DAILY.rollingPolicy.FileNamePattern=${flume.log.dir}/${flume.log.file}.%d{yyyy-MM-dd}
log4j.appender.DAILY.layout=org.apache.log4j.PatternLayout
log4j.appender.DAILY.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | corr=%X{correlatorId} | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

# Console appender, i.e. printing logs in the standard output.
# Add "console" to flume.root.logger above if you want to use this.
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.err
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | corr=%X{correlatorId} | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n
```

You may need to alter the above values with values of your own.

[Top](#top)

####<a name="section3.2.1"></a>Editing the docker files
The easiest way is by editing both the `Dockerfile` and/or `agent.conf` file under `docker/cygnus-ngsi` and building the cygnus-ngsi image from the scratch.

This gives you total control on the docker image.

[Top](#top)

####<a name="section3.2.2"></a>Environment variables
Those parameters associated to an environment variable can be easily overwritten in the command line using the `-e` option. For instance, if you want to change the log4j logging level, simply run:

    $ docker run -e CYGNUS_LOG_LEVEL='DEBUG' cygnus-ngsi

Or if you want to configure non empty MySQL user and password:

    $ docker run -e CYGNUS_MYSQL_USER='myuser' -e CYGNUS_MYSQL_PASS='mypass' cygnus-ngsi

[Top](#top)

####<a name="section3.2.3"></a>Using volumes
Another possibility is to start a container with a volume (`-v` option) and map the entire configuration file within the container with a local version of the file:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf cygnus-ngsi

Of course, you can combine volumes and environment variables overwriting:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf -e LOG_LEVEL='DEBUG' cygnus-ngsi

[Top](#top)
