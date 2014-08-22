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
package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.dao.PeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SimpleAddressPeerChannelAddressResolverDAO implements PeerChannelAddressResolverDAO {

    private Map<String, PeerChannelAddress> addresses = new HashMap<>();
    private int requests = 0;

    @Override
    public synchronized void insert(PeerChannelAddress address) {
        addresses.put(address.getPeerId().getId()+"."+address.getChannelType().getId(), address);
    }

    @Override
    public synchronized PeerChannelAddress find(Identifier peerId, Identifier adapterId) {
        requests++;
        return addresses.get(peerId.getId()+"."+adapterId.getId());
    }

    @Override
    public synchronized void remove(Identifier peerId, Identifier adapterId) {
        addresses.remove(peerId.getId()+"."+adapterId.getId());
    }

    public synchronized int getRequests() {
        return requests;
    }
}