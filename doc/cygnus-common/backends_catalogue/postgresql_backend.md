# PostgreSQL backend
## `PostgreSQLBackend` interface
This class enumerates the methods any [PostgreSQL](http://www.postgresql.org/) backend implementation must expose. In this case, the following ones:

    void createSchema(String schemaName) throws Exception;

> Creates a database, given its name, if not existing.

   void createTable(String schemaName, String tableName, String fieldNames) throws Exception;

> Creates a table, given its name, if not existing within the given database. The field names are given as well.

    void insertContextData(String schemaName, String tableName, String fieldNames, String fieldValues) throws Exception;

> Persists the accumulated context data (in the form of the given field values) regarding an entity within the given table. This table belongs to the given database. The field names are given as well to ensure the right insert of the field values.

## `PostgreSQLBackendImpl` class
This is a convenience backend class for PostgreSQL that implements the `PostgreSQLBackend` interface described above.

`PostgreSQLBackendImpl` really wraps the [PostgreSQL JDBC driver](https://jdbc.postgresql.org/).

It must be said this backend implementation enforces UTF-8 encoding through the usage of `charSet=UTF-8` property when getting a connection via the JDBC driver.
