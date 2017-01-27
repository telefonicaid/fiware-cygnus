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
package com.telefonica.iot.cygnus.metrics;

import java.util.HashMap;

/**
 *
 * @author frb
 */
public class CygnusMetrics {
    
    private final HashMap<String, HashMap<String, Metrics>> perServiceSubserviceMetrics;
    private final HashMap<String, Metrics> perServiceAggrMetrics;
    private final HashMap<String, Metrics> perSubserviceAggrMetrics;
    private final Metrics allAggrMetrics;
    
    /**
     * Constructor.
     */
    public CygnusMetrics() {
        perServiceSubserviceMetrics = new HashMap<>();
        perServiceAggrMetrics = new HashMap<>();
        perSubserviceAggrMetrics = new HashMap<>();
        allAggrMetrics = new Metrics();
    } // CygnusMetrics
    
    /**
     * Adds metrics to the given entity within the given service path within the given service.
     * @param service
     * @param subservice
     * @param incomingTransaction
     * @param incomingTransactionRequestSize
     * @param incomingTransactionResponseSize
     * @param incomingTransactionErrors
     * @param serviceTime
     * @param outgoingTransactions
     * @param outgoingTransactionRequestSize
     * @param outgoingTransactionResponseSize
     * @param outgoingTransactionErrors
     */
    public void add(String service, String subservice, long incomingTransaction, long incomingTransactionRequestSize,
            long incomingTransactionResponseSize, long incomingTransactionErrors, double serviceTime,
            long outgoingTransactions, long outgoingTransactionRequestSize, long outgoingTransactionResponseSize,
            long outgoingTransactionErrors) {
        // Add to per-service metrics
        HashMap<String, Metrics> subserviceMetrics = perServiceSubserviceMetrics.get(service);
        
        if (subserviceMetrics == null) {
            subserviceMetrics = new HashMap<>();
            perServiceSubserviceMetrics.put(service, subserviceMetrics);
        } // if
        
        Metrics metrics = subserviceMetrics.get(subservice);
        
        if (metrics == null) {
            metrics = new Metrics();
            subserviceMetrics.put(subservice, metrics);
        } // if
        
        metrics.addIncomingTransactions(incomingTransaction);
        metrics.addIncomingTransactionRequestSize(incomingTransactionRequestSize);
        metrics.addIncomingTransactionResponseSize(incomingTransactionResponseSize);
        metrics.addIncomingTransactionErrors(incomingTransactionErrors);
        metrics.addServiceTime(serviceTime);
        metrics.addOutgoingTransactions(outgoingTransactions);
        metrics.addOutgoingTransactionRequestSize(outgoingTransactionRequestSize);
        metrics.addOutgoingTransactionResponseSize(outgoingTransactionResponseSize);
        metrics.addOutgoingTransactionErrors(outgoingTransactionErrors);
        
        // Add to per-service aggregated metrics
        Metrics allsubserviceMetrics = perServiceAggrMetrics.get(service);
        
        if (allsubserviceMetrics == null) {
            allsubserviceMetrics = new Metrics();
            perServiceAggrMetrics.put(service, allsubserviceMetrics);
        } // if
        
        allsubserviceMetrics.addIncomingTransactions(incomingTransaction);
        allsubserviceMetrics.addIncomingTransactionRequestSize(incomingTransactionRequestSize);
        allsubserviceMetrics.addIncomingTransactionResponseSize(incomingTransactionResponseSize);
        allsubserviceMetrics.addIncomingTransactionErrors(incomingTransactionErrors);
        allsubserviceMetrics.addServiceTime(serviceTime);
        allsubserviceMetrics.addOutgoingTransactions(outgoingTransactions);
        allsubserviceMetrics.addOutgoingTransactionRequestSize(outgoingTransactionRequestSize);
        allsubserviceMetrics.addOutgoingTransactionResponseSize(outgoingTransactionResponseSize);
        allsubserviceMetrics.addOutgoingTransactionErrors(outgoingTransactionErrors);
        
        // Add to per-subservice metrics
        Metrics subsvcMetrics = perSubserviceAggrMetrics.get(subservice);
        
        if (subsvcMetrics == null) {
            subsvcMetrics = new Metrics();
            perSubserviceAggrMetrics.put(subservice, subsvcMetrics);
        } // if
        
        subsvcMetrics.addIncomingTransactions(incomingTransaction);
        subsvcMetrics.addIncomingTransactionRequestSize(incomingTransactionRequestSize);
        subsvcMetrics.addIncomingTransactionResponseSize(incomingTransactionResponseSize);
        subsvcMetrics.addIncomingTransactionErrors(incomingTransactionErrors);
        subsvcMetrics.addServiceTime(serviceTime);
        subsvcMetrics.addOutgoingTransactions(outgoingTransactions);
        subsvcMetrics.addOutgoingTransactionRequestSize(outgoingTransactionRequestSize);
        subsvcMetrics.addOutgoingTransactionResponseSize(outgoingTransactionResponseSize);
        subsvcMetrics.addOutgoingTransactionErrors(outgoingTransactionErrors);
        
        // Add to per-subservice aggregated metrics
        allAggrMetrics.addIncomingTransactions(incomingTransaction);
        allAggrMetrics.addIncomingTransactionRequestSize(incomingTransactionRequestSize);
        allAggrMetrics.addIncomingTransactionResponseSize(incomingTransactionResponseSize);
        allAggrMetrics.addIncomingTransactionErrors(incomingTransactionErrors);
        allAggrMetrics.addServiceTime(serviceTime);
        allAggrMetrics.addOutgoingTransactions(outgoingTransactions);
        allAggrMetrics.addOutgoingTransactionRequestSize(outgoingTransactionRequestSize);
        allAggrMetrics.addOutgoingTransactionResponseSize(outgoingTransactionResponseSize);
        allAggrMetrics.addOutgoingTransactionErrors(outgoingTransactionErrors);
    } // add
    
