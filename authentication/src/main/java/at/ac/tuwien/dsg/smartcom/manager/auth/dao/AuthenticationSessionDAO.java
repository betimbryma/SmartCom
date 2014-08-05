package at.ac.tuwien.dsg.smartcom.manager.auth.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;

import java.util.Date;

/**
 * DAO that can be used to store and assert the validity of a session of a given user.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AuthenticationSessionDAO {

    /**
     * Persist a session and save it in the database
     * @param peerId id of the peer which owns the session
     * @param token security token that will be used by the session
     * @param expires expiration date of the session
     */
    public void persistSession(Identifier peerId, String token, Date expires);

    /**
     * Checks if the given session is still valid for a given peer. It returns false if either
     * the session is not valid or expired. In both cases the session should be renewed.
     *
     * @param peerId id of the peer which owns the session
     * @param token security token that is used by the session
     * @return true if the session is valid for a user and false otherwise
     */
    public boolean isValidSession(Identifier peerId, String token);
}
