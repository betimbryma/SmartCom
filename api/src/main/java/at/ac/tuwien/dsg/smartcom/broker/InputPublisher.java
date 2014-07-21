package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * This class will be used to publish input received from the push adapter.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface InputPublisher {

    /**
     * Publish (inform the system) a new message that
     * has been received.
     *
     * @param message that has been received
     */
    public void publishInput(Message message);
}
