package at.ac.tuwien.dsg.smartcom.mb;

import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.queue.FeedbackRaiser;

/**
 * A broker that will be used to send messages using a queue or a
 * similar mechanism. Will only be used internally.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageBroker extends FeedbackRaiser {

    public Message receiveFeedback();

    public Message receiveRequests(String id);

    public void publishRequest(String id, Message message);

    public Message receiveTasks(String id);

    public void publishTask(String id, Message message);
}
