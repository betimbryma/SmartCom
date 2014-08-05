package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AuthenticationManager {

    public boolean authenticate(Identifier peerId, String securityToken);
}
