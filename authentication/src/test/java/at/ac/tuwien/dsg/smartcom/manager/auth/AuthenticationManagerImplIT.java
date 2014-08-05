package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.manager.AuthenticationManager;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.MongoDBAuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.manager.auth.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthenticationManagerImplIT {

    private PicoHelper pico;
    protected MongoDBInstance mongoDB;

    private MongoDBAuthenticationSessionDAO dao;
    private AuthenticationManager manager;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = mongoDB.getClient();
        pico = new PicoHelper();
        pico.addComponent(new MongoDBAuthenticationSessionDAO(mongo, "test-session", "session"));
        pico.addComponent(AuthenticationManagerImpl.class);

        dao = pico.getComponent(MongoDBAuthenticationSessionDAO.class);
        manager = pico.getComponent(AuthenticationManager.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        pico.stop();
    }

    @Test
    public void testAuthenticate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);

        dao.persistSession(Identifier.peer("test1"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test2"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test4"), "token1", calendar.getTime());

        assertTrue("Session seems not to be valid but should be!", manager.authenticate(Identifier.peer("test2"), "token1"));
        assertFalse("Session seems to be valid but shouldn't!", manager.authenticate(Identifier.peer("test1"), "token3"));

        dao.persistSession(Identifier.peer("test4"), "token3", calendar.getTime());
        assertTrue("Session seems not to be valid but should be!", manager.authenticate(Identifier.peer("test4"), "token3"));

        calendar.roll(Calendar.HOUR, false);
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        assertFalse("Session seems to be valid but shouldn't!", manager.authenticate(Identifier.peer("test3"), "token1"));
    }
}