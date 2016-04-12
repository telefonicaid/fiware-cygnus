/**
 * Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

package com.telefonica.iot.cygnus.backends.hdfs;

/**
 * Interface for those backends implementing the persistence in HDFS.
 * 
 * @author frb
 */
public interface HDFSBackend {
    
    /**
     * Supported file formats when writting to HDFS.
     */
    public enum FileFormat { JSONROW, CSVROW, JSONCOLUMN, CSVCOLUMN };
    
    /**
     * Creates a directory in HDFS given its relative path. The absolute path will be built as:
     * hdfs:///user/\<hdfsUser\>/\<dirPath\>
     * 
     * @param dirPath Directory to be created
     * @throws Exception
     */
    void createDir(String dirPath) throws Exception;
    
    /**
     * Creates a file in HDFS with initial content given its relative path. The absolute path will be build as:
     * hdfs:///user/\<hdfsUser\>/\<filePath\>
     * 
     * @param filePath File to be created
     * @param data Data to be written in the created file
     * @throws Exception
     */
    void createFile(String filePath, String data) throws Exception;
    
    /**
     * Appends data to an existent file in HDFS.
     * 
     * @param filePath File to be created
     * @param data Data to be appended in the file
     * @throws Exception
     */
    void append(String filePath, String data) throws Exception;
    
    /**
     * Checks if the file exists in HDFS.
     * 
     * @param filePath File that must be checked
     * @return
     * @throws Exception
     */
    boolean exists(String filePath) throws Exception;
    
} // HDFSBackend
