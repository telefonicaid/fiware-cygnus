/**
 * 
 */
package com.telefonica.iot.cygnus.backends.arcgis.model;

/**
 * @author dmartinez
 *
 */
public enum SpatialReference {
    WGS84(4326), ED50(23030), ETRS89(4258);

    private int wkid;

    /**
     * Constructor.
     * 
     * @param wkid
     */
    SpatialReference(int wkid) {
        this.wkid = wkid;
    }

    public Integer getWkid() {
        return wkid;
    }
}
