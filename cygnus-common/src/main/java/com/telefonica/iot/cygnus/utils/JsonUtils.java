/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author frb
 */
public final class JsonUtils {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private JsonUtils() {
    } // JsonUtils
    
    /**
     * Reads a Json file. It ignores white spaces and comments (starting with '#').
     * @param fileName
     * @return A String containing the Json read
     * @throws IOException
     */
    public static String readJsonFile(String fileName) throws IOException {
        if (fileName == null) {
            return null;
        } // if

        String jsonStr = "";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                } // if
                
                jsonStr += line;
            } // while
        } // try
        
        return jsonStr;
    } // readJsonFile

    /**
     * Writes a Json file.
     * @param fileName
     * @param data
     * @throws IOException
     */
    public static void writeJsonFile(String fileName, String data) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(data);
        } // try
    } // writeJsonFile
    
    /**
     * Parses a Json string.
     * @param jsonStr
     * @return A JSONObject
     * @throws ParseException
     */
    public static JSONObject parseJsonString(String jsonStr) throws ParseException {
        if (jsonStr == null) {
            return null;
        } // if

        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(jsonStr);
    } // parseJsonString
    
} // JsonUtils
