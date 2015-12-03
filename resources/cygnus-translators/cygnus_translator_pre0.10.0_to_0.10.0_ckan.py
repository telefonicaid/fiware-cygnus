#!/usr/bin/env python
# -*- coding: latin-1 -*-

# Author: frb

# Imports
import sys
import requests
import json
import pprint

# Show the usage
if len(sys.argv) < 7:
   print('Usage: cygnus_translator_pre0.10.0_to_0.10.0_hdfs.sh ckan_host ckan_port api_key org_name attr_persistence null_value backup')
   print('where ckan_host')
   print('      ckan_port')
   print('      api_key')
   print('      org_name')
   print('      attr_persistence')
   print('      null_value : string to be inserted as null value, use the keyword \"empty\" for an empty value')
   print('      backup     : either true or false')
   sys.exit(0)

# Input parameters
ckan_host=sys.argv[1]
ckan_port=sys.argv[2]
api_key=sys.argv[3]
org_name=sys.argv[4]
attr_persistence=sys.argv[5]
null_value=sys.argv[6]
backup=sys.argv[7]

# Check for the empty value
if null_value == 'empty':
   null_value=''

# Process an organization, given its name
def process_org(org_name):
   print('Processing organization ' + org_name)
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/organization_show'
   headers = {'Authorization': api_key}
   payload = {'id':org_name,'include_datasets':'true'}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()

   for pkg in dictionary['result']['packages']:
      process_pkg(pkg['id'])

# Process a package, given its id
def process_pkg(pkg_id):
   print(' |_Processing package ' + pkg_id)
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/package_show'
   headers = {'Authorization': api_key}
   payload = {'id':pkg_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()

   for res in dictionary['result']['resources']:
      process_res(res['id'], res['name'], pkg_id)

# Process a resource, given its id
def process_res(res_id, res_name, pkg_id):
   print('    |_Processing resource ' + res_id)
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/datastore_search'
   headers = {'Authorization': api_key}
   payload = {'id':res_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()
   fields = dictionary['result']['fields']

   for i in range(0, len(fields)):
      if fields[i]['id'] == '_id':
         fields.pop(i)
         break

   records = dictionary['result']['records']

   for i in range(0, len(records)):
      del records[i]['_id']

   do_backup(fields, records, res_name, pkg_id)
   add_new_fields(fields, res_id)

# Do backup of a resource given its fields, its records and its id
def do_backup(fields, records, res_name, pkg_id):
   print('       |_Backing the resource')

   # Common headers
   headers = {'Authorization': api_key}

   # Create the backup resource
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/resource_create'
   payload = {'name':res_name + '_bak','url':'none','format':'','package_id':pkg_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()
   res_id = dictionary['result']['id']

   # Create the datastore for the backup resource
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/datastore_create'
   payload = {'fields':fields,'force':'true','resource_id':res_id}
   req = requests.post(url, headers=headers, json=payload)

   # Create the backup resource view
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/resource_view_create'
   payload = {'view_type':'recline_grid_view','title':'Backup','resource_id':res_id}
   req = requests.post(url, headers=headers, json=payload)

   # Upsert the records in the backup resource
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/datastore_upsert'
   payload = {'records':records,'force':'true','method':'insert','resource_id':res_id}
   req = requests.post(url, headers=headers, json=payload)

# Add new fields to a resource, given its current fields and its id
def add_new_fields(fields, res_id):
   print('       |_Adding new fields')
   fields.append({'id':'fiware-servicepath','type':'text'})
   
   if attr_persistence == 'column':
      fields.append({'id':'entityId','type':'text'})
      fields.append({'id':'entityType','type':'text'})

   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/datastore_create'
   headers = {'Authorization': api_key}
   payload = {'resource_id':res_id,'force':'true','fields':fields}
   req = requests.post(url, headers=headers, json=payload)
   print(req.status_code)
   print(req.text)

# Main function
def main():
   process_org(org_name)

if __name__ == "__main__":
    main()
