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

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.ArcgisBaseTest;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.CredentialRestApi;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestAuthentication;

/**
 * 
 * @author dmartinez
 *
 */
public class CredentialRestApiTest extends CredentialRestApi implements ArcgisBaseTest {

    /**
     * 
     * @throws ArcgisException
     */
    public CredentialRestApiTest() throws ArcgisException {
        super(PORTAL_GENERATE_TOKEN_URL, null, PORTAL_FEATURETABLE_URL);
    }

    /**
     * 
     */
    @Test
    public void testUserCredential() {

        if (!ArcgisBaseTest.connectionTestsSkipped()){
            try {
                Credential credential = RestAuthentication.createUserToken(PORTAL_USER, PORTAL_PASSWORD,
                        new URL(PORTAL_GENERATE_TOKEN_URL), PORTAL_FEATURETABLE_URL, new Integer(1));
                System.out.println("ExpirationTime: " + credential.getExpirationTime());
                this.setCredential(credential);
                credential = getCredential();
                System.out.println(
                        "ExpirationTime after getCredential(): " + credential.getExpirationTime());
                System.out.println(" ------------> TOKEN: " + credential.getToken());
                String token = credential.getToken();
                assertTrue("Bad Credential", !"".equals(token));
    
                credential = getCredential();
                System.out.println(
                        "ExpirationTime after getCredential(): " + credential.getExpirationTime());
                System.out.println(" ------------> TOKEN: " + credential.getToken());
                String token2 = credential.getToken();
                assertTrue("Bad Credential", !token.equals(token2));
    
                Thread.sleep(61000);
    
                credential = getCredential();
                System.out.println("new ExpirationTime: " + credential.getExpirationTime());
                System.out.println(" ++----------> TOKEN: " + credential.getToken());
                assertTrue("Bad Credential", !token.equals(credential.getToken()));
    
            } catch (ArcgisException e) {
                System.err.println(e);
                fail("Cannot get credential.");
            } catch (Exception e) {
                System.err.println(e);
                fail("Cannot get credential.");
            }
        } else {
            System.out.println(" -- Skipped");
            assertTrue(true);
        }
    }

}
