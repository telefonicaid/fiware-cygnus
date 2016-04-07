/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 * <p>
 * This file is part of fiware-cygnus (FI-WARE project).
 * <p>
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 * <p>
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.backends.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cassandra related operations (database and table creation, context data insertion) when dealing with a Cassandra
 * persistence backend.
 * <p>
 * Adapted from {@link com.telefonica.iot.cygnus.backends.postgresql.PostgreSQLBackendImpl}
 *
 * @author jdegenhardt
 */
public class CassandraBackendImpl implements CassandraBackend {

    private static final CygnusLogger LOGGER = new CygnusLogger(CassandraBackendImpl.class);

    private static final String CREATE_KEYSPACE = "CREATE KEYSPACE IF NOT EXISTS %s"
            + " WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s %s";
    private CassandraDriver driver;

    /**
     * Constructor.
     *
     * @param cassandraUsername Username under what to login at Cassandra
     * @param cassandraPassword Password to use with username to login at Cassandra
     * @param cassandraNodes    Nodes (typically IPs) where cassandra cluster is running
     * @throws IllegalArgumentException gets thrown if a parameter is null or empty
     */
    public CassandraBackendImpl(String cassandraUsername,
                                String cassandraPassword,
                                String[] cassandraNodes) throws IllegalArgumentException {
        try {
            Validate.notNull(cassandraUsername, "cassandraUsername must not be null");
            Validate.notNull(cassandraPassword, "cassandraPassword must not be null");
            Validate.notEmpty(cassandraNodes, "cassandraNodes must not be null or empty");
            Validate.noNullElements(cassandraNodes, "cassandraNodes must not be null or contain null elements");
        } catch (NullPointerException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        }

        driver = new CassandraDriver(cassandraUsername, cassandraPassword, cassandraNodes);
    } // CassandraBackendImpl

    protected CassandraDriver getDriver() {
        return driver;
    } // getDriver

    /**
     * Sets the CassandraDriver driver. It is protected since it is only used by the tests.
     *
     * @param driver The CassandraDriver driver to be set.
     */
    protected void setDriver(CassandraDriver driver) {
        this.driver = driver;
    } // setDriver

    /**
     * Creates a keyspace given its name, if not exists.
     *
     * @param keyspaceName Keyspace name that shall get created
     */
    @Override
    public void createKeyspace(String keyspaceName) throws IllegalArgumentException {
        try {
            Validate.notBlank(keyspaceName, "keyspaceName must not be null or blank");
        } catch (NullPointerException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        }
        Session session = driver.getSession(null);

        // TODO get replication factor
        int replicationFactor = 3;
        String query = String.format(CREATE_KEYSPACE, keyspaceName, replicationFactor);
        executeQuery(session, query);
    } // createKeyspace

    /**
     * Executes a query within a given session.
     *
     * @param session session in what the query shall get executed
     * @param query   a simple query string
     */
    private void executeQuery(Session session, String query) {
        Statement create = new SimpleStatement(query);
        LOGGER.debug("Executing CQL query '" + query + "'");
        session.execute(create);
    }

    /**
     * Creates a table, given its name, if not exists in the given keyspace.
     *
     * @param keyspaceName    Keyspace name at what the table shall get created
     * @param tableName       name of the table that shall get created
     * @param typedFieldNames a list representing the columns and data types e.g.: "(name text, count bigint)"
     */
    @Override
    public void createTable(String keyspaceName,
                            String tableName,
                            String typedFieldNames) throws IllegalArgumentException {
        try {
            Validate.notBlank(keyspaceName, "keyspaceName must not be null or blank");
            Validate.notBlank(tableName, "tableName must not be null or blank");
            Validate.notBlank(typedFieldNames, "typedFieldNames must not be null or blank");
            Validate.isTrue(typedFieldNames.startsWith("("), "typedFieldNames"
                    + " must be a list and thus start with an opening bracket");
            Validate.isTrue(typedFieldNames.endsWith(")"), "typedFieldNames"
                    + " must be a list and thus end with a closing bracket");
        } catch (NullPointerException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        }
        Session session = driver.getSession(keyspaceName);

        typedFieldNames = typedFieldNames.substring(1);
        typedFieldNames = "(id uuid PRIMARY KEY," + typedFieldNames;

        String query = String.format(CREATE_TABLE, tableName, typedFieldNames);
        executeQuery(session, query);
    } // createTable

