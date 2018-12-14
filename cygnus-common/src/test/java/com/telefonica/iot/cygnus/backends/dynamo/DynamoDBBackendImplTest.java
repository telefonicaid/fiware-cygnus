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
package com.telefonica.iot.cygnus.backends.dynamo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import java.util.ArrayList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamoDBBackendImplTest {
    
    // instance to be tested
    private DynamoDBBackendImpl backend;
    
    // mocks
    @Mock
    private DynamoDB mockDynamoDB;
    @Mock
    private TableWriteItems mockTableWriteItems;
    @Mock
    private Table mockTable;
    
    // constants
    private final String accessKeyId = "xxxx";
    private final String secretAccessKey = "xxxx";
    private final String region = "eu-west-1";
    private final String tableName = "table-name";
    private final String primaryKey = "primary-key";
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        backend = new DynamoDBBackendImpl(accessKeyId, secretAccessKey, region);
        
        // set up the behaviour of the mocked classes
        when(mockDynamoDB.createTable(null)).thenReturn(null);
        when(mockDynamoDB.batchWriteItem(mockTableWriteItems)).thenReturn(null);
        when(mockDynamoDB.getTable(tableName)).thenReturn(mockTable);
    } // setUp
    
    /**
     * Test of createTable method, of class MySQLBackendImpl.
     */
    @Test
    public void testCreateTable() {
        System.out.println("Testing DynamoDBBackendImpl.createTable (within first database");
        
        try {
            backend.setDynamoDB(mockDynamoDB);
            backend.createTable(tableName, primaryKey);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDynamoDB().getTable(tableName) != null);
        } // try catch finally

    } // testCreateTable
    
    /**
     * Test of createTable method, of class MySQLBackendImpl.
     */
    @Test
    public void testPutItems() {
        System.out.println("Testing MySQLBackend.createTable (within first database");
        
        try {
            backend.setDynamoDB(mockDynamoDB);
            backend.createTable(tableName, primaryKey);
            ArrayList<Item> aggregation = new ArrayList<Item>();
            Item item = new Item().withString("field", "value");
            aggregation.add(item);
            backend.putItems(tableName, aggregation);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(backend.getDynamoDB().getTable(tableName) != null);
        } // try catch finally
    } // testPutItems
    
} // DynamoDBBackendImplTest
