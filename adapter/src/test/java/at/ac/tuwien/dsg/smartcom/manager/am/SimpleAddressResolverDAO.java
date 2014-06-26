package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SimpleAddressResolverDAO implements ResolverDAO {

    private Map<String, PeerAddress> addresses = new HashMap<>();
    private int requests = 0;

    @Override
    public synchronized void insert(PeerAddress address) {
        addresses.put(address.getPeerId().getId()+"."+address.getAdapterId().getId(), address);
    }

    @Override
    public synchronized PeerAddress find(Identifier peerId, Identifier adapterId) {
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