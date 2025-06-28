/**
 * Copyright 2014-2020 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.aggregation;

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.utils.NGSIUtils;

import java.util.*;

/**
 * The type Ngsi generic aggregator.
 */
public abstract class NGSIGenericAggregator {

    /**
     * The Aggregation of processed entityes.
     */
    protected LinkedHashMap<String, ArrayList<JsonElement>> aggregation;

    /**
     * The Aggregation of processed entityes.
     */
    protected LinkedHashMap<String, ArrayList<JsonElement>> lastData;

    /**
     * The Aggregation of processed entityes to be deleted.
     */
    protected LinkedHashMap<String, ArrayList<JsonElement>> lastDataDelete;

    /**
     * The Md aggregations for sinks who store on a diferent destination metadata.
     */
    protected Map<String, String> mdAggregations;

    private String service;
    private String servicePathForData;
    private String servicePathForNaming;
    private String entityForNaming;
    private String entityType;
    private String attribute;
    private String schemeName;
    private String dbName;
    private String tableName;
    private String collectionName;
    private String orgName;
    private String pkgName;
    private String resName;
    private String hdfsFolder;
    private String hdfsFile;
    private String hiveFields;
    private String csvString;
    private String lastDataTimestampKey;
    private String lastDataUniqueKey;
    private boolean attrNativeTypes;
    private boolean enableEncoding;
    private boolean enableNameMappings;
    private boolean enableGeoParse;
    private boolean enableGeoParseOracle;
    private boolean enableGeoParseOracleLocator;
    private boolean attrMetadataStore;
    private boolean enableUTCRecvTime;
    private String lastDataMode;
    private long lastDataTimestamp;
    private String lastDataTimestampKeyOnAggregation;
    private String lastDataUniqueKeyOnAggregation;

    /**
     * Gets aggregation.
     *
     * @return the aggregation
     */
    public LinkedHashMap<String, ArrayList<JsonElement>> getAggregation() {
        if (aggregation == null) {
            return new LinkedHashMap<>();
        } else {
            return aggregation;
        }
    } //getAggregation

    /**
     * Gets aggregation to persist. This means that the returned aggregation will not have metadata
     * in case that attrMetadataStore is set to false. Also, added fields for processing purposes
     * will be removed from the aggregation (like attrType on Column mode).
     *
     * @return the aggregation to persist
     */
    public LinkedHashMap<String, ArrayList<JsonElement>> getAggregationToPersist() {
        if (aggregation == null) {
            return new LinkedHashMap<>();
        } else {
            return NGSIUtils.linkedHashMapWithoutDefaultFields(aggregation, attrMetadataStore);
        }
    } //getAggregationToPersist

    /**
     * Sets aggregation.
     *
     * @param aggregation the aggregation
     */
    public void setAggregation(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        this.aggregation = aggregation;
    } //setAggregation

    /**
     * Gets last data.
     *
     * @return the last data
     */
    public LinkedHashMap<String, ArrayList<JsonElement>> getLastData() {
        if (lastData == null) {
            return new LinkedHashMap<>();
        } else {
            return lastData;
        }
    }

    /**
     * Gets last data.
     *
     * @return the last data delete
     */
    public LinkedHashMap<String, ArrayList<JsonElement>> getLastDataDelete() {
        if (lastDataDelete == null) {
            return new LinkedHashMap<>();
        } else {
            return lastDataDelete;
        }
    }

    /**
     * Sets last data
     *
     * @param lastData the last data
     */
    public void setLastData(LinkedHashMap<String, ArrayList<JsonElement>> lastData) {
        this.lastData = lastData;
    }

    /**
     * Sets last data delete
     *
     * @param lastData the last data delete
     */
    public void setLastDataDelete(LinkedHashMap<String, ArrayList<JsonElement>> lastDataDelete) {
        this.lastDataDelete = lastDataDelete;
    }

    /**
     * Gets last data to persist.This means that the returned aggregation will not have metadata
     * in case that attrMetadataStore is set to false. Also, added fields for processing purposes
     * will be removed from the aggregation (like attrType on Column mode).
     *
     * @return the last data
     */

