package at.ac.tuwien.dsg.smartcom.adapter.util;

import at.ac.tuwien.dsg.smartcom.adapter.PushTask;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface TaskScheduler {

    /**
     * Schedule a task
     *
     * @param task the task that should be scheduled
     */
    public PushTask schedule(PushTask task);
}
