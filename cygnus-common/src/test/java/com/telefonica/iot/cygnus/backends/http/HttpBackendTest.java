/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author pcoello25
 */

@RunWith(MockitoJUnitRunner.class)
public class HttpBackendTest {
    
    /**
     * This class is used to test once and only once the common functionality shared by all the real extending sinks.
     */
    private class HttpBackendImpl extends HttpBackend {

        public HttpBackendImpl(String[] hosts, String port, boolean ssl, boolean krb5, String krb5User, String krb5Password, String krb5LoginConfFile, String krb5ConfFile) {
            super(hosts, port, ssl, krb5, krb5User, krb5Password, krb5LoginConfFile, krb5ConfFile);
        } // HttpBackendImpl
   
    } // HttpBackendImpl
    
    // instance to be tested
    private HttpBackend httpBackend;
    
    private final HttpResponse mockResponse = mock(HttpResponse.class);
    private final HttpResponse mockArrayResponse = mock(HttpResponse.class);
    private final HttpRequestBase mockRequest = mock(HttpRequestBase.class);
    private final JsonResponse mockJsonResponse = mock(JsonResponse.class);
    
    private ArrayList<Header> headers = new ArrayList<Header>();
    private StringEntity normalEntity;
    private StringEntity arrayEntity;
    private String normalURL;
    private String arrayURL;
    private String[] host;    
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        
        String normalResponse = "{\"id\":\"5432502b1860a3873d395e95\",\"expires\":\"2016-09-25T08:17:47.00Z\",\"status\":\"active\",\"subject\":{\"entities\":[{\"id\":\"\",\"idPattern\":\"Sevilla:FUENTES\",\"type\":\"sevilla:fountain\"}],\"condition\":{\"attrs\":[\"TimeInstant\"]}},\"notification\":{\"timesSent\":8122767998226748692,\"lastNotification\":\"2015-07-28T22:24:33.00Z\",\"attrs\":[],\"http\":{\"url\":\"http://130.206.82.120:1026/ngsi10/notifyContext\"}}}";
        String arrayResponse = "[{\"notification\":{\"lastNotification\":\"2015-07-28T22:23:30.00Z\",\"timesSent\":7126568376946155044,\"http\":{\"url\":\"http:\\/\\/130.206.82.120:1026\\/ngsi10\\/notifyContext\"},\"attrs\":[]},\"expires\":\"2016-09-13T09:27:15.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"chlorine\"]},\"entities\":[{\"id\":\"\",\"type\":\"sevilla:fountain\",\"idPattern\":\"Sevilla:FUENTES:\"}]},\"id\":\"54228e731860a3873d395d66\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-07-28T22:24:33.00Z\",\"timesSent\":8122767998226748692,\"http\":{\"url\":\"http:\\/\\/130.206.82.120:1026\\/ngsi10\\/notifyContext\"},\"attrs\":[]},\"expires\":\"2016-09-25T08:17:47.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"TimeInstant\"]},\"entities\":[{\"id\":\"\",\"type\":\"sevilla:fountain\",\"idPattern\":\"Sevilla:FUENTES\"}]},\"id\":\"5432502b1860a3873d395e95\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-07-28T22:24:53.00Z\",\"timesSent\":8786703474088345564,\"http\":{\"url\":\"http:\\/\\/130.206.83.12:1026\\/ngsi10\\/notifyContext\"},\"attrs\":[]},\"expires\":\"2035-01-06T09:11:45.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"TimeInstant\"]},\"entities\":[{\"id\":\"\",\"type\":\"sevilla:fountain\",\"idPattern\":\"Sevilla:FUENTES\"}]},\"id\":\"553614511860a3f429e3fd80\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-07-28T22:25:01.00Z\",\"timesSent\":8785875184958230546,\"http\":{\"url\":\"http:\\/\\/130.206.83.12:1026\\/ngsi10\\/notifyContext\"},\"attrs\":[]},\"expires\":\"2035-01-06T09:27:18.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"TimeInstant\",\"longitude\",\"latitude\"]},\"entities\":[{\"id\":\"\",\"type\":\"sevilla:fountain\",\"idPattern\":\"Sevilla:FUENTES:\"}]},\"id\":\"553617f61860a3f429e3fd85\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2016-04-11T13:24:19.00Z\",\"timesSent\":7918500433202332193,\"http\":{\"url\":\"http:\\/\\/52.16.174.229:2700\"},\"attrs\":[]},\"expires\":\"2016-07-11T21:33:03.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"sound\"]},\"entities\":[{\"id\":\"\",\"type\":\"santander:sound\",\"idPattern\":\"urn:smartsantander:testbed:.*\"}]},\"id\":\"55a2dd0f9a3bb06493b38fef\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2016-04-11T13:24:19.00Z\",\"timesSent\":8793806529804560773,\"http\":{\"url\":\"http:\\/\\/52.16.174.229:2735\\/sound\"},\"attrs\":[]},\"expires\":\"2016-07-27T08:43:13.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"sound\"]},\"entities\":[{\"id\":\"\",\"type\":\"santander:sound\",\"idPattern\":\"urn:smartsantander:testbed:.*\"}]},\"id\":\"55b740a1760b7a367c9c7bd1\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2016-04-11T13:24:19.00Z\",\"timesSent\":8793414191782978685,\"http\":{\"url\":\"http:\\/\\/lmctmlgw7thh.runscope.net\\/sound\"},\"attrs\":[]},\"expires\":\"2016-07-27T08:46:15.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"sound\"]},\"entities\":[{\"id\":\"\",\"type\":\"santander:sound\",\"idPattern\":\"urn:smartsantander:testbed:.*\"}]},\"id\":\"55b74157760b7a367c9c7bd2\",\"status\":\"active\"},{\"notification\":{\"http\":{\"url\":\"http:\\/\\/130.206.123.223:5050\\/notify\"},\"attrs\":[\"taxiId\",\"time\",\"lat\",\"lon\",\"status\"]},\"expires\":\"2016-09-03T10:43:17.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"time\"]},\"entities\":[{\"id\":\"\",\"type\":\"Taxi\",\"idPattern\":\".*\"}]},\"id\":\"55f00d453de71c949d1422e1\",\"status\":\"active\"},{\"notification\":{\"http\":{\"url\":\"http:\\/\\/130.206.123.223:5050\\/notify\"},\"attrs\":[\"taxiId\",\"time\",\"lat\",\"lon\",\"status\"]},\"expires\":\"2016-09-03T11:22:52.00Z\",\"throttling\":1,\"subject\":{\"condition\":{\"attrs\":[\"time\"]},\"entities\":[{\"id\":\"\",\"type\":\"Taxi\",\"idPattern\":\".*\"}]},\"id\":\"55f0168c3de71c949d1422e2\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2016-03-09T22:10:04.00Z\",\"timesSent\":8512673826141801965,\"http\":{\"url\":\"http:\\/\\/130.206.123.223:5050\\/notify\"},\"attrs\":[\"taxiId\",\"time\",\"lat\",\"lon\",\"status\"]},\"expires\":\"2016-09-03T11:26:58.00Z\",\"throttling\":1,\"subject\":{\"condition\":{\"attrs\":[\"time\"]},\"entities\":[{\"id\":\"\",\"type\":\"Taxi\",\"idPattern\":\".*\"}]},\"id\":\"55f017823de71c949d1422e3\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-24T14:56:08.00Z\",\"timesSent\":8040851773959392932,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-21T13:30:50.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"status\"]},\"entities\":[{\"id\":\"40.64059722.944096\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"5603fb0aebf4aa5a1588cd21\",\"status\":\"active\"},{\"notification\":{\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-21T14:10:26.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"status\"]},\"entities\":[{\"id\":\"crowdId\",\"type\":\"crowd\",\"idPattern\":\"\"}]},\"id\":\"56040452ebf4aa5a1588cd22\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-24T14:11:30.00Z\",\"timesSent\":8040851773959392932,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-21T14:10:54.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"observations\"]},\"entities\":[{\"id\":\"crowdId\",\"type\":\"crowd\",\"idPattern\":\"\"}]},\"id\":\"5604046eebf4aa5a1588cd23\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-24T14:24:04.00Z\",\"timesSent\":8040851773959392932,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-21T14:23:15.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"status\"]},\"entities\":[{\"id\":\"40.64059781.944096\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"56040753ebf4aa5a1588cd24\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-24T14:25:21.00Z\",\"timesSent\":7512234279060279666,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-21T14:25:20.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"status\"]},\"entities\":[{\"id\":\"40.64059777777.944096\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"560407d0ebf4aa5a1588cd25\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-24T15:12:36.00Z\",\"timesSent\":7512234279060279666,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-21T15:12:36.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"status\"]},\"entities\":[{\"id\":\"40.64059712.944096\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"560412e4ebf4aa5a1588cd26\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-25T12:44:22.00Z\",\"timesSent\":8231085825504083150,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-22T10:37:15.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"aqiText\"]},\"entities\":[{\"id\":\"40.64368522.963004\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"560523dbf0ad42c03e86d912\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-25T12:44:40.00Z\",\"timesSent\":7901856702247101528,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-22T10:42:07.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"aqiText\"]},\"entities\":[{\"id\":\"40.60113922.960463\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"560524fff0ad42c03e86d918\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-25T12:44:39.00Z\",\"timesSent\":6886708066761051270,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-22T10:43:10.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"aqiText\"]},\"entities\":[{\"id\":\"40.64059922.944096\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"5605253ef0ad42c03e86d91b\",\"status\":\"active\"},{\"notification\":{\"lastNotification\":\"2015-09-25T12:44:39.00Z\",\"timesSent\":8578622459237801700,\"http\":{\"url\":\"http:\\/\\/exasa.gr\\/various\\/test.php\"},\"attrs\":[]},\"expires\":\"2025-09-22T10:43:14.00Z\",\"subject\":{\"condition\":{\"attrs\":[\"aqiText\"]},\"entities\":[{\"id\":\"40.62371522.957265\",\"type\":\"location\",\"idPattern\":\"\"}]},\"id\":\"56052542f0ad42c03e86d91c\",\"status\":\"active\"}]";
        
