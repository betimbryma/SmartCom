package at.ac.tuwien.dsg.smartcom.messaging;

import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.manager.dao.PeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PeerInfoServiceImplTest {

    public static final int PEER_COUNT = 10;
    private PicoHelper pico;
    private PeerInfoService peerInfoService;
    private PeerInfoCallbackImpl callback;

    @Before
    public void setUp() throws Exception {
        pico = new PicoHelper();
        pico.addComponent(PeerInfoServiceImpl.class);
        pico.addComponent(new PeerInfoCallbackImpl());
        pico.addComponent(new AddressResolver());

        peerInfoService = pico.getComponent(PeerInfoService.class);
        callback = pico.getComponent(PeerInfoCallbackImpl.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
    }

    @Test
    public void testGetPeerInfo() throws Exception {
        for (int i = 0; i < PEER_COUNT; i++) {
            Identifier peer = Identifier.peer("peer" + i);
            PeerInfo peerInfo = peerInfoService.getPeerInfo(peer);
            assertNotNull(peerInfo);
            assertEquals(peer, peerInfo.getId());
        }

        assertEquals(PEER_COUNT, callback.getRetrieveCounter());

        for (int i = 0; i < PEER_COUNT; i++) {
            Identifier peer = Identifier.peer("peer" + i);
            PeerInfo peerInfo = peerInfoService.getPeerInfo(peer);
            assertNotNull(peerInfo);
            assertEquals(peer, peerInfo.getId());
        }

        assertEquals(PEER_COUNT, callback.getRetrieveCounter());
    }

    private class PeerInfoCallbackImpl implements PeerInfoCallback {

        private Map<Identifier, PeerInfo> peerInfoMap = new HashMap<>();
        private AtomicInteger retrieveCounter = new AtomicInteger(0);

        private PeerInfoCallbackImpl() {
            for (int i = 0; i < PEER_COUNT; i++) {
                Identifier peerId = Identifier.peer("peer"+i);
                List<PeerChannelAddress> addresses = new ArrayList<>();
                addresses.add(new PeerChannelAddress(peerId, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
                addresses.add(new PeerChannelAddress(peerId, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
                peerInfoMap.put(peerId, new PeerInfo(peerId, DeliveryPolicy.Peer.PREFERRED, Collections.EMPTY_LIST, addresses));
            }
        }

        @Override
        public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
            retrieveCounter.incrementAndGet();
            return peerInfoMap.get(id);
        }

        public int getRetrieveCounter() {
            return retrieveCounter.intValue();
        }
    }

    private class AddressResolver implements PeerChannelAddressResolverDAO {

        @Override
        public void insert(PeerChannelAddress address) {

        }

        @Override
        public PeerChannelAddress find(Identifier peerId, Identifier adapterId) {
            return null;
        }

        @Override
        public void remove(Identifier peerId, Identifier adapterId) {

        }
    }
}