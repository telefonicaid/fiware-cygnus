# Hive Basic Client

## Installation and run
Build the Hive basic client with Maven:

    $ cd resources/hive-basic-client
    $ mvn package

You can run the built application by using Maven too:

    $ mvn exec:java -Dexec.args="<hive-server-ip> <hive-port> <hadoop-user> <hadoop-password>"

Once it is running, you will see a promt asking you for HiveQL sentences to be executed:

    remoteclient>

You can write any set of HiveQL senteces separated by ';':

    remoteclient> select * from <table>;
    [data_being_printed]

## Contact

Francisco Romero Bueno (frb at tid dot es).
