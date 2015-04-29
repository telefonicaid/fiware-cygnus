#OrionHDFSSink
##Functionality
`com.iot.telefonica.cygnus.sinks.OrionHDFSSink`, or simply `OrionHDFSSink` is a sink designed to persist NGSI-like context data events within a [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html) deployment. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always [transformed](from_ngsi_events_to_flume_events.md) into internal Flume events at Cygnus sources thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. In the end, the information within these Flume events must be mapped into specific HDFS data structures.

###Mapping Flume events to HDFS data structures
[HDFS organizes](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html#The_File_System_Namespace) the data in folders containinig big data files. Such organization is exploited by `OrionHDFSSink` each time a Flume event is taken, by performing the following workflow:

1. The bytes within the event's body are parsed and a `NotifyContextRequest` object container is created.
2. According to the [naming conventions](naming_convetions.md), a folder called `/user/<cosmos_default_userame>/<fiware-service>/<fiware-servicePath>/<destination>` is created (if not existing yet), where `<cosmos_default_username>` is a configuration parameter, and `<fiware_service>`, `<fiware-servicePath>` and `<destination>` values are got from the event headers.
3. The context responses/entities within the container are iterated, and a file called `<destination>.txt` is created (if not yet existing), where `<destination>` value is got from the event headers.
4. The context attributes within each context response/entity are iterated, and a new Json line (or lines) is appended to the current file. The format for this append depends on the configured persistence mode:
    * `row`: A Json line is added for each notified context attribute. This kind of line will always contain 8 fields:
        * `recvTimeTs`: UTC timestamp expressed in miliseconds.
        * `recvTime`: UTC timestamp in human-redable format ([ISO 6801](http://en.wikipedia.org/wiki/ISO_8601)).
        * `entityId`: Notified entity identifier.
        * `entityType`: Notified entity type.
        * `attrName`: Notified attribute name.
        * `attrType`: Notified attribute type.
        * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
        * `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
    * `column`: A single Json line is added for all the notified context attributes. This kind of line will contain two fields per each entity's attribute (one for the value, called `<attrName>`, and other for the metadata, called `<attrName>_md`), plus an additional field about the reception time of the data (`recvTime`).

####Important notes regarding the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields Json documents are appended for each notified attribute. Nevertheless, the `column` mode may be affected by several Json documents of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

###Hive
A special feature regarding HDFS persisted data is the posssibility to exploit it through Hive, a SQL-like querying system. `OrionHDFSSink` automatically [creates a Hive external table](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-Create/Drop/TruncateTable) (similar to a SQL table) for each persisted entity in the default database, being the name for such tables as `<username>_<fiware-service>_<fiware-servicePath>_<destination>_[row|column]`.

The fields regarding each data row match the fields of the Json documents appended to the HDFS files. They are deserialized by using a [Json serde](https://github.com/rcongiu/Hive-JSON-Serde).

###Example
Assuming the following Flume event is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	        content-type=application/json,
	        fiware-service=vehicles,
	        fiware-servicepath=4wheels,
	        timestamp=1429535775,
	        transactionId=1429535775-308-0000000000,
	        ttl=10,
	        destination=car1_car
        },
        body={
	        entityId=car1,
	        entityType=car,
	        attributes=[
	            {
	                attrName=speed,
	                attrType=kmh,
	                attrValue=112.9
	            },
	            {
	                attrName=oil_level,
	                attrType=percentage,
	                attrValue=74.6
	            }
	        ]
	    }
    }

Assuming `cosmos_default_username=myuser` and `attr_persistence=row` as configuration parameters, then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myuser/vehicles/4wheels/car1_car/car1_car.txt
    {"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124UTC","entityId":"car1","entityType":"car","attrName":"speed","attrType":"kmh","attrValue":"112.9","attrMd":[]}
    {"recvTimeTs":"1429535775","recvTime":"2015-04-20T12:13:22.41.124UTC","entityId":"car1","entityType":"car","attrName":"oil_level","attrType":"percentage","attrValue":"74.6","attrMd":[]}

If `attr_persistence=colum` then `OrionHDFSSink` will persist the data within the body as:

    $ hadoop fs -cat /user/myser/vehicles/4wheels/car1_car/car1_car.txt
    {"recvTime":"2015-04-20T12:13:22.41.124UTC","speed":"112.9","speed_md":[],"oil_level":"74.6","oil_level_md":[]}

NOTE: `hadoop fs -cat` is the HDFS equivalent to the Unix command `cat`.

With respect to Hive, the content of the tables in the `row` and `column` modes, respectively, is:

    $ hive
    Logging initialized using configuration in jar:file:/usr/local/hive-0.9.0-shark-0.8.0-bin/lib/hive-common-0.9.0-shark-0.8.0.jar!/hive-log4j.properties
    Hive history file=/tmp/root/hive_job_log_root_201504201213_821987796.txt
    hive> select * from myuser_vehicles_4wheels_car1_car_row;
    OK
    1429535775	2015-04-20T12:13:22.41.124UTC	car1	car	speed		kmh			112.9	[]
    1429535775	2015-04-20T12:13:22.41.124UTC	car1	car	oil_level	percentage	74.6	[]
    hive> select * from myuser_vehicles_4wheels_car1_car_column;
    2015-04-20T12:13:22.41.124UTC		112.9	[]	74.6	[]

NOTE: `hive` is the Hive CLI for locally querying the data.

##Configuration
`OrionHDFSSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionHDFSSink</i> |
| channel | yes | N/A |
| cosmos_host | no | localhost | FQDN/IP address where HDFS Namenode runs, or comma-separated list of FQDN/IP addresses where HDFS HA Namenodes run |
| cosmos_port | no | 14000 | <i>14000</i> if using HttpFS, <i>50070</i> if using WebHDFS |
| cosmos\_default\_username | yes | N/A |
| cosmos\_default\_password | yes | N/A |
| hdfs_api | no | httpfs | <i>httpfs</i> if using the HttpFS gateway or <i>webhdfs</i> if using the standard WebHDFS |
| attr_persistence | no | row | <i>row</i> or <i>column</i>
| hive_host | no | localhost |
| hive_port | no | 10000 |
| krb5_auth | no | false |
| krb5_user | yes | <i>empty</i> | Ignored if <i>krb5_auth=false</i>, mandatory otherwise |
| krb5_password | yes | <i>empty</i> | Ignored if <i>krb5_auth=false</i>, mandatory otherwise |
| krb5\_login\_conf\_file | no | /usr/cygnus/conf/krb5_login.conf | Ignored if <i>krb5_auth=false</i> |
| krb5\_conf\_file | no | /usr/cygnus/conf/krb5.conf | Ignored if <i>krb5_auth=false</i> |

A configuration example could be:

    cygnusagent.sinks = hdfs-sink
    cygnusagent.channels = hdfs-channel
    ...
    cygnusagent.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.OrionHDFSSink
    cygnusagent.sinks.hdfs-sink.channel = hdfs-channel
    cygnusagent.sinks.hdfs-sink.cosmos_host = 192.168.80.34
    cygnusagent.sinks.hdfs-sink.cosmos_port = 14000
    cygnusagent.sinks.hdfs-sink.cosmos_default_username = myuser
    cygnusagent.sinks.hdfs-sink.cosmos_default_password = mypassword
    cygnusagent.sinks.hdfs-sink.hdfs_api = httpfs
    cygnusagent.sinks.hdfs-sink.attr_persistence = column
    cygnusagent.sinks.hdfs-sink.hive_host = 192.168.80.35
    cygnusagent.sinks.hdfs-sink.hive_port = 10000
    cygnusagent.sinks.hdfs-sink.krb5_auth = false

## Use cases
Use `OrionHDFSSink` if you are looking for a Json-based document storage growing in the mid-long term.

## Contact
Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**
