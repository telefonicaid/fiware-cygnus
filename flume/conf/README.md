# Cygnus configuration Procedure

From version 0.6.0 Cygnus can start multiple instances by renaming and configuring some of 
templates files of this directory.

There are two types of configuration template files:
1- Files which is only one for all intances
2- Files wich there are one per each intance that is wanted to run.

## 1- Unique files for all instances

For the first group files it is necessary rename (or better copy) it without `.template` extension.
These files are: `log4j.properties.template`, `krb5.conf.template` and `matching_table.conf.template`

```bash
cp /usr/cygnus/log4j.properties.template /usr/cygnus/log4j.properties
cp /usr/cygnus/krb5.conf.template /usr/cygnus/krb5.conf
cp /usr/cygnus/matching_table.conf.template /usr/cygnus/matching_table.conf
```

Then each file can be edited to changed configuration that will affect to all intances of cygnus.

## 2- Files for each instance

The other files are `cygnus.conf.template` and `flume.conf.template`. This file should be copied 
for each intantance of Cygnus that want to run.

Example:
```bash
cp /usr/cygnus/flume.conf.template /usr/cygnus/cygnus_instance_example1.conf
cp /usr/cygnus/cygnus.conf.template /usr/cygnus/agent_example1.conf
```

It is very important that there are one, or more, file that begins with `cygnus_instance_.*` because
service script try to start one cygnus for file found in `/usr/cygnus/conf`.

Then edit first `cygnus_instance_example1.conf` changing values of:

```bash
CONFIG_FILE=/usr/cygnus/conf/agent_example1.conf
LOGFILE_NAME=cygnus_example1.log
ADMIN_PORT=8081
```

Next edit `/usr/cygnus/agent_example1.conf` changing value of listen port to be unique.

```bash
cygnusagent.sources.http-source.port = 5050
```

All  onfiguration explained above is for basic configuration of Cygnus instance to run. More fine configuration 
should be required, such as [Cygnus configuration](https://github.com/telefonicaid/fiware-connectors/tree/master/flume#cygnus-configuration "Cygnus fine configuration"), 
fine configuration of [log4j](https://github.com/telefonicaid/fiware-connectors/tree/master/flume#logs "Log4j fine configuration"), 
etc. 
