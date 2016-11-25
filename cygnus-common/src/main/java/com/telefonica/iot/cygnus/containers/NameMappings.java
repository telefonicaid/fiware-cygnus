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
import java.util.regex.Pattern;

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
        serviceMappings = new ArrayList<>();
    } // NameMappings
    
    public ArrayList<ServiceMapping> getServiceMappings() {
        return serviceMappings;
    } // getServiceMappings
    
    /**
     * Purges the Name Mappings if any field is missing or has an invalid value.
     */
    public void purge() {
    } // purge
    
    /**
     * Compiles the regular expressions into Java Patterns.
     */
    public void compilePatterns() {
        for (ServiceMapping serviceMapping : serviceMappings) {
            serviceMapping.compilePatterns();
        } // for
    } // compilePatterns
    
    /**
     * Overwrite of toString() method.
     * @return
     */
    @Override
    public String toString() {
        String nameMappingsStr = "{\"serviceMappings\":[";
        boolean first = true;
        
        for (ServiceMapping serviceMapping : serviceMappings) {
            if (first) {
                nameMappingsStr += serviceMapping.toString();
                first = false;
            } else {
                nameMappingsStr += "," + serviceMapping.toString();
            } // if eslse
        } // for
        
        nameMappingsStr += "]}";
        return nameMappingsStr;
    } // toString
    
    public void addServiceMapping(ServiceMapping serviceMapping) {
        serviceMappings.add(serviceMapping);
    } // addServiceMapping
    
    /**
     * ServiceMapping class.
     */
    public class ServiceMapping {
        
        private String originalService;
        private Pattern originalServicePattern;
        private String newService;
        private final ArrayList<ServicePathMapping> servicePathMappings;
    
        /**
         * Constructor.
         */
        public ServiceMapping() {
            servicePathMappings = new ArrayList<>();
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
        
        public Pattern getOriginalServicePattern() {
            return originalServicePattern;
        } // getOriginalServicePattern
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalServicePattern = Pattern.compile(originalService);

            for (ServicePathMapping servicePathMapping : servicePathMappings) {
                servicePathMapping.compilePatterns();
            } // for
        } // compilePatterns
        
        @Override
        public String toString() {
            String serviceMappingStr =
                    "{\"originalService\":\"" + getOriginalService() + "\","
                    + "\"newService\":\"" + getNewService() + "\","
                    + "\"servicePathMappings\":[";
            boolean first = true;
             
            for (Object servicePathMapping : servicePathMappings) {
                if (first) {
                    serviceMappingStr += servicePathMapping.toString();
                    first = false;
                } else {
                    serviceMappingStr += "," + servicePathMapping.toString();
                } // if else
            } // for
            
            serviceMappingStr += "]}";
            return serviceMappingStr;
        } // toString
        
    } // ServiceMapping
    
    /**
     * ServicePathMapping class.
     */
    public class ServicePathMapping {
        
        private String originalServicePath;
        private Pattern originalServicePathPattern;
        private String newServicePath;
        private final ArrayList<EntityMapping> entityMappings;
        
        /**
         * Constructor.
         */
        public ServicePathMapping() {
            entityMappings = new ArrayList<>();
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
        
        public Pattern getOriginalServicePathPattern() {
            return originalServicePathPattern;
        } // getOriginalServicePathPattern
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalServicePathPattern = Pattern.compile(originalServicePath);

            for (EntityMapping entityMapping : entityMappings) {
                entityMapping.compilePatterns();
            } // for
        } // compilePatterns
        
        @Override
        public String toString() {
            String entityMappingStr =
                    "{\"originalServicePath\":\"" + getOriginalServicePath() + "\","
                    + "\"newServicePath\":\"" + getNewServicePath() + "\","
                    + "\"entityMappings\": [";
            boolean first = true;
            
            for (Object entityMapping: entityMappings) {
                if (first) {
                    entityMappingStr += entityMapping.toString();
                    first = false;
                } else {
                    entityMappingStr += "," + entityMapping.toString();
                } // if else
            } // for
            
            entityMappingStr += "]}";
            return entityMappingStr;
        } // toString
        
    } // ServicePathMapping
    
    /**
     * EntityMapping class.
     */
    public class EntityMapping {
        
        private String originalEntityId;
        private Pattern originalEntityIdPattern;
        private String originalEntityType;
        private Pattern originalEntityTypePattern;
        private String newEntityId;
        private String newEntityType;
        private final ArrayList<AttributeMapping> attributeMappings;
        
        /**
         * Constructor.
         */
        public EntityMapping() {
            attributeMappings = new ArrayList<>();
        } // EntityMapping
        
        public ArrayList<AttributeMapping> getAttributeMappings() {
            return attributeMappings;
        } // getAttributeMappings
        
        public String getOriginalEntityId() {
            return originalEntityId;
        } // getOriginalEntityId
        
        public String getOriginalEntityType() {
            return originalEntityType;
        } // getOriginalEntityType
        
        public String getNewEntityId() {
            return newEntityId;
        } // getNewEntityId
        
        public String getNewEntityType() {
            return newEntityType;
        } // getNewEntityType
        
        public Pattern getOriginalEntityIdPattern() {
            return originalEntityIdPattern;
        } // getOriginalEntityIdPattern
        
        public Pattern getOriginalEntityTypePattern() {
            return originalEntityTypePattern;
        } // getOriginalEntityTypePattern
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalEntityIdPattern = Pattern.compile(originalEntityId);
            originalEntityTypePattern = Pattern.compile(originalEntityType);

            for (AttributeMapping attributeMapping : attributeMappings) {
                attributeMapping.compilePatterns();
            } // for
        } // compilePatterns
        
        @Override
        public String toString() {
            String attrMappingStr =
                    "{\"originalEntityId\":\"" + getOriginalEntityId() + "\","
                    + "\"originalEntityType\":\"" + getOriginalEntityType() + "\","
                    + "\"newEntityId\":\"" + getNewEntityId() + "\","
                    + "\"newEntityType\":\"" + getNewEntityType() + "\","
                    + "\"attributeMappings\":[";
            boolean first = true;
            
            for (Object attrMap: attributeMappings) {
                if (first) {
                    attrMappingStr += attrMap.toString();
                    first = false;
                } else {
                    attrMappingStr += "," + attrMap.toString();
                } // if else
            } // for
            
            attrMappingStr += "]}";
            return attrMappingStr;
        } // toString
        
    } // EntityMapping
    
    /**
     * AttributeMapping class.
     */
    public class AttributeMapping {
        
        private String originalAttributeName;
        private Pattern originalAttributeNamePattern;
        private String originalAttributeType;
        private Pattern originalAttributeTypePattern;
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
        
        public Pattern getOriginalAttributeNamePattern() {
            return originalAttributeNamePattern;
        } // getOriginalAttributeNamePattern
        
        public Pattern getOriginalAttributeTypePattern() {
            return originalAttributeTypePattern;
        } // getOriginalAttributeTypePattern
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalAttributeNamePattern = Pattern.compile(originalAttributeName);
            originalAttributeTypePattern = Pattern.compile(originalAttributeType);
        } // compilePatterns
        
        @Override
        public String toString() {
            String attrMappingStr =
                    "\"originalAttributeName\":\"" + getOriginalAttributeName() + "\","
                    + "\"originalAttributeType\":\"" + getOriginalAttributeType() + "\","
                    + "\"newAttributeName\":\"" + getNewAttributeName() + "\","
                    + "\"newAttributeType\":\"" + getNewAttributeType() + "\"";
            return attrMappingStr;
        } // toString
        
    } // AttributeMapping
    
} // NameMappings
