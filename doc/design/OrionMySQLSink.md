#<a name="top"></a>OrionMySQLSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Mapping Flume events to MySQL data structures](#section1.2)
    * [Example](#section1.3)
* [User guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About the table type and its relation with the grouping rules](#section2.3.1)
        * [About the persistence mode](#section2.3.2)
        * [About batching](#section2.3.3)
* [Programmers guide](#section3)
    * [`OrionMySQLSink` class](#section3.1)
    * [`MySQLBackendImpl` class](#section3.2)
* [Reporting issues and contact information](#section4)

##<a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.OrionMySQLSink`, or simply `OrionMySQLSink` is a sink designed to persist NGSI-like context data events within a [MySQL server](https://www.mysql.com/). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal Flume events at Cygnus sources. In the end, the information within these Flume events must be mapped into specific MySQL data structures.

Next sections will explain this in detail.

[Top](#top)

###<a name="section1.1"></a>Mapping NGSI events to flume events
Notified NGSI events (containing context data) are transformed into Flume events (such an event is a mix of certain headers and a byte-based body), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the Cygnus Http listeners (in Flume jergon, sources) thanks to `com.iot.telefonica.cygnus.handlers.OrionRestHandler`. Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

Since this is a common task done by Cygnus independently of the final backend, it is documented in [this](from_ngsi_events_to_flume_events.md) other independent document.

[Top](#top)

###<a name="section1.2"></a>Mapping Flume events to MySQL data structures
MySQL organizes the data in databases that contain tables of data rows. Such organization is exploited by `OrionMySQLSink` each time a Flume event is going to be persisted.

According to the [naming conventions](naming_conventions.md), a database named as the `fiware-service` header value within the event is created (if not existing yet).

Then, the context responses/entities within the container are iterated, and a table is created (if not yet existing) within the above database whose name depends on the configured table type:

* `table-by-destination`. A table named as the concatenation of `<fiware_servicePath>_<destination>` is created (if not yet existing).
* `table-by-service-path`. A table named as the `<fiware-servicePath>` is created (if not yet existing). 

The context attributes within each context response/entity are iterated, and a new data row (or rows) is inserted in the current table. The format for this row depends on the configured persistence mode:

* `row`: A data row is added for each notified context attribute. This kind of row will always contain 8 fields:
    * `recvTimeTs`: UTC timestamp expressed in miliseconds.
    * `recvTime`: UTC timestamp in human-redable format ([ISO 6801](http://en.wikipedia.org/wiki/ISO_8601)).
    * `entityId`: Notified entity identifier.
    * `entityType`: Notified entity type.
    * `attrName`: Notified attribute name.
    * `attrType`: Notified attribute type.
    * `attrValue`: In its simplest form, this value is just a string, but since Orion 0.11.0 it can be Json object or Json array.
    * `attrMd`: It contains a string serialization of the metadata array for the attribute in Json (if the attribute hasn't metadata, an empty array `[]` is inserted).
* `column`: A single data row is added for all the notified context attributes. This kind of row will contain two fields per each entity's attribute (one for the value, named `<attrName>`, and other for the metadata, named `<attrName>_md`), plus an additional field about the reception time of the data (`recvTime`).

[Top](#top)

###<a name="section1.3"></a>Example
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

Assuming `mysql_username=myuser`, `table_type=table-by-destination` and `attr_persistence=row` as configuration parameters, then `OrionMySQLSink` will persist the data within the body as:

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
    +------------+----------------------------+----------+------------+-------------+-----------+-----------+--------+
    | recvTimeTs | recvTime                   | entityId | entityType | attrName    | attrType  | attrValue | attrMd |
    +------------+----------------------------+----------+------------+-------------+-----------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124 | car1     | car        |  speed      | float     | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124 | car1     | car        |  oil_level  | float     | 74.6      | []     |
    +------------+----------------------------+----------+------------+-------------+-----------+-----------+--------+
    2 row in set (0.00 sec)

If `table_type=table-by-destination` and `attr_persistence=colum` then `OrionMySQLSink` will persist the data within the body as:

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
    +----------------------------+-------+----------+-----------+--------------+
    | recvTime                   | speed | speed_md | oil_level | oil_level_md |
    +----------------------------+-------+----------+-----------+--------------+
    | 2015-04-20T12:13:22.41.124 | 112.9 | []       |  74.6     | []           |
    +----------------------------+-------+----------+-----------+--------------+
    1 row in set (0.00 sec)
    
If `table_type=table-by-service-path` and `attr_persistence=row` then `OrionMySQLSink` will persist the data within the body as:

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
    | 4wheels            |
    +--------------------+
    1 row in set (0.00 sec)

    mysql> select * from 4wheels;
    +------------+----------------------------+----------+------------+-------------+-----------+-----------+--------+
    | recvTimeTs | recvTime                   | entityId | entityType | attrName    | attrType  | attrValue | attrMd |
    +------------+----------------------------+----------+------------+-------------+-----------+-----------+--------+
    | 1429535775 | 2015-04-20T12:13:22.41.124 | car1     | car        |  speed      | float     | 112.9     | []     |
    | 1429535775 | 2015-04-20T12:13:22.41.124 | car1     | car        |  oil_level  | float     | 74.6      | []     |
    +------------+----------------------------+----------+------------+-------------+-----------+-----------+--------+
    2 row in set (0.00 sec)
    
If `table_type=table-by-service-path` and `attr_persistence=colum` then `OrionMySQLSink` will persist the data within the body as:

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
    | 4wheels            |
    +--------------------+
    1 row in set (0.00 sec)

    mysql> select * from 4wheels;
    +----------------------------+-------+----------+-----------+--------------+
    | recvTime                   | speed | speed_md | oil_level | oil_level_md |
    +----------------------------+-------+----------+-----------+--------------+
    | 2015-04-20T12:13:22.41.124 | 112.9 | []       |  74.6     | []           |
    +----------------------------+-------+----------+-----------+--------------+
    1 row in set (0.00 sec)
    
NOTES:

* `mysql` is the MySQL CLI for querying the data.
* Time zone information is not added in MySQL timestamps since MySQL stores that information as a environment variable. MySQL timestamps are stored in UTC time.

[Top](#top)

##<a name="section2"></a>User guide
###<a name="section2.1"></a>Configuration
`OrionMySQLSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.OrionMySQLSink</i> |
| channel | yes | N/A |
| enable_grouping | no | false | <i>true</i> or <i>false</i> |
| mysql_host | no | localhost | FQDN/IP address where the MySQL server runs |
| mysql_port | no | 3306 |
| mysql_username | yes | N/A |
| mysql_password | yes | N/A |
| table_type | no | table-by-destination | <i>table-by-destination</i> or <i>table-by-service-path</i> |
| attr_persistence | no | row | <i>row</i> or <i>column</i>

A configuration example could be:

    cygnusagent.sinks = mysql-sink
    cygnusagent.channels = mysql-channel
    ...
    cygnusagent.sinks.mysql-sink.type = com.telefonica.iot.cygnus.sinks.OrionMySQLSink
    cygnusagent.sinks.mysql-sink.channel = mysql-channel
    cygnusagent.sinks.mysql-sink.enable_grouping = false
    cygnusagent.sinks.mysql-sink.mysql_host = 192.168.80.34
    cygnusagent.sinks.mysql-sink.mysql_port = 3306
    cygnusagent.sinks.mysql-sink.mysq_username = myuser
    cygnusagent.sinks.mysql-sink.mysql_password = mypassword
    cygnusagent.sinks.mysql-sink.table_type = table-by-destination
    cygnusagent.sinks.mysql-sink.attr_persistence = column
    
[Top](#top)

###<a name="section2.2"></a>Use cases
Use `OrionMySQLSink` if you are looking for a database storage not growing so much in the mid-long term.

[Top](#top)

###<a name="section2.3"></a>Important notes
####<a name="section2.3.1"></a>About the table type and its relation with the grouping rules
The table type configuration parameter, as seen, is a method for <i>direct</i> aggregation of data: by <i>default</i> destination (i.e. all the notifications about the same entity will be stored within the same MySQL table) or by <i>default</i> service-path (i.e. all the notifications about the same service-path will be stored within the same MySQL table).

The [Grouping feature](./interceptors.md) is another aggregation mechanims, but an <i>inderect</i> one. This means the grouping feature does not really aggregates the data into a single table, that's something the sink will done based on the configured table type (see above), but modifies the default destination or service-path, causing the data is finally aggregated (or not) depending on the table type.

For instance, if the chosen table type is by destination and the grouping feature is not enabled then two different entities data, `car1` and `car2` both of type `car` will be persisted in two different MySQL tables, according to their <i>default</i> destination, i.e. `car1_car` and `car2_car`, respectively. However, if a grouping rule saying "all cars of type `car` will have a modified destination named `cars`" is enabled then both entities data will be persisted in a single table named `cars`. In this example, the direct aggregation is determined by the table type (by destination), but inderectly we have been deciding the aggregation as well through a grouping rule.

[Top](#top)

####<a name="section2.3.2"></a>About the persistence mode
Please observe not always the same number of attributes is notified; this depends on the subscription made to the NGSI-like sender. This is not a problem for the `row` persistence mode, since fixed 8-fields data rows are inserted for each notified attribute. Nevertheless, the `column` mode may be affected by several data rows of different lengths (in term of fields). Thus, the `column` mode is only recommended if your subscription is designed for always sending the same attributes, event if they were not updated since the last notification.

In addition, when running in `column` mode, due to the number of notified attributes (and therefore the number of fields to be written within the Datastore) is unknown by Cygnus, the table can not be automatically created, and must be provisioned previously to the Cygnus execution. That's not the case of the `row` mode since the number of fields to be written is always constant, independently of the number of notified attributes.

[Top](#top)

####<a name="section2.3.3"></a>About batching

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`OrionMySQLSink` class
As any other NGSI-like sink, `OrionMySQLSink` extends the base `OrionSink`. The methods that are extended are:

    void persist(Map<String, String>, NotifyContextRequest) throws Exception;
    
The context data, already parsed by `OrionSink` in `NotifyContextRequest`, is iterated and persisted in the MySQL backend by means of a `MySQLBackend` instance. Header information from the `Map<String, String>` is used to complete the persitence process, such as the timestamp or the destination.
    
    public void start();

`MySQLBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `OrionMySQLSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);
    
A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

###<a name="section3.2"></a>`MySQLBackendImpl` class
This is a convenience backend class for MysQL that provides methods to persist the context data both in row and column format. Relevant methods are:

    public void createDatabase(String dbName) throws Exception;
    
Creates a database, given its name, if not existing.
    
    public void createTable(String dbName, String tableName) throws Exception;
    
Creates a table, given its name, if not existing within the given database.
    
    public void insertContextData(String dbName, String tableName, long recvTimeTs, String recvTime, String entityId, String entityType, String attrName, String attrType, String attrValue, String attrMd) throws Exception;
    
Persists the context data regarding a single entity's attribute within the table. This table belongs to the given database. Other notified attributes will be persisted by using this method, next to current one. This method creates the database or the table if any of them is missing (row-like mode).
    
    public void insertContextData(String dbName, String tableName, String recvTime, Map<String, String> attrs, Map<String, String> mds) throws Exception
    
Persists the context data regarding all an entity's attributes within the table. This table belongs to the given database. Since all the attributes are stored with this operation, no other one is required. This method does not create the database nor the table, and all of them must be provisioned in advanced (column-like)

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
