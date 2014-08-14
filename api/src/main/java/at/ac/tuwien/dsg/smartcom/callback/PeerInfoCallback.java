package at.ac.tuwien.dsg.smartcom.callback;

import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

/**
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface PeerInfoCallback {
    /**
     * Resolves the information of a given peer (e.g., provides the address and the method/adapter that should be used).
     *
     * @param id id of the requested peer
     * @return Returns information upon a peer
     * @throws NoSuchPeerException if there exists no such peer.
     */
    public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException;
}
