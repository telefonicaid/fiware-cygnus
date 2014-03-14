package es.tid.fiware.livedemoapp.cosmos.sftpbasicclient;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Francisco Romero Bueno
 * 
 * Basic SFTP client showing how to work with the Cosmos SFTP-based injection server.
 * This is based on Java Secure Channel (JSch), http://www.jcraft.com/jsch/
 * 
 */
public final class SFTPBasicClient {
    
    private SFTPBasicClient() {
    } // SFTPBasicClient
 
    /**
     * 
     * @param args
     * @throws JSchException
     * @throws SftpException
     */
    public static void main(String[] args) throws JSchException, SftpException {
        // parameters
        String hostname = "change_to_cosmos_master_node_ip";
        int port = 2222;
        String username = "change_to_your_user";
        String password = "change_to_your_password";
        String hdfsDirectory = "/user/your_user/whatever_path";
        String localFile = "whatever_local_file";
 
        // do not use SSH authentication
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
 
        // create the JSch session
        JSch ssh = new JSch();
        Session session = ssh.getSession(username, hostname, port);
        session.setConfig(config);
        session.setPassword(password);
        session.connect();
        
        // create the SFTP channel
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;
        
        // do some example operations
        sftp.cd(hdfsDirectory);
        System.out.println(sftp.ls(hdfsDirectory).toString().replace("[", "").replace("]", "").replace(", ", "\r\n"));
        sftp.put(localFile, hdfsDirectory);
        System.out.println(sftp.ls(hdfsDirectory).toString().replace("[", "").replace("]", "").replace(", ", "\r\n"));
        
        // close both the SFTP channel and the JSch session
        channel.disconnect();
        session.disconnect();
    } // main
    
} // SFTPBasicClient