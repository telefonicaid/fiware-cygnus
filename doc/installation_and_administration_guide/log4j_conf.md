#<a name="top"></a>log4j configuration
The file `log4j.properties` can be instantiated from a template given in the Cygnus repository, `conf/log4j.properties.template`.

Its content should not be edited unless some of the default values for log path, file name, logging level or appender are wanted to be changed.

```
# Define some default values.
# These can be overridden by system properties, e.g. the following logs in the standard output, which is very useful
# for testing purposes:
# -Dflume.root.logger=DEBUG,console
flume.root.logger=INFO,LOGFILE
#flume.root.logger=DEBUG,console
flume.log.dir=/var/log/cygnus/
flume.log.file=cygnus.log

# Logging levels for certain components.
log4j.logger.org.apache.flume.lifecycle = INFO
log4j.logger.org.jboss = WARN
log4j.logger.org.mortbay = INFO
log4j.logger.org.apache.avro.ipc.NettyTransceiver = WARN
log4j.logger.org.apache.hadoop = INFO

# Define the root logger to the system property "flume.root.logger".
log4j.rootLogger=${flume.root.logger}

# Stock log4j rolling file appender.
# Default log rotation configuration.
log4j.appender.LOGFILE=org.apache.log4j.RollingFileAppender
log4j.appender.LOGFILE.MaxFileSize=100MB
log4j.appender.LOGFILE.MaxBackupIndex=10
log4j.appender.LOGFILE.File=${flume.log.dir}/${flume.log.file}
log4j.appender.LOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.LOGFILE.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

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
log4j.appender.DAILY.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n

# Console appender, i.e. printing logs in the standar output.
# Add "console" to flume.root.logger above if you want to use this.
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.err
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=Cygnus | msg=%C[%L] : %m%n
```
