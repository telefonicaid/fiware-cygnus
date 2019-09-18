# <a name="top"></a>Installing cygnus-common from sources
Content:

* [Prerequisites](#section1)
* [`cygnus` user creation](#section2)
* [`log4j` path](#section3)
* [Installing Apache Flume](#section4)
* [Installing cygnus-common](#section5)
    * [Cloning `fiware-cygnus`](#section5.1)
    * [Installing `cygnus-common`](#section5.2)
    * [Installing `cygnus-flume-ng` script](#section5.3)
    * [Known issues](#section5.4)
* [Installing dependencies](#section6)

## <a name="section1"></a>Prerequisites
Maven (and thus Java SDK, since Maven is a Java tool) is needed in order to install cygnus-common.

In order to install Java SDK (not JRE), just type (CentOS machines):

    $ yum install java-1.7.0-openjdk-devel

Remember to export the JAVA_HOME environment variable. In the case of using `yum install` as shown above, it would be:

    $ export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk.x86_64

In order to do it permanently, edit `/root/.bash_profile` (`root` user) or `/etc/profile` (other users).

Maven is installed by downloading it from [maven.apache.org](http://maven.apache.org/download.cgi). Install it in a folder of your choice (represented by `APACHE_MAVEN_HOME`):

    $ wget http://www.eu.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
    $ tar xzvf apache-maven-3.2.5-bin.tar.gz
    $ mv apache-maven-3.2.5 APACHE_MAVEN_HOME

[Top](#top)

## <a name="section2"></a>`cygnus` user creation
It is highly recommended to create a `cygnus` Unix user, under which Cygnus will be installed and run. By the way, this is how the [RPM](./install_with_rpm.md) proceeds.

Creating such a user is quite simple. As a sudoer user (root or any other allowed), type the following:

    $ (sudo) useradd cygnus

You may add a password or not to the `cygnus` user:

    $ (sudo) passwd cygnus

Once created, change to this new fresh user in order to proceed with the rest of the installation:

    $ su - cygnus

[Top](#top)

## <a name="section3"></a>`log4j` path
Once the user is created is necessary to create the path `/var/log/cygnus` for `log4j` purposes. Start by creating the path and then give permissions for `cygnus` user:

    $ mkdir -p /var/log/cygnus
    $ chown -R cygnus:cygnus /var/log/cygnus

This step is important because if you don't have the log path created Cygnus will shutdown when running for the first time.

[Top](#top)

## <a name="section4"></a>Installing Apache Flume
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

## <a name="section5"></a>Installing cygnus-common
### <a name="section5.1"></a>Cloning `fiware-cygnus`
Start by cloning the Github repository containing cygnus-common:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ git checkout <branch>

`<branch>` should be typically a stable release branch, e.g. `release/0.13.0`, but could also be `master` (synchronized with the latest release) or `develop` (contains the latest not stable changes).

[Top](#top)

### <a name="section5.2"></a>Installing `cygnus-common`
The developed classes must be packaged in a Java jar file. This can be done as a fat Java jar containing all the third-party dependencies  (**recommended**). You may need to edit the `pom.xml` (\*):

    $ cd cygnus-common
    $ APACHE_MAVEN_HOME/bin/mvn clean compile exec:exec assembly:single
    $ cp target/cygnus-common-<x.y.z>-jar-with-dependencies.jar APACHE_FLUME_HOME/plugins.d/cygnus/libext

Or as a thin Java jar file containing only the `cygnus-common` classes. You may need to edit the `pom.xml` if necessary (\*):

    $ cd cygnus-common
    $ APACHE_MAVEN_HOME/bin/mvn exec:exec package
    $ cp target/cygnus-<x.y.z>.jar APACHE_FLUME_HOME/plugins.d/cygnus/libext

Please observe in this case, and depending on the Cygnus components you are going to use, you may need to install [additional](#section5) third-party .jar files under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/`. Typically, you can get these jar files from your Maven repository (under `.m2` in your user home directory) and use the `cp` command.

In both cases, the `cygnus-common` dependency must be installed at Maven as well, in order to build any other agent depending on it. Just run the following command:

    $ APACHE_MAVEN_HOME/bin/mvn install:install-file -Dfile=APACHE_FLUME_HOME/plugins.d/cygnus/libext/cygnus-common-<x.y.z>-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=<x.y.z> -Dpackaging=jar -DgeneratePom=false

(*) Please have into account from Cygnus 0.10.0 the version of `hadoop-core` within the `pom.xml` must match the Hadoop version you are going to use; on the contrary, the HDFS sink will not work. Of course, if you are not going to use the HDFS sink, simply use the default `hadoop-core` version (1.2.1) within the downloaded `pom.xml` for correct compilation purposes.

Finally, please find a `compile.sh` script containing all the commands shown in this section. It must be parameterized with the version of the current branch and the Apache Flume base path.

[Top](#top)

### <a name="section5.3"></a>Installing `cygnus-flume-ng` script
The installation is completed by copying the `cygnus-flume-ng` script into `APACHE_FLUME_HOME/bin`:

    $ cp target/classes/cygnus-flume-ng APACHE_FLUME_HOME/bin
    $ chmod a+x APACHE_FLUME_HOME/bin/cygnus-flume-ng

[Top](#top)

### <a name="section5.4"></a>Known issues
It may happen while compiling `cygnus-common` the Maven JVM has not enough memory. This can be changed as detailed at the [Maven official documentation](https://cwiki.apache.org/confluence/display/MAVEN/OutOfMemoryError):

    $ export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"

[Top](#top)

## <a name="section6"></a>Installing dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus jar**:

| Cygnus dependencies | Version | Required by / comments |
|---|---|---|
| mockito-all | 1.9.5 | Unit tests |
| junit | 4.11 | Unit tests |
| flume-ng-node | 1.4.0 | |
| httpclient | 4.3.1 | Overwrites the one bundled in Apache Flume |
| httpcore | 4.3.1 | Overwrites the one bundled in Apache Flume |
| libthrift | 0.9.1 | Overwrites the one bundled in Apache Flume |
| gson | 2.2.4 | Management Interface |
| json-simple | 1.1 | Management Interface and Grouping Rules |
| log4j | 1.2.17 | Logging |
| mysql-connector-java | 5.1.31 | `MySQLBackendImpl` at runtime |
| postgresql | 9.4-1202-jdbc41 | `PostgreSQLBackendImpl` at runtime |
| hadoop-core | 1.2.1 | `HDFSBackendImplBinary` |
| hive-exec | 0.13.0 | `HiveBackendImpl` at runtime |
| hive-jdbc | 0.13.0 | `HiveBackendImpl` at runtime |
| mongodb-driver | 3.11.0 | `MongoBackendImpl` |
| kafka-clients | 0.8.2.0 | `KafkaBackendImpl` |
| zkclient | 0.5 | `KafkaBackendImpl` |
| kafka_2.11 | 0.8.2.1 | `KafkaBackendImpl` |
| aws-java-sdk-dynamodb | 1.10.32 | `DynamoDBBackendImpl` |

[Top](#top)
