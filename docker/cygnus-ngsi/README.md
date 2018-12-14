# Installing cygnus-ngsi with docker

Please, refer to the [documentation](../../doc/cygnus-ngsi/installation_and_administration_guide/install_with_docker.md) if you want to use a docker image for cygnus-ngsi.

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
* `CYGNUS_CARTO_USER`
* `CYGNUS_CARTO_KEY`

