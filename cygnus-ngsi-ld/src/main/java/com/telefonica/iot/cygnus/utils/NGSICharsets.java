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
     * Encodes a string for PostgreSQL. This includes CartoDB. Only lowercase alphanumerics and _ are allowed.
     * @param in
     * @return The encoded string
     */
    public static String encodePostgreSQL(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
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
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // encodePostgreSQL
    
    /**
     * Encodes a string for HDFS.
     * @param in
     * @param allowSlashes
     * @return The encoded string
     */
    public static String encodeHDFS(String in, boolean allowSlashes) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            
            if (c == '/') {
                if (allowSlashes) {
                    out += '/';
                } else {
                    out += "x002f";
                } // if else
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
                    out += "xx";
                } else { // x --> x
                    out += c;
                } // if else
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else {
                out += c;
            } // else
        } // for
        
        return out;
    } // encodeHDFS
    
    /**
     * Encodes a string for CKAN. Only lowercase alphanumerics, - and _ are allowed.
     * @param in
     * @return The encoded string
     */
    public static String encodeCKAN(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
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
            } else if (c == '-') { // - --> -
                out += c;
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // encodeCKAN
    
    /**
     * Encodes a string for MySQL.
     * @param in
     * @return The encoded string
     */
    public static String encodeMySQL(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 65 && code <= 90) { // A-Z --> A-Z
                out += c;
            } else if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
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
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // encodeMySQL
    
    /**
     * Encodes a string for a MongoDB database.
     * @param in
     * @return The encoded string
     */
    public static String encodeMongoDBDatabase(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 65 && code <= 90) { // A-Z --> A-Z
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
                    out += "xx";
                } else { // x --> x
                    out += c;
                } // if else
            } else if (c == '/') {
                out += "x002f";
            } else if (c == '\\') {
                out += "x005c";
            } else if (c == '.') {
                out += "x002e";
            } else if (c == '"') {
                out += "x0022";
            } else if (c == '$') {
                out += "x0024";
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else {
                out += c;
            } // else
        } // for
        
        return out;
    } // encodeMongoDBDatabase
    
    /**
     * Encodes a string for a MongoDB collection.
     * @param in
     * @return The encoded string
     */
    public static String encodeMongoDBCollection(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            
            if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
                    out += "xx";
                } else { // x --> x
                    out += c;
                } // if else
            } else if (c == '/') { // it is accepted in collection names, however it is a problem when dumping
                out += "x002f";
            } else if (c == '$') {
                out += "x0024";
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else {
                out += c;
            } // else
        } // for
        
        return out;
    } // encodeMongoDBCollection
    
    /**
     * Encodes a string for DynamoDB.
     * @param in
     * @return The encoded string
     */
    public static String encodeDynamoDB(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 65 && code <= 90) { // A-Z --> A-Z
                out += c;
            } else if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
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
            } else if (c == '-') { // - --> -
                out += c;
            } else if (c == '.') { // . --> .
                out += c;
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // encodeDynamoDB
    
    /**
     * Encodes a string for Hive.
     * @param in
     * @return The encoded string
     */
    public static String encodeHive(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
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
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // encodeHive
    
    /**
     * Encodes a string for Kafka.
     * @param in
     * @return The encoded string
     */
    public static String encodeKafka(String in) {
        String out = "";
        
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            int code = c;
            
            if (code >= 65 && code <= 90) { // A-Z --> A-Z
                out += c;
            } else if (code >= 97 && code <= 119) { // a-w --> a-w
                out += c;
            } else if (c == 'x') {
                String next4;
            
                if (i + 4 < in.length()) {
                    next4 = in.substring(i + 1, i + 5);
                } else {
                    next4 = "WXYZ"; // whatever except a unicode
                } // if else
            
                if (next4.matches("^[0-9a-fA-F]{4}$")) { // x --> xx
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
            } else if (c == '-') { // - --> -
                out += c;
            } else if (c == '.') { // . --> .
                out += c;
            } else if (c == '=') { // = --> xffff
                out += "xffff";
            } else { // --> xUNICODE
                String hex = Integer.toHexString(code);
                out += "x" + ("0000" + hex).substring(hex.length());
            } // else
        } // for
        
        return out;
    } // encodeKafka

} // NGSICharsets
