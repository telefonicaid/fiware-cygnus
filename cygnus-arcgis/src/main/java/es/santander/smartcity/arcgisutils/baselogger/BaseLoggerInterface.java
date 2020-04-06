/**
 * 
 */
package es.santander.smartcity.arcgisutils.baselogger;

/**
 * @author jcamus
 *
 */
public interface BaseLoggerInterface {

    /**
     * 
     * @param message
     */
    public void logBasic(String message);

    /**
     * 
     * @param message
     */
    public void logDebug(String message);

    /**
     * 
     * @param message
     */
    public void logTrace(String message);

    /**
     * 
     * @param message
     */
    public void logError(String message);

}
