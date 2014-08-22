/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.ArrayList;
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
        pico.as(Characteristics.CACHE).addComponent(SimpleAddressPeerChannelAddressResolverDAO.class);

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
        pico.dispose();

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

    private class TestInputPushAdapter extends InputPushAdapter {

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
        protected void cleanUp() {
            text = "destroyed";
        }
    }
}