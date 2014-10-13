# -*- coding: utf-8 -*-
#
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-connectors (FI-WARE project).
#
# fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# francisco.romerobueno@telefonica.com
#
#     Author: Ivan Arias
#

from lettuce import world
from selenium import webdriver

def connect (url):
    """
    open a Firefox instance
    :param url: url
    :return: if is connect or not ( boolean)
    """
    world.browser = webdriver.Firefox()
    world.browser.implicitly_wait(30)
    world.verificationErrors = []
    world.accept_next_alert = True
    world.browser.get(url)
    if world.browser.title == "Network Error":
        return False
    else:
        return True

def disconnect ():
    """
    Disconnect the Browser instance
    """
    world.browser.quit()


