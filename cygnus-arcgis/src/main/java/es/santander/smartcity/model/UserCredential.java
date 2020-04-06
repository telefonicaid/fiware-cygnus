/**
 * 
 */
package es.santander.smartcity.model;

import java.time.LocalDateTime;

/**
 * @author dmartinez
 *
 */
public class UserCredential extends Credential {

    private String user;
    private String password;
    private String referer;

    /**
     * 
     */
    private UserCredential() {
        super();
    }

    /**
     * @param token
     */
    public UserCredential(String user, String passwd) {
        super();
        this.user = user;
        this.password = passwd;
    }

    /**
     * Constructor.
     * @param user
     * @param passwd
     * @param token
     * @param expiration
     */
    public UserCredential(String user, String passwd, String token, LocalDateTime expiration) {
        this(user, passwd);
        this.setToken(token, expiration);
    }

    /**
     * Constructor.
     * @param user
     * @param passwd
     * @param token
     * @param expirationMillis
     */
    public UserCredential(String user, String passwd, String token, int expirationMillis) {
        this(user, passwd);
        this.setTokenMillis(token, expirationMillis);
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the referer
     */
    public String getReferer() {
        return referer;
    }

    /**
     * @param referer
     *            the referer to set
     */
    public void setReferer(String referer) {
        this.referer = referer;
    }

}
