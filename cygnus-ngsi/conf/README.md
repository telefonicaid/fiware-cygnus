# `cygnus-ngsi` configuration notes
* The `agent_ngsi.conf.template` file is meant for substituting the `agent.conf.template` file installed by `cygnus-common`:

```
$ cp agent_ngsi.conf.template [APACHE_FLUME_HOME]/conf/
```

* The `grouping_rules.conf.template` file is specific for `cygnus-ngsi` and must be copied as well:

```
$ cp grouping_rules.conf.template [APACHE_FLUME_HOME]/conf/
``
