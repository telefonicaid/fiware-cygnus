# Installing cygnus-ngsi with docker

[![FIWARE Core](https://nexus.lab.fiware.org/repository/raw/public/badges/chapters/core.svg)](https://www.fiware.org/developers/catalogue/)
[![Support badge]( https://img.shields.io/badge/support-askbot-yellowgreen.svg)](https://ask.fiware.org/questions/scope%3Aall/tags%3Acygnus/)

Cygnus is a connector in charge of persisting context data sources into other third-party databases and storage systems, creating a historical view of the context. Internally, Cygnus is based on [Apache Flume](http://flume.apache.org/), Flume is a data flow system based on the concepts of flow-based programming. It supports powerful and scalable directed graphs of data routing, transformation, and system mediation logic. It was built to automate the flow of data between systems. While the term 'dataflow' can be used in a variety of contexts, we use it here to mean the automated and managed flow of information between systems.

Each data persistence agent within Cygnus is composed of three parts - a listener or source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

Please, refer to the [documentation](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/install_with_docker.md) if you want to use a docker image for cygnus-ngsi.

## How to use this image

The Cygnus must be instantiated and connected to an instance of the
[Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/),  the appropriate sink ports must be
configured and exposed dependent upon the type of data persistence used. A sample `docker-compose` file can be found
below,


```yml
version: "3.5"
services:

  # Orion is the context broker
  orion:
    image: fiware/orion
    hostname: orion
    container_name: fiware-orion
    depends_on:
      - mongo-db
    ports:
      - "1026:1026"
    command: -dbhost mongo-db -logLevel DEBUG -noCache

  # Cygnus is configured to write context data to PostgeSQL
  cygnus:
    image: fiware/cygnus-ngsi
    hostname: cygnus
    container_name: fiware-cygnus
    networks:
      - default
    depends_on:
      - postgres-db
    ports:
      - "5055:5055"
      - "5080:5080" 
    environment:
      - "CYGNUS_POSTGRESQL_HOST=postgres-db" # Hostname of the PostgreSQL server used to persist historical context data
      - "CYGNUS_POSTGRESQL_PORT=5432" # Port that the PostgreSQL server uses to listen to commands
      - "CYGNUS_POSTGRESQL_USER=postgres" # Username for the PostgreSQL database user
      - "CYGNUS_POSTGRESQL_PASS=password" # Password for the PostgreSQL database user
      - "CYGNUS_POSTGRESQL_ENABLE_CACHE=true" # Switch to enable caching within the PostgreSQL configuration
      - "CYGNUS_POSTGRESQL_SERVICE_PORT=5055" # The port the agent.conf is configured for
      - "CYGNUS_LOG_LEVEL=DEBUG" # The logging level for Cygnus
      - "CYGNUS_SERVICE_PORT=5055" # Notification Port that Cygnus listens to for Postgres subscriptions
      - "CYGNUS_API_PORT=5080" # Port that Cygnus listens on for operational reasons

  # Databases - Orion uses Mongo-DB, Cygnus is persisting to Postgres
  mongo-db:
    image: mongo:3.6
    hostname: mongo-db
    container_name: db-mongo
    ports:
      - "27017:27017" 
    command: --bind_ip_all --smallfiles
    volumes:
      - mongo-db:/data
      
  postgres-db:
    image: postgres
    hostname: postgres-db
    container_name: db-postgres
    ports:
      - "5432:5432
    environment:
      - "POSTGRES_PASSWORD=password"
      - "POSTGRES_USER=postgres"
      - "POSTGRES_DB=postgres"
    volumes:
      - postgres-db:/var/lib/postgresql/data

volumes:
  mongo-db: ~
  postgres-db: ~

```

It is possible to configure Cygnus using either Docker environment variables or by injecting an `agent.conf` 
directly or a combination thereof. More details about configuring cygnus using Docker volumes can be found [here](https://github.com/telefonicaid/fiware-cygnus/blob/master/doc/cygnus-ngsi/installation_and_administration_guide/install_with_docker.md#using-volumes)


### Docker Secrets

As an alternative to passing sensitive information via environment variables, `_FILE` may be appended to some sensitive environment variables, causing the initialization script to load the values for those variables from files present in the container. In particular, this can be used to load passwords from Docker secrets stored in `/run/secrets/<secret_name>` files. For example:

```bash
docker run --name some-cygnus -e CYGNUS_MYSQL_PASS_FILE=/run/secrets/mysql-root -d fiware/cygnus-ngsi:tag
```

Currently, this the `_FILE` suffix is supported for:

* `CYGNUS_MYSQL_USER`
* `CYGNUS_MYSQL_PASS`
* `CYGNUS_MONGO_USER`
* `CYGNUS_MONGO_PASS`
* `CYGNUS_HDFS_USER`
* `CYGNUS_HDFS_TOKEN`
* `CYGNUS_POSTGRESQL_USER`
* `CYGNUS_POSTGRESQL_PASS`
* `CYGNUS_POSTGIS_USER`
* `CYGNUS_POSTGIS_PASS`
* `CYGNUS_CARTO_USER`
* `CYGNUS_CARTO_KEY`

