/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.errors;

/**
 *
 * @author frb
 */
public class CygnusBadContextData extends CygnusError {
    
    /**
     * Constructor.
     * @param cygnusMessage
     */
    public CygnusBadContextData(String cygnusMessage) {
        super("BadContextData", null, cygnusMessage, null);
    } // CygnusBadContextData
    
    /**
     * Constructor.
     * @param cygnusMessage
     * @param javaException
     * @param javaMessage
     */
    public CygnusBadContextData(String cygnusMessage, String javaException, String javaMessage) {
        super("BadContextData", javaException, cygnusMessage, javaMessage);
    } // CygnusBadContextData
    
} // CygnusBadContextData
