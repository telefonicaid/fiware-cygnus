# Multitenancy in Cygnus
Content:

* [General multitenancy assumption](#section1)
* [Exceptions](#section2)
    * [`NGSIDynamoDBSink`](#section.2.1)
    * [`NGSIKafkaSink`](#section2.2)
    * [`NGSITestSink`](#section2.3)

## <a name="section1"></a>General multitenancy assumption
This section explains the general rule to be followed when implementing multitenancy in Cygnus, specifically when using the following sinks:

* `NGSIHDFSSink`
* `NGSIMySQLSink`
* `NGSICKANSink`
* `NGSIMongoSink`
* `NGSISTHSink`

`NGSIDynamoDBSink`, `NGSIKafkaSink` and `NGSITestSink`, as explained later, don't follow the general rule in terms of multitenancy.

The general multitenancy assumption is **a single superuser able to write in all the user spaces is configured in the sinks**. Such an assumption allows a single Cygnus instance may handle context data regarding several tenants (FIWARE services) since it is allowed to write on behalf of all of them. Of course, the several tenants will own each one a different specific user allowed to I/O its specific user space, once the data is stored by Cygnus.

The above mentioned solution can only work if the final storage holding the historic view if the context data is able to ensure some kind of data isolation per user space. Examples of user spaces are:

* A HDFS user folder.
* A MySQL database.
* A CKAN organization.
* A MongoDB database.

[Top](#top)

## <a name="section2"></a>Exceptions
### <a name="section2.1"></a>`NGSIDynamoDBSink`
DynamoDB handles a single database per client, having each client different access credentials. This means a single superuser cannot be configured in charge of writing data on behalf of several tenants. Even in the case a single DynamoDB user space is used for all the tenants and a table is created in a per client basis, it is not a valid a solution since having access to the user space means access to all the tables.

[This](https://github.com/telefonicaid/fiware-cygnus/issues/608) opened issue tries to enclose a valid solution than will be implemented sometime in the future.

[Top](#top)

### <a name="section2.2"></a>`NGSIKafkaSink`
For the time being, Kafka does not support any kind of authorization mechanism. This implies, despite several topics can be created, one per tenant, there is no control on who can read the different topics.

[Top](#top)

### <a name="section2.3"></a>`NGSITestSink`
This is a testing purpose sink and thus there is no need for multitenancy support.

[Top](#top)
