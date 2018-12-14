#!/usr/bin/env python
# -*- coding: latin-1 -*-
#
# Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FIWARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
# General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
# option) any later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
#

# Author: frb

# Imports
import sys
import requests
import json
import pprint

# Show the usage
# FIXME: the getopt library should be used instead
if len(sys.argv) < 8:
   print('Usage: cygnus_translator_pre0.10.0_to_0.10.0_hdfs.sh ckan_host ckan_port ssl api_key org_name attr_persistence null_value backup')
   print('where ckan_host       : IP address or FQDN of the host running the CKAN server')
   print('      ckan_port       : port where the above CKAN server is listening')
   print('      ssl             : either true or false')
   print('      api_key         : API key for a user allowed to update the given organization')
   print('      org_name        : organization name to be translated')
   print('      attr_persistence: either row or column')
   print('      backup          : either true or false')
   sys.exit(0)

# Input parameters
ckan_host=sys.argv[1]
ckan_port=sys.argv[2]

if sys.argv[3] == 'true':
   ssl='s'
else:
   ssl=''

api_key=sys.argv[4]
org_name=sys.argv[5]
attr_persistence=sys.argv[6]
backup=sys.argv[7]

# Process an organization, given its name
def process_org(org_name):
   print('Processing organization ' + org_name)
   url = 'http%s://%s:%s/api/3/action/organization_show' % (ssl, ckan_host, ckan_port)
   headers = {'Authorization': api_key}
   payload = {'id':org_name,'include_datasets':'true'}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()

   for pkg in dictionary['result']['packages']:
      process_pkg(pkg['id'])

# Process a package, given its id
def process_pkg(pkg_id):
   print(' |_Processing package ' + pkg_id)
   url = 'http%s://%s:%s/api/3/action/package_show' % (ssl, ckan_host, ckan_port)
   headers = {'Authorization': api_key}
   payload = {'id':pkg_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()

   for res in dictionary['result']['resources']:
      process_res(res['id'], res['name'], pkg_id)

# Process a resource, given its id
def process_res(res_id, res_name, pkg_id):
   print('    |_Processing resource ' + res_id)

   # Get the page 0
   url = 'http%s://%s:%s/api/3/action/datastore_search' % (ssl, ckan_host, ckan_port)
   headers = {'Authorization': api_key}
   payload = {'id':res_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()
   fields = dictionary['result']['fields']

   for i in range(0, len(fields)):
      if fields[i]['id'] == '_id':
         fields.pop(i)
         break

   if backup == 'true':
      # Get the page 0 records
      records = dictionary['result']['records']

      for i in range(0, len(records)):
         del records[i]['_id']

      # Get next pages records
      page = 1
      
      while True:
         url = 'http%s://%s:%s/api/3/action/datastore_search' % (ssl, ckan_host, ckan_port)
         headers = {'Authorization': api_key}
         payload = {'id':res_id, 'offset':page * 100}
         req = requests.post(url, headers=headers, json=payload)
         dictionary = req.json()
         page_records = dictionary['result']['records']

         for i in range(0, len(page_records)):
            del page_records[i]['_id']

         if (len(page_records) > 0):
            records += page_records
            page += 1
         else:
            break

      do_backup(fields, records, res_name, pkg_id)

   add_new_fields(fields, res_id)

# Do backup of a resource given its fields, its records and its id
def do_backup(fields, records, res_name, pkg_id):
   print('       |_Backing the resource')

   # Common headers
   headers = {'Authorization': api_key}

   # Create the backup resource
   url = 'http%s://%s:%s/api/3/action/resource_create' % (ssl, ckan_host, ckan_port)
   payload = {'name':res_name + '_bak','url':'none','format':'','package_id':pkg_id}
   req = requests.post(url, headers=headers, json=payload)
   dictionary = req.json()
   res_id = dictionary['result']['id']

   # Create the datastore for the backup resource
   url = 'http%s://%s:%s/api/3/action/datastore_create' % (ssl, ckan_host, ckan_port)
   payload = {'fields':fields,'force':'true','resource_id':res_id}
   req = requests.post(url, headers=headers, json=payload)

   # Create the backup resource view
   url = 'http%s://%s:%s/api/3/action/resource_view_create' % (ssl, ckan_host, ckan_port)
   payload = {'view_type':'recline_grid_view','title':'Backup','resource_id':res_id}
   req = requests.post(url, headers=headers, json=payload)

   # Upsert the records in the backup resource
   url = 'http%s://%s:%s/api/3/action/datastore_upsert' % (ssl, ckan_host, ckan_port)
   payload = {'records':records,'force':'true','method':'insert','resource_id':res_id}
   req = requests.post(url, headers=headers, json=payload)

# Add new fields to a resource, given its current fields and its id
def add_new_fields(fields, res_id):
   print('       |_Adding new fields')
   fields.append({'id':'fiwareServicePath','type':'text'})
   
   if attr_persistence == 'column':
      fields.append({'id':'entityId','type':'text'})
      fields.append({'id':'entityType','type':'text'})

   url = 'http%s://%s:%s/api/3/action/datastore_create' % (ssl, ckan_host, ckan_port)
   headers = {'Authorization': api_key}
   payload = {'resource_id':res_id,'force':'true','fields':fields}
   req = requests.post(url, headers=headers, json=payload)

# Main function
def main():
   process_org(org_name)

if __name__ == "__main__":
    main()
