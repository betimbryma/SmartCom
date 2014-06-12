package at.ac.tuwien.dsg.smartcom.queue;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * This class will be used to publish feedback received from the push adapter.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface FeedbackPublisher {

    /**
     * Publish (inform the system) a new message that
     * has been received.
     *
     * @param message that has been received
     */
    public void publishFeedback(Message message);
}