    /**
     * Gets metrics related to given service and service path.
     * @param service
     * @param servicePath
     * @return Metrics related to given service and ervice path
     */
    public Metrics getServiceSubserviceMetrics(String service, String servicePath) {
        return perServiceSubserviceMetrics.get(service).get(servicePath);
    } // getServiceSubserviceMetrics
    
    /**
     * Gets metrics related to given service.
     * @param service
     * @return Metrics related to given service
     */
    public Metrics getServiceAggrMetrics(String service) {
        return perServiceAggrMetrics.get(service);
    } // getServiceSubserviceMetrics
    
    /**
     * Gets metrics related to given service and service path.
     * @param servicePath
     * @return Metrics related to given service and ervice path
     */
    public Metrics getSubserviceAggrMetrics(String servicePath) {
        return perSubserviceAggrMetrics.get(servicePath);
    } // getSubserviceAggrMetrics
    
    /**
     * Gets metrics related to given service.
     * @return Metrics related to given service
     */
    public Metrics getAllAggrMetrics() {
        return this.allAggrMetrics;
    } // getAllAggrMetrics
   
    /**
     * Merges given source handler metrics into these ones.
     * @param other
     */
    public void merge(CygnusMetrics other) {
        HashMap<String, HashMap<String, Metrics>> otherServiceMetrics = other.perServiceSubserviceMetrics;
                
        for (String service : otherServiceMetrics.keySet()) {
            HashMap<String, Metrics> thisSubservices = this.perServiceSubserviceMetrics.get(service);

            if (thisSubservices == null) {
                thisSubservices = new HashMap<>();
                this.perServiceSubserviceMetrics.put(service, thisSubservices);
            } // if

            HashMap<String, Metrics> otherSubservices = otherServiceMetrics.get(service);

            for (String subservice : otherSubservices.keySet()) {
                Metrics thisMetrics = thisSubservices.get(subservice);

                if (thisMetrics == null) {
                    thisMetrics = new Metrics();
                    thisSubservices.put(subservice, thisMetrics);
                } // if
                
                Metrics otherMetrics = otherSubservices.get(subservice);
                thisMetrics.merge(otherMetrics);
            } // for
        } // for
        
        HashMap<String, Metrics> otherServiceSumMetrics = other.perServiceAggrMetrics;
        
        for (String service : otherServiceSumMetrics.keySet()) {
            Metrics thisMetrics = this.perServiceAggrMetrics.get(service);
            
            if (thisMetrics == null) {
                thisMetrics = new Metrics();
                this.perServiceAggrMetrics.put(service, thisMetrics);
            } // if
            
            Metrics otherMetrics = otherServiceSumMetrics.get(service);
            thisMetrics.merge(otherMetrics);
        } // for
        
        HashMap<String, Metrics> otherSuberviceMetrics = other.perSubserviceAggrMetrics;
        
        for (String subservice : otherSuberviceMetrics.keySet()) {
            Metrics thisMetrics = this.perSubserviceAggrMetrics.get(subservice);
            
            if (thisMetrics == null) {
                thisMetrics = new Metrics();
                this.perSubserviceAggrMetrics.put(subservice, thisMetrics);
            } // if
            
            Metrics otherMetrics = otherSuberviceMetrics.get(subservice);
            thisMetrics.merge(otherMetrics);
        } // for
        
        this.allAggrMetrics.merge(other.allAggrMetrics);
    } // merge
    
