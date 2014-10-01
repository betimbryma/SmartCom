package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

import static org.junit.Assert.assertNotNull;

public class SmartComTest {

    //@Test
    public void testSmartComInterface() throws Exception {
        SmartCom smartCom = new SmartCom(new PeerManager(), new PeerInfoCallback(), new CollectiveInfo());
        smartCom.initializeSmartCom();

        assertNotNull(smartCom.getCommunication());
    }

    private class PeerManager implements PeerAuthenticationCallback {

        @Override
        public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
            return false;
        }
    }

    private class CollectiveInfo implements CollectiveInfoCallback {

        @Override
        public at.ac.tuwien.dsg.smartcom.model.CollectiveInfo getCollectiveInfo(Identifier collective) throws NoSuchCollectiveException {
            return null;
        }
    }

    private class PeerInfoCallback implements at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback {

        @Override
        public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
            return null;
        }
    }
}