/**
 * 
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

    public boolean testPortal = false;

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
