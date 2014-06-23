In order to build the RPM package, follow the following steps:

* Build the Cygnus .jar with dependencies with `mvn clean compile assembly:single` (see Cygnus README.md
  for additional detail).
* Run the package.sh script (inside the scripts/ directory) which, upon finalization, will generate the .rpm
  file in the rpm/RPMS directory. You must specify the version number (matching with the one in the pom.xml
  file) with the `-v`argument, e.g.:
  
```
./package.sh -v 0.3
```