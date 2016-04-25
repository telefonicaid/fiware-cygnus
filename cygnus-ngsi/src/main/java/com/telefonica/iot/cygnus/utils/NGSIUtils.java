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
        
} // NGSIUtils
