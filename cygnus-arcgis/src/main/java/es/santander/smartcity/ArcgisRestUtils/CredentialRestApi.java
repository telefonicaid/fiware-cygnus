/**
 * 
 */
package es.santander.smartcity.ArcgisRestUtils;

import java.net.MalformedURLException;
import java.net.URL;

import es.santander.smartcity.exceptions.ArcgisException;
import es.santander.smartcity.model.Credential;

/**
 * @author dmartinez
 *
 */
public abstract class CredentialRestApi extends RestApi {

    protected URL tokenGenUrl;

    protected Credential credential;
    protected String referer;

    /**
     * @param tokenGenUrl
     * @param credential
     * @param referer
     * @param expirationMins
     */
    public CredentialRestApi(URL tokenGenUrl, Credential credential, String referer) {
        super();
        this.tokenGenUrl = tokenGenUrl;
        this.credential = credential;
        this.referer = referer;
    }

    /**
     * @param tokenGenUrl
     * @param credential
     * @param referer
     * @param expirationMins
     * @throws ArcgisException
     */
    public CredentialRestApi(String tokenGenUrl, Credential credential, String referer) throws ArcgisException {
        super();

        this.credential = credential;
        this.referer = referer;
        try {
            if (tokenGenUrl != null && !"".equals(tokenGenUrl.trim())) {
                this.tokenGenUrl = new URL(tokenGenUrl);
            } else {
                this.tokenGenUrl = null;
            }
        } catch (MalformedURLException e) {
            throw new ArcgisException("Can´t parse token generation url, MalformedURLException.");
        } catch (Exception e) {
            throw new ArcgisException("CredentialRestApi, Unexpected Exception " + e.toString());
        }
    }

    /**
     * Obtiene las credenciales de acceso, refresca el token si es necesario.
     * 
     * @return Credential
     * @throws ArcgisException
     */
    public Credential getCredential() throws ArcgisException {
        logDebug("------------------ getCredential() " + "\n\t tokenGenUrl: " + tokenGenUrl + "\n\t credential: "
                + credential + "\n\t credential.isExpired(): " + (credential != null ? credential.isExpired() : null));
        if (tokenGenUrl != null && (credential == null || credential.isExpired())) {
            logDebug("Creating/Refreshing token.");
            credential = RestAuthentication.createToken(credential, tokenGenUrl, referer);
        }

        return credential;
    }

    /**
     * Obtiene las credenciales de acceso, refresca el token si es necesario.
     * 
     * @param credential
     * @throws ArcgisException
     */
    protected void setCredential(Credential credential) throws ArcgisException {
        this.credential = credential;
    }

    /**
     * @return the tokenGenUrl
     */
    public String getTokenGenUrl() {
        if (tokenGenUrl == null) {
            return "";
        } else {
            return tokenGenUrl.toString();
        }
    }

    /**
     * @param tokenGenUrl
     *            the tokenGenUrl to set
     */
    public void setTokenGenUrl(URL tokenGenUrl) {
        this.tokenGenUrl = tokenGenUrl;
    }

}
