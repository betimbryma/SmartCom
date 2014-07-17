package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapterImpl;
import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.AdapterWithoutAnnotation;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
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

    Identifier peerId1 = Identifier.peer("peer1");
    Identifier peerId2 = Identifier.peer("peer2");

    private MutablePicoContainer pico;

    @Before
    public void setUp() throws Exception {
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        //mocks
        pico.as(Characteristics.CACHE).addComponent(SimpleMessageBroker.class);
        pico.as(Characteristics.CACHE).addComponent(new PMCallbackImpl());
        pico.as(Characteristics.CACHE).addComponent(SimpleAddressResolverDAO.class);

        //real implementations
        pico.as(Characteristics.CACHE).addComponent(AdapterManagerImpl.class);
        pico.as(Characteristics.CACHE).addComponent(AdapterExecutionEngine.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        manager = pico.getComponent(AdapterManagerImpl.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
    }

    @Test(timeout = 1500l)
    public void testRegisterOutputAdapterWithoutAnnotation() throws Exception {
        Identifier id = manager.registerOutputAdapter(AdapterWithoutAnnotation.class);
        assertNull("Adapter should not have an id because it should not have been registered!", id);
    }

    @Test(timeout = 1500l)
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

        barrier.await();

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

        RoutingRule routing1 = manager.createEndpointForPeer(peerId1);

        Message msg = new Message();
        msg.setReceiverId(peerId1);
        broker.publishTask(routing1.getRoute(), msg);

        broker.publishRequest(id1, new Message());
        Message input1 = broker.receiveInput();
        assertNotNull("First input should not be null!", input1);

        //remove the adapter
        //adapter for peerId1 should not work anymore
        //no new adapter should be created for peerId2
        manager.removeOutputAdapter(adapter);

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

        RoutingRule routing1 = manager.createEndpointForPeer(peerId1);

        Message msg = new Message();
        msg.setReceiverId(peerId1);
        broker.publishTask(routing1.getRoute(), msg);

        broker.publishRequest(id1, new Message());
        Message input1 = broker.receiveInput();
        assertNotNull("First input should not be null!", input1);

        //remove the adapter
        //adapter for peerId1 should not work anymore
        //no new adapter should be created for peerId2
        manager.removeOutputAdapter(adapter);

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

    private class TestInputPushAdapter extends InputPushAdapterImpl {

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
                        e.printStackTrace();
                        fail("Could not wait for barrier release!");
                    }
                }
            });
        }

        @Override
        public void preDestroy() {
            publishMessage = false;
        }

        @Override
        protected void cleanUp() {

        }
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