# Data organization and naming conventions
Cygnus persists context data into several storages, i.e. HDFS, MySQL and CKAN. These storages work in a very different way, organizing the data in files, database tables and hierarchical resources, respectively. In addition, all of them have different constraints regarding the naming of their elements.

This document summarizes the effort done by Cygnus in exposing an homogeneus way of organizing the data and a naming convention for the whole structure supporting the data. 

## Data organization
### Orion headers
The way Cygnus organizes the data derives from the way Orion Context Broker does it. When Orion notifies certain context data, a couple of Http headers are added to the notification:

* `fiware-service`: in some way, this header identifies the tenant or client the context data belongs to.
* `fiware-servicePath`: within a tenant, the data may come from different paths or sets, which will be identified by this header.

An example of how these headers are used could be found in a Smart City having different public services such as gardens maintenance or garbage collection. If for instance a sensor measure regarding a park's grass humidity has to be notified, then the city name will be given in the `fiware-service` header, while the `fiware-servicePath` will contain a path such as <i>/gardens/parks/south_park</i>.   

As can be seen, this way of organizing the data composes some kind of three-level hierarchical structure, being the first level the service, then being the second level the servicePath and finally the notified entity (which will be named according to the [`DestinationExtractor` interceptor](interceptors.md)). This structure will be translated to all the sinks within Cygnus.

### HDFS data organization
HDFS, like any other file system, organizes the data in files within directories. Thus, it seems obvious how the notified context data can be organized in HDFS:

    hdfs:///user/<default_username>/<fiware-service>/<fiware-servicePath>/<extracted_entity_name>/<extracted_entity_name>.txt

### MySQL data organization
MySQL, like any other relational database, organizes the data in tables within databases. Nevertheless, this data organization implies the usage of only two levels, which will match the `fiware-service` (database name) and the `entity_name` (table name). To fix it, the `fiware-servicePath` is prefixed to the table name.

    databases:                       tables:
    --------------------         ------------------------------------------------
    | <fiware_service> |---1:N---| <fiware_servicePath>_<extracted_entity_name> |
    --------------------         ------------------------------------------------

### CKAN data organization
CKAN perfectly fits the three-level data organization through the organization, package and resource concepts:

    organizations:               packages:                        resources:                          datastores:
    --------------------         ------------------------         ---------------------------         -----------------
    | <fiware_service> |---1:N---| <fiware_servicePath> |---1:N---| <extracted_entity_name> |---1:1---| <resource_id> |
    --------------------         ------------------------         ---------------------------         -----------------

Within CKAN, the data is really stored within a <i>datastore</i>, but this is transparent to the user due to he/she only sees the resource level of the hierarchy.

### Comparing data organizations

| Level        | Orion                       | HDFS                                                                                                                        | MySQL                                                | CKAN                                   |
|--------------|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|----------------------------------------|
| Servive      | `fiware-servive` header     | `hdfs:///user/<default_username>/<fiware_service>/`                                                                         | `<fiware-service>` database                          | `<fiware-service>` organization        |
| Service path | `fiware-servicePath` header | `hdfs:///user/<default_username>/<fiware-service>/<fiware-servicePath>/`                                                    | -                                                    | `<fiware-servicePath>` package/dataset |
| Entity       | Certain context data fields | `hdfs:///user/<default_username>/<fiware-service>/<fiware-servicePath>/<extracted_entity_name>/<extracted_entity_name>.txt` | `<fiware-servicePath>_<extracted_entity_name>` table | <extracted_entity_name> resource       |

## Naming conventions

### Valid character set
Each one of the levels of the different data organizations managed by Cygnus (i.e. service, servicePath and entity) must be named with lowercase alphanumeric characters plus the `_` character. Any uppercase characters will be casted to lowercase, and any other character different than an alphanumeric will be scaped to `_`.

#### Special considerations with `fiware-servicePath`
This header is constrained by the previous valid character set, of course, but a special consideration has to be commented. Tipically this header values are Unix paths-like, for instance `/gardens/parks/south_park`. According to the above rules the '/' characters should be scaped to `_`, resulting into `_gardens_parks_south_park`... but you will see it is really converted to `gardens_parks_south_park`. Please observe the initial `_` associated to the <i>root</i> directory has been deleted. This is in order to avoid converted strings such as `_` when only the root directoy `/` is notified.

### Maximum element (service, servicePath, entity) length
Service and servicePath are limited to 50 characters. Any other element is limited to 64 characters, even in the case the service or the servicePath are part of that element's name.

## Contact
Francisco Romero Bueno (francisco.romerobueno@telefonica.com) **[Main contributor]**
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) **[Contributor and Orion Context Broker owner]**
<br>
Germán Toro del Valle (german.torodelvalle@telefonica.com) **[Contributor]**
<br>
Iván Arias León (ivan.ariasleon@telefonica.com) **[Quality Assurance]**
