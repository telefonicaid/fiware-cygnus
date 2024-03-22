# <a name="top"></a>Installing cygnus-ngsi from sources
Content:

* [Prerequisites](#section1)
* [Installing cygnus-ngsi](#section2)
    * [Cloning `fiware-cygnus`](#section2.1)
    * [Installing `cygnus-common`](#section2.2)
    * [Installing `cygnus-ngsi`](#section2.3)
    * [Setup `cygnus`](#section2.4)
    * [Setup Agents](#section2.5)
    * [Running `cygnus-ngsi`](#section2.6)
* [SSL Support](#section3)
    * [Prerequisites](#section3.1)
    * [Setup](#section3.2)
      
Please note that the commands listed here are for Debian systems.

## <a name="section1"></a>Prerequisites
Install JDK 17

    $ sudo apt-get -y install openjdk-17-jdk openjdk-17-jdk-headless openjdk-17-jre openjdk-17-jre-headless
    
Add $JAVA_HOME to $PATH

    $ nano .bashrc
    $ export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    $ export PATH=$PATH:$JAVA_HOME

Install Maven

    $ sudo apt-get -y install maven

Create `cygnus` user

    $ adduser cygnus

Install Apache Flume 

    $ wget https://downloads.apache.org/flume/1.11.0/apache-flume-1.11.0-bin.tar.gz
    $ tar xzf apache-flume-1.11.0-bin.tar.gz
    $ mv apache-flume-1.11.0-bin /opt/apache-flume-1.11.0
    $ mkdir -p /opt/apache-flume-1.11.0/plugins.d/cygnus
    $ mkdir -p /opt/apache-flume-1.11.0/plugins.d/cygnus/lib
    $ mkdir -p /opt/apache-flume-1.11.0/plugins.d/cygnus/libext
    $ chown -R cygnus:cygnus /opt/apache-flume-1.11.0/
    
Add FLUME_HOME to $PATH

    $ nano .bashrc
    $ export FLUME_HOME=/opt/apache-flume-1.11.0
    $ export PATH=$PATH:$FLUME_HOME/bin

[Top](#top)

## <a name="section2"></a>Installing Cygnus
### <a name="section2.1"></a>Cloning `fiware-cygnus`
Start by cloning the Github repository:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ git checkout <branch>

`<branch>` should be typically a stable release branch, e.g. `release/0.13.0`, but could also be `master` (synchronized with the latest release) or `develop` (contains the latest not stable changes).

[Top](#top)

### <a name="section2.2"></a>Installing `cygnus-common`

    $ cd fiware-cygnus/cygnus-common
    $ mvn -B -T8 clean compile exec:exec assembly:single
    $ cp target/cygnus-common-3.6.0-SNAPSHOT-jar-with-dependencies.jar /opt/apache-flume-1.11.0/plugins.d/cygnus/libext/
    $ mvn install:install-file -Dfile=/opt/apache-flume-1.11.0/plugins.d/cygnus/libext/cygnus-common-3.6.0-SNAPSHOT-jar-with-dependencies.jar -DgroupId=com.telefonica.iot -DartifactId=cygnus-common -Dversion=3.6.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=false

[Top](#top)

### <a name="section2.3"></a>Installing `cygnus-ngsi`

    $ cd fiware-cygnus/cygnus-ngsi
    $ mvn -B -T8 clean compile exec:exec assembly:single
    $ cp target/cygnus-ngsi-3.6.0-SNAPSHOT-jar-with-dependencies.jar /opt/apache-flume-1.11.0/plugins.d/cygnus/lib/

Finally, please find a `compile.sh` script containing all the commands shown in this section. It must be parameterized with the version of the current branch and the Apache Flume base path.

[Top](#top)

### <a name="section2.4"></a>Setup Cygnus

    $ cp fiware-cygnus/cygnus-common/target/classes/cygnus-flume-ng /opt/apache-flume-1.11.0/bin/
    $ chmod +x /opt/apache-flume-1.11.0/bin/cygnus-flume-ng
    $ cp fiware-cygnus/cygnus-common/conf/log4j2.properties.template /opt/apache-flume-1.11.0/conf/log4j2.properties
    $ cp fiware-cygnus/cygnus-ngsi/conf/name_mappings.conf.template /opt/apache-flume-1.11.0/conf/name_mappings.conf
    $ mkdir -p /var/log/cygnus
    $ chown -R cygnus:cygnus /var/log/cygnus
    
Clean up

    $ cd fiware-cygnus/cygnus-common
    $ mvn -B -T8 clean
    $ cd fiware-cygnus/cygnus-ngsi
    $ mvn -B -T8 clean>
    
[Top](#top)

### <a name="section2.5"></a>Setup Agents
Setup Cygnus-Common and Cygnus-Ngsi agents

### <a name="section2.6"></a>Running `cygnus-ngsi`
Please make sure that you have ports 5080 (admin port) and 5050 (default instance port) open.<br/>

    $ /opt/apache-flume-1.11.0/bin/cygnus-flume-ng agent --conf {{PATH-TO-CONF-FOLDER}} -f {{PATH-TO-AGENT-NGSI-CONF}} -n {{AGENT-NAME}} -p 5080 -Dflume.root.logger=INFO,LOGFILE -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Dflume.monitoring.type= -Dflume.monitoring.port=41414

[Top](#top)

## <a name="section3"></a>SSL Support
Apache Flume has SSL support, <a href='https://flume.apache.org/FlumeUserGuide.html#ssl-tls-support'>mentioned here</a>

### <a name="section3.1"></a>Prerequisites for this is that you should have:
SSL certificated installed (using Certbot & let's encrypt)<br/>
Before doing this make sure that port 80 and 443 are open and that you have a domain assoicated with the IP of the server you are installing SSL on.<br/>
Install Certbot with snap

    $ sudo apt install snapd
    $ sudo snap install core; sudo snap refresh core
    $ sudo apt remove certbot
    $ sudo snap install --classic certbot
    $ sudo ln -s /snap/bin/certbot /usr/bin/certbot

Issue certificates
  
    $ sudo certbot certonly --standalone -d DOMAINNAME

Java Keystore setup using the above certificates.<br/>
These commands can be put in a bash file and run as a deployment hook, so whenever the certificates renewed the Java Keystore can be updated.<br/>
Replace all values withing {{}}

    $ openssl pkcs12 -export \
    -in {{ABSOLUTE_PATH_TO_LETSENCRYPT_FULLCHAIN}} \
    -inkey {{ABSOLUTE_PATH_TO_LETSENCRYPT_PRIVKEY}} \
    -out /tmp/cert.p12 \
    -name letsencrypt-keystore \
    -passout pass:{{TEMPORARY_PASSWORD}} \ 
    > /dev/null 2>&1

Import PKCS12 file into keystore

    $ keytool -noprompt -importkeystore \
    -srckeystore /tmp/cert.p12 \
    -srcstoretype PKCS12 \ 
    -srcstorepass {{TEMPORARY_PASSWORD}} \ 
    -deststorepass {{KEYSTORE_PASSWORD}} \ 
    -destkeypass {{PRIVATE_KEY_PASSWORD}} \ 
    -destkeystore {{ABSOLUTE_PATH_TO_KEYSTORE_IN_CONF_FOLDER}} \ 
    -alias letsencrypt-keystore \
    > /dev/null 2>&1

Delete PKCS12 file

    $ rm /tmp/cert.p12
    $ chown cygnus:cygnus /opt/apache-flume-1.11.0/conf/letsencrypt-keystore.jks`

### <a name="section3.2"></a>Setup
Clone /conf/flume-env.sh.template to /conf/flume-env.sh and edit to add the following:

    $ export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.keyStore=/path/to/letsencrypt-keystore.jks"`
    $ export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.keyStorePassword=password"`
    $ export FLUME_SSL_KEYSTORE_PATH=/path/to/etsencrypt-keystore.jks`
    $ export FLUME_SSL_KEYSTORE_PASSWORD=password`

Edit agent_ngsi config and add the following lines according to <a href='https://flume.apache.org/releases/content/1.11.0/FlumeUserGuide.html#http-source'>Flume http-source</a>

    $ cygnus-ngsi.sources.http-source.ssl = true`


[Top](#top)
