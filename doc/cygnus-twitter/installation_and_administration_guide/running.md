# Running a cygnus-twitter agent

Once the `agent_<id>.conf` file is properly configured, just use the following command to start:

    ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f agent_<id>.conf -n cygnus-twitter -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER} -Duser.timezone=UTC
    
Where `<id>` is the suffix at the end of the `agent_<id>.conf` files you used to configure the instance.

The parameters used in these commands are:

* `agent`. This is the type of application to be run by the `cygnus-flume-ng` script.
* `--conf`. Points to the Apache Flume configuration folder.
* `-f` (or `--conf-file`). This is the agent configuration (`agent_<id>.conf`) file. Please observe when running in this mode no `cygnus_instance_<id>.conf` file is required.
* `-n` (or `--name`). The name of the Cygnus agent to be run.
* `-Dflume.root.logger`. Changes the logging level and the logging appender for log4j.
* `-Duser.timezone=UTC`. Changes the timezone in order all the timestamps (logs, data reception times, etc) are UTC.
