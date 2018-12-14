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

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author frb
 */
public final class CommonUtilsForTests {
    
    private static final int MAX_LEN_TEST_TRACE_HEAD = 60;
    
    /**
     * Constructor. It is private since utility classes should not have a public or default constructor.
     */
    private CommonUtilsForTests() {
    } // CommonUtilsForTests
    
    /**
     * Gets a trace head.
     * @param originalHead
     * @return A trace head
     */
    public static String getTestTraceHead(String originalHead) {
        String traceHead = originalHead;
        traceHead += " " + StringUtils.repeat("-", MAX_LEN_TEST_TRACE_HEAD - originalHead.length());
        return traceHead;
    } // getTestTraceHead
    
} // CommonUtilsForTests
