# Resources for fiware-cygnus

## Hive basic client

Hive remote client mimicing the native Hive CLI. This can be used as the base for a custom Hive remote client (interactive or not).

# NGSI Notifications

A set of scripts that can be use to simulate Orion NGSI10 notifications. Its main purpose is Cygnus debugging.

* notification-json-compound.sh
* notification-json-md.sh
* notification-json-simple.sh  
* notification-json-2simple.sh  
* notification-xml-compound.sh
* notification-xml-md.sh
* notification-xml-simple.sh
* notification-xml-2simple.sh

All them use two arguments: firts one is the URL to send the notification, second one (optional) is the default organization (a default value is used if the second argument is missing).

```
./notification-json-simple.sh 127.0.0.1:5050/notify Org22
```

In addition the continous_notifier.sh script do a periodic JSON-based notification with a random attribute value, using the same two first arguments and a third one to specify the notification period (in seconds): 

```
./continous_notifier.sh 127.0.0.1:5050/notify Org22 10
```

## Cygnus translators

These scripts are aimed to convert persisted context data from one format to another, e.g. the translator cygnus-translator-0.1-to-0.2.sh converts from CSV to Json format when executed in a Hadoop cluster NameNode.

## CKAN scripts

Tooling scripts related with CKAN management.

## MySQL scripts

Tooling scripts related with MySQL management.

## Contact

* Francisco Romero Bueno (frb at tid dot es).
* Fermín Galán Márquez (fermin at tid dot es).
