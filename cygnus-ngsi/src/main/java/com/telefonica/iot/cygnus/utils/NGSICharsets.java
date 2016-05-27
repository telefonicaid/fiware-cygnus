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

/**
 *
 * @author frb
 */
public final class NGSICharsets {
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private NGSICharsets() {
    } // NGSICharsets
    
    /**
     * Encodes a string for CartoDB.
     * @param in
     * @return
     */
    public static String cartoDBEncode(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 65 && code <= 90) { // A-Z --> a-z
                out += (char) (code + 32);
            } else if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "abcd"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9]{4}$")) { // x --> xx
                    out += "xx";
                } else { // x --> x
                    out += c;
                } // if else
            } else if (code == 121 || code == 122) { // yz --> yz
                out += c;
            } else if (code >= 48 && code <= 57) { // 0-9 --> 0-9
                out += c;
            } else if (c == '_') { // _ --> _
                out += c;
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // cartoDBEncode

} // NGSICharsets
