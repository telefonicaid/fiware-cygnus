# CYGNUS Acceptance Tests

Folder for acceptance tests of Cygnus.

## How to Run the Acceptance Tests

### Prerequisites:

- Python 2.6 or newer
- pip installed (http://docs.python-guide.org/en/latest/starting/install/linux/)
- virtualenv installed (pip install virtualenv) (optional).
Note: We recommend the use of virtualenv, because is an isolated working copy of Python which allows you to work on a specific project without worry of affecting other projects.

### Environment preparation:

- Create a virtual environment somewhere, e.g. in ~/venv (virtualenv ~/venv) (optional)
- Activate the virtual environment (source ~/venv/bin/activate) (optional)
- Change to the test/acceptance folder of the project
- Install the requirements for the acceptance tests in the virtual environment (pip install -r requirements.txt --allow-all-external).
- Verify if  xmltodict and httplib2 libraries are installed, if not are installed:
     pip install xmltodict httplib2

## Requeriments for mysql

-  yum install MySQL-python
-  yum install mysql-devel
-  pip install mysql-python

### Tests execution:

- Change to the test/acceptance folder of the project if not already on it
- Rename properties.json.base to properties.json and replace values
- Run lettuce_tools (see available params with the -h option)

```
Some examples:
   lettuce_tools                                   -- run all features
   lettuce_tools -ft ckan_row.feature              -- run only one feature
   lettuce_tools -tg test -ft ckan_row.feature     -- run scenarios tagged with "test" in a feature
   lettuce_tools -tg=-skip -ft ckan_row.feature    -- run all scenarios except tagged with "skip" in a feature
```

### Tests Coverage:

- Cygnus-CKAN per row
- Cygnus-MYSQL per row
- Cygnus-HADOOP per row
- Cygnus-MYSQL per column
- Cygnus-CKAN per column
- Cygnus-HADOOP per column (pending)





