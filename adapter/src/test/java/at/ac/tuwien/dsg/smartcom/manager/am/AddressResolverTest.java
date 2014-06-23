package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class AddressResolverTest {
    private SimpleAddressResolverDAO dao;
    private AddressResolver resolver;

    @Before
    public void setUp() throws Exception {
        dao = new SimpleAddressResolverDAO();
        resolver = new AddressResolver(dao, 100);
    }

    @Test
    public void testGetPeerAddress() throws Exception {
        for (int i = 0; i < 1000; i++) {
            dao.insert(new PeerAddress("peer"+i, "adapter"+(i%2), Collections.EMPTY_LIST));
        }

        for (int i = 0; i < 100; i++) {
            assertNotNull("Resolver returns null!", resolver.getPeerAddress("peer" + i, "adapter" + (i % 2)));
        }
        assertEquals("Cache has not requested values correctly!", 100, dao.getRequests());


        for (int i = 0; i < 100; i++) {
            assertNotNull("Resolver returns null!", resolver.getPeerAddress("peer"+i, "adapter"+(i%2)));
        }
        int size = dao.getRequests();
        assertThat("Cache should not request items (they are in the cache)!", size, lessThan(200));

        for (int i = 0; i < 1000; i++) {
            assertNotNull("Resolver returns null!", resolver.getPeerAddress("peer" + i, "adapter" + (i % 2)));
        }
        assertThat("Cache has not requested values correctly!", dao.getRequests(), lessThan(1000 + size));
    }

    @Test
    public void testAddPeerAddress() throws Exception {
        assertNull("Resolver returns address that should not be available!", resolver.getPeerAddress("peer1", "adapter1"));

        PeerAddress address = new PeerAddress("peer1", "adapter1", Collections.EMPTY_LIST);
        resolver.addPeerAddress(address);

        int count = dao.getRequests();

        PeerAddress peerAddress = resolver.getPeerAddress("peer1", "adapter1");
        assertNotNull("Address should not be null!", peerAddress);
        assertEquals("Address does not match the inserted address", address, peerAddress);
        assertEquals("Request should be cached!", count, dao.getRequests());

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