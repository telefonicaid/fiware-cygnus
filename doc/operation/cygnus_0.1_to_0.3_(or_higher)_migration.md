# Cygnus 0.1 to 0.3 (or higher) migration

This document is addressed to those using Cygnus 0.1 and wanting to move to Cygnus 0.3.

Such a change of version is not as simple as deploying the last version of the Cygnus jar, which must be done for sure, and a data translation is mandatory in your Cosmos premises. There are three main reasons:

### Json instead of CSV-like files

First of all, starting from Cygnus 0.2 a very important change was made in the way the data was persisted in HDFS: the usage of CSV-like files was discarded and Json-like files where adopted instead.

Thus, having a line such as the following one in Cygnus 0.1:

    2014-02-27T14:46:21|13453464536|Room1|Room|temperature|centigrade|26.5

becomes the following line in Cygnus 0.3:

    {"recvTimeTs":"13453464536", "recvTime":"2014-02-27T14:46:21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":"26.5", "attrMd":[]}

In fact, the subyacent real reason was the appearance of complex attribute values and attribute metada in Orion, which naturally leaded us to adopt Json (not only in the HDFS sink, but in other storages such as MySQL and CKAN as well). This allows us for having things such as:

    {"recvTimeTs":"13453464536", "recvTime":"2014-02-27T14:46:21", "entityId":"Room1", "entityType":"Room", "attrName":"temperature", "attrType":"centigrade", "attrValue":{"real":"26","decimal":"5"}, "attrMd":[{name:ID, type:string, value:ground}]}

### Per-entity files instead of per-attribute-value files

The second reason avoiding a simple change of Cygnus version is the way the data is organized into files. Within Cygnus 0.1, a file was used per each attribute an entity may have, i.e. a `Room1`  entity of type `Room` having two attributes `temperature` (`centigrade` type) and `pressure` (`pascal` type) has its data persisted in these two files:

    /user/myser/mydata/Room1-Room-temperature-centigrade.txt
    /user/myser/mydata/Room1-Room-pressure-pascal.txt

Nevertheless, within Cygnus 0.3 a single file is used for the whole entity:

    /user/myser/mydata/Room1-Room/Room1-Room.txt

Please observe a dedicated folder is created as a container of the data file. Because from Cygnus 0.3 there are two different persistence modes (see velow), multiple files cannot coexist anymore in the same HDFS folder if we want Hive works. 

### Per-row and per-column persistence mode

Cygnus 0.3 has another novelty: the possibility to persist all the attributes an entity has in a single line. We call this persistnece mode "per-column", being called the traditional way (a line per each attribute) "per-row".

A per-column version of our example is:

    {"recvTime":"2014-02-27T14:46:21", "temperature":{"real":"26","decimal":"5"}, "temperature_md":[{"name":"ID", "type":"string", "value":"ground"}]}

Nevertheless, this does not affect the migration since the per-row mode used in Cygnus 0.1 is maintained in the data translation.

## Preparing the data translation

Your data within Cosmos is very important: it cannot be lost nor modified. Therefore the data translation must be prepared in advance:

1. First of all, identify the HDFS path where your CSV-like data is. Let's say it is `/user/myuser/mydata/`. 
2. Then, identify the HDFS path where you want to put your data in the new Json format. Let's say it is `/user/myuser/mydata2/`. <b>It is mandatory you use a path different than the one containing the current data</b>. There would not be problem with choosing the same path, since the translated files have different file names, but please observe the script does not automatically remove the original data files, thus in the end you would have files from two different formats living at the same time in the same location; this would avoids Hive working well (see the <i>Hive tables</i> section). In addition, if a rollback is necessary (due to a VM reboot, network fail, etc), you would have a mesh of CSV and Json data files together.
3. Check if you have enough disk space in your user account within the Head Node of Cosmos. The HDFS files are temporarilly downloaded to `/tmp`, where they are translated before being uploaded to HDFS. The reason is, due to the <i>big</i> nature of the data, the translation is not recommended to be done in memory. You can check the ammount of disk space required by performing the following command:<br>
`S hadoop fs -dus /user/myuser/mydata/`  

## Apply the data translation

