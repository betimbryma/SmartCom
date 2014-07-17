package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.SimplePeerManager;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapterImpl;
import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
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
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AdapterManagerInputAdapterTest {

    private List<Identifier> inputAdapterIds = new ArrayList<>();
    private InputPullAdapter pullAdapter;
    private AdapterManager manager;
    private InputPushAdapter pushAdapter;
    private MessageBroker broker;
    private MutablePicoContainer pico;


    @Before
    public void setUp() throws Exception {
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
        //mocks
        pico.as(Characteristics.CACHE).addComponent(SimpleMessageBroker.class);
        pico.as(Characteristics.CACHE).addComponent(SimplePeerManager.class);
        pico.as(Characteristics.CACHE).addComponent(SimpleAddressResolverDAO.class);

        //real implementations
        pico.as(Characteristics.CACHE).addComponent(AdapterManagerImpl.class);
        pico.as(Characteristics.CACHE).addComponent(AdapterExecutionEngine.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        manager = pico.getComponent(AdapterManagerImpl.class);

        pico.start();

        pullAdapter = new TestInputPullAdapter();
        pushAdapter = new TestInputPushAdapter();
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();

        for (Identifier id : inputAdapterIds) {
            manager.removeInputAdapter(id);
        }
    }

    @Test(timeout = 1500l)
    public void testAddPushAdapter() throws Exception {
        inputAdapterIds.add(manager.addPushAdapter(pushAdapter));

        Message input = broker.receiveInput();

        assertEquals("wrong input", "push", input.getContent());
    }

    @Test(timeout = 1500l)
    public void testAddPullAdapter() throws Exception {
        Identifier id = manager.addPullAdapter(pullAdapter, 0);
        inputAdapterIds.add(id);

        broker.publishRequest(id, new Message());

        Message input = broker.receiveInput();

        assertEquals("wrong input", "pull", input.getContent());
    }

    @Test(timeout = 1500l)
    public void testAddPullAdapterWithTimeout() throws Exception {
        Identifier id = manager.addPullAdapter(pullAdapter, 1000);
        inputAdapterIds.add(id);

        Message input = broker.receiveInput();

        assertEquals("wrong input", "pull", input.getContent());
    }

    private class TestInputPullAdapter implements InputPullAdapter {

        @Override
        public Message pull() {
            Message msg = new Message();
            msg.setContent("pull");
            return msg;
        }
    }

    private class TestInputPushAdapter extends InputPushAdapterImpl {

        String text = "uninitialized";

        @Override
        public void init() {
            text = "push";

            schedule(new PushTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message msg = new Message();
                    msg.setContent(text);
                    publishMessage(msg);
                }
            });
        }

        @Override
        public void preDestroy() {
            text = "destroyed";
        }

        @Override
        protected void cleanUp() {

        }
    }

    private class PMCallback implements at.ac.tuwien.dsg.smartcom.callback.PMCallback {

        @Override
        public Collection<PeerAddress> getPeerAddress(Identifier id) {
            return null;
        }

        @Override
        public boolean authenticate(Identifier username, String password) {
            return false;
        }
    }
}