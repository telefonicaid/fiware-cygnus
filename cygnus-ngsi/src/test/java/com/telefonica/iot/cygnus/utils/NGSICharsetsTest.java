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
    
    /**
     * [NGSICharsets.cartoDBEncode] -------- Upper case not accented characters are encoded.
     */
    @Test
    public void testCartoDBEncodeUpperCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                + "-------- Upper case not accented characters are not encoded");
        String in = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expected = "abcdefghijklmnopqrstuvwxyz";
        String out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has been encoded as '" + expected + "'");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has not been encoded as '" + expected + "'");
            throw e;
        } // try catch
    } // testCartoDBEncodeUpperChars
    
    /**
     * [NGSICharsets.cartoDBEncode] -------- Lower case not accented characters are not encoded.
     */
    @Test
    public void testCartoDBEncodeLowerCase() {
        System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                + "-------- Lower case not accented characters are not encoded");
        String in = "abcdefghijklmnopqrstuvwxyz";
        String expected = "abcdefghijklmnopqrstuvwxyz";
        String out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testCartoDBEncodeLowerCase
    
    /**
     * [NGSICharsets.cartoDBEncode] -------- Numbers are not encoded.
     */
    @Test
    public void testCartoDBEncodeNums() {
        System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                + "-------- Numbers are not encoded");
        String in = "0123456789";
        String expected = "0123456789";
        String out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testCartoDBEncodeNums
    
    /**
     * [NGSICharsets.cartoDBEncode] -------- Underscore is not encoded.
     */
    @Test
    public void testCartoDBEncodeUnderscore() {
        System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                + "-------- Underscore is not encoded");
        String in = "_";
        String expected = "_";
        String out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testCartoDBEncodeUnderscore
    
    /**
     * [NGSICharsets.cartoDBEncode] -------- Non lower case alphanumerics and undersocre are encoded.
     */
    @Test
    public void testCartoDBEncodeRare() {
        System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                + "-------- Non lower case alphanumerics and undersocre are encoded");
        String in = "ÁÉÍÓÚáéíóúÑñÇç";
        String expected = "x00c1x00c9x00cdx00d3x00dax00e1x00e9x00edx00f3x00fax00d1x00f1x00c7x00e7";
        String out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
        
        in = "!\"#$%&'()*+,-./";
        expected = "x0021x0022x0023x0024x0025x0026x0027x0028x0029x002ax002bx002cx002dx002ex002f";
        out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
        
        in = ":;<=>?@";
        expected = "x003ax003bx003cx003dx003ex003fx0040";
        out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
        
        in = "[\\]^`";
        expected = "x005bx005cx005dx005ex0060";
        out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
        
        in = "{|}~";
        expected = "x007bx007cx007dx007e";
        out = NGSICharsets.cartoDBEncode(in);
        
        try {
            assertEquals(expected, out);
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "-  OK  - '" + in + "' has not been encoded");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSICharsets.cartoDBEncode]")
                    + "- FAIL - '" + in + "' has been encoded as '" + out + "'");
            throw e;
        } // try catch
    } // testCartoDBEncodeRare
    
} // NGSICharsetsTest
