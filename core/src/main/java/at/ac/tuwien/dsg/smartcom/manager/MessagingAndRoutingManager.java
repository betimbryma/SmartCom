package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessagingAndRoutingManager {

	/**
	 * Sends a message. This message is sent to a collective or a single peer. 
	 * The method returns after the peer(s) have been determined. 
	 * Errors and exceptions thereafter will be sent to the Notification Callback. 
	 * Optional receipt acknowledgments are communicated back through the Notification Callback API.

	 * @param message to send
	 * @return Returns the internal ID of the middleware to track the message within the system.
	 */
    public Identifier send(Message message);

    public Identifier addRouting(RoutingRule rule);

    public RoutingRule removeRouting(Identifier routeId);

    public void registerNotificationCallback(NotificationCallback callback);
}
