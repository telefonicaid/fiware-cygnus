# <a name="top"></a>Testing
Content:

* [Unit testing](#section1)

## <a name="section1"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus/cygnus-twitter
    $ mvn test
    
You should get an output similat to the following one:

```
$ mvn test
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building cygnus-twitter 1.0.0_SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ cygnus-twitter ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/username/devel/fiware-cygnus/cygnus-twitter/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ cygnus-twitter ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ cygnus-twitter ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/username/devel/fiware-cygnus/cygnus-twitter/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ cygnus-twitter ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ cygnus-twitter ---
[INFO] Surefire report directory: /home/username/devel/fiware-cygnus/cygnus-twitter/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.telefonica.iot.cygnus.sinks.TwitterHDFSSinkTest
[TwitterHDFSSinkTest.start] --------------------- Start HDFS sink.
16/06/01 13:17:24 INFO http.HttpClientFactory: Setting max total connections (500)
16/06/01 13:17:24 INFO http.HttpClientFactory: Setting default max connections per route (100)
16/06/01 13:17:24 INFO sinks.TwitterSink: [null] Startup completed
[TwitterHDFSSinkTest.start] --------------  OK  - HDFS sink started.
[TwitterHDFSSinkTest.configure] ----------------- Configure HDFS parameters.
[TwitterHDFSSinkTest.configure] ----------  OK  - HDFS parameters detected in context.
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.512 sec
Running com.telefonica.iot.cygnus.sources.TwitterSourceTest
[TwitterSourceTest.basic] ----------------------- Start source.
16/06/01 13:17:24 INFO sources.TwitterSource: Consumer Key:        'iAtYJ4HpUVfIUoNnif1DA'
16/06/01 13:17:24 INFO sources.TwitterSource: Consumer Secret:     '172fOpzuZoYzNYaU3mMYvE8m8MEyLbztOdbrUolU'
16/06/01 13:17:24 INFO sources.TwitterSource: Access Token:        'zxcvbnm'
16/06/01 13:17:24 INFO sources.TwitterSource: Access Token Secret: '1234567890'
16/06/01 13:17:24 INFO sources.TwitterSource: South-West coordinate: 'null null'
16/06/01 13:17:24 INFO sources.TwitterSource: North-East coordinate: 'null null'
16/06/01 13:17:24 INFO sources.TwitterSource: Keywords:            'null'
16/06/01 13:17:24 INFO sources.TwitterSource: Starting twitter source com.telefonica.iot.cygnus.sources.TwitterSource{name:null,state:IDLE} ...
16/06/01 13:17:24 INFO sources.TwitterSource: Twitter source null started.
16/06/01 13:17:24 INFO sources.TwitterSource: Twitter source null stopping...
16/06/01 13:17:24 INFO sources.TwitterSource: Twitter source null stopped.
[TwitterSourceTest.basic] ----------------  OK  - Twitter source started properly.
[TwitterSourceTest.configure] ------------------- Configure Twitter parameters.
16/06/01 13:17:24 INFO sources.TwitterSource: Consumer Key:        'iAtYJ4HpUVfIUoNnif1DA'
16/06/01 13:17:24 INFO sources.TwitterSource: Consumer Secret:     '172fOpzuZoYzNYaU3mMYvE8m8MEyLbztOdbrUolU'
16/06/01 13:17:24 INFO sources.TwitterSource: Access Token:        'zxcvbnm'
16/06/01 13:17:24 INFO sources.TwitterSource: Access Token Secret: '1234567890'
16/06/01 13:17:24 INFO sources.TwitterSource: South-West coordinate: '40.748433 -73.985656'
16/06/01 13:17:24 INFO sources.TwitterSource: North-East coordinate: '40.758611 -73.979167'
16/06/01 13:17:24 INFO sources.TwitterSource: Keywords:            'keywords, more_keywords'
16/06/01 13:17:24 INFO sources.TwitterSource: Coordinates:         '-73.985656 40.748433 -73.979167 40.758611'
16/06/01 13:17:24 INFO sources.TwitterSource: keywords:            [keywords, more_keywords]
[TwitterSourceTest.configure] ------------  OK  - Twitter parameters detected in context.
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.573 sec

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.279 s
[INFO] Finished at: 2016-06-01T13:17:24+02:00
[INFO] Final Memory: 9M/81M
[INFO] ------------------------------------------------------------------------
```

[Top](#top)
