package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.am.dao.MongoDBResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.Collections;

import static org.junit.Assert.*;

public class AddressResolverIT {
    private AddressResolver resolver;

    private MongoDBInstance mongoDB;

    private MongoDBResolverDAO dao;
    private DBCollection collection;

    private MutablePicoContainer pico;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);
        collection = mongo.getDB("test-resolver").getCollection("resolver");

        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        pico.as(Characteristics.CACHE).addComponent(ResolverDAO.class, new MongoDBResolverDAO(mongo, "test-resolver", "resolver"));
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class, AddressResolver.class);

        pico.start();

        resolver = pico.getComponent(AddressResolver.class);
        dao = pico.getComponent(MongoDBResolverDAO.class);
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        pico.stop();
    }

    @Test(timeout = 5000l)
    public void testGetPeerAddress() throws Exception {
        for (int i = 0; i < 1000; i++) {
            dao.insert(new PeerAddress(Identifier.peer("peer"+i), Identifier.adapter("adapter"+(i%2)), Collections.EMPTY_LIST));
        }

        for (int i = 0; i < 1000; i++) {
            assertNotNull("Resolver returns null "+i+"!", resolver.getPeerAddress(Identifier.peer("peer" + i), Identifier.adapter("adapter" + (i % 2))));
        }
    }

    @Test(timeout = 5000l)
    public void testAddPeerAddress() throws Exception {
        Identifier peer1 = Identifier.peer("peer1");
        Identifier adapter1 = Identifier.adapter("adapter1");

        assertNull("Resolver returns address that should not be available!", resolver.getPeerAddress(peer1, adapter1));

        PeerAddress address = new PeerAddress(peer1, adapter1, Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        PeerAddress peerAddress = resolver.getPeerAddress(peer1, adapter1);
        assertNotNull("Address should not be null!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);

        peerAddress = dao.find(peer1, adapter1);
        assertNotNull("Address should be in the database!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);
    }

    @Test(timeout = 5000l)
    public void testRemovePeerAddress() throws Exception {
        Identifier peer1 = Identifier.peer("peer1");
        Identifier adapter1 = Identifier.adapter("adapter1");

        PeerAddress address = new PeerAddress(peer1, adapter1, Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        PeerAddress peerAddress = resolver.getPeerAddress(peer1, adapter1);
        assertNotNull("Address should not be null!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);

        resolver.removePeerAddress(peer1, adapter1);

        peerAddress = resolver.getPeerAddress(peer1, adapter1);
        assertNull("Address should not be present anymore!", peerAddress);

        peerAddress = dao.find(peer1, adapter1);
        assertNull("Address should not be in the database!", peerAddress);
    }
}