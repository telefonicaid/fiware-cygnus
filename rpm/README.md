In order to build the RPM packages, follow the following steps:

* Build the Cygnus .jars both _common_ and _ngsi_ with `mvn clean compile exec:exec assembly:single` (see [Cygnus-common README](../cygnus-common/README.md)
  and [Cygnus-NGSI README](../cygnus-ngsi/README.md) for additional detail).
* Run the `package.sh` script (inside the `rpm` directory) which, upon finalization, will generate the `.rpm`'s
  files in the `cygnus-common/spec/RPMS/x86_64` and `cygnus-ngsi/spec/RPMS/x86_64` directories. You must specify the version number (matching with the one in the pom.xml
  file) with the `-v` argument. Release for the rpm package must also specified whit `-r` option.
  ```
  ./package.sh -v 0.13.0 -r 5.ge58dffa
  ```
  You can see full options of script package.sh typing `./package.sh -h`

  The results packages contains:

  - `cygnus-common/spec/RPMS/x86_64`: has the Cygnus common features and the Apache Flume SW
  - `cygnus-ngsi/spec/RPMS/x86_64`: has only the jar and config templates for NGSI connector.

When a package is built it introduces three features in Cygnus SW:

* Daily log rotation: use logrotate tool to perform this operation. View
  [logrotate config file](../cygnus-common/spec/SOURCES/logrotate.d/logrotate-cygnus-daily) for more info
* Automatic deletion of files older than 30 days programmed in cron:
  [cron file](../cygnus-common/spec/SOURCES/cron.d/cleanup_old_cygnus_logfiles)
* init.d service script: which is a common service script (named as `cygnus`) with the usual operations
  `start`, `stop`, `restart` and `status`. It is invoked as other service script: `service cygnus start`
  and this actilon starts **all** instances configured for Cygnus but service script has a special
  feature: if a second parameter is provided (after the action) the script only acts over the
  instance indicated. I.E.
  `service cygnus start test1` In this case service script tries to start instance of cygnus called test1. If test1
  instance is not configured a special error will be shown.
  `service cygnus stop test1` Only test1 will be stopped. And so for `restart` and `status` operations.

**Note:** due to a bug in rpm generation ([issue #329](https://github.com/telefonicaid/fiware-cygnus/issues/329)) all packages
that were built before 2015/03/05 and installed should be erased manually and install a new one build after 2015/03/05.

The date of the building of the installed package can be viewed with `rpm -qi cygnus` and look into **Build Date** field:

```shell
$ rpm -qi cygnus
Name        : cygnus                       Relocations: (not relocatable)
Version     : 0.6.0                             Vendor: Telefonica I+D
Release     : 35.g26e07c4                   Build Date: Mon 16 Feb 2015 03:56:58 PM CET
Install Date: Mon 16 Feb 2015 04:14:04 PM CET      Build Host: ci-iot-deven-01
Group       : Applications/cygnus           Source RPM: cygnus-0.6.0-35.g26e07c4.src.rpm
Size        : 107280119                        License: AGPLv3
Signature   : (none)
Summary     : Package for cygnus component
Description :
This connector is a (conceptual) derivative work of ngsi2cosmos, and implements
a Flume-based connector for context data coming from Orion Context Broker.
```

All packages build before 2015/03/05 should be erased and generated again.
