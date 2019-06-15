# <a name="top"></a>NGSIElasticsearchSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to Elasticsearch data structures](#section1.2)
        * [Elasticsearch index naming conventions](#section1.2.1)
        * [Converting the type of `attrValue` according to `attrType`](#section1.2.2)
        * [Row-like storing](#section1.2.3)
        * [Column-like storing](#section1.2.4)
    * [Example](#section1.3)
        * [`NGSIEvent`](#section1.3.1)
        * [Index names](#section1.3.2)
        * [Row-like storing](#section1.3.3)
        * [Column-like storing](#section1.3.4)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Use cases](#section2.2)
    * [Important notes](#section2.3)
        * [About caching](#section2.3.1)
        * [About `recvTime` and `TimeInstant` metadata](#section2.3.2)
        * [About supported versions of Elasticsearch](#section2.3.3)
* [Programmers guide](#section3)
    * [`NGSIElasticsearchSink` class](#section3.1)
    * [`ElasticsearchBackend` class](#section3.2)
    * [Authentication and authorization](#section3.3)

## <a name="section1"></a>Functionality
`com.telefonica.iot.cygnus.sinks.NGSIElasticsearchSink`, or simply `NGSIElasticsearchSink` is a sink designed to persist NGSI context data events into an Elasticsearch. Usually, such a NGSI context data is notified from a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion), but any other sources can be accepted as long as they are NGSI.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific Elasticsearch data structures.

Next sections will explain this is detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](./ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to Elasticsearch data structures
Elasticsearch organizes the data in database that contain collections of Json documents. Such organization is exploited by `NGSIElasticsearchSink` each time a `NGSIEvent` is going to be persisted.

#### <a name="section1.2.1"></a>Elasticsearch index naming conventions
An index of Elasticsearch is automatically created (if not existing yet).

An Elasticsearch index must have [a single mapping type](https://www.elastic.co/guide/en/elasticsearch/reference/current/removal-of-types.html). And the index name has [some limitations](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/indices-create-index.html) like below:
* index name is lowercase only.
* index name cannot include `\, /, *, ?, ", <, >, |, ` ` (space character), ,, #, :`.
* index name cannot start with `-, _, +`.
* index name is limited to 255 bytes.

So `NGSIElasticsearchSink` constructs the index name according to the following steps:

1. create a base string by joining configured &lt;prefix&gt;, &lt;fiware service&gt;, &lt;fiware servicepath&gt;, &lt;entity Id&gt; and &lt;entity tyep&gt;.
2. convert the base string to lower cases.
3. replace forbidden characters to '-'.
4. when you use `Column-like storing`, append a hash string calculated by the attribute names to be stored
    * MD5 hash is calculated by concatinated attribute names such as  `attrName1:attrName2:...`.
5. when it starts with `-, _, +`, append 'idx' at the beggning of the base string.
6. append the created &lt;date&gt; such as `yyyy.mm.dd`.

According to the above rules, `NGSIElasticsearchSink` can handle the multiple subscriptions with different attributes of the same entity.

[Top](#top)

#### <a name="section1.2.2"></a>Converting the type of `attrValue` according to `attrType`
If `cast_value` parameter is set to `true`, the type of `attrValue` will be converted automatically according to the `attrType` when storing index. The converting rule is like below:

|`attrType` (ignore case)|the type to be converted|
|:--|:--|
|`int` or `integer`|Integer|
|`float`|Float|
|`number` or `double`|Double|
|`bool` or `boolean`|Boolean|
|otherwise|String|

[Top](#top)

#### <a name="section1.2.3"></a>Row-like storing
Regarding the specific data stored within the above index, if `attr_persistence` parameter is set to `row` (default storing mode) then the notified data is stored attribute by attribute, composing a Json document for each one of them. Each document contains the following fields:

* `recvTime`: timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)). You can set the timezone of recvTime by using the `timezone` parameter.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
* `attrName`: Notified attribute name.
* `attrType`: Notified attribute type.
* `attrValue`: Notified atribute value.
    * If `cast_value` parameter is set to `true`, the type of this value is automatically converted according to `attrType`.
    * Otherwise the value is handled as String.
* `attrMetadata`: Notified attribute metadata.

**CAUTION**  
Because Elasticsearch 6.0 or later must have a single mapping type, the type of `attrValue` handled by Elasticsearch is determined by the first registered record. Therefore, when you set the `attr_persistence` parameter as `row` and `cast_value` parameter as `true`, the later attribute records which have different type with the first attribute record will be ignored and will not be stored to Elasticsearch.

**Please make sure that the all attributes to be stored have the same `AttrType` when you want to set the `attr_persistent` parameter as `row` and the `cast_value` parameter as `true`.**

[Top](#top)

#### <a name="section1.2.4"></a>Column-like storing
Regarding the specific data stored within the above index, if `attr_persistence` parameter is set to `column` then a single Json document is composed for the whole notified entity. Each document contains a variable number of fields:

* `recvTime`: timestamp in human-readable format ([ISO 8601](http://en.wikipedia.org/wiki/ISO_8601)). You can set the timezone of recvTime by using the `timezone` parameter.
* `entityId`: Notified entity identifier.
* `entityType`: Notified entity type.
*  For each notified attribute, a field named as the attribute is considered. This field will store the attribute values along the time.
    * If `cast_value` parameter is set to `true`, the type of this value is automatically converted according to `attrType`.
    * Otherwise the value is handled as String.

**CAUTION**  
When `attr_persistence` parameter is set to `column`, **the metadata of each attribute is ignored and is not stored.**

### <a name="section1.3"></a>Example
#### <a name="section1.3.1"></a>`NGSIEvent`
Assuming the following `NGSIEvent` is created from a notified NGSI context data (the code below is an <i>object representation</i>, not any real data format):

```
ngsi-event={
    headers={
       content-type=application/json,
       timestamp=1429535775,
       transactionId=1429535775-308-0000000000,
       correlationId=1429535775-308-0000000000,
       fiware-service=vehicles,
       fiware-servicepath=/4wheels,
       <grouping_rules_interceptor_headers>,
       <name_mappings_interceptor_headers>
    },
    body={
      entityId=car1,
      entityType=car,
      attributes=[
          {
              attrName=speed,
              attrType=float,
              attrValue=112.9
          },
          {
              attrName=oil_level,
              attrType=float,
              attrValue=74.6
          },
          {
              attrName=driver,
              attrType=string,
              attrValue=Jhon
          },
          {
              attrName=headlight,
              attrType=boolean,
              attrValue=true
          }
      ]
  }
}
```

[Top](#top)

#### <a name="section1.3.2"></a>Index names
The index policy of Elasticsearch is a bit complicated (see the [Elasticsearch index naming conventions](#section1.2.1) section). Please see the below examples to help your understanding.

* Row-like sotring

|`date`|`attr_persistence`|`prefix`(default `cygnus`)|`index name`|
|:--|:--|:--|:--|
|June 15, 2019|row|N/A|`cygnus-vehicles-4wheels-car1-car-2019.06.15`|
|June 15, 2019|row|\_#PREFIX\*1|`idx_-prefix-1-vehicles-4wheels-car1-car-2019.06.15`|

* Column-like storing

|`date`|`attr_persistence`|`prefix`(default `cygnus`)|`index name`|
|:--|:--|:--|:--|
|June 15, 2019|column|N/A|`cygnus-vehicles-4wheels-car1-car-30ecbfb71797b22d45b1458d33e2a995-2019.06.15`|
|June 15, 2019|column|\_#PREFIX\*1|`idx_-prefix-1-vehicles-4wheels-car1-car-30ecbfb71797b22d45b1458d33e2a995-2019.06.15`|

> `30ecbfb71797b22d45b1458d33e2a995` is the MD5 hash of `driver:headlight:oil_level:speed:`

[Top](#top)

#### <a name="section1.3.3"></a>Row-like storing
Assuming `attr_persistence=row` and `cast_value=false` as configuration parameters, then `NGSIElasticsearchSink` will persist the 4 records within its index as:

```json
{
  "_index": "cygnus-vehicles-4wheels-car1-car-2019.06.15",
  "_type": "cygnus_type",
  "_id": "1560553413553-A75F02A46692678F14273BB4F7E5D11D",
  "_version": 1,
  "_score": null,
  "_source": {
    "recvTime": "2019-06-15T08:03:33.553+0900",
    "entityType": "car",
    "attrMetadata": [],
    "entityId": "car1",
    "attrValue": "Jhon",
    "attrName": "driver",
    "attrType": "string"
  },
  "fields": {
    "recvTime": [
      "2019-06-14T23:03:33.553Z"
    ]
  },
  "sort": [
    1560553413553
  ]
}
```
```json
{
  "_index": "cygnus-vehicles-4wheels-car1-car-2019.06.15",
  "_type": "cygnus_type",
  "_id": "1560553413553-B677CB462D993FE6184BAD73E7DAC2AE",
  "_version": 1,
  "_score": null,
  "_source": {
    "recvTime": "2019-06-15T08:03:33.553+0900",
    "entityType": "car",
    "attrMetadata": [],
    "entityId": "car1",
    "attrValue": "true",
    "attrName": "headlight",
    "attrType": "boolean"
  },
  "fields": {
    "recvTime": [
      "2019-06-14T23:03:33.553Z"
    ]
  },
  "sort": [
    1560553413553
  ]
}
```
```json
{
  "_index": "cygnus-vehicles-4wheels-car1-car-2019.06.15",
  "_type": "cygnus_type",
  "_id": "1560553413553-A0E90CE6731793853243F34B73DF423A",
  "_version": 1,
  "_score": null,
  "_source": {
    "recvTime": "2019-06-15T08:03:33.553+0900",
    "entityType": "car",
    "attrMetadata": [],
    "entityId": "car1",
    "attrValue": "74.6",
    "attrName": "oil_level",
    "attrType": "float"
  },
  "fields": {
    "recvTime": [
      "2019-06-14T23:03:33.553Z"
    ]
  },
  "sort": [
    1560553413553
  ]
}
```
```json
{
  "_index": "cygnus-vehicles-4wheels-car1-car-2019.06.15",
  "_type": "cygnus_type",
  "_id": "1560553413553-FE5C0A175BDF9956D0D3DAAFC0C3CFF9",
  "_version": 1,
  "_score": null,
  "_source": {
    "recvTime": "2019-06-15T08:03:33.553+0900",
    "entityType": "car",
    "attrMetadata": [],
    "entityId": "car1",
    "attrValue": "112.9",
    "attrName": "speed",
    "attrType": "float"
  },
  "fields": {
    "recvTime": [
      "2019-06-14T23:03:33.553Z"
    ]
  },
  "sort": [
    1560553413553
  ]
}
```

The generated `mapping_type` of this index is like below:

```json
{
  "cygnus-vehicles-4wheels-car1-car-2019.06.15": {
    "mappings": {
      "cygnus_type": {
        "properties": {
          "attrName": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "attrType": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "attrValue": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "entityId": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "entityType": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "recvTime": {
            "type": "date"
          }
        }
      }
    }
  }
}
```

[Top](#top)

#### <a name="section1.3.4"></a>Column-like storing
Assuming `attr_persistence=column` and `cast_value=true` as configuration parameters, then `NGSIElasticsearchSink` will persist a record within its index as:

```json
{
  "_index": "cygnus-vehicles-4wheels-car1-car-30ecbfb71797b22d45b1458d33e2a995-2019.06.15",
  "_type": "cygnus_type",
  "_id": "1560554072316-C9ABB55067F46D870218733ABE2F4BA8",
  "_version": 1,
  "_score": null,
  "_source": {
    "oil_level": 74.6,
    "recvTime": "2019-06-15T08:14:32.316+0900",
    "driver": "Jhon",
    "entityType": "car",
    "entityId": "car1",
    "speed": 112.9,
    "headlight": true
  },
  "fields": {
    "recvTime": [
      "2019-06-14T23:14:32.316Z"
    ]
  },
  "sort": [
    1560554072316
  ]
}
```

Because `cast_value` parameter is set as true, Elasticsearch handles the `speed` and `oil_level` as **float**, `driver` as **text**, and `headlight` as **boolean**.

The generated `mapping_type` of this index is like below:

```json
{
  "cygnus-vehicles-4wheels-car1-car-30ecbfb71797b22d45b1458d33e2a995-2019.06.15": {
    "mappings": {
      "cygnus_type": {
        "properties": {
          "driver": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "entityId": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "entityType": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "headlight": {
            "type": "boolean"
          },
          "oil_level": {
            "type": "float"
          },
          "recvTime": {
            "type": "date"
          },
          "speed": {
            "type": "float"
          }
        }
      }
    }
  }
}
```

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIElasticsearchSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | com.telefonica.iot.cygnus.sinks.NGSIElasticsearchSink |
| channel | yes | N/A | elasticsearch-channel |
| elasticsearch\_host | yes | localhost | the hostname of Elasticsearch server |
| elasticsearch\_port | yes | 9200 | the port number of Elasticsearch server (0 - 65535) |
| ssl | yes | false | true if connect to Elasticsearch server using SSL ("true" or "false") |
| index\_prefix | no | cygnus | the prefix of index name |
| mapping\_type | no | cygnus\_type | the mapping type name of Elasticsearch |
| ignore\_white\_spaces | no | true | true if exclusively white space-based attribute values must be ignored, false otherwise ("true" or "false") |
| attr\_persistence | no | row | the persistence style as row-style or column-style ("row" or "column") |
| timezone | no | UTC | timezone to be used as a document's timestamp |
| cast\_value | no | false | true if cast the attrValue using attrType ("true" or "false") |
| cache\_flash\_interval\_sec | no | 0 | 0 if notified data will be persisted to Elasticsearch immediately. positive integer if notified data are cached on NGSIElasticsearchSink's memory and will be persisted to Elasticsearch periodically every `cache_flash_interval_sec` |
| backend.max\_conns | no | 500 | Maximum number of connections allowed for a Http-based Elasticsearch backend |
| backend.max\_conns\_per\_route | no | 100 | Maximum number of connections per route allowed for a Http-based Elasticsearch backend |

A configuration example could be:

    cygnus-ngsi.sinks = elasticsearch-sink
    cygnus-ngsi.channels = elasticsearch-channel
    ...
    cygnus-ngsi.sinks.elasticsearch-sink.type = com.telefonica.iot.cygnus.sinks.NGSIElasticsearchSink
    cygnus-ngsi.sinks.elasticsearch-sink.channel = elasticsearch-channel
    cygnus-ngsi.sinks.elasticsearch-sink.elasticsearch_host = elasticsearch.local
    cygnus-ngsi.sinks.elasticsearch-sink.elasticsearch_port = 9200
    cygnus-ngsi.sinks.elasticsearch-sink.ssl = false
    cygnus-ngsi.sinks.elasticsearch-sink.index_prefix = cygnus
    cygnus-ngsi.sinks.elasticsearch-sink.mapping_type = cygnus_type
    cygnus-ngsi.sinks.elasticsearch-sink.ignore_white_spaces = true
    cygnus-ngsi.sinks.elasticsearch-sink.attr_persistence = row
    cygnus-ngsi.sinks.elasticsearch-sink.timezone = UTC
    cygnus-ngsi.sinks.elasticsearch-sink.cast_value = false
    cygnus-ngsi.sinks.elasticsearch-sink.cache_flash_interval_sec = 0
    cygnus-ngsi.sinks.elasticsearch-sink.backend.max_conns = 500
    cygnus-ngsi.sinks.elasticsearch-sink.backend.max_conns_per_route = 100

[Top](#top)

### <a name="section2.2"></a>Use cases
Use `NGSIElasticsearchSink` if you are looking for a Json-based full-text search engine.

[Top](#top)

### <a name="section2.3"></a>Important notes
#### <a name="section2.3.1"></a>About caching
`NGSIElasticsearchSink` stores the date to Elasticsearch by using [Elasticsearch's REST API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html) (see [elasticsearch\_backend.md](/doc/cygnus-common/backends_catalogue/elasticsearch_backend.md)). Unfortunately this round trip needs a bit time. Therefore, when NGSIElasticsearchSink is notified high frequently, it may be delayed until the notified data is stored in Elasticsearch.

In such a case, please consider using `cache_flash_interval_sec`. When you set the `cache_flash_interval_sec` parameter as not zero positive integer, the data to be stored are cached temporally in cygnus's process, and bulk inserted to Elasticsearch every configured seconds. When Cygnus is shutted down gracefully, the cached data will be flashed and be stored immediatelry to Elasticsearch.

**CAUTION**  
This `cache_flash_interval_sec` parameter is useful to improve the storing performance. However, if the Cygnus process aborts unexpectedly, the cached data may be lost and never recovered. When you set the `cache_flash_interval_sec` parameter, you have to always consider **the risk that the cached data may be lost**.

[Top](#top)

#### <a name="section2.3.2"></a>About `recvTime` and `TimeInstant` metadata
By default, `NGSIElasticsearchSink` stores the notification reception timestamp. Nevertheless, if a metadata named `TimeInstant` is notified, then such metadata value is used instead of the reception timestamp.

* Row-like storing

When NGSIElasticsearchSink is configured as `Row-like storing` and the notified attribute has `TimeInstant` metadata, its `TimeInstant` is used as the timestamp of the attribute record.

* Column-like storing

When NGSIElasticsearchSink is configured as `Column-like storing` and the **first** notified attribute has `TimeInstant` metadata, the **first** `TimeInstant` is used as the timestamp of the document record.

[Top](#top)

#### <a name="section2.3.3"></a>About supported versions of Elasticsearch
`NGSIElasticsearchSink` has been tested with the following versions of Elasticsearch:

* 6.3

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIElasticsearchSink` class
As any other NGSI-like sink, `NGSIElasticsearchSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the Elasticsearch index where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `ElasticsearchBackend` implementation.  
If `cache_flash_interval_sec` is not configured or zero, the notifed data is stored to Elasticsearch immediately at this time. Otherwise, the notified data are cached in Cygnus's process and are not stored at this time.

    public void start();

An implementation of `ElasticsearchBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIElasticsearchSink()` (contructor), `configure()` and `start()`.  
If `cache_flash_interval_sec` is configured, `SingleThreadScheduledExecutor` is created. In this case, this executer has the responsibility for storing cached data to Elasticsearch.

    public void stop();

If `cache_flash_interval_sec` is configured, the created executer stores the cached data if exist before the Cygnus process will be shutted down.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>`ElasticsearchBackend` class
This is a convenience backend class for Elasticsearch that provides methods to persist the context data to Elasticsearch. `ElasticsearchBackend` uses the REST API of Elasticsearch in order to persist the context data, so `ElasticsearchBackend` extends the `HttpBackend`. The public methods are:

    public JsonResponse bulkInsert(String index, String type, List<Map<String, String>> data) throws CygnusPersistenceError, CygnusRuntimeError;

The given `data` is bulk inserted to `index` of Elasticsearch. The mapping type of this `index` is created automatically as named `type`.

### <a name="section3.3"></a>Authentication and authorization
Elasticsearch has no built-in mechanism to authenticate and authorize the API requester. Thus, `NGSIElasticsearchSink` has no functionality about authentication and authorization.

[Top](#top)
