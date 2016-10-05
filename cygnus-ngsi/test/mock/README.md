## Mocks for Cygnus

Mocks are simulated requests that mimic the behavior of real requests in controlled ways. Used with HTTP as HTTPS protocols.

#### Usage:

```
 ***********************************************************************************************************
 *  usage: python cygnus_mock.py <-u> <-p=port> <-c=certificate file> <-dd= default dataset>               *
 *           ex: python cygnus_mock.py -p=8092 -c=server.pem  -dd=fiware-test                              *
 *                                                                                                         *
 *  parameters:                                                                                            *
 *         -u: show this usage.                                                                            *
 *         -p: change of mock port (by default 8090).                                                      *
 *         -c: certificate path and file used in https protocol.                                           *
 *        -dd: default dataset, obligatory in ckan  (by default "fiware-test").                              *
 *                                                                                                         *
 *  Comments:                                                                                              *
 *         Default Dataset is prefixed by organization name to ensure uniqueness ant it.                   *
 *            Must be purely lowercase alphanumeric (ascii) characters,                                    *
 *            plus "-" and "_" according to CKAN limitations.                                               *
 *         HTTP protocol: the certificate file is not necessary.                                           *
 *         HTTPS protocol: the certificate file is  necessary.                                             *
 *            how to create certificate file:                                                              *
 *                openssl req -new -x509 -keyout <file>.pem -out <file>.pem -days 365 -nodes               *
 *                                                                                                         *
 *                                     ( use <Ctrl-C> to stop )                                            *
 ***********************************************************************************************************
```

#### Paths mocked:

```
     1 - GET  - 200 -- /api/util/status
     2 - GET  - 200 -- /api/3/action/organization_show?id=row_default_1
     3 - POST - 200 -- /api/3/action/organization_create
     4 - POST - 200 -- /api/3/action/package_create
     5 - POST - 200 -- /api/3/action/resource_create
     6 - POST - 200 -- /api/3/action/datastore_create
     7 - GET  - 200 -- /api/3/action/package_show?id=row_default_1_fiware-test
     8 - POST - 200 -- /api/3/action/datastore_upsert
     9 - GET  - 200 -- /webhdfs/v1/user/cloud-user/row_default_1/Room1-Room/Room1-Room.txt?op=getfilestatus&user.name=cloud-user
    10 - PUT  - 200 -- /webhdfs/v1/user/cloud-user/row_default_1?op=mkdirs&user.name=cloud-user
    11 - PUT  - 307 -- /webhdfs/v1/user/cloud-user/row_default_1/Room1-Room/Room1-Room.txt?op=create&user.name=cloud-user
    12 - PUT  - 201 -- /webhdfs/v1/user/cloud-user/row_default_1/Room1-Room/Room1-Room.txt?op=create&user.name=cloud-user&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false
    13 - POST - 307 -- /webhdfs/v1/user/cloud-user/row_default_1/Room1-Room/Room1-Room.txt?op=append&user.name=cloud-user
    14 - POST - 200 -- /webhdfs/v1/user/cloud-user/row_default_1/Room1-Room/Room1-Room.txt?op=append&user.name=cloud-user&namenoderpcaddress=int-iot-hadoop-fe-01.novalocal:8020&overwrite=false
```
