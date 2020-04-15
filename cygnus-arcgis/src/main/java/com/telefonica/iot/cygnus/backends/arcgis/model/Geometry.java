package com.telefonica.iot.cygnus.backends.arcgis.model;

import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

/**
 * 
 * @author dmartinez
 *
 */
public interface Geometry {
    static int TYPE_POINT = 1;
    static int TYPE_SHAPE = 2;

    /**
     * Sets spatial reference value.
     * @param spatialReference
     */
    public void setSpatialReference(SpatialReference spatialReference);

    /**
     * Gets spatial reference value.
     * @return
     */
    public SpatialReference getSpatialReference();

    /**
     * Set geometry.
     * @param g
     * @throws ArcgisException
     */
    public void setValue(Geometry g) throws ArcgisException;

    /**
     * Sets geometry from geo-json string.
     * @param json
     */
    public void setGeometryFromJSON(String json);

    /**
     * Get geometry.
     * @return
     */
    public Object getValue();

    /**
     * parse to string.
     * @return
     */
    public String toString();

    /**
     * parse to Json.
     * @return
     */
    public JsonObject toJSON();

    /**
     * Factory method.
     * @param json
     * @return
     * @throws ArcgisException
     */
    public static Geometry createInstanceFromJson(JsonObject json) throws ArcgisException {
        return null;
    };

    /**
     * Gets geometry type.
     * @return
     */
    public int getGeometryType();
}
