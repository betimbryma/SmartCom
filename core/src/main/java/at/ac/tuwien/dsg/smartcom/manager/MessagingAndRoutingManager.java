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

    public Identifier send(Message message);

    public Identifier addRouting(RoutingRule rule);

    public RoutingRule removeRouting(Identifier routeId);

    public void registerNotificationCallback(NotificationCallback callback);
}