    public LinkedHashMap<String, ArrayList<JsonElement>> getLastDataToPersist() {
        if (lastData == null) {
            return new LinkedHashMap<>();
        } else {
            return NGSIUtils.linkedHashMapWithoutDefaultFields(lastData, attrMetadataStore);
        }
    }

    /**
     * Gets last data delete to persist.This means that the returned aggregation will not have metadata
     * in case that attrMetadataStore is set to false. Also, added fields for processing purposes
     * will be removed from the aggregation (like attrType on Column mode).
     *
     * @return the last data delete
     */

    public LinkedHashMap<String, ArrayList<JsonElement>> getLastDataDeleteToPersist() {
        if (lastDataDelete == null) {
            return new LinkedHashMap<>();
        } else {
            return NGSIUtils.linkedHashMapWithoutDefaultFields(lastDataDelete, attrMetadataStore);
        }
    }

    /**
     * Gets collection name.
     *
     * @param enableLowercase the enable lowercase
     * @return the collection name
     */
    public String getCollectionName(boolean enableLowercase) {
        if (enableLowercase) {
            return collectionName.toLowerCase();
        } else {
            return collectionName;
        }
    } //getCollectionName

    /**
     * Gets md aggregations.
     *
     * @return the md aggregations
     */
    public Map<String, String> getMdAggregations() {
        if (mdAggregations == null) {
            return new HashMap<>();
        } else {
            return mdAggregations;
        }
    } //getMdAggregations

    /**
     * Gets csv string. For HDFS sink.
     *
     * @return the csv string
     */
    public String getCsvString() {
        return csvString;
    } //getCsvString

    /**
     * Sets csv string.
     *
     * @param csvString the csv string
     */
    public void setCsvString(String csvString) {
        this.csvString = csvString;
    } //setCsvString

    /**
     * Gets last data timestamp key.
     *
     * @return the last value timestamp key
     */
    public String getLastDataTimestampKey() {
        return lastDataTimestampKey;
    }

    /**
     * Sets last data timestamp key.
     *
     * @param lastDataTimestampKey the last value timestamp key
     */
    public void setLastDataTimestampKey(String lastDataTimestampKey) {
        this.lastDataTimestampKey = lastDataTimestampKey;
    }

    /**
     * Get LastDataMode string
     *
     * @return the boolean
     */
    public String getLastDataMode() {
        return lastDataMode;
    }

    /**
     * Is enable last data boolean.
     *
     * @return the boolean
     */
    public boolean isEnableLastData() {
        if (lastDataMode == null) {
            return false;
        } else {
            return lastDataMode.equals("upsert") || lastDataMode.equals("both");
        }
    }

    /**
     * Sets last data mode.
     *
     * @param lastDataMode the last data mode to set
     */
    public void setLastDataMode(String lastDataMode) {
        this.lastDataMode = lastDataMode;
    }

    /**
     * Gets last data unique key.
     *
     * @return the last data unique key
     */
    public String getLastDataUniqueKey() { return lastDataUniqueKey; }

    /**
     * Sets last data unique key.
     *
     * @param lastDataUniqueKey the last data unique key
     */
    public void setLastDataUniqueKey(String lastDataUniqueKey) { this.lastDataUniqueKey = lastDataUniqueKey; }

    /**
     * Gets last data tiemstamp key on aggregation.
     *
     * @return the last data tiemstamp key on aggregation
     */
    public String getLastDataTimestampKeyOnAggregation() { return lastDataTimestampKeyOnAggregation; }

    /**
     * Sets last data tiemstamp key on aggregation.
     *
     * @param lastDataTimestampKeyOnAggregation the last data tiemstamp key on aggregation
     */
    public void setLastDataTimestampKeyOnAggregation(String lastDataTimestampKeyOnAggregation) { this.lastDataTimestampKeyOnAggregation = lastDataTimestampKeyOnAggregation; }

    /**
     * Gets last data key on aggregation.
     *
     * @return the last data key on aggregation
     */
    public String getLastDataUniqueKeyOnAggregation() { return lastDataUniqueKeyOnAggregation; }

    /**
     * Sets last data key on aggregation.
     *
     * @param lastDataUniqueKeyOnAggregation the last data key on aggregation
     */
    public void setLastDataUniqueKeyOnAggregation(String lastDataUniqueKeyOnAggregation) { this.lastDataUniqueKeyOnAggregation = lastDataUniqueKeyOnAggregation; }

