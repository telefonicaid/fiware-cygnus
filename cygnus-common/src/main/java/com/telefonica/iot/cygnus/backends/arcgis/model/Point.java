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

import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * 
 * @author dmartinez
 *
 */
public class Point implements Geometry {
    private static final CygnusLogger LOGGER = new CygnusLogger(Point.class);
    
    private static final String SPATIAL_REFERENCE_TAG = "spatialReference";
    private static final String WKID_TAG = "wkid";
    private static final String Y_TAG = "y";
    private static final String X_TAG = "x";


    private double x;
    private double y;

    private SpatialReference spatialReference;
    private int type = Geometry.TYPE_POINT;

    /**
     * Constructor.
     * 
     * @param lat
     * @param lng
     * @param spatialReference
     */
    public Point(double lat, double lng, SpatialReference spatialReference) {
        this.x = lat;
        this.y = lng;
        this.spatialReference = spatialReference;
    }

    /**
     * Constructor.
     * 
     * @param lat
     * @param lng
     */
    public Point(double lat, double lng) {
        this(lat, lng, SpatialReference.WGS84);
    }

    /**
     * SetValue.
     */
    public void setValue(Geometry g) throws ArcgisException {
        if (g.getGeometryType() == Geometry.TYPE_POINT) {
            Point point = (Point) g;
            this.x = point.x;
            this.y = point.y;
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
    public Point(String strPoint) throws ArcgisException {
        try {
            String[] coords = strPoint.split(",");
            if (coords.length == 2) {
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                this.spatialReference = SpatialReference.WGS84;

                this.x = x;
                this.y = y;
            } else {
                throw new ArcgisException("Unexpected string format for type Point.");
            }
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
        result.addProperty(X_TAG, this.x);
        result.addProperty(Y_TAG, this.y);

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
            double x = json.get(X_TAG).getAsDouble();
            double y = json.get(Y_TAG).getAsDouble();

            // TODO Get spatial reference from json object

            return new Point(x, y, SpatialReference.WGS84);
        } catch (Exception e) {
            LOGGER.error(e.getClass().getSimpleName() + "  " + e.getMessage());
            throw new ArcgisException("Unable to parse Point from json " + e.getMessage());
        }

    }

    /**
     * @return String
     */
    public String toString() {
        return getX() + "," + getY();
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
    public double getX() {
        return x;
    }

    /**
     * 
     * @return
     */
    public double getY() {
        return y;
    }

    @Override
    public Object getValue() {
        return null;
    }

}
