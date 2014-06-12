package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessagingAndRoutingManager {

    public String send(Message message);

    public String addRouting(RoutingRule rule);

    public RoutingRule removeRouting(String routeId);
}
