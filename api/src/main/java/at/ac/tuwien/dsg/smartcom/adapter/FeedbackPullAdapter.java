package at.ac.tuwien.dsg.smartcom.adapter;


import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * The Feedback Pull Adapter is dedicated to pull notifications from external tools or peers.
 * For example it can look at Dropbox if there is a new file available. An instance of a feedback
 * adapter is always assigned to a single task and therefore in the context of the task. It has to
 * transform the notification to an internal message. Upon registration of the feedback pull adapter
 * in the middleware the interval for pull checks has to be specified. From then on the middleware
 * will call the pull method in the specified time intervals.
 *
 * Having a stateful pull adapter has some advantages:
 * <ul>
 *  <li>the state of the communication (e.g., the corresponding execution id of feedback messages)
 *  will always be saved in the adapter and there is no need to save it in the adapter manager.</li>
 *  <li>race conditions due to the parallel execution of an adapter are not possible because each adapter
 *  is only executed by a single thread. Therefore no synchronisation has to be applied to the adapter</li>
 *  <li>the pull method does not require any parameters. Specific settings for adapters (e.g., an URL) can
 *  be set at the creation of the adapter and there is no need for a dirty parameter passing to a stateless
 *  adapter (e.g., a map of objects/strings).</li>
 * </ul>
 * This approach also has some downsides:
 * <ul>
 *     <li>feedback pull adapters have to be created in the Task Execution Engine or on higher levels
 *     (e.g., at the programming level)</li>
 *     <li>there might be a problem if too many adapters are running at the same time due to the amount of
 *     resources (i.e., memory) or required execution time. Due to the design of the Adapter Manager the
 *     Adapter Execution Engine could run on multiple machines which would eliminate or at least reduce
 *     this problem.</li>
 * </ul>
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface FeedbackPullAdapter extends FeedbackAdapter {

    /**
     * Pull data from a predefined location (only possible if the adapter supports pull). This method
     * has to be implemented by pull adapters.
     *
     * @return Returns a new message or null if there is no new information.
     * @throws AdapterException exception occurred during the pull operation.
     */
    public Message pull() throws AdapterException;
}
