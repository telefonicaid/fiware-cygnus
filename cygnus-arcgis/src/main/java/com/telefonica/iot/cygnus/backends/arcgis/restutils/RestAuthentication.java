package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.http.HttpResponse;
import com.telefonica.iot.cygnus.backends.arcgis.model.Credential;
import com.telefonica.iot.cygnus.backends.arcgis.model.UserCredential;

import com.telefonica.iot.cygnus.backends.arcgis.restutils.RestApi;

/**
 * 
 * @author dmartinez
 *
 */
public class RestAuthentication extends RestApi {

    private static final String REQUEST_FORMAT_PARAMETER = "pjson";
    private static final String APP_RESPONSE_EXPIRES_TAG = "expires_in";
    private static final String ONLINE_RESPONSE_EXPIRES_TAG = "expires";
    private static final String ONLINE_RESPONSE_TOKEN_TAG = "token";
    private static final String APP_RESPONSE_TOKEN_TAG = "access_token";

    /**
     * Extracts token from Json response.
     * 
     * @param tokenJson
     * @return
     * @throws ArcGisException
     */
    public static String tokenFromJson(String tokenJson, String tokenTag) throws ArcgisException {
        String result = null;
        if ("".equals(tokenTag)) {
            tokenTag = ONLINE_RESPONSE_TOKEN_TAG;
        }

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(tokenJson).getAsJsonObject();
        JsonElement node = json.get(tokenTag);

        if (node != null) {
            result = node.getAsString();
            JsonElement expirationElement = json.get(ONLINE_RESPONSE_EXPIRES_TAG);

            if (expirationElement == null) {
                expirationElement = json.get(APP_RESPONSE_EXPIRES_TAG);
                System.out.println("Expiration: " + expirationElement.getAsLong());
            } else {
                long expiration = expirationElement.getAsLong();
                Instant expirationInstant = Instant.ofEpochMilli(expiration);
                if (expirationInstant.isBefore(Instant.now())) {
                    // token is expired.
                    System.out.println("Token expired at " + expirationInstant);
                    return null;
                }
            }
        } else {
            String errorDesc = "Invalid token response format.";
            if (json.get("error") != null) {
                errorDesc = json.get("error").toString();
            }
            throw new ArcgisException(errorDesc);
        }

        return result;
    }

    /**
     * 
     * @param tokenJson
     * @param expirationTag
     * @return
     * @throws ArcgisException
     */
    public static LocalDateTime expirationFromJson(String tokenJson, String expirationTag)
            throws ArcgisException {
        LocalDateTime result = null;
        if ("".equals(expirationTag)) {
            expirationTag = ONLINE_RESPONSE_EXPIRES_TAG;
        }

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(tokenJson).getAsJsonObject();
        JsonElement node = json.get(expirationTag);

        if (node != null) {
            JsonElement expirationElement = json.get(ONLINE_RESPONSE_EXPIRES_TAG);

            if (expirationElement == null) {
                expirationElement = json.get(APP_RESPONSE_EXPIRES_TAG);
            }

            long expiration = expirationElement.getAsLong();

            result = LocalDateTime.ofInstant(Instant.ofEpochMilli(expiration),
                    ZoneId.systemDefault());
        } else {
            String errorDesc = "Invalid token response format.";
            if (json.get("error") != null) {
                errorDesc = json.get("error").toString();
            }
            throw new ArcgisException(errorDesc);
        }

        return result;
    }

    /**
     * Request a token for a non OAuth token authentication.
     * 
     * @param cred
     * @param tokenGenUrl
     * @param referer
     * @param expirationMins
     * @return token
     * @throws ArcGisException
     */
    public static Credential createUserToken(String user, String password, URL tokenGenUrl,
            String referer, Integer expirationMins) throws ArcgisException {
        String tokenJSON = null;
        try {
            Map<String, String> bodyParams = new LinkedHashMap<String, String>();
            bodyParams.put("username", user);
            bodyParams.put("password", password);
            bodyParams.put("referer", referer);
            bodyParams.put("username", user);
            if (expirationMins != null) {
                bodyParams.put("expiration", expirationMins.toString());
            }
            bodyParams.put("f", REQUEST_FORMAT_PARAMETER);

            HttpResponse response = httpPost(tokenGenUrl.toString(), bodyParams);

            if (response.getResponseCode() == 200) {
                tokenJSON = response.getBody();
                System.out.println("    tokenJSON: " + tokenJSON);
                String token = tokenFromJson(tokenJSON, ONLINE_RESPONSE_TOKEN_TAG);
                LocalDateTime expiration = expirationFromJson(tokenJSON,
                        ONLINE_RESPONSE_EXPIRES_TAG);

                System.out.println("Token recived from service: " + token);
                return new UserCredential(user, password, token, expiration);
            } else {
                throw new ArcgisException(response.toString());
            }
        } catch (ArcgisException e) {
            throw e;
        } catch (Exception e) {
            throw new ArcgisException("createUserToken, Unexpected Exception " + e.toString());
        }
    }

    /**
     * Request a token for a non OAuth token authentication.
     * 
     * @param cred
     * @param tokenGenUrl
     * @param referer
     * @return token
     * @throws ArcGisException
     */
    public static Credential createUserToken(String user, String password, URL tokenGenUrl,
            String referer) throws ArcgisException {
        return createUserToken(user, password, tokenGenUrl, referer, null);
    }

    /**
     * Crea el token dentro de un objeto credential.
     * 
     * @param credential
     * @param tokenGenUrl
     * @param referer
     * @param expirationMins
     * @return
     * @throws ArcgisException
     */
    public static Credential createToken(Credential credential, URL tokenGenUrl, String referer)
            throws ArcgisException {

        if (credential instanceof UserCredential) {
            UserCredential userCredential = (UserCredential) credential;
            credential = createUserToken(userCredential.getUser(), userCredential.getPassword(),
                    tokenGenUrl, referer);

        } else {
            throw new ArcgisException("Invalid Credential type.");
        }
        return credential;

    }
}
