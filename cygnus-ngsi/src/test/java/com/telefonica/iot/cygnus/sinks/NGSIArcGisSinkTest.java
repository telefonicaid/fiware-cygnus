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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.ArcgisLog;
import com.telefonica.iot.cygnus.utils.EntityArcGisUtils;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;

import es.santander.smartcity.arcgisutils.Entity;

/**
 *
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIArcGisSinkTest {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIArcGisSinkTest.class);
    private NGSIArcGisSink sink;

    @Mock
    private ArcgisLog mockArcgisLog;
    
    @Mock
    private EntityArcGisUtils mockEntityArcGisUtils;

    /**
     * Constructor.
     */
    public NGSIArcGisSinkTest() {
    } // NGSIArcGisSinkTest

    /**
     * @throws Exception
     * 
     */
    @Before
    public void setup() throws Exception {
        Mockito.doNothing().when(mockArcgisLog).add(Mockito.anyList());
        Mockito.doNothing().when(mockArcgisLog).addToBatch(Mockito.any(Entity.class));
        Mockito.doNothing().when(mockArcgisLog).addToBatch(Mockito.anyList());
        Mockito.when(mockArcgisLog.canAdd()).thenReturn(true);
        Mockito.when(mockArcgisLog.commitEntities()).thenReturn(true);
        Mockito.when(mockArcgisLog.isLoaded()).thenReturn(true);
        
        //EntityArcGisUtilsMock
        Mockito.when(mockEntityArcGisUtils.createEntities(Mockito.any(JSONArray.class), Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        Mockito.when(mockEntityArcGisUtils.createEntity(Mockito.any(JSONObject.class), Mockito.anyString(), Mockito.anyString())).thenReturn(null);

    } // setup

    /**
     * [NGSIArcGisSink.insertFeature] -------- When service-path is notified,
     * NGSIArcGisSink send entities to GIS.
     */
    @Test
    public void testInsertFeature() {
        LOGGER.debug(getTestTraceHead("[NGSIArcGISBaseSink.insertFeature]")
                + "-------- When / service-path is notified/defaulted, NGSIArcGisSink send entities to GIS.");

        try {
            String bodyJSON = "{\"id\": \"Car1\", \"type\": \"Car\", "
                    + "\"location\": { \"type\": \"geo:json\" \"value\": "
                    + "{\"type\": \"Point\", \"coordinates\": [-3.79109661, 43.48712556]}}, "
                    + " \"speed\": { \"type\":\"Float\", \"value\": 98 } }";
            String serviceFiware = "service";
            String servicePathFiware = "/test";

            String url = "XXXXXX";
            String username = "XXXXXX";
            String password = "XXXXXX";

            sink = new NGSIArcGisSink();

            sink.configure(NGSIUtilsForTests.createContextForArcGis(url, username, password));

            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(bodyJSON);
            sink.setEntityArcGisUtils(mockEntityArcGisUtils);
            NGSIArcGisSink.ArcGISDomain arcGisDomain = sink.new ArcGISDomain(serviceFiware, servicePathFiware, json);

            sink.setArcgisUtils(mockArcgisLog);

            try {
                sink.insertFeature(arcGisDomain);
                assertTrue(true);
                LOGGER.debug(getTestTraceHead("[NGSIArcGisSink.insertFeeature]") + "-  OKs");
            } catch (AssertionError e) {
                LOGGER.error(getTestTraceHead("[NGSIArcGisSink.insertFeeature]") + "- FAIL");
                throw e;
            } // try catch
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getTestTraceHead("[NGSIArcGisSink.insertFeeature]") + "- FAIL");
            assertTrue(false);
        } // catch

    } // testInsertFeature

} // NGSIArcGisSinkTest
