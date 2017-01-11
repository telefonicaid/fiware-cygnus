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

package com.telefonica.iot.cygnus.backends.http;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * 
 * @author frb
 * 
 * This is a convenience class in order the clients do not have to import stuff from org.apache.http and deal with its
 * details. It implements a Http client factory.
 * 
 */
public class HttpClientFactory {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(HttpClientFactory.class);
    private final String loginConfFile;
    private final String krb5ConfFile;
    private static PoolingClientConnectionManager connectionsManager;
    private static PoolingClientConnectionManager sslConnectionsManager;
   
    /**
     * Constructor.
     * @param ssl True if SSL connections are desired. False otherwise.
     * @param loginConfFile
     * @param krb5ConfFile
     * @param maxConns
     * @param maxConnsPerRoute
     */
    public HttpClientFactory(boolean ssl, String loginConfFile, String krb5ConfFile, int maxConns,
            int maxConnsPerRoute) {
        // set the Kerberos parameters
        this.loginConfFile = loginConfFile;
        this.krb5ConfFile = krb5ConfFile;
        
        // create the appropriate connections manager
        if (ssl) {
            sslConnectionsManager = new PoolingClientConnectionManager(getSSLSchemeRegistry());
            sslConnectionsManager.setMaxTotal(maxConns);
            sslConnectionsManager.setDefaultMaxPerRoute(maxConnsPerRoute);
        } else {
            connectionsManager = new PoolingClientConnectionManager();
            connectionsManager.setMaxTotal(maxConns);
            connectionsManager.setDefaultMaxPerRoute(maxConnsPerRoute);
        } // if else
        
        LOGGER.info("Setting max total connections (" + maxConns + ")");
        LOGGER.info("Setting default max connections per route (" + maxConnsPerRoute + ")");
    } // HttpClientFactory
    
    /**
     * Gets a HTTP client.
     * @param ssl True if SSL connections are desired. False otherwise
     * @param krb5Auth.
     * @return A http client obtained from the (SSL) Connections Manager.
     */
    public DefaultHttpClient getHttpClient(boolean ssl, boolean krb5Auth) {
        DefaultHttpClient httpClient;
        
        if (ssl) {
            httpClient = new DefaultHttpClient(sslConnectionsManager);
        } else {
            httpClient = new DefaultHttpClient(connectionsManager);
        } // if else
        
        if (krb5Auth) {
            // http://stackoverflow.com/questions/21629132/httpclient-set-credentials-for-kerberos-authentication
            
            System.setProperty("java.security.auth.login.config", loginConfFile);
            System.setProperty("java.security.krb5.conf", krb5ConfFile);
            System.setProperty("sun.security.krb5.debug", "false");
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
            Credentials jaasCredentials = new Credentials() {
                
                @Override
                public String getPassword() {
                    return null;
                } // getPassword

                @Override
                public Principal getUserPrincipal() {
                    return null;
                } // getUserPrincipal
                
            };

            // 'true' means the port is stripped from the principal names
            SPNegoSchemeFactory spnegoSchemeFactory = new SPNegoSchemeFactory(true);
            httpClient.getAuthSchemes().register(AuthPolicy.SPNEGO, spnegoSchemeFactory);
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null), jaasCredentials);
        } // if
        
        return httpClient;
    } // getHttpClient
    
    /**
     * Gets the number of leased connections for this connections manager. This is not really used within the code, but
     * could be used for debugging purposes.
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
     * Gets a SSL SchemeRegistry object accepting all the X509 certificates by default.
     * @return A SSL SchemeRegistry object.
     */
    private SchemeRegistry getSSLSchemeRegistry() {
        // http://stackoverflow.com/questions/2703161/how-to-ignore-ssl-certificate-errors-in-apache-httpclient-4-0
        
        SSLContext sslContext;
        
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.fatal("Fatal error (SSL cannot be used, no such algorithm. Details=" + e.getMessage() + ")");
            return null;
        } // try catch // try catch
        
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
            LOGGER.fatal("Fatal error (Cannot ignore SSL certificates. Details=" + e.getMessage() + ")");
            return null;
        } // try catch // try catch

        if (sslContext == null) {
            LOGGER.fatal("Fatal error (Cannot ignore SSL certificates, SSL context is null)");
            return null;
        } // if

        SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme httpsScheme = new Scheme("https", 443, sf);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);
        return schemeRegistry;
    } // getSSLSchemeRegistry

} // HttpClientFactory
