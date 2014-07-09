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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import com.google.common.io.Files;
//import org.apache.hadoop.thirdparty.guava.common.io.Files;
import org.apache.sshd.server.SshFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author logc
 */
public class HadoopSshFileTest extends BaseSftpTest {

    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(HadoopSshFile.class);
    public static final String USER01 = "/user01";
    private HadoopSshFile hadoopSshFile;
    private HadoopSshFile hadoopSshDir;
    private FileSystem hadoopFS;
    private FileSystem mockedFileSystem;
    private HadoopSshFile neverExists;
    private File tempDir;

    /**
     * Constructor.
     */
    public HadoopSshFileTest() {
        super(LOGGER);
    } // HadoopSshFileTest

    /**
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Before
    public void setUp() throws IOException, InterruptedException {
        Configuration configuration = new Configuration();
        this.tempDir = Files.createTempDir();
        boolean success = this.tempDir.setWritable(true, false);
        
        if (!success) {
            throw new IllegalStateException("could not set to writable: " + this.tempDir.toString());
        } // if
        
        String foodir = this.tempDir.getAbsolutePath().concat(USER01);
        configuration.set("fs.default.name", "file:///" + this.tempDir.toString());
        this.hadoopFS = FileSystem.get(configuration);
        this.hadoopSshDir = new HadoopSshFile(foodir, "user01", this.hadoopFS);
        this.hadoopSshFile = new HadoopSshFile(foodir + "/file01", "user01", this.hadoopFS);
        this.mockedFileSystem = mock(FileSystem.class);
        this.neverExists = new HadoopSshFile("/in/fantasy", "whatever_user", this.mockedFileSystem);
    } // setUp

    /**
     * 
     * @throws IOException
     */
    @After
    public void tearDown() throws IOException {
        if (this.hadoopSshFile.doesExist()) {
            this.hadoopSshFile.delete();
        } // if
        
        if (this.hadoopSshDir.doesExist()) {
            this.hadoopSshDir.delete();
        } // if
        
        boolean success = this.tempDir.delete();
        
        if (!success) {
            throw new IllegalStateException("could not delete: " + this.tempDir.toString());
        } // if
        
        this.hadoopFS.close();
        this.mockedFileSystem.close();
    } // tearDown

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetAbsolutePath() throws Exception {
        assertEquals(this.tempDir.getAbsolutePath().concat(USER01).concat("/file01").replaceAll("\\\\", "/"),
                this.hadoopSshFile.getAbsolutePath());
    } // testGetAbsolutePath

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetName() throws Exception {
        assertEquals("file01", this.hadoopSshFile.getName());
    } // testGetName

