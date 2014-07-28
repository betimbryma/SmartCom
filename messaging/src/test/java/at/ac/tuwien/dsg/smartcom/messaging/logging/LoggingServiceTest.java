package at.ac.tuwien.dsg.smartcom.messaging.logging;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LoggingServiceTest {

    private PicoHelper pico;
    private SimpleMessageBroker broker;
    private SimpleLoggingDAO dao;

    @Before
    public void setUp() throws Exception {
        pico = new PicoHelper();
        pico.addComponent(SimpleMessageBroker.class);
        pico.addComponent(new SimpleLoggingDAO());
        pico.addComponent(LoggingService.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        dao = pico.getComponent(SimpleLoggingDAO.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
    }

    @Test
    public void testLogging() throws Exception {
        Message message = new Message.MessageBuilder()
                .setId(Identifier.message("testId"))
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setSenderId(Identifier.peer("sender"))
                .setReceiverId(Identifier.peer("receiver"))
                .setConversationId("conversationId")
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        long counter_before = dao.size();

        broker.publishLog(message);

        synchronized (this) {
            wait(1000);
        }

        long counter_after = dao.size();

        assertThat(counter_before, Matchers.lessThan(counter_after));

        for (Message msg : dao.getMessages()) {
            if ("testId".equals(msg.getId().getId())) {
                assertEquals(message, msg);
            }
        }

    }

    private class SimpleLoggingDAO implements LoggingDAO {

        List<Message> messages = new ArrayList<>();

        @Override
        public void persist(Message message) {
            messages.add(message);
        }

        public long size() {
            return messages.size();
        }

        public List<Message> getMessages() {
            return Collections.unmodifiableList(messages);
        }
    }
}