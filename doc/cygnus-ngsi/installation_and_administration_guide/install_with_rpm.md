# Installing cygnus-ngsi with RPM (CentOS/RedHat)

Simply configure the FIWARE release repository if not yet configured:
```
sudo wget -P /etc/yum.repos.d/ https://nexus.lab.fiware.org/repository/raw/public/repositories/el/7/x86_64/fiware-release.repo
```
And use your applications manager in order to install the latest version of cygnus-ngsi:
```
sudo yum install cygnus-ngsi
```
The above will install cygnus-ngsi at `/usr/cygnus/`, together with cygnus-common (it contains all the common dependencies to all Cygnus agents, NGSI agent included).
