package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
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

    private final FeedbackAdapterFacade adapter;
    private final String id;
    private final MessageBroker broker;

    public FeedbackAdapterExecution(FeedbackAdapterFacade adapter, String id, MessageBroker broker) {
        this.adapter = adapter;
        this.id = id;
        this.broker = broker;
    }

    public void init() {
        //nothing to do here yet
    }

    public void preDestroy() {
        this.adapter.preDestroy();
    }

    public FeedbackAdapter getAdapter() {
        return adapter.getAdapter();
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
                response = adapter.checkForResponse();
            } catch (AdapterException e) {
                log.error("Error while checking response of adapter ()", id, e);
            }
            if (response != null) {
                enhanceMessage(response);
                log.info("Received response {}", response);

                broker.publishFeedback(response);
            } else {
                //TODO
            }
        }
    }

    private void enhanceMessage(Message response) {
        if (response.getSenderId() == null || response.getSenderId().isEmpty()) {
            response.setSenderId(id);
        }
    }
}
