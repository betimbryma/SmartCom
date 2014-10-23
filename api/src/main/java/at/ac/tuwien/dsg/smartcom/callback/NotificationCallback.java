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
package at.ac.tuwien.dsg.smartcom.callback;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * The Notification Callback will be used to inform the different SmartSociety platform
 * components of the messages that arrived for them (e.g., to inform the Task Execution
 * Engine about task results or other task-related information (e.g., an error)).
 *
 * As multiple SmartSociety components may expose this API, the Middleware will try to
 * determine the exact recipient components by reading the message fields and checking
 * the routing rules. However, in some cases (e.g., asynchronous input messages from
 * peers) the Middleware may not be able to determine the exact recipient components, and
 * will forward the message to all the SmartSociety platform components implementing the
 * API. There, a requirement for those components is that they be capable of handling
 * (filtering) unexpected messages.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface NotificationCallback {

    /**
     * Notifies the Task Execution Engine about task results or task-relation information like an error.
     *
     * @param message the received message. As explained, the Message may contain the Execution ID or other
     *                information that allows the Middleware to route the message.
     */
    public void notify(Message message);
}
