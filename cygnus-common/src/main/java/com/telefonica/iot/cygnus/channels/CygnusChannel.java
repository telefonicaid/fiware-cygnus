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
package com.telefonica.iot.cygnus.channels;

/**
 * Interface that all the Cygnus proprietary channels must implement. It defines common methods for all of them, such
 * as getNumEvents().
 * 
 * @author frb
 */
public interface CygnusChannel {
    
    /**
     * Gets the setup time of the channel.
     * @return The setup time (in miliseconds) of the channel
     */
    long getSetupTime();
    
    /**
     * Gets the number of events within the channel.
     * @return The number of events within the channel.
     */
    long getNumEvents();
    
    /**
     * Gets the number of put operations on the channel that went OK.
     * @return The number of put operations on the channel that went OK
     */
    long getNumPutsOK();
    
    /**
     * Gets the number of put operations on the channel that failed.
     * @return The number of put operations on the channel that failed
     */
    long getNumPutsFail();
    
    /**
     * Gets the number of take operations on the channel that went OK.
     * @return The number of take operations on the channel that went OK
     */
    long getNumTakesOK();
    
    /**
     * Gets the number of take operations on the channel that failed.
     * @return The number of take operations on the channel that failed
     */
    long getNumTakesFail();
    
    /**
     * Sets the number of put operations on the channel that went OK.
     * @param n The number to be set
     */
    void setNumPutsOK(long n);
    
    /**
     * Sets the number of put operations on the channel that failed.
     * @param n The number to be set
     */
    void setNumPutsFail(long n);
    
    /**
     * Sets the number of take operations on the channel that went OK.
     * @param n The number to be set
     */
    void setNumTakesOK(long n);
    
    /**
     * Sets the number of take operations on the channel that failed.
     * @param n The number to be set
     */
    void setNumTakesFail(long n);
    
} // CygnusChannel
