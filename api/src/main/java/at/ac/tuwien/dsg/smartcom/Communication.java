package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PeerAdapter;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * This is the main API for the SmartSociety Platform to interact with peers and
 * the middleware for the purpose of communication. It provides methods to start
 * the interaction with collectives and single peers and also defines methods to
 * extend and manipulate the behaviour of the middleware.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface Communication {

    /**
     * Sends a message. This message is sent to a collective or a single peer.
     * The method returns after the peer(s) have been determined. Errors and
     * exceptions thereafter will be sent to the Notification Callback. Optional
     * receipt acknowledgements are communicated back through the Notification
     * Callback API.
     *
     * @param message Specifies the message that should be handled by the middleware.
     * @return Returns the internal ID of the middleware to track the message within the system.
     * @throws CommunicationException a generic exception that will be thrown if something went wrong
     *              in the initial handling of the message.
     */
    public String send(Message message) throws CommunicationException;

    /**
     * Add a special route to the routing rules (e.g., route feedback from peer A
     * always to peer B). Returns the ID of the routing rule (can be used to delete it).
     * The middleware will check if the rule is valid and throw an exception otherwise.
     *
     * @param rule Specifies the routing rule that should be added to the routing rules of the middleware.
     * @return Returns the middleware internal ID of the rule
     * @throws InvalidRuleException if the routing rule is not valid.
     */
    public String addRouting(RoutingRule rule) throws InvalidRuleException;

    /**
     * Remove a previously defined routing rule identified by an ID.
     *
     * @param routeId The ID of the routing rule that should be removed.
     * @return The removed routing rule or nothing if there is no such rule in the system.
     */
    public RoutingRule removeRouting(String routeId);

    /**
     * Creates a feedback adapter that will wait for push notifications or will pull for updates in a
     * certain time interval. Returns the ID of the adapter.
     *
     * @param adapter Specifies the feedback push adapter.
     * @return Returns the middleware internal ID of the adapter.
     */
    public String addPushAdapter(FeedbackPushAdapter adapter);

    /**
     * Creates a feedback adapter that will pull for updates in a certain time interval.
     * Returns the ID of the adapter. The pull requests will be issued in the specified
     * interval until the adapter is explicitly removed from the system.
     *
     * @param adapter Specifies the feedback pull adapter
     * @param interval Interval in milliseconds that specifies when to issue pull requests. Can’t be zero or negative.
     * @return Returns the middleware internal ID of the adapter.
     */
    public String addPullAdapter(FeedbackPullAdapter adapter, long interval);

    /**
     * Removes a feedback adapter from the execution.
     *
     * @param adapterId The ID of the adapter that should be removed.
     * @return Returns the feedback adapter that has been removed or nothing if there is no such adapter.
     */
    public FeedbackAdapter removeFeedbackAdapter(String adapterId);

    /**
     * Registers a new type of peer adapter that can be used by the middleware to get in contact with a peer.
     * The peer adapters will be instantiated by the middleware on demand.
     *
     * @param adapter The peer adapter that can be used to contact peers.
     * @return Returns the middleware internal ID of the created adapter.
     */
    public String registerPeerAdapter(Class<PeerAdapter> adapter);

    /**
     * Removes a type of peer adapters. Adapters that are currently in use will be removed
     * as soon as possible (i.e., current communication won’t be aborted and waiting messages
     * in the adapter queue will be transmitted).
     *
     * @param adapterId Specifies the adapter that should be removed.
     */
    public void removePeerAdapter(String adapterId);
}