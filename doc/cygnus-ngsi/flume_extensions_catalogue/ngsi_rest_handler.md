# <a name="top"></a>NGSIRestHandler
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Example](#section1.2)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Accepted character set](#section2.2)
* [Programmers guide](#section3)
    * [`NGSIRestHandler` class](#section3.1)

## <a name="section1"></a>Functionality
### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
This section explains how a notified NGSI event (a http message containing headers and payload) is used to create a `NGSIEvent` object, suitable for being consumed by any of the Cygnus sinks, thanks to `NGSIRestHandler`.

It is necessary to remark again this handler is designed for being used by `HttpSource`, the native component of Apache Flume. An http message containing a NGSI-like notification will be received by `HttpSource` and passed to `NGSIRestHandler` in order to create one or more `NGSIEvent` objects (one per notified context element) to be put in a sink's channel (mainly, these channels are objects in memory, but could be files).

On the one hand, the http message containing the NGSI-like notification will be composed of a set of http headers, and a payload. On the other hand, the `NGSIEvent` objects are composed of a set of headers as well and an object of type `ContextElement` containing the already parsed version of the context elements within the notification; this parsed version of the notified body is ready for being consumed by other components in the agent architecture, such as interceptors or sinks, thus parsing is just made once.

As can be seen, there is a quasi-direct translation among http messages and `NGSIEvent` objects:

| http message | `NGSIEvent` object |
|---|---|
| `Fiware-Service` header | `fiware-service` header |
| `Fiware-ServicePath` header | `fiware-servicepath` header |
| `Fiware-Correlator` header | `fiware-correlator` header. If this header is not sent, the `fiware-correlator` is equals to the `transaction-id` header. |
|| `transaction-id` header (internally added) |
| any other header | discarded |
| payload | `ContextElement` object containing the parsed version of the payload |

All the FIWARE headers are added to the `NGSIEvent` object if notified. If not, default values are used (it is the case of `fiware-service` and `fiware-servicepath`, which take the configured value of `default_service` and `default_service_path` respectively, see below the configuration section) or auto-generated (it is the case of `fiware-correlator`, whose value is the same than `transaction-id`).

As already introduced, in addition to the `fiware-correlator`, a `transaction-id` is created for internally identify a complete Cygnus transaction, i.e. starting at the source when the context data is notified, and finishing in the sink, where such data is finally persisted. If `Fiware-Correlator` header is not notified, then `fiware-correlator` and `transactionid` get the same auto-generated value.

Finally, it must be said the `NGSIEVent` contains another field, of type `ContextElement` as well, in order the `NGSINameMappingsInterceptor` add a mapped version of the original context element added by this handler.

[Top](#top)

### <a name="section1.2"></a>Example
Let's assume the following not-intercepted event regarding a received notification (the code below is an <i>object representation</i>, not any real data format):

```
notification={
   headers={
	   fiware-service=hotel1,
	   fiware-servicepath=/other,/suites,
	   correlation-id=1234567890-0000-1234567890
   },
   body={
      {
	      entityId=suite.12,
	      entityType=room,
	      attributes=[
	         ...
	      ]
	   },
	   {
	      entityId=other.9,
	      entityType=room,
	      attributes=[
	         ...
	      ]
	   }
	}
}
```

As can be seen, two entities (`suite.12` and `other.9`) of the same type (`room`) within the same FIWARE service (`hotel`) but different service paths (`/suites` and `/other`) are notified. `NGSIRestHandler` will create two `NGSIEvent`'s:


```
ngsi-event-1={
   headers={
	   fiware-service=hotel,
	   fiware-servicepath=/suites,
	   transaction-id=1234567890-0000-1234567890,
	   correlation-id=1234567890-0000-1234567890,
	   timestamp=1234567890,
	   mapped-fiware-service=hotel
	   mapped-fiware-service-path=/suites
	},
   original-context-element={
	   entityId=suite.12,
	   entityType=room,
	   attributes=[
	      ...
	   ]
	}
}
    
ngsi-event-2={
   headers={
	   fiware-service=hotel,
	   fiware-servicepath=/other,
	   transaction-id=1234567890-0000-1234567890,
	   correlation-id=1234567890-0000-1234567890,
	   timestamp=1234567890,
	   mapped-fiware-service=hotel
	   mapped-fiware-service-path=/other
   },
   original-context-element={
	   entityId=other.9,
	   entityType=room,
	   attributes=[
	      ...
	   ]
	}
}
```

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIRestHandler` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| notification\_target | no | `notify/` | Any other configured value must start with `/`. |
| default\_service | no | `default` | Alphanumerics and underscores are only accepted. | 
| default\_service\_path | no | `/` | `/` is the root service path (also know as root subservice). Any other configured value must start with `/`. Apart from the initial slash, alphanumerics and underscores are only accepted. |

A configuration example could be:

    cygnus-ngsi.sources = http-source
    ...
    cygnus-ngsi.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
    cygnus-ngsi.sources.http-source.notification_target = /notify
    cygnus-ngsi.sources.http-source.default_service = default
    cygnus-ngsi.sources.http-source.default_service_path = /

[Top](#top)

### <a name="section2.2"></a>Accepted character set
This handler for NGSI only works with UTF-8 encoding. Thus, notifications must send a `Content-Type` header with `application/json; charset=utf-8` as value. Any other content type wont be considered and the notification will be discarded.

It is expected UTF-8 character set is maintained by all the Flume elements in the configuration, in order the final sinks (or their backend abstractions, if they exist) compose their writes/inserts/upserts by properly specifying this kind of encoding.

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIRestHandler` class
TBD

[Top](#top)
