# CKAN management scripts

## `ckan_datastore_cleaner.sh`
This script aims to delete old entries within CKAN datastores, interacting directly with the underlying Postgresql database that CKAN uses internally. Note that CKAN is considered a short-term historic storage, and thus cannot contain all the context information forever.

The way the data is deleted is by comparing the current date with the stored `recvTime` in each row. If such difference is greater than a specified number of days, the row is deleted.

The script iterates on all the tables on the datastore_default database (the one that holds all the datastore, each table name after the resource to which the datastore is associated). This is based on the assumption all the datastores are Cygnus related; be careful if you host any non Cygnus database on the same CKAN server: do not use a `recvTime` column or your data risks to be deleted!

### Usage
The script has 1 parameter:

* Number of days the context information is stored.

Use this command to manually run the script:
    $ ./ckan-datastore-cleaner.sh <data_lifetime_days>

The script assumes that it runs in the same machine than the Postgresql server by an user with sudo privileges.

### Cron scheduling
More interesting than running the script by hand is to schedule regular executions. This is achieved by using Cron.

First of all, install (simply copy) the `ckan-datastore-cleaner.sh` script somewhere only a privileged user has access, e.g. `/usr/share/cygnus/` (must be created if not yet existing). Do not forget to give the privileged user execution permissions:

    $ sudo mkdir /usr/share/cygnus/
    $ sudo cp ckan-datastore-cleaner.sh /usr/share/cygnus
    $ sudo chmod u+x /usr/share/cygnus/ckan-datastore-cleaner.sh

In order to program the scheduling, create a `cygnus_crontab.txt` file. Put the following line within that file, if wanting to execute the script every hour with an expiration interval of 1 day:

    0 */1 * * * /usr/share/cygnus/ckan-datastore-cleaner.sh 1

Now, invoke the crontab to set your cron job:

    $ sudo crontab cygnus_crontab.txt

You can check it has been successfully set by typing:

    $ sudo crontab -l
    0 */1 * * * /usr/share/cygnus/ckan-datastore-cleaner.sh 1

## `create-organization.sh`

This script creates an organization, which name is passed as argument.

The script has three parameters:

* The host/IP where the CKAN API is running
* The CKAN API key
* The organization name

## `create-package.sh`

This script creates a package/dataset within a given organization. It has four parameters:

* The host/IP where the CKAN API is running
* The CKAN API key
* The organization name in which the package/dataset will be created
* The package/dataset name

Pay attention to the "id" field in the response, you may need it to use create-resource.sh.

## `create-resource.sh`

This script creates a resource within a given package/dataset. It has four parameters:

* The host/IP where the CKAN API is running
* The CKAN API key
* The package/dataset ID in which the resource will be created
* The resource name

Pay attention to the "id" field in the response, you may need it to use create-datastore.sh.

## `create-datastore.sh`
This scripts shows an example for datastore creation, useful when Cygnus runs CKAN sink in "column" mode. I would need adaptation in your particular environment, as the column name will be different for sure in your case.

The script has three parameters:

* The host/IP where the CKAN API is running
* The CKAN API key
* The resource ID which datastore is going to be created

# Contact
Fermin Galan Marquez (fermin dot galanmarquez at telefonica dot com)
