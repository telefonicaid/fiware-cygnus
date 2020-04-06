/**
 * 
 */
package es.santander.smartcity.exceptions;

/**
 * @author dmartinez
 *
 */
public class ArcgisException extends Exception {
    protected int code;

    /**
     * 
     * @param message
     */
    public ArcgisException(String message) {
        super(message);
        code = 500;
    }

    /**
     * 
     * @param e
     */
    public ArcgisException(Exception e) {
        super(e);
        code = 500;
    }

    /**
     * 
     * @param code
     * @param message
     */
    public ArcgisException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 
     * @return code
     */
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        String result = "";
        if (code > 0) {
            result = code + " ";
        }
        return result + this.getMessage();
    }

}
