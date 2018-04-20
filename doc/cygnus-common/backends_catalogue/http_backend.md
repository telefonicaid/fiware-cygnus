# Http backend
## `HttpBackend` class
This class enumerates the methods any [HTTP] backend implementation must expose. In this case, the following ones:

    public HttpBackend(String host, String port, boolean ssl, boolean krb5, String krb5User, String krb5Password, String krb5LoginConfFile, String krb5ConfFile, int maxConns, int maxConnsPerRoute)

> Does a Http request given a method, a relative URL (the final URL will be composed by using this relative URL and the active Http endpoint), a list of headers and the payload.

    public JsonResponse doRequest(String method, String url, boolean relative, ArrayList<Header> headers, StringEntity entity) throws     CygnusRuntimeError, CygnusPersistenceError;

> Does a Http request given a method, a relative URL, a list of headers and the payload Protected method due to it's used by the tests.

    protected JsonResponse doRequest(String method, String url, ArrayList<Header> headers, StringEntity entity) throws CygnusRuntimeError, CygnusPersistenceError

> Starts a transaction. Basically, this means the byte counters are reseted.

    public void startTransaction();

> It sends a HTTP petition.

## `HttpClientFactory` class
Coming soon.

## `JsonResponse` class
 This is a class that it has a json Response, status code, Headers and soon.

## `KerberosCallbackHandler` class
Coming soon.
