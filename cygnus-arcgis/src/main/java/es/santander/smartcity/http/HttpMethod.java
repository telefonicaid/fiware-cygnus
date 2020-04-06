/**
 * 
 */
package es.santander.smartcity.http;

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
