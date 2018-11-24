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
