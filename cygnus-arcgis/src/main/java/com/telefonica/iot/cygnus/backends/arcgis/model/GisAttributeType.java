/**
 * 
 */
package com.telefonica.iot.cygnus.backends.arcgis.model;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

/**
 * @author dmartinez
 *
 */
public enum GisAttributeType {

    DATE("Date"), INTEGER("Integer"), NUMBER("Number"), BOOLEAN("Boolean"), STRING("String"), OID(
            "OBJECTID"), GID("GlobalID"), GEOMETRY("Geometry");

    private String strValue;

    /**
     * Constructor.
     * 
     * @param value
     */
    GisAttributeType(String value) {
        this.strValue = value;
    }

    /**
     * @return String
     */
    public String toString() {
        return strValue;
    }

    /**
     * 
     * @param strType
     * @return Gis attribute type
     * @throws ArcgisException
     */
    public static GisAttributeType fromString(String strType) throws ArcgisException {
        strType = strType.replace("esriFieldType", "");
        switch (strType) {
            case "Date":
                return GisAttributeType.DATE;
            case "Integer":
            case "SmallInteger":
                return GisAttributeType.INTEGER;
            case "Number":
            case "Double":
                return GisAttributeType.NUMBER;
            case "Boolean":
                return GisAttributeType.BOOLEAN;
            case "String":
                return GisAttributeType.STRING;
            case "OBJECTID":
            case "OID":
                return GisAttributeType.OID;
            case "GlobalID":
                return GisAttributeType.GID;
            case "Geometry":
                return GisAttributeType.GEOMETRY;
            default:
                throw new ArcgisException("Invalid string type: " + strType);
        }
    }

    /**
     * Prepares object value to send to Gis.
     * 
     * @param type
     * @param value
     * @return
     */
    public static Object parseAttValue(GisAttributeType type, Object value) {
        Object result = value;
        if (value != null) {

            // TODO parse/validate the rest of data types if needed.
            switch (type) {
                case BOOLEAN:
                    String boolStr = value.toString();
                    boolStr = boolStr.trim().toLowerCase();
    
                    if (("y").equals(boolStr) || ("s").equals(boolStr) || ("true").equals(boolStr)
                            || ("1").equals(boolStr)) {
                        result = new Integer(1);
                    } else {
                        result = new Integer(0);
                    }
                    break;
                case INTEGER:
                    String integerStr = value.toString();
                    try {
                        Integer intValue = Integer.parseInt(integerStr);
                        result = intValue;
                    } catch (NumberFormatException e) {
                        integerStr = integerStr.trim().toLowerCase();
                        switch (integerStr) {
                            case "y":
                            case "true":
                            case "t":
                            case "s":
                                result = true;
                                break;
        
                            case "n":
                            case "false":
                            case "f":
                                result = false;
                                break;
        
                            default:
                                result = null;
                        }
                    }
    
                    break;
                default:
                    result = value;
            }
        }
        return result;
    }
}
