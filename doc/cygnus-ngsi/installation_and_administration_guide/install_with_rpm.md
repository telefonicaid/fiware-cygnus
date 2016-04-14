#Installing Cygnus with RPM (CentOS/RedHat)
Simply configure the FIWARE repository if not yet configured:

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
    gpgcheck=0
    enabled=1
    EOL

And use your applications manager in order to install the latest version of Cygnus:

    $ yum install cygnus

The above will install Cygnus in `/usr/cygnus/`.

**NOTE**: The available RPM is compiled for Hadoop 0.20.2-cdh3u6, since this is the version run at [FIWARE Lab](https://cosmos.lab.fiware.org/). If you aim is to use Cygnus with a different Hadoop version, then you will have to install from sources (see next section) after editing the `pom.xml` file and adapting the `hadoop-core` dependency to your specific needs. Of course, if you are not going to use the HDFS sink then the available RPM will be perfectly valid for you.
