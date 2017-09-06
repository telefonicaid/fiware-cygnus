# <a name="top"></a>NGSIHDFSSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to HDFS data structures](#section1.2)
        * [HDFS paths naming conventions](#section1.2.1)
        * [Json row-like storing](#section1.2.2)
        * [Json column-like storing](#section1.2.3)
        * [CSV row-like storing](#section1.2.4)
        * [CSV column-like storing](#section1.2.5)
        * [Hive](#section1.2.6)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Path names](#section1.3.2)
        * [Json row-like storing](#section1.3.3)
        * [Json column-like storing](#section1.3.4)
        * [CSV row-like storing](#section1.3.5)
        * [CSV column-like storing](#section1.3.6)
        * [Hive storing](#section1.3.7)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the persistence mode](#section2.3.1)
        * [About the binary backend](#section2.3.2)
        * [About batching](#section2.3.3)
        * [About the encoding](#section2.3.4)
* [Programmers guide](#section3)
    * [`NGSIHDFSSink` class](#section3.1)
    * [OAuth2 authentication](#section3.2)
    * [Kerberos authentication](#section3.3)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIHDFSSink`, or simply `NGSIHDFSSink` is a sink designed to persist NGSI-like context data events within a [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html) deployment. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific HDFS data structures at the Cygnus sinks.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to HDFS data structures
[HDFS organizes](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html#The_File_System_Namespace) the data in folders containing big data files. Such organization is exploited by `NGSIHDFSSink` each time a `NGSIEvent` is going to be persisted.

[Top](#top)

#### <a name="section1.2.1"></a>HDFS paths naming conventions
Since the unique data model accepted for `NGSIHDFSSink` is per entity (see the [Configuration](#section2.1) section for more details), a HDFS folder:

    /user/<hdfs_userame>/<fiware-service>/<fiware-servicePath>/<destination>

is created (if not existing yet) for each notified entity, where `<hdfs_username>` is a configuration parameter about a HDFS superuser, `<fiware-service>` and `<fiware-servicePath>` are notified as Http headers (or defaulted in NGSIRestHandler), and `<destination>` is the `notified_entities`/`grouped_entities` header value (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSIEvent`.

Then, for each notified entity a file named `<destination>.txt` is created (if not yet existing) under the above directory. Again, `<destination>` is the `notified_entities`/`grouped_entities` header value (depending on using or not the grouping rules, see the [Configuration](#section2.1) section for more details) within the `NGSIEvent`.

Please observe HDFS folders and files follow the [Unix rules](https://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words) about allowed character set and path max length (255 characters). This leads to certain [encoding](#section2.3.4) is applied depending on the `enable_encoding` configuration parameter.

[Top](#top)

#### <a name="section1.2.2"></a>Json row-like storing
Regarding the specific data stored within the HDFS file, if `file_format` parameter is set to `json-row` (default storing mode) then the notified data is stored attribute by attribute, composing a Json document for each one of them. Each append contains the following fields:

* `recvTimeTs`: UTC timestamp expressed in miliseconds.
* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `attrName`: Notified attribute name.
* `attrType`: Notified attribute type.
* `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be JSON object or JSON array.
* `attrMd`: It contains a string serialization of the metadata array for the attribute in JSON (if the attribute hasn't metadata, an empty array `[]` is inserted).

[Top](#top)

#### <a name="section1.2.3"></a>Json column-like storing
Regarding the specific data stored within the HDFS file, if `file_format` parameter is set to `json-column` then a single Json document is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: The notified one or default one.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
* For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.

[Top](#top)

#### <a name="section1.2.4"></a>CSV row-like storing
Regarding the specific data stored within the HDFS file, if `file_format` parameter is set to `csv-row` then the notified data is stored attribute by attribute, composing a CSV record for each one of them. Each record contains the following fields:

* `recvTimeTs`: UTC timestamp expressed in miliseconds.
* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: Notified fiware-servicePath, or the default configured one if not notified.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `attrName`: Notified attribute name.
* `attrType`: Notified attribute type.
* `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 this can be a JSON object or JSON array.
* `attrMd`: In this case, the field does not contain the real metadata, but the name of the HDFS file storing such metadata. The reason to do this is the metadata may be an array of any length; each element within the array will be persisted as a single line in the metadata file containing the metadata's name, type and value, all of them separated by the ',' field separator. There will be a metadata file per each attribute under `/user/<hdfs_userame>/<fiware-service>/<fiware-servicePath>/<destination>_<attrName>_<attrType>/<destination>_<attrName>_<attrType>.txt`

[Top](#top)

#### <a name="section1.2.5"></a>CSV column-like storing
Regarding the specific data stored within the HDFS file, if `file_format` parameter is set to `csv-column` then a single CSV record is composed for the whole notified entity, containing the following fields:

* `recvTime`: UTC timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)).
* `fiwareServicePath`: The notified one or default one.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
* For each notified attribute, a field named as the concatenation of the attribute name and `_md` is considered. This field will store the attribute's metadata values along the time.

[Top](#top)

#### <a name="section1.2.6"></a>Hive
A special feature regarding HDFS persisted data is the possibility to exploit it through Hive, a SQL-like querying system. `NGSIHDFSSink` automatically [creates a Hive external table](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-Create/Drop/TruncateTable) (similar to a SQL table) for each persisted entity in the default database, being the name for such tables as `<username>_<fiware-service>_<fiware-servicePath>_<destination>_[row|column]`.

The fields regarding each data row match the fields of the JSON documents/CSV records appended to the HDFS files. In the case of JSON, they are deserialized by using a [JSON serde](https://github.com/rcongiu/Hive-JSON-Serde). In the case of CSV they are deserialized by the delimiter fields specified in the table creation.

[Top](#top)

### <a name="section1.3"></a>Example
#### <a name="section1.3.1"></a>`NGSIEvent`
Assuming the following `NGSIEvent` is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    ngsi-event={
        headers={
	         content-type=application/json,
	         timestamp=1429535775,
	         transactionId=1429535775-308-0000000000,
	         correlationId=1429535775-308-0000000000,
	         fiware-service=vehicles,
	         fiware-servicepath=/4wheels,
	         <grouping_rules_interceptor_headers>,
	         <name_mappings_interceptor_headers>
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

[Top](#top)

#### <a name="section1.3.2"></a>Path names
Assuming `hdfs_username=myuser` and `service_as_namespace=false` as configuration parameters, then `NGSIHDFSSink` will persist the data within the body in this file (old encoding):

    $ hadoop fs -cat /user/myuser/vehicles/4wheels/car1_car/car1_car.txt

Using the new encoding:

    $ hadoop fs -cat /user/myuser/vehicles/4wheels/car1xffffcar/car1xffffcar.txt

[Top](#top)

#### <a name="section1.3.3"></a>Json row-like storing
A pair of Json documents are appended to the above file, one per attribute:

```
{"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124Z","fiwareServicePath":"4wheels","entityId":"car1","entityType":"car","attrName":"speed","attrType":"float","attrValue":"112.9","attrMd":[]}
{"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124Z","fiwareServicePath":"4wheels","entityId":"car1","entityType":"car","attrName":"oil_level","attrType":"float","attrValue":"74.6","attrMd":[]}
```

[Top](#top)

#### <a name="section1.3.4"></a>Json column-like storing
A single Json document is appended to the above file, containing all the attributes:

```
{"recvTime":"2015-04-20T12:13:22.41.124Z","fiwareServicePath":"4wheels","entityId":"car1","entityType":"car","speed":"112.9","speed_md":[],"oil_level":"74.6","oil_level_md":[]}
```

[Top](#top)

#### <a name="section1.3.5"></a>CSV row-like storing
A pair of CSV records are appended to the above file, one per attribute:

```
1429535775,2015-04-20T12:13:22.41.124Z,4wheels,car1,car,speed,float,112.9,hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt
1429535775,2015-04-20T12:13:22.41.124Z,4wheels,car1,car,oil_level,float,74.6,hdfs:///user/myuser/vehicles/4wheels/car1_car_oil_level_float/car1_car_oil_level_float.txt
```

Please observe despite the metadata for the example above is empty, the metadata files are created anyway.

In the case the metadata for the `speed` attribute was, for instance:

    [
       {"name": "manufacturer", "type": "string", "value": "acme"},
       {"name": "installation_year", "type": "integer", "value": 2014}
    ]

then the `hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt` file content would be:

    1429535775,manufacturer,string,acme
    1429535775,installation_year,integer,2014

[Top](#top)

#### <a name="section1.3.6"></a>CSV column-like storing
A single CSV record is appended to the above file, containing all the attributes:

```
2015-04-20T12:13:22.41.124Z,112.9,4wheels,car1,car,hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt,74.6,hdfs:///user/myuser/vehicles/4wheels/car1_car_oil_level_float/car1_car_oil_level_float.txt}
```

Please observe despite the metadata for the example above is empty, the metadata files are created anyway.

In the case the metadata for the `speed` attribute was, for instance:

    [
       {"name": "manufacturer", "type": "string", "value": "acme"},
       {"name": "installation_year", "type": "integer", "value": 2014}
    ]

then the `hdfs:///user/myuser/vehicles/4wheels/car1_car_speed_float/car1_car_speed_float.txt` file content would be:

    1429535775,manufacturer,string,acme
    1429535775,installation_year,integer,2014

[Top](#top)

#### <a name="section1.3.7"></a>Hive storing
With respect to Hive, the content of the tables in the `json-row`, `json-column`, `csv-row` and `csv-column` modes, respectively, is:

    $ hive
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

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIHDFSSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIHDFSSink</i> |
| channel | yes | N/A ||
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_grouping | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_grouping_interceptor.md) for more details. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| data\_model | no | dm-by-entity |  Always <i>dm-by-entity</i>, even if not configured. |
| file\_format | no | json-row | <i>json-row</i>, <i>json-column</i>, <i>csv-row</i> or <i>json-column</i>. |
| backend.impl | no | rest | <i>rest</i>, if a WebHDFS/HttpFS-based implementation is used when interacting with HDFS; or <i>binary</i>, if a Hadoop API-based implementation is used when interacting with HDFS. |
| backend.max\_conns | no | 500 | Maximum number of connections allowed for a Http-based HDFS backend. Ignored if using a binary backend implementation. |
| backend.max\_conns\_per\_route | no | 100 | Maximum number of connections per route allowed for a Http-based HDFS backend. Ignored if using a binary backend implementation. |
| hdfs\_host | no | localhost | FQDN/IP address where HDFS Namenode runs, or comma-separated list of FQDN/IP addresses where HDFS HA Namenodes run. |
| hdfs\_port | no | 14000 | <i>14000</i> if using HttpFS (rest), <i>50070</i> if using WebHDFS (rest), <i>8020</i> if using the Hadoop API (binary). |
| hdfs\_username | yes | N/A | If `service_as_namespace=false` then it must be an already existent user in HDFS. If `service_as_namespace=true` then it must be a HDFS superuser. |
| hdfs\_password | yes | N/A | Password for the above `hdfs_username`; this is only required for Hive authentication. |
| oauth2\_token | yes | N/A | OAuth2 token required for the HDFS authentication. |
| service\_as\_namespace | no | false | If configured as <i>true</i> then the `fiware-service` (or the default one) is used as the HDFS namespace instead of `hdfs_username`, which in this case must be a HDFS superuser. |
| csv\_separator | no | , ||
| batch\_size | no | 1 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| batch\_retry\_intervals | no | 5000 | Comma-separated list of intervals (in miliseconds) at which the retries regarding not persisted batches will be done. First retry will be done as many miliseconds after as the first value, then the second retry will be done as many miliseconds after as second value, and so on. If the batch\_ttl is greater than the number of intervals, the last interval is repeated. |
| hive | no | true | <i>true</i> or <i>false</i>. |
| hive.server\_version | no | 2 | `1` if the remote Hive server runs HiveServer1 or `2` if the remote Hive server runs HiveServer2. |
| hive.host | no | localhost ||
| hive.port | no | 10000 ||
| hive.db\_type | no | default-db | <i>default-db</i> or <i>namespace-db</i>. If `hive.db_type=default-db` then the default Hive database is used. If `hive.db_type=namespace-db` and `service_as_namespace=false` then the `hdfs_username` is used as Hive database. If `hive.db_type=namespace-db` and `service_as_namespace=true` then the notified fiware-service is used as Hive database. |
| krb5\_auth | no | false | <i>true</i> or <i>false</i>. |
| krb5\_user | yes | <i>empty</i> | Ignored if `krb5_auth=false`, mandatory otherwise. |
| krb5\_password | yes | <i>empty</i> | Ignored if `krb5_auth=false`, mandatory otherwise. |
| krb5\_login\_conf\_file | no | /usr/cygnus/conf/krb5_login.conf | Ignored if `krb5_auth=false`. |
| krb5\_conf\_file | no | /usr/cygnus/conf/krb5.conf | Ignored if `krb5_auth=false`. |

A configuration example could be:

    cygnus-ngsi.sinks = hdfs-sink
    cygnus-ngsi.channels = hdfs-channel
    ...
    cygnus-ngsi.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.NGSIHDFSSink
    cygnus-ngsi.sinks.hdfs-sink.channel = hdfs-channel
    cygnus-ngsi.sinks.hdfs-sink.enable_encoding = false
    cygnus-ngsi.sinks.hdfs-sink.enable_grouping = false
    cygnus-ngsi.sinks.hdfs-sink.enable_lowercase = false
    cygnus-ngsi.sinks.hdfs-sink.enable_name_mappings = false
    cygnus-ngsi.sinks.hdfs-sink.data_model = dm-by-entity
    cygnus-ngsi.sinks.hdfs-sink.file_format = json-column
    cygnus-ngsi.sinks.hdfs-sink.backend.impl = rest
    cygnus-ngsi.sinks.hdfs-sink.backend.max_conns = 500
    cygnus-ngsi.sinks.hdfs-sink.backend.max_conns_per_route = 100
    cygnus-ngsi.sinks.hdfs-sink.hdfs_host = 192.168.80.34
    cygnus-ngsi.sinks.hdfs-sink.hdfs_port = 14000
    cygnus-ngsi.sinks.hdfs-sink.hdfs_username = myuser
    cygnus-ngsi.sinks.hdfs-sink.hdfs_password = mypassword
    cygnus-ngsi.sinks.hdfs-sink.oauth2_token = mytoken
    cygnus-ngsi.sinks.hdfs-sink.service_as_namespace = false
    cygnus-ngsi.sinks.hdfs-sink.batch_size = 100
    cygnus-ngsi.sinks.hdfs-sink.batch_timeout = 30
    cygnus-ngsi.sinks.hdfs-sink.batch_ttl = 10
    cygnus-ngsi.sinks.hdfs-sink.batch_retry_intervals = 5000
    cygnus-ngsi.sinks.hdfs-sink.hive = false
    cygnus-ngsi.sinks.hdfs-sink.krb5_auth = false

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIHDFSSink` if you are looking for a JSON or CSV-based document storage growing in the mid-long-term in estimated sizes of terabytes for future trending discovery, along the time persistent patterns of behaviour and so on.

For a short-term historic, those required by dashboards and charting user interfaces, other backends are more suited such as MongoDB, STH Comet or MySQL (Cygnus provides sinks for them, as well).

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `*-row` persistence mode, since fixed 8-fields JSON/CSV documents are appended for each notified attribute. Nevertheless, the `*-column` mode may be affected by several JSON documents/CSV records of different lengths (in term of fields). Thus, the `*-column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

[Top](#top)

#### <a name="section2.3.2"></a>About the binary backend
Current implementation of the HDFS binary backend does not support any authentication mechanism.

A desirable authentication method would be OAuth2, since it is the standard in FIWARE, but this is not currently supported by the remote RPC server the binary backend accesses.

Valid authentication mechanism are Kerberos and Hadoop Delegation Token, nevertheless none has been used and the backend simply requires a username (the one configured in `hdfs_username`) in order the `cygnus` user (the one running Cygnus) impersonates it.

Thus, it is not recommended to use this backend in multi-user environment, or at least not without accepting the risk any user may impersonate any other one by simply specifying his/her username.

There exists an [issue](https://github.com/telefonicaid/fiware-cosmos/issues/111) about adding OAuth2 support to the Hadoop RPC mechanism, in the context of the [`fiware-cosmos`](https://github.com/telefonicaid/fiware-cosmos) project.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `NGSIHDFSSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 `NGSIEvent`s. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same HDFS file. If processing the events one by one, we would need 100 writes to HDFS; nevertheless, in this example only one write is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination HDFS file. In the worst case, the whole 100 entities will be about 100 different entities (100 different HDFS destinations), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 writes of the event by event approach with only 10-15 writes.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIHDFSSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

[Top](#top)

#### <a name="section2.3.4"></a>About the encoding
Until version 1.2.0 (included), Cygnus applied a very simple encoding:

* All non alphanumeric characters were replaced by underscore, `_`.
* The underscore was used as concatenator character as well.
* The slash, `/`, in the FIWARE service paths is ignored.

From version 1.3.0 (included), Cygnus applies this specific encoding tailored to HDFS data structures:

* Alphanumeric characters are not encoded.
* Numeric characters are not encoded.
* Equals character, `=`, is encoded as `xffff`.
* User defined strings composed of a `x` character and a [Unicode](http://unicode-table.com) are encoded as `xx` followed by the Unicode.
* Slash character, `/`, is encoded as `x002f`.
* All the other characters are not encoded.
* `xffff` is used as concatenator character.

Despite the old encoding will be deprecated in the future, it is possible to switch the encoding type through the `enable_encoding` parameter as explained in the [configuration](#section2.1) section.

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIHDFSSink` class
As any other NGSI-like sink, `NGSIHDFSSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the HDFS file where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `HDFSBackend` implementation (binary or rest).

    public void start();

An implementation of `HDFSBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIHDFSSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>OAuth2 authentication
[OAuth2](http://oauth.net/2/) is the evolution of the OAuth protocol, an open standard for authorization. Using OAuth, client applications can access in a secure way certain server resources on behalf of the resource owner, and the best, without sharing their credentials with the service. This works because of a trusted authorization service in charge of emitting some pieces of security information: the access tokens. Once requested, the access token is attached to the service request in order the server may ask the authorization service for the validity of the user requesting the access (authentication) and the availability of the resource itself for this user (authorization).

A detailed architecture of OAuth2 can be found [here](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/PEP_Proxy_-_Wilma_-_Installation_and_Administration_Guide), but in a nutshell, FIWARE implements the above concept through the Identity Manager GE ([Keyrock](http://catalogue.fiware.org/enablers/identity-management-keyrock) implementation) and the Access Control ([AuthZForce](http://catalogue.fiware.org/enablers/authorization-pdp-authzforce) implementation); the join of this two enablers conform the OAuth2-based authorization service in FIWARE:

* Access tokens are requested to the Identity Manager, which is asked by the final service for authentication purposes once the tokens are received. Please observe by asking this the service not only discover who is the real FIWARE user behind the request, but the service has full certainty the user is who he/she says to be.
* At the same time, the Identity Manager relies on the Access Control for authorization purposes. The access token gives, in addition to the real identity of the user, his/her roles according to the requested resource. The Access Control owns a list of policies regarding who is allowed to access all the resources based on the user roles.

This is important for Cygnus since HDFS (big) data can be accessed through the native [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html) RESTful API. And it may be protected with the above mentioned mechanism. If that's the case, simply ask for an access token and add it to the configuration through `cygnus-ngsi.sinks.hdfs-sink.oauth2_token` parameter.

In order to get an access token, do the following request to your OAuth2 tokens provider; in FIWARE Lab this is `cosmos.lab.fi-ware.org:13000`:

    $ curl -X POST "http://computing.cosmos.lab.fiware.org:13000/cosmos-auth/v1/token" -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=password&username=frb@tid.es&password=xxxxxxxx”
    {"access_token": "qjHPUcnW6leYAqr3Xw34DWLQlja0Ix", "token_type": "Bearer", "expires_in": 3600, "refresh_token": “V2Wlk7aFCnElKlW9BOmRzGhBtqgR2z"}

As you can see, your FIWARE Lab credentials are required in the payload, in the form of a password-based grant type (this will be the only time you have to give them).

[Top](#top)

### <a name="section3.3"></a>Kerberos authentication
Hadoop Distributed File System (HDFS) can be remotely managed through a REST API called [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html). This API may be used without any kind of security (in this case, it is enough knowing a valid HDFS user name in order to access this user HDFS space), or a Kerberos infrastructure may be used for authenticating the users.

[Kerberos](http://web.mit.edu/kerberos/) is an authentication protocol created by MIT, current version is 5. It is based in symmetric key cryptography and a trusted third party, the Kerberos servers themselves. The protocol is as easy as authenticating to the Authentication Server (AS), which forwards the user to the Key Distribution Center (KDC) with a ticket-granting ticket (TGT) that can be used to retrieve the definitive client-to-server ticket. This ticket can then be used for authentication purposes against a service server (in both directions).

SPNEGO is a mechanism used to negotiate the choice of security technology. Through SPNEGO both client and server may negotiate the usage of Kerberos as authentication technology.   

Kerberos authentication in HDFS is easy to achieve from the command line if the Kerberos 5 client is installed and the user already exists as a principal in the Kerberos infrastructure. Then just get a valid ticket and use the `--negotiate` option in `curl`:

    $ kinit <USER>
    Password for <USER>@<REALM>:
    $ curl -i --negotiate -u:<USER> "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=..."

Nevertheless, Cygnus needs this process to be automated. Let's see how through the configuration.

[Top](#top)

#### `conf/cygnus.conf`
This file can be built from the distributed `conf/cygnus.conf.template`. Edit appropriately this part of the `NGSIHDFSSink` configuration:

    # Kerberos-based authentication enabling
    cygnus-ngsi.sinks.hdfs-sink.krb5_auth = true
    # Kerberos username
    cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
    # Kerberos password
    cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
    # Kerberos login file
    cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_login_file = /usr/cygnus/conf/krb5_login.conf
    # Kerberos configuration file
    cygnus-ngsi.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

I.e. start enabling (or not) the Kerberos authentication. Then, configure a user with an already registered Kerberos principal, and its password. Finally, specify the location of two special Kerberos files.

[Top](#top)

#### `conf/krb5_login.conf`

Contains the following line, which must not be changed (thus, the distributed file is not a template but the definitive one).

    cygnus_krb5_login {
        com.sun.security.auth.module.Krb5LoginModule required doNotPrompt=false debug=true useTicketCache=false;
    };

[Top](#top)

#### `conf/krb5.conf`

This file can be built from the distributed `conf/krb5.conf.template`. Edit it appropriately, basically by replacing `EXAMPLE.COM` by your Kerberos realm (this is the same than your domain, but uppercase, i.e. the realm for `example.com` is `EXAMPLE.COM`) and by configuring your Kerberos Key Distribution Center (KDC) and your Kerberos admin/authentication server (ask your network administrator in order to know them).

    [libdefaults]
     default_realm = EXAMPLE.COM
     dns_lookup_realm = false
     dns_lookup_kdc = false
     ticket_lifetime = 24h
     renew_lifetime = 7d
     forwardable = true

    [realms]
     EXAMPLE.COM = {
      kdc = kdc.example.com
      admin_server = admin_server.example.com
     }

    [domain_realms]
     .example.com = EXAMPLE.COM
     example.com = EXAMPLE.COM

[Top](#top)
