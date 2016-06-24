#<a name="top"></a>GroupingInterceptor
Content:

* [Functionality](#section1)
* [Grouping rules syntax](#section2)
* [Usage](#section3)
* [Management Interface related operations](#section4)

##<a name="section1"></a>Functionality
This is a custom Interceptor specifically designed for Cygnus. Its goal is to infer the destination entity where the data regarding a notified entity is going to be persisted. This destination entity, depending on the used sinks, may be a HDFS file name, a MySQL table name or a CKAN resource name. In addition, a new `fiware-servicePath` containing the destination entity may be configured (in case of HDFS, this is a folder; in case of CKAN this is a package; in case of MySQL this is simply a prefix for the table name; please, have a look to [doc/design/naming_conventions.md](doc/design/naming_conventions.md) for more details).

Such an inference is made by inspecting (but not modifying) certain configured fields of the body part of the event; if the concatenation of such fields matches a configured regular expression, then the configured destination entity is added as the value of a `destination` header. The already existing `fiware-servicePath` header may be substituted as well by the configured new service path.

If a notified entity contains more than one context response, then both the `destination` and the `fiware-servicePath` headers contains a comma-separated list of values.

[Top](#top)

##<a name="section2"></a>Grouping rules syntax
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
* <b>destination</b>: Name of the HDFS file or CKAN resource where the data will be effectively persisted. In the case of MySQL, Mongo and FIWARE Comet this sufixes the table/collection name.
* <b>fiware\_service\_path</b>: New `fiware-servicePath` replacing the notified one. The sinks will translate this into the name of the HDFS folder or CKAN package where the above destination entity will be placed. In the case of MySQL, Mongo and FIWARE Comet this prefixes the table/collection name.

For instance:

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
                "fiware_service_path": "rooms"
            },
            {
                "id": 2,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "Room\\.(\\D*)Room",
                "destination": "character_rooms",
                "fiware_service_path": "rooms"
            },
            {
                "id": 3,
                "fields": [
                    "entityType",
                    "entityId"
                ],
                "regex": "RoomRoom\\.(\\D*)",
                "destination": "character_rooms",
                "fiware_service_path": "rooms"
            },
            {
                "id": 4,
                "fields": [
                    "entityType"
                ],
                "regex": "Room",
                "destination": "other_rooms",
                "fiware_service_path": "rooms"
            }
        ]
    }

The above rules set that:

* All the `Room` entities having their identifiers composed by a `Room.` and an integer (e.g. `Room.12`) will be persisted in a `numeric_rooms` destination within a `rooms` service path (in the example, the concatenation is equals to `Room.12Room`).
* All the `Room` entities having their identifiers composed by a `Room.` and any number of characters (no digits) (e.g. `Room.left`) will be persisted in a `character_rooms` destination within a `rooms` service path (in the example, the concatenation is equals to `Room.leftRoom` when applying rule number 2, but `RoomRoom.left` when applying rule number 3; nevertheless, from a semantic point of view they are the same rule).
* All other rooms will go to `other_rooms` destination within a `rooms` service path.

Rules are tried sequentially, and if any rules matches then the default destination for the notified entity is generated, i.e. the concatenation of the entity id, `_` and the entity type; and the notified service path is maintained.

Regarding the syntax of the rules, all the fields are mandatory and must have a valid value.

[Top](#top)

##<a name="section3"></a>Usage
The grouping rules file is usually placed at `[FLUME_HOME_DIR]/conf/`, and there exists a template within Cygnus distribution.

The usage of such an Interceptor is:

    cygnusagent.sources.http-source.interceptors = gi <other-interceptors>
    cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.NGSIGroupingInterceptor$Builder
    cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = [FLUME_HOME_DIR]/conf/grouping_rules.conf

It is <b>very important</b> to configure the <b>absolute path to the grouping rules file</b>.

[Top](#top)

##<a name="section4"></a>Management Interface related operations

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

Please, check [this](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-common/installation_and_administration_guide/management_interface.md) link in order to know further details.

[Top](#top)
