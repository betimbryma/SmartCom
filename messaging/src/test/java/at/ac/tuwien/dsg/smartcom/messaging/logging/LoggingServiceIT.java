package at.ac.tuwien.dsg.smartcom.messaging.logging;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.MongoDBLoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.IdentifierType;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LoggingServiceIT {

    private PicoHelper pico;
    private MongoDBInstance mongoDB;
    private DBCollection collection;
    private LoggingService logging;
    private SimpleMessageBroker broker;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient client = mongoDB.getClient();
        collection = client.getDB("test-logging").getCollection("logging");

        pico = new PicoHelper();
        pico.addComponent(SimpleMessageBroker.class);
        pico.addComponent(LoggingDAO.class, new MongoDBLoggingDAO(client, "test-logging", "logging"));
        pico.addComponent(LoggingService.class);
        pico.addComponent(StatisticBean.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        logging = pico.getComponent(LoggingService.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
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

        long counter_before = collection.count();

        broker.publishLog(message);

        synchronized (this) {
            wait(1000);
        }

        long counter_after = collection.count();

        assertThat(counter_before, Matchers.lessThan(counter_after));

        for (DBObject dbObject : collection.find()) {
            if ("testId".equals(dbObject.get("_id"))) {
                assertEquals("testContent", dbObject.get("content"));
                assertEquals("testType", dbObject.get("type"));
                assertEquals("testSubType", dbObject.get("subtype"));
                assertEquals(IdentifierType.PEER, IdentifierType.valueOf((String) ((DBObject)dbObject.get("sender")).get("type")));
                assertEquals("sender", ((DBObject)dbObject.get("sender")).get("id"));
                assertEquals(IdentifierType.PEER, IdentifierType.valueOf((String) ((DBObject)dbObject.get("receiver")).get("type")));
                assertEquals("receiver", ((DBObject)dbObject.get("receiver")).get("id"));
                assertEquals("conversationId", dbObject.get("conversationId"));
                assertEquals(3l, dbObject.get("ttl"));
                assertEquals("testLanguage", dbObject.get("language"));
                assertEquals("securityToken", dbObject.get("securityToken"));
            }
        }

    }
}