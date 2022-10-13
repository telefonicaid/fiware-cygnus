# Last Data functionality

Cygnus is capable to perform a `upsert` operation on the following Sinks:

- PostgisSink
- PostgreSQLSink
- MySQLSink
  
In order to perform this operation Cygnus needs five keys.

- `last_data_mode` This is the mode of operation: `upsert`, `insert` or `both`. Default is `insert` mode
- `last_data_table_suffix` This is the suffix that will be added to the table name to perform the upsert operation.
- `last_data_unique_key` This is the reference to indicate to the database engine which is the reference key (or list of keys separed by a comma) to perform the upsert.
- `last_data_timestamp_key` This is the timestamp reference to know which record is the newest.
- `last_data_sql_timestamp_format` This is the timestamp format to indicate to the database how to cast the text timestamp to know if the stored record is older than the one trying to insert.


**The upsert mode performs a transaction where runs upsert querys, if any one of them fails, then the other one is rollbacked. This means they have to be run successfully by Cygnus to store on the database.**


This `upsert` (running with `last_data_mode` to `upsert` or `both`) consists of two main stages:

## Batch latest record

### Current Cygnus aggregation

This stage is performed on the `NGSIGenericColumnAggregator` class. This consists on getting the latest record of a given batch. This is in order to perform a single upsert operation.

First of all, it's important to remember that Cygnus acumulates all Batch Events on a `LinkedHashMap<String, ArrayList<JsonElement>>` collection. 

This means that all aggregated events will be stored on a Map wich contains lists. A way to see it would be something like this.

    +------------+----------------------------+-------------------+
    | recvTimeTs | recvTime                   | fiwareServicePath |     -----> keys
    +------------+----------------------------+-------------------+
    | 1429535774 | 2015-04-20T12:13:22.41.124 | 4wheels           |     -----> aggregated values each column represents an ArrayList
    | 1429535775 | 2015-04-20T12:13:22.41.125 | 4wheels           |
    +------------+----------------------------+-------------------+
 
 Just like a usual table.
 
 All aggregated events are aggregated at the bottom of the lists. If some event contains a key that doesn't exists on the `LinkedHashMap` collection. Then it's added a new key and a new list to the collection. 
 
 When this happens, all previous positions (which correspond to the previous processed events of the batch) are filled up with `null`. In the previous example, it would be something like this.
 
     +------------+----------------------------+-------------------+----------+
     | recvTimeTs | recvTime                   | fiwareServicePath | entityId |
     +------------+----------------------------+-------------------+----------+
     | 1429535774 | 2015-04-20T12:13:22.41.124 | 4wheels           | null     |
     | 1429535775 | 2015-04-20T12:13:22.41.125 | 4wheels           | null     |
     | 1429535773 | 2015-04-20T12:13:22.41.126 | 4wheels           | car1     |
     +------------+----------------------------+-------------------+----------+ 

Note the aggregation is initialized adding the keys in the following order:

- First: the fields that are part of the primary key of the table
- Second: all other main fields entityId, entityType, fiwareServicePath and recvTime
- Third: all the other fields, corresopnding to entity attributes

