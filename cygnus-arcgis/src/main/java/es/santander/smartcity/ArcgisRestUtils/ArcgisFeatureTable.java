package es.santander.smartcity.ArcgisRestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import es.santander.smartcity.arcgisutils.baselogger.BaseLogger;
import es.santander.smartcity.arcgisutils.baselogger.BaseLoggerInterface;
import es.santander.smartcity.exceptions.ArcgisException;
import es.santander.smartcity.model.Credential;
import es.santander.smartcity.model.Feature;
import es.santander.smartcity.model.Field;
import es.santander.smartcity.model.GisAttributeType;
import es.santander.smartcity.model.UserCredential;


public class ArcgisFeatureTable extends BaseLogger{
	protected static final Logger logger = Logger.getLogger(ArcgisFeatureTable.class);
	
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
	
	protected ArcgisFeatureTable() {
		connected = false;
		batchSize = 10;
		error = new AtomicBoolean(false);
		flushingBatch = new AtomicBoolean(false);
		arcGISFeatureTable = null;
	}
	
    protected void initFeatureTable(String url, String user, String password, String tokenGenUrl, boolean readOnly) {
		
		logDebug("Arcgis constructor.. " + url);

		logDebug("Arcgis url.. " + url);
		logDebug("Arcgis user.. " + user);
		logDebug("Arcgis password.. " + password);
		logDebug("Arcgis tokenGenUrl.. " + tokenGenUrl);
		logDebug("Arcgis readOnly.. " + readOnly);
		
		Credential credential = new UserCredential ( user, password);
		try {
			arcGISFeatureTable = new RestFeatureTable(url, credential, tokenGenUrl, this);
			logDebug ("Recovering attribute info from feature table. ->" + url);
			arcGISFeatureTable.getTableAttributesInfo();
			logDebug ("Table successfully connected.");
			connected = true;
		} catch (ArcgisException e) {
			logError ("Argis error: (" + e.getMessage() + ")");
			logError ("          URL: " + url);
			logError ("  tokenGenURL: " + tokenGenUrl);
			connected = false;
			setError(e); 
			this.errorDesc += "    " + url + "  -  " + tokenGenUrl;
		} catch (Exception e){
			connected = false;
			setError(e);
			this.errorDesc = "Unexpected exception: " + e.toString();
		}
	}
    
    /**
     * Constructor with bubble logs.
     * @param url
     * @param user
     * @param password
     * @param tokenGenUrl
     * @param readOnly
     * @param parentLogger
     */
    public ArcgisFeatureTable(String url, String user, String password, String tokenGenUrl, boolean readOnly, BaseLoggerInterface parentLogger) {
    	this.parentLogger = parentLogger;
    }
    
    	    
	/**
	 * @return the uniqueField
	 */
	public String getUniqueField() {
		return uniqueField;
	}

	/**
	 * @param uniqueField the uniqueField to set
	 */
	public void setUniqueField(String uniqueField) {
		this.uniqueField = uniqueField;
	}

	/**
	 * Add Feature to batch
	 * @param feature
	 */
    public  void addToBatch(Feature feature) {
    	synchronized(featureBatch){
	    	this.error.set(false);
	        
	    	if (connected) {
	
	    		logDebug("Arcgis: Adding feature to Batch... ");
	    		featureBatch.add(feature);
	    		
	    		if (featureBatch.size() >= getBatchSize()){
	    			logDebug("Sending Batch...");
	    			List<Feature> batch = new ArrayList<Feature>(featureBatch);
		        	featureBatch.clear();
		        	try{
				        if (batchAction == ADD_UPDATE_ACTION){
				        	addUpdateFeatures(batch, uniqueField);
				        } else {
				    		this.commitFeatures(batch, batchAction);		        	
				        }
		        	}catch (ArcgisException e){
		        		setError(e);
		        	}
		        	
	    		}
	    		
	    	} else {
	    		logDebug("Can't add Entitie.");
	            this.error.set(true);
	        }
	    }
    }
    
    /**
     * Add list of Feature to batch
     * @param listEntities
     */
    public synchronized void addToBatch(List<Feature> listEntities) {
    	synchronized(featureBatch){
	        for (Feature feature : listEntities) {
	            addToBatch(feature);
	        }
	    }
        
    }
    
    public int featuresBatched(){
    	return featureBatch.size() + updateBatch.size() + addBatch.size();
    }

    /**
	 * Force batch procesing.
	 * 
	 */
    public void flushBatch() {
    	synchronized(featureBatch){
    		clearResults();
    		try{
    			int batchesSize = featuresBatched();
		    	if( batchesSize>0 && !flushingBatch.get() && !hasError()){
		    		flushingBatch.set(true);
					logDebug("Flushing Batch...");
	
			        this.error.set(false);
		        	List<Feature> batch = new ArrayList<Feature>(featureBatch);
		        	featureBatch.clear();
		        	
		        	// Force to send all features
		        	int batchSizeBackup = this.batchSize;
		        	this.batchSize = 1;
		        	
			        if (batchAction == ADD_UPDATE_ACTION){
			        	addUpdateFeatures(batch, uniqueField);
			        } else {
			    		this.commitFeatures(batch, batchAction);		        	
			        }
			        this.batchSize = batchSizeBackup;
			        
		    		flushingBatch.set(false);
		    	}
    		}catch(ArcgisException e){
    			setError(e);
    		}
	    }
    } 
    
