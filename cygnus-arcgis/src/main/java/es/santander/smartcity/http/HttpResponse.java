/**
 * 
 */
package es.santander.smartcity.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.santander.smartcity.exceptions.ArcgisException;

/**
 * @author dmartinez
 *
 */
public class HttpResponse {
    private int responseCode;
    private String responseMessage;
    private String body;
    private Map<String, List<String>> headers;
    private ArcgisException responseError;

    /**
     * Default Constructor.
     */
    public HttpResponse() {
        headers = new HashMap<String, List<String>>();
        responseError = null;
    }

    /**
     * Resposecode.
     */
    public HttpResponse(int responseCode) {
        this();
        this.responseCode = responseCode;
    }

    /**
     * Resposecode.
     */
    public HttpResponse(int responseCode, String body) {
        this(responseCode);
        this.body = body;
    }

    /**
     * @return the responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @param responseCode
     *            the responseCode to set
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the headers
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * @param headers
     *            the headers to set
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * @return the responseMessage
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * @param responseMessage
     *            the responseMessage to set
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Full toString conversion.
     */
    public String toFullString() {
        String json = "{ \"responseCode\" : " + responseCode + ", \"responseMessage\": \"" + responseMessage
                + ", \"body\": \"" + body + "\"}";
        return json;
    }

    /**
     * is it an error response?
     * 
     * @return
     */
    public boolean hasError() {
        if (responseCode >= 300) {
            return true;
        } else {
            return this.responseError != null;
        }
    }

    /**
     * is it a success response?
     * 
     * @return
     */
    public boolean isSuccessful() {

        if (responseCode >= 300) {
            return false;
        } else {
            return this.responseError == null;
        }
    }

    /**
     * Resumed toString conversion.
     */
    public String toString() {
        String json = "{ \"responseCode\" : " + responseCode + ", \"responseMessage\": \"" + responseMessage + "\"}";
        return json;
    }

    public void setError(ArcgisException exception) {
        this.responseError = exception;
    }

    public boolean isResponseCodeError() {
        return responseCode >= 300 || responseCode < 200;
    }

    /**
     * 
     * @return
     */
    public int getErrorCode() {
        int result = -1;
        if (responseError != null) {
            result = responseError.getCode();
        } else if (isResponseCodeError()) {
            result = responseCode;
        }
        return result;
    }

    /**
     * 
     * @return
     */
    public String getErrorMessage() {
        String result = "";
        if (responseError != null) {
            result = responseError.getMessage();
        } else if (isResponseCodeError()) {
            result = responseMessage;
        }
        return result;
    }
}
