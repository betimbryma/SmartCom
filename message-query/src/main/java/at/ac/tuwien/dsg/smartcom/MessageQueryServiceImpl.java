package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.dao.MessageQueryDAO;
import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import org.picocontainer.annotations.Inject;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MessageQueryServiceImpl implements MessageQueryService {

    @Inject
    private MessageQueryDAO dao;

//    @Override
//    public Collection<Message> query(QueryCriteria criteria) throws IllegalQueryException {
//        return null;
//    }

    @Override
    public QueryCriteria createQuery() {
        return new QueryCriteriaImpl(dao);
    }
}
