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
package at.ac.tuwien.dsg.smartcom.manager.messaging;

import at.ac.tuwien.dsg.smartcom.broker.*;
import at.ac.tuwien.dsg.smartcom.broker.policy.DynamicReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class InputHandler implements MessageListener{

    private final MessagingAndRoutingManagerImpl manager;
    private final MessageBroker broker;
    private final StatisticBean statistic;

    private ReplicatingMessageListener inputListener;
    private ReplicatingMessageListener controlListener;
    private CancelableListener cancelableInputListener;
    private CancelableListener cancelableControlListener;

    public InputHandler(MessagingAndRoutingManagerImpl manager, MessageBroker broker, StatisticBean statistic) {
        this.manager = manager;
        this.broker = broker;
        this.statistic = statistic;
    }

    public void init() {
        inputListener = new ReplicatingMessageListener("input", this, new ReplicationFactory() {
            @Override
            public MessageListener createReplication() {
                return InputHandler.this;
            }
        }, new DynamicReplicationPolicy());
        cancelableInputListener = broker.registerInputListener(inputListener);

        controlListener = new ReplicatingMessageListener("control", this, new ReplicationFactory() {
            @Override
            public MessageListener createReplication() {
                return InputHandler.this;
            }
        }, new DynamicReplicationPolicy());
        cancelableControlListener = broker.registerControlListener(controlListener);
    }

    public void destroy() {
        cancelableInputListener.cancel();
        cancelableControlListener.cancel();
        inputListener.shutdown();
        controlListener.shutdown();
    }


    @Override
    public void onMessage(Message message) {
        if (PredefinedMessageHelper.CONTROL_TYPE.equals(message.getType())) {
            statistic.controlReceived();
        } else {
            statistic.inputReceived();
        }
        manager.send(message);
    }
}
