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

package com.telefonica.iot.cygnus.backends.arcgis.restutils;

/**
 * @author dmartinez
 *
 */
public interface ArcgisBaseTest {

    public static final String PORTAL_PASSWORD = "************";
    public static final String PORTAL_USER = "***************";
    public static final String PORTAL_FEATURETABLE_URL = "***************";
    public static final String PORTAL_PUBLIC_FEATURETABLE_URL = "***************";
    public static final String PORTAL_GENERATE_TOKEN_URL = "***************";

    public static final String ONLINE_PASSWORD = "***************";
    public static final String ONLINE_USER = "***************";
    public static final String ONLINE_GENERATE_TOKEN_URL = "***************";
    public static final String ONLINE_FEATURETABLE_URL = "***************";

    public static final boolean testPortal = false;
    public static final boolean skipConnectionTest = true;

    /**
     * 
     * @return
     */
    public static boolean connectionTestsSkipped() {
        return skipConnectionTest;
    }
    /**
     * 
     * @return
     */
    public static String getUser() {
        if (testPortal) {
            return PORTAL_USER;
        } else {
            return ONLINE_USER;
        }
    }
    

    /**
     * 
     * @return
     */
    public static String getPassword() {
        if (testPortal) {
            return PORTAL_PASSWORD;
        } else {
            return ONLINE_PASSWORD;
        }
    }

    /**
     * 
     * @return Tokenken generation url
     */
    public static String getGenerateTokenUrl() {
        if (testPortal) {
            return PORTAL_GENERATE_TOKEN_URL;
        } else {
            return ONLINE_GENERATE_TOKEN_URL;
        }
    }

    /**
     * 
     * @return feature url
     */
    public static String getFeatureUrl() {
        if (testPortal) {
            return PORTAL_FEATURETABLE_URL;
        } else {
            return ONLINE_FEATURETABLE_URL;
        }
    }

}
