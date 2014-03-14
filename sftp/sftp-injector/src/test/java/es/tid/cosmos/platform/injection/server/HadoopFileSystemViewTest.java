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

import org.apache.log4j.Logger;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * HadoopFileSystemViewTest.
 *
 * @author logc
 */
public class HadoopFileSystemViewTest extends BaseSftpTest {

    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(HadoopFileSystemView.class);
    private String userName;
    private Configuration conf;
    private HadoopFileSystemView hadoopFileSystemView;

    /**
     * Constructor.
     */
    public HadoopFileSystemViewTest() {
        super(LOGGER);
    } // HadoopFileSystemViewTest

    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.userName = "test";
        this.conf = new Configuration();
        this.hadoopFileSystemView = new HadoopFileSystemView(this.userName, this.conf);
    } // setUp

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetFileWithEmptyFilename() throws Exception {
        HadoopSshFile file = this.hadoopFileSystemView.getFile("");
        FileSystem hadoopFS = FileSystem.get(URI.create(this.conf.get("fs.default.name")), this.conf);/*, this.userName);*/
        String homePath = hadoopFS.getHomeDirectory().toString().replaceFirst(hadoopFS.getUri().toString(), "");
        assertEquals(homePath, file.getAbsolutePath());
        assertEquals(homePath.substring(homePath.lastIndexOf("/") + 1), file.getName());
    } // testGetFileWithEmptyFilename

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetFile() throws Exception {
        HadoopSshFile bar = this.hadoopFileSystemView.getFile("/foo/bar");
        assertEquals("bar", bar.getName());
        assertEquals("/foo/bar", bar.getAbsolutePath());
        assertFalse(bar.doesExist());
        HadoopSshFile bq = this.hadoopFileSystemView.getFile(bar, "bq");
        assertEquals("bq", bq.getName());
        assertEquals("/foo/bar/bq", bq.getAbsolutePath());
        assertFalse(bq.doesExist());
    } // testGetFile

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testRedirectionToHomePath() throws Exception {
        FileSystem fs = FileSystem.get(URI.create(this.conf.get("fs.default.name")), this.conf);//, "test");
        String homePath = fs.getHomeDirectory().toString().replaceFirst(fs.getUri().toString(), "");
        HadoopSshFile init = this.hadoopFileSystemView.getFile(new HadoopSshFile(".", "test", fs), ".");
        assertEquals(homePath, init.getAbsolutePath());
    } // testRedirectionToHomePath
    
} // HadoopFileSystemViewTest
