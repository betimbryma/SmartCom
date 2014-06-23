package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapterImpl;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.AdapterWithoutAnnotation;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

public class AdapterManagerImplTest {

    private AdapterManager manager;
    private MessageBroker broker;

    String peerId1 = "peer1";
    String peerId2 = "peer2";

    @Before
    public void setUp() throws Exception {
        broker = new SimpleMessageBroker();
        manager = new AdapterManagerImpl(new PMCallbackImpl(), broker);

        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        manager.destroy();
    }

    @Test(timeout = 1500)
    public void testRegisterPeerAdapterWithoutAnnotation() throws Exception {
        String id = manager.registerPeerAdapter(AdapterWithoutAnnotation.class);
        assertNull("Adapter should not have an id because it should not have been registered!", id);
    }

    @Test(timeout = 2000)
    public void testRemoveAdapterWithPushAdapter() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(6);
        List<String> feedbackAdapterIds = new ArrayList<>();

        feedbackAdapterIds.add(manager.addPushAdapter(new TestFeedbackPushAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPushAdapter(new TestFeedbackPushAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPushAdapter(new TestFeedbackPushAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPushAdapter(new TestFeedbackPushAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPushAdapter(new TestFeedbackPushAdapter(barrier)));

        for (String feedbackAdapterId : feedbackAdapterIds) {
            manager.removeAdapter(feedbackAdapterId);
        }

        barrier.await();

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message feedback = broker.receiveFeedback();
        if (feedback != null) {
            fail("There should be no message!");
        }
    }

    @Test(timeout = 1500)
    public void testRemoveAdapterWithPullAdapter() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(5);
        List<String> feedbackAdapterIds = new ArrayList<>();

        feedbackAdapterIds.add(manager.addPullAdapter(new TestFeedbackPullAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPullAdapter(new TestFeedbackPullAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPullAdapter(new TestFeedbackPullAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPullAdapter(new TestFeedbackPullAdapter(barrier)));
        feedbackAdapterIds.add(manager.addPullAdapter(new TestFeedbackPullAdapter(barrier)));

        for (String feedbackAdapterId : feedbackAdapterIds) {
            manager.removeAdapter(feedbackAdapterId);
            broker.publishRequest(feedbackAdapterId, new Message());
        }

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message feedback = broker.receiveFeedback();
        if (feedback != null) {
            fail("There should be no message!");
        }
    }

    @Test(timeout = 1500)
    public void testRemovePeerAdapterWithStatefulAdapter() throws Exception {
        FeedbackPullAdapter pullAdapter1 = new TestSimpleFeedbackPullAdapter("stateful."+peerId1);
        String id1 = manager.addPullAdapter(pullAdapter1);

        String adapter = manager.registerPeerAdapter(StatefulAdapter.class);

        RoutingRule routing1 = manager.createEndpointForPeer(peerId1);

        Message msg = new Message();
        msg.setReceiverId(peerId1);
        broker.publishTask(routing1.getRoute(), msg);

        broker.publishRequest(id1, new Message());
        Message feedback1 = broker.receiveFeedback();
        assertNotNull("First feedback should not be null!", feedback1);

        //remove the adapter
        //adapter for peerId1 should not work anymore
        //no new adapter should be created for peerId2
        manager.removePeerAdapter(adapter);

        RoutingRule routing2 = manager.createEndpointForPeer(peerId2);
        assertNull("There should be no routing for peerId2", routing2);

        msg = new Message();
        msg.setReceiverId(peerId1);
        broker.publishTask(routing1.getRoute(), msg);

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message feedback = broker.receiveFeedback();
        if (feedback != null) {
            fail("There should be no message!");
        }
    }

    @Test(timeout = 1500)
    public void testRemovePeerAdapterWithStatelessAdapter() throws Exception {
        FeedbackPullAdapter pullAdapter1 = new TestSimpleFeedbackPullAdapter("stateless."+peerId1);
        String id1 = manager.addPullAdapter(pullAdapter1);

        String adapter = manager.registerPeerAdapter(StatelessAdapter.class);

        RoutingRule routing1 = manager.createEndpointForPeer(peerId1);

        Message msg = new Message();
        msg.setReceiverId(peerId1);
        broker.publishTask(routing1.getRoute(), msg);

        broker.publishRequest(id1, new Message());
        Message feedback1 = broker.receiveFeedback();
        assertNotNull("First feedback should not be null!", feedback1);

        //remove the adapter
        //adapter for peerId1 should not work anymore
        //no new adapter should be created for peerId2
        manager.removePeerAdapter(adapter);

        RoutingRule routing2 = manager.createEndpointForPeer(peerId2);
        assertNull("There should be no routing for peerId2", routing2);

        msg = new Message();
        msg.setReceiverId(peerId1);
        broker.publishTask(routing1.getRoute(), msg);

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message feedback = broker.receiveFeedback();
        if (feedback != null) {
            fail("There should be no message!");
        }
    }

    private class TestSimpleFeedbackPullAdapter implements FeedbackPullAdapter {
        private final String pullAddress;

        private TestSimpleFeedbackPullAdapter(String pullAddress) {
            this.pullAddress = pullAddress;
        }

        @Override
        public Message pull() {
            return AdapterTestQueue.receive(pullAddress);
        }
    }

    private class TestFeedbackPullAdapter implements FeedbackPullAdapter {

        final CyclicBarrier barrier;

        private TestFeedbackPullAdapter(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public Message pull() {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                return null;
            }

            Message msg = new Message();
            msg.setContent("pull");
            return msg;
        }
    }

    private class TestFeedbackPushAdapter extends FeedbackPushAdapterImpl {

        String text = "uninitialized";
        final CyclicBarrier barrier;
        boolean publishMessage = true;

        private TestFeedbackPushAdapter(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void init() {
            text = "push";

            TimerTask action = new TimerTask() {
                public void run() {
                    try {
                        barrier.await();

                        if (publishMessage) {
                            Message msg = new Message();
                            msg.setContent(text);
                            publishMessage(msg);
                        }
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                        fail("Could not wait for barrier release!");
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(action, 0);
        }

        @Override
        public void preDestroy() {
            publishMessage = false;
        }
    }

    private class PMCallbackImpl implements PMCallback {
        @Override
        public Collection<PeerAddress> getPeerAddress(String id) {
            List<PeerAddress> addresses = new ArrayList<>();

            if (peerId1.equals(id)) {
                addresses.add(new PeerAddress(peerId1, "stateless", Collections.emptyList()));
                addresses.add(new PeerAddress(peerId1, "stateful", Collections.emptyList()));
            }

            if (peerId2.equals(id)) {
                addresses.add(new PeerAddress(peerId2, "stateless", Collections.emptyList()));
                addresses.add(new PeerAddress(peerId2, "stateful", Collections.emptyList()));
            }

            return addresses;
        }

        @Override
        public boolean authenticate(String username, String password) {
            return false;
        }
    }
}