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
package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AdapterManager {

    /**
     * Add a push adapter to the adapter manager
     * @param adapter the push adapter
     * @return the id of the push adapter
     */
    Identifier addPushAdapter(InputPushAdapter adapter);

    /**
     * Add a pull adapter to the adapter manager.
     * @param adapter the pull adapter
     * @param period defines the period between two pull attempts
     * @param deleteIfSuccessful delete the adapter after a successful execution
     * @return the id of the pull adapter
     */
    Identifier addPullAdapter(InputPullAdapter adapter, long period, boolean deleteIfSuccessful);

    /**
     * Removes a input adapter from the execution. Note that after returning,
     * the corresponding input adapter won't return any input anymore.
     * @param adapterId the id of the adapter
     * @return the adapter that has been removed or null if there is no such adapter
     */
    InputAdapter removeInputAdapter(Identifier adapterId);

    /**
     * Register a new type of output adapters in the adapter manager.
     * @param adapter new type of output adapters
     * @return id for the output adapter type
     * @throws CommunicationException if the adapter is not implemented correctly (e.g., annotation missing, constructor not valid)
     */
    Identifier registerOutputAdapter(Class<? extends OutputAdapter> adapter) throws CommunicationException;

    /**
     * Removes a output adapter type and all instances from the execution. After the method
     * returned no adapter of this type will handle messages anymore.
     * @param adapterId id of the output adapter type
     */
    void removeOutputAdapter(Identifier adapterId);

    /**
     * Create a new endpoint (adapter) for a specific peer. If there is already an
     * endpoint, a corresponding routing rule will be returned. If there is no such
     * adapter available, a new one will be created (based on available contact information
     * of the peer) and a corresponding routing rule will be returned.
     * @param peerInfo information provided on the peer
     * @return the identifier of the new endpoint for the peer
     */
    public List<Identifier> createEndpointForPeer(PeerInfo peerInfo);
}
