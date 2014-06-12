package at.ac.tuwien.dsg.smartcom.exception;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ErrorCode {
    /**
     * Returns the error code number that represents the error code
     * and that should be unique (note that this can't be guaranteed)
     * @return the error code number
     */
    public int getErrorNumber();
}
