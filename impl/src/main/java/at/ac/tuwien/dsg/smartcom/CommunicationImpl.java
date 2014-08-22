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
package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.MessagingAndRoutingManager;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import org.picocontainer.annotations.Inject;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class CommunicationImpl implements Communication {

    @Inject
    private MessagingAndRoutingManager marManager;

    @Inject
    private AdapterManager adapterManager;

    @Override
    public Identifier send(Message message) throws CommunicationException {
        return marManager.send(message);
    }

    @Override
    public Identifier addRouting(RoutingRule rule) throws InvalidRuleException {
        return marManager.addRouting(rule);
    }

    @Override
    public RoutingRule removeRouting(Identifier routeId) {
        return marManager.removeRouting(routeId);
    }

    @Override
    public Identifier addPushAdapter(InputPushAdapter adapter) {
        return adapterManager.addPushAdapter(adapter);
    }

    @Override
    public Identifier addPullAdapter(InputPullAdapter adapter, long interval) {
        return adapterManager.addPullAdapter(adapter, interval);
    }

    @Override
    public InputAdapter removeInputAdapter(Identifier adapterId) {
        return adapterManager.removeInputAdapter(adapterId);
    }

    @Override
    public Identifier registerOutputAdapter(Class<? extends OutputAdapter> adapter) throws CommunicationException {
        return adapterManager.registerOutputAdapter(adapter);
    }

    @Override
    public void removeOutputAdapter(Identifier adapterId) {
        adapterManager.removeOutputAdapter(adapterId);
    }

    @Override
    public void registerNotificationCallback(NotificationCallback callback) {
        marManager.registerNotificationCallback(callback);
    }
}
