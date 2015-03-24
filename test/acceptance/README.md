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
- Rename properties.json.base to properties.json and replace values.
- Run lettuce_tools (see available params with the -h option).

```
Some examples:
   lettuce_tools                                   -- run all features
   lettuce_tools -ft ckan_row.feature              -- run only one feature
   lettuce_tools -tg test -ft ckan_row.feature     -- run scenarios tagged with "test" in a feature
   lettuce_tools -tg=-skip -ft ckan_row.feature    -- run all scenarios except tagged with "skip" in a feature
```

### Tests Coverage:

- Cygnus-CKAN per row.
- Cygnus-MYSQL per row.
- Cygnus-HADOOP per row.
- Cygnus-MYSQL per column.
- Cygnus-CKAN per column.
- Cygnus-HADOOP per column (pending).

### configuration.json
We recommend to create `settings` folder in acceptance folder if it does not exists and store all configurations to features referenced by `properties.json.base` files.
The settings folder path could be changed in the `configuration.json` file in `path_to_settings_folder` field
This file initially will overwrite properties.json in each feature.
   ```
   ex: ckan_column_http_properties.json
   ```

   * path_to_settings_folder: path where are stored all configurations referenced by properties.json.base

### properties.json


- environment
    * name: name of product tested.
    * logs_path: folder name to logs.

- cygnus:
    * cygnus_url: cygnus protocol, host and port used to connect cygnus (endpoint).
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
    * hadoop_channel_capacity: capacity of the channel
    * hadoop_channel_transaction_capacity: amount of bytes that can be sent per transaction
    * hadoop_retries_open_file: number of retries for data verification.
    * hadoop_delay_to_retry: time to delay in each retry.

### tags

You can to use multiples tags in each scenario, possibles tags used:

    - happy_path, skip, errors_40x, only_develop, etc

and to filter scenarios by these tags: see Tests execution section.



