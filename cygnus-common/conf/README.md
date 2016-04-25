#`cygnus-common` configuration notes

* Cygnus is able to run multiple instances through the configuration of `cygnus_instance.conf.template` and `agent.conf.template` files (after renaming them by removing the `.template` sufix). A pair of `cygnus_instance_<ID>.conf` and `agent_<ID>.conf` must exist for each Cygnus instance to be run; the per-pair ID must be the same.
* The version of `agent.conf.template` distributed with `cygnus-common` is just a rename of native Apache Flume's `flume-conf.properties.template`. Nevertheless, when adding specific agents (e.g. `cygnus-ngsi`) such a configuration template is substituted by an agent-specific `agent.conf.template` (e.g. `agent_ngsi.conf.template`, the `ngsi` part would be the ID).
* `flume-env.sh.template` is the native Apache Flume file for configuring the Flume environment.
* `krb5.conf.template` and `krb5_login.conf` are related to Kerberized storages were to persist data, thus templates for Kerberos configuration are provided.
* `log4j.properties.template` is the log4j file used by Cygnus, independently of the agent meant to be run.