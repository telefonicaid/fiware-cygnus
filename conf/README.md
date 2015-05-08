# Cygnus configuration Procedure

From version 0.6.0 Cygnus is able to start multiple instances by renaming and configuring certain 
templates files of this directory.

There are two types of configuration template files:

* Those applying to all the Cygnus instances; thus, they are unique.
* Those applying specifically to one Cygnus instance; thus, there is a configuration file per each instance.

## Unique files for all instances

For them is necessary rename (or better copy) it without `.template` extension. These files are: `log4j.properties.template`, `flume-env.sh.template`, `krb5.conf.template` and `matching_table.conf.template`

```bash
cp /usr/cygnus/conf/log4j.properties.template /usr/cygnus/conf/log4j.properties
cp /usr/cygnus/conf/flume-env.sh.template /usr/cygnus/conf/flume-env.sh
cp /usr/cygnus/conf/krb5.conf.template /usr/cygnus/conf/krb5.conf
cp /usr/cygnus/conf/matching_table.conf.template /usr/cygnus/conf/matching_table.conf
```

Then each file can be edited in order to change the configuration that will affect to all intances of Cygnus.

## Files for each instance

The other files are `cygnus.conf.template` and `flume.conf.template`. These files should be copied for each instance of Cygnus wanted to be run.

Example:
```bash
cp /usr/cygnus/conf/cygnus_instance.conf.template  /usr/cygnus/conf/cygnus_instance_example1.conf
cp /usr/cygnus/conf/agent.conf.template /usr/cygnus/conf/agent_example1.conf
```

It is very important that there are one, or more, file that begins with `cygnus_instance_.*` because the service script try to start a Cygnus instance per each file prefixed as previously mentioned within
 `/usr/cygnus/conf`.

Then edit first `cygnus_instance_example1.conf` changing values of:

```bash
CONFIG_FILE=/usr/cygnus/conf/agent_example1.conf
LOGFILE_NAME=cygnus_example1.log
ADMIN_PORT=8081
POLLING_INTERVAL=30
```

Next edit `/usr/cygnus/agent_example1.conf` changing the value of listening port to be unique.

```bash
cygnusagent.sources.http-source.port = 5050
```

The configuration explained above is for basic configuration of Cygnus instance to run. More detailed configuration should be required, such as [Cygnus configuration](https://github.com/telefonicaid/fiware-cygnus/tree/master/flume#cygnus-configuration "Cygnus fine configuration"), fine configuration of [log4j](https://github.com/telefonicaid/fiware-cygnus/tree/master/flume#logs "Log4j detailed configuration"), etc. 