    /**
     * Adds Entities to Feature Table
     * @param featureArray List of etnities to add.
     * @throws ArcGisException 
     */
    public void addFeatures(List<Feature> featureArray) throws ArcgisException{
    	clearResults();
    	commitFeatures(featureArray, ADD_ACTION);
    }
    
    /**
     * Updates Entities in Feature table.
     * Unique attributes must have been set.
     * @param featureArray List of entities to update.
     * @throws ArcGisException 
     */
    public void updateFeatures(List<Feature> featureArray) throws ArcgisException{
    	clearResults();
    	commitFeatures(featureArray, UPDATE_ACTION);
    }
    
    
    /**
     * 
     * @param featureArray
     * @param uniqueField
     */
    public void addUpdateFeatures (List<Feature> featureArray, String uniqueField) throws ArcgisException{
    	clearResults();
    	if ( featureArray != null && featureArray.size()>0){
	    	String keyList = getUniqueFieldList(featureArray, uniqueField);
	    	String whereClause = uniqueField + " IN (" + keyList + ")";
	    	
	    	List<Feature> foundFeatures = queryFeatures(whereClause);
	    	splitFeatureListIfExists (featureArray, foundFeatures, updateBatch, addBatch, uniqueField);
    	}

    	if (addBatch.size() >= batchSize){
    		addBatch = removeDuplicates(addBatch, uniqueField);
    		commitFeatures(addBatch, ADD_ACTION);
    		addBatch.clear();
    	}
    	
    	if (updateBatch.size() >= batchSize){
    		commitFeatures(updateBatch, UPDATE_ACTION);
    		updateBatch.clear();
    	}
    	
    	
    	
    }
    
