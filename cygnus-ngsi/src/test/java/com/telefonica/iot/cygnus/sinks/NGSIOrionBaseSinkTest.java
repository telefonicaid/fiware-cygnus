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
package com.telefonica.iot.cygnus.sinks;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import com.telefonica.iot.cygnus.utils.PropertyUtils;

/**
 *
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIOrionBaseSinkTest {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIOrionBaseSinkTest.class);
    private PropertyUtils propertyUtils = null;
    private NGSIOrionSink sink;
    
    
    /**
     * Constructor.
     */
    public NGSIOrionBaseSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSIOrionBaseSinkTest

    /**
     * @throws Exception
     * 
     */
    @Before
    public void setup() throws Exception {
    } // setup

    /**
     * [NGSIOrionBaseSink.buildCollectionName] -------- When / service-path is
     * notified/defaulted and data_model=dm-by-entity, the OrionDB collections
     * name is the concatenation of the \<prefix\>, \<service-path\>, \
     * <entityId\> and \<entityType\>.
     */
    @Test
    public void testCreateUpdateEntityEncoding() {
        LOGGER.debug(getTestTraceHead("[NGSIOrionBaseSink.buildCollectionName]")
                + "-------- When / service-path is notified/defaulted and data_model=dm-by-entity, the OrionDB"
                + "collections name is the concatenation of the <prefix>, <service-path>, <entityId> and <entityType>");

        String datos = " {Processing headers={\"notified-entity\"=\"Room1_Room\", "
                + "\"transaction-id\"=\"e82ef180-4c99-4d67-a719-e02242c5e108\", \"grouped-servicepath\"=\""
                + propertyUtils.getProperty("orionFiwarePath")
                + "\", \"fiware-correlator\"=\"e82ef180-4c99-4d67-a719-e02242c5e108\", \"fiware-servicepath\"=\""
                + propertyUtils.getProperty("orionFiwarePath") + "\", \"fiware-service\"=\""
                + propertyUtils.getProperty("orionFiware")
                + "\", \"grouped-entity\"=\"Room1_Room\", "
                + "\"timestamp\"=\"1499097276139\"}, Processing context element={id=Room1, type=Room}, "
                + "Processing attribute={name=temperature, type=centigrade, value=\"26.5\", metadata=[]}, "
                + "updateObject={id=Room1, type=Room, \"temperature\" ={ type=\"centigrade\", value=\"26.5\"}}}";

        try {
            String bodyJSON = datos;

            propertyUtils = new PropertyUtils("./src/test/resources/login.properties");
            String orionHost = propertyUtils.getProperty("orionHost");
            String orionPort = propertyUtils.getProperty("orionPort");
            String orionHostKey = propertyUtils.getProperty("orionHostKey");
            String orionPortKey = propertyUtils.getProperty("orionPortKey");
            String orionUsername = propertyUtils.getProperty("orionUsername");
            String orionPassword = propertyUtils.getProperty("orionPassword");
            String orionFiware = propertyUtils.getProperty("orionFiware");
            String orionFiwarePath = propertyUtils.getProperty("orionFiwarePath");

            sink = new NGSIOrionSink();
            
            sink.configure(NGSIUtilsForTests.createContextForOrion(orionHost, orionPort, orionHostKey, orionPortKey,
                    orionUsername, orionPassword, orionFiware, orionFiwarePath));

            bodyJSON = new JSONObject(bodyJSON.replaceAll("=", ":")).getString("updateObject");
            

            try {
                assertTrue(sink.getInvalidConfiguration());
                LOGGER.debug(getTestTraceHead("[NGSIOrionBaseSink.updateRemoteContext]") + "-  OKs");
            } catch (AssertionError e) {
                LOGGER.error(getTestTraceHead("[NGSIOrionBaseSink.updateRemoteContext]") + "- FAIL");
                throw e;
            } // try catch
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getTestTraceHead("[NGSIOrionBaseSink.updateRemoteContext]") + "- FAIL");
            assertTrue(false);
        } // catch
    } // testCreateUpdateEntityEncoding

} // NGSIOrionBaseSinkTest
