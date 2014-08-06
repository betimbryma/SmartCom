package at.ac.tuwien.dsg.smartcom.services;

import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import at.ac.tuwien.dsg.smartcom.services.dao.MessageQueryDAO;
import org.picocontainer.annotations.Inject;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MessageQueryServiceImpl implements MessageQueryService {

    @Inject
    private MessageQueryDAO dao;

    @Override
    public QueryCriteria createQuery() {
        return new QueryCriteriaImpl(dao);
    }
}
