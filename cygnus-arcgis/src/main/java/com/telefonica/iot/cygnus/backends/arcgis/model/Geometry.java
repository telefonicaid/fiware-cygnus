package com.telefonica.iot.cygnus.backends.arcgis.model;

import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

import com.telefonica.iot.cygnus.backends.arcgis.model.Geometry;
import com.telefonica.iot.cygnus.backends.arcgis.model.SpatialReference;

/**
 * 
 * @author dmartinez
 *
 */
public interface Geometry {
    static int TYPE_POINT = 1;
    static int TYPE_SHAPE = 2;

    public void setSpatialReference(SpatialReference spatialReference);

    public SpatialReference getSpatialReference();

    public void setValue(Geometry g) throws ArcgisException;

    public void setGeometryFromJSON(String json);

    public Object getValue();

    public String toString();

    public JsonObject toJSON();

    public static Geometry createInstanceFromJson(JsonObject json) throws ArcgisException {
        return null;
    };

    public int getGeometryType();
}
