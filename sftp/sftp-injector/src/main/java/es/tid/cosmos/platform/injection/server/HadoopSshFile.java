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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.AccessControlException;
import org.apache.log4j.Logger;
import org.apache.sshd.server.SshFile;

//import es.tid.cosmos.base.util.Logger;

/**
 * HadoopSshFile.
 *
 * @author logc
 * @since  CTP 2
 */
public class HadoopSshFile implements SshFile {
    
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(HadoopSshFile.class);
    private final Path hadoopPath;
    private final String userName;
    private final FileSystem hadoopFS;
    private FSDataOutputStream fsDataOutputStream;
    private FSDataInputStream fsDataInputStream;

    /**
     * 
     * @param fileName
     * @param userName
     * @param hadoopFS
     * @throws IOException
     * @throws InterruptedException
     */
    protected HadoopSshFile(final String fileName, String userName, FileSystem hadoopFS)
        throws IOException, InterruptedException {
        this.hadoopPath = new Path(fileName);
        this.userName = userName;
        this.hadoopFS = hadoopFS;
        this.fsDataInputStream = null;
        this.fsDataOutputStream = null;
    } // HadoopSshFile

    @Override
    public String getAbsolutePath() {
        return this.hadoopPath.toString();
    } // getAbsolutePath

    @Override
    public String getName() {
        return this.hadoopPath.getName();
    } // getName

    @Override
    public String getOwner() {
        try {
            return this.hadoopFS.getFileStatus(this.hadoopPath).getOwner();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return this.userName;
        } // try catch
    } // getOwner

    @Override
    public boolean isDirectory() {
        try {
            return this.hadoopFS.getFileStatus(this.hadoopPath).isDir();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        } // try catch
    } // isDirectory

    @Override
    public boolean isFile() {
        try {
            return this.hadoopFS.isFile(this.hadoopPath);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        } // try catch
    } // isFile

    @Override
    public boolean doesExist() {
        try {
            return this.hadoopFS.exists(this.hadoopPath);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        } // try catch
    } // doesExist

    /**
     * Answer if the current user permissions on the current path are enough to perform a requested action. This method
     * is the common basis for the following permission-related methods, such as isReadable, isWritable, etc.
     *
     * @param queriedFsAction an action such as read, write ...
     * @return                is this user allowed to perform this action?
     */
    private boolean isAllowed(FsAction queriedFsAction) {
        try {
            String pathOwner = this.hadoopFS.getFileStatus(this.hadoopPath)
                    .getOwner();
            FsPermission permission = this.hadoopFS
                    .getFileStatus(this.hadoopPath).getPermission();
            FsAction action =
                    (pathOwner.equals(this.userName) ? permission.getUserAction() : permission.getOtherAction());
            return action.implies(queriedFsAction);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        } // try catch
    } // isAllowed

    @Override
    public boolean isReadable() {
        return isAllowed(FsAction.READ);
    } // isReadable

    @Override
    public boolean isWritable() {
        if (this.doesExist()) {
            return isAllowed(FsAction.WRITE);
        } else {
            return this.getParentFile().isWritable();
        } // if else
    } // isWritable

    @Override
    public boolean isExecutable() {
        return isAllowed(FsAction.EXECUTE);
    } // isExecutable

    @Override
    public boolean isRemovable() {
        return isAllowed(FsAction.WRITE);
    } // isRemovable

    @Override
    public SshFile getParentFile() {
        try {
            return new HadoopSshFile(this.hadoopPath.getParent().toString(), this.userName, this.hadoopFS);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
            return null;
        } // try catch
    } // getParentFile

    @Override
    public long getLastModified() {
        try {
            FileStatus fileStatus = this.hadoopFS.getFileStatus(this.hadoopPath);
            return fileStatus.getModificationTime();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return 0L;
        } // try catch
    } // getLastModified

    @Override
    public boolean setLastModified(long time) {
        try {
            this.hadoopFS.setTimes(this.hadoopPath, time, -1);
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } // try catch
        
        return false;
    } // setLastModified

