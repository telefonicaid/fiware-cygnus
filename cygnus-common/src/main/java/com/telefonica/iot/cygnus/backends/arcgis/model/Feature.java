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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * @author dmartinez
 *
 */
public class Feature {
    private static final CygnusLogger LOGGER = new CygnusLogger(Feature.class);
    
    private static final String GREGORIAN_CALENDAR_CLASSNAME = "GregorianCalendar";
    private static final String DATE_CLASSNAME = "Date";
    private static final String BOOLEAN_CLASSNAME = "Boolean";
    private static final String STRING_CLASSNAME = "String";
    private static final String DOUBLE_CLASSNAME = "Double";
    private static final String FLOAT_CLASSNAME = "Float";
    private static final String LONG_CLASSNAME = "Long";
    private static final String INTEGER_CLASSNAME = "Integer";
    
    private static final String ATTRIBUTES_TAG = "attributes";
    private static final String GEOMETRY_TAG = "geometry";
    
    private static final String DATE_PATTERN = "MM/dd/yyyy hh:mm:ss";
    private static final String OBJECTID_FIELDNAME = "OBJECTID";

    private Geometry geometry;
    private Map<String, Object> attributes;

    /**
     * 
     */
    public Feature() {
        attributes = new HashMap<String, Object>();
        geometry = null;
    }

    /**
     * 
     * @param geometry
     */
    public Feature(Geometry geometry) {
        this();
        this.geometry = geometry;
    }

    /**
     * 
     * @param geometry
     * @param attributes
     */
    public Feature(Geometry geometry, Map<String, Object> attributes) {
        this.geometry = geometry;
        this.attributes = attributes;
    }

    /**
     * 
     * @param attName
     * @param attValue
     */
    public void addAttribute(String attName, Object attValue) {
        attributes.put(attName, attValue);
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @param geometry
     *            the geometry to set
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 
     * @param latitud
     * @param longitud
     * @return
     */
    public static Feature createPointFeature(double latitud, double longitud) {
        return new Feature(new Point(longitud, latitud, SpatialReference.WGS84));
    }

    /**
     * 
     * @param latitud
     * @param longitud
     * @return
     */
    public static Feature createPointFeature(String latitud, String longitud) {
        double lat;
        double lon;
        try {
            lat = Double.parseDouble(latitud);
            lon = Double.parseDouble(longitud);
        } catch (Exception e) {
            lat = new Double(0);
            lon = new Double(0);
        }

        return createPointFeature(lat, lon);
    }

    /**
     * This method merges unexistent attributes from sourceFeature.
     * 
     * @param sourceFeature
     */
    public void completeAttributesFrom(Feature sourceFeature) {
        Map<String, Object> sourceAttributes = sourceFeature.getAttributes();
        Set<String> sourceKeyset = sourceAttributes.keySet();
        for (String key : sourceKeyset) {
            if (!this.attributes.containsKey(key)) {
                this.attributes.put(key, sourceAttributes.get(key));
            }
        }
    }

    /**
     * 
     */
    @Override
    public String toString() {
        return toJson().toString();

    }

    private void addProperty(JsonObject jsonObj, String name, Object property) {
        SimpleDateFormat simpleDateFormat = null;

        if (property != null) {
            switch (property.getClass().getSimpleName()) {
                case INTEGER_CLASSNAME:
                    jsonObj.addProperty(name, (Integer) property);
                    break;
                case LONG_CLASSNAME:
                    jsonObj.addProperty(name, (Long) property);
                    break;
                case FLOAT_CLASSNAME:
                    jsonObj.addProperty(name, (Float) property);
                    break;
                case DOUBLE_CLASSNAME:
                    jsonObj.addProperty(name, (Double) property);
                    break;
                case STRING_CLASSNAME:
                    jsonObj.addProperty(name, (String) property);
                    break;
                case BOOLEAN_CLASSNAME:
                    jsonObj.addProperty(name, (Boolean) property);
                    break;
                case DATE_CLASSNAME:
                    simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
                    jsonObj.addProperty(name, simpleDateFormat.format((Date) property));
                    break;
                case GREGORIAN_CALENDAR_CLASSNAME:
                    simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
                    GregorianCalendar calendar = (GregorianCalendar) property;
                    jsonObj.addProperty(name, simpleDateFormat.format(calendar.getTime()));
                    break;
                default:
                    jsonObj.addProperty(name, property.toString());
            }
        } else {
            jsonObj.add(name, null);
        }
    }

    /**
     * Retorna el OBJECTID del GIS de la entidad.
     * 
     * @return OBJECTID value
     * @throws ArcgisException
     */
    public Integer getObjectId() throws ArcgisException {
        Integer objectId = -1;
        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            if (OBJECTID_FIELDNAME.equals(attribute.getKey())) {
                objectId = (Integer) attribute.getValue();
                break;
            }
        }

        if ("".equals(objectId)) {
            throw new ArcgisException("Cant find " + GisAttributeType.OID + " in Feature Object.");
        } else {
            return objectId;
        }
    }

