# MySQL backend
## `MySQLBackend` interface
This class enumerates the methods any [MySQL](https://www.mysql.com/) backend implementation must expose. In this case, the following ones:

    void createDatabase(String dbName) throws Exception;

> Creates a database, given its name, if not existing.

    void createTable(String dbName, String tableName, String fieldNames) throws Exception;

> Creates a table, given its name, if not existing within the given database. The field names are given as well.

    insertContextData(String dbName, String tableName, String fieldNames, String fieldValues) throws Exception;

> Persists the accumulated context data (in the form of the given field values) regarding an entity within the given table. This table belongs to the given database. The field names are given as well to ensure the right insert of the field values.

## `MySQLBackendImpl` class
This is a convenience backend class for MySQL that implements the `MySQLBackend` interface described above.

`MySQLBackendImpl` really wraps the [MySQL JDBC driver](https://dev.mysql.com/downloads/connector/j/).

It must be said this backend implementation enforces UTF-8 encoding through the usage of `useUnicode=true` and  `characterEncoding=UTF-8` properties when getting a connection via the JDBC driver.
