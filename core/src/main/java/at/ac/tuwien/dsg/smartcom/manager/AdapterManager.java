package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PeerAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AdapterManager {

    /**
     * Initializes the adapter manager
     */
    void init();

    /**
     * Destroys the adapter manager and cleans up resources
     */
    void destroy();

    /**
     * Add a push adapter to the adapter manager
     * @param adapter the push adapter
     * @return the id of the push adapter
     */
    Identifier addPushAdapter(FeedbackPushAdapter adapter);

    /**
     * Add a pull adapter to the adapter manager.
     * @param adapter the pull adapter
     * @param period defines the period between two pull attempts
     * @return the id of the pull adapter
     */
    Identifier addPullAdapter(FeedbackPullAdapter adapter, int period);

    /**
     * Removes a feedback adapter from the execution. Note that after returning,
     * the corresponding feedback adapter won't return any feedback anymore.
     * @param adapterId the id of the adapter
     * @return the adapter that has been removed or null if there is no such adapter
     */
    FeedbackAdapter removeFeedbackAdapter(Identifier adapterId);

    /**
     * Register a new type of peer adapters in the adapter manager.
     * @param adapter new type of peer adapters
     * @return id for the peer adapter type
     */
    Identifier registerPeerAdapter(Class<? extends PeerAdapter> adapter);

    /**
     * Removes a peer adapter type and all instances from the execution. After the method
     * returned no adapter of this type will handle messages anymore.
     * @param adapterId id of the peer adapter type
     */
    void removePeerAdapter(Identifier adapterId);

    /**
     * Create a new endpoint (adapter) for a specific peer. If there is already an
     * endpoint, a corresponding routing rule will be returned. If there is no such
     * adapter available, a new one will be created (based on available contact information
     * of the peer) and a corresponding routing rule will be returned.
     * @param peerId id of the peer that requires a new endpoint
     * @return routing rule for the peer and the endpoint
     */
    public RoutingRule createEndpointForPeer(Identifier peerId);
}
