/**
 * Copyright 2018 Telefonica Investigación y Desarrollo, S.A.U
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
 *
 * Authorship: SlicingDice
 *
 */

package com.telefonica.iot.cygnus.backends.slicingdice;

import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;

public interface SlicingDiceBackend {

    /**
     * Creates the necessary columns on SlicingDice dimension.
     * @param fieldNames
     * @throws CygnusRuntimeError
     * @throws CygnusPersistenceError
     */
    void createColumns(String fieldNames) throws CygnusRuntimeError, CygnusPersistenceError;

    /**
     * Insert already processed context data into the given dimension.
     * @param valuesForInsert
     * @throws com.telefonica.iot.cygnus.errors.CygnusBadContextData
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     */
    void insertContextData(String valuesForInsert)
            throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError;

}