    /**
     * Gets hdfs folder. For HDFS sink.
     *
     * @param enableLowercase the enable lowercase
     * @return the hdfs folder
     */
    public String getHdfsFolder(boolean enableLowercase) {
        if (enableLowercase) {
            return hdfsFolder.toLowerCase();
        } else {
            return hdfsFolder;
        }
    } //getHdfsFolder

    /**
     * Sets hdfs folder.
     *
     * @param hdfsFolder the hdfs folder
     */
    public void setHdfsFolder(String hdfsFolder) {
        this.hdfsFolder = hdfsFolder;
    } //setHdfsFolder

    /**
     * Gets hdfs file.
     *
     * @param enableLowercase the enable lowercase
     * @return the hdfs file as it was stored. if enableLowercase is true, then the returned String is on lowerCase.
     */
    public String getHdfsFile(boolean enableLowercase) {
        if (enableLowercase) {
            return hdfsFile.toLowerCase();
        } else {
            return hdfsFile;
        }
    } //getHdfsFile

    /**
     * Is enable utc recv time boolean.
     *
     * @return the boolean
     */
    public boolean isEnableUTCRecvTime() {
        return enableUTCRecvTime;
    } //isEnableUTCRecvTime

    /**
     * Sets enable utc recv time. This is used to add UTC format to RECV_TIME field on aggregation.
     *
     * @param enableUTCRecvTime the enable utc recv time.
     */
    public void setEnableUTCRecvTime(boolean enableUTCRecvTime) {
        this.enableUTCRecvTime = enableUTCRecvTime;
    } //setEnableUTCRecvTime

    /**
     * Sets hdfs file.
     *
     * @param hdfsFile the hdfs file
     */
    public void setHdfsFile(String hdfsFile) {
        this.hdfsFile = hdfsFile;
    } //setHdfsFile

    /**
     * Sets md aggregations.
     *
     * @param mdAggregations the md aggregations
     */
    public void setMdAggregations(Map<String, String> mdAggregations) { this.mdAggregations = mdAggregations; } //setMdAggregations

    /**
     * Sets attr metadata store. This is used to remove metadata for aggregation. If true, then the method
     * getAggregationToPersist will crop metadata fields.
     *
     * @param attrMetadataStore the attr metadata store
     */
    public void setAttrMetadataStore(boolean attrMetadataStore) {
        this.attrMetadataStore = attrMetadataStore;
    } //setAttrMetadataStore

    /**
     * Is attr metadata store boolean.
     *
     * @return the boolean
     */
    public boolean isAttrMetadataStore() {
        return attrMetadataStore;
    } //isAttrMetadataStore

    /**
     * Is enable geo parse boolean. Postgis flag to process geometry types.
     *
     * @return the boolean
     */
    public boolean isEnableGeoParse() {
        return enableGeoParse;
    } //isEnableGeoParse

    /**
     * Sets enable geo parse.
     *
     * @param enableGeoParse the enable geo parse
     */
    public void setEnableGeoParse(boolean enableGeoParse) {
        this.enableGeoParse = enableGeoParse;
    } //setEnableGeoParse

    /**
     * Is enable geo parse boolean. Oracle flag to process geometry types.
     *
     * @return the boolean
     */
    public boolean isEnableGeoParseOracle() {
        return enableGeoParseOracle;
    } //isEnableGeoParseOracle

    /**
     * Sets enable geo parse oracle
     *
     * @param enableGeoParse the enable geo parse
     */
    public void setEnableGeoParseOracle(boolean enableGeoParseOracle) {
        this.enableGeoParseOracle = enableGeoParseOracle;
    } //setEnableGeoParseOracle

    /**
     * Is enable geo parse boolean. Oracle flag to process geometry types.
     *
     * @return the boolean
     */
    public boolean isEnableGeoParseOracleLocator() {
        return enableGeoParseOracleLocator;
    } //isEnableGeoParseOracleLocator

    /**
     * Sets enable geo parse oracle locator (12.2.)
     *
     * @param enableGeoParseLocator the enable geo parse
     */
    public void setEnableGeoParseOracleLocator(boolean enableGeoParseOracleLocator) {
        this.enableGeoParseOracleLocator = enableGeoParseOracleLocator;
    } //setEnableGeoParseOracleLocator


