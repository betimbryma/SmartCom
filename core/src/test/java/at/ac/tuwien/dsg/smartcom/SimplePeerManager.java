package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

import java.util.*;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SimplePeerManager implements PMCallback {

    Map<Identifier, List<PeerAddress>> identifierListMap = new HashMap<>();

    @Override
    public synchronized Collection<PeerAddress> getPeerAddress(Identifier id) throws NoSuchPeerException {
        return identifierListMap.get(id);
    }

    @Override
    public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
        return true;
    }

    public synchronized void addPeerAddress(Identifier id, PeerAddress address) {
        List<PeerAddress> peerAddresses = identifierListMap.get(id);
        if (peerAddresses == null) {
            peerAddresses = new ArrayList<PeerAddress>();
            identifierListMap.put(id, peerAddresses);
        }
        peerAddresses.add(address);
    }
}
