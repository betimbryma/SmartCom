package at.ac.tuwien.dsg.smartcom.mb;

import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class SimpleMessageBroker implements MessageBroker{

    private SimpleMessageBroker() {
    }


    private static final BlockingDeque<Message> feedbackQueue = new LinkedBlockingDeque<>();
    private static final Map<String,BlockingDeque<Message>> requestQueues = new HashMap<>();
    private static final Map<String,BlockingDeque<Message>> taskQueues = new HashMap<>();

    public static void raiseFeedback(Message message) {
        feedbackQueue.add(message);
    }

    public Message receiveFeedback() {
        try {
            return feedbackQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public Message receiveRequests(String id) {
        try {
            BlockingDeque<Message> queue;
            synchronized (requestQueues) {
                queue = requestQueues.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<Message>();
                    requestQueues.put(id, queue);
                }
            }
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void publishRequest(String id, Message message) {
        BlockingDeque<Message> queue;
        synchronized (requestQueues) {
            queue = requestQueues.get(id);

            if (queue == null) {
                queue = new LinkedBlockingDeque<Message>();
                requestQueues.put(id, queue);
            }
        }
        queue.add(message);
    }

    public Message receiveTasks(String id) {
        try {
            BlockingDeque<Message> queue;
            synchronized (taskQueues) {
                queue = taskQueues.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<Message>();
                    taskQueues.put(id, queue);
                }
            }
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void publishTask(String id, Message message) {
        BlockingDeque<Message> queue;
        synchronized (taskQueues) {
            queue = taskQueues.get(id);

            if (queue == null) {
                queue = new LinkedBlockingDeque<Message>();
                taskQueues.put(id, queue);
            }
        }
        queue.add(message);
    }
}
