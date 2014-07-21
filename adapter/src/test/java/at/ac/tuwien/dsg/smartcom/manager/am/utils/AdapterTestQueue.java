package at.ac.tuwien.dsg.smartcom.manager.am.utils;

import at.ac.tuwien.dsg.smartcom.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class AdapterTestQueue {
    private static final Logger log = LoggerFactory.getLogger(AdapterTestQueue.class);

    private AdapterTestQueue() {
    }

    private static final Map<String,BlockingDeque<Message>> blockingQueue = new HashMap<>();

    public static Message receive(String id) {
        try {
            BlockingDeque<Message> queue;
            synchronized (blockingQueue) {
                queue = blockingQueue.get(id);

                if (queue == null) {
                    queue = new LinkedBlockingDeque<>();
                    blockingQueue.put(id, queue);
                }
            }
            Message msg = queue.take();
            log.trace("Received message {}", msg);
            return msg;
        } catch (InterruptedException e) {
            return null;
        }
    }

    public static void publish(String id, Message message) {
        BlockingDeque<Message> queue;
        synchronized (blockingQueue) {
            queue = blockingQueue.get(id);

            if (queue == null) {
                queue = new LinkedBlockingDeque<>();
                blockingQueue.put(id, queue);
            }
        }
        queue.add(message);
        log.trace("Published message {}", message);
    }
}
