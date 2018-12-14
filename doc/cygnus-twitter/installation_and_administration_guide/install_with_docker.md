# <a name="top"></a>cygnus-twitter docker
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

    $ cd fiware-cygnus/docker/cygnus-twitter

And run the following command:

    $ docker build -t cygnus-twitter .

Once finished (it may take a while), you can check the available images at your docker by typing:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED              VIRTUAL SIZE
cygnus-twitter      latest              df556c817163        About a minute ago   1.132 GB
centos              6                   61bf77ab8841        4 weeks ago          228.9 MB
```

[Top](#top)

### <a name="section2.2"></a>Using docker hub image
Instead of building an image from the scratch, you may download it from [hub.docker.com](https://hub.docker.com/r/fiware/cygnus-twitter/):

    $ docker pull fiware/cygnus-twitter

It can be listed the same way than above:

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED              VIRTUAL SIZE
cygnus-twitter      latest              df556c817163        About a minute ago   1.132 GB
centos              6                   61bf77ab8841        4 weeks ago          228.9 MB
```

[Top](#top)

## <a name="section3"></a>Using the image
### <a name="section3.1"></a>As it is
The cygnus-twitter image (either built from the scratch, either downloaded from hub.docker.com) allows running a Cygnus agent in charge of receiving tweets from Twitter and persiting them into a HDFS storage.

Start a container for this image by typing in a terminal:

    $ docker run cygnus-twitter

Immediatelly after, you will start seeing cygnus-twitter logging traces:

```
+ exec /usr/lib/jvm/java-1.7.0/bin/java -Xmx20m -Dflume.root.logger=INFO,console -cp '/opt/apache-flume/conf:/opt/apache-flume/lib/*:/opt/apache-flume/plugins.d/cygnus/lib/*:/opt/apache-flume/plugins.d/cygnus/libext/*' -Djava.library.path= com.telefonica.iot.cygnus.nodes.CygnusApplication -f /opt/apache-flume/conf/agent.conf -n cygnus-twitter
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/apache-flume/lib/slf4j-log4j12-1.6.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/lib/cygnus-twitter-0.1.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/apache-flume/plugins.d/cygnus/libext/cygnus-common-0.13.0_SNAPSHOT-jar-with-dependencies.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
time=2016-05-05T09:57:55.150UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp= | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[166] : Starting Cygnus, version 0.13.0_SNAPSHOT.5200773899b468930e82df4a0b34d44fd4632893
...
...
time=2016-05-05T09:57:56.287UTC | lvl=INFO | corr= | trans= | srv= | subsrv= | function=main | comp=cygnus-twitter | msg=com.telefonica.iot.cygnus.nodes.CygnusApplication[286] : Starting a Jetty server listening on port 5080 (Management Interface)
```

You can check the running container (in a second terminal shell):

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                CREATED              STATUS              PORTS                NAMES
9ce0f09f5676        cygnus-twitter      "/cygnus-entrypoint.   About a minute ago   Up About a minute   5050/tcp, 5080/tcp   focused_kilby
```

You can check the IP address of the container above by doing:

```
$ docker inspect 9ce0f09f5676 | grep \"IPAddress\"
        "IPAddress": "172.17.0.13",
```

Once the IP address of the container is gotten, you may ask for the Cygnus version (in a second terminal shell):

```
$ $ curl "http://172.17.0.13:5080/v1/version"
{"success":"true","version":"0.13.0_SNAPSHOT.5200773899b468930e82df4a0b34d44fd4632893"}
```


You can stop the container as:

```
$ docker stop 9ce0f09f5676
9ce0f09f5676
$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

[Top](#top)

### <a name="section3.2"></a>Using a specific configuration
As seen above, the default configuation distributed with the image is tied to certain values that may not be suitable for you tests. Specifically:

* It only works for storing streaming tweets in a temporal file (/tmp).
* The logging level is `INFO`.
* The logging appender is `console`.

You may need to alter the above values with values of your own.

[Top](#top)

#### <a name="section3.2.1"></a>Editing the docker files
The easiest way is by editing both the `Dockerfile` and/or `agent.conf` file under `docker/cygnus-twitter` and building the cygnus-twitter image from the scratch.

This gives you total control on the docker image.

[Top](#top)

#### <a name="section3.2.2"></a>Environment variables
Those parameters associated to an environment variable can be easily overwritten in the command line using the `-e` option. For instance, if you want to change the log4j logging level, simply run:

    $ docker run -e LOG_LEVEL='DEBUG' cygnus-twitter

[Top](#top)

#### <a name="section3.2.3"></a>Using volumes
Another possibility is to start a container with a volume (`-v` option) and map the entire configuraton file within the container with a local version of the file:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf cygnus-twitter

Of course, you can combine volumes and environment variables overwritting:

    $ docker run -v /absolute/path/to/local/agent.conf:/opt/apache-flume/conf/agent.conf -e LOG_LEVEL='DEBUG' cygnus-twitter

[Top](#top)
