package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.CredentialRestApi;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestAuthentication;

import junit.framework.Assert;

public class CredentialRestApiTest extends CredentialRestApi implements ArcgisBaseTest {

    public CredentialRestApiTest() throws ArcgisException {
        super(PORTAL_GENERATE_TOKEN_URL, null, PORTAL_FEATURETABLE_URL);
    }

    @Test
    public void test() {

        try {
            Credential credential = RestAuthentication.createUserToken(PORTAL_USER, PORTAL_PASSWORD,
                    new URL(PORTAL_GENERATE_TOKEN_URL), PORTAL_FEATURETABLE_URL, new Integer(1));
            System.out.println("ExpirationTime: " + credential.getExpirationTime());
            this.setCredential(credential);
            credential = getCredential();
            System.out.println("ExpirationTime after getCredential(): " + credential.getExpirationTime());
            System.out.println(" ------------> TOKEN: " + credential.getToken());
            String token = credential.getToken();
            Assert.assertTrue("Bad Credential", !"".equals(token));

            credential = getCredential();
            System.out.println("ExpirationTime after getCredential(): " + credential.getExpirationTime());
            System.out.println(" ------------> TOKEN: " + credential.getToken());
            String token2 = credential.getToken();
            Assert.assertTrue("Bad Credential", !token.equals(token2));

            Thread.sleep(61000);

            credential = getCredential();
            System.out.println("new ExpirationTime: " + credential.getExpirationTime());
            System.out.println(" ++----------> TOKEN: " + credential.getToken());
            Assert.assertTrue("Bad Credential", !token.equals(credential.getToken()));

        } catch (ArcgisException e) {
            System.err.println(e);
            fail("Cannot get credential.");
        } catch (Exception e) {
            System.err.println(e);
            fail("Cannot get credential.");
        }
    }

}