    /**
     * Gets the Json string for this metrics.
     * @return The Json string for this metrics
     */
    public String toJsonString() {
        String json = "{\"services\":{";
        boolean firstService = true;
        
        for (String service : perServiceSubserviceMetrics.keySet()) {
            if (firstService) {
                json += "\"" + service + "\":{\"subservs\":{";
                firstService = false;
            } else {
                json += ",\"" + service + "\":{\"subservs\":{";
            } // if else
            
            HashMap<String, Metrics> subserviceMetrics = perServiceSubserviceMetrics.get(service);
            boolean firstSubservice = true;
            
            for (String subservice : subserviceMetrics.keySet()) {
                Metrics metrics = subserviceMetrics.get(subservice);
                
                if (firstSubservice) {
                    json += "\"" + subservice.substring(1) + "\":" + metrics.toJsonString();
                    firstSubservice = false;
                } else {
                    json += ",\"" + subservice.substring(1) + "\":" + metrics.toJsonString();
                } // if else
            } // for
            
            json += "},\"sum\":" + perServiceAggrMetrics.get(service).toJsonString() + "}";
        } // for
        
        json += "},\"sum\": {\"subservs\":{";
        boolean firstSubservice = true;
        
        for (String subservice : perSubserviceAggrMetrics.keySet()) {
            Metrics metrics = perSubserviceAggrMetrics.get(subservice);
            
            if (firstSubservice) {
                json += "\"" + subservice.substring(1) + "\":" + metrics.toJsonString();
                firstSubservice = false;
            } else {
                json += ",\"" + subservice.substring(1) + "\":" + metrics.toJsonString();
            } // if else
        } // for

        if (perSubserviceAggrMetrics.isEmpty()) {
            json += "},\"sum\":{}}}";
        } else {
            json += "},\"sum\":" + allAggrMetrics.toJsonString() + "}}";
        } // if else
        
        return json;
    } // toJsonString

    /**
     * Metrics class.
     */
    public class Metrics {
        
        private long incomingTransactions;
        private long incomingTransactionRequestSize;
        private long incomingTransactionResponseSize;
        private long incomingTransactionErrors;
        private double serviceTime;
        private long outgoingTransactions;
        private long outgoingTransactionRequestSize;
        private long outgoingTransactionResponseSize;
        private long outgoingTransactionErrors;
        

        /**
         * Constructor.
         */
        public Metrics() {
            incomingTransactions = 0;
            incomingTransactionRequestSize = 0;
            incomingTransactionResponseSize = 0;
            incomingTransactionErrors = 0;
            serviceTime = 0;
            outgoingTransactions = 0;
            outgoingTransactionRequestSize = 0;
            outgoingTransactionResponseSize = 0;
            outgoingTransactionErrors = 0;
        } // Metrics

        public long getIncomingTransactions() {
            return incomingTransactions;
        } // getIncomingTransactions

        public long getIncomingTransactionRequestSize() {
            return incomingTransactionRequestSize;
        } // getIncomingTransactionRequestSize

        public long getIncomingTransactionResponseSize() {
            return incomingTransactionResponseSize;
        } // getIncomingTransactionResponseSize

        public long getIncomingTransactionErrors() {
            return incomingTransactionErrors;
        } // getIncomingTransactionErrors
        
        public double getServiceTime() {
            return serviceTime;
        } // getServiceTime
        
        public long getOutgoingTransactions() {
            return outgoingTransactions;
        } // getOutgoingTransactions

        public long getOutgoingTransactionRequestSize() {
            return outgoingTransactionRequestSize;
        } // getOutgoingTransactionRequestSize

        public long getOutgoingTransactionResponseSize() {
            return outgoingTransactionResponseSize;
        } // getOutgoingTransactionResponseSize

        public long getOutgoingTransactionErrors() {
            return outgoingTransactionErrors;
        } // getOutgoingTransactionErrors

