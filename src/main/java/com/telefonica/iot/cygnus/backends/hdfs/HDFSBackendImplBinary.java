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

import com.telefonica.iot.cygnus.backends.hive.HiveBackend;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.Constants;
import com.telefonica.iot.cygnus.utils.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.PrivilegedExceptionAction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

/**
 *
 * @author frb
 */
public class HDFSBackendImplBinary implements HDFSBackend {
    
    private final String hdfsUser;
    private final String hdfsPassword;
    private final String oauth2Token;
    private final String hiveServerVersion;
    private final String hiveHost;
    private final String hivePort;
    private final boolean serviceAsNamespace;
    private FSGetter fsGetter;
    private static final CygnusLogger LOGGER = new CygnusLogger(HDFSBackendImplREST.class);
    
    /**
     * 
     * @param hdfsHosts
     * @param hdfsPort
     * @param hdfsUser
     * @param hdfsPassword
     * @param oauth2Token
     * @param hiveServerVersion
     * @param hiveHost
     * @param hivePort
     * @param krb5
     * @param krb5User
     * @param krb5Password
     * @param krb5LoginConfFile
     * @param krb5ConfFile
     * @param serviceAsNamespace
     */
    public HDFSBackendImplBinary(String[] hdfsHosts, String hdfsPort, String hdfsUser, String hdfsPassword,
            String oauth2Token, String hiveServerVersion, String hiveHost, String hivePort, boolean krb5,
            String krb5User, String krb5Password, String krb5LoginConfFile, String krb5ConfFile,
            boolean serviceAsNamespace) {
        this.hdfsUser = hdfsUser;
        this.hdfsPassword = hdfsPassword;
        this.oauth2Token = oauth2Token;
        this.hiveServerVersion = hiveServerVersion;
        this.hiveHost = hiveHost;
        this.hivePort = hivePort;
        this.serviceAsNamespace = serviceAsNamespace;
        this.fsGetter = new FSGetter(hdfsHosts, hdfsPort);
    } // HDFSBackendImplBinary
    
    protected void setFSGetter(FSGetter fsGetter) {
        this.fsGetter = fsGetter;
    } // setFSGetter

    @Override
    public void createDir(String dirPath) throws Exception {
        CreateDirPEA pea = new CreateDirPEA(dirPath);
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
        ugi.doAs(pea);
    } // createDir

    @Override
    public void createFile(String filePath, String data) throws Exception {
        CreateFilePEA pea = new CreateFilePEA(filePath, data);
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
        ugi.doAs(pea);
    } // createFile

    @Override
    public void append(String filePath, String data) throws Exception {
        AppendPEA pea = new AppendPEA(filePath, data);
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
        ugi.doAs(pea);
    } // append

    @Override
    public boolean exists(String filePath) throws Exception {
        ExistsPEA pea = new ExistsPEA(filePath);
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
        ugi.doAs(pea);
        return pea.exists();
    } // exists

    @Override
    public void provisionHiveTable(FileFormat fileFormat, String dirPath, String fields) throws Exception {
        String tag;
        
        switch (fileFormat) {
            case JSONROW:
            case CSVROW:
                tag = "_row";
                break;
            case JSONCOLUMN:
            case CSVCOLUMN:
                tag = "_column";
                break;
            default:
                tag = "";
        } // switch
        
        // get the table name to be created
        // the replacement is necessary because Hive, due it is similar to MySQL, does not accept '-' in the table names
        String tableName = Utils.encodeHive((serviceAsNamespace ? "" : hdfsUser + "_") + dirPath) + tag;
        LOGGER.info("Creating Hive external table=" + tableName);
        
        // get a Hive client
        HiveBackend hiveClient = new HiveBackend(hiveServerVersion, hiveHost, hivePort, hdfsUser, hdfsPassword);
        
        // create the query
        String query;
        
        switch (fileFormat) {
            case JSONCOLUMN:
            case JSONROW:
                query = "create external table if not exists " + tableName + " (" + fields + ") row format serde "
                        + "'org.openx.data.jsonserde.JsonSerDe' location '/user/"
                        + (serviceAsNamespace ? "" : (hdfsUser + "/")) + dirPath + "'";
                break;
            case CSVCOLUMN:
            case CSVROW:
                query = "create external table if not exists " + tableName + " (" + fields + ") row format "
                        + "delimited fields terminated by ',' location '/user/"
                        + (serviceAsNamespace ? "" : (hdfsUser + "/")) + dirPath + "'";
                break;
            default:
                query = "";
        } // switch

        // execute the query
        if (!hiveClient.doCreateTable(query)) {
            LOGGER.warn("The HiveQL external table could not be created, but Cygnus can continue working... "
                    + "Check your Hive/Shark installation");
        } // if
    } // provisionHiveTable
    
