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

import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SlicingDiceBackendImpl extends HttpBackend implements SlicingDiceBackend {

    private static final CygnusLogger LOGGER = new CygnusLogger(SlicingDiceBackendImpl.class);

    // this is the SlicingDice host, the user will not be able to change it
    private static final String SLICING_DICE_HOST = "api.slicingdice.com/v1";

    // this is the SlicingDice port, the user will not be able to change it
    private static final String SLICING_DICE_PORT = "443";

    private static final boolean IS_SSL = true;

    // max connections used by  SlicingDice
    private static final int MAX_CONNECTIONS = 50;

    // database key used to access SlicingDice API
    private final String databaseKey;

    /**
     * Constructor for the SlicingDice backend.
     *
     * @param databaseKey - the api key used to connect to the SlicingDice account
     */
    public SlicingDiceBackendImpl(final String databaseKey) {
        super(SLICING_DICE_HOST, SLICING_DICE_PORT, IS_SSL, false, null, null, null, null, MAX_CONNECTIONS, MAX_CONNECTIONS);

        this.databaseKey = databaseKey;
    }

    @Override
    public void createColumns(final String columnsToCreate) throws CygnusRuntimeError, CygnusPersistenceError {
        final String urlPath = "/column/";

        // do the SlicingDice request
        final JsonResponse res = doSlicingDiceRequest("POST", urlPath, columnsToCreate);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful column creation");
        } else if (res.getStatusCode() == 400) {
            final JSONArray errors = (JSONArray) res.getJsonObject().get("errors");
            final JSONObject error = (JSONObject) errors.get(0);
            final Long code = (Long) error.get("code");

            if (code == 3003) {
                LOGGER.debug("Column already exists");
            } else {
                throw new CygnusPersistenceError("Could not create the columns, " +
                        "statusCode=" + res.getStatusCode() + ")");
            }
        } else {
            throw new CygnusPersistenceError("Could not create the columns, " +
                    "statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // createColumns

    @Override
    public void insertContextData(final String valuesForInsert) throws CygnusBadContextData, CygnusRuntimeError, CygnusPersistenceError {
        final String urlPath = "/insert/";

        // do the SlicingDice request
        final JsonResponse res = doSlicingDiceRequest("POST", urlPath, valuesForInsert);

        // check the status
        if (res.getStatusCode() == 200) {
            LOGGER.debug("Successful inserted data on SlicingDice");
        } else {
            throw new CygnusPersistenceError("Could not create the columns, " +
                    "statusCode=" + res.getStatusCode() + ")");
        } // if else
    } // insertContextData

    JsonResponse doSlicingDiceRequest(final String method, final String urlPath,
                                              final String jsonString)
            throws CygnusPersistenceError, CygnusRuntimeError {
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", databaseKey));
        headers.add(new BasicHeader("Content-Type", "application/json"));
        return doRequest(method, urlPath, true, headers, new StringEntity(jsonString, "UTF-8"));
    } // doSlicingDiceRequest

    @Override
    protected JsonResponse createJsonResponse(final HttpResponse httpRes) throws CygnusRuntimeError {
        return super.createJsonResponse(httpRes);
    }
}
