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

package com.telefonica.iot.cygnus.backends.arcgis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.model.UserCredential;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.ArcgisBaseTest;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestFeatureTable;

/**
 * @author dmartinez
 *
 */
public class RestFeatureTableTest implements ArcgisBaseTest {

    //static final Logger LOGGER = Logger.getLogger(RestFeatureTableTest.class);
    static final Logger LOGGER = LogManager.getLogger(RestFeatureTableTest.class);

    /**
     * 
     */
    @Before
    public void setUp() {
        //BasicConfigurator.configure();
        Configurator.initialize(new DefaultConfiguration());
    }

    /**
     * 
     * @throws MalformedURLException
     */
    @Test
    public void connectionTest() throws MalformedURLException {
        String serviceUrl = ArcgisBaseTest.getFeatureUrl();
        String tokenUrl = ArcgisBaseTest.getGenerateTokenUrl();

        System.out.println("----------------  ConnectionTest. Portal:" + ArcgisBaseTest.testPortal);

        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                Credential credential = new UserCredential(ArcgisBaseTest.getUser(),
                        ArcgisBaseTest.getPassword());
                RestFeatureTable featureTable = new RestFeatureTable(serviceUrl, credential, tokenUrl);
    
                System.out.println("Connecting....");
    
                String whereClause = "OBJECTID>0";
                featureTable.getFeatureList(whereClause);
    
                System.out.println("Token: " + credential.getToken());
    
                assertTrue("Ejecución correcta", true);
    
            } catch (ArcgisException e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                fail(e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }

    /**
     * 
     * @param serviceUrl
     * @return
     * @throws ArcgisException
     */
    private RestFeatureTable createFeatureTable(String serviceUrl) throws ArcgisException {
        String tokenUrl = ArcgisBaseTest.getGenerateTokenUrl();

        System.out.println("----------------  createFeatureTable");
        Credential credential = new UserCredential(ArcgisBaseTest.getUser(),
                ArcgisBaseTest.getPassword());
        return new RestFeatureTable(serviceUrl, credential, tokenUrl);
    }

    /**
     * 
     * @throws MalformedURLException
     */
    @Test
    public void connectionWithoutCredentialsTest() throws MalformedURLException {
        String serviceUrl = ArcgisBaseTest.getFeatureUrl();
        System.out.println("----------------  ConnectionWithoutCredentialsTest");

        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                RestFeatureTable featureTable = createFeatureTable(serviceUrl);
    
                System.out.println("Connecting....");
    
                String whereClause = "OBJECTID>0";
                featureTable.getFeatureList(whereClause);
    
            } catch (ArcgisException e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                fail(e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

        assertTrue("Ejecución correcta", true);
    }

    
    /**
     * 
     * @throws MalformedURLException
     * @throws ArcgisException
     */
    @Test
    public void addFeature() throws MalformedURLException, ArcgisException {

        String serviceUrl = ArcgisBaseTest.getFeatureUrl();
        System.out.println("----------------  addFeature");
        
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try{
                RestFeatureTable featureTable = createFeatureTable(serviceUrl);
        
                Feature feature = FeatureTestFactory.getNewOcupacionFeature("Prueba addPortalFeature");
                System.out.println(feature.toJson().toString());
        
                featureTable.addFeature(feature);
                assertTrue("OK", true);
            } catch(Exception e) {
                fail ("ERROR adding feature." + e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }

    /**
     * 
     * @throws MalformedURLException
     * @throws ArcgisException
     */
    @Test
    public void updateFeature() throws MalformedURLException, ArcgisException {
        
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try{
                System.out.println("----------------  updateFeature");
                String serviceUrl = ArcgisBaseTest.getFeatureUrl();
                RestFeatureTable featureTable = createFeatureTable(serviceUrl);
        
                Feature feature = FeatureTestFactory.getUpdatedOcupacionFeature(621,
                        "prueba updatePortalFeature");
                System.out.println(feature.toJson().toString());
        
                featureTable.updateFeature(feature);
                assertTrue("OK", true);
            } catch(Exception e) {
                fail ("ERROR adding feature." + e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }

    /**
     * 
     * @throws MalformedURLException
     * @throws ArcgisException
     */
    @Test
    public void deleteFeature() throws MalformedURLException, ArcgisException {        
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                System.out.println("----------------  deleteFeature");
                String serviceUrl = ArcgisBaseTest.getFeatureUrl();
                RestFeatureTable featureTable = createFeatureTable(serviceUrl);
    
                List<String> idList = new ArrayList<String>();
                idList.add("327");
                idList.add("326");
    
                featureTable.deleteEntities(idList);
                assertTrue("OK", true);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }

    /**
     * 
     * @throws MalformedURLException
     * @throws ArcgisException
     */
    @Test
    public void getFeatures() throws MalformedURLException, ArcgisException {      
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                System.out.println("----------------  getOnlineFeatures");
                String serviceUrl = ArcgisBaseTest.getFeatureUrl();
                String tokenUrl = ArcgisBaseTest.getGenerateTokenUrl();
                Credential credential = new UserCredential(ArcgisBaseTest.getUser(),
                        ArcgisBaseTest.getPassword());
                RestFeatureTable featureTable = new RestFeatureTable(serviceUrl, credential, tokenUrl);
    
                String whereClause = "OBJECTID>0";
    
                List<Feature> featureList = featureTable.getFeatureList(whereClause);
                assertTrue(featureList.size() > 0);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }

    /**
     * 
     * @throws MalformedURLException
     * @throws ArcgisException
     */
    @Test
    public void getTableAttributesInfoNoCred() throws MalformedURLException, ArcgisException {      
        if (!ArcgisBaseTest.connectionTestsSkipped()){
        try {
                System.out.println("----------------  getTableAttributesInfo_noCred");
                String serviceUrl = ArcgisBaseTest.getFeatureUrl();
                RestFeatureTable featureTable = createFeatureTable(serviceUrl);
    
                featureTable.getTableAttributesInfo();
                System.out.println(
                        "Number of fields detected: " + featureTable.getTableAttributes().size());
                System.out.println(
                        "Number of Unique fields: " + featureTable.getUniqueAttributes().size());
                assertTrue(true);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }

    /**
     * 
     * @throws MalformedURLException
     * @throws ArcgisException
     */
    @Test
    public void getTableAttributesInfo() throws MalformedURLException, ArcgisException {  
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                System.out.println("----------------  getTableAttributesInfo");
                String serviceUrl = ArcgisBaseTest.getFeatureUrl();
                serviceUrl = ArcgisBaseTest.getGenerateTokenUrl();
                Credential credential = new UserCredential(ArcgisBaseTest.getUser(),
                        ArcgisBaseTest.getPassword());
    
                RestFeatureTable featureTable = new RestFeatureTable(serviceUrl, credential,
                        ArcgisBaseTest.getGenerateTokenUrl());
    
                featureTable.getTableAttributesInfo();
                System.out.println(
                        "Number of fields detected: " + featureTable.getTableAttributes().size());
                System.out.println(
                        "Number of Unique fields: " + featureTable.getUniqueAttributes().size());
                assertTrue(true);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }

    }
}
