#<a name="top"></a>Installing Cygnus from sources
Content:

* [Prerequisites](#prerequisites)
* [Cygnus user creation](#usercreation)
* [Installing Apache Flume](#installflume)
* [Installing Cygnus](#installcygnus)
* [Installing dependencies](#installdeps)
    * [OrionCKANSink dependencies](#ckandeps)
    * [OrionHDFSSink dependencies](#hdfsdeps)
    * [OrionMySQLSink dependencies](#mysqldeps)
* [Reporting issues and contact information](#contact)

##<a name="prerequisites"></a>Prerequisites
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

##<a name="usercreation"></a>Cygnus user creation
It is highly recommended to create a `cygnus` Unix user, under which Cygnus will be installed and run. By the way, this is how the [RPM](./src_rpm.md) proceeds.

Creating such a user is quite simple. As a sudoer user (root or any other allowed), type the following:

    $ (sudo) useradd cygnus
    
You may add a password or not to the `cygnus` user:

    $ (sudo) passwd cygnus
    
Once created, change to this new fresh user in order to proceed with the rest of the installation:

    $ su - cygnus
    
[Top](#top)

##<a name="installflume"></a>Installing Apache Flume
Apache Flume can be easily installed by downloading its latests version from [flume.apache.org](http://flume.apache.org/download.html). Move the untared directory to a folder of your choice (represented by `APACHE_FLUME_HOME`):

    $ wget http://www.eu.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
    $ tar xvzf apache-flume-1.4.0-bin.tar.gz
    $ mv apache-flume-1.4.0-bin APACHE_FLUME_HOME
    $ mkdir -p APACHE_FLUME_HOME/plugins.d/cygnus/
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ mkdir APACHE_FLUME_HOME/plugins.d/cygnus/libext

The creation of the `plugins.d` directory is related to the installation of third-party software, like Cygnus.

Before continuing with the installation of Cygnus, some tuning must be done on Flume:

* Please observe the version of `httpcomponents-core` and `httpcomponents-client` in the `pom.xml` (`httpclient-4.3.1.jar and httpcore-4.3.1.jar`) don't match match the version of such packages within the Flume bundle (`httpclient-4.2.1.jar and httpcore-4.2.1.jar`). In order the most recent version of the libraries, the ones within the Flume bundle must be removed (or renamed).
* libthrift-0.9.1.jar must overwrite `APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar` (it can be got from the [this](http://repo1.maven.org/maven2/org/apache/thrift/libthrift/0.9.1/libthrift-0.9.1.jar) URL).

[Top](#top)

##<a name="installcygnus"></a>Installing Cygnus
Then, the developed classes must be packaged in a Java jar file; this can be done by including the dependencies in the package (**recommended**):

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ git checkout <branch>

Edit the `pom.xml` if necessary (*), then continue:

    $ APACHE_MAVEN_HOME/bin/mvn clean compile exec:exec assembly:single
    $ cp target/cygnus-<x.y.z>-jar-with-dependencies.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ cp target/classes/cygnus-flume-ng APACHE_FLUME_HOME/bin
    $ chmod a+x APACHE_FLUME_HOME/bin/cygnus-flume-ng

or not:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ git checkout <branch>
    
Edit the `pom.xml` if necessary (*), then continue:

    $ APACHE_MAVEN_HOME/bin/mvn exec:exec package
    $ cp target/cygnus-<x.y.z>.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    $ cp target/classes/cygnus-flume-ng APACHE_FLUME_HOME/bin
    $ chmod a+x APACHE_FLUME_HOME/bin/cygnus-flume-ng

where `<branch>` is `develop` if you are trying to install the latest features or `release/<x.y.z>` if you are trying to install a stable release. `<x.y.z>` stands for a specific version number (e.g. `0.3`, `0.5.1`...).

If the dependencies are included in the built Cygnus package, then nothing has to be done. If not, and depending on the Cygnus components you are going to use, you may need to install additional .jar files under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/`. Typically, you can get the .jar file from your Maven repository (under `.m2` in your user home directory) and use the `cp` command.

(*) Please have into account from Cygnus 0.10.0 the version of `hadoop-core` within the `pom.xml` must match the Hadoop version you are going to use; on the contrary, the HDFS sink will not work. Of course, if you are not going to use the HDFS sink, simply use the default `hadoop-core` version (1.2.1) within the downloaded `pom.xml` for correct compilation purposes.

[Top](#top)

##<a name="installdeps"></a>Installing dependencies
###<a name="ckandeps"></a>OrionCKANSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* json-simple-1.1.jar

[Top](#top)

###<a name="hdfsdeps"></a>OrionHDFSSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* hadoop-core-0.20.0.jar (or higher)
* hive-exec-0.12.0.jar
* hive-jdbc-0.12.0.jar
* hive-metastore-0.12.0.jar
* hive-service-0.12.0.jar
* hive-common-0.12.0.jar
* hive-shims-0.12.0.jar
* httpclient-4.3.1.jar
* httpcore-4.3.1.jar

In addition, as already said, remember to overwrite the `APACHE_FLUME_HOME/lib/libthrift-0.7.0.jar` package with this one:

* libthrift-0.9.1.jar

[Top](#top)

###<a name="mysqldeps"></a>OrionMysQLSink dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the Cygnus package**:

* mysql-connector-java-5.1.31-bin.jar

[Top](#top)

##<a name="contact"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](german.torodelvalle@telefonica.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.

[Top](#top)
