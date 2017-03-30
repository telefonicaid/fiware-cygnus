# CKAN backend
## `CKANBackend` interface
This class enumerates the methods any [CKAN](http://ckan.org/) backend implementation must expose. In this case, the following ones:

    void persist(String orgName, String pkgName, String resName, String records, boolean createEnabled) throws Exception;
    
> Persists the aggregated context data regarding a single entity's attribute (row mode) or a full list of attributes (column mode) within the datastore associated to the given resource. This resource belongs to the given package/dataset, which in the end belongs to the given organization as well. This method creates the parts of the hierarchy (organization, package/dataset, resource and datastore) if any of them is missing.

## `CKANBackendImpl` class
This is a convenience backend class for CKAN that extends the `HttpBackend` abstract class (which provides common logic for any Http connection-based backend) and implements the `CKANBackend` interface described above.

`CKANBackendImpl` really wraps the [CKAN API](http://docs.ckan.org/en/latest/api/).

It must be said this backend implementation enforces UTF-8 encoding through the usage of a `Content-Type` http header with a value of `application/json; charset=utf-8`.

## `CKANCache` class
This class is used to improve the performance of `NGSICKANSink` by caching information about the already created organizations, packages/datasets and resources (and datastores). `CKANCache` implements the `HttpBackend` interface since its methods are able to interact directly with CKAN API when some element of the hierarchy is not cached.

In detail, this is the workflow when `NGSICKANSink` is combined with `CKANCache`:

1. `NGSICKANSink`, previously to accessing CKAN API (it consumes a lot of computational resources), queries the cache for the data (stored in memory, faster and efficient), in order to know if the different elements of the hierarchy involved in the persistence operation are already created or not.
2. If the element is cached, then a single upsert operation is done against the CKAN API.
3. If the element is not cached, CKAN is queried in order to get the information. If the element was not found, `NGSICKANSink` is informed about that. If the element was found, it is cached for future queries and `NGSICKANSink` performs an upsert operation against the CKAN API.
4. If the element was not found in the cache nor in CKAN, it is created by `NGSICKANSink`. Then, an upsert operation is performed.
