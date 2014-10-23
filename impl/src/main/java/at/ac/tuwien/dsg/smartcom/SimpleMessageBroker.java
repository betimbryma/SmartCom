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
package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.broker.CancelableListener;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class SimpleMessageBroker implements MessageBroker {
    private static final Logger log = LoggerFactory.getLogger(SimpleMessageBroker.class);

    private final static String AUTH_QUEUE = "AUTH_QUEUE";
    private final static String MIS_QUEUE = "MIS_QUEUE";
    private final static String MPS_QUEUE = "MPS_QUEUE";
    private final static String CONTROL_QUEUE = "CONTROL_QUEUE";
    private final static String LOG_QUEUE = "LOG_QUEUE";

    private final BlockingDeque<Message> inputQueue = new LinkedBlockingDeque<>();
    private final Map<Identifier,BlockingDeque<Message>> requestQueues = new HashMap<>();
    private final Map<Identifier,BlockingDeque<Message>> taskQueues = new HashMap<>();
    private final Map<String, BlockingDeque<Message>> specialQueues = new HashMap<>();

    private MessageListener inputListener = null;
    private final Map<Identifier,MessageListener> requestListeners = new HashMap<>();
    private final Map<Identifier,MessageListener> taskListeners = new HashMap<>();
    private final Map<String, MessageListener> specialListeners = new HashMap<>();

    public SimpleMessageBroker() {
        specialQueues.put(AUTH_QUEUE, new LinkedBlockingDeque<Message>());
        specialQueues.put(MIS_QUEUE, new LinkedBlockingDeque<Message>());
        specialQueues.put(MPS_QUEUE, new LinkedBlockingDeque<Message>());
        specialQueues.put(CONTROL_QUEUE, new LinkedBlockingDeque<Message>());
        specialQueues.put(LOG_QUEUE, new LinkedBlockingDeque<Message>());
    }

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(40);
    }

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }

    @Override
    public void publishInput(final Message message) {
        synchronized (inputQueue) {
            if (inputListener == null) {
                inputQueue.add(message);
                log.trace("Published input {}", message);
            } else {
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        inputListener.onMessage(message);
                    }
                });
                log.trace("Called listener on input {}", message);
            }
        }
    }

    @Override
    public Message receiveInput() {
        try {
            log.trace("Receiving input...");
            return inputQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public CancelableListener registerInputListener(MessageListener listener) {
        synchronized (inputQueue) {
            log.trace("added input listener");
            inputListener = listener;
        }
        return new CancelableListenerImpl();
    }

    @Override
    public Message receiveRequests(Identifier id) {
        try {
            BlockingDeque<Message> queue = requestQueues.get(id);
            if (queue == null) {
                synchronized (requestQueues) {
                    queue = requestQueues.get(id);
                    if (queue == null) {
                        queue = new LinkedBlockingDeque<>();
                        requestQueues.put(id, queue);
                    }
                }
            }
            log.trace("Receiving requests with id {}", id);
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public CancelableListener registerRequestListener(Identifier id, MessageListener listener) {
        synchronized (requestListeners) {
            log.trace("registered request listener");
            requestListeners.put(id, listener);
        }
        return new CancelableListenerImpl();
    }

    @Override
    public void publishRequest(Identifier id, final Message message) {
        BlockingDeque<Message> queue = requestQueues.get(id);
        if (queue == null) {
            synchronized (requestQueues) {
                queue = requestQueues.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<>();
                    requestQueues.put(id, queue);
                }
            }
        }
        final MessageListener listener = requestListeners.get(id);
        if (listener != null) {
            log.trace("called listener on request {} and id {}", message, id);
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    listener.onMessage(message);
                }
            });
        } else {
            log.trace("Published request {} and id {}", message, id);
            queue.add(message);
        }
    }

    @Override
    public Message receiveOutput(Identifier id) {
        try {
            BlockingDeque<Message> queue;
            synchronized (taskQueues) {
                queue = taskQueues.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<>();
                    taskQueues.put(id, queue);
                }
            }
            log.trace("Receiving task with id {}", id);
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public CancelableListener registerOutputListener(Identifier id, MessageListener listener) {
        synchronized (taskListeners) {
            log.trace("Registered task listener with id {}", id);
            taskListeners.put(id, listener);
        }
        return new CancelableListenerImpl();
    }

    @Override
    public void publishOutput(Identifier id, final Message message) {
        BlockingDeque<Message> queue = taskQueues.get(id);
        if (queue == null) {
            synchronized (taskQueues) {
                queue = taskQueues.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<>();
                    taskQueues.put(id, queue);
                }
            }
        }
        final MessageListener listener = taskListeners.get(id);
        if (listener != null) {
            log.trace("Called listener on task {} and id {}", message, id);
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    listener.onMessage(message);
                }
            });
        } else {
            log.trace("Published task {} and id {}", message, id);
            queue.add(message);
        }
    }

    @Override
    public void publishControl(Message message) {
        publishSpecial(CONTROL_QUEUE, message);
    }

    @Override
    public Message receiveControl() {
        return receiveSpecial(CONTROL_QUEUE);
    }

    @Override
    public CancelableListener registerControlListener(MessageListener listener) {
        specialListeners.put(CONTROL_QUEUE, listener);
        return new CancelableListenerImpl();
    }

    @Override
    public void publishAuthRequest(Message message) {
        publishSpecial(AUTH_QUEUE, message);
    }

    @Override
    public Message receiveAuthRequest() {
        return receiveSpecial(AUTH_QUEUE);
    }

    @Override
    public CancelableListener registerAuthListener(MessageListener listener) {
        specialListeners.put(AUTH_QUEUE, listener);
        return new CancelableListenerImpl();
    }

    @Override
    public void publishMessageInfoRequest(Message message) {
        publishSpecial(MIS_QUEUE, message);
    }

    @Override
    public Message receiveMessageInfoRequest() {
        return receiveSpecial(MIS_QUEUE);
    }

    @Override
    public CancelableListener registerMessageInfoListener(MessageListener listener) {
        specialListeners.put(MIS_QUEUE, listener);
        return new CancelableListenerImpl();
    }

    @Override
    public void publishMetricsRequest(Message message) {
        publishSpecial(MPS_QUEUE, message);
    }

    @Override
    public Message receiveMetricsRequest() {
        return receiveSpecial(MPS_QUEUE);
    }

    @Override
    public CancelableListener registerMetricsListener(MessageListener listener) {
        specialListeners.put(MPS_QUEUE, listener);
        return new CancelableListenerImpl();
    }

    @Override
    public void publishLog(Message message) {
        publishSpecial(LOG_QUEUE, message);
    }

    @Override
    public Message receiveLog() {
        return receiveSpecial(LOG_QUEUE);
    }

    @Override
    public CancelableListener registerLogListener(MessageListener listener) {
        specialListeners.put(LOG_QUEUE, listener);
        return new CancelableListenerImpl();
    }

    private void publishSpecial(String id, final Message message) {
        BlockingDeque<Message> messages = specialQueues.get(id);
        final MessageListener listener = specialListeners.get(id);
        if (listener != null) {
            log.trace("Called listener for {} on {}", id, message);
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    listener.onMessage(message);
                }
            });
        } else {
            log.trace("Published {} {}", id, message);
            messages.add(message);
        }
    }

    private Message receiveSpecial(String id) {
        log.trace("Receiving {}", id);
        return specialQueues.get(id).poll();
    }

    private class CancelableListenerImpl implements CancelableListener {

        @Override
        public void cancel() {

        }
    }
}
