/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.SocketException;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.model.Field;
import com.telefonica.iot.cygnus.backends.arcgis.model.GisAttributeType;
import com.telefonica.iot.cygnus.backends.arcgis.model.UserCredential;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * 
 * @author dmartinez
 *
 */
public class ArcgisFeatureTable {
    private static final CygnusLogger LOGGER = new CygnusLogger(ArcgisFeatureTable.class);
    
    public static final int ADD_ACTION = 0;
    public static final int UPDATE_ACTION = 1;
    public static final int DELETE_ACTION = 2;
    public static final int ADD_UPDATE_ACTION = 3;

    private int batchAction = ADD_ACTION;
    private String uniqueField = null;

    // Flags and semaphores
    private AtomicBoolean flushingBatch;
    protected AtomicBoolean error;
    private boolean connected;

    private int batchSize;
    private String errorDesc = "";
    private int errorCode = 0;
    private List<Feature> featureBatch = Collections.synchronizedList(new ArrayList<Feature>());

    private List<Feature> addBatch = Collections.synchronizedList(new ArrayList<Feature>());
    private List<Feature> updateBatch = Collections.synchronizedList(new ArrayList<Feature>());

    private RestFeatureTable arcGISFeatureTable;

    /**
     * 
     */
    protected ArcgisFeatureTable() {
        connected = false;
        batchSize = 10;
        error = new AtomicBoolean(false);
        flushingBatch = new AtomicBoolean(false);
        arcGISFeatureTable = null;
    }

    /**
     * Constructor.
     * 
     * @param url
     * @param user
     * @param password
     * @param tokenGenUrl
     * @param readOnly
     */
    public ArcgisFeatureTable(String url, String user, String password, String tokenGenUrl,
            boolean readOnly) {
        this();
        
        LOGGER.debug("Arcgis constructor.. " + url);

        LOGGER.debug("Arcgis url.. " + url);
        LOGGER.debug("Arcgis tokenGenUrl.. " + tokenGenUrl);
        LOGGER.debug("Arcgis readOnly.. " + readOnly);

        Credential credential = new UserCredential(user, password);
        try {
            arcGISFeatureTable = new RestFeatureTable(url, credential, tokenGenUrl);
            LOGGER.debug("Recovering attribute info from feature table. ->" + url);
            arcGISFeatureTable.getTableAttributesInfo();
            LOGGER.debug("Table successfully connected.");
            connected = true;
        } catch (ArcgisException e) {
            LOGGER.error("Argis error while connecting to Feature Table: (" + e.getMessage() + ")"
                + "\n\t URL: " + url
                + "\n\t tokenGenURL: " + tokenGenUrl);
            connected = false;
            setError(e);
            this.errorDesc += "    " + url + "  -  " + tokenGenUrl;
        } catch (Exception e) {
            connected = false;
            setError(e);
            this.errorDesc = "Unexpected exception: " + e.toString();
        }
    }


    /**
     * @return the uniqueField
     */
    public String getUniqueField() {
        return uniqueField;
    }

    /**
     * @param uniqueField
     *            the uniqueField to set
     */
    public void setUniqueField(String uniqueField) {
        this.uniqueField = uniqueField;
    }

    /**
     * Add Feature to batch.
     * 
     * @param feature
     */
    public void addToBatch(Feature feature) {
        synchronized (featureBatch) {
            this.error.set(false);

            if (connected) {

                LOGGER.debug("Arcgis: Adding feature to Batch... ");
                featureBatch.add(cleanFeature(feature));

                if (featureBatch.size() >= getBatchSize()) {
                    LOGGER.debug("Sending Batch...");
                    List<Feature> batch = new ArrayList<Feature>(featureBatch);
                    featureBatch.clear();
                    try {
                        if (batchAction == ADD_UPDATE_ACTION) {
                            addUpdateFeatures(batch, uniqueField);
                        } else {
                            this.commitFeatures(batch, batchAction);
                        }
                    } catch (ArcgisException e) {
                        setError(e);
                    }
                }

            } else {
                LOGGER.error("Not connected, can't add Entitie to batch.");
                this.error.set(true);
                this.errorDesc = "Not connected, can't add Entitie to batch";
            }
        }
    }

    /**
     * Add list of Feature to batch.
     * 
     * @param listEntities
     */
    public synchronized void addToBatch(List<Feature> listEntities) {
        synchronized (featureBatch) {
            for (Feature feature : listEntities) {
                addToBatch(feature);
            }
        }

    }

    /**
     * Returns how many features are in the batch lists.
     * 
     * @return
     */
    public int featuresBatched() {
        return featureBatch.size() + updateBatch.size() + addBatch.size();
    }

