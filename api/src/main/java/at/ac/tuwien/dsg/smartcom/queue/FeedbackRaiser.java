package at.ac.tuwien.dsg.smartcom.queue;

import at.ac.tuwien.dsg.smartcom.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This class will be used to raise feedback received from the push adapter.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface FeedbackRaiser {

    /**
     * Raise (inform the system) a new message that
     * has been received.
     *
     * @param message that has been received
     */
    public void raiseFeedback(Message message);
}
