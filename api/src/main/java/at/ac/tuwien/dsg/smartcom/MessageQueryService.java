package at.ac.tuwien.dsg.smartcom;


import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;

import java.util.Collection;

/**
 * This service can be used to query the logged messages. To query the service, a QueryCriteria
 * object has to be used that specifies the query.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageQueryService {

    /**
     * Queries the message log for messages that match a given query statement (similar to the JPA Criteria API).
     *
     * @param criteria Statement that will be used to query the message log using the QueryCriteria class (similar to the JPA Criteria API).
     * @return Returns a collection of messages that match the given query statement.
     * @throws IllegalQueryException if the query is not valid.
     */
    public Collection<Message> query(QueryCriteria criteria) throws IllegalQueryException;
}
