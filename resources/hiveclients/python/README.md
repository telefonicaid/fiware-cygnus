#Python-based clients for HiveServer1 and HiveServer2

##HiveServer1 client
This sample code works with [`python_hive_utils`](https://github.com/eventbrite/python_hive_utils). The sample code given by the [Hive official](https://cwiki.apache.org/confluence/display/Hive/HiveClient#HiveClient-Python page) only works on a standalone server where is possible to access the built Python libraries for Hive.

Thus, start by installing `python_hive_utils`:

    $ (sudo) pip install hive_utils
    
In order to run the client, just execute the `hiveserver1-client` script:

    $ python hiveserver1-client <hive_host> <hive_port> <db_name> <hadoop_user> <hadoop_password>

##HiveServer2 client
This sample code works with [`pyhs2`](https://github.com/BradRuderman/pyhs2), thus start by installing it:

    $ (sudo) pip install pyhs2
    
In order to run the client, just execute the `hiveserver2-client` script:

    $ python hiveserver2-client <hive_host> <hive_port> <db_name> <hadoop_user> <hadoop_password>

##Contact

Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com)
