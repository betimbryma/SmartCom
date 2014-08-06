package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulExceptionAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestInputPullAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
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

import static org.junit.Assert.*;

public class AdapterManagerOutputAdapterTest {

    private AdapterManager manager;
    private MessageBroker broker;

    private Identifier peerId1 = Identifier.peer("peer1");
    private Identifier peerId2 = Identifier.peer("peer2");

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
        pico.dispose();
    }

    @Test(timeout = 1500l)
    public void testRegisterOutputAdapterWithStatelessAdapter() throws CommunicationException {
        InputPullAdapter pullAdapter1 = new TestInputPullAdapter(peerId1.getId()+".stateless");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0);
        InputPullAdapter pullAdapter2 = new TestInputPullAdapter(peerId2.getId()+".stateless");
        Identifier id2 = manager.addPullAdapter(pullAdapter2, 0);

        manager.registerOutputAdapter(StatelessAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerId1);
        Identifier routing2 = manager.createEndpointForPeer(peerId2);

        Message msg1 = new Message();
        msg1.setReceiverId(peerId1);

        Message msg2 = new Message();
        msg2.setReceiverId(peerId2);

        broker.publishTask(routing1, msg1);
        broker.publishTask(routing2, msg2);

        broker.publishRequest(id1, new Message());
        broker.publishRequest(id2, new Message());

        Message input1 = broker.receiveInput();
        Message input2 = broker.receiveInput();

        assertNotNull("No input received!", input1);
        assertNotNull("No input received!", input2);

        Message acknowledge1 = broker.receiveControl();
        Message acknowledge2 = broker.receiveControl();

        assertNotNull("No control received!", acknowledge1);
        assertNotNull("No control received!", acknowledge2);
        assertEquals("ACK", acknowledge1.getSubtype());
        assertEquals("ACK", acknowledge2.getSubtype());
    }

    @Test(timeout = 2000l)
    public void testRegisterOutputAdapterWithStatefulAdapter() throws CommunicationException {
        InputPullAdapter pullAdapter1 = new TestInputPullAdapter(peerId1.getId()+".stateful");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0);
        InputPullAdapter pullAdapter2 = new TestInputPullAdapter(peerId2.getId()+".stateful");
        Identifier id2 = manager.addPullAdapter(pullAdapter2, 0);

        manager.registerOutputAdapter(StatefulAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerId1);
        Identifier routing2 = manager.createEndpointForPeer(peerId2);

        Message msg1 = new Message();
        msg1.setId(Identifier.message("1"));
        msg1.setReceiverId(peerId1);

        Message msg2 = new Message();
        msg2.setId(Identifier.message("2"));
        msg2.setReceiverId(peerId2);

        broker.publishTask(routing1, msg1);
        broker.publishTask(routing2, msg2);

        broker.publishRequest(id1, new Message.MessageBuilder().setId(Identifier.message("3")).create());
        broker.publishRequest(id2, new Message.MessageBuilder().setId(Identifier.message("4")).create());

        Message input1 = broker.receiveInput();
        Message input2 = broker.receiveInput();

        assertNotNull("No input received!", input1);
        assertNotNull("No input received!", input2);

        Message acknowledge1 = broker.receiveControl();
        Message acknowledge2 = broker.receiveControl();

        assertNotNull("No control received!", acknowledge1);
        assertNotNull("No control received!", acknowledge2);
        assertEquals("ACK", acknowledge1.getSubtype());
        assertEquals("ACK", acknowledge2.getSubtype());
    }

    @Test()
    public void testRegisterOutputAdapterWithAdapterThatThrowsException() throws CommunicationException {
        manager.registerOutputAdapter(StatefulExceptionAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerId1);
        Identifier routing2 = manager.createEndpointForPeer(peerId2);

        assertNull(routing1);

        Message msg = new Message();
        msg.setId(Identifier.message("2"));
        msg.setReceiverId(peerId2);

        broker.publishTask(routing2, msg);

        synchronized (this) {
            try {
                wait(1000l);
            } catch (InterruptedException ignored) {

            }
        }

        Message control = broker.receiveControl();

        assertNotNull("No control received!", control);
        assertEquals("CONTROL", control.getType());
        assertEquals("ERROR", control.getSubtype());
    }

    private class PMCallbackImpl implements PMCallback {
        @Override
        public Collection<PeerAddress> getPeerAddress(Identifier id) {
            List<PeerAddress> addresses = new ArrayList<>();

            if (peerId1.equals(id)) {
                addresses.add(new PeerAddress(peerId1, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
                addresses.add(new PeerAddress(peerId1, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
                List<String> parameters = new ArrayList<>();
                parameters.add("test");
                addresses.add(new PeerAddress(peerId1, Identifier.adapter("exception"), parameters));
            }

            if (peerId2.equals(id)) {
                addresses.add(new PeerAddress(peerId2, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
                addresses.add(new PeerAddress(peerId2, Identifier.adapter("stateful"), Collections.EMPTY_LIST));
                addresses.add(new PeerAddress(peerId2, Identifier.adapter("exception"), Collections.EMPTY_LIST));
            }

            return addresses;
        }

        @Override
        public boolean authenticate(Identifier username, String password) {
            return false;
        }
    }
}