package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
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
    private final boolean deleteIfSuccessful;

    private final StatisticBean statistic;

    /**
     * Create a new Input Adapter Execution for a given adapter and id. The broker is used to publish the received messages
     * @param adapter that will be handled by the input adapter execution
     * @param id of the adapter
     * @param broker that is used to publish messages
     * @param deleteIfSuccessful delete the adapter if it has been executed successfully
     * @param statistic
     */
    public InputAdapterExecution(InputPullAdapter adapter, Identifier id, MessageBroker broker, boolean deleteIfSuccessful, StatisticBean statistic) {
        this.adapter = adapter;
        this.id = id;
        this.broker = broker;
        this.deleteIfSuccessful = deleteIfSuccessful;
        this.statistic = statistic;
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
            log.trace("Adapter {}: Waiting for requests...", id.getId());
            Message message = broker.receiveRequests(id);
            statistic.requestReceived();
            log.trace("Adapter {}: Received request {}", id.getId(), message);
            if (message == null) {
                log.debug("Adapter {}: Received interrupted!", id.getId());
                break;
            }
            Message response = null;
            try {
                response = adapter.pull();
            } catch (AdapterException e) {
                log.error("Adapter {}: Error while checking response", id.getId(), e);
                //TODO should we raise an error message here too? similar to the one in OutputAdapterExecution
            }
            if (response != null) {
                enhanceMessage(response);
                log.debug("Adapter {}: Received response {}", id.getId(), response);

                broker.publishInput(response);

                if (deleteIfSuccessful) {
                    break;
                }
            } else {
                handleNoMessageReceived();
            }
        }
    }

    /**
     * no message has been received
     */
    private void handleNoMessageReceived() {
        log.trace("Adapter {}: No message received!", id.getId());
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
