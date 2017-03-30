# MongoDB backend
## `MongoBackend` interface
This class enumerates the methods any [MongoDB](https://www.mongodb.com/) backend implementation must expose. In this case, the following ones:

    void createDatabase(String dbName) throws Exception;

> Creates a database, given its name, if not exists.

    void createCollection(String dbName, String collectionName, long dataExpiration) throws Exception;

> Creates a collection, given its name, if not exists in the given database.

    void insertContextDataRaw(String dbName, String collectionName, ArrayList<Document> aggregation) throws Exception;

> Inserts a new document in the given collection within the given database. Such a document contains all the information regarding a single notified NGSI entity.

    void insertContextDataAggregated(String dbName, String collectionName, long recvTimeTs, String entityId, String entityType, String attrName, String attrType, String attrValue) throws Exception

> Creates, if not existing yet, or updates a set of documents in the given collection within the given database. Such documents contain aggregated information (sum, square root of the sum, minimum, maximum and number of occurences) regarding the notified NGSI entity for certain resolutions and ranges of time.

    void storeCollectionHash(String dbName, String hash, boolean isAggregated, String fiwareService, String fiwareServicePath, String entityId, String entityType, String attrName, String destination) throws Exception;
        
> Stores the hash associated to a collection build based on the givn parameters.

## `MongoBackendImpl` class
This is a convenience backend class for MongoDB that implements the `MongoBackend` interface described above.

`MongoBackendImpl` really wraps the [MongoDB driver for Java](https://docs.mongodb.com/ecosystem/drivers/java/).

Nothing special is done with regards to the encoding. Since Cygnus generally works with UTF-8 character set, this is how the data is written into the collections. It will responsability of the MongoDB client to convert the bytes read into UTF-8.
