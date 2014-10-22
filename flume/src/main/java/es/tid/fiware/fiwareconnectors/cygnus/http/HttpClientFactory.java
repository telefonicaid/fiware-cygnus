/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.http;

import es.tid.fiware.fiwareconnectors.cygnus.utils.Constants;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author frb
 * 
 * This is a convenience class in order the clients do not have to import stuff from org.apache.http and deal with its
 * details. It implements a Http client factory.
 * 
 */
public class HttpClientFactory {
    
    private static Logger logger;
    private static PoolingClientConnectionManager connectionsManager;
    private static PoolingClientConnectionManager sslConnectionsManager;
   
    /**
     * Constructor.
     * @param ssl True if SSL connections are desired. False otherwise.
     */
    public HttpClientFactory(boolean ssl) {
        // create the logger
        logger = Logger.getLogger(HttpClientFactory.class);
        
        if (ssl) {
            sslConnectionsManager = new PoolingClientConnectionManager(getSchemeRegistry());
            sslConnectionsManager.setMaxTotal(Constants.MAX_CONNS);
            sslConnectionsManager.setDefaultMaxPerRoute(Constants.MAX_CONNS_PER_ROUTE);
        } else {
            connectionsManager = new PoolingClientConnectionManager();
            connectionsManager.setMaxTotal(Constants.MAX_CONNS);
            connectionsManager.setDefaultMaxPerRoute(Constants.MAX_CONNS_PER_ROUTE);
        } // if else
        
        logger.info("Setting max total connections (" + Constants.MAX_CONNS + ")");
        logger.info("Settubg default max connections per route (" + Constants.MAX_CONNS_PER_ROUTE + ")");
    } // HttpClientFactory
    
    /**
     * Gets a HTTP client.
     * @param ssl True if SSL connections are desired. False otherwise.
     * @return A http client obtained from the (SSL) Connections Manager.
     */
    public DefaultHttpClient getHttpClient(boolean ssl) {
        if (ssl) {
            return new DefaultHttpClient(sslConnectionsManager);
        } else {
            return new DefaultHttpClient(connectionsManager);
        } // if else
    } // getHttpClient
    
    /**
     * Gets the number of leased connections for this connections manager.
     * @param ssl
     * @return
     */
    public int getLeasedConnections(boolean ssl) {
        if (ssl) {
            return sslConnectionsManager.getTotalStats().getLeased();
        } else {
            return connectionsManager.getTotalStats().getLeased();
        } // if else
    } // getLeasedConnections
    
    /**
     * Gets a SchemeRegistry object accepting all the X509 certificates by default.
     * @return A SchemeRegistry object.
     */
    private SchemeRegistry getSchemeRegistry() {
        // http://stackoverflow.com/questions/2703161/how-to-ignore-ssl-certificate-errors-in-apache-httpclient-4-0
        
        SSLContext sslContext = null;
        
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            logger.fatal("Fatal error (SSL cannot be used, no such algorithm. Details=" + e.getMessage() + ")");
            return null;
        } // try catch
        
        try {
            // set up a TrustManager that trusts everything
            sslContext.init(null, new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    } // getAcceptedIssuers

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    } // getAcceptedIssuers

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    } // checkServerTrusted
                }
            }, new SecureRandom());
        } catch (KeyManagementException e) {
            logger.fatal("Fatal error (Cannot ignore SSL certificates. Details=" + e.getMessage() + ")");
            return null;
        } // try catch

        if (sslContext == null) {
            logger.fatal("Fatal error (Cannot ignore SSL certificates, SSL context is null)");
            return null;
        } // if
        
        SSLSocketFactory sf = new SSLSocketFactory(sslContext);
        Scheme httpsScheme = new Scheme("https", 443, sf);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);
        return schemeRegistry;
    } // getSchemeRegistry
    
} // HttpClientFactory