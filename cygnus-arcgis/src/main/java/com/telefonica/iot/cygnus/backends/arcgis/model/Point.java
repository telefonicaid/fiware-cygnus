package com.telefonica.iot.cygnus.backends.arcgis.model;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

import com.telefonica.iot.cygnus.backends.arcgis.model.Geometry;
import com.telefonica.iot.cygnus.backends.arcgis.model.Point;
import com.telefonica.iot.cygnus.backends.arcgis.model.SpatialReference;

/**
 * 
 * @author dmartinez
 *
 */
public class Point implements Geometry {
    protected static final Logger LOGGER = Logger.getLogger(Point.class);

    private double x;
    private double y;

    private SpatialReference spatialReference;
    private int type = Geometry.TYPE_POINT;

    /**
     * Constructor.
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
        result.addProperty("x", this.x);
        result.addProperty("y", this.y);

        JsonObject spatialRef = new JsonObject();
        spatialRef.addProperty("wkid", spatialReference.getWkid());

        result.add("spatialReference", spatialRef);
        return result;
    }

    /**
     * Factroy method.
     * @param json
     * @return
     * @throws ArcgisException
     */
    public static Geometry createInstanceFromJson(JsonObject json) throws ArcgisException {
        try {
            double x = json.get("x").getAsDouble();
            double y = json.get("y").getAsDouble();

            // TODO Obtener el spatial reference del json

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
