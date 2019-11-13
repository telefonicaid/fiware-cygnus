# <a name="top"></a>Persisting information from Orion to Kafka, using Cygnus
Content:

* [Introduction](#section1)
* [Running Orion](#section2)
* [Running Kafka](#section3)
    * [Zookeeper](#section3.1)
    * [Brokers](#section3.2)
* [Running Cygnus](#section4)
* [Creating a subscription](#section5)
* [Appending entities](#section6)
* [Updating entities](#section7)

## <a name="section1"></a>Introduction

Step-by-step guide for storing NGSI-like context data in Kafka topics using Cygnus. This process has some components that have to be explained detailedly. All the components are running in a local machine, using localhost and differents ports for every component.  

[Top](#top)

## <a name="section2"></a>Running Orion

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

## <a name="section3"></a>Running Kafka

[Kafka](http://kafka.apache.org/) is a distributed, partitioned, and replicated commit log service. The information is stored by topics, published by producers in brokers and consumed by consumers. Our case needs to run a [Zookeeper](https://zookeeper.apache.org/), necessary for manage the consumer and producer actions and the functionality of the brokers connected. Instead, we need to configure the `brokers` in order to store the information properly.

Kafka distributed architecture follows the scheme above:

![Kafka's distribution][kafka]

This section is divided in two components: Zookeepers and Brokers. Every part needs a concrete configuration that will be explained properly.

[Top](#top)

### <a name="section3.1"></a>Zookeeper

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
```
[2016-08-30 07:33:39,178] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2016-08-30 07:33:39,219] INFO autopurge.snapRetainCount set to 3 (org.apache.zookeeper.server.DatadirCleanupManager)
[2016-08-30 07:33:39,219] INFO autopurge.purgeInterval set to 0 (org.apache.zookeeper.server.DatadirCleanupManager)
[2016-08-30 07:33:39,219] INFO Purge task is not scheduled. (org.apache.zookeeper.server.DatadirCleanupManager)
[2016-08-30 07:33:39,219] WARN Either no config or no quorum defined in config, running  in standalone mode (org.apache.zookeeper.server.quorum.QuorumPeerMain)
[2016-08-30 07:33:39,293] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2016-08-30 07:33:39,293] INFO Starting server (org.apache.zookeeper.server.ZooKeeperServerMain)
[2016-08-30 07:33:39,314] INFO Server environment:zookeeper.version=3.4.6-1569965, built on 02/20/2014 09:09 GMT (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:host.name=pcoello25.PC (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:java.version=1.8.0_101 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:java.vendor=Oracle Corporation (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:java.home=/usr/lib/jvm/java-8-oracle/jre (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:java.class.path=:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../core/build/dependant-libs-2.10.4*/*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../examples/build/libs//kafka-examples*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../contrib/hadoop-consumer/build/libs//kafka-hadoop-consumer*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../contrib/hadoop-producer/build/libs//kafka-hadoop-producer*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../clients/build/libs/kafka-clients*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/jopt-simple-3.2.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-javadoc.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-scaladoc.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-sources.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-test.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka-clients-0.8.2.1.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/log4j-1.2.16.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/lz4-1.2.0.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/metrics-core-2.2.0.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/scala-library-2.10.4.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/slf4j-api-1.7.6.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/slf4j-log4j12-1.6.1.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/snappy-java-1.1.1.6.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/zkclient-0.3.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/zookeeper-3.4.6.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../core/build/libs/kafka_2.10*.jar (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,314] INFO Server environment:java.io.tmpdir=/tmp (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:java.compiler=<NA> (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:os.name=Linux (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:os.arch=amd64 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:os.version=4.4.0-36-generic (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:user.name=pcoello25 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:user.home=/home/pcoello25 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,315] INFO Server environment:user.dir=/home/pcoello25/cygnus/kafka_2.10-0.8.2.1 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,337] INFO tickTime set to 3000 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,337] INFO minSessionTimeout set to -1 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,337] INFO maxSessionTimeout set to -1 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:39,446] INFO binding to port 0.0.0.0/0.0.0.0:2181 (org.apache.zookeeper.server.NIOServerCnxnFactory)
[2016-08-30 07:33:54,325] INFO Accepted socket connection from /0:0:0:0:0:0:0:1:45268 (org.apache.zookeeper.server.NIOServerCnxnFactory)
[2016-08-30 07:33:54,412] INFO Client attempting to establish new session at /0:0:0:0:0:0:0:1:45268 (org.apache.zookeeper.server.ZooKeeperServer)
[2016-08-30 07:33:54,415] INFO Creating new log file: log.1 (org.apache.zookeeper.server.persistence.FileTxnLog)
[2016-08-30 07:33:54,485] INFO Established session 0x156d9f04d320000 with negotiated timeout 6000 for client /0:0:0:0:0:0:0:1:45268 (org.apache.zookeeper.server.ZooKeeperServer)
```

As we see in the text above, the execution of this command use the entire console. At this moment we have two options:
* Execute every command in different tabs, leaving Zookeeper in the current tab and opening new tabs.
* Execute every command with `nohup` option (execute in background).

At this moment we have a local Orion ContextBroker running on por 1026 and a local Zookeeper running on port 2181.

[Top](#top)

### <a name="section3.2"></a>Brokers

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

[2016-08-30 07:33:53,845] INFO Verifying properties (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,935] INFO Property broker.id is overridden to 1 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,936] INFO Property host.name is overridden to 0.0.0.0 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,936] INFO Property log.cleaner.enable is overridden to false (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,936] INFO Property log.dirs is overridden to /tmp/kafka-logs-1 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,936] INFO Property log.retention.check.interval.ms is overridden to 300000 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,936] INFO Property log.retention.hours is overridden to 168 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,937] INFO Property log.segment.bytes is overridden to 1073741824 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,937] INFO Property num.io.threads is overridden to 8 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,937] INFO Property num.network.threads is overridden to 3 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,937] INFO Property num.partitions is overridden to 1 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,938] INFO Property num.recovery.threads.per.data.dir is overridden to 1 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,938] INFO Property port is overridden to 9092 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,938] INFO Property socket.receive.buffer.bytes is overridden to 102400 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,938] INFO Property socket.request.max.bytes is overridden to 104857600 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,938] INFO Property socket.send.buffer.bytes is overridden to 102400 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,939] INFO Property zookeeper.connect is overridden to localhost:2181 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:53,939] INFO Property zookeeper.connection.timeout.ms is overridden to 6000 (kafka.utils.VerifiableProperties)
[2016-08-30 07:33:54,021] INFO [Kafka Server 1], starting (kafka.server.KafkaServer)
[2016-08-30 07:33:54,046] INFO [Kafka Server 1], Connecting to zookeeper on localhost:2181 (kafka.server.KafkaServer)
[2016-08-30 07:33:54,087] INFO Starting ZkClient event thread. (org.I0Itec.zkclient.ZkEventThread)
[2016-08-30 07:33:54,098] INFO Client environment:zookeeper.version=3.4.6-1569965, built on 02/20/2014 09:09 GMT (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,098] INFO Client environment:host.name=pcoello25.PC (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,098] INFO Client environment:java.version=1.8.0_101 (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,098] INFO Client environment:java.vendor=Oracle Corporation (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,098] INFO Client environment:java.home=/usr/lib/jvm/java-8-oracle/jre (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,098] INFO Client environment:java.class.path=:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../core/build/dependant-libs-2.10.4*/*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../examples/build/libs//kafka-examples*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../contrib/hadoop-consumer/build/libs//kafka-hadoop-consumer*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../contrib/hadoop-producer/build/libs//kafka-hadoop-producer*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../clients/build/libs/kafka-clients*.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/jopt-simple-3.2.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-javadoc.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-scaladoc.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-sources.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka_2.10-0.8.2.1-test.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/kafka-clients-0.8.2.1.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/log4j-1.2.16.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/lz4-1.2.0.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/metrics-core-2.2.0.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/scala-library-2.10.4.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/slf4j-api-1.7.6.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/slf4j-log4j12-1.6.1.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/snappy-java-1.1.1.6.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/zkclient-0.3.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../libs/zookeeper-3.4.6.jar:/home/pcoello25/cygnus/kafka_2.10-0.8.2.1/bin/../core/build/libs/kafka_2.10*.jar (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,098] INFO Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:java.io.tmpdir=/tmp (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:java.compiler=<NA> (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:os.name=Linux (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:os.arch=amd64 (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:os.version=4.4.0-36-generic (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:user.name=pcoello25 (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:user.home=/home/pcoello25 (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,099] INFO Client environment:user.dir=/home/pcoello25/cygnus/kafka_2.10-0.8.2.1 (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,100] INFO Initiating client connection, connectString=localhost:2181 sessionTimeout=6000 watcher=org.I0Itec.zkclient.ZkClient@4450d156 (org.apache.zookeeper.ZooKeeper)
[2016-08-30 07:33:54,173] INFO Opening socket connection to server localhost/0:0:0:0:0:0:0:1:2181. Will not attempt to authenticate using SASL (unknown error) (org.apache.zookeeper.ClientCnxn)
[2016-08-30 07:33:54,324] INFO Socket connection established to localhost/0:0:0:0:0:0:0:1:2181, initiating session (org.apache.zookeeper.ClientCnxn)
[2016-08-30 07:33:54,487] INFO Session establishment complete on server localhost/0:0:0:0:0:0:0:1:2181, sessionid = 0x156d9f04d320000, negotiated timeout = 6000 (org.apache.zookeeper.ClientCnxn)
[2016-08-30 07:33:54,489] INFO zookeeper state changed (SyncConnected) (org.I0Itec.zkclient.ZkClient)
[2016-08-30 07:33:54,820] INFO Log directory '/tmp/kafka-logs' not found, creating it. (kafka.log.LogManager)
[2016-08-30 07:33:54,887] INFO Loading logs. (kafka.log.LogManager)
[2016-08-30 07:33:54,922] INFO Logs loading complete. (kafka.log.LogManager)
[2016-08-30 07:33:54,923] INFO Starting log cleanup with a period of 300000 ms. (kafka.log.LogManager)
[2016-08-30 07:33:54,947] INFO Starting log flusher with a default period of 9223372036854775807 ms. (kafka.log.LogManager)
[2016-08-30 07:33:55,008] INFO Awaiting socket connections on 0.0.0.0:9092. (kafka.network.Acceptor)
[2016-08-30 07:33:55,009] INFO [Socket Server on Broker 1], Started (kafka.network.SocketServer)
[2016-08-30 07:33:55,254] INFO Will not load MX4J, mx4j-tools.jar is not in the classpath (kafka.utils.Mx4jLoader$)
[2016-08-30 07:33:55,314] INFO 1 successfully elected as leader (kafka.server.ZookeeperLeaderElector)
[2016-08-30 07:33:55,438] INFO Registered broker 1 at path /brokers/ids/1 with address 0.0.0.0:9092. (kafka.utils.ZkUtils$)
[2016-08-30 07:33:55,511] INFO [Kafka Server 1], started (kafka.server.KafkaServer)
[2016-08-30 07:33:55,603] INFO New leader is 1 (kafka.server.ZookeeperLeaderElector$LeaderChangeListener)
```

As we see in the text above, the execution of this command use the entire console. At this moment we have two options:
* Execute every command in different tabs, leaving the broker in the current tab and opening new tabs.
* Execute every command with `nohup` option (execute in background).

At this moment we have a local Orion ContextBroker running on port 1026, a local Zookeeper running on port 2181 and a local Broker running on port 9092.

[Top](#top)

## <a name="section4"></a>Running Cygnus

Cygnus is the connector in charge of persisting Orion context data in Kafka, creating a historical view of such data. Cygnus runs once we have configured the file `agent.conf`, that contains all the values necessary. We are going to use the agent below:
```
cygnus-ngsi.sources = http-source
cygnus-ngsi.sinks = kafka-sink
cygnus-ngsi.channels = kafka-channel

cygnus-ngsi.sources.http-source.channels = kafka-channel
cygnus-ngsi.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnus-ngsi.sources.http-source.port = 5050
cygnus-ngsi.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
cygnus-ngsi.sources.http-source.handler.notification_target = /notify
cygnus-ngsi.sources.http-source.handler.default_service = def_serv
cygnus-ngsi.sources.http-source.handler.default_service_path = /def_servpath
cygnus-ngsi.sources.http-source.handler.events_ttl = 2
cygnus-ngsi.sources.http-source.interceptors = ts gi
cygnus-ngsi.sources.http-source.interceptors.ts.type = timestamp
cygnus-ngsi.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
cygnus-ngsi.sources.http-source.interceptors.gi.grouping_rules_conf_file = /path/to/your/grouping_rules/conf/grouping_rules.conf

cygnus-ngsi.channels.kafka-channel.type = memory
cygnus-ngsi.channels.kafka-channel.capacity = 1000
cygnus-ngsi.channels.kafka-channel.trasactionCapacity = 100

cygnus-ngsi.sinks.kafka-sink.type = com.telefonica.iot.cygnus.sinks.NGSIKafkaSink
cygnus-ngsi.sinks.kafka-sink.channel = kafka-channel
cygnus-ngsi.sinks.kafka-sink.enable_grouping = false
cygnus-ngsi.sinks.kafka-sink.data_model = dm-by-entity
cygnus-ngsi.sinks.kafka-sink.broker_list = localhost:9092
cygnus-ngsi.sinks.kafka-sink.zookeeper_endpoint = localhost:2181
cygnus-ngsi.sinks.kafka-sink.batch_size = 1
cygnus-ngsi.sinks.kafka-sink.batch_timeout = 10
```

Important agent values:
* cygnus-ngsi.sources.http-source.port: Must be 5050. It's important to set this value for the subscription.
* cygnus-ngsi.sources.http-source.handler.notification_target: We are going to use `/notify`, and it's important too for the subscription.
* cygnus-ngsi.sinks.kafka-sink.broker_list: As we have configure only 1 broker must be the local direction of it (localhost:9092)
* cygnus-ngsi.sinks.kafka-sink.zookeeper_endpoint: The same as the previous files (localhost:2181)

'agent.conf' file must be in the folder `conf`, ubicated in the root folder of `cygnus/apache-flume-1.4.0/` and must be executed like this:
```
$ bin/cygnus-flume-ng agent --conf conf -f conf/agent.conf -n cygnus-ngsi -Dflume.root.logger=DEBUG,console
```

At this moment we have a local Orion ContextBroker running on por 1026, a local Zookeeper running on port 2181, a local Broker running on port 9092 and a local Cygnus running on port 5050.

[Top](#top)

## <a name="section5"></a>Creating a subscription

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
* Field "reference": Endpoint where Orion will send the notifications. Previous values from Cygnus `cygnus-ngsi.sources.http-source.port` and `cygnus-ngsi.sources.http-source.handler.notification_target` define that, for that reason our agent must be configured with `5050/notify`.
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
MongoDB shell version: 3.6.14
connecting to: test
> show databases
admin               (empty)
local               0.078GB
orion-library       0.031GB
```

[Top](#top)

## <a name="section6"></a>Appending entities

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

Another check of our success are the log of Cygnus. The traces show us how Cygnus store the information as we can see:
```
time=2016-08-30T08:02:58.434CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header host received with value localhost:5050
time=2016-08-30T08:02:58.435CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header user-agent received with value orion/0.28.0-next libcurl/7.47.0
time=2016-08-30T08:02:58.435CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-service received with value Library
time=2016-08-30T08:02:58.435CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-servicepath received with value /catalog
time=2016-08-30T08:02:58.436CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header accept received with value application/xml, application/json
time=2016-08-30T08:02:58.436CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-length received with value 706
time=2016-08-30T08:02:58.436CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-type received with value application/json; charset=utf-8
time=2016-08-30T08:02:58.435CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header host received with value localhost:5050
time=2016-08-30T08:02:58.436CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header user-agent received with value orion/0.28.0-next libcurl/7.47.0
time=2016-08-30T08:02:58.437CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-service received with value Library
time=2016-08-30T08:02:58.437CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-servicepath received with value /catalog
time=2016-08-30T08:02:58.437CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header accept received with value application/xml, application/json
time=2016-08-30T08:02:58.437CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-length received with value 706
time=2016-08-30T08:02:58.437CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-type received with value application/json; charset=utf-8
time=2016-08-30T08:02:58.451CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[248] : Starting internal transaction (31ce961a-2767-4acd-bd5e-b623c3062148)
time=2016-08-30T08:02:58.451CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[248] : Starting internal transaction (4b1be263-2502-4ca6-91fc-e43e8e642904)
time=2016-08-30T08:02:58.452CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[264] : Received data ({  "subscriptionId" : "57c52135e08b32f1445139ee",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "type" : "Book",        "isPattern" : "false",        "id" : "Book1",        "attributes" : [          {            "name" : "title",            "type" : "text",            "value" : "Game of Thrones: Book 1"          },          {            "name" : "pages",            "type" : "integer",            "value" : "542"          },          {            "name" : "price",            "type" : "float",            "value" : "18.50"          }        ]      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-08-30T08:02:58.452CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[264] : Received data ({  "subscriptionId" : "57c52162e08b32f1445139ef",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "type" : "Book",        "isPattern" : "false",        "id" : "Book1",        "attributes" : [          {            "name" : "title",            "type" : "text",            "value" : "Game of Thrones: Book 1"          },          {            "name" : "pages",            "type" : "integer",            "value" : "542"          },          {            "name" : "price",            "type" : "float",            "value" : "18.50"          }        ]      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-08-30T08:02:58.452CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[269] : Adding flume event header (name=fiware-service, value=testservice)
time=2016-08-30T08:02:58.452CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[269] : Adding flume event header (name=fiware-service, value=testservice)
time=2016-08-30T08:02:58.453CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[273] : Adding flume event header (name=fiware-servicepath, value=/catalog)
time=2016-08-30T08:02:58.453CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[276] : Adding flume event header (name=fiware-correlator, value=4b1be263-2502-4ca6-91fc-e43e8e642904)
time=2016-08-30T08:02:58.453CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[279] : Adding flume event header (name=transaction-id, value=4b1be263-2502-4ca6-91fc-e43e8e642904)
time=2016-08-30T08:02:58.454CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[286] : Event put in the channel, id=1696244802
time=2016-08-30T08:02:58.455CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[86] : Event intercepted, id=1696244802
time=2016-08-30T08:02:58.452CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[273] : Adding flume event header (name=fiware-servicepath, value=/catalog)
time=2016-08-30T08:02:58.458CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[276] : Adding flume event header (name=fiware-correlator, value=31ce961a-2767-4acd-bd5e-b623c3062148)
time=2016-08-30T08:02:58.458CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[279] : Adding flume event header (name=transaction-id, value=31ce961a-2767-4acd-bd5e-b623c3062148)
time=2016-08-30T08:02:58.458CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[286] : Event put in the channel, id=1132188697
time=2016-08-30T08:02:58.459CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[86] : Event intercepted, id=1132188697
time=2016-08-30T08:02:58.594CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[147] : Adding flume event header (name=notified-entities, value=Book1_Book)
time=2016-08-30T08:02:58.594CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[151] : Adding flume event header (name=grouped-entities, value=Book1_Book)
time=2016-08-30T08:02:58.594CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[155] : Adding flume event header (name=grouped-servicepaths, value=/catalog)
time=2016-08-30T08:02:58.594CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[158] : Event put in the channel, id=1132188697
time=2016-08-30T08:02:58.594CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[147] : Adding flume event header (name=notified-entities, value=Book1_Book)
time=2016-08-30T08:02:58.595CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[151] : Adding flume event header (name=grouped-entities, value=Book1_Book)
time=2016-08-30T08:02:58.595CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[155] : Adding flume event header (name=grouped-servicepaths, value=/catalog)
time=2016-08-30T08:02:58.595CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[158] : Event put in the channel, id=1696244802
time=2016-08-30T08:02:58.620CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[380] : Event got from the channel (id=1696244802, headers={grouped-entities=Book1_Book, transaction-id=4b1be263-2502-4ca6-91fc-e43e8e642904, grouped-servicepaths=/testPath, fiware-correlator=4b1be263-2502-4ca6-91fc-e43e8e642904, fiware-servicepath=/testPath, fiware-service=testservice, notified-entities=Book1_Book, timestamp=1472536978455}, bodyLength=672)
time=2016-08-30T08:02:58.628CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-08-30T08:02:58.629CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=persistBatch | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[132] : [kafka-sink] Processing sub-batch regarding the library_/catalog_Book1_Book destination
time=2016-08-30T08:02:58.640CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=topicExists | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[58] : Checking if topic 'library_catalog_book1_book' already exists.
time=2016-08-30T08:02:59.307CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[299] : [kafka-sink] Creating topic at OrionKafkaSink. Topic: library_catalog_book1_book , partitions: 1 , replication factor: 1
time=2016-08-30T08:03:00.381CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=info | comp=Cygnus | msg=kafka.utils.Logging$class[68] : Topic creation {"version":1,"partitions":{"0":[3]}}
time=2016-08-30T08:03:00.404CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=debug | comp=Cygnus | msg=kafka.utils.Logging$class[52] : Updated path /brokers/topics/library_catalog_book1_book with {"version":1,"partitions":{"0":[3]}} for replica assignment
time=2016-08-30T08:03:00.404CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=createTopic | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[67] : Creating topic: library_catalog_book1_book , partitions: 1 , replication factor: 1.
time=2016-08-30T08:03:00.404CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[305] : [kafka-sink] Persisting data at OrionKafkaSink. Topic (library_catalog_book1_book), Data ({"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472536978455}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"542"},{"name":"price","type":"float","value":"18.50"}],"type":"Book","isPattern":"false","id":"Book1"}})
time=2016-08-30T08:03:01.001CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=send | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[74] : Record: 'ProducerRecord(topic=library_catalog_book1_book, partition=null, key=null, value={"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472536978455}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"542"},{"name":"price","type":"float","value":"18.50"}],"type":"Book","isPattern":"false","id":"Book1"}}' sent to Kafka.
time=2016-08-30T08:03:01.001CEST | lvl=INFO | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[400] : Finishing internal transaction (4b1be263-2502-4ca6-91fc-e43e8e642904)
time=2016-08-30T08:03:01.002CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[380] : Event got from the channel (id=1132188697, headers={{grouped-entities=Book1_Book, transaction-id=31ce961a-2767-4acd-bd5e-b623c3062148, grouped-servicepaths=/testPath, fiware-correlator=31ce961a-2767-4acd-bd5e-b623c3062148, fiware-servicepath=/testPath, fiware-service=testservice, notified-entities=Book1_Book, timestamp=1472536978459}, bodyLength=672)
time=2016-08-30T08:03:01.004CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-08-30T08:03:01.004CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=persistBatch | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[132] : [kafka-sink] Processing sub-batch regarding the library_/catalog_Book1_Book destination
time=2016-08-30T08:03:01.009CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=topicExists | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[58] : Checking if topic 'library_catalog_book1_book' already exists.
time=2016-08-30T08:03:01.062CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[305] : [kafka-sink] Persisting data at OrionKafkaSink. Topic (library_catalog_book1_book), Data ({"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472536978459}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"542"},{"name":"price","type":"float","value":"18.50"}],"type":"Book","isPattern":"false","id":"Book1"}})
time=2016-08-30T08:03:01.062CEST | lvl=DEBUG | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=send | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[74] : Record: 'ProducerRecord(topic=library_catalog_book1_book, partition=null, key=null, value={"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472536978459}],"body":{"attributes":[{"name":"title","type":"text","value":"Game of Thrones: Book 1"},{"name":"pages","type":"integer","value":"542"},{"name":"price","type":"float","value":"18.50"}],"type":"Book","isPattern":"false","id":"Book1"}}' sent to Kafka.
time=2016-08-30T08:03:01.063CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[400] : Finishing internal transaction (31ce961a-2767-4acd-bd5e-b623c3062148)
time=2016-08-30T08:03:16.065CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[342] : Batch accumulation time reached, the batch will be processed as it is
time=2016-08-30T08:03:16.065CEST | lvl=INFO | corr=31ce961a-2767-4acd-bd5e-b623c3062148 | trans=31ce961a-2767-4acd-bd5e-b623c3062148 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[400] : Finishing internal transaction ()
```

[Top](#top)

## <a name="section7"></a>Updating entities

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
```
time=2016-08-30T08:15:20.113CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header host received with value localhost:5050
time=2016-08-30T08:15:20.114CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header user-agent received with value orion/0.28.0-next libcurl/7.47.0
time=2016-08-30T08:15:20.114CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-service received with value Library
time=2016-08-30T08:15:20.114CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-servicepath received with value /catalog
time=2016-08-30T08:15:20.115CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header accept received with value application/xml, application/json
time=2016-08-30T08:15:20.115CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header host received with value localhost:5050
time=2016-08-30T08:15:20.115CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-length received with value 706
time=2016-08-30T08:15:20.115CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header user-agent received with value orion/0.28.0-next libcurl/7.47.0
time=2016-08-30T08:15:20.116CEST | lvl=DEBUG | corr=4b1be263-2502-4ca6-91fc-e43e8e642904 | trans=4b1be263-2502-4ca6-91fc-e43e8e642904 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-type received with value application/json; charset=utf-8
time=2016-08-30T08:15:20.116CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-service received with value testservice
time=2016-08-30T08:15:20.116CEST | lvl=INFO | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[248] : Starting internal transaction (79e5aee5-dd0e-4aeb-90d7-532ae0df95b4)
time=2016-08-30T08:15:20.116CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header fiware-servicepath received with value /catalog
time=2016-08-30T08:15:20.117CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header accept received with value application/xml, application/json
time=2016-08-30T08:15:20.117CEST | lvl=INFO | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[264] : Received data ({  "subscriptionId" : "57c52162e08b32f1445139ef",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "type" : "Book",        "isPattern" : "false",        "id" : "Book1",        "attributes" : [          {            "name" : "title",            "type" : "text",            "value" : "Game of Thrones: Book 1"          },          {            "name" : "price",            "type" : "float",            "value" : "22.50"          },          {            "name" : "pages",            "type" : "integer",            "value" : "231"          }        ]      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-08-30T08:15:20.117CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-length received with value 706
time=2016-08-30T08:15:20.117CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[269] : Adding flume event header (name=fiware-service, value=library)
time=2016-08-30T08:15:20.118CEST | lvl=DEBUG | corr= | trans= | srv= | subsrv= | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[192] : Header content-type received with value application/json; charset=utf-8
time=2016-08-30T08:15:20.118CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[273] : Adding flume event header (name=fiware-servicepath, value=/catalog)
time=2016-08-30T08:15:20.118CEST | lvl=INFO | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[248] : Starting internal transaction (b7445350-084c-4ad0-bfbe-d2225a0a94ea)
time=2016-08-30T08:15:20.118CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[276] : Adding flume event header (name=fiware-correlator, value=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4)
time=2016-08-30T08:15:20.119CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[279] : Adding flume event header (name=transaction-id, value=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4)
time=2016-08-30T08:15:20.119CEST | lvl=INFO | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[264] : Received data ({  "subscriptionId" : "57c52135e08b32f1445139ee",  "originator" : "localhost",  "contextResponses" : [    {      "contextElement" : {        "type" : "Book",        "isPattern" : "false",        "id" : "Book1",        "attributes" : [          {            "name" : "title",            "type" : "text",            "value" : "Game of Thrones: Book 1"          },          {            "name" : "price",            "type" : "float",            "value" : "22.50"          },          {            "name" : "pages",            "type" : "integer",            "value" : "231"          }        ]      },      "statusCode" : {        "code" : "200",        "reasonPhrase" : "OK"      }    }  ]})
time=2016-08-30T08:15:20.119CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[286] : Event put in the channel, id=314577326
time=2016-08-30T08:15:20.119CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[269] : Adding flume event header (name=fiware-service, value=library)
time=2016-08-30T08:15:20.122CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[273] : Adding flume event header (name=fiware-servicepath, value=/catalog)
time=2016-08-30T08:15:20.122CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[276] : Adding flume event header (name=fiware-correlator, value=b7445350-084c-4ad0-bfbe-d2225a0a94ea)
time=2016-08-30T08:15:20.122CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[279] : Adding flume event header (name=transaction-id, value=b7445350-084c-4ad0-bfbe-d2225a0a94ea)
time=2016-08-30T08:15:20.123CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=getEvents | comp=Cygnus | msg=com.telefonica.iot.cygnus.handlers.NGSIRestHandler[286] : Event put in the channel, id=841738740
time=2016-08-30T08:15:20.123CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[86] : Event intercepted, id=841738740
time=2016-08-30T08:15:20.120CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[86] : Event intercepted, id=314577326
time=2016-08-30T08:15:20.125CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[147] : Adding flume event header (name=notified-entities, value=Book1_Book)
time=2016-08-30T08:15:20.126CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[151] : Adding flume event header (name=grouped-entities, value=Book1_Book)
time=2016-08-30T08:15:20.126CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[155] : Adding flume event header (name=grouped-servicepaths, value=/catalog)
time=2016-08-30T08:15:20.126CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[158] : Event put in the channel, id=841738740
time=2016-08-30T08:15:20.129CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[147] : Adding flume event header (name=notified-entities, value=Book1_Book)
time=2016-08-30T08:15:20.130CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[151] : Adding flume event header (name=grouped-entities, value=Book1_Book)
time=2016-08-30T08:15:20.130CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[155] : Adding flume event header (name=grouped-servicepaths, value=/catalog)
time=2016-08-30T08:15:20.130CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=intercept | comp=Cygnus | msg=com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor[158] : Event put in the channel, id=314577326
time=2016-08-30T08:15:20.183CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[380] : Event got from the channel (id=841738740, headers={grouped-entities=Book1_Book, transaction-id=b7445350-084c-4ad0-bfbe-d2225a0a94ea, grouped-servicepaths=/catalog, fiware-correlator=b7445350-084c-4ad0-bfbe-d2225a0a94ea, fiware-servicepath=/catalog, fiware-service=library, notified-entities=Book1_Book, timestamp=1472537720123}, bodyLength=672)
time=2016-08-30T08:15:20.187CEST | lvl=INFO | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-08-30T08:15:20.187CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=persistBatch | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[132] : [kafka-sink] Processing sub-batch regarding the library_/catalog_Book1_Book destination
time=2016-08-30T08:15:20.191CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=topicExists | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[58] : Checking if topic 'library_catalog_book1_book' already exists.
time=2016-08-30T08:15:20.211CEST | lvl=INFO | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[305] : [kafka-sink] Persisting data at OrionKafkaSink. Topic (library_catalog_book1_book), Data ({"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472537720123}],"body":{"attributes":[{"name":"title","type":"text","value":"Seat Panda"},{"name":"price","type":"float","value":"145.50"},{"name":"pages","type":"integer","value":"5"}],"type":"Book","isPattern":"false","id":"Book1"}})
time=2016-08-30T08:15:20.211CEST | lvl=DEBUG | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=send | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[74] : Record: 'ProducerRecord(topic=library_catalog_book1_book, partition=null, key=null, value={"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472537720123}],"body":{"attributes":[{"name":"title","type":"text","value":"Seat Panda"},{"name":"price","type":"float","value":"145.50"},{"name":"pages","type":"integer","value":"5"}],"type":"Book","isPattern":"false","id":"Book1"}}' sent to Kafka.
time=2016-08-30T08:15:20.211CEST | lvl=INFO | corr=b7445350-084c-4ad0-bfbe-d2225a0a94ea | trans=b7445350-084c-4ad0-bfbe-d2225a0a94ea | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[400] : Finishing internal transaction (b7445350-084c-4ad0-bfbe-d2225a0a94ea)
time=2016-08-30T08:15:20.211CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[380] : Event got from the channel (id=314577326, headers={grouped-entities=Book1_Book, transaction-id=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4, grouped-servicepaths=/catalog, fiware-correlator=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4, fiware-servicepath=/catalog, fiware-service=library, notified-entities=Book1_Book, timestamp=1472537720119}, bodyLength=672)
time=2016-08-30T08:15:20.218CEST | lvl=INFO | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[396] : Batch completed, persisting it
time=2016-08-30T08:15:20.220CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=persistBatch | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[132] : [kafka-sink] Processing sub-batch regarding the library_/catalog_Book1_Book destination
time=2016-08-30T08:15:20.225CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=topicExists | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[58] : Checking if topic 'library_catalog_book1_book' already exists.
time=2016-08-30T08:15:20.233CEST | lvl=INFO | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=persistAggregation | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSIKafkaSink[305] : [kafka-sink] Persisting data at OrionKafkaSink. Topic (library_catalog_book1_book), Data ({"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472537720119}],"body":{"attributes":[{"name":"title","type":"text","value":"Seat Panda"},{"name":"price","type":"float","value":"145.50"},{"name":"pages","type":"integer","value":"5"}],"type":"Book","isPattern":"false","id":"Book1"}})
time=2016-08-30T08:15:20.233CEST | lvl=DEBUG | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=send | comp=Cygnus | msg=com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImpl[74] : Record: 'ProducerRecord(topic=library_catalog_book1_book, partition=null, key=null, value={"headers":[{"fiware-service":"library"},{"fiware-servicePath":"/catalog"},{"timestamp":1472537720119}],"body":{"attributes":[{"name":"title","type":"text","value":"Seat Panda"},{"name":"price","type":"float","value":"145.50"},{"name":"pages","type":"integer","value":"5"}],"type":"Book","isPattern":"false","id":"Book1"}}' sent to Kafka.
time=2016-08-30T08:15:20.234CEST | lvl=INFO | corr=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | trans=79e5aee5-dd0e-4aeb-90d7-532ae0df95b4 | srv=library | subsrv=/catalog | function=processNewBatches | comp=Cygnus | msg=com.telefonica.iot.cygnus.sinks.NGSISink[400] : Finishing internal transaction (79e5aee5-dd0e-4aeb-90d7-532ae0df95b4)
```

[Top](#top)

[kafka]: https://github.com/pcoello25/fiware-cygnus/blob/master/doc/cygnus-ngsi/integration/img/kafka_zookeeper.png "Architecture for sinfonier"
