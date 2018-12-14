# <a name="top"></a>Testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus/cygnus-common
    $ mvn test
    
You should get an output similat to the following one:

```
[INFO] ------------------------------------------------------------------------
[INFO] Building cygnus-common 0.13.0_SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.5:resources (default-resources) @ cygnus-common ---
[debug] execute contextualize
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ cygnus-common ---
[INFO] Compiling 48 source files to /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-common/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.5:testResources (default-testResources) @ cygnus-common ---
[debug] execute contextualize
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-common/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ cygnus-common ---
[INFO] Compiling 13 source files to /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-common/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.10:test (default-test) @ cygnus-common ---
[INFO] Surefire report directory: /Users/frb/devel/fiware/fiware-cygnus-main/cygnus-common/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.telefonica.iot.cygnus.backends.ckan.CKANBackendImplTest
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANBackendImpl.persist
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.395 sec
Running com.telefonica.iot.cygnus.backends.ckan.CKANCacheTest
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.getOrgId
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.getPkgId
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.getResId
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.isCachedOrg
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.isCachedPkg
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.isCachedRes
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.setOrgId
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.setPkgId
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:32 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing CKANCache.setResId
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.098 sec
Running com.telefonica.iot.cygnus.backends.dynamo.DynamoDBBackendImplTest
16/05/03 17:44:37 WARN http.AmazonHttpClient: Detected a possible problem with the current JVM version (1.6.0_65).  If you experience XML parsing problems using the SDK, try upgrading to a more recent JVM update.
Testing MySQLBackend.createTable (within first database
16/05/03 17:44:38 ERROR dynamo.DynamoDBBackendImpl: Error while creating the DynamoDB table table-name. Details=null
Testing DynamoDBBackendImpl.createTable (within first database
16/05/03 17:44:38 ERROR dynamo.DynamoDBBackendImpl: Error while creating the DynamoDB table table-name. Details=null
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.092 sec
Running com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplBinaryTest
Testing HDFSBackendImpl.createFile
2016-05-03 17:44:38.917 java[75850:5880382] Unable to load realm info from SCDynamicStore
Testing HDFSBackendImpl.append
Testing HDFSBackendImplBinary.createDir
Testing HDFSBackendImpl.exists
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.542 sec
Running com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplRESTTest
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing HDFSBackendImplREST.createFile
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing HDFSBackendImplREST.append
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing HDFSBackendImplREST.createDir
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting max total connections (500)
16/05/03 17:44:39 INFO http.HttpClientFactory: Setting default max connections per route (100)
Testing HDFSBackendImplREST.exists
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 sec
Running com.telefonica.iot.cygnus.backends.http.JsonResponseTest
Testing JsonResponseTest.getReasonPhrase
Testing JsonResponseTest.getLocationHeader
Testing JsonResponseTest.getStatusCode
Testing JsonResponseTest.getJsonObject
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 sec
Running com.telefonica.iot.cygnus.backends.kafka.KafkaBackendImplTest
[KafkaBackendImplTest.send] --------------------- The backend sends a message to Kafka
[KafkaBackendImpl.send] ------------------  OK  - Added to be sent
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.369 sec
Running com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImplTest
Testing MySQLBackend.createTable (within first database
Testing MySQLBackend.createTable (within second database
Testing MySQLBackend.insertContextData
Testing MySQLBackend.createDatabase (first database creation
Testing MySQLBackend.createDatabase (second database creation
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.109 sec
Running com.telefonica.iot.cygnus.backends.postgresql.PostgreSQLBackendImplTest
Testing PostgreSQLBackend.createSchema (first schema creation
Testing PostgreSQLBackend.createSchema (second schema creation
Testing PostgreSQLBackend.createTable (within first schema
Testing PostgreSQLBackend.createTable (within second schema
Testing PostgreSQLBackend.insertContextData
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.02 sec
Running com.telefonica.iot.cygnus.channelselectors.RoundRobinChannelSelectorTest
Testing RoundRobinChannelSelector.getRequiredChannels
Testing RoundRobinChannelSelector.configure
Testing RoundRobinChannelSelector.getOptionalChannels
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.073 sec
Running com.telefonica.iot.cygnus.interceptors.CygnusGroupingRuleTest
[GroupingRule.isValid] -------------------------- fiware-servicePath field in a grouping rule must start with '/'
[GroupingRule.isValid] -------------------  OK  - The fiware-servicePath field in the rule '{"fiware_service_path":"\/rooms","regex":"room1","destination":"all_rooms","fields":["entityId"]}' starts with '/'
[GroupingRule.getXXXX] -------------------------- Rule's attributes are not null
[GroupingRule.getPattern] ----------------  OK  - Rule?s pattern is not null
[GroupingRule.getId] ---------------------  OK  - Rule?s id is upper than 0
[GroupingRule.getFields] -----------------  OK  - Rule?s fields are not null
[GroupingRule.getRegex] ------------------  OK  - Rule?s regex is not null
[GroupingRule.getDestination] ------------  OK  - Rule?s destination is not null
[GroupingRule.getNewFiwareServicePath] ---  OK  - Rule?s newFiwareServicePath is not null
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 sec
Running com.telefonica.iot.cygnus.management.ManagementInterfaceTest
Testing ManagementInterface.handle
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.073 sec
Running com.telefonica.iot.cygnus.utils.CommonUtilsTest
[Utils.getTimeInstant] -------------------------- When getting a time instant, it is properly obtained when passing a valid ISO 8601 timestamp with microseconds
[Utils.getTimeInstant] -------------------  OK  - Time instant obtained for '[{"name":"TimeInstant","value":"2017-01-01T00:00:01.123456Z","type":"SQL timestamp"}]' is '1483228801123'
[Utils.getTimeInstant] -------------------------- When getting a time instant, it is properly obtained when passing a valid SQL timestamp with miliseconds
[Utils.getTimeInstant] -------------------  OK  - Time instant obtained for '[{"name":"TimeInstant","value":"2017-01-01 00:00:01.123","type":"SQL timestamp"}]' is '1483228801123'
[Utils.getTimeInstant] -------------------------- When getting a time instant, it is properly obtained when passing a valid ISO 8601 timestamp without miliseconds
[Utils.getTimeInstant] -------------------  OK  - Time instant obtained for '[{"name":"TimeInstant","value":"2017-01-01T00:00:01Z","type":"SQL timestamp"}]' is '1483228801000'
[Utils.getTimeInstant] -------------------------- When getting a time instant, it is properly obtained when passing a valid ISO 8601 timestamp with miliseconds
[Utils.getTimeInstant] -------------------  OK  - Time instant obtained for '[{"name":"TimeInstant","value":"2017-01-01T00:00:01.123Z","type":"SQL timestamp"}]' is '1483228801123'
[Utils.getTimeInstant] -------------------------- When getting a time instant, it is properly obtained when passing a valid SQL timestamp without miliseconds
[Utils.getTimeInstant] -------------------  OK  - Time instant obtained for '[{"name":"TimeInstant","value":"2017-01-01 00:00:01","type":"SQL timestamp"}]' is '1483228801000'
[Utils.getTimeInstant] -------------------------- When getting a time instant, it is properly obtained when passing a valid SQL timestamp with microseconds
[Utils.getTimeInstant] -------------------  OK  - Time instant obtained for '[{"name":"TimeInstant","value":"2017-01-01 00:00:01.123456","type":"SQL timestamp"}]' is '1483228801123'
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.152 sec

Results :

Tests run: 43, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 27.870s
[INFO] Finished at: Tue May 03 17:44:40 CEST 2016
[INFO] Final Memory: 24M/81M
[INFO] ------------------------------------------------------------------------
```
