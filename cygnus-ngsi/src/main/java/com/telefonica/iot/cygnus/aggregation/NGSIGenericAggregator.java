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

package com.telefonica.iot.cygnus.aggregation;

import com.google.gson.JsonElement;
import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.NGSIConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * The type Ngsi generic aggregator.
 */
public abstract class NGSIGenericAggregator {

    // Logger
    private static final CygnusLogger LOGGER = new CygnusLogger(NGSIGenericAggregator.class);

    private static final String DEFAULT_ATTR_NATIVE_TYPES = "false";

    protected LinkedHashMap<String, ArrayList<JsonElement>> aggregation;

    private String service;
    private String servicePathForData;
    private String servicePathForNaming;
    private String entityForNaming;
    private String entityType;
    private String attribute;
    private String dbName;
    private String tableName;
    private String collectionName;
    private String orgName;
    private String pkgName;
    private String resName;
    private boolean attrNativeTypes;
    protected boolean enableGrouping;
    protected boolean enableEncoding;
    protected boolean enableNameMappings;
    private boolean enableGeoParse;

    public boolean isEnableGeoParse() {
        return enableGeoParse;
    }

    public void setEnableGeoParse(boolean enableGeoParse) {
        this.enableGeoParse = enableGeoParse;
    }

    public LinkedHashMap<String, ArrayList<JsonElement>> getAggregation() {
        if (aggregation == null) {
            return new LinkedHashMap<>();
        } else {
            return aggregation;
        }
    }

    public void setAggregation(LinkedHashMap<String, ArrayList<JsonElement>> aggregation) {
        this.aggregation = aggregation;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServicePathForData() {
        return servicePathForData;
    }

    public void setServicePathForData(String servicePathForData) {
        this.servicePathForData = servicePathForData;
    }

    public String getServicePathForNaming() {
        return servicePathForNaming;
    }

    public void setServicePathForNaming(String servicePathForNaming) {
        this.servicePathForNaming = servicePathForNaming;
    }

    public String getEntityForNaming() {
        return entityForNaming;
    }

    public void setEntityForNaming(String entityForNaming) {
        this.entityForNaming = entityForNaming;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getDbName(boolean enableLowercase) {
        if (enableLowercase) {
            return dbName.toLowerCase();
        } else {
            return dbName;
        }
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName(boolean enableLowercase) {
        if (enableLowercase) {
            return tableName.toLowerCase();
        } else {
            return tableName;
        }
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public boolean isAttrNativeTypes() {
        return attrNativeTypes;
    }

    public void setAttrNativeTypes(boolean attrNativeTypes) {
        this.attrNativeTypes = attrNativeTypes;
    }

    public boolean isEnableGrouping() {
        return enableGrouping;
    }

    public void setEnableGrouping(boolean enableGrouping) {
        this.enableGrouping = enableGrouping;
    }

    public boolean isEnableEncoding() {
        return enableEncoding;
    }

    public void setEnableEncoding(boolean enableEncoding) {
        this.enableEncoding = enableEncoding;
    }

    public boolean isEnableNameMappings() {
        return enableNameMappings;
    }

    public void setEnableNameMappings(boolean enableNameMappings) {
        this.enableNameMappings = enableNameMappings;
    }

    /**
     * Aggregate.
     *
     * @param cygnusEvent the cygnus event
     */
    public abstract void aggregate(NGSIEvent cygnusEvent);

    /**
     * Initialize.
     *
     * @param cygnusEvent the cygnus event
     */
    public abstract void initialize(NGSIEvent cygnusEvent);

}
