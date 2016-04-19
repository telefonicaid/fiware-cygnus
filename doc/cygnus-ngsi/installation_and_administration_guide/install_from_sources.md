#<a name="top"></a>Installing Cygnus from sources
Content:

* [Prerequisites](#section1)
* [Cygnus user creation](#section2)
* [Installing Apache Flume](#section3)
* [Installing Cygnus](#section4)
    * [Cloning `fiware-cygnus`](#section4.1)
    * [Installing `cygnus-common`](#section4.2)
    * [Installing `cygnus-ngsi`](#section4.3)
    * [Known issues](#section4.4)
* [Installing dependencies](#section5)
    * [Cygnus dependencies](#section5.1)
    * [OrionCKANSink dependencies](#section5.2)
    * [OrionHDFSSink dependencies](#section5.3)
    * [OrionMySQLSink dependencies](#section5.4)
    * [OrionDynamoDBSink dependencies](#section5.5)
    * [OrionMongoSink dependencies](#section5.6)
    * [OrionSTHSink dependencies](#section5.7)
    * [OrionKafkaSink dependencies](#section5.8)
    * [OrionTestSink dependencies](#section5.9)

##<a name="section1"></a>Prerequisites
Maven (and thus Java SDK, since Maven is a Java tool) is needed in order to install Cygnus.

In order to install Java SDK (not JRE), just type (CentOS machines):

    $ yum install java-1.6.0-openjdk-devel

Remember to export the JAVA_HOME environment variable. In the case of using `yum install` as shown above, it would be:

    $ export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64

In order to do it permanently, edit `/root/.bash_profile` (`root` user) or `/etc/profile` (other users).

Maven is installed by downloading it from [maven.apache.org](http://maven.apache.org/download.cgi). Install it in a folder of your choice (represented by `APACHE_MAVEN_HOME`):

    $ wget http://www.eu.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
    $ tar xzvf apache-maven-3.2.5-bin.tar.gz
    $ mv apache-maven-3.2.5 APACHE_MAVEN_HOME
    
[Top](#top)

##<a name="section2"></a>Cygnus user creation
It is highly recommended to create a `cygnus` Unix user, under which Cygnus will be installed and run. By the way, this is how the [RPM](./install_with_rpm.md) proceeds.

Creating such a user is quite simple. As a sudoer user (root or any other allowed), type the following:

    $ (sudo) useradd cygnus
    
You may add a password or not to the `cygnus` user:

    $ (sudo) passwd cygnus
    
Once created, change to this new fresh user in order to proceed with the rest of the installation:

    $ su - cygnus
    
[Top](#top)

##<a name="section3"></a>Installing Apache Flume
Apache Flume can be easily installed by downloading its latests version from [flume.apache.org](http://flume.apache.org/download.html). Move the untared directory to a folder of your choice (represented by `APACHE_FLUME_HOME`):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME
    $ mv APACHE_FLUME_HOME/lib/httpclient-4.2.1.jar APACHE_FLUME_HOME/lib/httpclient-4.2.1.jar.old
    $ mv APACHE_FLUME_HOME/lib/httpcore-4.2.1.jar APACHE_FLUME_HOME/lib/httpcore-4.2.1.jar.old
    $ mv APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar.old
    $ mkdir -p APACHE_FLUME_HOME/plugins.d/cygnus/
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/libext

Some remarks:

* The creation of the `plugins.d` directory is related to the installation of third-party software, like Cygnus.
* Please observe the version of `httpcomponents-core` and `httpcomponents-client` in the `pom.xml` (`httpclient-4.3.1.jar and httpcore-4.3.1.jar`) don't match match the version of such packages within the Flume bundle (`httpclient-4.2.1.jar and httpcore-4.2.1.jar`). In order the most recent version of the libraries, the ones within the Flume bundle must be removed (or renamed).

[Top](#top)

##<a name="section4"></a>Installing Cygnus
###<a name="section4.1"></a>Cloning `fiware-cygnus`
Start by cloning the Github repository:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ git checkout <branch>
    
`<branch>` should be typically a stable release branch, e.g. `release/0.13.0`, but could also be `master` (synchronized with the latest release) or `develop` (contains the latest not stable changes).

[Top](#top)

###<a name="section4.2"></a>Installing `cygnus-common`
`cygnus-ngsi` agent requires `cygnus-common`. At the moment of writing, this kind of dependency is not available at any Maven repository, thus must be built and installed from the sources.

Thus, the developed classes must be packaged in a Java jar file. This can be done as a fat Java jar containing all the third-party dependencies  (**recommended**). You may need to edit the `pom.xml` (\*):

    $ cd cygnus-common
    $ APACHE_MAVEN_HOME/bin/mvn clean compile exec:exec assembly:single
    $ cp target/cygnus-common-<x.y.z>-jar-with-dependencies.jar APACHE_FLUME_HOME/plugins.d/cygnus/libext

Or as a thin Java jar file containing only the `cygnus-common` classes. You may need to edit the `pom.xml` if necessary (\*):

    $ cd cygnus-common
    $ APACHE_MAVEN_HOME/bin/mvn exec:exec package
    $ cp target/cygnus-<x.y.z>.jar APACHE_FLUME_HOME/plugins.d/cygnus/libext

Please observe in this case, and depending on the Cygnus components you are going to use, you may need to install [additional](#section5) third-party .jar files under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/`. Typically, you can get these jar files from your Maven repository (under `.m2` in your user home directory) and use the `cp` command.

In both cases, the `cygnus-common` dependency must be installed at Maven as well, in order to build `cygnus-ngsi`. Just run the following command:

    $ mvn install:install-file -Dfile=APACHE_MAVEN_HOME/plugins.d/cygnus/libext/cygnus-common-<x.y.z>-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=<x.y.z> -Dpackaging=jar -DgeneratePom=false

(*) Please have into account from Cygnus 0.10.0 the version of `hadoop-core` within the `pom.xml` must match the Hadoop version you are going to use; on the contrary, the HDFS sink will not work. Of course, if you are not going to use the HDFS sink, simply use the default `hadoop-core` version (1.2.1) within the downloaded `pom.xml` for correct compilation purposes.

[Top](#top)

###<a name="section4.3"></a>Installing `cygnus-ngsi`
`cygnus-ngsi` can be built as a fat Java jar file containing all third-party dependencies (**recommended**):

    $ cd cygnus-ngsi
    $ APACHE_MAVEN_HOME/bin/mvn clean compile exec:exec assembly:single
    $ cp target/cygnus-ngsi-<x.y.z>-jar-with-dependencies.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    
Or as a thin Java jar file:

    $ cd cygnus-ngsi
    $ APACHE_MAVEN_HOME/bin/mvn exec:exec package
    $ cp target/cygnus-<x.y.z>.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    
In both cases, the installation is completed by copying the `cygnus-flume-ng` script into `APACHE_FLUME_HOME/bin`:

    $ cp target/classes/cygnus-flume-ng APACHE_FLUME_HOME/bin
    $ chmod a+x APACHE_FLUME_HOME/bin/cygnus-flume-ng

[Top](#top)

###<a name="section4.4"></a>Known issues
It may happen while compiling either `cygnus-common` either `cygnus-ngsi` the Maven JVM has not enough memory. This can be chaged as detailed at the [Maven official documentation](https://cwiki.apache.org/confluence/display/MAVEN/OutOfMemoryError):

    $ export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"

[Top](#top)

##<a name="section5"></a>Installing dependencies
###<a name="section5.1"></a>Cygnus dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|  Cygnus dependencies  |   Version   |
|:----------------------|------------:|
|     flume-ng-core     |    1.4.0    | 
|     flume-ng-node     |    1.4.0    |
|          gson         |    2.2.4    |
|      json-simple      |     1.1     |
|        xml-apis       |    1.2.01   |
|         log4j         |    1.2.17   |

[Top](#top)

###<a name="section5.2"></a>OrionCKANSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|  OrionCKANSink dependencies |   Version    |
|:----------------------------|-------------:|
|          httpclient         |    4.3.1     |
|           httpcore          |    4.3.1     |

[Top](#top)

###<a name="section5.3"></a>OrionHDFSSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|  OrionHDFSSink dependencies  |      Version       |
|:-----------------------------|-------------------:|
|         hadoop-core          |  1.2.1 (or higher) |
|          hive-exec           |       0.13.0       |
|          hive-jdbc           |       0.13.0       |
|          httpclient          |       4.3.1        |
|          httpcore            |       4.3.1        |
|         jetty-server         |   7.2.0.v20101020  |
|          libthrift           |       0.9.1        |


[Top](#top)

###<a name="section5.4"></a>OrionMysQLSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|  OrionMySQLSink dependencies |   Version   |
|:-----------------------------|------------:|
|    mysql-connector-java      |    5.1.31   |

[Top](#top)

###<a name="section5.5"></a>OrionDynamoDBSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|  OrionDynamoDBSink dependencies |   Version    |
|:--------------------------------|-------------:|
|           httpclient            |    4.3.1     |
|             httpcore            |    4.3.1     |
|       aws-java-sdk-dynamodb     |   1.10.32    |
 
[Top](#top)

###<a name="section5.6"></a>OrionMongoSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|  OrionMongoSink dependencies |   Version   |
|:-----------------------------|------------:|
|         mongo-driver         |    3.0.0    |

[Top](#top)

###<a name="section5.7"></a>OrionSTHSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

|   OrionSTHSink dependencies  |   Version   |
|:-----------------------------|------------:|
|         mongo-driver         |    3.0.0    |

[Top](#top)

###<a name="section5.8"></a>OrionKafkaSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

| OrionKafkaSink dependencies |    Version   |
|:----------------------------|-------------:|
|       kafka-clients         |    0.8.2.0   |
|          zkclient           |      0.5     |
|           kafka             | 2.11-0.8.2.1 |

[Top](#top)

###<a name="section5.9"></a>OrionTestSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

| OrionTestSink dependencies |   Version   |
|:---------------------------|------------:|
|        mockito-all         |    1.9.5    |
|           junit            |     4.11    |
|        curator-test        |    2.8.0    |
   
[Top](#top)
