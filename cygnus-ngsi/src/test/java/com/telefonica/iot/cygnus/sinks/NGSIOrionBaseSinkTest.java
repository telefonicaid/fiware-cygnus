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
import static org.mockito.Mockito.when;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.telefonica.iot.cygnus.backends.orion.OrionBackend;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;
import com.telefonica.iot.cygnus.utils.auth.keystone.KeyStoneUtils;

/**
 *
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIOrionBaseSinkTest {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIOrionBaseSinkTest.class);
    private NGSIOrionSink sink;

    @Mock
    private KeyStoneUtils mockKeyStoneUtils;
    @Mock
    private OrionBackend mockOrionBackend;
    
    
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

        Mockito.doNothing().when(mockOrionBackend).updateRemoteContext(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockKeyStoneUtils.getSessionToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("Token");
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
        
        try {

            
            String bodyJSON = "{\"id\": \"Car1\", \"type\": \"Car\", \"speed\": { \"type\":\"Float\", \"value\": 98 } }";
            
            String orionHost = "XXXXXX Host";
            String orionPort = "80";
            String orionHostKey = "XXXXXX Host KEY";
            String orionPortKey = "8080";
            String orionUsername = "XXXXXX Username";
            String orionPassword = "XXXXXX Password";
            String orionFiware = "XXXXXX Fiware";
            String orionFiwarePath = "XXXXXX FiwarePath";
            String enableEncoding = "falso";

            sink = new NGSIOrionSink();
            
            sink.configure(NGSIUtilsForTests.createContextForOrion(orionHost, orionPort, orionHostKey, orionPortKey,
                    orionUsername, orionPassword, orionFiware, orionFiwarePath));
            sink.setKeyStoneUtils(mockKeyStoneUtils);
            sink.setOrionBackend(mockOrionBackend);
            bodyJSON = new JSONObject(bodyJSON.replaceAll("=", ":")).toString();
            

            try {
                sink.updateRemoteContext(bodyJSON, orionFiware, orionFiwarePath);
                assertTrue(true);
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