    /**
     * Force batch procesing.
     * 
     */
    public void flushBatch() {
        synchronized (featureBatch) {
            clearResults();
            try {
                int batchesSize = featuresBatched();
                if (batchesSize > 0 && !flushingBatch.get() && !hasError()) {
                    flushingBatch.set(true);
                    LOGGER.debug("Flushing Batch...");

                    this.error.set(false);
                    List<Feature> batch = new ArrayList<Feature>(featureBatch);
                    featureBatch.clear();

                    // Force to send all features
                    int batchSizeBackup = this.batchSize;
                    this.batchSize = 1;

                    if (batchAction == ADD_UPDATE_ACTION) {
                        addUpdateFeatures(batch, uniqueField);
                    } else {
                        this.commitFeatures(batch, batchAction);
                    }
                    this.batchSize = batchSizeBackup;

                    flushingBatch.set(false);
                }
            } catch (ArcgisException e) {
                setError(e);
            }
        }
    }

    /**
     * Adds Entities to Feature Table.
     * 
     * @param featureArray
     *            List of etnities to add.
     * @throws ArcGisException
     */
    public void addFeatures(List<Feature> featureArray) throws ArcgisException {
        clearResults();
        commitFeatures(featureArray, ADD_ACTION);
    }

    /**
     * Updates Entities in Feature table. Unique attributes must have been set.
     * 
     * @param featureArray
     *            List of entities to update.
     * @throws ArcGisException
     */
    public void updateFeatures(List<Feature> featureArray) throws ArcgisException {
        clearResults();
        commitFeatures(featureArray, UPDATE_ACTION);
    }

    /**
     * 
     * @param featureArray
     * @param uniqueField
     */
    public void addUpdateFeatures(List<Feature> featureArray, String uniqueField)
            throws ArcgisException {
        clearResults();
        if (featureArray != null && featureArray.size() > 0) {
            String keyList = getUniqueFieldList(featureArray, uniqueField);
            String whereClause = uniqueField + " IN (" + keyList + ")";

            List<Feature> foundFeatures = queryFeatures(whereClause);
            splitFeatureListIfExists(featureArray, foundFeatures, updateBatch, addBatch,
                    uniqueField);
        }

        if (addBatch.size() >= batchSize) {
            addBatch = removeDuplicates(addBatch, uniqueField);
            commitFeatures(addBatch, ADD_ACTION);
            addBatch.clear();
        }

        if (updateBatch.size() >= batchSize) {
            commitFeatures(updateBatch, UPDATE_ACTION);
            updateBatch.clear();
        }

    }

    /**
     * Removes duplicate features from list given a field that should be unique, last occurrence
     * remains.
     * 
     * @param featureList
     * @param uniqueField
     * @return
     */
    protected List<Feature> removeDuplicates(List<Feature> featureList, String uniqueField) {
        List<Feature> resultList = new ArrayList<Feature>();
        Map<String, String> processedList = new HashMap<String, String>();

        for (int i = 0; i < featureList.size(); i++) {
            Feature feature = featureList.get(i);
            Map<String, Object> attributes = feature.getAttributes();
            String uniqueFieldValue = "";

            if (attributes.containsKey(uniqueField)) {
                uniqueFieldValue = "" + attributes.get(uniqueField);
            }

            if (!processedList.containsKey(uniqueFieldValue)) {
                // Register key as processed
                processedList.put(uniqueFieldValue, uniqueField);

                // search for the last occurrence and use it
                for (int j = i + 1; j < featureList.size(); j++) {
                    Feature feature2 = featureList.get(j);
                    Map<String, Object> attributes2 = feature2.getAttributes();
                    String uniqueFieldValue2 = "";

                    if (attributes2.containsKey(uniqueField)) {
                        uniqueFieldValue2 = "" + attributes2.get(uniqueField);
                    }

                    if (uniqueFieldValue.equals(uniqueFieldValue2)) {
                        feature = feature2;
                    }
                }
                // insert the last occurrence of given feature into result list
                resultList.add(feature);

            }
        }

        return resultList;
    }

    /**
     * 
     * @param featureArray
     * @param uniqueField
     * @return comma separated list with uniqueField values.
     * @throws ArcgisException
     */
    protected String getUniqueFieldList(List<Feature> featureArray, String uniqueField)
            throws ArcgisException {
        StringBuffer result = new StringBuffer();
        String separator = "";
        boolean quoted = true;

        // Checks if field exists in table, an retrieves it's type
        if (hasAttribute(uniqueField)) {
            String uniqueFieldType = getAttributeType(uniqueField);
            quoted = GisAttributeType.DATE.toString().equalsIgnoreCase(uniqueFieldType)
                || GisAttributeType.STRING.toString().equalsIgnoreCase(uniqueFieldType);
            LOGGER.debug("uniqueField: " + uniqueField +  " uniqueFieldType " + uniqueFieldType + " in table");
            // Make the list
            for (Feature feature : featureArray) {
                if (feature.getAttributes().containsKey(uniqueField)) {
                    String value = feature.getAttributes().get(uniqueField).toString();
                    if (quoted) {
                        value = "'" + value + "'";
                    }
                    result.append(separator + value);
                    separator = ",";
                }
            }
        } else {
            LOGGER.error("Can't find attribute " + uniqueField + "in table");
            throw new ArcgisException("Can't find attribute " + uniqueField + "in table");
        }
        return result.toString();
    }

