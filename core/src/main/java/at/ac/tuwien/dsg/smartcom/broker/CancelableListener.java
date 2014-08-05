package at.ac.tuwien.dsg.smartcom.broker;

/**
 * Class that can be returned by a listener registration that allows the caller to cancel
 * the listener and release its resources.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface CancelableListener {

    /**
     * Cancels a registration (i.e., releases resources)
     */
    public void cancel();
}
