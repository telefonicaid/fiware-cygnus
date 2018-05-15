/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */
package com.telefonica.iot.cygnus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * 
 */

/**
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public class PropertyUtils {

    private static final CygnusLogger LOGGER = new CygnusLogger(PropertyUtils.class);
    private String file;
    private InputStream input = null;
    private Properties prop = null;

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file
     */
    public PropertyUtils(String file) {
        super();
        this.file = file;
        try {
            prop = new Properties();
            input = PropertyUtils.class.getClassLoader().getResourceAsStream("login.properties");

            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            // ex.printStackTrace();
            LOGGER.error("Error open the file. Details=" + ex.getLocalizedMessage());
        }

    }

    /**
     * @param key
     * @return
     */
    public String getProperty(String key) {
        LOGGER.debug("Innit getProperty(key --> " + key + ", file --> " + file + ")");
        String value = null;

        // get the property value
        value = prop.getProperty(key);
        return value;

    }
}
