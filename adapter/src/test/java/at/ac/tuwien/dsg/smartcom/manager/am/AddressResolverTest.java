package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.Collections;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class AddressResolverTest {
    private SimpleAddressResolverDAO dao;
    private AddressResolver resolver;

    private MutablePicoContainer pico;

    @Before
    public void setUp() throws Exception {
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        pico.as(Characteristics.CACHE).addComponent(ResolverDAO.class, SimpleAddressResolverDAO.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class, AddressResolver.class);

        pico.start();

        dao = pico.getComponent(SimpleAddressResolverDAO.class);
        resolver = pico.getComponent(AddressResolver.class);
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
        pico.dispose();
    }

    @Test(timeout = 5000l)
    public void testGetPeerAddress() throws Exception {
        for (int i = 0; i < 1000; i++) {
            dao.insert(new PeerAddress(Identifier.peer("peer"+i), Identifier.adapter("adapter"+(i%2)), Collections.EMPTY_LIST));
        }

        for (int i = 0; i < 100; i++) {
            assertNotNull("Resolver returns null!", resolver.getPeerAddress(Identifier.peer("peer" + i), Identifier.adapter("adapter" + (i % 2))));
        }
        assertEquals("Cache has not requested values correctly!", 100, dao.getRequests());


        for (int i = 0; i < 100; i++) {
            assertNotNull("Resolver returns null!", resolver.getPeerAddress(Identifier.peer("peer"+i), Identifier.adapter("adapter"+(i%2))));
        }
        int size = dao.getRequests();
        assertThat("Cache should not request items (they are in the cache)!", size, lessThan(200));

        for (int i = 0; i < 1000; i++) {
            assertNotNull("Resolver returns null!", resolver.getPeerAddress(Identifier.peer("peer" + i), Identifier.adapter("adapter" + (i % 2))));
        }
        assertThat("Cache has not requested values correctly!", dao.getRequests(), lessThan(1000 + size));
    }

    @Test(timeout = 500l)
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