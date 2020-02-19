/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telefonica.iot.cygnus.log.CygnusLogger;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.io.File.separator;

/**
 *
 * @author frb
 */
public final class NGSIUtils {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIUtils.class);
    private static final Pattern ENCODEPATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-]");
    private static final Pattern ENCODEPATTERNSLASH = Pattern.compile("[^a-zA-Z0-9\\.\\-\\/]");
    private static final Pattern ENCODEHIVEPATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern ENCODESTHDBPATTERN = Pattern.compile("[=\\/\\\\.\\$\" ]");
    private static final Pattern ENCODESTHCOLLECTIONPATTERN = Pattern.compile("[=\\$]");
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private NGSIUtils() {
    } // NGSIUtils
    
    /**
     * Encodes a string replacing all the non alphanumeric characters by '_' (except by '-' and '.').
     * This should be only called when building a persistence element name, such as table names, file paths, etc.
     * 
     * @param in
     * @param deleteSlash
     * @param encodeSlash
     * @return The encoded version of the input string.
     */
    public static String encode(String in, boolean deleteSlash, boolean encodeSlash) {
        if (deleteSlash) {
            return ENCODEPATTERN.matcher(in.substring(1)).replaceAll("_");
        } else if (encodeSlash) {
            return ENCODEPATTERN.matcher(in).replaceAll("_");
        } else {
            return ENCODEPATTERNSLASH.matcher(in).replaceAll("_");
        } // if else
    } // encode

    /**
     * Encodes a string replacing all '/', '\', '.', ' ', '"' and '$' by '_'.
     * @param in
     * @return The encoded version of the input string
     */
    public static String encodeSTHDB(String in) {
        return ENCODESTHDBPATTERN.matcher(in).replaceAll("_");
    } // encodeSTHDB
    
    /**
     * Encodes a string replacing all '$' by '_'.
     * @param in
     * @return The encoded version of the input string
     */
    public static String encodeSTHCollection(String in) {
        return ENCODESTHCOLLECTIONPATTERN.matcher(in).replaceAll("_");
    } // encodeSTHCollection
    
    /**
     * Gets a geometry value, ready for insertion in CartoDB, given a NGSI attribute value and its metadata.
     * If the attribute is not geo-related, it is returned as it is.
     * @param attrValue
     * @param attrType
     * @param metadata
     * @param swapCoordinates
     * @return The geometry value, ready for insertion in CartoDB/PostGIS, or the value as it is
     */
    public static ImmutablePair<String, Boolean> getGeometry(String attrValue, String attrType, String metadata,
            boolean swapCoordinates) {
        // First, check the attribute type
        if (attrType.equals("geo:point")) {
            String[] split = attrValue.split(",");
                
            if (swapCoordinates) {
                return new ImmutablePair(
                        "ST_SetSRID(ST_MakePoint(" + split[1].trim() + "::double precision , " + split[0].trim() + "::double precision ), 4326)", true);
            } else {
                return new ImmutablePair(
                        "ST_SetSRID(ST_MakePoint(" + split[0].trim() + "::double precision , " + split[1].trim() + "::double precision ), 4326)", true);
            } // if else
        } // if
        
        if (attrType.equals("geo:json")) {
            return new ImmutablePair("ST_GeomFromGeoJSON('" + attrValue + "')", true);
        } // if

        // TBD: What about:  ?
          // 'geo:line'
          // 'geo:box'
          // 'geo:polygon'
          // 'geo:multipoint'
          // 'geo:multiline'
          // 'geo:multipolygon'

        
        // The type was not 'geo:point' nor 'geo:json', thus try the metadata
        JSONParser parser = new JSONParser();
        JSONArray mds;
        
        try {
            mds = (JSONArray) parser.parse(metadata);
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the metadata. Details: " + e.getMessage());
            return new ImmutablePair(attrValue, false);
        } // try catch
        
        for (Object mdObject : mds) {
            JSONObject md = (JSONObject) mdObject;
            String mdName = (String) md.get("name");
            String mdType = (String) md.get("type");
            String mdValue = (String) md.get("value");
            
            if (mdName.equals("location") && mdType.equals("string") && mdValue.equals("WGS84")) {
                String[] split = attrValue.split(",");
                
                if (swapCoordinates) {
                    return new ImmutablePair(
                            "ST_SetSRID(ST_MakePoint(" + split[1].trim() + "::double precision , " + split[0].trim() + "::double precision ), 4326)", true);
                } else {
                    return new ImmutablePair(
                            "ST_SetSRID(ST_MakePoint(" + split[0].trim() + "::double precision , " + split[1].trim() + "::double precision ), 4326)", true);
                } // if else
            } // if
        } // for
        
        // The attribute was not related to a geolocation
        return new ImmutablePair(attrValue, false);
    } // getGeometry

    /**
     * Gets string value from json element.
     *
     * @param value           the value to process
     * @param quotationMark   the quotation mark
     * @param attrNativeTypes the attr native types
     * @return the string value from json element
     */
    public static String getStringValueFromJsonElement(JsonElement value, String quotationMark, boolean attrNativeTypes) {
        String stringValue;
        if (attrNativeTypes) {
            if (value == null || value.isJsonNull()) {
                stringValue = "NULL";
            } else if (value.isJsonPrimitive()) {
                if (value.getAsJsonPrimitive().isBoolean()) {
                    stringValue = value.getAsString().toUpperCase();
                } else if (value.getAsJsonPrimitive().isNumber()) {
                    stringValue = value.getAsString();
                }else {
                    if (value.toString().contains("ST_GeomFromGeoJSON") || value.toString().contains("ST_SetSRID")) {
                        stringValue = value.getAsString().replace("\\", "");
                    } else {
                        stringValue = quotationMark + value.getAsString() + quotationMark;
                    }
                }
            } else {
                stringValue = quotationMark + value.toString() + quotationMark;
            }
        } else {
            if (value != null && value.isJsonPrimitive()) {
                stringValue = quotationMark + value.getAsString() + quotationMark;
            } else {
                if (value == null){
                    stringValue = quotationMark + "NULL" + quotationMark;
                } else {
                    stringValue = quotationMark + value.toString() + quotationMark;
                }
            }
        }
        return stringValue;
    }

    /**
     * Gets values for insert.
     *
     * @param aggregation     the aggregation
     * @param attrNativeTypes the attr native types
     * @return a String with all VALUES in SQL query format.
     */
    public static String getValuesForInsert(LinkedHashMap<String, ArrayList<JsonElement>> aggregation, boolean attrNativeTypes) {
        String valuesForInsert = "";
        int numEvents = collectionSizeOnLinkedHashMap(aggregation);

        for (int i = 0; i < numEvents; i++) {
            if (i == 0) {
                valuesForInsert += "(";
            } else {
                valuesForInsert +=  ",(";
            } // if else
            boolean first = true;
            Iterator<String> it = aggregation.keySet().iterator();
            while (it.hasNext()) {
                String entry = (String) it.next();
                ArrayList<JsonElement> values = (ArrayList<JsonElement>) aggregation.get(entry);
                JsonElement value = values.get(i);
                String stringValue = getStringValueFromJsonElement(value, "'", attrNativeTypes);
                if (first) {
                    valuesForInsert += stringValue;
                    first = false;
                } else {
                    valuesForInsert += "," + stringValue;
                } // if else
            } // while
            valuesForInsert += ")";
        } // for
        return valuesForInsert;
    } // getValuesForInsert

    /**
     * Gets fields for create.
     *
     * @param aggregation the aggregation
     * @return the fields (column names) for create in SQL format.
     */
    public static String getFieldsForCreate(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        String fieldsForCreate = "(";
        boolean first = true;
        Iterator<String> it = aggregation.keySet().iterator();
        while (it.hasNext()) {
            if (first) {
                fieldsForCreate += (String) it.next() + " text";
                first = false;
            } else {
                fieldsForCreate += "," + (String) it.next() + " text";
            } // if else
        } // while

        return fieldsForCreate + ")";
    } // getFieldsForCreate

    /**
     * Gets fields for insert.
     *
     * @param aggregation the aggregation
     * @return the fields (column names) for insert in SQL format.
     */
    public static String getFieldsForInsert(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        String fieldsForInsert = "(";
        boolean first = true;
        Iterator<String> it = aggregation.keySet().iterator();
        while (it.hasNext()) {
            if (first) {
                fieldsForInsert += (String) it.next();
                first = false;
            } else {
                fieldsForInsert += "," + (String) it.next();
            } // if else
        } // while
        return fieldsForInsert + ")";
    } // getFieldsForInsert

    /**
     * Linked hash map to json list array list.
     *
     * @param aggregation the aggregation
     * @return an ArrayList of JsonObjects wich contain all entityes on a LinkedHashMap
     */
    public static ArrayList<JsonObject> linkedHashMapToJsonList(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<JsonObject> jsonStrings = new ArrayList<>();
        int numEvents = collectionSizeOnLinkedHashMap(aggregation);
        for (int i = 0; i < numEvents; i++) {
            Iterator<String> it = aggregation.keySet().iterator();
            JsonObject jsonObject = new JsonObject();
            while (it.hasNext()) {
                String entry = (String) it.next();
                ArrayList<JsonElement> values = (ArrayList<JsonElement>) aggregation.get(entry);
                if (values.get(i) != null)
                    jsonObject.add(entry, values.get(i));
            }
            jsonStrings.add(jsonObject);
        }
        return jsonStrings;
    }

    /**
     * Linked hash map to json list with out empty md array list.
     *
     * @param aggregation the aggregation
     * @return an ArrayList of JsonObjects wich contain all attributes on a LinkedHashMap, this method also removes empty medatada fields.
     */
    public static ArrayList<JsonObject> linkedHashMapToJsonListWithOutEmptyMD(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<JsonObject> jsonStrings = new ArrayList<>();
        int numEvents = collectionSizeOnLinkedHashMap(aggregation);
        for (int i = 0; i < numEvents; i++) {
            Iterator<String> it = aggregation.keySet().iterator();
            JsonObject jsonObject = new JsonObject();
            while (it.hasNext()) {
                String entry = (String) it.next();
                ArrayList<JsonElement> values = (ArrayList<JsonElement>) aggregation.get(entry);
                if (values.get(i) != null) {
                    if (entry.contains("_md") || entry.contains("Md")) {
                        if (!values.get(i).toString().contains("[]"))
                            jsonObject.add(entry, values.get(i));
                    } else {
                        jsonObject.add(entry, values.get(i));
                    }
                }
            }
            jsonStrings.add(jsonObject);
        }
        return jsonStrings;
    }

    /**
     * Crop linked hash map linked hash map.
     *
     * @param aggregation the aggregation
     * @param keysToCrop  the keys to crop
     * @return removes all keys on list keysToCrop from the aggregation object.
     */
    public static LinkedHashMap<String, ArrayList<JsonElement>> cropLinkedHashMap(LinkedHashMap<String, ArrayList<JsonElement>> aggregation, ArrayList<String> keysToCrop) {
        LinkedHashMap<String, ArrayList<JsonElement>> cropedLinkedHashMap = (LinkedHashMap<String, ArrayList<JsonElement>>) aggregation.clone();
        for (String key : keysToCrop) {
            cropedLinkedHashMap.remove(key);
        }
        return cropedLinkedHashMap;
    }

    /**
     * Collection size on linked hash map int.
     *
     * @param aggregation the aggregation
     * @return the number of attributes contained on the aggregation object.
     */
    public static int collectionSizeOnLinkedHashMap(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        ArrayList<ArrayList<JsonElement>> list = new ArrayList<>(aggregation.values());
        return list.get(0).size();
    }

    /**
     * Linked hash map without default fields linked hash map.
     *
     * @param aggregation       the aggregation
     * @param attrMetadataStore the attr metadata store
     * @return the linked hash map without metadata objects (if attrMetadataStore is set to true)
     * also, removes "_type" and "RECV_TIME_TSC" keys from the object
     */
    public static LinkedHashMap<String, ArrayList<JsonElement>> linkedHashMapWithoutDefaultFields(LinkedHashMap<String, ArrayList<JsonElement>> aggregation, boolean attrMetadataStore) {
        ArrayList<String> keysToCrop = new ArrayList<>();
        Iterator<String> it = aggregation.keySet().iterator();
        while (it.hasNext()) {
            String entry = (String) it.next();
            if ((!attrMetadataStore && (entry.contains("_md") || entry.contains("_MD") || entry.equals(NGSIConstants.ATTR_MD)) || entry.contains("_type")) || (entry.equals(NGSIConstants.RECV_TIME_TS+"C"))) {
                keysToCrop.add(entry);
            }
        }
        return cropLinkedHashMap(aggregation, keysToCrop);
    }

    /**
     * Attribute names array list.
     *
     * @param aggregation the aggregation
     * @return an arraylist containing all names the attributes contained on the LinkedHashMap.
     */
    public static ArrayList<String> attributeNames(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        String [] keys= aggregation.keySet().toArray(new String[aggregation.size()]);
        ArrayList <String> attributeNames = new ArrayList<>(Arrays.asList(keys));
        attributeNames.remove(NGSIConstants.RECV_TIME_TS);
        attributeNames.remove(NGSIConstants.RECV_TIME_TS+"C");
        attributeNames.remove(NGSIConstants.RECV_TIME);
        attributeNames.remove(NGSIConstants.FIWARE_SERVICE_PATH);
        attributeNames.remove(NGSIConstants.ENTITY_ID);
        attributeNames.remove(NGSIConstants.ENTITY_TYPE);
        ArrayList <String> cropedList = (ArrayList<String>) attributeNames.clone();
        for (String key : cropedList) {
            if (key.contains("_md") || key.contains("_type")) {
                attributeNames.remove(key);
            }
        }
        return attributeNames;
    }

} // NGSIUtils
