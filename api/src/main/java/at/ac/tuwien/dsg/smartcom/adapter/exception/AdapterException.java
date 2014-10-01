package at.ac.tuwien.dsg.smartcom.adapter.exception;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AdapterException extends Exception {
    public AdapterException() {
    }

    public AdapterException(String message) {
        super(message);
    }

    public AdapterException(Throwable cause) {
        super(cause);
    }
}
