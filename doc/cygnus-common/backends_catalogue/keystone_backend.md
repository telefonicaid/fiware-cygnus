# Keystone backend
## `KeystoneBackend` interface
This class enumerates the methods any [Keystone] backend implementation must expose. In this case, the following ones:

    String getSessionToken(String user, String password, String fiwareService, String fiwareServicePath) 
    throws CygnusRuntimeError, CygnusPersistenceError;
    
> Gets KeyStone session token given user and password and using tokens cache.
    
    String updateSessionToken(String user, String password, String fiwareService, String fiwareServicePath)
            throws CygnusRuntimeError, CygnusPersistenceError;
 
> Forces token update skipping cache.
   
## `KeystoneBackendImpl` class
This is a convenience backend class for Keystone that implements the `KeystoneBackend` interface described above.

`KeystoneBackendImpl` really wraps the FIWARE-IOT-Stack Authentication API ([Auth API](http://fiware-iot-stack.readthedocs.io/en/latest/authentication_api/index.html))