# FI-WARE Connectors

The FI-WARE Connectors repository is meant to include all the pieces of software connecting FI-WARE GEs among them or with other external actors (users, legacy software, etc.).

## Structure of the repository

This repository is structured as follows:
* ```flume```, a.k.a. Cygnus, contains all the necessary for connecting Orion Context Broker with Cosmos Big Data through Flume.
* ```sftp``` contains the code for a SFTP-based server which directly talks with HDFS.
* ```resources``` contains additional miscellaneous pieces of code such as basic client templates, example configuration files about any connector, running scripts, etc.

## Contact

* Fermín Galán Márquez (fermin at tid dot es).
* Francisco Romero Bueno (frb at tid dot es).
