#<a name="top"></a>OrionHDFSSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to HDFS data structures](#section1.2)
    * [Hive](#section1.3)
    * [Example](#section1.4)
* [User guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the persistence mode](#section2.3.1)
        * [About the binary backend](#section2.3.2)
        * [About batching](#section2.3.3)
* [Programmers guide](#section3)
    * [`OrionHDFSSink` class](#section3.1)
    * [`HDFSBackendImpl` class](#section3.2)
    * [Authentication and authorization](#section3.3)
* [Reporting issues and contact information](#section4)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionHDFSSink`, or simply `OrionHDFSSink` is a sink designed to persist NGSI-like context data events within a [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html) deployment. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific HDFS data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

Since this is a common task done by Cygnus independently of the final backend, it is documented in [this](from_ngsi_events_to_flume_events.md) other independent document.

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to HDFS data structures
[HDFS organizes](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html#The_File_System_Namespace) the data in folders containinig big data files. Such organization is exploited by `OrionHDFSSink` each time a Flume event is going to be persisted.

According to the [naming conventions](naming_conventions.md), a folder named `/user/<hdfs_userame>/<fiware-service>/<fiware-servicePath>/<destination>` is created (if not existing yet), where `<hdfs_username>` is a configuration parameter, and `<fiware_service>`, `<fiware-servicePath>` and `<destination>` values are got from the event headers.

Then, the context responses/entities within the event body are iterated, and a file named `<destination>.txt` is created (if not yet existing) under the above directory, where `<destination>` value is got from the event headers.

The context attributes within each context response/entity are iterated, and a one or more lines are appended to the current file. The format for this append depends on the configured persistence mode:

* `json-row`: A JSON line is added for each notified context attribute. This kind of line will always contain 8 fields:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `servicePath`: Notified fiware-servicePath, or the default configured one if not notified.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be JSON object or JSON array.
    * `attrMd`: It contains a string serialization of the metadata array for the attribute in JSON (if the attribute hasn't metadata, an empty array `[]` is inserted).
* `json-column`: A single JSON line is added for all the notified context attributes. This kind of line will contain two fields per each entity's attribute (one for the value, named `<attrName>`, and another for the metadata, named `<attrName>_md`), plus four additional fields:
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `servicePath`: The notified one or default one.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
* `csv-row`: A CSV line is added for each notified context attribute. As `json-row`, this kind of line will always contain 8 fields:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `servicePath`: Notified fiware-servicePath, or the default configured one if not notified.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 this can be a JSON object or JSON array.
    * `attrMd`: In this case, the field does not contain the real metadata, but the name of the HDFS file storing such metadata. The reason to do this is the metadata may be an array of any length; each element within the array will be persisted as a single line in the metadata file containing the metadata's name, type and value, all of them separated by the ',' field sepator. There will be a metadata file per each attribute under `/user/<hdfs_userame>/<fiware-service>/<fiware-servicePath>/<destination>_<attrName>_<attrType>/<destination>_<attrName>_<attrType>.txt`
* `csv-column`: A single CSV line is added for all the notified context attributes. This kind of line will contain two fields per each entity's attribute (one for the value, named `<attrName>`, and another for the metadata, named `<attrName>_md_file` and containing the name of the HDFS file storing such metadata as explained above), plus four additional fields:
    * `recvTime`: UTC timestamp in human-redable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
    * `servicePath`: The notified one or default one.
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.

[Top](#top)

###<a name="section1.3"></a>Hive
A special feature regarding HDFS persisted data is the posssibility to exploit it through Hive, a SQL-like querying system. `OrionHDFSSink` automatically [creates a Hive external table](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-Create/Drop/TruncateTable) (similar to a SQL table) for each persisted entity in the default database, being the name for such tables as `<username>_<fiware-service>_<fiware-servicePath>_<destination>_[row|column]`.

The fields regarding each data row match the fields of the JSON documents/CSV records appended to the HDFS files. In the case of JSON, they are deserialized by using a [JSON serde](https://github.com/rcongiu/Hive-JSON-Serde). In the case of CSV they are deserialized by the delimiter fields specified in the table creation.

[Top](#top)

###<a name="section1.4"></a>Example
Assuming the following Flume event is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	        content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         ttl=10,
	         notified-service=vehicles,
	         notified-servicepath=4wheels,
	         default-destination=car1_car
	         default-servicepaths=4wheels
	         grouped-destination=car1_car
	         grouped-servicepath=4wheels
        },
        body={
	        entityId=car1,
	        entityType=car,
	        attributes=[
	            {
	                attrName=speed,
	                attrType=float,
	                attrValue=112.9
	            },
	            {
	                attrName=oil_level,
	                attrType=float,
	                attrValue=74.6
	            }
	        ]
	    }
    }

Assuming `batch_size=1`, `hdfs_username=myuser`, `service_as_namespace=false` and `file_format=json-row` as configuration parameters, then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myuser/vehicles/4wheels/car1_car/car1_car.txt
    {"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124Z","fiware-servicePath":"4wheels","entityId":"car1","entityType":"car","attrName":"speed","attrType":"float","attrValue":"112.9","attrMd":[]}
    {"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124Z","fiware-servicePath":"4wheels","entityId":"car1","entityType":"car","attrName":"oil_level","attrType":"float","attrValue":"74.6","attrMd":[]}

If `file_format=json-colum` then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myser/vehicles/4wheels/car1_car/car1_car.txt
    {"recvTime":"2015-04-20T12:13:22.41.124Z","fiware-servicePath":"4wheels","entityId":"car1","entityType":"car","speed":"112.9","speed_md":[],"oil_level":"74.6","oil_level_md":[]}
    
If `file_format=csv-row` then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myuser/vehicles/4wheels/car1_car/car1_car.txt
    1429535775,2015-04-20T12:13:22.41.124Z,4wheels,car1,car,speed,float,112.9,hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt
    1429535775,2015-04-20T12:13:22.41.124Z,4wheels,car1,car,oil_level,float,74.6,hdfs:///user/myuser/vehicles/4wheels/car1_car_oil_level_float/car1_car_oil_level_float.txt

If `file_format=csv-column` then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myser/vehicles/4wheels/car1_car/car1_car.txt
    2015-04-20T12:13:22.41.124Z,112.9,4wheels,car1,car,hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt,74.6,hdfs:///user/myuser/vehicles/4wheels/car1_car_oil_level_float/car1_car_oil_level_float.txt}
    
NOTE: `hadoop fs -cat` is the HDFS equivalent to the Unix command `cat`.
    
Please observe despite the metadata for the example above is empty, the metadata files are created anyway.

In the case the metadata for the `speed` attribute was, for instance:

    [
       {"name": "manufacturer", "type": "string", "value": "acme"},
       {"name": "installation_year", "type": "integer", "value": 2014}
    ]
    
then the `hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt` file content would be:

    1429535775,manufacturer,string,acme
    1429535775,installation_year,integer,2014

With respect to Hive, the content of the tables in the `json-row`, `json-column`, `csv-row` and `csv-column` modes, respectively, is:

    $ hive
    Logging initialized using configuration in jar:file:/usr/local/hive-0.9.0-shark-0.8.0-bin/lib/hive-common-0.9.0-shark-0.8.0.jar!/hive-log4j.properties
    Hive history file=/tmp/root/hive_job_log_root_201504201213_821987796.txt
    hive> select * from myuser_vehicles_4wheels_car1_car_row;
    OK
    1429535775	2015-04-20T12:13:22.41.124Z	4wheels	car1	car	speed		float	112.9	[]
    1429535775	2015-04-20T12:13:22.41.124Z	4wheels	car1	car	oil_level	float	74.6	[]
    hive> select * from myuser_vehicles_4wheels_car1_car_column;
    2015-04-20T12:13:22.41.124Z		4wheels	car1	car	112.9	[]	74.6	[]
    hive> select * from myuser_vehicles_4wheels_car1_car_row;
    OK
    1429535775	2015-04-20T12:13:22.41.124Z	4wheels	car1	car	speed		float	112.9	hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt
    1429535775	2015-04-20T12:13:22.41.124Z	car1	car	oil_level	float	74.6	hdfs:///user/myuser/vehicles/4wheels/car1_car_oil_level_float/car1_car_oil_level_float.txt
    hive> select * from myuser_vehicles_4wheels_car1_car_column;
    2015-04-20T12:13:22.41.124Z		4wheels	car1	car	112.9	hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt	74.6	hdfs:///user/myuser/vehicles/4wheels/car1_car_oil_level_float/car1_car_oil_level_float.txt

NOTE: `hive` is the Hive CLI for locally querying the data.

[Top](#top)

##<a name="section2"></a>User guide
###<a name="section2.1"></a>Configuration
`OrionHDFSSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionHDFSSink</i> |
| channel | yes | N/A |
| enable_grouping | no | false | <i>true</i> or <i>false</i> |
| backend_impl | no | rest | <i>rest</i>, if a WebHDFS/HttpFS-based implementation is used when interacting with HDFS; or <i>binary</i>, if a Hadoop API-based implementation is used when interacting with HDFS |
| hdfs_host | no | localhost | FQDN/IP address where HDFS Namenode runs, or comma-separated list of FQDN/IP addresses where HDFS HA Namenodes run |
| cosmos_host<br>(**deprecated**)| no | localhost | FQDN/IP address where HDFS Namenode runs, or comma-separated list of FQDN/IP addresses where HDFS HA Namenodes run.<br>Still usable; if both are configured, `hdfs_host` is preferred |
| hdfs_port | no | 14000 | <i>14000</i> if using HttpFS (rest), <i>50070</i> if using WebHDFS (rest), <i>8020</i> if using the Hadoop API (binary) |
| cosmos_port<br>(**deprecated**) | no | 14000 | <i>14000</i> if using HttpFS (rest), <i>50070</i> if using WebHDFS (rest), <i>8020</i> if using the Hadoop API (binary).<br>Still usable; if both are configured, `hdfs_port` is preferred |
| hdfs_username | yes | N/A | If `service_as_namespace=false` then it must be an already existent user in HDFS. If `service_as_namespace=true` then it must be a HDFS superuser |
| cosmos\_default\_username<br>(**deprecated**) | yes | N/A | If `service_as_namespace=false` then it must be an already existent user in HDFS. If `service_as_namespace=true` then it must be a HDFS superuser.<br>Still usable; if both are configured, `hdfs_username` is preferred |
| hdfs_password | yes | N/A | Password for the above `hdfs_username`/`cosmos_default_username`; this is only required for Hive authentication |
| oauth2_token | yes | N/A | OAuth2 token required for the HDFS authentication |
| service\_as\_namespace | no | false | If configured as <i>true</i> then the `fiware-service` (or the default one) is used as the HDFS namespace instead of `hdfs_username`/`cosmos_default_username`, which in this case must be a HDFS superuser |
| file_format | no | json-row | <i>json-row</i>, <i>json-column</i>, <i>csv-row</i> or <i>json-column</i>
| batch_size | no | 1 | Number of events accumulated before persistence |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is |
| hive\_server\_version | no | 2 | `1` if the remote Hive server runs HiveServer1 or `2` if the remote Hive server runs HiveServer2 |
| hive_host | no | localhost |
| hive_port | no | 10000 |
| krb5_auth | no | false |
| krb5_user | yes | <i>empty</i> | Ignored if `krb5_auth=false`, mandatory otherwise |
| krb5_password | yes | <i>empty</i> | Ignored if `krb5_auth=false`, mandatory otherwise |
| krb5\_login\_conf\_file | no | /usr/cygnus/conf/krb5_login.conf | Ignored if `krb5_auth=false` |
| krb5\_conf\_file | no | /usr/cygnus/conf/krb5.conf | Ignored if `krb5_auth=false` |

A configuration example could be:

    cygnusagent.sinks = hdfs-sink
    cygnusagent.channels = hdfs-channel
    ...
    cygnusagent.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.OrionHDFSSink
    cygnusagent.sinks.hdfs-sink.channel = hdfs-channel
    cygnusagent.sinks.hdfs-sink.enable_grouping = false
    cygnusagent.sinks.hdfs-sink.backend_impl = rest
    cygnusagent.sinks.hdfs-sink.hdfs_host = 192.168.80.34
    cygnusagent.sinks.hdfs-sink.hdfs_port = 14000
    cygnusagent.sinks.hdfs-sink.hdfs_username = myuser
    cygnusagent.sinks.hdfs-sink.hdfs_password = mypassword
    cygnusagent.sinks.hdfs-sink.oauth2_token = mytoken
    cygnusagent.sinks.hdfs-sink.file_format = json-column
    cygnusagent.sinks.hdfs-sink.batch_size = 100
    cygnusagent.sinks.hdfs-sink.batch_timeout = 30
    cygnusagent.sinks.hdfs-sink.hive_server_version = 2
    cygnusagent.sinks.hdfs-sink.hive_host = 192.168.80.35
    cygnusagent.sinks.hdfs-sink.hive_port = 10000
    cygnusagent.sinks.hdfs-sink.krb5_auth = false

[Top](#top)

###<a name="section2.2"></a>Use cases
Use `OrionHDFSSink` if you are looking for a JSON or CSV-based document storage growing in the mid-long-term in estimated sizes of terabytes for future trending discovery, along the time persistent patterns of behaviour and so on.

For a short-term historic, those required by dashboards and charting user interfaces, other backends are more suited such as MongoDB, STH or MySQL (Cygnus provides sinks for them, as well).

[Top](#top)

###<a name="section2.3"></a>Important notes

####<a name="section2.3.1"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `*-row` persistence mode, since fixed 8-fields JSON/CSV documents are appended for each notified attribute. Nevertheless, the `*-column` mode may be affected by several JSON documents/CSV records of different lengths (in term of fields). Thus, the `*-column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

[Top](#top)

####<a name="section2.3.2"></a>About the binary backend
Current implementation of the HDFS binary backend does not support any authentication mechanism.

A desirable authentication method would be OAuth2, since it is the standard in FIWARE, but this is not currenty supported by the remote RPC server the binary backend accesses.

Valid authentication mechanims are Kerberos and Hadoop Delegation Token, nevertheless none has been used and the backend simply requires a username (the one configured in `hdfs_username`) in order the `cygnus` user (the one running Cygnus) impersonates it.

Thus, it is not recommended to use this backend in multi-user environment, or at least not without accepting the risk any user may impersonate any other one by simply specifying his/her username.

There exists an [issue](https://github.com/telefonicaid/fiware-cosmos/issues/111) about adding OAuth2 support to the Hadoop RPC mechanism, in the context of the [`fiware-cosmos`](https://github.com/telefonicaid/fiware-cosmos) project.

[Top](#top)

####<a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `OrionHDFSSink` extends `OrionSink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows exteding classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same HDFS file. If processing the events one by one, we would need 100 writes to HDFS; nevertheless, in this example only one write is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination HDFS file. In the worst case, the whole 100 entities will be about 100 different entities (100 different HDFS destinations), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 writes of the event by event approach with only 10-15 writes.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `OrionHDFSSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](../operation/performance_tuning_tips.md).

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`OrionHDFSSink` class
As any other NGSI-like sink, `OrionHDFSSink` extends the base `OrionSink`. The methods that are extended are:

    void persistBatch(Batch defaultEvents, Batch groupedEvents) throws Exception;
    
A `Batch` contanins a set of `CygnusEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the HDFS file where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `HDFSBackend` implementation (binary or rest). There are two sets of events, default and grouped ones, because depending on the sink configuration the default or the grouped notified destination and fiware servicePath are used.
    
    public void start();

An implementation of `HDFSBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionHDFSSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section3.2"></a>`HDFSBackendImpl` class
This is a convenience backend class for HDFS that extends the `HttpBackend` abstract class (provides common logic for any Http connection-based backend) and implements the `HDFSBackend` interface (provides the methods that any HDFS backend must implement). Relevant methods are:

    public void createDir(String dirPath) throws Exception;
    
Creates a HDFS directory, given its path.
    
    public void createFile(String filePath, String data) throws Exception;
    
Creates a HDFS file, given its path, and writes initial data to it.
    
    public void append(String filePath, String data) throws Exception;
    
Appends new data to an already existent given HDFS file.

    public boolean exists(String filePath) throws Exception;
    
Checks if a HDFS file, given its path, exists ot not.
    
    public void provisionHiveTable(FileFormat fileFormat, String dirPath, String tag) throws Exception;
    
Provisions a Hive table with data stored using constant 8-fields. This is usually invoked for `*-row`-like mode storage within the given HDFS path. A tag can be added to the end of the table name (usually `_row`).
    
    public void provisionHiveTable(FileFormat fileFormat, String dirPath, String fields, String tag) throws Exception;
    
Provisions a Hive table with data stored using the given variable length fields. This is usually invoked for `*-column`-like mode storage within the given HDFS path. A tag can be added to the end of the table name (usually `_column`).

[Top](#top)

###<a name="section3.3"></a>Authentication and authorization
[OAuth2](http://oauth.net/2/) is the evolution of the OAuth protocol, an open standard for authorization. Using OAuth, client applications can access in a secure way certain server resources on behalf of the resource owner, and the best, without sharing their credentials with the service. This works because of a trusted authorization service in charge of emitting some pieces of security information: the access tokens. Once requested, the access token is attached to the service request in order the server may ask the authorization service for the validity of the user requesting the access (authentication) and the availability of the resource itself for this user (authorization).

A detailed architecture of OAuth2 can be found [here](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/PEP_Proxy_-_Wilma_-_Installation_and_Administration_Guide), but in a nutshell, FIWARE implements the above concept through the Identity Manager GE ([Keyrock](http://catalogue.fiware.org/enablers/identity-management-keyrock) implementation) and the Access Control ([AuthZForce](http://catalogue.fiware.org/enablers/authorization-pdp-authzforce) implementation); the join of this two enablers conform the OAuth2-based authorization service in FIWARE:

* Access tokens are requested to the Identity Manager, which is asked by the final service for authentication purposes once the tokens are received. Please observe by asking this the service not only discover who is the real FIWARE user behind the request, but the service has full certainty the user is who he/she says to be.
* At the same time, the Identity Manager relies on the Access Control for authorization purposes. The access token gives, in addition to the real identity of the user, his/her roles according to the requested resource. The Access Control owns a list of policies regarding who is allowed to access all the resources based on the user roles.

This is important for Cygnus since HDFS (big) data can be accessed through the native [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html) RESTful API. And it may be protected with the above mentioned mechanism. If that's the case, simply ask for an access token and add it to the configuration through `cygnusagent.sinks.hdfs-sink.oauth2_token` parameter.

In order to get an access token, do the following request to your OAuth2 tokens provider; in FIWARE Lab this is `cosmos.lab.fi-ware.org:13000`:

    $ curl -X POST "http://cosmos.lab.fi-ware.org:13000/cosmos-auth/v1/token" -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=password&username=frb@tid.es&password=xxxxxxxx”
    {"access_token": "qjHPUcnW6leYAqr3Xw34DWLQlja0Ix", "token_type": "Bearer", "expires_in": 3600, "refresh_token": “V2Wlk7aFCnElKlW9BOmRzGhBtqgR2z"}

As you can see, your FIWARE Lab credentials are required in the payload, in the form of a password-based grant type (this will be the only time you have to give them).

[Top](#top)

##<a name="section4"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](german.torodelvalle@telefonica.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.

[Top](#top)

