#Installing cygnus-ngsi with RPM (CentOS/RedHat)
Simply configure the FIWARE repository if not yet configured:

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
    gpgcheck=0
    enabled=1
    EOL

And use your applications manager in order to install the latest version of cygnus-ngsi:

    $ yum install cygnus-ngsi

The above will install cygnus-ngsi at `/usr/cygnus/`, together with cygnus-common (it contains all the common dependencies to all Cygnus agents, NGSI agent included).
