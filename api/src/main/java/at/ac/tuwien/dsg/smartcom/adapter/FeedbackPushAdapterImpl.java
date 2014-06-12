package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.queue.FeedbackPublisher;

/**
 * Implementation of the FeedbackPushAdapter that provides a method
 * to inform the system of newly arrived feedback. This class should be
 * implemented instead of the FeedbackPushAdapter interface for adapters
 * that use push to communicate with external communication channels.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class FeedbackPushAdapterImpl implements FeedbackPushAdapter {

    private FeedbackPublisher feedbackPublisher;

    /**
     * Publish a message that has been received. this method should only be
     * called when implementing a push service to notify the middleware that
     * there was a new message.
     *
     * @param message Message that has been received.
     */
    protected final void publishMessage(Message message) {
        feedbackPublisher.publishFeedback(message);
    }

    public final void setFeedbackPublisher(FeedbackPublisher feedbackPublisher) {
        this.feedbackPublisher = feedbackPublisher;
    }
}
