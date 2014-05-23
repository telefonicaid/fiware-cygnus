/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of cosmos-injector (FI-WARE project).
 *
 * cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with [PROJECT NAME]. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * frb@tid.es
 */

package es.tid.fiware.orionconnectors.cosmosinjector.hdfs;

import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Interface for those backends implementing the persistence in HDFS.
 * 
 * @author frb
 */
public interface HDFSBackend {
    
    /**
     * Creates a directory in HDFS.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param dirPath Directory to be created.
     */
    void createDir(DefaultHttpClient httpClient, String dirPath) throws Exception;
    
    /**
     * Creates a file in HDFS with initial content.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param filePath File to be created.
     * @param data Data to be written in the created file.
     */
    void createFile(DefaultHttpClient httpClient, String filePath, String data) throws Exception;
    
    /**
     * Appends data to an existent file in HDFS.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param filePath File where to be append the data.
     * @param data Data to be appended in the file.
     */
    void append(DefaultHttpClient httpClient, String filePath, String data) throws Exception;
    
    /**
     * Checks if the file exists in HDFS.
     * 
     * @param httpClient HTTP client for accessing the backend server.
     * @param filePath File that must be checked.
     */
    boolean exists(DefaultHttpClient httpClient, String filePath) throws Exception;
    
} // HDFSBackend
