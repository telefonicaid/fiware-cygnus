# <a name="top"></a>IPv6 support
Content:

* [Service endpoint](#section1)
* [API](#section2)
* [GUI](#section3)

## <a name="section1"></a>Service endpoint 
Native Flume Http sources supprt IPv6, therefore cygnus-ngsi supports IPv6 at its service endpoint.

It is just a matter of configuring the Http source `bind` parameter (which by default takes the value `127.0.0.1` when not explicitely configured) to `::` (undefined address) or `::1` (IPv6 localhost).

For instance, let's suppose a cygnus-ngsi focused on persisting NGSI data in MySQL. IPv6 is enabled in the Http source as said above:

```
cygnus-ngsi.sources = http-source
cygnus-ngsi.sinks = mysql-sink
cygnus-ngsi.channels = mysql-channel

cygnus-ngsi.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnus-ngsi.sources.http-source.channels = mysql-channel
cygnus-ngsi.sources.http-source.bind = ::
cygnus-ngsi.sources.http-source.port = 5050
...
```

[Top](#top)

## <a name="section2"></a>API
Currently, the host part of the API binding is harcoded to the IPv4 undefined address, i.e. `0.0.0.0`. Thus, IPv6 cannot be enabled.

[Top](#top)

## <a name="section3"></a>GUI
Currently, the host part of the GUI binding is harcoded to the IPv4 undefined address, i.e. `0.0.0.0`. Thus, IPv6 cannot be enabled.

[Top](#top)
