package at.ac.tuwien.dsg.smartcom.adapter;

/**
 * Indicates that this is a pull task that will be execute in regular intervals
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class PushTask implements Runnable {

    /**
     * cancel the execution of the task
     */
    public void cancel() {

    }

}
