package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.broker.InputPublisher;
import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.ArrayList;
import java.util.List;

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

    protected InputPublisher inputPublisher;
    private List<PushTask> taskList = new ArrayList<>(1);

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
        for (PushTask pushTask : taskList) {
            pushTask.cancel();
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
     * push notification handler (if needed)
     */
    public abstract void init();
}
