/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of injector-server (FI-WARE project).
 *
 * injector-server is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * injector-server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with injector-server. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.cosmos.platform.injection.server;

import java.net.URI;
import java.net.URL;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author sortega
 */
public class Configuration {
    private static final String DB_NAME = "DB_NAME";
    private static final String DB_PASSWORD = "DB_PASS";
    private static final String DB_USER = "DB_USER";
    private static final String FRONTEND_DB = "FRONTEND_DB";
    private static final String HDFS_URL = "HDFS_URL";
    private static final String JOBTRACKER_URL = "JOBTRACKER_URL";
    private static final String PORT = "SERVER_SOCKET_PORT";
    private static final int DEFAULT_PORT = 22;

    private final PropertiesConfiguration properties;

    /**
     * 
     * @param url
     * @throws ConfigurationException
     */
    public Configuration(URL url) throws ConfigurationException {
        this.properties = new PropertiesConfiguration(url);
    } // Configuration

    public String getDbName() {
        return this.properties.getString(DB_NAME);
    } // getDbName

    public String getDbPassword() {
        return this.properties.getString(DB_PASSWORD);
    } // getDbPassword

    public String getDbUser() {
        return this.properties.getString(DB_USER);
    } // getDbUser

    public String getFrontendDbUrl() {
        return this.properties.getString(FRONTEND_DB);
    } // getFrontendDbUrl

    public URI getHdfsUrl() {
        return URI.create(this.properties.getString(HDFS_URL));
    } // getHdfsUrl

    public String getJobTrackerUrl() {
        return this.properties.getString(JOBTRACKER_URL);
    } // getJobTrackerUrl

    public int getPort() {
        return this.properties.getInt(PORT, DEFAULT_PORT);
    } // getPort
    
} // Configuration
