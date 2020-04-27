/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import java.util.Date;

import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.sql.SQLBackendImpl;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * 
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public class NGSIArcgisFeatureTable extends ArcgisFeatureTable {
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIArcgisFeatureTable.class);
    
    private static final String CLASS_NAME = "NGSIArcgisFeatureTable";
    private long timeoutSecs = 60;
    private Date lastPersist = new Date();

    /**
     * 
     * @param featureServiceUrl
     * @param username
     * @param password
     * @param getTokenUrl
     * @param b
     */
    public NGSIArcgisFeatureTable(String featureServiceUrl, String username, String password,
            String getTokenUrl, long timeoutSecs) {
        super();
        this.timeoutSecs = timeoutSecs;
    }

    /**
     * Timed out?.
     * 
     * @return
     */
    public boolean hasTimeout() {
        return ((new Date().getTime() - this.lastPersist.getTime()) > (this.timeoutSecs * 1000))
                && featuresBatched() > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * es.santander.smartcity.ArcgisRestUtils.ArcgisFeatureTable#addToBatch(es.santander.smartcity.
     * model.Feature)
     */
    @Override
    public void addToBatch(Feature feature) {
        super.addToBatch(feature);
        this.lastPersist = new Date();
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.ArcgisRestUtils.ArcgisFeatureTable#flushBatch()
     */
    @Override
    public void flushBatch() {
        super.flushBatch();
        this.lastPersist = new Date();
    }

}
