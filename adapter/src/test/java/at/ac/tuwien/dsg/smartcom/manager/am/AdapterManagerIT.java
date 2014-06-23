package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestFeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.MongoDBResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        broker = new SimpleMessageBroker();

        MongoClient mongo = new MongoClient("localhost", 12345);
        ResolverDAO dao = new MongoDBResolverDAO(mongo, "test-resolver", "resolver");

        manager = new AdapterManagerImpl(dao, new PMCallbackImpl(), broker);
        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        manager.destroy();
    }

    @Test
    public void test() throws InterruptedException {
        String statefulAdapterId = manager.registerPeerAdapter(StatefulAdapter.class);
        String statelessAdapterId = manager.registerPeerAdapter(StatelessAdapter.class);

        List<String> adapterIds = new ArrayList<>(AMOUNT_OF_PEERS);
        List<RoutingRule> rules = new ArrayList<>(AMOUNT_OF_PEERS);
        List<String> peers = new ArrayList<>(AMOUNT_OF_PEERS);
        for (int i = 0; i < AMOUNT_OF_PEERS; i++) {
            peers.add("peer"+i);
        }

        for (String peer : peers) {
            RoutingRule route = manager.createEndpointForPeer(peer);
            rules.add(route);
            adapterIds.add(manager.addPullAdapter(new TestFeedbackPullAdapter(peer+"."+route.getRoute().replaceFirst("adapter.",""))));
        }

        FeedbackListener listener = new FeedbackListener();

        broker.registerFeedbackListener(listener);

        for (RoutingRule rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule.getReceiver());
            broker.publishTask(rule.getRoute(), msg);
        }

        for (String adapterId : adapterIds) {
            broker.publishRequest(adapterId, new Message());
        }

        synchronized (this) {
            wait(20000l);
        }

        int counter = listener.counter.get();

        assertEquals("Not enough feedback received!", AMOUNT_OF_PEERS, counter);

        manager.removePeerAdapter(statefulAdapterId);

        for (RoutingRule rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule.getReceiver());
            broker.publishTask(rule.getRoute(), msg);
        }

        for (String adapterId : adapterIds) {
            broker.publishRequest(adapterId, new Message());
        }

        synchronized (this) {
            wait(20000l);
        }

        assertThat("No more requests handled after removed one (of two) peer adapters!", listener.counter.get(), greaterThan(counter));
    }
    
    private class PMCallbackImpl implements PMCallback {
        @Override
        public Collection<PeerAddress> getPeerAddress(String id) {
            List<PeerAddress> addresses = new ArrayList<>();

            addresses.add(new PeerAddress(id, "stateless", Collections.EMPTY_LIST));
            addresses.add(new PeerAddress(id, "stateful", Collections.EMPTY_LIST));

            Collections.shuffle(addresses);

            return addresses;
        }

        @Override
        public boolean authenticate(String username, String password) {
            return false;
        }
    }

    private class FeedbackListener implements MessageListener {
        AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void onMessage(Message message) {
            counter.getAndIncrement();
        }
    }
}