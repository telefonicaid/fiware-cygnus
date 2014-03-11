# FI-WARE Connectors

The FI-WARE Connectors project is meant to include all the pieces of software connecting FI-WARE GEs among them or with other external actors (users, legacy software, etc.).

## Structure of the repository

This repository is structured following these guidelines:
* There exists a folder per each GE or external actor receiving data. E.g. the cosmos folder is about all the connectors designed to inject data into Cosmos.
* Within a folder, a set of subdirectories will contain specific data connectors. E.g., the cosmos folder contains a subdirectory called cygnus, which is a Flume-based connector for data comming from Orion (and others), and a stfp-injector subdirectory having code implementing a SFTP server running on top of HDFS.

Thus, according to the above guidelines, there are the following FI-WARE connectors:

| Connector | Description |
| :-------- | :---------- |
| fiware-connectors/cosmos/cygnus | Flume-based connector for Cosmos mainly used to persist context data coming from Orion |
| fiware-connectors/cosmos/sftp-injector | SFTP server for injecting data files in HDFS |

## Contact

Fermín Galán Márquez (fgalan at tid dot es)
Francisco Romero Bueno (frb at tid dot es)
