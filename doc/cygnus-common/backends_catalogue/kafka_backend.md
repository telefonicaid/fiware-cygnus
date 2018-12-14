# Kafka backend
## `KafkaBackend` interface
This class enumerates the methods any [Kafka](http://kafka.apache.org/) backend implementation must expose. In this case, the following ones:

    boolean topicExists(String topic) throws Exception;

> Gets if the given topic exists.

    void createTopic(String topic, int partitions, int replicationFactor);

> Creates a topic given its name, number of partitions and replication factor.

    void send(ProducerRecord<String, String> record);

> Sends a record to Kafka. A record is composed by a topic name and the data to be send.

## `KafkaBackendImpl` class
This is a convenience backend class for CKAN that implements the `KafkaBackend` interface described above.

`KafkaBackendImpl` really wraps the [`KafkaProducer`](http://kafka.apache.org/082/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html) and `AdminUtils` Java classes.

Nothing special is done with regards to the encoding. Since Cygnus generally works with UTF-8 character set, this is how the data is written into the topics. It will responsability of the Kafka consumer to convert the bytes read into UTF-8.
