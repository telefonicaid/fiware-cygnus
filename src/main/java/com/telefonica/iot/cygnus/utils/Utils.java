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

/**
 *
 * @author frb
 */
public final class Utils {
    
    private static final Pattern ENCODEPATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-]");
    private static final Pattern ENCODEHIVEPATTERN = Pattern.compile("[^a-zA-Z0-9]");
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private Utils() {
    } // Utils
    
    /**
     * Encodes a string replacing all the non alphanumeric characters by '_' (except by '-' and '.').
     * 
     * @param in
     * @return The encoded version of the input string.
     */
    public static String encode(String in) {
        String res = ENCODEPATTERN.matcher(in).replaceAll("_");
        return (res.startsWith("_") ? res.substring(1, res.length()) : res);
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
     * Encodes a string from an ArrayList
     * @param in
     * @return The encoded string
     */
    public static String toString(ArrayList in) {
        String out = "";
        
        for (int i=0; i < in.size(); i++) {
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
        InputStream stream = Utils.class.getClassLoader().getResourceAsStream("pom.properties");
        
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
        InputStream stream = Utils.class.getClassLoader().getResourceAsStream("last_git_commit.txt");
        
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
     * @throws Exception
     */
    public static Long getTimeInstant(String metadata) throws Exception {
        Long res = null;
        JSONParser parser = new JSONParser();
        JSONArray mds = (JSONArray) parser.parse(metadata);
        
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
                        DateTimeFormatter formatter = DateTimeFormat.forPattern(
                                "yyyy-MM-dd'T'hh:mm:ss'Z'").withOffsetParsed();
                        dateTime = formatter.parseDateTime(mdValue);
                    } catch (Exception e1) {
                        try {
                            // ISO 8601 with miliseconds
                            DateTimeFormatter formatter = DateTimeFormat.forPattern(
                                    "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'").withOffsetParsed();
                            dateTime = formatter.parseDateTime(mdValue);
                        } catch (Exception e2) {
                            try {
                                DateTimeFormatter formatter = DateTimeFormat.forPattern(
                                        "yyyy-MM-dd hh:mm:ss").withOffsetParsed();
                                dateTime = formatter.parseDateTime(mdValue);
                            } catch (Exception e3) {
                                try {
                                    DateTimeFormatter formatter = DateTimeFormat.forPattern(
                                            "yyyy-MM-dd hh:mm:ss.SSS").withOffsetParsed();
                                    dateTime = formatter.parseDateTime(mdValue);
                                } catch (Exception e4) {
                                    return null;
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
        
} // Utils
