package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterExecutionEngine;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;

/**
 * Execution environment for an output adapter instance. It handles automatically tasks and tells the adapter to send
 * messages to peers.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ReplicatingOutputAdapterExecution extends OutputAdapterExecution {
    private final AdapterExecutionEngine execEngine;

    /**
     * Creates a new replicating output adapter execution for an adapter and its id. The address resolver and the broker are used
     * to support the execution of the adapter.
     * @param adapter that is executed by this class
     * @param address used to resolve peer addresses for sending messages
     * @param id of the adapter
     * @param broker used to receive tasks
     * @param execEngine executionEngine that executes this object
     */
    public ReplicatingOutputAdapterExecution(OutputAdapter adapter,
                                             AddressResolver address,
                                             Identifier id,
                                             MessageBroker broker,
                                             AdapterExecutionEngine execEngine,
                                             StatisticBean statistic) {
        super(adapter, address, id, broker, statistic);
        this.execEngine = execEngine;
    }

    @Override
    public void onMessage(Message message) {

        statistic.outputReceived();
        increaseCounter();

        message.setSenderId(Identifier.adapter(id.getId(), "#1"));

        super.onMessage(message);
    }

    private void increaseCounter() {
//        execEngine.increaseReplicaCounter(id);
    }
}