    /**
     * 
     * @param featureArray
     * @param serverFeatures
     * @param ExistentFeatures
     * @param newFeatures
     * @param uniqueField
     * @throws ArcgisException
     */
    protected void splitFeatureListIfExists(List<Feature> featureArray,
            List<Feature> serverFeatures, List<Feature> existentFeatures, List<Feature> newFeatures,
            String uniqueField) throws ArcgisException {

        for (Feature feature : featureArray) {
            boolean found = false;
            String featureId = null;
            if (feature.getAttributes().containsKey(uniqueField)) {
                featureId = feature.getAttributes().get(uniqueField).toString();
            }

            int i = 0;
            if (serverFeatures != null) {
                while (!found && i < serverFeatures.size()) {
                    Feature serverFeature = serverFeatures.get(i);
                    String serverFeatureId = null;
                    if ( serverFeature.getAttributes().containsKey(uniqueField)) {
                        serverFeatureId = serverFeature.getAttributes().get(uniqueField).toString();
                    }
                    if (featureId.equalsIgnoreCase(serverFeatureId)) {
                        found = true;
                        Integer oid = serverFeature.getObjectId();
                        LOGGER.debug("retrieved ObjectId: " + oid + " from feature " + serverFeatureId);
                        if (!oid.equals(-1)) {
                            feature.setObjectId(oid);
                        } else {
                            Integer gid = serverFeature.getGlobalId();
                            if (!gid.equals(-1)) {
                                LOGGER.info(" GlobalId " + gid + " found in serverFeature " + serverFeatureId);
                                feature.setGlobalId(gid);
                            } else {
                                LOGGER.warn("None ObjectId neither GlobalId were found in serverFeature " + serverFeatureId);
                                feature.setObjectId(oid);
                            }
                        }
                        existentFeatures.add(feature);
                    }
                    i++;
                }
            }

            if (!found) {
                newFeatures.add(feature);
            }
        }
    }

    /**
     * Clear both result lists, success and error entities and error flags.
     * 
     */
    public void clearResults() {
        if (!flushingBatch.get()) {
            this.error.set(false);
            this.errorCode = 0;
            this.errorDesc = "";
        }
    }

    /**
     * @return the batchSize
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * @param batchSize
     *            the batchSize to set
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * @return the batchAction
     */
    public int getBatchAction() {
        return batchAction;
    }

    /**
     * @param batchAction
     *            the batchAction to set
     * @throws ArcGisException
     */
    public void setBatchAction(int batchAction) throws ArcgisException {
        if (batchAction < ADD_ACTION || batchAction > ADD_UPDATE_ACTION) {
            throw new ArcgisException("setBatchAction, invalid Action: " + batchAction);
        } else if (featureBatch.size() == 0) {
            this.batchAction = batchAction;
        } else {
            throw new ArcgisException("Can't change action while processing commits.");
        }
    }

    /**
     * @return the uniqueIdField
     * @throws ArcgisException
     */
    public String getUniqueIdField() throws ArcgisException {
        if (arcGISFeatureTable.hasUniqueIdField()) {
            return this.arcGISFeatureTable.getUniqueIdField();
        } else {
            throw new ArcgisException("No uniqueIdField in Feature table.");
        }
    }

    /**
     * @return the uniqueIdField
     */
    public boolean hasUniqueIdField() {
        return this.arcGISFeatureTable.hasUniqueIdField();
    }

    /**
     * Where clause like "upper(COUNTRY_NAME) LIKE 'Spain,France,UK'".
     * 
     * @param whereClause
     * @return
     */
    private AtomicBoolean processingQuery = new AtomicBoolean(false);

    /**
     * 
     * @param whereClause
     * @return
     */
    public List<Feature> queryFeatures(String whereClause) {
        List<Feature> resultFeatureList = null;
        if (this.hasError()) {
            LOGGER.error("queryFeatures: Arcgis Error: " + this.getErrorCode());
            LOGGER.error("queryFeatures: Arcgis Error Description: " + this.getErrorDesc());
            return resultFeatureList;
        }

        LOGGER.debug("queryFeatures - processingQuery: " + processingQuery.get());

        LOGGER.debug("queryFeatures - processingQuery: Process init.");

        try {
            resultFeatureList = arcGISFeatureTable.getFeatureList(whereClause);
            LOGGER.debug("queryFeatures - processingQuery: Returning Results. "
                    + resultFeatureList.size());
        } catch (ArcgisException e) {
            LOGGER.error(e.getMessage());
            setError(e);
        }

        return resultFeatureList;
    }

