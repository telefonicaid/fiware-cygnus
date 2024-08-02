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

package com.telefonica.iot.cygnus.backends.arcgis.model;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * 
 * @author avega
 *
 */
public class Polygon implements Geometry {
    private static final CygnusLogger LOGGER = new CygnusLogger(Polygon.class);
    
    private static final String SPATIAL_REFERENCE_TAG = "spatialReference";
    private static final String WKID_TAG = "wkid";
    private static final String RINGS_TAG = "rings";    

    public List<List<double[]>> rings;

    private SpatialReference spatialReference;
    private int type = Geometry.TYPE_SHAPE; // TBD

    /**
     * Constructor.
     * 
     * @param rings
     * @param spatialReference
     */
    public Polygon(List<List<double[]>> rings, SpatialReference spatialReference) {
        this.rings = rings;
        this.spatialReference = spatialReference;
    }

    /**
     * Constructor.
     * 
     * @param rings
     */
    public Polygon(List<List<double[]>> rings) {
        this(rings, SpatialReference.WGS84);
    }

    /**
     * SetValue.
     */
    public void setValue(Geometry g) throws ArcgisException {
        if (g.getGeometryType() == Geometry.TYPE_SHAPE) {
            Polygon polygon = (Polygon) g;
            this.rings = polygon.rings;
        } else {
            throw new ArcgisException("Invalid Geometry Type, Polygon expected.");
        }
    }

    /**
     * Constructor.
     * 
     * @param strPoint
     * @throws ArcgisException
     */
    public Polygon(String strPolyline) throws ArcgisException {
        try {
            JsonObject jsonObject = JsonParser.parseString(strPolyline).getAsJsonObject();
            String theRingsStr = jsonObject.get("rings").toString();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<List<double[]>>>() {}.getType();
            this.rings = gson.fromJson(theRingsStr, listType);
            this.spatialReference = SpatialReference.WGS84;
        } catch (NumberFormatException e) {
            LOGGER.error(e.getClass().getSimpleName() + "  " + e.getMessage());
            throw new ArcgisException("Unexpected string format for type Polygon.");
        }
    }

    /**
     * Sets Geometry From JSON.
     */
    public void setGeometryFromJSON(String json) {
        // TODO Auto-generated method stub

    }

    /**
     * @return JsonObject
     */
    public JsonObject toJSON() {
        JsonObject result = new JsonObject();
        LOGGER.debug("toJSON  ");
        result.addProperty(RINGS_TAG, this.toString());

        JsonObject spatialRef = new JsonObject();
        spatialRef.addProperty(WKID_TAG, spatialReference.getWkid());

        result.add(SPATIAL_REFERENCE_TAG, spatialRef);
        return result;
    }

    /**
     * Factroy method.
     * 
     * @param json
     * @return
     * @throws ArcgisException
     */
    public static Geometry createInstanceFromJson(JsonObject json) throws ArcgisException {
        try {
            return new Polygon(json.get(RINGS_TAG).getAsString());
        } catch (Exception e) {
            LOGGER.error(e.getClass().getSimpleName() + "  " + e.getMessage());
            throw new ArcgisException("Unable to parse Polygon from json " + e.getMessage());
        }

    }

    /**
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"rings\": [");
        for (int i = 0; i < this.rings.size(); i++) {
            List<double[]> innerList = this.rings.get(i);
            for (int j = 0; j < innerList.size(); j++) {
                sb.append(" [");
                sb.append("[");
                double[] array = innerList.get(j);
                for (double value : array) {
                    sb.append(" ").append(value).append(",");
                }
                sb.append(" ]");
                sb.setLength(sb.length() - 2);
                sb.append(" ],");
            }
        }
        sb.setLength(sb.length() - 2);
        sb.append(" ]}");
        return sb.toString();
    }

    /**
     * @return geometry type
     */
    public int getGeometryType() {
        return type;
    }

    /**
     * 
     */
    public void setSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    /**
     * 
     */
    public SpatialReference getSpatialReference() {
        return this.spatialReference;
    }

    /**
     * 
     * @return
     */
    public List<List<double[]>> getRings() {
        return this.rings;
    }

    @Override
    public Object getValue() {
        return null;
    }

}
