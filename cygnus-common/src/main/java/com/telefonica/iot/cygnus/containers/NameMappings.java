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

import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 *
 * @author frb
 */
public class NameMappings {
    
    private static final String DEFAULT_ORIGINAL_MAPPING = "^(.*)";
    private final ArrayList<ServiceMapping> serviceMappings;
    
    private static final CygnusLogger LOGGER = new CygnusLogger(NameMappings.class);
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
        if (serviceMappings != null) {
            for (ServiceMapping serviceMapping : serviceMappings) {
                serviceMapping.purge();
            } // for
        } // if
    } // purge
    
    /**
     * Compiles the regular expressions into Java Patterns.
     */
    public void compilePatterns() {
        if (serviceMappings != null) {
            for (ServiceMapping serviceMapping : serviceMappings) {
                serviceMapping.compilePatterns();
            } // for
        } // if
    } // compilePatterns
    
    /**
     * Overwrite of toString() method.
     * @return
     */
    @Override
    public String toString() {
        String nameMappingsStr = "{\"serviceMappings\":[";
        
        if (serviceMappings != null) {
            boolean first = true;

            for (ServiceMapping serviceMapping : serviceMappings) {
                if (first) {
                    nameMappingsStr += serviceMapping.toString();
                    first = false;
                } else {
                    nameMappingsStr += "," + serviceMapping.toString();
                } // if eslse
            } // for
        } // if
        
        nameMappingsStr += "]}";
        return nameMappingsStr;
    } // toString
    
    /**
     * Adds new service mappings to these name mappings.
     * @param newServiceMappings
     * @param update
     */
    public void add(ArrayList<ServiceMapping> newServiceMappings, boolean update) {
        for (ServiceMapping newServiceMapping : newServiceMappings) {
            ServiceMapping serviceMapping = get(newServiceMapping.originalService);
            
            if (serviceMapping == null) {
                serviceMappings.add(newServiceMapping);
            } else {
                if (update) {
                    serviceMapping.newService = newServiceMapping.newService;
                } // if
                
                serviceMapping.add(newServiceMapping.getServicePathMappings(), update);
            } // if else
        } // for
    } // add
    
    /**
     * Removes service mappings from these name mappings if there are no service path mappings.
     * @param newServiceMappings
     */
    public void remove(ArrayList<ServiceMapping> newServiceMappings) {
        for (ServiceMapping newServiceMapping : newServiceMappings) {
            ServiceMapping serviceMapping = get(newServiceMapping.originalService);
            
            if (serviceMapping != null) {
                ArrayList<ServicePathMapping> newServicePathMappings = newServiceMapping.getServicePathMappings();
                
                if (newServicePathMappings == null) {
                    serviceMappings.remove(serviceMapping);
                } else {
                    serviceMapping.remove(newServicePathMappings);
                } // if else
            } // if
            // else {
            //     Nothing is done if the service mapping is null
            // }
        } // for
    } // remove
    
    private ServiceMapping get(String originalService) {
        for (ServiceMapping serviceMapping : serviceMappings) {
            if (serviceMapping.originalService.equals(originalService)) {
                return serviceMapping;
            } // if
        } // for
        
        return null;
    } // get
    
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
         * Purges the ServiceMappings if any field is missing or has an invalid value.
         */
        public void purge() {
            if (originalService == null ) {
                originalService = DEFAULT_ORIGINAL_MAPPING;
                LOGGER.debug("[NameMappings] No originalService found in mapping, using default.");
            }
            if (servicePathMappings != null) {
                for (ServicePathMapping servicePathMapping : servicePathMappings) {
                    servicePathMapping.purge();
                } // for
            } // if
        } // purge
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalServicePattern = Pattern.compile(originalService);

            if (servicePathMappings != null) {
                for (ServicePathMapping servicePathMapping : servicePathMappings) {
                    servicePathMapping.compilePatterns();
                } // for
            } // if
        } // compilePatterns
        
        @Override
        public String toString() {
            String serviceMappingStr =
                    "{\"originalService\":\"" + getOriginalService() + "\","
                    + "\"newService\":\"" + getNewService() + "\","
                    + "\"servicePathMappings\":[";
            
            if (servicePathMappings != null) {
                boolean first = true;

                for (Object servicePathMapping : servicePathMappings) {
                    if (first) {
                        serviceMappingStr += servicePathMapping.toString();
                        first = false;
                    } else {
                        serviceMappingStr += "," + servicePathMapping.toString();
                    } // if else
                } // for
            } // if
            
            serviceMappingStr += "]}";
            return serviceMappingStr;
        } // toString
        
        /**
         * Adds new service path mappings to this service mapping.
         * @param newServicePathMappings
         * @param update
         */
        public void add(ArrayList<ServicePathMapping> newServicePathMappings, boolean update) {
            for (ServicePathMapping newServicePathMapping : newServicePathMappings) {
                ServicePathMapping servicePathMapping = get(newServicePathMapping.originalServicePath);

                if (servicePathMapping == null) {
                    servicePathMappings.add(newServicePathMapping);
                } else {
                    if (update) {
                        servicePathMapping.newServicePath = newServicePathMapping.newServicePath;
                    } // if
                    
                    servicePathMapping.add(newServicePathMapping.getEntityMappings(), update);
                } // if else
            } // for
        } // add
        
        /**
         * Removes service path mappings from these service mappings if there are no entity mappings.
         * @param newServicePathMappings
         */
        public void remove(ArrayList<ServicePathMapping> newServicePathMappings) {
            for (ServicePathMapping newServicePathMapping : newServicePathMappings) {
                ServicePathMapping servicePathMapping = get(newServicePathMapping.originalServicePath);

                if (servicePathMapping != null) {
                    ArrayList<EntityMapping> newEntityMappings = newServicePathMapping.getEntityMappings();

                    if (newEntityMappings == null) {
                        servicePathMappings.remove(servicePathMapping);
                    } else {
                        servicePathMapping.remove(newEntityMappings);
                    } // if else
                } // if
                // else {
                //     Nothing is done if the service path mapping is null
                // }
            } // for
        } // remove
        
        private ServicePathMapping get(String originalServicePath) {
            for (ServicePathMapping servicePathMapping : servicePathMappings) {
                if (servicePathMapping.originalServicePath.equals(originalServicePath)) {
                    return servicePathMapping;
                } // if
            } // for

            return null;
        } // get
        
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
         * Purges the ServicePathMappings if any field is missing or has an invalid value.
         */
        public void purge() {
            if (originalServicePath == null ) {
                originalServicePath = DEFAULT_ORIGINAL_MAPPING;
                LOGGER.debug("[NameMappings] No originalServicePath found in mapping, using default.");
            }
            if (entityMappings != null) {
                for (EntityMapping entityMapping : entityMappings) {
                    entityMapping.purge();
                } // for
            } // if
        } // purge
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalServicePathPattern = Pattern.compile(originalServicePath);

            if (entityMappings != null) {
                for (EntityMapping entityMapping : entityMappings) {
                    entityMapping.compilePatterns();
                } // for
            } // if
        } // compilePatterns
        
        @Override
        public String toString() {
            String entityMappingStr =
                    "{\"originalServicePath\":\"" + getOriginalServicePath() + "\","
                    + "\"newServicePath\":\"" + getNewServicePath() + "\","
                    + "\"entityMappings\": [";
            
            if (entityMappings != null) {
                boolean first = true;

                for (Object entityMapping: entityMappings) {
                    if (first) {
                        entityMappingStr += entityMapping.toString();
                        first = false;
                    } else {
                        entityMappingStr += "," + entityMapping.toString();
                    } // if else
                } // for
            } // if
            
            entityMappingStr += "]}";
            return entityMappingStr;
        } // toString
        
        /**
         * Adds new entity mappings to this service path mapping.
         * @param newEntityMappings
         * @param update
         */
        public void add(ArrayList<EntityMapping> newEntityMappings, boolean update) {
            for (EntityMapping newEntityMapping : newEntityMappings) {
                EntityMapping entityMapping = get(newEntityMapping.originalEntityId,
                        newEntityMapping.originalEntityType);

                if (entityMapping == null) {
                    entityMappings.add(newEntityMapping);
                } else {
                    if (update) {
                        entityMapping.newEntityId = newEntityMapping.newEntityId;
                        entityMapping.newEntityType = newEntityMapping.newEntityType;
                    } // if
                    
                    entityMapping.add(newEntityMapping.getAttributeMappings(), update);
                } // if else
            } // for
        } // add
        
        /**
         * Removes entity mappings from these service path mappings if there are no attribute mappings.
         * @param newEntityMappings
         */
        public void remove(ArrayList<EntityMapping> newEntityMappings) {
            for (EntityMapping newEntityMapping : newEntityMappings) {
                EntityMapping entityMapping = get(newEntityMapping.originalEntityId,
                        newEntityMapping.originalEntityType);

                if (entityMapping != null) {
                    ArrayList<AttributeMapping> newAttributeMappings = newEntityMapping.getAttributeMappings();

                    if (newAttributeMappings == null) {
                        entityMappings.remove(entityMapping);
                    } else {
                        entityMapping.remove(newAttributeMappings);
                    } // if else
                } // if
                // else {
                //     Nothing is done if the entity mapping is null
                // }
            } // for
        } // remove
        
        private EntityMapping get(String originalEntityId, String originalEntityType) {
            for (EntityMapping entityMapping : entityMappings) {
                if (entityMapping.originalEntityId.equals(originalEntityId)
                        && entityMapping.originalEntityType.equals(originalEntityType)) {
                    return entityMapping;
                } // if
            } // for

            return null;
        } // get
        
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
         * Purges the EntityMappings if any field is missing or has an invalid value.
         */
        public void purge() {
            if (originalEntityId == null ) {
                originalEntityId = DEFAULT_ORIGINAL_MAPPING;
                LOGGER.debug("[NameMappings] No originalEntityId found in mapping, using default.");
            }
            if (originalEntityType == null ) {
                originalEntityType = DEFAULT_ORIGINAL_MAPPING;
                LOGGER.debug("[NameMappings] No originalEntityType found in mapping, using default.");
            }
            //TODO purge attributes
        } // purge
        
        /**
         * Compiles the regular expressions into Java Patterns.
         */
        public void compilePatterns() {
            originalEntityIdPattern = Pattern.compile(originalEntityId);
            originalEntityTypePattern = Pattern.compile(originalEntityType);

            if (attributeMappings != null) {
                for (AttributeMapping attributeMapping : attributeMappings) {
                    attributeMapping.compilePatterns();
                } // for
            } // if
        } // compilePatterns
        
        @Override
        public String toString() {
            String attrMappingStr =
                    "{\"originalEntityId\":\"" + getOriginalEntityId() + "\","
                    + "\"originalEntityType\":\"" + getOriginalEntityType() + "\","
                    + "\"newEntityId\":\"" + getNewEntityId() + "\","
                    + "\"newEntityType\":\"" + getNewEntityType() + "\","
                    + "\"attributeMappings\":[";

            if (attributeMappings != null) {
                boolean first = true;
                
                for (Object attrMap: attributeMappings) {
                    if (first) {
                        attrMappingStr += attrMap.toString();
                        first = false;
                    } else {
                        attrMappingStr += "," + attrMap.toString();
                    } // if else
                } // for
            } // for
            
            attrMappingStr += "]}";
            return attrMappingStr;
        } // toString
        
        /**
         * Adds new attribute mappings to this entity mapping.
         * @param newAttributeMappings
         * @param update
         */
        public void add(ArrayList<AttributeMapping> newAttributeMappings, boolean update) {
            for (AttributeMapping newAttributeMapping : newAttributeMappings) {
                AttributeMapping attributeMapping = get(newAttributeMapping.originalAttributeName,
                        newAttributeMapping.originalAttributeType);

                if (attributeMapping == null) {
                    attributeMappings.add(newAttributeMapping);
                } else if (update) {
                    attributeMapping.newAttributeName = newAttributeMapping.newAttributeName;
                    attributeMapping.newAttributeType = newAttributeMapping.newAttributeType;
                } // if else
                // else {
                //     Nothing is done if the attribute mapping already exists and updating is not enabled
                // }
            } // for
        } // add
        
        /**
         * Removes attribute mappings from these entity mappings.
         * @param newAttributeMappings
         */
        public void remove(ArrayList<AttributeMapping> newAttributeMappings) {
            for (AttributeMapping newAttributeMapping : newAttributeMappings) {
                AttributeMapping attributeMapping = get(newAttributeMapping.originalAttributeName,
                        newAttributeMapping.originalAttributeType);

                if (attributeMapping != null) {
                    attributeMappings.remove(attributeMapping);
                } // if
                // else {
                //     Nothing is done if the attribute mapping is null
                // }
            } // for
        } // remove
        
        private AttributeMapping get(String originalAttributeName, String originalAttributeType) {
            for (AttributeMapping attributeMapping : attributeMappings) {
                if (attributeMapping.originalAttributeName.equals(originalAttributeName)
                        && attributeMapping.originalAttributeType.equals(originalAttributeType)) {
                    return attributeMapping;
                } // if
            } // for

            return null;
        } // get
        
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
                    "{\"originalAttributeName\":\"" + getOriginalAttributeName() + "\","
                    + "\"originalAttributeType\":\"" + getOriginalAttributeType() + "\","
                    + "\"newAttributeName\":\"" + getNewAttributeName() + "\","
                    + "\"newAttributeType\":\"" + getNewAttributeType() + "\"}";
            return attrMappingStr;
        } // toString
        
    } // AttributeMapping
    
} // NameMappings
