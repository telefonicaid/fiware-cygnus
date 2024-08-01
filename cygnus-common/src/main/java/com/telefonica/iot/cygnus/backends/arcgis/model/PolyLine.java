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
public class PolyLine implements Geometry {
    private static final CygnusLogger LOGGER = new CygnusLogger(PolyLine.class);
    
    private static final String SPATIAL_REFERENCE_TAG = "spatialReference";
    private static final String WKID_TAG = "wkid";
    private static final String PATHS_TAG = "paths";    

    public List<List<double[]>> paths;

    private SpatialReference spatialReference;
    private int type = Geometry.TYPE_SHAPE; // TBD

    /**
     * Constructor.
     * 
     * @param paths
     * @param spatialReference
     */
    public PolyLine(List<List<double[]>> paths, SpatialReference spatialReference) {
        this.paths = paths;
        this.spatialReference = spatialReference;
    }

    /**
     * Constructor.
     * 
     * @param lat
     * @param lng
     */
    public PolyLine(List<List<double[]>> paths) {
        this(paths, SpatialReference.WGS84);
    }

    /**
     * SetValue.
     */
    public void setValue(Geometry g) throws ArcgisException {
        if (g.getGeometryType() == Geometry.TYPE_SHAPE) {
            PolyLine polyline = (PolyLine) g;
            this.paths = polyline.paths;
        } else {
            throw new ArcgisException("Invalid Geometry Type, Point expected.");
        }
    }

    /**
     * Constructor.
     * 
     * @param strPoint
     * @throws ArcgisException
     */
    public PolyLine(String strPolyline) throws ArcgisException {
        try {
            JsonObject jsonObject = JsonParser.parseString(strPolyline).getAsJsonObject();
            String thePathsStr = jsonObject.get("paths").toString();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<List<double[]>>>() {}.getType();
            this.paths = gson.fromJson(thePathsStr, listType);
            this.spatialReference = SpatialReference.WGS84;
        } catch (NumberFormatException e) {
            LOGGER.error(e.getClass().getSimpleName() + "  " + e.getMessage());
            throw new ArcgisException("Unexpected string format for type PolyLine.");
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
        result.addProperty(PATHS_TAG, this.toString());

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
            return new PolyLine(json.get(PATHS_TAG).getAsString());
        } catch (Exception e) {
            LOGGER.error(e.getClass().getSimpleName() + "  " + e.getMessage());
            throw new ArcgisException("Unable to parse PolyLine from json " + e.getMessage());
        }

    }

    /**
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"paths\": [");
        for (int i = 0; i < this.paths.size(); i++) {
            List<double[]> innerList = this.paths.get(i);
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
    public List<List<double[]>> getPaths() {
        return this.paths;
    }

    @Override
    public Object getValue() {
        return null;
    }

}
