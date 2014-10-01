package at.ac.tuwien.dsg.smartcom.broker.policy;

/**
 * Defines the policy on when to scale up, down or do not scale at all.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ReplicationPolicy {

    /**
     * Defines the replication policy based on the parameters passed to the method. Decides whether to scale up, down or do
     * not scale at all.
     *
     * @param messagesReceived number of messages that have been received since the last call
     * @param handlers number of handlers that are already handling the messages
     * @param messagesPending messages that are currently waiting in the queue to be processed
     * @param messagesHandled messages handled since the last call
     * @return the decision whether to scale up, down or not at all as well as the number of resources that should be added/removed
     */
    ReplicationPolicyResult determineReplicationPolicy(int messagesReceived, int handlers, int messagesPending, int messagesHandled);
}
