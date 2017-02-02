/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
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

/**
 *
 * @author frb
 */
public class NTIMetadata {
    
    private ArrayList<Service> services;
    
    /**
     * Constructor.
     */
    public NTIMetadata() {
        services = new ArrayList<>();
    } // NTIMetadata
    
    /**
     * Searches for invalid entries.
     */
    public void purge() {
    } // purge
    
    /**
     * Pre-compiles regular expression patterns.
     */
    public void compilePatterns() {
    } // compilePatterns
    
    /**
     * Service-related information.
     */
    private class Service {
        
        private String service;
        private ArrayList<ServicePath> servicePaths;
        
        /**
         * Constructor.
         */
        public Service() {
            servicePaths = new ArrayList<>();
        } // Service
        
        /**
         * Searches for invalid entries.
         */
        public void purge() {
        } // purge
    
        /**
         * Pre-compiles regular expression patterns.
         */
        public void compilePatterns() {
        } // compilePatterns
        
    } // Service
    
    /**
     * Service path-related information.
     */
    private class ServicePath {
        
        private String servicePath;
        private ArrayList<Entity> entities;
        private String titleTranslated;
        private String keywords;
        private String notesTranslated;
        private String openessScore;
        private String lastUpdated;
        private String updateFrequency;
        private String language;
        private String spatial;
        private String hasBegining;
        private String hasEnd;
        private String references;
        private String conformsTo;
        
        /**
         * Constructor.
         */
        public ServicePath() {
            entities = new ArrayList<>();
        } // ServicePath
        
        /**
         * Searches for invalid entries.
         */
        public void purge() {
        } // purge
    
        /**
         * Pre-compiles regular expression patterns.
         */
        public void compilePatterns() {
        } // compilePatterns
        
    } // ServicePath
    
    /**
     * Entity-related information.
     */
    private class Entity {
        
        private String entityId;
        private String entityType;
        private String nameTranslated;
        private String descriptionTranslated;
        private String resourceFileInformation;
        private String resourceFileSize;
        private String resourceFiwareURL;
        private String resourcePayload;
        private String resourceViewing;
        private String resourceLanguage;
        
        /**
         * Constructor.
         */
        public Entity() {
        } // Entity
        
        /**
         * Searches for invalid entries.
         */
        public void purge() {
        } // purge
    
        /**
         * Pre-compiles regular expression patterns.
         */
        public void compilePatterns() {
        } // compilePatterns
        
    } // Entity
    
} // NTIMetadata
