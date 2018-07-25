# <a name="top"></a>cygnus-common docker
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

    $ cd fiware-cygnus/docker/cygnus-common

And run the following command:

    $ docker build -t cygnus-common .

Once finished (it may take a while), you can check the available images at your docker by typing:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
cygnus-common       latest              0d2e537ac922        41 minutes ago      673.8 MB
centos              6                   61bf77ab8841        6 weeks ago         228.9 MB                          
```

[Top](#top)

### <a name="section2.2"></a>Using docker hub image
Instead of building an image from the scratch, you may download it from [hub.docker.com](https://hub.docker.com/r/fiware/cygnus-common/):

    $ docker pull fiware/cygnus-common

It can be listed the same way than above:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
cygnus-common       latest              0d2e537ac922        41 minutes ago      673.8 MB
centos              6                   61bf77ab8841        6 weeks ago         228.9 MB                          
```

[Top](#top)

## <a name="section3"></a>Using the image
### <a name="section3.1"></a>As it is
The cygnus-common image (either built from the scratch, either downloaded from hub.docker.com) allows running a Cygnus agent in charge of logging messages at INFO level. This is because the default agent configuration runs a [logger-sink](https://flume.apache.org/FlumeUserGuide.html#logger-sink).

Start a container for this image by typing in a terminal:

    $ docker run cygnus-common

Immediately after, you will start seeing cygnus-common logging traces:

```
+ exec /usr/lib/jvm/java-1.7.0/bin/java -Xmx20m -Dflume.root.logger=INFO,console -cp '/opt/apache-flume/conf:/opt/apache-flume/lib/*:/opt/apache-flume/plugins.d/cygnus/lib/*:/opt/apache-flume/plugins.d/cygnus/libext/*' -Djava.library.path= com.telefonica.iot.cygnus.nodes.CygnusApplication -f /opt/apache-flume/conf/agent.conf -n cygnus-common
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/apache-flume/lib/slf4j-log4j12-1.6.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/libext/cygnus-common-1.0.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
time=2016-05-17T06:36:23.867UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp= | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[166] : Starting Cygnus, version 1.0.0_SNAPSHOT.d7cfee4455a59a1854cc53f37e16ff4866b26010
...
...
time=2016-05-17T06:36:25.046UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp=cygnus-common | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 5080 (Management Interface)
```

You can check the running container (in a second terminal shell):

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                CREATED             STATUS              PORTS                NAMES
c88bc1b66cdc        cygnus-common       "/cygnus-entrypoint.   6 seconds ago       Up 5 seconds        5050/tcp, 5080/tcp   naughty_mayer  
```

You can check the IP address of the container above by doing:

```
$ docker inspect c88bc1b66cdc | grep \"IPAddress\"
        "IPAddress": "172.17.0.8",
```

Once the IP address of the container is gotten, you may ask for the Cygnus version (in a second terminal shell):

```
$ curl "http://172.17.0.8:5080/v1/version"
{"success":"true","version":"1.0.0_SNAPSHOT.d7cfee4455a59a1854cc53f37e16ff4866b26010"}
```

Even you can use API methods to see how to cygnus-common print the logs at INFO level.

```
$ curl -X POST "http://172.17.0.7:5080/v1/subscriptions?ngsi_version=2" -d '{"subscription":{"description": "title_of_subscription","subject": {"entities": [{"idPattern": ".*","type": "Room"}],"condition": {"attrs": ["attr1"],"expression": {"q": "attr1>40"}}},"notification": {"http": {"url": "http://localhost:1234"},"attrs": ["attr1","attr2"]},"expires": "2016-05-05T14:00:00.00Z","throttling": 5}, "endpoint":{"host":"orion_host", "port":"orion_port", "ssl":"false", "xauthtoken":"your_auth_token"}}'
{"success":"true","result" : { SubscriptionID = 573ac3b6aba73680b1905f5c}
```

You will be able to see something like the following in the cygnus-common terminal:

```
time=2016-05-17T07:09:40.956UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=handle | comp=cygnus-common | msg=com.telefonica.iot.cygnus.management.ManagementInterface[131] : Management interface request. Method: POST, URI: /v1/subscriptions
time=2016-05-17T07:09:41.343UTC | lvl=INFO | corr=dfca71a9-41f1-4f98-8fa8-4f15612a72db | trans=dfca71a9-41f1-4f98-8fa8-4f15612a72db | srv= | subsrv= | function=<init> | comp=cygnus-common | msg=com.telefonica.iot.cygnus.backends.http.HttpClientFactory[79] : Setting max total connections (500)
time=2016-05-17T07:09:41.343UTC | lvl=INFO | corr=dfca71a9-41f1-4f98-8fa8-4f15612a72db | trans=dfca71a9-41f1-4f98-8fa8-4f15612a72db | srv= | subsrv= | function=<init> | comp=cygnus-common | msg=com.telefonica.iot.cygnus.backends.http.HttpClientFactory[80] : Setting default max connections per route (100)
```

You can stop the container as:

```
$ docker stop c88bc1b66cdc
c88bc1b66cdc
$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

[Top](#top)

### <a name="section3.2"></a>Using a specific configuration
As seen above, the default configuration distributed with the image is tied to certain values that may not be suitable for you tests. Specifically:

* The logging level is `INFO`.
* The logging appender is `console`.

You may need to alter the above values with values of your own.

[Top](#top)

#### <a name="section3.2.1"></a>Editing the docker files
The easiest way is by editing both the `Dockerfile` and/or `agent.conf` file under `docker/cygnus-common` and building the cygnus-common image from the scratch.

This gives you total control on the docker image.

[Top](#top)

#### <a name="section3.2.2"></a>Environment variables
Those parameters associated to an environment variable can be easily overwritten in the command line using the `-e` option. For instance, if you want to change the log4j logging level, simply run:

    $ docker run -e LOG_LEVEL='DEBUG' cygnus-common

[Top](#top)

#### <a name="section3.2.3"></a>Using volumes
Another possibility is to start a container with a volume (`-v` option) and map the entire configuration file within the container with a local version of the file:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf cygnus-common-1

Of course, you can combine volumes and environment variables overwriting:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf -e LOG_LEVEL='DEBUG' cygnus-common

[Top](#top)
