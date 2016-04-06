#<a name="top"></a>OrionRestHandler
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to flume events](#section1.1)
    * [Additional headers added by Flume interceptors](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
* [Programmers guide](#section3)
    * [`OrionRestHandler` class](#section3.1)

##<a name="section1"></a>Functionality
###<a name="section1.1"></a>Mapping NGSI events to flume events
This section explains how a notified NGSI event (an http message) containing context data is converted into a Flume event (an object in memory or a file), suitable for being consumed by any of the Cygnus sinks, thanks to `OrionRestHandler`.

It is necessary to remark again this handler is designed for being used by `HttpSource`, the native component of Apache Flume. An http message containing a NGSI-like notification will be received by `HttpSource` and passed to `OrionRestHandler` in order to create one, and only one, Flume event object to be put in a sink's channel (mainly, these channels are objects in memory, or files).

On the one hand, the http message containing the NGSI-like notification will be composed of a set of http headers, and a payload. On the other hand, a Flume event object is composed of a set of headers, and a body. As can be seen, there is a quasi-direct translation among http message and Flume event object:

| http message | Flume event object |
|---|---|
| `Content-Type` header | `content-type` header (discarded from 0.14.0) |
| `Fiware-Service` header | `fiware-service` header |
| `Fiware-ServicePath` header | `fiware-servicepath` header |
| `Fiware-Correlator` header | `fiware-correlator` header |
| any other header | discarded |
| payload | body |

All the FIWARE headers are added to the Flume event object if notified. If not, default values are used (it is the case of `fiware-service` and `fiware-servicepath`, which take the configured value of `default_service` and `default_service_path` respectively, see below the configuration section) or auto-generated (it is the case of `fiware-correlator`).

In addition to the `fiware-correlator`, a `transaction-id` is created for internally identify a complete Cygnus transaction, i.e. starting at the source when the context data is notified, and finishing in the sink, where such data is finally persisted. If `Fiware-Correlator` header is not notified, then `fiware-correlator` and `transactionid` get the same auto-generated value.

[Top](#top)

###<a name="section1.2"></a>Additional headers added by Flume interceptors
Despite all the details about interceptors used in Cygnus are widely documented [here](./grouping_interceptor.md), it is worth reminding that:

* An interceptor is a piece of code in charge of "intercepting" events before they are put in the sink's channel and modifying them by adding/removing/modifying a header.
* A `timestamp` header is added by the native `TimestampInterceptor`. It is expressed as a Unix time.
* A `notified-entities` header is added by the custom `GroupingInterceptor`. This header contains one <i>default destination</i> per each notified context element. It is used by the sinks when the grouping rules are not enabled.
* A `notified-servicepaths` header is added by the custom `GroupingInterceptor`. This header contains one <i>default service path</i> per each notified context element. It is used by the sinks when the grouping rules are not enabled.
* A `grouped-entities` header is added by the custom `GroupingInterceptor`. This header contains one <i>grouped destination</i> per each notified context element. It is used by the sinks when the grouping rules are enabled.
* A `grouped-servicepath` header is added by the custom `GroupingInterceptor`. This header contains one <i>grouped service path</i> per each notified context element. It is used by the sinks when the grouping rules are enabled.


[Top](#top)

###<a name="section1.3"></a>Example
A NGSI-like event example could be (the code below is an <i>object representation</i>, not any real data format; look for it at [Orion documentation](https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/Publish/Subscribe_Broker_-_Orion_Context_Broker_-_User_and_Programmers_Guide#ONCHANGE)):

    ngsi-event={
        http-headers={
            Content-Length: 492
            Host: localhost:1028
            Accept: application/json
            Content-Type: application/json
            Fiware-Service: vehicles
            Fiware-ServicePath: 4wheels
            Fiware-Correlator: ABCDEF1234567890
        },
        payload={
            {
                "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
                "originator" : "localhost",
                "contextResponses" : [
                    {
                        "contextElement" : {
                        "attributes" : [
                            {
                                "name" : "speed",
                                "type" : "float",
                                "value" : "112.9",
                                "metadatas": []
                            },
                            {
                                "name" : "oil_level",
                                "type" : "float",
                                "value" : "74.6",
                                "metadatas": []
                            }
                        ],
                        "type" : "car",
                        "isPattern" : "false",
                        "id" : "car1"
                    },
                    "statusCode" : {
                        "code" : "200",
                        "reasonPhrase" : "OK"
                    }
                ]
            }
        }
    }

As said, Flume events are not much more different than the above representation: there is a set of headers and a body. This is an advantage, since allows for a quick translation between formats. The equivalent <i>object representation</i> (not any real data format) for such a notified NGSI event could be the following Flume event:

    flume-event={
        headers={
	         timestamp=1429535775,
	         fiware-service=vehicles,
	         fiware-servicepath=/4wheels,
	         fiware-correlator=ABCDEF1234567890
	         notified-entities=car1_car
	         notified-servicepaths=/4wheels
	         grouped-entities=car1_car
	         grouped-servicepath=/4wheels
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
	             }
	         ]
	     }
    }

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`OrionRestHandler` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| notification\_target | no | `notify/` | Any other configured value must start with `/`. |
| default\_service | no | `default` || 
| default\_service\_path | no | `/` | `/` is the root service path (also know as root subservice). Any other configured value must start with `/`. |

A configuration example could be:

    cygnusagent.sources = http-source
    ...
    cygnusagent.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.OrionRestHandler
    cygnusagent.sources.http-source.notification_target = /notify
    cygnusagent.sources.http-source.default_service = default
    cygnusagent.sources.http-source.default_service_path = /

[Top](#top)

##<a name="section3"></a>Programmers guide
###<a name="section3.1"></a>`OrionRestHandler` class
TBD

[Top](#top)