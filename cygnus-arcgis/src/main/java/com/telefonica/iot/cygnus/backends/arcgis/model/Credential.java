/**
 * 
 */
package com.telefonica.iot.cygnus.backends.arcgis.model;

import java.time.LocalDateTime;

import org.apache.log4j.Logger;

import com.telefonica.iot.cygnus.backends.arcgis.baselogger.BaseLogger;

/**
 * @author dmartinez
 *
 */
public abstract class Credential extends BaseLogger {
    static final Logger LOGGER = Logger.getLogger(Credential.class);
    private String token;
    private LocalDateTime expirationTime;

    /**
     * Default constructor.
     */
    public Credential() {
        token = "";
        expirationTime = LocalDateTime.now().minusMinutes(1);
    }

    /**
     * @param token
     */
    public Credential(String token, LocalDateTime expirationTime) {
        super();
        setToken(token, expirationTime);
    }

    /**
     * @param token
     */
    public Credential(String token, int expirationMillis) {
        super();
        this.setTokenMillis(token, expirationMillis);
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * token the token to set.
     * 
     * @param token
     * @param expirationTime
     */
    public void setToken(String token, LocalDateTime expirationTime) {
        this.token = token;
        this.expirationTime = expirationTime;
    }

    /**
     * 
     * @param token
     * @param expirationMillis
     */
    public void setTokenMillis(String token, int expirationMillis) {
        LocalDateTime expiration = LocalDateTime.now().plusNanos(expirationMillis * 1000000);
        this.setToken(token, expiration);
    }

    /**
     * token the token to set.
     * 
     * @param token
     * @param expirationMins
     */
    public void setToken(String token, int expirationMins) {
        this.token = token;
        this.expirationTime = LocalDateTime.now().plusMinutes(expirationMins - 1);
    }

    /**
     * ¿ha caducado la credencial?
     * 
     * @return
     */
    public boolean isExpired() {
        logDebug("Checking Credential expiration time: " + expirationTime);
        return LocalDateTime.now().isAfter(expirationTime) || "".equals(token);
    }

    /**
     * @return the expirationTime
     */
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

}
