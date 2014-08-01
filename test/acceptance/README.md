# CYGNUS Acceptance Tests

Folder for acceptance tests of Cygnus.

## How to Run the Acceptance Tests

### Prerequisites:

- Python 2.6 or newer

- pip installed (http://docs.python-guide.org/en/latest/starting/install/linux/)

- virtualenv installed (pip install virtualenv)

### Environment preparation:

- Create a virtual environment somewhere, e.g. in ~/venv (virtualenv ~/venv)

- Activate the virtual environment (source ~/venv/bin/activate)

- Make sure pdihub.hi.inet domain is reachable from your system (some of the requirements come from it) and instruct Git not to validate SSL connections to it (export GIT\_SSL\_NO\_VERIFY=true).

- Change to the test/acceptance folder of the project

- Install the requirements for the acceptance tests in the virtual environment (pip install -r requirements.txt --allow-all-external).

## Requeriments for selenium

- pip install -U selenium (before, activate the virtualent)
- download selenium-server-standalone from http://docs.seleniumhq.org/download/
- java -jar selenium-server-standalone-x.y.z.jar (required Java and firefox installed)

## Requeriments for mysql

- pip install python-mysqldb

### Tests execution:

- Change to the test/acceptance folder of the project if not already on it

- Rename properties.json.base to properties.json and replace values

- Run lettucetdaf (see available params with the -h option)

### Tests Coverage:

- Cygnus-CKAN per row
- Cygnus-MYSQL per row
- Cygnus-COSMOS per row (pending)
- Cygnus-MYSQL per column (pending)
- Cygnus-CKAN per column (pending)
- Cygnus-COSMOS per column(pending)





