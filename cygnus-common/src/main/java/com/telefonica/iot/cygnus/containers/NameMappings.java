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
        serviceMappings = new ArrayList<ServiceMapping>();
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
        String nameMappingsStr = "   \"serviceMapping\": [\n";
        int lastIndex = serviceMappings.size() - 1;
        
        for (ServiceMapping map : serviceMappings) {
            nameMappingsStr += "      {\n"
                            + map.toString() + "\n"
                            + "      }";    
            
            if (!map.equals(serviceMappings.get(lastIndex))) {
                nameMappingsStr += ",\n";
            } // if
            
        } // for
        nameMappingsStr += "\n   ]\n}";
        return nameMappingsStr;
    } // toString
    
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
            int lastIndex = servicePathMappings.size() - 1;
            String serviceMappingStr = "         \"originalService\":\"" + getOriginalService() + "\",\n"
                                     + "         \"newService\":\"" + getNewService()+ "\",\n"
                                     + "         \"servicePathMappings\": [\n";
             
            for (Object originalSP : servicePathMappings) {
                serviceMappingStr += "            {\n" 
                                   + originalSP.toString() + "\n"
                                   + "            }";
                
                if (!originalSP.equals(servicePathMappings.get(lastIndex))) {
                    serviceMappingStr += ",\n";
                } // if
                
            } // for    
            
            serviceMappingStr += "\n         ]";         
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
            int lastIndex = entityMappings.size() - 1;
            String entityMappingStr = "               \"originalServicePath\":\"" + getOriginalServicePath()+ "\",\n"
                                 + "               \"newServicePath\":\"" + getNewServicePath()+ "\",\n"
                                 + "               \"entityMappings\": [\n";
            
            for (Object entityMap: entityMappings) {
                entityMappingStr += "                  {\n" 
                            + entityMap.toString() + "\n"
                            + "                  }";
                
                if (!entityMap.equals(entityMappings.get(lastIndex))) {
                    entityMappingStr += ",\n";
                } // if
                
            } // for
            
            entityMappingStr += "\n                ]";  
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
            attributeMappings = new ArrayList<AttributeMapping>();
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
            int lastIndex = attributeMappings.size()-1;
            String attrMappingStr = "                     \"originalEntityId\":\"" + getOriginalEntityId() + "\",\n"
                        + "                     \"originalEntityType\":\"" + getOriginalEntityType()+ "\",\n"
                        + "                     \"newEntityId\":\"" + getNewEntityId() + "\",\n"
                        + "                     \"newEntityType\":\"" + getNewEntityType() + "\",\n"
                        + "                     \"attributeMappings\":[\n";
            
            for (Object attrMap: attributeMappings) {
                attrMappingStr += "                        {\n" 
                            + attrMap.toString() + "\n" 
                            + "                        }";
            
                if (!attrMap.equals(attributeMappings.get(lastIndex))) {
                    attrMappingStr += ",\n";
                } // if
                
            } // for
            
            attrMappingStr += "\n                    ]";        
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
            String attrMappingStr = "                           \"originalAttributeName\":\"" 
                        + getOriginalAttributeName() + "\",\n"
                        + "                           \"originalAttributeType\":\"" 
                        + getOriginalAttributeType() + "\",\n"
                        + "                           \"newAttributeName\":\"" 
                        + getNewAttributeName() + "\",\n"
                        + "                           \"newAttributeType\":\"" 
                        + getNewAttributeType() + "\"";
            return attrMappingStr;
        } // toString
        
    } // AttributeMapping
    
} // NameMappings
