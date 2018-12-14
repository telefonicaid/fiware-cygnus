# DynamoDB backend
## `DynamoDBBackend` interface
This class enumerates the methods any [DynamoDB](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html) backend implementation must expose. In this case, the following ones:

    void createTable(String tableName, String primaryKey) throws Exception;
    
> Creates a table, given its name, if not existing within the DynamoDB user space. The field acting as primary key must be given as well.
    
    void putItems(String tableName, ArrayList<Item> aggregation) throws Exception;
    
> Puts, in the given table, as many items as contained within the given aggregation.

## `DynamoDBBackendImpl` class
This is a convenience backend class for DynamoDB that implements the `DynamoDBBackend` interface described above.

`DynamoDBBackendImpl` really wraps the [DynamoDB API](http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/Welcome.html).
