/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author frb
 * 
 * Class representing a batch of Cygnus events.
 * 
 */
public class Batch {
    
    private final ArrayList<String> persistedDestinations;
    private final HashMap<String, ArrayList<CygnusEvent>> eventsPerDestination;

    /**
     * Constructor.
     */
    public Batch() {
        persistedDestinations = new ArrayList<String>();
        eventsPerDestination = new HashMap<String, ArrayList<CygnusEvent>>();
    } // Batch

    public Set<String> getDestinations() {
        return eventsPerDestination.keySet();
    } // getDestinations

    /**
     * Get the list of events regarding a destination.
     * @param destination
     * @return The list of events regarding the given destination
     */
    public ArrayList<CygnusEvent> getEvents(String destination) {
        return eventsPerDestination.get(destination);
    } // getEvents

    /**
     * Adds a events of events to a given destination.
     * @param destination
     * @param events
     */
    public void addEvents(String destination, ArrayList<CygnusEvent> events) {
        eventsPerDestination.put(destination, events);
    } // addEvents

    /**
     * Sets a destination has been persistedDestinations.
     * @param destination
     */
    public void setPersisted(String destination) {
        persistedDestinations.add(destination);
    } // setPersisted;

    /**
     * Gets is a destination has been persistedDestinations.
     * @param destination
     * @return True if the given destination has been persistedDestinations, false otherwise
     */
    public boolean isPersisted(String destination) {
        return persistedDestinations.contains(destination);
    } // isPersisted
        
} // Batch
