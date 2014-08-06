package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.manager.AuthenticationManager;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.AuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an implementation for the AuthenticationManager interface that can be used
 * to authenticate a peer and check if his security token is still valid.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AuthenticationManagerImpl implements AuthenticationManager {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationManagerImpl.class);

    @Inject
    private AuthenticationSessionDAO dao; //used to check if a token is valid

    @Override
    public boolean authenticate(Identifier peerId, String securityToken) {
        log.debug("Authenticating peer {} and security token {}", peerId, securityToken);
        return dao.isValidSession(peerId, securityToken);
    }
}
