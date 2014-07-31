## `create-organization.sh`

This script creates an organization, which name is passed as argument.

The script has three parameters:

* The host/IP where teh CKAN API is running
* The CKAN API key
* The organization name

## `create-package.sh`

This script creates a package/dataset within a given organization.

* The host/IP where the CKAN API is running
* The CKAN API key
* The organization name in which the package/dataset will be created
* The package/dataset name

Pay attention to the "id" field in the response, you may need it to use create-resource.sh.

## `create-resource.sh`

This script creates a resource within a given package/dataset.

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
