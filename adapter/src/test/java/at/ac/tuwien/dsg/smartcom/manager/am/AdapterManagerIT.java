package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestInputPullAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.MongoDBResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AdapterManagerIT {
    public static final int AMOUNT_OF_PEERS = 1000;
    private MongoDBInstance mongoDB;

    private AdapterManager manager;
    private MessageBroker broker;

    private MutablePicoContainer pico;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();
        MongoClient mongo = new MongoClient("localhost", 12345);

        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        //mocks
        pico.as(Characteristics.CACHE).addComponent(SimpleMessageBroker.class);
        pico.as(Characteristics.CACHE).addComponent(new PMCallbackImpl());

        //mongodb resolver dao
        pico.as(Characteristics.CACHE).addComponent(new MongoDBResolverDAO(mongo, "test-resolver", "resolver"));

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
        mongoDB.tearDown();

        pico.stop();
    }

    @Test(timeout = 30000l)
    public void test() throws InterruptedException {
        Identifier statefulAdapterId = manager.registerOutputAdapter(StatefulAdapter.class);
        Identifier statelessAdapterId = manager.registerOutputAdapter(StatelessAdapter.class);

        List<Identifier> adapterIds = new ArrayList<>(AMOUNT_OF_PEERS);
        List<Identifier[]> rules = new ArrayList<>(AMOUNT_OF_PEERS);
        List<Identifier> peers = new ArrayList<>(AMOUNT_OF_PEERS);
        for (int i = 0; i < AMOUNT_OF_PEERS; i++) {
            peers.add(Identifier.peer("peer"+i));
        }

        for (Identifier peer : peers) {
            Identifier route = manager.createEndpointForPeer(peer);
            Identifier[] array = new Identifier[2];
            array[0] = route;
            array[1] = peer;
            rules.add(array);
            adapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(peer.getId()+"."+route.getIdWithoutPostfix()), 0));
        }

        InputListener listener = new InputListener();

        broker.registerInputListener(listener);

        for (Identifier[] rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule[1]);
            broker.publishTask(rule[0], msg);
        }

        for (Identifier adapterId : adapterIds) {
            broker.publishRequest(adapterId, new Message());
        }

        int counterOld = -1;
        int counter;
        while ((counter = listener.counter.get()) != counterOld) {
            synchronized (this) {
                wait(1000l);
            }
            counterOld = counter;
        }

        assertEquals("Not enough input received!", AMOUNT_OF_PEERS, counter);

        System.out.println("remove");

        manager.removeOutputAdapter(statefulAdapterId);

        for (Identifier[] rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule[1]);
            broker.publishTask(rule[0], msg);
        }

        for (Identifier adapterId : adapterIds) {
            broker.publishRequest(adapterId, new Message());
        }

        counterOld = -1;
        int counter2;
        while ((counter2 = listener.counter.get()) != counterOld) {
            synchronized (this) {
                wait(1000l);
            }
            counterOld = counter2;
        }

        assertThat("No more requests handled after removed one (of two) output adapters!", listener.counter.get(), greaterThan(counter));
    }
    
    private class PMCallbackImpl implements PMCallback {
        @Override
        public Collection<PeerAddress> getPeerAddress(Identifier id) {
            List<PeerAddress> addresses = new ArrayList<>();

            addresses.add(new PeerAddress(id, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
            addresses.add(new PeerAddress(id, Identifier.adapter("stateful"), Collections.EMPTY_LIST));

            Collections.shuffle(addresses);

            return addresses;
        }

        @Override
        public boolean authenticate(Identifier peerId, String password) {
            return false;
        }
    }

    private class InputListener implements MessageListener {
        AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void onMessage(Message message) {
            counter.getAndIncrement();
        }
    }
}