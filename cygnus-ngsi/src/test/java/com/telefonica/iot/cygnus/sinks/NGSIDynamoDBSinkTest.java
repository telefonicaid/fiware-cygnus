/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
import org.apache.flume.Context;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIDynamoDBSinkTest {
    
    /**
     * [NGSIDynamoDBSink.configure] -------- When not configured, not mandatory parameters get default values.
     */
    @Test
    public void testConfigureDefaults() {
        System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                + "-------- When not configured, not mandatory parameters get default values");
        String accessKeyId = "my_access_key";
        String attrPersistence = null;
        String batchSize = null;
        String batchTimeout = null;
        String batchTTL = null;
        String dataModel = null;
        String enableGrouping = null;
        String enableLowercase = null;
        String region = null;
        String secretAccessKey = "my_secret_access_key";
        NGSIDynamoDBSink sink = new NGSIDynamoDBSink();
        sink.configure(createContext(accessKeyId, attrPersistence, batchSize, batchTimeout, batchTTL, dataModel,
                enableGrouping, enableLowercase, region, secretAccessKey));
        
        try {
            assertTrue(sink.getRowAttrPersistence());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'attr_persistence=row' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'attr_persistence=row' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(1, sink.getBatchSize());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'batch_size=1' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'batch_size=1' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(30, sink.getBatchTimeout());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'batch_timeout=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'batch_timeout=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(10, sink.getBatchTTL());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'batch_ttl=30' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'batch_ttl=30' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertEquals(Enums.DataModel.DMBYENTITY, sink.getDataModel());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'data_model=dm-by-entity' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'data_model=dm-by-entity' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(sink.getEnableEncoding());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'enable_encoding=true' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'enable_encoding=true' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableGrouping());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'enable_grouping=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'enable_grouping=false' not configured by default");
            throw e;
        } // try catch
        
        try {
            assertTrue(!sink.getEnableLowerCase());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'enable_lowercase=false' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'enable_lowercase=false' not configured by default");
            throw e;
        } // try catch

        try {
            assertEquals("eu-west-1", sink.getRegion());
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "-  OK  - 'region=eu-west-1' configured by default");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSIDynamoDBSink.configure]")
                    + "- FAIL - 'region=eu-west-1' not configured by default");
            throw e;
        } // try catch
    } // testConfigureDefaults
    
    private Context createContext(String accessKeyId, String attrPersistence, String batchSize, String batchTimeout,
            String batchTTL, String dataModel, String enableGrouping, String enableLowercase, String region,
            String secretAccessKey) {
        Context context = new Context();
        context.put("access_key_id", accessKeyId);
        context.put("attr_persistence", attrPersistence);
        context.put("batch_size", batchSize);
        context.put("batch_timeout", batchTimeout);
        context.put("batch_ttl", batchTTL);
        context.put("data_model", dataModel);
        context.put("enable_grouping", enableGrouping);
        context.put("enable_lowercase", enableLowercase);
        context.put("region", region);
        context.put("secret_access_key", secretAccessKey);
        return context;
    } // createContext
    
} // NGSIDynamoDBSinkTest
