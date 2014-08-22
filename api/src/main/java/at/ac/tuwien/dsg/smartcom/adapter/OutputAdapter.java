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
package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

/**
 * The Output Adapter API will be used to implement an adapter that can
 * send (push) messages to a peer. Therefore the push method has to be
 * implemented. Output Adapters will receive a message, transform this
 * message and push it to the peer over an external communication channel
 * (e.g., send the message to a web platform or a mobile application).
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface OutputAdapter {

    /**
     * Push a message to the peer. This method defines the handling of the
     * actual communication between the platform and the peer.
     *
     * @param message Message that should be sent to the peer.
     * @param address The adapter specific address of the peer. If the adapter is stateful, this address will be the same on every call.
     *
     * @throws AdapterException an exception occurred during the sending of a message
     */
    public void push(Message message, PeerChannelAddress address) throws AdapterException;
}
