package at.ac.tuwien.dsg.smartcom.dao;

import at.ac.tuwien.dsg.smartcom.QueryCriteriaImpl;
import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import at.ac.tuwien.dsg.smartcom.utils.MessageQueryTestClass;
import org.junit.Before;

public class MongoDBMessageQueryDAOTest extends MessageQueryTestClass {


    private MongoDBMessageQueryDAO dao;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = new MongoDBMessageQueryDAO(mongoDB.getClient(), "test-log", "log");
    }

    @Override
    public QueryCriteria createCriteria() {
        return new QueryCriteriaImpl(dao);
    }
}