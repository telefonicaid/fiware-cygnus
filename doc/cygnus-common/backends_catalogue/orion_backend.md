# Orion backend
## `OrionBackend` interface
This class enumerates the methods any [Orion](https://github.com/telefonicaid/fiware-orion) backend implementation must expose. In this case, the following ones:

    JsonResponse subscribeContextV2(String cygnusSubscription, String token) throws Exception;
 
> Subscribes to Orion given a Json Cygnus subscription (Orion subscription + Orion endpoint) and a token for authentication purposes.
   
    JsonResponse deleteSubscriptionV2(String subscriptionId, String token) throws Exception;

> Deletes a subscription to Orion given its ID and a token for authentication purposes.
    
    JsonResponse getSubscriptionsByIdV2(String token, String subscriptionId) throws Exception;

> Gets a subscription to Orion given its ID and a token for authentication purposes.
    
    void updateRemoteContext(String bodyJSON, String orionToken, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError, UnsupportedEncodingException, CygnusBadAuthorization,JSONException;

> Create or update entities in a remote Orion.

### Deprecated methods

The class also exposes the following *deprecated* methods:

    JsonResponse subscribeContextV1(String cygnusSubscription, String token) throws Exception;

> Subscribes to Orion given a Json Cygnus subscription (Orion subscription + Orion endpoint) and a token for authentication purposes (NGSIv1).

    JsonResponse deleteSubscriptionV1(String subscriptionId, String token) throws Exception;

> Deletes a subscription to Orion given its ID and a token for authentication purposes (NGSIv1).

## `OrionBackendImpl` class
This is a convenience backend class for Orion that implements the `OrionBackend` interface described above.

`OrionBackendImpl` really wraps the Orion [NGSIv2 API](http://telefonicaid.github.io/fiware-orion/api/v2/latest/)).
