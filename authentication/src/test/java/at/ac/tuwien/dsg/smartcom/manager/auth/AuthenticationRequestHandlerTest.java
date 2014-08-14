package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.AuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.junit.Before;

import java.util.Date;

public class AuthenticationRequestHandlerTest extends AuthenticationRequestHandlerTestClass {

    @Override
    @Before
    public void setUp() throws Exception {
        pico = new PicoHelper();
        pico.addComponent(new SimpleAuthenticationSessionDAO());
        pico.addComponent(new SimplePeerAuthenticationCallback());
        pico.addComponent(new SimpleMessageBroker());
        pico.addComponent(AuthenticationRequestHandler.class);

        super.setUp();
    }

    private class SimpleAuthenticationSessionDAO implements AuthenticationSessionDAO {

        @Override
        public void persistSession(Identifier peerId, String token, Date expires) {

        }

        @Override
        public boolean isValidSession(Identifier peerId, String token) {
            return true;
        }
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