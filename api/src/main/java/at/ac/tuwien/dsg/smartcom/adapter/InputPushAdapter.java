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
package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.broker.InputPublisher;
import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The Input Push Adapter API can be used to implement an adapter for a
 * communication channel that uses push to get notified of new messages. The
 * concrete implementation has to use the InputPushAdapterImpl class, which
 * provides methods that support the implementation of the adapter. The external
 * tool/peer pushes the message to the adapter, which transforms the message into
 * the internal format and calls the publishMessage of the InputPushAdapterImpl
 * class. This method delegates the message to the corresponding queue and
 * subsequently to the correct component of the system. The adapter has to
 * start a handler for the push notification (e.g., a handler that uses long
 * polling) in its init method.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class InputPushAdapter implements InputAdapter {

    public static final int TASKLIST_THRESHOLD = 20;
    protected InputPublisher inputPublisher;
    private final List<Future<?>> taskList = Collections.synchronizedList(new ArrayList<Future<?>>());

    /**
     * Publish a message that has been received. this method should only be
     * called when implementing a push service to notify the middleware that
     * there was a new message.
     *
     * @param message Message that has been received.
     */
    protected final void publishMessage(Message message) {
        inputPublisher.publishInput(message);
    }

    public final void setInputPublisher(InputPublisher inputPublisher) {
        if (this.inputPublisher == null)
            this.inputPublisher = inputPublisher;
    }


    protected TaskScheduler scheduler;

    /**
     * Schedule a push task
     *
     * @param task that should be scheduled
     */
    protected final void schedule(PushTask task) {
        taskList.add(scheduler.schedule(task));

        //remove tasks which are done after a while
        if (taskList.size() > TASKLIST_THRESHOLD) {
            scheduler.schedule(new PushTask() {
                @Override
                public void run() {
                    synchronized (taskList) {
                        List<Future<?>> remaining = new ArrayList<Future<?>>();
                        for (Future<?> pushTask : taskList) {
                            if (!pushTask.isDone()) {
                                remaining.add(pushTask);
                            }
                        }
                        taskList.clear();
                        taskList.addAll(remaining);
                    }
                }
            });
        }
    }

    public void setScheduler(TaskScheduler scheduler) {
        if (this.scheduler == null)
            this.scheduler = scheduler;
    }

    /**
     * Notifies the push adapter that it will be destroyed after the method returns.
     * Can be used to clean up and destroy handlers and so forth.
     */
    public final void preDestroy() {
        for (Future<?> pushTask : taskList) {
            pushTask.cancel(true);
        }
        cleanUp();
    }

    /**
     * clean up resources that have been used by the adapter.
     * Note that scheduled tasks have already been marked for cancellation,
     * when this method has been called.
     */
    protected abstract void cleanUp();

    /**
     * Method that can be used to initialize the adapter and other handlers like a
     * push notification handler (if needed).  E.g., to create a server socket that
     * listens for connections on a specific port.
     */
    public abstract void init();
}
