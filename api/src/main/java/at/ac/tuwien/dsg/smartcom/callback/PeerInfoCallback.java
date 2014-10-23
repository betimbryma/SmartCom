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
package at.ac.tuwien.dsg.smartcom.callback;

import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

/**
 * This callback is used to resolve peer information upon a peer. This information does not change very
 * often but is queried quite frequently, therefore retrieved data should be cached as long as the callback does
 * not provide the required performance throughput.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface PeerInfoCallback {
    /**
     * Resolves the information about a given peer (e.g., provides the address and the method/adapter that should be used).
     *
     * @param id id of the requested peer
     * @return Returns information about a peer
     * @throws NoSuchPeerException if there exists no such peer.
     */
    public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException;
}
