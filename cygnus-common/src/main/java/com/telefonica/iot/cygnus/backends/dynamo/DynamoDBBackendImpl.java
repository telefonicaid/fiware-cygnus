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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.ArrayList;

/**
 *
 * @author frb
 */
public class DynamoDBBackendImpl implements DynamoDBBackend {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(DynamoDBBackendImpl.class);
    private DynamoDB dynamoDB;
    
    /**
     * Constructor.
     * @param accessKeyId
     * @param secretAccessKey
     * @param region
     */
    public DynamoDBBackendImpl(String accessKeyId, String secretAccessKey, String region) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(awsCredentials);
        client.setRegion(Region.getRegion(Regions.fromName(region)));
        dynamoDB = new DynamoDB(client);
    } // DynamoDBBackendImpl

    /**
     * Gets the dynamo DB object. It is protected since it is only used by the tests.
     * @return 
     */
    protected DynamoDB getDynamoDB() {
        return dynamoDB;
    } // getDynamoDB
    
    /**
     * Sets the dynamo DB object. It is protected since it is only used by the tests.
     * @param dynamoDB
     */
    protected void setDynamoDB(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    } // setDynamoDB
    
    @Override
    public void createTable(String tableName, String primaryKey) throws Exception {
        try {
            // Create the key schema for the given primary key
            ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement()
                .withAttributeName(primaryKey)
                .withKeyType(KeyType.HASH));
            
            // Create the attribute definitions
            ArrayList<AttributeDefinition> attrDefs = new ArrayList<AttributeDefinition>();
            attrDefs.add(new AttributeDefinition()
                .withAttributeName(primaryKey)
                .withAttributeType("N"));

            // Create the table request given the table name, the key schema and the attribute definitios
            CreateTableRequest tableRequest = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attrDefs)
                .withProvisionedThroughput(new ProvisionedThroughput()
                    .withReadCapacityUnits(5L)
                    .withWriteCapacityUnits(5L));

            // Create the table
            LOGGER.debug("Creating DynamoDB table " + tableName);
            Table table = dynamoDB.createTable(tableRequest);

            // Wait until the table is active
            LOGGER.debug("Waiting until the DynamoDB table " + tableName + " becomes active");
            table.waitForActive();
        } catch (Exception e) {
            LOGGER.error("Error while creating the DynamoDB table " + tableName
                    + ". Details=" + e.getMessage());
        } // try catch
    } // createTable
    
    @Override
    public void putItems(String tableName, ArrayList<Item> aggregation) throws Exception {
        try {
            TableWriteItems tableWriteItems = new TableWriteItems(tableName);
            tableWriteItems.withItemsToPut(aggregation);
            BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);
        } catch (Exception e) {
            LOGGER.error("Error while putting a batch of items in the table " + tableName
                    + ". Details=" + e.getMessage());
        } // try catch
    } // putItems
    
} // DynamoDBBackendImpl
