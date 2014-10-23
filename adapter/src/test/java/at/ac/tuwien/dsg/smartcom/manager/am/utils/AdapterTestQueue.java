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
            queue = blockingQueue.get(id);

            if (queue == null) {
                synchronized (blockingQueue) {
                    if (queue == null) {
                        queue = new LinkedBlockingDeque<>();
                        blockingQueue.put(id, queue);
                    }
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
        queue = blockingQueue.get(id);

        if (queue == null) {
            synchronized (blockingQueue) {
                if (queue == null) {
                    queue = new LinkedBlockingDeque<>();
                    blockingQueue.put(id, queue);
                }
            }
        }
        queue.add(message);
        log.trace("Published message {}", message);
    }

    public static void clear() {
        blockingQueue.clear();
    }
}
