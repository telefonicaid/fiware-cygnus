# Installing cygnus-common with RPM (CentOS/RedHat)

Simply configure the FIWARE release repository if not yet configured:
```
sudo wget -P /etc/yum.repos.d/ https://nexus.lab.fiware.org/repository/raw/public/repositories/el/7/x86_64/fiware-release.repo
```
And use your applications manager in order to install the latest version of cygnus-common:
```
sudo yum install cygnus-common
```
The above will install cygnus-common at `/usr/cygnus/`.
