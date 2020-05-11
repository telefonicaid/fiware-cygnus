
package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.interceptors.NGSIEvent;
import com.telefonica.iot.cygnus.interceptors.NGSILDEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author amunoz
 * 
 * Class representing a batch of NGSILDEvent's.
 * 
 */
public class NGSILDBatch {

    private HashMap<String, SubBatch> subBatches;
    private int numEvents;
    private Iterator entries;
    private Entry nextEntry;

    /**
     * Constructor.
     */
    public NGSILDBatch() {
        subBatches = new HashMap<>();
        numEvents = 0;
        entries = null;
        nextEntry = null;
    } // NGSIBatch
    
    public int getNumEvents() {
        return numEvents;
    } // getNumEvents

    /**
     * Adds an event to the given destination sub-batch.
     * @param destination
     * @param event
     */
    public void addEvent(String destination, NGSILDEvent event) {
        SubBatch subBatch = subBatches.get(destination);
        
        if (subBatch == null) {
            ArrayList<NGSILDEvent> events = new ArrayList<>();
            events.add(event);
            subBatch = new SubBatch(false, events);
            subBatches.put(destination, subBatch);
        } else {
            subBatch.getEvents().add(event);
        } // if else

        numEvents++;
    } // addEvent

    /**
     * Starts an iterator for the sub-batches.
     */
    public void startIterator() {
        entries = subBatches.entrySet().iterator();
    } // getIterator

    /**
     * Return if there is another element to iterate.
     * @return True if there is another element to iterare, false otherwise. Internally, gets stores a pointer to the
     * next entry
     */
    public boolean hasNext() {
        if (entries.hasNext()) {
            nextEntry = (Entry) entries.next();
            return true;
        } else {
            return false;
        } // if else
    } // hasNext
    
    /**
     * Gets the next sub-batch destination.
     * @return The next sub-batch destination
     */
    public String getNextDestination() {
        return (String) nextEntry.getKey();
    } // getNextDestination
    
    /**
     * Gets the next sub-batch events.
     * @return The next sub-batch events
     */
    public ArrayList<NGSILDEvent> getNextEvents() {
        return ((SubBatch) nextEntry.getValue()).getEvents();
    } // getNextEvent
    
    /**
     * Sets the next sub-batch as persisted.
     * @param persisted
     */
    public void setNextPersisted(boolean persisted) {
        ((SubBatch) nextEntry.getValue()).setPersisted(persisted);
    } // setNextPersisted
    
    /**
     * Class representing a SubBatch of NGSIEvent's.
     */
    private class SubBatch {
        
        private boolean persisted;
        private ArrayList<NGSILDEvent> events;
        
        /**
         * Constructor.
         * @param persisted
         * @param events
         */
        public SubBatch(boolean persisted, ArrayList<NGSILDEvent> events) {
            this.persisted = persisted;
            this.events = events;
        } // SubBatch
        
        /**
         * Constructor.
         */
        public SubBatch() {
        } // SubBatch
        
        public boolean getPersisted() {
            return persisted;
        } // getPersisted
        
        public void setPersisted(boolean persisted) {
            this.persisted = persisted;
        } // setPersisted
        
        public ArrayList<NGSILDEvent> getEvents() {
            return this.events;
        } // getEvents
        
        public void setEvents(ArrayList<NGSILDEvent> events) {
            this.events = events;
        } // setEvents
        
    } // SubBatch
        
} // NGSIBatch
