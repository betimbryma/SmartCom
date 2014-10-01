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
