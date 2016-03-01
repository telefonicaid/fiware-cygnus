/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author frb
 */
public final class Utils {
    
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
        String res = in.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        return (res.startsWith("_") ? res.substring(1, res.length()) : res);
    } // encode
    
    /**
     * Encodes a string replacing all the non alphanumeric characters by '_'.
     * 
     * @param in
     * @return The encoded version of the input string.
     */
    public static String encodeHive(String in) {
        return in.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    } // encodeHive
    
    /**
     * Converts a XML node into Json.
     * @param xmlNode
     * @return
     * @throws Exception
     */
    public static JsonElement basicXml2Json(Node xmlNode) throws Exception {
        // if the XML node has not attributes, it is either an object either a string
        if (!xmlNode.hasAttributes()) {
            Node child = xmlNode.getFirstChild();

            if (child.getFirstChild() != null) {
                NodeList domObjects = ((Element) xmlNode).getChildNodes();
                JsonObject jsonObject = new JsonObject();

                for (int i = 0; i < domObjects.getLength(); i++) {
                    Node domObject = domObjects.item(i);
                    jsonObject.add(domObject.getNodeName(), basicXml2Json(domObject));
                } // for

                return jsonObject;
            } else {
                return new JsonPrimitive(xmlNode.getTextContent());
            } // if else
        } // if

        // if the "type" attribute is not among the existing ones then return error
        if (xmlNode.getAttributes().getNamedItem("type") == null) {
            throw new Exception("Attributes different than \"type\" are not allowed withing or any child tag "
                    + "according to Orion notification API");
        } // if

        String valueType = xmlNode.getAttributes().getNamedItem("type").getTextContent();

        // if the value of the "type" attribute is "vector", the proceed, return error otherwise
        if (valueType.equals("vector")) {
            NodeList domItems = ((Element) xmlNode).getElementsByTagName("item");

            if (domItems.getLength() == 0) {
                throw new Exception("No <item> tag within <contextValue type=\"vector\">");
            } // if

            JsonArray jsonArray = new JsonArray();

            for (int i = 0; i < domItems.getLength(); i++) {
                jsonArray.add(basicXml2Json(domItems.item(i)));
            } // for

            return jsonArray;
        } else {
            throw new Exception("Unknown XML node type: " + valueType);
        } // if else if else
    } // basicXml2Json
    
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
        
} // Utils
