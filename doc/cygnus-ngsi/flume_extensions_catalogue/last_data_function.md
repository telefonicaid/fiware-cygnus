# Last Data functionality.

Cygnus is capable to perform an `upsert` operation on the following Sinks.

- PostgisSink
- PostgreSQLSink  
  
This Operation doesn't overrides the usual inserts on the usual way.

In order to perform this operation Cygnus needs four keys.

- `last_data_table_suffix` This is the suffix that will be added to the table name to perform the upsert operation.
- `last_data_unique_key` This is the reference to indicate to the database engine wich is the reference key to perform the upsert.
- `last_data_timestamp_key` This is the timestamp reference to know which record is the newest.
- `last_data_sql_timestamp_format` This is the timestamp format to indicate to the database how to cast the text timestamp to know if the stored record is older than the one trying to insert.


This `upsert` consists of two main stages:

## Batch latest record.

### Current Cygnus aggregation.

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
     
### Last Data aggretation

When last data is enabled. Cygnus will create a second `LinkedHashMap<String, ArrayList<JsonElement>>` collection on the `NGSIGenericColumnAggregator` class. 

This collection doesn't have it's own aggregate function. The reason for that is that for optimization, it's cheaper to fill up the collection on the fly while the usual aggregation collection is filled up. Since the last_data collection will only store the oldest record.

There are two events which trigger the `last_data` collection storing.

- When the `last_data` collection is empty.
- When the last event aggregated is newer than the stored on the `last_data` collection.

To know that. Cygnus transforms the `last_data_timestamp_key` to a long value, and compares if the long value that corresponds to the stored on the `last_data` collection is older than the last event aggregated.

If any of those cases are true. Then starts the aggregation_last_data process.

- First the `last_data` collection is initialized as a new one. This means that all previous data will be removed.
- Then, a `for each` loop is started with all keys of the `usual_aggregation` collection. 
- A new `key - ArrayList<JsonElement>` relation is created.
- The last element of all of the `usual_aggregation` collection lists is added to the new list created in the previous step.
- Per each one of the keys processed on the `for each` loop the relation `key - ArrayList<JsonElement>` is added to the `last_data` collection.

This means that the event stored into the `last_data` collection will **not** contain NULL values in case other events procesed in the batch contain columns that the newest entitys doesnt.

Take this exapmple.

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

## SQL UPSERT

Once the aggregation is processed, then a query is created to upsert a single record with a PreparedStatement.

A query like this one is executed

`INSERT INTO pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data 
(recvTime,fiwareServicePath,entityId,entityType,fillingLevel,fillingLevel_md) VALUES (?, ?, ?, ?, ?, ?) 
ON CONFLICT (entityId) DO UPDATE SET 
recvTime=EXCLUDED.recvTime, fiwareServicePath=EXCLUDED.fiwareServicePath, entityType=EXCLUDED.entityType, 
fillingLevel=EXCLUDED.fillingLevel, fillingLevel_md=EXCLUDED.fillingLevel_md 
WHERE pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data.entityId=EXCLUDED.entityId 
AND to_timestamp(pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data.recvTime, 'YYYY-MM-DD HH24:MI:SS.MS') 
< to_timestamp(EXCLUDED.recvTime, 'YYYY-MM-DD HH24:MI:SS.MS')`

There are some important considerations for this query.

`INSERT INTO pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data`

On the table name the `last_data_table_suffix` is added at the end of the usual table name. So in this case, the usual insert is made to `subpruebapostman_5dde9_wastecontainer`.

`ON CONFLICT (entityId)`

This line defines that this `INSERT` could create a conflict. `(entityId)` in this case represents `last_data_unique_key`, wich by default is `entityId`.

`recvTime=EXCLUDED.recvTime`

By default PostgreSQL references the conflicting values as `EXCLUDED`.

`to_timestamp(pruebapostmanx.subpruebapostman_5dde9_wastecontainer_last_data.recvTime, 'YYYY-MM-DD HH24:MI:SS.MS')`

`to_timestamp` casts a Strig timestamp into a SQL Timestamp format. In order to do so, it needs to be provided with a timestamp format (`last_data_sql_timestamp_format`) and a key that corresponds to the value to cast (`last_data_timestamp_key`). In this case, this line casts the current value stored into the table.

`< to_timestamp(EXCLUDED.recvTime, 'YYYY-MM-DD HH24:MI:SS.MS')`

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