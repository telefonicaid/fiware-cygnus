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
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;

/**
 * HadoopFileSystemFactory.
 *
 * @author logc
 * @since  CTP 2
 */
public class HadoopFileSystemFactory implements FileSystemFactory {
    
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(HadoopFileSystemFactory.class);
    private final Configuration configuration;

    /**
     * 
     * @param configuration
     */
    public HadoopFileSystemFactory(Configuration configuration) {
        this.configuration = configuration;
    } // HadoopFileSystemFactory

    @Override
    public FileSystemView createFileSystemView(Session session) throws IOException {
        try {
            return new HadoopFileSystemView(session.getUsername(),
                    this.configuration);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } // try catch
    } // createFileSystemView
    
} // HadoopFileSystemFactory
