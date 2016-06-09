#Running a cygnus-twitter agent

Once the `agent_<id>.conf` file is properly configured, just use the following command to start:

    ${FLUME_HOME}/bin/cygnus-flume-ng agent --conf ${CYGNUS_CONF_PATH} -f agent_<id>.conf -n cygnus-twitter -Dflume.root.logger=${CYGNUS_LOG_LEVEL},${CYGNUS_LOG_APPENDER}
    
Where `<id>` is the suffix at the end of the `agent_<id>.conf` files you used to configure the instance.