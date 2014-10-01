package at.ac.tuwien.dsg.smartcom.exception;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ErrorCode {
    private final int id;
    private final String message;

    public ErrorCode(int id, String message) {
        this.id = id;
        this.message = message;
    }

    /**
     * Returns the error code number that represents the error code
     * @return the error code number
     */
    public int getErrorNumber() {
        return id;
    }

    /**
     * Returns the message of the error code
     * @return message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error #"+ id + ": '"+message+"'";
    }
}
