#Running Cygnus as a process
**Note**: If you installed Cygnus through the RPM, APACHE\_FLUME\_HOME is `/usr/cygnus/`. If not, it is a directory of your choice.

Cygnus implements its own startup script, `cygnus-flume-ng` which replaces the standard `flume-ng` one, which in the end runs a custom `com.telefonica.iot.cygnus.nodes.CygnusApplication` instead of a standard `org.apache.flume.node.Application`. 

In foreground (with logging):

    $ APACHE_FLUME_HOME/bin/cygnus-flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/agent_<id>.conf -n cygnusagent -Dflume.root.logger=INFO,console [-p <mgmt-if-port>] [-t <polling-interval>]

In background:

    $ nohup APACHE_FLUME_HOME/bin/cygnus-flume-ng agent --conf APACHE_FLUME_HOME/conf -f APACHE_FLUME_HOME/conf/agent_<id>.conf -n cygnusagent -Dflume.root.logger=INFO,LOGFILE [-p <mgmt-if-port>] [-t <polling-interval>] &

The parameters used in these commands are:

* `agent`. This is the type of application to be run by the `cygnus-flume-ng` script.
* `--conf`. Points to the Apache Flume configuration folder.
* `-f` (or `--conf-file`). This is the agent configuration (`agent_<id>.conf`) file. Please observe when running in this mode no `cygnus_instance_<id>.conf` file is required.
* `-n` (or `--name`). The name of the Flume agent to be run.
* `-Dflume.root.logger`. Changes the logging level and the logging appender for log4j.
* `-p` (or `--mgmt-if-port`). Configures the listening port for the Management Interface. If not configured, the default value is used, `8081`.
* `-t` (or `--polling-interval`). Configures the polling interval (seconds) when the configuration is periodically reloaded. If not configured, the default value is used, `30`.
