package at.ac.tuwien.dsg.smartcom.callback.exception;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerAuthenticationException extends Exception {
    public PeerAuthenticationException() {
    }

    public PeerAuthenticationException(String message) {
        super(message);
    }
}
