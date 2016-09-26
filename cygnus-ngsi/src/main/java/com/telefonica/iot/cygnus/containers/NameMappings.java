/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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
package com.telefonica.iot.cygnus.containers;

import java.util.ArrayList;

/**
 *
 * @author frb
 */
public class NameMappings {
    
    private final ArrayList<ServiceMapping> serviceMappings;
    
    /**
     * Constructor.
     */
    public NameMappings() {
        serviceMappings = new ArrayList<ServiceMapping>();
    } // NameMappings
    
    public ArrayList<ServiceMapping> getServiceMappings() {
        return serviceMappings;
    } // getServiceMappings
    
    /**
     * ServiceMapping class.
     */
    public class ServiceMapping {
        
        private String originalService;
        private String newService;
        private final ArrayList<ServicePathMapping> servicePathMappings;
    
        /**
         * Constructor.
         */
        public ServiceMapping() {
            servicePathMappings = new ArrayList<ServicePathMapping>();
        } // NameMappings
        
        public ArrayList<ServicePathMapping> getServicePathMappings() {
            return servicePathMappings;
        } // getServicePathMappings
        
        public String getOriginalService() {
            return originalService;
        } // getOriginalService
        
        public String getNewService() {
            return newService;
        } // getNewService
        
    } // ServiceMapping
    
    /**
     * ServicePathMapping class.
     */
    public class ServicePathMapping {
        
        private String originalServicePath;
        private String newServicePath;
        private final ArrayList<EntityMapping> entityMappings;
        
        /**
         * Constructor.
         */
        public ServicePathMapping() {
            entityMappings = new ArrayList<EntityMapping>();
        } // ServicePathMapping
        
        public ArrayList<EntityMapping> getEntityMappings() {
            return entityMappings;
        } // getEntityMappings
        
        public String getOriginalServicePath() {
            return originalServicePath;
        } // getOriginalServicePath
        
        public String getNewServicePath() {
            return newServicePath;
        } // getNewServicePath
        
    } // ServicePathMapping
    
    /**
     * EntityMapping class.
     */
    public class EntityMapping {
        
        private String originalEntityName;
        private String originalEntityType;
        private String newEntityName;
        private String newEntityType;
        private final ArrayList<AttributeMapping> attributeMappings;
        
        /**
         * Constructor.
         */
        public EntityMapping() {
            attributeMappings = new ArrayList<AttributeMapping>();
        } // EntityMapping
        
        public ArrayList<AttributeMapping> getAttributeMappings() {
            return attributeMappings;
        } // getAttributeMappings
        
        public String getOriginalEntityName() {
            return originalEntityName;
        } // getOriginalEntityName
        
        public String getOriginalEntityType() {
            return originalEntityType;
        } // getOriginalEntityType
        
        public String getNewEntityName() {
            return newEntityName;
        } // getNewEntityName
        
        public String getNewEntityType() {
            return newEntityType;
        } // getNewEntityType
        
    } // EntityMapping
    
    /**
     * AttributeMapping class.
     */
    public class AttributeMapping {
        
        private String originalAttributeName;
        private String originalAttributeType;
        private String newAttributeName;
        private String newAttributeType;
        
        /**
         * Constructor.
         */
        public AttributeMapping() {
        } // AttributeMapping
        
        public String getOriginalAttributeName() {
            return originalAttributeName;
        } // getOriginalAttributeName
        
        public String getOriginalAttributeType() {
            return originalAttributeType;
        } // getOriginalAttributeType
        
        public String getNewAttributeName() {
            return newAttributeName;
        } // getNewAttributeName
        
        public String getNewAttributeType() {
            return newAttributeType;
        } // getNewAttributeType
        
    } // AttributeMapping
    
} // NameMappings
