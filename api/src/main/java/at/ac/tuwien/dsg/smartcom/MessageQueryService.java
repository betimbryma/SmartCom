package at.ac.tuwien.dsg.smartcom;


import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;

/**
 * This service can be used to query the logged messages. To query the service, a QueryCriteria
 * object has to be used that specifies the query.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageQueryService {

    /**
     * Creates a query object that can be used to specify the criteria for the query.
     *
     * @return query criteria object
     */
    public QueryCriteria createQuery();
}
