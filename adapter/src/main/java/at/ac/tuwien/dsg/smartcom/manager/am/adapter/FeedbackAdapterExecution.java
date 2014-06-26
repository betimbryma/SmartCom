package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class FeedbackAdapterExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FeedbackAdapterExecution.class);

    private final FeedbackPullAdapter adapter;
    private final String id;
    private final MessageBroker broker;

    public FeedbackAdapterExecution(FeedbackPullAdapter adapter, String id, MessageBroker broker) {
        this.adapter = adapter;
        this.id = id;
        this.broker = broker;
    }

    public void init() {
        //nothing to do here yet
    }

    public FeedbackAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            log.info("Waiting for requests...");
            Message message = broker.receiveRequests(id);
            if (message == null) {
                log.info("Received interrupted!");
                break;
            }
            Message response = null;
            try {
                response = adapter.pull();
            } catch (AdapterException e) {
                log.error("Error while checking response of adapter ()", id, e);
            }
            if (response != null) {
                enhanceMessage(response);
                log.info("Received response {}", response);

                broker.publishFeedback(response);
            } else {
                handleNoMessageReceived();
            }
        }
    }

    private void handleNoMessageReceived() {
        //TODO what should we do here?
        // proposal:
        //      if message has a sender, create and send a feedback to the sender,
        //          that there is no feedback available
        //      otherwise: don't send a message
    }

    private void enhanceMessage(Message response) {
        if (response.getSenderId() == null || response.getSenderId().isEmpty()) {
            response.setSenderId(id);
        }
    }
}
