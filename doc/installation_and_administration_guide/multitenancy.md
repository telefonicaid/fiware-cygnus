#Multitenancy in Cygnus
Content:

* [General multitenancy assumptions](#section1)
* [Exceptions](#section2)
    * [`OrionDynamoDBsink`](#section.2.1)
    * [`OrionKafkaSink`](#section2.2)
    * [`OrionTestSink`](#section2.3)

##<a name="section1"></a>General multitenancy assumptions
This section explains the general rule to be followed when implementing multitenancy in Cygnus, specifically when using the following sinks:

* `OrionHDFSSink`
* `OrionMySQLSink`
* `OrionCKANSink`
* `OrionMongoSink`
* `OrionSTHSink`

`OrionDynamoDBSink`, `OrionKafkaSink` and `OrionTestSink`, as explained later, don't follow the general rule in terms of multitenancy.

[Top](#top)

##<a name="section2"></a>Exceptions
###<a name="section2.1"></a>`OrionDynamoDBSink`

[Top](#top)

###<a name="section2.2"></a>`OrionKafkaSink`

[Top](#top)

###<a name="section2.3"></a>`OrionTestSink`

[Top](#top)
