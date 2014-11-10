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
  * Verify if  xmltodict and httplib2 requests libraries are installed, if not are installed:
     ```
     pip install xmltodict httplib2 requests
     ```

##### Requeriments for mysql

-  yum install MySQL-python
-  yum install mysql-devel
-  pip install mysql-python

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

### properties.json
- environment
    * name: name of product tested.
    * logs_path: folder name to logs.

- cygnus:
    * cygnus_url: cygnus protocol, host and port used to connect cygnus (edpoint).
    * cygnus_user_agent: server agent to notification header from context broker.
    * cygnus_organization_per_row_default: organization name per row mode used by "default" value.
    * cygnus_organization_per_col_default: organization name per column mode by "default" value.
    * cygnus_resource_default: resource name used by "default" value.
    * cygnus_attributesNumber_default: number of attributes used by "default" value.
    * cygnus_metadatasNumber_default: number of attributes metadatas used by "default" value.
    * cygnus_compoundNumber_default: number of attributes compounds used by "default" value.
    
- ckan:
    * ckan_version: ckan version installed.
    * ckan_url:  ckan protocol, host and port used to connect ckan server (edpoint).
    * ckan_authorization: the CKAN API key to use, it is the same that used in cygnus.conf in "cygnusagent.sinks.ckan-sink.api_key".
    * ckan_dataset_default: dataset name by default, it is the same that used in cygnus.conf in "cygnusagent.sinks.ckan-sink.default_dataset".

- mysql:
    * mysql_version: mysql version installed.
    * mysql_host: IP address or host of mysql Server.
    * mysql_port: port where mysql is listening.
    * mysql_user: user valid in the mysql server.
    * mysql_pass: password to user above.
    * mysql_attrValueType_default:  type of data by default for attributes.
    * mysql_metadataType_default: type of data by default for attributes metadatas.

-  hadoop:
    * hadoop_version: hadoop version installed.
    * hadoop_verify_version: determine whether the version is verified or not (True or False).
    * hadoop_namenode_url: namenode protocol, host and port used (edpoint).
    * hadoop_conputenode_url: resource manager protocol, host and port used (edpoint).
    * hadoop_user: user valid hadoop system.
    * hadoop_retries_open_file: number of retries for data verification.
    * hadoop_delay_to_retry: time to delay in each retry.

### tags

You can to use multiples tags in each scenario, possibles tags used:

    - happy_path, skip, errors_40x, etc

and to filter scenarios by these tags: see Tests execution section.



