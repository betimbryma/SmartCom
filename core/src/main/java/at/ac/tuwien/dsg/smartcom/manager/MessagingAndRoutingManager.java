/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

    public Identifier registerNotificationCallback(NotificationCallback callback);
    
    public boolean unregisterNotificationCallback(Identifier callback);
}
