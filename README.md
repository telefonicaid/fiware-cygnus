#Cygnus
[![License badge](https://img.shields.io/badge/license-AGPL-blue.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Documentation badge](https://readthedocs.org/projects/fiware-cygnus/badge/?version=latest)](http://fiware-cygnus.readthedocs.org/en/latest/?badge=latest)
[![Docker badge](https://img.shields.io/docker/pulls/fiware/cygnus.svg)](https://hub.docker.com/r/fiware/cygnus/)
[![Support badge]( https://img.shields.io/badge/support-sof-yellowgreen.svg)](http://stackoverflow.com/questions/tagged/fiware-cygnus)
[![Support badge]( https://img.shields.io/badge/support-askbot-yellowgreen.svg)](https://ask.fiware.org/questions/scope%3Aall/tags%3Acygnus/)

##Welcome
This project is part of [FIWARE](http://fiware.org), being part of the [Cosmos](http://catalogue.fiware.org/enablers/bigdata-analysis-cosmos) Ecosystem.

Cygnus is a connector in charge of persisting certain sources of data in certain configured third-party storages, creating a historical view of such data.

Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/), a technology addressing the design and execution of data collection and persistence <i>agents</i>. An agent is basically composed of a listener or source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Cygnus is designed to run a specific Flume agent per source of data.

Current stable release is able to persist the following sources of data in the following third-party storages:

* NGSI-like context data in:
    * [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.
    * [MySQL](https://www.mysql.com/), the well-know relational database manager.
    * [CKAN](http://ckan.org/), an Open Data platform.
    * [MongoDB](https://www.mongodb.org/), the NoSQL document-oriented database.
    * [FIWARE Comet](https://github.com/telefonicaid/IoT-STH), a Short-Term Historic database built on top of MongoDB.
    * [Kafka](http://kafka.apache.org/), the publish-subscribe messaging broker.
    * [DynamoDB](https://aws.amazon.com/dynamodb/), a cloud-based NoSQL database by [Amazon Web Services](https://aws.amazon.com/).
* Twitter data in:
    * [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.

##Further documentation
The per agent **Quick Start Guide** provides a good documentation summary.

Nevertheless, both the **Installation and Administration Guide** and the **User and Programmer Guide** shown within this documentation for each agent cover more advanced topics.

The per agent **Flume Extensions Catalogue** completes the available documentation for Cygnus.

Of special interest are the [Contributing Guidelines](./doc/contributing/contributing_guidelines.md) if your aim is to extend Cygnus.

Finally, please check our [Apiary Documentation](http://telefonicaid.github.io/fiware-cygnus/api/) if you want to know how to use our API methods for Cygnus.

##Reporting issues and contact information
Any doubt you may have, please refer to the [Cygnus Core Team](./reporting_issues_and_contact.md).