1. Log into your Cosmos account within the Head Node.
2. Get an executable copy of the translation script:<br>
`$ wget --no-check-certificate https://raw.githubusercontent.com/telefonicaid/fiware-cygnus/release/iotplatform-v1/resources/cygnus-translators/cygnus-translator-0.1-to-0.3.sh`
`$ chmod +x cygnus-translator-0.1-to-0.3.sh`
3. Run the script by giving your HDFS username, a custom prefix to be added to the output files (optional), the input HDFS path and the output HDFS path:<br>
`$ ./cygnus-translator-0.1-to-0.3 myuser "" /user/myuser/mydata/ /user/myuser/mydata2/`
4. You can check the new translated files have been created by listing the destination folder:<br>
`$ hadoop fs -ls /user/myser/mydata2/`
<br>

The output of the script should be something like:

    $ ./cygnus-translator-0.1-to-0.3.sh myuser "" /user/myuser/mydata/ /user/myuser/mydata2/
    hdfs://localhost/user/myuser/mydata2/ exists but is empty
    Creating /tmp/cygnus.4Hud as working directory
    Reading hdfs://localhost/user/myuser/mydata/OUTSMART.NODE_3500-Node-Latitud-urn_x_ogc_def_phenomenon_IDAS_1.0_latitude.txt (130 bytes) [DONE]
    Translating into /tmp/cygnus.4Hud/output.XRav [DONE]
    Writing hdfs://localhost/user/myuser/mydata2/OUTSMART.NODE_3500-Node.0.tmp (234 bytes) [DONE]
    ...

As you can see, first of all the destinarion HDFS folder is checked; it may not be created (then it is created), it may exist but be empty (nothing is done), or it may exist having certain content (in that case the script exits with error). Then, a temporal working directory is created in `/tmp`. Finally, the source HDFS files are read, one by one, into temporal files which are translated before their are uploaded to the final HDFS destination. Both the temporal folder and files are deleted.

## Hive tables

Hive allows for querying data within HDFS using a SQL-like language. These queries are executed against logical tables pointing to the real data; the path of the data is a parameter of table creation command.

Since a data translation has been performed and the data location has changed, it is necessary to destroy the unique table pointing to the original HDFS path and create new ones (<b>one per each entity</b>). Step by step:

1. Log into your Cosmos account within the Head Node.
2. Invoke the Hive CLI:<br>
`$ hive`
3. Identify the table pointing to the all data. The following command shows all teh available Hive tables:<br>
`hive> show tables;`
4. Delete the table:<br>
`hive> drop table <old_table_name>`
5. Create a new table pointing to the new entity data location:<br>
`hive> create external <new_table_name>  (recvTimeTs string, recvTime string, entityId string, entityType string, attrName string, attrType string, attrValue string, attrMd string) row format serde '/usr/local/hive-0.9.0-shark-0.8.0-bin/lib/json-serde-1.1.9.3-SNAPSHOT.jar' location '/user/myuser/mydata2/';`
6. Check the new table works well by querying it:<br>
`hive> select * from <new_table_name>;`

Repeat the steps 5 and 6 for every entity in your data.

I you have a look to the Hive table creation command, you will see the format of the data (Json) is interpreted by Hive through a SerDe (serializer-deserializer).

Tip: The new table name is advisable to be composed as:

    <new_table_name>=<cosmos_user>_<path>_<to>_<the>_<data>_<entity_descriptor>
    <entity_despriptor>=<entityId>_<entityType>

## Cygnus configuration tips

In terms of configuration, there is almost no changes between Cygnus 0.1 and 0.3 (or higher). Nevertheless, there are a couple of things you should know for a complete compatibility among versions:

* From Cygnus 0.3 there is no `cygnusagent.sinks.hdfs-sink.cosmos_dataset` in `OrionHDFSSink`. Now, there is a default organization parameter (`cygnusagent.sources.http-source.handler.default_organization`) which is used to build the HDFS path if no other organization information is given by Orion in the notifications. Unless you are able to configure Orion to behave as described, you will have to define the default destination through this parameter. Thus, if you had a `cygnusagent.sinks.hdfs-sink.cosmos_dataset = path/to/my/folder` you can now have `cygnusagent.sources.http-source.handler.default_organization = path/to/my/folder`.
* Remember Cygnus 0.1 worked in the currently called "row-like" mode, thus configure Cygnus 0.3 properly: `cygnusagent.sinks.hdfs-sink.attr_persistence = row`

## Rollbacking (if something goes wrong)

Rollbacking is as simple as deleting the content that may be generated under the destination HDFS folder and continue using Cygnus 0.1 with the original HDFS content. 

## Contact information

Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com)
