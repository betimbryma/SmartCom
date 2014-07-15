package at.ac.tuwien.dsg.smartcom.exception;


import java.util.Map;
import java.util.TreeMap;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class CommunicationException extends Exception {
    private ErrorCode errorCode;
    private Map<String, Object> properties = new TreeMap<>();

    public static CommunicationException wrap(Throwable exception, ErrorCode errorCode) {
        if (exception instanceof CommunicationException) {
            CommunicationException se = (CommunicationException)exception;
            if (errorCode != null && errorCode != se.getErrorCode()) {
                return new CommunicationException(exception.getMessage(), exception, errorCode);
            }
            return se;
        } else {
            return new CommunicationException(exception.getMessage(), exception, errorCode);
        }
    }

    public static CommunicationException wrap(Throwable exception) {
        return wrap(exception, null);
    }

    public CommunicationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CommunicationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public CommunicationException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public CommunicationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public CommunicationException setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object get(String name) {
        return properties.get(name);
    }

    public CommunicationException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "CommunicationException{" +
                "errorCode=" + errorCode +
                '}';
    }
}
