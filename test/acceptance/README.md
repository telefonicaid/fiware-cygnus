# CYGNUS Acceptance Tests

Folder for acceptance tests of Cygnus.

## How to Run the Acceptance Tests

### Prerequisites:

- Python 2.6 or newer
- pip installed (http://docs.python-guide.org/en/latest/starting/install/linux/)
- virtualenv installed (pip install virtualenv) (optional).
Note: We recommend the use of virtualenv, because is an isolated working copy of Python which allows you to work on a specific project without worry of affecting other projects.

##### Environment preparation:

- If you are going to use a virtual environment (optional):
  * Create a virtual environment somewhere, e.g. in ~/venv (virtualenv ~/venv) (optional)
  * Activate the virtual environment (source ~/venv/bin/activate) (optional)
- Both if you are using a virtual environment or not:
  * Change to the test/acceptance folder of the project.
  * Install the requirements for the acceptance tests in the virtual environment
     ```
     pip install -r requirements.txt --allow-all-external
     ```
  * Verify if  xmltodict and requests libraries are installed, if not:
     ```
     pip install xmltodict requests
     ```

##### Requirements to mysql
    ```
      yum install MySQL-python mysql-devel
    ```

#### Requirements to fabric
    ```
     yum install gcc python-devel
    ```

### Tests execution:

- Change to the test/acceptance folder of the project if not already on it.
- `properties.json` will be create automatically from setting folder (see configuration.json), with configurations files previously created
- Run lettuce_tools (see available params with the -h option).

```
Some examples:
   lettuce_tools                                   -- run all features
   lettuce_tools -ft ckan_row.feature              -- run only one feature
   lettuce_tools -tg test -ft ckan_row.feature     -- run scenarios tagged with "test" in a feature
   lettuce_tools -tg=-skip -ft ckan_row.feature    -- run all scenarios except tagged with "skip" in a feature
```

### Tests Suites Coverage (Features):

- Cygnus-CKAN per row using HTTP.
- Cygnus-CKAN per row using HTTPS.
- Cygnus-CKAN per row using multi-instances.
- Cygnus-CKAN per row using grouping rules.
- Cygnus-CKAN per column using HTTP.
- Cygnus-CKAN per column using HTTPS.
- Cygnus-CKAN per column using multi-instances.
- Cygnus-CKAN per column using grouping rules.

- Cygnus-MYSQL per row.
- Cygnus-MYSQL per row using multi-instances.
- Cygnus-MYSQL per row using grouping rules.
- Cygnus-MYSQL per column.
- Cygnus-MYSQL per column using multi-instances.
- Cygnus-MYSQL per column using grouping rules.

- Cygnus-HADOOP per row.
- Cygnus-HADOOP per row using Kerberos.
- Cygnus-HADOOP per row using multi-instances.
- Cygnus-HADOOP per row using grouping rules.
- Cygnus-HADOOP per column (pending).
- Cygnus-HADOOP per column using Kerberos (pending).
- Cygnus-HADOOP per column using multi-instances (pending).
- Cygnus-HADOOP per column using grouping rules (pending).

- Cygnus-MONGO raw
- Cygnus-MONGO raw using grouping rules.
- Cygnus-MONGO raw using multi-instances.

- Cygnus-STH aggregated
- Cygnus-STH aggregated using grouping rules.
- Cygnus-STH aggregated using multi-instances.

### configuration.json

We recommend to create `settings` folder in acceptance folder if it does not exists and store all configurations to features referenced by `properties.json.base` files.
The settings folder path could be changed in the `configuration.json` file in `path_to_settings_folder` field
This file initially will overwrite properties.json in each feature.
   ```
   ex: ckan_column_http_properties.json
   ```
   * path_to_settings_folder: path where are stored all configurations referenced by properties.json.base
   * grouping_rules_name: name of grouping rules configuration
   * log_file: path and file where is log file

### grouping_rules.conf

We recommend to create your configurations json in a copy file into settings folders and it will be copied to cygnus in conf folder, remotely.
In cygnus will be called `grouping_rules.conf` by default (see configuration.json). If your tests do not use grouping rules implicitly,
probably with to copy template to grouping_rules.conf is enough and then use `"default"` as file name value in step.
`""` value (empty) in file name in step does not do anything and also removes the last grouping_rules.conf file if exists.
Note: in case of Malformed grouping rule, it will be discarded.

### checking log file

Verify if a label and its text exists in the last lines. The file log by default is `"/var/log/cygnus/cygnus.log`(see configuration.json)

### properties.json

- environment
    * name: name of product tested.
    * logs_path: folder name to logs.

- cygnus:
    * cygnus_url: cygnus protocol, host used to connect at cygnus (endpoint).
    * cygnus_port: port used to connect at cygnus .
    * cygnus_management_port: used to management operations (endpoint).
    * cygnus_version: cygnus version
    * cygnus_verify_version: determine whether the version is verified or not (True or False).
    * cygnus_log_level: logging levels for cygnus ( INFO | DEBUG )
    * cygnus_user_agent: server agent to notification header from context broker.
    * cygnus_organization_per_row_default: organization name per row mode used by "default" value.
    * cygnus_organization_per_col_default: organization name per column mode by "default" value.
    * cygnus_service_path_default: service path used by "default" value.
    * cygnus_identity_id_default: identity id used by "default" value.
    * cygnus_identity_type_default: identity type used by "default" value.
    * cygnus_attributes_number_default: number of attributes used by "default" value.
    * cygnus_attributes_name_default: attributes name used by "default" value.
    * cygnus_fabric_user: user used to connect by Fabric
    * cygnus_fabric_password: password used to connect by Fabric, if use password, cert_file must be None.
    * cygnus_fabric_cert_file: cert_file used to connect by Fabric, if use cert file, password must be None.
    * cygnus_fabric_error_retry: Number of times Fabric will attempt to connect when connecting to a new server
    * cygnus_fabric_source_path: source path where are templates files
    * cygnus_fabric_target_path: target path where are copied config files
    * cygnus_fabric_sudo:  with superuser privileges (True | False)

- ckan:
    * ckan_version: ckan version installed.
    * ckan_verify_version: determine whether the version is verified or not (True or False).
    * ckan_authorization: the CKAN API key to use, it is the same that used in cygnus.conf in "cygnusagent.sinks.ckan-sink.api_key".
    * ckan_host: the FQDN/IP address for the CKAN API endpoint
    * ckan_port: the port for the CKAN API endpoint
    * ckan_orion_url: Orion URL used to compose the resource URL with the convenience operation URL to query it
    * ckan_ssl: enable SSL for secure Http transportation; 'true' or 'false'
    * ckan_channel_capacity: capacity of the channel
    * ckan_channel_transaction_capacity: amount of bytes that can be sent per transaction
    * ckan_retries_dataset_search: number of retries for data verification.
    * ckan_delay_to_retry: time to delay in each retry.

- mysql:
    * mysql_version: mysql version installed.
    * mysql_verify_version: determine whether the version is verified or not (True or False).
    * mysql_host: IP address or host of mysql Server.
    * mysql_port: port where mysql is listening.
    * mysql_user: user valid in the mysql server.
    * mysql_pass: password to user above.
    * mysql_channel_capacity: capacity of the channel
    * mysql_channel_transaction_capacity: amount of bytes that can be sent per transaction
    * mysql_retries_table_search: number of retries for data verification.
    * mysql_delay_to_retry: time to delay in each retry.

-  hadoop:
    * hadoop_version: hadoop version installed.
    * hadoop_verify_version: determine whether the version is verified or not (True or False).
    * hadoop_namenode_url: namenode protocol, host and port used (endpoint).
    * hadoop_managenode_url: resource manager protocol, host and port used (endpoint).
    * hadoop_user: user valid hadoop system.
    * hadoop_password: default password for the default username
    * hadoop_api: HDFS backend type (webhdfs, httpfs or infinity)
    * hadoop_krb5_auth: Kerberos-based authentication enabling
    * hadoop_krb5_user: Kerberos username
    * hadoop_krb5_password: Kerberos password
    * hadoop_krb5_default_realm: Identifies the default Kerberos realm for the client. Set its value to your Kerberos realm.
    * hadoop_krb5_kdc: The name or address of a host running a KDC for that realm. An optional port number, separated from the hostname by a colon, may be included
    * hadoop_krb5_admin_server: Identifies the host where the administration server is running. Typically, this is the master Kerberos server
    * hadoop_krb5_dns_lookup_realm: Looks for DNS records for fallback host-to-realm mappings and the default realm
    * hadoop_krb5_dns_lookup_kdc: Indicate whether DNS SRV records should be used to locate the KDCs and other servers for a realm
    * hadoop_krb5_ticket_lifetime: Sets the default lifetime for initial ticket requests. The default value is 1 day.
    * hadoop_krb5_renew_lifetime: Sets the default renewable lifetime for initial ticket requests. The default value is 0.
    * hadoop_krb5_forwardable: If this flag is true, initial tickets will be forwardable by default, if allowed by the KDC. The default value is false.
    * hadoop_channel_capacity: capacity of the channel
    * hadoop_channel_transaction_capacity: amount of bytes that can be sent per transaction
    * hadoop_retries_open_file: number of retries for data verification.
    * hadoop_delay_to_retry: time to delay in each retry.

 - mongo:
    * mongo_version: mongo version installed.
    * mongo_verify_version: determine whether the version is verified or not (True or False).
    * mongo_host: IP address or host of mongo Server.
    * mongo_port: port where mongo is listening.
    * mongo_user: user used in mongo server.
    * mongo_password: password to user above.
    * mongo_database": mongo database by default
    * mongo_channel_capacity: capacity of the channel
    * mongo_channel_transaction_capacity: amount of bytes that can be sent per transaction
    * mongo_retries_search: number of retries for data verification.
    * mongo_delay_to_retry: time to delay in each retry.

 - sth:
    * sth_version: mongo version installed.
    * sth_verify_version: determine whether the version is verified or not (True or False).
    * sth_host: IP address or host of mongo Server.
    * sth_port: port where mongo is listening.
    * sth_user: user used in mongo server.
    * sth_password: password to user above.
    * sth_database": mongo database by default
    * sth_channel_capacity: capacity of the channel
    * sth_channel_transaction_capacity: amount of bytes that can be sent per transaction
    * sth_retries_search: number of retries for data verification.
    * sth_delay_to_retry: time to delay in each retry.

### tags

You can to use multiples tags in each scenario, possibles tags used:

    - happy_path, skip, errors_40x, only_develop, ISSUE_XXX, BUG_XXX, multi_instances, grouping_rules, etc

and to filter scenarios by these tags: see Tests execution section.



