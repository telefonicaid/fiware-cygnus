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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;

/**
 * InjectionServer connects an SFTP client to an HDFS filesystem.
 *
 * @author logc
 * @since  CTP 2
 */
public class InjectionServer {
    
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(InjectionServer.class);
    private HadoopFileSystemFactory hadoopFileSystemFactory;
    private final Configuration configuration;

    /**
     * Constructs this instance from the configured values.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    public InjectionServer(Configuration serverConfig) throws IOException, URISyntaxException, ConfigurationException {
        this.configuration = serverConfig;
        org.apache.hadoop.conf.Configuration hadoopConfig =
                new org.apache.hadoop.conf.Configuration();
        hadoopConfig.set("fs.default.name", serverConfig.getHdfsUrl()
                                                        .toString());
        hadoopConfig.set("mapred.job.tracker", serverConfig.getJobTrackerUrl());
        this.hadoopFileSystemFactory = new HadoopFileSystemFactory(hadoopConfig);
    } // InjectionServer

    /**
     * Sets up and start an SFTP server.
     */
    public void setupSftpServer() {
        SshServer sshd = SshServer.setUpDefaultServer();
        
        // general settings
        sshd.setFileSystemFactory(hadoopFileSystemFactory);
        sshd.setPort(this.configuration.getPort());
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        
        // user authentication settings
        sshd.setPasswordAuthenticator(setupPasswordAuthenticator());
        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);
        
        // command settings
        sshd.setCommandFactory(new ScpCommandFactory());
        
        // subsystem settings
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);

        try {
            sshd.start();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        } // try catch
    } // setupSftpServer

    /**
     * 
     * @return
     */
    private FrontendPassword setupPasswordAuthenticator() {
        FrontendPassword passwordAuthenticator = new FrontendPassword();
        passwordAuthenticator.setFrontendCredentials(this.configuration.getFrontendDbUrl(),
                this.configuration.getDbName(), this.configuration.getDbUser(), this.configuration.getDbPassword());
        return passwordAuthenticator;
    } // setupPasswordAuthenticator
    
} // InjectionServer
