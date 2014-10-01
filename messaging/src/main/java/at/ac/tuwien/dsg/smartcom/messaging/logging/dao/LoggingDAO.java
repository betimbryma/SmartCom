package at.ac.tuwien.dsg.smartcom.messaging.logging.dao;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface LoggingDAO {

    /**
     * Persists a message in an underlying database system.
     *
     * @param message
     */
    public void persist(Message message);
}
