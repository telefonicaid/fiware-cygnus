# <a name="top"></a>TwitterHDFSSink
Content:

* [Functionality](#section1)
    * [Mapping Twitter events to flume events](#section1.1)
    * [Mapping Flume events to HDFS data structures](#section1.2)
    * [Hive](#section1.3)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
    * [Important notes](#section2.3)
        * [About the binary backend](#section2.3.2)
        * [About batching](#section2.3.3)
* [Programmers guide](#section3)
    * [`TwitterHDFSSink` class](#section3.1)
    * [`HDFSBackendImpl` class](#section3.2)
    * [OAuth2 authentication](#section3.3)
    * [Kerberos authentication](#section3.4)

## <a name="section1"></a>Functionality
`com.telefonica.iot.cygnus.sinks.TwitterHDFSSink`, or simply `TwitterHDFSSink` is a sink designed to persist tweets data events within a [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html) deployment. The data is provided by Twitter.

Tweets are always transformed into internal Flume events at twitter agent source. In the end, the information within these Flume events must be mapped into specific HDFS data structures at the Twitter agent sinks.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping Twitter events to flume events
Received Twitter events are transformed into Flume events (specifically `TwitterEvent`), independently of the final backend where it is persisted.

The body of a flume TwitterEvent is the representation of a tweet in JSON format. Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

### <a name="section1.2"></a>Mapping Flume events to HDFS data structures
[HDFS organizes](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html#The_File_System_Namespace) the data in folders containinig big data files. Such organization is exploited by `TwitterHDFSSink` each time a Flume event is going to be persisted.

A file named `/user/<hdfs_folder>/<hdfs_file>` is created (if not existing yet), where `<hdfs_folder>` and `<hdfs_file>` are configuration parameters.

In this file, a JSON line is created per each tweet. The tweet contains all the standard fields specified by [Twitter](https://dev.twitter.com/overview/api/tweets). In the file, tweets are separated by `\n`.
To avoid confusions and make the HDFS file reliable all `\n` in tweets have been removed (since they do not provide semantic information). This way, the only `\n` characters that appear in the file are those that split tweets into lines.
[Top](#top)

### <a name="section1.3"></a>Hive
Hive is currently not supported in this version of the `TwitterHDFSSink`.

[Top](#top)


## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`TwitterHDFSSink` is configured through the following parameters:

| Parameter | Mandatory | Default value | Comments |
|---|---|---|---|
| type | yes | N/A | Must be <i>com.telefonica.iot.cygnus.sinks.TwitterHDFSSink</i> |
| channel | yes | N/A ||
| enable\_lowercase | no | false | <i>true</i> or <i>false</i>. |
| backend.impl | no | rest | <i>rest</i>, if a WebHDFS/HttpFS-based implementation is used when interacting with HDFS; or <i>binary</i>, if a Hadoop API-based implementation is used when interacting with HDFS. |
| backend.max_conns | no | 500 | Maximum number of connections allowed for a Http-based HDFS backend. Ignored if using a binary backend implementation. |
| backend.max_conns_per_route | no | 100 | Maximum number of connections per route allowed for a Http-based HDFS backend. Ignored if using a binary backend implementation. |
| hdfs_host | no | localhost | FQDN/IP address where HDFS Namenode runs, or comma-separated list of FQDN/IP addresses where HDFS HA Namenodes run. |
| hdfs_port | no | 14000 | <i>14000</i> if using HttpFS (rest), <i>50070</i> if using WebHDFS (rest), <i>8020</i> if using the Hadoop API (binary). |
| hdfs_username | yes | N/A | An already existent user in HDFS. |
| hdfs_password | yes | N/A | Password for the above `hdfs_username`; this is only required for Hive authentication. |
| oauth2_token | yes | N/A | OAuth2 token required for the HDFS authentication. |
| batch_size | no | 1 | Number of events accumulated before persistence. |
| batch_timeout | no | 30 | Number of seconds the batch will be building before it is persisted as it is. |
| batch_ttl | no | 10 | Number of retries when a batch cannot be persisted. Use `0` for no retries, `-1` for infinite retries. Please, consider an infinite TTL (even a very large one) may consume all the sink's channel capacity very quickly. |
| krb5_auth | no | false | <i>true</i> or <i>false</i>. |
| krb5_user | yes | <i>empty</i> | Ignored if `krb5_auth=false`, mandatory otherwise. |
| krb5_password | yes | <i>empty</i> | Ignored if `krb5_auth=false`, mandatory otherwise. |
| krb5\_login\_conf\_file | no | /usr/cygnus/conf/krb5_login.conf | Ignored if `krb5_auth=false`. |
| krb5\_conf\_file | no | /usr/cygnus/conf/krb5.conf | Ignored if `krb5_auth=false`. |
| hdfs_folder | yes | N/A | The folder where the file with tweets will be stored. |
| hdfs_file | yes | N/A | The name of the file where tweets will be stored. This file will be created inside the `hdfs_folder` |

A configuration example could be:

    # ============================================
    # TwitterHDFSSink configuration
    # channel name from where to read notification events
    cygnus-twitter.sinks.hdfs-sink.channel = hdfs-channel
    # sink class, must not be changed
    cygnus-twitter.sinks.hdfs-sink.type = com.telefonica.iot.cygnus.sinks.TwitterHDFSSink
    # true if lower case is wanted to forced in all the element names, false otherwise
    # cygnus-twitter.sinks.hdfs-sink.enable_lowercase = false
    # rest if the interaction with HDFS will be WebHDFS/HttpFS-based, binary if based on the Hadoop API
    # cygnus-twitter.sinks.hdfs-sink.backend.impl = rest
    # maximum number of Http connections to HDFS backend
    # cygnus-twitter.sinks.hdfs-sink.backend.max_conns = 500
    # maximum number of Http connections per route to HDFS backend
    # cygnus-twitter.sinks.hdfs-sink.backend.max_conns_per_route = 100
    # Comma-separated list of FQDN/IP address regarding the HDFS Namenode endpoints
    # If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
    # cygnus-twitter.sinks.hdfs-sink.hdfs_host = localhost
    # port of the HDFS service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs
    # cygnus-twitter.sinks.hdfs-sink.hdfs_port = 14000
    # username allowed to write in HDFS
    cygnus-twitter.sinks.hdfs-sink.hdfs_username = hdfs_username
    # password for the above username; this is only required for Hive authentication
    cygnus-twitter.sinks.hdfs-sink.hdfs_password = xxxxxxxx
    # OAuth2 token for HDFS authentication
    cygnus-twitter.sinks.hdfs-sink.oauth2_token = xxxxxxxx
    # timeout for batch accumulation
    # cygnus-twitter.sinks.hdfs-sink.batch_timeout = 30
    # number of retries upon persistence error
    # cygnus-twitter.sinks.hdfs-sink.batch_ttl = 10
    # Hive enabling
    # cygnus-twitter.sinks.hdfs-sink.hive = false
    # Hive server version, 1 or 2 (ignored if hive is false)
    # cygnus-twitter.sinks.hdfs-sink.hive.server_version = 2
    # Hive FQDN/IP address of the Hive server (ignored if hive is false)
    # cygnus-twitter.sinks.hdfs-sink.hive.host = localhost
    # Hive port for Hive external table provisioning (ignored if hive is false)
    # cygnus-twitter.sinks.hdfs-sink.hive.port = 10000
    # Hive database type, available types are default-db and namespace-db
    # cygnus-twitter.sinks.hdfs-sink.hive.db_type = default-db
    # Kerberos-based authentication enabling
    # cygnus-twitter.sinks.hdfs-sink.krb5_auth = false
    # Kerberos username (ignored if krb5_auth is false)
    cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
    # Kerberos password (ignored if krb5_auth is false)
    cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
    # Kerberos login file (ignored if krb5_auth is false)
    # cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_login_conf_file = /usr/cygnus/conf/krb5_login.conf
    # Kerberos configuration file (ignored if krb5_auth is false)
    # cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf
    # Set folder and file to store tweets
    cygnus-twitter.sinks.hdfs-sink.hdfs_folder = olympic_games_2016
    cygnus-twitter.sinks.hdfs-sink.hdfs_file = tweets.txt

[Top](#top)



### <a name="section2.3"></a>Important notes

#### <a name="section2.3.2"></a>About the binary backend
Current implementation of the HDFS binary backend does not support any authentication mechanism.

A desirable authentication method would be OAuth2, since it is the standard in FIWARE, but this is not currenty supported by the remote RPC server the binary backend accesses.

Valid authentication mechanims are Kerberos and Hadoop Delegation Token, nevertheless none has been used and the backend simply requires a username (the one configured in `hdfs_username`) in order the `cygnus` user (the one running Cygnus) impersonates it.

Thus, it is not recommended to use this backend in multi-user environment, or at least not without accepting the risk any user may impersonate any other one by simply specifying his/her username.

There exists an [issue](https://github.com/telefonicaid/fiware-cosmos/issues/111) about adding OAuth2 support to the Hadoop RPC mechanism, in the context of the [`fiware-cosmos`](https://github.com/telefonicaid/fiware-cosmos) project.

[Top](#top)

#### <a name="section2.3.3"></a>About batching
As explained in the [programmers guide](#section3), `TwitterHDFSSink` extends `TwitterSink`, which provides a built-in mechanism for collecting events from the internal Flume channel. This mechanism allows exteding classes have only to deal with the persistence details of such a batch of events in the final backend.

What is important regarding the batch mechanism is it largely increases the performance of the sink, because the number of writes is dramatically reduced. Let's see an example, let's assume a batch of 100 Flume events. In the best case, all these events regard to the same entity, which means all the data within them will be persisted in the same HDFS file. If processing the events one by one, we would need 100 writes to HDFS; nevertheless, in this example only one write is required. Obviously, not all the events will always regard to the same unique entity, and many entities may be involved within a batch. But that's not a problem, since several sub-batches of events are created within a batch, one sub-batch per final destination HDFS file. In the worst case, the whole 100 entities will be about 100 different entities (100 different HDFS destinations), but that will not be the usual scenario. Thus, assuming a realistic number of 10-15 sub-batches per batch, we are replacing the 100 writes of the event by event approach with only 10-15 writes.

The batch mechanism adds an accumulation timeout to prevent the sink stays in an eternal state of batch building when no new data arrives. If such a timeout is reached, then the batch is persisted as it is.

By default, `TwitterHDFSSink` has a configured batch size and batch accumulation timeout of 1 and 30 seconds, respectively. Nevertheless, as explained above, it is highly recommended to increase at least the batch size for performance purposes. Which are the optimal values? The size of the batch it is closely related to the transaction size of the channel the events are got from (it has no sense the first one is greater then the second one), and it depends on the number of estimated sub-batches as well. The accumulation timeout will depend on how often you want to see new data in the final storage.

[Top](#top)

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`TwitterHDFSSink` class
`TwitterHDFSSink` extends the base `TwitterSink`. The methods that are extended are:

    void persistBatch(Batch batch) throws Exception;

A `Batch` contanins a set of `TwitterEvent` objects, which are the result of parsing the notified context data events. Data within the batch is classified by destination, and in the end, a destination specifies the HDFS file where the data is going to be persisted. Thus, each destination is iterated in order to compose a per-destination data string to be persisted thanks to any `HDFSBackend` implementation (binary or rest).

    public void start();

An implementation of `HDFSBackend` is created. This must be done at the `start()` method and not in the constructor since the invoking sequence is `TwitterHDFSSink()` (contructor), `configure()` and `start()`.

    public void configure(Context);

A complete configuration as the described above is read from the given `Context` instance.

[Top](#top)

### <a name="section3.2"></a>`HDFSBackendImpl` class
This is a convenience backend class for HDFS that extends the `HttpBackend` abstract class (provides common logic for any Http connection-based backend) and implements the `HDFSBackend` interface (provides the methods that any HDFS backend must implement). Relevant methods are:

    public void createDir(String dirPath) throws Exception;

Creates a HDFS directory, given its path.

    public void createFile(String filePath, String data) throws Exception;

Creates a HDFS file, given its path, and writes initial data to it.

    public void append(String filePath, String data) throws Exception;

Appends new data to an already existent given HDFS file.

    public boolean exists(String filePath) throws Exception;

Checks if a HDFS file, given its path, exists ot not.

    public void provisionHiveTable(FileFormat fileFormat, String dirPath, String tag) throws Exception;

[Top](#top)

### <a name="section3.3"></a>OAuth2 authentication
[OAuth2](http://oauth.net/2/) is the evolution of the OAuth protocol, an open standard for authorization. Using OAuth, client applications can access in a secure way certain server resources on behalf of the resource owner, and the best, without sharing their credentials with the service. This works because of a trusted authorization service in charge of emitting some pieces of security information: the access tokens. Once requested, the access token is attached to the service request in order the server may ask the authorization service for the validity of the user requesting the access (authentication) and the availability of the resource itself for this user (authorization).

A detailed architecture of OAuth2 can be found [here](http://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/PEP_Proxy_-_Wilma_-_Installation_and_Administration_Guide), but in a nutshell, FIWARE implements the above concept through the Identity Manager GE ([Keyrock](http://catalogue.fiware.org/enablers/identity-management-keyrock) implementation) and the Access Control ([AuthZForce](http://catalogue.fiware.org/enablers/authorization-pdp-authzforce) implementation); the join of this two enablers conform the OAuth2-based authorization service in FIWARE:

* Access tokens are requested to the Identity Manager, which is asked by the final service for authentication purposes once the tokens are received. Please observe by asking this the service not only discover who is the real FIWARE user behind the request, but the service has full certainty the user is who he/she says to be.
* At the same time, the Identity Manager relies on the Access Control for authorization purposes. The access token gives, in addition to the real identity of the user, his/her roles according to the requested resource. The Access Control owns a list of policies regarding who is allowed to access all the resources based on the user roles.

This is important for Cygnus since HDFS (big) data can be accessed through the native [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html) RESTful API. And it may be protected with the above mentioned mechanism. If that's the case, simply ask for an access token and add it to the configuration through `cygnus-twitter.sinks.hdfs-sink.oauth2_token` parameter.

In order to get an access token, do the following request to your OAuth2 tokens provider; in FIWARE Lab this is `cosmos.lab.fi-ware.org:13000`:

    $ curl -X POST "http://cosmos.lab.fi-ware.org:13000/cosmos-auth/v1/token" -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=password&username=frb@tid.es&password=xxxxxxxx”
    {"access_token": "qjHPUcnW6leYAqr3Xw34DWLQlja0Ix", "token_type": "Bearer", "expires_in": 3600, "refresh_token": “V2Wlk7aFCnElKlW9BOmRzGhBtqgR2z"}

As you can see, your FIWARE Lab credentials are required in the payload, in the form of a password-based grant type (this will be the only time you have to give them).

[Top](#top)

### <a name="section3.4"></a>Kerberos authentication
Hadoop Distributed File System (HDFS) can be remotely managed through a REST API called [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html). This API may be used without any kind of security (in this case, it is enough knowing a valid HDFS user name in order to access this user HDFS space), or a Kerberos infrastructure may be used for authenticating the users.

[Kerberos](http://web.mit.edu/kerberos/) is an authentication protocol created by MIT, current version is 5. It is based in symmetric key cryptography and a trusted third party, the Kerberos servers themselves. The protocol is as easy as authenticating to the Authentication Server (AS), which forwards the user to the Key Distribution Center (KDC) with a ticket-granting ticket (TGT) that can be used to retrieve the definitive client-to-server ticket. This ticket can then be used for authentication purposes against a service server (in both directions).

SPNEGO is a mechanism used to negotiate the choice of security technology. Through SPNEGO both client and server may negotiate the usage of Kerberos as authentication technology.   

Kerberos authentication in HDFS is easy to achieve from the command line if the Kerberos 5 client is installed and the user already exists as a principal in the Kerberos infrastructure. Then just get a valid ticket and use the `--negotiate` option in `curl`:

    $ kinit <USER>
    Password for <USER>@<REALM>:
    $ curl -i --negotiate -u:<USER> "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=..."

Nevertheless, Cygnus needs this process to be automated. Let's see how through the configuration.

[Top](#top)

#### `conf/cygnus.conf`
This file can be built from the distributed `conf/cugnus.conf.template`. Edit appropriately this part of the `NGSIHDFSSink` configuration:

    # Kerberos-based authentication enabling
    cygnus-twitter.sinks.hdfs-sink.krb5_auth = true
    # Kerberos username
    cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
    # Kerberos password
    cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
    # Kerberos login file
    cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_login_file = /usr/cygnus/conf/krb5_login.conf
    # Kerberos configuration file
    cygnus-twitter.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

I.e. start enabling (or not) the Kerberos authentication. Then, configure a user with an already registered Kerberos principal, and its password. Finally, specify the location of two special Kerberos files.

[Top](#top)

#### `conf/krb5_login.conf`

Contains the following line, which must not be changed (thus, the distributed file is not a template but the definitive one).

    cygnus_krb5_login {
        com.sun.security.auth.module.Krb5LoginModule required doNotPrompt=false debug=true useTicketCache=false;
    };

[Top](#top)

#### `conf/krb5.conf`

This file can be built from the distributed `conf/krb5.conf.template`. Edit it appropriately, basically by replacing `EXAMPLE.COM` by your Kerberos realm (this is the same than your domain, but uppercase, i.e. the realm for `example.com` is `EXAMPLE.COM`) and by configuring your Kerberos Key Distribution Center (KDC) and your Kerberos admin/authentication server (ask your netowork administrator in order to know them).

    [libdefaults]
     default_realm = EXAMPLE.COM
     dns_lookup_realm = false
     dns_lookup_kdc = false
     ticket_lifetime = 24h
     renew_lifetime = 7d
     forwardable = true

    [realms]
     EXAMPLE.COM = {
      kdc = kdc.example.com
      admin_server = admin_server.example.com
     }

    [domain_realms]
     .example.com = EXAMPLE.COM
     example.com = EXAMPLE.COM

[Top](#top)
