package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.dao.MongoDBPeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.dao.PeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
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

    private MongoDBPeerChannelAddressResolverDAO dao;

    private MutablePicoContainer pico;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);

        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        pico.as(Characteristics.CACHE).addComponent(PeerChannelAddressResolverDAO.class, new MongoDBPeerChannelAddressResolverDAO(mongo, "test-resolver", "resolver"));
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class, AddressResolver.class);

        pico.start();

        resolver = pico.getComponent(AddressResolver.class);
        dao = pico.getComponent(MongoDBPeerChannelAddressResolverDAO.class);
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        pico.stop();
        pico.dispose();
    }

    @Test(timeout = 5000l)
    public void testGetPeerAddress() throws Exception {
        for (int i = 0; i < 1000; i++) {
            dao.insert(new PeerChannelAddress(Identifier.peer("peer"+i), Identifier.adapter("adapter"+(i%2)), Collections.EMPTY_LIST));
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

        PeerChannelAddress address = new PeerChannelAddress(peer1, adapter1, Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        PeerChannelAddress peerChannelAddress = resolver.getPeerAddress(peer1, adapter1);
        assertNotNull("Address should not be null!", peerChannelAddress);
        assertEquals("Address does not match the inserted address", address, peerChannelAddress);

        peerChannelAddress = dao.find(peer1, adapter1);
        assertNotNull("Address should be in the database!", peerChannelAddress);
        assertEquals("Address does not match the inserted address", address, peerChannelAddress);
    }

    @Test(timeout = 5000l)
    public void testRemovePeerAddress() throws Exception {
        Identifier peer1 = Identifier.peer("peer1");
        Identifier adapter1 = Identifier.adapter("adapter1");

        PeerChannelAddress address = new PeerChannelAddress(peer1, adapter1, Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        PeerChannelAddress peerChannelAddress = resolver.getPeerAddress(peer1, adapter1);
        assertNotNull("Address should not be null!", peerChannelAddress);
        assertEquals("Address does not match the inserted address", address, peerChannelAddress);

        resolver.removePeerAddress(peer1, adapter1);

        peerChannelAddress = resolver.getPeerAddress(peer1, adapter1);
        assertNull("Address should not be present anymore!", peerChannelAddress);

        peerChannelAddress = dao.find(peer1, adapter1);
        assertNull("Address should not be in the database!", peerChannelAddress);
    }
}