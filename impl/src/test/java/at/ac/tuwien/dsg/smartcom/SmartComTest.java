package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class SmartComTest {

    //@Test
    public void testSmartComInterface() throws Exception {
        SmartCom smartCom = new SmartCom(new PeerManager(), new CollectiveInfo());
        smartCom.initializeSmartCom();

        assertNotNull(smartCom.getCommunication());
    }

    private class PeerManager implements PMCallback {

        @Override
        public Collection<PeerAddress> getPeerAddress(Identifier id) throws NoSuchPeerException {
            return null;
        }

        @Override
        public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
            return false;
        }
    }

    private class CollectiveInfo implements CollectiveInfoCallback {

        @Override
        public List<Identifier> resolveCollective(Identifier collective) throws NoSuchCollectiveException {
            return null;
        }
    }
}