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