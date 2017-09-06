# <a name="top"></a>Installing cygnus-twitter from sources
Content:

* [Prerequisites](#section1)
* [Installing cygnus-twitter](#section2)
    * [Cloning `fiware-cygnus`](#section2.1)
    * [Installing `cygnus-twitter`](#section2.2)
    * [Known issues](#section2.3)
* [Installing dependencies](#section3)

## <a name="section1"></a>Prerequisites
[`cygnus-common`](../../cygnus-common/installation_and_administration_guide/install_from_sources.md) must be installed. This includes Maven, `cygnus` user creation, Apache Flume and `cygnus-flume-ng` script installation.

[Top](#top)

## <a name="section2"></a>Installing Cygnus
### <a name="section2.1"></a>Cloning `fiware-cygnus`
Start by cloning the Github repository:

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus
    $ git checkout <branch>
    
`<branch>` should be typically a stable release branch, e.g. `release/1.1.0`, but could also be `master` (synchronized with the latest release) or `develop` (contains the latest not stable changes).

[Top](#top)

### <a name="section2.2"></a>Installing `cygnus-twitter`
`cygnus-twitter` can be built as a fat Java jar file containing all third-party dependencies (**recommended**):

    $ cd cygnus-twitter
    $ APACHE_MAVEN_HOME/bin/mvn clean compile exec:exec assembly:single
    $ cp target/cygnus-twitter-<x.y.z>-jar-with-dependencies.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib
    
Or as a thin Java jar file:

    $ cd cygnus-twitter
    $ APACHE_MAVEN_HOME/bin/mvn exec:exec package
    $ cp target/cygnus-<x.y.z>.jar APACHE_FLUME_HOME/plugins.d/cygnus/lib

[Top](#top)

### <a name="section2.3"></a>Known issues
It may happen while compiling `cygnus-twitter` the Maven JVM has not enough memory. This can be changed as detailed at the [Maven official documentation](https://cwiki.apache.org/confluence/display/MAVEN/OutOfMemoryError):

    $ export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"

[Top](#top)

## <a name="section3"></a>Installing dependencies
These are the packages you will need to install under `APACHE_FLUME_HOME/plugins.d/cygnus/libext/` **if you did not included them in the cygnus-common jar**:

| Cygnus dependencies | Version | Required by / comments |
|---|---|---|
| mockito-all | 1.9.5 | Unit tests |
| junit | 4.11 | Unit tests |
| log4j | 1.2.17 | Logging |
| slf4j-simple | 1.7.21 | Logging |
| cygnus-common | latest | |

[Top](#top)
