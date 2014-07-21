package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class InputAdapterExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(InputAdapterExecution.class);

    private final InputPullAdapter adapter;
    private final Identifier id;
    private final MessageBroker broker;

    public InputAdapterExecution(InputPullAdapter adapter, Identifier id, MessageBroker broker) {
        this.adapter = adapter;
        this.id = id;
        this.broker = broker;
    }

    public void init() {
        //nothing to do here yet
    }

    public InputAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            log.debug("Adapter {}: Waiting for requests...", id.getId());
            Message message = broker.receiveRequests(id);
            log.debug("Adapter {}: Received request {}", id.getId(), message);
            if (message == null) {
                log.debug("Adapter {}: Received interrupted!", id.getId());
                break;
            }
            Message response = null;
            try {
                response = adapter.pull();
            } catch (AdapterException e) {
                log.error("Adapter {}: Error while checking response", id.getId(), e);
            }
            if (response != null) {
                enhanceMessage(response);
                log.debug("Adapter {}: Received response {}", id.getId(), response);

                broker.publishInput(response);
            } else {
                handleNoMessageReceived();
            }
        }
    }

    private void handleNoMessageReceived() {
        log.error("Adapter {}: No message received!", id.getId());
        //TODO what should we do here?
        // proposal:
        //      if message has a sender, create and send a input to the sender,
        //          that there is no input available
        //      otherwise: don't send a message
    }

    private void enhanceMessage(Message response) {
        if (response.getSenderId() == null) {
            response.setSenderId(id);
        }
    }
}
