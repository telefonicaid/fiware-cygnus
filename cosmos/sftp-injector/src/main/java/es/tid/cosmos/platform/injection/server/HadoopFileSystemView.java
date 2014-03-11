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

import java.io.IOException;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SshFile;

/**
 * HadoopFileSystemView.
 *
 * @author logc
 * @since  CTP 2
 */
public class HadoopFileSystemView implements FileSystemView {
    
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(HadoopFileSystemView.class);
    private String homePath;
    private FileSystem hadoopFS;
    private final String userName;

    /**
     * 
     * @param userName
     * @param configuration
     * @throws IOException
     * @throws InterruptedException
     */
    public HadoopFileSystemView(String userName, Configuration configuration) throws IOException, InterruptedException {
        this.userName = userName;
        
        try {
            this.hadoopFS = FileSystem.get(
                    URI.create(configuration.get("fs.default.name")), configuration);//, this.userName);
            this.homePath = this.hadoopFS.getHomeDirectory().toString().replaceFirst(
                    this.hadoopFS.getUri().toString(), "");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } // try catch
/*            
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } // try catch
*/
    } // HadoopFileSystemView

    @Override
    public HadoopSshFile getFile(String file) {
        try {
            return this.getFile("", file);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } // try catch
        
        return null;
    } // getFile

    @Override
    public HadoopSshFile getFile(SshFile baseDir, String file) {
        try {
            return this.getFile(baseDir.getAbsolutePath(), file);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } // try catch
        
        return null;
    } // getFile

    private HadoopSshFile getFile(String baseDir, String file) throws IOException, InterruptedException {
        String requestedDir = baseDir;
        String requestedFile = file;
        
        if (requestedDir.isEmpty() && (requestedFile.isEmpty() || requestedFile.equals(Path.CUR_DIR))) {
            requestedDir = this.homePath;
            requestedFile = "";
            LOG.debug("redirecting to home path: " + this.homePath);
        } // if
        
        String wholePath = requestedDir + requestedFile;
        
        if (!requestedDir.endsWith(Path.SEPARATOR) && !requestedFile.startsWith(Path.SEPARATOR)) {
            wholePath = requestedDir + Path.SEPARATOR + requestedFile;
        } // if
        
        return new HadoopSshFile(wholePath, this.userName, this.hadoopFS);
    } // getFile
    
} // HadoopFileSystemView
