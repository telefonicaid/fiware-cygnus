# <a name="top"></a>Testing
Content:

* [Unit testing](#section1)
* [e2e testing](#section2)

## <a name="section1"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus/cygnus-ngsi
    $ mvn test

You should get an output similar to the following one:

```
$ mvn test
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building cygnus-ngsi 0.13.0_SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.5:resources (default-resources) @ cygnus-ngsi ---
[debug] execute contextualize
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO]
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ cygnus-ngsi ---
[INFO] Compiling 12 source files to /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-ngsi/target/classes
[INFO]
[INFO] --- maven-resources-plugin:2.5:testResources (default-testResources) @ cygnus-ngsi ---
[debug] execute contextualize
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-ngsi/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ cygnus-ngsi ---
[INFO] Compiling 16 source files to /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-ngsi/target/test-classes
[INFO]
[INFO] --- maven-surefire-plugin:2.10:test (default-test) @ cygnus-ngsi ---
[INFO] Surefire report directory: /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-ngsi/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.telefonica.iot.cygnus.containers.NotifyContextRequestTest
getSubscriptionId (notify-json-simple)
getSubscriptionId (notify-json-compound)
getSubscriptionId (notify-json-metadata)
getOriginator (notify-json-simple)
getOriginator (notify-json-compound)
getOriginator (notify-json-metadata)
getOriginator (notify-json-simple)
getOriginator (notify-json-compound)
getOriginator (notify-json-compound-nested)
getOriginator (notify-json-metadata)
getOriginator (notify-json-simple-unordered)
getOriginator (notify-json-simple-null-attrs)
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.343 sec
Running com.telefonica.iot.cygnus.handlers.NGSIRestHandlerTest
[OrionRestHandler.generateUniqueId] ------------- When a correlation ID is genereated, both generated correlation ID and generated transaction ID have the same value
[OrionRestHandler.generateUniqueId] ------  OK  - The generated transaction ID '1234567890-123-1234567890' is equals to the generated correlator ID '1234567890-123-1234567890'
[OrionRestHandler.configure] -------------------- The configured default service path must start with '/'
[OrionRestHandler.configure] -------------  OK  - The configured default service path '/something' starts with '/'
[OrionRestHandler.getEvents] -------------------- When a notification is sent as a Http message, a single Flume event is generated
[OrionRestHandler.getEvents] -------------  OK  - A single event has been generated
[OrionRestHandler.getEvents] -------------------- When a the configuration is wrong, no evetns are obtained
[OrionRestHandler.getEvents] -------------  OK  - No events are processed since the configuration is wrong
[OrionRestHandler.generateUniqueId] ------------- An internal transaction ID is generated
[OrionRestHandler.generateUniqueId] ------  OK  - An internal transaction ID 'dd9ea2f8-91dc-45f6-be17-9880136432fb' has been generated
[OrionRestHandler.configure] -------------------- The configured notification target must start with '/'
[OrionRestHandler.configure] -------------  OK  - The configured notification target '/notify' starts with '/'
[OrionRestHandler.configure] -------------------- When not configured, the default values are used for non mandatory parameters
[OrionRestHandler.configure] -------------  OK  - The default configuration value for 'notification_target' is '/notify'
[OrionRestHandler.configure] -------------  OK  - The default configuration value for 'default_service' is 'default'
[OrionRestHandler.configure] -------------  OK  - The default configuration value for 'default_service_path' is '/'
[OrionRestHandler.getEvents] -------------------- When a Flume event is generated, it contains fiware-service, fiware-servicepath, fiware-correlator and transaction-id headers
[OrionRestHandler.getEvents] -------------  OK  - The generated Flume event contains 'fiware-service'
[OrionRestHandler.getEvents] -------------  OK  - The generated Flume event contains 'fiware-servicepath'
[OrionRestHandler.getEvents] -------------  OK  - The generated Flume event contains 'fiware-correlator'
[OrionRestHandler.getEvents] -------------  OK  - The generated Flume event contains 'transaction-id'
[OrionRestHandler.generateUniqueId] ------------- When a correlation ID is not notified, it is generated
[OrionRestHandler.generateUniqueId] ------  OK  - The transaction ID has been generated
[OrionRestHandler.getEvents] -------------------- When a notification is sent, the headers are valid
[OrionRestHandler.getEvents] -------------  OK  - The value for 'Content-Type' header is 'application/json'
[OrionRestHandler.getEvents] -------------  OK  - The value for 'fiware-servicePath' header starts with '/'
[OrionRestHandler.getEvents] -------------  OK  - The length of 'fiware-service' header value is  less or equal than '50'
[OrionRestHandler.getEvents] -------------  OK  - The length of 'fiware-servicePath' header value is less or equal than '50'
[OrionRestHandler.getEvents] -------------------- When a Flume event is generated, it contains the payload of the Http notification as body
[OrionRestHandler.getEvents] -------------  OK  - The event body '{"contextResponses":[{"statusCode":{"reasonPhrase":"OK","code":"200"},"contextElement":{"id":"room1","attributes":[{"name":"temperature","value":"26.5","type":"centigrade"}],"type":"Room","isPattern":"false"}}],"originator":"localhost","subscriptionId":"51c0ac9ed714fb3b37d7d5a8"}' is equal to the notification Json '{"contextResponses":[{"statusCode":{"reasonPhrase":"OK","code":"200"},"contextElement":{"id":"room1","attributes":[{"name":"temperature","value":"26.5","type":"centigrade"}],"type":"Room","isPattern":"false"}}],"originator":"localhost","subscriptionId":"51c0ac9ed714fb3b37d7d5a8"}'
[OrionRestHandler.generateUniqueId] ------------- When a correlator ID is notified, it is reused
[OrionRestHandler.generateUniqueId] ------  OK  - The notified transaction ID '1234567890-123-1234567890' has been reused
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.43 sec
Running com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptorTest
[GroupingInterceptor.intercept] ----------------- When a Flume event is put in the channel, it contains fiware-service, fiware-servicepath, fiware-correlator, transaction-id, notified-entities, grouped-servicepaths and grouped-entities headers
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'fiware-service'
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'fiware-servicepath'
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'fiware-correlator'
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'transaction-id'
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'notified-entities'
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'grouped-servicepaths'
[GroupingInterceptor.intercept] ----------  OK  - The generated Flume event contains 'grouped-entities'
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.061 sec
Running com.telefonica.iot.cygnus.sinks.NGSICartoDBSinkTest
[NGSICartoDBSink.configure] --------------------- Configured `flip_coordinates` cannot be different than `true` or `false`
[NGSICartoDBSink.configure] --------------  OK  - 'flip_coordinates=falso' was detected
[CartoDBAggregator.initialize] ------------------ When initializing through an initial geolocated event, a table name is created
[CartoDBAggregator.initialize] -----------  OK  - A table name has been created
[NGSICartoDBSink.configure] --------------------- Configured 'api_key' cannot be null
[NGSICartoDBSink.configure] --------------  OK  - null value detected for 'api_key'
[NGSICartoDBSink.buildTableName] ---------------- When a root service-path is notified/defaulted and data_model is 'dm-by-service-path' the CartoDB table name cannot be created
[NGSICartoDBSink.buildTableName] ---------  OK  - It was detected the table name could not be created
[CartoDBAggregator.aggregate] ------------------- When aggregating a single geolocated event, the aggregation values string starts with '(' and finishes with ')'
[CartoDBAggregator.aggregate] ------------  OK  - '('2016-04-20T07:19:55.801Z','somePath','someId','someType',ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326),'someValue2','[]')' starts with '('
[CartoDBAggregator.aggregate] ------------  OK  - '('2016-04-20T07:19:55.801Z','somePath','someId','someType',ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326),'someValue2','[]')' ends with ')'
[CartoDBAggregator.initialize] ------------------ When initializing through an initial geolocated event, the aggregation fields string is lower case, starts with '(' and finishes with ')'
[CartoDBAggregator.initialize] -----------  OK  - '(recvtime,fiwareservicepath,entityid,entitytype,the_geom,somename2,somename2_md)' is lower case
[CartoDBAggregator.initialize] -----------  OK  - '(recvtime,fiwareservicepath,entityid,entitytype,the_geom,somename2,somename2_md)' starts with '('
[CartoDBAggregator.initialize] -----------  OK  - '(recvtime,fiwareservicepath,entityid,entitytype,the_geom,somename2,somename2_md)' ends with ')'
[NGSICartoDBSink.start] ------------------------- When started, a CartoDB backend is created
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/Users/frb/.m2/repository/org/slf4j/slf4j-simple/1.7.21/slf4j-simple-1.7.21.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/Users/frb/.m2/repository/com/telefonica/iot/cygnus-common/0.13.0_SNAPSHOT/cygnus-common-0.13.0_SNAPSHOT.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/Users/frb/.m2/repository/org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.SimpleLoggerFactory]
[NGSICartoDBSink.start] ------------------  OK  - A CartoDB backend has been created
[NGSICartoDBSink.buildTableName] ---------------- When a non root service-path is notified/defaulted and data_model is 'dm-by-service-path' the CartoDB table name is the lower case of <servicePath>
[NGSICartoDBSink.buildTableName] ---------  OK  - 'somepath' is equals to the lower case of <servicePath>
[NGSICartoDBSink.configure] --------------------- Independently of the configured value, enable_lowercase is always 'true' by default
[NGSICartoDBSink.configure] --------------  OK  - 'enable_lowercase=false' was confiured, nevertheless it is always true by default
[NGSICartoDBSink.buildTableName] ---------------- When a root service-path is notified/defaulted and data_model is 'dm-by-attribute' the CartoDB table name is the lower case of <servicePath>_<entityId>_<entityYype>_<attrName>_<attrType>
[NGSICartoDBSink.buildTableName] ---------  OK  - 'someid_sometype_somename1_sometype1' is equals to the lower case of <entityId>_<entityType>_<attrName>_<attrType>
[CartoDBAggregator.aggregate] ------------------- When aggregating a single geolocated event, the aggregation values string contains a value and a metadata value for each attribute in the event except for the geolocation attribute, which is added as a specific value (a point)
[CartoDBAggregator.initialize] -----------  OK  - '-3.7167, 40.3833' and '{"name":"location","type":"string","value":"WGS84"}' are not in the rows '('2016-04-20T07:19:55.801Z','somePath','someId','someType',ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326),'someValue2','[]')'
[CartoDBAggregator.initialize] -----------  OK  - 'someValue2' and '[]' are in the rows '('2016-04-20T07:19:55.801Z','somePath','someId','someType',ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326),'someValue2','[]')'
[CartoDBAggregator.initialize] -----------  OK  - 'ST_SetSRID(ST_MakePoint(-3.7167, 40.3833), 4326)' is in the rows '('2016-04-20T07:19:55.801Z','somePath','someId','someType',ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326),'someValue2','[]')'
[CartoDBAggregator.initialize] ------------------ When initializing through an initial geolocated event, the aggregation fields string contains a field and a metadata field for each attribute in the event except for the geolocation attribute, which is added as a specific field ('the_geom')
[CartoDBAggregator.initialize] -----------  OK  - 'somename1' and 'somename1_md' are not in the fields '(recvtime,fiwareservicepath,entityid,entitytype,the_geom,somename2,somename2_md)'
[CartoDBAggregator.initialize] -----------  OK  - 'somename2' and 'somename2_md' are in the fields '(recvtime,fiwareservicepath,entityid,entitytype,the_geom,somename2,somename2_md)'
[CartoDBAggregator.initialize] -----------  OK  - 'the_geom' is in the fields '(recvtime,fiwareservicepath,entityid,entitytype,the_geom,somename2,somename2_md)'
[CartoDBAggregator.aggregate] ------------------- When aggregating a single geolocated event, if flip_coordinates=true then the_geom field contains a point with exchanged latitude and longitude.
[CartoDBAggregator.aggregate] ------------  OK  - '('2016-04-20T07:19:55.801Z','somePath','someId','someType',ST_SetSRID(ST_MakePoint(40.3833,-3.7167), 4326),'someValue2','[]')' contains the coordinates '-3.7167, 40.3833' flipped
[NGSICartoDBSink.buildTableName] ---------------- When a non service-path is notified/defaulted and data_model is 'dm-by-entity' the CartoDB table name is the lower case of <servicePath>_<entityId>_<entityType>
[NGSICartoDBSink.buildTableName] ---------  OK  - 'someid_sometype' is equals to the lower case of <entityId>_<entityType>
[NGSICartoDBSink.buildTableName] ---------------- When a non root service-path is notified/defaulted and data_model is 'dm-by-attribute' the CartoDB table name is the lower case of <servicePath>_<entityId>_<entityYype>_<attrName>_<attrType>
[NGSICartoDBSink.buildTableName] ---------  OK  - 'somepath_someid_sometype_somename1_sometype1' is equals to the lower case of <servicePath>_<entityId>_<entityType>_<attrName>_<attrType>
[NGSICartoDBSink.buildTableName] ---------------- When a non root service-path is notified/defaulted and data_model is 'dm-by-entity' the CartoDB table name is the lower case of <servicePath>_<entityId>_<entityType>
[NGSICartoDBSink.buildTableName] ---------  OK  - 'somepath_someid_sometype' is equals to the lower case of <servicePath>_<entityId>_<entityType>
[NGSICartoDBSink.configure] --------------------- Configured 'endpoint' cannot be null
[NGSICartoDBSink.configure] --------------  OK  - null value detected for 'endpoint'
[NGSICartoDBSink.configure] --------------------- Configured 'endpoint' must be a URI using the 'http' or 'https' schema
[NGSICartoDBSink.configure] --------------  OK  - Invalid or inexistent schema detected for 'endpoint'
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.25 sec
Running com.telefonica.iot.cygnus.sinks.NGSICKANSinkTest
Testing OrionCKANSink.start
Testing OrionCKANSink.configure
Testing OrionCKANSinkTest.persistBatch (row persistence, enable grouping)
Testing OrionCKANSinkTest.persistBatch (row persistence, disable grouping)
Testing OrionCKANSinkTest.persistBatch (column attr persistence, enable grouping)
Testing OrionCKANSinkTest.persistBatch (column attr persistence, disable grouping)
Testing OrionCKANSink.persistBatch ("root" servicePath name)
Testing OrionCKANSink.persistBatch (multiple destinations and fiware-servicePaths)
Testing OrionCKANSinkTest.persistBatch (null batches)
Testing OrionCKANSink.persisBatch (normal resource lengths)
Testing OrionCKANSink.persistBatch (too long service name)
Testing OrionCKANSink.persistBatch (too long servicePath name)
Testing OrionCKANSink.persistBatch (too long destination name)
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.171 sec
Running com.telefonica.iot.cygnus.sinks.NGSIDynamoDBSinkTest
Testing OrionDynamoDBSink.start
Testing OrionDynamoDBSink.configure
Testing OrionDynamoDBSink.persistBatch ("root" servicePath name)
Testing OrionDynamoDBSink.persistBatch (multiple destinations and fiware-servicePaths)
Testing OrionDynamoDBSink.persist (null batches)
Testing OrionDynamoDBSink.persistBatch (normal resource lengths)
Testing OrionDynamoDBSink.persistBatch (too long service name)
Testing OrionDynamoDBSink.persistBatch (too long servicePath name)
Testing OrionDynamoDBSink.persistBatch (too long destination name)
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.596 sec
Running com.telefonica.iot.cygnus.sinks.NGSIHDFSSinkTest
Testing OrionHDFSSink.start
Testing OrionHDFSSinkTest.configure
Testing OrionHDFSSink.persistBatch (json-row file format)
Testing OrionHDFSSink.persistBatch (json-column file format)
Testing OrionHDFSSink.persistBatch (csv-row file format)
Testing OrionHDFSSink.persistBatch (csv-column file format)
Testing OrionHDFSSink.persistBatch ("root" servicePath name)
Testing OrionHDFSSink.persistBatch (multiple destinations and fiware-servicePaths)
Testing OrionHDFSSink.persistBatch (null batches)
Testing OrionHDFSSink.configure (deprecated parameters are used)
Testing OrionHDFSSink.persistBatch (normal resource lengths)
Testing OrionHDFSSink.persistBatch (too long service name)
Testing OrionHDFSSink.persistBatch (too long servicePath name)
Testing OrionHDFSSink.persistBatch (too long destination name)
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.863 sec
Running com.telefonica.iot.cygnus.sinks.NGSIKafkaSinkTest
[OrionKafkaSink.buildTopicName] ----------------- When the root service-path is notified/defaulted and data_model=dm-by-attribute, the Kafka topic name is <service>_<entityId>_<entityType>_<attrName>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service_entityId_entityType_attributeName
[OrionKafkaSink.buildTopicName] ----------------- When the root service-path is notified/defaulted and data_model=dm-by-entity, the Kafka topic name is <service>_<entityId>_<entityType>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service_entityId_entityType
[OrionKafkaSink.buildTopicName] ----------------- When a non root service-path is notified/defaulted and data_model=dm-by-service, the Kafka topic name is <service>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equal to service
[OrionKafkaSink.buildTopicName] ----------------- When the root service-path is notified/defaulted and data_model=dm-by-service-path, the Kafka topic name is <service>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service
[OrionKafkaSink.buildTopicName] ----------------- When a non root service-path is notified/defaulted and data_model=dm-by-entity, the Kafka topic name is <service>_<service-path>_<entityId>_<entityType>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service_servicePath_entityId_entityType
[OrionKafkaSink.buildTopicName] ----------------- When a non root service-path is notified/defaulted and data_model=dm-by-service-path, the Kafka topic name is <service>_<service-path>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service_servicePath
[OrionKafkaSink.buildTopicName] ----------------- When a non root service-path is notified/defaulted and data_model=dm-by-attribute, the Kafka topic name is <service>_<service-path>_<entityId>_<entityType>_<attrName>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service_servicePath_entityId_entityType_attributeName
[OrionKafkaSink.buildTopicName] ----------------- When the root service-path is notified/defaulted and data_model=dm-by-service, the Kafka topic name is <service>
[OrionKafkaSink.buildTopicName] ----------  OK  - Created topic is equals to service
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 sec
Running com.telefonica.iot.cygnus.sinks.NGSIMongoBaseSinkTest
[NGSIMongoBaseSink.buildCollectionName] --------- When / service-path is notified/defaulted and data_model=dm-by-entity, the MongoDBcollections name is <prefix>/_<entityId>_<entityType>
[NGSIMongoBaseSink.buildCollectionName] --  OK  - 'sth_/_someId_someType' was crated as collection name
[NGSIMongoBaseSink.buildCollectionName] --------- When / service-path is notified/defaulted and data_model=dm-by-attribute, the MongoDB collections name is <prefix>/_<entityId>_<entityType>_<attrName>_<attrType>
[NGSIMongoBaseSink.buildCollectionName] --  OK  - 'sth_/_someId_someType_someName_someType' was crated as collection name
[NGSIMongoBaseSink.buildCollectionName] --------- When / service-path is notified/defaulted and data_model=dm-by-service-path, the MongoDB collection name is <prefix>/
[NGSIMongoBaseSink.buildCollectionName] --  OK  - 'sth_/' was crated as collection name
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 sec
Running com.telefonica.iot.cygnus.sinks.NGSIMongoSinkTest
[OrionMongoSink.configure] ---------------------- Configured 'db_prefix' is encoded when having forbiden characters
[OrionMongoSink.configure] ---------------  OK  - 'db_prefix=this\is/a$prefix.with forbiden"chars:-,' correctly encoded as 'this_is_a_prefix_with_forbiden_chars:-,'
[OrionMongoSink.configure] ---------------------- Configured 'collection_prefix' cannot be 'system.'
[OrionMongoSink.configure] ---------------  OK  - 'system.' value detected for 'collection_prefix'
[OrionMongoSink.configure] ---------------------- Configured 'collection_prefix' is encoded when having forbiden characters
[OrionMongoSink.configure] ---------------  OK  - 'collection_prefix=this\is/a$prefix.with-forbiden,chars:-.' correctly encoded as 'this\is/a_prefix.with-forbiden,chars:-.'
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 sec
Running com.telefonica.iot.cygnus.sinks.NGSIMySQLSinkTest
Testing OrionMySQLSink.start
Testing OrionMySQLSink.configure
Testing OrionMySQLSinkTest.persistBatch ("root" servicePath name)
Testing OrionMySQLSinkTest.persistBatch (multiple destinations and fiware-servicePaths)
Testing OrionHDFSSinkTest.persist (null batches)
Testing OrionMySQLSinkTest.persistBatch (normal resource lengths)
Testing OrionMySQLSinkTest.persistBatch (too long service name)
Testing OrionMySQLSinkTest.persistBatch (too long servicePath name)
Testing OrionMySQLSinkTest.persistBatch (too long destination name)
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.171 sec
Running com.telefonica.iot.cygnus.sinks.NGSIPostgreSQLSinkTest
Testing OrionPostgreSQLSink.start
Testing OrionPostgreSQLSink.configure
Testing OrionPostgreSQLSinkTest.persistBatch ("root" servicePath name)
Testing OrionPostgreSQLSinkTest.persistBatch (multiple destinations and fiware-servicePaths)
Testing OrionHDFSSinkTest.persist (null batches)
Testing OrionPostgreSQLSinkTest.persistBatch (normal resource lengths)
Testing OrionPostgreSQLSinkTest.persistBatch (too long service name)
Testing OrionPostgreSQLSinkTest.persistBatch (too long servicePath name)
Testing OrionPostgreSQLSinkTest.persistBatch (too long destination name)
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.099 sec
Running com.telefonica.iot.cygnus.sinks.NGSISinkTest
[OrionSink.start] ------------------------------- The sink starts properly
[OrionSink.start] ------------------------  OK  - The sink started properly, the lifecycle state is 'START'
[OrionSink.configure] --------------------------- The configuration becomes invalid upon out-of-the-limits configured values for parameters having a discrete set of accepted values, or numerical values having upper or lower limits
[OrionSink.configure] --------------------  OK  - A wrong configuration 'batch_size='0' has been detected
[OrionSink.configure] --------------------  OK  - A wrong configuration 'batch_timeout='0' has been detected
[OrionSink.configure] --------------------  OK  - A wrong configuration 'batch_ttl='-2' has been detected
[OrionSink.configure] --------------------  OK  - A wrong configuration 'data_model='dm-by-other' has been detected
[OrionSink.configure] --------------------  OK  - A wrong configuration 'enable_grouping='falso' has been detected
[OrionSink.configure] --------------------  OK  - A wrong configuration 'enable_lowercase='verdadero' has been detected
[OrionSink.configure] --------------------------- When not configured, the default values are used for non mandatory parameters
[OrionSink.configure] --------------------  OK  - The default configuration value for 'batch_size' is '1'
[OrionSink.configure] --------------------  OK  - The default configuration value for 'batch_timeout' is '30'
[OrionSink.configure] --------------------  OK  - The default configuration value for 'batch_ttl' is '10'
[OrionSink.configure] --------------------  OK  - The default configuration value for 'data_model' is 'dm-by-entity'
[OrionSink.configure] --------------------  OK  - The default configuration value for 'enable_grouping' is 'false'
[OrionSink.configure] --------------------  OK  - The default configuration value for 'enable_lowercase' is 'false'
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 sec
Running com.telefonica.iot.cygnus.sinks.NGSISTHSinkTest
[OrionSTHSink.configure] ------------------------ Configured 'db_prefix' is encoded when having forbiden characters
[OrionSTHSink.configure] -----------------  OK  - 'db_prefix=this\is/a$prefix.with forbiden"chars:-.' correctly encoded as 'this_is_a_prefix_with_forbiden_chars:-_'
[OrionSTHSink.configure) ------------------------ Configured 'collection_prefix' cannot be 'system.'
[OrionSTHSink.configure] -----------------  OK  - 'system.' value detected for 'collection_prefix'
[OrionSTHSink.configure] ------------------------ Configured 'collection_prefix' is encoded when having forbiden characters
[OrionSTHSink.configure] -----------------  OK  - 'collection_prefix=this\is/a$prefix.with-forbiden,chars:-.' correctly encoded as 'this\is/a_prefix.with-forbiden,chars:-.'
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec
Running com.telefonica.iot.cygnus.utils.NGSIUtilsTest
[Utils.getLocation] ----------------------------- When getting a location, a CartoDB point is obtained when passing an attribute of type 'geo:point'
[Utils.getLocation] ----------------------  OK  - Location 'ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326)' obtained for an attribute with metadata '[{"name":"location","value":"WGS84","type":"string"}]' and value '-3.7167, 40.3833'
[Utils.getLocation] ----------------------------- When getting a location, the original attribute is returned when the attribute type is not geo:point and there is no WGS84 location metadata
[Utils.getLocation] ----------------------  OK  - Location '-3.7167, 40.3833' obtained for a not geolocated attribute
[Utils.getLocation] ----------------------------- When getting a location, a CartoDB point is obtained when passing an attribute of type 'geo:point'
[Utils.getLocation] ----------------------  OK  - Location 'ST_SetSRID(ST_MakePoint(-3.7167,40.3833), 4326)' obtained for an attribute of type 'geo:point' and value '-3.7167, 40.3833'
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec

Results :

Tests run: 85, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 16.015s
[INFO] Finished at: Tue May 03 17:48:48 CEST 2016
[INFO] Final Memory: 31M/81M
[INFO] ------------------------------------------------------------------------
```

[Top](#top)

## <a name="section2"></a>e2e testing
Cygnus can be tested form a e2e point of view by using any of the scripts, [given with this repo](../../../cygnus-ngsi/resources/ngsi-examples), emulating a NGSI-like notification. You can find both Json and XML examples of simple and compound notifications, with or without metadata, even model entities and loops of continuous notifiers.

For instance, if running the `notification-json-simple.sh`:

```
$ ./notification-json-simple.sh http://localhost:5050/notify myservice myservicepath
*   Trying ::1...
* Connected to localhost (::1) port 5050 (#0)
> POST /notify HTTP/1.1
> Host: localhost:5050
> Content-Type: application/json
> Accept: application/json
> User-Agent: orion/0.10.0
> Fiware-Service: myservice
> Fiware-ServicePath: myservicepath
> Content-Length: 460
>
* upload completely sent off: 460 out of 460 bytes
< HTTP/1.1 200 OK
< Transfer-Encoding: chunked
< Server: Jetty(6.1.26)
<
* Connection #0 to host localhost left intact
```

You will see the server (Cygnus) is sending back a `200 OK` response.

Of course, this is just a e2e test. For real e2e integration with a real NGSI-like source, such as [Orion Context Broker](https://github.com/telefonicaid/fiware-orion), please refer to the [User and Programmer Guide](../user_and_programmer_guide/connecting_orion.md).

Support for NGSIv2 notifications has been added from version above 1.17.1. For this purpose, it's been added a new dir [/NGSIv2](./resources/ngsi-examples/NGSIv2) which contains script files in order to emulate some NGSIv2 notification types. 

[Top](#top)