        /**
         * Adds as many incoming transactions as given.
         * @param incomingTransactions
         */
        public void addIncomingTransactions(long incomingTransactions) {
            this.incomingTransactions += incomingTransactions;
        } // addIncomingTransactions

        /**
         * Adds as many bytes for incoming transaction requests as given.
         * @param incomingTransactionRequestSize
         */
        public void addIncomingTransactionRequestSize(long incomingTransactionRequestSize) {
            this.incomingTransactionRequestSize += incomingTransactionRequestSize;
        } // addIncomingTransactionRequestSize

        /**
         * Adds as many bytes for incoming transaction responses as given.
         * @param incomingTransactionResponseSize
         */
        public void addIncomingTransactionResponseSize(long incomingTransactionResponseSize) {
            this.incomingTransactionResponseSize += incomingTransactionResponseSize;
        } // addIncomingTransactionResponseSize

        /**
         * Adds as many incoming transaction errors as given.
         * @param incomingTransactionErrors
         */
        public void addIncomingTransactionErrors(long incomingTransactionErrors) {
            this.incomingTransactionErrors += incomingTransactionErrors;
        } // addIncomingTransactionErrors
        
        /**
         * Adds as many service milliseconds as given.
         * @param serviceTime
         */
        public void addServiceTime(double serviceTime) {
            this.serviceTime += serviceTime;
        } // addIncomingTransactionErrors
        
        /**
         * Adds as many outgoing transactions as given.
         * @param outgoingTransactions
         */
        public void addOutgoingTransactions(long outgoingTransactions) {
            this.outgoingTransactions += outgoingTransactions;
        } // addOutgoingTransactions

        /**
         * Adds as many bytes for outgoing transaction requests as given.
         * @param outgoingTransactionRequestSize
         */
        public void addOutgoingTransactionRequestSize(long outgoingTransactionRequestSize) {
            this.outgoingTransactionRequestSize += outgoingTransactionRequestSize;
        } // addOutgoingTransactionRequestSize

        /**
         * Adds as many bytes for outgoing transaction responses as given.
         * @param outgoingTransactionResponseSize
         */
        public void addOutgoingTransactionResponseSize(long outgoingTransactionResponseSize) {
            this.outgoingTransactionResponseSize += outgoingTransactionResponseSize;
        } // addOutgoingTransactionResponseSize

        /**
         * Adds as many outgoing transaction errors as given.
         * @param outgoingTransactionErrors
         */
        public void addOutgoingTransactionErrors(long outgoingTransactionErrors) {
            this.outgoingTransactionErrors += outgoingTransactionErrors;
        } // addOutgoingTransactionErrors

        /**
         * Merges given metrics with these ones.
         * @param metrics
         */
        public void merge(Metrics metrics) {
            incomingTransactions += metrics.incomingTransactions;
            incomingTransactionRequestSize += metrics.incomingTransactionRequestSize;
            incomingTransactionResponseSize += metrics.incomingTransactionResponseSize;
            incomingTransactionErrors += metrics.incomingTransactionErrors;
            serviceTime += metrics.serviceTime;
            outgoingTransactions += metrics.outgoingTransactions;
            outgoingTransactionRequestSize += metrics.outgoingTransactionRequestSize;
            outgoingTransactionResponseSize += metrics.outgoingTransactionResponseSize;
            outgoingTransactionErrors += metrics.outgoingTransactionErrors;
        } // merge
        
        /**
         * Gets the Json string for this metrics.
         * @return The Json string for this metrics
         */
        public String toJsonString() {
            double avg = (outgoingTransactions == 0 ? 0 : serviceTime / outgoingTransactions);
            
            return "{\"incomingTransactions\":" + incomingTransactions + ","
                    + "\"incomingTransactionRequestSize\":" + incomingTransactionRequestSize + ","
                    + "\"incomingTransactionResponseSize\":" + incomingTransactionResponseSize + ","
                    + "\"incomingTransactionErrors\":" + incomingTransactionErrors + ","
                    + "\"serviceTime\":" + avg + ","
                    + "\"outgoingTransactions\":" + outgoingTransactions + ","
                    + "\"outgoingTransactionRequestSize\":" + outgoingTransactionRequestSize + ","
                    + "\"outgoingTransactionResponseSize\":" + outgoingTransactionResponseSize + ","
                    + "\"outgoingTransactionErrors\":" + outgoingTransactionErrors + "}";
        } // toJsonString
        
    } // Metrics
    
} // CygnusMetrics
