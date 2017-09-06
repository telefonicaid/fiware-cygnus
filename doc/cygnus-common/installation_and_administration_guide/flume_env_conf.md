# <a name="top"></a>Flume environment configuration
The file `flume-env.sh` can be instantiated from a template given in the Cygnus repository, `conf/flume-env.sh.template`. 

```
#=============================================
# To be put in APACHE_FLUME_HOME/conf/flume-env.sh
#=============================================

#JAVA_HOME=/usr/lib/jvm/java-6-sun

# Give Flume more memory and pre-allocate, enable remote monitoring via JMX
#JAVA_OPTS="-Xms100m -Xmx200m -Dcom.sun.management.jmxremote"

# Note that the Flume conf directory is always included in the classpath.
#FLUME_CLASSPATH="/path/to/the/flume/classpath"
```

`flume-env.sh` file has been inherited from Apache Flume, and it is used in order to configure certain Flume parameters such as an alternative classpath, some Java options etc.
