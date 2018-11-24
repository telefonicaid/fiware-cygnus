package com.telefonica.iot.cygnus.backends.slicingdice;

import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusBadContextData;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

public class SlicingDiceBackendImpl extends HttpBackend implements SlicingDiceBackend {

    private static final CygnusLogger LOGGER = new CygnusLogger(SlicingDiceBackendImpl.class);

    // this is the SlicingDice host, the user will not be able to change it
    private static final String SLICING_DICE_HOST = "api.slicingdice.com";

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

    private JsonResponse doSlicingDiceRequest(final String method, final String urlPath,
                                              final String jsonString)
            throws CygnusPersistenceError, CygnusRuntimeError {
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", databaseKey));
        headers.add(new BasicHeader("Content-Type", "application/json"));
        return doRequest(method, urlPath, true, headers, new StringEntity(jsonString, "UTF-8"));
    } // doSlicingDiceRequest
}
