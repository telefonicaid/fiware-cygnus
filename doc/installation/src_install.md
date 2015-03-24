# Installing Cygnus from sources
## Prerequisites

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

## Installing Cygnus and its dependencies

Apache Flume can be easily installed by downloading its latests version from [flume.apache.org](http://flume.apache.org/download.html). Move the untared directory to a folder of your choice (represented by `APACHE_FLUME_HOME`):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME
    $ mkdir -p APACHE_FLUME_HOME/plugins.d/cygnus/
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/libext

The creation of the `plugins.d` directory is related to the installation of third-party software, like Cygnus.

Then, the developed classes must be packaged in a Java jar file; this can be done by including the dependencies in the package (**recommended**):

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ git checkout <branch>
    $ cd fiware-cygnus
    $ APACHE_MAVEN_HOME/bin/mvn clean compile exec:exec assembly:single
    $ cp target/cygnus-<x.y.z>-jar-with-dependencies.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ cp target/classes/cygnus-flume-ng APACHE_FLUME_HOME/bin

or not:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ git checkout <branch>
    $ cd fiware-cygnus
    $ APACHE_MAVEN_HOME/bin/mvn exec:exec package
    $ cp target/cygnus-<x.y.z>.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ cp target/classes/cygnus-flume-ng APACHE_FLUME_HOME/bin

where `<branch>` is `develop` if you are trying to install the latest features or `release/<x.y.z>` if you are trying to install a stable release. `<x.y.z>` stands for a specific version number (e.g. `0.3`, `0.5.1`...).

If the dependencies are included in the built Cygnus package, then nothing has to be done. If not, and depending on the Cygnus components you are going to use, you may need to install additional .jar files under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/`. Typically, you can get the .jar file from your Maven repository (under `.m2` in your user home directory) and use the `cp` command.

In addition:

* Observe the version of `httpcomponents-core` and `httpcomponents-client` in the `pom.xml` are matching the version of such packages within the Flume bundle (`httpclient-4.2.1.jar and httpcore-4.2.1.jar`). These are not the newest versions of such packages, but trying to build Cygnus with such newest libraries has shown incompatibilities with Flume's ones.
* libthrift-0.9.1.jar must overwrite `APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar` (it can be got from the [this](http://repo1.maven.org/maven2/org/apache/thrift/libthrift/0.9.1/libthrift-0.9.1.jar) URL).

### OrionCKANSink dependencies

These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* json-simple-1.1.jar

### OrionHDFSSink dependencies

These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* hadoop-core-0.20.0.jar (or higher)
* hive-exec-0.12.0.jar
* hive-jdbc-0.12.0.jar
* hive-metastore-0.12.0.jar
* hive-service-0.12.0.jar
* hive-common-0.12.0.jar
* hive-shims-0.12.0.jar

These packages are not necessary to be installed since they are already included in the Flume bundle (they have been listed just for informative purposes):

* httpclient-4.2.1.jar
* httpcore-4.2.2.jar

In addition, as already said, remember to overwrite the `APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar` package with this one:

* libthrift-0.9.1.jar

### OrionMysQLSink dependencies

These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* mysql-connector-java-5.1.31-bin.jar

##Contact
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com)
<br>
Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
