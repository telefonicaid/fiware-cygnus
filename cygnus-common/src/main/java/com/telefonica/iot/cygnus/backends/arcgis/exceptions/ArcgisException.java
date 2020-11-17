/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.arcgis.exceptions;

/**
 * @author dmartinez
 *
 */
public class ArcgisException extends Exception {
    private static final int DEFAULT_RESULT_CODE = 500;
    protected int code;

    /**
     * 
     * @param message
     */
    public ArcgisException(String message) {
        super(message);
        code = DEFAULT_RESULT_CODE;
    }

    /**
     * 
     * @param e
     */
    public ArcgisException(Exception e) {
        super(e);
        code = DEFAULT_RESULT_CODE;
    }

    /**
     * 
     * @param code
     * @param message
     */
    public ArcgisException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 
     * @return code
     */
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        String result = "";
        if (code > 0) {
            result = code + " ";
        }
        return result + this.getMessage();
    }

}
