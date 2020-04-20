# <a name="top"></a>Cygnus-twitter

* [Welcome to Cygnus-arcgis](#section1)
* [Basic operation](#section2)
    * [Configuration](#section2.1)
    * [Unit testing](#section2.2)
    * [Management API overview](#section2.3)
* [Features summary](#section5)
* [Licensing](#section6)
* [Reporting issues and contact information](#section7)

## <a name="section1"></a>Welcome to Cygnus-twitter
This project is part of [FIWARE](http://fiware.org), being part of the [Cosmos](http://catalogue.fiware.org/enablers/bigdata-analysis-cosmos) Ecosystem.

Cygnus-twitter is a connector in charge of persisting [ArcGis](https://www.esri.com/en-us/what-is-gis/overview) features, creating a geographical view of such data.

Internally, Cygnus-arcgis is based on [Apache Flume](http://flume.apache.org/). In fact, Cygnus-arcgis is a Flume agent, which is basically composed of a source in charge of receiving the data from [NGSI](https://fiware-orion.readthedocs.io/en/master/user/ngsiv2_implementation_notes/index.html) notifications, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into an Arcgis Feature Table layer.

Current stable release is able to persist following features regarding it's geometry type:

* [Point](https://pro.arcgis.com/es/pro-app/arcpy/classes/pointgeometry.htm) geometry features.

[Top](#top)

## <a name="section2"></a>Basic operation
### <a name="section2.1"></a>Configuration
Cygnus-arcgis is a tool with a high degree of configuration required for properly running it.

So, the starting point is choosing the internal architecture of the Cygnus agent. Let's assume the simplest one:

```
+---------------+    +-------------------+    +----------------+
|   Http source   |---| memory channel |----|    Arcgis Sink   |
+---------------+    +-------------------+    +----------------+
```

Attending to the above architecture, the content of `/usr/cygnus/conf/cygnus_1.conf` will specify the following parameters:

* #### The source:

```
cygnusagent.sources.arcgis-source.channels = arcgis-channel
cygnusagent.sources.arcgis-source.type = org.apache.flume.source.http.HTTPSource
cygnusagent.sources.arcgis-source.port = 5050
cygnusagent.sources.arcgis-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
cygnusagent.sources.arcgis-source.handler.notification_target = /notify
cygnusagent.sources.arcgis-source.interceptors = ts nmi
cygnusagent.sources.arcgis-source.interceptors.ts.type = timestamp

cygnusagent.sources.arcgis-source.interceptors.nmi.type = com.telefonica.iot.cygnus.interceptors.NGSINameMappingsInterceptor$Builder
cygnusagent.sources.arcgis-source.interceptors.nmi.name_mappings_conf_file = /{cygnusPath}/conf/arcgis_name_mappings.conf
```
If you need to split 'NGSI' complex attributes using `NGSINameMapping` 's JsonPath capabilities, dont forget to add:

```
cygnusagent.sources.arcgis-source.interceptors.nmi.stop_on_first_attr_match = false
```

* #### The Channel
```
cygnusagent.channels.arcgis-channel.type = com.telefonica.iot.cygnus.channels.CygnusMemoryChannel
cygnusagent.channels.arcgis-channel.capacity = 1000
cygnusagent.channels.arcgis-channel.transactionCapacity = 100
```

 * #### The Sink:
 
```
cygnusagent.sinks.arcgis-sink.type = com.telefonica.iot.cygnus.sinks.NGSIArcgisFeatureTableSink
cygnusagent.sinks.arcgis-sink.channel = arcgis-channel
cygnusagent.sinks.arcgis-sink.enable_name_mappings = true

# Arcgis services root url
cygnusagent.sinks.arcgis-sink.arcgis_service_url = https://services.arcgis.com/{hash}/arcgis/rest/services
# Arcgis server generate token service url
cygnusagent.sinks.arcgis-sink.arcgis_gettoken_url = https://arcgis.com/sharing/generateToken

# User credentials
cygnusagent.sinks.arcgis-sink.arcgis_username = XXXXXXXXX
cygnusagent.sinks.arcgis-sink.arcgis_password = XXXXXXXXX

cygnusagent.sinks.arcgis-sink.arcgis_maxBatchSize = 10

```

[Top](#top)

### <a name="section2.2"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and Cygnus sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ mvn test

[Top](#top)

### <a name="section2.3"></a>Management API overview
Run the following `curl` in order to get the version (assuming Cygnus runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/version"
{
    "success": "true",
    "version": "0.12.0_SNAPSHOT.52399574ea8503aa8038ad14850380d77529b550"
}
```

Run the following `curl` in order to get certain Flume components statistics (assuming Cygnus runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/stats" | python -m json.tool
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   489  100   489    0     0  81500      0 --:--:-- --:--:-- --:--:-- 97800
{
    "stats": {
        "channels": [
            {
                "name": "hdfs-channel",
                "num_events": 0,
                "num_puts_failed": 0,
                "num_puts_ok": 11858,
                "num_takes_failed": 1,
                "num_takes_ok": 11858,
                "setup_time": "2016-02-05T10:34:25.80Z",
                "status": "START"
            }
        ],
        "sinks": [
            {
                "name": "hdfs-sink",
                "num_persisted_events": 11800,
                "num_processed_events": 11858,
                "setup_time": "2016-02-05T10:34:24.978Z",
                "status": "START"
            }
        ],
        "sources": [
            {
                "name": "twitter-source",
                "num_processed_events": 11858,
                "num_received_events": 11858,
                "setup_time": "2016-02-05T10:34:24.921Z",
                "status": "START"
            }
        ]
    },
    "success": "true"
}
```

Many other operations, like getting/putting/updating/deleting the grouping rules can be found in Management Interface [documentation](../../doc/cygnus-common/installation_and_administration_guide/management_interface.md).

[Top](#top)

## <a name="section5"></a>Features summary
<table>
  <tr><th>Component</th><th>Feature</th><th>From version</th></tr>
  <tr><td rowspan="5">ArcgisFeatureTableSink</td><td>First implementation</td><td>2.0.0</td></tr>
  <tr><td>Feature Table endpoint support</td><td>2.0.0</td></tr>
  <tr><td>User Credential support</td><td>2.0.0</td></tr>
  <tr><td>Point Features support</td><td>2.0.0</td></tr>
  <tr><td>Batching mechanism</td><td>2.0.0</td></tr>
  <tr><td rowspan="3">All sinks</td><td>JsonPath Name Mapping</td><td>2.0.0</td></tr>
  <tr><td>NameMapping not to stop on first attribute match</td><td>2.0.0</td></tr>
  <tr><td>NameMapping allow attribute clonning</td><td>2.0.0</td></tr>
</table>

[Top](#top)

## <a name="section6"></a>Licensing
Cygnus is licensed under Affero General Public License (GPL) version 3. You can find a [copy of this license in the repository](../../LICENSE).

[Top](#top)

## <a name="section7"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelerator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.

[Top](#top)
