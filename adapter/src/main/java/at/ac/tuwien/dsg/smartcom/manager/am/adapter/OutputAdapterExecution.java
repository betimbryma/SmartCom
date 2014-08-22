/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execution environment for an output adapter instance. It handles automatically tasks and tells the adapter to send
 * messages to peers.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class OutputAdapterExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(OutputAdapterExecution.class);

    private final OutputAdapter adapter; //that is handled by this adapter execution
    private final AddressResolver address; //used to resolve addresses of peers
    private final Identifier id; //of the adapter
    private final MessageBroker broker; //used to receive tasks

    /**
     * Creates a new output adapter execution for an adapter and its id. The address resolver and the broker are used
     * to support the execution of the adapter.
     *
     * @param adapter that is executed by this class
     * @param address used to resolve peer addresses for sending messages
     * @param id of the adapter
     * @param broker used to receive tasks
     */
    public OutputAdapterExecution(OutputAdapter adapter, AddressResolver address, Identifier id, MessageBroker broker) {
        this.adapter = adapter;
        this.address = address;
        this.id = id;
        this.broker = broker;
    }

    public OutputAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            log.debug("Adapter {}: Waiting for new task ...", id);
            Message message = broker.receiveOutput(id);
            if (message == null) {
                log.debug("Adapter {}: Received interrupted!", id);
                break;
            }
            log.debug("Adapter {}: Received task {}", id, message);
            PeerChannelAddress peerChannelAddress = address.getPeerAddress(message.getReceiverId(), id);

            log.debug("Adapter {}: Sending message {} to peer {}", id, message, peerChannelAddress);
            try {
                adapter.push(message, peerChannelAddress);

                //TODO should we send an acknowledgement here?
                broker.publishControl(PredefinedMessageHelper.createAcknowledgeMessage(message));
            } catch (AdapterException e) {
                broker.publishControl(PredefinedMessageHelper.createCommunicationErrorMessage(message, e.getMessage()));
            }
        }
    }
}
