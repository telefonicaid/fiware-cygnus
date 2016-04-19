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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
    private static final Pattern ENCODESTHDBPATTERN = Pattern.compile("[\\/\\\\.\\$\" ]");
    private static final Pattern ENCODESTHCOLLECTIONPATTERN = Pattern.compile("\\$");
    private static final Pattern ENCODEPOSTGRESQLPATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final DateTimeFormatter FORMATTER1 = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss'Z'").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER2 = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER3 = DateTimeFormat.forPattern(
            "yyyy-MM-dd HH:mm:ss").withOffsetParsed().withZoneUTC();
    private static final DateTimeFormatter FORMATTER4 = DateTimeFormat.forPattern(
            "yyyy-MM-dd HH:mm:ss.SSS").withOffsetParsed().withZoneUTC();
    
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
     * Encodes a string from an ArrayList.
     * @param in
     * @return The encoded string
     */
    public static String toString(ArrayList in) {
        String out = "";
        
        for (int i = 0; i < in.size(); i++) {
            if (i == 0) {
                out = in.get(i).toString();
            } else {
                out += "," + in.get(i).toString();
            } // if else
        } // for
        
        return out;
    } // toString
    
    /**
     * Gets the Cygnus version from the pom.xml.
     * @return The Cygnus version
     */
    public static String getCygnusVersion() {
        InputStream stream = NGSIUtils.class.getClassLoader().getResourceAsStream("pom.properties");
        
        if (stream == null) {
            return "UNKNOWN";
        } // if
        
        Properties props = new Properties();
        
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "UNKNOWN";
        } // try catch
    } // getCygnusVersion
    
    /**
     * Gets the hash regarding the last Git commit.
     * @return The hash regarding the last Git commit.
     */
    public static String getLastCommit() {
        InputStream stream = NGSIUtils.class.getClassLoader().getResourceAsStream("last_git_commit.txt");
        
        if (stream == null) {
            return "UNKNOWN";
        } // if
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return reader.readLine();
        } catch (Exception e) {
            return "UNKNOWN";
        } // catch
    } // getLastCommit
    
    /**
     * Gets the human redable version of timestamp expressed in miliseconds.
     * @param ts
     * @param addUTC
     * @return
     */
    public static String getHumanReadable(long ts, boolean addUTC) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String humanRedable = sdf.format(new Date(ts));
        humanRedable += "T";
        sdf = new SimpleDateFormat("HH:mm:ss.S");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        humanRedable += sdf.format(new Date(ts)) + (addUTC ? "Z" : "");
        return humanRedable;
    } // getHumanRedable
    
    /**
     * Decides if a given string is a number.
     * http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
     * @param s
     * @return
     */
    public static boolean isANumber(String s) {
        return isANumber(s, 10);
    } // isANumber

    /**
     * Decides if a given string is a number, given its radix.
     * http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
     * @param s
     * @param radix
     * @return
     */
    public static boolean isANumber(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        } // if
        
        for (int i = 0; i < s.length(); i++) {
            if ((i == 0) && (s.charAt(i) == '-')) {
                if (s.length() == 1) {
                    return false;
                } // if
                
                continue;
            } // if
            
            if ((i == 0) && (s.charAt(i) == '.')) {
                return false;
            } // if
            
            if ((i == s.length() - 1) && (s.charAt(i) == '.')) {
                return false;
            } // if
            
            if (s.charAt(i) == '.') {
                continue;
            } // if
            
            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            } // if
            
        } // for
        
        return true;
    } // isANumber
    
    /**
     * Gets the timestamp within a TimeInstant metadata, if exists.
     * @param metadata
     * @return The timestamp within a TimeInstant metadata
     */
    public static Long getTimeInstant(String metadata) {
        Long res = null;
        JSONParser parser = new JSONParser();
        JSONArray mds;
        
        try {
            mds = (JSONArray) parser.parse(metadata);
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the metadata. Details: " + e.getMessage());
            return null;
        } // try catch
        
        for (Object mdObject : mds) {
            JSONObject md = (JSONObject) mdObject;
            String mdName = (String) md.get("name");
            
            if (mdName.equals("TimeInstant")) {
                String mdValue = (String) md.get("value");
                
                if (isANumber(mdValue)) {
                    res = new Long(mdValue);
                } else {
                    DateTime dateTime;
                    
                    try {
                        // ISO 8601 without miliseconds
                        dateTime = FORMATTER1.parseDateTime(mdValue);
                    } catch (Exception e1) {
                        LOGGER.debug(e1.getMessage());
                        
                        try {
                            // ISO 8601 with miliseconds
                            dateTime = FORMATTER2.parseDateTime(mdValue);
                        } catch (Exception e2) {
                            LOGGER.debug(e2.getMessage());
                            
                            try {
                                // ISO 8601 with microsencods
                                String mdValueTruncated = mdValue.substring(0, mdValue.length() - 4) + "Z";
                                dateTime = FORMATTER2.parseDateTime(mdValueTruncated);
                            } catch (Exception e3) {
                                LOGGER.debug(e3.getMessage());
                                
                                try {
                                    // SQL timestamp without miliseconds
                                    dateTime = FORMATTER3.parseDateTime(mdValue);
                                } catch (Exception e4) {
                                    LOGGER.debug(e4.getMessage());

                                    try {
                                        // SQL timestamp with miliseconds
                                        dateTime = FORMATTER4.parseDateTime(mdValue);
                                    } catch (Exception e5) {
                                        LOGGER.debug(e5.getMessage());
                                        
                                        try {
                                            // SQL timestamp with microseconds
                                            String mdValueTruncated = mdValue.substring(0, mdValue.length() - 3);
                                            dateTime = FORMATTER4.parseDateTime(mdValueTruncated);
                                        } catch (Exception e6) {
                                            LOGGER.debug(e6.getMessage());
                                            return null;
                                        } // try catch
                                    } // try catch
                                } // try catch
                            } // try catch
                        } // try catch
                    } // try catch

                    GregorianCalendar cal = dateTime.toGregorianCalendar();
                    res = cal.getTimeInMillis();
                } // if else
                
                break;
            } // if
        } // for
        
        return res;
    } // getTimeInstant
    
    /**
     * Gets the geolocation value, ready for insertion in CartoDB, given a NGSI attribute value and its metadata.
     * If the attribute is not geo-related, it is returned as it is.
     * @param attrValue
     * @param metadata
     * @return The geolocation value, ready for insertion in CartoDB, or tehe value as it is
     */
    public static String getLocation(String attrValue, String metadata) {
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
                String[] split = attrValue.trim().split(",");
                return "ST_SetSRID(ST_MakePoint(" + split[0] + "," + split[1] + "), 4326)";
            } // if
        } // for
        
        return attrValue;
    } // getLocation
        
} // NGSIUtils
