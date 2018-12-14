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

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSICharsetsTest {
    
    /**
     * Constructor.
     */
    public NGSICharsetsTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSICharsetsTest
    
    // ---------- PostgreSQL ---------
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- Upper case not accented characters are encoded.
     */
    @Test
    public void testEncodePostgreSQLUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- Upper case not accented characters are encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "x0041x0042x0043x0044x0045x0046x0047x0048x0049x004ax004bx004cx004dx004ex004f"
                + "x0050x0051x0052x0053x0054x0055x0056x0057x0058x0059x005a";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLUpperCase
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodePostgreSQLLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLLowerCase
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodePostgreSQLNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLNums
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- Underscore is not encoded.
     */
    @Test
    public void testEncodePostgreSQLUnderscore() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- Underscore is not encoded");
        String in = "_";
        String expected = "_";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLUnderscore
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- '=' (internal concatenator) is encoded as "xffff" (public concatenator).
     */
    @Test
    public void testEncodePostgreSQLEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLEquals
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodePostgreSQLSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLSinglex
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodePostgreSQLxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodePostgreSQLxffff
    
    /**
     * [NGSICharsets.encodePostgreSQL] -------- Non lower case alphanumerics nor undersocre are encoded.
     */
    @Test
    public void testEncodePostgreSQLRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                + "-------- Non lower case alphanumerics and undersocre are encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç";
        String expected = "x00c1x00c9x00cdx00d3x00dax00e1x00e9x00edx00f3x00fax00d1x00f1x00c7x00e7";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "!\"#$%&'()*+,-./";
        expected = "x0021x0022x0023x0024x0025x0026x0027x0028x0029x002ax002bx002cx002dx002ex002f";
        out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        // '=' has been excluded from here since it is not notified by Orion and it is used as concatenator
        in = ":;<>?@";
        expected = "x003ax003bx003cx003ex003fx0040";
        out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "[\\]^`";
        expected = "x005bx005cx005dx005ex0060";
        out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "{|}~";
        expected = "x007bx007cx007dx007e";
        out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodePostgreSQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
    } // testEncodePostgreSQLRare
    
    // ---------- CKAN ---------
    
    /**
     * [NGSICharsets.encodeCKAN] -------- Upper case not accented characters are encoded.
     */
    @Test
    public void testEncodeCKANUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- Upper case not accented characters are encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "x0041x0042x0043x0044x0045x0046x0047x0048x0049x004ax004bx004cx004dx004ex004f"
                + "x0050x0051x0052x0053x0054x0055x0056x0057x0058x0059x005a";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeCKANUpperCase
    
    /**
     * [NGSICharsets.encodeCKAN] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeCKANLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeCKANLowerCase
    
    /**
     * [NGSICharsets.encodeCKAN] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeCKANNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeCKANNums
    
    /**
     * [NGSICharsets.encodeCKAN] -------- Underscore is not encoded.
     */
    @Test
    public void testEncodeCKANUnderscore() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- Underscore is not encoded");
        String in = "_";
        String expected = "_";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeCKANUnderscore
    
    /**
     * [NGSICharsets.encodeCKAN] -------- Hyphen is not encoded.
     */
    @Test
    public void testEncodeCKANHyphen() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- Hyphen is not encoded");
        String in = "-";
        String expected = "-";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeCKANHyphen
    
    /**
     * [NGSICharsets.encodeCKAN] -------- '=' (internal concatenator) is encoded as "xffff" (public concatenator).
     */
    @Test
    public void testEncodeCKANEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeCKANEquals
    
    /**
     * [NGSICharsets.encodeCKAN] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeCKANSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeCKANSinglex
    
    /**
     * [NGSICharsets.encodeCKAN] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeCKANxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeCKANxffff
    
    /**
     * [NGSICharsets.encodeCKAN] -------- Non lower case alphanumerics nor undersocre nor hyphen are encoded.
     */
    @Test
    public void testEncodeCKANRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                + "-------- Non lower case alphanumerics and undersocre are encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç";
        String expected = "x00c1x00c9x00cdx00d3x00dax00e1x00e9x00edx00f3x00fax00d1x00f1x00c7x00e7";
        String out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "!\"#$%&'()*+,./";
        expected = "x0021x0022x0023x0024x0025x0026x0027x0028x0029x002ax002bx002cx002ex002f";
        out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        // '=' has been excluded from here since it is not notified by Orion and it is used as concatenator
        in = ":;<>?@";
        expected = "x003ax003bx003cx003ex003fx0040";
        out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "[\\]^`";
        expected = "x005bx005cx005dx005ex0060";
        out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "{|}~";
        expected = "x007bx007cx007dx007e";
        out = NGSICharsets.encodeCKAN(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeCKAN]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
    } // testEncodePostgreSQLRare
    
    // ---------- HDFS ---------
    
    /**
     * [NGSICharsets.encodeHDFS] -------- Upper case not accented characters are not encoded.
     */
    @Test
    public void testEncodeHDFSUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- Upper case not accented characters are not encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String out = NGSICharsets.encodeHDFS(in, false);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSUpperCase
    
    /**
     * [NGSICharsets.encodeHDFS] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeHDFSLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeHDFS(in, false);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSLowerCase
    
    /**
     * [NGSICharsets.encodeHDFS] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeHDFSNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeHDFS(in, false);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSNums
    
    /**
     * [NGSICharsets.encodeHDFS] -------- '=' (internal concatenator) is encoded as "xffff" (public concatenator).
     */
    @Test
    public void testEncodeHDFSEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSEquals
    
    /**
     * [NGSICharsets.encodeHDFS] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeHDFSSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeHDFS(in, false);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSSinglex
    
    /**
     * [NGSICharsets.encodeHDFS] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeHDFSxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeHDFS(in, false);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSxffff
    
    /**
     * [NGSICharsets.encodeHDFS] -------- Rare characters are not encoded.
     */
    @Test
    public void testEncodeHDFSRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                + "-------- Rare characters are not encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç!\"#$%&'()*+,.-_:;<>?@[\\]^`{|}~";
        String expected = "ÁÉÍÓÚáéíóúÑñÇç!\"#$%&'()*+,.-_:;<>?@[\\]^`{|}~";
        String out = NGSICharsets.encodeHDFS(in, false);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeHDFS]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeHDFSRare
    
    // ---------- MySQL ---------
    
    /**
     * [NGSICharsets.encodeMySQL] -------- Upper case not accented characters are not encoded.
     */
    @Test
    public void testEncodeMySQLUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- Upper case not accented characters are not encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLUpperCase
    
    /**
     * [NGSICharsets.encodeMySQL] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeMySQLLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLLowerCase
    
    /**
     * [NGSICharsets.encodeMySQL] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeMySQLNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLNums
    
    /**
     * [NGSICharsets.encodeMySQL] -------- Underscore is not encoded.
     */
    @Test
    public void testEncodeMySQLUnderscore() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- Underscore is not encoded");
        String in = "_";
        String expected = "_";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLUnderscore
    
    /**
     * [NGSICharsets.encodeMySQL] -------- '=' (internal concatenator) is encoded as "xffff" (public concatenator).
     */
    @Test
    public void testEncodeMySQLEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodePostgreSQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLEquals
    
    /**
     * [NGSICharsets.encodeMySQL] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeMySQLSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLSinglex
    
    /**
     * [NGSICharsets.encodeMySQL] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeMySQLxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMySQLxffff
    
    /**
     * [NGSICharsets.encodeMySQL] -------- Non lower case alphanumerics nor undersocre are encoded.
     */
    @Test
    public void testEncodeMySQLRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                + "-------- Non lower case alphanumerics and undersocre are encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç";
        String expected = "x00c1x00c9x00cdx00d3x00dax00e1x00e9x00edx00f3x00fax00d1x00f1x00c7x00e7";
        String out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "!\"#$%&'()*+,-./";
        expected = "x0021x0022x0023x0024x0025x0026x0027x0028x0029x002ax002bx002cx002dx002ex002f";
        out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        // '=' has been excluded from here since it is not notified by Orion and it is used as concatenator
        in = ":;<>?@";
        expected = "x003ax003bx003cx003ex003fx0040";
        out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "[\\]^`";
        expected = "x005bx005cx005dx005ex0060";
        out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "{|}~";
        expected = "x007bx007cx007dx007e";
        out = NGSICharsets.encodeMySQL(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMySQL]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
    } // testEncodeMySQLRare
    
    // ---------- MongoDB database ----------
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- Upper case not accented characters are encoded.
     */
    @Test
    public void testEncodeMongoDBDatabaseUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- Upper case not accented characters are encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "x0041x0042x0043x0044x0045x0046x0047x0048x0049x004ax004bx004cx004dx004ex004f"
                + "x0050x0051x0052x0053x0054x0055x0056x0057x0058x0059x005a";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabaseUpperCase
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeMongoDBDatabasesLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabasesLowerCase
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeMongoDBDatabaseNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabaseNums
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- '=' (internal concatenator) is encoded as "xffff" (public
     * concatenator).
     */
    @Test
    public void testEncodeMongoDBDatabaseEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabaseEquals
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeMongoDBDatabaseSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabaseSinglex
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeMongoDBDatabasexffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabasexffff
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- Forbidden characters are encoded.
     */
    @Test
    public void testEncodeMongoDBDatabaseForbiddenCharacters() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- Forbidden characters are encoded");
        String in = "/\\.\"$";
        String expected = "x002fx005cx002ex0022x0024";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabaseForbiddenCharacters
    
    /**
     * [NGSICharsets.encodeMongoDBDatabase] -------- Rare characters are not encoded.
     */
    @Test
    public void testEncodeMongoDBDatabaseRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                + "-------- Rare characters are not encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç!#%&'()*+,-_:;<>?@[]^`{|}~";
        String expected = "ÁÉÍÓÚáéíóúÑñÇç!#%&'()*+,-_:;<>?@[]^`{|}~";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBDatabase]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBDatabaseRare
    
    // ---------- MongoDB collection ----------
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- Upper case not accented characters are not encoded.
     */
    @Test
    public void testEncodeMongoDBCollectionUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- Upper case not accented characters are not encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionUpperCase
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeMongoDBCollectionLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionLowerCase
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeMongoDBCollectionNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionNums
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- '=' (internal concatenator) is encoded as "xffff" (public
     * concatenator).
     */
    @Test
    public void testEncodeMongoDBCollectionEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionEquals
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeMongoDBCollectionSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionSinglex
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeMongoDBCollectionxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionxffff
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- Forbidden characters are encoded.
     */
    @Test
    public void testEncodeMongoDBCollectionForbiddenCharacters() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- Forbidden characters are encoded");
        String in = "/$";
        String expected = "x002fx0024";
        String out = NGSICharsets.encodeMongoDBDatabase(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionForbiddenCharacters
    
    /**
     * [NGSICharsets.encodeMongoDBCollection] -------- Rare characters are not encoded.
     */
    @Test
    public void testEncodeMongoDBCollectionRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                + "-------- Rare characters are not encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç!#%&'()*+.,-_:;<>?@[\\]^`{|}~";
        String expected = "ÁÉÍÓÚáéíóúÑñÇç!#%&'()*+.,-_:;<>?@[\\]^`{|}~";
        String out = NGSICharsets.encodeMongoDBCollection(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeMongoDBCollection]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeMongoDBCollectionRare
    
    // ---------- DynamoDB ----------
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Upper case not accented characters are not encoded.
     */
    @Test
    public void testEncodeDynamoDBUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Upper case not accented characters are not encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBUpperCase
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeDynamoDBLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBLowerCase
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeDynamoDBNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBNums
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Underscore is not encoded.
     */
    @Test
    public void testEncodeDynamoDBUnderscore() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Underscore is not encoded");
        String in = "_";
        String expected = "_";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBUnderscore
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Hyphen is not encoded.
     */
    @Test
    public void testEncodeDynamoDBHyphen() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Hyphen is not encoded");
        String in = "-";
        String expected = "-";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBHyphen
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Dot is not encoded.
     */
    @Test
    public void testEncodeDynamoDBDot() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Dot is not encoded");
        String in = ".";
        String expected = ".";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBDot
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- '=' (internal concatenator) is encoded as "xffff" (public concatenator).
     */
    @Test
    public void testEncodeDynamoDBEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBEquals
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeDynamoDBSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBSinglex
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeDynamoDBxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeDynamoDBxffff
    
    /**
     * [NGSICharsets.encodeDynamoDB] -------- Non lower case alphanumerics nor underscore nor hyphen nor dot are
     * encoded.
     */
    @Test
    public void testEncodeDynamoDBRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                + "-------- Non lower case alphanumerics and underscore are encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç";
        String expected = "x00c1x00c9x00cdx00d3x00dax00e1x00e9x00edx00f3x00fax00d1x00f1x00c7x00e7";
        String out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "!\"#$%&'()*+,/";
        expected = "x0021x0022x0023x0024x0025x0026x0027x0028x0029x002ax002bx002cx002f";
        out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        // '=' has been excluded from here since it is not notified by Orion and it is used as concatenator
        in = ":;<>?@";
        expected = "x003ax003bx003cx003ex003fx0040";
        out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "[\\]^`";
        expected = "x005bx005cx005dx005ex0060";
        out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "{|}~";
        expected = "x007bx007cx007dx007e";
        out = NGSICharsets.encodeDynamoDB(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeDynamoDB]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
    } // testEncodeDynamoDBRare
    
    // ---------- Kafka ----------
    
    /**
     * [NGSICharsets.encodeKafka] -------- Upper case not accented characters are not encoded.
     */
    @Test
    public void testEncodeKafkaUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Upper case not accented characters are not encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaUpperCase
    
    /**
     * [NGSICharsets.encodeKafka] -------- Lower case not accented characters except 'x' are not encoded.
     */
    @Test
    public void testEncodeKafkaLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwyz";
        String expected = "abcdefghijklmnopqrstuvwyz";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaLowerCase
    
    /**
     * [NGSICharsets.encodeKafka] -------- Numbers are not encoded.
     */
    @Test
    public void testEncodeKafkaBNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaNums
    
    /**
     * [NGSICharsets.encodeKafka] -------- Underscore is not encoded.
     */
    @Test
    public void testEncodeKafkaUnderscore() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Underscore is not encoded");
        String in = "_";
        String expected = "_";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaUnderscore
    
    /**
     * [NGSICharsets.encodeKafka] -------- Hyphen is not encoded.
     */
    @Test
    public void testEncodeKafkaHyphen() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Hyphen is not encoded");
        String in = "-";
        String expected = "-";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaHyphen
    
    /**
     * [NGSICharsets.encodeKafka] -------- Dot is not encoded.
     */
    @Test
    public void testEncodeKafkaDot() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Dot is not encoded");
        String in = ".";
        String expected = ".";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaDot
    
    /**
     * [NGSICharsets.encodeKafka] -------- '=' (internal concatenator) is encoded as "xffff" (public concatenator).
     */
    @Test
    public void testEncodeKafkaEquals() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- '=' (internal concatenator) is encoded as \"xffff\" (public concatenator)");
        String in = "=";
        String expected = "xffff";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaEquals
    
    /**
     * [NGSICharsets.encodeKafka] -------- A single 'x' is not encoded.
     */
    @Test
    public void testEncodeKafkaSinglex() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- A single 'x' is not encoded");
        String in = "x";
        String expected = "x";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaSinglex
    
    /**
     * [NGSICharsets.encodeKafka] -------- "xffff" is encoded (escaped) as "xxffff".
     */
    @Test
    public void testEncodeKafkaxffff() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- \"xffff\" is encoded (escaped) as \"xxffff\"");
        String in = "xffff";
        String expected = "xxffff";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "' instead of '" + expected + "'");
            throw e;
        } // try catch
    } // testEncodeKafkaxffff
    
    /**
     * [NGSICharsets.encodeKafka] -------- Non lower case alphanumerics nor underscore nor hyphen nor dot are
     * encoded.
     */
    @Test
    public void testEncodeKafkaRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                + "-------- Non lower case alphanumerics and underscore are encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç";
        String expected = "x00c1x00c9x00cdx00d3x00dax00e1x00e9x00edx00f3x00fax00d1x00f1x00c7x00e7";
        String out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "!\"#$%&'()*+,/";
        expected = "x0021x0022x0023x0024x0025x0026x0027x0028x0029x002ax002bx002cx002f";
        out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        // '=' has been excluded from here since it is not notified by Orion and it is used as concatenator
        in = ":;<>?@";
        expected = "x003ax003bx003cx003ex003fx0040";
        out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "[\\]^`";
        expected = "x005bx005cx005dx005ex0060";
        out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
        
        in = "{|}~";
        expected = "x007bx007cx007dx007e";
        out = NGSICharsets.encodeKafka(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "-  OK  - '" + in + "' has been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.encodeKafka]")
                    + "- FAIL - '" + in + "' has not been encoded");
            throw e;
        } // try catch
    } // testEncodeKafkaRare
    
} // NGSICharsetsTest
