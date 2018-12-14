# <a name="top"></a>NGSINameMappingsInterceptor
Content:

* [Functionality](#section1)
    * [Name mappings syntax](#section1.1)
    * [Headers before and after intercepting](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Management Interface related operations](#section2.2)

## <a name="section1"></a>Functionality
This is a custom Interceptor specifically designed for Cygnus. Its purpose is to alter an original `NGSIEvent` object (which comes from a NGSI notification handled by [`NGSIRestHandler`](./ngsi_rest_handler.md)) by <i>replacing</i> (one or more at the same time):

* The FIWARE service Http header sent with the notification.
* The FIWARE service path Http header sent with the notification.
* Any entity ID within the notification.
* Any entity type within the notification.
* Any attribute name within the notification.
* Any attribute type within the notification.

As known, a `NGSIEvent` contains a set of headers and an already parsed version of the notified Json payload as an object of type `ContextElement`. Being said this, it must be said there is no real replacement but another twin `ContextElement` with propper mapped names is created and added to the `NGSIEvent`, resulting in:

* Original headers in the Flume `Event` object are extended with mapped versions of FIWARE service and FIWARE service path.
* An already parsed `ContextElement` object contains the original context element.
* An already parsed `ContextElement` object contains the mapped context element.

[Top](#top)

### <a name="section1.1"></a>Name mappings syntax
There exists a <i>name mappings</i> file containing a Json following this format:

```
{
   "serviceMappings": [
      {
         "originalService": "<original_service>",
         "newService": "<new_service>",
         "servicePathMappings": [
            {
               "originalServicePath": "<original_service_path>",
               "newServicePath": "<new_service_path>",
               "entityMappings": [
                  {
                     "originalEntityId": "<original_entity_id>",
                     "originalEntityType": "<original_entity_type>",
                     "newEntityId": "<new_entity_id>",
                     "newEntityType": "<new_entity_type>",
                     "attributeMappings": [
                        {
                           "originalAttributeName": "<original_attribute_name>",
                           "originalAttributeType": "<original_attribute_type>",
                           "newAttributeName": "<new_attribute_name>",
                           "newAttributeType": "<new_attribute_type>"
                        },
                        ...
                     ]
                  },
                  ...
               ]
            },
            ...
         ]
      },
      ...
   ]
}
```

The above Json is quite straightforward: the `<original_service>` is mapped as the `<new_service>`, the `<original_service_path>` is mapped as the `<new_service_path>` and so on. The name mappings are iterated until a map is found; if no map is found, the mapped version of the original notification is equals to the original one.

However, certain special behaviours must be noticed:

* If any of the original names is not present, then the mapping affects all the names of that type.
* When any of the new names is not present, then the original name is used in the mapping.
* The original names support Java-based regular expressions.

[Top](#top)

### <a name="section1.2"></a>Headers before and after intercepting
Before interception, these are the headers added by the [`NGSIRestHandler`](./ngsi_rest_handler.md) to all the internal Flume events of type `Event`:

* `fiware-service`. FIWARE service which the entity related to the notified data belongs to.
* `fiware-servicepath`. FIWARE service path which the entity related to notified data belongs to. If the notification relates to several entities in different service paths, these are included within this header separated by comma.
* `fiware-correlator`. UUID reserved for identifying e2e flows all along an integration which Cygnus belongs to.
* `transaction-id`. UUID for identifying internal flows, from a Flume source to a Flume sink.

After interception, these are the headers added by NGSINameMappingsInterceptor to a `NGSIEvent`:

* `mapped-fiware-service`. Mapped version of the original FIWARE service.
* `mapped-fiwre-service-path`. Mapped version of the original FIWARE service path.

Other interceptors may add further headers, such as the `timestamp` header added by native Timestamp interceptor.

[Top](#top)

### <a name="section1.3"></a>Example
Let's assume these name mappings:

```
{
   "serviceMappings": [
      {
         "originalService": "hotel1",
         "servicePathMappings": [
            {
               "originalServicePath": "/suites",
               "entityMappings": [
                  {
                     "originalEntityId": "suite.(\\d*)",
                     "originalEntityType": "room",
                     "newEntityId": "all_suites",
                     "newEntityType": "room",
                     "attributeMappings": []
                  }
               ]
            },
            {
               "originalServicePath": "/other",
               "entityMappings": [
                  {
                     "originalEntityId": "room.(\\d*)",
                     "originalEntityType": "room",
                     "newEntityId": "all_other",
                     "newEntityType": "room",
                     "attributeMappings": []
                  }
               ]
            }
         ]
      }
   ]
}
```

Now, let's assume the following not-intercepted event regarding a received notification (the code below is an <i>object representation</i>, not any real data format):

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
	},
	mapped-context-element=null
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
	},
	mapped-context-element=null
}
```

Once intercepted, the above events will look like this (the code below is an <i>object representation</i>, not any real data format):

```
intercepted-ngsi-event-1={
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
	},
	mapped-context-element={
	   entityId=all_suites,
	   entityType=room,
	   attributes=[
	      ...
	   ]
	}
}
    
intercepted-ngsi-event-2={
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
	},
   mapped-context-element={
      entityId=all_other,
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
`NGSINameMappingsInterceptor` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| name\_mappings\_conf\_file | yes | N/A | It is <b>very important</b> to configure the <b>absolute path to the name mappings file</b>. The name mappings file is usually placed at `[FLUME_HOME_DIR]/conf/`, and there exists a template within Cygnus distribution. |

A configuration example could be:

    cygnus-ngsi.sources.http-source.interceptors = nmi <other-interceptors>
    cygnus-ngsi.sources.http-source.interceptors.nmi.type = com.telefonica.iot.cygnus.interceptors.NGSINameMappingsInterceptor$Builder
    cygnus-ngsi.sources.http-source.interceptors.nmi.name_mappings_conf_file = [FLUME_HOME_DIR]/conf/name_mappings.conf
    
Nevertheless, this piece of configuration only enables the interception of the original Flume events put by `NGSIRestHandler` in order to put in the configured channels a NGSI event containing both the original and the mapped notifications (and already parsed for sinks usage). Thus, in order a sink uses the mapped version of the original notification instead of the origianl one, the `enable_name_mappings` parameter must be set to true in the sink configuration. For instance, in HDFS:

    cygnus-ngsi.sinks.hdfs-sink.enable_name_mappings = true
    
Please check the specific sink documentation in the [Flume extensions catalogue](../flume_extensions_catalogue) for further details.

[Top](#top)

### <a name="section2.2"></a>Management Interface related operations
Coming soon.

[Top](#top)
