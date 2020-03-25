# Elasticsearch backend
## `ElasticsearchBackend` interface
This interface enumerates the methods any [Elasticsearch](https://www.elastic.co/products/elasticsearch) backend implementation must expose. In this case, the following ones:

    JsonResponse bulkInsert(String index, String type, List<Map<String, String>> data) throws CygnusPersistenceError, CygnusRuntimeError;

> Bulk-inserts the given `data` to `index` of Elasticsearch. The mapping type of this `index` is created automatically as named `type`.
## `ElasticsearchBackendImpl` class
This is a convenience backend class for Elasticsearch that implements the `ElasticsearchBackend` interface described above.

`ElasticsearchBackendImpl` uses the REST API of Elasticsearch in order to persist the context data, so `ElasticsearchBackend` extends the `HttpBackend`.

The `charset` parameter is used to encode attribute values when sending them to Elasticsearch through its REST API. "UTF-8" is used to encode attribute values when a valid `charset` parameter is not given.
