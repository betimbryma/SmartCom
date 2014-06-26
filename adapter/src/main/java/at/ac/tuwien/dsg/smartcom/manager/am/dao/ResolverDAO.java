package at.ac.tuwien.dsg.smartcom.manager.am.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ResolverDAO {
    void insert(PeerAddress address);

    PeerAddress find(Identifier peerId, Identifier adapterId);

    void remove(Identifier peerId, Identifier adapterId);
}
