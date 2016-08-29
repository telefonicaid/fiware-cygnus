#<a name="top"></a>Persisting information from Orion to Kafka, using Cygnus
Content:

* [Introduction](#section1)
* [Running Orion](#section2)
* [Running Kafka](#section3)
    * [Zookeeper](#section3.1)
    * [Brokers](#section3.2)
* [Running Cygnus](#section4)
* [Creating a subscription](#section5)
* [Appending entities](#section6)
* [Updating subscription](#section7)
* [Conclusion](#section8)

##<a name="section1"></a>Introduction

Step-by-step guide for storing NGSI-like context data in Kafka topics using Cygnus. This process has some components that have to be explained detailedly. All the components are running in a local machine, using localhost and differents ports for every component.  

[Top](#top)

##<a name="section2"></a>Running Orion

(Orion Context Broker)[https://github.com/telefonicaid/fiware-orion] must be installed following [this guide](https://github.com/telefonicaid/fiware-orion/blob/develop/doc/manuals/admin/install.md). Orion allows us to manage all the whole lifecycle of context information including updates, queries, registrations and subscriptions. In this case we are going to do subscriptions to context information and update this information with new values.

Orion must be started from line command, in port 1026 and adding the `-multiservice` option in order to get a ordered distribution of our information. `-multiservice` option allows us to store the information in MongoDB in a database called with a pattern `orion-<service>`. If this option is disabled all the information will be store in a database called `orion`. Please, be sure that you execute this command with `-multiservice` option as shown below:

```
$ contextBroker -port 1026 -multiservice
```

Once executed we can continue with the rest of the components. You can check that Orion is running properly with a simple GET version:
```
$ curl -X GET http://localhost:1026/version
{
  "orion" : {
  "version" : "0.28.0-next",
  "uptime" : "0 d, 0 h, 0 m, 2 s",
  "git_hash" : "23cfaccf3f3be667d2fc08fa6edeb1da07301b88",
  "compile_time" : "Tue Mar 15 09:53:41 CET 2016",
  "compiled_by" : "root",
  "compiled_in" : "pcoello25"
  }
}
```

[Top](#top)

##<a name="section3"></a>Running Kafka

[Kafka](http://kafka.apache.org/) is a distributed, partitioned, and replicated commit log service. The information is stored by topics, published by producers in brokers and consumed by consumers. Our case needs to run a [Zookeeper](https://zookeeper.apache.org/), necessary for manage the consumer and producer actions and the functionality of the brokers connected. Instead, we need to configure the `brokers` in order to store the information properly.

Kafka distributed architecture follows the scheme above:

![Kafka's distribution][kafka]

This section is divided in two components: Zookeepers and Brokers. Every part needs a concrete configuration that will be explained properly.

[Top](#top)

###<a name="section3.1"></a>Zookeeper

[Zookeeper](https://zookeeper.apache.org/) is a part of Kafka that must be started before Brokers. In this case we need to adjust some configuration files: consumer, producer and Zookeeper.

`consumer.properties` doesn't have changes, we are going to use as it comes in the installation of Kakfa. The only value that we need to check is `zookeeper.connect`, that must be set to "localhost:2181", our local running zookeeper.

```
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# see kafka.consumer.ConsumerConfig for more details

# Zookeeper connection string
# comma separated host:port pairs, each corresponding to a zk
# server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002"
zookeeper.connect=localhost:2181

# timeout in ms for connecting to zookeeper
zookeeper.connection.timeout.ms=6000

#consumer group id
group.id=test-consumer-group

#consumer timeout
#consumer.timeout.ms=5000
```

`producer.properties` needs some changes in order to set the brokers. The parameter `metadata.broker.list` defines the list of brokers that will interact with zookeeper and will store our topics and all our information. We are going to define 3 brokers in 3 differents port as we will see in the [next section](#section3.2)

```
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# see kafka.producer.ProducerConfig for more details

############################# Producer Basics #############################

# list of brokers used for bootstrapping knowledge about the rest of the cluster
# format: host1:port1,host2:port2 ...
metadata.broker.list=localhost:9092,localhost:9093,localhost:9094

# name of the partitioner class for partitioning events; default partition spreads data randomly
#partitioner.class=

# specifies whether the messages are sent asynchronously (async) or synchronously (sync)
producer.type=sync

# specify the compression codec for all data generated: none, gzip, snappy, lz4.
# the old config values work as well: 0, 1, 2, 3 for none, gzip, snappy, lz4, respectively
compression.codec=none

# message encoder
serializer.class=kafka.serializer.DefaultEncoder

# allow topic level compression
#compressed.topics=

############################# Async Producer #############################
# maximum time, in milliseconds, for buffering data on the producer queue
#queue.buffering.max.ms=

# the maximum size of the blocking queue for buffering on the producer
#queue.buffering.max.messages=

# Timeout for event enqueue:
# 0: events will be enqueued immediately or dropped if the queue is full
# -ve: enqueue will block indefinitely if the queue is full
# +ve: enqueue will block up to this many milliseconds if the queue is full
#queue.enqueue.timeout.ms=

# the number of messages batched at the producer
#batch.num.messages=
```

`zookeeper.properties`, same as `consumer.properties`, doesn't have changes.
```
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# the directory where the snapshot is stored.
dataDir=/tmp/zookeeper
# the port at which the clients will connect
clientPort=2181
# disable the per-ip limit on the number of connections since this is a non-production config
maxClientCnxns=0
```

Once configured, it's time to run Zookeeper and Brokers. In the root folder of Kafka execute:
```
$ bin/zookeeper-server-start.sh config/zookeeper.properties
```
With this command we start the Zookeeper server in port 2181 with the configuration file that we have configured:

IMAGEN DE ZOOKEEPER

As we see in the image the execution of this command use the entire console. At this moment we have two options:
* Execute every command in different tabs, leaving Zookeeper in the current tab and opening new tabs.
* Execute every command with `nohup` option (execute in background).

At this moment we have a local Orion ContextBroker running on por 1026 and a local Zookeeper running on port 2181.

[Top](#top)

###<a name="section3.2"></a>Brokers

Brokers, known as 'servers' too, need to be configured with some values. This concrete case show how to configure 1 broker, enough for this task. They are managed by a configuration file which name is `serverx.properties`, being x the number of the broker. We are going to configure `server1.properties`.
```
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# see kafka.server.KafkaConfig for additional details and defaults

############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=1

############################# Socket Server Settings #############################

# The port the socket server listens on
port=9092

# Hostname the broker will bind to. If not set, the server will bind to all interfaces
host.name=0.0.0.0

# Hostname the broker will advertise to producers and consumers. If not set, it uses the
# value for "host.name" if configured.  Otherwise, it will use the value returned from
# java.net.InetAddress.getCanonicalHostName().
#advertised.host.name=0.0.0.0

# The port to publish to ZooKeeper for clients to use. If this is not set,
# it will publish the same port that the broker binds to.
#advertised.port=9092

# The number of threads handling network requests
num.network.threads=3

# The number of threads doing disk I/O
num.io.threads=8

# The send buffer (SO_SNDBUF) used by the socket server
socket.send.buffer.bytes=102400

# The receive buffer (SO_RCVBUF) used by the socket server
socket.receive.buffer.bytes=102400

# The maximum size of a request that the socket server will accept (protection against OOM)
socket.request.max.bytes=104857600


############################# Log Basics #############################

# A comma seperated list of directories under which to store log files
log.dirs=/tmp/kafka-logs-1

# The default number of log partitions per topic. More partitions allow greater
# parallelism for consumption, but this will also result in more files across
# the brokers.
num.partitions=1

# The number of threads per data directory to be used for log recovery at startup and flushing at shutdown.
# This value is recommended to be increased for installations with data dirs located in RAID array.
num.recovery.threads.per.data.dir=1

############################# Log Flush Policy #############################

# Messages are immediately written to the filesystem but by default we only fsync() to sync
# the OS cache lazily. The following configurations control the flush of data to disk.
# There are a few important trade-offs here:
#    1. Durability: Unflushed data may be lost if you are not using replication.
#    2. Latency: Very large flush intervals may lead to latency spikes when the flush does occur as there will be a lot of data to flush.
#    3. Throughput: The flush is generally the most expensive operation, and a small flush interval may lead to exceessive seeks.
# The settings below allow one to configure the flush policy to flush data after a period of time or
# every N messages (or both). This can be done globally and overridden on a per-topic basis.

# The number of messages to accept before forcing a flush of data to disk
#log.flush.interval.messages=10000

# The maximum amount of time a message can sit in a log before we force a flush
#log.flush.interval.ms=1000

############################# Log Retention Policy #############################

# The following configurations control the disposal of log segments. The policy can
# be set to delete segments after a period of time, or after a given size has accumulated.
# A segment will be deleted whenever *either* of these criteria are met. Deletion always happens
# from the end of the log.

# The minimum age of a log file to be eligible for deletion
log.retention.hours=168

# A size-based retention policy for logs. Segments are pruned from the log as long as the remaining
# segments don't drop below log.retention.bytes.
#log.retention.bytes=1073741824

# The maximum size of a log segment file. When this size is reached a new log segment will be created.
log.segment.bytes=1073741824

# The interval at which log segments are checked to see if they can be deleted according
# to the retention policies
log.retention.check.interval.ms=300000

# By default the log cleaner is disabled and the log retention policy will default to just delete segments after their retention expires.
# If log.cleaner.enable=true is set the cleaner will be enabled and individual logs can then be marked for log compaction.
log.cleaner.enable=false

############################# Zookeeper #############################

# Zookeeper connection string (see zookeeper docs for details).
# This is a comma separated host:port pairs, each corresponding to a zk
# server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002".
# You can also append an optional chroot string to the urls to specify the
# root directory for all kafka znodes.
zookeeper.connect=localhost:2181

# Timeout in ms for connecting to zookeeper
zookeeper.connection.timeout.ms=6000
```

Some parameters important here:
* broker.id: Unique id for a broker. Must be different if we use more than one broker.
* port: 9092 is the chosen port for first Broker. Everyone has its own port. For next brokers you must use 9093, 9094, 9095...
* host.name: Must be 0.0.0.0
* log.dirs: Path to the file with logs. By default /tmp/kafka-logs.
* zookeeper.connect: Same as `zookeeper.properties`, this parameter must be `localhost:2181`

Once configured, it's time to run the Broker. In the root folder of Kafka execute:
```
& bin/kafka-server-start.sh config/server1.properties
```

![Broker console initiation logs][broker]

As we see in the image the execution of this command use the entire console. At this moment we have two options:
* Execute every command in different tabs, leaving the broker in the current tab and opening new tabs.
* Execute every command with `nohup` option (execute in background).

At this moment we have a local Orion ContextBroker running on port 1026, a local Zookeeper running on port 2181 and a local Broker running on port 9092.

[Top](#top)

##<a name="section4"></a>Running Cygnus

Cygnus is the connector in charge of persisting Orion context data in Kafka, creating a historical view of such data. Cygnus runs once we have configured the file `agent.conf`, that contains all the values necessary. We are going to use the agent below:
```
cygnusagent.sources = http-source
cygnusagent.sinks = kafka-sink
cygnusagent.channels = kafka-channel

cygnusagent.sources.http-source.channels = kafka-channel
cygnusagent.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnusagent.sources.http-source.port = 5050
cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
cygnusagent.sources.http-source.handler.notification_target = /notify
cygnusagent.sources.http-source.handler.default_service = def_serv
cygnusagent.sources.http-source.handler.default_service_path = /def_servpath
cygnusagent.sources.http-source.handler.events_ttl = 2
cygnusagent.sources.http-source.interceptors = ts gi
cygnusagent.sources.http-source.interceptors.ts.type = timestamp
cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = /path/to/your/grouping_rules/conf/grouping_rules.conf

cygnusagent.channels.kafka-channel.type = memory
cygnusagent.channels.kafka-channel.capacity = 1000
cygnusagent.channels.kafka-channel.trasactionCapacity = 100

cygnusagent.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.NGSIKafkaSink
cygnusagent.sinks.kafka-sink.channel = kafka-channel
cygnusagent.sinks.kafka-sink.enable_grouping = false
cygnusagent.sinks.kafka-sink.data_model = dm-by-entity
cygnusagent.sinks.kafka-sink.broker_list = localhost:9092
cygnusagent.sinks.kafka-sink.zookeeper_endpoint = localhost:2181
cygnusagent.sinks.kafka-sink.batch_size = 1
cygnusagent.sinks.kafka-sink.batch_timeout = 10
```

Important agent values:
* cygnusagent.sources.http-source.port: Must be 5050. It's important to set this value for the subscription.
* cygnusagent.sources.http-source.handler.notification_target: We are going to use `/notify`, and it's important too for the subscription.
* cygnusagent.sinks.kafka-sink.broker_list: As we have configure only 1 broker must be the local direction of it (localhost:9092)
* cygnusagent.sinks.kafka-sink.zookeeper_endpoint: The same as the previous files (localhost:2181)

'agent.conf' file must be in the folder `conf`, ubicated in the root folder of `cygnus/apache-flume-1.4.0/` and must be executed like this:
```
$ bin/cygnus-flume-ng agent --conf conf -f conf/agent.conf -n cygnusagent -Dflume.root.logger=DEBUG,console
```

At this moment we have a local Orion ContextBroker running on por 1026, a local Zookeeper running on port 2181, a local Broker running on port 9092 and a local Cygnus running on port 5050.

[Top](#top)

##<a name="section5"></a>Creating a subscription

After all the previous requisites we can do a subscription. Now we have to define the behaviour of our subscription, defining an entity, a type for that entity, a Fiware-Service and a Fiware-ServicePath that will be part of the request. For this example we use:
* Entity: Book1
* Type: Book
* Fiware-Service: Library
* Fiware-ServicePath: /catalog

Executing the request to our local instance of Orion (in port 1026), sending headers with `Content-Type`, `Accept`, `Fiware-Service` and `Fiware-ServicePath` and a complete JSON with the information required by Orion.

```
(curl localhost:1026/v1/subscribeContext -s -S --header 'Content-type: application/json' --header 'Accept: application/json' --header 'Fiware-Service: Library' --header 'Fiware-ServicePath: /catalog' -d @- | python -mjson.tool) <<EOF
{
    "entities": [
        {
            "type": "Book",
            "isPattern": "false",
            "id": "Book1"
        }
    ],
    "attributes": [
    ],
    "reference": "http://localhost:5050/notify",
    "duration": "P1M",
    "notifyConditions": [
        {
            "type": "ONCHANGE",
            "condValues": [
                "title",
                "pages",
                "price"
            ]
        }
    ],
    "throttling": "PT5S"
}
EOF
```

Subscription to Orion have some JSON fields with different purposes:
* Field "entities": Describe the entity and its type.
* Field "attributes": Defines the attributes that will be notified when receives an update.
* Field "reference": Endpoint where Orion will send the notifications. Previous values from Cygnus `cygnusagent.sources.http-source.port` and `cygnusagent.sources.http-source.handler.notification_target` define that, for that reason our agent must be configured with `5050/notify`.
* Field "notifyConditions":
  * Field "condValues": Orion will send updates when any of these attributes change.

Once sent, we must receive a positive answer from Orion with a 200 OK status advertising that we are subscribe to the entity `Book1`, of type `Book` with a in the Service `Library` and the Service-Path `/catalog`.

```
{
    "subscribeResponse": {
        "duration": "P1M",
        "subscriptionId": "51c0ac9ed714fb3b37d7d5a8"
    }
}
```

We can easily check that the creation is correct entering in MongoDB. As we explain in [Orion section](#section2) MongoDB creates a database with the name `orion-<service>`. For instance, our database name will be `orion-library` and will be empty at the moment. Checking in mongo:
```
$ mongo
MongoDB shell version: 2.6.10
connecting to: test
> show databases
admin               (empty)
local               0.078GB
orion-library       0.031GB
```

[Top](#top)

##<a name="section6"></a>Appending entities

The first action that we need is to append an entity. As we said previously we are going to append an entity `Book1` of type `Book`. Sending a request to our local Orion with the `APPEND` option we store the entity for future notifications:
```
(curl localhost:1026/v1/updateContext -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Fiware-Service: Library' --header 'Fiware-ServicePath: /catalog' -d @- | python -mjson.tool) <<EOF
{
    "contextElements": [
        {
            "type": "Book",
            "isPattern": "false",
            "id": "Book1",
            "attributes": [
                {
                    "name": "title",
                    "type": "text",
                    "value": "Game of Thrones: Book 1"
                },
                {
                    "name": "pages",
                    "type": "integer",
                    "value": "927"
                },
                {
                    "name": "price",
                    "type": "float",
                    "value": "18.50"
                }
            ]
        }
    ],
    "updateAction": "APPEND"
}
EOF
```

As we can see, we store information about the entity `Book1` of type `Book` in Fiware-Service `Library` and Fiware-ServicePath `/catalog`. Besides this, Cygnus has received the notification and it has stored the information into Kafka, in the topic `library_catalog_book1_book`. First, check the list of topics created in Kafka and then show the content of the topic that we had just created.
```
$ bin/kafka-topics.sh --list --zookeeper localhost:2181
library_catalog_book1_book

$ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beggining --topic library_catalog_book1_book
{"headers":[{"fiware-service":"library"},{"fiware-servicepath":"/catalog"},{"timestamp":"1472120955879"}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"927"},{"name":"price","type":"float","value":"18.50"}],"type":"Book","isPattern":"false","id":"Book1"}}
Consumed 1 message
```

Another check of our success are the log of Cygnus. The traces show us how Cygnus store the information as we can see in the next image:

![APPEND request on Cygnus][cugnus_append]

[Top](#top)

##<a name="section7"></a>Updating subscription

Once appended the entity, we are going to update the information. This request is the same as `APPEND`, the only different is that, in this case, we have to send the `UPDATE` option.
```
(curl localhost:1026/v1/updateContext -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Fiware-Service: Library' --header 'Fiware-ServicePath: /catalog' -d @- | python -mjson.tool) <<EOF
{
    "contextElements": [
        {
            "type": "Book",
            "isPattern": "false",
            "id": "Book1",
            "attributes": [
                {
                    "name": "title",
                    "type": "text",
                    "value": "Game of Thrones: Book 1"
                },
                {
                    "name": "pages",
                    "type": "integer",
                    "value": "545"
                },
                {
                    "name": "price",
                    "type": "float",
                    "value": "12.50"
                }
            ]
        }
    ],
    "updateAction": "UPDATE"
}
EOF
```
Sending the same headers with the information about Fiware-Service and Fiware-ServicePath and the same information about EntityId and EntityType we can update the content of our appended entities. Once sent, we can check the update in Kafka:
```
$ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic libraryorion_catalog_book1_book
{"headers":[{"fiware-service":"library"},{"fiware-servicepath":"/catalog"},{"timestamp":"1472120955879"}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"927"},{"name":"price","type":"float","value":"18.50"}],"type":"Book","isPattern":"false","id":"Book1"}}
{"headers":[{"fiware-service":"library"},{"fiware-servicepath":"/catalog"},{"timestamp":"1472120955879"}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"545"},{"name":"price","type":"float","value":"12.50"}],"type":"Book","isPattern":"false","id":"Book1"}}
Consumed 2 messages
```

Once again, another check are the traces of Cygnus. There we can see how Cygnus receive the notification and store the information in Kafka:

![UPDATE request on Cygnus][cygnus_update]

[Top](#top)

[kafka]: https://github.com/telefonicaid/fiware-cosmos/blob/doc/1132_add_integration_examples/doc/cygnus-ngsi/integration/img/kafka_zookeeper.png "Architecture for sinfonier"
[broker]: https://github.com/pcoello25/fiware-cygnus/blob/doc/1132_add_integration_examples/doc/cygnus-ngsi/integration/img/broker.png "Broker"
[cygnus_append]: https://github.com/telefonicaid/fiware-cosmos/blob/doc/1132_add_integration_examples/doc/cygnus-ngsi/integration/img/cygnus_append.png "APPEND request on Cygnus"
[cygnus_update]: https://github.com/telefonicaid/fiware-cosmos/blob/doc/1132_add_integration_examples/doc/cygnus-ngsi/integration/img/cygnus_update.png "UPDATE request on Cygnus"
