#<a name="top"></a>Resources for fiware-cygnus

* [Hive clients](#section1)
* [NGSI notifications](#section2)
* [Cygnus translators](#section3)
* [CKAN scripts](#section4)
* [MySQK scripts](#section5)
* [Contact](#section6)

##<a name="section1"></a>Hive clients

# NGSI Notifications
A set of Hive clients mimicing the native Hive CLI written in several programming languages both for HiveServer1 and HiveServer2. Specifically, there are clients for:

* Java:
    * [`hiveserver1-client`](./hiveclients/java/hiveserver1-client)
    * [`hiveserver2-client`](/.hiveclients/java/hiveserver2-client)
* Python:
    * [`hiveserver1-client.py`](./hiveclients/python/hiveserver1-client.py)
    * [`hiveserver2-client.py`](./hiveclients/python/hiveserver2-client.py)

These clients can be used as the base for a custom Hive remote client (interactive or not).

[Top](#top)

##<a name="section2"></a>NGSI notifications

A set of scripts that can be use to simulate Orion NGSI10 notifications. Its main purpose is Cygnus debugging.

* notification-json-compound.sh
* notification-json-md.sh
* notification-json-simple.sh  
* notification-json-2simple.sh  

All them use two arguments: first one is the URL to send the notification, second one (optional) is the default organization (a default value is used if the second argument is missing).

```
./notification-json-simple.sh 127.0.0.1:5050/notify Org22
```

In addition the continous_notifier.sh script do a periodic JSON-based notification with a random attribute value, using the same two first arguments and a third one to specify the notification period (in seconds):

```
./continous_notifier.sh 127.0.0.1:5050/notify Org22 10
```

[Top](#top)

##<a name="section3"></a>Cygnus translators

These scripts are aimed to convert persisted context data from one format to another, e.g. the translator cygnus-translator-0.1-to-0.2.sh converts from CSV to Json format when executed in a Hadoop cluster NameNode.

[Top](#top)

##<a name="section4"></a>CKAN scripts

Tooling scripts related with CKAN management.

[Top](#top)

##<a name="section5"></a>MySQL scripts

Tooling scripts related with MySQL management.

[Top](#top)

##<a name="section6"></a>Contact

Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com)

[Top](#top)
