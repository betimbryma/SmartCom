package at.ac.tuwien.dsg.smartcom.manager.auth.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class MongoDBAuthenticationSessionDAOTest {

    protected MongoDBInstance mongoDB;
    private DBCollection collection;
    private MongoDBAuthenticationSessionDAO dao;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = mongoDB.getClient();
        collection = mongo.getDB("test-session").getCollection("session");
        dao = new MongoDBAuthenticationSessionDAO(mongo, "test-session", "session");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testInsertSession() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.AUGUST, 5, 13, 24, 30);

        dao.persistSession(Identifier.peer("test1"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test2"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test4"), "token1", calendar.getTime());

        assertEquals(4, collection.getCount());

        calendar.add(Calendar.HOUR, 4);
        dao.persistSession(Identifier.peer("test3"), "token2", calendar.getTime());

        assertEquals(4, collection.getCount());
        assertEquals(calendar.getTime(), collection.find(new BasicDBObject().append("_id", "test3")).next().get("expires"));
    }

    @Test
    public void testIsValidSession() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);

        dao.persistSession(Identifier.peer("test1"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test2"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test4"), "token1", calendar.getTime());

        assertTrue("Session seems not to be valid but should be!", dao.isValidSession(Identifier.peer("test2"), "token1"));
        assertFalse("Session seems to be valid but shouldn't!", dao.isValidSession(Identifier.peer("test1"), "token3"));

        dao.persistSession(Identifier.peer("test4"), "token3", calendar.getTime());
        assertTrue("Session seems not to be valid but should be!", dao.isValidSession(Identifier.peer("test4"), "token3"));

        calendar.add(Calendar.HOUR, -1);
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        assertFalse("Session seems to be valid but shouldn't!", dao.isValidSession(Identifier.peer("test3"), "token1"));
    }
}