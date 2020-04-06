/**
 * 
 */
package com.telefonica.iot.cygnus.backends.arcgis.http;

/**
 * @author dmartinez
 *
 */
public enum HttpMethod {
    GET("GET"), POST("POST");

    private String stringValue;

    /**
     * 
     * @param stringValue
     */
    HttpMethod(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Convertir Enum a String.
     */
    public String toString() {
        return this.stringValue;
    }
}
