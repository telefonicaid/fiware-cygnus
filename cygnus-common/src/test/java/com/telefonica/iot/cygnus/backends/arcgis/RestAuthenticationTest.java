/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
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

package com.telefonica.iot.cygnus.backends.arcgis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.ArcgisBaseTest;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestAuthentication;

/**
 * @author dmartinez
 *
 */
public class RestAuthenticationTest implements ArcgisBaseTest {

    /**
     * 
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {

    }

    /**
     * 
     */
    @Test
    public void getPortalTokenTest() {
        Credential credential;
        
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                System.out.println("------- TEST getPortalTokenTest()");
                credential = RestAuthentication.createUserToken(PORTAL_USER, PORTAL_PASSWORD,
                        new URL(PORTAL_GENERATE_TOKEN_URL),
                                                                "https://sags1.int.ayto-santander.es/arcgis/rest/services/Policia", 0, 0);
                System.out.println("Recovered credential: " + credential);
                assertTrue(!"".equals(credential.getToken()));
            } catch (MalformedURLException e) {
                System.err.println("ERROR: Malformed Url.");
                fail();
            } catch (ArcgisException e) {
                System.err.println(e.getMessage());
                System.err.println("ERROR: Cant Generate credential.");
                fail();
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }
    }

    /**
     * 
     */
    @Test
    public void getOnlineTokenTest() {
        Credential credential;
        
        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                System.out.println("------- TEST getOnlineTokenTest()");
    
                credential = RestAuthentication.createUserToken(ONLINE_USER, ONLINE_PASSWORD,
                                                                new URL(ONLINE_GENERATE_TOKEN_URL), "*", 0, 0);
                System.out.println("Recovered Token: " + credential);
                assertTrue(!"".equals(credential.getToken()));
            } catch (MalformedURLException e) {
                System.err.println("ERROR: Malformed Url.");
                fail();
            } catch (ArcgisException e) {
                System.err.println("ERROR: Cant Generate token.");
                fail();
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }
    }

}
