package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class OutputAdapterExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(OutputAdapterExecution.class);

    private final OutputAdapter adapter;
    private final AddressResolver address;
    private final Identifier id;
    private final boolean stateful;
    private final MessageBroker broker;

    public OutputAdapterExecution(OutputAdapter adapter, AddressResolver address, Identifier id, boolean stateful, MessageBroker broker) {
        this.adapter = adapter;
        this.address = address;
        this.id = id;
        this.stateful = stateful;
        this.broker = broker;
    }

    public OutputAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            log.debug("Adapter {}: Waiting for new task ...", id);
            Message message = broker.receiveTasks(id);
            if (message == null) {
                log.debug("Adapter {}: Received interrupted!", id);
                break;
            }
            log.debug("Adapter {}: Received task {}", id, message);
            PeerAddress peerAddress = address.getPeerAddress(message.getReceiverId(), id);

            log.debug("Adapter {}: Sending message {} to peer {}", id, message, peerAddress);
            adapter.push(message, peerAddress);
        }
    }
}
