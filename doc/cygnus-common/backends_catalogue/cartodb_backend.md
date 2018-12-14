# CartoDB backend
## `CartoDBBackend` interface
This class enumerates the methods any [Carto](https://carto.com/) backend implementation must expose. In this case, the following ones:

    void insert(String tableName, String fields, String rows) throws Exception;
   
> Inserts the given aggregated data rows in the given table within the given database.

##`CartoDBBackendImpl` class
This is a convenience backend class for HDFS that extends the `HttpBackend` abstract class (provides common logic for any Http connection-based backend) and implements the `CartoDBBackend` interface described above.

`CartoDBBackendImpl` really wraps the Carto [SQL API](https://carto.com/docs/carto-engine/sql-api/).

It must be said this backend implementation enforces UTF-8 encoding in the PostgreSQL queries within the SQL API requests.
