package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryService;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryServiceImpl;
import at.ac.tuwien.dsg.smartcom.services.dao.MongoDBMessageQueryDAO;
import at.ac.tuwien.dsg.smartcom.utils.MessageQueryTestClass;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.junit.Before;

public class MessageQueryServiceImplIT extends MessageQueryTestClass {

    private PicoHelper pico;
    private MessageQueryService service;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        pico = new PicoHelper();
        pico.addComponent(logger);
        pico.addComponent(new MongoDBMessageQueryDAO(mongoDB.getClient(), "test-log", "log"));
        pico.addComponent(MessageQueryServiceImpl.class);

        service = pico.getComponent(MessageQueryService.class);

        pico.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        pico.stop();
    }

    @Override
    public QueryCriteria createCriteria() {
        return service.createQuery();
    }
}