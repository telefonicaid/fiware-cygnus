# Error persistence.

If there is an exception when trying to persist data into storage. Cygnus is capable to store it into a new table as long as there is successful connection to persistence manager.

This only applies to SQL type backends PostgreSQL & MySQL. Notice that Postgis uses PostgreSQL backend so this function extends to Postgis as well as PostgreSQL and MySQL.

This feature is enabled by default but could be disabled by setting sink option `persist_errors` to false.
The among of errors which are persisted is the latest 100 by default, but couldd be changed by setting sink option `max_latest_errors`.

As mentioned before, if there is an exception when trying to persist data, Cygnus tryes to create a table with the following pattern.

MySQL
```
$database_name_error_log

```

Postgis / PostgreSQL

```
$schema_name_error_log
```

with the following scheme.

Column name   | SQL Type
------------- | --------------------------------------- 
timestamp     | TIMESTAMP
error         | TEXT
query         | TEXT

`For instance.`

If Cygnus receives a request that attemp to execute the query:

```
INSERT INTO prueba2.streetlight (recvT
ime,fiwareServicePath,entityId,entityType,TimeInstant,TimeInstant_md,serialNumber,serialNumber_md) VALUES ('2020-02-27 09:12:29.344','/e','farolahttpu
l','Streetlight','2019-10-31T13:49:17.00Z','[]','OP00096F160318C000004','[]'),('2020-02-27 09:12:30.510','/e','farolahttpul','Streetlight','2019-10-31
T13:49:17.00Z','[]','OP00096F160318C000004','[]')
```

But the table streetlight doesn't exists, then this will produce a SQL Exception;

```
ERROR: relation "prueba2.streetlight" does not exist
```

Then Cygnus will create a table:

```
prueba2_error_log
```

And finally execute a query to insert the exception into the new table.

```
2020-02-27 09:12:42.049 | ERROR: relation "prueba2.streetlight" does not exist+| INSERT INTO prueba2.streetlight (recvT
ime,fiwareServicePath,entityId,entityType,TimeInstant,TimeInstant_md,serialNumber,serialNumber_md) VALUES ('2020-02-27 09:12:29.344','/e','farolahttpu
l','Streetlight','2019-10-31T13:49:17.00Z','[]','OP00096F160318C000004','[]'),('2020-02-27 09:12:30.510','/e','farolahttpul','Streetlight','2019-10-31
T13:49:17.00Z','[]','OP00096F160318C000004','[]')
```
