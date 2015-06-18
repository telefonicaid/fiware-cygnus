#<a name="section0"></a>Flume Interceptors at Cygnus
* [What is a Flume Interceptor](#section1)
* [`Timestamp` Interceptor](#section2)
    * [Usage](#section2.1)
* [`GroupingInterceptor` Interceptor](#section3)
    * [Grouping rules](#section3.1)
    * [Usage](#section3.2)
* [Contact](#section4)

##<a name="section1"></a>What is a Flume Interceptor
Interceptors are components of the Flume agent architecture. Typically, such an agent is based on a source dealing with the input, a sink dealing with the output and a channel communicating them. The source processes the input, producing Flume events (an object based on a set of headers and a byte-based body) that are put in the channel; then the sink consumes the events by getting them from the channel. This basic architecture may be enriched by the addition of Interceptors, a chained sequence of Flume events preprocessors that <i>intercept</i> the events before they are put into the channel and performing one of these operations:

* Drop the event.
* Modify an existent header of the Flume event.
* Add a new header to the Flume event.

Interceptors should never modify the body part. Once an event is preprocessed, it is put in the channel as usual.

As can be seen, this mechanism allows for very useful ways of enriching the basic Flume events a certain Flume source may generate. Let's see how Cygnus makes use of this concept in order to add certain information to the Flume events created from the Orion notifications.

[Top](#top)  

##<a name="section2"></a>`Timestamp` Interceptor
This is an Interceptor that can be [natively found](https://flume.apache.org/FlumeUserGuide.html#timestamp-interceptor) in any Flume distribution. It adds a `timestamp` header to the Flume event, whose value expresses the number of miliseconds from January the 1st, 1970.

[Top](#top)

###<a name="section2.1"></a>Usage

The way Cygnus makes use of this Interceptor is the standard one:

    cygnusagent.sources.http-source.interceptors = ts <other-interceptors>
    cygnusagent.sources.http-source.interceptors.ts.type = timestamp 

[Top](#top)

##<a name="section3"></a>`GroupingInterceptor` Interceptor
This is a custom Interceptor specifically designed for Cygnus. Its goal is to infer the destination entity where the data regarding a notified entity is going to be persisted. This destination entity, depending on the used sinks, may be a HDFS file name, a MySQL table name or a CKAN resource name. In addition, a new `fiware-servicePath` containing the destination entity may be configured (in case of HDFS, this is a folder; in case of CKAN this is a package; in case of MySQL this is simply a prefix for the table name; please, have a look to [doc/design/naming_conventions.md](doc/design/naming_conventions.md) for more details).

Such an inference is made by inspecting (but not modifying) certain configured fields of the body part of the event; if the concatenation of such fields matches a configured regular expresion, then the configured destination entity is added as the value of a `destination` header. The already existing `fiware-servicePath` header may be substituted as well by the configured new service path.

If a notified entity contains more than one context response, then both the `destination` and the `fiware-servicePath` headers contains a comma-separated list of values.

[Top](#top)

###<a name="section3.1"></a>Grouping rules
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

* <b>id</b>: A unique unsigned integer-based identifier. Not really used in the current implementation, but could be useful in the future.
* <b>fields</b>: These are the fields that will be concatenated for regular expression matching. The available dictionary of fields for concatenation is <i>entityId</i>, <i>entityType</i> and <i>servicePath</i>. The order of these fields is important since the concatenation is made from left to right.
* <b>regex</b>: Java-like regular expression to be applied on the concatenated fields.
* <b>destination</b>: Name of the HDFS file or CKAN resource where the data will be effectively persisted. In the case of MySQL, Mongo and STH this sufixes the table/collection name. Please, have a look to [doc/design/naming_conventions.md](doc/design/naming_conventions.md) for more details.
* <b>fiware\_service\_path</b>: New `fiware-servicePath` replacing the notified one. The sinks will translate this into the name of the HDFS folder or CKAN package where the above destination entity will be placed. In the case of MySQL, Mongo and STH this prefixes the table/collection name. Please, have a look to [doc/design/naming_conventions.md](doc/design/naming_conventions.md) for more details.

For instance:

    {
        "grouping_rules": [
            {
                "id": 1,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "Room\.(\d*)Room",
                "destination": "numeric_rooms",
                "fiware_service_path": "rooms"
            },
            {
                "id": 2,
                "fields": [
                    "entityId",
                    "entityType"
                ],
                "regex": "Room\.(\D*)Room",
                "destination": "character_rooms",
                "fiware_service_path": "rooms"
            },
            {
                "id": 3,
                "fields": [
                    "entityType",
                    "entityId"
                ],
                "regex": "RoomRoom\.(\D*)",
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

* All the `Room` entities having their identifiers composed by a `Room.` and an integer (e.g. `Room.12`) will be persisted in a `numeric_rooms` destination within a `rooms` service parth (in the example, the concatenation is equals to `Room.12Room`).
* All the `Room` entities having their identifiers composed by a `Room.` and any number of characters (no digits) (e.g. `Room.left`) will be persisted in a `character_rooms` destination within a `rooms` service path (in the example, the concatenation is equals to `Room.leftRoom` when appliying rule number 2, but `RoomRoom.left` when applying rule number 3; nevertheless, from a semantic point of view they are the same rule).
* All other rooms will go to `other_rooms` destination within a `rooms` service path.

Rules are tryed sequentially, and if any rules matches then the default destination for the notified entity is generated, i.e. the concatenation of the entity id, `_` and the entity type; and the notified service path is maintained.

Regarding the syntax of the rules, all the fields are mandatory and must have a valid value.

[Top](#top)

###<a name="section3.2"></a>Usage

The grouping rules file is usually placed at `[FLUME_HOME_DIR]/conf/`, and there exists a template within Cygnus distribution.

The usage of such an Interceptor is:

    cygnusagent.sources.http-source.interceptors = gi <other-interceptors>
    cygnusagent.sources.http-source.interceptors.gi.type = com.telefonica.iot.cygnus.interceptors.GroupingInterceptor$Builder
    cygnusagent.sources.http-source.interceptors.gi.grouping_rules_conf_file = [FLUME_HOME_DIR]/conf/grouping_rules.conf

It is <b>very important</b> to configure the <b>absolute path to the grouping rules file</b>.

[Top](#top)

##<a name="section4"></a>Contact
Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**

[Top](#top)