    /**
     * Show that the owner is the logged in user as long as the file does not exist. This complies with clients that
     * request information about a path before creating it. The owner is the username of the Java process after
     * creation, since these tests use the native filesystem underneath.
     *
     * @throws Exception
     */
    @Test
    public void testGetOwner() throws Exception {
        assertEquals("user01", this.hadoopSshFile.getOwner());
        assertFalse(this.hadoopSshFile.doesExist());
        this.hadoopSshFile.create();
        assertNotSame("user01", this.hadoopSshFile.getOwner());
    } // testGetOwner

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testIsDirectory() throws Exception {
        this.hadoopSshDir.mkdir();
        assertTrue(this.hadoopSshDir.isDirectory());
    } // testIsDirectory

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testIsDirectoryDoesNotThrowException() throws Exception {
        assertFalse(this.hadoopSshDir.doesExist());
        assertFalse(this.hadoopSshDir.isDirectory());
    } // testIsDirectoryDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testDoesExist() throws Exception {
        assertFalse(this.hadoopSshFile.doesExist());
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.doesExist());
    } // testDoesExist

    /**
     * Show that method doesExist does not send an exception when it encounters an Exception.
     */
    @Test
    public void testExceptionDoesExist() throws Exception {
        when(this.mockedFileSystem.exists(Matchers.<Path>any()))
                .thenThrow(new IOException("you have been mocked"));
        assertFalse(this.neverExists.doesExist());
    } // testExceptionDoesExist

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testIsReadable() throws Exception {
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.isReadable());
    } // testIsReadable

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testIsFile() throws Exception {
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.isFile());
    } // testIsFile

    /**
     * Show that when isFile finds an IOException, e.g. the file is not found, the function returns false instead of
     * re-throwing the exception.
     *
     * @throws Exception
     */
    @Test
    public void testIsFileDoesNotThrowException() throws Exception {
        when(this.mockedFileSystem.isFile(Matchers.<Path>any())).thenThrow(new IOException("you have been mocked"));
        assertFalse(this.neverExists.isFile());
    } // testIsFileDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testIsWritable() throws Exception {
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.isWritable());
    } // testIsWritable

    /**
     * When a path does not yet exist, the write permission of its parent path is returned.
     *
     * @throws Exception
     */
    @Test
    public void testIsWritableWhenNotYetExisting() throws Exception {
        this.hadoopSshDir.create();
        assertTrue(this.hadoopSshDir.isWritable());
        assertFalse(this.hadoopSshFile.doesExist());
        assertTrue(this.hadoopSshFile.isWritable());
    } // testIsWritableWhenNotYetExisting

    /**
     * Show that there is no sense of an executable permission in HDFS. Anything is not executable before creation, and
     * executable afterwards, even directories.
     *
     * @throws Exception
     */
    @Test
    public void testIsExecutable() throws Exception {
        assertFalse(this.hadoopSshFile.isExecutable());
        this.hadoopSshDir.mkdir();
        assertTrue(this.hadoopSshDir.isExecutable());
    } // testIsExecutable

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testIsRemovable() throws Exception {
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.isRemovable());
    } // testIsRemovable

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetParentFile() throws Exception {
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.getParentFile().toString().startsWith(
                "es.tid.cosmos.platform.injection.server.HadoopSshFile@"));
    } // testGetParentFile

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetLastModified() throws Exception {
        this.hadoopSshFile.create();
        assertNotSame(0, this.hadoopSshFile.getLastModified());
    } // testGetLastModified

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testSetLastModified() throws Exception {
        this.hadoopSshFile.create();
        long fixedTime = System.currentTimeMillis();
        this.hadoopSshFile.setLastModified(fixedTime);
        // This assertion is not assertEquals because there is a precision
        // mismatch between HDFS and System.currentTimeMillis; we try to
        // write with more decimal places than can be read. What we can say is
        // that this difference has an upper limit.
        long retrievedTime = this.hadoopSshFile.getLastModified();
        assertTrue(String.format("sent: %s, got: %s, diff: %s", fixedTime, retrievedTime, fixedTime - retrievedTime),
                Math.abs(fixedTime - retrievedTime) < 1000);
    } // testSetLastModified

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testSetLastModifiedDoesNotThrowException() throws Exception {
        doThrow(new IOException("times could not be set")).when(this.mockedFileSystem).setTimes(Matchers.<Path>any(),
                Matchers.anyLong(), Matchers.anyLong());
        assertFalse(this.neverExists.setLastModified(123L));
    } // testSetLastModifiedDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetSize() throws Exception {
        this.hadoopSshFile.create();
        OutputStream ostream = this.hadoopSshFile.createOutputStream(0);
        ostream.write("Hello world".getBytes());
        ostream.close();
        assertEquals(11, this.hadoopSshFile.getSize());
    } // testGetSize

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetSizeDoesNotThrowException() throws Exception {
        when(this.mockedFileSystem.getFileStatus(Matchers.any(Path.class)))
                .thenThrow(new IOException("mocked"));
        this.neverExists.create();
        assertEquals(0L, this.neverExists.getSize());
    } // testGetSizeDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testMkdir() throws Exception {
        assertTrue(this.hadoopSshDir.mkdir());
    } // testMkdir

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testMkdirDoesNotThrowException() throws Exception {
        when(this.mockedFileSystem.mkdirs(Matchers.<Path>any())).thenThrow(new IOException("could not create dir"));
        this.neverExists.create();
        assertFalse(this.neverExists.mkdir());
    } // testMkdirDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {
        this.hadoopSshFile.create();
        assertTrue(this.hadoopSshFile.doesExist());
        assertTrue(this.hadoopSshFile.delete());
        assertFalse(this.hadoopSshFile.doesExist());
    } // testDelete

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteDoesNotThrowException() throws Exception {
        HadoopSshFile mockFile = mock(HadoopSshFile.class);
        when(mockFile.isDirectory()).thenReturn(true);
        mockFile.create();
        when(this.mockedFileSystem.delete(Matchers.<Path>any(), Matchers.anyBoolean())).thenThrow(
                new IOException("could not delete path"));
        assertFalse(mockFile.delete());
    } // testDeleteDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        assertTrue(this.hadoopSshFile.create());
        assertTrue(this.hadoopSshFile.doesExist());
    } // testCreate

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testTruncate() throws Exception {
        this.hadoopSshFile.create();
        OutputStream ostream = this.hadoopSshFile.createOutputStream(0);
        ostream.write("Hello world".getBytes());
        ostream.close();
        assertEquals(11, this.hadoopSshFile.getSize());
        this.hadoopSshFile.truncate();
        assertEquals(0, this.hadoopSshFile.getSize());
    } // testTruncate

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testMove() throws Exception {
        String newsubdir = this.tempDir.getAbsolutePath().concat("/user01/new/file01");
        HadoopSshFile newfoo = new HadoopSshFile(newsubdir, "user01", this.hadoopFS);
        this.hadoopSshFile.create();
        assertFalse(newfoo.doesExist());
        this.hadoopSshFile.move(newfoo);
        assertTrue(newfoo.doesExist());
    } // testMove

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testMoveDoesNotThrowException() throws Exception {
        when(this.mockedFileSystem.rename(Matchers.<Path>any(), Matchers.<Path>any())).thenThrow(new IOException(
                "could not rename this path"));
        assertFalse(this.neverExists.move(new HadoopSshFile("/wherever", "some_user", this.mockedFileSystem)));
    } // testMoveDoesNotThrowException

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testListSshFiles() throws Exception {
        this.hadoopSshFile.create();
        List<SshFile> fileList = this.hadoopSshDir.listSshFiles();
        assertEquals(1, fileList.size());
        SshFile found = fileList.get(0);
        assertEquals(this.hadoopSshFile.getAbsolutePath(), found.getAbsolutePath());
    } // testListSshFiles

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testListFilesWhenDirectoryNotReadable() throws Exception {
        when(this.mockedFileSystem.listStatus(Matchers.<Path>any())).thenThrow(
                new AccessControlException("not authorized"));
        HadoopSshFile mockedDir = spy(this.neverExists);
        doReturn(true).when(mockedDir).isDirectory();
        assertEquals(1, mockedDir.listSshFiles().size());
    } // testListFilesWhenDirectoryNotReadable

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testWriteToFile() throws Exception {
        this.hadoopSshFile.create();
        OutputStream ostream = this.hadoopSshFile.createOutputStream(0L);
        ostream.write("Hello world".getBytes());
        this.hadoopSshFile.handleClose();
        assertEquals(11, this.hadoopSshFile.getSize());
    } // testWriteToFile

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testReadFromFile() throws Exception {
        this.hadoopSshFile.create();
        InputStream istream = this.hadoopSshFile.createInputStream(0L);
        int read = istream.read();
        istream.close();
        assertEquals(-1, read);
        String written = "Hello world";
        OutputStream outputStream = this.hadoopSshFile.createOutputStream(0L);
        outputStream.write(written.getBytes());
        this.hadoopSshFile.handleClose();
        InputStream inputStream = this.hadoopSshFile.createInputStream(0L);
        StringWriter writer = new StringWriter();
        int byteRead;
        
        while ((byteRead = inputStream.read()) != -1) {
            writer.write(byteRead);
        } // while
        
        assertEquals(written, writer.toString());
    } // testReadFromFile
    
} // HadoopSshFileTest