    /**
     * Gets long timestamp of the record stored on the last data collection
     *
     * @return lastDataTimestamp the long
     */

    public long getLastDataTimestamp() { return lastDataTimestamp; }

    /**
     * Sets long timestamp of the record stored on the last data collection
     *
     * @param lastDataTimestamp the timestamp of the record on the last data collection
     */

    public void setLastDataTimestamp(long lastDataTimestamp) { this.lastDataTimestamp = lastDataTimestamp; }



    /**
     * Sets collection name.
     *
     * @param collectionName the collection name
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    } //setCollectionName

    /**
     * Gets service.
     *
     * @return the service
     */
    public String getService() {
        return service;
    } //getService

    /**
     * Gets hive fields.
     *
     * @return the hive fields
     */
    public String getHiveFields() {
        return hiveFields;
    } //getHiveFields

    /**
     * Sets hive fields.
     *
     * @param hiveFields the hive fields
     */
    public void setHiveFields(String hiveFields) {
        this.hiveFields = hiveFields;
    } //setHiveFields

    /**
     * Sets service.
     *
     * @param service the service
     */
    public void setService(String service) {
        this.service = service;
    } //setService

    /**
     * Gets service scheme name for Postgis/Postgres
     *
     * @return the scheme name for Postgis/Postgres
     */
    public String getSchemeName(boolean enableLowercase) {
        if (enableLowercase) {
            return schemeName.toLowerCase();
        } else {
            return schemeName;
        }
    }

