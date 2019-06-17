# Elasticsearch backend
## `ElasticsearchBackend` interface
This interface enumerates the methods any [Elasticsearch](https://www.elastic.co/products/elasticsearch) backend implementation must expose. In this case, the following ones:

    JsonResponse bulkInsert(String index, String type, List<Map<String, String>> data) throws CygnusPersistenceError, CygnusRuntimeError;

> Bulk-inserts the given `data` to `index` of Elasticsearch. The mapping type of this `index` is created automatically as named `type`.
## `ElasticsearchBackendImpl` class
This is a convenience backend class for Elasticsearch that implements the `ElasticsearchBackend` interface described above.

`ElasticsearchBackendImpl` uses the REST API of Elasticsearch in order to persist the context data, so `ElasticsearchBackend` extends the `HttpBackend`.

Nothing special is done in this class with regards to the encoding, because Cygnus generally works with UTF-8 character set and Elasticsearch can accepts the UTF-8.
