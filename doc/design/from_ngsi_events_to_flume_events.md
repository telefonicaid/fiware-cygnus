#From NGSI events to Flume events
This document explains how a notified NGSI event containing context data is converted into a Flume event, suitable for being consumed by any of the Cygnus sinks.

A NGSI-like event example could be (the code below is an <i>object representation</i>, not any real data format):

    ngsi-event={
        http-headers={
            Content-Length: 492
            User-Agent: orion/0.9.0
            Host: localhost:1028
            Accept: application/xml, application/json
            Content-Type: application/json
            Fiware-Service:vehicles
            Fiware-ServicePath: 4wheels 
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
                              "type" : "kmh",
                              "value" : "112.9",
                              "metadatas": [
                                 {
                                    "name": "meter",
                                    "type": "string",
                                    "value": "digital"
                                 }
                              ]
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
                  }
               ]
            }
         }

Flume events are not much more different than the above representation: there is a set of headers and a body. This is an advantage, since allows for a quick translation between formats. The equivalent <i>object representation</i> (not any real data format) for such a notified NGSI event could be the following Flume event:

    flume-event={
        headers={
	        content-type=application/json,
	        fiware-service=vehicles,
	        fiware-servicepath=4wheels,
	        timestamp=1429535775,
	        transactionId=1429535775-308-0000000000,
	        ttl=10,
	        destination=car1_car
        },
        body={
	        entityId=car1,
	        entityType=car,
	        attributes=[
	            {
	                attrName=speed,
	                attrType=kmh,
	                attrValue=112.9
	            }
	        ]
	    }
    }

The headers are a subset of the notified HTTP headers and others added by Cygnus interceptors (see [doc/interceptors.md](interceptors.md) for more details):

* The <b>content-type</b> header is a replica of the HTTP header. It is needed for the different sinks to know how to parse the event body. In this case it is JSON.
* Note that Orion can include a `Fiware-Service` HTTP header specifying the tenant/organization associated to the notification, which is added to the event headers as well (as `fiware-service`). Since version 0.3, Cygnus is able to support this header, although the actual processing of such tenant/organization depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service specified in the `default_service` configuration property. Please observe that the notified `fiware-service` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* Orion can notify another HTTP header, `Fiware-ServicePath` specifying a subservice within a tenant/organization, which is added to the event headers as well (as `fiware-servicepath`). Since version 0.6, Cygnus is able to support this header, although the actual processing of such subservice depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service path specified in the `default_service_path` configuration property. Please observe that the notified `fiware-servicePath` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* The notification reception time is included in the list of headers (as <b>timestamp</b>) for timestamping purposes in the different sinks. It is added by a native interceptor. See the [doc/design/interceptors.md](doc/design/interceptors.md) document for more details.
* The <b>transactionId</b> identifies a complete Cygnus transaction, starting at the source when the context data is notified, and finishing in the sink, where such data is finally persisted.
* The time-to-live (or <b>ttl</b>) specifies the number of re-injection retries in the channel when something goes wrong while persisting the data. This re-injection mechanism is part of the reliability features of Flume. -1 means inifinite retries.
* The <b>destination</b> headers is used to identify the persistence element within the used storage, i.e. a file in HDFS, a MySQL table or a CKAN resource. This is added by a custom interceptor called `DestinationExtractor` added to the Flume's suite. See the <i>doc/design/interceptors</i> document for more details.

The body simply contains a byte representation of the HTTP payload that will be parsed by the sinks.

## Contact
Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**