Doing so the fields in the SQL query generated from the aggregation will come in the same order, so the
string-based ordering within the batch will avoid posible deadlock situations (more information
in [this section](#batch-ordering)).

### Last Data aggregation

When last data is enabled. Cygnus will create a second `LinkedHashMap<String, ArrayList<JsonElement>>` collection on the `NGSIGenericColumnAggregator` class. 

This collection doesn't have it's own aggregate function. The reason for that is that for optimization, it's cheaper to fill up the collection on the fly while the usual aggregation collection is filled up. Since the last_data collection will only store the oldest record per each one of the values of the Unique Key Row.

There are two events which trigger the `last_data` collection storing.

- When the `last_data` collection is empty.
- When the `last_data_unique_key` value of the aggregated object doesn't exists on the collection.
- When the `last_data_unique_key` already exists on the collection and `last_data_timestamp_key` is newer than the stored on the `last_data` collection.

To know that. Cygnus transforms the `last_data_timestamp_key` to a long value, and compares if the long value that corresponds to the stored on the `last_data` collection is older than the last event aggregated.

Getting a little deeper on the third case. As the aggregation process flows, each one of the processed records is compared on the `last_data_unique_key`. 
If it already exists on the collection, then it's compared the `last_data_timestamp_key` of the processed record with the element on the collection with the same `last_data_unique_key`, it the new record has a newer timestamp, then the previous record on the collection is removed and the trigger is enabled to aggregate the processed record to the collection. 

If any of those cases are true. Then starts the aggregation_last_data process.

- Then, a `for each` loop is started with all keys of the `usual_aggregation` collection. 
- A new `key - ArrayList<JsonElement>` relation is created.
- The last element of all of the `usual_aggregation` collection lists is added to the new list created in the previous step.
- If the key already exists on the `last_data` collection then the new value it's added at the end, if it's a new key. Then it's created a new list with null values. As much as events are stored on the `last_data` collection.
- Per each one of the keys processed on the `for each` loop the relation `key - ArrayList<JsonElement>` is added to the `last_data` collection.

This means that the event stored into the `last_data` collection will **not** contain NULL values in case other events procesed in the batch contain columns that the newest entitys doesnt.

Take this example.

In case the `usual_aggregation` collection contains:

     +------------+----------------------------+-------------------+----------+
     | recvTimeTs | recvTime                   | fiwareServicePath | entityId |
     +------------+----------------------------+-------------------+----------+
     | 1429535774 | 2015-04-20T12:13:22.41.124 | 4wheels           | null     |
     | 1429535775 | 2015-04-20T12:13:22.41.125 | 4wheels           | null     |
     | 1429535773 | 2015-04-20T12:13:22.41.126 | 4wheels           | car1     |
     +------------+----------------------------+-------------------+----------+ 
     
The `last_data` collection should contain:

     +------------+----------------------------+-------------------+
     | recvTimeTs | recvTime                   | fiwareServicePath |
     +------------+----------------------------+-------------------+
     | 1429535775 | 2015-04-20T12:13:22.41.125 | 4wheels           |
     +------------+----------------------------+-------------------+     
 
All this process happens per each NGSIEvent aggregated. 

### Batch ordering

Cygnus does a string-based ordering in the batch insert statements before executing them on database. For instance
if we have in the same batch an update for PUMP-001 and an update for PUMP-002 the batch will be as follows:

```
INSERT INTO myservice.pump_lastdata (entityId,entityType,...) VALUE ('PUPM-001','Pump',...) ...
INSERT INTO myservice.pump_lastdata (entityId,entityType,...) VALUE ('PUPM-002','Pump',...) ...
```

no matter if the PUMP-001 notification came before PUMP-002 or the other way around.

This, combined with the field ordering [already described in section before](#current-cygnus-aggregation)
avoids deadlocks when two Cygnus instances are trying to upsert in the same table
(more detail on this in issue [#2197](https://github.com/telefonicaid/fiware-cygnus/issues/2197)).

## SQL UPSERT

Once the aggregation is processed, then a query is created to upsert a single record with a PreparedStatement.

**Notice that in both cases MySQL and PostgreSQL this function always expects to find timestamp keys into Text format.**

A query like this one is executed

### POSTGRESQL

`INSERT INTO pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data 
(entityId,entityType,fiwareServicePath,recvTime,fillingLevel,fillingLevel_md) VALUES (?, ?, ?, ?, ?, ?) 
ON CONFLICT (entityId) DO UPDATE SET 
entityType=EXCLUDED.entityType, fiwareServicePath=EXCLUDED.fiwareServicePath, recvTime=EXCLUDED.recvTime, 
fillingLevel=EXCLUDED.fillingLevel, fillingLevel_md=EXCLUDED.fillingLevel_md 
WHERE pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data.entityId=EXCLUDED.entityId 
AND to_timestamp(pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data.recvTime::text, 'YYYY-MM-DD HH24:MI:SS.MS') 
< to_timestamp(EXCLUDED.recvTime::text, 'YYYY-MM-DD HH24:MI:SS.MS')`

There are some important considerations for this query.

`INSERT INTO pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data`

On the table name the `last_data_table_suffix` is added at the end of the usual table name. So in this case, the usual insert is made to `subpruebapostman_5dde9_wastecontainer`.

`ON CONFLICT (entityId)`

This line defines that this `INSERT` could create a conflict. `(entityId)` in this case represents `last_data_unique_key`, wich by default is `entityId`.

`entityType=EXCLUDED.entityType`

By default PostgreSQL references the conflicting values as `EXCLUDED`.

`to_timestamp(pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data.recvTime::text, 'YYYY-MM-DD HH24:MI:SS.MS')`

`to_timestamp` casts a Strig timestamp into a SQL Timestamp format. In order to do so, it needs to be provided with a timestamp format (`last_data_sql_timestamp_format`) and a key that corresponds to the value to cast (`last_data_timestamp_key`). In this case, this line casts the current value stored into the table.

`< to_timestamp(EXCLUDED.recvTime::text, 'YYYY-MM-DD HH24:MI:SS.MS')`

This line casts the conflictive timestamp value and compares it with the stored one.

**Notice that if the previous stored event contain data in a column that the intended values to insert not contain. The previous values stores in those columns will remain after the update**

Take this exapmple.

In case the table contains:

     +------------+----------------------------+-------------------+----------+
     | recvTimeTs | recvTime                   | fiwareServicePath | entityId |
     +------------+----------------------------+-------------------+----------+
     | 1429535774 | 2015-04-20T12:13:22.41.124 | 4wheels1          | null     |
     | 1429535775 | 2015-04-20T12:13:22.41.125 | 4wheels2          | null     |
     | 1429535773 | 2015-04-20T12:13:22.41.126 | 4wheels3          | car1     |
     +------------+----------------------------+-------------------+----------+ 
     
And Cygnus tryes to upsert the following 

     +------------+----------------------------+----------+
     | recvTimeTs | recvTime                   | entityId |
     +------------+----------------------------+----------+
     | 1429535775 | 2015-04-20T12:13:22.41.125 | car2     |
     +------------+----------------------------+----------+
     
The table updated would be:     

     +------------+----------------------------+-------------------+----------+
     | recvTimeTs | recvTime                   | fiwareServicePath | entityId |
     +------------+----------------------------+-------------------+----------+
     | 1429535774 | 2015-04-20T12:13:22.41.124 | 4wheels1          | null     |
     | 1429535775 | 2015-04-20T12:13:22.41.125 | 4wheels2          | car2     |
     | 1429535773 | 2015-04-20T12:13:22.41.126 | 4wheels3          | car1     |
     +------------+----------------------------+-------------------+----------+ 
     
     
Notice the value `4wheels2` on the column `fiwareServicePath` remained after the update because it was not updated.



### MYSQL

`INSERT INTO sub_5dde9_last_data 
(entityId,entityType,fiwareServicePath,recvTime,fillingLevel,fillingLevel_md) 
VALUES (?, ?, ?, ?, ?, ?)  
ON DUPLICATE KEY UPDATE 
fiwareServicePath=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < 
(STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(fiwareServicePath), fiwareServicePath), 
entityType=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < 
(STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(entityType), entityType), 
fillingLevel=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < 
(STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(fillingLevel), fillingLevel), 
fillingLevel_md=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < 
(STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(fillingLevel_md), fillingLevel_md), 
recvTime=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < 
(STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(recvTime), recvTime)`

In general the syntax is very similar to PostgreSQL query. The first part executes a usual INSERT query.

`INSERT INTO sub_5dde9_last_data 
             (recvTime,fiwareServicePath,entityId,entityType,fillingLevel,fillingLevel_md) 
             VALUES (?, ?, ?, ?, ?, ?`
             
Notice that on the table name the `last_data_table_suffix` is added at the end of the usual table name. So in this case, the usual insert is made to `sub_5dde9_wastecontainer`.             

`ON DUPLICATE KEY UPDATE`

This line indicates the expected behaviour when the insert query finds any trouble when inserting data with the unique column. 

`fiwareServicePath=IF((entityId=VALUES(entityId)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < 
(STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(fiwareServicePath), fiwareServicePath)`

This defines the behaviour that mySQL will follow when finds any key previously inserted into the database.

As you can see on that statement. The record `fiwareServicePath` will be updated `IF`

`IF((key=VALUES(key)) AND (STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < (STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))), VALUES(key), key)`

`key=VALUES(key)`

The record previously inserted has the same key that the one that is trying to be inserted (referred as `VALUES(key)`)

`AND`

`(STR_TO_DATE(recvTime, '%Y-%m-%d %H:%i:%s.%f') < (STR_TO_DATE(VALUES(recvTime), '%Y-%m-%d %H:%i:%s.%f'))`

`STR_TO_DATE(last_data_timestamp_key, 'last_data_sql_timestamp_format')`

This line casts to a timestamp the indicated key `last_data_timestamp_key` with the format `last_data_sql_timestamp_format` and then compares it with the timestampof the record that is beeing tried to insert (referred as `VALUES(recvTime)`) also casted to timestamp.

**Notice that if the previous stored event contain data in a column that the intended values to insert not contain. The previous values stores in those columns will remain after the update**
