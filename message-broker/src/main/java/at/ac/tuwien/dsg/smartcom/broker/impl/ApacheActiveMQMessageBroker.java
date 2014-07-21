package at.ac.tuwien.dsg.smartcom.broker.impl;

import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.broker.utils.BrokerErrorUtils;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Apache ActiveMQ Message Broker
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ApacheActiveMQMessageBroker implements MessageBroker {
    private static final Logger log = LoggerFactory.getLogger(ApacheActiveMQMessageBroker.class);
    private static final String requestQueuePrefix = "SmartCom.request.";
    private static final String taskQueuePrefix = "SmartCom.task.";

    private Connection connection;
    private Session session;

    private ThreadLocal<Session> localSession;
    private ThreadLocal<MessageProducer> localProducer;
    private List<Session> sessions;

    private Queue inputQueue;
    private Queue controlQueue;
    private Queue authQueue;
    private Queue messageInfoQueue;
    private Queue metricsQueue;
    private Queue logQueue;

    public ApacheActiveMQMessageBroker(String host, int port) throws CommunicationException {
        setUp(host, port);
    }

    private void setUp(String host, int port) throws CommunicationException {
        try {
            log.debug("Initialising Apache ActiveMQ Message Broker!");

            //ConnectionFactory for the Apache ActiveMQ instance
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://"+host+":"+port);

            //Connect to the instance
            connection = connectionFactory.createConnection();
            connection.start();

            sessions = Collections.synchronizedList(new ArrayList<Session>());

            //Sessions for this broker
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            setUpDestinations(session);

            localSession = new ThreadLocal<>();
            localProducer = new ThreadLocal<>();
        } catch (JMSException e) {
            throw BrokerErrorUtils.createBrokerException(e);
        }
    }

    private void setUpDestinations(Session session) throws JMSException {
        inputQueue = session.createQueue("SmartCom.input");
        controlQueue = session.createQueue("SmartCom.control");
        authQueue = session.createQueue("SmartCom.auth");
        messageInfoQueue = session.createQueue("SmartCom.messageInfo");
        metricsQueue = session.createQueue("SmartCom.metrics");
        logQueue = session.createQueue("SmartCom.log");
    }

    public void cleanUp() throws CommunicationException {
        try {
            for (Session session : sessions) {
                session.close();
            }

            session.close();
            connection.close();
        } catch (JMSException e) {
            throw BrokerErrorUtils.createBrokerException(e);
        }
    }

    @Override
    public Message receiveInput() {
        return receiveMessage(inputQueue);
    }

    @Override
    public void registerInputListener(final MessageListener listener) {
        setListener(listener, inputQueue);
    }

    @Override
    public void publishInput(Message message) {
        sendMessage(message, inputQueue);
    }

    /**
     * Receive a message from a destination.
     * @param destination of the message
     * @return the received message
     */
    private Message receiveMessage(Destination destination) {
        log.debug("Waiting for message in queue {}", destination);
        MessageConsumer consumer = null;
        try {
            consumer = session.createConsumer(destination);

            Message msg =  (Message) ((ObjectMessage) consumer.receive()).getObject();

            consumer.close();

            return msg;
        } catch (JMSException e) {
            log.error("Error while receiving "+destination.toString(), e);
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (JMSException e1) {
                    throw BrokerErrorUtils.createRuntimeBrokerException(e);
                }
            }
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    /**
     * registers a listener on the destination
     * @param listener that has to be registered
     * @param destination for the listener
     */
    private void setListener(final MessageListener listener, final Destination destination) {
        try {
            MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(new javax.jms.MessageListener() {
                @Override
                public void onMessage(javax.jms.Message message) {
                    try {
                        listener.onMessage((Message) ((ObjectMessage) message).getObject());
                    } catch (JMSException e) {
                        log.error("Error in message listener for "+destination.toString(), e);
                        throw BrokerErrorUtils.createRuntimeBrokerException(e);
                    }
                }
            });
        } catch (JMSException e) {
            log.error("Error while setting "+destination.toString()+" listener", e);
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    /**
     * Send a message to a specific destination. It uses thread local sessions and
     * producers for the sending of messages and creates them if they are not present.
     *
     * @param message that should be sent
     * @param destination of the message
     */
    private void sendMessage(Message message, Destination destination) {
        log.debug("Sending message {} to queue {}", message, destination);
        try {
            initLocalSessionAndProducer();

            ObjectMessage msg = localSession.get().createObjectMessage(message);
            localProducer.get().send(destination, msg);
        } catch (JMSException e) {
            log.error("Error while sending " + destination.toString() + " message", e);
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    /**
     * initialize thread local session and producer if there are no such
     * @throws JMSException
     */
    private void initLocalSessionAndProducer() throws JMSException {
        if (localSession.get() == null) {
            localSession.set(connection.createSession(false, Session.AUTO_ACKNOWLEDGE));
            sessions.add(localSession.get());

            MessageProducer producer = session.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            localProducer.set(producer);
        }
    }

    @Override
    public Message receiveRequests(Identifier id) {
        return receiveMessage(createDestination(requestQueuePrefix, id));
    }

    private Destination createDestination(String prefix, Identifier id) {
        try {
            return session.createQueue(prefix + id.getId());
        } catch (JMSException e) {
            log.error("Error while sending message to queue '{}' with id {} message", prefix, id, e);
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    @Override
    public void registerRequestListener(Identifier id, MessageListener listener) {
        setListener(listener, createDestination(requestQueuePrefix, id));
    }

    @Override
    public void publishRequest(Identifier id, Message message) {
        sendMessage(message, createDestination(requestQueuePrefix, id));
    }

    @Override
    public Message receiveTasks(Identifier id) {
        return receiveMessage(createDestination(taskQueuePrefix, id));
    }

    @Override
    public void registerTaskListener(Identifier id, MessageListener listener) {
        setListener(listener, createDestination(taskQueuePrefix, id));
    }

    @Override
    public void publishTask(Identifier id, Message message) {
        sendMessage(message, createDestination(taskQueuePrefix, id));
    }

    @Override
    public void publishControl(Message message) {
        sendMessage(message, controlQueue);
    }

    @Override
    public Message receiveControl() {
        return receiveMessage(controlQueue);
    }

    @Override
    public void registerControlListener(MessageListener listener) {
        setListener(listener, controlQueue);
    }

    @Override
    public void publishAuthRequest(Message message) {
        sendMessage(message, authQueue);
    }

    @Override
    public Message receiveAuthRequest() {
        return receiveMessage(authQueue);
    }

    @Override
    public void registerAuthListener(MessageListener listener) {
        setListener(listener, authQueue);
    }

    @Override
    public void publishMessageInfoRequest(Message message) {
        sendMessage(message, messageInfoQueue);
    }

    @Override
    public Message receiveMessageInfoRequest() {
        return receiveMessage(messageInfoQueue);
    }

    @Override
    public void registerMessageInfoListener(MessageListener listener) {
        setListener(listener, messageInfoQueue);
    }

    @Override
    public void publishMetricsRequest(Message message) {
        sendMessage(message, metricsQueue);
    }

    @Override
    public Message receiveMetricsRequest() {
        return receiveMessage(metricsQueue);
    }

    @Override
    public void registerMetricsListener(MessageListener listener) {
        setListener(listener, metricsQueue);
    }

    @Override
    public void publishLog(Message message) {
        sendMessage(message, logQueue);
    }

    @Override
    public Message receiveLog() {
        return receiveMessage(logQueue);
    }

    @Override
    public void registerLogListener(MessageListener listener) {
        setListener(listener, logQueue);
    }
}
