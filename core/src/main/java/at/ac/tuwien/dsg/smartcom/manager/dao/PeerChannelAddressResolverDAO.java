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
package at.ac.tuwien.dsg.smartcom.manager.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

/**
 * DAO to insert, find and remove peer addresses identified by the id of a peer
 * and of an adapter.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface PeerChannelAddressResolverDAO {

    /**
     * Insert a new peer address.
     * @param address the peer address
     */
    void insert(PeerChannelAddress address);

    /**
     * Find a peer address identified by the peer id and the adapter id.
     *
     * It will return either the corresponding peer address or null if there is no such
     * address available.
     *
     * @param peerId id of the peer
     * @param adapterId id of the adapter
     * @return the peer address or null if there is no such address
     */
    PeerChannelAddress find(Identifier peerId, Identifier adapterId);

    /**
     * Remove a peer address identified by the peer id and the adapter id.
     *
     * @param peerId id of the peer
     * @param adapterId id of the adapter
     */
    void remove(Identifier peerId, Identifier adapterId);
}
