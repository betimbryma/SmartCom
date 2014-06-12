package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.queue.FeedbackPublisher;

/**
 * A broker that will be used to send messages using a queue or a
 * similar mechanism. Will only be used internally.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageBroker extends FeedbackPublisher {

    /**
     * Receives feedback from the underlying communication channel.
     * Might block until there is feedback available.
     *
     * @return feedback
     */
    public Message receiveFeedback();

    public void registerFeedbackListener(MessageListener listener);

    /**
     * Receives request messages for a given id. Might block until there
     * is a message available.
     *
     * @param id that the request message is assigned to
     * @return the message
     */
    public Message receiveRequests(String id);

    public void registerRequestListener(String id, MessageListener listener);

    /**
     * Publish a new request message for a given id.
     *
     * @param id that the messages is dedicated to
     * @param message that should be published
     */
    public void publishRequest(String id, Message message);

    public Message receiveTasks(String id);

    public void registerTaskListener(String id, MessageListener listener);

    public void publishTask(String id, Message message);

    public void publishControl(Message message);

    public Message receiveControl();

    public void registerControlListener(MessageListener listener);

    public void publishAuthRequest(Message message);

    public Message receiveAuthRequest();

    public void registerAuthListener(MessageListener listener);

    public void publishMessageInfoRequest(Message message);

    public Message receiveMessageInfoRequest();

    public void registerMessageInfoListener(MessageListener listener);

    public void publishMetricsRequest(Message message);

    public Message receiveMetricsRequest();

    public void registerMetricsListener(MessageListener listener);

    public void publishLog(Message message);

    public Message receiveLog();

    public void registerLogListener(MessageListener listener);
}
