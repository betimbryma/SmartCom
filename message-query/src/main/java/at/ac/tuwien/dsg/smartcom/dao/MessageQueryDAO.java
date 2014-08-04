package at.ac.tuwien.dsg.smartcom.dao;

import at.ac.tuwien.dsg.smartcom.QueryCriteriaImpl;
import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;
import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.Collection;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageQueryDAO {

    /**
     * Queries the message log for messages that match a given query statement (similar to the JPA Criteria API).
     *
     * @param criteria Statement that will be used to query the message log using the QueryCriteria class (similar to the JPA Criteria API).
     * @return Returns a collection of messages that match the given query statement.
     * @throws IllegalQueryException if the query is not valid.
     */
    public Collection<Message> query(QueryCriteriaImpl criteria) throws IllegalQueryException;
}
