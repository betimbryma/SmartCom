package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.queue.FeedbackPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the FeedbackPushAdapter that provides a method
 * to inform the system of newly arrived feedback. This class should be
 * implemented instead of the FeedbackPushAdapter interface for adapters
 * that use push to communicate with external communication channels.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class FeedbackPushAdapterImpl implements FeedbackPushAdapter {

    private FeedbackPublisher feedbackPublisher;
    private List<PushTask> taskList = new ArrayList<PushTask>(1);

    /**
     * Publish a message that has been received. this method should only be
     * called when implementing a push service to notify the middleware that
     * there was a new message.
     *
     * @param message Message that has been received.
     */
    protected final void publishMessage(Message message) {
        feedbackPublisher.publishFeedback(message);
    }

    public final void setFeedbackPublisher(FeedbackPublisher feedbackPublisher) {
        if (this.feedbackPublisher == null)
            this.feedbackPublisher = feedbackPublisher;
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
