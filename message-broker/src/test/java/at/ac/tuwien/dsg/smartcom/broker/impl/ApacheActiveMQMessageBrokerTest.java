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
package at.ac.tuwien.dsg.smartcom.broker.impl;

import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.broker.utils.ApacheActiveMQUtils;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApacheActiveMQMessageBrokerTest {

    ApacheActiveMQMessageBroker broker;

    @Before
    public void setUp() throws Exception {
        ApacheActiveMQUtils.startActiveMQ(61616); //uses standard port
        broker = new ApacheActiveMQMessageBroker("localhost", 61616);
    }

    @After
    public void tearDown() throws Exception {
        try {
            broker.cleanUp();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        broker = null;
        ApacheActiveMQUtils.stopActiveMQ();
    }

    @Test
    public void testInput() throws Exception {
        Method receive = broker.getClass().getMethod("receiveInput");
        Method publish = broker.getClass().getMethod("publishInput", Message.class);
        Method listener = broker.getClass().getMethod("registerInputListener", MessageListener.class);

        parametrizedTest(receive, publish, listener, null);
    }

    @Test
    public void testRequests() throws Exception {
        Method receive = broker.getClass().getMethod("receiveRequests", Identifier.class);
        Method publish = broker.getClass().getMethod("publishRequest", Identifier.class, Message.class);
        Method listener = broker.getClass().getMethod("registerRequestListener", Identifier.class, MessageListener.class);

        parametrizedTest(receive, publish, listener, Identifier.adapter("test"));
    }

    @Test
    public void testReceiveTasks() throws Exception {
        Method receive = broker.getClass().getMethod("receiveOutput", Identifier.class);
        Method publish = broker.getClass().getMethod("publishOutput", Identifier.class, Message.class);
        Method listener = broker.getClass().getMethod("registerOutputListener", Identifier.class, MessageListener.class);

        parametrizedTest(receive, publish, listener, Identifier.adapter("test"));
    }

    @Test
    public void testControl() throws Exception {
        Method receive = broker.getClass().getMethod("receiveControl");
        Method publish = broker.getClass().getMethod("publishControl", Message.class);
        Method listener = broker.getClass().getMethod("registerControlListener", MessageListener.class);

        parametrizedTest(receive, publish, listener, null);
    }

    @Test
    public void testAuthRequest() throws Exception {
        Method receive = broker.getClass().getMethod("receiveAuthRequest");
        Method publish = broker.getClass().getMethod("publishAuthRequest", Message.class);
        Method listener = broker.getClass().getMethod("registerAuthListener", MessageListener.class);

        parametrizedTest(receive, publish, listener, null);
    }

    @Test
    public void testMessageInfoRequest() throws Exception {
        Method receive = broker.getClass().getMethod("receiveMessageInfoRequest");
        Method publish = broker.getClass().getMethod("publishMessageInfoRequest", Message.class);
        Method listener = broker.getClass().getMethod("registerMessageInfoListener", MessageListener.class);

        parametrizedTest(receive, publish, listener, null);
    }

    @Test
    public void testMetricsRequest() throws Exception {
        Method receive = broker.getClass().getMethod("receiveMetricsRequest");
        Method publish = broker.getClass().getMethod("publishMetricsRequest", Message.class);
        Method listener = broker.getClass().getMethod("registerMetricsListener", MessageListener.class);

        parametrizedTest(receive, publish, listener, null);
    }

    @Test
    public void testLog() throws Exception {
        Method receive = broker.getClass().getMethod("receiveLog");
        Method publish = broker.getClass().getMethod("publishLog", Message.class);
        Method listener = broker.getClass().getMethod("registerLogListener", MessageListener.class);

        parametrizedTest(receive, publish, listener, null);
    }

    void parametrizedTest(final Method receive, final Method publish, Method listener, final Identifier identifier) throws IllegalAccessException, InvocationTargetException, InterruptedException, BrokenBarrierException {
        Message msg = new Message.MessageBuilder().setContent("TestInput").create();
        if (identifier == null) {
            publish.invoke(broker, msg);
        } else {
            publish.invoke(broker, identifier, msg);
        }

        Message received;
        if (identifier == null) {
            received = (Message) receive.invoke(broker);
        } else {
            received = (Message) receive.invoke(broker, identifier);
        }
        assertEquals(msg, received);

        final CyclicBarrier barrier = new CyclicBarrier(10);
        final CyclicBarrier barrier2 = new CyclicBarrier(11);
        for (int i = 0; i < 5; i++) {
            new Thread() {

                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (identifier == null) {
                            publish.invoke(broker, new Message.MessageBuilder().setContent("TestInput" + Thread.currentThread().getId()).create());
                        } else {
                            publish.invoke(broker, identifier, new Message.MessageBuilder().setContent("TestInput" + Thread.currentThread().getId()).create());
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    try {
                        barrier2.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        final AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            new Thread() {

                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (identifier == null) {
                            assertNotNull(receive.invoke(broker));
                        } else {
                            assertNotNull(receive.invoke(broker, identifier));
                        }
                        counter.getAndIncrement();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    try {
                        barrier2.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        barrier2.await();

        assertEquals(5, counter.get());

        final AtomicInteger counter1 = new AtomicInteger(0);
        final AtomicInteger counter2 = new AtomicInteger(0);

        MessageListener listener1 = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                counter1.getAndIncrement();
            }
        };
        MessageListener listener2 = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                counter2.getAndIncrement();
            }
        };

        if (identifier == null) {
            listener.invoke(broker, listener1);
            listener.invoke(broker, listener2);
        } else {
            listener.invoke(broker, identifier, listener1);
            listener.invoke(broker, identifier, listener2);
        }

        final CyclicBarrier barrier3 = new CyclicBarrier(10);
        final CyclicBarrier barrier4 = new CyclicBarrier(11);

        for(int i = 0; i < 10; i++) {
            new Thread() {

                @Override
                public void run() {
                    try {
                        barrier3.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (identifier == null) {
                            publish.invoke(broker, new Message.MessageBuilder().setContent("TestInput" + Thread.currentThread().getId()).create());
                        } else {
                            publish.invoke(broker, identifier, new Message.MessageBuilder().setContent("TestInput" + Thread.currentThread().getId()).create());
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    try {
                        barrier4.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        barrier4.await();

        synchronized (this) {
            wait(1000l);
        }

        assertEquals(10, counter1.get()+counter2.get());
    }
}