    /**
     * Sets service scheme name for Postgis/Postgres
     *
     * @param schemeName the scheme name for Postgis/Postgres
     */
    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }


    /**
     * Gets service path for data.
     *
     * @return the service path for data
     */
    public String getServicePathForData() {
        return servicePathForData;
    } //getServicePathForData

    /**
     * Sets service path for data.
     *
     * @param servicePathForData the service path for data
     */
    public void setServicePathForData(String servicePathForData) {
        this.servicePathForData = servicePathForData;
    } //setServicePathForData

    /**
     * Gets service path for naming.
     *
     * @return the service path for naming
     */
    public String getServicePathForNaming() {
        return servicePathForNaming;
    } //getServicePathForNaming

    /**
     * Sets service path for naming.
     *
     * @param servicePathForNaming the service path for naming
     */
    public void setServicePathForNaming(String servicePathForNaming) {
        this.servicePathForNaming = servicePathForNaming;
    } //setServicePathForNaming

    /**
     * Gets entity for naming.
     *
     * @return the entity for naming
     */
    public String getEntityForNaming() {
        return entityForNaming;
    } //getEntityForNaming

    /**
     * Sets entity for naming.
     *
     * @param entityForNaming the entity for naming
     */
    public void setEntityForNaming(String entityForNaming) {
        this.entityForNaming = entityForNaming;
    } //setEntityForNaming

    /**
     * Gets entity type.
     *
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    } //getEntityType

    /**
     * Sets entity type.
     *
     * @param entityType the entity type
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    } //setEntityType

  /**
     * Gets attribute.
     *
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    } //getAttribute

    /**
     * Sets attribute.
     *
     * @param attribute the attribute
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    } //setAttribute

    /**
     * Gets db name.
     *
     * @param enableLowercase the enable lowercase. If enableLowercase is true, then the returned String is on lowerCase.
     * @return the db name
     */
    public String getDbName(boolean enableLowercase) {
        if (enableLowercase) {
            return dbName.toLowerCase();
        } else {
            return dbName;
        }
    } //getDbName

    /**
     * Sets db name.
     *
     * @param dbName the db name
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    } //setDbName

    /**
     * Gets table name.
     *
     * @param enableLowercase the enable lowercase. If enableLowercase is true, then the returned String is on lowerCase.
     * @return the table name
     */
    public String getTableName(boolean enableLowercase) {
        if (enableLowercase) {
            return tableName.toLowerCase();
        } else {
            return tableName;
        }
    } //getTableName

    /**
     * Sets table name.
     *
     * @param tableName the table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    } //setTableName

    /**
     * Gets org name.
     *
     * @param enableLowercase the enable lowercase. If enableLowercase is true, then the returned String is on lowerCase.
     * @return the org name
     */
    public String getOrgName(boolean enableLowercase) {
        if (enableLowercase) {
            return orgName.toLowerCase();
        } else {
            return orgName;
        }
    } //getOrgName

    /**
     * Sets org name.
     *
     * @param orgName the org name
     */
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    } //setOrgName

    /**
     * Gets pkg name.
     *
     * @param enableLowercase the enable lowercase. If enableLowercase is true, then the returned String is on lowerCase.
     * @return the pkg name
     */
    public String getPkgName(boolean enableLowercase) {
        if (enableLowercase) {
            return pkgName.toLowerCase();
        } else {
            return pkgName;
        }
    } //getPkgName

    /**
     * Sets pkg name.
     *
     * @param pkgName the pkg name
     */
    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    } //setPkgName

    /**
     * Gets res name.
     *
     * @param enableLowercase the enable lowercase. If enableLowercase is true, then the returned String is on lowerCase.
     * @return the res name
     */
    public String getResName(boolean enableLowercase) {
        if (enableLowercase) {
            return resName.toLowerCase();
        } else {
            return resName;
        }
    } //getResName

    /**
     * Sets res name.
     *
     * @param resName the res name
     */
    public void setResName(String resName) {
        this.resName = resName;
    } //setResName

    /**
     * Is attr native types boolean.
     *
     * @return the boolean
     */
    public boolean isAttrNativeTypes() {
        return attrNativeTypes;
    } //isAttrNativeTypes

    /**
     * Sets attr native types.
     *
     * @param attrNativeTypes the attr native types
     */
    public void setAttrNativeTypes(boolean attrNativeTypes) {
        this.attrNativeTypes = attrNativeTypes;
    } //setAttrNativeTypes

    /**
     * Is enable encoding boolean.
     *
     * @return the boolean
     */
    public boolean isEnableEncoding() {
        return enableEncoding;
    } //isEnableEncoding

    /**
     * Sets enable encoding.
     *
     * @param enableEncoding the enable encoding
     */
    public void setEnableEncoding(boolean enableEncoding) {
        this.enableEncoding = enableEncoding;
    } //setEnableEncoding

    /**
     * Is enable name mappings boolean.
     *
     * @return the boolean
     */
    public boolean isEnableNameMappings() {
        return enableNameMappings;
    } //isEnableNameMappings

    /**
     * Sets enable name mappings.
     *
     * @param enableNameMappings the enable name mappings
     */
    public void setEnableNameMappings(boolean enableNameMappings) {
        this.enableNameMappings = enableNameMappings;
    } //setEnableNameMappings

    /**
     * getEscapedString
     *
     * @param JsonElement an UnrestrictedString
     * @param String a quotationMark to escape
     * @return the escaped string
     */
    public String getEscapedString(JsonElement value, String quotationMark) {
        String escaped = value.toString();
        switch (quotationMark) {
        case "'":
            escaped = escaped.replaceAll("'", "''");
            break;
        case "\"":
            // Currently not used but maybe in the future could be useful
            escaped = escaped.replaceAll("\"", "\"\"");
            break;
        }
        return escaped;
    }

    /**
     * Aggregate declaration for child classes.
     *
     * @param cygnusEvent the cygnus event
     */
    public abstract void aggregate(NGSIEvent cygnusEvent);

    /**
     * Initialize declaration for child classes.
     *
     * @param cygnusEvent the cygnus event
     */
    public abstract void initialize(NGSIEvent cygnusEvent);

    /**
     * Returns the timestamp to use for the RECV_TIME_TS field. 
     * @param cygnusEvent the event to produce the timestamp for.
     * @return the timestamp value to use. Default milliseconds.
     */
    public long getRecvTimeTsValue(NGSIEvent cygnusEvent) {
    	return cygnusEvent.getRecvTimeTs();
    }
    
	/**
	 * Returns a possible adapted value for attribute value. 
	 * @param attrValue The input
	 * @return the adapted output. Default unchanged, same object.
	 */
    public JsonElement adaptAttrValue(JsonElement attrValue) {
    	// Default: No adaptation.
    	return attrValue;
    }
}
