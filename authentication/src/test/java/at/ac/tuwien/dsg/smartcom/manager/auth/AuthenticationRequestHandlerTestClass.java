package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class AuthenticationRequestHandlerTestClass {
    PicoHelper pico;
    AuthenticationRequestHandler handler;
    MessageBroker broker;

    @Before
    public void setUp() throws Exception {
        handler = pico.getComponent(AuthenticationRequestHandler.class);
        broker = pico.getComponent(MessageBroker.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
    }

    @Test
    public void testOnMessage() throws Exception {
        //the content should be either "true" or "false".
        //Because the PMCallback will return either true or false depending on the content of the message
        //if the content is empty or anything else, it will throw an error
        broker.publishAuthRequest(PredefinedMessageHelper.createAuthenticationRequestMessage(Identifier.peer("test1"), "true"));

        Message message = broker.receiveControl();
        assertNotNull(message);
        assertNotNull(message.getContent());
        assertNotNull(message.getSenderId());
        assertEquals("AUTH", message.getType());
        assertEquals("REPLY", message.getSubtype());

        broker.publishAuthRequest(PredefinedMessageHelper.createAuthenticationRequestMessage(Identifier.peer("test1"), "false"));

        message = broker.receiveControl();
        assertNotNull(message);
        assertNotNull(message.getSenderId());
        assertEquals("AUTH", message.getType());
        assertEquals("FAILED", message.getSubtype());

        broker.publishAuthRequest(PredefinedMessageHelper.createAuthenticationRequestMessage(Identifier.peer("test1"), "error"));

        message = broker.receiveControl();
        assertNotNull(message);
        assertNotNull(message.getSenderId());
        assertEquals("AUTH", message.getType());
        assertEquals("ERROR", message.getSubtype());
    }
}
