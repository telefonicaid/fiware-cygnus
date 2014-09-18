# -*- coding: utf-8 -*-
'''
(c) Copyright 2013 Telefonica, I+D. Printed in Spain (Europe). All Rights
Reserved.

The copyright to the software program(s) is property of Telefonica I+D.
The program(s) may be used and or copied only with the express written
consent of Telefonica I+D or in accordance with the terms and conditions
stipulated in the agreement/contract under which the program(s) have
been supplied.
'''

from lettuce import world
import json
import os
import sys

"""
Parse the JSON configuration file located in the src folder and
store the resulting dictionary in the lettuce world global variable.
"""
with open("properties.json") as config_file:
    try:
        world.config = json.load(config_file)
    except Exception, e:
        print 'Error parsing config file: %s' % (e)
        sys.exit(1)

"""
Make sure the logs path exists and create it otherwise.
"""
if not os.path.exists(world.config["environment"]["logs_path"]):
    os.makedirs(world.config["environment"]["logs_path"])
