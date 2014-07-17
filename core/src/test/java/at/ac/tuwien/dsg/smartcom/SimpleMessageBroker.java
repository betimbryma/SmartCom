package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class SimpleMessageBroker implements MessageBroker {
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

    @Override
    public void publishInput(Message message) {
        synchronized (inputQueue) {
            if (inputListener == null) {
                inputQueue.add(message);
            } else {
                inputListener.onMessage(message);
            }
        }
    }

    @Override
    public Message receiveInput() {
        synchronized (inputQueue) {
            try {
                return inputQueue.take();
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    @Override
    public void registerInputListener(MessageListener listener) {
        synchronized (inputQueue) {
            inputListener = listener;
        }
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
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public void registerRequestListener(Identifier id, MessageListener listener) {
        synchronized (requestListeners) {
            requestListeners.put(id, listener);
        }
    }

    @Override
    public void publishRequest(Identifier id, Message message) {
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
        MessageListener listener = requestListeners.get(id);
        if (listener != null) {
            listener.onMessage(message);
        } else {
            queue.add(message);
        }
    }

    @Override
    public Message receiveTasks(Identifier id) {
        try {
            BlockingDeque<Message> queue;
            synchronized (taskQueues) {
                queue = taskQueues.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<>();
                    taskQueues.put(id, queue);
                }
            }
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public void registerTaskListener(Identifier id, MessageListener listener) {
        synchronized (taskListeners) {
            taskListeners.put(id, listener);
        }
    }

    @Override
    public void publishTask(Identifier id, Message message) {
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
        MessageListener listener = taskListeners.get(id);
        if (listener != null) {
            listener.onMessage(message);
        } else {
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
    public void registerControlListener(MessageListener listener) {
        specialListeners.put(CONTROL_QUEUE, listener);
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
    public void registerAuthListener(MessageListener listener) {
        specialListeners.put(AUTH_QUEUE, listener);
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
    public void registerMessageInfoListener(MessageListener listener) {
        specialListeners.put(MIS_QUEUE, listener);
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
    public void registerMetricsListener(MessageListener listener) {
        specialListeners.put(MPS_QUEUE, listener);
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
    public void registerLogListener(MessageListener listener) {
        specialListeners.put(LOG_QUEUE, listener);
    }

    private void publishSpecial(String id, Message message) {
        BlockingDeque<Message> messages = specialQueues.get(id);
        MessageListener listener = specialListeners.get(id);
        if (listener != null) {
            listener.onMessage(message);
        } else {
            messages.add(message);
        }
    }

    private Message receiveSpecial(String id) {
        return specialQueues.get(id).poll();
    }
}
