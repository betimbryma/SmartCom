package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageListener {

    /**
     * This method will be notified if there is a new message available.
     * @param message that has been received
     */
    public void onMessage(Message message);
}
