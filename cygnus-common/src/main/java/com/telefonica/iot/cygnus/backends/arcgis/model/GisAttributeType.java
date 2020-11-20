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
     * Converts a given string to its boolean value.
     * 
     * Returns 
     *      true if boolStr is one of ("true","t","y","1","s") ignoring case
     *      false if boolStr is one of ("false","f","n","0")
     *      If it's none of above, returns null
     * 
     * @param boolStr
     * @return
     */
    public static Boolean parseBoolean (String boolStr){
        switch (boolStr.trim()) {
        case "y":
        case "true":
        case "t":
        case "s":
        case "1":
            return true;

        case "n":
        case "false":
        case "f":
        case "0":
            return false;

        default:
            return null;
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
    
                    if (parseBoolean(boolStr)) {
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
                        // if it's not an integer, it should be a boolean value
                        result = parseBoolean(integerStr);
                    }
    
                    break;
                default:
                    result = value;
            }
        }
        return result;
    }
}
