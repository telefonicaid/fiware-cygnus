# Cygnus_LD Quick Start Guide
This quick start overviews the steps a newbie programmer will have to follow in order to get familiar with Cygnus-LD and its basic functionality.
 This guide is intended for using Cygnus-LD in order to persist the NGSI-LD notifications in the PostgreSQL 
 For more detailed information, please refer to the [README](https://github.com/telefonicaid/fiware-cygnus/blob/master/README.md);  

## Running Cygnus

In this guide, all the components are provided using docker containers. For running and setting up the components please follow the next steps:
 
Open a terminal clone the cygnus repository located in anmunoz/fiware-cygnus
```
git clone https://github.com/anmunoz/fiware-cygnus.git
```
Go to the docker folder of cygnus-ngsi-ld
```
cd docker/cygnus-ngsi-ld/
```

Run docker-compose up for starting orion-ld, cygnus and postgresql:
```
sudo docker-compose up 
```

Once the container were up and running, open other terminal and execute the simulated notification:
```
chmod 775 notification-example.sh
sh ./notification-example.sh
```

Now you can verify if the schema and table was created in postgres and also
if the content of the notification was inserted in the table:
``` 
sudo docker exec -it postgres-db psql -U postgres
```
Then, check if everything is created in postgres
```
psql (12.2 (Debian 12.2-2.pgdg100+1))
Type "help" for help.

postgres=# \dn
  List of schemas
  Name   |  Owner   
---------+----------
 openiot | postgres
 public  | postgres
(2 rows)

postgres=# \dt openiot.*
               List of relations
 Schema  |       Name       | Type  |  Owner   
---------+------------------+-------+----------
 openiot | urn_ngsi_ld_OffStreetParking_Downtown1 | table | postgres
(1 row)

postgres=# select *from openiot.urn_ngsi_ld_OffStreetParking_Downtown1;
         recvtime         |                entityid                |    entitytype    | availablespotnumber | availablespotnumber_observedat | availablespotnumber_reliability | availablespotnumber_provide
dby |     name     |                  location                  | totalspotnumber 
--------------------------+----------------------------------------+------------------+---------------------+--------------------------------+---------------------------------+----------------------------
----+--------------+--------------------------------------------+-----------------
 2020-05-13T14:46:21.918Z | urn:ngsi:ld:OffStreetParking:Downtown1 | OffStreetParking | 122                 | 2017-07-29T12:05:02Z           | 0.7                             | urn:ngsi-ld:Camera:C1      
    | Downtown One | {"type":"Point","coordinates":[-8.5,41.2]} | 200
(1 row)

```

## Configuration used in this example

The cygnus-ld docker images has the preconfigured agent.conf file with the option set for working with PostgreSQL:
This file contains the following:

```
cygnus-ngsi.sources = http-source
cygnus-ngsi.sinks = postgresql-sink
cygnus-ngsi.channels = test-channel

cygnus-ngsi.sources.http-source.channels = test-channel
cygnus-ngsi.sources.http-source.type = org.apache.flume.source.http.HTTPSource
cygnus-ngsi.sources.http-source.port = 5050
cygnus-ngsi.sources.http-source.handler = com.telefonica.iot.cygnus.handlers.NGSIRestHandler
cygnus-ngsi.sources.http-source.handler.notification_target = /notify
cygnus-ngsi.sources.http-source.handler.default_service = def_serv_ld
cygnus-ngsi.sources.http-source.handler.ngsi_version = ld
cygnus-ngsi.sources.http-source.handler.events_ttl = 2
cygnus-ngsi.sources.http-source.interceptors = ts
cygnus-ngsi.sources.http-source.interceptors.ts.type = timestamp



cygnus-ngsi.channels.test-channel.type = memory
cygnus-ngsi.channels.test-channel.capacity = 1000
cygnus-ngsi.channels.test-channel.transactionCapacity = 100


cygnus-ngsi.sinks.postgresql-sink.type = com.telefonica.iot.cygnus.sinks.NGSILDPostgreSQLSink
cygnus-ngsi.sinks.postgresql-sink.channel = test-channel
cygnus-ngsi.sinks.postgresql-sink.enable_encoding = false
cygnus-ngsi.sinks.postgresql-sink.enable_grouping = false
cygnus-ngsi.sinks.postgresql-sink.enable_lowercase = false
cygnus-ngsi.sinks.postgresql-sink.enable_name_mappings = false
cygnus-ngsi.sinks.postgresql-sink.data_model = dm-by-entity
cygnus-ngsi.sinks.postgresql-sink.postgresql_host = localhost
cygnus-ngsi.sinks.postgresql-sink.postgresql_port = 5432
cygnus-ngsi.sinks.postgresql-sink.postgresql_database = postgres
cygnus-ngsi.sinks.postgresql-sink.postgresql_username = postgres
cygnus-ngsi.sinks.postgresql-sink.postgresql_password = example
cygnus-ngsi.sinks.postgresql-sink.postgresql_options = sslmode=require
cygnus-ngsi.sinks.postgresql-sink.attr_persistence = column
cygnus-ngsi.sinks.postgresql-sink.attr_native_types = false
cygnus-ngsi.sinks.postgresql-sink.batch_size = 1
cygnus-ngsi.sinks.postgresql-sink.batch_timeout = 30
cygnus-ngsi.sinks.postgresql-sink.batch_ttl = 10
cygnus-ngsi.sinks.postgresql-sink.batch_retry_intervals = 5000
cygnus-ngsi.sinks.postgresql.backend.enable_cache = false
```

## Orion-LD and Cygnus integration 
In the fist part of this guide we use sumilated notification for storing ngsi-ld data in Cygnus.
In this part we will create a subscription in Orion-ld in order to send a notification to cygnus-ld adn estore that notification un postgresql
the components involved will be Orionld->Cygnu-LD->PostgreSQL
We are going to use the same docker-compose that we use inthe first part of this guide. Then 
we are going to create a simple entiy en Orion-LD executin the following command:
```
  curl localhost:1026/ngsi-ld/v1/entities -H 'Content-Type: application/ld+json' -H 'Content-Type: application/ld+json' -H 'fiware-service: openiot' \
  -d '{ "id": "urn:entities:E1", "type": "T", "P1": { "type": "Property", "value": 12 },"@context": "https://fiware.github.io/tutorials.Step-by-Step/tutorials-context.jsonld"}' 
```
Now you can verify if the entity wa created:
```
curl 'http://localhost:1026/ngsi-ld/v1/entities?type=T&prettyPrint=yes&space=2' -H 'Content-Type: application/ld+json' -H 'fiware-service: openiot' 

```
Now we are going to create a Subscription to the created entity E1, in order to send a notification to Cygnus-ld
when the Property will be greater than 12 (P1>12):

```
curl -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions' -H 'Content-Type: application/ld+json' -H 'fiware-service: openiot' -d @- <<EOF 
{
  "description": "LD Notify me of P1>12 in E1",
  "type": "Subscription",
  "entities": [
    { 
      "id": "urn:entities:E1",	
      "type": "T"
    }
  ],
  "watchedAttributes":["P1"],
  "q":"P1>12",
  "notification": {
    "attributes": [
      "P1"
    ],
    "format": "keyValues",
    "endpoint": {
      "uri": "http://cygnus-ld:5050/notify",
      "accept": "application/ld+json"
    }
  },
  "@context": "https://fiware.github.io/tutorials.Step-by-Step/tutorials-context.jsonld"
}
EOF
```
Now we are going to update the P1 value in order to orion trigger the notification to Cygnus

```
curl -X POST 'http://localhost:1026/ngsi-ld/v1/entities/urn:entities:E1/attrs' -H 'fiware-service: openiot' --header 'Content-Type: application/ld+json' -d '{"P1": { "type": "Property", "value": 17 },"@context": "https://fiware.github.io/tutorials.Step-by-Step/tutorials-context.jsonld"}'

```
At this point we can check in the data was saved in PostgreSQL by entering to the postgresql container
```
sudo docker exec -it postgres-db psql -U postgres
```

```
psql (12.2 (Debian 12.2-2.pgdg100+1))
Type "help" for help.


postgres=# \dn
  List of schemas
  Name   |  Owner   
---------+----------
 openiot | postgres
 public  | postgres
(2 rows)

postgres=# \dt openiot.*
              List of relations
 Schema  |      Name       | Type  |  Owner   
---------+-----------------+-------+----------
 openiot | urn_entities_e1 | table | postgres
(1 row)

postgres=# select *from openiot.urn_entities_e1;
         recvtime         |    entityid     | entitytype | p1 
--------------------------+-----------------+------------+----
 2020-05-13T15:23:16.358Z | urn:entities:E1 | T          | 17
(1 row)


```

## Running Cygnus-LD with Historic Context Tutorial

In this part we are going to use the tutorial provided in [NGSI-LD FOR NGSI-V2 DEVELOPERS » SUBSCRIPTIONS AND REGISTRATIONS](https://fiware-tutorials.readthedocs.io/en/latest/ld-subscriptions-registrations/index.html#using-subscriptions-with-ngsi-ld)
The additional tasks that are needed to set up this enviroment are:
 * Modify the docker-compose file for including postgresql and cygnus-ld
 * Create two new subscriptions for notifying Cygnus
 * Check the data inserted in the psotgresql
The scenario provided in this guide is:
![tutorial scenario](./images/cygnus-ld-tutorial.png)

As you can see we add cygnus an postgresql for storing the context data

(1) Clone the repository:

```
git clone https://github.com/FIWARE/tutorials.LD-Subscriptions-Registrations.git
cd tutorials.LD-Subscriptions-Registrations/docker-compose
```

(2) Edit the orion.yml file and add the following:

```
  postgres-db:
     hostname: postgres-db
     container_name: postgres-db
     image: postgres
     restart: always
     ports:
       - "5432:5432"
     environment:
       POSTGRES_PASSWORD: example
 
  cygnus-ld:
     hostname: cygnus-ld
     container_name: cygnus-ld
     image: anmunozx/cygnus-ld
     environment:
       CYGNUS_POSTGRESQL_HOST: postgres-db
       CYGNUS_POSTGRESQL_PASS: example
       CYGNUS_POSTGRESQL_USER: postgres
       CYGNUS_POSTGRESQL_DATA_MODEL: dm-by-entity
     ports: 
       - "5050:5050"
```
Your orion.yml file will look like this:
```
# WARNING: Do not deploy this tutorial configuration directly to a production environment
#
# The tutorial docker-compose files have not been written for production deployment and will not 
# scale. A proper architecture has been sacrificed to keep the narrative focused on the learning 
# goals, they are just used to deploy everything onto a single Docker machine. All FIWARE components 
# are running at full debug and extra ports have been exposed to allow for direct calls to services. 
# They also contain various obvious security flaws - passwords in plain text, no load balancing,
# no use of HTTPS and so on. 
# 
# This is all to avoid the need of multiple machines, generating certificates, encrypting secrets
# and so on, purely so that a single docker-compose file can be read as an example to build on, 
# not use directly. 
# 
# When deploying to a production environment, please looking at the SmartSDK Recipes
# in order to scale up to a proper architecture:
# 
# see: https://smartsdk.github.io/smartsdk-recipes/
#
version: "3.5"
services:
  # Orion LD is the context broker
  orion:
    image: fiware/orion-ld:${ORION_LD_VERSION}
    hostname: orion
    container_name: fiware-orion-ld
    depends_on:
      - mongo-db
    networks:
      - default
    ports:
      - "${ORION_LD_PORT}:${ORION_LD_PORT}" # localhost:1026
    command: -dbhost mongo-db -logLevel DEBUG -t 255
    healthcheck:
      test: curl --fail -s http://orion:${ORION_LD_PORT}/version || exit 1

  
  # Databases
  mongo-db:
    image: mongo:${MONGO_DB_VERSION}
    hostname: mongo-db
    container_name: db-mongo
    expose:
      - "${MONGO_DB_PORT}"
    ports:
      - "${MONGO_DB_PORT}:${MONGO_DB_PORT}" # localhost:27017
    networks:
      - default
    command: --nojournal
    volumes:
      - mongo-db:/data

  postgres-db:
       hostname: postgres-db
       container_name: postgres-db
       image: postgres
       restart: always
       ports:
         - "5432:5432"
       environment:
         POSTGRES_PASSWORD: example
   
  cygnus-ld:
       hostname: cygnus-ld
       container_name: cygnus-ld
       image: anmunozx/cygnus-ld
       environment:
         CYGNUS_POSTGRESQL_HOST: postgres-db
         CYGNUS_POSTGRESQL_PASS: example
         CYGNUS_POSTGRESQL_USER: postgres
         CYGNUS_POSTGRESQL_DATA_MODEL: dm-by-entity
       ports: 
         - "5050:5050"


  # Tutorial displays a web app to manipulate the context directly
  tutorial:
    image: fiware/tutorials.context-provider
    hostname: tutorial
    container_name: fiware-tutorial
    depends_on:
      - orion
    networks:
      default:
        aliases:
          - iot-sensors
          - context-provider
    expose:
      - "${TUTORIAL_APP_PORT}" # localhost:3000
      - "${TUTORIAL_DUMMY_DEVICE_PORT}" # localhost:3001
    ports:
      - "${TUTORIAL_APP_PORT}:${TUTORIAL_APP_PORT}" # localhost:3000
      - "${TUTORIAL_DUMMY_DEVICE_PORT}:${TUTORIAL_DUMMY_DEVICE_PORT}" # localhost:3001
    environment:
      - "DEBUG=tutorial:*"
      - "WEB_APP_PORT=${TUTORIAL_APP_PORT}" # Port used by the content provider proxy and web-app for viewing data
      - "NGSI_VERSION=ngsi-ld"
      - "CONTEXT_BROKER=http://orion:${ORION_LD_PORT}/ngsi-ld/v1" # URL of the context broker to update context
      - "DEVICE_BROKER=http://devices:${ORION_EDGE_PORT}/v2" # URL of the device's context broker to update context
      - "NGSI_LD_PREFIX=urn:ngsi-ld:"
      
      - "IOTA_HTTP_HOST=iot-agent"
      - "IOTA_HTTP_PORT=${IOTA_SOUTH_PORT}"
      - "DUMMY_DEVICES_PORT=${TUTORIAL_DUMMY_DEVICE_PORT}" # Port used by the dummy IOT devices to receive commands
      - "DUMMY_DEVICES_TRANSPORT=HTTP" # Default transport used by dummy Io devices

      - "OPENWEATHERMAP_KEY_ID=<ADD_YOUR_KEY_ID>"
      - "TWITTER_CONSUMER_KEY=<ADD_YOUR_CONSUMER_KEY>"
      - "TWITTER_CONSUMER_SECRET=<ADD_YOUR_CONSUMER_SECRET>"
    healthcheck:
      test: curl --fail -s http://tutorial:${TUTORIAL_APP_PORT}/version || exit 1

networks:
  default: ~

volumes:
  mongo-db: ~

```
(3) Start the services:
```
./services orion
```
(4) Create a new subscription for Store001
```
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
--data-raw '{
  "description": "Notify Cygnus of low stock in Store 001",
  "type": "Subscription",
  "entities": [{"type": "Shelf"}],
  "watchedAttributes": ["numberOfItems"],
  "q": "numberOfItems<10;locatedIn==urn:ngsi-ld:Building:store001",
  "notification": {
    "attributes": ["numberOfItems", "stocks", "locatedIn"],
    "format": "keyValues",
    "endpoint": {
      "uri": "http://cygnus-ld:5050/notify",
      "accept": "application/json"
    }
  },
   "@context": "https://fiware.github.io/tutorials.Step-by-Step/tutorials-context.jsonld"
}'
```
(5) Create a new subscription for Store002
```
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/json' \
-H 'Link: <https://fiware.github.io/tutorials.Step-by-Step/tutorials-context.jsonld>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"' \
--data-raw '{
  "description": "LD Notify Cyguns of low stock in Store 002",
  "type": "Subscription",
  "entities": [{"type": "Shelf"}],
  "watchedAttributes": ["numberOfItems"],
  "q": "numberOfItems<10;locatedIn==urn:ngsi-ld:Building:store002",
  "notification": {
    "attributes": ["numberOfItems", "stocks", "locatedIn"],
    "format": "normalized",
    "endpoint": {
      "uri": "http://cygnus-ld:5050/notify",
      "accept": "application/ld+json"
    }
  }
}'

```
(6) Check the created subscriptions
```
curl -L -X GET 'http://localhost:1026/ngsi-ld/v1/subscriptions/'

```
(7) Trigger the notifications
Open two tabs on a browser. Go to the event monitor (http://localhost:3000/app/monitor) to see the payloads that are received when a subscription fires, and then go to store001 (http://localhost:3000/app/store/urn:ngsi-ld:Building:store001) and buy beer until less than 10 items are in stock. The low stock message should be displayed on screen.

(8) Verify the data stored in postgresql
```

psql (12.2 (Debian 12.2-2.pgdg100+1))
Type "help" for help.

postgres=# \dn
    List of schemas
    Name     |  Owner   
-------------+----------
 def_serv_ld | postgres
 public      | postgres
(2 rows)

postgres=# \dt def_serv_ld.*
                     List of relations
   Schema    |           Name            | Type  |  Owner   
-------------+---------------------------+-------+----------
 def_serv_ld | urn_ngsi_ld_shelf_unit001 | table | postgres
(1 row)


postgres=# select * from def_serv_ld.urn_ngsi_ld_shelf_unit001;
         recvtime         |         entityid          | entitytype |            locatedin            | numberofitems |          stocks           
--------------------------+---------------------------+------------+---------------------------------+---------------+---------------------------
 2020-05-13T16:18:59.380Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 9             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:18:59.384Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 8             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:00.93Z  | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 7             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:00.991Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 6             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:01.428Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 5             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:02.284Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 4             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:02.735Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 3             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:03.176Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 2             | "urn:ngsi-ld:Product:001"
 2020-05-13T16:19:03.819Z | urn:ngsi-ld:Shelf:unit001 | Shelf      | "urn:ngsi-ld:Building:store001" | 1             | "urn:ngsi-ld:Product:001"
(9 rows)
```

## Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [joseandres.munoz@upm.es](mailto:joseandres.munoz@upm.es) **[Main contributor] Andrés Munoz-Arcentales**


**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](http://stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.
