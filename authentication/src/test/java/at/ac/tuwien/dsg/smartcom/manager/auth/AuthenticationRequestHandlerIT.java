package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.MongoDBAuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.junit.Before;

public class AuthenticationRequestHandlerIT extends AuthenticationRequestHandlerTestClass {

    protected MongoDBInstance mongoDB;
    private MongoDBAuthenticationSessionDAO dao;

    @Override
    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        pico = new PicoHelper();
        pico.addComponent(new MongoDBAuthenticationSessionDAO(mongoDB.getClient(), "test-session", "session"));
        pico.addComponent(new SimplePeerAuthenticationCallback());
        pico.addComponent(new SimpleMessageBroker());
        pico.addComponent(AuthenticationRequestHandler.class);

        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        mongoDB.tearDown();
    }

    private class SimplePeerAuthenticationCallback implements PeerAuthenticationCallback {

        @Override
        public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
            if ("true".equals(password)) {
                return true;
            }
            if ("false".equals(password)) {
                return false;
            }
            throw new PeerAuthenticationException();
        }
    }
}