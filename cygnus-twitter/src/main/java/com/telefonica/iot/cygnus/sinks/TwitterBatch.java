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

package com.telefonica.iot.cygnus.sinks;

import java.util.ArrayList;

/**
 *
 * @author cardiealb
 *
 * Class representing a batch of Twitter events.
 *
 */
public class TwitterBatch {

    private long numEvents;
    private final ArrayList<TwitterEvent> batch;


    /**
     * Constructor.
     */
    public TwitterBatch() {
        numEvents = 0;
        batch = new ArrayList<TwitterEvent>();
    } // TwitterBatch


    public void addEvent(TwitterEvent event) {
        batch.add(event);
        numEvents++;
    } // addEvent


    public ArrayList<TwitterEvent> getBatch() {
        return batch;
    } // getBatch

    /**
     * Gets the total number of events within this batch.
     * @return The total number of events within this batch
     */
    public long getNumEvents() {
        return numEvents;
    } // getNumEvents

} // TwitterBatch
