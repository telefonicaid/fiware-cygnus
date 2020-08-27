# <a name="top"></a>NGSIPostGISSink
Content:

* [Functionality](#section1)
* [Administration guide](#section2)
* [Programmers guide](#section3)
* [Native types](#section4)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIPostGISSink`, or simply `NGSIPostGISSink` is a sink designed to persist NGSI-like context data events within a [PostGIS server](https://postgis.net/) which is relational database extension of [PostgreSQL server](https://www.postgresql.org/) which allows store GIS objects (Geographic Information Systems). Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking the <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific PostgreSQL data structures.

For futher detail please refear to [`NGSIPostgreSQLSink`](/ngsi_postgresql_sink.md)
Basically PostGIS sink translates NGSI attribute value about geometry (geo:point, geo:json) to PostGIS format and insert it.


    geo:point ->  ST_SetSRID(ST_MakePoint())

    geo:json -> ST_GeomFromGeoJSON()

Colum should be provisioned as type [`geometry`](http://postgis.net/workshops/postgis-intro/geometries.html)

    CREATE TABLE myTable ( geom geometry );

[Top](#top)

## <a name="section2"></a>Administration guide
Please refear to [`NGSIPostgreSQLSink`](/ngsi_postgresql_sink.md) since all administration options about PostgreSQLSink applies to PostGISSink.

## <a name="section3"></a>Programmers guide
Please refear to [`NGSIPostgreSQLSink`](/ngsi_postgresql_sink.md) since all programing details about PostgreSQLSink applies to PostGISSink.


## <a name="section4"></a>Native types

Regarding the specific data stored within the above table, if `attr_native_types` parameter is set to `true` then attribute is inserted using its native type (according with the following table), if `false` then will be stringify. 

Type json     | Type PostGreSQL/POSTGIS
------------- | --------------------------------------- 
string        | text
number        | double, precision, real, others (numeric, decimal)
boolean       | boolean (TRUE, FALSE, NULL)
DateTime      | timestamp, timestamp with time zone, timestamp without time zone
json          | text o json - it`s treated as String
null          | NULL

This only applies to Column mode.