    /**
     * Removes duplicate features from list given a field that should be unique, last occurrence remains.
     * @param featureList
     * @param uniqueField
     * @return
     */
    protected List<Feature> removeDuplicates (List<Feature> featureList, String uniqueField){
    	List<Feature> resultList = new ArrayList<Feature>();
    	Map<String,String>  processedList = new HashMap<String, String>();
    	
    	for (int i = 0; i < featureList.size(); i++) {
    		Feature feature = featureList.get(i);
    		Map<String, Object> attributes = feature.getAttributes();
    		String uniqueFieldValue = "";
    		
			if (attributes.containsKey(uniqueField) ){
				uniqueFieldValue = "" + attributes.get(uniqueField);
			}
			
			if (!processedList.containsKey(uniqueFieldValue)){
				// Register key as processed
				processedList.put(uniqueFieldValue, uniqueField);
				
				// search for the last occurrence and use it
				for (int j = i+1; j < featureList.size(); j++){
					Feature feature2 = featureList.get(j);
		    		Map<String, Object> attributes2 = feature2.getAttributes();
					String uniqueFieldValue2 = "";
		    		
					if (attributes2.containsKey(uniqueField) ){
						uniqueFieldValue2 = "" + attributes2.get(uniqueField);
					}
					
					if (uniqueFieldValue.equals(uniqueFieldValue2)){
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
    protected String getUniqueFieldList (List<Feature> featureArray, String uniqueField ) throws ArcgisException{
    	StringBuffer result = new StringBuffer();
    	String separator = "";
    	boolean quoted = true;
    	
    	//Checks if field exists in table, an retrieves it's type
    	if ( hasAttribute(uniqueField)){
	    	String uniqueFieldType = getAttributeType(uniqueField);
	    	quoted = GisAttributeType.DATE.equals(uniqueFieldType) || GisAttributeType.STRING.equals(uniqueFieldType);
	    	
	    	// Make the list
	    	for (Feature feature : featureArray) {
				if(feature.getAttributes().containsKey(uniqueField)){
					String value = feature.getAttributes().get(uniqueField).toString();
					if (quoted) value = "'" + value + "'";
					result.append(separator + value);
					separator = ",";
				}
			}
    	} else {
    		logError("Can't find attribute " + uniqueField + "in table");
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
    protected void splitFeatureListIfExists (
    		List<Feature> featureArray, 
    		List<Feature> serverFeatures,
    		List<Feature> existentFeatures, 
    		List<Feature> newFeatures, 
    		String uniqueField) throws ArcgisException{
    	
    	for (Feature feature : featureArray) {
			boolean found = false;
			String featureId = feature.getAttributes().get(uniqueField).toString();

			int i = 0;
			while (!found && i < serverFeatures.size()){
				Feature serverFeature = serverFeatures.get(i);
				String serverFeatureId = serverFeature.getAttributes().get(uniqueField).toString();
						
				if (featureId.equals(serverFeatureId)){
					found = true;
					Integer oid =  serverFeature.getObjectId();
					feature.setObjectId( oid );
					existentFeatures.add(feature);
				}
				i++;
			}
			
			if (!found) newFeatures.add(feature);
		}
    }
    /**
     * Clear both result lists, success and error entities and error flags.
     * @throws ArcGisException Can't clear results while running commits.
     */
    public void clearResults() {
    	if (!flushingBatch.get()){
	//    	errorFeatureList.clear();
	//    	successFeatureList.clear();   
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
	 * @param batchSize the batchSize to set
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
	 * @param batchAction the batchAction to set
	 * @throws ArcGisException 
	 */
	public void setBatchAction(int batchAction) throws ArcgisException {
		if(batchAction < ADD_ACTION || batchAction > ADD_UPDATE_ACTION){
			throw new ArcgisException ("setBatchAction, invalid Action: " + batchAction);
		} else if ( featureBatch.size() == 0){
			this.batchAction = batchAction;
		}else {
    		throw new ArcgisException("Can't change action while processing commits.");
    	}
	}
	
	/**
	 * @return the uniqueIdField
	 * @throws ArcgisException 
	 */
	public String getUniqueIdField() throws ArcgisException {
		if (arcGISFeatureTable.hasUniqueIdField()){
			return this.arcGISFeatureTable.getUniqueIdField();
		}else{
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
     * 
     * Where clause like "upper(COUNTRY_NAME) LIKE 'Spain,France,UK'"
     * @param whereClause
     * @return
     */
    private AtomicBoolean processingQuery = new AtomicBoolean(false); 
    public List<Feature> queryFeatures (String whereClause){
    	List<Feature> resultFeatureList = null;
    	if(this.hasError()){
    		logError("queryFeatures: Arcgis Error: " + this.getErrorCode());
            logError("queryFeatures: Arcgis Error Description: " + this.getErrorDesc());
    		return resultFeatureList;
    	}
    	
    	logDebug("queryFeatures - processingQuery: " + processingQuery.get());
    	
    	logDebug("queryFeatures - processingQuery: Process init.");
    	    	
    	try{
    		resultFeatureList = arcGISFeatureTable.getFeatureList(whereClause);
    		logDebug("queryFeatures - processingQuery: Returning Results. " + resultFeatureList.size());
    	}catch (ArcgisException e){
    		logError(e.getMessage());
    		setError(e);
    	}
    	
        return resultFeatureList;
    }


    /**
     * Commit changes to Feature table.
     * @param featureArray
     * @param action  ADD_ACTION or UPDATE_ACTION
     */
    private void commitFeatures(final List<Feature> featureList, int action) {
        logDebug("Init commitFeatures()");
                
        // check features can be added, based on edit capabilities
        if (connected) {
        	
            
            int sizeList = featureList.size();
            
            if (!hasError() && sizeList>0 ){

            	synchronized(featureBatch){
	                logBasic("Adding entities to Arcgis: " + sizeList);
	                // Esperamos a que termine de insertar el paquete anterior
	                try{
		                switch (action){
		        		case ADD_ACTION:
		        			arcGISFeatureTable.addFeatureList(featureList);
		        			break;
		        		case UPDATE_ACTION:
		        			arcGISFeatureTable.updateFeatureList(featureList);
		        			break;
		        		default:
		        			logError("commitFeatures: Invalid Action");
		        			this.error.set(true);
		        		}
		             } catch (ArcgisException e) {
		            	 logError(e.getMessage());
		            	 setError(new Exception(e.getMessage()));
		             }
		            
		            logDebug("pendingFeatures.commitFeatures adding listener to commitFeatureFuture: " + featureList.size());
            	}     
            } else {
            	logError ("WARN - Argis.commitFeatures called with 0 entities.");
            }
            
        } else {
            logError("Can't commit Entities.");
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
     * @param e exception object
     */
    private void setError(Exception e) {
        // executionException may contain an ArcGISRuntimeException with edit
        // error information.
        this.error.set(true);
    	this.errorDesc = e.getMessage();
    	
        if (e!=null && e.getCause() instanceof ArcgisException) {
        	ArcgisException agsEx = (ArcgisException) e.getCause();
            logError("Error Code: " + agsEx.getCode());
            logError("Error Message: " + agsEx.getMessage());

        	this.errorCode = agsEx.getCode();
        } else {
            if(e != null && e.getCause() != null) {
                logError("Error Cause: " + e.getCause().getMessage());
            }
        	logError("Error Message: " + e.getMessage());
        	
        	this.errorCode = -1;
        }
    }
    
    /**
	 * @return the errorDesc
	 */
	public String getErrorDesc() {
		if (hasError()){
			return errorDesc;
		}else{
			return "";
		}
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		if (hasError()){
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
	 * @param error the error to set
	 */
	protected void setError(AtomicBoolean error) {
		this.error = error;
	}
	/**
	 * @param errorDesc the errorDesc to set
	 */
	protected void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}
	/**
	 * @param errorCode the errorCode to set
	 */
	protected void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean connected(){
		return this.connected;
	}

}
