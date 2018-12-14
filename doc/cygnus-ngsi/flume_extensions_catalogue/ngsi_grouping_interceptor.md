# <a name="top"></a>NGSIGroupingInterceptor
**IMPORTANT NOTE: from release 1.6.0, this feature is deprecated in favour of Name Mappings. More details can be found [here](./deprecated_and_removed.md#section2.1).**

Content:

* [Functionality](#section1)
    * [Grouping rules syntax](#section1.1)
    * [Headers before and after intercepting](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Management Interface related operations](#section2.2)

## <a name="section1"></a>Functionality
This is a custom Interceptor specifically designed for Cygnus. Its purpose is to alter an original `NGSIEvent` object (which comes from a NGSI notification handled by [`NGSIRestHandler`](./ngsi_rest_handler.md)) by inferring the destination entity where the data regarding a notified entity is going to be persisted. This destination entity, depending on the used sinks, may be a HDFS file name, a MySQL table name or a CKAN resource name. In addition, a new `fiware-servicePath` containing the destination entity may be configured; for instance, in case of HDFS, this is a folder; in case of CKAN this is a package; in case of MySQL this is simply a prefix for the table name.

Such an inference is made by inspecting (but not modifying) certain configured fields within the `ContextElement` object of the `NGSIEvent`; if the concatenation of such fields matches a configured regular expression, then:

* The configured destination entity is added as the value of a `grouped-entity` header.
* The configured destination FIWARE service path is added as the value of a `grouped-servicepath` header.

Additionally, a `notified-entity` header is added containing the concatenation of the original entity ID and type.

This way, those sinks having enabled the grouping rules will use both the `grouped-entity` and the `grouped-servicepath` headers. If not, i.e. the original notification is aimed to be used, then the `notified-entity` and the `fiware-servicepath` headers will be used.

[Top](#top)

### <a name="section1.1"></a>Grouping rules syntax
There exists a file containing Json-like <i>rules</i> definition, following this format:

    {
        "grouping_rules": [
            {
                "id": 1,
                "fields": [
                    ...
                ],
                "regex": "...",
                "destination": "...",
                "fiware_service_path": "..."
            },
            ...
        ]
    }

Being:

* <b>id</b>: A unique unsigned integer-based identifier.
* <b>fields</b>: These are the fields that will be concatenated for regular expression matching. The available dictionary of fields for concatenation is "entityId", "entityType" and "servicePath". The order of these fields is important since the concatenation is made from left to right.
* <b>regex</b>: Java-like regular expression to be applied on the concatenated fields. Special characters like '\' must be escaped ('\' is escaped as "\\\\").
* <b>destination</b>: Name of the HDFS file or CKAN resource where the data will be effectively persisted. In the case of MySQL, Mongo and STH Comet this sufixes the table/collection name.
* <b>fiware\_service\_path</b>: New `fiware-servicePath` replacing the notified one. The sinks will translate this into the name of the HDFS folder or CKAN package where the above destination entity will be placed. In the case of MySQL, Mongo and STH Comet this prefixes the table/collection name.

For each notification, rules are tried sequentially until one of them matches; at that moment the rules are not checked anymore for that notification.

Regarding the syntax of the rules, all the fields are mandatory and must have a valid value.

[Top](#top)

### <a name="section1.2"></a>Headers before and after intercepting
Before interception, these are the headers added by the [NGSIRestHandler](./ngsi_rest_handler.md) to all the internal Flume events:

* `fiware-service`. FIWARE service which the entity related to the notified data belongs to.
* `fiware-servicepath`. FIWARE service path which the entity related to notified data belongs to. If the notification relates to several entities in different service paths, these are included within this header separated by comma.
* `fiware-correlator`. UUID reserved for identifying e2e flows all along an integration which Cygnus belongs to.
* `transaction-id`. UUID for identifying internal flows, from a Flume source to a Flume sink.

After interception, these are the headers added by NGSIGroupingInterceptor:

* `notified-entities`. Comma-separated list of full entity IDs/destinations, being this the concatenation of a notified entity ID and its type. Since this is an internal header, the concatenation character is the equals, `=`; when doing public, this equals is translater into `_` (old encoding) or `xffff` (new encoding).
* `grouped-entities`. Comma-separated list of grouped/modified entities/destinations.
* `grouped-servicepaths`. Comma-separated list of grouped/modified FIWARE service paths.

Other interceptors may add further headers, such as the `timestamp` header added by native Timestamp interceptor.

[Top](#top)

### <a name="section1.3"></a>Example
Let's assume these rules:

    {
        "grouping_rules": [
            {
                "id": 1,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "other\\.(\\d*)room",
                "destination": "all_other",
                "fiware_service_path": "/other"
            },
            {
                "id": 2,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "suite\\.(\\D*)room",
                "destination": "all_suites",
                "fiware_service_path": "/suites"
            }
        ]
    }

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
	   mapped-fiware-service=hotel,
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
	   mapped-fiware-service=hotel,
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
	   grouped-entity=all_suites,
	   grouped-servicepath=/suites,
	   notified-entity=suite.12_room
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
    
intercepted-ngsi-event-2={
   headers={
	   fiware-service=hotel,
	   fiware-servicepath=/other,
	   transaction-id=1234567890-0000-1234567890,
	   correlation-id=1234567890-0000-1234567890,
	   timestamp=1234567890,
	   grouped-entity=all_other,
	   grouped-servicepath=/other,
	   notified-entity=other.9_room
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

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`NGSIGroupingInterceptor` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| grouping\_rules\_conf\_file | yes | N/A | It is <b>very important</b> to configure the <b>absolute path to the grouping rules file</b>. The grouping rules file is usually placed at `[FLUME_HOME_DIR]/conf/`, and there exists a template within Cygnus distribution. |
| enable\_encoding | no | false | <i>true</i> or <i>false</i>, <i>true</i> applies the new encoding, <i>false</i> applies the old encoding. |

A configuration example could be:

    cygnus-ngsi.sources.http-source.interceptors = gi <other-interceptors>
    cygnus-ngsi.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
    cygnus-ngsi.sources.http-source.interceptors.gi.grouping_rules_conf_file = [FLUME_HOME_DIR]/conf/grouping_rules.conf
    cygnus-ngsi.sources.http-source.interceptors.gi.enable_encoding = false

[Top](#top)

### <a name="section2.2"></a>Management Interface related operations

The Management Interface of Cygnus exposes a set of operations under the `/v1/groupingrules` path related to the grouping rules feature, allowing listing/updating/removing the rules. For instance:

```
GET http://<cygnus_host>:<management_port>/v1/groupingrules
```

```
{
    "grouping_rules": [
        {
            "destination": "allcars",
            "fields": [
                "entityType"
            ],
            "fiware_service_path": "cars",
            "id": 1,
            "regex": "Car"
        },
        {
            "destination": "allrooms",
            "fields": [
                "entityType"
            ],
            "fiware_service_path": "rooms",
            "id": 2,
            "regex": "Room"
        }
    ]
}
```

Please, check [this](../../cygnus-common/installation_and_administration_guide/management_interface.md) link in order to know further details.

[Top](#top)
