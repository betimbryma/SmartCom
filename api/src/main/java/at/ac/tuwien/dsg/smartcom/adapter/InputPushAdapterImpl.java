package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.broker.InputPublisher;
import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the InputPushAdapter that provides a method
 * to inform the system of newly arrived input. This class should be
 * implemented instead of the InputPushAdapter interface for adapters
 * that use push to communicate with external communication channels.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class InputPushAdapterImpl implements InputPushAdapter {

    private InputPublisher inputPublisher;
    private List<PushTask> taskList = new ArrayList<PushTask>(1);

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


    private TaskScheduler scheduler;

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

    @Override
    public void preDestroy() {
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
}
