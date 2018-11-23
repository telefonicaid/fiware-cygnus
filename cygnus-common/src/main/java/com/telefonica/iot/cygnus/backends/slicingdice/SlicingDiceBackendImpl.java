package com.telefonica.iot.cygnus.backends.slicingdice;

import com.telefonica.iot.cygnus.backends.http.HttpBackend;

public class SlicingDiceBackendImpl extends HttpBackend implements SlicingDiceBackend {

    // this is the SlicingDice host, the user will not be able to change it
    private static final String SLICING_DICE_HOST = "api.slicingdice.com";

    // this is the SlicingDice port, the user will not be able to change it
    private static final String SLICING_DICE_PORT = "443";

    private static final boolean IS_SSL = true;

    // max connections used by  SlicingDice
    private static final int MAX_CONNECTIONS = 50;

    // database key used to access SlicingDice API
    private final String databaseKey;

    // boolean that indicates if we can auto create columns and dimensions if needed
    private final boolean autoCreate;

    /**
     * Constructor for the SlicingDice backend.
     *
     * @param databaseKey - the api key used to connect to the SlicingDice account
     * @param autoCreate - if true we will auto create fields and dimensions when the request
     *                   arrives
     */
    public SlicingDiceBackendImpl(final String databaseKey, final boolean autoCreate) {
        super(SLICING_DICE_HOST, SLICING_DICE_PORT, IS_SSL, false, null, null, null, null, MAX_CONNECTIONS, MAX_CONNECTIONS);

        this.databaseKey = databaseKey;
        this.autoCreate = autoCreate;
    }
}
