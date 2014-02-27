package es.tid.fiware.orionconnectors.cosmosinjector.http;

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
            sslConnectionsManager.setMaxTotal(500);
            sslConnectionsManager.setDefaultMaxPerRoute(100);
        } else {
            connectionsManager = new PoolingClientConnectionManager();
            connectionsManager.setMaxTotal(500);
            connectionsManager.setDefaultMaxPerRoute(100);
        } // if else
        
        logger.info("Setting max total connections to 500 and default"
                + " max connections per route to 100");
    } // HttpClientFactory
    
    /**
     * 
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
     * Gets a SchemeRegistry object accepting all the X509 certificates by default.
     * @return A SchemeRegistry object.
     */
    private SchemeRegistry getSchemeRegistry() {
        // http://stackoverflow.com/questions/2703161/how-to-ignore-ssl-certificate-errors-in-apache-httpclient-4-0
        
        SSLContext sslContext = null;
        
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            logger.fatal("SSL cannot be used. Reason: NoSuchAlgorithmException");
            logger.fatal(e.getMessage());
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
            logger.fatal("Cannot ignore SSL certificates. Reason: KeyManagementException");
            logger.fatal(e.getMessage());
            return null;
        } // try catch

        if (sslContext == null) {
            logger.fatal("Cannot ignore SSL certificates. Reason: sslContext == null");
            return null;
        } // if
        
        SSLSocketFactory sf = new SSLSocketFactory(sslContext);
        Scheme httpsScheme = new Scheme("https", 443, sf);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);
        return schemeRegistry;
    } // getSchemeRegistry
    
} // HttpClientFactory
