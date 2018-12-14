# <a name="top"></a>Cygnus agent configuration
Content:

* [Introduction](#section1)
* [`cygnus_instance_<id>.conf`](#section2)
* [`agent_<id>.conf`](#section3)

## <a name="section1"></a>Introduction
Any Cygnus agent is configured through two different files:

* A `cygnus_instance_<id>.conf` file addressing all those non Flume parameters, such as the Flume agent name, the specific log file for this instance, the administration port, etc. This configuration file is not necessary if Cygnus is run as a standalone application (see later), but it is mandatory if run as a service (see later).
* An `agent_<id>.conf` file addressing all those Flume parameters, i.e. how to configure the different sources, channels, sinks, etc. that compose the Flume agent behind the Cygnus instance. It is always mandatory.

Please observe there may exist several Cygnus instances identified by `<id>`, and this `<id>` must be the same for both configuration files regarding the same Cygnus instance. This is necessary if wanting to run several instances of Cygnus as a service in the same machine. E.g. running two different instances of Cygnus will require:

* First instance:
    * `cygnus_instance_1.conf`
    * `agent_1.conf`
* Second instance:
    * `cygnus_instance_2.conf`
    * `agent_2.conf`

In addition, (a unique) `log4j.properties` controls how Cygnus logs its traces.

[Top](#top)

## <a name="section2"></a>`cygnus_instance_<id>.conf`
The file `cygnus_instance_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/cygnus_instance.conf.template`.

```
# The OS user that will be running Cygnus. Note this must be `root` if you want to run cygnus in a privileged port (<1024), either the admin port or the port in which Cygnus receives Orion notifications
CYGNUS_USER=cygnus
# Which is the config folder
CONFIG_FOLDER=/usr/cygnus/conf
# Which is the config file
CONFIG_FILE=/usr/cygnus/conf/agent_<id>.conf
# Name of the agent. The name of the agent is not trivial, since it is the base for the Flume parameters naming conventions, e.g. it appears in <AGENT_NAME>.sources.http-source.channels=...
AGENT_NAME=cygnus-common
# Name of the logfile located at /var/log/cygnus. It is important to put the extension '.log' in order to the log rotation works properly
LOGFILE_NAME=cygnus.log
# Administration port. Must be unique per instance
ADMIN_PORT=5080
# Polling interval (seconds) for the configuration reloading
POLLING_INTERVAL=30
```

As you can see, this file allows configuring the log file. For a detailed logging configuration, please check the [`log4j.properties`](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-common/installation_and_administration_guide/log4j_conf.md) section.

[Top](#top)

## <a name="section3"></a>`agent_<id>.conf`
The file `agent_<id>.conf` can be instantiated from a template given in the Cygnus repository, `conf/agent.conf.template`.

While no specific Cygnus agent is used, this template is just the Apache Flume template.

[Top](#top)
