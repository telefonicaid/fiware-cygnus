# MySQL scripts

## `mysql-cleaner.sh`
This script aims to delete old entries within Cygnus related MySQL tables. In the context information persistence world, MySQL is conceived as a short-term historic storage, and thus cannot contain all the context information forever.

The way the data is deleted is by comparing the current date with the stored `recvTime` in each row. If such difference is greater than a specified number of days, the row is deleted.

The script iterates on all the databases (since each database belogs to a tenant/service) and all the tables within databases (since each table belongs to a NGSI entity). This is based on the assumption all the databases and tables are Cygnus related; be careful if you host any non Cygnus database on the same MySQL server: do not use a `recvTime` column or your data risks to be deleted!

### Usage
The script has four parameters:

* Host where the MySQL server is running. This parameter allows the remote deletion of context information.
* MySQL user allowed to delete the data.
* Password for the above user.
* Number of days the context information is stored.

Use this command to manually run the script:

    $ ./mysql-cleaner.sh <MySQL_host> <MySQL_user> <MySQL_password> <data_lifetime_days>

### Cron scheduling
More interesting than running the script by hand is to schedule regular executions. This is achieved by using Cron.

First of all, install (simply copy) the `mysql-cleaner.sh` script somewhere only a privileged user has access, e.g. `/usr/share/cygnus/` (must be created if not yet existing). Do not forget to give the privileged user execution permissions:

    $ sudo mkdir /usr/share/cygnus/
    $ sudo cp mysql-clener.sh /usr/share/cygnus 
    $ sudo chmod u+x /usr/share/cygnus/mysql-cleaner.sh

In order to program the scheduling, create a `cygnus_crontab.txt` file. Put the following line within that file, if wanting to execute the script every hour:

    0 */1 * * * /usr/share/cygnus/mysql-cleaner.sh

Now, invoke the crontab to set your cron job:

    $ sudo crontab cygnus_crontab.txt

You can check it has been successfully set by typing:

    $ sudo crontab -l
    0 */1 * * * /usr/share/cygnus/mysql-cleaner.sh <MySQL_host> <MySQL_user> <MySQL_password> <data_lifetime_days>      

# Contact
Francisco Romero Bueno (frb at tid dot es)
