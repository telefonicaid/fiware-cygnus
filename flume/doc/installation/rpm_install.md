#Installing Cygnus via RPM
Simply configure the FIWARE repository if not yet configured and use your applications manager in order to install the latest version of Cygnus (CentOS/RedHat example):

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
    gpgcheck=0
    enabled=1
    EOL
    $ yum install cygnus

##Contact
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com)
<br>
Francisco Romero Bueno (francisco.romerobueno@telefonica.com)