package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulExceptionAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestInputPullAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
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

import static org.junit.Assert.*;

public class AdapterManagerOutputAdapterTest {

    private AdapterManager manager;
    private MessageBroker broker;

    private PeerInfo peerInfo1;
    private PeerInfo peerInfo2;

    private Identifier peerId1 = Identifier.peer("peer1");
    private Identifier peerId2 = Identifier.peer("peer2");

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
        pico.as(Characteristics.CACHE).addComponent(StatisticBean.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        manager = pico.getComponent(AdapterManagerImpl.class);

        pico.start();

        List<PeerChannelAddress> addresses1 = new ArrayList<>();
        addresses1.add(new PeerChannelAddress(peerId1, Identifier.channelType("stateless"), Collections.EMPTY_LIST));
        addresses1.add(new PeerChannelAddress(peerId1, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        List<String> parameters = new ArrayList<>();
        parameters.add("test");
        addresses1.add(new PeerChannelAddress(peerId1, Identifier.channelType("exception"), parameters));
        peerInfo1 = new PeerInfo(peerId1, DeliveryPolicy.Peer.PREFERRED, Collections.EMPTY_LIST, addresses1);

        List<PeerChannelAddress> addresses2 = new ArrayList<>();
        addresses2.add(new PeerChannelAddress(peerId2, Identifier.channelType("stateless"), Collections.EMPTY_LIST));
        addresses2.add(new PeerChannelAddress(peerId2, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        addresses2.add(new PeerChannelAddress(peerId2, Identifier.channelType("exception"), Collections.EMPTY_LIST));
        peerInfo2 = new PeerInfo(peerId2, DeliveryPolicy.Peer.PREFERRED, Collections.EMPTY_LIST, addresses2);
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
        pico.dispose();

        AdapterTestQueue.clear();
    }

    @Test(timeout = 5000l)
    public void testRegisterOutputAdapterWithStatelessAdapter() throws CommunicationException {
        InputPullAdapter pullAdapter1 = new TestInputPullAdapter(peerId1.getId()+".stateless");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0, false);
        InputPullAdapter pullAdapter2 = new TestInputPullAdapter(peerId2.getId()+".stateless");
        Identifier id2 = manager.addPullAdapter(pullAdapter2, 0, false);

        manager.registerOutputAdapter(StatelessAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerInfo1).get(0);
        Identifier routing2 = manager.createEndpointForPeer(peerInfo2).get(0);

        Message msg1 = new Message();
        msg1.setReceiverId(peerId1);

        Message msg2 = new Message();
        msg2.setReceiverId(peerId2);

        broker.publishOutput(routing1, msg1);
        broker.publishOutput(routing2, msg2);

        broker.publishRequest(id1, new Message());
        broker.publishRequest(id2, new Message());

        Message input1 = broker.receiveInput();
        Message input2 = broker.receiveInput();

        assertNotNull("No input received!", input1);
        assertNotNull("No input received!", input2);

//        Message acknowledge1 = broker.receiveControl();
//        Message acknowledge2 = broker.receiveControl();
//
//        assertNotNull("No control received!", acknowledge1);
//        assertNotNull("No control received!", acknowledge2);
//        assertEquals("ACK", acknowledge1.getSubtype());
//        assertEquals("ACK", acknowledge2.getSubtype());
    }

    @Test(timeout = 10000l)
    public void testRegisterOutputAdapterWithStatefulAdapter() throws CommunicationException {
        InputPullAdapter pullAdapter1 = new TestInputPullAdapter(peerId1.getId()+".stateful");
        Identifier id1 = manager.addPullAdapter(pullAdapter1, 0, false);
        InputPullAdapter pullAdapter2 = new TestInputPullAdapter(peerId2.getId()+".stateful");
        Identifier id2 = manager.addPullAdapter(pullAdapter2, 0, false);

        manager.registerOutputAdapter(StatefulAdapter.class);

        Identifier routing1 = manager.createEndpointForPeer(peerInfo1).get(0);
        Identifier routing2 = manager.createEndpointForPeer(peerInfo2).get(0);

        Message msg1 = new Message();
        msg1.setId(Identifier.message("1"));
        msg1.setReceiverId(peerId1);

        Message msg2 = new Message();
        msg2.setId(Identifier.message("2"));
        msg2.setReceiverId(peerId2);

        broker.publishOutput(routing1, msg1);
        broker.publishOutput(routing2, msg2);

        broker.publishRequest(id1, new Message.MessageBuilder().setId(Identifier.message("3")).create());
        broker.publishRequest(id2, new Message.MessageBuilder().setId(Identifier.message("4")).create());

        Message input1 = broker.receiveInput();
        Message input2 = broker.receiveInput();

        assertNotNull("No input received!", input1);
        assertNotNull("No input received!", input2);

        synchronized (this) {
            try {
                wait(1000);
            } catch (InterruptedException ignored) {

            }
        }

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

        assertThat(manager.createEndpointForPeer(peerInfo1), Matchers.empty());
        Identifier routing2 = manager.createEndpointForPeer(peerInfo2).get(0);

        Message msg = new Message();
        msg.setId(Identifier.message("2"));
        msg.setReceiverId(peerId2);

        broker.publishOutput(routing2, msg);

        synchronized (this) {
            try {
                wait(1000l);
            } catch (InterruptedException ignored) {

            }
        }

        Message control = broker.receiveControl();

        assertNotNull("No control received!", control);
        assertEquals("CONTROL", control.getType());
        assertEquals("COMERROR", control.getSubtype());
    }
}