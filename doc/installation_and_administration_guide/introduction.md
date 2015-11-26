#<a name="top"></a>Introduction
This document details how to install and administrate Cygnus.

Cygnus is a connector in charge of persisting [Orion](https://github.com/telefonicaid/fiware-orion) context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using Cygnus.

Cygnus uses the subscription/notification feature of Orion. A subscription is made in Orion on behalf of Cygnus, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/). In fact, Cygnus is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Orion context data in:

* [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.
* [MySQL](https://www.mysql.com/), the well-know relational database manager.
* [CKAN](http://ckan.org/), an Open Data platform.
* [MongoDB](https://www.mongodb.org/), the NoSQL document-oriented database.
* [STH](https://github.com/telefonicaid/IoT-STH), a Short-Term Historic database built on top of MongoDB.
* [Kafka](http://kafka.apache.org/), the publish-subscribe messaging broker.
* [DynamoDB](https://aws.amazon.com/dynamodb/), a cloud-based NoSQL database by [Amazon Web Services](https://aws.amazon.com/).

[Top](#top)

##Intended audience
This document is mainly addressed to those FIWARE users already using an [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance and willing to create historical views from the context data managed by Orion. In that case, you will need this documente in order to learn how to install and administrate Cygnus.

If your aim is to create a new sink for Cygnus, or expand it in some way, please refer to the [User and Programmer Guide](../user_and_programmer_guide/introduction.md).

[Top](#top)

##Structure of the document
Apart from this introduction, this Installation and Administration Guide mainly contains sections about installing, configuring, running and testing Cygnus. The FIWARE user will also find useful information regarding logs and alarmas, how to manage Cygnus through the RESTful interface and important performance tips. In addition, sanity check procedures (useful to know wether the installation was successful or not) and diagnosis procedures (a set of tips aiming to help when an issue arises) are provided as well.

[Top](#top)
