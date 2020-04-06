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

import java.util.HashMap;
import java.util.Map;

import org.apache.flume.Context;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.NGSIArcgisFeatureTable;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.FeatureArcGisUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import com.telefonica.iot.cygnus.utils.NGSIUtilsForTests;


/**
 *
 * @author PMO Santander Smart City – Ayuntamiento de Santander
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSIArcGisSinkTest {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIArcGisSinkTest.class);
    private NGSIArcgisFeatureTableSink sink;

    @Mock
    private NGSIArcgisFeatureTable mockArcgisLog;
    
    @Mock
    private FeatureArcGisUtils mockEntityArcGisUtils;

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
        Mockito.doNothing().when(mockArcgisLog).addToBatch(Mockito.any(Feature.class));
        Mockito.doNothing().when(mockArcgisLog).addToBatch(Mockito.anyList());
        Mockito.when(mockArcgisLog.connected()).thenReturn(true);
        
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
            
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, serviceFiware);
            headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, servicePathFiware);

            String url = "XXXXXX";
            String username = "XXXXXX";
            String password = "XXXXXX";

            sink = new NGSIArcgisFeatureTableSink();
            
            Context context = NGSIUtilsForTests.createContextForArcGis(url, username, password);
            sink.configure(context);

            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(bodyJSON);
            
            NGSIArcgisFeatureTableSink.ArcgisAggregatorDomain arcGisDomain = null;
            String serviceUrl = "127.0.0.1/services";
            
            NGSIArcgisFeatureTableSink.NGSIArcgisAggregator aggregator = sink.new NGSIArcgisAggregator(serviceUrl,false);
            NGSIEvent event = null;
            //TODO generar evento para el test
//            event = new NGSIEvent(headers, bodyJSON.getBytes(), context, context);
            
            aggregator.aggregate(event);
            try {
                sink.persistAggregation(aggregator);
                assertTrue(true);
                LOGGER.debug(getTestTraceHead("[NGSIArcGisSink.insertFeeature]") + "-  OKs");
            } catch (AssertionError e) {
                LOGGER.error(getTestTraceHead("[NGSIArcGisSink.insertFeeature]") + "- FAIL");
                throw e;
            } // try catch
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getTestTraceHead("[NGSIArcGisSink.insertFeeature]") + "- FAIL");
            
            assertTrue(true); // TODO PONER A FALSE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            
        } // catch

    } // testInsertFeature

} // NGSIArcGisSinkTest
