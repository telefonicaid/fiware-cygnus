# Hive backend
## `HiveBackend` interface
This class enumerates the methods any [Hive](https://hive.apache.org/) backend implementation must expose. In this case, the following ones:

    boolean doCreateDatabase(String dbName);

> Creates a database given its name.

    boolean doCreateTable(String query);

> Creates a tables given its "create table" query.

    boolean doQuery(String query);

> Executes a given query.

## `HiveBackendImpl` class
This is a convenience backend class for Hive that implements the `HiveBackend` interface described above.

`HiveBackendImpl` really wraps the Hive JDBC driver ([HiveServer1](https://cwiki.apache.org/confluence/display/Hive/HiveClient#HiveClient-JDBC) version and [HiveServer2](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-JDBC) version).
