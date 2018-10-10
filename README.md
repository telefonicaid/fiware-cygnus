# Cygnus

[![FIWARE Core Context Management](https://img.shields.io/badge/FIWARE-Core-233c68.svg?label=FIWARE&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABsAAAAVCAYAAAC33pUlAAAABHNCSVQICAgIfAhkiAAAA8NJREFUSEuVlUtIFlEUx+eO+j3Uz8wSLLJ3pBiBUljRu1WLCAKXbXpQEUFERSQF0aKVFAUVrSJalNXGgmphFEhQiZEIPQwKLbEUK7VvZrRvbr8zzjfNl4/swplz7rn/8z/33HtmRhn/MWzbXmloHVeG0a+VSmAXorXS+oehVD9+0zDN9mgk8n0sWtYnHo5tT9daH4BsM+THQC8naK02jCZ83/HlKaVSzBey1sm8BP9nnUpdjOfl/Qyzj5ust6cnO5FItJLoJqB6yJ4QuNcjVOohegpihshS4F6S7DTVVlNtFFxzNBa7kcaEwUGcbVnH8xOJD67WG9n1NILuKtOsQG9FngOc+lciic1iQ8uQGhJ1kVAKKXUs60RoQ5km93IfaREvuoFj7PZsy9rGXE9G/NhBsDOJ63Acp1J82eFU7OIVO1OxWGwpSU5hb0GqfMydMHYSdiMVnncNY5Vy3VbwRUEydvEaRxmAOSSqJMlJISTxS9YWTYLcg3B253xsPkc5lXk3XLlwrPLuDPKDqDIutzYaj3eweMkPeCCahO3+fEIF8SfLtg/5oI3Mh0ylKM4YRBaYzuBgPuRnBYD3mmhA1X5Aka8NKl4nNz7BaKTzSgsLCzWbvyo4eK9r15WwLKRAmmCXXDoA1kaG2F4jWFbgkxUnlcrB/xj5iHxFPiBN4JekY4nZ6ccOiQ87hgwhe+TOdogT1nfpgEDTvYAucIwHxBfNyhpGrR+F8x00WD33VCNTOr/Wd+9C51Ben7S0ZJUq3qZJ2OkZz+cL87ZfWuePlwRcHZjeUMxFwTrJZAJfSvyWZc1VgORTY8rBcubetdiOk+CO+jPOcCRTF+oZ0okUIyuQeSNL/lPrulg8flhmJHmE2gBpE9xrJNkwpN4rQIIyujGoELCQz8ggG38iGzjKkXufJ2Klun1iu65bnJub2yut3xbEK3UvsDEInCmvA6YjMeE1bCn8F9JBe1eAnS2JksmkIlEDfi8R46kkEkMWdqOv+AvS9rcp2bvk8OAESvgox7h4aWNMLd32jSMLvuwDAwORSE7Oe3ZRKrFwvYGrPOBJ2nZ20Op/mqKNzgraOTPt6Bnx5citUINIczX/jUw3xGL2+ia8KAvsvp0ePoL5hXkXO5YvQYSFAiqcJX8E/gyX8QUvv8eh9XUq3h7mE9tLJoNKqnhHXmCO+dtJ4ybSkH1jc9XRaHTMz1tATBe2UEkeAdKu/zWIkUbZxD+veLxEQhhUFmbnvOezsJrk+zmqMo6vIL2OXzPvQ8v7dgtpoQnkF/LP8Ruu9zXdJHg4igAAAABJRU5ErkJgggA=)](https://www.fiware.org/developers/catalogue/)
[![License: APGL](https://img.shields.io/github/license/telefonicaid/fiware-cygnus.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Documentation badge](https://img.shields.io/readthedocs/fiware-cygnus.svg)](http://fiware-cygnus.readthedocs.org/en/latest/?badge=latest)
[![](https://img.shields.io/badge/tag-fiware--cygnus-orange.svg?logo=stackoverflow)](http://stackoverflow.com/questions/tagged/fiware-cygnus)
[![Support badge]( https://img.shields.io/badge/support-askbot-yellowgreen.svg)](https://ask.fiware.org/questions/scope%3Aall/tags%3Acygnus/)
<br>
[![Docker badge](https://img.shields.io/docker/pulls/fiware/cygnus-common.svg)](https://hub.docker.com/r/fiware/cygnus-common/)
[![Docker badge](https://img.shields.io/docker/pulls/fiware/cygnus-ngsi.svg)](https://hub.docker.com/r/fiware/cygnus-ngsi/)
[![Docker badge](https://img.shields.io/docker/pulls/fiware/cygnus-twitter.svg)](https://hub.docker.com/r/fiware/cygnus-twitter/)

## Welcome
This project is part of [FIWARE](http://fiware.org), being part of the [Cosmos](http://catalogue.fiware.org/enablers/bigdata-analysis-cosmos) Ecosystem.

Cygnus is a connector in charge of persisting certain sources of data in certain configured third-party storages, creating a historical view of such data.

Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/), a technology addressing the design and execution of data collection and persistence <i>agents</i>. An agent is basically composed of a listener or source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Cygnus is designed to run a specific Flume agent per source of data.

Current stable release is able to persist the following sources of data in the following third-party storages:

* NGSI-like context data in:
    * [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.
    * [MySQL](https://www.mysql.com/), the well-known relational database manager.
    * [CKAN](http://ckan.org/), an Open Data platform.
    * [MongoDB](https://www.mongodb.org/), the NoSQL document-oriented database.
    * [STH Comet](https://github.com/telefonicaid/IoT-STH), a Short-Term Historic database built on top of MongoDB.
    * [Kafka](http://kafka.apache.org/), the publish-subscribe messaging broker.
    * [DynamoDB](https://aws.amazon.com/dynamodb/), a cloud-based NoSQL database by [Amazon Web Services](https://aws.amazon.com/).
    * [PostgreSQL](http://www.postgresql.org/), the well-known relational database manager.
    * [Carto](https://carto.com/), the database specialized in geolocated data.
* Twitter data in:
    * [HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html), the [Hadoop](http://hadoop.apache.org/) distributed file system.

**IMPORTANT NOTE**: for the time being, cygnus-ngsi and cygus-twitter agents cannot be installed in the same base path, because of an incompatibility with the required version of the `httpclient` library. Of course, if you are going to use just one of the agents, there is no problem at all.

## Cygnus place in FIWARE architecture
Cygnus (more specifically, cygnus-ngsi agent) plays the role of a connector between Orion Context Broker (which is a NGSI source of data) and many FIWARE storages such as CKAN, Cosmos Big Data (Hadoop) and STH Comet. Of course, as previously said, you may add MySQL, Kafka, Carto, etc as other non FIWARE storages to the FIWARE architecture.

![FIWARE architecture](doc/images/fiware_architecture.png)

## Further documentation
The per agent **Quick Start Guide** found at readthedocs.org provides a good documentation summary ([cygnus-ngsi](http://fiware-cygnus.readthedocs.io/en/latest/cygnus-ngsi/quick_start_guide/index.html), [cygnus-twitter](http://fiware-cygnus.readthedocs.io/en/latest/cygnus-twitter/quick_start_guide/index.html)).

Nevertheless, both the **Installation and Administration Guide** and the **User and Programmer Guide** for each agent also found at [readthedocs.org](http://fiware-cygnus.readthedocs.io/en/latest/) cover more advanced topics.

The per agent **Flume Extensions Catalogue** completes the available documentation for Cygnus ([cygnus-ngsi](http://fiware-cygnus.readthedocs.io/en/latest/cygnus-ngsi/flume_extensions_catalogue/introduction/index.html), [cygnus-twitter](http://fiware-cygnus.readthedocs.io/en/latest/cygnus-twitter/flume_extensions_catalogue/introduction/index.html)).

Other interesting links are:

* Our [Apiary Documentation](http://telefonicaid.github.io/fiware-cygnus/api/latest) if you want to know how to use our API methods for Cygnus.
* [cygnus-ngsi](doc/cygnus-ngsi/integration) **integration** examples .
* [cygnus-ngsi](https://edu.fiware.org/mod/resource/view.php?id=1037) **introductory course** in FIWARE Academy.
* The [Contributing Guidelines](doc/contributing/contributing_guidelines.md) if your aim is to extend Cygnus.

## Licensing
Cygnus is licensed under Affero General Public License (GPL) version 3. You can find a [copy of this license in the repository](./LICENSE).

## Reporting issues and contact information
Any doubt you may have, please refer to the [Cygnus Core Team](./reporting_issues_and_contact.md).
