# <a name="top"></a>Backends as short-term historics
Backends are used by Cygnus NGSI as "infinite" historical context data repositories. More and more data is appended to files, tables and collections as data flow from a NGSI source. Such a data flow may never end, thus, insertions may never end too, exhausting the available storing resources.

Therefore, it is important to provide mechanisms in charge of controlling how much data is stored in the persistence backends, removing old data in favour of new one, resulting in some kind of short-term historic implementation.

From version 1.7.0 this is something that can be done by means of the **capping** and/or **expirating** features.

## How it works
There are two approaches when deciding which data must be removed from existent historics:

* By **capping** data "records"(*) once certain size limit has been reached. In other words, to ensure that only the last N records are stored, honouring the capping limit in place.
* By **expirating** "records"(*) once certain keepalive limit has been reached. In other words, to control only records added in the last N seconds are maintained, deleting old ones.

The above can be controlled by means of specific configuration parameters in certain sinks:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| persistence\_policy.max_records | no | -1 | Maximum number of records allowed for a persistence element (table, resource, collection, etc.) before it is capped. `-1` disables this policy. |
| persistence\_policy.expiration_time | no | -1 | Maximum number of seconds a record is maintained in a persistence element (table, resource, collection, etc.) before expiration. `-1` disables this policy. |
| persistence\_policy.checking_time | no | 3600 | Frequency (in seconds) at which the sink checks for record expiration. |

Which sinks provide this kind of functionality? For the time being:

* `NGSIMySQLSink`
* `NGSICKANSink`
* `NGSIMongoSink` (but in a slight different way, see next section)
* `NGSISTHSink` (but in a slight different way, see next section)

(*) A record may mean many different things depending on the persistence backend: a Json entry in a HDFS file, a row in a MySQL table, a record in a CKAN resource...

[Top](#top)

## The special case of `NGSIMongoSink` and `NGSISTHSink`
`NGSIMongoSink` and `NGSISTHSink` implement this kind of functionality from version 0.13.0, since the data stored in MongoDB and STH Comet was wanted to be a short-term historic from the very begining. Nevertheless, the parameters controlling the functionality are very different from the above ones:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| data\_expiration | no | 0 | Collections will be removed if older than the value specified in seconds. The reference of time is the one stored in the `recvTime` property. Set to 0 if not wanting this policy. |
| collections\_size | no | 0 | The oldest data (according to insertion time) will be removed if the size of the data collection gets bigger than the value specified in bytes. Notice that the size-based truncation policy takes precedence over the time-based one. Set to 0 if not wanting this policy. Minimum value (different than 0) is 4096 bytes. <b>Only available for `NGSIMongoSink`</b>. |
| max\_documents | no | 0 | The oldest data (according to insertion time) will be removed if the number of documents in the data collections goes beyond the specified value. Set to 0 if not wanting this policy. <b>Only available for `NGSIMongoSink`</b>. |

There are also differences in the implementations: while MongoDB natively provides mechanisms for controlling the collections growing at collection creation time -either by size either by time-, MySQL and CKAN don't provide something similar. In these cases, the functionality has been built on top of the API through delete operations calculated by Cygnus itself.

[Top](#top)

## Future work
Most probably in the future all the sinks sharing this feature will see their parameters homogenized, since conceptually the capping/expirating feature implmented by CKAN and MySQL sinks is the same than the time and size-based data management policies in MongoDB and STH sinks.

[Top](#top)
