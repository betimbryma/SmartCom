package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.AdapterWithoutAnnotation;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.*;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

public class AdapterManagerTest {

    private AdapterManager manager;
    private MessageBroker broker;

    private Identifier peerId1 = Identifier.peer("peer1");
    private Identifier peerId2 = Identifier.peer("peer2");

    private PeerInfo peerInfo1;
    private PeerInfo peerInfo2;

    private MutablePicoContainer pico;

    @Before
    public void setUp() throws Exception {
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        //mocks
        pico.as(Characteristics.CACHE).addComponent(SimpleMessageBroker.class);
        pico.as(Characteristics.CACHE).addComponent(SimpleAddressPeerChannelAddressResolverDAO.class);

        //real implementations
        pico.as(Characteristics.CACHE).addComponent(AdapterManagerImpl.class);
        pico.as(Characteristics.CACHE).addComponent(AdapterExecutionEngine.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        manager = pico.getComponent(AdapterManagerImpl.class);

        pico.start();

        List<PeerChannelAddress> addresses1 = new ArrayList<>();
        addresses1.add(new PeerChannelAddress(peerId1, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
        addresses1.add(new PeerChannelAddress(peerId1, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
        peerInfo1 = new PeerInfo(peerId1, DeliveryPolicy.Peer.PREFERRED, Collections.EMPTY_LIST, addresses1);

        List<PeerChannelAddress> addresses2 = new ArrayList<>();
        addresses2.add(new PeerChannelAddress(peerId2, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
        addresses2.add(new PeerChannelAddress(peerId2, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
        peerInfo2 = new PeerInfo(peerId2, DeliveryPolicy.Peer.PREFERRED, Collections.EMPTY_LIST, addresses2);
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
        pico.dispose();
    }

    @Test(timeout = 1500l, expected = CommunicationException.class)
    public void testRegisterOutputAdapterWithoutAnnotation() throws Exception {
        Identifier id = manager.registerOutputAdapter(AdapterWithoutAnnotation.class);
        assertNull("Adapter should not have an id because it should not have been registered!", id);
    }

    @Test(timeout = 2000l)
    public void testRemoveAdapterWithPushAdapter() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(6);
        List<Identifier> inputAdapterIds = new ArrayList<>();

        inputAdapterIds.add(manager.addPushAdapter(new TestInputPushAdapter(barrier)));
        inputAdapterIds.add(manager.addPushAdapter(new TestInputPushAdapter(barrier)));
        inputAdapterIds.add(manager.addPushAdapter(new TestInputPushAdapter(barrier)));
        inputAdapterIds.add(manager.addPushAdapter(new TestInputPushAdapter(barrier)));
        inputAdapterIds.add(manager.addPushAdapter(new TestInputPushAdapter(barrier)));

        for (Identifier inputAdapterId : inputAdapterIds) {
            manager.removeInputAdapter(inputAdapterId);
        }

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException ignored) {
        }

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message input = broker.receiveInput();
        if (input != null) {
            fail("There should be no message!");
        }
    }

    @Test(timeout = 1500l)
    public void testRemoveAdapterWithPullAdapter() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(5);
        List<Identifier> inputAdapterIds = new ArrayList<>();

        inputAdapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(barrier), 0));
        inputAdapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(barrier), 0));
        inputAdapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(barrier), 0));
        inputAdapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(barrier), 0));
        inputAdapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(barrier), 0));

        for (Identifier inputAdapterId : inputAdapterIds) {
            manager.removeInputAdapter(inputAdapterId);
            broker.publishRequest(inputAdapterId, new Message());
        }

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message input = broker.receiveInput();
        if (input != null) {
            fail("There should be no message!");
        }
    }

    @Test(timeout = 3000l)
    public void testRemoveOutputAdapterWithStatefulAdapter() throws Exception {
        InputPullAdapter pullAdapter1 = new TestSimpleInputPullAdapter(peerId1.getId()+".stateful");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0);

        Identifier adapter = manager.registerOutputAdapter(StatefulAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerInfo1).get(0);

        Message msg = new Message();
        msg.setId(Identifier.message("1"));
        msg.setReceiverId(peerId1);
        broker.publishOutput(routing1, msg);

        broker.publishRequest(id1, new Message.MessageBuilder().setId(Identifier.message("2")).create());
        Message input1 = broker.receiveInput();
        assertNotNull("First input should not be null!", input1);

        //remove the adapter
        //adapter for peerId1 should not work anymore
        //no new adapter should be created for peerId2
        manager.removeOutputAdapter(adapter);

        assertThat("There should be no routing for peerId2", manager.createEndpointForPeer(peerInfo2), Matchers.empty());

        msg = new Message();
        msg.setId(Identifier.message("3"));
        msg.setReceiverId(peerId1);
        broker.publishOutput(routing1, msg);

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message input = broker.receiveInput();
        if (input != null) {
            fail("There should be no message!");
        }
    }

    @Test(timeout = 1500l)
    public void testRemoveOutputAdapterWithStatelessAdapter() throws Exception {
        InputPullAdapter pullAdapter1 = new TestSimpleInputPullAdapter(peerId1.getId()+".stateless");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0);
        Identifier adapter = manager.registerOutputAdapter(StatelessAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerInfo1).get(0);

        Message msg = new Message();
        msg.setId(Identifier.message("1"));
        msg.setReceiverId(peerId1);
        broker.publishOutput(routing1, msg);

        broker.publishRequest(id1, new Message.MessageBuilder().setId(Identifier.message("2")).create());
        Message input1 = broker.receiveInput();
        assertNotNull("First input should not be null!", input1);

        //remove the adapter
        //adapter for peerId1 should not work anymore
        //no new adapter should be created for peerId2
        manager.removeOutputAdapter(adapter);

        assertThat("There should be no routing for peerId2", manager.createEndpointForPeer(peerInfo2), Matchers.empty());

        msg = new Message();
        msg.setId(Identifier.message("3"));
        msg.setReceiverId(peerId1);
        broker.publishOutput(routing1, msg);

        final Thread thisThread = Thread.currentThread();
        TimerTask action = new TimerTask() {
            public void run() {
                thisThread.interrupt();
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000);

        Message input = broker.receiveInput();
        if (input != null) {
            fail("There should be no message!");
        }
    }

    private class TestSimpleInputPullAdapter implements InputPullAdapter {
        private final String pullAddress;

        private TestSimpleInputPullAdapter(String pullAddress) {
            this.pullAddress = pullAddress;
        }

        @Override
        public Message pull() {
            return AdapterTestQueue.receive(pullAddress);
        }
    }

    private class TestInputPullAdapter implements InputPullAdapter {

        final CyclicBarrier barrier;

        private TestInputPullAdapter(CyclicBarrier barrier) {
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

    private class TestInputPushAdapter extends InputPushAdapter {

        String text = "uninitialized";
        final CyclicBarrier barrier;
        boolean publishMessage = true;

        private TestInputPushAdapter(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void init() {
            text = "push";

            schedule(new PushTask() {
                @Override
                public void run() {
                    try {
                        barrier.await();

                        if (publishMessage) {
                            Message msg = new Message();
                            msg.setContent(text);
                            publishMessage(msg);
                        }
                    } catch (InterruptedException | BrokenBarrierException e) {
                        //e.printStackTrace();
                        //fail("Could not wait for barrier release!");
                    }
                }
            });
        }

        @Override
        public void cleanUp() {
            publishMessage = false;
        }
    }
}