    @Override
    public long getSize() {
        try {
            return this.hadoopFS.getFileStatus(this.hadoopPath).getLen();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return 0L;
        } // try catch
    } // getSize

    @Override
    public boolean mkdir() {
        try {
            if (this.getParentFile().doesExist() && this.getParentFile().isWritable()
                    && this.hadoopFS.mkdirs(this.hadoopPath)) {
                LOG.info("directory " + this.getAbsolutePath() + "created by user" + this.userName);
                return true;
            } // if
            
            return false;
        } catch (IOException e) {
            LOG.error(String.format("cannot create dir: %s because of %s", this.getAbsolutePath(), e.getMessage()), e);
            return false;
        } // try catch
    } // mkdir

    @Override
    public boolean delete() {
        try {
            return this.hadoopFS.delete(this.hadoopPath, this.isDirectory());
        } catch (IOException e) {
            LOG.error(String.format("cannot delete path: %s because of %s", this.getAbsolutePath(), e.getMessage()), e);
            return false;
        } // try catch
    } // delete

    @Override
    public boolean create() throws IOException {
        return this.hadoopFS.createNewFile(this.hadoopPath);
    } // create

    @Override
    public void truncate() throws IOException {
        this.hadoopFS.create(this.hadoopPath, true).close();
    } // truncate

    @Override
    public boolean move(SshFile destination) {
        try {
            Path dest = new Path(destination.getAbsolutePath());
            return this.hadoopFS.rename(this.hadoopPath, dest);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } // try catch
        
        return false;
    } // move

    @Override
    public List<SshFile> listSshFiles() {
        if (!this.isDirectory()) {
            return null;
        } // if
        
        try {
            FileStatus[] fileStatuses = this.hadoopFS.listStatus(this.hadoopPath);
            LinkedList<SshFile> files = new LinkedList<SshFile>();
            
            for (FileStatus fileStatus : fileStatuses) {
                String fileName = fileStatus.getPath().getName();
                files.add(new HadoopSshFile(this.appendToPath(fileName), this.userName, this.hadoopFS));
            } // for
            
            return Collections.unmodifiableList(files);
        } catch (AccessControlException e) {
            LOG.error(e.getMessage(), e);
            
            try {
                return Collections.singletonList((SshFile) new HadoopSshFile(Path.CUR_DIR, this.userName,
                        this.hadoopFS));
            } catch (IOException e1) {
                LOG.error(e1.getMessage(), e);
            } catch (InterruptedException e1) {
                LOG.error(e1.getMessage(), e);
            } // try catch
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        } // try catch
        
        return null;
    } // listSshFiles

    /**
     * 
     * @param fileName
     * @return
     */
    private String appendToPath(String fileName) {
        String absPath = this.getAbsolutePath();
        return ((absPath.endsWith(Path.SEPARATOR)) ? absPath : (absPath + Path.SEPARATOR)) + fileName;
    } // appendToPath

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        if (!this.isWritable()) {
            throw new IOException("No write permission for: " + this.getAbsolutePath());
        } // if
        
        // when offset != 0, append with bufferSize?
        this.fsDataOutputStream = this.hadoopFS.create(this.hadoopPath);
        return this.fsDataOutputStream;
    } // createOutputStream

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        if (!this.isReadable()) {
            throw new IOException("no read permission for: " + this.getAbsolutePath());
        } // if
        
        // when offset != 0, append with bufferSize?
        this.fsDataInputStream = this.hadoopFS.open(this.hadoopPath);
        return this.fsDataInputStream;
    } // createInputStream

    @Override
    public void handleClose() throws IOException {
        try {
            if (this.fsDataOutputStream != null) {
                this.fsDataOutputStream.close();
            } // if
            
            if (this.fsDataInputStream != null) {
                this.fsDataInputStream.close();
            } // if
        } catch (Exception e) {
            LOG.info("closed path handle that was not open", e);
        } // try catch
    } // handleClose
    
} // HadoopSshFile
