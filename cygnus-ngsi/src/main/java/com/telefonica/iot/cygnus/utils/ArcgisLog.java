package com.telefonica.iot.cygnus.utils;

import com.telefonica.iot.cygnus.log.CygnusLogger;

import es.santander.smartcity.arcgisutils.Arcgis;
import es.santander.smartcity.arcgisutils.exception.ArcGisException;

/**
 * 
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 *
 */
public final class ArcgisLog extends Arcgis {

    private static CygnusLogger cygnusLogger;

    /**
     * 
     * @param url
     * @param user
     * @param password
     */
    private ArcgisLog(String url, String user, String password)throws ArcGisException  {
        super(url, user, password);
        logDebug("init ArcgisLog");
    }

    /**
     * 
     * @param cygnusLogger
     * @param url
     * @param user
     * @param password
     * @return
     */
    public static ArcgisLog getInstance(CygnusLogger cygnusLogger, String url,
            String user, String password)throws ArcGisException  {
        cygnusLogger.debug("init cygnusLog");
        ArcgisLog.cygnusLogger = cygnusLogger;
        cygnusLogger.debug("inited cygnusLog");
        cygnusLogger.debug("init ArcgisLog(url --> " + url + ", user --> " + user + ", password --> XXXXX)");
        return new ArcgisLog(url, user, password);
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.utils.Arcgis#logBasic(java.lang.String)
     */
    @Override
    public void logBasic(String message) {
        super.logBasic(message);
        cygnusLogger.info(this.getClass().getName() + ": " + message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.utils.Arcgis#logDebug(java.lang.String)
     */
    @Override
    public void logDebug(String message) {
        super.logDebug(message);
        cygnusLogger.debug(this.getClass().getName() + ": " + message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.santander.smartcity.utils.Arcgis#logError(java.lang.String)
     */
    @Override
    public void logError(String message) {
        super.logError(message);
        if (this != null && this.getClass() != null && this.getClass().getName() != null) {
            cygnusLogger.error(this.getClass().getName() + ": " + message);
        }
        cygnusLogger.error("Error : " + message);
    }

}
