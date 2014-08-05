package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AuthenticationManager {

    /**
     * Authenticate a peer based on its identifier and a security token.
     * The method returns true if the authentication was successful and false otherwise
     *
     * @param peerId id of the peer
     * @param securityToken security token of the peer
     * @return true if the authentication was successful and false otherwise
     */
    public boolean authenticate(Identifier peerId, String securityToken);
}
