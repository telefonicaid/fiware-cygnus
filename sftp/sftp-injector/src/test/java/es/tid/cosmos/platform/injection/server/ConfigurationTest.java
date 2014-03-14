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

import java.net.URI;
import java.net.URL;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author sortega
 */
public class ConfigurationTest {

    /**
     * 
     * @throws Exception
     */
    @Test(expected = ConfigurationException.class)
    public void shouldThrowExceptionWhenNotFound() throws Exception {
        new Configuration(getClass().getResource("/not/existing"));
    } // shouldThrowExceptionWhenNotFound

    /**
     * 
     * @throws Exception
     */
    @Test
    public void shouldParseProperties() throws Exception {
        URL testConfig = getClass().getResource("test_config.properties");
        Configuration instance = new Configuration(testConfig);
        assertEquals(2222, instance.getPort());
        assertEquals(URI.create("hdfs://pshdp01:8011"), instance.getHdfsUrl());
        assertEquals("pshdp01:8012", instance.getJobTrackerUrl());
        assertEquals("jdbc:mysql:localhost", instance.getFrontendDbUrl());
        assertEquals("database", instance.getDbName());
        assertEquals("root", instance.getDbUser());
        assertEquals("toor", instance.getDbPassword());
    } // shouldParseProperties
    
} // ConfigurationTest
