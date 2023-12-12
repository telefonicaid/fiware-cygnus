# <a name="top"></a>NGSIArcgisFeatureTableSink
Content:

* [Functionality](#section1)
    * [Mapping NGSI events to `NGSIEvent` objects](#section1.1)
    * [Mapping `NGSIEvent`s to Arcgis](#section1.2)
        * [ArcGis layers naming conventions](#section1.2.1)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Important notes](#section2.2)
        * [About batching](#section2.2.1)
        * [About case-sensitivity](#section2.2.2)
        * [About Arcgis data types](#section2.2.3)
* [Programmers guide](#section3)
    * [`NGSIArcgisFeatureTableSink` class](#section3.1)
    * [Authentication and authorization](#section3.2)

## <a name="section1"></a>Functionality
`com.iot.telefonica.cygnus.sinks.NGSIArcgisFeatureTableSink` is a sink designed to persist NGSI-like context data events within an [ArcGis] (https://www.arcgis.com/home/index.html) feature table. Usually, such a context data is notified by a [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) instance, but could be any other system speaking <i>NGSI language</i>.

Independently of the data generator, NGSI context data is always transformed into internal `NGSIEvent` objects at Cygnus sources. In the end, the information within these events must be mapped into specific ArcGis structures.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping NGSI events to `NGSIEvent` objects
Notified NGSI events (containing context data) are transformed into `NGSIEvent` objects (for each context element a `NGSIEvent` is created; such an event is a mix of certain headers and a `ContextElement` object), independently of the NGSI data generator or the final backend where it is persisted.

This is done at the cygnus-ngsi Http listeners (in Flume jergon, sources) thanks to [`NGSIRestHandler`](/ngsi_rest_handler.md). Once translated, the data (now, as `NGSIEvent` objects) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping `NGSIEvent`s to ArcGis
ArcGis stores data in it's  own databases using it's own data organization, you can checkout this info Reading Feature Table details at Arcgis server, Such organization is exploited by `NGSIArcgisFeatureTableSink` each time a `NGSIEvent` is going to be persisted.
Arcgis feature tables must be provisioned before sending entities.

[Top](#top)

#### <a name="section1.2.1"></a>ArcGis databases naming conventions
Each entity type needs an url and an unique field to be persisted into the feature table.

NGSIArcgisFeatureTableSink composes each table's url with entitie's `service` and `service path`, to provide multiple tables access. The final url is composed of `cygnus-ngsi.sinks.arcgis-sink.arcgis_service_url`+`fiware-service`+`fiware-servicepath`. 

Unique field is provided to allow `NGSIArcgisFeatureTableSink` to update existant entities. NGSI `entity type` will be used as unique field name. This means that a feature named `type` in the Feature Table cannot be filled in by the sink. If Feature Table needs to persist the value of entity type it has to be in a field different than `type`.

All this parameters, can be customized using Context Broker custom notifications (preferred) or Cygnus mapping capabilities (should be avoided except in justified cases).

Assuming that the feature table's url: `https://arcgis.com/{hash}/arcgis/rest/services/vehicles/cars` and feature table definition is:
```
Fields:

    objectid ( type: esriFieldTypeOID, alias: objectid, editable: false, nullable: false, defaultValue: null, modelName: objectid )
    licensePlate ( type: esriFieldTypeString, alias: licensePlate, editable: true, nullable: true, length: 255, defaultValue: null, modelName: licensePlate )
    speed ( type: esriFieldTypeDouble, alias: speed, editable: true, nullable: true, defaultValue: null, modelName: speed )
    oillevel ( type: esriFieldTypeDouble, alias: oillevel, editable: true, nullable: true, defaultValue: null, modelName: oillevel )
```

Let's see both configuration options:

##### Using Context Broker custom notifications (preferred)

["CB custom notifications"](https://github.com/telefonicaid/fiware-orion/blob/master/doc/manuals/orion-api.md#custom-notifications) is the preferred option because of its simplicity and not having to manage configuration (name-mappings) files in the server. 

###### Entity data in CB:

	service = vehicles
	service-path = /4wheels
	entity-type = Car

If the Feature table for type "Car" is `https://arcgis.com/{hash}/arcgis/rest/services/vehicles/cars`, the subscription with custom notif would be:

```
  {
    "description": "Subs arcgis",
    "status": "active",
    "subject": {
      "entities": [
        {
          "idPattern": ".*",
          "type": "Car"
        }
      ],
      "condition": {
        "attrs": [
          "speed",
          "oilLevel"
        ]
      }
    },
    "notification": {
      "attrs": [
        "speed",
        "oilLevel"
      ],
      "onlyChangedAttrs": false,
      "attrsFormat": "normalized",
      "httpCustom": {
        "url": "http://iot-cygnus:<source_arcgis_port>/notify",
          "ngsi": {
            "type": "licensePlate"
          },
          "headers": {
            "fiware-service": "vehicles",
            "fiware-servicepath": "/cars"
          }
        }
    }
  }
```

Note that to avoid using the name mappings to modify the unique field value of `type` attribute, it is required the use of [ngsi patching functionality](https://github.com/telefonicaid/fiware-orion/blob/master/doc/manuals/orion-api.md#ngsi-payload-patching).

###### result

	Feature table url: https://arcgis.com/{hash}/arcgis/rest/services/vehicles/car
	Table's unique field: licensePlate

[Top](#top)

##### Using Cygnus name mappings

Let's see an example:	

###### Agent.conf file:

	agent.arcgis-sink.arcgis_service_url = https://arcgis.com/{hash}/arcgis/rest/services
        agent.arcgis-sink.enable_name_mappings = true
###### Entity data:

	service = vehicles
	service-path = /4wheels
	entity-type = Car

The name mappings configuration would be:

```
{
  "serviceMappings": [{
      "originalService": "vehicles",
      "servicePathMappings": [{
          "originalServicePath": "/4wheels",
          "newServicePath": "/cars",
          "entityMappings": [{
              "originalEntityType": "Car",
              "newEntityType": "licensePlate",
              "originalEntityId": "^.*"
            }
          ]
        }
      ]
    }
  ]
}
```
 
###### result

	Feature table url: https://arcgis.com/{hash}/arcgis/rest/services/vehicles/cars
	Table's unique field: licensePlate

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIArcgisFeatureTableSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.NGSIArcgisFeatureTableSink</i> |
| channel | yes | N/A ||
| enable_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. ||
| enable\_name\_mappings | no | false | <i>true</i> or <i>false</i>. Check this [link](./ngsi_name_mappings_interceptor.md) for more details. ||
| arcgis\_service\_url | yes | N/A | https://{url\_host}/{id\_arcgis}/arcgis/rest/services|
| arcgis\_username | yes | N/A |  |
| arcgis\_password | yes | N/A |  |
| arcgis\_gettoken\_url | yes | N/A |https://{url\_host}/sharing/generateToken|
| arcgis\_maxBatchSize | no | 10 | Number of events accumulated before persistence. |
| batch\_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch\_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |


A configuration example could be:

    cygnus-ngsi.sinks = arcgis-sink
    cygnus-ngsi.channels = arcgis-channel
    ...
    cygnus-ngsi.sinks.arcgis-sink.type = com.telefonica.iot.cygnus.sinks.NGSIArcgisFeatureTableSink
    cygnus-ngsi.sinks.arcgis-sink.channel = arcgis-channel
    cygnus-ngsi.sinks.arcgis-sink.enable_name_mappings = true
    cygnus-ngsi.sinks.arcgis-sink.arcgis_service_url = https://arcgis.com/UsuarioArcgis/arcgis/rest/services
    cygnus-ngsi.sinks.arcgis-sink.arcgis_username = myuser
    cygnus-ngsi.sinks.arcgis-sink.arcgis_password = mypassword
    cygnus-ngsi.sinks.arcgis-sink.arcgis_gettoken_url = https://arcgis.com/sharing/generateToken
    cygnus-ngsi.sinks.arcgis-sink.arcgis_maxBatchSize = 10

[Top](#top)

### <a name="section2.2"></a>Important notes

#### <a name="section2.2.1"></a>About batching
As explained in the [programmers guide](#section3), `NGSIArcgisFeatureTableSink` extends `NGSISink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows extending classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 10 `NGSIEvent`s. In the best case, all these events regard to the same type of entity, which means all the data within them will be persisted in the same ArcGis layer. If processing the events one by one, we would need 10 inserts into ArcGis; nevertheless, in this example only one insert is required. Obviously, not all the events will always regard to the same unique type of entity, and many entities may be involved within a batch. 

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

Regarding the retries of not persisted batches, a couple of parameters is used. On the one hand, a Time-To-Live (TTL) is used, specifing the number of retries Cygnus will do before definitely dropping the event. On the other hand, a list of retry intervals can be configured. Such a list defines the first retry interval, then se second retry interval, and so on; if the TTL is greater than the length of the list, then the last retry interval is repeated as many times as necessary.

By default, `NGSIArcgisFeatureTableSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage. A deeper discussion on the batches of events and their appropriate sizing may be found in the [performance document](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/performance_tips.md).

Connections to `cygnus-ngsi.sinks.arcgis-sink.arcgis_service_url` and `cygnus-ngsi.sinks.arcgis-sink.arcgis_service_url` are done without check certificate validation of Java SSL Connections.

[Top](#top)

#### <a name="section2.2.2"></a>About case-sensitivity

**[FIXME #2320](https://github.com/telefonicaid/fiware-cygnus/issues/2320)**. Currently Arcgis sink is case sensitive with the attributes to persist in the Feature Table although arcgis is not case sensitive. This behaviour requires the use of name-mappings to match the case letters of the attribute's definition in the Feature Table.

For instance, if we have the following Feature Layer definition for the "Car" entity type:
```
Fields:

    objectid ( type: esriFieldTypeOID, alias: objectid, editable: false, nullable: false, defaultValue: null, modelName: objectid )
    licensePlate ( type: esriFieldTypeString, alias: licensePlate, editable: true, nullable: true, length: 255, defaultValue: null, modelName: licensePlate )
    speed ( type: esriFieldTypeDouble, alias: speed, editable: true, nullable: true, defaultValue: null, modelName: speed )
    oillevel ( type: esriFieldTypeDouble, alias: oillevel, editable: true, nullable: true, defaultValue: null, modelName: oillevel )
```

And the model definition of the `Car` is:

```
{
    "id": "car1",
    "type": "Car",
    "location": {
        "type": "geo:json",
        "value": {
            "coordinates": [
                -0.350062,
                40.054448
            ],
            "type": "Point"
        }
    },
    "speed": {
        "type": "Number",
        "value": 112.9
    },
    "oilLevel": {
        "type": "Number",
        "value": 74.6
    }
}
```

The name mappings required to persist the attributes is:

```
{
  "serviceMappings": [{
      "originalService": "vehicles",
      "servicePathMappings": [{
          "originalServicePath": "/4wheels",
          "newServicePath": "/cars",
          "entityMappings": [{
              "originalEntityType": "Car",
              "newEntityType": "licensePlate",
              "originalEntityId": "^.*"
              "attributeMappings": [{
                  "originalAttributeName": "oilLevel",
                  "newAttributeName": "oillevel",
                  "originalAttributeType": "^.*"
                },
                ...
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

Note that `speed` attribute is not required in the name mappings file as it match, including case-sensitivity, with the field in the Feature Layer.

[Top](#top)

#### <a name="section2.2.3"></a>About Arcgis data types

- **esriFieldTypeDate**

From https://doc.arcgis.com/en/data-pipelines/latest/process/output-feature-layer.htm
> ... Date fields are stored in feature layers using the format milliseconds from epoch and the coordinated universal time (UTC) time zone. The values will be displayed differently depending on where you are viewing the data. For example, querying the feature service REST end point will return values in milliseconds from epoch, such as 1667411518878....

So, to persist a `esriFieldTypeDate` field in the Feature Layer, cygnus has to receive an attribute "Number" from the CB with the milliseconds from epoch.

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`NGSIArcgisFeatureTableSink` class
As any other NGSI-like sink, `NGSIArcgisFeatureTableSink` extends the base `NGSISink`. The methods that are extended are:

    void persistBatch(NGSIBatch batch) throws CygnusBadConfiguration,
          CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError;

A `Batch` contains a set of `NGSIEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the ArcGis where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `Arcgis` implementation.

    public void start();

This must be done at the `start()` method and not in the constructor since the invoking sequence is `NGSIArcgisFeatureTableSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>Authentication and authorization
Current implementation of `NGSIArcgisFeatureTableSink` relies on the username and password credentials created at the ArcGis endpoint.

[Top](#top)
