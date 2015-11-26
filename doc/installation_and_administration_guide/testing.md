#Testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ mvn test
    
You should get an output similat to the following one:

```
$ mvn test
[INFO] Scanning for projects...
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for com.telefonica.iot:cygnus:jar:0.10.0_SNAPSHOT
[WARNING] 'version' uses an unsupported snapshot version format, should be '*-SNAPSHOT' instead. @ line 7, column 12
[WARNING] 
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING] 
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
[WARNING] 
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building cygnus 0.10.0_SNAPSHOT
[INFO] ------------------------------------------------------------------------
[WARNING] The artifact xml-apis:xml-apis:jar:1.2.01 has been relocated to xerces:xmlParserAPIs:jar:2.6.2
[INFO] 
[INFO] --- maven-resources-plugin:2.5:resources (default-resources) @ cygnus ---
[debug] execute contextualize
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ cygnus ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:2.5:testResources (default-testResources) @ cygnus ---
[debug] execute contextualize
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/frb/devel/fiware/fiware-cygnus/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ cygnus ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.10:test (default-test) @ cygnus ---
[INFO] Surefire report directory: /Users/frb/devel/fiware/fiware-cygnus/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.telefonica.iot.cygnus.backends.ckan.CKANBackendImplTest
15/11/25 10:50:44 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:44 INFO http.HttpClientFactory: Settubg default max connections per route (100)
15/11/25 10:50:44 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:44 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANBackendImpl.persist (column)
15/11/25 10:50:44 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:44 INFO http.HttpClientFactory: Settubg default max connections per route (100)
15/11/25 10:50:44 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:44 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANBackendImpl.persist (row)
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.365 sec
Running com.telefonica.iot.cygnus.backends.ckan.CKANCacheTest
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.getOrgId
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.getPkgId
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.getResId
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.isCachedOrg
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.isCachedPkg
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.isCachedRes
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.setOrgId
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.setPkgId
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing CKANCache.setResId
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.072 sec
Running com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplBinaryTest
Testing HDFSBackendImpl.createFile
2015-11-25 10:50:45.283 java[2544:112404] Unable to load realm info from SCDynamicStore
Testing HDFSBackendImpl.append
Testing HDFSBackendImplBinary.createDir
Testing HDFSBackendImpl.exists
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.781 sec
Running com.telefonica.iot.cygnus.backends.hdfs.HDFSBackendImplRESTTest
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing HDFSBackendImplREST.createFile
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing HDFSBackendImplREST.append
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing HDFSBackendImplREST.createDir
15/11/25 10:50:45 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:45 INFO http.HttpClientFactory: Settubg default max connections per route (100)
Testing HDFSBackendImplREST.exists
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.037 sec
Running com.telefonica.iot.cygnus.backends.http.JsonResponseTest
Testing JsonResponseTest.getReasonPhrase
Testing JsonResponseTest.getLocationHeader
Testing JsonResponseTest.getStatusCode
Testing JsonResponseTest.getJsonObject
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec
Running com.telefonica.iot.cygnus.backends.mysql.MySQLBackendImplTest
Testing MySQLBackend.createTable (within first database
Testing MySQLBackend.createTable (within second database
Testing MySQLBackend.insertContextData
Testing MySQLBackend.createDatabase (first database creation
Testing MySQLBackend.createDatabase (second database creation
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.097 sec
Running com.telefonica.iot.cygnus.channelselectors.RoundRobinChannelSelectorTest
Testing RoundRobinChannelSelector.getRequiredChannels
Testing RoundRobinChannelSelector.configure
Testing RoundRobinChannelSelector.getOptionalChannels
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.075 sec
Running com.telefonica.iot.cygnus.containers.NotifyContextRequestTest
getSubscriptionId (notify-xml-simple)
getSubscriptionId (notify-xml-compound)
getSubscriptionId (notify-xml-metadata)
getSubscriptionId (notify-json-simple)
getSubscriptionId (notify-json-compound)
getSubscriptionId (notify-json-metadata)
getOriginator (notify-xml-simple)
getOriginator (notify-xml-compound)
getOriginator (notify-xml-metadata)
getOriginator (notify-json-simple)
getOriginator (notify-json-compound)
getOriginator (notify-json-metadata)
getOriginator (notify-xml-simple)
getOriginator (notify-xml-compound)
getOriginator (notify-xml-compound-nested)
getOriginator (notify-xml-metadata)
getOriginator (notify-xml-simple-unordered)
getOriginator (notify-xml-simple-null-attrs)
getOriginator (notify-json-simple)
getOriginator (notify-json-compound)
getOriginator (notify-json-compound-nested)
getOriginator (notify-json-metadata)
getOriginator (notify-json-simple-unordered)
getOriginator (notify-json-simple-null-attrs)
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.222 sec
Running com.telefonica.iot.cygnus.handlers.OrionRestHandlerTest
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Cygnus version (0.10.0_SNAPSHOT.UNKNOWN)
Testing 'configure' method from class 'OrionRestHandler'
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Startup completed
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Cygnus version (0.10.0_SNAPSHOT.UNKNOWN)
Testing 'getEvents' method from class 'OrionRestHandler' (invalid characters
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Startup completed
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Starting transaction (1448445046-433-0000000000)
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Received data (<tag1>1</tag1><tag2>2</tag2>)
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Event put in the channel (id=53881443, ttl=10)
Testing 'getEvents' method from class 'OrionRestHandler' ("root" servicePath name
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Startup completed
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Starting transaction (1448445046-433-0000000001)
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Received data (<tag1>1</tag1><tag2>2</tag2>)
15/11/25 10:50:46 INFO handlers.OrionRestHandler: Event put in the channel (id=211201404, ttl=10)
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.091 sec
Running com.telefonica.iot.cygnus.http.JettyServerTest
15/11/25 10:50:46 INFO mortbay.log: Logging to org.slf4j.impl.Log4jLoggerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog
Testing JettyServer.testConfigure
Wait 5 seconds before checking the Jetty server is running
15/11/25 10:50:46 INFO mortbay.log: jetty-6.1.26
15/11/25 10:50:46 INFO mortbay.log: Started SocketConnector@0.0.0.0:12345
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.086 sec
Running com.telefonica.iot.cygnus.interceptors.GroupingInterceptorTest
Testing GroupingInterceptor.initialize
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: Grouping rules read: {    "grouping_rules": [        {            "id": 1,            "fields": [                "entityId",                "entityType"            ],            "regex": "Room\.(\d*)Room",            "destination": "numeric_rooms",            "fiware_service_path": "rooms"        },        {            "id": 2,            "fields": [                "entityId"            ],            "regex": "Car",            "destination": "cars",            "fiware_service_path": "vehicles"        },        {            "id": 3,            "fields": [                "servicePath"            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "city_indicators",            "some_other_field_to_be_ignored": "xxx"        },        {            "id": 4        },        {            "id": 5,            "fields": [            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "city_indicators",            "some_other_field_to_be_ignored": "xxx"        },        {            "id": 6,            "fields": [                "servicePath"            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "",            "some_other_field_to_be_ignored": "xxx"        },        {            "id": "abc",            "fields": [                "servicePath"            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "",            "some_other_field_to_be_ignored": "xxx"        }    ]}
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: Grouping rules syntax is OK
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, some field is missing. It will be discarded. Details={"id":4}
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, some field is empty. It will be discarded. Details={"id":5,"some_other_field_to_be_ignored":"xxx","fiware_service_path":"city_indicators","regex":"GARDENS","destination":"gardens","fields":[]}
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, some field is empty. It will be discarded. Details={"id":6,"some_other_field_to_be_ignored":"xxx","fiware_service_path":"","regex":"GARDENS","destination":"gardens","fields":["servicePath"]}
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, the id is not numeric or it is missing. It will be discarded. Details={"id":"abc","some_other_field_to_be_ignored":"xxx","fiware_service_path":"","regex":"GARDENS","destination":"gardens","fields":["servicePath"]}
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: Grouping rules regex'es have been compiled
Testing GroupingInterceptor.intercept (grouping_rules.conf exists)
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: Grouping rules read: {    "grouping_rules": [        {            "id": 1,            "fields": [                "entityId",                "entityType"            ],            "regex": "Room\.(\d*)Room",            "destination": "numeric_rooms",            "fiware_service_path": "rooms"        },        {            "id": 2,            "fields": [                "entityId"            ],            "regex": "Car",            "destination": "cars",            "fiware_service_path": "vehicles"        },        {            "id": 3,            "fields": [                "servicePath"            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "city_indicators",            "some_other_field_to_be_ignored": "xxx"        },        {            "id": 4        },        {            "id": 5,            "fields": [            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "city_indicators",            "some_other_field_to_be_ignored": "xxx"        },        {            "id": 6,            "fields": [                "servicePath"            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "",            "some_other_field_to_be_ignored": "xxx"        },        {            "id": "abc",            "fields": [                "servicePath"            ],            "regex": "GARDENS",            "destination": "gardens",            "fiware_service_path": "",            "some_other_field_to_be_ignored": "xxx"        }    ]}
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: Grouping rules syntax is OK
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, some field is missing. It will be discarded. Details={"id":4}
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, some field is empty. It will be discarded. Details={"id":5,"some_other_field_to_be_ignored":"xxx","fiware_service_path":"city_indicators","regex":"GARDENS","destination":"gardens","fields":[]}
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, some field is empty. It will be discarded. Details={"id":6,"some_other_field_to_be_ignored":"xxx","fiware_service_path":"","regex":"GARDENS","destination":"gardens","fields":["servicePath"]}
15/11/25 10:50:51 WARN interceptors.GroupingInterceptor: Invalid grouping rule, the id is not numeric or it is missing. It will be discarded. Details={"id":"abc","some_other_field_to_be_ignored":"xxx","fiware_service_path":"","regex":"GARDENS","destination":"gardens","fields":["servicePath"]}
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: Grouping rules regex'es have been compiled
Testing GroupingInterceptor.intercept (grouping_rules.conf is not set)
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: No grouping rules read
Testing GroupingInterceptor.intercept (grouping_rules.conf does not exist)
15/11/25 10:50:51 ERROR interceptors.GroupingInterceptor: File not found. Details=whatever (No such file or directory))
15/11/25 10:50:51 INFO interceptors.GroupingInterceptor: No grouping rules read
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.136 sec
Running com.telefonica.iot.cygnus.management.ManagementInterfaceTest
Testing ManagementInterface.handle
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.101 sec
Running com.telefonica.iot.cygnus.sinks.OrionCKANSinkTest
Testing OrionCKANSink.start
15/11/25 10:50:52 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:52 INFO http.HttpClientFactory: Settubg default max connections per route (100)
15/11/25 10:50:52 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:52 INFO http.HttpClientFactory: Settubg default max connections per route (100)
15/11/25 10:50:52 INFO sinks.OrionCKANSink: [null] Startup completed
Testing OrionCKANSink.processContextResponses (normal resource lengths)
15/11/25 10:50:52 INFO sinks.OrionCKANSink: [null] Persisting data at OrionCKANSink (orgName=vehicles, pkgName=vehicles_4wheels, resName=car1-car, data=123456789,1970-01-02T10:17:36.789Z,car1,car,speed,float,"112.9",[])
Testing OrionCKANSink.processContextResponses (too long service name)
Testing OrionCKANSink.processContextResponses (too long servicePath name)
Testing OrionCKANSink.processContextResponses (too long destination name)
Testing OrionCKANSink.processContextResponses ("root" servicePath name)
15/11/25 10:50:52 INFO sinks.OrionCKANSink: [null] Persisting data at OrionCKANSink (orgName=vehicles, pkgName=vehicles, resName=car1-car, data=123456789,1970-01-02T10:17:36.789Z,car1,car,speed,float,"112.9",[])
Testing OrionCKANSink.processContextResponses (multiple destinations and fiware-servicePaths)
15/11/25 10:50:52 INFO sinks.OrionCKANSink: [null] Persisting data at OrionCKANSink (orgName=vehicles, pkgName=vehicles_4wheelsSport, resName=sport1, data=123456789,1970-01-02T10:17:36.789Z,car1,car,speed,float,"112.9",[])
15/11/25 10:50:52 INFO sinks.OrionCKANSink: [null] Persisting data at OrionCKANSink (orgName=vehicles, pkgName=vehicles_4wheelsUrban, resName=urban1, data=123456789,1970-01-02T10:17:36.789Z,car2,car,speed,float,"115.8",[])
Testing OrionCKANSink.configure
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.476 sec
Running com.telefonica.iot.cygnus.sinks.OrionHDFSSinkTest
Testing OrionHDFSSink.start
15/11/25 10:50:52 INFO http.HttpClientFactory: Setting max total connections (500)
15/11/25 10:50:52 INFO http.HttpClientFactory: Settubg default max connections per route (100)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Startup completed
Testing OrionHDFSSinkTest.configure
Testing OrionHDFSSink.persistBatch (json-row file format)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles/cars/my_cars/my_cars.txt), Data ({"recvTimeTs":"123456","recvTime":"1970-01-02T10:17:36.789Z","fiwareservicepath":"cars","entityId":"car1","entityType":"car","attrName":"speed","attrType":"float","attrValue":"112.9","attrMd":[]})
Testing OrionHDFSSink.persistBatch (json-column file format)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles/cars/my_cars/my_cars.txt), Data ({"recvTime":"1970-01-02T10:17:36.789Z","fiwareservicepath":"cars","entityId":"car1","entityType":"car", "speed":"112.9", "speed_md":[]})
Testing OrionHDFSSink.persistBatch (csv-row file format)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles/cars/my_cars/my_cars.txt), Data (123456,1970-01-02T10:17:36.789Z,cars,car1,car,speed,float,112.9,hdfs:///user/user1/vehicles/cars/my_cars_speed_float/my_cars_speed_float.txt)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting metadata at OrionHDFSSink. HDFS file (vehicles/cars/my_cars_speed_float/my_cars_speed_float.txt), Data ()
Testing OrionHDFSSink.persistBatch (csv-column file format)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles/cars/my_cars/my_cars.txt), Data (1970-01-02T10:17:36.789Z,cars,car1,car,112.9,hdfs:///user/user1/vehicles/cars/my_cars_speed_float/my_cars_speed_float.txt)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting metadata at OrionHDFSSink. HDFS file (vehicles/cars/my_cars_speed_float/my_cars_speed_float.txt), Data ()
Testing OrionHDFSSink.persistBatch ("root" servicePath name)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles//my_cars/my_cars.txt), Data ({"recvTimeTs":"123456","recvTime":"1970-01-02T10:17:36.789Z","fiwareservicepath":"","entityId":"car1","entityType":"car","attrName":"speed","attrType":"float","attrValue":"112.9","attrMd":[]})
Testing OrionHDFSSink.persistBatch (multiple destinations and fiware-servicePaths)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles/cars/my_cars/my_cars.txt), Data ({"recvTimeTs":"123456","recvTime":"1970-01-02T10:17:36.789Z","fiwareservicepath":"cars","entityId":"car1","entityType":"car","attrName":"speed","attrType":"float","attrValue":"112.9","attrMd":[]})
Testing OrionHDFSSink.persistBatch (null batches)
Testing OrionHDFSSink.configure (deprecated parameters are used)
Testing OrionHDFSSink.persistBatch (normal resource lengths)
15/11/25 10:50:52 INFO sinks.OrionHDFSSink: [null] Persisting data at OrionHDFSSink. HDFS file (vehicles/cars/my_cars/my_cars.txt), Data ({"recvTimeTs":"123456","recvTime":"1970-01-02T10:17:36.789Z","fiwareservicepath":"cars","entityId":"car1","entityType":"car","attrName":"speed","attrType":"float","attrValue":"112.9","attrMd":[]})
Testing OrionHDFSSink.persistBatch (too long service name)
Testing OrionHDFSSink.persistBatch (too long servicePath name)
Testing OrionHDFSSink.persistBatch (too long destination name)
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.143 sec
Running com.telefonica.iot.cygnus.sinks.OrionKafkaSinkTest
15/11/25 10:50:52 INFO server.ZooKeeperServerMain: Starting server
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:zookeeper.version=3.4.5-1392090, built on 09/30/2012 17:52 GMT
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:host.name=mac-510380.hi.inet
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.version=1.6.0_65
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.vendor=Apple Inc.
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.home=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.class.path=/Users/frb/devel/fiware/fiware-cygnus/target/test-classes:/Users/frb/devel/fiware/fiware-cygnus/target/classes:/Users/frb/.m2/repository/org/mockito/mockito-all/1.9.5/mockito-all-1.9.5.jar:/Users/frb/.m2/repository/junit/junit/4.11/junit-4.11.jar:/Users/frb/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-core/1.4.0/flume-ng-core-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-sdk/1.4.0/flume-ng-sdk-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-configuration/1.4.0/flume-ng-configuration-1.4.0.jar:/Users/frb/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:/Users/frb/.m2/repository/com/google/guava/guava/10.0.1/guava-10.0.1.jar:/Users/frb/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar:/Users/frb/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar:/Users/frb/.m2/repository/commons-codec/commons-codec/1.8/commons-codec-1.8.jar:/Users/frb/.m2/repository/org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar:/Users/frb/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:/Users/frb/.m2/repository/commons-lang/commons-lang/2.5/commons-lang-2.5.jar:/Users/frb/.m2/repository/org/apache/avro/avro/1.7.3/avro-1.7.3.jar:/Users/frb/.m2/repository/com/thoughtworks/paranamer/paranamer/2.3/paranamer-2.3.jar:/Users/frb/.m2/repository/org/apache/avro/avro-ipc/1.7.3/avro-ipc-1.7.3.jar:/Users/frb/.m2/repository/org/apache/velocity/velocity/1.7/velocity-1.7.jar:/Users/frb/.m2/repository/io/netty/netty/3.4.0.Final/netty-3.4.0.Final.jar:/Users/frb/.m2/repository/joda-time/joda-time/2.1/joda-time-2.1.jar:/Users/frb/.m2/repository/org/mortbay/jetty/servlet-api/2.5-20110124/servlet-api-2.5-20110124.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jetty-util/6.1.26/jetty-util-6.1.26.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jetty/6.1.26/jetty-6.1.26.jar:/Users/frb/.m2/repository/org/apache/thrift/libthrift/0.7.0/libthrift-0.7.0.jar:/Users/frb/.m2/repository/org/apache/mina/mina-core/2.0.4/mina-core-2.0.4.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-node/1.4.0/flume-ng-node-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-sinks/flume-hdfs-sink/1.4.0/flume-hdfs-sink-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-sinks/flume-irc-sink/1.4.0/flume-irc-sink-1.4.0.jar:/Users/frb/.m2/repository/org/schwering/irclib/1.10/irclib-1.10.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-channels/flume-jdbc-channel/1.4.0/flume-jdbc-channel-1.4.0.jar:/Users/frb/.m2/repository/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar:/Users/frb/.m2/repository/commons-pool/commons-pool/1.5.4/commons-pool-1.5.4.jar:/Users/frb/.m2/repository/org/apache/derby/derby/10.8.2.2/derby-10.8.2.2.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-channels/flume-file-channel/1.4.0/flume-file-channel-1.4.0.jar:/Users/frb/.m2/repository/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar:/Users/frb/.m2/repository/com/google/protobuf/protobuf-java/2.4.1/protobuf-java-2.4.1.jar:/Users/frb/.m2/repository/log4j/apache-log4j-extras/1.1/apache-log4j-extras-1.1.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.3/jackson-core-asl-1.9.3.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.3/jackson-mapper-asl-1.9.3.jar:/Users/frb/.m2/repository/org/apache/httpcomponents/httpclient/4.2.1/httpclient-4.2.1.jar:/Users/frb/.m2/repository/org/apache/httpcomponents/httpcore/4.2.1/httpcore-4.2.1.jar:/Users/frb/.m2/repository/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:/Users/frb/.m2/repository/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar:/Users/frb/.m2/repository/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar:/Users/frb/.m2/repository/xerces/xmlParserAPIs/2.6.2/xmlParserAPIs-2.6.2.jar:/Users/frb/.m2/repository/mysql/mysql-connector-java/5.1.31/mysql-connector-java-5.1.31.jar:/Users/frb/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/Users/frb/.m2/repository/org/apache/hadoop/hadoop-core/1.2.1/hadoop-core-1.2.1.jar:/Users/frb/.m2/repository/xmlenc/xmlenc/0.52/xmlenc-0.52.jar:/Users/frb/.m2/repository/com/sun/jersey/jersey-core/1.8/jersey-core-1.8.jar:/Users/frb/.m2/repository/com/sun/jersey/jersey-json/1.8/jersey-json-1.8.jar:/Users/frb/.m2/repository/org/codehaus/jettison/jettison/1.1/jettison-1.1.jar:/Users/frb/.m2/repository/com/sun/xml/bind/jaxb-impl/2.2.3-1/jaxb-impl-2.2.3-1.jar:/Users/frb/.m2/repository/javax/xml/bind/jaxb-api/2.2.2/jaxb-api-2.2.2.jar:/Users/frb/.m2/repository/javax/xml/stream/stax-api/1.0-2/stax-api-1.0-2.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-jaxrs/1.7.1/jackson-jaxrs-1.7.1.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-xc/1.7.1/jackson-xc-1.7.1.jar:/Users/frb/.m2/repository/com/sun/jersey/jersey-server/1.8/jersey-server-1.8.jar:/Users/frb/.m2/repository/asm/asm/3.1/asm-3.1.jar:/Users/frb/.m2/repository/commons-httpclient/commons-httpclient/3.0.1/commons-httpclient-3.0.1.jar:/Users/frb/.m2/repository/org/apache/commons/commons-math/2.1/commons-math-2.1.jar:/Users/frb/.m2/repository/commons-configuration/commons-configuration/1.6/commons-configuration-1.6.jar:/Users/frb/.m2/repository/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:/Users/frb/.m2/repository/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:/Users/frb/.m2/repository/commons-beanutils/commons-beanutils-core/1.8.0/commons-beanutils-core-1.8.0.jar:/Users/frb/.m2/repository/commons-net/commons-net/1.4.1/commons-net-1.4.1.jar:/Users/frb/.m2/repository/tomcat/jasper-runtime/5.5.12/jasper-runtime-5.5.12.jar:/Users/frb/.m2/repository/tomcat/jasper-compiler/5.5.12/jasper-compiler-5.5.12.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jsp-api-2.1/6.1.14/jsp-api-2.1-6.1.14.jar:/Users/frb/.m2/repository/org/mortbay/jetty/servlet-api-2.5/6.1.14/servlet-api-2.5-6.1.14.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jsp-2.1/6.1.14/jsp-2.1-6.1.14.jar:/Users/frb/.m2/repository/ant/ant/1.6.5/ant-1.6.5.jar:/Users/frb/.m2/repository/commons-el/commons-el/1.0/commons-el-1.0.jar:/Users/frb/.m2/repository/net/java/dev/jets3t/jets3t/0.6.1/jets3t-0.6.1.jar:/Users/frb/.m2/repository/hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar:/Users/frb/.m2/repository/oro/oro/2.0.8/oro-2.0.8.jar:/Users/frb/.m2/repository/org/eclipse/jdt/core/3.1.1/core-3.1.1.jar:/Users/frb/.m2/repository/org/apache/hive/hive-exec/0.13.0/hive-exec-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-ant/0.13.0/hive-ant-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-metastore/0.13.0/hive-metastore-0.13.0.jar:/Users/frb/.m2/repository/com/jolbox/bonecp/0.8.0.RELEASE/bonecp-0.8.0.RELEASE.jar:/Users/frb/.m2/repository/org/datanucleus/datanucleus-api-jdo/3.2.6/datanucleus-api-jdo-3.2.6.jar:/Users/frb/.m2/repository/org/datanucleus/datanucleus-rdbms/3.2.9/datanucleus-rdbms-3.2.9.jar:/Users/frb/.m2/repository/javax/jdo/jdo-api/3.0.1/jdo-api-3.0.1.jar:/Users/frb/.m2/repository/javax/transaction/jta/1.1/jta-1.1.jar:/Users/frb/.m2/repository/org/apache/hive/hive-shims/0.13.0/hive-shims-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-common/0.13.0/hive-shims-common-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-0.20/0.13.0/hive-shims-0.20-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-common-secure/0.13.0/hive-shims-common-secure-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-0.20S/0.13.0/hive-shims-0.20S-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-0.23/0.13.0/hive-shims-0.23-0.13.0.jar:/Users/frb/.m2/repository/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/Users/frb/.m2/repository/org/antlr/antlr-runtime/3.4/antlr-runtime-3.4.jar:/Users/frb/.m2/repository/org/antlr/stringtemplate/3.2.1/stringtemplate-3.2.1.jar:/Users/frb/.m2/repository/antlr/antlr/2.7.7/antlr-2.7.7.jar:/Users/frb/.m2/repository/org/antlr/ST4/4.0.4/ST4-4.0.4.jar:/Users/frb/.m2/repository/org/apache/ant/ant/1.9.1/ant-1.9.1.jar:/Users/frb/.m2/repository/org/apache/ant/ant-launcher/1.9.1/ant-launcher-1.9.1.jar:/Users/frb/.m2/repository/org/apache/commons/commons-compress/1.4.1/commons-compress-1.4.1.jar:/Users/frb/.m2/repository/org/tukaani/xz/1.0/xz-1.0.jar:/Users/frb/.m2/repository/org/apache/thrift/libfb303/0.9.0/libfb303-0.9.0.jar:/Users/frb/.m2/repository/org/apache/zookeeper/zookeeper/3.4.5/zookeeper-3.4.5.jar:/Users/frb/.m2/repository/jline/jline/0.9.94/jline-0.9.94.jar:/Users/frb/.m2/repository/org/codehaus/groovy/groovy-all/2.1.6/groovy-all-2.1.6.jar:/Users/frb/.m2/repository/org/datanucleus/datanucleus-core/3.2.10/datanucleus-core-3.2.10.jar:/Users/frb/.m2/repository/stax/stax-api/1.0.1/stax-api-1.0.1.jar:/Users/frb/.m2/repository/org/apache/hive/hive-jdbc/0.13.0/hive-jdbc-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-common/0.13.0/hive-common-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-serde/0.13.0/hive-serde-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-service/0.13.0/hive-service-0.13.0.jar:/Users/frb/.m2/repository/net/sf/jpam/jpam/1.1/jpam-1.1.jar:/Users/frb/.m2/repository/org/eclipse/jetty/aggregate/jetty-all/7.6.0.v20120127/jetty-all-7.6.0.v20120127.jar:/Users/frb/.m2/repository/org/apache/geronimo/specs/geronimo-jta_1.1_spec/1.1.1/geronimo-jta_1.1_spec-1.1.1.jar:/Users/frb/.m2/repository/javax/mail/mail/1.4.1/mail-1.4.1.jar:/Users/frb/.m2/repository/javax/activation/activation/1.1/activation-1.1.jar:/Users/frb/.m2/repository/org/apache/geronimo/specs/geronimo-jaspic_1.0_spec/1.0/geronimo-jaspic_1.0_spec-1.0.jar:/Users/frb/.m2/repository/org/apache/geronimo/specs/geronimo-annotation_1.0_spec/1.1.1/geronimo-annotation_1.0_spec-1.1.1.jar:/Users/frb/.m2/repository/asm/asm-commons/3.1/asm-commons-3.1.jar:/Users/frb/.m2/repository/asm/asm-tree/3.1/asm-tree-3.1.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-server/7.2.0.v20101020/jetty-server-7.2.0.v20101020.jar:/Users/frb/.m2/repository/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-continuation/7.2.0.v20101020/jetty-continuation-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-http/7.2.0.v20101020/jetty-http-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-io/7.2.0.v20101020/jetty-io-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-util/7.2.0.v20101020/jetty-util-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/mongodb/mongodb-driver/3.0.0/mongodb-driver-3.0.0.jar:/Users/frb/.m2/repository/org/mongodb/mongodb-driver-core/3.0.0/mongodb-driver-core-3.0.0.jar:/Users/frb/.m2/repository/org/mongodb/bson/3.0.0/bson-3.0.0.jar:/Users/frb/.m2/repository/org/apache/kafka/kafka-clients/0.8.2.0/kafka-clients-0.8.2.0.jar:/Users/frb/.m2/repository/net/jpountz/lz4/lz4/1.2.0/lz4-1.2.0.jar:/Users/frb/.m2/repository/org/xerial/snappy/snappy-java/1.1.1.6/snappy-java-1.1.1.6.jar:/Users/frb/.m2/repository/com/101tec/zkclient/0.5/zkclient-0.5.jar:/Users/frb/.m2/repository/org/apache/kafka/kafka_2.11/0.8.2.1/kafka_2.11-0.8.2.1.jar:/Users/frb/.m2/repository/org/scala-lang/modules/scala-xml_2.11/1.0.2/scala-xml_2.11-1.0.2.jar:/Users/frb/.m2/repository/com/yammer/metrics/metrics-core/2.2.0/metrics-core-2.2.0.jar:/Users/frb/.m2/repository/net/sf/jopt-simple/jopt-simple/3.2/jopt-simple-3.2.jar:/Users/frb/.m2/repository/org/scala-lang/modules/scala-parser-combinators_2.11/1.0.2/scala-parser-combinators_2.11-1.0.2.jar:/Users/frb/.m2/repository/org/scala-lang/scala-library/2.11.5/scala-library-2.11.5.jar:/Users/frb/.m2/repository/org/apache/curator/curator-test/2.8.0/curator-test-2.8.0.jar:/Users/frb/.m2/repository/org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar:
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.library.path=.:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.io.tmpdir=/var/folders/nr/tjbgf0m95gv7nnspcmgzqp0h0000gq/T/
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:java.compiler=<NA>
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:os.name=Mac OS X
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:os.arch=x86_64
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:os.version=10.10.5
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:user.name=frb
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:user.home=/Users/frb
15/11/25 10:50:52 INFO server.ZooKeeperServer: Server environment:user.dir=/Users/frb/devel/fiware/fiware-cygnus
15/11/25 10:50:52 INFO server.ZooKeeperServer: tickTime set to 3000
15/11/25 10:50:52 INFO server.ZooKeeperServer: minSessionTimeout set to -1
15/11/25 10:50:52 INFO server.ZooKeeperServer: maxSessionTimeout set to -1
15/11/25 10:50:53 INFO server.NIOServerCnxnFactory: binding to port 0.0.0.0/0.0.0.0:2181
15/11/25 10:50:53 INFO persistence.FileTxnSnapLog: Snapshotting: 0x0 to /private/var/folders/nr/tjbgf0m95gv7nnspcmgzqp0h0000gq/T/1448445052750-0/version-2/snapshot.0
Testing OrionKafkaSink.start
15/11/25 10:50:54 INFO producer.ProducerConfig: ProducerConfig values: 
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	block.on.buffer.full = true
	retry.backoff.ms = 100
	buffer.memory = 33554432
	batch.size = 16384
	metrics.sample.window.ms = 30000
	metadata.max.age.ms = 300000
	receive.buffer.bytes = 32768
	timeout.ms = 30000
	max.in.flight.requests.per.connection = 5
	metric.reporters = []
	bootstrap.servers = [localhost:9092]
	client.id = 
	compression.type = none
	retries = 0
	max.request.size = 1048576
	send.buffer.bytes = 131072
	acks = 1
	reconnect.backoff.ms = 10
	linger.ms = 0
	metrics.num.samples = 2
	metadata.fetch.timeout.ms = 60000

15/11/25 10:50:55 INFO zkclient.ZkEventThread: Starting ZkClient event thread.
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:zookeeper.version=3.4.5-1392090, built on 09/30/2012 17:52 GMT
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:host.name=mac-510380.hi.inet
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.version=1.6.0_65
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.vendor=Apple Inc.
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.home=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.class.path=/Users/frb/devel/fiware/fiware-cygnus/target/test-classes:/Users/frb/devel/fiware/fiware-cygnus/target/classes:/Users/frb/.m2/repository/org/mockito/mockito-all/1.9.5/mockito-all-1.9.5.jar:/Users/frb/.m2/repository/junit/junit/4.11/junit-4.11.jar:/Users/frb/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-core/1.4.0/flume-ng-core-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-sdk/1.4.0/flume-ng-sdk-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-configuration/1.4.0/flume-ng-configuration-1.4.0.jar:/Users/frb/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:/Users/frb/.m2/repository/com/google/guava/guava/10.0.1/guava-10.0.1.jar:/Users/frb/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar:/Users/frb/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar:/Users/frb/.m2/repository/commons-codec/commons-codec/1.8/commons-codec-1.8.jar:/Users/frb/.m2/repository/org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar:/Users/frb/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:/Users/frb/.m2/repository/commons-lang/commons-lang/2.5/commons-lang-2.5.jar:/Users/frb/.m2/repository/org/apache/avro/avro/1.7.3/avro-1.7.3.jar:/Users/frb/.m2/repository/com/thoughtworks/paranamer/paranamer/2.3/paranamer-2.3.jar:/Users/frb/.m2/repository/org/apache/avro/avro-ipc/1.7.3/avro-ipc-1.7.3.jar:/Users/frb/.m2/repository/org/apache/velocity/velocity/1.7/velocity-1.7.jar:/Users/frb/.m2/repository/io/netty/netty/3.4.0.Final/netty-3.4.0.Final.jar:/Users/frb/.m2/repository/joda-time/joda-time/2.1/joda-time-2.1.jar:/Users/frb/.m2/repository/org/mortbay/jetty/servlet-api/2.5-20110124/servlet-api-2.5-20110124.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jetty-util/6.1.26/jetty-util-6.1.26.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jetty/6.1.26/jetty-6.1.26.jar:/Users/frb/.m2/repository/org/apache/thrift/libthrift/0.7.0/libthrift-0.7.0.jar:/Users/frb/.m2/repository/org/apache/mina/mina-core/2.0.4/mina-core-2.0.4.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-node/1.4.0/flume-ng-node-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-sinks/flume-hdfs-sink/1.4.0/flume-hdfs-sink-1.4.0.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-sinks/flume-irc-sink/1.4.0/flume-irc-sink-1.4.0.jar:/Users/frb/.m2/repository/org/schwering/irclib/1.10/irclib-1.10.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-channels/flume-jdbc-channel/1.4.0/flume-jdbc-channel-1.4.0.jar:/Users/frb/.m2/repository/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar:/Users/frb/.m2/repository/commons-pool/commons-pool/1.5.4/commons-pool-1.5.4.jar:/Users/frb/.m2/repository/org/apache/derby/derby/10.8.2.2/derby-10.8.2.2.jar:/Users/frb/.m2/repository/org/apache/flume/flume-ng-channels/flume-file-channel/1.4.0/flume-file-channel-1.4.0.jar:/Users/frb/.m2/repository/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar:/Users/frb/.m2/repository/com/google/protobuf/protobuf-java/2.4.1/protobuf-java-2.4.1.jar:/Users/frb/.m2/repository/log4j/apache-log4j-extras/1.1/apache-log4j-extras-1.1.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.3/jackson-core-asl-1.9.3.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.3/jackson-mapper-asl-1.9.3.jar:/Users/frb/.m2/repository/org/apache/httpcomponents/httpclient/4.2.1/httpclient-4.2.1.jar:/Users/frb/.m2/repository/org/apache/httpcomponents/httpcore/4.2.1/httpcore-4.2.1.jar:/Users/frb/.m2/repository/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:/Users/frb/.m2/repository/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar:/Users/frb/.m2/repository/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar:/Users/frb/.m2/repository/xerces/xmlParserAPIs/2.6.2/xmlParserAPIs-2.6.2.jar:/Users/frb/.m2/repository/mysql/mysql-connector-java/5.1.31/mysql-connector-java-5.1.31.jar:/Users/frb/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/Users/frb/.m2/repository/org/apache/hadoop/hadoop-core/1.2.1/hadoop-core-1.2.1.jar:/Users/frb/.m2/repository/xmlenc/xmlenc/0.52/xmlenc-0.52.jar:/Users/frb/.m2/repository/com/sun/jersey/jersey-core/1.8/jersey-core-1.8.jar:/Users/frb/.m2/repository/com/sun/jersey/jersey-json/1.8/jersey-json-1.8.jar:/Users/frb/.m2/repository/org/codehaus/jettison/jettison/1.1/jettison-1.1.jar:/Users/frb/.m2/repository/com/sun/xml/bind/jaxb-impl/2.2.3-1/jaxb-impl-2.2.3-1.jar:/Users/frb/.m2/repository/javax/xml/bind/jaxb-api/2.2.2/jaxb-api-2.2.2.jar:/Users/frb/.m2/repository/javax/xml/stream/stax-api/1.0-2/stax-api-1.0-2.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-jaxrs/1.7.1/jackson-jaxrs-1.7.1.jar:/Users/frb/.m2/repository/org/codehaus/jackson/jackson-xc/1.7.1/jackson-xc-1.7.1.jar:/Users/frb/.m2/repository/com/sun/jersey/jersey-server/1.8/jersey-server-1.8.jar:/Users/frb/.m2/repository/asm/asm/3.1/asm-3.1.jar:/Users/frb/.m2/repository/commons-httpclient/commons-httpclient/3.0.1/commons-httpclient-3.0.1.jar:/Users/frb/.m2/repository/org/apache/commons/commons-math/2.1/commons-math-2.1.jar:/Users/frb/.m2/repository/commons-configuration/commons-configuration/1.6/commons-configuration-1.6.jar:/Users/frb/.m2/repository/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:/Users/frb/.m2/repository/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:/Users/frb/.m2/repository/commons-beanutils/commons-beanutils-core/1.8.0/commons-beanutils-core-1.8.0.jar:/Users/frb/.m2/repository/commons-net/commons-net/1.4.1/commons-net-1.4.1.jar:/Users/frb/.m2/repository/tomcat/jasper-runtime/5.5.12/jasper-runtime-5.5.12.jar:/Users/frb/.m2/repository/tomcat/jasper-compiler/5.5.12/jasper-compiler-5.5.12.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jsp-api-2.1/6.1.14/jsp-api-2.1-6.1.14.jar:/Users/frb/.m2/repository/org/mortbay/jetty/servlet-api-2.5/6.1.14/servlet-api-2.5-6.1.14.jar:/Users/frb/.m2/repository/org/mortbay/jetty/jsp-2.1/6.1.14/jsp-2.1-6.1.14.jar:/Users/frb/.m2/repository/ant/ant/1.6.5/ant-1.6.5.jar:/Users/frb/.m2/repository/commons-el/commons-el/1.0/commons-el-1.0.jar:/Users/frb/.m2/repository/net/java/dev/jets3t/jets3t/0.6.1/jets3t-0.6.1.jar:/Users/frb/.m2/repository/hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar:/Users/frb/.m2/repository/oro/oro/2.0.8/oro-2.0.8.jar:/Users/frb/.m2/repository/org/eclipse/jdt/core/3.1.1/core-3.1.1.jar:/Users/frb/.m2/repository/org/apache/hive/hive-exec/0.13.0/hive-exec-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-ant/0.13.0/hive-ant-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-metastore/0.13.0/hive-metastore-0.13.0.jar:/Users/frb/.m2/repository/com/jolbox/bonecp/0.8.0.RELEASE/bonecp-0.8.0.RELEASE.jar:/Users/frb/.m2/repository/org/datanucleus/datanucleus-api-jdo/3.2.6/datanucleus-api-jdo-3.2.6.jar:/Users/frb/.m2/repository/org/datanucleus/datanucleus-rdbms/3.2.9/datanucleus-rdbms-3.2.9.jar:/Users/frb/.m2/repository/javax/jdo/jdo-api/3.0.1/jdo-api-3.0.1.jar:/Users/frb/.m2/repository/javax/transaction/jta/1.1/jta-1.1.jar:/Users/frb/.m2/repository/org/apache/hive/hive-shims/0.13.0/hive-shims-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-common/0.13.0/hive-shims-common-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-0.20/0.13.0/hive-shims-0.20-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-common-secure/0.13.0/hive-shims-common-secure-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-0.20S/0.13.0/hive-shims-0.20S-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/shims/hive-shims-0.23/0.13.0/hive-shims-0.23-0.13.0.jar:/Users/frb/.m2/repository/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/Users/frb/.m2/repository/org/antlr/antlr-runtime/3.4/antlr-runtime-3.4.jar:/Users/frb/.m2/repository/org/antlr/stringtemplate/3.2.1/stringtemplate-3.2.1.jar:/Users/frb/.m2/repository/antlr/antlr/2.7.7/antlr-2.7.7.jar:/Users/frb/.m2/repository/org/antlr/ST4/4.0.4/ST4-4.0.4.jar:/Users/frb/.m2/repository/org/apache/ant/ant/1.9.1/ant-1.9.1.jar:/Users/frb/.m2/repository/org/apache/ant/ant-launcher/1.9.1/ant-launcher-1.9.1.jar:/Users/frb/.m2/repository/org/apache/commons/commons-compress/1.4.1/commons-compress-1.4.1.jar:/Users/frb/.m2/repository/org/tukaani/xz/1.0/xz-1.0.jar:/Users/frb/.m2/repository/org/apache/thrift/libfb303/0.9.0/libfb303-0.9.0.jar:/Users/frb/.m2/repository/org/apache/zookeeper/zookeeper/3.4.5/zookeeper-3.4.5.jar:/Users/frb/.m2/repository/jline/jline/0.9.94/jline-0.9.94.jar:/Users/frb/.m2/repository/org/codehaus/groovy/groovy-all/2.1.6/groovy-all-2.1.6.jar:/Users/frb/.m2/repository/org/datanucleus/datanucleus-core/3.2.10/datanucleus-core-3.2.10.jar:/Users/frb/.m2/repository/stax/stax-api/1.0.1/stax-api-1.0.1.jar:/Users/frb/.m2/repository/org/apache/hive/hive-jdbc/0.13.0/hive-jdbc-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-common/0.13.0/hive-common-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-serde/0.13.0/hive-serde-0.13.0.jar:/Users/frb/.m2/repository/org/apache/hive/hive-service/0.13.0/hive-service-0.13.0.jar:/Users/frb/.m2/repository/net/sf/jpam/jpam/1.1/jpam-1.1.jar:/Users/frb/.m2/repository/org/eclipse/jetty/aggregate/jetty-all/7.6.0.v20120127/jetty-all-7.6.0.v20120127.jar:/Users/frb/.m2/repository/org/apache/geronimo/specs/geronimo-jta_1.1_spec/1.1.1/geronimo-jta_1.1_spec-1.1.1.jar:/Users/frb/.m2/repository/javax/mail/mail/1.4.1/mail-1.4.1.jar:/Users/frb/.m2/repository/javax/activation/activation/1.1/activation-1.1.jar:/Users/frb/.m2/repository/org/apache/geronimo/specs/geronimo-jaspic_1.0_spec/1.0/geronimo-jaspic_1.0_spec-1.0.jar:/Users/frb/.m2/repository/org/apache/geronimo/specs/geronimo-annotation_1.0_spec/1.1.1/geronimo-annotation_1.0_spec-1.1.1.jar:/Users/frb/.m2/repository/asm/asm-commons/3.1/asm-commons-3.1.jar:/Users/frb/.m2/repository/asm/asm-tree/3.1/asm-tree-3.1.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-server/7.2.0.v20101020/jetty-server-7.2.0.v20101020.jar:/Users/frb/.m2/repository/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-continuation/7.2.0.v20101020/jetty-continuation-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-http/7.2.0.v20101020/jetty-http-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-io/7.2.0.v20101020/jetty-io-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/eclipse/jetty/jetty-util/7.2.0.v20101020/jetty-util-7.2.0.v20101020.jar:/Users/frb/.m2/repository/org/mongodb/mongodb-driver/3.0.0/mongodb-driver-3.0.0.jar:/Users/frb/.m2/repository/org/mongodb/mongodb-driver-core/3.0.0/mongodb-driver-core-3.0.0.jar:/Users/frb/.m2/repository/org/mongodb/bson/3.0.0/bson-3.0.0.jar:/Users/frb/.m2/repository/org/apache/kafka/kafka-clients/0.8.2.0/kafka-clients-0.8.2.0.jar:/Users/frb/.m2/repository/net/jpountz/lz4/lz4/1.2.0/lz4-1.2.0.jar:/Users/frb/.m2/repository/org/xerial/snappy/snappy-java/1.1.1.6/snappy-java-1.1.1.6.jar:/Users/frb/.m2/repository/com/101tec/zkclient/0.5/zkclient-0.5.jar:/Users/frb/.m2/repository/org/apache/kafka/kafka_2.11/0.8.2.1/kafka_2.11-0.8.2.1.jar:/Users/frb/.m2/repository/org/scala-lang/modules/scala-xml_2.11/1.0.2/scala-xml_2.11-1.0.2.jar:/Users/frb/.m2/repository/com/yammer/metrics/metrics-core/2.2.0/metrics-core-2.2.0.jar:/Users/frb/.m2/repository/net/sf/jopt-simple/jopt-simple/3.2/jopt-simple-3.2.jar:/Users/frb/.m2/repository/org/scala-lang/modules/scala-parser-combinators_2.11/1.0.2/scala-parser-combinators_2.11-1.0.2.jar:/Users/frb/.m2/repository/org/scala-lang/scala-library/2.11.5/scala-library-2.11.5.jar:/Users/frb/.m2/repository/org/apache/curator/curator-test/2.8.0/curator-test-2.8.0.jar:/Users/frb/.m2/repository/org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar:
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.library.path=.:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.io.tmpdir=/var/folders/nr/tjbgf0m95gv7nnspcmgzqp0h0000gq/T/
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:java.compiler=<NA>
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:os.name=Mac OS X
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:os.arch=x86_64
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:os.version=10.10.5
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:user.name=frb
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:user.home=/Users/frb
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Client environment:user.dir=/Users/frb/devel/fiware/fiware-cygnus
15/11/25 10:50:55 INFO zookeeper.ZooKeeper: Initiating client connection, connectString=localhost:2181 sessionTimeout=10000 watcher=org.I0Itec.zkclient.ZkClient@455dd32a
15/11/25 10:50:55 INFO zookeeper.ClientCnxn: Opening socket connection to server localhost/0:0:0:0:0:0:0:1:2181. Will not attempt to authenticate using SASL (No se puede localizar una configuraci?n de inicio de sesi?n)
15/11/25 10:50:55 INFO zookeeper.ClientCnxn: Socket connection established to localhost/0:0:0:0:0:0:0:1:2181, initiating session
15/11/25 10:50:55 INFO server.NIOServerCnxnFactory: Accepted socket connection from /0:0:0:0:0:0:0:1%0:52613
15/11/25 10:50:55 INFO server.ZooKeeperServer: Client attempting to establish new session at /0:0:0:0:0:0:0:1%0:52613
15/11/25 10:50:55 INFO persistence.FileTxnLog: Creating new log file: log.1
15/11/25 10:50:55 INFO server.ZooKeeperServer: Established session 0x1513e0d89210000 with negotiated timeout 10000 for client /0:0:0:0:0:0:0:1%0:52613
15/11/25 10:50:55 INFO zookeeper.ClientCnxn: Session establishment complete on server localhost/0:0:0:0:0:0:0:1:2181, sessionid = 0x1513e0d89210000, negotiated timeout = 10000
15/11/25 10:50:55 INFO zkclient.ZkClient: zookeeper state changed (SyncConnected)
15/11/25 10:50:55 INFO sinks.OrionKafkaSink: [null] Startup completed
15/11/25 10:50:55 INFO server.NIOServerCnxn: Closed socket connection for client /0:0:0:0:0:0:0:1%0:52613 which had sessionid 0x1513e0d89210000
15/11/25 10:50:55 INFO server.NIOServerCnxnFactory: NIOServerCnxn factory exited run method
15/11/25 10:50:55 INFO server.ZooKeeperServer: shutting down
15/11/25 10:50:55 INFO server.SessionTrackerImpl: Shutting down
15/11/25 10:50:55 INFO server.PrepRequestProcessor: Shutting down
15/11/25 10:50:55 INFO zookeeper.ClientCnxn: Unable to read additional data from server sessionid 0x1513e0d89210000, likely server has closed socket, closing socket connection and attempting reconnect
15/11/25 10:50:55 INFO server.SyncRequestProcessor: Shutting down
15/11/25 10:50:55 INFO server.PrepRequestProcessor: PrepRequestProcessor exited loop!
15/11/25 10:50:55 INFO server.SyncRequestProcessor: SyncRequestProcessor exited!
15/11/25 10:50:55 INFO server.FinalRequestProcessor: shutdown of request processor complete
15/11/25 10:50:55 INFO server.ZooKeeperServer: shutting down
15/11/25 10:50:55 INFO server.SessionTrackerImpl: Shutting down
15/11/25 10:50:55 INFO server.PrepRequestProcessor: Shutting down
15/11/25 10:50:55 INFO server.SyncRequestProcessor: Shutting down
15/11/25 10:50:55 INFO server.FinalRequestProcessor: shutdown of request processor complete
15/11/25 10:50:55 INFO zkclient.ZkClient: zookeeper state changed (Disconnected)
15/11/25 10:50:55 INFO zookeeper.ClientCnxn: Opening socket connection to server localhost/127.0.0.1:2181. Will not attempt to authenticate using SASL (No se puede localizar una configuraci?n de inicio de sesi?n)
15/11/25 10:50:55 WARN zookeeper.ClientCnxn: Session 0x1513e0d89210000 for server null, unexpected error, closing socket connection and attempting reconnect
java.net.ConnectException: Connection refused
	at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method)
	at sun.nio.ch.SocketChannelImpl.finishConnect(SocketChannelImpl.java:599)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:350)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1068)
15/11/25 10:50:55 INFO server.ZooKeeperServerMain: Starting server
15/11/25 10:50:55 INFO server.ZooKeeperServer: tickTime set to 3000
15/11/25 10:50:55 INFO server.ZooKeeperServer: minSessionTimeout set to -1
15/11/25 10:50:55 INFO server.ZooKeeperServer: maxSessionTimeout set to -1
15/11/25 10:50:55 INFO server.NIOServerCnxnFactory: binding to port 0.0.0.0/0.0.0.0:2181
15/11/25 10:50:55 INFO persistence.FileTxnSnapLog: Snapshotting: 0x0 to /private/var/folders/nr/tjbgf0m95gv7nnspcmgzqp0h0000gq/T/1448445055801-0/version-2/snapshot.0
Testing OrionKafkaSink.configure
15/11/25 10:50:56 INFO server.NIOServerCnxnFactory: NIOServerCnxn factory exited run method
15/11/25 10:50:56 INFO server.ZooKeeperServer: shutting down
15/11/25 10:50:56 INFO server.ZooKeeperServer: shutting down
15/11/25 10:50:56 INFO server.SessionTrackerImpl: Shutting down
15/11/25 10:50:56 INFO server.PrepRequestProcessor: Shutting down
15/11/25 10:50:56 INFO server.SessionTrackerImpl: Shutting down
15/11/25 10:50:56 INFO server.PrepRequestProcessor: Shutting down
15/11/25 10:50:56 INFO server.PrepRequestProcessor: PrepRequestProcessor exited loop!
15/11/25 10:50:56 INFO server.SyncRequestProcessor: Shutting down
15/11/25 10:50:56 INFO server.SyncRequestProcessor: Shutting down
15/11/25 10:50:56 INFO server.SyncRequestProcessor: SyncRequestProcessor exited!
15/11/25 10:50:56 INFO server.FinalRequestProcessor: shutdown of request processor complete
15/11/25 10:50:56 INFO server.FinalRequestProcessor: shutdown of request processor complete
15/11/25 10:50:56 INFO server.ZooKeeperServerMain: Starting server
15/11/25 10:50:56 INFO server.ZooKeeperServer: tickTime set to 3000
15/11/25 10:50:56 INFO server.ZooKeeperServer: minSessionTimeout set to -1
15/11/25 10:50:56 INFO server.ZooKeeperServer: maxSessionTimeout set to -1
15/11/25 10:50:56 INFO server.NIOServerCnxnFactory: binding to port 0.0.0.0/0.0.0.0:2181
15/11/25 10:50:56 INFO persistence.FileTxnSnapLog: Snapshotting: 0x0 to /private/var/folders/nr/tjbgf0m95gv7nnspcmgzqp0h0000gq/T/1448445056821-0/version-2/snapshot.0
15/11/25 10:50:57 INFO server.SessionTrackerImpl: SessionTrackerImpl exited loop!
15/11/25 10:50:57 INFO server.SessionTrackerImpl: SessionTrackerImpl exited loop!
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Opening socket connection to server localhost/0:0:0:0:0:0:0:1:2181. Will not attempt to authenticate using SASL (No se puede localizar una configuraci?n de inicio de sesi?n)
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Socket connection established to localhost/0:0:0:0:0:0:0:1:2181, initiating session
15/11/25 10:50:57 INFO server.NIOServerCnxnFactory: Accepted socket connection from /0:0:0:0:0:0:0:1%0:52619
15/11/25 10:50:57 INFO server.ZooKeeperServer: Client attempting to renew session 0x1513e0d89210000 at /0:0:0:0:0:0:0:1%0:52619
15/11/25 10:50:57 INFO server.ZooKeeperServer: Invalid session 0x1513e0d89210000 for client /0:0:0:0:0:0:0:1%0:52619, probably expired
15/11/25 10:50:57 INFO zkclient.ZkClient: zookeeper state changed (Expired)
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Unable to reconnect to ZooKeeper service, session 0x1513e0d89210000 has expired, closing socket connection
15/11/25 10:50:57 INFO zookeeper.ZooKeeper: Initiating client connection, connectString=localhost:2181 sessionTimeout=10000 watcher=org.I0Itec.zkclient.ZkClient@455dd32a
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: EventThread shut down
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Opening socket connection to server localhost/0:0:0:0:0:0:0:1:2181. Will not attempt to authenticate using SASL (No se puede localizar una configuraci?n de inicio de sesi?n)
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Socket connection established to localhost/0:0:0:0:0:0:0:1:2181, initiating session
15/11/25 10:50:57 INFO server.NIOServerCnxn: Closed socket connection for client /0:0:0:0:0:0:0:1%0:52619 which had sessionid 0x1513e0d89210000
15/11/25 10:50:57 INFO server.NIOServerCnxnFactory: Accepted socket connection from /0:0:0:0:0:0:0:1%0:52620
15/11/25 10:50:57 INFO server.ZooKeeperServer: Client attempting to establish new session at /0:0:0:0:0:0:0:1%0:52620
15/11/25 10:50:57 INFO persistence.FileTxnLog: Creating new log file: log.1
15/11/25 10:50:57 INFO server.ZooKeeperServer: Established session 0x1513e0d973a0000 with negotiated timeout 10000 for client /0:0:0:0:0:0:0:1%0:52620
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Session establishment complete on server localhost/0:0:0:0:0:0:0:1:2181, sessionid = 0x1513e0d973a0000, negotiated timeout = 10000
15/11/25 10:50:57 INFO zkclient.ZkClient: zookeeper state changed (SyncConnected)
Testing OrionKafkaSink.persist (topic-per-service)
15/11/25 10:50:57 INFO sinks.OrionKafkaSink: [null] Creating topic vehicles at OrionKafkaSink
15/11/25 10:50:57 INFO sinks.OrionKafkaSink: [null] Persisting data at OrionKafkaSink. Topic (vehicles), Data ({"headers":[{"fiware-service":"vehicles"},{"fiware-servicePath":"4wheels"},{"timestamp":123456789}],"body":{"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"}],"type":"car","isPattern":"false","id":"car1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}})
Testing OrionKafkaSink.persist (topic-per-service-path)
15/11/25 10:50:57 INFO sinks.OrionKafkaSink: [null] Creating topic 4wheels at OrionKafkaSink
15/11/25 10:50:57 INFO sinks.OrionKafkaSink: [null] Persisting data at OrionKafkaSink. Topic (4wheels), Data ({"headers":[{"fiware-service":"vehicles"},{"fiware-servicePath":"4wheels"},{"timestamp":123456789}],"body":{"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"}],"type":"car","isPattern":"false","id":"car1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}})
Testing OrionKafkaSink.persist (topic-per-destination)
15/11/25 10:50:57 INFO sinks.OrionKafkaSink: [null] Creating topic car1_car at OrionKafkaSink
15/11/25 10:50:57 INFO sinks.OrionKafkaSink: [null] Persisting data at OrionKafkaSink. Topic (car1_car), Data ({"headers":[{"fiware-service":"vehicles"},{"fiware-servicePath":"4wheels"},{"timestamp":123456789}],"body":{"contextElement":{"attributes":[{"name":"speed","type":"float","value":"112.9"}],"type":"car","isPattern":"false","id":"car1"},"statusCode":{"code":"200","reasonPhrase":"OK"}}})
15/11/25 10:50:57 INFO server.NIOServerCnxn: Closed socket connection for client /0:0:0:0:0:0:0:1%0:52620 which had sessionid 0x1513e0d973a0000
15/11/25 10:50:57 INFO server.NIOServerCnxnFactory: NIOServerCnxn factory exited run method
15/11/25 10:50:57 INFO zookeeper.ClientCnxn: Unable to read additional data from server sessionid 0x1513e0d973a0000, likely server has closed socket, closing socket connection and attempting reconnect
15/11/25 10:50:57 INFO server.ZooKeeperServer: shutting down
15/11/25 10:50:57 INFO server.ZooKeeperServer: shutting down
15/11/25 10:50:57 INFO server.SessionTrackerImpl: Shutting down
15/11/25 10:50:57 INFO server.SessionTrackerImpl: Shutting down
15/11/25 10:50:57 INFO server.PrepRequestProcessor: Shutting down
15/11/25 10:50:57 INFO server.SyncRequestProcessor: Shutting down
15/11/25 10:50:57 INFO server.PrepRequestProcessor: Shutting down
15/11/25 10:50:57 INFO server.SyncRequestProcessor: SyncRequestProcessor exited!
15/11/25 10:50:57 INFO server.PrepRequestProcessor: PrepRequestProcessor exited loop!
15/11/25 10:50:57 INFO server.FinalRequestProcessor: shutdown of request processor complete
15/11/25 10:50:57 INFO server.SyncRequestProcessor: Shutting down
15/11/25 10:50:57 INFO server.FinalRequestProcessor: shutdown of request processor complete
15/11/25 10:50:57 INFO zkclient.ZkClient: zookeeper state changed (Disconnected)
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.965 sec
Running com.telefonica.iot.cygnus.sinks.OrionMongoBaseSinkTest
Testing OrionMongoBaseSink.start
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Startup completed
Testing OrionMongosink.configure
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.249 sec
Running com.telefonica.iot.cygnus.sinks.OrionMongoSinkTest
Testing OrionMongoSink.processContextResponses (single destination and fiware-servicePath
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Persisting data at OrionMongoSink. Database: test_vehicles, Collection: test_/4wheels_car1-car, Data: 123456,1970-01-02T10:17:36.789Z,car1,car,speed,float,112.9,[]
Testing OrionCKANSinkTest.processContextResponses (multiple destinations and fiware-servicePaths)
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Persisting data at OrionMongoSink. Database: test_vehicles, Collection: test_/4wheelsSport_sport1, Data: 1429535,1970-01-17T13:05:35.775Z,car1,car,speed,float,112.9,[]
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Persisting data at OrionMongoSink. Database: test_vehicles, Collection: test_/4wheelsUrban_urban1, Data: 1429535,1970-01-17T13:05:35.775Z,car2,car,speed,float,115.8,[]
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 sec
Running com.telefonica.iot.cygnus.sinks.OrionMySQLSinkTest
Testing OrionMySQLSink.start
15/11/25 10:50:58 INFO sinks.OrionMySQLSink: [null] Startup completed
Testing OrionMySQLSink.configure
Testing OrionMySQLSinkTest.persistBatch ("root" servicePath name)
15/11/25 10:50:58 INFO sinks.OrionMySQLSink: [null] Persisting data at OrionMySQLSink. Database (vehicles), Table (my_cars), Fields ((recvTimeTs,recvTime,fiwareservicepath,entityId,entityType,attrName,attrType,attrValue,attrMd)), Values (('123456789','1970-01-02T10:17:36.789Z','','car1','car','speed','float','112.9','[]'))
Testing OrionMySQLSinkTest.persistBatch (multiple destinations and fiware-servicePaths)
15/11/25 10:50:58 INFO sinks.OrionMySQLSink: [null] Persisting data at OrionMySQLSink. Database (vehicles), Table (cars_my_cars), Fields ((recvTimeTs,recvTime,fiwareservicepath,entityId,entityType,attrName,attrType,attrValue,attrMd)), Values (('123456789','1970-01-02T10:17:36.789Z','cars','car1','car','speed','float','112.9','[]'))
Testing OrionHDFSSinkTest.persist (null batches)
Testing OrionMySQLSinkTest.persistBatch (normal resource lengths)
15/11/25 10:50:58 INFO sinks.OrionMySQLSink: [null] Persisting data at OrionMySQLSink. Database (vehicles), Table (cars_my_cars), Fields ((recvTimeTs,recvTime,fiwareservicepath,entityId,entityType,attrName,attrType,attrValue,attrMd)), Values (('123456789','1970-01-02T10:17:36.789Z','cars','car1','car','speed','float','112.9','[]'))
Testing OrionMySQLSinkTest.persistBatch (too long service name)
Testing OrionMySQLSinkTest.persistBatch (too long servicePath name)
Testing OrionMySQLSinkTest.persistBatch (too long destination name)
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.059 sec
Running com.telefonica.iot.cygnus.sinks.OrionSTHSinkTest
Testing OrionSTHSink.processContextResponses
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Persisting data at OrionMongoSink. Database: test_vehicles, Collection: test_/4wheels_car1-car, Data: 123456,1970-01-02T10:17:36.789Z,car1,car,speed,float,112.9,[]
Testing OrionCKANSinkTest.processContextResponses (multiple destinations and fiware-servicePaths)
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Persisting data at OrionMongoSink. Database: test_vehicles, Collection: test_/4wheelsSport_sport1, Data: 1429535,1970-01-17T13:05:35.775Z,car1,car,speed,float,112.9,[]
15/11/25 10:50:58 INFO sinks.OrionMongoBaseSink: [null] Persisting data at OrionMongoSink. Database: test_vehicles, Collection: test_/4wheelsUrban_urban1, Data: 1429535,1970-01-17T13:05:35.775Z,car2,car,speed,float,115.8,[]
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 sec

Results :

Tests run: 60, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 22.475s
[INFO] Finished at: Wed Nov 25 10:50:59 CET 2015
[INFO] Final Memory: 9M/81M
[INFO] ------------------------------------------------------------------------
```