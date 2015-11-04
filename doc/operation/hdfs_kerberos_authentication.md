# Enabling Kerberos SPNEGO authentication in HDFS

Hadoop Distributed File System (HDFS) can be remotely managed through a REST API called [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html). This API may be used without any kind of security (in this case, it is enough knowing a valid HDFS user name in order to access this user HDFS space), or a Kerberos infrastructure may be used for authenticating the users.

[Kerberos](http://web.mit.edu/kerberos/) is an authentication protocol created by MIT, current version is 5. It is based in symmetric key cryptography and a trusted third party, the Kerberos servers themselves. The protocol is as easy as authenticating to the Authentication Server (AS), which forwards the user to the Key Distribution Center (KDC) with a ticket-granting ticket (TGT) that can be used to retrieve the definitive client-to-server ticket. This ticket can then be used for authentication purposes against a service server (in both directions).

SPNEGO is a mechanism used to negotiate the choice of security technology. Through SPNEGO both client and server may negotiate the usage of Kerberos as authentication technology.   

Kerberos authentication in HDFS is easy to achieve from the command line if the Kerberos 5 client is installed and the user already exists as a principal in the Kerberos infrastructure. Then just get a valid ticket and use the `--negotiate` option in `curl`:

    $ kinit <USER>
    Password for <USER>@<REALM>:
    $ curl -i --negotiate -u:<USER> "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=..."

Nevertheless, Cygnus needs this process to be automated. Let's see how through the configuration.

## Kerberos-related Cygnus configuration
### `conf/cygnus.conf`
This file can be built from the distributed `conf/cugnus.conf.template`. Edit appropriately this part of the `OrionHDFSSink` configuration:

    # Kerberos-based authentication enabling
    cygnusagent.sinks.hdfs-sink.krb5_auth = true
    # Kerberos username
    cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_user = krb5_username
    # Kerberos password
    cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_password = xxxxxxxxxxxxx
    # Kerberos login file
    cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_login_file = /usr/cygnus/conf/krb5_login.conf
    # Kerberos configuration file
    cygnusagent.sinks.hdfs-sink.krb5_auth.krb5_conf_file = /usr/cygnus/conf/krb5.conf

I.e. start enabling (or not) the Kerberos authentication. Then, configure a user with an already registered Kerberos principal, and its password. Finally, specify the location of two special Kerberos files.

### `conf/krb5_login.conf`

Contains the following line, which must not be changed (thus, the distributed file is not a template but the definitive one).

    cygnus_krb5_login {
        com.sun.security.auth.module.Krb5LoginModule required doNotPrompt=false debug=true useTicketCache=false;
    };

### `conf/krb5.conf`

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

## Contact

* Fermín Galán Márquez (fermin at tid dot es).
* Francisco Romero Bueno (frb at tid dot es).
