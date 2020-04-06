/**
 * 
 */
package es.santander.smartcity.ArcgisRestUtils;

/**
 * @author dmartinez
 *
 */
public interface ArcgisBaseTest {

    public static final String PORTAL_PASSWORD = "9PnFpddj";
    public static final String PORTAL_USER = "pmosmartcity2";
    public static final String PORTAL_FEATURETABLE_URL = "https://sags1/arcgis/rest/services/Policia/Ocupaciones/FeatureServer/0";
    public static final String PORTAL_PUBLIC_FEATURETABLE_URL = "https://sags1/arcgis/rest/services/Policia/SenalesTrafico_ETRS89/MapServer/5";
    public static final String PORTAL_GENERATE_TOKEN_URL = "https://sagps1.int.ayto-santander.es/portal/sharing/rest/generateToken";

    public static final String ONLINE_PASSWORD = "Admincic2017";
    public static final String ONLINE_USER = "sc_stdri";
    public static final String ONLINE_GENERATE_TOKEN_URL = "https://aytosantander.maps.arcgis.com/sharing/generateToken";
    public static final String ONLINE_FEATURETABLE_URL = "https://services5.arcgis.com/398f12mJiCbJeoAQ/arcgis/rest/services/OcupacionDummy/FeatureServer/0";

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

    public static String getGenerateTokenUrl() {
        if (testPortal) {
            return PORTAL_GENERATE_TOKEN_URL;
        } else {
            return ONLINE_GENERATE_TOKEN_URL;
        }
    }

    public static String getFeatureUrl() {
        if (testPortal) {
            return PORTAL_FEATURETABLE_URL;
        } else {
            return ONLINE_FEATURETABLE_URL;
        }
    }

}
