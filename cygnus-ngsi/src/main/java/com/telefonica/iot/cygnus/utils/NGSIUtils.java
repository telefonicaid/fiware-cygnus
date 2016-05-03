/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author frb
 */
public final class NGSIUtils {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIUtils.class);
    private static final Pattern ENCODEPATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-]");
    private static final Pattern ENCODEPATTERNSLASH = Pattern.compile("[^a-zA-Z0-9\\.\\-\\/]");
    private static final Pattern ENCODEHIVEPATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern ENCODESTHCOLLECTIONPATTERN = Pattern.compile("\\$");
    private static final Pattern ENCODEPOSTGRESQLPATTERN = Pattern.compile("[^a-zA-Z0-9]");
    
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
     * Encodes a string replacing all the non alphanumeric characters by '_'.
     * 
     * @param in
     * @return The encoded version of the input string.
     */
    public static String encodeHive(String in) {
        return ENCODEHIVEPATTERN.matcher(in).replaceAll("_").toLowerCase();
    } // encodeHive
    
    /**
     * Encodes a string replacing all '$' by '>'.
     * @param in
     * @return The encoded version of the input string
     */
    public static String encodeSTHCollection(String in) {
        return ENCODESTHCOLLECTIONPATTERN.matcher(in).replaceAll(">");
    } // encodeSTHCollection
    
    /**
     * Encodes a tring replacing all ' ' with '_'.
     * @param in
     * @param deleteSlash
     * @return
     */
    public static String encodePostgreSQL(String in, boolean deleteSlash) {
        // PostgreSQL is case insensitive:
        // http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
        if (deleteSlash) {
            return ENCODEPOSTGRESQLPATTERN.matcher(in.substring(1)).replaceAll("_").toLowerCase();
        } else {
            return ENCODEPOSTGRESQLPATTERN.matcher(in).replaceAll("_").toLowerCase();
        } // else
    } // encodePostgreSQL
    
    /**
     * Gets the geolocation value, ready for insertion in CartoDB, given a NGSI attribute value and its metadata.
     * If the attribute is not geo-related, it is returned as it is.
     * @param attrValue
     * @param attrType
     * @param metadata
     * @param flipCoordinates
     * @return The geolocation value, ready for insertion in CartoDB, or tehe value as it is
     */
    public static String getLocation(String attrValue, String attrType, String metadata, boolean flipCoordinates) {
        // First, check the attribute type
        if (attrType.equals("geo:point")) {
            String[] split = attrValue.split(",");
                
            if (flipCoordinates) {
                return "ST_SetSRID(ST_MakePoint(" + split[1].trim() + "," + split[0].trim() + "), 4326)";
            } else {
                return "ST_SetSRID(ST_MakePoint(" + split[0].trim() + "," + split[1].trim() + "), 4326)";
            } // if else
        } // if
        
        // The type was not 'geo:pint', thus try the metadata
        JSONParser parser = new JSONParser();
        JSONArray mds;
        
        try {
            mds = (JSONArray) parser.parse(metadata);
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the metadata. Details: " + e.getMessage());
            return attrValue;
        } // try catch
        
        for (Object mdObject : mds) {
            JSONObject md = (JSONObject) mdObject;
            String mdName = (String) md.get("name");
            String mdType = (String) md.get("type");
            String mdValue = (String) md.get("value");
            
            if (mdName.equals("location") && mdType.equals("string") && mdValue.equals("WGS84")) {
                String[] split = attrValue.split(",");
                
                if (flipCoordinates) {
                    return "ST_SetSRID(ST_MakePoint(" + split[1].trim() + "," + split[0].trim() + "), 4326)";
                } else {
                    return "ST_SetSRID(ST_MakePoint(" + split[0].trim() + "," + split[1].trim() + "), 4326)";
                } // if else
            } // if
        } // for
        
        // The attribute was not related to a geolocation
        return attrValue;
    } // getLocation
        
} // NGSIUtils
