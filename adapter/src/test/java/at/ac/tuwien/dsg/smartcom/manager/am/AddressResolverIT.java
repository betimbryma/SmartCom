package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.am.dao.MongoDBResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class AddressResolverIT {
    private AddressResolver resolver;

    private MongoDBInstance mongoDB;

    MongoDBResolverDAO dao;
    DBCollection collection;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);
        collection = mongo.getDB("test-resolver").getCollection("resolver");
        dao = new MongoDBResolverDAO(mongo, "test-resolver", "resolver");
        resolver = new AddressResolver(dao, 100);
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testGetPeerAddress() throws Exception {
        for (int i = 0; i < 1000; i++) {
            dao.insert(new PeerAddress("peer"+i, "adapter"+(i%2), Collections.EMPTY_LIST));
        }

        for (int i = 0; i < 1000; i++) {
            assertNotNull("Resolver returns null "+i+"!", resolver.getPeerAddress("peer" + i, "adapter" + (i % 2)));
        }
    }

    @Test
    public void testAddPeerAddress() throws Exception {
        assertNull("Resolver returns address that should not be available!", resolver.getPeerAddress("peer1", "adapter1"));

        PeerAddress address = new PeerAddress("peer1", "adapter1", Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        PeerAddress peerAddress = resolver.getPeerAddress("peer1", "adapter1");
        assertNotNull("Address should not be null!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);

        peerAddress = dao.find("peer1", "adapter1");
        assertNotNull("Address should be in the database!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);
    }

    @Test
    public void testRemovePeerAddress() throws Exception {
        PeerAddress address = new PeerAddress("peer1", "adapter1", Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        PeerAddress peerAddress = resolver.getPeerAddress("peer1", "adapter1");
        assertNotNull("Address should not be null!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);

        resolver.removePeerAddress("peer1", "adapter1");

        peerAddress = resolver.getPeerAddress("peer1", "adapter1");
        assertNull("Address should not be present anymore!", peerAddress);

        peerAddress = dao.find("peer1", "adapter1");
        assertNull("Address should not be in the database!", peerAddress);
    }
}