# <a name="top"></a>Introduction
This document details how to install and administrate a **cygnus-twitter** agent.

cygnus-twitter is a connector in charge of persisting [Twitter](http://www.twitter.com) statuses data in certain configured third-party storages.

cygnus-twitter uses the API of Twitter to collect data. cygnus-twitter requires an API key and secret get data.

Internally, cygnus-twitter is based on [Apache Flume](http://flume.apache.org/), which is used through **cygnus-common** and which cygnus-twitter depends on. In fact, cygnus-twitter is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Twitter context data in:

* [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.

[Top](#top)

## Intended audience
This document is mainly addressed to those FIWARE users that want to collect public information from twitter, based on keywords and/or geolocated information. In that case, you will need this document in order to learn how to install and administrate cygnus-twitter.

If your aim is to create a new sink for cygnus-twitter, or expand it in some way, please refer to the [User and Programmer Guide](../user_and_programmer_guide/programmer_guide.md).

[Top](#top)

## Structure of the document
Apart from this introduction, this Installation and Administration Guide mainly contains sections about installing, configuring, running and testing cygnus-twitter.

It is very important to note that, for those topics not covered by this documentation, the related section in cygnus-common applies. Specifically:

* Hardware requirements.
* Flume environment configuration.
* log4j configuration.
* Running as a process.
* Management interface.
* Logs and alarms.

[Top](#top)
