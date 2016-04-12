#Cygnus configuration procedure

From version 0.6.0 Cygnus is able to start multiple instances by renaming and configuring certain templates files within this directory.

There are two types of configuration template files:

* Those applying to all the Cygnus instances; thus, they are unique.
* Those applying specifically to one Cygnus instance; thus, there is a configuration file per each instance.

##Unique files for all instances

These files are `log4j.properties.template`, `flume-env.sh.template`, `krb5.conf.template` and `matching_table.conf.template`. It is necessary to rename them by removing the `.template` part:

```bash
cp /usr/cygnus/conf/log4j.properties.template /usr/cygnus/conf/log4j.properties
cp /usr/cygnus/conf/flume-env.sh.template /usr/cygnus/conf/flume-env.sh
cp /usr/cygnus/conf/krb5.conf.template /usr/cygnus/conf/krb5.conf
cp /usr/cygnus/conf/matching_table.conf.template /usr/cygnus/conf/matching_table.conf
```

Then each file can be edited in order to change the configuration that will affect to all intances of Cygnus.

##Files per instance

These files are `cygnus_instance.conf.template` and `agent.conf.template`. A copy must be created for each instance of Cygnus wanted to be run, followinf this format:

```bash
cp /usr/cygnus/conf/cygnus_instance.conf.template /usr/cygnus/conf/cygnus_instance_example1.conf
cp /usr/cygnus/conf/agent.conf.template /usr/cygnus/conf/agent_example1.conf
```

It is very important the Cygnus instance files begin with `cygnus_instance_.*` because the service script try to start a Cygnus instance per each file prefixed as previously mentioned within
 `/usr/cygnus/conf`.

Parameters within a `cygnus_instance_*.conf` file are the following ones, their semantic is straightforward:

```bash
CONFIG_FILE=/usr/cygnus/conf/agent_example1.conf
LOGFILE_NAME=cygnus_example1.log
ADMIN_PORT=8081
POLLING_INTERVAL=30
```

Parameters within an `agent_*.conf` file are widely described in the main [README.md](../README.md) and in each [specific sink document](../doc/design). The only remark that will be done here is the value of sources listening ports must be unique.

```bash
cygnusagent.sources.http-source.port = 5050
```
