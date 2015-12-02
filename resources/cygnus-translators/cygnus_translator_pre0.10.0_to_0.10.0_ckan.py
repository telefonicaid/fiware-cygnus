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

def process_org(org_name):
   print('Processing organization ' + org_name)
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/organization_show'
   headers = {'Authorization': api_key}
   payload = {'id':org_name,'include_datasets':'true'}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()

   for pkg in dictionary['result']['packages']:
      process_pkg(pkg['id'])

def process_pkg(pkg_id):
   print(' |_Processing package ' + pkg_id)
   url = 'http://' + ckan_host + ':' + ckan_port + '/api/3/action/package_show'
   headers = {'Authorization': api_key}
   payload = {'id':pkg_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()

   for res in dictionary['result']['resources']:
      process_res(res['id'])

def process_res(res_id):
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

def main():
   process_org(org_name)

if __name__ == "__main__":
    main()
