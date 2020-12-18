# <a name="top"></a>Introduction
This document details how to install and administrate a **cygnus-ngsi-ld** agent.

cygnus-ngsi-ld is a connector in charge of persisting [Orion](https://github.com/telefonicaid/fiware-orion) context data in certain configured third-party storages, creating a historical view of such data. In other words, Orion only stores the last value regarding an entity's attribute, and if an older value is required then you will have to persist it in other storage, value by value, using cygnus-ngsi-ld.

cygnus-ngsi-ld uses the subscription/notification feature of Orion. A subscription is made in Orion on behalf of cygnus-ngsi-ld, detailing which entities we want to be notified when an update occurs on any of those entities attributes.

Internally, cygnus-ngsi-ld is based on [Apache Flume](http://flume.apache.org/), which is used through **cygnus-common** and which cygnus-ngsi-ld depends on. In fact, cygnus-ngsi-ld is a Flume agent, which is basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Current stable release is able to persist Orion context data in:

* [PostgreSQL](http://www.postgresql.org/), the well-know relational database manager.
* [PostGIS](http://postgis.net/), a spatial database extender for PostgreSQL object-relational database.
* [CKAN](http://ckan.org/), an Open Data platform. 

[Top](#top)

## Intended audience
This document is mainly addressed to those FIWARE users already using an [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance and willing to create historical views from the context data managed by Orion. In that case, you will need this document in order to learn how to install and administrate cygnus-ngsi-ld.

If your aim is to create a new sink for cygnus-ngsi-ld, or expand it in some way, please refer to the [User and Programmer Guide](../user_and_programmer_guide/introduction.md).

[Top](#top)

## Structure of the document
Apart from this introduction, this Installation and Administration Guide mainly contains sections about installing, configuring, running and testing cygnus-ngsi-ld. The FIWARE user will also find useful information regarding multitenancy or performance tips. In addition, sanity check procedures (useful to know wether the installation was successful or not) and diagnosis procedures (a set of tips aiming to help when an issue arises) are provided as well.


* [Introduction](./introduction.md)
* Installation:
    * [Installation via docker](./install_with_docker.md)
    * [Installation from sources](./install_from_sources.md)
* Configuration:
    * [NGSI agent configuration](./ngsi_agent_conf.md)
    * [Configuration examples](./configuration_examples.md)
* Running:
    * [Running as a process](./running_as_process.md)
    * [Running as a service](./running_as_service.md)
* Advanced topics:
    * [Testing](./testing.md)
* [Sanity checks](./sanity_checks.md)
* [Diagnosis procedures](./diagnosis_procedures.md)
* [Reporting issues and contact information](./issues_and_contact.md)


[Top](#top)