    @Override
    public void insertContextData(String keyspaceName, String tableName, String fieldNames, String fieldValues) {
        try {
            Validate.notBlank(keyspaceName, "keyspaceName must not be null or blank");
            Validate.notBlank(tableName, "tableName must not be null or blank");
            Validate.notBlank(fieldNames, "fieldNames must not be null or blank");
            Validate.notBlank(fieldValues, "fieldValues must not be null or blank");
            Validate.isTrue(fieldNames.startsWith("("), "fieldNames"
                    + " must be a list and thus start with an opening bracket");
            Validate.isTrue(fieldNames.endsWith(")"), "fieldNames must be a list and thus end with a closing bracket");
            Validate.isTrue(fieldValues.startsWith("("), "fieldValues"
                    + " must be a list and thus start with an opening bracket");
            Validate.isTrue(fieldValues.endsWith(")"), "fieldValues"
                    + " must be a list and thus end with a closing bracket");
        } catch (NullPointerException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e);
            throw new IllegalArgumentException(e);
        }

        Session session = driver.getSession(keyspaceName);

        String[] names = splitToArray(fieldNames);
        String[] namesWithId = new String[names.length + 1];
        namesWithId[0] = "id";
        System.arraycopy(names, 0, namesWithId, 1, names.length);
        String[] values = splitToArray(fieldValues);
        String[] valuesWithUUID = new String[values.length + 1];
        valuesWithUUID[0] = UUID.randomUUID().toString();
        System.arraycopy(values, 0, valuesWithUUID, 1, values.length);
        Insert insert = QueryBuilder.insertInto(tableName).values(namesWithId, valuesWithUUID);
        LOGGER.debug("Executing CQL query '" + insert.getQueryString() + "'");
        session.execute(insert);
    } // insertContextData

    /**
     * Splits the String to an array.
     *
     * @param str a list of Strings like "(string1, string2, string3)
     * @return an array containing the strings
     */
    private String[] splitToArray(String str) {
        String[] array = str.split(",");
        for (int i = 0; i < array.length; i++) {
            String tmp = array[i];
            while (tmp.startsWith("(")) {
                tmp = tmp.substring(1);
            } // while
            while (tmp.endsWith(")")) {
                tmp = tmp.substring(0, tmp.length() - 1);
            } // while
            tmp = tmp.trim();
            array[i] = tmp;
        } // for
        return array;
    } // splitToArray

    /**
     * This class represents the collection of created sessions to a Cassandra Cluster.
     */
    class CassandraDriver {

        private final Map<String, Session> sessions;
        private final Cluster cassandraCluster;
        private final String cassandraUsername;
        private final String cassandraPassword;

        /**
         * Constructor to create a Cassandra Driver.
         *
         * @param cassandraUsername Username under what to login at Cassandra
         * @param cassandraPassword Password to use with username to login at Cassandra
         * @param cassandraNodes    Nodes (typically IPs) where Cassandra is running
         */
        CassandraDriver(String cassandraUsername, String cassandraPassword, String... cassandraNodes) {
            this.sessions = new HashMap<String, Session>();
            this.cassandraUsername = cassandraUsername;
            this.cassandraPassword = cassandraPassword;
            this.cassandraCluster = getCluster(cassandraNodes);
        } // CassandraDriver

        /**
         * Gets a session with a connection to the Cassandra cluster.
         *
         * @param keyspaceName Specifies the keyspace that shall be used
         * @return a session with a connection to a cassandra keyspace
         */
        Session getSession(String keyspaceName) {

            Session session = sessions.get(keyspaceName);

            if (session == null || session.isClosed()) {
                if (keyspaceName == null) {
                    session = cassandraCluster.connect();
                } else {
                    session = cassandraCluster.connect(keyspaceName);
                }
                sessions.put(keyspaceName, session);
            } // if

            return session;
        } // getSession

        /**
         * Gets the number of sessions created.
         *
         * @return The number of sessions created
         */
        int numConnectionsCreated() {
            return sessions.size();
        } // numConnectionsCreated

        /**
         * Gets if a session is created for the given keyspace.
         *
         * @param keyspaceName name of the keyspace to what a session shall exist
         * @return True if the session exists, false otherwise
         */
        boolean isSessionCreated(String keyspaceName) {
            return sessions.containsKey(keyspaceName);
        } // isSessionCreated

        /**
         * Creates a Cassandra connection.
         *
         * @param cassandraNodes The Cassandra nodes to connect to (usually an IP address)
         * @return A Cassandra cassandraCluster
         */
        private Cluster getCluster(String... cassandraNodes) {
            return Cluster.builder()
                    .addContactPoints(cassandraNodes)
                    .withCredentials(cassandraUsername, cassandraPassword)
                    .build();
        } // getCluster
    } // CassandraDriver
} // CassandraBackendImpl

