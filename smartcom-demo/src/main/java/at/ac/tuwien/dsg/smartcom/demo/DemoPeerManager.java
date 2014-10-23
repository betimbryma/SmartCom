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
package at.ac.tuwien.dsg.smartcom.demo;

import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class DemoPeerManager implements PeerAuthenticationCallback, PeerInfoCallback, CollectiveInfoCallback{

    //peer registrations
    Map<Identifier, PeerInfo> peerInfoMap = Collections.synchronizedMap(new HashMap<Identifier, PeerInfo>());
    Map<Identifier, String> peerPasswordMap = Collections.synchronizedMap(new HashMap<Identifier, String>());

    //collective registrations
    Map<Identifier, CollectiveInfo> collectiveInfoMap = Collections.synchronizedMap(new HashMap<Identifier, CollectiveInfo>());

    @Override
    public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
        String pwd = peerPasswordMap.get(peerId);
        return !pwd.isEmpty() && pwd.equals(password);
    }

    @Override
    public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
        return peerInfoMap.get(id);
    }

    @Override
    public CollectiveInfo getCollectiveInfo(Identifier collective) throws NoSuchCollectiveException {
        return collectiveInfoMap.get(collective);
    }

    public void addPeer(Identifier id, PeerInfo info, String password) {
        peerInfoMap.put(id, info);
        peerPasswordMap.put(id, password);
    }

    public void registerCollective(CollectiveInfo info) {
        collectiveInfoMap.put(info.getId(), info);
    }

    public void addPeerToCollective(Identifier peer, Identifier collective) {
        CollectiveInfo collectiveInfo = collectiveInfoMap.get(collective);
        synchronized (collectiveInfo) {
            collectiveInfo.getPeers().add(peer);
        }
    }

    public void removePeerFromCollective(Identifier peer, Identifier collective) {
        CollectiveInfo collectiveInfo = collectiveInfoMap.get(collective);
        synchronized (collectiveInfo) {
            collectiveInfo.getPeers().remove(peer);
        }
    }

}
