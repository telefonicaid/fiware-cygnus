# <a name="top"></a>Introduction
This document details how to install and administrate **cygnus-common**.

cygnus-common is the base for any Cygnus agent. Cygnus agents are based on [Apache Flume](http://flume.apache.org/) agents, which are basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

cygnus-common provides a set of extensions for Apache Flume, for instance, defining how a Http source handler must look like or adding channels suitable for reading Cygnus-like counters. But not only Flume extensions, but interesting functionality for any agent in terms of a common Management Interface, common backend classes for HDFS, MySQL, MongoDB, PostgreSQL and many others, unified logging classes and error handling, etc.

[Top](#top)

## Intended audience
This document is mainly addressed to those FIWARE users willing to create historical views about any source of data handled by any of the available Cygnus agents. In that case, you will need this document in order to learn how to install and administrate cygnus-common.

If your aim is to contribute to cygnus-common, please refer to the [Contribution guidelines](../../contributing/contributing_guidelines.md).

[Top](#top)

## Structure of the document
Apart from this introduction, this Installation and Administration Guide mainly contains sections about installing, configuring, running and testing cygnus-common. The FIWARE user will also find useful information regarding logs and alarms, how to manage a Cygnus agent through the RESTful interface and important performance tips. In addition, sanity check procedures (useful to know wether the installation was successful or not) and diagnosis procedures (a set of tips aiming to help when an issue arises) are provided as well.

[Top](#top)
