package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.queue.InputPublisher;

/**
 * A broker that will be used to send messages using a queue or a
 * similar mechanism. Will only be used internally.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageBroker extends InputPublisher {

    /**
     * Receives input from the underlying communication channel.
     * Might block until there is input available.
     *
     * @return input
     */
    public Message receiveInput();

    /**
     * Register a message listener that will be notified on the arrival of
     * new input messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for input messages
     */
    public void registerInputListener(MessageListener listener);

    /**
     * Receives request messages for a given id. Might block until there
     * is a message available.
     *
     * @param id that the request message is assigned to
     * @return the message
     */
    public Message receiveRequests(Identifier id);

    /**
     * Register a message listener that will be notified on the arrival of
     * new request messages for a given receiver. Note that a message listener
     * will consume the message and that there can only be one listener at a
     * time for each id.
     *
     * @param id the id of the request receiver
     * @param listener for request messages
     */
    public void registerRequestListener(Identifier id, MessageListener listener);

    /**
     * Publish a new request message for a given id.
     *
     * @param id that the messages is dedicated to
     * @param message that should be published
     */
    public void publishRequest(Identifier id, Message message);

    /**
     * Receives task messages for a given id. Might block until there
     * is a message available.
     *
     * @param id that the task message is assigned to
     * @return the message
     */
    public Message receiveTasks(Identifier id);

    /**
     * Register a message listener that will be notified on the arrival of
     * new task messages for a given receiver. Note that a message listener
     * will consume the message and that there can only be one listener at a
     * time for each id.
     *
     * @param id the id of the task receiver
     * @param listener for task messages
     */
    public void registerTaskListener(Identifier id, MessageListener listener);

    /**
     * Publish a task message for a given receiver destination id (note that
     * this is not the same as the receiver id! One receiver id can be resolved
     * to many receiver destination ids).
     *
     * @param id of the receiver destination
     * @param message that should be published
     */
    public void publishTask(Identifier id, Message message);

    /**
     * Publish a control message.
     *
     * @param message control message
     */
    public void publishControl(Message message);

    /**
     * Receives control messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveControl();

    /**
     * Register a message listener that will be notified on the arrival of
     * new control messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for control messages
     */
    public void registerControlListener(MessageListener listener);

    /**
     * Publish authentication request message.
     *
     * @param message that should be published
     */
    public void publishAuthRequest(Message message);

    /**
     * Receives authentication messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveAuthRequest();

    /**
     * Register a message listener that will be notified on the arrival of
     * new authentication messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for authentication messages
     */
    public void registerAuthListener(MessageListener listener);

    /**
     * publish message info request message.
     *
     * @param message that should be published
     */
    public void publishMessageInfoRequest(Message message);

    /**
     * Receives message info request messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveMessageInfoRequest();

    /**
     * Register a message listener that will be notified on the arrival of
     * new message info request messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for message info request messages
     */
    public void registerMessageInfoListener(MessageListener listener);

    /**
     * publishes a metrics request message.
     *
     * @param message that should be published
     */
    public void publishMetricsRequest(Message message);

    /**
     * Receives metrics request messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveMetricsRequest();

    /**
     * Register a message listener that will be notified on the arrival of
     * new metrics request messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for metrics request messages
     */
    public void registerMetricsListener(MessageListener listener);

    /**
     * publishes a log message
     *
     * @param message that should be published
     */
    public void publishLog(Message message);

    /**
     * Receives log messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveLog();

    /**
     * Register a message listener that will be notified on the arrival of
     * new log messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for log messages
     */
    public void registerLogListener(MessageListener listener);
}
