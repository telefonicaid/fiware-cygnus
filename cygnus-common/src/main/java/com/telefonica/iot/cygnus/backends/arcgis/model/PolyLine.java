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
import com.google.gson.JsonObject;
import com.google.gson.Gson;
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

    class Paths {
        private List<List<Point>> paths;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n  \"paths\": [\n");
            for (List<Point> path : this.paths) {
                sb.append("    [\n");
                for (Point point : path) {
                    sb.append("      ").append(point.toString()).append(",\n");
                }
                sb.setLength(sb.length() - 2);
                sb.append("\n    ],\n");
            }
            sb.setLength(sb.length() - 2);
            sb.append("\n  ]\n}");
            return sb.toString();
        }
        
    }
    private List<List<Point>> paths;

    private SpatialReference spatialReference;
    private int type = Geometry.TYPE_SHAPE; // TBD

    /**
     * Constructor.
     * 
     * @param paths
     * @param spatialReference
     */
    public PolyLine(List<List<Point>> paths, SpatialReference spatialReference) {
        this.paths = paths;
        this.spatialReference = spatialReference;
    }

    /**
     * Constructor.
     * 
     * @param lat
     * @param lng
     */
    public PolyLine(List<List<Point>> paths) {
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
            Gson gson = new Gson();
            Paths wrapper = gson.fromJson(strPolyline, Paths.class);
            this.paths = wrapper.paths;
            this.spatialReference = SpatialReference.WGS84;
            
        } catch (NumberFormatException e) {
            throw new ArcgisException("Unexpected string format for type Point.");
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
        result.addProperty(PATHS_TAG, this.paths.toString());

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
        return getPaths().toString();
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
    public List<List<Point>> getPaths() {
        return paths;
    }

    @Override
    public Object getValue() {
        return null;
    }

}
