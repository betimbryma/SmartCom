package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * The Feedback Push Adapter API can be used to implement an adapter for a
 * communication channel that uses push to get notified of new messages. The
 * concrete implementation has to use the FeedbackPushAdapterImpl class, which
 * provides methods that support the implementation of the adapter. The external
 * tool/peer pushes the message to the adapter, which transforms the message into
 * the internal format and calls the publishMessage of the FeedbackPushAdapterImpl
 * class. This method delegates the message to the corresponding queue and
 * subsequently to the correct component of the system. The adapter has to
 * start a handler for the push notification (e.g., a handler that uses long
 * polling) in its init method.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 * @see at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapterImpl
 */
public interface FeedbackPushAdapter extends FeedbackAdapter {

    /**
     * Method that can be used to initialize the adapter and other handlers like a
     * push notification handler (if needed)
     */
    public void init();

    /**
     * Notifies the push adapter that it will be destroyed after the method returns.
     * Can be used to clean up and destroy handlers and so forth.
     */
    public void preDestroy();
}
