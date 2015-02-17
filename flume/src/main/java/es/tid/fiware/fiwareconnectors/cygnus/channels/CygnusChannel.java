/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */
package es.tid.fiware.fiwareconnectors.cygnus.channels;

/**
 * Interface that all the Cygnus proprietary channels must implement. It defines common methods for all of them, such
 * as getNumEvents().
 * 
 * @author frb
 */
public interface CygnusChannel {
    
    /**
     * Gets the number of events within the channel.
     * @return The number of events within the channel.
     */
    int getNumEvents();
    
    /**
     * Rollbacks the number of events when a transaction is rollbacked as well. This method is necessary beacuse when
     * a transaction is rollbacked the "put" method is not used but some kind of internal re-linking is done at the
     * chanel; thus, the number of events is not increased, which must be deliberatedly done by issuing this method.
     */
    void rollback();
    
} // CygnusChannel
