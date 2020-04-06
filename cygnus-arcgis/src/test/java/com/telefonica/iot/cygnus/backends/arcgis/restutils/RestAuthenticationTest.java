/**
 * 
 */
package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestAuthentication;

/**
 * @author dmartinez
 *
 */
public class RestAuthenticationTest implements ArcgisBaseTest {

    @Before
    public void setup() throws Exception {

    }

    @Test
    public void getPortalTokenTest() {
        Credential credential;
        try {
            System.out.println("------- TEST getPortalTokenTest()");
            credential = RestAuthentication.createUserToken(PORTAL_USER, PORTAL_PASSWORD,
                    new URL(PORTAL_GENERATE_TOKEN_URL),
                    "https://sags1.int.ayto-santander.es/arcgis/rest/services/Policia");
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
    }

    @Test
    public void getOnlineTokenTest() {
        Credential credential;
        try {
            System.out.println("------- TEST getOnlineTokenTest()");

            credential = RestAuthentication.createUserToken(ONLINE_USER, ONLINE_PASSWORD,
                    new URL(ONLINE_GENERATE_TOKEN_URL), "*");
            System.out.println("Recovered Token: " + credential);
            assertTrue(!"".equals(credential.getToken()));
        } catch (MalformedURLException e) {
            System.err.println("ERROR: Malformed Url.");
            fail();
        } catch (ArcgisException e) {
            System.err.println("ERROR: Cant Generate token.");
            fail();
        }
    }

}