    /**
     * Establece el OBJECTID del GIS de la entidad.
     * 
     * @param objectId
     * @return
     * @throws ArcgisException
     */
    public void setObjectId(Integer objectId) throws ArcgisException {
        try {
            boolean found = false;

            for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                if (OBJECTID_FIELDNAME.equals(attribute.getKey())) {
                    found = true;
                    attribute.setValue(objectId);
                    break;
                }
            }
            if (!found) {
                attributes.put(OBJECTID_FIELDNAME, objectId);
            }
        } catch (Exception e) {
            throw new ArcgisException(
                    "Error setting OBJECTID for feature " + this.toString() + " - Error: " + e);
        }
    }

    /**
     * 
     * @return
     */
    public JsonObject toJson() {
        JsonObject resultJSON = new JsonObject();

        resultJSON.add(GEOMETRY_TAG, this.getGeometry().toJSON());

        JsonObject attributes = new JsonObject();
        for (Map.Entry<String, Object> attribute : this.attributes.entrySet()) {
            addProperty(attributes, attribute.getKey(), attribute.getValue());
        }
        resultJSON.add(ATTRIBUTES_TAG, attributes);

        return resultJSON;
    }

    /**
     * 
     * @param json
     * @return
     * @throws ArcgisException
     */
    public static Feature createInstanceFromJson(String json) throws ArcgisException {
        JsonParser parser = new JsonParser();
        JsonObject jsonObj = parser.parse(json).getAsJsonObject();
        return createInstanceFromJson(jsonObj);
    }

    /**
     * 
     * @param json
     * @return
     * @throws ArcgisException
     */
    public static Feature createInstanceFromJson(JsonObject json) throws ArcgisException {
        try {
            Geometry geometry;
            if (json.has(GEOMETRY_TAG)) {
                JsonObject jsonGeometry = json.get(GEOMETRY_TAG).getAsJsonObject();
                geometry = Point.createInstanceFromJson(jsonGeometry); // TODO another 
                                                                       //geometry types?
            } else {
                geometry = new Point(0, 0);
            }
            Map<String, Object> attributes = attToMap(json.get(ATTRIBUTES_TAG).getAsJsonObject());
            return new Feature(geometry, attributes);
        } catch (Exception e) {
            throw new ArcgisException("Can't cast Feature from Json, "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

    }

    /**
     * 
     * @param attributes
     * @return
     * @throws ArcgisException
     */
    protected static Map<String, Object> attToMap(JsonObject attributes) throws ArcgisException {
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        if (attributes.isJsonObject()) {
            for (Entry<String, JsonElement> attribute : attributes.entrySet()) {
                JsonElement attValue = attribute.getValue();
                if (attValue.isJsonPrimitive()) {
                    try {
                        int intValue = attValue.getAsInt();
                        if (attValue.getAsString().equals(Integer.toString(intValue))) {
                            resultMap.put(attribute.getKey(), attValue.getAsInt());
                        } else {
                            throw new Exception("It isn't an integer, maybe a long");
                        }
                    } catch (Exception e2) {
                        try {
                            resultMap.put(attribute.getKey(), attValue.getAsLong());
                        } catch (Exception e1) {
                            try {
                                resultMap.put(attribute.getKey(), attValue.getAsDouble());
                            } catch (Exception e3) {
                                resultMap.put(attribute.getKey(), attValue.getAsString());
                            }
                        }
                    }
                } else if (attValue.isJsonNull()) {
                    resultMap.put(attribute.getKey(), null);
                } else {
                    resultMap.put(attribute.getKey(), attValue.getAsString());
                }
            }
        } else {
            LOGGER.error("Cant parse attributes, JsonObject expected.");
            throw new ArcgisException("Cant parse attributes, JsonObject expected.");
        }
        return resultMap;
    }
}
