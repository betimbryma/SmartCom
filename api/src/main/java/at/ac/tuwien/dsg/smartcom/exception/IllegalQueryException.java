package at.ac.tuwien.dsg.smartcom.exception;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class IllegalQueryException extends Exception {
    public IllegalQueryException() {
    }

    public IllegalQueryException(String message) {
        super(message);
    }
}
