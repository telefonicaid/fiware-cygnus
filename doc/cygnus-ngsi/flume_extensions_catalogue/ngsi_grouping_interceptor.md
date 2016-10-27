#<a name="top"></a>NGSIGroupingInterceptor
Content:

* [Functionality](#section1)
    * [Grouping rules syntax](#section1.1)
    * [Headers before and after intercepting](#section1.2)
    * [Example](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Management Interface related operations](#section2.2)

##<a name="section1"></a>Functionality
This is a custom Interceptor specifically designed for Cygnus. Its goal is to infer the destination entity where the data regarding a notified entity is going to be persisted. This destination entity, depending on the used sinks, may be a HDFS file name, a MySQL table name or a CKAN resource name. In addition, a new `fiware-servicePath` containing the destination entity may be configured; for instance, in case of HDFS, this is a folder; in case of CKAN this is a package; in case of MySQL this is simply a prefix for the table name.

Such an inference is made by inspecting (but not modifying) certain configured fields of the body part of the event; if the concatenation of such fields matches a configured regular expression, then the configured destination entity is added as the value of a `destination` header. The already existing `fiware-servicePath` header may be substituted as well by the configured new service path.

If a notified entity contains more than one context response, then both the `destination` and the `fiware-servicePath` headers contains a comma-separated list of values.

[Top](#top)

###<a name="section1.1"></a>Grouping rules syntax
There exists a <i>grouping rules</i> file containing Json-like <i>rules</i> definition, following this format:

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

###<a name="section1.2"></a>Headers before and after intercepting
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

###<a name="section1.3"></a>Example
Let's assume these rules:

    {
        "grouping_rules": [
            {
                "id": 1,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "Room\\.(\\d*)Room",
                "destination": "numeric_rooms",
                "fiware_service_path": "/rooms"
            },
            {
                "id": 2,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "Room\\.(\\D*)Room",
                "destination": "character_rooms",
                "fiware_service_path": "/rooms"
            },
            {
                "id": 3,
                "fields": [
                    "entityType",
                    "entityId"
                ],
                "regex": "RoomRoom\\.(\\D*)",
                "destination": "character_rooms",
                "fiware_service_path": "/rooms"
            },
            {
                "id": 4,
                "fields": [
                    "entityType"
                ],
                "regex": "Room",
                "destination": "other_rooms",
                "fiware_service_path": "/rooms"
            }
        ]
    }

The above rules set that:

* All the `Room` entities having their identifiers composed by a `Room.` and an integer (e.g. `Room.12`) will be persisted in a `numeric_rooms` destination within a `rooms` service path (in the example, the concatenation is equals to `Room.12Room`).
* All the `Room` entities having their identifiers composed by a `Room.` and any number of characters (no digits) (e.g. `Room.left`) will be persisted in a `character_rooms` destination within a `rooms` service path (in the example, the concatenation is equals to `Room.leftRoom` when applying rule number 2, but `RoomRoom.left` when applying rule number 3; nevertheless, from a semantic point of view they are the same rule).
* All other rooms will go to `other_rooms` destination within a `rooms` service path.

Now, let's assume the following not-intercepted event regarding a received notification (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	         fiware-service=hotel,
	         fiware-servicepath=/allrooms,/allrooms,
	         transaction-id=1234567890-0000-1234567890,
	         correlation-id=1234567890-0000-1234567890
        },
        body=[
           {
	            entityId=Room.12,
	            entityType=Room.left,
	            attributes=[
	                ...
	            ]
	        },
	        {
	            entityId=Room2,
	            entityType=Room,
	            attributes=[
	                ...
	            ]
	        }
	    ]
    }

As can be seen, two entities (`Room1` and `Room2`) of the same type (`Room`) within the same FIWARE service (`hotel`) and service paths (`/allrooms` for both of them) are notified. Once intercepted, this how the event should look like (the code below is an <i>object representation</i>, not any real data format):

    flume-event={
        headers={
	         fiware-service=hotel,
	         fiware-servicepath=/allrooms,/allrooms,
	         transaction-id=1234567890-0000-1234567890,
	         correlation-id=1234567890-0000-1234567890,
	         timestamp=1234567890,
	         notified-entities=Room.12=Room,Room.left=Room,
	         grouped-entities=numeric-rooms,character-rooms
	         grouped-servicepaths=/rooms,/rooms
        },
        body=[
           {
	            entityId=Room.12,
	            entityType=Room.left,
	            attributes=[
	                ...
	            ]
	        },
	        {
	            entityId=Room2,
	            entityType=Room,
	            attributes=[
	                ...
	            ]
	        }
	    ]
    }

[Top](#top)

##<a name="section2"></a>Administration guide
###<a name="section2.1"></a>Configuration
`NGSIGroupingInterceptor` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| grouping\_rules\_conf\_file | yes | N/A | It is <b>very important</b> to configure the <b>absolute path to the grouping rules file</b>. The grouping rules file is usually placed at `[FLUME_HOME_DIR]/conf/`, and there exists a template within Cygnus distribution. |

A configuration example could be:

    cygnus-ngsi.sources.http-source.interceptors = gi <other-interceptors>
    cygnus-ngsi.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
    cygnus-ngsi.sources.http-source.interceptors.gi.grouping_rules_conf_file = [FLUME_HOME_DIR]/conf/grouping_rules.conf

[Top](#top)

###<a name="section2.2"></a>Management Interface related operations

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
