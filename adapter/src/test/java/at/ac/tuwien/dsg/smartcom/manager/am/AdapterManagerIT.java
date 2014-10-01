package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestInputPullAdapter;
import at.ac.tuwien.dsg.smartcom.manager.dao.MongoDBPeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

public class AdapterManagerIT {
    private static final int AMOUNT_OF_PEERS = 1000;
    private MongoDBInstance mongoDB;

    private AdapterManager manager;
    private MessageBroker broker;

    private MutablePicoContainer pico;
    private MongoClient mongo;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();
        mongo = new MongoClient("localhost", 12345);

        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        //mocks
        pico.as(Characteristics.CACHE).addComponent(SimpleMessageBroker.class);

        //mongodb resolver dao
        pico.as(Characteristics.CACHE).addComponent(new MongoDBPeerChannelAddressResolverDAO(mongo, "test-resolver", "resolver"));

        //real implementations
        pico.as(Characteristics.CACHE).addComponent(AdapterManagerImpl.class);
        pico.as(Characteristics.CACHE).addComponent(AdapterExecutionEngine.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class);
        pico.as(Characteristics.CACHE).addComponent(StatisticBean.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        manager = pico.getComponent(AdapterManagerImpl.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        mongo.close();

        pico.stop();
        pico.dispose();
    }

    @Test(timeout = 40000l)
    public void test() throws InterruptedException, CommunicationException {
        Identifier statefulAdapterId = manager.registerOutputAdapter(StatefulAdapter.class);

        List<Identifier> adapterIds = new ArrayList<>(AMOUNT_OF_PEERS);
        List<Identifier[]> rules = new ArrayList<>(AMOUNT_OF_PEERS);
        List<PeerInfo> peerInfos = new ArrayList<>(AMOUNT_OF_PEERS);
        for (int i = 0; i < AMOUNT_OF_PEERS; i++) {
            Identifier id = Identifier.peer("peer"+i);

            List<PeerChannelAddress> addresses = new ArrayList<>();
            addresses.add(new PeerChannelAddress(id, Identifier.channelType("stateless"), Collections.EMPTY_LIST));
            addresses.add(new PeerChannelAddress(id, Identifier.channelType("stateful"), Collections.EMPTY_LIST));

            Collections.shuffle(addresses);

            peerInfos.add(new PeerInfo(id, DeliveryPolicy.Peer.PREFERRED, Collections.EMPTY_LIST, addresses));
        }

        for (PeerInfo peer : peerInfos) {
            List<Identifier> routes = manager.createEndpointForPeer(peer);
            assertThat(routes, Matchers.not(Matchers.empty()));
            Identifier[] array = new Identifier[2];
            array[0] = routes.get(0);
            array[1] = peer.getId();
            rules.add(array);
            adapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(peer.getId().getId()+"."+array[0].returnIdWithoutPostfix()), 0, false));
        }

        InputListener listener = new InputListener();

        broker.registerInputListener(listener);

        for (Identifier[] rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule[1]);
            broker.publishOutput(rule[0], msg);
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

        for (int i = 0; i < AMOUNT_OF_PEERS; i++) {
            Message acknowledge = broker.receiveControl();

            assertNotNull("No control received!", acknowledge);
            assertEquals("ACK", acknowledge.getSubtype());
        }

        manager.removeOutputAdapter(statefulAdapterId);

        for (Identifier[] rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule[1]);
            broker.publishOutput(rule[0], msg);
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

        assertThat("No more requests handled after removed one (of two) output adapters!", listener.counter.get(), greaterThanOrEqualTo(counter));
    }

    private class InputListener implements MessageListener {
        AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void onMessage(Message message) {
            counter.getAndIncrement();
        }
    }
}