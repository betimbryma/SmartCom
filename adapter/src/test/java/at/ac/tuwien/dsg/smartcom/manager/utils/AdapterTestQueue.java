package at.ac.tuwien.dsg.smartcom.manager.utils;

import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class AdapterTestQueue {

    private AdapterTestQueue() {
    }

    private static final Map<String,BlockingDeque<Message>> blockingQueue = new HashMap<String,BlockingDeque<Message>>();

    public static Message receive(String id) {
        try {
            BlockingDeque<Message> queue;
            synchronized (blockingQueue) {
                queue = blockingQueue.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<Message>();
                    blockingQueue.put(id, queue);
                }
            }
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public static void publish(String id, Message message) {
        BlockingDeque<Message> queue;
        synchronized (blockingQueue) {
            queue = blockingQueue.get(id);

            if (queue == null) {
                queue = new LinkedBlockingDeque<Message>();
                blockingQueue.put(id, queue);
            }
        }
        queue.add(message);
    }
}
