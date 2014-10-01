package at.ac.tuwien.dsg.smartcom.broker;

/**
 * Factory that creates a replica of a specific listener. This class is used
 * by the ReplicatingMessageListener to externalise the creation of a
 * replica instance.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ReplicationFactory {

    /**
     * Create the replica of a message listener. Does not necessarily have
     * to create a new instance. E.g., stateless instances could be reused if
     * this does not prohibit a performance gain (e.g., by additional synchronisation).
     * @return (new) instance of a message listener (a so called replica)
     */
    public MessageListener createReplication();
}
