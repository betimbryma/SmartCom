package at.ac.tuwien.dsg.smartcom.manager.am.dao;

import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ResolverDAO {
    void insert(PeerAddress address);

    PeerAddress find(String peerId, String adapterId);

    void remove(String peerId, String adapterId);
}
