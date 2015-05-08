#OrionMySQLSink
##Functionality
`com.iot.telefonica.cygnus.sinks.OrionMySQLSink`, or simply `OrionMySQLSink` is a sink designed to persist NGSI-like context data events within a [MySQL server](https://www.mysql.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always [transformed](from_ngsi_events_to_flume_events.md) into internal Flume events at Cygnus sources thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. In the end, the information within these Flume events must be mapped into specific MySQL data structures.

###Mapping Flume events to HDFS data structures
MySQL organizes the data in databases that contain tables of data rows. Such organization is exploited by `OrionCKANSink` each time a Flume event is taken, by performing the following workflow:

1. The bytes within the event's body are parsed and a `NotifyContextRequest` object container is created.
2. A database called as the `fiware-service` header value within the event is created (if not existing yet).
3. The context responses/entities within the container are iterated, and a table called as the concatenation of `<fiware_servicePath>_<destination>` is created (if not yet existing) for each unit data.
4. The context attributes within each context response/entity are iterated, and a new data row (or rows) is inserted in the current table. The format for this row depends on the configured persistence mode:
    * `row`: A data row is added for each notified context attribute. This kind of row will always contain 8 fields:
        * `recvTimeTs`: UTC timestamp expressed in miliseconds.
        * `recvTime`: UTC timestamp in human-redable format ([ISO 6801](http://en.wikipedia.org/wiki/ISO_8601)).
        * `entityId`: Notified entity identifier.
        * `entityType`: Notified entity type.
        * `attrName`: Notified attribute name.
        * `attrType`: Notified attribute type.
        * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
        * `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
    * `column`: A single data row is added for all the notified context attributes. This kind of row will contain two fields per each entity's attribute (one for the value, called `<attrName>`, and other for the metadata, called `<attrName>_md`), plus an additional field about the reception time of the data (`recvTime`).

####Important notes regarding the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields data rows are inserted for each notified attribute. Nevertheless, the `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the table can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

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

Assuming `mysql_username=myuser` and `attr_persistence=row` as configuration parameters, then `OrionMySQLSink` will persist the data within the body as:

    $ mysql -u myuser -p
    Enter password: 
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    ...
    mysql> show databases;
    +-----------------------+
    | Database              |
    +-----------------------+
    | information_schema    |
    | vehicles              |
    | mysql                 |
    | test                  |
    +-----------------------+
    4 rows in set (0.05 sec)

    mysql> use vehicles;
    ...
    Database changed
    mysql> show tables;
    +--------------------+
    | Tables_in_vehicles |
    +--------------------+
    | 4wheels_car1_car   |
    +--------------------+
    1 row in set (0.00 sec)

    mysql> select * from 4wheels_car1_car;
    +------------+-----------------------------+----------+------------+-------------+-----------+-----------+--------+
    | recvTimeTs | recvTime                    | entityId | entityType | attrName    | attrType  | attrValue | attrMd |
    +------------+-----------------------------+----------+------------+-------------+-----------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124Z | car1     | car        |  speed      | float     | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124Z | car1     | car        |  oil_level  | float     | 74.6      | []     |
    +------------+-----------------------------+----------+------------+-------------+-----------+-----------+--------+
    2 row in set (0.00 sec)

If `attr_persistence=colum` then `OrionHDFSSink` will persist the data within the body as:

    $ mysql -u myuser -p
    Enter password: 
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    ...
    mysql> show databases;
    +-----------------------+
    | Database              |
    +-----------------------+
    | information_schema    |
    | vehicles              |
    | mysql                 |
    | test                  |
    +-----------------------+
    4 rows in set (0.05 sec)

    mysql> use vehicles;
    ...
    Database changed
    mysql> show tables;
    +--------------------+
    | Tables_in_vehicles |
    +--------------------+
    | 4wheels_car1_car   |
    +--------------------+
    1 row in set (0.00 sec)

    mysql> select * from 4wheels_car1_car;
    +-----------------------------+-------+----------+-----------+--------------+
    | recvTime                    | speed | speed_md | oil_level | oil_level_md |
    +-----------------------------+-------+----------+-----------+--------------+
    | 2015-04-20T12:13:22.41.124Z | 112.9 | []       |  74.6     | []           |
    +-----------------------------+-------+----------+-----------+--------------+
    1 row in set (0.00 sec)
    
NOTE: `mysql` is the MySQL CLI for querying the data.

##Configuration
`OrionMySQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionMySQLSink</i> |
| channel | yes | N/A |
| mysql_host | no | localhost | FQDN/IP address where the MySQL server runs |
| mysql_port | no | 3306 |
| mysql_username | yes | N/A |
| mysql_password | yes | N/A |
| attr_persistence | no | row | <i>row</i> or <i>column</i>

A configuration example could be:

    cygnusagent.sinks = mysql-sink
    cygnusagent.channels = mysql-channel
    ...
    cygnusagent.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.OrionMySQLSink
    cygnusagent.sinks.mysql-sink.channel = mysql-channel
    cygnusagent.sinks.mysql-sink.mysql_host = 192.168.80.34
    cygnusagent.sinks.mysql-sink.mysql_port = 3306
    cygnusagent.sinks.mysql-sink.mysq_username = myuser
    cygnusagent.sinks.mysql-sink.mysql_password = mypassword
    cygnusagent.sinks.mysql-sink.attr_persistence = column

## Use cases
Use `OrionMySQLSink` if you are looking for a database storage not growing so much in the mid-long term.

## Contact
Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**
