In order to build the RPM package, follow the following steps:

* Build the Cygnus .jar with dependencies with `mvn clean compile exec:exec assembly:single` (see Cygnus [README.md](../README.md)
  for additional detail).
* Run the `package.sh` script (inside the scripts/ directory) which, upon finalization, will generate the .rpm
  file in the `rpm/RPMS` directory. You must specify the version number (matching with the one in the pom.xml
  file) with the `-v` argument, e.g.:
  ```
  ./package.sh -v 0.3
  ```
  You can see full options of script package.sh typing `./package.sh -h`

Note: due to a bug in rpm generation ([issue #329](https://github.com/telefonicaid/fiware-connectors/issues/329)) all packages 
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