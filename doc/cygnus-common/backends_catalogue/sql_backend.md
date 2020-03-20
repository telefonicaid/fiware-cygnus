# SQL backend

## `SQLBackend` class

This class replaces MySQL and PostgreSQL backends. Since actually the backend implemetation for Cygnus purposes doesn't uses any particular feature of each handler, it's possible to use a generic backend implementation for both SQL interfaces.

This class also has methods to execute any SQL query.

`SQLDriver` wraps 

- [PostgreSQL JDBC driver](https://jdbc.postgresql.org/).
- [MySQL JDBC driver](https://dev.mysql.com/downloads/connector/j/).

It must be said this backend implementation enforces UTF-8 encoding through the usage of `charSet=UTF-8` property when getting a connection via the JDBC driver.

Also, this class persists into persistence manager any error regarding to any attempt to persist information. For further reference read [Error persistence](../installation_and_administration_guide/error_persistance.md).