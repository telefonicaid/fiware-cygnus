## Mocks for Cygnus

Mocks are simulated requests that mimic the behavior of real requests in controlled ways.

#### Usage:

```
 ****************************************************************************
 *  usage: python cygnus_mock.py <port> <organization> <dataset> <resource> *
 *      values by default:                                                  *
 *           port        : 8090                                             *
 *           organization: orga_default                                     *
 *           dataset     : fiware-test                                      *
 *           resource    : room1-room                                       *
 *       Note: all values will be defined in lowercase.                     *
 *                  ( use <Ctrl-C> to stop )                                *
 ****************************************************************************
```

#### Paths mocked:

``` 
     1 - GET  - /api/util/status
     2 - GET  - /api/3/action/organization_show?id=orga_default
     3 - POST - /api/3/action/organization_create
     4 - POST - /api/3/action/package_create
     5 - POST - /api/3/action/resource_create
     6 - POST - /api/3/action/datastore_create
     7 - GET  - /api/3/action/package_show?id=orga_default_fiware-test
     8 - POST - /api/3/action/datastore_upsert
     9 - GET  - /webhdfs/v1/user/username/orga_default/Room1-Room/Room1-Room.txt?op=getfilestatus&user.name=username
    10 - PUT  - /webhdfs/v1/user/username/orga_default?op=mkdirs&user.name=username
    11 - PUT  - /webhdfs/v1/user/username/orga_default/Room1-Room/Room1-Room.txt?op=create&user.name=username
    12 - POST - /webhdfs/v1/user/username/orga_default/Room1-Room/Room1-Room.txt?op=append&user.name=username
```