    /**
     * Privileged Exception Action for creating a new HDFS directory.
     */
    private class CreateDirPEA implements PrivilegedExceptionAction {
        
        private final String dirPath;
        
        public CreateDirPEA(String dirPath) {
            this.dirPath = dirPath;
        } // CreateDirPEA

        @Override
        public Void run() throws Exception {
            String effectiveDirPath = "/user/" + (serviceAsNamespace ? "" : (hdfsUser + "/")) + dirPath;
            FileSystem fileSystem = fsGetter.get();
            Path path = new Path(effectiveDirPath);
        
            if (!fileSystem.mkdirs(path)) {
                fileSystem.close();
                throw new CygnusPersistenceError("The /user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                        + dirPath + " directory could not be created in HDFS");
            } // if
        
            fileSystem.close();
            return null;
        } // run
    
    } // CreateDirPEA
    
    /**
     * Privileged Exception Action for creating a new HDFS file with initial content.
     */
    private class CreateFilePEA implements PrivilegedExceptionAction {
        
        private final String filePath;
        private final String data;
        
        public CreateFilePEA(String filePath, String data) {
            this.filePath = filePath;
            this.data = data;
        } // CreateFilePEA

        @Override
        public Void run() throws Exception {
            String effectiveFilePath = "/user/" + (serviceAsNamespace ? "" : (hdfsUser + "/")) + filePath;
            FileSystem fileSystem = fsGetter.get();
            Path path = new Path(effectiveFilePath);
            FSDataOutputStream out = fileSystem.create(path);
        
            if (out == null) {
                fileSystem.close();
                throw new CygnusPersistenceError("The /user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                        + filePath + " file could not be created in HDFS");
            } // if
        
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data + "\n");
            writer.close();
            fileSystem.close();
            return null;
        } // run
    
    } // CreateFilePEA
    
    /**
     * Privileged Exception Action for appending data to an existing HDFS file.
     */
    private class AppendPEA implements PrivilegedExceptionAction {
        
        private final String filePath;
        private final String data;
        
        public AppendPEA(String filePath, String data) {
            this.filePath = filePath;
            this.data = data;
        } // AppendPEA

        @Override
        public Void run() throws Exception {
            String effectiveDirPath = "/user/" + (serviceAsNamespace ? "" : (hdfsUser + "/")) + filePath;
            FileSystem fileSystem = fsGetter.get();
            Path path = new Path(effectiveDirPath);
            FSDataOutputStream out = fileSystem.append(path);
        
            if (out == null) {
                fileSystem.close();
                throw new CygnusPersistenceError("The /user/" + (serviceAsNamespace ? "" : (hdfsUser + "/"))
                        + filePath + " file could not be created in HDFS");
            } // if
        
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.append(data + "\n");
            writer.close();
            fileSystem.close();
            return null;
        } // run
    
    } // AppendPEA
    
    /**
     * Privileged Exception Action for finding out wether a file exists or not.
     */
    private class ExistsPEA implements PrivilegedExceptionAction {
        
        private final String filePath;
        private boolean exists;
        
        public ExistsPEA(String filePath) {
            this.filePath = filePath;
        } // ExistsPEA

        @Override
        public Void run() throws Exception {
            String effectiveDirPath = "/user/" + (serviceAsNamespace ? "" : (hdfsUser + "/")) + filePath;
            FileSystem fileSystem = fsGetter.get();
            Path path = new Path(effectiveDirPath);
            exists = fileSystem.exists(path);
            fileSystem.close();
            return null;
        } // run
            
        public boolean exists() {
            return exists;
        } // exists
    
    } // ExistsPEA
    
    /**
     * Hadoop FileSystem getter method. By creating a protected class, this can be mocked.
     */
    protected class FSGetter {
        
        private final String[] hdfsHosts;
        private final String hdfsPort;
        
        /**
         * Constructor.
         * @param hdfsHosts
         * @param hdfsPort
         */
        public FSGetter(String[] hdfsHosts, String hdfsPort) {
            this.hdfsHosts = hdfsHosts;
            this.hdfsPort = hdfsPort;
        } // FSGetter
        
        /**
         * Gets a Hadoop FileSystem.
         * @return
         * @throws java.io.IOException
         */
        public FileSystem get() throws IOException {
            for (String hdfsHost: hdfsHosts) {
                Configuration conf = new Configuration();
                //conf.addResource(new Path("/Users/frb/devel/fiware/fiware-cygnus/conf/core-site.xml"));
                //conf.addResource(new Path("/Users/frb/devel/fiware/fiware-cygnus/conf/hdfs-site.xml"));
                conf.set("fs.default.name", "hdfs://" + hdfsHost + ":" + hdfsPort);
                return FileSystem.get(conf);
            } // for
            
            LOGGER.error("No HDFS file system could be got, the sink will not work!");
            return null;
        } // get
        
    } // FSGetter
    
} // HDFSBackendImplBinary
