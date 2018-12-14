# <a name="top"></a>Diagnosis procedures
Content:

* [Problem: Logs are not traced](#section1)
    * [Reason: There may be a problem with the logging folder](#section1.1) 
    * [Reason: There may be a problem with the logging configuration of Cygnus](#section1.2)
* [Problem: The API does not work](#section2)
    * [Reason: There may be a problem with the configured port](#section2.1)
    * [Reason: The configured port is not open in the firewall](#section2.2)
* [Problem: The GUI does not work](#section4)
* [Other problems](#section5)

## <a name="section1"></a>Problem: Logs are not traced
### <a name="section1.1"></a>Reason: There may be a problem with the logging folder
First, check the folder `/var/log/cygnus` has been created:

```
$ ls -la /var/log/cygnus
total 117712
drwxr-xr-x   3 frb   staff       102 19 feb 09:02 .
drwxr-xr-x  61 root  wheel      2074  6 may 07:32 ..
-rw-r--r--   1 frb   staff  60265711 19 feb 09:00 cygnus.log
```
    
Second, check the ownership of the above folder. If the owner is not `cygnus` and the group is not `cygnus`, you must change the ownership:

    $ chown cygnus:cygnus /var/log/cygnus
    
Third, check the permissions of the log folder. If the permissions does not contain write permissions, add them:

    $ chmod a+w /var/log/cygnus

[Top](#top)

### <a name="section1.2"></a>Reason: There may be a problem with the logging configuration of Cygnus
Check the log4j configuration is using a file-related appender.

First of all, check you have a valid `lo4j.properties` file (not a template) in `/usr/cygnus/conf/`.

Then, verify you have these lines configured:

    flume.log.dir=/var/log/cygnus/
    flume.log.file=cygnus.log
    
Another possibility is you are changing the above configuration from the command line, by using the following arguments:

    -Dflume.root.logger=<log_level>,<appender>
    
Check the apender value is `LOG_FILE`.

[Top](#top)

## <a name="section2"></a>Problem: The API does not work
### <a name="section2.1"></a>Reason: There may be a problem with the configured port
Check the port you are using in the request is the one configued in Cygnus. By default, it is `5080`, but can be modified by Cygnus administrator.

[Top](#top)

### <a name="section2.2"></a>Reason: The configured port is not open in the firewall
The API port may be properly configured but not opened in the firewall (if such a firewall is running) protecting your machine.

The specific solution depends on the specific firewall. Here, `iptables`-based firewalling is shown. Please, check the port is open (default `5080` is used in the examples):

```
$ (sudo) iptables -L
Chain INPUT (policy ACCEPT)
target     prot opt source               destination
ACCEPT     tcp  --  anywhere             anywhere            tcp dpt:5080

Chain FORWARD (policy ACCEPT)
target     prot opt source               destination

Chain OUTPUT (policy ACCEPT)
target     prot opt source               destination
```

If not, open it:

    $ (sudo) iptables -I INPUT -p tcp --dport 5080 -j ACCEPT

[Top](#top)

## <a name="section3"></a>Problem: The GUI does not work
Coming soon.

[Top](#top)

## <a name="section4"></a>Other problems
Please look for `fiware-cygnus` tag in [stackoverflow.com](http://stackoverflow.com/search?q=fiware+cygnus).

[Top](#top)
