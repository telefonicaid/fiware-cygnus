/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import com.telefonica.iot.cygnus.log.CygnusLogger;

import es.santander.smartcity.ArcgisRestUtils.ArcgisFeatureTable;

/**
 * 
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public class NGSIArcgisFeatureTable extends ArcgisFeatureTable {

    private static CygnusLogger cygnusLogger;

    /**
     * 
     * @param featureServiceUrl
     * @param username
     * @param password
     * @param getTokenUrl
     * @param b
     */
    public NGSIArcgisFeatureTable(String featureServiceUrl, String username, String password, String getTokenUrl) {
		super(featureServiceUrl, username, password, getTokenUrl, false);
	}

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.utils.Arcgis#logBasic(java.lang.String)
     */
    @Override
    public void logBasic(String message) {
        super.logBasic(message);
        cygnusLogger.info(this.getClass().getName() + ": " + message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.utils.Arcgis#logDebug(java.lang.String)
     */
    @Override
    public void logDebug(String message) {
        super.logDebug(message);
        cygnusLogger.debug(this.getClass().getName() + ": " + message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.utils.Arcgis#logError(java.lang.String)
     */
    @Override
    public void logError(String message) {
        super.logError(message);
        if (this != null && this.getClass() != null && this.getClass().getName() != null) {
            cygnusLogger.error(this.getClass().getName() + ": " + message);
        }
        cygnusLogger.error("Error : " + message);
    }

}
