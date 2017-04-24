# Installing cygnus-common with RPM (CentOS/RedHat)
Simply configure the FIWARE repository if not yet configured:

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.lab.fiware.org/repo/rpm/6/
    gpgcheck=0
    enabled=1
    EOL

And use your applications manager in order to install the latest version of cygnus-common:

    $ yum install cygnus-common

The above will install cygnus-common at `/usr/cygnus/`.
