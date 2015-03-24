# ngsi2cosmos to Cygnus migration

[ngsi2cosmos](https://github.com/telefonicaid/fiware-livedemoapp/tree/master/package/ngsi2cosmos) was the origin of Cygnus, in the sense it covered the HDFS-related persistence of Orion context data. Cygnus has evolved such concept allowing for a multi-storage persistence based not only in HDFS but MySQL and CKAN.

ngsi2cosmos is no more supported, thus it is highly recommended to those users still using ngs2cosmos to move to Cygnus. The goal of this document is to explain such migration path. Of course, if you have never used ngsi2cosmos for HDFS persistence, or you plan to use it for MySQL/CKAN persistence, simply install the last version of Cygnus and ignore this document.

## ngsi2cosmos vs. Cygnus

Feature | ngs2cosmos | Cygnus 0.1 | Cygnus 0.2 | Cygnus 0.3 or higher
--- | --- | --- | --- | ---
HDFS persistence | CSV-like | CSV-like | Json | Json
MySQL persistence | no | no | yes | yes | yes
CKAN persistence | no | no | yes | yes | yes
Complex attributes | no | no | yes | yes | yes
Metadata support | no | no | no | yes
Persistence mode | row | row | row | row or column
Files organization | per attribute | per attribute | per-entity | per-entity 

As can be seen, ngs2cosmos is functionally equivalent to Cygnus 0.1, i.e. simple context data with no metadata is stored as CSV-like data in HDFS files, following an attribute-value or row-like schema:

    2014-02-27T14:46:21|13453464536|Room1|Room|temperature|centigrade|26.5

If no additional features are required in your deployment, simply move to Cygnus 0.1.

Nevertheless, if you need to persist into MySQL or CKAN premises as well, or you plan to manage complex attributes, then move to Cygnus 0.2, but please notice that Json-like persistence is used instead. Focusing on HDFS, the data would be persisted as (assuming a complex attribute):


    {"ts":"13453464536", "iso8601data":"2014-02-27T14_46_21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":{"real":"26","decimal":"5"}}

Finally, if your data is accompanied by metadata or you want to persist all the data in a single line instead of multiple attribute-value lines, Cygnus 0.3 and higher is your best bet. An example of row mode is (assuming a complex attribute and metadata):

    {"recvTimeTs":"13453464536", "recvTime":"2014-02-27T14:46:21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":{"real":"26","decimal":"5"}, "attrMd":[{name:ID, type:string, value:ground}]}

Being the column mode as:

    {"recvTime":"2014-02-27T14:46:21", "temperature":{"real":"26","decimal":"5"}, "temperature_md":[{"name":"ID", "type":"string", "value":"ground"}]}

## Moving to Cygnus 0.1

This kind of migration is as simple as installing Cygnus 0.1. Take it from https://github.com/telefonicaid/fiware-connectors/releases/tag/release-0.1, or `git checkout` to `release/0.1` after cloning the source code:

    $ git clone https://github.com/telefonicaid/fiware-connectors
    $ git checkout release/0.1

Follow the `flume/README.md` in order to get an instance. Properly configured, Cygnus 0.1 is 100% functionally equivalent to ngsi2cosmos, thus nothing has to be done in Orion nor Cosmos.  

## Moving to Cygnus 0.2

First of all, install Cygnus 0.2 (in fact, install 0.2.1 which has an important bugfix). Take it from https://github.com/telefonicaid/fiware-connectors/releases/tag/release-0.2.1, or `git checkout` to `release/0.2.1` after cloning the source code:

    $ git clone https://github.com/telefonicaid/fiware-connectors
    $ git checkout release/0.2.1

Follow the `flume/README.md` in order to get an instance.

Then, due to Cygnus 0.2 (and higher) persists the data in Json format, after installing Cygnus 0.2.1 you will need to translate the data within your HDFS space. In order to do so, please follow the guidelines within `doc/operation/cygnus_0.1_to_0.2_migration.md`.

## Moving to Cygnus 0.3 or higher

First of all, install Cygnus 0.3 (or higher, if available). Take it from https://github.com/telefonicaid/fiware-connectors/releases/tag/release-3, or `git checkout` to `release/0.3` after cloning the source code:

    $ git clone https://github.com/telefonicaid/fiware-connectors
    $ git checkout release/0.3 # or release/0.4 etc

Follow the `flume/README.md` in order to get an instance.

Then, due to from Cygnus 0.2 the data is persisted in Json format, after installing Cygnus 0.3 you will need to translate the data. In order to do so, please follow the guidelines within `doc/operation/cygnus_0.1_to_0.3_migration.md`.

## Contact information

Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com)