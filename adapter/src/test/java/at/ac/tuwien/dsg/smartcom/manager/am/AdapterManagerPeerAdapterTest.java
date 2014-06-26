package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestFeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class AdapterManagerPeerAdapterTest {

    private AdapterManager manager;
    private MessageBroker broker;

    private Identifier peerId1 = Identifier.peer("peer1");
    private Identifier peerId2 = Identifier.peer("peer1");

    @Before
    public void setUp() throws Exception {
        broker = new SimpleMessageBroker();
        manager = new AdapterManagerImpl(new SimpleAddressResolverDAO(), new PMCallbackImpl(), broker);

        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        manager.destroy();
    }

    @Test(timeout = 1500l)
    public void testRegisterPeerAdapterWithStatelessAdapter() {
        FeedbackPullAdapter pullAdapter1 = new TestFeedbackPullAdapter(peerId1.getId()+".stateless");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0);
        FeedbackPullAdapter pullAdapter2 = new TestFeedbackPullAdapter(peerId2.getId()+".stateless");
        Identifier id2 = manager.addPullAdapter(pullAdapter2, 0);

        manager.registerPeerAdapter(StatelessAdapter.class);

        RoutingRule routing1 = manager.createEndpointForPeer(peerId1);
        RoutingRule routing2 = manager.createEndpointForPeer(peerId2);

        Message msg1 = new Message();
        msg1.setReceiverId(peerId1);

        Message msg2 = new Message();
        msg2.setReceiverId(peerId2);

        broker.publishTask(routing1.getRoute(), msg1);
        broker.publishTask(routing2.getRoute(), msg2);

        broker.publishRequest(id1, new Message());
        broker.publishRequest(id2, new Message());

        Message feedback1 = broker.receiveFeedback();
        Message feedback2 = broker.receiveFeedback();

        assertNotNull("No feedback received!", feedback1);
        assertNotNull("No feedback received!", feedback2);
    }

    @Test(timeout = 1500l)
    public void testRegisterPeerAdapterWithStatefulAdapter() {
        FeedbackPullAdapter pullAdapter1 = new TestFeedbackPullAdapter(peerId1.getId()+".stateful");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0);
        FeedbackPullAdapter pullAdapter2 = new TestFeedbackPullAdapter(peerId2.getId()+".stateful");
        Identifier id2 = manager.addPullAdapter(pullAdapter2, 0);

        manager.registerPeerAdapter(StatefulAdapter.class);

        RoutingRule routing1 = manager.createEndpointForPeer(peerId1);
        RoutingRule routing2 = manager.createEndpointForPeer(peerId2);

        Message msg1 = new Message();
        msg1.setReceiverId(peerId1);

        Message msg2 = new Message();
        msg2.setReceiverId(peerId2);

        broker.publishTask(routing1.getRoute(), msg1);
        broker.publishTask(routing2.getRoute(), msg2);

        broker.publishRequest(id1, new Message());
        broker.publishRequest(id2, new Message());

        Message feedback1 = broker.receiveFeedback();
        Message feedback2 = broker.receiveFeedback();

        assertNotNull("No feedback received!", feedback1);
        assertNotNull("No feedback received!", feedback2);
    }

    private class PMCallbackImpl implements PMCallback {
        @Override
        public Collection<PeerAddress> getPeerAddress(Identifier id) {
            List<PeerAddress> addresses = new ArrayList<>();

            if (peerId1.equals(id)) {
                addresses.add(new PeerAddress(peerId1, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
                addresses.add(new PeerAddress(peerId1, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
            }

            if (peerId2.equals(id)) {
                addresses.add(new PeerAddress(peerId2, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
                addresses.add(new PeerAddress(peerId2, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
            }

            return addresses;
        }

        @Override
        public boolean authenticate(Identifier username, String password) {
            return false;
        }
    }
}