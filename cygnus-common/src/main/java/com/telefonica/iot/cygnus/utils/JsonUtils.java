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
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
     * @throws java.lang.Exception
     */
    public static String readJsonFile(String fileName) throws Exception {
        if (fileName == null) {
            return null;
        } // if

        String jsonStr = "";
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(fileName));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            } // if

            jsonStr += line;
        } // while

        return jsonStr;
    } // readJsonFile

    /**
     * Parses a Json string.
     * @param jsonStr
     * @return A JSONObject
     * @throws java.lang.Exception
     */
    public static JSONObject parseJsonString(String jsonStr) throws Exception {
        if (jsonStr == null) {
            return null;
        } // if

        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(jsonStr);
    } // parseJsonString
    
} // Jsonu-tils