    /**
     * Commit changes to Feature table.
     * 
     * @param featureArray
     * @param action
     *            ADD_ACTION or UPDATE_ACTION
     */
    private void commitFeatures(final List<Feature> featureList, int action) {
        LOGGER.debug("Init commitFeatures()");

        // check features can be added, based on edit capabilities
        if (connected) {

            int sizeList = featureList.size();

            if (!hasError() && sizeList > 0) {

                synchronized (featureBatch) {
                    LOGGER.info("Adding entities to Arcgis: " + sizeList);
                    // Esperamos a que termine de insertar el paquete anterior
                    try {
                        switch (action) {
                        case ADD_ACTION:
                            arcGISFeatureTable.addFeatureList(featureList);
                            break;
                        case UPDATE_ACTION:
                            arcGISFeatureTable.updateFeatureList(featureList);
                            break;
                        default:
                            LOGGER.error("commitFeatures: Invalid Action");
                            this.error.set(true);
                        }
                    } catch (ArcgisException e) {
                        LOGGER.error(e.getMessage());
                        setError(new Exception(e.getMessage()));
                    }

                    LOGGER.debug(
                            "pendingFeatures.commitFeatures adding listener to commitFeatureFuture: "
                                    + featureList.size());
                }
            } else {
                LOGGER.error("WARN - Argis.commitFeatures called with " + sizeList + " entities and hasError() " + hasError());
            }

        } else {
            LOGGER.error("Can't commit Entities.");
            this.error.set(true);
        }
    }

    /**
     * 
     * @return
     */
    public boolean hasError() {
        return error.get();
    }

    /**
     * 
     * @param e
     *            exception object
     */
    private void setError(Exception e) {
        // executionException may contain an ArcGISRuntimeException with edit
        // error information.
        this.error.set(true);
        this.errorDesc = e.getMessage();

        if (e != null && e.getCause() instanceof ArcgisException) {
            ArcgisException agsEx = (ArcgisException) e.getCause();
            LOGGER.error("Error Code: " + agsEx.getCode());
            LOGGER.error("Error Message: " + agsEx.getMessage());

            this.errorCode = agsEx.getCode();
        } else {
            if (e != null && e.getCause() != null) {
                if (e.getCause() instanceof SocketException) {
                    connected = false;
                }
                LOGGER.error("Error Cause: " + e.getCause().getMessage());
            }
            LOGGER.error("Error Message: " + e.getMessage());

            this.errorCode = -1;
        }
    }

    /**
     * @return the errorDesc
     */
    public String getErrorDesc() {
        if (hasError()) {
            return errorDesc;
        } else {
            return "";
        }
    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        if (hasError()) {
            return errorCode;
        } else {
            return 0;
        }
    }

    /**
     * 
     * @param attName
     * @return
     */
    public boolean hasAttribute(String attName) {
        return arcGISFeatureTable.getTableAttributes().containsKey(attName);
    }

    /**
     * 
     * @param attName
     * @return
     */
    public String getAttributeType(String attName) {
        Field attribute = arcGISFeatureTable.getTableAttributes().get(attName);
        return attribute.getType().toString();
    }

    /**
     * @return the error
     */
    protected AtomicBoolean getError() {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    protected void setError(AtomicBoolean error) {
        this.error = error;
    }

    /**
     * @param errorDesc
     *            the errorDesc to set
     */
    protected void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    /**
     * @param errorCode
     *            the errorCode to set
     */
    protected void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Cleans feature attributes before sending it to Gis
     * 
     * @param feature
     * @return
     */
    protected Feature cleanFeature(Feature feature) {
        Map<String, Object> attributes = feature.getAttributes();
        List<String> attrsToRemove = new ArrayList<String>();

        for (Map.Entry<String, Object> attrEntry : attributes.entrySet()) {
            String attName = attrEntry.getKey();
            Object attValue = attrEntry.getValue();
            if (hasAttribute(attName)) {
                Field attribute = arcGISFeatureTable.getTableAttributes().get(attName);

                // Parse value if needed
                attrEntry.setValue(GisAttributeType.parseAttValue(attribute.getType(), attValue));
            } else {
                attrsToRemove.add(attName);
            }
        }

        for (String attName : attrsToRemove) {
            LOGGER.debug("ArcgisFeatureTable, cleanFeature: Field not found in table, ignoring it, "
                    + attName);
            attributes.remove(attName);
        }

        return feature;
    }

    /**
     * 
     * @return
     */
    public boolean connected() {
        return this.connected;
    }

}
