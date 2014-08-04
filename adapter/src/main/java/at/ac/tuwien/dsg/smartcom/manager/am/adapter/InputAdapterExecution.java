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
 * Execution environment for an input pull adapter instance. It handles automatically pull requests and the publishing
 * of messages to the system.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class InputAdapterExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(InputAdapterExecution.class);

    private final InputPullAdapter adapter; //adapter that is handled by this execution environment
    private final Identifier id; //id of the adapter and the execution environment
    private final MessageBroker broker; //broker used to publish received message

    /**
     * Create a new Input Adapter Execution for a given adapter and id. The broker is used to publish the received messages
     * @param adapter that will be handled by the input adapter execution
     * @param id of the adapter
     * @param broker that is used to publish messages
     */
    public InputAdapterExecution(InputPullAdapter adapter, Identifier id, MessageBroker broker) {
        this.adapter = adapter;
        this.id = id;
        this.broker = broker;
    }

    /**
     * returns the adapter that is handled by this input adapter execution
     * @return adapter of this adapter execution
     */
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

    /**
     * no message has been received
     */
    private void handleNoMessageReceived() {
        log.error("Adapter {}: No message received!", id.getId());
        //TODO what should we do here?
        // proposal:
        //      if message has a sender, create and send a input to the sender,
        //          that there is no input available
        //      otherwise: don't send a message
    }

    /**
     * Enhance the properties of the message as much as possible.
     * E.g., add the id of the adapter as a sender id if it is null
     * @param response message that should be enhanced
     */
    private void enhanceMessage(Message response) {
        if (response.getSenderId() == null) {
            response.setSenderId(id);
        }
    }
}
