In order to build the RPM package, follow the following steps:

* Build the Cygnus .jar with dependencies with `mvn clean compile assembly:single` (see Cygnus README.md
  for additional detail).
* Run the package.sh script (inside the scripts/ directory) which, upon finalization, will generate the .rpm
  file in the rpm/RPMS directory.