        normalURL = "http://orion.lab.fiware.org:1026/v2/subscriptions/12345";
        arrayURL = "http://orion.lab.fiware.org:1026/v2/subscriptions";
        
        normalEntity = new StringEntity(normalResponse);
        arrayEntity = new StringEntity(arrayResponse);
        
        host = new String[] {"orion.lab.fi-ware.org"};
        
        headers.add(new BasicHeader("Content-type", "application/json"));
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("X-Auth-token", "lbJu9rmnfYd880eCOPCl0HPGxNfX96"));
        
        when(mockResponse.getEntity()).thenReturn(normalEntity);
        
        when(mockArrayResponse.getEntity()).thenReturn(arrayEntity);
    } // setUp
    
    @Test
    public void testDoRequestWithNormalResponse() throws Exception {
        System.out.println(getTestTraceHead("[HttpBackend]") + "- Testing HttpBackend.doRequest - 'GET method (by ID) gets a valid response'");
        httpBackend = new HttpBackendImpl(host, normalURL, false, false, null, null, null, null);
        
        try {
            httpBackend.doRequest("GET", normalURL, headers, normalEntity);
            System.out.println(getTestTraceHead("[HttpBackend]") + "-  OK  - Succesfully got");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend]") + " - FAIL - There was some problem when handling the GET subscription.");
            throw e;
        } // try catch
        
    } // testDoRequestWithNormalResponse
    
    @Test
    public void testDoRequestWithArrayResponse () throws Exception {
        System.out.println(getTestTraceHead("[HttpBackend]") + "- Testing HttpBackend.doRequest - 'GET method (all subscriptions) gets a valid response'");
        httpBackend = new HttpBackendImpl(host, arrayURL, false, false, null, null, null, null);
        
        try {
            httpBackend.doRequest("GET", arrayURL, headers, arrayEntity);
            System.out.println(getTestTraceHead("[HttpBackend]") + "-  OK  - Succesfully got");
        } catch (Exception e) {
            System.out.println(getTestTraceHead("[HttpBackend]") + " - FAIL - There was some problem when handling the GET subscription.");
            throw e;
        } // try catch
        
    } // testDoRequestWithArrayResponse

} // HttpBackendTest
